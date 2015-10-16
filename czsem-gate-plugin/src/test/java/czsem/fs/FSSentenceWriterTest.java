package czsem.fs;

import gate.Document;
import gate.Factory;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;

public class FSSentenceWriterTest {

	@Test
	public static void testPrintTree() throws Exception {
		GateUtils.initGateKeepLog();
		
		Document doc = Factory.newDocument(
			FSSentenceWriterTest.class.getResource("/stanford.gate.xml"));
		
		StringWriter out = new StringWriter();
		FSSentenceWriter wr = new FSSentenceWriter(doc.getAnnotations(), new PrintWriter(out));
		
		wr.printTree();

		System.err.println(out.toString());
		System.err.println(wr.getAttributes());
		
		Assert.assertEquals(out.toString().replace("\r\n", "\n"), 
				"[category=VBN,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],kind=word,length=10,orth=lowercase,string=visualized,{sentence_order}=4,{ann_id}=9]([category=NNS,dependencies=\\[amod(1)\\],kind=word,length=11,orth=lowercase,string=annotations,{sentence_order}=1,{ann_id}=3]([category=JJ,kind=word,length=10,orth=upperInitial,string=Dependency,{sentence_order}=0,{ann_id}=1]),[category=MD,kind=word,length=3,orth=lowercase,string=can,{sentence_order}=2,{ann_id}=5],[category=VB,kind=word,length=2,orth=lowercase,string=be,{sentence_order}=3,{ann_id}=7],[category=IN,dependencies=\\[pobj(15)\\],kind=word,length=2,orth=lowercase,string=by,{sentence_order}=5,{ann_id}=11]([category=NN,dependencies=\\[det(13)\\],kind=word,length=4,orth=lowercase,string=tool,{sentence_order}=7,{ann_id}=15]([category=DT,kind=word,length=4,orth=lowercase,string=this,{sentence_order}=6,{ann_id}=13])))\n");
		
	}
}
