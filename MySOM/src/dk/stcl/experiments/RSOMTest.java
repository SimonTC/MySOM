package dk.stcl.experiments;

import java.util.Random;

import dk.stcl.som.RSOM;

public class RSOMTest {
	private Random rand = new Random(1234);
	private RSOM rsom;
	private final int NUM_ITERATIONS = 1000;
	
	private double[][] sequences;

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
		sequences = new double[4][4];
		
		sequences[0][0] = 0;
		sequences[0][1] = 0;
		sequences[0][2] = 1;
		sequences[0][3] = 1;
		
		sequences[1][0] = 1;
		sequences[1][1] = 1;
		sequences[1][2] = 0;
		sequences[1][3] = 0;
		
		sequences[2][0] = 1;
		sequences[2][1] = 1;
		sequences[2][2] = 0;
		sequences[2][3] = 1;
		
		sequences[3][0] = 0;
		sequences[3][1] = 0;
		sequences[3][2] = 1;
		sequences[3][3] = 1;
	}
	
	private void setupRSOM(){
		rsom = new RSOM(2, 2, 1, rand, 0.1, 1, 0.7);
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
