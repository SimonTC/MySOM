package dk.stcl.experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import dk.stcl.som.containers.SomNode;
import dk.stcl.som.online.rsom.RSOM;

public class ControllerBraveNewWorld {
	private ArrayList<double[][]> sequences;
	private RSOM rsom;
	private Random rand = new Random(1234);
	private ArrayList<String> text;
	
	private final int SOM_SIZE = 20;
	private final double INITIAL_LEARNING = 0.1;
	private final double DECAY = 0.5;
	private final int NUM_ITERATIONS = 100;
	private final double THRESHOLD = 0.7;

	public static void main(String[] args) {
		ControllerBraveNewWorld controller = new ControllerBraveNewWorld();
		String path = "C:/Users/Simon/Documents/Experiments/RSOM/Brave_New_World/Brave_New_World.txt";
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
		rsom = new RSOM(SOM_SIZE, SOM_SIZE, 8, rand,  DECAY);
		
	}
	
	public void train(){
		for (int iteration = 1; iteration <= NUM_ITERATIONS; iteration++){
			if (iteration % 1 == 0) System.out.println("Starting training - iteration " + iteration);
			for (double[][] word : sequences){
				for (double[] letter : word){
					rsom.step(letter);
				}
				if (iteration == NUM_ITERATIONS) System.out.println("BMU: " + rsom.getBMU().getId());
				
				rsom.flush();
			}

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
			
			s = s.trim();
			
			receptiveFields[bmu.getRow()][bmu.getCol()] = s;
		}
		
		//Find the common end for the words at each
		String[][] triggers = new String[SOM_SIZE][SOM_SIZE];	
		for (int i = 0; i < SOM_SIZE; i++){
			for (int j = 0; j < SOM_SIZE; j++){
				String field = receptiveFields[i][j];
				String trigger = "";
				if (field != null){
					
					String[] words = field.split(" ");
					
					//Find max length
					int maxLength = Integer.MIN_VALUE;
					for (String w : words){
						if (w.length() > maxLength) maxLength = w.length();
					}
					
					
					
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					int k = 0;
					boolean stop = false;
					while ( k < maxLength && !stop){
						
						//Find possible endings
						for (String w : words){
							String tmp = "";
							if (k < w.length()){
								tmp = w.substring(w.length() - k - 1);
								int m = 0;
								if (map.containsKey(tmp)){
									m = map.get(tmp);
								}
								map.put(tmp, m + 1);
							}
						}
						
						//Which ending is the most common
						int maxVotes = Integer.MIN_VALUE;
						int total = 0;
						String bestKey = "";
						for (String s : map.keySet()){
							int n = map.get(s);
							if (n > maxVotes){
								maxVotes = n;
								bestKey = s;
							}
							total += n;						
						}
						
						double percentage = (double) maxVotes / (double) total;
						
						if (percentage > THRESHOLD){
							trigger = bestKey;
						} else {
							stop = true;
						}
						k++;
					}
				} else {
					trigger = "-";
				}
				if (trigger.equalsIgnoreCase(" ")) trigger = "-";
				triggers[i][j] = trigger;				
			}
		}
		
		//Print table
		String s = "";
		for (int  i = 0; i < triggers[0].length; i++){
			s = s + "%15s";
		}
		s = s + "\n";
		for (final Object[] row : triggers) {			
		    System.out.format(s, row);
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
				for (String w : words){
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
