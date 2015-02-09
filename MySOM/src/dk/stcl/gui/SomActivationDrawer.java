package dk.stcl.gui;

import java.awt.Dimension;

import javax.swing.JFrame;

import dk.stcl.core.som.ISOM;

/**
 * Visualizes the activation of a SOM map.
 * @author Simon
 *
 */
public class SomActivationDrawer extends JFrame {

	private ISOM som;
	private MatrixPanel panel;
	
	/**
	 * 
	 * @param frameSize
	 * @param som
	 * @param scaled true if the values are scaled between 0 and 1
	 */
	public SomActivationDrawer(int frameSize, ISOM som, boolean scaled){
		this.som = som;
		
		setPreferredSize(new Dimension(frameSize, frameSize));
		
		panel = new MatrixPanel(som.computeActivationMatrix(), scaled);
		
		add(panel);
	}
	
	
	/**
	 * Updates the activation panel. Has to be called before .repaint() Else no effect
	 * on the map
	 */
	public void updateData() {
		this.panel.registerMatrix(som.computeActivationMatrix());
		this.panel.revalidate();
		this.panel.repaint();
	}

}
