package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Node;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import java.util.List;

import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;

@CreoleResource(name = "czsem SubsequentAnnotationMerge", comment = "Merges annotations that follow in a row to one single annotation.")
public class SubsequentAnnotationMergePR extends AbstractLanguageAnalyserWithInputOutputAS 
{
	private static final long serialVersionUID = -3136012330465966425L;
	
	protected String annotationTypeName;
	protected int maxDistanceBetweenAnnotations;
	protected boolean deleteOriginalAnnotations;
	
	public String getAnnotationTypeName() {
		return annotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="damage")
	public void setAnnotationTypeName(String annotationTypeName) {
		this.annotationTypeName = annotationTypeName;
	}
	public Integer getMaxDistanceBetweenAnnotations() {
		return maxDistanceBetweenAnnotations;
	}
	@RunTime
	@CreoleParameter(defaultValue="1")
	public void setMaxDistanceBetweenAnnotations(Integer maxDistanceBetweenAnnotations) {
		this.maxDistanceBetweenAnnotations = maxDistanceBetweenAnnotations;
	}

	public Boolean getDeleteOriginalAnnotations() {
		return deleteOriginalAnnotations;
	}

	@RunTime
	@CreoleParameter(defaultValue="false")
	public void setDeleteOriginalAnnotations(Boolean deleteOriginalAnnotations) {
		this.deleteOriginalAnnotations = deleteOriginalAnnotations;
	}

	@Override
	public void execute() throws ExecutionException
	{
		initBeforeExecute();
		AnnotationSet annotations = inputAS.get(annotationTypeName);		  
		List<Annotation> orderedAnnotations = gate.Utils.inDocumentOrder(annotations);
		
		for (int i = 0; i < orderedAnnotations.size(); i++)
		{
			FeatureMap fm = Factory.newFeatureMap();
			Annotation atart_annot = orderedAnnotations.get(i);
			fm.putAll(atart_annot.getFeatures());
			
			Node start_node = atart_annot.getStartNode();
			Node current_end_node = atart_annot.getEndNode();
			
			for (int j = i+1; j < orderedAnnotations.size(); j++, i++)
			{
				Annotation next_annot = orderedAnnotations.get(j);
				if (next_annot.getStartNode().getOffset() - current_end_node.getOffset() > maxDistanceBetweenAnnotations)
				{
					break;
				}
				current_end_node = next_annot.getEndNode();
				fm.putAll(next_annot.getFeatures());
			}
			
			outputAS.add(start_node, current_end_node, annotationTypeName, fm);
		}
		
		if (deleteOriginalAnnotations)
		{
			inputAS.removeAll(annotations);
		}
	}


}
