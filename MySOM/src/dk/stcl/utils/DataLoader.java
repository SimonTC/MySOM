package dk.stcl.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

public class DataLoader {
	
	private FileReader reader;
	private BufferedReader br;
	private int numLines, numColumns;
	private String filePath, delimiter;
	
	public boolean init(String datafilePath, String delimiter){
		this.filePath = datafilePath;
		this.delimiter = delimiter;
		try {
			setNumColumns(countColumns(datafilePath, delimiter));
			reader = new FileReader(datafilePath);
			numLines = countLines(datafilePath);
			br = new BufferedReader(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean reset(){
		this.close();
		return this.init(filePath, delimiter);
		
	}
	
	public int getNumLines(){
		return numLines;
	}
	
	public String readline(){
		String s = null;
		try {
			s = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public void close(){
		try {
			reader.close();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param datafilePath path to the data file
	 * @param splitBy separator used in the file
	 * @param containsTitles if true, the first line will be ignored
	 * @param featureScaling if true, the data will be normalized to the range [0;1]. Normalization is based on the values of the whole data set
	 * @return
	 */
	public  SimpleMatrix loadData(String datafilePath, String splitBy, boolean containsTitles, boolean featureScaling){
		BufferedReader br = null;
		
		double[][] data;
		SimpleMatrix matrix = null;
		
		try {
			
			//Count number of lines in file
			int numLines = countLines(datafilePath);
			
			FileReader reader = new FileReader(datafilePath);
			br = new BufferedReader(reader);
			String line;
			
			int numColumns = 0;
			//Figure out how many columns there is in the file
			line = br.readLine();
			if (line != null){
				String[] tmp = line.split(splitBy);
				numColumns = tmp.length;
			}
			//Reset
			reader.close();
			br.close();
			reader = new FileReader(datafilePath);
			br = new BufferedReader(reader);
			
			matrix = new SimpleMatrix(numLines, numColumns);
			
			//Import file
			
			if (containsTitles){
				br.readLine();
			}
			
			double maxValue = Double.NEGATIVE_INFINITY;
			double minValue = Double.POSITIVE_INFINITY;
			
			int lineCounter = 0;
				
			while ((line = br.readLine()) != null){
				String[] content = line.split(splitBy);
				for (int i = 0; i < content.length; i++){
					Double d =Double.parseDouble(content[i]);
					
					if (d > maxValue) maxValue = d;
					if (d < minValue) minValue = d;
					
					matrix.set(lineCounter, i, d);
				}
				
				lineCounter++;
			}
			
			if (featureScaling){
				//Using the common ops from ejml to prevent new matrices from being created s this might lead to out of memory
				CommonOps.subtract(matrix.getMatrix(), minValue, matrix.getMatrix());
				CommonOps.divide(matrix.getMatrix(),maxValue-minValue);
			}			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return matrix;
	}
	
	private int countColumns(String filename, String delimiter) throws IOException{
		FileReader r = new FileReader(filename);
		BufferedReader b = new BufferedReader(r);
		
		int numColumns = 0;
		//Figure out how many columns there is in the file
		String line = b.readLine();
		if (line != null){
			String[] tmp = line.split(delimiter);
			numColumns = tmp.length;
		}
		//Reset
		r.close();
		b.close();
		
		return numColumns;

	}
	/**
	 * Source: http://stackoverflow.com/a/453067
	 * @return
	 */
	private int countLines(String filename) throws IOException {
		    InputStream is = new BufferedInputStream(new FileInputStream(filename));
		    try {
		        byte[] c = new byte[1024];
		        int count = 0;
		        int readChars = 0;
		        boolean empty = true;
		        while ((readChars = is.read(c)) != -1) {
		            empty = false;
		            for (int i = 0; i < readChars; ++i) {
		                if (c[i] == '\n') {
		                    ++count;
		                }
		            }
		        }
		        return (count == 0 && !empty) ? 1 : count;
		    } finally {
		        is.close();
		    }
		
	}

	public int getNumColumns() {
		return numColumns;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

}
