package dk.stcl.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.som.ISOM;

public class GeneralExperimentGUI extends JFrame {
	
	private SimpleMatrixVisualizer inputPanel;
	private SomModelDrawer modelPanel;
	private SomActivationVisualizer activationPanel;
	private SimpleMatrixVisualizer miscPanel;
	private SomModelDrawer somModelsInRSOMPanel;
	private SomActivationVisualizer rsomActivationPanel;
	
	

	public void initialize(ISOM som, IRSOM rsom, SimpleMatrix input, boolean valuesAreScaled, SimpleMatrix miscMatrix){
		int width = this.getPreferredSize().width;
		int height = this.getPreferredSize().height;
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel somArea = new JPanel();
		int somAreaElementHeight = height / 2;
		int somAreaElementWidth = width / 4;
		Dimension somAreaElementDimension = new Dimension(somAreaElementWidth, somAreaElementHeight);
		getContentPane().add(somArea, BorderLayout.NORTH);
		somArea.setLayout(new GridLayout(1, 4, 0, 0));
		
		inputPanel = new SimpleMatrixVisualizer();
		inputPanel.setPreferredSize(somAreaElementDimension);
		inputPanel.initialize(input, valuesAreScaled);
		somArea.add(inputPanel);
		
		modelPanel = new SomModelDrawer();
		modelPanel.setPreferredSize(somAreaElementDimension);
		modelPanel.initialize(som, valuesAreScaled);
		somArea.add(modelPanel);
		
		activationPanel = new SomActivationVisualizer();
		activationPanel.setPreferredSize(somAreaElementDimension);
		activationPanel.initialize(som, valuesAreScaled);
		somArea.add(activationPanel);
		
		miscPanel = new SimpleMatrixVisualizer();
		miscPanel.setPreferredSize(somAreaElementDimension);
		miscPanel.initialize(miscMatrix, valuesAreScaled);
		somArea.add(miscPanel);
		
		
		JPanel rsomArea = new JPanel();
		getContentPane().add(rsomArea, BorderLayout.CENTER);
		rsomArea.setLayout(new GridLayout(0, 2, 0, 0));
		
		int rsomAreaElementHeight = height / 2;
		int rsomAreaElementWidth = width / 2;
		Dimension rsomAreaElementDimension = new Dimension(rsomAreaElementWidth, rsomAreaElementHeight);
		
		somModelsInRSOMPanel = new SomModelDrawer();
		somModelsInRSOMPanel.setPreferredSize(rsomAreaElementDimension);
		somModelsInRSOMPanel.initialize(rsom, valuesAreScaled);
		rsomArea.add(somModelsInRSOMPanel);
		
		rsomActivationPanel = new SomActivationVisualizer();
		rsomActivationPanel.setPreferredSize(rsomAreaElementDimension);
		rsomActivationPanel.initialize(rsom, valuesAreScaled);
		rsomArea.add(rsomActivationPanel);
	}
	
	public void updateData(SimpleMatrix miscMatrix){
		
		activationPanel.updateData();
		miscPanel.registerMatrix(miscMatrix);
		rsomActivationPanel.updateData();
	}

	

}
