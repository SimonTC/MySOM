package dk.stcl.experiments.visualization;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.gui.SomModelDrawer;
import dk.stcl.gui.SomNodeWeightsVisualizer;

public class FrameDemo{

	public static void main(String args[]) throws InterruptedException{
		JFrame myFrame = new JFrame("This is my frame");
		//myFrame.setPreferredSize(new Dimension(500, 500));
		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		myFrame.setLayout(new GridBagLayout());
		
		
		SomModelDrawer viz = new SomModelDrawer();		
		viz.setPreferredSize(new Dimension(500,500));
		ISomBasics som = new SOM_SemiOnline(3, 9,new Random(), 0.1, 0.125, 0.3);
		viz.initialize(som, true);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.BOTH;
		myFrame.add(viz, constraints);
		myFrame.pack();
		myFrame.setVisible(true);
		
		for (int i = 0; i < 10000; i++){
			SimpleMatrix input = createInput();
			som.step(input);
			myFrame.repaint();
			Thread.sleep(100);
		}
	}
	
	private static SimpleMatrix createInput(){
		double[] inputs = new double[9];
		Random rand = new Random();
		for (int i = 0; i < inputs.length; i++){
			inputs[i] = rand.nextDouble();
		}
		
		SimpleMatrix m = new SimpleMatrix(1, 9, true, inputs);
		return m;
	}
	
	
}

