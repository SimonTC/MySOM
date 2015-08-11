package dk.stcl.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.som.ISOM;

import javax.swing.JLabel;

import java.awt.Component;

import javax.swing.SwingConstants;
import javax.swing.Box;

import java.awt.Color;

public class GeneralExperimentGUI extends JFrame {
	private static Color BACKGROUND = Color.LIGHT_GRAY;
	
	public GeneralExperimentGUI() {
		getContentPane().setBackground(Color.LIGHT_GRAY);
		getContentPane().setPreferredSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(500, 500));
		
		JPanel SomArea = new JPanel();
		SomArea.setMinimumSize(new Dimension(250, 250));
		SomArea.setMaximumSize(new Dimension(250, 250));
		SomArea.setPreferredSize(new Dimension(250, 250));
		SomArea.setSize(new Dimension(500, 250));
		getContentPane().add(SomArea, BorderLayout.NORTH);
		SomArea.setLayout(new BoxLayout(SomArea, BoxLayout.X_AXIS));
		
		Component glue_4 = Box.createGlue();
		SomArea.add(glue_4);
		
		Box _inputArea = new Box(BoxLayout.Y_AXIS);
		SomArea.add(_inputArea);
		//_inputArea.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Input");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		_inputArea.add(lblNewLabel, BorderLayout.NORTH);
		
		SimpleMatrixVisualizer _inputPanel = new SimpleMatrixVisualizer();
		_inputPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		_inputArea.add(_inputPanel);
		_inputPanel.setPreferredSize(new Dimension(125, 125));
		
		Component glue = Box.createGlue();
		SomArea.add(glue);
		
		SomModelDrawer _modelPanel = new SomModelDrawer();
		_modelPanel.setPreferredSize(new Dimension(125, 125));
		SomArea.add(_modelPanel);
		
		Component glue_1 = Box.createGlue();
		SomArea.add(glue_1);
		
		SomActivationVisualizer _activationPanel = new SomActivationVisualizer();
		_activationPanel.setPreferredSize(new Dimension(125, 125));
		SomArea.add(_activationPanel);
		
		Component glue_2 = Box.createGlue();
		SomArea.add(glue_2);
		
		SimpleMatrixVisualizer _miscPanel = new SimpleMatrixVisualizer();
		_miscPanel.setPreferredSize(new Dimension(125, 125));
		SomArea.add(_miscPanel);
		
		Component glue_3 = Box.createGlue();
		SomArea.add(glue_3);
		
		JPanel rsomArea = new JPanel();
		rsomArea.setPreferredSize(new Dimension(250, 250));
		getContentPane().add(rsomArea, BorderLayout.SOUTH);
		rsomArea.setLayout(new GridLayout(0, 2, 0, 0));
		
		SomModelDrawer _somModelsInRSOMPanel = new SomModelDrawer();
		rsomArea.add(_somModelsInRSOMPanel);
		
		SomActivationVisualizer _RsomAcrivationPanel = new SomActivationVisualizer();
		rsomArea.add(_RsomAcrivationPanel);
	}
	
	private SimpleMatrixVisualizer inputPanel;
	private SomModelDrawer modelPanel;
	private SomActivationVisualizer activationPanel;
	private SomModelDrawer possibleInputsPanel;
	private SomModelDrawer somModelsInRSOMPanel;
	private SomActivationVisualizer rsomActivationPanel;
	
	public void initialize(ISOM som, IRSOM rsom, SimpleMatrix input, boolean valuesAreScaled, ISOM possibleInputs){
		this.getContentPane().removeAll();
		this.getContentPane().setBackground(Color.DARK_GRAY);
		int width = this.getPreferredSize().width;
		int height = this.getPreferredSize().height;
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel somArea = new JPanel();
		int somAreaElementHeight = 250;
		int somAreaElementWidth = 250;
		Dimension somAreaElementDimension = new Dimension(somAreaElementWidth, somAreaElementHeight);
		getContentPane().add(somArea, BorderLayout.NORTH);
		//somArea.setLayout(new GridLayout(1, 4, 0, 0));
		somArea.add(Box.createGlue());
		somArea.setBackground(BACKGROUND);
		
		Box inputArea = new Box(BoxLayout.Y_AXIS);
		JLabel lblInput = new JLabel("Input");
		lblInput.setHorizontalAlignment(SwingConstants.CENTER);
		inputPanel = new SimpleMatrixVisualizer();
		inputPanel.setPreferredSize(somAreaElementDimension);
		inputPanel.initialize(input, valuesAreScaled);
		setLayoutOfSubArea(inputArea, lblInput, inputPanel);
		somArea.add(inputArea);
		
		somArea.add(Box.createGlue());
		
		Box somModelArea = new Box(BoxLayout.Y_AXIS);
		JLabel lblSomModelArea = new JLabel("SOM models");
		modelPanel = new SomModelDrawer();
		modelPanel.setPreferredSize(somAreaElementDimension);
		modelPanel.initialize(som, valuesAreScaled);
		setLayoutOfSubArea(somModelArea, lblSomModelArea, modelPanel);
		somArea.add(somModelArea);
		
		somArea.add(Box.createGlue());
		
		Box somActivationArea = new Box(BoxLayout.Y_AXIS);
		JLabel lblSomActivationArea = new JLabel("SOM activation");
		activationPanel = new SomActivationVisualizer();
		activationPanel.setPreferredSize(somAreaElementDimension);
		activationPanel.initialize(som, valuesAreScaled);
		setLayoutOfSubArea(somActivationArea, lblSomActivationArea, activationPanel);
		somArea.add(somActivationArea);
		
		somArea.add(Box.createGlue());
		
		Box possibleInputsArea = new Box(BoxLayout.Y_AXIS);
		JLabel lblPossibleInputsArea = new JLabel("Possible inputs");
		possibleInputsPanel = new SomModelDrawer();
		possibleInputsPanel.setPreferredSize(somAreaElementDimension);
		possibleInputsPanel.initialize(possibleInputs, valuesAreScaled);
		setLayoutOfSubArea(possibleInputsArea, lblPossibleInputsArea, possibleInputsPanel);
		somArea.add(possibleInputsArea);
		
		somArea.add(Box.createGlue());
		
		JPanel rsomArea = new JPanel();
		rsomArea.setBackground(BACKGROUND);
		getContentPane().add(rsomArea, BorderLayout.CENTER);
		//rsomArea.setLayout(new GridLayout(1, 2, 0, 0));
		
		int rsomAreaElementHeight = 250;
		int rsomAreaElementWidth = 250;
		Dimension rsomAreaElementDimension = new Dimension(rsomAreaElementWidth, rsomAreaElementHeight);
		
		rsomArea.add(Box.createGlue());
		
		Box rsomModelsArea = new Box(BoxLayout.Y_AXIS);
		JLabel lblRsomModelsArea = new JLabel("RSOM models");
		somModelsInRSOMPanel = new SomModelDrawer();
		somModelsInRSOMPanel.setPreferredSize(rsomAreaElementDimension);
		somModelsInRSOMPanel.initialize(rsom, valuesAreScaled);
		setLayoutOfSubArea(rsomModelsArea, lblRsomModelsArea, somModelsInRSOMPanel);
		rsomArea.add(rsomModelsArea);
		
		rsomArea.add(Box.createGlue());
		
		Box rsomActivationsArea = new Box(BoxLayout.Y_AXIS);
		JLabel lblRsomActivationArea = new JLabel("RSOM activation");
		rsomActivationPanel = new SomActivationVisualizer();
		rsomActivationPanel.setPreferredSize(rsomAreaElementDimension);
		rsomActivationPanel.initialize(rsom, valuesAreScaled);
		setLayoutOfSubArea(rsomActivationsArea, lblRsomActivationArea, rsomActivationPanel);
		rsomArea.add(rsomActivationsArea);
		
		rsomArea.add(Box.createGlue());
	}
	
	public void updateData(SimpleMatrix inputMatrix, SimpleMatrix miscMatrix){
		inputPanel.registerMatrix(inputMatrix);
		activationPanel.updateData();
		rsomActivationPanel.updateData();
		
	}
	
	private void setLayoutOfSubArea(Box area, JLabel label, JPanel panel){
		//area.setLayout(new BorderLayout());
		panel.setBackground(BACKGROUND);
		area.setBackground(BACKGROUND);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		area.add(label, BorderLayout.NORTH);
		area.add(panel, BorderLayout.CENTER);
	}

	

}
