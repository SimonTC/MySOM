package dk.stcl.experiments.mnist;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.gui.SimpleMatrixVisualizer;

import java.awt.GridLayout;

import dk.stcl.gui.SomModelDrawer;

public class MnistGUI extends JFrame {
	SomModelDrawer somModelDrawer;
	public MnistGUI() {
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		somModelDrawer = new SomModelDrawer();
		getContentPane().add(somModelDrawer);
	}
	
	public void initialize(ISomBasics som){
		somModelDrawer.initialize(som, true);		
	}

}
