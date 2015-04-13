package dk.stcl.core.basic.containers;

import java.util.Random;

import dk.stcl.core.utils.SomConstants;

public class SomMap {
	
	private int rows, columns;
	private SomNode[] nodes; 
	
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
	 * Instantiates the map as a copy of the map described in the string.
	 * The string representation is created by the toFileString() method
	 * @param s
	 */
	public SomMap(String s){
		String[] lines = s.split(SomConstants.LINE_SEPARATOR);
		String[] mapsize = lines[0].split(" ");
		columns = Integer.parseInt(mapsize[0]);
		rows = Integer.parseInt(mapsize[1]);
		nodes = new SomNode[rows * columns];
		for (int i = 1; i < lines.length; i++){
			SomNode n = new SomNode(lines[i]);
			nodes[n.getId()] = n;
		}
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
		
		for (int row = 0; row < rows; row++){
			for (int col = 0; col < columns; col++){
				SomNode n;
				if (rand != null){
					n = new SomNode(inputLength, rand, col, row, coordinateToIndex(row, col));
				} else{
					n = new SomNode(inputLength, col, row, coordinateToIndex(row, col));
				}
					
				nodes[coordinateToIndex(row, col)] = n;
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
	 * Create String representation of the map to be used when recreating this map
	 * @return
	 */
	public String toFileString(){
		String ls = SomConstants.LINE_SEPARATOR;
		String s = columns + " " + rows + ls;
		for (SomNode n : nodes){
			s += n.toFileString() + ls;
		}
		return s;
	}
	
	
	

}
