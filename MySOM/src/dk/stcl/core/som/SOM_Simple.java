package dk.stcl.core.som;

import java.util.Random;

import dk.stcl.core.rsom.RSOM_Simple;

public class SOM_Simple extends RSOM_Simple implements ISOM {	
	private static final long serialVersionUID = 1L;
	public SOM_Simple(int mapSize, int inputLength, Random rand, double initialLearningRate, double activationCodingFactor, int maxIterations) {
		super(mapSize, inputLength, rand, initialLearningRate, activationCodingFactor, maxIterations, 1); //Setting decay to one lets the rsom work as a normal som
	}
	
	


}
