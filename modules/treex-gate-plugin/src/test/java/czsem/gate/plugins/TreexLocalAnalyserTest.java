package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Utils;
import gate.creole.SerialAnalyserController;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.gate.treex.TreexServerExecution.RedirectionType;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;
import czsem.gate.utils.PRSetup.SinglePRSetup;

public class TreexLocalAnalyserTest {
	
	@BeforeClass
	public static void initGate() throws Exception {
		GateUtils.initGateKeepLog();
		GateUtils.registerComponentIfNot(TreexLocalAnalyser.class);
	}
	
	@Test(groups = { "slow" })
	public static void czechNETest() throws Exception {
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("serverPortNumber", 9994)
	    			.putFeature("languageCode", "cs")
	    			.putFeature("logRedirectionType", RedirectionType.INHERIT_IO)
	    			.putFeatureList("scenarioSetup", "W2A::CS::Segment", "devel/analysis/cs/s_w2n_dedek.scen")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "czechNETest");
		Corpus corpus = Factory.newCorpus("czechNETest");
		
		//MainFrame.getInstance().setVisible(true);
		
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012 v Hradci Králové, Česká republika (ČR), Česko");
		corpus.add(doc);
		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		//GateUtils.saveGateDocumentToXML(doc, "NETest.gate.xml");
		
		//while (MainFrame.getInstance().isVisible()) { Thread.sleep(10); }
		
		analysis.cleanup();
		
		Assert.assertTrue(doc.getAnnotations().get("n-node").size() > 0, "there are no n-nodes!");
		
		GateUtils.deleteAllPublicGateResources();
	}

	
	@Test(groups = { "slow" })
	public static void czechFeaturamaCrash() throws Exception {
    	
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
    			.putFeature("logRedirectionType", RedirectionType.INHERIT_IO)
	    		.putFeature("serverPortNumber", 1111)
	    		/*
	    		.putFeature("languageCode", "en")	
	    		.putFeatureList("scenarioSetup",
	    					"W2A::EN::Segment",
	    					"W2A::EN::Tokenize",
				            "W2A::EN::TagFeaturama",
				            "W2A::EN::Lemmatize")
				            */
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("englishSimpleTest");
		/* Featurama is crashing on tokens longer than 40 characters! */
		Document doc = Factory.newDocument("\n12345678901234567890123456789012345678901\n────────────────────────────────────────\n");
		corpus.add(doc);
		analysis.setCorpus(corpus);
		analysis.execute();
		analysis.cleanup();
	}

	@Test(groups = { "slow" })
	public static void englishFeaturamaTest() throws Exception {
    	
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    		.putFeature("serverPortNumber", 9999)	
	    		.putFeature("languageCode", "en")	
	    		.putFeatureList("scenarioSetup",
	    					"W2A::EN::Segment",
	    					"W2A::EN::Tokenize",
				            "W2A::EN::TagFeaturama",
				            "W2A::EN::Lemmatize")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("englishSimpleTest");
		Document doc = Factory.newDocument("Hallo world! Life is great, isn't it?");
		corpus.add(doc);
		Document doc2 = Factory.newDocument("This is the second document in the corpus.");
		corpus.add(doc2);
		analysis.setCorpus(corpus);
		analysis.execute();
		analysis.cleanup();
		
		Assert.assertEquals(doc.getAnnotations().size(), 13);
		Assert.assertEquals(doc2.getAnnotations().size(), 10);

		FeatureMap f = doc2.getAnnotations().get(2).getFeatures();
		Assert.assertEquals(f.get("form"), "is");
		Assert.assertEquals(f.get("lemma"), "be");
		Assert.assertEquals(f.get("tag"), "VBZ");
		
		GateUtils.deleteAllPublicGateResources();
	}

	@Test
	public void unicodeSOHTest() throws Exception {
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    		.putFeature("serverPortNumber", 7999)	
	    		.putFeature("languageCode", "en")	
	    		.putFeatureList("scenarioSetup", 
	    					"W2A::EN::Segment",
	    					"W2A::EN::Tokenize")//,
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "unicodeSOHTest");
		Corpus corpus = Factory.newCorpus("unicodeSOHTest");
		Document doc = Factory.newDocument("Hallo world! Li\u0001fe is great, isn't it?");
		corpus.add(doc);
		analysis.setCorpus(corpus);
		analysis.execute();
		analysis.cleanup();
		
		AnnotationSet a = doc.getAnnotations().get("Token").get(13l);
		
		Assert.assertEquals((long) gate.Utils.start(a), 13l);
		Assert.assertEquals((long) gate.Utils.end(a), 15l);
		
		Assert.assertEquals(doc.getAnnotations().get("Sentence").size(), 2);
		
	}

	
	@Test
	public static void englishSimpleTest() throws Exception {
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    		.putFeature("serverPortNumber", 9993)	
	    		.putFeature("languageCode", "en")	
	    		.putFeatureList("scenarioSetup", 
	    					"W2A::EN::Segment",
	    					"W2A::EN::Tokenize")//,
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("englishSimpleTest");
		Document doc = Factory.newDocument("Hallo world! Life is great, isn't it?");
		corpus.add(doc);
		Document doc2 = Factory.newDocument("This is the second document in the corpus.");
		corpus.add(doc2);
		analysis.setCorpus(corpus);
		analysis.execute();
		analysis.cleanup();
		
		Assert.assertEquals(doc.getAnnotations().size(), 21);
		Assert.assertEquals(doc.getAnnotations().get("Token").size(), 11);
		Assert.assertEquals(doc.getAnnotations().get("SpaceToken").size(), 6);
		Assert.assertEquals(doc.getAnnotations().get("Sentence").size(), 2);
		Assert.assertEquals(doc.getAnnotations().get("Split").size(), 2);
		
		Assert.assertEquals(doc2.getAnnotations().size(), 18);
		
		GateUtils.deleteAllPublicGateResources();
	}
	

	@Test(groups = { "slow" })
	public static void czechFullTest() throws Exception {
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("serverPortNumber", 9997)	
	    			.putFeature("languageCode", "cs")
	    			.putFeature("logRedirectionType", RedirectionType.INHERIT_IO)
	    			.putFeatureList("scenarioSetup", "W2A::CS::Segment", "devel/analysis/cs/s_w2t_dedek.scen")
//	    					"dedek.scen",
//	    					"devel\\analysis\\cs\\s_w2t_dedek.scen")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "czechFullTest");
		Corpus corpus = Factory.newCorpus("czechFullTest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012");
		corpus.add(doc);
		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		analysis.cleanup();
		GateUtils.deleteAllPublicGateResources();
	}

	@Test(groups = { "slow" })
	public static void inintFromPluginDirTest() throws Exception {
		GateUtils.initGate();
		GateUtils.registerAllCzsemPrs();
		
		ProcessingResource pr = new SinglePRSetup(TreexLocalAnalyser.class)
			.putFeature("serverPortNumber", 9991)	
	    	.putFeature("logRedirectionType", RedirectionType.INHERIT_IO)
			.putFeature("verifyOnInit", true).createPR();
		
		Factory.deleteResource(pr);
	}

	@Test
	public static void czechSimpleTest() throws Exception {
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("serverPortNumber", 9998)	
	    			.putFeature("languageCode", "cs")
	    			.putFeatureList("scenarioSetup",
	    					"W2A::CS::Segment",
	    					"W2A::CS::Tokenize")//,
//	    					"W2A::CS::TagMorce",
//	    					"W2A::CS::FixMorphoErrors")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(prs, "czechSimpleTest");
		Corpus corpus = Factory.newCorpus("czechSimpleTest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012");
		corpus.add(doc);
		
		String strDoc2 = 
		
		"\n   11d)  Zákon  č.  218/2000  Sb.,  o  rozpočtových  pravidlech  a o změně"
		+"\n   některých   souvisejících   zákonů   (rozpočtová  pravidla),  ve  znění"
		+"\n   pozdějších předpisů."
		+"\n"
		+"\n   Zákon  č. 250/2000 Sb., o rozpočtových pravidlech územních rozpočtů, ve"
		+"\n   znění pozdějších předpisů."
		+"\n"
		+"\n   11e) Nařízení (ES) č. 1606/2002 Evropského parlamentu a Rady ze dne 19."
		+"\n   července 2002, o používání Mezinárodních účetních standardů."
		+"\n";
		
		Document doc2 = Factory.newDocument(strDoc2);
		corpus.add(doc2);
		
		String strDoc3 = 
		 "\n"
		+"\n   Změna: 126/2008 Sb."
		+"\n"
		+"\n   Změna: 304/2008 Sb. (část)"
		+"\n"
		+"\n   Změna: 230/2009 Sb."
		+"\n"
		+"\n   Změna: 304/2008 Sb."
		+"\n"
		+"\n   Změna: 227/2009 Sb."
		+"\n";
		
		Document doc3 = Factory.newDocument(strDoc3);
		corpus.add(doc3);

		String str4 = 
				"\n"+"1 obalená tableta obsahuje 10,0 mg butylscopolaminii bromidum." +
				"\n"+"Pomocné látky: 1 obalená tableta obsahuje 41,2 mg sacharosy." +
				"\n"+"Úplný seznam pomocných látek viz bod 6.1.";
		
		Document doc4 = Factory.newDocument(str4);
		corpus.add(doc4);
		
		String str5 = 
				"\n"+"Zhoršená funkce ledvin a jater" +
				"\n"+"Doporučené počáteční a udržovací dávky pro pacienty se zhoršenou renální funkcí:" +
				"\n"+"Clearance kreatininu                Sérová hladina                              Intravenózní dávka" +
				"\n"+"(ml/min/1,73 m2)                           kreatininu                                        (mg)" +
				"\n"+"                                                          (µmol/l)" +
				"\n"+"> 60                                                < 124                                            viz obvyklé dávkování" +
				"\n"+"                                                        " +
				"\n"+"30 – 60                                           124 až 168                           200 – 400 mg každých 12 hod." +
				"\n"+"< 30                                                 > 169                                  200 – 400 mg každých 24 hod.";

		Document doc5 = Factory.newDocument(str5);
		corpus.add(doc5);
		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		analysis.cleanup();

		List<Annotation> ords = gate.Utils.inDocumentOrder(doc3.getAnnotations().get("Sentence"));
		int index = 0;
		
		long off [][] = {
				{  5,  24},
				{ 29,  55},
				{ 60,  79},
				{ 84, 103},
				{108, 127},
		};
		
		for (Annotation s : ords) {
			System.out.format("%3d %30s %3d\n", s.getStartNode().getOffset(), Utils.stringFor(doc3, s), s.getEndNode().getOffset());
			Assert.assertEquals((long) s.getStartNode().getOffset(), off[index][0]);
			Assert.assertEquals((long) s.getEndNode().getOffset(), off[index][1]);
			index++;
		}
		
		Assert.assertEquals(doc.getAnnotations().get("Sentence").size(), 2);
		Assert.assertTrue(doc2.getAnnotations().get("Sentence").size() > 1, 
				String.format("Sentences should be more than 1 but was: %d", doc2.getAnnotations().get("Sentence").size()));
		
		AnnotationSet sents4 = doc4.getAnnotations().get().get("Sentence");
		Assert.assertEquals(sents4.size(), 3);

		Assert.assertEquals(doc5.getAnnotations().get().get("Sentence").size(), 1);
		 
		
		
		GateUtils.deleteAllPublicGateResources();
	}

}
