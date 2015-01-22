package dk.stcl.utils;

import org.ejml.simple.SimpleMatrix;

import dk.stcl.som.containers.SomNode;
import dk.stcl.som.som.ISOM;

/**
 * This class is used for labeling the nodes of a SOM
 * @author Simon
 *
 */
public class SomLabeler {

	public SomLabeler() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Labels each node in the SOM with the label of that sample where to the weight vector is smallest
	 * @param somToBelabeled
	 * @param data
	 * @param labels
	 */
	public void labelSOM (ISOM  somToBelabeled, SimpleMatrix[] data, int[] labels ){
		assert data.length == labels.length : "The number of labels does not equal the number of samples!";
		
		SomNode[] nodes = somToBelabeled.getNodes();
		
		double[][] labelScores = new double[labels.length][];
		
		for (int sampleID = 0; sampleID < data.length; sampleID++){
			SimpleMatrix sample = data[sampleID];
			labelScores[sampleID] = calulateDistance(nodes, sample);
		}
		
		labelNodes(nodes, labelScores, labels);
		
	}
	
	private double[] calulateDistance(SomNode[] nodes, SimpleMatrix sample){
		double[] distances = new double[nodes.length];
		
		for (int i = 0; i < nodes.length; i++){
			SomNode n = nodes[i];
			SimpleMatrix weightVector = n.getVector();
			SimpleMatrix diff = weightVector.minus(sample);
			double dist = diff.normF();
			distances[i] = dist;
		}
		
		return distances;
	}
	
	private void labelNodes(SomNode[] nodes, double[][] labelScores, int[] labels){
		for (int nodeID = 0; nodeID < nodes.length; nodeID++){
			double minDist = Double.POSITIVE_INFINITY;
			int minLabelID = -1;
			for (int labelID = 0; labelID < labelScores.length; labelID++){
				double dist = labelScores[labelID][nodeID];
				if (dist < minDist) {minLabelID = labelID; minDist = dist;}
			}
			nodes[nodeID].setLabel(minLabelID);
		}
	}

}
