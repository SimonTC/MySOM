package dk.stcl.som;

import java.util.Random;
import java.util.Vector;

import org.ejml.data.MatrixIterator;
import org.ejml.simple.SimpleMatrix;

public class SOM extends SomBasics {
	
	/**
	 * Creates a new SOM where all node vector values are initialized to a random value between 0 and 1
	 * @param columns width of the map 
	 * @param rows height of the map 
	 */
	public SOM(int columns, int rows, int inputLength, Random rand) {
		super(columns, rows, inputLength, rand);
	}
	
	/** 
	 * Creates a new SOM where all node vector values are initialized to a random value between 0 and 1
	 * @param columns width of the map 
	 * @param rows height of the map  
	 */
	public SOM(int columns, int rows, int inputLength, Random rand, double initialLearningrate, double initialNeighborhodRadius) {
		super(columns, rows, inputLength, rand, initialLearningrate, initialNeighborhodRadius);
	}
	
	
	/**
	 * Finds the BMU to the given input node and updates the vectors of all the nodes
	 * @param inputNode
	 * @param learningRate
	 * @param neighborhoodRadius
	 * @return
	 */
	public SomNode step (SimpleMatrix inputVector, double learningRate, double neighborhoodRadius){
		//Find BMU
		bmu = getBMU(inputVector);
		
		if (learning){
			//Adjust Weights
			updateWeights(bmu, inputVector, learningRate, neighborhoodRadius);	
		}
		
		return bmu;
	}
	
	public SomNode step(double[] inputVector){
		SimpleMatrix vector = new SimpleMatrix(1, inputLength, true, inputVector);
		
		bmu = this.step(vector, this.learningRate, this.neighborhoodRadius);
		
		return bmu;
	}
	
	
		
	/**
	 * Returns the node which vector is least different from the vector of the input node. This method also updates the internal error matrix
	 * @param input input as a somNode
	 * @return
	 */
	public SomNode getBMU(SimpleMatrix inputVector){
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
		double learningEffect = learningEffect(n,bmu);
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

	
	
	
	

}
