package dk.stcl.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;

/**
 * Visualizes all the internal models of a som map
 * @author Simon
 *
 */
public class SomModelDrawer extends JPanel {

	private ArrayList<SomNodeWeightsVisualizer> panels;
	
	public void initialize(ISomBasics som, boolean scaledWeights){
		int mapSize = som.getHeight();
		setLayout(new GridLayout(mapSize, mapSize, 2, 2));		
		
		//Create panels
		int panelWidth = getPreferredSize().width / mapSize;
		int panelHeight = getPreferredSize().height / mapSize;
		panels = new ArrayList<SomNodeWeightsVisualizer>();
		for (SomNode n : som.getNodes()){
			SomNodeWeightsVisualizer p = new SomNodeWeightsVisualizer();
			p.setPreferredSize(new Dimension(panelWidth, panelHeight));
			p.initialize(n, mapSize, scaledWeights);
			panels.add(p);
			add(p);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		updatePanels();
	}
	
	private void updatePanels(){
		for (SomNodeWeightsVisualizer p : panels){
			p.updateData();
		}
	}
}
