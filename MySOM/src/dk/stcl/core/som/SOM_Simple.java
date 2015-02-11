package dk.stcl.core.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;

public class SOM_Simple extends SomBasics implements ISOM {

	protected int maxIterations;
	protected double mapRadius;
	protected double timeConstant;
	protected double initialNeighborHoodRadius, curNeighborhoodRadius;
	protected double initialLearningRate, curLearningRate;
	protected double activationCodingFactor;
	
	public SOM_Simple(int mapSize, int inputLength, Random rand, int maxIterations, double initialLearningRate, double activationCodingFactor) {
		super(mapSize, inputLength, rand);
		this.mapRadius = (double) mapSize / 2;
		this.maxIterations = maxIterations;
		this.timeConstant = maxIterations / Math.log(mapRadius + 0.01); //Have to add a small constant in case the map radius is 1 as this would lead the time constant to be == Infinity
		initialNeighborHoodRadius = mapRadius;
		curNeighborhoodRadius = initialNeighborHoodRadius;
		this.initialLearningRate = initialLearningRate;
		this.curLearningRate = initialLearningRate;
		this.activationCodingFactor = activationCodingFactor;
	}

	@Override
	protected void updateWeights(SimpleMatrix inputVector){
		for (SomNode n : somMap.getNodes()){
			double dist = n.squaredDistanceTo(bmu);
			//double dist = n.normDistanceTo(bmu);
			if (dist <= curNeighborhoodRadius){
				SimpleMatrix weightVector = n.getVector();
				SimpleMatrix diff = inputVector.minus(weightVector);
				double neighborhoodEffect = calculateNeighborhoodEffect(n, bmu);
				
				double effect = neighborhoodEffect * curLearningRate;
				SimpleMatrix delta = diff.scale(effect);
				
				SimpleMatrix newVector = weightVector.plus(delta);
				n.setVector(newVector);
			}
		}
	}
	
	public SomNode findBMU(SimpleMatrix inputVector){
		double minDist = Double.POSITIVE_INFINITY;
		bmu = null;
		
		for (SomNode n : somMap.getNodes()){
			SimpleMatrix weightVector = n.getVector();
			SimpleMatrix diff = inputVector.minus(weightVector);
			double dist = diff.normF();
			double error = Math.pow(dist, 2);
			error = error / inputLength;
			
			if (dist < minDist){
				minDist = dist;
				bmu = n;
			}
			errorMatrix.set(n.getRow(), n.getCol(), error);
		}
		
		return bmu;
	}
	
	@Override
	public void adjustNeighborhoodRadius(int iteration){
		curNeighborhoodRadius = mapRadius * Math.exp(-(double) iteration / timeConstant);	
	}
	
	@Override
	public void adjustLearningRate(int iteration){
		curLearningRate = initialLearningRate * Math.exp(-(double) iteration / maxIterations);
	}
	
	protected double calculateNeighborhoodEffect(SomNode n, SomNode bmu){
		double dist = Math.pow(n.normDistanceTo(bmu),2);
		double bottom = 2 * Math.pow(curNeighborhoodRadius, 2);
		double effect =  Math.exp(- dist / bottom);
		return effect;
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


}
