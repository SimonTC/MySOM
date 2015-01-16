package dk.stcl.experiments;

import java.util.ArrayList;
import java.util.Random;

import dk.stcl.som.SomBasics;
import dk.stcl.som.offline.som.SomOffline;
import dk.stcl.som.online.som.PLSOM;

public class SOMTEST {
	
	private ArrayList<double[][]> sequences;
	
	private final int SOM_SIZE = 3;
	private final boolean USE_PLSOM = false;
	private final double INITIAL_LEARNING = 0.1;
	private Random rand = new Random(1234);
	private final boolean USE_SIMPLE_SEQUENCES = false;
	private final int NUM_ITERATIONS = 1000;
	
	private SomBasics som;
	
	
	
	public SOMTEST() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		SOMTEST runner = new SOMTEST();
		runner.run();

	}
	
	public void run(){
		buildSequences(USE_SIMPLE_SEQUENCES);
		setupSOM();
		train();
	}

	private void buildSequences(boolean simple){
		if (simple) {
			sequences = simpleSequences();
		} else {
			sequences = complexSequences();
		}
		
		
	}
	
	private void setupSOM(){
		int inputLength = sequences.get(0)[0].length;
		int size = SOM_SIZE;
		if (USE_PLSOM){
			som = new PLSOM(size, size, inputLength, rand);
		} else {
			som = new SomOffline(size, size, inputLength, rand, INITIAL_LEARNING, size / 2);
		}
	}
	
	public void train(){
		for (int i = 1; i < NUM_ITERATIONS; i++){
			System.out.println("-----------------------------");
			System.out.println("Iteration: " + i);
			for (double[][] seq : sequences){
				String s = "";
				for (double[] d : seq){
					double[] inputVector = d;
					som.step(inputVector);
					s += som.getBMU().getId() + " ";
				}
				System.out.println(s);
			}
			
    		
    		//Sensitize som
    		som.sensitize(i, NUM_ITERATIONS, true, false);
			
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
}
