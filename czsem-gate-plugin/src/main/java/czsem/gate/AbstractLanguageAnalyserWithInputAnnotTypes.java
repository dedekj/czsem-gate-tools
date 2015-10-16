package czsem.gate;

import java.util.List;

import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

public abstract class AbstractLanguageAnalyserWithInputAnnotTypes extends AbstractLanguageAnalyserWithInputOutputAS
{

	private static final long serialVersionUID = 1L;

	protected List<String> inputAnnotationTypeNames = null;

	public List<String> getInputAnnotationTypeNames() {
		return inputAnnotationTypeNames;
	}

	@RunTime
	@Optional
	@CreoleParameter(defaultValue = "acquired")
	public void setInputAnnotationTypeNames(List<String> inputAnnotationTypeNames) {
		this.inputAnnotationTypeNames = inputAnnotationTypeNames;
	}

}