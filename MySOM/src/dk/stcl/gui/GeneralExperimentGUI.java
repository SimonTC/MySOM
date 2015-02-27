package dk.stcl.gui;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.som.ISOM;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class GeneralExperimentGUI extends JFrame {
	private SimpleMatrixVisualizer inputPanel;
	private SomModelDrawer modelPanel;
	private SomActivationVisualizer activationPanel;
	private SimpleMatrixVisualizer miscPanel;
	private SomModelDrawer somModelsInRSOMPanel;
	private SomActivationVisualizer rsomActivationPanel;
	
	

	public void initialize(ISOM som, IRSOM rsom, SimpleMatrix input, boolean valuesAreScaled, SimpleMatrix miscMatrix){
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel SomArea = new JPanel();
		getContentPane().add(SomArea, BorderLayout.NORTH);
		SomArea.setLayout(new GridLayout(1, 4, 0, 0));
		
		inputPanel = new SimpleMatrixVisualizer();
		inputPanel.initialize(input, valuesAreScaled);
		SomArea.add(inputPanel);
		
		modelPanel = new SomModelDrawer();
		modelPanel.initialize(som, valuesAreScaled);
		SomArea.add(modelPanel);
		
		activationPanel = new SomActivationVisualizer();
		activationPanel.initialize(som, valuesAreScaled);
		SomArea.add(activationPanel);
		
		miscPanel = new SimpleMatrixVisualizer();
		miscPanel.initialize(miscMatrix, valuesAreScaled);
		SomArea.add(miscPanel);
		
		JPanel rsomArea = new JPanel();
		getContentPane().add(rsomArea, BorderLayout.CENTER);
		rsomArea.setLayout(new GridLayout(0, 2, 0, 0));
		
		somModelsInRSOMPanel = new SomModelDrawer();
		somModelsInRSOMPanel.initialize(rsom, valuesAreScaled);
		rsomArea.add(somModelsInRSOMPanel);
		
		rsomActivationPanel = new SomActivationVisualizer();
		rsomActivationPanel.initialize(rsom, valuesAreScaled);
		rsomArea.add(rsomActivationPanel);
	}
	
	public void updateData(SimpleMatrix miscMatrix){
		
		activationPanel.updateData();
		miscPanel.registerMatrix(miscMatrix);
		rsomActivationPanel.updateData();
	}

	

}
