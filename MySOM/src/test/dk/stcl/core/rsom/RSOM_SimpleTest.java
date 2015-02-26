package test.dk.stcl.core.rsom;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.RSOM_Simple;

public class RSOM_SimpleTest {
	private SomMap weightMap, leakyDifferencesMap;
	private RSOM_Simple rsom;
	private Random rand = new Random(1234);
	double initialLearningRate, activationCodingFactor, decay;
	int maxIterations;
	double curNeighborhoodRadius;
	double curLearningRate;
	double timeConstant;
	SomNode bmu;
	double e = 0.000001;

	@Before
	public void setUp() throws Exception {
		int mapSize = 2;
		int inputLength = 1;
		initialLearningRate = 1;
		activationCodingFactor = 0.125;
		decay = 0.8;
		maxIterations = 20;
		
		rsom = new RSOM_Simple(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor, maxIterations, decay);
		weightMap = new SomMap(mapSize, mapSize, inputLength);
		copyWeightMap();
		leakyDifferencesMap = new SomMap(mapSize, mapSize, inputLength);
		
		timeConstant = maxIterations / mapSize;
	}
	
	private void copyWeightMap(){
		for (SomNode n : rsom.getNodes()){
			int id = n.getId();
			SimpleMatrix copy = new SimpleMatrix(n.getVector());
			weightMap.get(id).setVector(copy);
		}
	}

	@Test
	public void test() {
		ArrayList<SimpleMatrix[]> sequences = createSequences();
		for (int i = 0; i < maxIterations; i++){
			
			if (i % 1000 == 0){
				System.out.println("Iteration: " + i + " / " + maxIterations);
			}
			//Choose random sequence
			SimpleMatrix[] seq = sequences.get(rand.nextInt(sequences.size()));
			int counter = 0;
			for (SimpleMatrix m : seq){
				if (counter >= seq.length) counter = 0;
				//Add noise
				//m = addNoise(m, 0.2);
				
				/*
				System.out.println();
				System.out.println("Input");
				m.print();
				*/
				
				//Present to network
				rsom.step(m);
				testStep(m);
				
				counter++;
				rsom.sensitize(i);	
				assertTrue("Test failed in iteration " + i + "-" + counter , isRsomCorrect());
			}
							
			//rsom.flush();
			
		}
		
		printLeakyMaps();
		printWeightMaps();
		
	}
	
	private void printBMU(){
		System.out.println("Rsom bmu: " + rsom.getBMU().getId());
		System.out.println("Our bmu:  " + bmu.getId());
		System.out.println();
	}
	
	private void printLeakyMaps(){
		System.out.println("Rsom leaky map:");
		buildNormFMap(rsom.getLeakyDifferencesMap()).print();
		System.out.println();
		System.out.println("Our leaky map:");
		buildNormFMap(leakyDifferencesMap).print();
		System.out.println();
	}
	
	private void printWeightMaps(){
		System.out.println("Rsom weigth map:");
		buildNormFMap(rsom.getSomMap()).print();
		System.out.println();
		System.out.println("Our weigth map:");
		buildNormFMap(weightMap).print();
		System.out.println();
	}
	
	boolean isRsomCorrect(){
		//Test that BMU is the same
		if (!bmu.equals(rsom.getBMU())){
			System.out.println("BMU not the same");
			printBMU();
			return false;
		}
		
		//Test that leaky values are the same		
		SomNode[] rsomLeakyNodes = rsom.getLeakyDifferencesMap().getNodes();
		SomNode[] ourLeakyNodes = leakyDifferencesMap.getNodes();
		
		for (int i = 0; i < ourLeakyNodes.length; i++){
			SimpleMatrix rsomLeakVector = rsomLeakyNodes[i].getVector();
			SimpleMatrix ourLeakVector = ourLeakyNodes[i].getVector();
			
			double rsomNormF = rsomLeakVector.normF();
			double ourNormF = ourLeakVector.normF();
			if (!isEqual(ourNormF, rsomNormF, e)){
				System.out.println("Leaky map not the same!");
				printLeakyMaps();
				return false;				
			}			
		}
		
		//Test that weights are the same
		SomNode[] rsomWeightNodes = rsom.getNodes();
		SomNode[] ourWeightNodes = weightMap.getNodes();
		for (int i = 0; i < ourWeightNodes.length; i++){
			SimpleMatrix rsomWeightVector = rsomWeightNodes[i].getVector();
			SimpleMatrix ourWeigtVector = ourWeightNodes[i].getVector();
			
			double rsomNormF = rsomWeightVector.normF();
			double ourNormF = ourWeigtVector.normF();
			if (!isEqual(ourNormF, rsomNormF, e)){
				System.out.println("Weight map not the same!");
				printWeightMaps();
				return false;				
			}			
		}
		
		return true;
		
		
		
		
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
	
	private boolean isEqual(double a, double b, double e){
		double diff = a - b;
		return (diff > 0 - e && diff < 0 + e) ;
		
	}
	
	private void testStep(SimpleMatrix input){
		updateLeakyDifferencesMap(input);
		bmu = findBMU(leakyDifferencesMap);
		updateWeightMap(bmu);
		
	}
	
	private void updateLeakyDifferencesMap(SimpleMatrix input){
				
		for (int i = 0; i < leakyDifferencesMap.getNodes().length; i++){
			SomNode leakyNode = leakyDifferencesMap.get(i);
			SomNode weightNode = weightMap.get(i);
			updateLeakyNode(leakyNode, weightNode, input);			
		}
		
	}
	
	private void updateLeakyNode(SomNode leakyNode, SomNode weightNode, SimpleMatrix inputVector){
		SimpleMatrix leakedDifBefore = leakyNode.getVector();
		SimpleMatrix weightVector = weightNode.getVector();
		SimpleMatrix leakedDifNow = leakedDifBefore.scale(1-decay);
		SimpleMatrix weightDif = inputVector.minus(weightVector);
		weightDif = weightDif.scale(decay);
		leakedDifNow = leakedDifNow.plus(weightDif);
		leakyNode.setVector(leakedDifNow);
	}
	
	private SomNode findBMU(SomMap leakyDifMap){
		double min = Double.POSITIVE_INFINITY;
		SomNode bmu = null;
		for (SomNode n : leakyDifMap.getNodes()){
			double value = n.getVector().normF();
			if ( value < min){
				min = value;
				bmu = n;
			}
		}
		return bmu;
	}
	
	private void updateWeightMap(SomNode bmu){
		for (int i = 0; i < weightMap.getNodes().length; i++){
			SomNode leakyNode = leakyDifferencesMap.get(i);
			SomNode weightNode = weightMap.get(i);
			double neighborEffect = calculateNeighborhoodEffect(weightNode, bmu);
			updateWeightNode(weightNode, curLearningRate, neighborEffect, leakyNode);		
		}
	}
	
	private void updateWeightNode(SomNode weightNode, double learning, double neighborhoodEffect, SomNode leakyNode){
		SimpleMatrix leakedDIffVector = leakyNode.getVector();
		SimpleMatrix oldWeights = weightNode.getVector();
		SimpleMatrix delta = leakedDIffVector.scale(learning * neighborhoodEffect);
		SimpleMatrix newWeights = oldWeights.plus(delta);
		weightNode.setVector(newWeights);
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
	
	public void adjustNeighborhoodRadius(int iteration){
		double initialRadius =  (double) weightMap.getHeight() / 2.0;
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

}