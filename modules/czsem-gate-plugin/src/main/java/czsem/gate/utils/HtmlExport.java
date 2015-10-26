package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.corpora.DocumentContentImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.dumpingPR.DumpingPR;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HtmlExport {
	private static final Logger logger = LoggerFactory.getLogger(HtmlExport.class);
	
	
	protected String annotationSetName;
	protected URL outputDirectoryUrl;
	protected String[] annotationTypes; 
	protected String[] elementStyles = null;
	
	protected boolean addHeaderWithStyles = true;
	protected boolean addSafeHtmlEnd = false;
	
	protected SerialAnalyserController pipeline;
	protected Corpus corpus;
	protected String headerPrefix ="\n\n\n"+"body	{white-space: pre-wrap;}\n";


	public HtmlExport(String annotationSetName, URL outputDirectoryUrl,	String[] annotationTypes) {
		this.annotationSetName = annotationSetName;
		this.outputDirectoryUrl = outputDirectoryUrl;
		this.annotationTypes = annotationTypes;
	}
	
	public HtmlExport(String annotationSetName, URL outputDirectoryUrl,	String[] annotationTypes, String[] colorNames) {
		this.annotationSetName = annotationSetName;
		this.outputDirectoryUrl = outputDirectoryUrl;
		this.annotationTypes = annotationTypes;
		setColorNames(colorNames);
	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGateKeepLog();
		Gate.getCreoleRegister().registerComponent(DumpingPR.class);
		
		//GateUtils.registerPluginDirectory("Tools");
		
		
		String fileName = "../intlib/documents/ucto.gate.xml";
		String outputDir = "target/export";
		String asName = "tmt2";
		String [] annotationTypes = {"Sentence"};
		String[] colorNames = {"red"};
		
		doExport(fileName, outputDir, asName, annotationTypes, colorNames);
		
	}
	
	public void useWhiteSpacePreWrap(boolean useWhiteSpacePreWrap) {
		headerPrefix ="\n\n\n";
		if (useWhiteSpacePreWrap) headerPrefix += "body	{white-space: pre-wrap;}\n";
	}
	
	public void setColorNames(String[] colorNames) {
		elementStyles = new String [annotationTypes.length];
		
		for (int c=0; c< colorNames.length; c++) {
			elementStyles[c] = String.format("{background: %s ; }\n", colorNames[c]);
		}
	}

	public void addSafeHtmlEnd(Document doc) throws InvalidOffsetException {
		AnnotationSet htmlAS = doc.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME).get("html");
		if (htmlAS.size() != 1) throw new IllegalStateException("Wrong html annot count!");

		Annotation htmlAnn = htmlAS.iterator().next();
		
		long htmlOff = htmlAnn.getEndNode().getOffset();

		long size = doc.getContent().size();		
		
		if (htmlOff != size) { 
			logger.warn("Html doesn't end at doc end!");
			
			AnnotationSet exportAs = doc.getAnnotations(annotationSetName);
			AnnotationSet exportAnns = exportAs.get(htmlOff, htmlOff+1).get(czsem.Utils.setFromArray(annotationTypes));
			
			for (Annotation a: exportAnns) {
				GateUtils.moveAnnotation(exportAs, a, a.getStartNode().getOffset(), htmlOff);
			}						
		}
		
		// add single space at the end of document
		doc.edit(size, size, new DocumentContentImpl(" "));
		
		//move all annotation ends
		List<AnnotationSet> ass = new ArrayList<AnnotationSet>(doc.getNamedAnnotationSets().size()+1);
		ass.addAll(doc.getNamedAnnotationSets().values());
		ass.add(doc.getAnnotations());
		
		for (AnnotationSet as : ass) {
			for (Annotation ann : as.get(size, size+1)) {
				GateUtils.moveAnnotation(as, ann, ann.getStartNode().getOffset(), size);
			}
		}
		
		//move html tag
		GateUtils.moveAnnotation(
				doc.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME),
				htmlAnn, 0L, size+1);				
	}

	public void addExportHeader(Document doc) throws InvalidOffsetException {
		
		StringBuilder sb = new StringBuilder(headerPrefix);
		if (elementStyles != null) {
			for (int i = 0; i < annotationTypes.length; i++) {
				sb.append(annotationTypes[i]);
				sb.append(elementStyles[i]);
			}
			sb.append("\n");
		}
		
		String docPrefix = sb.toString();
		DocumentContent replacement = new DocumentContentImpl(docPrefix);
		doc.edit(0L, 0L, replacement);
		
		AnnotationSet markupAs = doc.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
		
		FeatureMap f = Factory.newFeatureMap();
		markupAs.add(0L, doc.getContent().size(), "html", f);
		markupAs.add(1L, (long) docPrefix.length(), "head", f);
		markupAs.add(2L, docPrefix.length()-1L, "style", f);
		markupAs.add((long) docPrefix.length(), doc.getContent().size(), "body", f);
		
	}

	public void init() throws ResourceInstantiationException {
		PRSetup[] setup = new PRSetup [] {
				new PRSetup.SinglePRSetup(DumpingPR.class)
				.putFeature("includeFeatures", true)
				.putFeature("useStandOffXML", false)
				.putFeature("useSuffixForDumpFiles", true)
				.putFeature("suffixForDumpFiles", ".html")
				.putFeature("outputDirectoryUrl", outputDirectoryUrl)
				.putFeature("annotationSetName", annotationSetName)
				.putFeatureList("annotationTypes", annotationTypes)
				.putFeatureList("dumpTypes")
				
				
		};
		
		pipeline = PRSetup.buildGatePipeline(setup, "HTML export pipeline");
		
		corpus = Factory.newCorpus("HTML export corpus");
		pipeline.setCorpus(corpus);
		
	}
	
	public void close() {
		Factory.deleteResource(corpus);
		GateUtils.deepDeleteController(pipeline);		
	}

	
	public void doExport(Document doc) throws InvalidOffsetException, ExecutionException {
		if (addHeaderWithStyles)
			addExportHeader(doc);
		
		if (addSafeHtmlEnd)
			addSafeHtmlEnd(doc);
				
		corpus.clear();
		corpus.add(doc);		
		pipeline.execute();		
	}

	public void doExport(String fileName) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException, ExecutionException {
		
		
		System.err.println("reading doc: " + fileName);
		Document doc = Factory.newDocument(new File(fileName).toURI().toURL(), "utf8");
		System.err.println("reading finished");
		
		doExport(doc);
		Factory.deleteResource(doc);
	}

	public static void doExport(String fileName, String outputDir, String asName, String[] annotationTypes, String[] colorNames) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException, ExecutionException {
		new File(outputDir).mkdirs();

		HtmlExport htmlExport = new HtmlExport(asName, new File(outputDir).toURI().toURL(), annotationTypes, colorNames);
		htmlExport.init();
		htmlExport.doExport(fileName);
	}

	public String[] getElementStyles() {
		return elementStyles;
	}

	public void setElementStyles(String[] elementStyles) {
		this.elementStyles = elementStyles;
	}

	public boolean getAddHeaderWithStyles() {
		return addHeaderWithStyles;
	}

	public void setAddHeaderWithStyles(boolean addHeaderWithStyles) {
		this.addHeaderWithStyles = addHeaderWithStyles;
	}

	public boolean getAddSafeHtmlEnd() {
		return addSafeHtmlEnd;
	}

	public void setAddSafeHtmlEnd(boolean addSafeHtmlEnd) {
		this.addSafeHtmlEnd = addSafeHtmlEnd;
	}
}

