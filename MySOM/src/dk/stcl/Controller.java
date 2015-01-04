package dk.stcl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.gui.SomActivationDrawer;
import dk.stcl.gui.SomModelDrawer;
import dk.stcl.som.SOM;
import dk.stcl.som.SomNode;
import dk.stcl.utils.DataLoader;

public class Controller {

	private SimpleMatrix data;
	private SOM som;
	private SomModelDrawer gui;
	private Random rand;
	private DataLoader trainLoader, validationLoader, testLoader;
	private FileWriter writer;
	
	/********************************************/
	/*      Parameters used in the code         */
	/********************************************/
	private final int GUI_SIZE = 500;
	private final int SOM_SIZE = 10;
	private final double INITIAL_LEARNING = 0.1;
	private final int MAX_ITERATIONS = 1000;
	/**
	 * @throws IOException ******************************************/
	
	public static void main(String[] args) throws IOException{
		String dataPathTrain ="C:/Users/Simon/Documents/Experiments/SOM/MNIST/training.csv";
		String dataPathValidation ="C:/Users/Simon/Documents/Experiments/SOM/MNIST/validation.csv";
		String dataPathTest ="C:/Users/Simon/Documents/Experiments/SOM/MNIST/test.csv";
		String filepathTestLabels = "C:/Users/Simon/Documents/Experiments/SOM/MNIST/test_output.csv";
		
		Controller c = new Controller();
		c.setupExperiment(dataPathTrain, dataPathValidation, dataPathTest, filepathTestLabels);
		c.run(false);
	}
	
	public Controller() {
		// TODO Auto-generated constructor stub
	}

	public void setupExperiment(String trainFilePath, String validationFilePath, String testFilePath, String filpathTestLabels) throws IOException{
		
		System.out.println("Importing data");
		
		/*
		//Load training data
		DataLoader loader = new DataLoader();
		data = loader.loadData(inputFilePath, ",", true, true);
		loader = null;
		*/
		
		//Initialize data loaders
		trainLoader = new DataLoader();
		trainLoader.init(trainFilePath, ",");
		
	    validationLoader = new DataLoader();
	    validationLoader.init(validationFilePath, ",");
	    
	    testLoader = new DataLoader();
	    testLoader.init(testFilePath, ",");
	    
	    writer = new FileWriter(filpathTestLabels);
	    
		
		//Create SOM
		System.out.println("Creating som");
		rand = new Random();
		//int inputLength = data.numCols();
		int inputLength = trainLoader.getNumColumns() - 1;
		int size = SOM_SIZE;
		som = new SOM(size, size, inputLength, rand, INITIAL_LEARNING, size / 2);
				
		/*
		//Create GUI
		System.out.println("Creating gui");
		gui = new SomModelDrawer(som, GUI_SIZE);
		gui.setTitle("Visualization");
		gui.pack();
		gui.setVisible(true);
		*/
		
	}
	
	/**
	 * 
	 * @param withLabels if true first value is removed from the sample
	 */
	public void run(boolean visualizeClustering){
		System.out.println("Starting run");
		int maxIterations = trainLoader.getNumLines(); // MAX_ITERATIONS;//data.size() * 3; //TODO: Should probably be something else
		//int dataSize = data.numRows();
		//int inputlength = data.numCols();
		int FRAMES_PER_SECOND = 30;
	    int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
		
	    //Clustering
	    System.out.println("Starting clustering");
	    String line;
	    int iteration = 1;
	    trainLoader.readline(); //Jump over headlines
	    while ((line = trainLoader.readline()) != null){
	    	if (iteration % 1000 == 0){
	    		System.out.println("Clustering - Iteration: " + iteration + " / " + maxIterations);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(",");
	    	double[] sample = new double[content.length - 1];
			for (int i = 1; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //We don't want to include the label in column 1
				d = d / (double) 255; //Normalization
				sample[i-1] = d;
			}
			
			//Sensitize som
			som.sensitize(iteration, maxIterations);
			
			//Present sample to som
			som.step(sample);
			
			if (visualizeClustering){
				//Visualize
				visualizeSom(iteration, maxIterations);
				
				try {
					Thread.sleep(SKIP_TICKS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
			iteration++;
	    }
		
	    /*
	    for (int iteration = 1; iteration <= maxIterations; iteration++){
			
			
			
			//Choose random sample
			int sampleID = rand.nextInt(dataSize);
			double[] sample;
			if (withLabels){
				 sample = data.extractMatrix(sampleID, sampleID + 1, 1, inputlength).getMatrix().data;
			} else {
				sample = data.extractVector(true, sampleID).getMatrix().data;
			}
			
			//Sensitize som
			som.sensitize(iteration, maxIterations);
			
			//Present sample to som
			som.step(sample);
			
			//Visualize
			visualizeSom(iteration, maxIterations);
			
			try {
				Thread.sleep(SKIP_TICKS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		*/
	    
		//Assign labels		
	    
	    trainLoader.reset();
	    
	    int[][] labelVoting = new int[som.getNodes().length][10]; //We use ten now because we know there is ten digits
	    
	    System.out.println("Voting on labels");
	    iteration = 1;
	    trainLoader.readline(); //Jump over headlines
	    while ((line = trainLoader.readline()) != null){
	    	if (iteration % 1000 == 0){
	    		System.out.println("Voting - Iteration: " + iteration + " / " + maxIterations);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(",");
	    	double[] sample = new double[content.length - 1];
			for (int i = 1; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //We don't want to include the label in column 1
				d = d / (double) 255; //Normalization
				sample[i-1] = d;
			}
			
			SimpleMatrix inputVector = new SimpleMatrix(1, content.length - 1, true, sample);
			SomNode bmu = som.getBMU(inputVector);
			int id = bmu.getId();
			int label = (int) Integer.parseInt(content[0]);
			System.out.println("Label: " + label);
			labelVoting[id][label]++;	
			iteration++;
	    }
	    
	    System.out.println("Assigning labels");
	    for (SomNode n : som.getNodes()){
			int id = n.getId();
			int[] votes = labelVoting[id];
			int max = Integer.MIN_VALUE;
			int label = -1;
			for (int i = 0; i < votes.length; i++){
				if (votes[i] > max){
					max = votes[i];
					label = i;
				}
			}
			System.out.println("Setting label to:" + label);
			n.setLabel("" + label);
		}
	    trainLoader.close();
	    
	    System.out.println("Validate results");
	    iteration = 1;
	    som.setLearning(false);
	    validationLoader.readline(); //Jump headline
	    int totalSamples = validationLoader.getNumLines() - 1;
	    int correct = 0;
	    
	    while ((line = validationLoader.readline()) != null){
	    	if (iteration % 1000 == 0){
	    		System.out.println("validating - Iteration: " + iteration + " / " + totalSamples);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(",");
	    	double[] sample = new double[content.length - 1];
			for (int i = 1; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //We don't want to include the label in column 1
				d = d / (double) 255; //Normalization
				sample[i-1] = d;
			}
			
			SimpleMatrix inputVector = new SimpleMatrix(1, content.length - 1, true, sample);
			SomNode bmu = som.getBMU(inputVector);
			int bmuLabel = Integer.parseInt(bmu.getLabel());
			int label = (int) Integer.parseInt(content[0]);
			
			if (bmuLabel == label){
				correct++;
			}		
			
			System.out.println("Classified|Corect: " + bmuLabel + "|" + label);
			iteration++;
	    }
	    
	    double accuracy = (double) correct / totalSamples;
	    
	    System.out.println("Accuracy on validation set: " + accuracy);
	    validationLoader.close();
	    
	    
	    //Classify the test data
	    System.out.println("Classify test data");
	    iteration = 1;
	    testLoader.readline(); //Jump headline
	    totalSamples = testLoader.getNumLines() - 1;
	    String classes ="";
	    while ((line = testLoader.readline()) != null){
	    	if (iteration % 1000 == 0){
	    		System.out.println("Analysing test - Iteration: " + iteration + " / " + totalSamples);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(",");
	    	double[] sample = new double[content.length];
			for (int i = 0; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //No labels in the test data
				d = d / (double) 255; //Normalization
				sample[i] = d;
			}
			
			SimpleMatrix inputVector = new SimpleMatrix(1, content.length, true, sample);
			SomNode bmu = som.getBMU(inputVector);
			String bmuLabel = bmu.getLabel();
			classes += iteration + "," + bmuLabel + "\n";
			
			
			iteration++;
	    }
	    
	    try {
			writer.write(classes);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void visualizeSom(int iteration, int maxIterations){
		gui.setTitle("Visualiztion - Iteration: " + iteration + " / " + maxIterations);
		gui.updateData();
		gui.revalidate();
		gui.repaint();
	}
}
