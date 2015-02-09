package dk.stcl.experiments.tests.som;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.som.ISOM;
import dk.stcl.core.som.SOM_Normal;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.experiments.movinglines.MovingLinesGUI;

public class TwoDPictures {
	
	private ISOM pooler;
	private ISOM possibleInputs;
	private MovingLinesGUI frame;
	private SimpleMatrix[] figureMatrices;
	private enum SOMTYPES {Normal, Semi_Online, PLSOM};
	private SOMTYPES somType = SOMTYPES.Normal;
	private final int FRAMES_PER_SECOND = 10;
	private Random rand = new Random(1234);
    private int maxIterations = 1000;
    private boolean useSimpleImages = true;
	int figureRows;
	int figureColumns;
	int mapSize = 3;
    
    private boolean visualizeTraining = false;
    private boolean visualizeResult = true;
	
	public static void main(String[] args) {
		TwoDPictures runner = new TwoDPictures();
		runner.run();
	}
	
	public void run(){
		setupExperiment(maxIterations, useSimpleImages, visualizeTraining);		
		runExperiment(visualizeTraining);
		
		if (visualizeResult){
			pooler.setLearning(false);
			setupGraphics(pooler, figureRows, mapSize);
			runExperiment(visualizeResult);
		}
		
	}
	
	private void runExperiment(boolean visualization){
		int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;	
		for (int i = 0; i < maxIterations; i++){			
			//Choose random figure
			SimpleMatrix input = figureMatrices[rand.nextInt(figureMatrices.length)];
			
			//Sensitize pooler
			pooler.sensitize(i, maxIterations);
			
			//Feed forward
			pooler.step(input);
			
			
			if (visualization){
				//Update graphicss
				updateGraphics(input, pooler, i);
				
				//Sleep
				try {
					Thread.sleep(SKIP_TICKS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}			
		
	}
	
	private void setupExperiment(int iterations, boolean simple, boolean visualization){

		
		//Create Figure matrices
		if (simple){
			figureMatrices = simpleFigures();
			figureRows = 5;
			figureColumns = 5;
		} else {
			figureMatrices = pixelArtFigures();
			figureRows = 100;
			figureColumns = 100;			
		}

		
		//Create spatial pooler
		int inputLength = figureColumns * figureRows;

		double activationCodingFactor = 0.125;
		double initialLearningRate = 0.1;
		switch (somType) {
		case Normal:
			pooler = new SOM_Normal(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor);
			break;
		case PLSOM:
			break;
		case Semi_Online: 
			pooler = new SOM_SemiOnline(mapSize, mapSize, inputLength, rand, initialLearningRate, 3, activationCodingFactor);
			break;
		default:
			break;
		
		}
		
		
		if (visualization) {
			//Setup graphics
			setupGraphics(pooler, figureRows, mapSize);
		}
		
	}
	
	private void updateGraphics(SimpleMatrix inputVector, ISOM spatialPooler, int iteration){
		frame.updateData(inputVector, spatialPooler, null);
		frame.setTitle("Visualiztion - Iteration: " + iteration);
		frame.revalidate();
		frame.repaint();
	}
	
	private void setupGraphics(ISOM spatialPooler, int somModelSize, int sizeOfSom) {
		frame = new MovingLinesGUI(spatialPooler, possibleInputs);
		frame.setTitle("Visualiztion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create blank input
		int numRows = figureMatrices[0].numRows();
		int numCols = figureMatrices[0].numCols();
		SimpleMatrix tmp = new SimpleMatrix(numRows, numCols);
		
		//Update graphics
		updateGraphics(tmp, spatialPooler, 0);
		frame.pack();
		frame.setVisible(true);

	}
	
	private SimpleMatrix[] simpleFigures(){
		SimpleMatrix[] matrices = new SimpleMatrix[4];
		
		double[][] bigTData = {
				{0,0,0,0,0},
				{0,1,1,1,0},
				{0,0,1,0,0},
				{0,0,1,0,0},
				{0,0,1,0,0}};
		SimpleMatrix bigT = new SimpleMatrix(bigTData);
		bigT.reshape(1, bigT.numCols() * bigT.numRows());
		matrices[0] = bigT;
		
		double[][] smallOData = {
				{0,0,0,0,0},
				{0,1,1,1,0},
				{0,1,0,1,0},
				{0,1,1,1,0},
				{0,0,0,0,0}};
		SimpleMatrix smallO = new SimpleMatrix(smallOData);
		smallO.reshape(1, smallO.numCols() * smallO.numRows());
		matrices[1] = smallO;
		
		double[][] bigOData = {
				{1,1,1,1,1},
				{1,0,0,0,1},
				{1,0,0,0,1},
				{1,0,0,0,1},
				{1,1,1,1,1}};
		SimpleMatrix bigO = new SimpleMatrix(bigOData);
		bigO.reshape(1, bigO.numCols() * bigO.numRows());
		matrices[2] = bigO;
		
		double[][] smallVData = {
				{0,0,0,0,0},
				{0,0,0,0,0},
				{1,0,0,0,1},
				{0,1,0,1,0},
				{0,0,1,0,0}};
		SimpleMatrix smallV = new SimpleMatrix(smallVData);
		smallV.reshape(1, smallV.numCols() * smallV.numRows());
		matrices[3] = smallV;
		
		return matrices;
	}
	
	private SimpleMatrix[] pixelArtFigures(){
		String parent = "images/PixelArt/";
		String marioPath = parent + "MarioBW.png";
		String nyanCatPath = parent + "NyanCatBW.png";
		String obama1Path = parent + "Obama1BW.png";
		String obama2Path = parent + "Obama2BW.png";
		
		SimpleMatrix[] matrices = new SimpleMatrix[4];
		double[][] marioData = loadNormalizedImage(marioPath);
		double[][] nyanCatData = loadNormalizedImage(nyanCatPath);
		double[][] obama1Data = loadNormalizedImage(obama1Path);
		double[][] obama2Data = loadNormalizedImage(obama2Path);
		
		SimpleMatrix mario = new SimpleMatrix(marioData);
		mario.reshape(1, 100*100);
		
		
		SimpleMatrix nyanCat = new SimpleMatrix(nyanCatData);
		nyanCat.reshape(1, 100*100);
		
		SimpleMatrix obama1 = new SimpleMatrix(obama1Data);
		obama1.reshape(1, 100*100);
		
		SimpleMatrix obama2 = new SimpleMatrix(obama2Data);
		obama2.reshape(1, 100*100);
		
		matrices[0] = mario;
		matrices[1] = nyanCat;
		matrices[2] = obama1;
		matrices[3] = obama2;
		
		
		
		return matrices;
	}
	
	private double[][] loadNormalizedImage(String imagePath){
		try 
		{
		    File image = new File(imagePath);
			BufferedImage img = ImageIO.read(image);
			//int[][] rgbValues = convertToRGB(img);
			
			Raster raster=img.getData();
			
			int w = raster.getWidth();
		    int h=raster.getHeight();
		    double normPixelValues[][]=new double[w][h];
		    
		    int counter = 0;
		    
		   for (int x=0;x<w;x++)
		    {
		        for(int y=0;y<h;y++)
		        {
		        	int rgb = img.getRGB(x, y);
		        	int grayValue = (rgb) & 0x000000FF; //Is the same as the blue = red = green rgb value
		        	normPixelValues[y][x]=   1- ((double)grayValue / 255);
		        	
		        }
		    }

		    return normPixelValues;
		    
			

		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
		return null;
	}

	private int count(double[][] array){
		int counter = 0;
		for (int x = 0; x < array[0].length; x++){
			for (int y = 0; y < array.length; y++){
				double value = array[x][y];
				if (value > 0 && value < 1){
	        		counter++;
	        	}
			}
		}
		return counter;
	}
}
