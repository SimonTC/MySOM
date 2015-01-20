package dk.stcl.experiments.movinglines;

import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.basic.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.rsom.IRSOM;
import dk.stcl.som.rsom.RSOM;
import dk.stcl.som.som.ISOM;
import dk.stcl.som.som.PLSOM;
import dk.stcl.som.som.SOM;

	

public class TwoDLines {
	
	private HashMap<Integer, Integer> somLabelMap;
	private HashMap<Integer, Integer> rsomLabelMap;
	
	private SimpleMatrix[][] sequences;
	private ISOM spatialPooler;
	private ISOM possibleInputs;
	private IRSOM temporalPooler;
	private MovingLinesGUI frame;
	private final int GUI_SIZE = 500;
	private final int MAX_ITERTIONS = 10000;
	private final int FRAMES_PER_SECOND = 10;
	
	private final double DECAY = 0.3;
	
	private final boolean VISUAL_RUN = true;
	
	private final boolean VISUAL_TRAINING = false;
	
	private Random rand = new Random();
	
	private final boolean USE_PLSOM = false;
	private final int STDDEV = 2;
	private final int SOM_SIZE = 5;


	public static void main(String[] args){
		TwoDLines runner = new TwoDLines();
		runner.run();
	}
	
	private void run(){
		boolean visualize = VISUAL_TRAINING;
		double[] fitness = new double[2];
		
		for (int i = 0; i < 10; i++){
			setupExperiment(rand, visualize);
			runExperiment(MAX_ITERTIONS, visualize, rand);
			temporalPooler.flush();
			temporalPooler.setLearning(false);
			spatialPooler.setLearning(false);
			label();
			double[] curFitness = validate();
			fitness[0] += curFitness[0];
			fitness[1] += curFitness[1];
		}
		
		System.out.println("SOM fitness: " + ((double) fitness[0] / 10));
		System.out.println("RSOM fitness: " + ((double) fitness[1] / 10));
		
		if  (VISUAL_RUN) visualRun(rand);
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
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
	    SimpleMatrix[] seq;
	    int curSeqID = 0;
	    
	    if (visualize){
	    	setupVisualization(spatialPooler, GUI_SIZE);
	    }
	    
	    for (int i = 1; i <= maxIterations; i++){
	    	//Choose sequence
	    	boolean change = rand.nextDouble() > 0.9 ? true : false;
	    	
	    	if (change){
				boolean choose = rand.nextBoolean();
				switch (curSeqID){
				case 0 : curSeqID = choose ? 1 : 2; break;
				case 1 : curSeqID = choose ? 2 : 0; break;
				case 2 : curSeqID = choose ? 0 : 1; break;
				}
			} 
			int curInputID = rand.nextInt(3);
			seq = sequences[curSeqID];
			
			SimpleMatrix input = seq[curInputID];
			step(input);
			
			if (visualize){
				//Visualize
    			updateGraphics(input,i);					
				try {
					Thread.sleep(SKIP_TICKS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
	    
	    if (visualize){
		    frame.setVisible(false);
		    frame.dispose();
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
			temporalPooler.flush();
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
			SomNode somBMU = spatialPooler.getBMU();
			SomNode rsomBMU = temporalPooler.getBMU();
			
			if (somBMU.getLabel() == somLabelMap.get(hash)) somCorrect++;
			if (rsomBMU.getLabel() == rsomLabelMap.get(hash)) rsomCorrect++;
			temporalPooler.flush();
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
	
	private void step(SimpleMatrix input){
		//Spatial classification	    		
		spatialPooler.step(input.getMatrix().data);
		SimpleMatrix spatialActivation = spatialPooler.computeActivationMatrix();
		
		//Transform spatial output matrix to vector
		double[] spatialOutputVector = spatialActivation.getMatrix().data;
		
		//Temporal classification
		temporalPooler.step(spatialOutputVector);	    	
	}
	
	private void doSequence(SimpleMatrix[] sequence, boolean visualize, int iteration){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
		for (SimpleMatrix m : sequence){
    			
    		step(m);
    		
    		if (visualize){
				//Visualize
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
		//Spatial pooler
		int spatialInputLength = 9;
		int spatialMapSize = SOM_SIZE;
		
		
		if (USE_PLSOM){
			spatialPooler = new PLSOM(spatialMapSize, spatialMapSize, spatialInputLength, rand, 0.1, STDDEV, 0.125);
		} else {
			spatialPooler = new SOM(spatialMapSize, spatialMapSize, spatialInputLength, rand, 0.1, STDDEV, 0.125);
		}	
		
		
		//Temporal pooler
		int temporalInputLength = spatialMapSize * spatialMapSize;
		int temporalMapSize = 2;
		
		
		temporalPooler = new RSOM(temporalMapSize, temporalMapSize, temporalInputLength, rand, 0.1, STDDEV, 0.125, DECAY);
		
		
	}
	
	private void buildSequences(){
		sequences = new SimpleMatrix[3][3];
		possibleInputs = new SOM(3, 3, 9, new Random(),0,0,0);
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
