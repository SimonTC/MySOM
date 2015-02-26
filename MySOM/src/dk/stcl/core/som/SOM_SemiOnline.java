package dk.stcl.core.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.CopyOfSomBasics;
import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.RSOM_SemiOnline;

/**
 * This implementation off an online som is based on the description in the LoopSom paper
 * @author Simon
 *
 */
//TODO: Better citation
public class SOM_SemiOnline extends RSOM_SemiOnline implements ISOM {
	
	
	
	/**
	 * 
	 * @param mapSize
	 * @param inputLength
	 * @param rand
	 * @param learningRate
	 * @param activationCodingFactor
	 * @param stddev
	 */
	public SOM_SemiOnline(int mapSize, int inputLength, Random rand, double learningRate, double activationCodingFactor, double stddev ) {
		super(mapSize, inputLength, rand, learningRate, activationCodingFactor, stddev, 1); //Set decay factor to one to use as SOM
	}
	
	

}
