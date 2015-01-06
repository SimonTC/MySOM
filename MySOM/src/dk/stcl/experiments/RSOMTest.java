package dk.stcl.experiments;

import java.util.ArrayList;
import java.util.Random;

import dk.stcl.som.RSOM;

public class RSOMTest {
	private Random rand = new Random(1234);
	private RSOM rsom;
	private final int NUM_ITERATIONS = 1000;
	private final double DECAY = 0.7;
	private final int SIZE = 3;
	
	private ArrayList<double[]> sequences;
	

	public static void main(String[] args) {
		RSOMTest runner = new RSOMTest();
		runner.run();

	}

	public void run(){
		buildSequences();
		setupRSOM();
		train();
	}
	
	private void buildSequences(){
		sequences = new ArrayList<double[]>();
		
		double[] d1 = {0,0,1,1};
		double[] d2 = {1,1,0,0};
		double[] d3 = {1,0,1,0};
		double[] d4 = {0,1,0,1};
		double[] d5 = {0,0,1,1,1};
		sequences.add(d1);
		sequences.add(d2);
		sequences.add(d3);
		sequences.add(d4);
		sequences.add(d5);
		
	}
	
	private void setupRSOM(){
		rsom = new RSOM(SIZE, SIZE, 1, rand, 0.1, (double) SIZE / 2, DECAY);
	}
	
	public void train(){
		for (int i = 1; i < NUM_ITERATIONS; i++){
			System.out.println("-----------------------------");
			System.out.println("Iteration: " + i);
			for (double[] seq : sequences){
				String s = "";
				for (double d : seq){
					double[] inputVector = {d};
					rsom.step(inputVector);
					s += rsom.getBMU().getId() + " ";
				}
				System.out.println(s);
				rsom.flush();
			}
			rsom.sensitize(i, NUM_ITERATIONS, true, false);
		}
	}
}
