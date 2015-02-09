package dk.stcl.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;


public class SomPanel extends JPanel {

	private ISomBasics som;
	private int somModelsRows;
	private int somModelsColumns;
	private MatrixPanel[] panels;
	
	public SomPanel(ISomBasics som, int somModelsRows, int somModelsColumns) {
		this.som = som;
		this.somModelsRows = somModelsRows;
		this.somModelsColumns = somModelsColumns;
		
		//Create grid
		int rows = som.getHeight();
		int cols = som.getWidth();		
		setLayout(new GridLayout(rows, cols, 2, 2));
		panels = new MatrixPanel[rows * cols];
		
		//Add matrixPanels
		for (int i = 0; i < som.getNodes().length; i++){
			MatrixPanel p = new MatrixPanel(new SimpleMatrix(somModelsRows, somModelsColumns), true);
			add(p);
			panels[i] = p;
		}
		
	}
	
	public void updateData(ISomBasics somModel){
		this.som = somModel;		
		for (int i = 0; i < som.getNodes().length; i++){
			SomNode n = som.getNode(i);
			SimpleMatrix m = new SimpleMatrix(n.getVector());
			m.reshape(somModelsRows, somModelsColumns);
			MatrixPanel p = panels[i];
			p.registerMatrix(m);
			p.revalidate();
			p.repaint();			
		}
	}
	
	public void updateData(ISomBasics somModel, boolean[]highlightList){
		this.som = somModel;		
		for (int i = 0; i < som.getNodes().length; i++){
			SomNode n = som.getNode(i);
			SimpleMatrix m = new SimpleMatrix(n.getVector());
			m.reshape(somModelsRows, somModelsColumns);
			MatrixPanel p = panels[i];
			if (highlightList[i]){
				p.setBorder(BorderFactory.createLineBorder(Color.RED));
			} else {
				p.setBorder(BorderFactory.createEmptyBorder());
			}
			p.registerMatrix(m);
			p.revalidate();
			p.repaint();			
		}
	}
	
	

	

}
