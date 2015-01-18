package dk.stcl.experiments.loopSOMFun;

import java.util.Random;

import javax.xml.crypto.NodeSetData;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomNode;
import dk.stcl.som.online.rsom.RSOMlo;
import dk.stcl.som.online.som.SOMlo;

public class FourPoints {

	double[][] trainingSet;
	SOMlo som;
	RSOMlo rsom;
	Random rand = new Random(1234);
	
	public static void main(String[] args) {
		FourPoints runner = new FourPoints();
		runner.run();

	}
	
	public FourPoints() {
		// TODO Auto-generated constructor stub
	}
	
	private void createPoolers(){
		som = new SOMlo(2, 2, 2, rand, 0.1, 1, 0.125);
		rsom = new RSOMlo(2, 1, 4, rand, 0.01, 0.7, 0.125, 0.3);
	}
	
	public void run(){
		createTrainingSet();
		createPoolers();
		train(5000);
		som.setLearning(false);
		rsom.setLearning(false);
		labelRSOM();
		for (SomNode n : rsom.getNodes()){
			System.out.println("Node " + n.getId() + " responds to group " + n.getLabel());
		}
	}
	
	private void createTrainingSet(){
		double[][] tmp = {
				{0,0},
				{0,1},
				{1,1},
				{1,1}
		};
		trainingSet = tmp;
	}
	
	private void train(int iterations){
		int curPairIndex = rand.nextInt(trainingSet.length);
		
		for (int i = 1; i <= iterations; i++){
			curPairIndex = chooseNextPair(curPairIndex);
			double[] pair = trainingSet[curPairIndex];
			
			step(pair);
		}
	}
	
	private void step(double[] input){
		//give input to SOM
		som.step(input);
		
		//Calculate activation
		SimpleMatrix activation = som.computeActivationMatrix();
		
		//Give activation to RSOM
		rsom.step(activation.getMatrix().data);

	}
	
	private void labelRSOM(){
		double[][][] labelPairs = {
				{{0,1},{0,0}},
				{{0,0},{0,1}},
				{{1,1},{1,0}},
				{{1,0},{1,1}}
		};
		
		for (int group = 0; group < labelPairs.length; group++){
			for (int pair = 0; pair < labelPairs[group].length; pair++){
				double[] input = labelPairs[group][pair];
				step(input);
			}
			SomNode bmu = rsom.getBMU();
			bmu.setLabel("" + group);
			rsom.flush();			
		}
	}
	
	private void validate(){
		double ambiguousChance = 0.0;
		
	}
	
	private int chooseNextPair(int curPairIndex){
		double result = rand.nextDouble();
		int nextPair = -1;
		if (result > 0.9){
			//Choose pair from other group
			if (curPairIndex == 0 || curPairIndex == 1){
				nextPair = rand.nextBoolean()? 2 : 3;
			} else {
				nextPair = rand.nextBoolean()? 0 : 1;
			}
		} else {
			//Choose pair from current group
			switch (curPairIndex){
			case 0: nextPair = 1; break;
			case 1: nextPair = 0; break;
			case 2: nextPair = 3; break;
			case 3: nextPair = 2; break;
			}
		}
		
		return nextPair;
	}
	

}
