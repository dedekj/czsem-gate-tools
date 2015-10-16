package czsem.gate;

import gate.AnnotationSet;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;


import czsem.gate.utils.GateAwareTreeIndex;

public abstract class AbstractAnnotationDependencyMarker extends AbstractLanguageAnalyserWithInputAnnotTypes
{
	private static final long serialVersionUID = 1L;
	
	protected String tokensAndDependenciesASName = null;

	
	protected AnnotationSet inputTokensAS = null;
	protected AnnotationSet inputDependenciesAS = null;
	protected String tokenAnnotationTypeName = "Token";
	protected String dependencyAnnotationTypeName = "aDependency";
	protected GateAwareTreeIndex treeIndex;

	public GateAwareTreeIndex getTreeIndex() {
		return treeIndex;
	}
	
	public void setTreeIndex(GateAwareTreeIndex treeIndex) {
		this.treeIndex = treeIndex;
	}
	
	public String getTokenAnnotationTypeName() {
		return tokenAnnotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="Token")
	public void setTokenAnnotationTypeName(String tokenAnnotationTypeName) {
		this.tokenAnnotationTypeName = tokenAnnotationTypeName;
	}
	public String getDependencyAnnotationTypeName() {
		return dependencyAnnotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="aDependency")
	public void setDependencyAnnotationTypeName(String dependencyAnnotationTypeName) {
		this.dependencyAnnotationTypeName = dependencyAnnotationTypeName;
	}
	
	public void initIndex(AnnotationSet tokensAndDependenciesAS) {
		
		inputTokensAS = tokensAndDependenciesAS.get(tokenAnnotationTypeName);
		inputDependenciesAS = tokensAndDependenciesAS.get(dependencyAnnotationTypeName);
		treeIndex = new GateAwareTreeIndex(inputDependenciesAS);
		
	}
	
	@Override
	protected void initBeforeExecute()
	{
		super.initBeforeExecute();
		
		if (getTokensAndDependenciesASName() == null) setTokensAndDependenciesASName(getInputASName());
		
		AnnotationSet tocDepAS = document.getAnnotations(getTokensAndDependenciesASName());

		initIndex(tocDepAS);
		
	}

	public String getTokensAndDependenciesASName() {
		return tokensAndDependenciesASName;
	}
	
	@RunTime
	@Optional
	@CreoleParameter(comment="If not set, inputAS is taken.")
	public void setTokensAndDependenciesASName(String tokensAndDependenciesASName) {
		this.tokensAndDependenciesASName = tokensAndDependenciesASName;
	}


}
