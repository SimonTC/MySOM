package dk.stcl.experiments.tests.rsom;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.ISomBasics;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.IRSOM;
import dk.stcl.core.rsom.RSOM_Online;
import dk.stcl.core.rsom.RSOM_SemiOnline;
import dk.stcl.core.rsom.RSOM_Simple;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.experiments.movinglines.MovingLinesGUI;

public class CopyOfTemporalStability {
	
	//private IRSOM rsom;
	private RSOM_Simple rsom;
	private ArrayList<SimpleMatrix[]> sequences;
	private SimpleMatrix bigT, bigO, smallO, smallV, blank;
	private MovingLinesGUI frame;
	private Random rand = new Random(1234);
	private SOM_SemiOnline spatialDummy = new SOM_SemiOnline(2,2,5,rand,0,0,0);
	
	private final int ITERATIONS = 100000;
	private final boolean VISUALIZE_TRAINING = false;
	private final boolean VISUALIZE_RESULT = true;
	private final int GUI_SIZE = 500;
	private final int FRAMES_PER_SECOND = 10;

	public static void main(String[] args){
		CopyOfTemporalStability runner = new CopyOfTemporalStability();
		runner.run();
	}

	public void run(){
		//Setup experiment		
		setupExperiment(ITERATIONS, rand);
		
		//Setup graphics
		if (VISUALIZE_TRAINING) setupVisualization(spatialDummy, GUI_SIZE);
		
		runExperiment(ITERATIONS, rand, VISUALIZE_TRAINING);
		
		if (VISUALIZE_RESULT){
			rsom.flush();
			rsom.setLearning(false);
			setupVisualization(spatialDummy, GUI_SIZE);
			runExperiment(ITERATIONS, rand, true);
			 
		}
		
	}

	private void setupExperiment(int maxIterations, Random rand){
		buildSequences();
		buildRSOM();
	}
	
	private void buildRSOM(){
		int mapSize = 2;
		int inputLength = blank.getNumElements();
		double learningRate = 0.01;
		double stddev = 1;
		double activationCodingFactor = 0.125;
		double decay = 0.1;
		
		//rsom = new RSOM_Online(mapSize, mapSize, inputLength, rand, learningRate, stddev, activationCodingFactor, decay);
		
		//rsom = new RSOM_SemiOnline(mapSize, mapSize, inputLength, rand, learningRate, stddev, activationCodingFactor, decay);
		
		rsom = new RSOM_Simple(mapSize, inputLength, rand, ITERATIONS, learningRate, activationCodingFactor, decay);
		
	}

	
	private void runExperiment(int maxIterations, Random rand, boolean visualize){
	    int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
	   
	    float next_game_tick = System.currentTimeMillis();
	    int curSeqID = 0;
    	int curInputID = -1;
	    
	    for (int i = 0; i < maxIterations; i++){
	    	//Choose sequence
	    	boolean change = rand.nextDouble() > 0.90 ? true : false;
	    	SimpleMatrix[] curSequence = null;
			if (change){
				rsom.flush();
				int nextSeqID;
				do {
					nextSeqID = rand.nextInt(sequences.size());
				} while (nextSeqID == curSeqID);
				
				curSeqID = nextSeqID;
				curInputID = -1;
			} 
			curSequence = sequences.get(curSeqID);
			curInputID++;
			curInputID = curInputID >= curSequence.length? 0 : curInputID;
    		
    		//Temporal classification
    		rsom.step((curSequence[curInputID]));
    		
    		if (visualize){
	    		//Update graphics
	    		updateGraphics(curSequence[curInputID], curSeqID);
	    		
	    		//Sleep
				next_game_tick+= SKIP_TICKS;
				try {
					Thread.sleep(SKIP_TICKS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
    		}
    		rsom.sensitize(i, maxIterations);
	    }
	    
	    System.out.println("Rsom models:");
	    System.out.println();
	    for (SomNode n : rsom.getNodes()){
	    	SimpleMatrix vector = new SimpleMatrix(n.getVector());
	    	vector.reshape(2, 2);
	    	vector.print();
	    	System.out.println(vector.elementSum());
	    	System.out.println();
	    }
	}

	private void setupVisualization(ISomBasics som, int GUI_SIZE){
		//Create GUI
		
		frame = new MovingLinesGUI(som, spatialDummy);
		frame.setTitle("Visualiztion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		updateGraphics(blank,0); //Give a blank
		frame.pack();
		frame.setVisible(true);	
	}
	
	private void updateGraphics(SimpleMatrix inputVector, int iteration){
		spatialDummy.step(inputVector.getMatrix().data);
		frame.updateData(inputVector, spatialDummy, rsom);
		frame.setTitle("Visualiztion - Sequence: " + iteration);
		frame.revalidate();
		frame.repaint();

	}


	private void buildSequences(){
		createFigures();
		sequences = new ArrayList<SimpleMatrix[]>();

		SimpleMatrix[] seq1 = {bigT};
		SimpleMatrix[] seq2 = {bigO};
		SimpleMatrix[] seq3 = {smallO};
		
		SimpleMatrix[] seq4 = {smallO, bigO};
		SimpleMatrix[] seq5 = {bigT, smallO};
		SimpleMatrix[] seq6 = {bigT, bigT};
		
		SimpleMatrix[] seq7 = {bigO, smallO, smallO, smallV};
		SimpleMatrix[] seq8 = {bigT, bigO, smallV, bigT};
		SimpleMatrix[] seq9 = {smallO, bigT, bigT, bigO};
		SimpleMatrix[] seq10 = {bigT, smallO, bigO, smallO};
		SimpleMatrix[] seq11 = {bigT, bigT, bigT, bigT};
		SimpleMatrix[] seq12 = {smallO, smallO, bigO, bigO};
		
		SimpleMatrix[] seq5_1 = {bigT, smallO, bigT, bigT, bigT};
		SimpleMatrix[] seq5_2 = {bigT, smallO, smallO, smallO, smallO};
		SimpleMatrix[] seq5_3 = {smallV, smallV, bigO, smallV, smallV};
		SimpleMatrix[] seq5_4 = {bigT, bigO, bigO, bigO, smallV};

		
		sequences.add(seq1);
		sequences.add(seq2);
		sequences.add(seq3);
		
		//sequences.add(seq4);
		//sequences.add(seq5);
		//sequences.add(seq6);
		
		//sequences.add(seq7);
		//sequences.add(seq8);
		/*
		sequences.add(seq9);
		sequences.add(seq10);
		sequences.add(seq11);
		sequences.add(seq12);
		*/
		//sequences.add(seq12);
		
		/*
		sequences.add(seq5_1);
		sequences.add(seq5_2);
		sequences.add(seq5_3);
		sequences.add(seq5_4);
		*/
	}
	
	private void createFigures(){		
		double[][] bigTData = {
				{1,0,0,0,0}};
		bigT = new SimpleMatrix(bigTData);
		bigT.reshape(1, bigT.numCols() * bigT.numRows());
		
		double[][] smallOData = {
				{0,1,0,0,0}};
		smallO = new SimpleMatrix(smallOData);
		smallO.reshape(1, smallO.numCols() * smallO.numRows());
		
		double[][] bigOData = {
				{0,0,1,0,0}};
		bigO = new SimpleMatrix(bigOData);
		bigO.reshape(1, bigO.numCols() * bigO.numRows());
		
		double[][] smallVData = {
				{0,0,0,1,0}};
		smallV = new SimpleMatrix(smallVData);
		smallV.reshape(1, smallV.numCols() * smallV.numRows());
		
		double[][] blankData = {
				{0,0,0,0,1}};
		blank = new SimpleMatrix(blankData);
		blank.reshape(1, blank.numCols() * blank.numRows());
	}

}
