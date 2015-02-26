package dk.stcl.experiments.tests.rsom;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.RSOM_Simple;
import dk.stcl.core.som.SOM_Simple;

public class RSOMasSOM {
	
	RSOM_Simple rsom;
	SOM_Simple som;
	ArrayList<SimpleMatrix[]> sequences;
	int iterations = 10000;
	Random rand = new Random(1234);
	int maxNumber;
	double scale;

	public static void main(String[] args) {
		RSOMasSOM r =  new RSOMasSOM();
		
		r.run();
	}
	
	
	public void run(){
		setupExperiment();
		runExperiment();
		printWeightMaps();
	}
	
	private void setupExperiment(){
		buildUniqueSequences(9, 1);
		setupSOMs();
	}
	
	private void runExperiment(){
		
		for (int i = 0; i < iterations; i++){
			som.sensitize(i);
			rsom.sensitize(i);
			
			SimpleMatrix[] sequence = sequences.get(rand.nextInt(sequences.size()));
			
			for (SimpleMatrix m : sequence){
				som.step(m);
				rsom.step(m);
			}
		}
	}
	
	private void setupSOMs(){
		int mapSize = 4;
		int inputLength = sequences.get(0)[0].getNumElements();
		double initialLearningRate = 0.1;
		double activationCodingFactor = 0.125;
				
		som = new SOM_Simple(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor, iterations);
		rsom = new RSOM_Simple(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor, iterations, 1);
		copyWeightMap();
	}
	
	
	private void copyWeightMap(){
		for (SomNode n : rsom.getNodes()){
			int id = n.getId();
			SimpleMatrix copy = new SimpleMatrix(n.getVector());
			som.getSomMap().get(id).setVector(copy);
		}
	}
	
	private void buildUniqueSequences(int numSequences, int sequenceLength){
		int number = 1;
		maxNumber = numSequences * sequenceLength;
		scale = 1;//maxNumber;
		sequences = new ArrayList<SimpleMatrix[]>();
		
		for (int seq = 0; seq < numSequences; seq++){
			SimpleMatrix[] sequence = new SimpleMatrix[sequenceLength];
			for (int i = 0; i < sequenceLength; i++){
				double[][] data = {{number}};
				SimpleMatrix m = new SimpleMatrix(data);
				m = m.divide(scale);
				sequence[i] = m;
				number++;
			}
			sequences.add(sequence);
		}
	}
	
	private void printWeightMaps(){
		System.out.println();
		System.out.println("RSOM weigth map:");
		buildNormFMap(rsom.getSomMap()).print();
		System.out.println();
		System.out.println("SOM weigth map:");
		buildNormFMap(som.getSomMap()).print();
		System.out.println();
	}
	
	private SimpleMatrix buildNormFMap(SomMap map){
		SimpleMatrix m = new SimpleMatrix(map.getHeight(), map.getWidth());
		
		for (SomNode n : map.getNodes()){
			int id = n.getId();
			double normF = n.getVector().normF();
			m.set(id, normF);
		}
		
		return m;
	}
	
	private SimpleMatrix buildSingleValueMap(SomMap map){
		SimpleMatrix m = new SimpleMatrix(map.getHeight(), map.getWidth());
		
		for (SomNode n : map.getNodes()){
			int id = n.getId();
			SimpleMatrix vector = n.getVector().scale(scale);
			double value = vector.get(0);
			m.set(id, value);
		}
		
		return m;
	}

}
