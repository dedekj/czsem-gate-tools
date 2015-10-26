package czsem.gate.treex;

import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;

import java.net.URL;
import java.util.List;

import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;

public class TreexPreprocessingTest {
	
	private TreexServerConnectionXmlRpc tsConn;

	public TreexPreprocessingTest() throws Exception
	{
		GateUtils.loggerSetup(Level.ALL);
		
		TreexServerExecution tse = new TreexServerExecution();
		tse.setPortNumber(9097);
		tse.start();

		tsConn = tse.getConnection();
	}
	
	@Test
	public void simplePreprocessingTest() throws Exception {
		
		
		tsConn.initScenario("en");
		testGateXmlFile("demo_en.gate.xml");

		tsConn.initScenario("cs");
		testGateXmlFile("demo_cs.gate.xml");

		//Object treex_ret = tsConn.encodeTreexFile(treexFilePath);
		
	}
	
	@AfterClass
	public void terminate() {
		tsConn.terminateServer();		
	}

	protected void testGateXmlFile(String gateXmlResourceName) throws Exception {
		URL gateXmlUrl = getClass().getResource(gateXmlResourceName);
		GateUtils.initGateKeepLog();
		Document doc = Factory.newDocument(gateXmlUrl, "utf8");
		
		TreexInputDocPrepare ip = new TreexInputDocPrepare(doc, "treex");
		
		Object treex_ret = tsConn.analyzePreprocessedDoc(doc.getContent().toString(), ip.createInputDocData());
		
		Document retDoc = AnnotateWithTreexSourceTest.annotateUsingTeexReturn(treex_ret, gateXmlUrl);
		
		comapareSets(retDoc, doc, "treex", "Sentence");
		comapareSets(retDoc, doc, "treex", "Token");
	}

	public static List<Annotation> getAnns(Document doc, String set, String type) {
		return gate.Utils.inDocumentOrder(doc.getAnnotations(set).get(type));
	}

	public static void comapareSets(Document aDoc, Document eDoc, String set, String type) {
		List<Annotation> aSet = getAnns(aDoc, set, type);
		List<Annotation> eSet = getAnns(eDoc, set, type);
		
		for (int a=0; a<aSet.size(); a++)
		{
			Assert.assertEquals(
					aSet.get(a).getStartNode().getOffset(), 
					eSet.get(a).getStartNode().getOffset());
			
			Assert.assertEquals(
					aSet.get(a).getEndNode().getOffset(), 
					eSet.get(a).getEndNode().getOffset());
			
			compareFeatures(aSet.get(a).getFeatures(), eSet.get(a).getFeatures());
			
			/*
			System.err.print(aSet.get(a));
			System.err.println(eSet.get(a));
			System.err.println();
			*/
		}
		
	}

	public static void compareFeatures(FeatureMap aFM, FeatureMap eFM) {
		Assert.assertEquals(aFM, eFM);
		/*
		System.err.println(aFM);
		System.err.println(eFM);
		System.err.println();
		*/
	}


}
