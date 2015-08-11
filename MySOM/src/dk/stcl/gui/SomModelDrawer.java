package dk.stcl.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.som.SOM_Simple;

/**
 * Visualizes all the internal models of a som map
 * @author Simon
 *
 */
public class SomModelDrawer extends JPanel {
	private boolean[] highlightList;
	
	public SomModelDrawer() {
		ISomBasics som = new SOM_Simple(3, 5, 0, 0, 1);
		this.initialize(som, true);		
	}

	private ArrayList<SomNodeWeightsVisualizer> panels;
	
	public void initialize(ISomBasics som, boolean scaledWeights){
		this.removeAll();
		int mapSize = som.getHeight();
		setLayout(new GridLayout(mapSize, mapSize, 5, 5));
		
		//Create panels
		int panelWidth = getPreferredSize().width / mapSize;
		int panelHeight = getPreferredSize().height / mapSize;
		panels = new ArrayList<SomNodeWeightsVisualizer>();
		for (SomNode n : som.getNodes()){
			int modelMapSize = (int) Math.sqrt(n.getVector().getNumElements());
			SomNodeWeightsVisualizer p = new SomNodeWeightsVisualizer();
			p.setPreferredSize(new Dimension(panelWidth, panelHeight));
			p.initialize(n, modelMapSize, scaledWeights);
			//p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			panels.add(p);
			add(p);
		}
		
		highlightList = new boolean[panels.size()];
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		updatePanels();
	}
	
	public void updateHighlighList(boolean[] newHighlightList){
		this.highlightList = newHighlightList;
	}
	
	private void updatePanels(){
		for (int i = 0; i < panels.size(); i++){
			SomNodeWeightsVisualizer p = panels.get(i);
			p.updateData();
			if (highlightList[i]){
				p.setBorder(BorderFactory.createLineBorder(Color.RED));
			} else {
				p.setBorder(BorderFactory.createEmptyBorder());
			}
		}
		for (SomNodeWeightsVisualizer p : panels){
			p.updateData();
		}
	}
}
