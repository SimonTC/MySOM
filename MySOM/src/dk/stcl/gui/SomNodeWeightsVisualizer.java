package dk.stcl.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomNode;

/**
 * Visualizes the model of  single som node
 * @author Simon
 *
 */
public class SomNodeWeightsVisualizer extends JPanel {

	private SomNode node;
	private SimpleMatrixVisualizer panel;
	private SimpleMatrix matrix;
	private int mapSize;
	
	/**
	 * 
	 * @param frameSize
	 * @param som
	 * @param scaled true if the values are scaled between 0 and 1
	 */
	public void initialize(SomNode node, int mapSize, boolean scaled){
		this.node = node;
		this.mapSize = mapSize;
		
		updateMatrix();
		
		panel = new SimpleMatrixVisualizer();
		panel.initialize(matrix, scaled);
		
		panel.setPreferredSize(getPreferredSize());
		
		add(panel);
	}
	
	/**
	 * Updates the activation panel. Has to be called before .repaint() Else no effect
	 * on the map
	 */
	public void updateData() {
		updateMatrix();
		this.panel.registerMatrix(matrix);
	}
	
	private void updateMatrix(){
		matrix = new SimpleMatrix(node.getVector());
		matrix.reshape(mapSize, mapSize);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

}
