package czsem.netgraph.treesource;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import czsem.gate.utils.GateAwareTreeIndexWithAnnIdMap;
import czsem.netgraph.GateAnnotTableModel;

public class TreeIndexTreeSource implements TreeSource<Integer>, Comparator<Integer> {
	
	protected GateAwareTreeIndexWithAnnIdMap index;
	protected Document doc;
	protected int selectedNode;
	
	protected final LinkedHashSet<Object> selectedAttributes 
		= new LinkedHashSet<>(Collections.singleton(GateAnnotTableModel.ATTR.STRING));

	@Override
	public Integer getRoot() {
		return index.findRoot();
	}

	@Override
	public Collection<Integer> getChildren(Integer parent) {
		return index.getChildren(parent);
	}

	@Override
	public List<TreeSource.NodeLabel> getLabels(Integer node) {
		List<TreeSource.NodeLabel> ret = new ArrayList<>(selectedAttributes.size());
		for (Object attr : selectedAttributes) {
			Annotation a = index.getAnnIdMap().get(node);
			Object val = GateAnnotTableModel.getAnnotationAttr(doc, a, attr);
			if (val == null) val = "";
			ret.add(new StaticLabel(val.toString()));
		}
		return ret;
	}

	@Override
	public Comparator<Integer> getOrderComparator() {
		return this;
	}

	@Override
	public int compare(Integer node1, Integer node2) {
		Annotation a1 = index.getAnnIdMap().get(node1);
		Annotation a2 = index.getAnnIdMap().get(node2);
		return Utils.OFFSET_COMPARATOR.compare(a1, a2);
	}

	public void setTreeAS(Document doc, AnnotationSet annotations) {
		this.doc = doc;
		index = new GateAwareTreeIndexWithAnnIdMap();
		index.setNodesAS(annotations);
		index.addDependecies(annotations.get(null, Collections.singleton("args")));
		selectedNode = index.findRoot();
	}

	public Annotation getSelectedAnnot() {
		return index.getAnnIdMap().get(selectedNode);
	}

	public Document getDoc() {
		return doc;
	}

	public LinkedHashSet<Object> getSelectedAttributes() {
		return selectedAttributes;
	}
}
