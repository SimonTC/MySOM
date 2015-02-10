package dk.stcl.core.basic;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;

public abstract class SomBasics implements ISomBasics {

	protected SomMap somMap;
	protected SimpleMatrix errorMatrix; //Contains the differences between the input weights and the node weights
	protected boolean learning; //If false no learning will take place when a new input is presented
	protected SomNode bmu;
	protected int inputLength, rows, columns;
	protected SimpleMatrix inputVector;
	protected double somFitness;
	protected SimpleMatrix activationMatrix;
	
	
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
	
	public SomBasics(int mapSize, int inputLength, Random rand) {
		this(mapSize,mapSize,inputLength, rand);
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

	public void printLabelMap(){
		 for (int row = 0; row <somMap.getHeight(); row++) {
		        for (int col = 0; col < somMap.getWidth(); col++) {
		            System.out.printf("%4d", somMap.get(col, row).getLabel());
		        }
		        System.out.println();
		    }
	}
	
	public int getInputVectorLength(){
		return inputLength;
	}
	
	public SimpleMatrix getActivationMatrix(){
		return activationMatrix;
	}

	
	
}
