package czsem.fs;

import gate.Annotation;
import gate.AnnotationSet;

public class GateAnnotationsNodeAttributes extends GateAnnotationsNodeAttributesAbstract {
	protected AnnotationSet annotations;

	public GateAnnotationsNodeAttributes(AnnotationSet annotations) {
		this.annotations = annotations;
	}

	@Override
	public Annotation getAnnotation(int node_id) {
		return annotations.get(node_id);
	}
}
