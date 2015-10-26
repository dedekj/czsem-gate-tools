package czsem.gate.treex;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.treex.TreexServerExecution.RedirectionType;

public class TreexAnalysisTest {
	private static final Logger logger = LoggerFactory.getLogger(TreexAnalysisTest.class);
	
	@BeforeClass
	public static void initLogger()
	{
		//TODO
		//GateUtils.loggerSetup(Level.ALL);
		
	}
	
	@Test()
	public void isScenarioInitializedTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.start();

		TreexServerConnectionXmlRpc conn = tse.getConnection();
		
		boolean init = conn.isScenarioInitialized();
		Assert.assertEquals(init, false);

		conn.initScenario("en", "W2A::Segment");

		init = conn.isScenarioInitialized();
		Assert.assertEquals(init, true);
		
		conn.terminateServer();				
	}

	
	@Test( groups="slow" )
	public void unicodeSOHTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.start();
				
		TreexServerConnectionXmlRpc conn = tse.getConnection();
		conn.initScenario("en", "W2A::Segment", "W2A::Tokenize");
		
		for (int i=0; i<= 255; i++) {
			String str = i+" "+Character.toString((char) i);

			try {
				conn.analyzeText(str);
			} catch (Exception e) {	
				System.err.format("case %d: break;\n", i);
				/**/
				conn.terminateServer();			
				throw e;
				/**/
			}
		}

		conn.terminateServer();				
	}

	
	@Test
	public void segmentAndTokenizeTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.start();
				
		TreexServerConnectionXmlRpc conn = tse.getConnection();
		
		conn.initScenario("cs", "W2A::Segment", "W2A::Tokenize");
		
		Object ret = conn.analyzeText("Ahoj světe! Hallo world! Nula se píše jako 0 .");
		
		conn.terminateServer();
		
		List<Map<String, Object>> treexRet = Utils.objectArrayToGenericList(ret);		
		Assert.assertEquals(treexRet.size(), 3);
		
		List<Object> nodes1 = Utils.objectArrayToGenericList(treexRet.get(0).get("nodes"));		
		Assert.assertEquals(nodes1.size(), 3);

		List<Object> nodes2 = Utils.objectArrayToGenericList(treexRet.get(1).get("nodes"));		
		Assert.assertEquals(nodes2.size(), 3);

		List<Object> nodes3 = Utils.objectArrayToGenericList(treexRet.get(2).get("nodes"));		
		Assert.assertEquals(nodes3.size(), 6);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> toc = (Map<String, Object>) nodes3.get(4); 
		
		Assert.assertEquals(toc.get("ord"), "5");
		Assert.assertEquals(toc.get("form"), "0");

		
	}

	@Test( groups="slow" )
	public void morceTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.setRedirectionType(RedirectionType.INHERIT_IO);
		tse.start();
				
		TreexServerConnectionXmlRpc conn = tse.getConnection();
		
		conn.initScenario("en", "W2A::EN::Segment", "W2A::EN::Tokenize", "W2A::EN::TagMorce");
		
		logger.debug("Before first sentence.");
		Object ret = conn.analyzeText("Hallo world!");

		logger.debug("Before second sentence.");
		ret = conn.analyzeText("Life is great, isn't it?");

		logger.debug("Second sentence finished!");

		Object ret3 = conn.analyzeText("Zero is written as 0 .");

		logger.debug("Third sentence finished!");
		
		conn.terminateServer();
		
		List<Map<String, Object>> treexRet = Utils.objectArrayToGenericList(ret);		
		Assert.assertEquals(treexRet.size(), 1);
		
		List<Object> nodes1 = Utils.objectArrayToGenericList(treexRet.get(0).get("nodes"));		
		Assert.assertEquals(nodes1.size(), 8);

		List<Map<String, Object>> treexRet3 = Utils.objectArrayToGenericList(ret3);		
		Assert.assertEquals(treexRet.size(), 1);
		
		List<Object> nodes3 = Utils.objectArrayToGenericList(treexRet3.get(0).get("nodes"));		
		Assert.assertEquals(nodes3.size(), 6);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> toc = (Map<String, Object>) nodes3.get(4); 
		
		Assert.assertEquals(toc.get("ord"), "5");
		Assert.assertEquals(toc.get("form"), "0");

		
	}

}
