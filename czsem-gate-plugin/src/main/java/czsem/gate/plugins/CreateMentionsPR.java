package czsem.gate.plugins;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import czsem.Utils;
import czsem.gate.AbstractLanguageAnalyserWithInputAnnotTypes;

// 
@CreoleResource(name = "czsem CreateMentions", comment = "Creates annotations of a type �Mention� and puts class labels (original annotation types) to the feature �class�.")
public class CreateMentionsPR extends AbstractLanguageAnalyserWithInputAnnotTypes 
{
	
	private static final long serialVersionUID = 3795111522583168425L;
	
	public static final String defaultMentionAnntotationTypeName = "Mention";
	private String mentionAnntotationTypeName = defaultMentionAnntotationTypeName;
	private boolean inverseFunction = false;
	private boolean copyFeatures = false;
	/** Used only during inverse work. Creates a mention using a referenced annotation. The reference is inside an aligned annotation. example: 'NamedEntity_root.origRootID'*/	 
	private String useReferenceAnnotationFeature = null;

	@Override
	public void execute() throws ExecutionException
	{
		initBeforeExecute();
		
		if (inverseFunction)
			inverseWork();
		else
			regularWork();
					
	}
	
	public void inverseWork()
	{
		AnnotationSet annotations = null;
		if (inputAnnotationTypeNames == null || inputAnnotationTypeNames.size() == 0)
		{
			annotations = inputAS.get(getMentionAnntotationTypeName());
		}
		else
		{
			HashSet<String> cls_set = new HashSet<String>();
			cls_set.add("class");
			annotations = inputAS.get(getMentionAnntotationTypeName(), cls_set);
			
			Iterator<Annotation> iter = annotations.iterator();
			
			while(iter.hasNext())
			{
				Annotation annotation = iter.next();
				String cls = (String) annotation.getFeatures().get("class");
				
				if (! inputAnnotationTypeNames.contains(cls))
				{
					iter.remove();					
				}								
			}
		}
		
		
		String refType = null;
		String refFeature = null;
		Set<String> setRefFeature = null;
		if (useReferenceAnnotationFeature != null)
		{
			String[] split = useReferenceAnnotationFeature.split("\\.", 2);
			refType = split[0];
			refFeature = split[1];
			setRefFeature = new HashSet<String>(1);
			setRefFeature.add(refFeature);
		}

		for (Annotation annotation : annotations)
		{
			FeatureMap fm = Factory.newFeatureMap();
			fm.putAll(annotation.getFeatures());
			String cls = (String) fm.get("class");
			fm.put("class", annotation.getType());
			fm.put("origMentID", annotation.getId());
			
			if (useReferenceAnnotationFeature != null)
			{
				AnnotationSet refSet = inputAS.
					getContained(
							annotation.getStartNode().getOffset(),
							annotation.getEndNode().getOffset())
								.get(refType, setRefFeature);
				
				if (refSet.isEmpty()) continue;
				
				Annotation annotWithReference = refSet.iterator().next();
				Annotation referedAnnot = inputAS.get(
						(Integer) annotWithReference.getFeatures().get(refFeature));

				outputAS.add(referedAnnot.getStartNode(), referedAnnot.getEndNode(), cls, fm);								
			}
			else
				outputAS.add(annotation.getStartNode(), annotation.getEndNode(), cls, fm);
			
			
		}					

		
	}

	public void regularWork()
	{
		AnnotationSet annotations = null;
		if (inputAnnotationTypeNames == null || inputAnnotationTypeNames.size() == 0)
			annotations = inputAS.get();
		else
			annotations = inputAS.get(Utils.setFromList(inputAnnotationTypeNames));
		
		for (Annotation annotation : annotations)
		{
			FeatureMap fm = Factory.newFeatureMap();
			if (getCopyFeatures()) fm.putAll(annotation.getFeatures());				
			fm.put("class", annotation.getType());
			fm.put("origMentID", annotation.getId());
			
			outputAS.add(annotation.getStartNode(), annotation.getEndNode(), getMentionAnntotationTypeName(), fm);
			
			
		}					
	}

	@RunTime
	@CreoleParameter(defaultValue="Mention")	
	public void setMentionAnntotationTypeName(String mentionAnntotationTypeName) {
		this.mentionAnntotationTypeName = mentionAnntotationTypeName;
	}

	public String getMentionAnntotationTypeName() {
		return mentionAnntotationTypeName;
	}

	@RunTime
	@CreoleParameter(comment="Inverse work of the PR - form Mentions create original annotations.", defaultValue="false")
	public void setInverseFunction(Boolean inverseFunction) {
		this.inverseFunction = inverseFunction;
	}

	public Boolean getInverseFunction() {
		return inverseFunction;
	}
	
	@Optional
	@RunTime
	@CreoleParameter(comment="Used only during inverse work. Creates a mention using a referenced annotation. The reference is inside an aligned annotation. example: 'NamedEntity_root.origRootID'")
	public void setUseReferenceAnnotationFeature(String useReferenceAnnotationFeature) {
		this.useReferenceAnnotationFeature = useReferenceAnnotationFeature;
	}

	public String getUseReferenceAnnotationFeature() {
		return useReferenceAnnotationFeature;
	}


	@Optional
	@RunTime
	@CreoleParameter(comment="Weather to copy features form the original annotations.", defaultValue="false")
	public void setCopyFeatures(Boolean copyFeatures) {
		this.copyFeatures = copyFeatures;
	}

	public Boolean getCopyFeatures() {
		return copyFeatures;
	}
}
