package czsem.gate.plugins;

import gate.AnnotationSet;
import gate.Document;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.AnnotationDiffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import czsem.gate.DocumentFeaturesDiff;
import czsem.utils.MultiSet;

/**
 * Mostly copied form {@link QualityAssurancePR}, slightly modified. 
 * @author dedek
 *
 */
@CreoleResource(name = "czsem LearningEvaluator", comment = "Measures performance between two AS, similar to QualityAssurancePR")
public class LearningEvaluator extends AbstractLanguageAnalyser
{		
	 /** A temporary (not persistent) repository that stores results of all LearningEvaluators that were executed so far.*/	 
	public static class CentralResultsRepository
	{
		public static CentralResultsRepository repository = new CentralResultsRepository();
		
		private Map<LearningEvaluator, List<DocumentDiff>> repository_map = new HashMap<LearningEvaluator, List<DocumentDiff>>();
		
		public void clear()
		{
			repository_map.clear();
		}

		public Collection<LearningEvaluator> getContent()
		{
			return repository_map.keySet();
		}

		public int getNumberOfTokens(LearningEvaluator e, DiffCondition diffCondition)
		{
			int ret = 0;
			for (DocumentDiff diff : repository_map.get(e))
			{
				if (diffCondition.evaluate(diff))
					ret += diff.numOfTokens;			
			}
		
			return ret;			
		}
		
		public AnnotationDiffer getOveralResults(LearningEvaluator e, DiffCondition diffCondition)
		{
			return e.countOverallDiffer(repository_map.get(e), diffCondition);
		}
		
		public void addDocumentDiff(LearningEvaluator eval, DocumentDiff diff)
		{
			List<DocumentDiff> prev = repository_map.get(eval);
			if (prev == null) prev = new ArrayList<LearningEvaluator.DocumentDiff>();
			prev.add(diff);
			repository_map.put(eval, prev);
		}

		public void logAll()
		{
			Logger.getLogger(getClass()).info("---complete repository statistics---");
			for (LearningEvaluator eval : repository_map.keySet())
			{
				eval.logStatistics(repository_map.get(eval));				
			}
		}

		public static class PrNameAndFold
		{
			public PrNameAndFold(String prName, int fold) {
				this.prName = prName;
				this.fold = fold;
			}
			String prName;
			int fold;
			
			@Override
			public int hashCode() {			
				return new HashCodeBuilder().append(prName).append(fold).toHashCode();
			}
			@Override
			public boolean equals(Object obj) {
				   if (obj == null) { return false; }
				   if (obj == this) { return true; }
				   if (obj.getClass() != getClass()) {
				     return false;
				   }
				   PrNameAndFold rhs = (PrNameAndFold) obj;
				   return new EqualsBuilder()
				                 .append(prName, rhs.prName)
				                 .append(fold, rhs.fold)
				                 .isEquals();				 			
			}
		}
		
		public static class NumDocsAndTrainInst
		{
			public NumDocsAndTrainInst(int numDocs,
					MultiSet<String> instanceClassTypes) {
				this.numDocs = numDocs;
				this.instanceClassTypes = instanceClassTypes;
			}
			public int numDocs;
			public MultiSet<String> instanceClassTypes;
		}
		
		private Map<PrNameAndFold, NumDocsAndTrainInst> train_inst_stats = 
			new HashMap<LearningEvaluator.CentralResultsRepository.PrNameAndFold, LearningEvaluator.CentralResultsRepository.NumDocsAndTrainInst>();

		
		public void addNumberDocsAndTrainingInstances(String prName, int numDocs,
				int actual_fold_number, MultiSet<String> instanceClassTypes)
		{
			train_inst_stats.put(
					new PrNameAndFold(prName, actual_fold_number),
					new NumDocsAndTrainInst(numDocs, instanceClassTypes));			
		}
		

		public int getNumTrainInst(String trainPRName, List<String> annotation_types, int fold_number)
		{
			NumDocsAndTrainInst entry = train_inst_stats.get(new PrNameAndFold(trainPRName, fold_number));
			
			if (entry == null) return -1;

			int ret = 0;
			MultiSet<String> insts = entry.instanceClassTypes;
			for (String ann_type : annotation_types)
			{
				ret += insts.get(ann_type);				
			}
			return ret;
		}

		public int getNumDocs(String trainPRName, int fold_number) {
			NumDocsAndTrainInst entry = train_inst_stats.get(new PrNameAndFold(trainPRName, fold_number));
			return entry == null ? -1 : entry.numDocs;
		}

		public List<DocumentDiff> getDocumentDiffs(LearningEvaluator evaluator) {
			return repository_map.get(evaluator);			
		}		
	}
	
	public static class DocumentDiff
	{
		public DocumentDiff(String documentName, int runNumber, int foldNumber) {
			this.documentName = documentName;
			this.runNumber = runNumber;
			this.foldNumber = foldNumber;
		}
		public int runNumber = 0;
		public int foldNumber = 0;
		public String documentName;
		public AnnotationDiffer []	diff;
		
		public int numOfTokens = 0;
	}
	
	private static final long serialVersionUID = -3577722098895242238L;

	private String keyASName;
	private String responseASName;
	private List<String> annotationTypes;
	private List<String> featureNames;

	private AnnotationSet keyAS;
	private AnnotationSet responseAS;
	
	private List<DocumentDiff> documentDifs;
	
	private boolean keyAnnotationsAreInDocumentFeatures;
	
	public final int actualRunNumber = 0;
	public int actualFoldNumber = 1;


	protected AnnotationDiffer calculateDocumentDiff(Document document, String annotTypeName)
	{				
		AnnotationSet responsesIter = responseAS.get(annotTypeName);
		
		if (getKeyAnnotationsAreInDocumentFeatures())
		{
			return DocumentFeaturesDiff.computeDiffWithDocFeatures(document, featureNames, responsesIter);
		}

		AnnotationSet keysIter = keyAS.get(annotTypeName);
		
		AnnotationDiffer differ = new AnnotationDiffer();
		differ.setSignificantFeaturesSet(new HashSet<String>(featureNames));
		differ.calculateDiff(keysIter, responsesIter); // compare
		
		return differ;		
	}

	protected AnnotationDiffer [] calculateDocumentDiff(Document document)
	{
		
		AnnotationDiffer [] ret = new AnnotationDiffer[annotationTypes.size()];
		
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = calculateDocumentDiff(document, annotationTypes.get(i));
			
		}
		
		return ret;
	}


	@Override
	public void execute() throws ExecutionException
	{
		keyAS = document.getAnnotations(keyASName);
		responseAS = document.getAnnotations(responseASName);
		
		DocumentDiff diff = new DocumentDiff(document.getName(), actualRunNumber, actualFoldNumber);
		diff.diff = calculateDocumentDiff(document);
		Object tocNum = document.getFeatures().get("numOfTokens");
		if (tocNum != null) diff.numOfTokens = (Integer) tocNum; 
		
		documentDifs.add(diff);
		CentralResultsRepository.repository.addDocumentDiff(this, diff);
		
		if (documentDifs.size() == corpus.size())
		{
			logStatistics(documentDifs);
		}
				
	}

	public static String getStatisticsStr(AnnotationDiffer diff)
	{
		return String.format("match:%3d  miss:%3d  spur:%3d  overlap:%3d  prec: %f  rec: %f  f1: %f  lenientf1: %f",
				diff.getCorrectMatches(),
				diff.getMissing(),
				diff.getSpurious(),
				diff.getPartiallyCorrectMatches(),
				diff.getPrecisionStrict(),
				diff.getRecallStrict(),
				diff.getFMeasureStrict(1),
				diff.getFMeasureLenient(1)
			);
	}
	
	
	public static interface DiffCondition
	{
		boolean evaluate(DocumentDiff diff);		
	}
	
	public static class AllDiffsCondition implements DiffCondition
	{
		@Override
		public boolean evaluate(DocumentDiff diff) {return true;}	
	}
	
	public AnnotationDiffer countOverallDiffer(List<DocumentDiff> docDifs, DiffCondition diffCondition)
	{
		ArrayList<AnnotationDiffer> overall = new ArrayList<AnnotationDiffer>
			(annotationTypes.size() * docDifs.size());
	
		for (DocumentDiff diff : docDifs)
		{
			if (diffCondition.evaluate(diff))
				overall.addAll(Arrays.asList(diff.diff));			
		}
				
		return new AnnotationDiffer(overall);		
	}
	
	protected void logStatistics(List<DocumentDiff> docDifs)
	{								
		AnnotationDiffer overall_differ = countOverallDiffer(docDifs, new AllDiffsCondition());
		
		Logger logger = Logger.getLogger(getClass());
		
//ILP_config_NE_roots_subtree
		logger.info(String.format("%28s overall: %s", responseASName, getStatisticsStr(overall_differ)));
	}

	@Override
	public Resource init() throws ResourceInstantiationException
	{
		documentDifs = new ArrayList<DocumentDiff>();
		Locale.setDefault(Locale.ENGLISH);
		return super.init();
	}

	public String getKeyASName() {
		return keyASName;
	}

	@RunTime
	@CreoleParameter(defaultValue="Key")	
	public void setKeyASName(String keyASName) {
		this.keyASName = keyASName;
	}

	public String getResponseASName() {
		return responseASName;
	}

	@RunTime
	@CreoleParameter(defaultValue="")
	public void setResponseASName(String responseASName) {
		this.responseASName = responseASName;
	}

	public List<String> getAnnotationTypes() {
		return annotationTypes;
	}

	@RunTime
	@CreoleParameter(defaultValue="Mention")
	public void setAnnotationTypes(List<String> annotationTypes) {
		this.annotationTypes = annotationTypes;
	}

	public List<String> getFeatureNames() {
		return featureNames;
	}

	/**
	 * @see {@link AnnotationDiffer#setSignificantFeaturesSet(java.util.Set)}
	 */
	@RunTime
	@CreoleParameter(defaultValue="")
	public void setFeatureNames(List<String> featureNames) {
		this.featureNames = featureNames;
	}

	public Boolean getKeyAnnotationsAreInDocumentFeatures() {
		return keyAnnotationsAreInDocumentFeatures;
	}

	@RunTime
	@CreoleParameter(defaultValue="false")
	public void setKeyAnnotationsAreInDocumentFeatures(Boolean keyAnnotationsAreInDocumentFeatures) {
		this.keyAnnotationsAreInDocumentFeatures = keyAnnotationsAreInDocumentFeatures;
	}
	

}
