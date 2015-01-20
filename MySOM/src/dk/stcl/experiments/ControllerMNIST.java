package dk.stcl.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.gui.SomModelDrawer;
import dk.stcl.som.ISomBasics;
import dk.stcl.som.containers.SomNode;
import dk.stcl.som.som.PLSOM;
import dk.stcl.som.som.SOM;
import dk.stcl.utils.DataLoader;

public class ControllerMNIST {

	private SimpleMatrix data;
	private ISomBasics som;
	private SomModelDrawer gui;
	private Random rand = new Random();
	private DataLoader trainLoader, validationLoader, testLoader;
	private FileWriter writer;
	
	/********************************************/
	/*      Parameters used in the code         */
	/********************************************/
	private final int GUI_SIZE = 500;
	private final int SOM_SIZE = 5;
	private final double INITIAL_LEARNING = 0.1;
	private final double STDDEV = 3;
	private final int MAX_ITERATIONS = 1000;
	private final String DELIMITER = ";";
	private final int PRINT_EVERY = 100;
	
	private final boolean CLASSIFY_NEW_DATA = false;
	private final boolean USE_PLSOM = false;
	private static final boolean VISUALIZE = false;
	int FRAMES_PER_SECOND = 100;
	/**
	 * @throws IOException ******************************************/
	
	public static void main(String[] args) throws IOException{
		String dataPathTrain ="C:/Users/Simon/Documents/Experiments/SOM/MNIST/train_small.csv";
		String dataPathValidation ="C:/Users/Simon/Documents/Experiments/SOM/MNIST/validation_small.csv";
		String dataPathTest ="C:/Users/Simon/Documents/Experiments/SOM/MNIST/test.csv";
		String filepathTestLabels = "C:/Users/Simon/Documents/Experiments/SOM/MNIST/test_output.csv";
		
		ControllerMNIST c = new ControllerMNIST();
		c.setupExperiment(dataPathTrain, dataPathValidation, dataPathTest, filepathTestLabels, VISUALIZE);
		c.run(VISUALIZE);
	}
	
	public ControllerMNIST() {
		// TODO Auto-generated constructor stub
	}

	public void setupExperiment(String trainFilePath, String validationFilePath, String testFilePath, String filpathTestLabels, boolean visualize) throws IOException{
		
		System.out.println("Importing data");
		
		/*
		//Load training data
		DataLoader loader = new DataLoader();
		data = loader.loadData(inputFilePath, ",", true, true);
		loader = null;
		*/
		
		//Initialize data loaders
		trainLoader = new DataLoader();
		trainLoader.init(trainFilePath, DELIMITER);
		
	    validationLoader = new DataLoader();
	    validationLoader.init(validationFilePath, DELIMITER);
	    
	    testLoader = new DataLoader();
	    testLoader.init(testFilePath, ",");
	    
	    writer = new FileWriter(filpathTestLabels);
	    
		
		//Create SOM
		System.out.println("Creating som");
		//int inputLength = data.numCols();
		int inputLength = trainLoader.getNumColumns() - 1;
		int size = SOM_SIZE;
		if (USE_PLSOM){
			som = new PLSOM(size, size, inputLength, rand, INITIAL_LEARNING, STDDEV, 0.125);
		} else {
			som = new SOM(size, size, inputLength, rand, INITIAL_LEARNING, STDDEV, 0.125);
		}
		//som = new SOM(size, size, inputLength, rand, INITIAL_LEARNING, 1, 0.125);
		//som = new PLSOM(size, size, inputLength, rand);
				
		if (visualize){
			//Create GUI
			System.out.println("Creating gui");
			gui = new SomModelDrawer(som, GUI_SIZE);
			gui.setTitle("Visualization");
			gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			gui.pack();
			gui.setVisible(true);
		}
		
	}
	
	/**
	 * 
	 * @param withLabels if true first value is removed from the sample
	 */
	public void run(boolean visualizeClustering){
		long startTime = System.nanoTime();
		System.out.println("Starting run");
		int maxIterations = trainLoader.getNumLines(); // MAX_ITERATIONS;//data.size() * 3; //TODO: Should probably be something else
		//int dataSize = data.numRows();
		//int inputlength = data.numCols();
		
	    int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
		
	    //Clustering
	    System.out.println("Starting clustering");
	    String line;
	    int iteration = 1;
	    trainLoader.readline(); //Jump over headlines
	    while ((line = trainLoader.readline()) != null){
	    	if (iteration % PRINT_EVERY == 0){
	    		System.out.println("Clustering - Iteration: " + iteration + " / " + maxIterations);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(DELIMITER);
	    	double[] sample = new double[content.length - 1];
			for (int i = 1; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //We don't want to include the label in column 1
				d = d / (double) 255; //Normalization
				sample[i-1] = d;
			}
			
			
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
	    
	    long endTime = System.nanoTime();
	    
	    long totalTIme = (long) endTime - startTime;
	    
	    System.out.println("Time used: " + (totalTIme));
		
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
	    som.setLearning(false);
	    while ((line = trainLoader.readline()) != null){
	    	if (iteration % PRINT_EVERY == 0){
	    		System.out.println("Voting - Iteration: " + iteration + " / " + maxIterations);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(DELIMITER);
	    	double[] sample = new double[content.length - 1];
			for (int i = 1; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //We don't want to include the label in column 1
				d = d / (double) 255; //Normalization
				sample[i-1] = d;
			}
			
			SimpleMatrix inputVector = new SimpleMatrix(1, content.length - 1, true, sample);
			SomNode bmu = som.findBMU(inputVector);
			int id = bmu.getId();
			int label = (int) Integer.parseInt(content[0]);
			//System.out.println("Label: " + label);
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
			//System.out.println("Setting label to:" + label);
			n.setLabel(label);
		}
	    trainLoader.close();
	    
	    System.out.println("Validate results");
	    iteration = 1;
	    som.setLearning(false);
	    validationLoader.readline(); //Jump headline
	    int totalSamples = validationLoader.getNumLines() - 1;
	    int correct = 0;
	    
	    while ((line = validationLoader.readline()) != null){
	    	if (iteration % PRINT_EVERY == 0){
	    		System.out.println("validating - Iteration: " + iteration + " / " + totalSamples);
	    	}
	    	
	    	//Get sample
	    	String[] content = line.split(DELIMITER);
	    	double[] sample = new double[content.length - 1];
			for (int i = 1; i < content.length; i++){
				double d =Double.parseDouble(content[i]); //We don't want to include the label in column 1
				d = d / (double) 255; //Normalization
				sample[i-1] = d;
			}
			
			SimpleMatrix inputVector = new SimpleMatrix(1, content.length - 1, true, sample);
			SomNode bmu = som.findBMU(inputVector);
			int bmuLabel = bmu.getLabel();
			int label = (int) Integer.parseInt(content[0]);
			
			if (bmuLabel == label){
				correct++;
			}		
			
			//System.out.println("Classified|Corect: " + bmuLabel + "|" + label);
			iteration++;
	    }
	    
	    double accuracy = (double) correct / totalSamples;
	    
	    System.out.println("Accuracy on validation set: " + accuracy);
	    validationLoader.close();
	    
	    if (CLASSIFY_NEW_DATA) {
		    //Classify the test data
		    System.out.println("Classify test data");
		    iteration = 1;
		    testLoader.readline(); //Jump headline
		    totalSamples = testLoader.getNumLines() - 1;
		    String classes ="";
		    while ((line = testLoader.readline()) != null){
		    	if (iteration % PRINT_EVERY == 0){
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
				SomNode bmu = som.findBMU(inputVector);
				int bmuLabel = bmu.getLabel();
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
	}
	
	private void visualizeSom(int iteration, int maxIterations){
		gui.setTitle("Visualiztion - Iteration: " + iteration + " / " + maxIterations);
		gui.updateData();
		gui.revalidate();
		gui.repaint();
	}
}
