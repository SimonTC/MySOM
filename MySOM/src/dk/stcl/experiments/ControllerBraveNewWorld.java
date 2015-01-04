package dk.stcl.experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import dk.stcl.som.RSOM;

public class ControllerBraveNewWorld {
	private ArrayList<double[][]> sequences;
	private RSOM rsom;
	private Random rand;
	private ArrayList<String> text;
	
	private final int SOM_SIZE = 10;
	private final double INITIAL_LEARNING = 0.1;
	private final double DECAY = 0.7;
	private final int NUM_ITERATIONS = 10;

	public static void main(String[] args) {
		ControllerBraveNewWorld controller = new ControllerBraveNewWorld();
		String path = "C:/Users/Simon/Documents/Experiments/RSOM/Brave_New_World/test.txt";
		controller.run(path);

	}
	
	public void run(String textFilePath){
		setup(textFilePath);
		train();
	}
	
	public void setup(String textFilePath){
		sequences = loadText(textFilePath);
		rand = new Random();
		rsom = new RSOM(SOM_SIZE, SOM_SIZE, 8, rand, INITIAL_LEARNING, SOM_SIZE / 2, DECAY);
	}
	
	public void train(){
		for (int iteration = 1; iteration <= NUM_ITERATIONS; iteration++){
			System.out.println("Starting training - iteration " + iteration);
			for (double[][] word : sequences){
				for (double[] letter : word){
					rsom.step(letter);
				}
				rsom.flush();
			}
			rsom.sensitize(iteration, NUM_ITERATIONS);
		}
	}
	
	public void visualizeMap(){
		rsom.setLearning(false);
		ArrayList<ArrayList<String>> receptiveFields = new ArrayList<ArrayList<String>>();
		
		for (double[][] word : sequences){
			String curWord ="";
			for (double[] letter : word){
				rsom.step(letter);				
			}
		}
		
	}
	
	private ArrayList<double[][]> loadText(String textFilePath){
		ArrayList<double[][]> sequences = new ArrayList<double[][]>();
		try {
			FileReader reader = new FileReader(textFilePath);
			BufferedReader br = new BufferedReader(reader);
			String line;
			String regex = "\\p{Punct}";
			while ((line = br.readLine()) != null){
				//Split
				line = line.trim();
				String[] words = line.split(" ");
				for (String w:words){
					//Remove punctuations
					w = w.replaceAll(regex, "");
					
					text.add(w);
					
					//Convert to binary
					sequences.add(convertToBinary(w, true));
				}
			}
			
			reader.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sequences;
		
	}
	

	private double[][] convertToBinary(String s, boolean toLowerCase){
		
		if (toLowerCase) s = s.toLowerCase();  
		byte[] bytes = s.getBytes();
		
		double[][] inBinary= new double[bytes.length][8];
		for (int b = 0; b < bytes.length; b++){
		     int val = bytes[b];
		     String binary = Integer.toBinaryString(val);
		     while (binary.length() <8){
		    	 binary = "0" + binary;
		     }
		     
		     char[] tmp = binary.toCharArray();
		     for (int i = 0; i < 8; i++){
		    	 inBinary[b][i] = Double.parseDouble("" + tmp[i]);
		     }		     
		}
		return inBinary;

	}

}
