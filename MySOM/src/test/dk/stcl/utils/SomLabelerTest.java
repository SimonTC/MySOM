package test.dk.stcl.utils;

import static org.junit.Assert.*;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import dk.stcl.core.basic.containers.SomNode;
import dk.stcl.core.som.ISOM;
import dk.stcl.core.som.SOM_SemiOnline;
import dk.stcl.utils.SomLabeler;

public class SomLabelerTest {

	SimpleMatrix[] samples;
	int[] labels;
	ISOM som;
	int inputLength;
	Random rand = new Random(1234);
	SomLabeler labeler;
	
	@Before
	public void setUp() throws Exception {
		createSamples();
		labeler = new SomLabeler();
		
	}
	
	private void createSamples(){
		double[][][] tmp = {
				{{0,0,0}},
				{{1,0,0}},
				{{0,1,0}},
				{{0,0,1}},
				{{1,1,0}},
				{{1,1,1}},
				{{1,0,1}},
				{{0,1,1}},
				{{2,1,0}}
		};	
		
		inputLength = tmp[0][0].length;
		
		samples = new SimpleMatrix[tmp.length];
		labels = new int[tmp.length];
		
		for (int i = 0; i < tmp.length; i++){
			samples[i] = new SimpleMatrix(tmp[i]);		
			labels[i] = i;
		}
	}

	@Test
	public void When_SamplesEqualsSomMap_Expect_EachNodeGivenUniqueCorrectLabel(){
		//Setup SOM
		int size = 3;
		som = new SOM_SemiOnline(size, size, inputLength, rand, 0, 0, 0);
		SomNode[] nodes = som.getNodes();
		
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			n.setVector(samples[i]);
		}
		
		//Label SOM
		labeler.labelSOM(som, samples, labels);
		
		//Test
		nodes = som.getNodes();
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			int actual = n.getLabel();
			int expected = labels[i];
			assertTrue("Node " + n.getId() + " Actual: " + actual + " Expected: " + expected, actual == expected);
		}
		
	}
	
	@Test
	public void When_LabelingIsFinished_Expect_AllNodesHasALabel() {
		//Setup SOM
		int size = 3;
		som = new SOM_SemiOnline(size, size, inputLength, rand, 0, 0, 0);
		SomNode[] nodes = som.getNodes();
		
		//Label SOM
		labeler.labelSOM(som, samples, labels);
		
		//Test
		nodes = som.getNodes();
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			int actual = n.getLabel();
			assertTrue("Node " + n.getId() + " Actual: " + actual + " Expected: > -1", actual > -1);
		}

	}
	
	@Test
	public void When_AllNodesEqualToOneSample_Expect_OnlyOneLabelGiven(){
		//Setup SOM
		int size = 3;
		som = new SOM_SemiOnline(size, size, inputLength, rand, 0, 0, 0);
		SomNode[] nodes = som.getNodes();
		int chosenSample = 3;
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			n.setVector(samples[chosenSample]);
		}
		
		//Label SOM
		labeler.labelSOM(som, samples, labels);
		
		//Test
		nodes = som.getNodes();
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			int actual = n.getLabel();
			int expected = chosenSample;
			assertTrue("Node " + n.getId() + " Actual: " + actual + " Expected: " + expected, actual == expected);
		}

	}
	
	@Test
	public void When_NodeNearlyEqualToSample_Expect_NodeGetsLabelOfSample(){
		//Setup SOM
		int size = 3;
		som = new SOM_SemiOnline(size, size, inputLength, rand, 0, 0, 0);
		SomNode[] nodes = som.getNodes();
		
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			
			SimpleMatrix m = samples[i].minus(rand.nextDouble() * 0.4 * 2 - 0.4);
			
			n.setVector(m);
		}
		
		//Label SOM
		labeler.labelSOM(som, samples, labels);
		
		//Test
		nodes = som.getNodes();
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			int actual = n.getLabel();
			int expected = labels[i];
			assertTrue("Node " + n.getId() + " Actual: " + actual + " Expected: " + expected, actual == expected);
		}

	}

}
