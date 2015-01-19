package dk.stcl.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import dk.stcl.som.IRSOM;
import dk.stcl.som.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.online.rsom.RSOM;
import dk.stcl.som.online.rsom.RSOMlo;

public class RSOMTest {
	private Random rand = new Random();
	private IRSOM rsom;
	private final int NUM_ITERATIONS = 20000;
	private final double DECAY = 0.3;
	private final int SIZE = 3;
	private final boolean USE_LINE_SEQUENCES = true;
	private enum RSOMTYPES {RSOM,RSOMlo};
	private final RSOMTYPES type = RSOMTYPES.RSOMlo; 
	private HashMap<Integer, Integer> labelMap;
	
	
	private ArrayList<double[][]> sequences;
	
	private final double LEARNING_RATE = 0.1;
	private final int INPUTLENGTH = 1;
	private final int MAX_SEQUENCE_LENGTH = 3;
	private final boolean DIFFERENT_LENGTH = true;
	private final int NUM_SEQUENCES = 100;
	

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
		
	}
	
	private void buildSequences (int inputLength, int maxSequenceLength, boolean differentLength, int numSequences){
		sequences = new ArrayList<double[][]>();
		
		if (USE_LINE_SEQUENCES){
			sequences = lineSequences();
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
			SomNode bmu = doSequence(seq);
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
			SomNode bmu = doSequence(seq);
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
	
	private ArrayList<double[][]> lineSequences(){
		ArrayList<double[][]> complex = new ArrayList<double[][]>();
		double[][] hor = {
				{0,0,1,0,0,0,0,0,0},
				{1,0,0,0,0,0,0,0,0},
				{0,1,0,0,0,0,0,0,0}};
		
		double[][] ver = {
				{0,0,0,0,0,0,1,0,0},
				{0,0,0,0,0,0,0,0,1},
				{0,0,0,0,0,0,0,1,0}};
		
		double[][] blank = {
				{0,0,0,1,0,0,0,0,0},
				{0,0,0,1,0,0,0,0,0},
				{0,0,0,1,0,0,0,0,0}};
		
		complex.add(hor);
		complex.add(ver);
		complex.add(blank);
		
		return complex;
	}
	
	private void setupRSOM(){
		int inputSize = sequences.get(0)[0].length;
		
		switch (type){
		case RSOM: rsom = new RSOM(SIZE, SIZE, inputSize, rand, DECAY);
			break;
		case RSOMlo: rsom = new RSOMlo(SIZE, SIZE, inputSize, rand, LEARNING_RATE, 1, 0.3, DECAY);
			break;
		default:
			break;
		
		}
		
	}
	
	public void train(){
		for (int i = 1; i < NUM_ITERATIONS; i++){
			//System.out.println("-----------------------------");
			//System.out.println("Iteration: " + i);
			//rsom.sensitize(i, NUM_ITERATIONS, true, false);
			for (double[][] seq : sequences){
				doSequence(seq);
				rsom.flush();
			}
			
		}
	}
	
	private SomNode doSequence(double[][] seq){
		SomNode bmu = null;
		for (double[] d : seq){
			bmu = rsom.step(d);
		}
		return bmu;
	}
}
