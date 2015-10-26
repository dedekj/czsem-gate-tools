package czsem.fs;

import gate.Annotation;
import gate.AnnotationSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class FSSentenceStringBuilder {
	
	private StringWriter wr;
	private FSSentenceWriter fswr;

	public FSSentenceStringBuilder(Annotation covered_annotation, AnnotationSet document_annotations)	 {
		this(findApropriateSentenceAnnotaions(covered_annotation, document_annotations));
	}
	
	public static AnnotationSet findApropriateSentenceAnnotaions(Annotation covered_annotation, AnnotationSet document_annotations)
	{
		Annotation sentence = covered_annotation; 

		if (! covered_annotation.getType().equals("Sentence"))
		{
			AnnotationSet sentences = document_annotations.getCovering("Sentence", 
					covered_annotation.getStartNode().getOffset(), 
					covered_annotation.getEndNode().getOffset());
			
			Iterator<Annotation> iter = sentences.iterator();
			if (iter.hasNext())	sentence = iter.next();
			else return null;
		}
		
		AnnotationSet sas = document_annotations.getContained(
				sentence.getStartNode().getOffset(), 
				sentence.getEndNode().getOffset());
		
		return sas;
				
	}

	public FSSentenceStringBuilder(AnnotationSet sentence_annotations)	 {
		wr = new StringWriter();
		fswr = new FSSentenceWriter(sentence_annotations, new PrintWriter(wr));
		fswr.printTree();
	}

	
	
	public String getTree() {
		return wr.toString();
	}



	public String[] getAttributes() {
		return fswr.getAttributes().toArray(new String [0]);
	}

}
