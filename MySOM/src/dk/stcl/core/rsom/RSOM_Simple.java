package dk.stcl.core.rsom;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;

/**
 * This clas is an implementation of the RSOM from the LoopSOM paper
 * @author Simon
 *
 */
//TODO: Better citation
public class RSOM_Simple extends SomBasics implements IRSOM {
	
	private SomMap leakyDifferencesMap;
	private double decayFactor;
	private int maxIterations;
	private double mapRadius;
	private double timeConstant;
	private double initialNeighborHoodRadius, curNeighborhoodRadius;
	private double initialLearningRate, curLearningRate;
	private double activationCodingFactor;

	public RSOM_Simple(int mapSize, int inputLength, Random rand, double initialLearningRate,
			double activationCodingFactor, int maxIterations, double decay) {
		
		super(mapSize, inputLength, rand);
		
		this.decayFactor = decay;
		this.mapRadius = (double) mapSize / 2;
		updateMaxIterations(maxIterations);
		initialNeighborHoodRadius = mapRadius;
		curNeighborhoodRadius = initialNeighborHoodRadius;
		this.initialLearningRate = initialLearningRate;
		this.curLearningRate = initialLearningRate;
		this.activationCodingFactor = activationCodingFactor;
		
		setupLeakyDifferences();
	}
	
	public void updateMaxIterations(int maxIterations){
		this.maxIterations = maxIterations;
		this.timeConstant = maxIterations / mapRadius;
	}
	
	
	private void setupLeakyDifferences(){
		leakyDifferencesMap = new SomMap(columns, rows, inputLength);
	}
	

	@Override
	/**
	 * 
	 * @param inputVector
	 * @return
	 */
	public SomNode findBMU(SimpleMatrix inputVector) {		
		updateLeakyDifferences(inputVector);
		
		double min = Double.POSITIVE_INFINITY;
		SomNode[] leakyNodes = leakyDifferencesMap.getNodes();
		SomNode BMU = null;
		for (SomNode n : leakyNodes){
			double value = n.getVector().normF();
			double error = Math.pow(value, 2);
			error = error / inputLength;
			errorMatrix.set(n.getRow(), n.getCol(), error);
			if (value < min) {
				min = value;
				int col = n.getCol();
				int row = n.getRow();
				BMU = somMap.get(col, row);
			}
		}		
		
		assert (BMU != null): "No BMU was found";

		return BMU;
	}
	
	/**
	 * Updates the vector values of the nodes in the leaky differences map
	 * @param inputVector
	 * @param leakyCoefficient
	 */
	private void updateLeakyDifferences(SimpleMatrix inputVector){
		for (int row = 0; row < leakyDifferencesMap.getHeight(); row++){
			for (int col = 0; col < leakyDifferencesMap.getWidth(); col++){
				SomNode leakyDifferenceNode = leakyDifferencesMap.get(col, row);
				SomNode weightNode = somMap.get(col, row);
				
				//Calculate difference between input vector and weight vector
				SimpleMatrix weightVector = weightNode.getVector();
				SimpleMatrix weightDiff = inputVector.minus(weightVector);
				weightDiff = weightDiff.scale(decayFactor);
				
				SimpleMatrix leakyDifferenceVector = leakyDifferenceNode.getVector();
				leakyDifferenceVector = leakyDifferenceVector.scale(1-decayFactor);
								
				SimpleMatrix sum = leakyDifferenceVector.plus(weightDiff);
				
				leakyDifferenceNode.setVector(sum);
				leakyDifferencesMap.set(col, row, leakyDifferenceNode);
			}
		}
	}
	
	@Override
	protected void updateWeights(SimpleMatrix inputVector){
		for (SomNode n : somMap.getNodes()){
			//double dist = n.squaredDistanceTo(bmu);
			double dist = n.normDistanceTo(bmu);
			if (dist <= curNeighborhoodRadius){
				
				SimpleMatrix weightVector = n.getVector();
				
				SomNode leakyDifferenceNode = leakyDifferencesMap.get(n.getCol(), n.getRow());
				
				double neighborhoodEffect = calculateNeighborhoodEffect(n, bmu);
				SimpleMatrix delta = leakyDifferenceNode.getVector().scale(curLearningRate * neighborhoodEffect);
				
				weightVector = weightVector.plus(delta);
				n.setVector(weightVector);
			}
		}
	}
	
	/**
	 * Resets the leaky difference vector. 
	 * Used between sequences when training
	 */
	public void flush(){
		setupLeakyDifferences();
	}
	
	public SomMap getLeakyDifferencesMap(){
		return leakyDifferencesMap;
	}
	
	@Override
	public void adjustNeighborhoodRadius(int iteration){
		curNeighborhoodRadius = mapRadius * Math.exp(-(double) iteration / timeConstant);	
	}
	
	@Override
	public void adjustLearningRate(int iteration){
		curLearningRate = initialLearningRate * Math.exp(-(double) iteration / maxIterations);
	}
	
	protected double calculateNeighborhoodEffect(SomNode n, SomNode bmu){
		double dist = Math.pow(n.normDistanceTo(bmu),2);
		double bottom = 2 * Math.pow(curNeighborhoodRadius, 2);
		double effect =  Math.exp(- dist / bottom);
		return effect;
	}


	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#computeActivationMatrix()
	 */
	@Override
	public SimpleMatrix computeActivationMatrix(){
		SimpleMatrix activation = errorMatrix.elementPower(2);
		activation = activation.divide(-2 * Math.pow(activationCodingFactor, 2));	 
		activation = activation.elementExp();		
		activationMatrix = activation;
		return activation;
	}


	@Override
	public double calculateSOMFitness() {
		double fitness = 1 - errorMatrix.get(bmu.getRow(), bmu.getCol());
		return fitness;
	}

	
	

}
