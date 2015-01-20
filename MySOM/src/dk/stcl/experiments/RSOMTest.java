package dk.stcl.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.experiments.movinglines.MovingLinesGUI;
import dk.stcl.som.IRSOM;
import dk.stcl.som.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.rsom.RSOM;
import dk.stcl.som.som.SOM;

public class RSOMTest {
	private Random rand = new Random();
	private IRSOM rsom;
	private final int NUM_ITERATIONS = 1000;
	private final double DECAY = 0.3;
	private final int SIZE = 5;
	
	private final boolean USE_LINE_SEQUENCES = false;
	private final boolean USE_LINE_SEQUENCES_Random = true;
	
	
	private enum RSOMTYPES {RSOM,RSOMlo};
	private final RSOMTYPES type = RSOMTYPES.RSOMlo; 
	private HashMap<Integer, Integer> labelMap;
	
	private double[][] hor, ver, blank;
	
	private SOM spatialDummy = new SOM(3, 3, 3, rand, 0, 0, 0);
	
	private final int STDDEV = 1;
	
	private ArrayList<double[][]> sequences;
	
	private final double LEARNING_RATE = 0.1;
	private final int INPUTLENGTH = 1;
	private final int MAX_SEQUENCE_LENGTH = 3;
	private final boolean DIFFERENT_LENGTH = true;
	private final int NUM_SEQUENCES = 100;
	
	private MovingLinesGUI frame;
	private final int GUI_SIZE = 500;
	private final int FRAMES_PER_SECOND = 10;
	private final boolean VISUALIZE = false;
	

	public static void main(String[] args) {
		RSOMTest runner = new RSOMTest();
		runner.run();

	}

	public void run(){
		double fitness = 0;
		for (int i = 0; i < 10; i++){
			buildSequences(INPUTLENGTH, MAX_SEQUENCE_LENGTH, DIFFERENT_LENGTH, NUM_SEQUENCES);
			setupRSOM();
			train();
			rsom.flush();
			rsom.setLearning(false);
			label();
			rsom.flush();
			fitness += validate();
		}
		fitness = fitness / 10;
		System.out.println("Fitness: " + fitness);
		
		if (VISUALIZE) visualRun(rand);
		
	}
	
	private void visualRun( Random rand){
		setupVisualization(spatialDummy, GUI_SIZE);
		for (int i = 1; i < 500;i++){
			double[][] seq = null;
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
		SimpleMatrix tmp = new SimpleMatrix(3, 3, true, sequences.get(0)[0]);
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
	
	private void buildSequences (int inputLength, int maxSequenceLength, boolean differentLength, int numSequences){
		sequences = new ArrayList<double[][]>();
		
		if (USE_LINE_SEQUENCES){
			lineSequences();
			sequences.add(hor);
			sequences.add(ver);
			sequences.add(blank);
		} else {
			for (int seq = 0; seq < numSequences; seq++){
				int length = 0;
				if (differentLength){
					length = rand.nextInt(maxSequenceLength) + 1;
				} else {
					length = maxSequenceLength;
				}
				double[][] sequence = new double[length][inputLength];
				for (int part = 0; part < length; part++){
					for (int i = 0; i < inputLength; i++){
						sequence[part][i] = rand.nextInt(2);
					}
				}
				sequences.add(sequence);
			}
		}
		
	}
	
	private void label(){
		int nextFreeLabel = 0;
		int curLabel = -1;
		labelMap = new HashMap<Integer, Integer>();
		
		for (double[][] seq : sequences){
			int hash = toHash(seq);
			SomNode bmu = doSequence(seq, false);
			rsom.flush();
			if (labelMap.containsKey(hash)){
				curLabel = labelMap.get(hash);
			} else {
				curLabel = nextFreeLabel++;
				labelMap.put(hash, curLabel);
			}
			
			bmu.setLabel(curLabel);
		}
	}
	
	private double validate(){
		int correct = 0;
		int total = 0;
		int curlabel = 0;
		
		for (double[][] seq : sequences){
			total++;
			int hash = toHash(seq);
			SomNode bmu = doSequence(seq, false);
			curlabel = labelMap.get(hash);
			if (bmu.getLabel() == curlabel) correct++;	
			rsom.flush();
		}
		
		double fitness = (double) correct / total;
		
		return fitness;
	}
	
	private int toHash(double[][] input){
		String s = "";
		for (double[] pair : input){
			for (double d : pair){
				s = s + (int) d;
			}
		}
		return s.hashCode();
	}
	
	private ArrayList<double[][]> simpleSequences(){
		ArrayList<double[][]> simple = new ArrayList<double[][]>();
		double[][] d1 = {{0},{0},{1},{1}};
		double[][] d2 = {{1},{1},{0},{0}};
		double[][] d3 = {{1},{0},{1},{0}};
		double[][] d4 = {{0},{1},{0},{1}};
		double[][] d5 = {{0},{0},{1},{1},{1}};
		simple.add(d1);
		simple.add(d2);
		simple.add(d3);
		simple.add(d4);
		simple.add(d5);
		
		return simple;
	}
	
	private void lineSequences(){
		double[][] hor = {
				{0,0,1,0,0,0,0,0,0},
				{1,0,0,0,0,0,0,0,0},
				{0,1,0,0,0,0,0,0,0}};
		this.hor = hor;
		
		double[][] ver = {
				{0,0,0,0,0,0,1,0,0},
				{0,0,0,0,0,0,0,0,1},
				{0,0,0,0,0,0,0,1,0}};
		this.ver = ver;
		
		double[][] blank = {
				{0,0,0,1,0,0,0,0,0},
				{0,0,0,1,0,0,0,0,0},
				{0,0,0,1,0,0,0,0,0}};
		this.blank = blank;
	
		
	}
	
	private void setupRSOM(){
		int inputSize = sequences.get(0)[0].length;
		rsom = new RSOM(SIZE, SIZE, inputSize, rand, LEARNING_RATE, STDDEV, 0.3, DECAY);
		
	}
	
	public void train(){
		int curSeqID = 0;
		int curInputID = Integer.MAX_VALUE;
		double[][] seq = null;
		for (int i = 1; i < NUM_ITERATIONS; i++){
			if (USE_LINE_SEQUENCES){
				if (!USE_LINE_SEQUENCES_Random){
					if (curInputID >= hor.length){
						curSeqID = rand.nextInt(3);
						if (curSeqID == 0) seq = hor;
						if (curSeqID == 1) seq = ver;
						if (curSeqID == 2) seq = blank;
						curInputID = 0;
					}
					step(seq[curInputID]);
					curInputID++;
				} else {
					boolean change = rand.nextDouble() > 0.9 ? true : false;
					if (change){
						boolean choose = rand.nextBoolean();
						switch (curSeqID){
						case 0 : curSeqID = choose ? 1 : 2; break;
						case 1 : curSeqID = choose ? 2 : 0; break;
						case 2 : curSeqID = choose ? 0 : 1; break;
						}
					} 
					curInputID = rand.nextInt(3);
					if (curSeqID == 0) seq = hor;
					if (curSeqID == 1) seq = ver;
					if (curSeqID == 2) seq = blank;
					
					step(seq[curInputID]);
				}
				
				
			} else {
				for (double[][] sequence : sequences){
					doSequence(sequence, false);
					//rsom.flush();
				}
			}
			
		}
	}
	
	private SomNode step(double[] input){
		SomNode bmu = rsom.step(input);
		return bmu;
	}
	
	private SomNode doSequence(double[][] seq, boolean visualize){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
		SomNode bmu = null;
		for (double[] d : seq){
			bmu = step(d);
			
			if (visualize){
				//Visualize
				SimpleMatrix m = new SimpleMatrix(3, 3, true, d);
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
