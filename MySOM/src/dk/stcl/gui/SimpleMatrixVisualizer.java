package dk.stcl.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

/**
 * This class is used to visualize the values in a SimpleMatrix object as gray values.
 * @author Simon
 *
 */
public class SimpleMatrixVisualizer extends JPanel {

	private SimpleMatrix matrix;
	private int columns, rows;
	private boolean scaled;
	
	public void initialize(SimpleMatrix matrix, boolean scaled){
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
					rgb = (int) ((1-value) * 255);
				} else {
					rgb = (int) value;
				}
				Color c = new Color(rgb, rgb, rgb);
				g.setColor(c);
				g.fillRect((int)(x*cellWidth), (int)(y*cellHeight),
							(int)cellWidth, (int)cellHeight);
			}
		}
	}

	

}
