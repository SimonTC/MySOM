package dk.stcl.experiments.tests.rsom;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.RSOM_SemiOnline;

public class RSOM_SemiOnline_Temporality {
	
	private RSOM_SemiOnline rsom;
	private Random rand = new Random(1234);
	double initialLearningRate, activationCodingFactor, decay;
	int maxIterations = 100000;
	double initialRadius;
	double curNeighborhoodRadius;
	double curLearningRate;
	double timeConstant;
	SomNode bmu;
	double scale;
	
	public static void main(String[] args){
		RSOM_SemiOnline_Temporality r = new RSOM_SemiOnline_Temporality();
		r.run();
	}
	
	public void run(){
		setupRun(3, 0.68);
		//ArrayList<SimpleMatrix[]> sequences = createSequences_Longer();
		ArrayList<SimpleMatrix[]> sequences = createSequences_NoValueIsTheSame(4,10);
		//train
		runExperiment(sequences, maxIterations,false);
		
		//Evaluate
		System.out.println("Evaluating");
		rsom.flush();
		rsom.setLearning(false);
		runExperiment(sequences, 500, true);
		printLeakyMaps();
		printWeightMaps();
	}
	
	private void runExperiment(ArrayList<SimpleMatrix[]> sequences, int iterations, boolean printBMUs){
		int count = Integer.MAX_VALUE;
		SimpleMatrix[] seq = null;
		for (int i = 0; i <= iterations; i++){
			if (count >= sequences.get(0).length){
				//rsom.flush();
				count = 0;
				seq = sequences.get(rand.nextInt(sequences.size()));
			}
			rsom.sensitize(i);
			
			SimpleMatrix m = seq[count];
			m = addNoise(m, 0);
			
			//Present to network
			SomNode bmu = rsom.step(m);
			
			if (printBMUs){
				System.out.println("Input: " + (m.get(0) * scale) + " BMU: " + bmu.getId());
			}
			
			count++;
			
		}
	}

	
	private void setupRun(int mapSize, double decay){
		int inputLength = 1;
		initialLearningRate = 1;
		activationCodingFactor = 0.125;
		this.decay = decay;
		initialRadius = (double)mapSize / 2.0;
		
		rsom = new RSOM_SemiOnline(mapSize, inputLength, rand, initialLearningRate, mapSize, 2, decay);
		
		timeConstant = maxIterations / initialRadius;
	}
	
	private SimpleMatrix addNoise(SimpleMatrix m, double noiseMagnitude){
		double noise = (rand.nextDouble() - 0.5) * 2 * noiseMagnitude;
		m = m.plus(noise);
		return m;
	}
	
	private SimpleMatrix buildNormFMap(SomMap map){
		SimpleMatrix m = new SimpleMatrix(map.getHeight(), map.getWidth());
		
		for (SomNode n : map.getNodes()){
			int id = n.getId();
			SimpleMatrix vector = n.getVector().scale(scale);
			double normF = vector.normF();
			m.set(id, normF);
		}
		
		return m;
	}

	
	private ArrayList<SimpleMatrix[]> createSequences_NoValueIsTheSame(int numSequences, int sequenceLength){
		ArrayList<SimpleMatrix[]> sequences = new ArrayList<>();
		int numInputs = numSequences * sequenceLength;
		scale = numInputs;
		
		int value = 1;
		
		for (int i = 0; i < numSequences; i++){
			SimpleMatrix[] seq = new SimpleMatrix[sequenceLength];
			for (int j = 0; j < sequenceLength; j++){
				double[][] data = {{value}};
				SimpleMatrix m = new SimpleMatrix(data);
				m = m.divide(scale);
				seq[j] = m;
				value++;
			}
			sequences.add(seq);
		}
		
		return sequences;
		
	}
	

	
	private ArrayList<SimpleMatrix[]> createSequences_Longer(){
		ArrayList<SimpleMatrix[]> sequences = new ArrayList<>();
		scale = 20;		
		
		//double[] possibleInputs = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		double[] possibleInputs = {1,2,3,4,0,1,2,2,3,0,4,3,2,2,0,3,3,2,1,0,4,3,2,1};
		SimpleMatrix[] seq = new SimpleMatrix[possibleInputs.length];
		for (int i = 0; i < possibleInputs.length; i++){
			double d = possibleInputs[i];
			double[][] in1 = {{d}};
			SimpleMatrix ma1 = new SimpleMatrix(in1);
			ma1 = ma1.divide(scale);
			seq[i] = ma1;
		}
		
		sequences.add(seq);
		return sequences;
	}
	
	public void adjustNeighborhoodRadius(int iteration){
		curNeighborhoodRadius = initialRadius * Math.exp(-(double) iteration / timeConstant);	
	}
	
	public void adjustLearningRate(int iteration){
		curLearningRate = initialLearningRate * Math.exp(-(double) iteration / maxIterations);
	}
	
	protected double calculateNeighborhoodEffect(SomNode n, SomNode bmu){
		double dist = Math.pow(n.normDistanceTo(bmu),2);
		double bottom = 2 * Math.pow(curNeighborhoodRadius, 2);
		double effect =  Math.exp(- dist / bottom);
		return effect;
	}
	
	private void printLeakyMaps(){
		System.out.println();
		System.out.println("Rsom leaky map:");
		buildNormFMap(rsom.getLeakyDifferencesMap()).print();
		System.out.println();
	}
	
	private void printWeightMaps(){
		System.out.println();
		System.out.println("Rsom weigth map:");
		buildNormFMap(rsom.getSomMap()).print();
		System.out.println();
	}

}
