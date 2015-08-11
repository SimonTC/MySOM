package dk.stcl.experiments.movinglines;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.rsom.RSOM_SemiOnline;
import dk.stcl.core.som.ISOM;
import dk.stcl.core.som.PLSOM;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.gui.GeneralExperimentGUI;

	

public class MovingLines {
	
	private HashMap<Integer, Integer> somLabelMap;
	private HashMap<Integer, Integer> rsomLabelMap;
	
	private SimpleMatrix[][] sequences;
	private ISOM spatialPooler;
	private ISOM possibleInputs;
	private IRSOM temporalPooler;
	private GeneralExperimentGUI frame;
	private final int GUI_SIZE = 600;
	private final int MAX_ITERTIONS = 10000;
	private final int FRAMES_PER_SECOND = 10;
	
	private final double DECAY = 0.3;
	
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
		temporalPooler.flush();
		temporalPooler.setLearning(false);
		spatialPooler.setLearning(false);
		label();
		double[] fitness = validate();
		System.out.println("SOM fitness: " + fitness[0]);
		System.out.println("RSOM fitness: " + fitness[1]);
		
		visualRun(rand);
	}
	
	private void visualRun( Random rand){
		setupVisualization(spatialPooler, GUI_SIZE);
		for (int i = 1; i < 500;i++){
			SimpleMatrix[] seq;
			seq = sequences[rand.nextInt(sequences.length)];	    	
	    	doSequence(seq, true, i);
		}
	}
	
	private void runExperiment(int maxIterations, boolean visualize, Random rand){
		
	    SimpleMatrix[] seq;
	    
	    for (int i = 1; i <= maxIterations; i++){
	    	//Choose sequence
	    	seq = sequences[rand.nextInt(sequences.length)];	    	
	    	doSequence(seq, visualize, i);
	    }
	}
	
	private void label(){
		int nextFreeSOMLabel = 0;
		int curSOMLabel = -1;
		
		int nextFreeRSOMLabel = 0;
		int curRSOMLabel = -1;
		somLabelMap = new HashMap<Integer, Integer>();
		rsomLabelMap = new HashMap<Integer, Integer>();
		
		for (SimpleMatrix[] sequence : sequences){
			int hash = toHash(sequence);
			doSequence(sequence, false, 0);
			temporalPooler.flush();
			
			if (somLabelMap.containsKey(hash)){
				curSOMLabel = somLabelMap.get(hash);
			} else {
				curSOMLabel = nextFreeSOMLabel++;
				somLabelMap.put(hash, curSOMLabel);
			}
			
			if (rsomLabelMap.containsKey(hash)){
				curRSOMLabel = rsomLabelMap.get(hash);
			} else {
				curRSOMLabel = nextFreeRSOMLabel++;
				rsomLabelMap.put(hash, curRSOMLabel);
			}
			
			SomNode somBMU = spatialPooler.getBMU();
			SomNode rsomBMU = temporalPooler.getBMU();
			
			somBMU.setLabel(curSOMLabel);
			rsomBMU.setLabel(curRSOMLabel);			
		}
	}
	
	private double[] validate(){
		double[] fitness = new double[2];
		
		int somCorrect = 0;
		int rsomCorrect = 0;
		int total = 0;
		
		for (SimpleMatrix[] sequence : sequences){
			total++;
			int hash = toHash(sequence);
			doSequence(sequence, false, 0);
			temporalPooler.flush();
			SomNode somBMU = spatialPooler.getBMU();
			SomNode rsomBMU = temporalPooler.getBMU();
			
			if (somBMU.getLabel() == somLabelMap.get(hash)) somCorrect++;
			if (rsomBMU.getLabel() == rsomLabelMap.get(hash)) rsomCorrect++;
		}
		
		fitness[0] = (double) somCorrect / total;
		fitness[1] = (double) rsomCorrect / total;
		
		return fitness;
	}
	
	private int toHash(SimpleMatrix[] input){
		String s = "";
		for (SimpleMatrix m : input){
			for (double d : m.getMatrix().data){
				s = s + (int) d;
			}
		}
		return s.hashCode();
	}
	
	private void doSequence(SimpleMatrix[] sequence, boolean visualize, int iteration){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
		for (SimpleMatrix m : sequence){
    		//Spatial classification	    
			SimpleMatrix input = new SimpleMatrix(1, 9, true, m.getMatrix().data);
    		spatialPooler.step(input);
    		SimpleMatrix spatialActivation = spatialPooler.computeActivationMatrix();
    		
    		//Transform spatial output matrix to vector
    		double[] spatialOutputVector = spatialActivation.getMatrix().data;
    		
    		double[] orthogonalized = orthogonalize(spatialOutputVector);
    		
    		//Temporal classification
    		temporalPooler.step(orthogonalized);	    		
    		
    		if (visualize){
				//Visualize
    			temporalPooler.computeActivationMatrix();
    			updateGraphics(m,iteration);					
				try {
					Thread.sleep(SKIP_TICKS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
		frame.updateData(inputVector,possibleInputs.getActivationMatrix());
		frame.setTitle("Visualization - Iteration: " + iteration);
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
		frame = new GeneralExperimentGUI();
		frame.setPreferredSize(new Dimension(500, 500));
		frame.initialize(spatialPooler, temporalPooler, sequences[2][0], true,possibleInputs);
		frame.setTitle("Visualiztion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//updateGraphics(sequences[2][0],0); //Give a blank
		frame.pack();
		frame.setVisible(true);	
	}
	
	private void setupPoolers(Random rand){		
		//Spatial pooler
		int spatialInputLength = 9;
		int spatialMapSize = 3;
		double learningRate = 0.1;
		double stddev = 1;
		double activationCodingFactor = 0.125;
		
		if (USE_PLSOM){
			spatialPooler = new PLSOM(spatialMapSize, spatialInputLength, rand, learningRate, activationCodingFactor, stddev);
		} else {
			spatialPooler = new SOM_SemiOnline(spatialMapSize, spatialInputLength, rand, learningRate, activationCodingFactor, stddev);
		}
		
		//Temporal pooler
		int temporalInputLength = spatialMapSize * spatialMapSize;
		int temporalMapSize = 2;
		
		
		temporalPooler = new RSOM_SemiOnline(temporalMapSize, temporalInputLength, rand, learningRate, activationCodingFactor, stddev, DECAY);
		
		
	}
	
	private void buildSequences(){
		sequences = new SimpleMatrix[3][3];
		possibleInputs = new SOM_SemiOnline(3, 9, new Random(), 0, 0, 0);
		SomNode[] nodes = possibleInputs.getNodes();
		
		SimpleMatrix m;
		
		//Horizontal down
		double[][] hor1 = {
				{1,1,1},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(hor1);
		sequences[0][0] = m;
		nodes[0] = new SomNode(m);
		
		double[][] hor2 = {
				{0,0,0},
				{1,1,1},
				{0,0,0}};
		m = new SimpleMatrix(hor2);
		sequences[0][1] = m;
		nodes[1] = new SomNode(m);
		
		double[][] hor3 = {
				{0,0,0},
				{0,0,0},
				{1,1,1}};
		m = new SimpleMatrix(hor3);
		sequences[0][2] = m;
		nodes[2] = new SomNode(m);
		
		//Vertical right
		double[][] ver1 = {
				{1,0,0},
				{1,0,0},
				{1,0,0}};
		m = new SimpleMatrix(ver1);
		sequences[1][0] = m;
		nodes[3] = new SomNode(m);
		
		double[][] ver2 = {
				{0,1,0},
				{0,1,0},
				{0,1,0}};
		m = new SimpleMatrix(ver2);
		sequences[1][1] = m;
		nodes[4] = new SomNode(m);
		
		double[][] ver3 = {
				{0,0,1},
				{0,0,1},
				{0,0,1}};
		m = new SimpleMatrix(ver3);
		sequences[1][2] = m;
		nodes[5] = new SomNode(m);
		
		//Blank
		double[][] blank = {
				{0,0,0},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(blank);
		sequences[2][0] = m;
		sequences[2][1] = m;
		sequences[2][2] = m;
		nodes[6] = new SomNode(m);
		nodes[7] = new SomNode(m);
		nodes[8] = new SomNode(m);
	}

}
