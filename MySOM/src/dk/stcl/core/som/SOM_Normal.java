package dk.stcl.core.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomNode;

public class SOM_Normal extends SomBasics implements ISOM {
	double initialLearningRate, learningRate;
	double initialNeighborhoodRadius, neighborhoodRadius;
	double activationCodingFactor;
	
	public SOM_Normal(int size, int inputLength, Random rand, double initialLearningRate, double activationCodingFactor) {
		super(size, inputLength, rand);
		this.initialLearningRate = initialLearningRate;
		this.learningRate = initialLearningRate;
		this.initialNeighborhoodRadius = Math.max(columns, rows) / 2.0;
		this.neighborhoodRadius = initialLearningRate;
		this.activationCodingFactor = activationCodingFactor;
	}


	@Override
	public SomNode step(SimpleMatrix inputVector) {
		
		this.inputVector = inputVector;
		
		//Find BMU
		bmu = findBMU(inputVector);
		
		if (learning){
			//Adjust Weights
			updateWeights(bmu, inputVector);	
		}
			
		return bmu;
	}
	
	@Override
	public SomNode findBMU(SimpleMatrix inputVector){
		SomNode BMU = null;
		double minDiff = Double.POSITIVE_INFINITY;
		for (SomNode n : somMap.getNodes()){
			SimpleMatrix weightVector = n.getVector();
			SimpleMatrix diffVector = inputVector.minus(weightVector);
			double error = Math.pow(diffVector.normF(), 2);
			double diff = error / inputLength;
			if (diff < minDiff){
				minDiff = diff;
				BMU = n;
			}			
			errorMatrix.set(n.getRow(), n.getCol(), diff);
		}
		
		assert (BMU != null): "No BMU was found";
		
		//errorMatrix.print();

		return BMU;
	}

	/**
	 * Updates the value weights of the nodes
	 * @param bmu
	 * @param inputVector
	 */
	private void updateWeights(SomNode bmu, SimpleMatrix inputVector){
		
		//Calculate start and end coordinates for the weight updates
		int bmuCol = bmu.getCol();
		int bmuRow = bmu.getRow();
		int colStart = (int) (bmuCol - neighborhoodRadius) ;
		int rowStart = (int) (bmuRow - neighborhoodRadius) ;
		int colEnd = (int) (bmuCol + neighborhoodRadius) ;
		int rowEnd = (int) (bmuRow + neighborhoodRadius) ;
		
		//Make sure we don't get out of bounds errors
		if (colStart < 0) colStart = 0;
		if (rowStart < 0) rowStart = 0;
		if (colEnd > somMap.getWidth()) colEnd = somMap.getWidth();
		if (rowEnd > somMap.getHeight()) rowEnd = somMap.getHeight();
		
		SimpleMatrix effect = new SimpleMatrix(somMap.getHeight(), somMap.getWidth());
		effect.set(-1);
		/*
		System.out.println("BMU row " + bmuRow + " BMU col " + bmuCol);
		System.out.println("Col start " + colStart + " Col end " + colEnd);
		System.out.println("Row start " + rowStart + " Row end " + rowEnd);
		*/
		//Adjust weights
		for (int col = 0; col < somMap.getWidth(); col++){
			for (int row = 0; row < somMap.getHeight(); row++){
				//System.out.println("Col: " + col + " Row: " + row);
				SomNode n = somMap.get(col, row);
				double neighborhoodEffect = calculateNeighborhoodEffect(bmu, n);
				effect.set(row, col, neighborhoodEffect);
				adjustNodeWeights(n, neighborhoodEffect, learningRate);
			}
		}
		/*
		for (int col = colStart; col < colEnd; col++){
			for (int row = rowStart; row < rowEnd; row++){
				System.out.println("Col: " + col + " Row: " + row);
				SomNode n = somMap.get(col, row);
				double neighborhoodEffect = calculateNeighborhoodEffect(bmu, n);
				effect.set(row, col, neighborhoodEffect);
				adjustNodeWeights(n, neighborhoodEffect, learningRate);
			}
		}*/
		//effect.print();
		//System.out.println();
	}
	
	/**
	 * Calculates the neighborhood effect on node n
	 * @param bmu
	 * @param n
	 * @return
	 */
	private double calculateNeighborhoodEffect(SomNode bmu, SomNode n){
		double squaredDist =  Math.pow(n.normDistanceTo(bmu),2);
		double effect = Math.exp(- squaredDist / (2 * Math.pow(neighborhoodRadius, 2)));
		return effect;
	}

	private void adjustNodeWeights(SomNode n, double neighborhoodEffect,
			double learningRate) {
		
		SimpleMatrix valueVector = n.getVector();
		
		//Calculate difference between input and current values
		SimpleMatrix diff = inputVector.minus(valueVector);
		
		//Multiply by learning rate and neighborhood effect
		double effect = learningRate * neighborhoodEffect;
		SimpleMatrix delta = diff.scale(effect);
		
		//Add the diff-values to the value vector
		valueVector = valueVector.plus(delta);
		
		n.setVector(valueVector);
	}

	/* (non-Javadoc)
	 * @see dk.stcl.som.ISomBasics#computeActivationMatrix()
	 */
	@Override
	public SimpleMatrix computeActivationMatrix(){
		SimpleMatrix activation = errorMatrix.elementPower(2);
		activation = activation.divide(-2 * Math.pow(activationCodingFactor, 2));	 
		activation = activation.elementExp();		
		activationMatrix = activation;
		return activation;
	}


	@Override
	public double calculateSOMFitness() {
		double fitness = 1 - errorMatrix.get(bmu.getRow(), bmu.getCol());
		return fitness;
	}


	@Override
	public void sensitize(int iteration, int maxIterations) {
		adjustLearningRate(iteration, maxIterations);
		adjustNeighborhoodRadius(iteration, maxIterations);
		
		//System.out.println("Iteration " + iteration + " LR: " + learningRate + " NR: " + neighborhoodRadius);
		
	}


	@Override
	public void adjustLearningRate(int iteration, int maxIterations) {
		learningRate = initialLearningRate * Math.exp(-(double) iteration / maxIterations);

	}

	@Override
	public void adjustNeighborhoodRadius(int iteration, int maxIterations) {
		double timeConstant = maxIterations / Math.log(initialNeighborhoodRadius);
		neighborhoodRadius = initialNeighborhoodRadius * Math.exp(-(double) iteration / timeConstant);
	}
	



}
