package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.Resource;
import gate.Utils;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;

import czsem.gate.treex.TreexAnalyserXmlRpc;
import czsem.gate.treex.TreexException;
import czsem.gate.treex.TreexServerConnectionXmlRpc;
import czsem.gate.treex.TreexServerExecution;
import czsem.gate.treex.TreexServerExecution.RedirectionType;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;

@CreoleResource(name = "czsem TreexLocalAnalyser", comment = "Analyzes givem corpus by Treex localy ( see http://ufal.mff.cuni.cz/treex/ )", 
	helpURL="https://czsem-suite.atlassian.net/wiki/display/DOC/Treex+GATE+Plugin")
public class TreexLocalAnalyser extends TreexAnalyserXmlRpc {

	private static final long serialVersionUID = -3111101835623696930L;
		
	private int serverPortNumber;
	private RedirectionType logRedirectionType = RedirectionType.LOG_FILES_REPLACE;

	private TreexServerExecution treexExec;

	
	
	@Override
	public void cleanup() {
		if (getServerConnection() == null) return;
		getServerConnection().terminateServer();
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		setScenarioString(computeScenarioString());
		
		try {
			initConnection();
		} catch (TreexException e) {
			throw formatInitException(e);
		}
		
		return super.init();
	}

	public TreexServerConnectionXmlRpc initConnection() throws TreexException {
		//debugClassloader();
		
		treexExec = new TreexServerExecution();
		treexExec.setRedirectionType(getLogRedirectionType());
		treexExec.setPortNumber(getServerPortNumber());
		
		setServerConnection(treexExec.getConnection());
		
		try {
			treexExec.start();
			initScenario();
		} catch(TreexException e) {
			getServerConnection().terminateServer();
			setServerConnection(null);
			throw e;
		}

		return getServerConnection();
	}


	public void debugClassload(String name) {
		URL url = getClass().getClassLoader().getResource(name.replace('.', '/') + ".class");
		System.err.println(url);

		url = Gate.getClassLoader().getResource(name.replace('.', '/') + ".class");
		System.err.println(url);

		url = Gate.getClassLoader().getParent().getResource(name.replace('.', '/') + ".class");
		System.err.println(url);
		
		System.err.println("---");
		try {
			getClass().getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void debugClassloader() {
		debugClassload("org.apache.log4j.Logger");
		debugClassload("org.xml.sax.SAXException");
		debugClassload("org.xml.sax.ContentHandler");
		debugClassload("org.xml.sax.Attributes");
		debugClassload("org.xml.sax.helpers.AttributesImpl");
		debugClassload("org.xml.sax.InputSource");
		debugClassload("javax.xml.parsers.ParserConfigurationException");
		debugClassload("javax.xml.parsers.SAXParserFactory");
		debugClassload("javax.xml.parsers.SAXParser");
		debugClassload("org.xml.sax.SAXParseException");
		debugClassload("org.xml.sax.XMLReader");
	}

	@CreoleParameter(defaultValue="9090")
	public void setServerPortNumber(Integer serverPortNumber) {
		this.serverPortNumber = serverPortNumber;
	}

	public Integer getServerPortNumber() {
		return serverPortNumber;
	}

	@Override
	protected String getHandshakeCode() {
		return treexExec.getHandshakeCode();
	}
	
	public static void main (String [] args) throws Exception {
		System.err.println("--- Treex TreexLocalAnalyser Test ---");
		GateUtils.initGateKeepLog();
		GateUtils.registerComponentIfNot(TreexLocalAnalyser.class);
		
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(		
			new PRSetup [] {		
					new PRSetup.SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("logRedirectionType", RedirectionType.INHERIT_IO)
			},
			"TreexLocalAnalyser Test");
		
		Corpus corpus = Factory.newCorpus("czechNETest");
		
		//MainFrame.getInstance().setVisible(true);
		
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012 v Hradci Králové, Česká republika (ČR), Česko");
		corpus.add(doc);
		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		AnnotationSet sents = doc.getAnnotations().get("Sentence");
		for (Annotation s: sents) {
			System.err.println(Utils.cleanStringFor(doc, s));
		}
		
		analysis.cleanup();

	}

	public RedirectionType getLogRedirectionType() {
		return logRedirectionType;
	}

	@CreoleParameter(defaultValue="LOG_FILES_REPLACE")
	public void setLogRedirectionType(RedirectionType logRedirectionType) {
		this.logRedirectionType = logRedirectionType;
	}
	
	
}
