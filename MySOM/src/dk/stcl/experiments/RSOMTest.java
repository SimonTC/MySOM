package dk.stcl.experiments;

import java.util.ArrayList;
import java.util.Random;

import dk.stcl.som.IRSOM;
import dk.stcl.som.ISomBasics;
import dk.stcl.som.online.rsom.RSOM;
import dk.stcl.som.online.rsom.RSOMlo;

public class RSOMTest {
	private Random rand = new Random(1234);
	private IRSOM rsom;
	private final int NUM_ITERATIONS = 1000;
	private final double DECAY = 0.1;
	private final int SIZE = 3;
	private final boolean USE_SIMPLE_SEQUENCES = true;
	private enum RSOMTYPES {RSOM,RSOMlo};
	private final RSOMTYPES type = RSOMTYPES.RSOMlo; 
	
	private ArrayList<double[][]> sequences;
	

	public static void main(String[] args) {
		RSOMTest runner = new RSOMTest();
		runner.run();

	}

	public void run(){
		buildSequences(USE_SIMPLE_SEQUENCES);
		setupRSOM();
		train();
	}
	
	private void buildSequences(boolean simple){
		if (simple) {
			sequences = simpleSequences();
		} else {
			sequences = complexSequences();
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
	
	private void setupRSOM(){
		int inputSize = sequences.get(0)[0].length;
		
		switch (type){
		case RSOM: rsom = new RSOM(SIZE, SIZE, inputSize, rand, DECAY);
			break;
		case RSOMlo: rsom = new RSOMlo(SIZE, SIZE, inputSize, rand, 0.1, 1, 0.3, DECAY);
			break;
		default:
			break;
		
		}
		
	}
	
	public void train(){
		for (int i = 1; i < NUM_ITERATIONS; i++){
			System.out.println("-----------------------------");
			System.out.println("Iteration: " + i);
			//rsom.sensitize(i, NUM_ITERATIONS, true, false);
			for (double[][] seq : sequences){
				String s = "";
				for (double[] d : seq){
					double[] inputVector = d;
					rsom.step(inputVector);
					s += rsom.getBMU().getId() + " ";
				}
				System.out.println(s);
				rsom.flush();
			}
			
		}
	}
}
