package dk.stcl.core.basic.containers;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class SomNode {
	
	private SimpleMatrix valueVector;
	private int col, row; // Coordinates of the node
	private int label;
	private int id;
	
	/**
	 * Creates a node with a vector with values set to zero
	 * @param vectorSize 
	 * @param col
	 * @param row
	 */
	public SomNode(int vectorSize, int col, int row, int id) {
		// Create vector with the dimension values and set values between 0 and 1
		valueVector = new SimpleMatrix(1, vectorSize);
		valueVector.set(0);
		setCoordinate(col, row);
		this.id = id;
		this.label = -1;
	}
	
	/**
	 * Creates a node with a vector with random values between 0 and 1
	 * @param vectorSize 
	 * @param rand
	 * @param col
	 * @param row
	 */
	public SomNode(int vectorSize, Random rand, int col, int row, int id) {
		// Create vector with the dimension values and set values between 0 and 1
		valueVector = SimpleMatrix.random(1, vectorSize, 0, 1, rand);	
		setCoordinate(col, row);
		this.id = id;
		this.label = -1;
	}
	
	/**
	 * Creates a new node where its internal vector is referencing the given vector
	 * @param vector
	 * @param col
	 * @param row
	 */
	public SomNode(SimpleMatrix vector, int col, int row, int id){
		this.valueVector = vector;
		setCoordinate(col, row);
		this.id = id;
		this.label = -1;
	}
	
	/**
	 * Constructor used to create the somnode from the string created by the toFileString() method
	 * @param s
	 */
	public SomNode(String s){
		String[] arr = s.split(" ");
		id = Integer.parseInt(arr[0]);
		label = Integer.parseInt(arr[1]);
		row = Integer.parseInt(arr[2]);
		col = Integer.parseInt(arr[3]);
		
		int vectorSize = arr.length - 4;
		double[] data = new double[vectorSize];
		for (int i = 4; i < arr.length; i++){
			double d = Double.parseDouble(arr[i]);
			data[i-4] = d;
		}
		valueVector = new SimpleMatrix(1, vectorSize, true, data);
	}

	
	/**
	 * Creates a new node with a value vector containing the given data
	 * @param vector
	 * @param col
	 * @param row
	 */
	public SomNode(double[] vectorData, int col, int row, int id){
		//Create 1D vector
		valueVector = new SimpleMatrix(1, vectorData.length);
		
		//Add data
		for (int i = 0; i< vectorData.length; i++){
			valueVector.set(0, i, vectorData[i]);
		}
		setCoordinate(col, row);
		
		this.id = id;
		this.label = -1;
	}
	
	
	
	
	/**
	 * Only used if node is not placed in a map
	 * @param vector
	 */
	public SomNode(SimpleMatrix vector){
		this.valueVector = vector;
	}
	
	private void setCoordinate(int col, int row){
		this.col = col;
		this.row = row;
	}
	
	/**
	 * Returns the vector with the values for this node
	 * @return
	 */
	public SimpleMatrix getVector(){
		return valueVector;
	}
	
	/**
	 * Calculates the squared difference between the values of the value vector of the node and thatVector
	 * @param n
	 * @return
	 */
	public double squaredDifference(SimpleMatrix thatVector){
		SimpleMatrix diff = valueVector.minus(thatVector);
		diff = diff.elementPower(2);
		return diff.elementSum();
	}
	
	public void setVector(SimpleMatrix vector){
		valueVector = vector;
	}
	
	
	/**
	 * Calculates the euclidian distance between the two nodes.
	 * Based on the coordinates of the nodes
	 * @param n
	 * @return returns the squared distance
	 */
	public double squaredDistanceTo(SomNode n){
		int thatX = n.getCol();
		int thatY = n.getRow();
		int myX = col;
		int myY = row;
		
		int diffX = thatX - myX;
		diffX *= diffX;
		
		int diffY = thatY - myY;
		diffY *=diffY;
		
		return diffX + diffY;
	}
	
	/**
	 * Uses the frobenius norm to calculate distance in the map
	 * @param n
	 * @return
	 */
	public double normDistanceTo(SomNode n){
		int thatX = n.getCol();
		int thatY = n.getRow();
		int myX = col;
		int myY = row;
		
		double diffX = thatX - myX;
		diffX = Math.pow(Math.abs(diffX), 2);
		
		double diffY = thatY - myY;
		diffY = Math.pow(Math.abs(diffY), 2);
		
		return Math.sqrt(diffX + diffY);

	}
	
	public int getCol(){
		return col;
	}
	
	public int getRow(){
		return row;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SomNode other = (SomNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	/**
	 * Creates a String representation of this node.
	 * This string can be savd in a file to be used when recreating this node
	 * @return
	 */
	public String toFileString(){
		String s = id + " " + label + " " + row + " " + col;
		for (double d : valueVector.getMatrix().data){
			s += " " + d;
		}
		return s;
	}
	
	
	
	
	
	

}
