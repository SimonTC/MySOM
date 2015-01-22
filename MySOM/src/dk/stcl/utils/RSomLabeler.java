package dk.stcl.utils;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomNode;
import dk.stcl.som.rsom.IRSOM;
import dk.stcl.som.som.ISOM;

/**
 * This class is used for labeling the nodes of a SOM
 * @author Simon
 *
 */
public class RSomLabeler {
	
	private Random rand;
	
	public RSomLabeler() {
		rand = new Random();
	}
	
	/**
	 * Labels each node in the SOM with the label of that sample where to the weight vector is smallest
	 * @param rsomToBelabeled
	 * @param data
	 * @param sequenceLabels
	 */
	public void labelSOM (IRSOM  rsomToBelabeled, SimpleMatrix[][] sequences, int[] sequenceLabels, int iterations ){
		assert sequences.length == sequenceLabels.length : "The number of labels does not equal the number of sequences!";
		
		SomNode[] nodes = rsomToBelabeled.getNodes();
		
		SimpleMatrix[] labelScores = new SimpleMatrix[sequenceLabels.length];
		
		for (int sequenceID = 0; sequenceID < sequences.length; sequenceID++){
			rsomToBelabeled.flush();
			SimpleMatrix[] sequence = sequences[sequenceID];
			SimpleMatrix scores = new SimpleMatrix(rsomToBelabeled.getErrorMatrix());
			scores.scale(0);
			
			for (int i = 0; i < iterations; i++){
				int inputID = rand.nextInt(sequence.length);
				SimpleMatrix input = sequence[inputID];
				rsomToBelabeled.step(input);
				scores = scores.plus(rsomToBelabeled.getErrorMatrix());
			}			
			
			labelScores[sequenceID] = scores;
		}
		
		labelNodes(nodes, labelScores, sequenceLabels);
		
	}
	
	private void labelNodes(SomNode[] nodes, SimpleMatrix[]labelScores, int[] labels){
		for (int nodeID = 0; nodeID < nodes.length; nodeID++){
			double minDist = Double.POSITIVE_INFINITY;
			int minLabelID = -1;
			for (int labelID = 0; labelID < labelScores.length; labelID++){
				double score = labelScores[labelID].get(nodeID);  
				if (score < minDist) {minLabelID = labelID; minDist = score;}
			}
			nodes[nodeID].setLabel(minLabelID);
		}
	}

}
