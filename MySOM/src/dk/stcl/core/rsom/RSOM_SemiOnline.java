package dk.stcl.core.rsom;

import java.io.Serializable;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.utils.SomConstants;

/**
 * This class is an implementation of the RSOM from:
 * R. Pinto and P. Engel, “LoopSOM: a robust som variant using self-organizing temporal feedback connections,” Proc. 7th ENIA-BRAZILIAN …, 2009.
 * @author Simon
 *
 */
public class RSOM_SemiOnline extends SomBasics implements IRSOM {
	private static final long serialVersionUID = 1L;
	private SomMap leakyDifferencesMap;
	private double decayFactor;
	private double learningRate;
	protected double stddev;
	private double activationCodingFactor;
	
	public RSOM_SemiOnline(int mapSize, int inputLength, Random rand,
			double learningRate, double activationCodingFactor, double stddev, double decayFactor) {			
		super(mapSize, inputLength, rand);
		this.learningRate = learningRate;
		this.stddev = stddev;
		this.activationCodingFactor = activationCodingFactor;
		this.decayFactor = decayFactor;
		setupLeakyDifferences();
	}
	
	public RSOM_SemiOnline(int mapSize, int inputLength, 
			double learningRate, double activationCodingFactor, double stddev, double decayFactor) {
			this(mapSize, inputLength, null, learningRate, activationCodingFactor, stddev, decayFactor);
	}
	
	public RSOM_SemiOnline(String s, int startLine){
		super(s, startLine + 1);
		String[] lines = s.split(SomConstants.LINE_SEPARATOR);
		String[] somInfo = lines[startLine].split(" ");
		decayFactor = Double.parseDouble(somInfo[0]);
		learningRate = Double.parseDouble(somInfo[1]);
		stddev = Double.parseDouble(somInfo[2]);
		activationCodingFactor = Double.parseDouble(somInfo[3]);
		setupLeakyDifferences();
	}
	
	private void setupLeakyDifferences(){
		leakyDifferencesMap = new SomMap(columns, rows, inputLength);
	}
	
	@Override
	public String toInitializationString(){
		String s = super.toInitializationString();
		String info = decayFactor + " " + learningRate + " " + stddev + " " + activationCodingFactor + SomConstants.LINE_SEPARATOR;
		return info + s;
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
			double avgError = error / inputLength;
			errorMatrix.set(n.getRow(), n.getCol(), avgError);
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
	protected void updateWeights(SimpleMatrix inputVector) {
		for (SomNode n : somMap.getNodes()){
			double neighborhoodEffect = calculateNeighborhoodEffect(n, bmu);
			adjustNodeWeights(n, neighborhoodEffect, learningRate, somFitness);
		}		
	}
	
	protected double calculateNeighborhoodEffect(SomNode bmu, SomNode n) {		
		//double dist = bmu.distanceTo(n);
		double dist = Math.pow(bmu.normDistanceTo(n),2);
		double error = 1 - somFitness;
		double effect;
		if (error == 0){
			effect = 0;
		} else {
			effect = Math.exp(-dist / (error * Math.pow(stddev, 2)));
		}
		
		return effect;
	}

	
	/**
	 * Adjust the weights of the nodes based on the difference between the valueVectors of this node and input vector
	 * @param n node which weight vector should be adjusted
	 * @param inputVector
	 * @param learningRate
	 * @param learningEffect How effective the learning is. This is dependant on the distance to the bmu
	 */
	protected void adjustNodeWeights(SomNode n, double neighborhoodEffect,
			double learningRate, double somFitness){
		SimpleMatrix weightVector = n.getVector();
		
		SomNode leakyDifferenceNode = leakyDifferencesMap.get(n.getCol(), n.getRow());
		
		double learningEffect = neighborhoodEffect * learningRate;
		
		SimpleMatrix delta = leakyDifferenceNode.getVector().scale(learningEffect);
		
		weightVector = weightVector.plus(delta);
		n.setVector(weightVector);
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
		SimpleMatrix bmuVector = bmu.getVector();
		SimpleMatrix diff = bmuVector.minus(inputVector);
		double error = Math.pow(diff.normF(), 2);
		double avgError = error / inputLength;
		return 1 - avgError;
		
	}
	
	@Override
	public void adjustLearningRate(int iteration) {
		// Learning rate is not adjusted in the semi-onlineSOM
		
	}

	@Override
	public void adjustNeighborhoodRadius(int iteration) {
		//Neighborhood radius is not adjusted by way of time in the semi-online SOM
		
	}
	

}
