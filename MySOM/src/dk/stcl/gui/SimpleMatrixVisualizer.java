package dk.stcl.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

public class SimpleMatrixVisualizer extends JPanel {
	
	private static int borderSize = 1;
	
	public SimpleMatrixVisualizer() {
		setBackground(Color.RED);
		
		this.initialize(new SimpleMatrix(3, 3), true);
	}
	
	private SimpleMatrix matrix;
	private int columns, rows;
	private boolean scaled;
	
	public void initialize(SimpleMatrix matrix, boolean scaled){
		this.removeAll();
		registerMatrix(matrix);
		this.scaled = scaled;
	}
	
	public void registerMatrix(SimpleMatrix matrix){
		this.matrix = matrix;
		this.columns = matrix.numCols();
		this.rows = matrix.numRows();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Clear the map
		g.clearRect(0, 0, getWidth(), getHeight());

		// Draw the grid
		int cellWidth = getWidth() / columns;
		int cellHeight = getHeight() / rows;
		
		for (int x=0; x<columns; x++) {
			for (int y=0; y<rows; y++) {				
				
				double value = matrix.get(y, x);
				int rgb;
				if (scaled){
					rgb = (int) ((value) * 255);
				} else {
					rgb = (int) value;
				}
				Color c = new Color(rgb, rgb, rgb);
				g.setColor(c);
				
				
				//Create rectangel with border
				g.fillRect((int)(x*cellWidth) + borderSize, (int)(y*cellHeight) + borderSize,
						(int)cellWidth - borderSize, (int)cellHeight - borderSize);
			
				
			}
		}
	}
}
