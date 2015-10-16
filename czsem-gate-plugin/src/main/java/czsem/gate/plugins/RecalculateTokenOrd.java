package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import java.util.List;

@CreoleResource(name = "czsem RecalculateTokenOrd")
public class RecalculateTokenOrd  extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = -7536637069315382960L;
	private String annotationSetName = null;
	private String tokenAnnotationTypeName = "Token";
	private String sentenceAnnotationTypeName = "Sentence";
	private String ordFeatureName = "ord";
	
	
	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		AnnotationSet as = doc.getAnnotations(getAnnotationSetName());
		AnnotationSet tocs = as.get(getTokenAnnotationTypeName());
		AnnotationSet sents = as.get(getSentenceAnnotationTypeName());
		
		for (Annotation s: sents) {
			List<Annotation> sorted = Utils.inDocumentOrder(Utils.getContainedAnnotations(tocs, s));
			int ord = 1;
			for (Annotation t : sorted)
			{
				t.getFeatures().put(getOrdFeatureName(), Integer.toString(ord++));
			}
		}
	}
	
	
	public String getAnnotationSetName() {
		return annotationSetName;
	}	
	@RunTime
	@CreoleParameter(defaultValue="")
	public void setAnnotationSetName(String annotationSetName) {
		this.annotationSetName = annotationSetName;
	}
	
	public String getTokenAnnotationTypeName() {
		return tokenAnnotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="Token")
	public void setTokenAnnotationTypeName(String tokenAnnotationTypeName) {
		this.tokenAnnotationTypeName = tokenAnnotationTypeName;
	}
	
	public String getSentenceAnnotationTypeName() {
		return sentenceAnnotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="Sentence")
	public void setSentenceAnnotationTypeName(String sentenceAnnotationTypeName) {
		this.sentenceAnnotationTypeName = sentenceAnnotationTypeName;
	}

	public String getOrdFeatureName() {
		return ordFeatureName;
	}
	@RunTime
	@CreoleParameter(defaultValue="ord")
	public void setOrdFeatureName(String ordFeatureName) {
		this.ordFeatureName = ordFeatureName;
	}
}
