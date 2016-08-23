package czsem.gate.treex;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.util.InvalidOffsetException;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.utils.GateUtils;

public class AnnotateWithTreexSourceTest {
	

	static void compareSentencesAndZones(Document doc, Object treex_ret) throws InvalidOffsetException
	{
		List<Map<String, Object>> zones = Utils.objectArrayToGenericList(treex_ret);
		AnnotationSet sents = doc.getAnnotations("treex").get("Sentence");
		
		Iterator<Map<String, Object>> i = zones.iterator();
		
		for (Annotation sentence : gate.Utils.inDocumentOrder(sents))
		{
			Map<String, Object> zone = i.next();						
			Assert.assertEquals(zone.get("sentence"), gate.Utils.stringFor(doc, sentence));			
		}
	}
	

	public static void annotateUsingTeexReturnTest(Object treex_ret, URL gateXmlUrl) throws Exception
	{
		assertDocumentsAreSame(annotateUsingTeexReturn(treex_ret, gateXmlUrl), Factory.newDocument(gateXmlUrl, "utf8"));		
	}

	public static Document annotateUsingTeexReturn(Object treex_ret, URL gateXmlUrl) throws Exception
	{
		
		GateUtils.initGateKeepLog();
		Document doc = Factory.newDocument(gateXmlUrl, "utf8");
		
		compareSentencesAndZones(doc, treex_ret);

		
		String text = doc.getContent().toString();

		Document retDoc = Factory.newDocument(text);

		TreexReturnAnalysis tra = new TreexReturnAnalysis(treex_ret);
		tra.annotate(retDoc, "treex");
		
		return retDoc;
	}
	
	public static void assertDocumentsAreSame(Document actual, Document expected) {
		
		/*
		try {
			String file = expected.getSourceUrl().getFile();
			file = FilenameUtils.getName(file);
			System.err.println(file);
			GateUtils.saveGateDocumentToXML(actual, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		*/
		
		
		//TODO Should be less strict...
		AnnotationSet asAct = actual.getAnnotations("treex");
		AnnotationSet asExpect = expected.getAnnotations("treex");

		Assert.assertEquals(actual.getContent().toString(), expected.getContent().toString());


		Assert.assertEquals(asAct.getAllTypes(), asExpect.getAllTypes());

		Assert.assertEquals(asAct.size(), asExpect.size());
		
		Assert.assertEquals(asAct.get("Sentence"), asExpect.get("Sentence"));
		
				
	}

	void annotateUsingSerializedDataTest(String serializedModelResourceName, String gateXmlResourceName) throws Exception
	{
		Object treex_ret = Utils.deserializeFromStram(getClass().getResourceAsStream(serializedModelResourceName));

		URL gateXmlUrl = getClass().getResource(gateXmlResourceName);
		annotateUsingTeexReturnTest(treex_ret, gateXmlUrl);
	}

	//@Test
	public void annotateUsingSerializedData() throws Exception {
		annotateUsingSerializedDataTest("demo_en.ser", "demo_en.gate.xml");
		annotateUsingSerializedDataTest("demo_cs.ser", "demo_cs.gate.xml");
	}

	@Test
	public void annotateUsingTeexFile() throws Exception {
		annotateUsingTeexFileTest("demo_en.treex", "demo_en.gate.xml");
		annotateUsingTeexFileTest("demo_cs.treex", "demo_cs.gate.xml");
	}

	private void annotateUsingTeexFileTest(String treexFileName, String gateXmlResourceName) throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.start();
		
		TreexServerConnectionXmlRpc tsConn = tse.getConnection();

		URL treexFileUrl = getClass().getResource(treexFileName);
		String treexFilePath = Utils.URLToFilePath(treexFileUrl);
		Object treex_ret = tsConn.encodeTreexFile(treexFilePath);
		
		tsConn.terminateServer();
		
		URL gateXmlUrl = getClass().getResource(gateXmlResourceName);
		annotateUsingTeexReturnTest(treex_ret, gateXmlUrl);
		
	}
}
