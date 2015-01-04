package dk.stcl.experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import dk.stcl.som.RSOM;
import dk.stcl.som.SomNode;

public class ControllerBraveNewWorld {
	private ArrayList<double[][]> sequences;
	private RSOM rsom;
	private Random rand;
	private ArrayList<String> text;
	
	private final int SOM_SIZE = 10;
	private final double INITIAL_LEARNING = 0.1;
	private final double DECAY = 0.7;
	private final int NUM_ITERATIONS = 100;
	private final double THRESHOLD = 0.9;

	public static void main(String[] args) {
		ControllerBraveNewWorld controller = new ControllerBraveNewWorld();
		String path = "C:/Users/Simon/Documents/Experiments/RSOM/Brave_New_World/test.txt";
		controller.run(path);

	}
	
	public void run(String textFilePath){
		setup(textFilePath);
		train();
		visualizeMap();
	}
	
	public void setup(String textFilePath){
		text = new ArrayList<String>();
		sequences = loadText(textFilePath);
		System.out.println("Number of words: " + sequences.size());
		rand = new Random();
		rsom = new RSOM(SOM_SIZE, SOM_SIZE, 8, rand, INITIAL_LEARNING, SOM_SIZE / 2, DECAY);
		
	}
	
	public void train(){
		for (int iteration = 1; iteration <= NUM_ITERATIONS; iteration++){
			System.out.println("Starting training - iteration " + iteration);
			for (double[][] word : sequences){
				String s = "";
				for (double[] letter : word){
					rsom.step(letter);
					for (double d : letter){
						s += "" + d;
					}
					s = s + " ";
				}
				if (iteration == NUM_ITERATIONS) System.out.println("BMU for '" + s + "': " + rsom.getBMU().getId());
				
				rsom.flush();
			}
			rsom.sensitize(iteration, NUM_ITERATIONS);
		}
	}
	
	public void visualizeMap(){
		rsom.setLearning(false);
		
		//Figure out where all the words are mapped to
		String[][] receptiveFields = new String[SOM_SIZE][SOM_SIZE];		
		Iterator<String> iterator = text.iterator();
		
		for (double[][] word : sequences){
			String curWord = iterator.next();
			for (double[] letter : word){
				rsom.step(letter);				
			}
			SomNode bmu = rsom.getBMU();
			String s = receptiveFields[bmu.getRow()][bmu.getCol()];
			
			if (s == null) s = "";
			
			if (!s.contains(curWord)){
				s = s + " " + curWord;
			}
			
			receptiveFields[bmu.getRow()][bmu.getCol()] = s;
		}
		
		//Find the common end for the words at each
		String[][] triggers = new String[SOM_SIZE][SOM_SIZE];	
		for (int i = 0; i < SOM_SIZE; i++){
			for (int j = 0; j < SOM_SIZE; j++){
				String[] words = receptiveFields[i][j].split(" ");
				
				//Find max length
				int max = Integer.MIN_VALUE;
				for (String w : words){
					if (w.length() > max) max = w.length();
				}
				
				String trigger = "";
				
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				int k = 0;
				boolean stop = false;
				while ( k < max && !stop){
					//Find possible endings
					for (String w : words){
						String tmp = "";
						if (w.length() < k){
							tmp = w.substring(w.length() - k);
							int m = map.get(tmp);
							map.put(tmp, m + 1);
						}
					}
					
					//Which ending is the most common
					max = Integer.MIN_VALUE;
					int total = 0;
					String bestKey = "";
					for (String s : map.keySet()){
						int n = map.get(s);
						if (n > max){
							max = n;
							bestKey = s;
						}
						total += n;						
					}
					
					double percentage = (double) max / (double) total;
					
					if (percentage > THRESHOLD){
						trigger += bestKey;
					} else {
						stop = true;
					}
					k++;
				}
				triggers[i][j] = trigger;				
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
