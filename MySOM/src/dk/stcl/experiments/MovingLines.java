package dk.stcl.experiments;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.gui.SomModelDrawer;
import dk.stcl.som.PLSOM;
import dk.stcl.som.RSOM;
import dk.stcl.som.SOM;

	

public class MovingLines {
	
	private SimpleMatrix[][] sequences;
	private SOM spatialPooler;
	private RSOM temporalPooler;
	private SomModelDrawer gui;
	private final int GUI_SIZE = 500;
	private final int MAX_ITERTIONS = 500;
	private final boolean USE_PLSOM = true;


	public static void main(String[] args){
		MovingLines runner = new MovingLines();
		runner.run();
	}
	
	private void run(){
		boolean visualize = true;
		Random rand = new Random(1234);
		setupExperiment(rand, visualize);
		runExperiment(MAX_ITERTIONS, visualize, rand);
	}
	
	private void runExperiment(int maxIterations, boolean visualize, Random rand){
		int FRAMES_PER_SECOND = 40;
	    int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
	    SimpleMatrix[] seq;
	    
	    for (int i = 1; i <= maxIterations; i++){
	    	//Choose sequence
	    	seq = sequences[rand.nextInt(sequences.length)];
	    	
	    	for (SimpleMatrix m : seq){
	    		spatialPooler.step(m.getMatrix().data);
	    		
	    		if (visualize){
					//Visualize
					visualizeSom(i, maxIterations);
					
					try {
						Thread.sleep(SKIP_TICKS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    		spatialPooler.sensitize(i, maxIterations);
	    	}
	    }
	}
	
	private void visualizeSom(int iteration, int maxIterations){
		gui.setTitle("Visualiztion - Iteration: " + iteration + " / " + maxIterations);
		gui.updateData();
		gui.revalidate();
		gui.repaint();
	}
	
	private void setupExperiment(Random rand, boolean visualize){
		setupPoolers(rand);
		buildSequences();
		if (visualize){
			setupVisualization(spatialPooler, GUI_SIZE);
		}
	}
	
	private void setupVisualization(SOM som, int GUI_SIZE){
		//Create GUI
		//System.out.println("Creating gui");
		gui = new SomModelDrawer(som, GUI_SIZE);
		gui.setTitle("Visualization");
		gui.pack();
		gui.setVisible(true);		
	}
	
	private void setupPoolers(Random rand){
		if (USE_PLSOM){
			spatialPooler = new PLSOM(3, 3, 9, rand);
		} else {
			spatialPooler = new SOM(3, 3, 9, rand);
		}
	}
	
	private void buildSequences(){
		sequences = new SimpleMatrix[3][3];
		
		SimpleMatrix m;
		
		//Horizontal down
		double[][] hor1 = {
				{1,1,1},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(hor1);
		m.reshape(1, 9);
		sequences[0][0] = m;
		
		double[][] hor2 = {
				{0,0,0},
				{1,1,1},
				{0,0,0}};
		m = new SimpleMatrix(hor2);
		m.reshape(1, 9);
		sequences[0][1] = m;
		
		double[][] hor3 = {
				{0,0,0},
				{0,0,0},
				{1,1,1}};
		m = new SimpleMatrix(hor3);
		m.reshape(1, 9);
		sequences[0][2] = m;
		
		//Vertical right
		double[][] ver1 = {
				{1,0,0},
				{1,0,0},
				{1,0,0}};
		m = new SimpleMatrix(ver1);
		m.reshape(1, 9);
		sequences[1][0] = m;
		
		double[][] ver2 = {
				{0,1,0},
				{0,1,0},
				{0,1,0}};
		m = new SimpleMatrix(ver2);
		m.reshape(1, 9);
		sequences[1][1] = m;
		
		double[][] ver3 = {
				{0,0,1},
				{0,0,1},
				{0,0,1}};
		m = new SimpleMatrix(ver3);
		m.reshape(1, 9);
		sequences[1][2] = m;
		
		//Blank
		double[][] blank = {
				{0,0,0},
				{0,0,0},
				{0,0,0}};
		m = new SimpleMatrix(blank);
		m.reshape(1, 9);
		sequences[2][0] = m;
		sequences[2][1] = m;
		sequences[2][2] = m;
	}

}
