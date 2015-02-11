package dk.stcl.core.som;


import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomNode;

public class PLSOM extends SOM_SemiOnline implements ISOM {
	
	public PLSOM(int mapSize, int inputLength, Random rand,
			double learningRate, double activationCodingFactor,  double stddev) {
		
		super(mapSize, inputLength, rand, learningRate, activationCodingFactor, stddev);
		
		setupDiameterCalculation(inputLength);
		neighborHoodRange = (double) rows * rangeFactor; //TODO: change rangefactor to input parameter
	}

	private double inputSpaceSize;
	private ArrayList<SimpleMatrix> inputSpaceMembers;
	private int inputSpaceDimensions;
	private double neighborHoodRange;
	private final double rangeFactor = 1;

	private void setupDiameterCalculation(int inputLength){
		inputSpaceSize = -1;
		inputSpaceMembers = new ArrayList<SimpleMatrix>();
		inputSpaceDimensions = inputLength + 1;
	}
	
	/**
	 * Calculates the diameter of the input space following E. Berglands article Improved PLSOM algorithm
	 * @param inputVector
	 */
	//TODO: Better citation
	private double determineInputSpaceSize(SimpleMatrix inputVector){
		double diameter = calculateDiameter(inputVector);
		if (diameter > inputSpaceSize){
			inputSpaceSize = diameter;
			while (inputSpaceMembers.size() >= inputSpaceDimensions){
				int id = -1;
				double minDistance = Double.POSITIVE_INFINITY;
				for (int i = 0; i < inputSpaceMembers.size(); i++){
					SimpleMatrix m = inputSpaceMembers.get(i);
					SimpleMatrix diff = m.minus(inputVector);
					double d = diff.normF();// calculateEuclideanDistance(inputVector, m);
					if (d < minDistance){
						minDistance = d;
						id = i;
					}					
				}
				inputSpaceMembers.remove(id);
			}
			inputSpaceMembers.add(inputVector);
		}
		
		return inputSpaceSize;
	}
	
	private double calculateDiameter(SimpleMatrix inputVector){
		double maxDist = 0;
		for (SimpleMatrix m : inputSpaceMembers){
			SimpleMatrix diff = m.minus(inputVector);
			double d =  diff.normF(); //calculateEuclideanDistance(m, inputVector);
			if ( d > maxDist) maxDist = d;
		}
		return maxDist;
	}
	
	private double calculateEuclideanDistance(SimpleMatrix thisVector, SimpleMatrix thatVector){
		SimpleMatrix diff = thisVector.minus(thatVector);
		diff = diff.elementPower(2);
		double d =  diff.elementSum();
		return d;
	}
	
	private double calculateNeighborhoodSize(double somFitness){
		double neighborhoodSize = neighborHoodRange * (Math.log(1 + somFitness * (Math.E - 1)));
		return neighborhoodSize;
	}

	private double calculateMaxRadius(SomNode bmu, SimpleMatrix inputVector, double minimumLearningEffect) {
		double neighborhoodSize = calculateNeighborhoodSize(somFitness);
		double maxRadius = Math.sqrt(-Math.log(minimumLearningEffect) * Math.pow(neighborhoodSize, 2));
		
		maxRadius = Math.floor(Math.sqrt(maxRadius)); //TODO: Decide whether to use floor or ceiling
		
		return (int) maxRadius;

	}
	
	@Override
	protected double calculateNeighborhoodEffect(SomNode bmu, SomNode n) {
		double neighborhoodSize = calculateNeighborhoodSize(somFitness);
		double distance = bmu.squaredDistanceTo(n);
		double neighborhoodEffect = Math.exp(-Math.pow(distance, 2) / Math.pow(neighborhoodSize, 2));
		return neighborhoodEffect;

	}


	@Override
	protected void adjustNodeWeights(SomNode n, double neighborhoodEffect,
			double learningRate, double somFitness) {
		SimpleMatrix valueVector = n.getVector();
		
		//Calculate difference between input and current values
		SimpleMatrix diff = inputVector.minus(valueVector);
		
		//Multiply by som fitness and neighborhood effect
		SimpleMatrix tmp = new SimpleMatrix(diff.numRows(), diff.numCols());
		tmp.set(somFitness * neighborhoodEffect);
		diff = diff.elementMult(tmp);
		
		//Add the diff-values to the value vector
		valueVector = valueVector.plus(diff);
		
		n.setVector(valueVector);

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
	public double calculateSOMFitness() {
		//Calculate error between BMU and input
		double error = bmu.squaredDifference(inputVector);
		double avgError = error / inputVector.getMatrix().data.length; //TODO: Remove avgError if it doesnt help
		
		//Calculate current size of input space
		double size = determineInputSpaceSize(inputVector);
		
		//Calculate how fit the SOM is (Big is bad)
		double curFitness;
		if (error == 0){
			curFitness = 0;
		} else {
			curFitness = Math.min(avgError / size, 1);
		}
		return curFitness;
	}

}
