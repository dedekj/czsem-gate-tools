package czsem.fs;

import gate.Factory;
import gate.Resource;
import gate.Utils;
import gate.creole.ontology.Ontology;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;

public class GateAnnotationsNodeAttributesWithOntoTest {
	private Resource onto;

	@BeforeClass
	public void beforeClass() throws Exception {
		GateUtils.initGateKeepLog();
		GateUtils.registerPluginDirectory("Ontology");

		onto = Factory.createResource("gate.creole.ontology.impl.sesame.OWLIMOntology",
				Utils.featureMap("turtleURL", getClass().getResource("/onto/test_onto.ttl")));
	}

	@AfterClass
	public void afterClass() throws Exception {
		Factory.deleteResource(onto);
	}

	@Test
	public void isSubClassOf() throws Exception {
		
		GateAnnotationsNodeAttributesWithOnto attrs = new GateAnnotationsNodeAttributesWithOnto(null);
		attrs.setOntology((Ontology) onto);
		
		boolean isSubclass = attrs.isSubClassOf("RedWine", "Liquid");
				
		Assert.assertTrue(isSubclass);
	}
}
