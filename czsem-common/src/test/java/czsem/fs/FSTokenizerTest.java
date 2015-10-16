package czsem.fs;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FSTokenizerTest {

	@Test
	public static void testFSTokenizer() {
		FSTokenizer t = new FSTokenizer("[a=\\<b\\>]");

		
		Assert.assertEquals(t.getCharList(), Arrays.asList(new Character[] {'[', null, '=', null, ']'}));
		Assert.assertEquals(t.getStringList(), Arrays.asList(new String[] {"a","<b>"}));
		
		t = new FSTokenizer("[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],length=10]([string=annotations]([]))");
		System.err.println(t.getCharList());
		System.err.println(t.getStringList());

		Assert.assertEquals(t.getCharList(), Arrays.asList(new Character[] {'[', null, '=', null, ',', null, '=', null, ',', null, '=', null, ',', null, '=', null, ']', '(', '[', null, '=', null, ']', '(', '[', ']', ')', ')'}));
		Assert.assertEquals(t.getStringList(), Arrays.asList(new String[] {"string", "visualized", "kind", "word", "dependencies", "[nsubjpass(3), aux(5), auxpass(7), prep(11)]", "length", "10", "string", "annotations"}));

		t = new FSTokenizer(" [ id = 2]");
		System.err.println(t.getCharList());
		System.err.println(t.getStringList());

		Assert.assertEquals(t.getCharList(), Arrays.asList(new Character[] {' ', '[', ' ', null, '=', null, ']'}));
		Assert.assertEquals(t.getStringList(), Arrays.asList(new String[] { "id ", " 2"}));

		t = new FSTokenizer(" [ id = 2 ]");
		System.err.println(t.getCharList());
		System.err.println(t.getStringList());

		Assert.assertEquals(t.getCharList(), Arrays.asList(new Character[] {' ', '[', ' ', null, '=', null, ']'}));
		Assert.assertEquals(t.getStringList(), Arrays.asList(new String[] { "id ", " 2 "}));
	}
}
