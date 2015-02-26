package dk.stcl.experiments.tests.som_rsom;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.rsom.RSOM_Simple;
import dk.stcl.core.som.ISOM;
import dk.stcl.core.som.SOM_Simple;

public class TemporalClassification {

	SimpleMatrix[] numbers;
	ArrayList<ArrayList<SimpleMatrix>> sequences;
	int iterations = 100000;
	ISOM som;
	IRSOM rsom;
	Random rand = new Random(1234);
	
	public static void main(String[] args) {
		TemporalClassification r = new TemporalClassification();
		r.run();

	}
	
	public void run(){
		buildSequences();
		buildPoolers();
		runExperiment();
		
		 System.out.println("Rsom models:");
		 System.out.println();
		 printModels(rsom.getNodes());
		 
		 System.out.println();
		 System.out.println("SOM models:");
		 System.out.println();
		 printModels(som.getNodes());
		
	}
	
	private void printModels(SomNode[] nodes){
		for (SomNode n : nodes){
	    	SimpleMatrix vector = new SimpleMatrix(n.getVector());
	    	vector.print();
	    	System.out.println(vector.elementSum());
	    	System.out.println();
	    }
	}
	
	
	
	private void runExperiment(){
		int counter = Integer.MAX_VALUE;
		ArrayList<SimpleMatrix> seq = null;
		for (int i = 0; i < iterations; i++){
			if (counter >= sequences.get(0).size()){
				rsom.flush();
				seq = sequences.get(rand.nextInt(sequences.size()));
				counter = 0;
			}
			
			SimpleMatrix m = seq.get(counter);
			som.step(m);
			SimpleMatrix somActivation = som.computeActivationMatrix();
			
			somActivation.reshape(1, somActivation.getNumElements());
			
			rsom.step(somActivation);
			
			counter++;
			rsom.sensitize(i);
			som.sensitize(i);
		}
	}
	
	private void buildPoolers(){
		int mapSize_Som = 4;
		int inputLength_Som = 4;
		double initialLearningRate = 1;
		double activationCodingFactor = 0.125;
		
		int mapSize_Rsom = 2;
		int inputLength_RSOM = mapSize_Som * mapSize_Som;
		double decay = 0.68; //Will let you remember 4 inputs
		
		som = new SOM_Simple(mapSize_Som, inputLength_Som, rand, initialLearningRate, activationCodingFactor, iterations);
		rsom = new RSOM_Simple(mapSize_Rsom, inputLength_RSOM, rand, initialLearningRate, activationCodingFactor, iterations, decay);
	}
	
	private void buildSequences(){
		buildNumbers();
		sequences = new ArrayList<>();
		
		ArrayList<SimpleMatrix> seq = new ArrayList<>();
		for (int counter = 1; counter <= numbers.length; counter++){
			if (counter % 4 == 0){
				sequences.add(seq);
				seq = new ArrayList<>();
			}			
			seq.add(numbers[counter - 1]);			
		}
		
	}
	
	private void buildNumbers(){
		numbers = new SimpleMatrix[16];
		
		double[][] numberData = {
				{0,0,0,0},
				{0,0,0,1},
				{0,0,1,0},
				{0,0,1,1},
				{0,1,0,0},
				{0,1,0,1},
				{0,1,1,0},
				{0,1,1,1},
				{1,0,0,0},
				{1,0,0,1},
				{1,0,1,0},
				{1,0,1,1},
				{1,1,0,0},
				{1,1,0,1},
				{1,1,1,0},
				{1,1,1,1}
		};
		
		for (int i = 0; i < numberData.length; i++){
			double[] data = numberData[i];
			double[][] tmp = {data};
			SimpleMatrix m = new SimpleMatrix(tmp);
			//m.reshape(2, 2);
			numbers[i] = m;
		}
	}

	
	
}
