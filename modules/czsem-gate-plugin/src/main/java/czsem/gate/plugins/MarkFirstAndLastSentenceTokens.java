package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import java.util.HashSet;
import java.util.Set;

@CreoleResource(name = "czsem MarkFirstAndLastSentenceTokens", comment = "Adds sentencePos feature to tokens on beginnings and ends of sentences.")
public class MarkFirstAndLastSentenceTokens extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = 6260836415226688928L;

	private String annotationSetName = null;
	private String tokenAnnotationTypeName = "Token";
	private String sentenceAnnotationTypeName = "Sentence";
	
	
	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		AnnotationSet as = doc.getAnnotations(getAnnotationSetName());
		AnnotationSet tocs = as.get(getTokenAnnotationTypeName());
		AnnotationSet sents = as.get(getSentenceAnnotationTypeName());
		
		Set<Long> sentStarts = new HashSet<Long>();		
		Set<Long> sentEnds = new HashSet<Long>();		
		for (Annotation s: sents) {
			sentStarts.add(s.getStartNode().getOffset());
			sentEnds.add(s.getEndNode().getOffset());
		}


		for (Annotation t : tocs)
		{
			int val = 0;
			if (sentStarts.contains(t.getStartNode().getOffset()))
				val = 1;
			
			if (sentEnds.contains(t.getEndNode().getOffset()))
				val |= 2;
			
			switch (val) {
			case 1:
				t.getFeatures().put("sentencePos", "first");				
				break;
			case 2:
				t.getFeatures().put("sentencePos", "last");				
				break;
			case 3:
				t.getFeatures().put("sentencePos", "both");				
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


}
