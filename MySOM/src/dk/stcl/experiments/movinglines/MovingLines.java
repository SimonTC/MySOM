package dk.stcl.experiments.movinglines;

import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.gui.SomModelDrawer;
import dk.stcl.som.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.offline.som.SomOffline;
import dk.stcl.som.online.rsom.RSOM;
import dk.stcl.som.online.som.PLSOM;

	

public class MovingLines {
	
	private SimpleMatrix[][] sequences;
	private ISomBasics spatialPooler;
	private SomOffline possibleInputs;
	private RSOM temporalPooler;
	private MovingLinesGUI frame;
	private final int GUI_SIZE = 500;
	private final int MAX_ITERTIONS = 1000;
	private final boolean USE_PLSOM = false;


	public static void main(String[] args){
		MovingLines runner = new MovingLines();
		runner.run();
	}
	
	private void run(){
		boolean visualize = true;
		Random rand = new Random(1234);
		setupExperiment(rand, visualize);
		runExperiment(MAX_ITERTIONS, visualize, rand);
	}
	
	private void runExperiment(int maxIterations, boolean visualize, Random rand){
		int FRAMES_PER_SECOND = 30;
	    int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
	    SimpleMatrix[] seq;
	    
	    for (int i = 1; i <= maxIterations; i++){
	    	//Choose sequence
	    	seq = sequences[rand.nextInt(sequences.length)];
	    	
	    	//temporalPooler.sensitize(i, maxIterations);
	    	
	    	for (SimpleMatrix m : seq){
	    		//Spatial classification	    		
	    		spatialPooler.step(m.getMatrix().data);
	    		SimpleMatrix spatialActivation = spatialPooler.computeActivationMatrix();
	    		
	    		//Transform spatial output matrix to vector
	    		double[] spatialOutputVector = spatialActivation.getMatrix().data;
	    		
	    		//Orthogonalize output
	    		double[] orthogonalized = orthogonalize(spatialOutputVector);
	    		
	    		//Temporal classification
	    		temporalPooler.step(orthogonalized);	    		
	    		
	    		if (visualize){
					//Visualize
	    			updateGraphics(m,i);					
					try {
						Thread.sleep(SKIP_TICKS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    		
	    		//Sensitize som
	    		spatialPooler.sensitize(i, maxIterations);
	    	}
	    	temporalPooler.flush();
	    }
	}
	
	private double[] orthogonalize(double[] vector){
		int maxID = -1;
		double maxValue = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < vector.length; i++){
			double d = vector[i];
			if (d > maxValue){
				maxValue = d;
				maxID = i;
			}
		}
		
		double[] newVector = new double[vector.length];
		newVector[maxID] = 1;
		return newVector;
	}
	
	private void updateGraphics(SimpleMatrix inputVector, int iteration){
		frame.updateData(inputVector, spatialPooler, temporalPooler);
		frame.setTitle("Visualiztion - Iteration: " + iteration);
		frame.revalidate();
		frame.repaint();

	}
	
	private void setupExperiment(Random rand, boolean visualize){
		setupPoolers(rand);
		buildSequences();
		if (visualize){
			setupVisualization(spatialPooler, GUI_SIZE);
		}
	}
	
	private void setupVisualization(ISomBasics som, int GUI_SIZE){
		//Create GUI
		frame = new MovingLinesGUI(som, possibleInputs);
		frame.setTitle("Visualiztion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		updateGraphics(sequences[2][0],0); //Give a blank
		frame.pack();
		frame.setVisible(true);	
	}
	
	private void setupPoolers(Random rand){		
		//Spatia pooler
		int spatialInputLength = 9;
		int spatialMapSize = 5;
		
		if (USE_PLSOM){
			spatialPooler = new PLSOM(spatialMapSize, spatialMapSize, spatialInputLength, rand);
		} else {
			spatialPooler = new SomOffline(spatialMapSize, spatialMapSize, spatialInputLength, rand);
		}
		
		//Temporal pooler
		int temporalInputLength = spatialMapSize * spatialMapSize;
		int temporalMapSize = 2;
		double decayFactor = 0.7;
		temporalPooler = new RSOM(temporalMapSize, temporalMapSize, temporalInputLength, rand, decayFactor);
	}
	
	private void buildSequences(){
		sequences = new SimpleMatrix[3][3];
		possibleInputs = new SomOffline(3, 3, 9, new Random());
		SomNode[] nodes = possibleInputs.getNodes();
		
		SimpleMatrix m;
		
		//Horizontal down
		double[][] hor1 = {
				{1,1,1},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(hor1);
		m.reshape(1, 9);
		sequences[0][0] = m;
		nodes[0] = new SomNode(m);
		
		double[][] hor2 = {
				{0,0,0},
				{1,1,1},
				{0,0,0}};
		m = new SimpleMatrix(hor2);
		m.reshape(1, 9);
		sequences[0][1] = m;
		nodes[1] = new SomNode(m);
		
		double[][] hor3 = {
				{0,0,0},
				{0,0,0},
				{1,1,1}};
		m = new SimpleMatrix(hor3);
		m.reshape(1, 9);
		sequences[0][2] = m;
		nodes[2] = new SomNode(m);
		
		//Vertical right
		double[][] ver1 = {
				{1,0,0},
				{1,0,0},
				{1,0,0}};
		m = new SimpleMatrix(ver1);
		m.reshape(1, 9);
		sequences[1][0] = m;
		nodes[3] = new SomNode(m);
		
		double[][] ver2 = {
				{0,1,0},
				{0,1,0},
				{0,1,0}};
		m = new SimpleMatrix(ver2);
		m.reshape(1, 9);
		sequences[1][1] = m;
		nodes[4] = new SomNode(m);
		
		double[][] ver3 = {
				{0,0,1},
				{0,0,1},
				{0,0,1}};
		m = new SimpleMatrix(ver3);
		m.reshape(1, 9);
		sequences[1][2] = m;
		nodes[5] = new SomNode(m);
		
		//Blank
		double[][] blank = {
				{0,0,0},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(blank);
		m.reshape(1, 9);
		sequences[2][0] = m;
		sequences[2][1] = m;
		sequences[2][2] = m;
		nodes[6] = new SomNode(m);
		nodes[7] = new SomNode(m);
		nodes[8] = new SomNode(m);
	}

}
