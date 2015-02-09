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
	 * @see dk.stcl.som.ISomBasics#findBMU(org.ejml.simple.SimpleMatrix)
	 */
	@Override
	public abstract SomNode findBMU(SimpleMatrix inputVector);
	
	/**
	 * Updates the value weights of the nodes
	 * @param bmu
	 * @param inputVector
	 */
	protected void updateWeights(SomNode bmu, SimpleMatrix inputVector){
		double maxRadius = calculateMaxRadius(bmu, inputVector, 0.01);
		
		double learningRate = calculateLearningRate(bmu, inputVector);
		
		//Calculate start and end coordinates for the weight updates
		int bmuCol = bmu.getCol();
		int bmuRow = bmu.getRow();
		int colStart = (int) (bmuCol - maxRadius);
		int rowStart = (int) (bmuRow - maxRadius );
		int colEnd = (int) (bmuCol + maxRadius);
		int rowEnd = (int) (bmuRow + maxRadius );
		
		//Make sure we don't get out of bounds errors
		if (colStart < 0) colStart = 0;
		if (rowStart < 0) rowStart = 0;
		if (colEnd > somMap.getWidth()) colEnd = somMap.getWidth();
		if (rowEnd > somMap.getHeight()) rowEnd = somMap.getHeight();
		
		//Adjust weights
		for (int col = colStart; col < colEnd; col++){
			for (int row = rowStart; row < rowEnd; row++){
				SomNode n = somMap.get(col, row);
				double neighborhoodEffect = calculateNeighborhoodEffect(bmu, n);
				adjustNodeWeights(n, neighborhoodEffect, learningRate, somFitness);
			}
		}

	}
	@Override
	public SomNode step(SimpleMatrix inputVector) {
		
		this.inputVector = inputVector;
		
		//Find BMU
		bmu = findBMU(inputVector);
		
		//Calculate fitness of SOM
		somFitness = calculateSomFitness(bmu, inputVector);
		
		if (learning){
			//Adjust Weights
			updateWeights(bmu, inputVector);	
		}
			
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
	
	/**
	 * This method is used in optimization of the code. 
	 * Max radius is the radius of the circle with the BMU as centrum in which weight updates are performed
	 * @param bmu
	 * @param inputVector
	 * @param minimumLearningEffect The radius will be calculated such that the learning effect on nodes within the radius will be at least this value
	 * @return
	 */
	protected abstract double calculateMaxRadius(SomNode bmu, SimpleMatrix inputVector, double minimumLearningEffect);
	
	/**
	 * Calculates the learning effect on node n
	 * @param bmu
	 * @param n
	 * @return
	 */
	protected abstract double calculateNeighborhoodEffect(SomNode bmu, SomNode n);
	
	
	/**
	 * Calculates the learning rate
	 * @param bmu
	 * @param inputVector
	 * @return
	 */
	protected abstract double calculateLearningRate(SomNode bmu, SimpleMatrix inputVector);
	
	/**
	 * Updates the weights of node n
	 * @param n
	 * @param neighborhoodEffect
	 * @param learningRate
	 */
	protected abstract void adjustNodeWeights(SomNode n, double neighborhoodEffect, double learningRate, double somFitness);
	
	/**
	 * Calculates the fitness if the SOM.
	 * This is normally how good a fit the BMU is to the input vector
	 * @param BMU
	 * @param inputVector
	 * @return
	 */
	protected abstract double calculateSomFitness(SomNode bmu, SimpleMatrix inputVector);	

	
}
