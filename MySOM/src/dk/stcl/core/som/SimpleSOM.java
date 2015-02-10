package dk.stcl.core.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;

public class SimpleSOM implements ISOM {

	private SomMap map;
	private int maxIterations;
	private double mapRadius;
	private double timeConstant;
	private double initialNeighborHoodRadius, curNeighborhoodRadius;
	private double initialLearningRate, curLearningRate;
	private boolean learning;
	private int mapSize;
	private SomNode bmu;
	private int inputLength;
	private SimpleMatrix errorMatrix;
	private SimpleMatrix activationMatrix;
	
	public SimpleSOM(int mapSize, int inputLength, Random rand, int maxIterations, double initialLearningRate) {
		map = new SomMap(mapSize, mapSize, inputLength, rand);
		
		this.mapRadius = (double) mapSize / 2;
		this.maxIterations = maxIterations;
		this.timeConstant = maxIterations / Math.log(mapRadius);
		initialNeighborHoodRadius = mapRadius;
		curNeighborhoodRadius = initialNeighborHoodRadius;
		this.initialLearningRate = initialLearningRate;
		this.curLearningRate = initialLearningRate;
		learning = true;
		this.mapSize = mapSize;
		this.inputLength = inputLength;
		this.errorMatrix = new SimpleMatrix(mapSize, mapSize);
		this.activationMatrix = new SimpleMatrix(mapSize, mapSize);
		
	}
	
	public SomNode step(SimpleMatrix inputVector){
		//Find BMU
		bmu = findBMU(inputVector);
		
		//Update weights
		if (learning) updateWeights(inputVector);
		
		return bmu;
	}
	
	private void updateWeights(SimpleMatrix inputVector){
		SimpleMatrix m = new SimpleMatrix(mapSize, mapSize);
		SimpleMatrix delta = null;
		for (SomNode n : map.getNodes()){
			double dist = n.squaredDistanceTo(bmu);
			//double dist = n.normDistanceTo(bmu);
			if (dist <= curNeighborhoodRadius){
				SimpleMatrix weightVector = n.getVector();
				SimpleMatrix diff = inputVector.minus(weightVector);
				double neighborhoodEffect = calculateNeighborhoodEffect(n, bmu);
				
				double effect = neighborhoodEffect * curLearningRate;
				m.set(n.getId(), effect);
				delta = diff.scale(effect);
				
				SimpleMatrix newVector = weightVector.plus(delta);
				n.setVector(newVector);
			}
		}
		delta.reshape(mapSize, mapSize);
		//delta.print();
	}
	
	public SomNode findBMU(SimpleMatrix inputVector){
		double minDist = Double.POSITIVE_INFINITY;
		bmu = null;
		
		for (SomNode n : map.getNodes()){
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
	
	public void adjustNeighborhoodRadius(int step){
		curNeighborhoodRadius = mapRadius * Math.exp(-(double) step / timeConstant);		
	}
	
	public void adjustLearningRate(int step){
		curLearningRate = initialLearningRate * Math.exp(-(double) step / maxIterations);
	}
	
	private double calculateNeighborhoodEffect(SomNode n, SomNode bmu){
		double dist = Math.pow(n.normDistanceTo(bmu),2);
		double bottom = 2 * Math.pow(curNeighborhoodRadius, 2);
		double effect =  Math.exp(- dist / bottom);
		return effect;
	}
	
	public void setLearning(boolean learning){
		this.learning = learning;
	}

	@Override
	public SimpleMatrix computeActivationMatrix() {
		activationMatrix = new SimpleMatrix(mapSize, mapSize);
		activationMatrix.set(1);
		double maxError = errorMatrix.elementMaxAbs();
		if (maxError == 0) maxError = 1;
		activationMatrix = activationMatrix.minus(errorMatrix.divide(maxError));
		return activationMatrix;
	}

	@Override
	public void setNode(SomNode n, int row, int column) {
		map.set(column, row, n);
		
	}

	@Override
	public SomNode step(double[] inputVector) {
		SimpleMatrix m = new SimpleMatrix(1, inputVector.length, true, inputVector);
		return findBMU(m);
	}


	@Override
	public void adjustLearningRate(int iteration, int maxIterations) {
		adjustLearningRate(iteration);
		
	}

	@Override
	public void adjustNeighborhoodRadius(int iteration, int maxIterations) {
		adjustNeighborhoodRadius(iteration);
		
	}

	@Override
	public void printLabelMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double calculateSOMFitness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SomNode getBMU() {
		return bmu;
	}

	@Override
	public SimpleMatrix getErrorMatrix() {
		return errorMatrix;
	}

	@Override
	public int getHeight() {
		return mapSize;
	}

	@Override
	public SomMap getSomMap() {
		return map;
	}

	@Override
	public SomNode getNode(int id) {
		return map.get(id);
	}

	@Override
	public SomNode getNode(int row, int col) {
		return map.get(col, row);
	}

	@Override
	public SomNode[] getNodes() {
		return map.getNodes();
	}

	@Override
	public SimpleMatrix getActivationMatrix() {
		return activationMatrix;
	}

	@Override
	public int getWidth() {
		return mapSize;
	}

	@Override
	public int getInputVectorLength() {
		return inputLength;
	}

	@Override
	public boolean getLearning() {
		return learning;
	}

	@Override
	public void sensitize(int iteration, int maxIterations) {
		adjustLearningRate(iteration);
		adjustNeighborhoodRadius(iteration);
		//System.out.println("Step " + iteration  + " LR " + curLearningRate + " NR " + curNeighborhoodRadius);
		
	}

}
