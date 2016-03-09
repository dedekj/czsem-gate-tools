package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Corpus;
import gate.CreoleRegister;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageResource;
import gate.ProcessingResource;
import gate.Resource;
import gate.Utils;
import gate.corpora.DocumentImpl;
import gate.creole.ResourceData;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.event.CreoleListener;
import gate.persist.PersistenceException;
import gate.util.AnnotationDiffer;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.persistence.PersistenceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.utils.AbstractConfig.ConfigLoadException;


public class GateUtils
{
	private static final Logger logger = LoggerFactory.getLogger(GateUtils.class);

	/** Removing accents (and diacritics) */
	public static String removeDiacritics(String orig) {  
		//System.err.println(java.text.Normalizer.normalize("Ďědečéék",java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));
		return StringUtils.stripAccents(orig); 
	}

	@SuppressWarnings("unchecked")
	public static Integer[] decodeEdge(Annotation a)
	{
		Integer [] ret = new Integer[2];
		ArrayList<Integer> list = (ArrayList<Integer>) a.getFeatures().get("args");
		ret[0] = list.get(0);
		ret[1] = list.get(1);
		return ret;
	}
	
	public static FeatureMap createDependencyArgsFeatureMap(Integer parent_id, Integer child_id)
	{
		FeatureMap fm = Factory.newFeatureMap();
		ArrayList<Integer> args = new ArrayList<Integer>(2);

		args.add(parent_id);
		args.add(child_id);
		fm.put("args", args);
		
		return fm;
	}

	public static Document loadDocumentFormDatastore(DataStore ds, String docId) throws ResourceInstantiationException {
		return (Document) loadResourceFormDatastore(ds, "gate.corpora.DocumentImpl", docId);
	}

	public static Corpus loadCorpusFormDatastore(DataStore ds, String copusId) throws ResourceInstantiationException {
		return (Corpus) loadResourceFormDatastore(ds, "gate.corpora.SerialCorpusImpl", copusId);
	}

	public static DataStore openDataStore(String storage_url) throws PersistenceException
	{
		return Factory.openDataStore("gate.persist.SerialDataStore", storage_url); 
	}

	
	public static void printStoredIds(DataStore ds) throws PersistenceException
	{
		for (Object o : ds.getLrTypes())
		{
			System.err.println(o);			
			for (Object string : ds.getLrIds((String) o)) {
				System.err.print("     ");
				System.err.println(string);
			}
		}		
	}
	
	public static Resource loadResourceFormDatastore(DataStore ds, String calassName, String obj_id) throws ResourceInstantiationException
	{
		FeatureMap docFeatures = Factory.newFeatureMap();
		
		docFeatures.put(DataStore.LR_ID_FEATURE_NAME, obj_id);
		docFeatures.put(DataStore.DATASTORE_FEATURE_NAME, ds);		

		return Factory.createResource(calassName, docFeatures);
	}
	
	public static class CorpusDocumentCounter
	{
		protected Corpus copus;
		protected Set<String> seenDocuments;
		private int numDocs; 

		public CorpusDocumentCounter(Corpus corpus) {
			this.copus = corpus;
			setNumDocs(corpus.size());
			
			seenDocuments = new HashSet<String>(getNumDocs());
		}
		
		public boolean isLastDocument()
		{
//			System.err.format("%d %d\n", numDocs, seenDocuments.size());
			return getNumDocs() <= seenDocuments.size();			
		}

		/**
		 * @return false if the document is already present in the collection  
		 */
		public boolean addDocument(Document doc)
		{
			return addDocument(doc.getName());
		}

		/**
		 * @return false if the document is already present in the collection  
		 */
		public boolean addDocument(String name) {
			return seenDocuments.add(name);
		}
		
		public Set<String> getDocumentSet()		
		{
			return seenDocuments;
		}

		public void setNumDocs(int numDocs) {
			this.numDocs = numDocs;
		}

		public int getNumDocs() {
			return numDocs;
		}

		
	}
	
	public static abstract class CustomizeDiffer
	{
		public abstract String getKeyAs();
		public abstract String getReponseAS();
		public abstract String getAnnotationType();		
	
		public String annotType;
		public void setAnnotType(String annotType)
		{
			this.annotType = annotType;
		}
	}

	public static void safeDeepReInitPR_or_Controller(ProcessingResource processingResource) throws ResourceInstantiationException
	{
		if (processingResource instanceof Controller)
			deepReInitController((Controller) processingResource);
		else
			processingResource.reInit();
	}

	public static void deepReInitController(Controller contoler) throws ResourceInstantiationException
	{
		Collection<ProcessingResource> prs = contoler.getPRs();
		for (ProcessingResource processingResource : prs)
		{
			safeDeepReInitPR_or_Controller(processingResource);
			//processingResource.reInit();			
		}		
	}

	public static void deleteAllPublicGateResources()
	{
		CreoleRegister reg = Gate.getCreoleRegister();
		
		for (ProcessingResource i : reg.getPublicPrInstances())
		{
			Factory.deleteResource(i);			
		}
	
		for (LanguageResource l : reg.getPublicLrInstances())
		{
			Factory.deleteResource(l);			
		}
	
	}

	/** One level only, not full recursion! **/
	public static void deepDeleteController(Controller controller) {
		
		Collection<ProcessingResource> prs = controller.getPRs();
		
		List<ProcessingResource> toRemove = new ArrayList<>(prs);
		
		for (ProcessingResource processingResource : toRemove) {
			Factory.deleteResource(processingResource);
		}
		
		Factory.deleteResource(controller);
	}

	public static void registerCzsemPlugin() throws GateException, URISyntaxException, IOException
	{
		registerAllCzsemPrs();		
	}

	public static void registerPluginDirectory(File pluginDirectory) throws MalformedURLException, GateException
	{
	    Gate.getCreoleRegister().registerDirectories( 
	    		pluginDirectory.toURI().toURL());		
	}
	
	public static String getUserPluginsHome() {
		return Gate.getUserConfig().getString("gate.user.plugins");
	}
	
	public static void registerUserPluginDirectory(String pluginDirectoryName) throws MalformedURLException, GateException
	{
		registerPluginDirectory(
				new File(getUserPluginsHome(), pluginDirectoryName));
	}

	public static void registerOrdinalOrUserPluginDirectory(String pluginDirectoryName) throws MalformedURLException, GateException
	{
		try {
			registerPluginDirectory(pluginDirectoryName);
		} catch (GateException e) {
			registerUserPluginDirectory(pluginDirectoryName);
		}
	}
	
	public static void registerPluginParserStanford() throws MalformedURLException, GateException {
		try {
			GateUtils.registerPluginDirectory("Stanford_CoreNLP");
		} catch (MalformedURLException | GateException e) {
			GateUtils.registerPluginDirectory("Parser_Stanford");			
		}			
	}

	public static void registerPluginDirectory(String pluginDirectoryName) throws MalformedURLException, GateException
	{
		registerPluginDirectory(
    		    new File(Gate.getPluginsHome(), pluginDirectoryName));		
	}
	

	public static void saveGateDocumentToXML(Document doc, String filename) throws IOException {
		saveGateDocumentToXML(doc, new FileOutputStream(filename));
		logger.debug("saveGateDocumentToXML done: {}", filename);
	}
	
	public static void saveGateDocumentToXML(Document doc, OutputStream stream) throws IOException
	{
		Writer out = new OutputStreamWriter(new BufferedOutputStream(stream), "utf8");
		out.write(doc.toXml());
		out.close();
		
	}

	
	public static void saveBMCDocumentToDirectory(Document doc, String directory, String nameFeature) throws IOException
	{
		//if (doc.getAnnotations("TectoMT").size() <= 0) throw new RuntimeException("No TectoMT annotations present in document!");
		
		String filename = (String) doc.getFeatures().get(nameFeature);
		
		saveGateDocumentToXML(doc, directory+"/"+filename+".xml");		
	}

	public static void saveBMCCorpusToDirectory(Corpus corpus, String directory, String nameFeature) throws IOException
	{
		
		for (Object doc_o : corpus)
		{
			Document doc = (Document) doc_o;
			saveBMCDocumentToDirectory(doc, directory, nameFeature);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void deleteAndCelarCorpusDocuments(Corpus corpus)
	{		
		//delete documents				
		for (Iterator iter = corpus.iterator(); iter.hasNext(); )
		{
			Object doc = iter.next();
			iter.remove();
			Factory.deleteResource((Resource) doc);
		}		
	}


	
	public static AnnotationDiffer calculateSimpleDiffer(Document doc, CustomizeDiffer cd)
	{
		return calculateSimpleDiffer(doc, cd.getKeyAs(), cd.getReponseAS(), cd.getAnnotationType());		
	}

	public static AnnotationDiffer calculateSimpleDiffer(Document doc, String keyAS, String responseAS, String annotationType)
	{
		return calculateSimpleDiffer(
				doc.getAnnotations(keyAS).get(annotationType), 
				doc.getAnnotations(responseAS).get(annotationType)); // compare
	}
	
	public static AnnotationDiffer calculateSimpleDiffer(AnnotationSet keyAS, AnnotationSet responseAS)
	{
		AnnotationDiffer differ = new AnnotationDiffer();
		differ.setSignificantFeaturesSet(new HashSet<String>());
		differ.calculateDiff(keyAS,	responseAS); // compare
		return differ;
	}
	

	public static boolean testAnnotationsDisjoint(AnnotationSet annots)
	{
		List<Annotation> ordered = Utils.inDocumentOrder(annots);
		for (int i=0; i<ordered.size()-1; i++)
		{
			Annotation a = ordered.get(i);
			Annotation next = ordered.get(i+1);
			
			if (a.getEndNode().getOffset() > next.getStartNode().getOffset()) return false;			
		}
		return true;
	}

	public static void initGate() throws GateException, IOException, URISyntaxException {
		//initGate(Level.OFF);
		//TODO
		initGateKeepLog();
	}

	public static void setGateHome() throws ConfigLoadException
	{
		if (Gate.getGateHome() == null)
			Gate.setGateHome(new File(CzsemConfig.getConfig().getGateHome()));
	}

	public static ClassLoader getGateClassLoader()  {
		if (Gate.isInitialised()) {
			return Gate.getClassLoader();
		} else {
			return null;
		}
	}

	
	public static void initGateKeepLog() throws GateException, ConfigLoadException {
		if (Gate.isInitialised()) return;
		
		setGateHome();

		Gate.init();						
		
	}
	
	/*
	public static void loggerSetup(Level logLevel)
	{
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(logLevel);
	    logger.removeAllAppenders();
		BasicConfigurator.configure();		
	}

	public static void initGate(Level logLevel) throws GateException, IOException, URISyntaxException {
		if (Gate.isInitialised()) return;
		
		//TODO
		//loggerSetup(logLevel);
		
		initGateKeepLog();		
	}

	
	public static void initGateInSandBox() throws GateException 
	{ initGateInSandBox(Level.OFF); }

	public static void initGateInSandBox(Level logLevel) throws GateException {
		if (Gate.isInitialised()) return;
		
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(logLevel);
		BasicConfigurator.configure();

		Gate.runInSandbox(true);
		Gate.init();				
	}
	*/

	public static void initGateInSandBoxKeepLog() throws GateException {
		if (Gate.isInitialised()) return;
		Gate.runInSandbox(true);
		Gate.init();				
	}
	
	public static boolean isPrCalssRegisteredInCreole(String classname)
	{
		Set<String> types = Gate.getCreoleRegister().getPrTypes();
		return types.contains(classname);				
	}

	public static boolean isPrCalssRegisteredInCreole(Class<? extends Resource> clazz)
	{
		return isPrCalssRegisteredInCreole(clazz.getCanonicalName());
	}

	/**
	 * @deprecated: use {@link gate.Utils#stringFor(Document, gate.SimpleAnnotation)} instead
	 */
	@Deprecated
	public static String getAnnotationContent(Annotation annotation, Document doc) {
		return Utils.stringFor(doc, annotation);
	}

	public static void registerComponentIfNot(Class<? extends Resource> class1) throws GateException {
		if (! isPrCalssRegisteredInCreole(class1)) {
			Gate.getCreoleRegister().registerComponent(class1);
		}
	}

	public static Document createDoc(File file, String encoding, String mimeType) throws ResourceInstantiationException, MalformedURLException {
		URL url =file.toURI().toURL();
	    FeatureMap parameterValues = Factory.newFeatureMap();
	    parameterValues.put(Document.DOCUMENT_URL_PARAMETER_NAME, url);
	    parameterValues.put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, mimeType);
	    return (Document) Factory.createResource(DocumentImpl.class.getCanonicalName(), parameterValues);
	}
	
	
	/**
	 * Changes the span of an existing annotation by creating a new annotation
	 * with the same ID, type and features but with the new start and end
	 * offsets.
	 * 
	 * @param set
	 *            the annotation set
	 * @param oldAnnotation
	 *            the annotation to be moved
	 * @param newStartOffset
	 *            the new start offset
	 * @param newEndOffset
	 *            the new end offset
	 */
	public static void moveAnnotation(AnnotationSet set, Annotation oldAnnotation, Long newStartOffset, Long newEndOffset)	throws InvalidOffsetException {
		Integer oldID = oldAnnotation.getId();
		set.remove(oldAnnotation);
		set.add(oldID, newStartOffset, newEndOffset, oldAnnotation.getType(), oldAnnotation.getFeatures());
	}
	
	public static AnnotationSet getAnnotationsByRefString(String ref, Document doc) {
		if (ref == null || ref.isEmpty()) return null; 
			
		String[] split = ref.split("\\.");
		AnnotationSet ret = doc.getAnnotations(split[0]).get(split[1]);
		
		return ret;
	}

	public static long spaceBetweenAnnotations(Annotation firstAnn, Annotation secondAnn) {
		return secondAnn.getStartNode().getOffset() - firstAnn.getEndNode().getOffset();
	}

	/**
	 * @return true if the given instance was contained in the register, false otherwise (i.e. the instance had already been removed).
	 */
	public static boolean releseGateReference(Resource resource) {
	    if (resource instanceof CreoleListener) {
	    	CreoleListener l = (CreoleListener) resource;
			CreoleRegister r = Gate.getCreoleRegister();
	    	r.removeCreoleListener(l);
	    }

	    ResourceData rd = Gate.getCreoleRegister().get(resource.getClass().getName());
	    	    
	    if (rd != null) 
	    	return rd.removeInstantiation(resource);
	    
	    return false;
		
	}

	public static char removeDiacritics(char ch1) {
		String str = String.valueOf(ch1);
		String ret = removeDiacritics(str);
		return ret.charAt(0);
	}

	public static void registerAllPrsInPackage(String packageStr) throws GateException {
		Reflections reflections = new Reflections(packageStr);
		
		Set<Class<?>> types = reflections.getTypesAnnotatedWith(CreoleResource.class);

		for (Class<?> cls : types) {
			
			@SuppressWarnings("unchecked")
			Class<? extends Resource> typedCls = (Class<? extends Resource>) cls;
			
			registerComponentIfNot(typedCls);
		}
		
		logger.info("Registered "+types.size()+" GATE resorces from package " + packageStr);
	}

	public static void registerAllCzsemPrs() throws GateException {
		registerAllPrsInPackage("czsem.gate.plugins");
		registerComponentIfNot(NotCheckingParametersSerialController.class);
	}
	
	public static synchronized Object persistenceManagerLoadObjectFromFileSynchronized(File file) throws PersistenceException, ResourceInstantiationException, IOException {
		return PersistenceManager.loadObjectFromFile(file);
	}

}
