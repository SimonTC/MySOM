package dk.stcl.som;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomMap;
import dk.stcl.som.containers.SomNode;

public interface ISomBasics {

	/**
	 * The activation matrix is computed as 1 - (Ei / Emax)
	 * @return
	 */
	public abstract SimpleMatrix computeActivationMatrix();

	public abstract SomNode getBMU();

	public abstract SimpleMatrix getErrorMatrix();

	public abstract int getHeight();

	public abstract SomMap getSomMap();

	public abstract SomNode getNode(int id);

	public abstract SomNode getNode(int row, int col);

	public abstract SomNode[] getNodes();

	public abstract int getWidth();

	public abstract boolean getLearning();

	public abstract void setLearning(boolean learning);

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

	/**
	 * Updates both the learning rate and the neighborhood radius based on the current timestep.
	 * @param iteration
	 * @param maxIterations
	 */
	public abstract void sensitize(int timestep, int maxTimesteps);

	/**
	 * Updates the learning rate and the neighborhood radius based on the current timestep.
	 * @param timestep
	 * @param maxTimesteps
	 * @param doNeighborhood if false neighborhood radius will not be updated
	 * @param doLearningRate if false learning rate will not be updated
	 */
	public abstract void sensitize(int timestep, int maxTimesteps,
			boolean doNeighborhood, boolean doLearningRate);

}