package dk.stcl.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import dk.stcl.som.basic.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.rsom.RSOM;
import dk.stcl.som.som.SOM;

public class SOMTEST {
	
	private ArrayList<double[][]> sequences;
	private HashMap<Integer, Integer> labelMap;
	double[] fitnessScores;
	
	private enum somTypes {SOMlo, RSOMlo};
	
	private final int SOM_SIZE = 3;
	private final somTypes type = somTypes.RSOMlo;
	private final double INITIAL_LEARNING = 0.1;
	private Random rand = new Random();
	private final boolean USE_SIMPLE_SEQUENCES = false;
	private final int NUM_ITERATIONS = 1000;
	
	private ISomBasics som;
	
	
	
	public SOMTEST() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		SOMTEST runner = new SOMTEST();
		runner.run();

	}
	
	public void run(){
		//buildSequences(USE_SIMPLE_SEQUENCES);
		sequences = buildSequences(4, 100);
		for (int i = 0; i < sequences.size(); i++){
			double fitness = 0;
			for (int j = 0; j < 10; j++){
				double[][] trainingSet = sequences.get(i);
				setupSOM(trainingSet);
				train(trainingSet);
				som.setLearning(false);
				label(trainingSet);
				fitness+= validate(trainingSet);
		}
			fitness = fitness / 10;
			System.out.println("Fitness, length " + (i+1) + ": " + fitness);
		}
		
		
	}
	
	private void label(double[][] trainingSet){
		int nextFreeLabel = 0;
		int curLabel = -1;
		labelMap = new HashMap<Integer, Integer>();
		
		for (double[] d : trainingSet){
			SomNode bmu = som.step(d);
			int hash = toHash(d);
			
			if (labelMap.containsKey(hash)){
				curLabel = labelMap.get(hash);
			} else {
				curLabel = nextFreeLabel++;
				labelMap.put(hash, curLabel);
			}
			
			bmu.setLabel(curLabel);
			nextFreeLabel++;
		}

		
	}
	
	private double validate(double[][] trainingSet){
		int correct = 0;
		int total = 0;
		int curlabel = 0;
		for (double[] d : trainingSet){
			int hash = toHash(d);
			total++;
			SomNode bmu = som.step(d);
			curlabel = labelMap.get(hash);
			if (bmu.getLabel() == curlabel) correct++;	
			curlabel++;
		}
		
		double fitness = (double) correct / total;
		
		return fitness;
	}
	
	private int toHash(double[] input){
		String s = "";
		for (double d : input){
			s = s + (int) d;
		}
		return s.hashCode();
	}
	
	private void buildSequences(boolean simple){
		if (simple) {
			sequences = simpleSequences();
		} else {
			sequences = complexSequences();
		}
		
		
	}
	
	private void setupSOM(double[][] trainingSet){
		int inputLength = trainingSet[0].length;
		int size = 3; //(int) Math.pow(2, inputLength);
		
		switch (type){
		case SOMlo: som = new SOM(size, size, inputLength, rand, INITIAL_LEARNING, 1, 0.125);
			break;
		case RSOMlo: som = new RSOM(size, size, inputLength, rand, INITIAL_LEARNING, 1, 0.125, 1);
			break;
		default:
			break;
		
		
		}
		
	}
	
	public void train(double[][] trainingSet){
		for (int i = 1; i < NUM_ITERATIONS; i++){
			//System.out.println("-----------------------------");
			//System.out.println("Iteration: " + i);
			String s = "";
			for (double[] inputVector : trainingSet){
				som.step(inputVector);
				s += som.getBMU().getId() + " ";
			}
			//System.out.println(s);		
			
		}
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
	
	private ArrayList<double[][]> complexSequences(){
		ArrayList<double[][]> complex = new ArrayList<double[][]>();
		double[][] hor = {
				{0,0,1,0,0,0,0,0,0},
				{0,0,0,0,0,1,0,0,0},
				{0,1,0,0,0,0,0,0,0}};
		
		double[][] ver = {
				{1,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,1},
				{0,0,0,0,1,0,0,0,0}};
		
		double[][] blank = {
				{0,0,0,1,0,0,0,0,0},
				{0,0,0,1,0,0,0,0,0},
				{0,0,0,1,0,0,0,0,0}};
		
		complex.add(hor);
		complex.add(ver);
		complex.add(blank);
		
		return complex;
	}
	
	private ArrayList<double[][]> buildSequences (int maxInputLength, int numSequencesPerLength){
		ArrayList<double[][]> sequences = new ArrayList<double[][]>();
		
		for (int length = 1; length <= maxInputLength; length++){
			double[][] inputGroup = new double[numSequencesPerLength][length];
			for (int seqNum = 0; seqNum < numSequencesPerLength; seqNum++){
				for (int i = 0; i < length; i++){
					inputGroup[seqNum][i] = rand.nextInt(2);
				}
			}
			sequences.add(inputGroup);
		}
		return sequences;
		
	}
	
}
