package dk.stcl.core.rsom;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.core.som.SOM_Simple;

/**
 * This clas is an implementation of the RSOM from the LoopSOM paper
 * @author Simon
 *
 */
//TODO: Better citation
public class RSOM_Simple extends SOM_Simple implements IRSOM {
	
	private SomMap leakyDifferencesMap;
	private double decayFactor;

	public RSOM_Simple(int mapSize, int inputLength, Random rand,
			int maxIterations, double initialLearningRate,
			double activationCodingFactor, double decay) {
		super(mapSize, inputLength, rand, maxIterations, initialLearningRate,
				activationCodingFactor);
		this.decayFactor = decay;
		setupLeakyDifferences();
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
			errorMatrix.set(n.getRow(), n.getCol(), value);
			if (value < min) {
				min = value;
				//TODO: is BMU the node from the som map or from leaky differences?
				int col = n.getCol();
				int row = n.getRow();
				BMU = somMap.get(col, row);
				//BMU = n;
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
			double dist = n.squaredDistanceTo(bmu);
			//double dist = n.normDistanceTo(bmu);
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
	

}
