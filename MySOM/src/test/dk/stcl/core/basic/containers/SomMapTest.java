package test.dk.stcl.core.basic.containers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import dk.stcl.core.basic.containers.SomMap;
import dk.stcl.core.basic.containers.SomNode;

public class SomMapTest {
	
	SomMap map;
	Random rand = new Random();

	@Before
	public void setUp() throws Exception {
		map = new SomMap(3, 3, 4, rand);
	}

	@Test
	public void testToFileString() {
		String s = map.toFileString();
		
		String[] lines = s.split("\n");
		String mapSize[] = lines[0].split(" ");
		assertEquals("Column size wrong", 3, Integer.parseInt(mapSize[0]));
		assertEquals("Row size wrong", 3, Integer.parseInt(mapSize[1]));
		assertTrue(lines.length == map.getNodes().length + 1);
	}

	@Test
	public void testSomMapString() {
		String s = map.toFileString();
		SomMap newMap = new SomMap(s);
		
		assertTrue(map.getHeight() == newMap.getHeight());
		assertTrue(map.getWidth() == newMap.getWidth());
		
		SomNode[] nodes = map.getNodes();
		SomNode[] newNodes = newMap.getNodes();
		
		assertTrue(nodes.length == newNodes.length);
		
		for (int i = 0; i <nodes.length; i++){
			SomNode node = nodes[i];
			SomNode newNode = newNodes[i];
			assertTrue(node.equals(newNode));
			assertEquals(0, node.getVector().minus(newNode.getVector()).elementSum(), 0.00001);
		}
	}

}
