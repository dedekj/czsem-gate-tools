package czsem.gate.plugins;

import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import czsem.Utils;
import czsem.gate.AbstractLanguageAnalyserWithInputAnnotTypes;

@SuppressWarnings("serial")
public class PutTokenIdFeaturePR extends AbstractLanguageAnalyserWithInputAnnotTypes {
	public static final String defaultTokenIdFeatureName = "ann_id";

	private String tokenIdFeatureName = defaultTokenIdFeatureName;

	@Override
	public void execute() throws ExecutionException {
		initBeforeExecute();
		
		AnnotationSet ans;
		
		if (inputAnnotationTypeNames == null || inputAnnotationTypeNames.size() == 0)
			ans = inputAS.get("Token");
		else {
			ans = inputAS.get(Utils.setFromList(inputAnnotationTypeNames));			
		}

		
		for (Annotation a : ans) {
			a.getFeatures().put(tokenIdFeatureName, a.getId().toString());
		}
		
	}

	@RunTime
	@Optional
	@CreoleParameter(defaultValue = defaultTokenIdFeatureName)
	public void setTokenIdFeatureName(String tokenIdFeatureName) {
		this.tokenIdFeatureName = tokenIdFeatureName;
	}

	public String getTokenIdFeatureName() {
		return tokenIdFeatureName;
	}
	
	@Override
	@RunTime
	@Optional
	@CreoleParameter(defaultValue = "Token")
	public void setInputAnnotationTypeNames(List<String> inputAnnotationTypeNames) {
		this.inputAnnotationTypeNames = inputAnnotationTypeNames;
	}
	
	@Override
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}



}
