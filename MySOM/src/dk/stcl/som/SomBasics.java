package dk.stcl.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public abstract class SomBasics {

	protected SomMap somMap;
	protected SimpleMatrix errorMatrix; //Contains the differences between the input weights and the node weights
	protected boolean learning; //If false no learning will take place when a new input is presented
	protected double learningRate, neighborhoodRadius;
	protected double initialLearningRate, initialNeighborhoodRadius;
	protected SomNode bmu;
	protected int inputLength, rows, columns;
	
	
	/**
	 * 
	 * @param columns number of columns in the internal weightMap
	 * @param rows number of rows in the internal weight map
	 * @param inputLength length of the value vectors
	 * @param rand
	 * @param initialLearningrate
	 * @param initialNeighborhodRadius
	 */
	public SomBasics(int columns, int rows, int inputLength, Random rand, double initialLearningrate, double initialNeighborhodRadius) {
		this.inputLength = inputLength;
		this.rows = rows;
		this.columns = columns;
		somMap = new SomMap(columns, rows, inputLength, rand);
		errorMatrix = new SimpleMatrix(rows, columns);
		learning = true;
		this.initialLearningRate = initialLearningrate;
		this.initialNeighborhoodRadius = initialNeighborhodRadius;
		
	}
	
	/**
	 * Constructor without initial learning rate and neigborhood radius. These are set to 0.9 and half map size
	 * @param columns number of columns in the internal weightMap
	 * @param rows number of rows in the internal weight map
	 * @param inputLength length of the value vectors
	 * @param rand
	 */
	public SomBasics(int columns, int rows, int inputLength, Random rand) {
		this(columns, rows, inputLength, rand, 0.9, Math.max(columns, rows) / (double)2);
	}
	
	/**
	 * The activation matrix is computed as 1 - (Ei / Emax)
	 * @return
	 */
	public SimpleMatrix computeActivationMatrix(){
		double maxError = errorMatrix.elementMaxAbs();
		SimpleMatrix m = errorMatrix.divide(maxError);
		SimpleMatrix activation = new SimpleMatrix(errorMatrix.numRows(), errorMatrix.numCols());
		activation.set(1);
		activation = activation.minus(m);	
		return activation;
	}
	
	public SomNode getBMU(){
		return bmu;
	}
	
	public abstract SomNode getBMU(SimpleMatrix inputVector);
	
	public SimpleMatrix getErrorMatrix(){
		return errorMatrix;
	}
	
	public int getHeight(){
		return somMap.getHeight();
	}
	
	public SomMap getSomMap(){
		return somMap;
	}
	
	public SomNode getNode(int id){
		return somMap.get(id);
	}
	
	public SomNode getNode(int row, int col){
		return somMap.get(col, row);
	}
	
	public SomNode[] getNodes(){
		return somMap.getNodes();
	}
	
	public int getWidth(){
		return somMap.getWidth();
	}
	
	public boolean getLearning(){
		return learning;
	}
	
	public void setLearning(boolean learning){
		this.learning = learning;
	}
	
	/**
	 * Returns a matrix with the weights of all the nodes in the map
	 * @return
	 */
	public SimpleMatrix[][] getWeightMatrix(){
		return somMap.getWeightMatrix();
	}
	
	/**
	 * Calculates the learning effect based on distance to the learning center.
	 * The lower the distance, the higher the learning effect
	 * @param n
	 * @param bmu
	 * @return the learning effect. 0 if node n is outside of the neighborhood radius
	 */
	protected double learningEffect(SomNode n, SomNode bmu){
		double dist = n.distanceTo(bmu);
		double squaredRadius = neighborhoodRadius * neighborhoodRadius;
		double learningEffect;
		if (dist <= squaredRadius){
			learningEffect = Math.exp(-(dist / (2 * squaredRadius)));
		} else {
			learningEffect = 0;
		}
		
		return learningEffect;
	}
	
	/**
	 * Place the node n at coordinate (row,column) in the som map
	 * @param n
	 * @param row
	 * @param column
	 */
	public void set(SomNode n, int row, int column){
		somMap.set(column, row, n);
	}
	
	public abstract SomNode step (SimpleMatrix inputVector, double learningRate, double neighborhoodRadius);
	
	/**
	 * Adjusts the weights of the nodes in the map
	 * @param bmu
	 * @param inputVector
	 * @param learningRate
	 * @param neighborhoodRadius
	 */
	protected void updateWeights(SomNode bmu,SimpleMatrix inputVector, double learningRate, double neighborhoodRadius){
		//Calculate start and end coordinates for the weight updates
		int bmuCol = bmu.getCol();
		int bmuRow = bmu.getRow();
		int colStart = (int) (bmuCol - neighborhoodRadius);
		int rowStart = (int) (bmuRow - neighborhoodRadius );
		int colEnd = (int) (bmuCol + neighborhoodRadius);
		int rowEnd = (int) (bmuRow + neighborhoodRadius );
		
		//Make sure we don't get out of bounds errors
		if (colStart < 0) colStart = 0;
		if (rowStart < 0) rowStart = 0;
		if (colEnd > somMap.getWidth()) colEnd = somMap.getWidth();
		if (rowEnd > somMap.getHeight()) rowEnd = somMap.getHeight();
		
		//Adjust weights
		for (int col = colStart; col < colEnd; col++){
			for (int row = rowStart; row < rowEnd; row++){
				SomNode n = somMap.get(col, row);
				weightAdjustment(n, bmu, inputVector, neighborhoodRadius, learningRate);
			}
		}
	}
	
	/**
	 * Updates the learning rate and the neighborhood radius based on the current timestep.
	 * 
	 * radius is update by: sigma(t) = sigma(0) * exp (-t/lambda)
	 * 	where sigma(t) is the width of the neigborhood radius at time t and lambda is a time constant = maxTimesteps / log(sigma(0)
	 * 
	 * learning rate is updated by learning(t) = learning(0) * exp(-t/maxTimesteps)
	 * 
	 */
	public void sensitize(int timestep, int maxTimesteps){		
		sensitize(timestep, maxTimesteps, true, true);
	}
	
	/**
	 * Updates the learning rate and the neighborhood radius based on the current timestep.
	 * 
	 * radius is update by: sigma(t) = sigma(0) * exp (-t/lambda)
	 * 	where sigma(t) is the width of the neigborhood radius at time t and lambda is a time constant = maxTimesteps / log(sigma(0)
	 * 
	 * learning rate is updated by learning(t) = learning(0) * exp(-t/maxTimesteps)
	 * 
	 * @param timestep
	 * @param maxTimesteps
	 * @param doNeighborhood if false neighborhood radius will not be updated
	 * @param doLearningRate if false learning rate will not be updated
	 */
	public void sensitize(int timestep, int maxTimesteps, boolean doNeighborhood, boolean doLearningRate){
		
		if (doNeighborhood){
			//Update neighborhood radius
			double lambda = (double) maxTimesteps / Math.log(initialNeighborhoodRadius);		
			neighborhoodRadius = initialNeighborhoodRadius * Math.exp(-(double)timestep / lambda);
		}
		
		if (doLearningRate){
			//Update learning rate
			learningRate = initialLearningRate * Math.exp((double) -timestep / maxTimesteps);
			if (learningRate < 0.01) learningRate = 0.01;
		}
	}
	
	/**
	 * Adjusts the weights of a single node
	 * @param bmu
	 * @param inputVector
	 * @param neighborhoodRadius
	 * @param learningRate
	 * @return
	 */
	public abstract void weightAdjustment(SomNode n, SomNode bmu, SimpleMatrix inputVector, double neighborhoodRadius, double learningRate );

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}

	public void setNeighborhoodRadius(double neighborhoodRadius) {
		this.neighborhoodRadius = neighborhoodRadius;
	}

}
