package dk.stcl.experiments.visualization;

import java.awt.Dimension;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.gui.SomModelDrawer;
import dk.stcl.gui.SomNodeWeightsVisualizer;

public class FrameDemo{

	public static void main(String args[]){
		JFrame myFrame = new JFrame("This is my frame");
		//myFrame.setPreferredSize(new Dimension(500, 500));
		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		
		SomModelDrawer viz = new SomModelDrawer();		
		viz.setPreferredSize(new Dimension(100,100));
		ISomBasics som = new SOM_SemiOnline(3, 9,new Random(), 0.1, 0.125, 0.3);
		viz.initialize(som, true);
		myFrame.add(viz);
		myFrame.pack();
		myFrame.setVisible(true);
	}
}

