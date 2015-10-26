package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;
import gate.creole.splitter.SentenceSplitter;
import gate.creole.tokeniser.DefaultTokeniser;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;

public class MarkFirstAndLastSentenceTokensTest {

	@Test
	public static void execute() throws Exception {
		GateUtils.initGateKeepLog();
		System.err.println(Gate.getPluginsHome());
		System.err.println(Gate.getGateHome());
		GateUtils.registerPluginDirectory("ANNIE");
		Gate.getCreoleRegister().registerComponent(MarkFirstAndLastSentenceTokens.class);
		
		PRSetup [] setup = {
			new PRSetup.SinglePRSetup(DefaultTokeniser.class),	
			new PRSetup.SinglePRSetup(SentenceSplitter.class),	
			new PRSetup.SinglePRSetup(MarkFirstAndLastSentenceTokens.class),	
		};
		
		SerialAnalyserController p = PRSetup.buildGatePipeline(setup, "SentenceTokensTest");
		Corpus c = Factory.newCorpus("SentenceTokensTest");
		Document d = Factory.newDocument("This is the first sentence.\n\n"+"onlyWord");
		c.add(d);
		p.setCorpus(c);
		p.execute();
		
		AnnotationSet relevantTocs = d.getAnnotations().get("Token", Utils.setFromArray(new String[] {"sentencePos"}));
		
		Iterator<Annotation> ordered = gate.Utils.inDocumentOrder(relevantTocs).iterator();
		
		Assert.assertEquals(		
				ordered.next().getFeatures().get("sentencePos"),
				"first");

		Assert.assertEquals(		
				ordered.next().getFeatures().get("sentencePos"),
				"last");

		Assert.assertEquals(		
				ordered.next().getFeatures().get("sentencePos"),
				"both");
	}
}
