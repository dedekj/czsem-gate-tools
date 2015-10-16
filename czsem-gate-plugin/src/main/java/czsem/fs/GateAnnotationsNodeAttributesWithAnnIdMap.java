package czsem.fs;

import gate.Annotation;

import java.util.Map;

public class GateAnnotationsNodeAttributesWithAnnIdMap extends GateAnnotationsNodeAttributesAbstract {
	
	public static final String META_ATTR_ANN_TYPE = "_annotation_type";
	public static final String META_ATTR_ANN_ID = "_annotation_id";

	protected Map<Integer, Annotation> annIdMap;
	protected boolean useMetaAttributes = true;

	public GateAnnotationsNodeAttributesWithAnnIdMap(Map<Integer, Annotation> annIdMap) {
		this.annIdMap = annIdMap;
	}

	@Override
	public Annotation getAnnotation(int node_id) {
		return annIdMap.get(node_id);
	}

	public boolean isUseMetaAttributes() {
		return useMetaAttributes;
	}

	public void setUseMetaAttributes(boolean useMetaAttributes) {
		this.useMetaAttributes = useMetaAttributes;
	}

	@Override
	public Object getValue(int node_id, String attrName) {
		if (! isUseMetaAttributes()) return super.getValue(node_id, attrName);
		
		switch (attrName) {
		case META_ATTR_ANN_TYPE:
			return annIdMap.get(node_id).getType();
		case META_ATTR_ANN_ID:
			return node_id;
		default:
			return super.getValue(node_id, attrName);
		}
	}

	@Override
	protected void addAdditionalAttributes(Map<String, Object> sorted,	int node_id, Annotation a) {
		sorted.put(META_ATTR_ANN_ID, node_id);
		sorted.put(META_ATTR_ANN_TYPE, a.getType());
	}
	
	
}
