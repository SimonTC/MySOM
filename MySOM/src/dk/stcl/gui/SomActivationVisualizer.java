package dk.stcl.gui;

import javax.swing.JPanel;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.som.SOM_Simple;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import org.ejml.simple.SimpleMatrix;

import java.awt.Color;
import java.awt.GridLayout;

/**
 * Visualizes the activation of a SOM map.
 * @author Simon
 *
 */
public class SomActivationVisualizer extends JPanel {
	
	private ISomBasics som;
	private SimpleMatrixVisualizer MatrixViz;
	
	public SomActivationVisualizer() {
		
		ISomBasics som = new SOM_Simple(3, 5, 0, 0, 1);
		this.initialize(som, true);
	}
	
	/**
	 * 
	 * @param frameSize
	 * @param som
	 * @param scaled true if the values are scaled between 0 and 1
	 */
	public void initialize(ISomBasics som, boolean scaled){
		this.removeAll();
		this.som = som;
		
		MatrixViz = new SimpleMatrixVisualizer();
		MatrixViz.initialize(som.getActivationMatrix(), scaled);
		setLayout(new GridLayout(0, 1, 0, 0));
		
		MatrixViz.setPreferredSize(getPreferredSize());
		
		add(MatrixViz);
	}
	
	
	/**
	 * Updates the activation panel. Has to be called before .repaint() Else no effect
	 * on the map
	 */
	public void updateData() {
		this.MatrixViz.registerMatrix(som.getActivationMatrix());
		this.MatrixViz.revalidate();
		this.MatrixViz.repaint();
	}
}
