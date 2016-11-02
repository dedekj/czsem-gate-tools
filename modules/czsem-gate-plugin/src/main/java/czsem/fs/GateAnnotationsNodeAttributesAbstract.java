package czsem.fs;

import gate.Annotation;
import gate.FeatureMap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public abstract class GateAnnotationsNodeAttributesAbstract implements NodeAttributes {
	
	public abstract Annotation getAnnotation(int node_id);
	
	@Override
	public Collection<Entry<String, Object>> get(int node_id) {
		
		Annotation a = getAnnotation(node_id);
		FeatureMap fm = a.getFeatures();
		
		TreeMap<String, Object> sorted = new TreeMap<String, Object>();
		for (Entry<Object, Object> e : fm.entrySet()) {
			sorted.put(e.getKey().toString(), e.getValue());
		}
		
		addAdditionalAttributes(sorted, node_id, a);
		
		Set<Entry<String, Object>> f = sorted.entrySet();
		return f;
	}

	protected void addAdditionalAttributes(Map<String, Object> sorted, int node_id, Annotation a) {
		// Do nothing
	}

	@Override
	public Object getValue(int node_id, String attrName) {
		return getAnnotation(node_id).getFeatures().get(attrName);
	}

	@Override
	public boolean isSubClassOf(Object dataValue, String restricitonString) {
		return restricitonString.equals(dataValue);
	}

}