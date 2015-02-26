package dk.stcl.core.som;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.core.basic.SomBasics;
import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.rsom.RSOM_Simple;

public class SOM_Simple extends RSOM_Simple implements ISOM {	
	
	public SOM_Simple(int mapSize, int inputLength, Random rand, double initialLearningRate, double activationCodingFactor, int maxIterations) {
		super(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor, maxIterations, 1); //Setting decay to one lets the rsom work as a normal som
	}
	
	


}
