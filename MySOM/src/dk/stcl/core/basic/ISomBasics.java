package dk.stcl.core.basic;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;

public interface ISomBasics {

	/**
	 * Computes the activation matrix of the SOM
	 * The actual activation computation is dependent on the SOM implementation
	 * @return
	 */
	public abstract SimpleMatrix computeActivationMatrix();

	/**
	 * Place the node n at coordinate (row,column) in the som map
	 * @param n
	 * @param row
	 * @param column
	 */
	public abstract void setNode(SomNode n, int row, int column);

	public abstract SomNode step(double[] inputVector);

	/**
	 * Finds the BMU to the given input node and updates the vectors of all the nodes
	 * @param inputNode
	 * @param learningRate
	 * @param neighborhoodRadius
	 * @return
	 */
	public abstract SomNode step(SimpleMatrix inputVector);

	/**
	 * Finds the best matching unit
	 * @param inputVector
	 * @return
	 */
	public abstract SomNode findBMU(SimpleMatrix inputVector);
	
	public abstract void adjustLearningRate(int iteration);
	
	public abstract void adjustNeighborhoodRadius(int iteration);
	
	public void printLabelMap();
	
	public double calculateSOMFitness();
	
	/**
	 * Collects the best matching unit. Can be called after the step() function has been called.
	 * @return 
	 */
	public abstract SomNode getBMU();

	public abstract SimpleMatrix getErrorMatrix();

	public abstract int getHeight();

	public abstract SomMap getSomMap();

	public abstract SomNode getNode(int id);

	public abstract SomNode getNode(int row, int col);

	public abstract SomNode[] getNodes();
	
	public SimpleMatrix getActivationMatrix();

	public abstract int getWidth();
	
	public int getInputVectorLength();

	public abstract void setLearning(boolean learning);

	public abstract boolean getLearning();
	
	/**
	 * Adjusts the learning rate and neighborhood radius of the SOM
	 * @param iteration
	 * @param maxIterations
	 */
	public void sensitize(int iteration);


}