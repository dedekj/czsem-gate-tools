package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import czsem.fs.DependencyConfiguration;
import czsem.fs.FSSentenceWriter.TokenDependecy;
import czsem.fs.TreeIndex;


public class GateAwareTreeIndex extends TreeIndex
{
	
	protected void addDependency(Annotation a)
	{
		Integer[] dep = GateUtils.decodeEdge(a);
		addDependency(dep[0], dep[1]);
	}; 
	
	protected void addTokenDpendency(Annotation a, String feature_name)
	{
		Integer child = (Integer) a.getFeatures().get(feature_name);
		if (child == null) return;
		addDependency(a.getId(), child);		
	}; 

	public void addDependecies(AnnotationSet dependenciesAS)
	{
		for (Annotation dep : dependenciesAS)
		{
			addDependency(dep);
		}							
	}
	
	public void addTokenDependecies(AnnotationSet tokenAS, String feature_name)
	{
		for (Annotation toc : tokenAS)
		{
			addTokenDpendency(toc, feature_name);
		}							
	}
	
	/** <b>Not including root!!!</b> **/
	public List<Annotation> getAllCildrenAnnotations(AnnotationSet annotations)
	{
		List<Annotation> ret = new ArrayList<Annotation>();
		
		for (Set<Integer> children_ids : childIndex.values())
		{
			for (Integer id : children_ids)
			{
				ret.add(annotations.get(id));
			}			
		}

		return ret;
	}

	
	public GateAwareTreeIndex() {}

	public GateAwareTreeIndex (AnnotationSet dependencyAnnotatons)
	{
		addDependecies(dependencyAnnotatons);							
	}

	public void addDependecies(AnnotationSet annotations, DependencyConfiguration configuration) {
		for (String depName : configuration.getDependencyNames())
			addDependecies(annotations.get(depName));

		for (TokenDependecy tocDep : configuration.getTokenDepDefs())
			addTokenDependecies(annotations.get(tocDep.tokenTypeName), tocDep.depFeatureName);
	}

	public void addDependency(Annotation parentAnn, Annotation childAnn) {
		addDependency(parentAnn.getId(), childAnn.getId());
	}


}
