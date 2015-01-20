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

import dk.stcl.som.basic.ISomBasics;
import dk.stcl.som.containers.SomNode;

public class SomModelDrawer extends JFrame {

	private ISomBasics som;
	private ArrayList<MapPanel> panels;
	
	public SomModelDrawer(ISomBasics som, int totalSize) throws HeadlessException {
		this.som = som;
		
		setPreferredSize(new Dimension(totalSize, totalSize));
		
		setLayout(new GridLayout(som.getHeight(), som.getWidth()));		
		//Create panels
		panels = new ArrayList<SomModelDrawer.MapPanel>();
		for (SomNode n : som.getNodes()){
			int modelSize = (int) Math.sqrt(n.getVector().numCols());
			MapPanel p = new MapPanel(n, modelSize);
			panels.add(p);
			add(p);
		}
	}

	public void updateData() {
		
		for (MapPanel p: panels){
			p.revalidate();
			p.repaint();
		}
	}

	class MapPanel extends JPanel {
		private int modelSize;
		SomNode n;
		
		public MapPanel(SomNode n, int modelSize){
			super();
			this.n = n;
			this.modelSize = modelSize;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			//Get data
			double[] data = n.getVector().getMatrix().data;
			SimpleMatrix matrix = new SimpleMatrix(modelSize, modelSize, true, data);

			// Clear the map
			g.clearRect(0, 0, getWidth(), getHeight());

			// Draw the grid
			int cellWidth = getWidth() / modelSize;
			int cellHeight = getHeight() / modelSize;
			
			for (int x=0; x<modelSize; x++) {
				for (int y=0; y<modelSize; y++) {
					double value = matrix.get(y, x);
					int rgb = (int) ((1-value) * 255);
					Color c = new Color(rgb, rgb, rgb);
					g.setColor(c);
					g.fillRect((int)(x*cellWidth), (int)(y*cellHeight),
								(int)cellWidth, (int)cellHeight);
				}
			}
		}
	}
	

}
