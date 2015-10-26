package czsem.gate;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.creole.SerialAnalyserController;
import gate.util.AnnotationDiffer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import czsem.Utils;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;

public class DocumentFeaturesDiff
{
	public static class AnnotationDifferDocumentFeaturesImpl extends AnnotationDiffer 
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public AnnotationDifferDocumentFeaturesImpl(int correct, int missing, int spurious)
		{
			super();
			
			this.spurious = spurious;
			this.missing = missing;
			this.correctMatches = correct;
			
		    keyList = new ArrayList(Collections.nCopies(getKeysCount(), null));
		    responseList = new ArrayList(Collections.nCopies(getResponsesCount(), null));
		}

		@Override
		public int getKeysCount() {
			return correctMatches + missing;
		}

		@Override
		public int getResponsesCount() {
			return correctMatches + spurious;
		}
	}
	
	public static void main(String [] args) throws Exception
	{
		//Logger.getLogger(DocumentFeaturesDiff.class).setLevel(Level.ALL);

		GateUtils.initGateKeepLog();
		GateUtils.registerCzsemPlugin();
		
		ProcessingResource eval = 
			new PRSetup.SinglePRSetup(LearningEvaluator.class)
				.putFeature("keyASName", ":-)")
//				.putFeature("responseASName", "lemma_flex")
				.putFeature("responseASName", "flex")
				.putFeature("keyAnnotationsAreInDocumentFeatures", true)
				.putFeatureList("annotationTypes", "Lookup")
				.putFeatureList("featureNames", "meshID").createPR();
				
				
		
		SerialAnalyserController controller = (SerialAnalyserController)	    	   
			Factory.createResource(SerialAnalyserController.class.getCanonicalName());
		
		controller.add(eval);
		
		Corpus corpus = Factory.newCorpus(null);
		corpus.populate(
				new File("C:\\Users\\dedek\\Desktop\\bmc\\experiment\\analyzed").toURI().toURL(),
//				new File("C:\\Users\\dedek\\Desktop\\bmca_devel").toURI().toURL(),
				null, "utf8", false);
		
		System.err.println("populated");
		
		controller.setCorpus(corpus);

		
		controller.execute();

		
	}

	public static AnnotationDiffer computeDiffWithGoldStandardData(
			Set<String> goldData,
			Set<String> responsesData)
	{
		int correct = 0;
		int missing = 0;
		int spurious;

		
		for (String doc_val : goldData)
		{
			if (responsesData.contains(doc_val))
			{
				correct++;
				//log.debug("cerrect id: " + doc_val);
			}
			else missing++;				
		}
		spurious = responsesData.size() - correct;
		return new AnnotationDifferDocumentFeaturesImpl(correct, missing, spurious);				
	}

	
	public static AnnotationDiffer computeDiffWithGoldStandardDataForSingleFeature(
			String featureName,
			Set<String> goldData,
			AnnotationSet responsesAnnotations)
	{
		Set<String> vals_from_annot =  new HashSet<String>();
		for (Annotation annotation : responsesAnnotations)
		{
			vals_from_annot.add((String) annotation.getFeatures().get(featureName));				
		}
		
		return computeDiffWithGoldStandardData(goldData, vals_from_annot);		
	}

	
	@SuppressWarnings("unchecked")
	public static AnnotationDiffer computeDiffWithDocFeatures(Document document, List<String> featureNames, AnnotationSet responsesAnnotations)
	{		
		FeatureMap doc_fm = document.getFeatures();
		//Logger log = Logger.getLogger(DocumentFeaturesDiff.class);

		
		int correct = 0;
		int missing = 0;
		int spurious = 0;

		for (String feature_name : featureNames)
		{
			//int cur_correct = 0;
			
			List<String> f = (List<String>) doc_fm.get(feature_name);
			if (f == null) 
			{
				f = (List<String>) doc_fm.get(feature_name+"s");
			}
					
			AnnotationDiffer diff = computeDiffWithGoldStandardDataForSingleFeature(
					feature_name,
					Utils.setFromList(f),
					responsesAnnotations);
			
			
			spurious += diff.getSpurious();
			correct += diff.getCorrectMatches();
			missing += diff.getMissing();
		}

		
		
		return new AnnotationDifferDocumentFeaturesImpl(correct, missing, spurious);
	}

}
