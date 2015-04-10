package dk.stcl.gui;

import javax.swing.JPanel;

import dk.stcl.core.basic.ISomBasics;

/**
 * Visualizes the activation of a SOM map.
 * @author Simon
 *
 */
public class SomActivationVisualizer extends JPanel {

	private ISomBasics som;
	private SimpleMatrixVisualizer panel;
	
	/**
	 * 
	 * @param frameSize
	 * @param som
	 * @param scaled true if the values are scaled between 0 and 1
	 */
	public void initialize(ISomBasics som, boolean scaled){
		this.som = som;
		
		panel = new SimpleMatrixVisualizer();
		panel.initialize(som.getActivationMatrix(), scaled);
		
		panel.setPreferredSize(getPreferredSize());
		
		add(panel);
	}
	
	
	/**
	 * Updates the activation panel. Has to be called before .repaint() Else no effect
	 * on the map
	 */
	public void updateData() {
		this.panel.registerMatrix(som.getActivationMatrix());
		this.panel.revalidate();
		this.panel.repaint();
	}

}
