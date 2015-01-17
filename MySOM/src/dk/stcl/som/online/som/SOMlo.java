package dk.stcl.som.online.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomNode;
import dk.stcl.som.online.SomOnline;

/**
 * This implementation off an online som is based on the description in the LoopSom paper
 * @author Simon
 *
 */
//TODO: Better citation
public class SOMlo extends SomOnline {
	
	private double learningRate;
	private double stddev; //TODO: Give a good real name
	private double activationCodingFactor;
	
	public SOMlo(int columns, int rows, int inputLength, Random rand, double learningRate, double stddev, double activationCodingFactor) {
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
		double dist = bmu.distanceTo(n);
		double error = 1 - somFitness;
		double effect = Math.exp(-dist / (error * Math.pow(stddev, 2)));
		
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
		SimpleMatrix tmp = new SimpleMatrix(diff.numRows(), diff.numCols());
		tmp.set(learningRate * neighborhoodEffect);
		diff = diff.elementMult(tmp);
		
		//Add the diff-values to the value vector
		valueVector = valueVector.plus(diff);
		
		n.setVector(valueVector);

	}

	@Override
	protected double calculateSomFitness(SomNode bmu, SimpleMatrix inputVector) {
		double error = bmu.squaredDifference(inputVector);
		double avgError = error / inputLength;
		return 1 - avgError;
	}

	@Override
	public SomNode findBMU(SimpleMatrix inputVector){
		SomNode BMU = null;
		double minDiff = Double.POSITIVE_INFINITY;
		for (SomNode n : somMap.getNodes()){
			double diff = n.squaredDifference(inputVector);
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
		SimpleMatrix activation = errorMatrix.divide(2 * Math.pow(activationCodingFactor, 2));	 
		return activation;
	}


}
