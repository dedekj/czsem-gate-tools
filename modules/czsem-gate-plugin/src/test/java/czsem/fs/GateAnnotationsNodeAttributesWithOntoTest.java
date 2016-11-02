package czsem.fs;

import gate.Factory;
import gate.Resource;
import gate.Utils;
import gate.creole.ontology.Ontology;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;

public class GateAnnotationsNodeAttributesWithOntoTest {
	@BeforeClass
	public void beforeClass() throws Exception {
		GateUtils.initGateKeepLog();
		GateUtils.registerPluginDirectory("Ontology");
	}

	@Test
	public void isSubClassOf() throws Exception {
		Resource onto = Factory.createResource("gate.creole.ontology.impl.sesame.OWLIMOntology",
				Utils.featureMap("turtleURL", getClass().getResource("/onto/test_onto.ttl")));
		
		GateAnnotationsNodeAttributesWithOnto attrs = new GateAnnotationsNodeAttributesWithOnto(null);
		attrs.setOntology((Ontology) onto);
		
		boolean isSubclass = attrs.isSubClassOf("RedWine", "Liquid");
		
		Factory.deleteResource(onto);
		
		Assert.assertTrue(isSubclass);
	}
}
