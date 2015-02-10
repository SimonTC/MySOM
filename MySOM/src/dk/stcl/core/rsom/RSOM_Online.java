package dk.stcl.core.rsom;

import java.util.LinkedList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.containers.SomNode;
/**
 * SOM_Online calculates its error based on how stable it is
 * @author Simon
 *
 */
public class RSOM_Online extends RSOM_SemiOnline implements IRSOM {
	private int memoryLength;
	private LinkedList<Integer> stateChangeList; 
	private double instabilitySum;
	private SomNode oldBMU;
	
	public RSOM_Online(int columns, int rows, int inputLength, Random rand,
			double learningRate, double stddev, double activationCodingFactor,
			double decayFactor) {
		super(columns, rows, inputLength, rand, learningRate, stddev,
				activationCodingFactor, decayFactor);
		
		memoryLength = calculateMemoryLength(0.05, decayFactor);
		stateChangeList = new LinkedList<Integer>();
		instabilitySum = 0;
		oldBMU = null;
		
		
	}
	
	@Override
	public double calculateSOMFitness() {
		//Make sure the memory list is not too long
		if (stateChangeList.size() >= memoryLength){
			double removedValue = stateChangeList.removeFirst();
			instabilitySum = instabilitySum - removedValue;
		}
		
		//See if any instability should be added
		int instability = 0;
		if (oldBMU == null){
			instability = 1;
		} else if (bmu.getId() != oldBMU.getId()){
			instability = 1;
		}
		
		//Add instability to list
		stateChangeList.addLast(instability);
		instabilitySum += instabilitySum;
		
		//Calculate fitness
		double curInstability = (double) instabilitySum / stateChangeList.size();
		
		double fitness = 1 - curInstability;
		
		oldBMU = bmu;
		
		return fitness;
	}


	/**
	 * 
	 * @param minInfluence minimum influence you want the oldest memory to have on the map
	 * @param decay
	 * @return
	 */
	private int calculateMemoryLength(double minInfluence, double decay){
		int memory = (int) Math.ceil(Math.log(minInfluence) / Math.log(1-decay));
		return memory;
	}
}
