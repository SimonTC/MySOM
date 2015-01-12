package dk.stcl.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class RSOM extends PLSOM {
	
	private SomMap leakyDifferencesMap;
	private double decayFactor;

	public RSOM(int columns, int rows, int inputLength, Random rand,
			double initialLearningrate, double initialNeighborhodRadius, double decayFactor) {
		super(columns, rows, inputLength, rand, initialLearningrate,
				initialNeighborhodRadius);
		
		setupLeakyDifferences();
		this.decayFactor = decayFactor;		
	}

	public RSOM(int columns, int rows, int inputLength, Random rand, double decayFactor) {
		super(columns, rows, inputLength, rand);
		setupLeakyDifferences();
		this.decayFactor = decayFactor;
	}
	
	private void setupLeakyDifferences(){
		leakyDifferencesMap = new SomMap(columns, rows, inputLength);
	}
	

	@Override
	/**
	 * 
	 * @param inputVector Not used in the RSOM
	 * @return
	 */
	public SomNode getBMU(SimpleMatrix inputVector) {		
		double min = Double.POSITIVE_INFINITY;
		SomNode[] leakyNodes = leakyDifferencesMap.getNodes();
		SomNode BMU = null;
		for (SomNode n : leakyNodes){
			double value = n.getVector().elementSum();
			value = Math.abs(value);
			errorMatrix.set(n.getRow(), n.getCol(), value);
			if (value < min) {
				min = value;
				BMU = n;
			}
		}		
		
		assert (BMU != null): "No BMU was found";

		return BMU;
	}
	
	/**
	 * Updates the vector values of the nodes in the leaky differences map
	 * @param inputVector
	 * @param leakyCoefficient
	 */
	private void updateLeakyDifferences(SimpleMatrix inputVector){
		for (int row = 0; row < leakyDifferencesMap.getHeight(); row++){
			for (int col = 0; col < leakyDifferencesMap.getWidth(); col++){
				SomNode leakyDifferenceNode = leakyDifferencesMap.get(col, row);
				SomNode weightNode = somMap.get(col, row);
				
				//Calculate difference between input vector and weight vector
				SimpleMatrix weightDiff = inputVector.minus(weightNode.getVector());
				weightDiff = weightDiff.scale(decayFactor);
				
				SimpleMatrix leakyDifferenceVector = leakyDifferenceNode.getVector();
				leakyDifferenceVector = leakyDifferenceVector.scale(1-decayFactor);
								
				SimpleMatrix sum = leakyDifferenceVector.plus(weightDiff);
				
				leakyDifferenceNode.setVector(sum);
				leakyDifferencesMap.set(col, row, leakyDifferenceNode);
			}
		}
	}

	@Override
	public SomNode step(SimpleMatrix inputVector, double learningRate,
			double neighborhoodRadius) {
		
		updateLeakyDifferences(inputVector);
		
		//Find BMU
		bmu = getBMU(null);		
		
		if (learning){
			updateWeights(bmu, inputVector, learningRate, neighborhoodRadius);
		}
	
		return bmu;
	}
	
	public SomNode step(double[] inputVector){
		SimpleMatrix vector = new SimpleMatrix(1, inputLength, true, inputVector);
		
		bmu = this.step(vector, this.learningRate, this.neighborhoodRadius);
		
		return bmu;
	}

	@Override
	public void weightAdjustment(SomNode n, SomNode bmu,
			SimpleMatrix inputVector, double neighborhoodRadius,
			double learningRate) {
		
		double learningEffect = learningEffect(n,bmu);		
		SimpleMatrix weightVector = n.getVector();
		SomNode leakyDifferenceNode = leakyDifferencesMap.get(n.getCol(), n.getRow());
		SimpleMatrix delta = leakyDifferenceNode.getVector().scale(learningRate * learningEffect);
		weightVector = weightVector.plus(delta);
		n.setVector(weightVector);

	}
	
	/**
	 * Resets the leaky difference vector. 
	 * Used between sequences when training
	 */
	public void flush(){
		setupLeakyDifferences();
	}
	
	

}
