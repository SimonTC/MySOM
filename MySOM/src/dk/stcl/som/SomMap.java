package dk.stcl.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

public class SomMap {
	
	private int rows, columns;
	private SomNode[] nodes; 
	private SimpleMatrix[][] weightMatrix; //Contains pointers to the weight vectors of each node
	
	/**
	 * Creates a new map where all node vector values are initialized to a random value between 0 and 1
	 * @param columns width of the map 
	 * @param rows height of the map 
	 */
	public SomMap(int columns, int rows, int inputLength, Random rand) {
		this.columns = columns;
		this.rows = rows;
		initializeMap(inputLength, rand);
	}
	
	/**
	 * Creates a new map where all node vector values are initialized to 0
	 * @param columns width of the map 
	 * @param rows height of the map 
	 */
	public SomMap(int columns, int rows, int inputLength) {
		this.columns = columns;
		this.rows = rows;
		initializeMap(inputLength, null);
	}
	
	/**
	 * Fills the map with nodes where the vector values are set to random values between 0 and 1
	 * @param columns
	 * @param rows
	 * @param inputLength
	 * @param rand
	 */
	private void initializeMap(int inputLength, Random rand){
		nodes = new SomNode[rows * columns];
		weightMatrix = new SimpleMatrix[rows][columns];
		
		for (int row = 0; row < rows; row++){
			for (int col = 0; col < columns; col++){
				SomNode n;
				if (rand != null){
					n = new SomNode(inputLength, rand, col, row, coordinateToIndex(row, col));
				} else{
					n = new SomNode(inputLength, col, row, coordinateToIndex(row, col));
				}
					
				nodes[coordinateToIndex(row, col)] = n;
				weightMatrix[row][col] = n.getVector();
			}
		}
	}
	
	private int coordinateToIndex(int row, int col){
		return (row * columns + col);
	}
	
	public SomNode[] getNodes(){
		return this.nodes;
	}
	
	public SomNode get(int x, int y){
		return nodes[coordinateToIndex(y, x)];
	}
	
	public void set(int x, int y, SomNode n){
		nodes[coordinateToIndex(y, x)] = n;;
	}
	
	public SomNode get(int id){
		return nodes[id];
	}
	
	public int getWidth(){
		return columns;
	}
	
	public int getHeight(){
		return rows;
	}
	
	/**
	 * Resets all the vector values in the map to resetValue
	 * @param resetValue
	 */
	public void reset(double resetValue){
		for (int row = 0; row < rows; row++){
			for (int col = 0; col < columns; col++){
				SomNode n  = nodes[coordinateToIndex(row, col)];
				n.getVector().set(resetValue);
			}
		}
	}
	
	/**
	 * Returns a matrix of all the weight vectors of the nodes
	 * @return
	 */
	public SimpleMatrix[][] getWeightMatrix(){
		return weightMatrix;
	}

}
