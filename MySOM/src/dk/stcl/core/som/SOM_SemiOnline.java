package dk.stcl.core.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.CopyOfSomBasics;
import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomNode;

/**
 * This implementation off an online som is based on the description in the LoopSom paper
 * @author Simon
 *
 */
//TODO: Better citation
public class SOM_SemiOnline extends SomBasics implements ISOM {
	
	private double learningRate;
	protected double stddev; //TODO: Give a good real name
	private double activationCodingFactor;
	
	public SOM_SemiOnline(int columns, int rows, int inputLength, Random rand, double learningRate, double stddev, double activationCodingFactor) {
		super(columns, rows, inputLength, rand);
		this.learningRate = learningRate;
		this.stddev = stddev;
		this.activationCodingFactor = activationCodingFactor;
	}
	
	public SOM_SemiOnline(int mapSize, int inputLength, Random rand, double learningRate, double stddev, double activationCodingFactor) {
		this(mapSize, mapSize, inputLength, rand, learningRate, stddev, activationCodingFactor);
	}
	
	

	@Override
	protected void updateWeights(SimpleMatrix inputVector) {
		for (SomNode n : somMap.getNodes()){
			double neighborhoodEffect = calculateNeighborhoodEffect(n, bmu);
			adjustNodeWeights(n, neighborhoodEffect, learningRate, somFitness);
		}		
	}
	
	

	protected double calculateNeighborhoodEffect(SomNode bmu, SomNode n) {		
		//double dist = bmu.distanceTo(n);
		double dist = Math.pow(bmu.normDistanceTo(n),2);
		double error = 1 - somFitness;
		double effect;
		if (error == 0){
			effect = 0;
		} else {
			effect = Math.exp(-dist / (error * Math.pow(stddev, 2)));
		}
		
		return effect;
	}


	protected void adjustNodeWeights(SomNode n, double neighborhoodEffect,
			double learningRate, double somFitness) {
		SimpleMatrix valueVector = n.getVector();
		
		//Calculate difference between input and current values
		SimpleMatrix diff = inputVector.minus(valueVector);
		
		//Multiply by som fitness and neighborhood effect
		double learningEffect = neighborhoodEffect * learningRate;
		
		SimpleMatrix delta = diff.scale(learningEffect);
		
		SimpleMatrix newVector = valueVector.plus(delta);
		n.setVector(newVector);

	}


	@Override
	public SomNode findBMU(SimpleMatrix inputVector){
		SomNode BMU = null;
		double minDiff = Double.POSITIVE_INFINITY;
		for (SomNode n : somMap.getNodes()){
			SimpleMatrix weightVector = n.getVector();
			SimpleMatrix diffVector = inputVector.minus(weightVector);
			double error = Math.pow(diffVector.normF(), 2);
			double avgError = error / inputLength;
			SimpleMatrix avgDiffVector = diffVector.scale(1 / weightVector.numCols());
			double diff = avgError; //diffVector.normF();
			if (diff < minDiff){
				minDiff = diff;
				BMU = n;
			}			
			errorMatrix.set(n.getRow(), n.getCol(), diff);
		}
		
		assert (BMU != null): "No BMU was found";

		return BMU;
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
		SimpleMatrix bmuVector = bmu.getVector();
		SimpleMatrix diff = bmuVector.minus(inputVector);
		double error = Math.pow(diff.normF(), 2);
		double avgError = error / inputLength;
		return 1 - avgError;
		
	}
	
	@Override
	public void adjustLearningRate(int iteration) {
		// Learning rate is not adjusted in the semi-onlineSOM
		
	}

	@Override
	public void adjustNeighborhoodRadius(int iteration) {
		//Neighborhood radius is not adjusted by way of time in the semi-online SOM
		
	}



}
