package dk.stcl.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomMap;
import dk.stcl.som.containers.SomNode;

public abstract class SomBasics {

	protected SomMap somMap;
	protected SimpleMatrix errorMatrix; //Contains the differences between the input weights and the node weights
	protected boolean learning; //If false no learning will take place when a new input is presented
	protected SomNode bmu;
	protected int inputLength, rows, columns;
	
	/**
	 * 
	 * @param columns number of columns in the internal weightMap
	 * @param rows number of rows in the internal weight map
	 * @param inputLength length of the value vectors
	 * @param rand
	 */
	public SomBasics(int columns, int rows, int inputLength, Random rand) {
		this.inputLength = inputLength;
		this.rows = rows;
		this.columns = columns;
		somMap = new SomMap(columns, rows, inputLength, rand);
		errorMatrix = new SimpleMatrix(rows, columns);
		learning = true;
		
	}	
	
	/**
	 * The activation matrix is computed as 1 - (Ei / Emax)
	 * @return
	 */
	public SimpleMatrix computeActivationMatrix(){
		double maxError = errorMatrix.elementMaxAbs();
		SimpleMatrix m;
		if (maxError == 0){
			m = errorMatrix;
		} else {
			m = errorMatrix.divide(maxError);
		}		 
		SimpleMatrix activation = new SimpleMatrix(errorMatrix.numRows(), errorMatrix.numCols());
		activation.set(1);
		activation = activation.minus(m);	
		return activation;
	}
	
	public SomNode getBMU(){
		return bmu;
	}
	
	public SimpleMatrix getErrorMatrix(){
		return errorMatrix;
	}
	
	public int getHeight(){
		return somMap.getHeight();
	}
	
	public SomMap getSomMap(){
		return somMap;
	}
	
	public SomNode getNode(int id){
		return somMap.get(id);
	}
	
	public SomNode getNode(int row, int col){
		return somMap.get(col, row);
	}
	
	public SomNode[] getNodes(){
		return somMap.getNodes();
	}
	
	public int getWidth(){
		return somMap.getWidth();
	}
	
	public boolean getLearning(){
		return learning;
	}
	
	public void setLearning(boolean learning){
		this.learning = learning;
	}
	
	/**
	 * Place the node n at coordinate (row,column) in the som map
	 * @param n
	 * @param row
	 * @param column
	 */
	public void setNode(SomNode n, int row, int column){
		somMap.set(column, row, n);
	}
	
	public SomNode step(double[] inputVector){
		SimpleMatrix vector = new SimpleMatrix(1, inputLength, true, inputVector);
		
		bmu = this.step(vector);
		
		return bmu;
	}
	
	/**
	 * Finds the BMU to the given input node and updates the vectors of all the nodes
	 * @param inputNode
	 * @param learningRate
	 * @param neighborhoodRadius
	 * @return
	 */
	public abstract SomNode step (SimpleMatrix inputVector);
	
	/**
	 * Finds the best matching unit
	 * @param inputVector
	 * @return
	 */
	public abstract SomNode findBMU(SimpleMatrix inputVector);
	
	/**
	 * Updates the value weights of the nodes
	 * @param bmu
	 * @param inputVector
	 */
	protected abstract void updateWeights(SomNode bmu, SimpleMatrix inputVector);
	
	/**
	 * Updates both the learning rate and the neighborhood radius based on the current timestep.
	 * @param iteration
	 * @param maxIterations
	 */
	public void sensitize(int timestep, int maxTimesteps){
		this.sensitize(timestep, maxTimesteps, true, true);
	}
	
	/**
	 * Updates the learning rate and the neighborhood radius based on the current timestep.
	 * @param timestep
	 * @param maxTimesteps
	 * @param doNeighborhood if false neighborhood radius will not be updated
	 * @param doLearningRate if false learning rate will not be updated
	 */
	public abstract void sensitize(int timestep, int maxTimesteps, boolean doNeighborhood, boolean doLearningRate);
	

	
}
