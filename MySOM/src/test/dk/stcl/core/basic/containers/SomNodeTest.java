package test.dk.stcl.core.basic.containers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import dk.stcl.core.basic.containers.SomNode;

public class SomNodeTest {
	SomNode n;
	Random rand = new Random();
	double[] data = {0.25, 0.13, 1.85, 10};
	@Before
	public void setUp() throws Exception {		
		n = new SomNode(data, 1, 3, 5);
		n.setLabel(7);
	}

	@Test
	public void testToString() {
		String result = n.toString();
		String[] arr = result.split(" ");
		assertTrue("ID doesn't match", Integer.parseInt(arr[0]) == 5);
		assertTrue("Label doesn't match", Integer.parseInt(arr[1]) == 7);
		assertTrue("Row doesn't match", Integer.parseInt(arr[2]) == 3);
		assertTrue("Column doesn't match", Integer.parseInt(arr[3]) == 1);
		for (int i = 4; i <8; i++){
			assertEquals(data[i-4], Double.parseDouble(arr[i]), 0.00001);
		}
	}

	@Test
	public void testSomNodeString() {
		String result = n.toString();
		SomNode newNode = new SomNode(result);
		assertTrue("ID doesn't match", newNode.getId() == n.getId());
		assertTrue("Label doesn't match", newNode.getLabel() == n.getLabel());
		assertTrue("Row doesn't match", newNode.getRow() == n.getRow());
		assertTrue("Column doesn't match", newNode.getCol() == n.getCol());
		
		SimpleMatrix correctVector = n.getVector();
		SimpleMatrix newVector = newNode.getVector();
		assertEquals(0, newVector.minus(correctVector).elementSum(), 0.00001); 
	}
	
	

}
