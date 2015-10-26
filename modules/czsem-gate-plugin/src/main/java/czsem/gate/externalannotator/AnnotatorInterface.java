package czsem.gate.externalannotator;

import gate.Annotation;
import gate.util.InvalidOffsetException;
import czsem.gate.externalannotator.Annotator.Annotable;
import czsem.gate.externalannotator.Annotator.AnnotableDependency;

public interface AnnotatorInterface {
	void annotate(Annotable ann, Long startOffset, Long endOffset) throws InvalidOffsetException;
	void annotateDependecy(AnnotableDependency dAnn) throws InvalidOffsetException;
	Annotation getAnnotation(Integer id);
}
