package dk.stcl.experiments.tests.rsom;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.RSOM_Simple;

public class TemporalSequenceClassification {
	
	private RSOM_Simple rsom;
	private Random rand = new Random(1234);
	private int maxIterations;
	private ArrayList<SimpleMatrix[]> sequences;
	int scale = 1;

	public static void main(String[] args){
		TemporalSequenceClassification t = new TemporalSequenceClassification();
		t.run();
	}
	
	public void run(){
		setupExperiment();
		runExperiment();
		printRSOM();
	}
	
	private void setupExperiment(){
		int mapSize = 2;
		int inputLength = 1;
		double initialLearningRate = 1;
		double activationCodingFactor = 0.125;
		double decay = 0.8;
		maxIterations = 20;//mapSize * mapSize * 2000;
		
		rsom = new RSOM_Simple(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor, maxIterations, decay);
		
		sequences = createSequences();
		
	}
	
	private void runExperiment(){
		
		for (int i = 0; i < maxIterations; i++){
			
			if (i % 1000 == 0){
				System.out.println("Iteration: " + i + " / " + maxIterations);
			}
			//Choose random sequence
			SimpleMatrix[] seq = sequences.get(rand.nextInt(sequences.size()));
			
			for (SimpleMatrix m : seq){
				//Add noise
				//m = addNoise(m, 0.2);
				
				/*
				System.out.println();
				System.out.println("Input");
				m.print();
				*/
				
				//Present to network
				rsom.step(m);
			}
			
			rsom.sensitize(i);		
			//rsom.flush();
			
		}
	}
	
	private void printRSOM(){
		System.out.println();
		System.out.println("RSOM weights:");
		for (SomNode n : rsom.getSomMap().getNodes()){
			SimpleMatrix v = n.getVector().scale(scale);
			v.print();
		}
	}
	
	private SimpleMatrix addNoise(SimpleMatrix m, double noiseMagnitude){
		double noise = (rand.nextDouble() - 0.5) * 2 * noiseMagnitude;
		m = m.plus(noise);
		return m;
	}
	
	private ArrayList<SimpleMatrix[]> createSequences(){
		ArrayList<SimpleMatrix[]> sequences = new ArrayList<>();
		double[][] in1 = {{21}};
		SimpleMatrix ma1 = new SimpleMatrix(in1);
		
		double[][] in2 = {{1}};
		SimpleMatrix ma2 = new SimpleMatrix(in2);
		
		SimpleMatrix[] pair = {ma1,ma2};
		sequences.add(pair);
		
		/*
		double[] possibleInputs = {1,6,11,16,21};
		
		for (double d : possibleInputs){
			double[][] in1 = {{d}};
			SimpleMatrix ma1 = new SimpleMatrix(in1);
			ma1 = ma1.divide(scale);
			for (double e : possibleInputs){
				double[][] in2 = {{e}};
				SimpleMatrix ma2 = new SimpleMatrix(in2);
				ma2 = ma2.divide(scale);
				
				SimpleMatrix[] pair = {ma1,ma2};
				sequences.add(pair);
			}
		}
		*/
		return sequences;
	}
}
