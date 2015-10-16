package czsem.gate.plugins;

import java.net.URL;

import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gate.util.ExtensionFileFilter;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;

public class ControlledCrossValidationTest {

	@Test
	public static void testCreate() throws Exception {
		//System.err.println(ControlledCrossValidationTest.class.getResource("/ControlledCrossValidationTest/fillFoldByNumber/"));
		
		GateUtils.initGateKeepLog();
		Gate.getCreoleRegister().registerComponent(ControlledCrossValidation.class);
		
		Corpus corpus = Factory.newCorpus("cross test");
		URL foldsUrl = ControlledCrossValidationTest.class.getResource("/ControlledCrossValidationTest/fillFoldByNumber/");
		ExtensionFileFilter filter = new ExtensionFileFilter();
		filter.addExtension("txt");
		corpus.populate(foldsUrl, filter, "utf8", true);		
		Assert.assertEquals(corpus.size(), 3);
		
		
		ControlledCrossValidation pr = (ControlledCrossValidation) 
				new PRSetup.SinglePRSetup(ControlledCrossValidation.class)
					.putFeature("corpus", corpus)
					.putFeature("numberOfFolds", 3)
					.putFeature("foldDefinitionDirectoryUrl", foldsUrl)
					.createPR();
		
		Assert.assertEquals(pr.corpusFolds[0][0].size(), 1);
		Assert.assertEquals(pr.corpusFolds[0][1].size(), 2);
		Assert.assertEquals(pr.corpusFolds[2][0].size(), 1);
		Assert.assertEquals(pr.corpusFolds[2][1].size(), 2);
	}
}
