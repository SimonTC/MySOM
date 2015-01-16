package dk.stcl.som;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class PLSOM extends SOM {
	
	private double inputSpaceSize;
	private ArrayList<SimpleMatrix> inputSpaceMembers;
	private int inputSpaceDimensions;
	private double neighborHoodRange;
	private double curFitness;
	private final double rangeFactor = 1;

	public PLSOM(int columns, int rows, int inputLength, Random rand) {
		super(columns, rows, inputLength, rand);
		setupDiameterCalculation(inputLength);
		neighborHoodRange = (double) rows * rangeFactor; //TODO: change to input parameter
	}

	public PLSOM(int columns, int rows, int inputLength, Random rand,
			double initialLearningrate, double initialNeighborhodRadius) {
		super(columns, rows, inputLength, rand, initialLearningrate,
				initialNeighborhodRadius);
		setupDiameterCalculation(inputLength);
		neighborHoodRange = (double) rows * rangeFactor; //TODO: change to input parameter
	}
	
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
					double d = calculateEuclideanDistance(inputVector, m);
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
			double d =  calculateEuclideanDistance(m, inputVector);
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
	
	private double calculateNeighborhoodEffect(SomNode bmu, SomNode node, double somFitness){
		double neighborhoodSize = calculateNeighborhoodSize(somFitness);
		double distance = bmu.distanceTo(node);
		double neighborhoodEffect = Math.exp(-Math.pow(distance, 2) / Math.pow(neighborhoodSize, 2));
		return neighborhoodEffect;
		
	}
	
	private double calculateNeighborhoodSize(double somFitness){
		double neighborhoodSize = neighborHoodRange * (Math.log(1 + somFitness * (Math.E - 1)));
		return neighborhoodSize;
	}
	
	private int calculateMaxDistance(double somFitness, double minLEarningEffect){
		double neighborhoodSize = calculateNeighborhoodSize(somFitness);
		double maxDistance = Math.sqrt(-Math.log(minLEarningEffect) * Math.pow(neighborhoodSize, 2));
		
		maxDistance = Math.floor(Math.sqrt(maxDistance)); //TODO: Change to ceiling
		
		return (int) maxDistance;
	}
	
	@Override
	/**
	 * Adjusts the weights of the nodes in the map
	 * @param bmu
	 * @param inputVector
	 * @param learningRate - Not used
	 * @param neighborhoodRadius - Not used
	 */
	protected void updateWeights(SomNode bmu,SimpleMatrix inputVector, double learningRate, double neighborhoodRadius){
		//Calculate error between BMU and input
		double error = bmu.squaredDifference(inputVector);
		
		//Calculate current size of input space
		double size = determineInputSpaceSize(inputVector);
		
		//Calculate how fit the SOM is (Big is bad)
		if (error == 0){
			curFitness = 0;
		} else {
			curFitness = Math.min(error / size, 1);
		}
		
		//Calculate max distance to bmu 
		int maxDistance = calculateMaxDistance(curFitness, 0.01); //TODO: Change the 0.001 to a parameter
		
		//Calculate start and end coordinates for the weight updates
				int bmuCol = bmu.getCol();
				int bmuRow = bmu.getRow();
				int colStart = (int) (bmuCol - maxDistance);
				int rowStart = (int) (bmuRow - maxDistance );
				int colEnd = (int) (bmuCol + maxDistance);
				int rowEnd = (int) (bmuRow + maxDistance );
				
				//Make sure we don't get out of bounds errors
				if (colStart < 0) colStart = 0;
				if (rowStart < 0) rowStart = 0;
				if (colEnd > somMap.getWidth()) colEnd = somMap.getWidth();
				if (rowEnd > somMap.getHeight()) rowEnd = somMap.getHeight();
				
				//Adjust weights
				for (int col = colStart; col < colEnd; col++){
					for (int row = rowStart; row < rowEnd; row++){
						SomNode n = somMap.get(col, row);
						double neighborhoodEffect = calculateNeighborhoodEffect(bmu, n, curFitness);
						adjustNodeWeights(n, inputVector, curFitness, neighborhoodEffect);	
					}
				}
		
		
		/*
		//Update weights
		//TODO: make this more efficient by only looking at nodes within a certain range. Could be achieved by solving the neighborhood effect formula with respect to distance
		for (SomNode n : somMap.getNodes()){
			double neighborhoodEffect = calculateNeighborhoodEffect(bmu, n, curFitness);
			//System.out.println("Neighborhood effect: " + neighborhoodEffect); //TODO: Remove
			//System.out.println("" + neighborhoodEffect);
			adjustNodeWeights(n, inputVector, curFitness, neighborhoodEffect);			
		}
		*/
		
	}
	
	@Override
	public void weightAdjustment(SomNode n, SomNode bmu, SimpleMatrix inputVector, double neighborhoodRadius, double learningRate ){
		assert false : "This method should never be called";
	}
	
	
	/**
	 * Adjust the weights of the nodes based on the difference between the valueVectors of this node and input vector
	 * @param n node which weight vector should be adjusted
	 * @param inputVector
	 * @param learningRate
	 * @param learningEffect How effective the learning is. This is dependant on the distance to the bmu
	 */
	protected void adjustNodeWeights(SomNode n, SimpleMatrix inputVector, double somFitness, double neighborhoodEffect){
		SimpleMatrix valueVector = n.getVector();
		
		//Calculate difference between input and current values
		SimpleMatrix diff = inputVector.minus(valueVector);
		
		//Multiply by som fitness and neighborhood effect
		SimpleMatrix tmp = new SimpleMatrix(diff.numRows(), diff.numCols());
		tmp.set(somFitness * neighborhoodEffect);
		diff = diff.elementMult(tmp);
		
		//Add the dist-values to the value vector
		valueVector = valueVector.plus(diff);
		
		n.setVector(valueVector);
	}
	
	public double getFitness(){
		return curFitness;
	}
	

}
