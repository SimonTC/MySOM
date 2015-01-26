package dk.stcl.experiments.movinglines;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.gui.MatrixPanel;
import dk.stcl.gui.SomPanel;
import dk.stcl.som.basic.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.rsom.IRSOM;



public class MovingLinesGUI extends JFrame {
	private MatrixPanel input;
	private SomPanel spatialModels;
	private MatrixPanel spatialActivation;
	private SomPanel possibleInputs;
	private SomPanel rsomModel1;
	private SomPanel rsomModel2;
	private SomPanel rsomModel3;
	private SomPanel rsomModel4;
	private MatrixPanel rsomActivation1;
	private MatrixPanel rsomActivation2;
	private MatrixPanel rsomActivation3;
	private MatrixPanel rsomActivation4;
	
	private int singleSomModelWidth;
	
	public MovingLinesGUI(ISomBasics spatialSom, ISomBasics possibleInputsSom) {
		//Create overall grid layout
		int rows = 3;
		int cols = 4;
		int gap = 2;
		setLayout(new GridLayout(rows, cols, gap, gap));
		
		int inputLength = spatialSom.getInputVectorLength();
		
		//Set preffered size of GUI
		int somHeight = spatialSom.getHeight(); //This should be equal to the width also
		singleSomModelWidth =  (int) Math.sqrt(spatialSom.getInputVectorLength()); //How many cells (size x size) are there in a single SOM model
		int somModelCellSize = 5;	  //How many pixels (size x size) does a single cell in a SOM model require in height and width
		int gapBetweenSomodels = 2;
		
		int somPanelSize = somHeight * singleSomModelWidth * gapBetweenSomodels * somModelCellSize;
		
		setPreferredSize(new Dimension(cols * somPanelSize, rows * somPanelSize));
		
		
		//Add input area
		input = new MatrixPanel(new SimpleMatrix(singleSomModelWidth, singleSomModelWidth), true);
		input.setSize(singleSomModelWidth, singleSomModelWidth);
		add(input);
		
		//Add visualization of spatial som models
		spatialModels = new SomPanel(spatialSom, singleSomModelWidth, singleSomModelWidth);
		spatialModels.setSize(somPanelSize, somPanelSize);
		add(spatialModels);
		
		//Add visualization of spatial activation
		spatialActivation = new MatrixPanel(new SimpleMatrix(5, 5), true);
		spatialActivation.setSize(somPanelSize, somPanelSize);
		add(spatialActivation);
		
		//Add blank (Or the different figures that can be seen
		if (possibleInputsSom != null){
			possibleInputs = new SomPanel(possibleInputsSom, somHeight, somHeight);
			possibleInputs.setSize(somPanelSize, somPanelSize);
			add(possibleInputs);
		}
		
		// 2 x RSOM models
		rsomModel1 = new SomPanel(spatialSom, somHeight, somHeight);
		rsomModel1.setSize(somPanelSize, somPanelSize);
		add(rsomModel1);
		
		rsomModel2 = new SomPanel(spatialSom, somHeight, somHeight);
		rsomModel2.setSize(somPanelSize, somPanelSize);
		add(rsomModel2);
		
		//Add two of the RSOM activation cells
		rsomActivation1 = new MatrixPanel(new SimpleMatrix(1, 1), true);
		rsomActivation1.setSize(somPanelSize, somPanelSize);
		add(rsomActivation1);
		
		rsomActivation2 = new MatrixPanel(new SimpleMatrix(1, 1), true);
		rsomActivation2.setSize(somPanelSize, somPanelSize);
		add(rsomActivation2);
		
		// 2 x RSOM models
		rsomModel3 = new SomPanel(spatialSom, somHeight, somHeight);
		rsomModel3.setSize(somPanelSize, somPanelSize);
		add(rsomModel3);
		
		rsomModel4 = new SomPanel(spatialSom, somHeight, somHeight);
		rsomModel4.setSize(somPanelSize, somPanelSize);
		add(rsomModel4);
		
		//Add two of the RSOM activation cells
		rsomActivation3 = new MatrixPanel(new SimpleMatrix(1, 1),true);
		rsomActivation3.setSize(somPanelSize, somPanelSize);
		add(rsomActivation3);
		
		rsomActivation4 = new MatrixPanel(new SimpleMatrix(1, 1),true);
		rsomActivation4.setSize(somPanelSize, somPanelSize);
		add(rsomActivation4);		
	}
	
	public void updateData(SimpleMatrix inputVector, ISomBasics spatialPooler, IRSOM temporalPooler){
		
		//Update input area
		SimpleMatrix inputMatrix = new SimpleMatrix(inputVector);
		inputMatrix.reshape(singleSomModelWidth, singleSomModelWidth);
		input.registerMatrix(inputMatrix);
		input.repaint();
		input.revalidate();
		
		//Update spatial activation
		SimpleMatrix activationMatrix = spatialPooler.computeActivationMatrix();
		spatialActivation.registerMatrix(activationMatrix);
		spatialActivation.repaint();
		spatialActivation.revalidate();
		
		
		//Create list of spatial models to be highlighted
		int maxID = findIDOfMaxValue(activationMatrix);
		boolean[] highlights = new boolean[activationMatrix.getMatrix().data.length];
		highlights[maxID] = true;
		
		//Update Spatial models
		ISomBasics spatialSom = spatialPooler;
		spatialModels.updateData(spatialSom, highlights);
		spatialModels.repaint();
		spatialModels.revalidate();	
		
		if (temporalPooler != null){
		
			//Update RSOM activation
			SimpleMatrix temporalActivationMatrix = temporalPooler.computeActivationMatrix();
			double tmp1[][] = {{temporalActivationMatrix.get(0, 0)}};
			rsomActivation1.registerMatrix(new SimpleMatrix(tmp1));
			rsomActivation1.repaint();
			rsomActivation1.revalidate();
			
			double tmp2[][] = {{temporalActivationMatrix.get(0, 1)}};
			rsomActivation2.registerMatrix(new SimpleMatrix(tmp2));
			rsomActivation2.repaint();
			rsomActivation2.revalidate();
			
			double tmp3[][] = {{temporalActivationMatrix.get(1, 0)}};
			rsomActivation3.registerMatrix(new SimpleMatrix(tmp3));
			rsomActivation3.repaint();
			rsomActivation3.revalidate();
			
			double tmp4[][] = {{temporalActivationMatrix.get(1, 1)}};
			rsomActivation4.registerMatrix(new SimpleMatrix(tmp4));
			rsomActivation4.repaint();
			rsomActivation4.revalidate();
		
			//Update RSOM models
			
			rsomModel1.updateData(spatialSom, getSpatialModelsInTemporalModel(temporalPooler, 0));
			rsomModel1.repaint();
			rsomModel1.revalidate();
			
			rsomModel2.updateData(spatialSom, getSpatialModelsInTemporalModel(temporalPooler, 1));
			rsomModel2.repaint();
			rsomModel2.revalidate();
			
			rsomModel3.updateData(spatialSom, getSpatialModelsInTemporalModel(temporalPooler, 2));
			rsomModel3.repaint();
			rsomModel3.revalidate();
			
			rsomModel4.updateData(spatialSom, getSpatialModelsInTemporalModel(temporalPooler, 3));
			rsomModel4.repaint();
			rsomModel4.revalidate();
		}
		
		
		
	}
	
	private boolean[] getSpatialModelsInTemporalModel(IRSOM temporalModels, int modelID){
		//Collect Model
		SomNode model = temporalModels.getNode(modelID);
		
		//Create boolean vector where an item is true if weight value of that item is higher than the mean.
		SimpleMatrix weightVector = model.getVector();
		int vectorSize = weightVector.numCols() * weightVector.numRows();
		double mean = weightVector.elementSum() / (double)vectorSize;
		double threshold = 2 * ( 1 / (temporalModels.getHeight() * temporalModels.getWidth())); 
		boolean[] importantModels = new boolean[vectorSize];
		
		for (int i = 0; i <vectorSize; i++){
			if (weightVector.get(i) > mean){
				importantModels[i] = true;
			}
		}
		
		
		return importantModels;
	}
	
	/**
	 * 
	 * @param m
	 * @return id of cell with max value
	 */
	private int findIDOfMaxValue(SimpleMatrix m){
		double maxValue = Double.NEGATIVE_INFINITY;
		int maxID = -1;
		for (int row = 0; row < m.numRows(); row++){
			for (int col = 0; col < m.numCols(); col++){
				double value = m.get(row, col);
				if (value > maxValue){
					maxValue = value;
					maxID = m.getIndex(row, col);
				}
			}
		}
		return maxID;
	}

	

}
