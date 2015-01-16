package dk.stcl.som.online;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.SomBasics;
import dk.stcl.som.containers.SomNode;

/**
 * All classes implementing the SomOnline are capable of performing online learning where the learning effect is independent on the current time step
 * @author Simon
 *
 */
public abstract class SomOnline extends SomBasics {
	protected SimpleMatrix inputVector;
	protected double somFitness;
	
	public SomOnline(int columns, int rows, int inputLength, Random rand) {
		super(columns, rows, inputLength, rand);
		
	}
	
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
	
	@Override
	/**
	 * Sensitizing is not used in online learning
	 */
	public void sensitize(int timestep, int maxTimesteps, boolean doNeighborhood, boolean doLearningRate) {};

	
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
