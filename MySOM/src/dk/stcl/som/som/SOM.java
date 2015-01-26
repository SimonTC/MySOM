package dk.stcl.som.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.basic.SomBasics;
import dk.stcl.som.containers.SomNode;

/**
 * This implementation off an online som is based on the description in the LoopSom paper
 * @author Simon
 *
 */
//TODO: Better citation
public class SOM extends SomBasics implements ISOM {
	
	private double learningRate;
	private double stddev; //TODO: Give a good real name
	private double activationCodingFactor;
	
	public SOM(int columns, int rows, int inputLength, Random rand, double learningRate, double stddev, double activationCodingFactor) {
		super(columns, rows, inputLength, rand);
		this.learningRate = learningRate;
		this.stddev = stddev;
		this.activationCodingFactor = activationCodingFactor;
	}

	@Override
	protected double calculateMaxRadius(SomNode bmu, SimpleMatrix inputVector,
			double minimumLearningEffect) {
		return rows; //Currently we do not perform any optimization
	}

	@Override
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

	@Override
	protected double calculateLearningRate(SomNode bmu, SimpleMatrix inputVector) {
		return learningRate;
	}

	@Override
	protected void adjustNodeWeights(SomNode n, double neighborhoodEffect,
			double learningRate, double somFitness) {
		SimpleMatrix valueVector = n.getVector();
		
		//Calculate difference between input and current values
		SimpleMatrix diff = inputVector.minus(valueVector);
		
		//Multiply by som fitness and neighborhood effect
		SimpleMatrix learningEffect = new SimpleMatrix(diff.numRows(), diff.numCols());
		learningEffect.set(learningRate * neighborhoodEffect);
		diff = diff.elementMult(learningEffect);
		
		//Add the diff-values to the value vector
		valueVector = valueVector.plus(diff);
		
		n.setVector(valueVector);

	}

	@Override
	protected double calculateSomFitness(SomNode bmu, SimpleMatrix inputVector) {
		SimpleMatrix bmuVector = bmu.getVector();
		SimpleMatrix diff = bmuVector.minus(inputVector);
		double error = Math.pow(diff.normF(), 2);
		double avgError = error / inputLength;
		return 1 - avgError;
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
	
	@Override
	public SimpleMatrix computeActivationMatrix(){
		SimpleMatrix activation = errorMatrix.elementPower(2); 
		activation = activation.divide(-2 * Math.pow(activationCodingFactor, 2));	 
		activation = activation.elementExp();
		return activation;
	}


}
