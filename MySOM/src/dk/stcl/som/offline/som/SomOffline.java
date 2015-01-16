package dk.stcl.som.offline.som;

import java.util.Random;
import java.util.Vector;

import org.ejml.data.MatrixIterator;
import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.SomBasics;
import dk.stcl.som.containers.SomNode;
/**
 * This implementation of SOM cannot perform unlimited online learning. LEarning rate and neighborhood radius is dependent on the current timestep
 * @author Simon
 *
 */
public class SomOffline extends SomBasics {
	
	private double initialLearningRate, initialNeighborhoodRadius;
	private double learningRate, neighborhoodRadius;
	
	/** 
	 * Creates a new SOM where all node vector values are initialized to a random value between 0 and 1
	 * Initial learning rate is set to 0.9 and initial neighborhood radius to size / 2
	 * @param columns width of the map 
	 * @param rows height of the map  
	 */
	public SomOffline(int columns, int rows, int inputLength, Random rand) {
		super(columns, rows, inputLength, rand);
		this.initialLearningRate = 0.9;
		this.initialNeighborhoodRadius = (double) columns / 2;
		this.learningRate = initialLearningRate;
		this.neighborhoodRadius = initialNeighborhoodRadius;
	}
	
	/** 
	 * Creates a new SOM where all node vector values are initialized to a random value between 0 and 1
	 * @param columns width of the map 
	 * @param rows height of the map  
	 */
	public SomOffline(int columns, int rows, int inputLength, Random rand, double initialLearningrate, double initialNeighborhoodRadius) {
		super(columns, rows, inputLength, rand);
		this.initialLearningRate = initialLearningrate;
		this.initialNeighborhoodRadius = initialNeighborhoodRadius;
		this.learningRate = initialLearningrate;
		this.neighborhoodRadius = initialNeighborhoodRadius;
	}
	
	/**
	 * Adjusts the weights of the nodes in the map
	 * @param bmu
	 * @param inputVector
	 * @param learningRate
	 * @param neighborhoodRadius
	 */
	protected void updateWeights(SomNode bmu,SimpleMatrix inputVector){
		//Calculate start and end coordinates for the weight updates
		int bmuCol = bmu.getCol();
		int bmuRow = bmu.getRow();
		int colStart = (int) (bmuCol - neighborhoodRadius);
		int rowStart = (int) (bmuRow - neighborhoodRadius );
		int colEnd = (int) (bmuCol + neighborhoodRadius);
		int rowEnd = (int) (bmuRow + neighborhoodRadius );
		
		//Make sure we don't get out of bounds errors
		if (colStart < 0) colStart = 0;
		if (rowStart < 0) rowStart = 0;
		if (colEnd > somMap.getWidth()) colEnd = somMap.getWidth();
		if (rowEnd > somMap.getHeight()) rowEnd = somMap.getHeight();
		
		//Adjust weights
		for (int col = colStart; col < colEnd; col++){
			for (int row = rowStart; row < rowEnd; row++){
				SomNode n = somMap.get(col, row);
				weightAdjustment(n, bmu, inputVector, neighborhoodRadius, learningRate);
			}
		}
	}
	
		
	/**
	 * Returns the node which vector is least different from the vector of the input node. This method also updates the internal error matrix
	 * @param input input as a somNode
	 * @return
	 */
	public SomNode findBMU(SimpleMatrix inputVector){
		SomNode BMU = null;
		double minDiff = Double.POSITIVE_INFINITY;
		for (SomNode n : somMap.getNodes()){
			double diff = n.squaredDifference(inputVector);
			if (diff < minDiff){
				minDiff = diff;
				BMU = n;
			}			
			errorMatrix.set(n.getRow(), n.getCol(), diff);
		}
		
		assert (BMU != null): "No BMU was found";

		return BMU;
	}
	
	/**
	 * Updates the weights of a single node
	 */
	public void weightAdjustment(SomNode n, SomNode bmu, SimpleMatrix inputVector, double neighborhoodRadius, double learningRate ){
		double learningEffect = neighborhoodEffect(n,bmu);
		adjustNodeWeights(n, inputVector, learningRate, learningEffect);					
		
	}
	
	/**
	 * Adjust the weights of the nodes based on the difference between the valueVectors of this node and input vector
	 * @param n node which weight vector should be adjusted
	 * @param inputVector
	 * @param learningRate
	 * @param learningEffect How effective the learning is. This is dependant on the distance to the bmu
	 */
	private void adjustNodeWeights(SomNode n, SimpleMatrix inputVector, double learningRate, double learningEffect){
		SimpleMatrix valueVector = n.getVector();
		
		//Calculate difference between input and current values
		SimpleMatrix diff = inputVector.minus(valueVector);
		
		//Multiply by learning rate and learning effect
		SimpleMatrix tmp = new SimpleMatrix(diff.numRows(), diff.numCols());
		tmp.set(learningRate * learningEffect);
		diff = diff.elementMult(tmp);
		
		//Add the dist-values to the value vector
		valueVector = valueVector.plus(diff);
		
		n.setVector(valueVector);
	}
	
	/**
	 * Calculates the learning effect based on distance to the learning center.
	 * The lower the distance, the higher the learning effect
	 * @param n
	 * @param bmu
	 * @return the learning effect. 0 if node n is outside of the neighborhood radius
	 */
	private double neighborhoodEffect(SomNode n, SomNode bmu){
		double dist = n.distanceTo(bmu);
		double squaredRadius = neighborhoodRadius * neighborhoodRadius;
		double learningEffect;
		if (dist <= squaredRadius){
			learningEffect = Math.exp(-(dist / (2 * squaredRadius)));
		} else {
			learningEffect = 0;
		}
		
		return learningEffect;
	}
	
	
	@Override
	/**
	 * Updates the learning rate and the neighborhood radius based on the current timestep.
	 * 
	 * radius is update by: sigma(t) = sigma(0) * exp (-t/lambda)
	 * 	where sigma(t) is the width of the neigborhood radius at time t and lambda is a time constant = maxTimesteps / log(sigma(0)
	 * 
	 * learning rate is updated by learning(t) = learning(0) * exp(-t/maxTimesteps)
	 * 
	 * @param timestep
	 * @param maxTimesteps
	 * @param doNeighborhood if false neighborhood radius will not be updated
	 * @param doLearningRate if false learning rate will not be updated
	 */
	public void sensitize(int timestep, int maxTimesteps, boolean doNeighborhood, boolean doLearningRate){
		
		if (doNeighborhood){
			//Update neighborhood radius
			double lambda = (double) maxTimesteps / Math.log(initialNeighborhoodRadius);		
			neighborhoodRadius = initialNeighborhoodRadius * Math.exp(-(double)timestep / lambda);
		}
		
		if (doLearningRate){
			//Update learning rate
			learningRate = initialLearningRate * Math.exp((double) -timestep / maxTimesteps);
			if (learningRate < 0.01) learningRate = 0.01;
		}
	}
	
	@Override
	public SomNode step(SimpleMatrix inputVector) {
		//Find BMU
		bmu = findBMU(inputVector);
		
		if (learning){
			//Adjust Weights
			updateWeights(bmu, inputVector);	
		}
			
		return bmu;
	}

	
	
	
	

}
