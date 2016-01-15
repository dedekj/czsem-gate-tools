package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;

import java.util.HashMap;
import java.util.Map;

public class GateAwareTreeIndexWithAnnIdMap extends GateAwareTreeIndex {
	
	protected AnnotationSet nodesAS;
	
	protected Map<Integer, Annotation> annIdMap = new HashMap<>();

	public Map<Integer, Annotation> getAnnIdMap() {
		return annIdMap;
	}

	public AnnotationSet getNodesAS() {
		return nodesAS;
	}

	public void setNodesAS(AnnotationSet nodesAS) {
		this.nodesAS = nodesAS;
	}

	@Override
	protected void addNode(Integer id) {
		super.addNode(id);
		
		if (nodesAS != null)
			annIdMap.put(id, nodesAS.get(id));
	}

	@Override
	public void addDependency(Annotation parentAnn, Annotation childAnn) {
		super.addDependency(parentAnn, childAnn);
		addNodeAnnotation(parentAnn);
		addNodeAnnotation(childAnn);
	}

	protected void addNodeAnnotation(Annotation a) {
		annIdMap.put(a.getId(), a);
	}

}
