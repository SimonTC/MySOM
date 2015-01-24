package dk.stcl.experiments;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomMap;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.rsom.RSOM;
import dk.stcl.utils.RSomLabeler;

public class UnderstandingRSOM {
	
	private RSOM rsom;
	private Random rand = new Random(1234);
	
	private double learningRate = 0.1;
	private double stddev = 1;
	private double decayFactor = 0.7;
	
	private double[][][] sequences;
	
	public static void main (String[] args){
		UnderstandingRSOM a = new UnderstandingRSOM();
		a.run();
	}
	
	public UnderstandingRSOM() {
		// TODO Auto-generated constructor stub
	}
	
	public void run(){
		rsom = new RSOM(2, 2, 2, rand, learningRate, stddev, 0.1, decayFactor);
		//buildSimpleSequences();
		buildMoreComplexSequences();
		//Print initial maps
		printMaps(0);
		
		//Do training
		for (int seqID = 0; seqID < sequences.length; seqID++){
			for (int i = 1; i <= 100; i++){
				double[][] sequence = sequences[seqID];
				for (double[] d : sequence){
					rsom.step(d);
					printMaps(i);
				}
			}
		}
		

		
		
	}
	
	private void buildSimpleSequences(){
		sequences = new double[4][3][2];
		
		double[][] seq1 = {
				{0,0},
				{0,0},
				{0,0}
		};
		sequences[0] = seq1;
		
		double[][] seq2= {
				{1,1},
				{1,1},
				{1,1}
		};
		sequences[1] = seq2;
		
		double[][] seq3 = {
				{0,1},
				{0,1},
				{0,1}
		};
		sequences[2] = seq3;
		
		double[][] seq4 = {
				{1,0},
				{1,0},
				{1,0}
		};
		sequences[3] = seq4;
	}
	
	private void buildMoreComplexSequences(){
		sequences = new double[4][3][2];
		
		double[][] seq1 = {
				{0,0},
				{0,1},
				{0,0}
		};
		sequences[0] = seq1;
		
		double[][] seq2= {
				{1,1},
				{0,0},
				{1,1}
		};
		sequences[1] = seq2;
		
		double[][] seq3 = {
				{0,1},
				{1,1},
				{0,1}
		};
		sequences[2] = seq3;
		
		double[][] seq4 = {
				{1,1},
				{1,0},
				{1,1}
		};
		sequences[3] = seq4;
	}

	private void printMaps(int iteration){
		//Print weight map
		int numChar = 6;
		int precision = 3;
		String format = "%"+numChar+"."+precision+"f ";
		SomMap weights = rsom.getSomMap();
		System.out.print("Weights:    ");
		for (SomNode n : weights.getNodes()){
			for (double d : n.getVector().getMatrix().data){
				System.out.printf(format, d);
			}
			System.out.print("|");
		}
		
		System.out.println();
		System.out.print("Leaky diff: ");
		
		
		//Print difference map
		SomMap diff = rsom.getLeakyDifferencesMap();
		for (SomNode n : diff.getNodes()){
			for (double d : n.getVector().getMatrix().data){
				System.out.printf(format, d);
			}
			System.out.print("|");
		}
		
		System.out.println();
		System.out.println();
		
	}
}
