package dk.stcl.som.loopsom;

import java.util.Random;

import dk.stcl.som.SOM;

public class LoopSOMSpatial extends SOM {

	public LoopSOMSpatial(int columns, int rows, int inputLength, Random rand) {
		super(columns, rows, inputLength, rand);
		// TODO Auto-generated constructor stub
	}

	public LoopSOMSpatial(int columns, int rows, int inputLength, Random rand,
			double initialLearningrate, double initialNeighborhodRadius) {
		super(columns, rows, inputLength, rand, initialLearningrate,
				initialNeighborhodRadius);
		// TODO Auto-generated constructor stub
	}

}
