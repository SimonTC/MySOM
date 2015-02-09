package dk.stcl.experiments.tests.som;

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
import dk.stcl.experiments.movinglines.MovingLinesGUI;
import dk.stcl.utils.SomLabeler;

	
/**
 * This class tests if the SOM correctly labels different line elements.
 * @author Simon
 *
 */
public class Test_SOM_LineSegments {

	private SimpleMatrix[] sequences;
	private ISOM spatialPooler;
	private ISOM possibleInputs;
	private MovingLinesGUI frame;
	private final int GUI_SIZE = 500;
	private final int MAX_ITERTIONS = 1000;
	private final int FRAMES_PER_SECOND = 4;
	
	private final boolean VISUAL_RUN = true;
	
	private final boolean VISUAL_TRAINING = false;
	
	private Random rand = new Random(1234);
	
	private final boolean USE_PLSOM = false;
	private final int STDDEV = 2;
	private final int SOM_SIZE = 5;
	
	SomLabeler labeler = new SomLabeler();
	private int[] labels;


	public static void main(String[] args){
		Test_SOM_LineSegments runner = new Test_SOM_LineSegments();
		runner.run();
	}
	
	private void run(){
		boolean visualize = VISUAL_TRAINING;
		double fitness = 0;
		double totalFitness = 0;
		double validationNoiseMagnitude = 0.3;
		
		for (int i = 0; i < 100; i++){
			double noiseMagnitude = (double) i / 100;
			setupExperiment(rand, visualize);
			
			runExperiment(MAX_ITERTIONS, visualize, rand, noiseMagnitude);
			
			spatialPooler.setLearning(false);
			
			labeler.labelSOM(spatialPooler, sequences, labels);

			fitness = validate(validationNoiseMagnitude);
			totalFitness += fitness;
			
			System.out.println("Fitness = " + fitness + " Noise magnitude: " + noiseMagnitude);
			
		}
		
		System.out.println();
		System.out.println("SOM fitness: " + ((double) totalFitness / 100));
		
		if  (VISUAL_RUN) visualRun(rand);
	}
	
	private void visualRun( Random rand){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND; 
		setupVisualization(spatialPooler, GUI_SIZE);
		for (int i = 1; i < 500;i++){
			SimpleMatrix seq;
			seq = sequences[rand.nextInt(sequences.length)];	    	
			step(seq);
			
			//Visualize
			updateGraphics(seq,i);					
			try {
				Thread.sleep(SKIP_TICKS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void runExperiment(int maxIterations, boolean visualize, Random rand, double noiseMagnitude){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND; 
	    SimpleMatrix input;
	    
	    if (visualize){
	    	setupVisualization(spatialPooler, GUI_SIZE);
	    }
	    
	    for (int i = 1; i <= maxIterations; i++){
	    	int curInputID = rand.nextInt(sequences.length);
			input = sequences[curInputID];
			
			SimpleMatrix noisy = addnoise(input, noiseMagnitude);
			
			step(noisy);
			
			if (visualize){
				//Visualize
    			updateGraphics(noisy,i);					
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

	private SimpleMatrix addnoise(SimpleMatrix input, double noiseMagnitude){
		SimpleMatrix noisy = new SimpleMatrix(input);
		double[] values = noisy.getMatrix().data;
		for (int i = 0; i < values.length; i++){
			double d = values[i];
			double noise = (rand.nextDouble() - 0.5) * 2 * noiseMagnitude;
			double newValue = d + noise;
			if (newValue > 1) newValue = 1;
			if (newValue < 0) newValue = 0;
			values[i] = newValue;			
		}
		
		noisy.getMatrix().setData(values);
		
		return noisy;
	}
	
	private double validate(double noiseMagnitude){
		int numValidations = 1000;
		double fitness = 0;
		
		int somCorrect = 0;
		
		for (int i = 0; i < numValidations; i++){
			int inputID = rand.nextInt(sequences.length);
			SimpleMatrix input = sequences[inputID];
			SimpleMatrix noisy = addnoise(input, noiseMagnitude);
			step(noisy);
			int actualLabel = spatialPooler.getBMU().getLabel();
			int expectedLabel = labels[inputID];
			
			if (actualLabel == expectedLabel) somCorrect++;
		}
		
		fitness = (double) somCorrect / numValidations;
		
		return fitness;
	}
	
	
	private void step(SimpleMatrix input){
		//Spatial classification	    		
		spatialPooler.step(input.getMatrix().data);
		    	
	}
		
	private void updateGraphics(SimpleMatrix inputVector, int iteration){
		frame.updateData(inputVector, spatialPooler, null);
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
		updateGraphics(sequences[6],0); //Give a blank
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
			spatialPooler = new SOM_SemiOnline(spatialMapSize, spatialMapSize, spatialInputLength, rand, 0.1, STDDEV, 0.125);
		}	

		
		
	}
	
	private void buildSequences(){
		sequences = new SimpleMatrix[7];
		possibleInputs = new SOM_SemiOnline(3, 3, 9, new Random(),0,0,0);
		SomNode[] nodes = possibleInputs.getNodes();
		labels = new int[sequences.length];
		
		for (int i = 0; i < labels.length; i++){
			labels[i] = i;
		}
		
		SimpleMatrix m;
		
		//Horizontal down
		double[][] hor1 = {
				{1,1,1},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(hor1);
		m.reshape(1, 9);
		sequences[0] = m;
		nodes[0] = new SomNode(m);
		
		double[][] hor2 = {
				{0,0,0},
				{1,1,1},
				{0,0,0}};
		m = new SimpleMatrix(hor2);
		m.reshape(1, 9);
		sequences[1] = m;
		nodes[1] = new SomNode(m);
		
		double[][] hor3 = {
				{0,0,0},
				{0,0,0},
				{1,1,1}};
		m = new SimpleMatrix(hor3);
		m.reshape(1, 9);
		sequences[2] = m;
		nodes[2] = new SomNode(m);
		
		//Vertical right
		double[][] ver1 = {
				{1,0,0},
				{1,0,0},
				{1,0,0}};
		m = new SimpleMatrix(ver1);
		m.reshape(1, 9);
		sequences[3] = m;
		nodes[3] = new SomNode(m);
		
		double[][] ver2 = {
				{0,1,0},
				{0,1,0},
				{0,1,0}};
		m = new SimpleMatrix(ver2);
		m.reshape(1, 9);
		sequences[4] = m;
		nodes[4] = new SomNode(m);
		
		double[][] ver3 = {
				{0,0,1},
				{0,0,1},
				{0,0,1}};
		m = new SimpleMatrix(ver3);
		m.reshape(1, 9);
		sequences[5] = m;
		nodes[5] = new SomNode(m);
		
		//Blank
		double[][] blank = {
				{0,0,0},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(blank);
		m.reshape(1, 9);
		sequences[6] = m;
		nodes[6] = new SomNode(m);
	}

}
