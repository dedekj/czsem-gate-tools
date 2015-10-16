package czsem.fs;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.NodeAttributes.IdNodeAttributes;
import czsem.gate.utils.GateAwareTreeIndex;


public class FSTreeWriterTest {

	@Test
	public static void testPrintTree() {
		StringWriter out = new StringWriter();
		
		FSTreeWriter tw = new FSTreeWriter(new PrintWriter(out), new IdNodeAttributes());
		
		GateAwareTreeIndex i = tw.getIndex();
		
		i.addDependency(0,1);
		i.addDependency(0,2);
		i.addDependency(1,3);
		i.addDependency(1,4);
		tw.printTree();
		
		Assert.assertEquals(out.toString(), "[id=0]([id=1]([id=3],[id=4]),[id=2])");
		
	}
}
