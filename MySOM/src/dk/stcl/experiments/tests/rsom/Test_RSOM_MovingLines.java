package dk.stcl.experiments.tests.rsom;

import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.rsom.RSOM_SemiOnline;
import dk.stcl.core.rsom.RSOM_Simple;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.experiments.movinglines.MovingLinesGUI;
import dk.stcl.utils.RSomLabeler;

public class Test_RSOM_MovingLines {
	private final int NUM_ITERATIONS = 100000;
	private final double DECAY = 0.3;
	private final int SIZE = 2;
	private final int STDDEV = 2;
	private final double LEARNING_RATE = 0.1;
	private final double NOISE_MAGNITUDE = 0.0;
	private final double CROSS_CHANCE = 0.0;
	private final double ACTIVATION_CODING_FACTOR = 0.125;
	
	private enum RSOM_TYPES {Simple, Semi_Online};
	private RSOM_TYPES type = RSOM_TYPES.Simple;
	
	private int[] labels;
	private Random rand = new Random();
	private IRSOM rsom;
	private SimpleMatrix[] hor, ver, blank;
	private SimpleMatrix cross;
	
	private SOM_SemiOnline spatialDummy = new SOM_SemiOnline(3, 9, rand, 0, 0, 0);
	
	
	
	private SimpleMatrix[][] sequences;
	
	
	private MovingLinesGUI frame;
	private final int GUI_SIZE = 500;
	private final int FRAMES_PER_SECOND = 5;
	private final boolean VISUALIZE = true;
	

	public static void main(String[] args) {
		Test_RSOM_MovingLines runner = new Test_RSOM_MovingLines();
		runner.run();

	}

	public void run(){
		double fitness = 0;
		for (int i = 0; i < 10; i++){
			buildSequences();
			setupRSOM();
			train(NOISE_MAGNITUDE);
			rsom.flush();
			rsom.setLearning(false);
			label();
			rsom.printLabelMap();
			System.out.println();
			rsom.flush();
			fitness += validate(NOISE_MAGNITUDE,CROSS_CHANCE);
		}
		fitness = fitness / 10;
		System.out.println("Fitness: " + fitness);
		
		if (VISUALIZE) visualRun(rand);
		
	}
	
	private void visualRun( Random rand){
		setupVisualization(spatialDummy, GUI_SIZE);
		for (int i = 1; i < 500;i++){
			SimpleMatrix[] seq = null;
			int id = rand.nextInt(3);
			if (id ==0) seq = hor;
			if (id == 1) seq = ver;
			if (id == 2) seq = blank;
			doSequence(seq, true);

		}
	}
	
	private void setupVisualization(ISomBasics som, int GUI_SIZE){
		//Create GUI
		
		frame = new MovingLinesGUI(som, spatialDummy);
		frame.setTitle("Visualiztion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SimpleMatrix tmp = sequences[2][0];
		updateGraphics(tmp,0); //Give a blank
		frame.pack();
		frame.setVisible(true);	
	}
	
	private void updateGraphics(SimpleMatrix inputVector, int iteration){
		spatialDummy.step(inputVector.getMatrix().data);
		frame.updateData(inputVector, spatialDummy, rsom);
		frame.setTitle("Visualiztion - Iteration: " + iteration);
		frame.revalidate();
		frame.repaint();

	}
	
	private void buildSequences (){		
		lineSequences();		
	}
	
	public void train(double noiseMagnitude){
		int curSeqID = 0;
		int curInputID = Integer.MAX_VALUE;
		SimpleMatrix[] seq = blank;
		for (int i = 1; i < NUM_ITERATIONS; i++){
			boolean change = curInputID >= seq.length;
			if (change){
				rsom.flush();
				curInputID = 0;
				boolean choose = rand.nextBoolean();
				switch (curSeqID){
				case 0 : curSeqID = choose ? 1 : 2; break;
				case 1 : curSeqID = choose ? 2 : 0; break;
				case 2 : curSeqID = choose ? 0 : 1; break;
				}
			} 
			if (curSeqID == 0) seq = hor;
			if (curSeqID == 1) seq = ver;
			if (curSeqID == 2) seq = blank;
			
			SimpleMatrix input = seq[curInputID];
			
			SimpleMatrix noisyInput = addnoise(input, noiseMagnitude);
			
			step(noisyInput);		
			
			rsom.sensitize(i);
			
			curInputID++;
		}
	}
	
	private void label(){
		
		labels = new int[sequences.length];
		
		for (int i = 0; i < labels.length; i++ ){
			labels[i] = i;
		}
		
		RSomLabeler labeler = new RSomLabeler();
		labeler.labelRSOM(rsom, sequences, labels, 100); 
	}
	
	private double validate(double noiseMagnitude, double crossChance){
		int numValidations = 1000;
		double fitness = 0;
		
		int somCorrect = 0;
		
		int curSeqID = 0;
		int curInputID = Integer.MAX_VALUE;
		SimpleMatrix[] seq = blank;
		for (int i = 1; i < numValidations; i++){
			boolean change = curInputID >= seq.length;
			if (change){
				rsom.flush();
				curInputID = 0;
				boolean choose = rand.nextBoolean();
				switch (curSeqID){
				case 0 : curSeqID = choose ? 1 : 2; break;
				case 1 : curSeqID = choose ? 2 : 0; break;
				case 2 : curSeqID = choose ? 0 : 1; break;
				}
			} 
			
			SimpleMatrix input;
			if (rand.nextDouble() > crossChance){
				if (curSeqID == 0) seq = hor;
				if (curSeqID == 1) seq = ver;
				if (curSeqID == 2) seq = blank;
				input = seq[curInputID];
			} else {
				input = cross;
			}
			
			SimpleMatrix noisyInput = addnoise(input, noiseMagnitude);
			
			step(noisyInput);		
			
			int actualLabel = rsom.getBMU().getLabel();
			int expectedLabel = labels[curSeqID];
			
			if (actualLabel == expectedLabel) somCorrect++;
			
			curInputID++;
		}
		
		fitness = (double) somCorrect / numValidations;
		
		return fitness;
	}

	
	private void lineSequences(){
		sequences = new SimpleMatrix[3][3];
		SimpleMatrix m;
		
		//Horizontal down
		hor = new SimpleMatrix[3];
		double[][] hor1 = {
				{1,1,1},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(hor1);
		m.reshape(1, 9);
		hor[0] = m;
		sequences[0][0] = m;
		
		double[][] hor2 = {
				{0,0,0},
				{1,1,1},
				{0,0,0}};
		m = new SimpleMatrix(hor2);
		m.reshape(1, 9);
		hor[1] = m;
		sequences[0][1] = m;

		
		double[][] hor3 = {
				{0,0,0},
				{0,0,0},
				{1,1,1}};
		m = new SimpleMatrix(hor3);
		m.reshape(1, 9);
		hor[2] = m;
		sequences[0][2] = m;
		
		//Vertical right
		ver = new SimpleMatrix[3];
		double[][] ver1 = {
				{1,0,0},
				{1,0,0},
				{1,0,0}};
		m = new SimpleMatrix(ver1);
		m.reshape(1, 9);
		ver[0] = m;
		sequences[1][0] = m;
		
		double[][] ver2 = {
				{0,1,0},
				{0,1,0},
				{0,1,0}};
		m = new SimpleMatrix(ver2);
		m.reshape(1, 9);
		ver[1] = m;
		sequences[1][1] = m;
		
		double[][] ver3 = {
				{0,0,1},
				{0,0,1},
				{0,0,1}};
		m = new SimpleMatrix(ver3);
		m.reshape(1, 9);
		ver[2] = m;
		sequences[1][2] = m;
		
		//Blank
		blank = new SimpleMatrix[3];
		double[][] blank = {
				{0,0,0},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(blank);
		m.reshape(1, 9);
		sequences[2][0] = m;
		sequences[2][1] = m;
		sequences[2][2] = m;
		this.blank[0] = m;
		this.blank[1] = m;
		this.blank[2] = m;
		
		//Cross
		double[][] cross = {
				{0,1,0},
				{1,1,1},
				{0,1,0}
		};
		
		m = new SimpleMatrix(cross);
		m.reshape(1, 9);
		
		this.cross = m;
		
	
		
	}
	
	private void setupRSOM(){
		int inputSize = sequences[0][0].numCols();
		switch(type){
		case Semi_Online: rsom = new RSOM_SemiOnline(SIZE, inputSize, rand, LEARNING_RATE, ACTIVATION_CODING_FACTOR, STDDEV, DECAY);
			break;
		case Simple: rsom = new RSOM_Simple(SIZE, inputSize, rand, LEARNING_RATE, ACTIVATION_CODING_FACTOR, NUM_ITERATIONS, DECAY);
			break;
		default:
			break;
		
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
	
	private SomNode step(SimpleMatrix input){
		SomNode bmu = rsom.step(input);
		return bmu;
	}
	
	private SomNode doSequence(SimpleMatrix[] seq, boolean visualize){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
		SomNode bmu = null;
		for (SimpleMatrix m : seq){
			bmu = step(m);
			
			if (visualize){
				//Visualize
    			updateGraphics(m,1);					
				try {
					Thread.sleep(SKIP_TICKS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bmu;
	}
}
