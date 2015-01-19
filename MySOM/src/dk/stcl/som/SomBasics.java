package dk.stcl.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomMap;
import dk.stcl.som.containers.SomNode;

public abstract class SomBasics implements ISomBasics {

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
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#computeActivationMatrix()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getBMU()
	 */
	@Override
	public SomNode getBMU(){
		return bmu;
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getErrorMatrix()
	 */
	@Override
	public SimpleMatrix getErrorMatrix(){
		return errorMatrix;
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getHeight()
	 */
	@Override
	public int getHeight(){
		return somMap.getHeight();
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getSomMap()
	 */
	@Override
	public SomMap getSomMap(){
		return somMap;
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getNode(int)
	 */
	@Override
	public SomNode getNode(int id){
		return somMap.get(id);
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getNode(int, int)
	 */
	@Override
	public SomNode getNode(int row, int col){
		return somMap.get(col, row);
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getNodes()
	 */
	@Override
	public SomNode[] getNodes(){
		return somMap.getNodes();
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getWidth()
	 */
	@Override
	public int getWidth(){
		return somMap.getWidth();
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#getLearning()
	 */
	@Override
	public boolean getLearning(){
		return learning;
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#setLearning(boolean)
	 */
	@Override
	public void setLearning(boolean learning){
		this.learning = learning;
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#setNode(dk.stcl.som.containers.SomNode, int, int)
	 */
	@Override
	public void setNode(SomNode n, int row, int column){
		somMap.set(column, row, n);
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#step(double[])
	 */
	@Override
	public SomNode step(double[] inputVector){
		SimpleMatrix vector = new SimpleMatrix(1, inputLength, true, inputVector);
		
		bmu = this.step(vector);
		
		return bmu;
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#step(org.ejml.simple.SimpleMatrix)
	 */
	@Override
	public abstract SomNode step (SimpleMatrix inputVector);
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#findBMU(org.ejml.simple.SimpleMatrix)
	 */
	@Override
	public abstract SomNode findBMU(SimpleMatrix inputVector);
	
	/**
	 * Updates the value weights of the nodes
	 * @param bmu
	 * @param inputVector
	 */
	protected abstract void updateWeights(SomNode bmu, SimpleMatrix inputVector);
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#sensitize(int, int)
	 */
	@Override
	public void sensitize(int timestep, int maxTimesteps){
		this.sensitize(timestep, maxTimesteps, true, true);
	}
	
	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#sensitize(int, int, boolean, boolean)
	 */
	@Override
	public abstract void sensitize(int timestep, int maxTimesteps, boolean doNeighborhood, boolean doLearningRate);
	

	
}
