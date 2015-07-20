package dk.stcl.core.basic;

import java.io.Serializable;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.utils.SomConstants;

public abstract class SomBasics implements ISomBasics, Serializable {

	private static final long serialVersionUID = 1L;
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
		activationMatrix = new SimpleMatrix(rows, columns);
		
	}		
	
	public SomBasics(int mapSize, int inputLength, Random rand) {
		this(mapSize,mapSize,inputLength, rand);
	}
	
	public SomBasics(String initializationString, int startLine){
		String[] lines = initializationString.split(SomConstants.LINE_SEPARATOR);
		String[] somInfo = lines[startLine].split(" ");
		inputLength = Integer.parseInt(somInfo[0]);
		rows = Integer.parseInt(somInfo[1]);
		columns = Integer.parseInt(somInfo[2]);
		int startID = 0;
		for (int i = 0; i <= startLine + 1; i++){
			startID += lines[i].length();
		}
		startID += startLine; //Beed to consider the /n that is coming after each line
		int endID = initializationString.indexOf("TEMPORAL");
		if (endID == -1 || endID < startID) endID = initializationString.length();
		String somMapDescription = initializationString.substring(startID, endID);
		somMap = new SomMap(somMapDescription);
		errorMatrix = new SimpleMatrix(rows, columns);
		learning = true;
		activationMatrix = new SimpleMatrix(rows, columns);
	}
	

	public SomNode step(SimpleMatrix inputVector){
		//Save input vector
		this.inputVector = inputVector;
		
		//Find BMU
		bmu = findBMU(inputVector);
		
		//Calculate SOM fitness
		somFitness = calculateSOMFitness();
		
		//Update weights
		if (learning) updateWeights(inputVector);
		
		return bmu;
	}
	
	public String toFileString(){
		String s = inputLength + " " + rows + " " + columns + SomConstants.LINE_SEPARATOR;
		s += somMap.toFileString();
		return s;
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
	
	@Override
	public void sensitize(int iteration) {
		adjustLearningRate(iteration);
		adjustNeighborhoodRadius(iteration);		
	}	
	
	protected abstract void updateWeights(SimpleMatrix inputVector);

	
	
}
