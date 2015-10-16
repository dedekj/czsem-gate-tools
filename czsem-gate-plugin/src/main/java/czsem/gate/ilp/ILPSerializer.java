
package czsem.gate.ilp;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.creole.AbstractLanguageAnalyser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

import czsem.ILP.LinguisticSerializer;
import czsem.utils.MultiSet;


/** Exports given corpus to ILP background knowledge **/
public class ILPSerializer extends AbstractLanguageAnalyser
{
	private static final long serialVersionUID = 6469933231715581382L;
	static Logger logger = Logger.getLogger(ILPSerializer.class);
	
	protected LinguisticSerializer lingSer;
	
	protected String docName;
	protected AnnotationSet as;

//	protected String [] class_attribute_values;
	
	private MultiSet<String> instanceClassTypes = new MultiSet<String>();
	
	
	protected String [] token_types =
	{
//		"Token",
//		"tToken"
	};

	protected String [][] token_features = 
	{
//			{"form", "lemma", "tag", "afun", "ord"},
//			{"nodetype", "t_lemma", "functor", "deepord", "formeme", "sempos", "gender", "negation", "number",
//			"degcmp", "verbmod", "deontmod", "tense", "aspect", "resultative",	"dispmod", "iterativeness"}
	};
	
	protected String [] tree_dependecies =
	{
//		"Dependency",
//		"aDependency",
//		"tDependency",
//		"auxRfDependency"
	};


	protected String [][] tree_dependecy_args =
	{
//			{"Token", "Token"},			
//			{"Token", "Token"},			
//			{"tToken", "tToken"},			
//			{"tToken", "Token"},			
	};

	protected String [][] overlap_dependecy_args =
	{
//			{"Lookup", "tToken"}			
	};

	
	protected String [] one2one_dependecies =
	{
//		"lex.rf"	
	};

	protected String [][] one2one_dependecy_args =
	{
//			{"tToken", "Token"}
	};

	public ILPSerializer(String projectDir, String projectName) throws FileNotFoundException, UnsupportedEncodingException
	{
		lingSer = new LinguisticSerializer(projectDir, projectName);		
	}
	
	protected void createFeatureTypes()
	{
		int cumulative_features = 0;
		for (int t=0; t<token_types.length; t++)
		{
			for (int f=0; f<token_features[t].length; f++)
			{
				lingSer.createFeatureType(cumulative_features++, token_types[t], token_features[t][f]);
			}
		}				
	}
	protected void createOverlapDependencyTypes()
	{
		for (int d=0; d<overlap_dependecy_args.length; d++)
		{
			lingSer.createOverlapRelsDependencyType(
					d,
					"overlap_"+overlap_dependecy_args[d][0]+'_'+ overlap_dependecy_args[d][1],
					overlap_dependecy_args[d][0],
					overlap_dependecy_args[d][1]);
		}		
	}
	protected void createTreeDependencyTypes()
	{
		for (int d=0; d<tree_dependecies.length; d++)
		{
			lingSer.createTreeDependencyType(
					d,
					tree_dependecies[d],
					tree_dependecy_args[d][0],
					tree_dependecy_args[d][1]);
		}
		
	}
	protected void createOne2oneTreeDependencyTypes()
	{
		for (int d=0; d<one2one_dependecies.length; d++)
		{
			lingSer.createOneToOneDependencyType(
					d,
					one2one_dependecies[d],
					one2one_dependecy_args[d][0],
					one2one_dependecy_args[d][1]);
		}		
	}
	
	protected void serializeToneks()
	{
		int token_features_offset = 0;
		for (int t=0; t<token_types.length; t++)
		{
			AnnotationSet tocs = as.get(token_types[t]);			
			for (Annotation token : tocs)
			{
				FeatureMap feats = token.getFeatures();
				for (int f=0; f<token_features[t].length; f++)
				{
					Object feat_val = feats.get(token_features[t][f]);
					
					if (feat_val != null)
					{
						lingSer.putFeature(
								token_features_offset + f,
								renderID(token.getId()),
								feat_val.toString());
						
					}					
				}
			}
			
			token_features_offset+= token_features[t].length;			
		}					
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> argsFromDependencyAnnotation(Annotation dep_annot)
	{
		return (List<Integer>) dep_annot.getFeatures().get("args");
	}
	
	protected void serializeTreeDependency(int dependencyIndex, int parent_id, int child_id)
	{
		lingSer.putTreeDependency(
				dependencyIndex,
				renderID(parent_id),
				renderID(child_id));
	}
	protected void serializeOverlapDependency(int dependencyIndex, int parent_id, int child_id)
	{
		lingSer.putOverlapDependency(
				dependencyIndex,
				renderID(parent_id),
				renderID(child_id));
	}
	
	protected void serializeOne2OneDependency(int dependencyIndex, Annotation parent)
	{
		Object child = parent.getFeatures().get(one2one_dependecies[dependencyIndex]);
		
		if (child != null)
		{
			lingSer.putOneToOneDependency(
					dependencyIndex, 
					renderID(parent.getId()),
					renderID((Integer) child));					
		}		
	}

	
	protected void serializeOverlapDependencies()
	{
		for (int d=0; d<overlap_dependecy_args.length; d++)
		{
			AnnotationSet parents = as.get(overlap_dependecy_args[d][0]);
			for (Annotation parent : parents)
			{
				AnnotationSet potential_children = as.get(overlap_dependecy_args[d][1]);
				AnnotationSet children = potential_children.getContained(parent.getStartNode().getOffset(), parent.getEndNode().getOffset());
				for (Annotation child : children) {
					serializeOverlapDependency(d, parent.getId(), child.getId());									
				}
			}			
		}		
	}

	protected void serializeTreeDependencies()
	{
		for (int d=0; d<tree_dependecies.length; d++)
		{
			AnnotationSet deps = as.get(tree_dependecies[d]);
			for (Annotation dep : deps)
			{
				List<Integer> args = argsFromDependencyAnnotation(dep);
				serializeTreeDependency(d, args.get(0), args.get(1));				
			}			
		}		
	}
	
	protected void serializeOne2OneDependencies()
	{
		for (int d=0; d<one2one_dependecies.length; d++)
		{
			AnnotationSet parents = as.get(one2one_dependecy_args[d][0]);
			for (Annotation parent : parents)
			{
				try
				{
					serializeOne2OneDependency(d, parent);
				}
				catch (ClassCastException e)
				{
					logger.error(one2one_dependecies[d], e);
				}
				
			}			
		}				
	}


	public void serializeDocument(Document document, String asName)
	{
		docName = document.getName();
		as = document.getAnnotations(asName);
		
		serializeToneks();
		serializeTreeDependencies();
		serializeOne2OneDependencies();				
		serializeOverlapDependencies();
	}

	protected String renderID(String id)
	{
		return renderID(new Integer(id));
	}

	protected String renderID(Integer id)
	{
		return renderID(id, docName);
	}

	protected static String renderID(Integer id, String docName)
	{		
		StringBuilder sb = new StringBuilder();
		sb.append("id_");
		sb.append(docName);
		sb.append('_');
		sb.append(id);
		
		assert parseID(sb.toString()) == id;
		
		return  sb.toString();
	}

	protected static String renderID(Annotation anntotation, AnnotationSet as)
	{
		return renderID(anntotation.getId(), as.getDocument().getName());
	}

	protected static int parseID(String id_string)
	{				
		String[] split = id_string.split("_");
		return Integer.parseInt(split[split.length-1]);
	}

	
	public void train() throws IOException, InterruptedException, URISyntaxException
	{
		lingSer.train();
	}

	public void serializeTrainingInstance(String instanceGateId, String docName, String instanceTypeName, String class_attribute_vlaue)
	{
		if (instanceGateId == null) 
			throw new NullPointerException(
					String.format(
							"Instance ID is null. docName: '%s', instanceTypeName: '%s'",
							docName, instanceTypeName));
		
		getInstanceClassTypes().add(class_attribute_vlaue);
		
		String instance_id = renderID(instanceGateId);
		
		lingSer.putExample(instance_id, instanceTypeName, class_attribute_vlaue);									
	}

	public void flushAndClose()
	{
		lingSer.flushAndClose();		
	}

	public void initSerializer(Element serializerOtionsElem)
	{
		parseOptions(serializerOtionsElem);
		
		createFeatureTypes();
		createTreeDependencyTypes();
		createOne2oneTreeDependencyTypes();
		createOverlapDependencyTypes();
		
	}
	

	public void initLearning(String className, String classTypeName, String learning_settings)
	{		
		lingSer.putLearningSettings(learning_settings);
		lingSer.putModes();
		lingSer.putDeterminations(className, classTypeName);				
	}

/*	
	public static String [] parseClassAttributeValuesFromSettingsFile(URL config_doc_url) throws JDOMException, IOException
	{
		SAXBuilder parser = new SAXBuilder();
		org.jdom.Document ilp_dom = parser.build(config_doc_url);
		
		Element serializerOtionsElem = ilp_dom.getRootElement().getChild("ENGINE").getChild("OPTIONS").getChild("serializer");

		return parseClassAttributeValuesFromSerializerOptions(serializerOtionsElem);
	}

	@SuppressWarnings("unchecked")
	public static String [] parseClassAttributeValuesFromSerializerOptions(Element serializerOtionsElem)
	{
		List<Element> class_attribute_values = serializerOtionsElem.getChild("class_attribute_values").getChildren("value");
		String [] ret = new String[class_attribute_values.size()];
		for (int v = 0; v < class_attribute_values.size(); v++)
		{
			ret[v] = class_attribute_values.get(v).getText();			
		}

		return ret;		
	}
/**/
	@SuppressWarnings("unchecked")
	protected void parseOptions(Element serializerOtionsElem)
	{		
//		class_attribute_values = parseClassAttributeValuesFromSerializerOptions(serializerOtionsElem);

		if (serializerOtionsElem.getChild("tokens") != null)
		{
			List<Element> tokens = serializerOtionsElem.getChild("tokens").getChildren("token");
			this.token_types = new String[tokens.size()];
			this.token_features = new String[tokens.size()][];
			for (int t=0; t<tokens.size(); t++)
			{
				Element token = tokens.get(t);
				this.token_types[t] = token.getAttributeValue("typename");
				List<Element> token_features = token.getChild("features").getChildren("feature");
				this.token_features[t] = new String[token_features.size()];
				for (int f = 0; f < token_features.size(); f++)
				{
					this.token_features[t][f] = token_features.get(f).getValue();				
				}
			}
		}
		
		
		if (serializerOtionsElem.getChild("tree_dependecies") != null)
		{
			List<Element> tree_dependecies = serializerOtionsElem.getChild("tree_dependecies").getChildren("dependecy");
			this.tree_dependecies = new String[tree_dependecies.size()];
			this.tree_dependecy_args = new String[tree_dependecies.size()][];
			for (int tree_dep = 0; tree_dep < tree_dependecies.size(); tree_dep++)
			{
				this.tree_dependecies[tree_dep] = tree_dependecies.get(tree_dep).getAttributeValue("typename");
				this.tree_dependecy_args[tree_dep] = new String[2];
				this.tree_dependecy_args[tree_dep][0] = tree_dependecies.get(tree_dep).getAttributeValue("parent_typename");
				this.tree_dependecy_args[tree_dep][1] = tree_dependecies.get(tree_dep).getAttributeValue("child_typename");			
			}
		}
		
		if (serializerOtionsElem.getChild("one2one_dependecies") != null)
		{
			List<Element> one2one_dependecies = serializerOtionsElem.getChild("one2one_dependecies").getChildren("dependecy");		
			this.one2one_dependecies = new String[one2one_dependecies.size()];
			this.one2one_dependecy_args = new String[one2one_dependecies.size()][];
			for (int one_dep = 0; one_dep < one2one_dependecies.size(); one_dep++)
			{
				this.one2one_dependecies[one_dep] = one2one_dependecies.get(one_dep).getAttributeValue("typename");
				this.one2one_dependecy_args[one_dep] = new String[2];
				this.one2one_dependecy_args[one_dep][0] = one2one_dependecies.get(one_dep).getAttributeValue("parent_typename");
				this.one2one_dependecy_args[one_dep][1] = one2one_dependecies.get(one_dep).getAttributeValue("child_typename");			
			}
		}

		if (serializerOtionsElem.getChild("overlap_dependecies") != null)
		{
			List<Element> ovelap_dependecies = serializerOtionsElem.getChild("overlap_dependecies").getChildren("dependecy");
			this.overlap_dependecy_args = new String[ovelap_dependecies.size()][];
			for (int overlap_dep = 0; overlap_dep < ovelap_dependecies.size(); overlap_dep++)
			{
				this.overlap_dependecy_args[overlap_dep] = new String[2];
				this.overlap_dependecy_args[overlap_dep][0] = ovelap_dependecies.get(overlap_dep).getAttributeValue("parent_typename");
				this.overlap_dependecy_args[overlap_dep][1] = ovelap_dependecies.get(overlap_dep).getAttributeValue("child_typename");			
			}
		}

	}

	public void setBackgroundSerializerFileName(String fileName) throws FileNotFoundException, UnsupportedEncodingException
	{
		lingSer.setBackgroundSerializerFileName(fileName);		
	}

	public Collection<String>[] classifyInstances(String[] instancesGateIds, String targetRelationName) throws IOException, InterruptedException, URISyntaxException
	{
		for (int i = 0; i < instancesGateIds.length; i++) {
			instancesGateIds[i] = renderID(instancesGateIds[i]);
		}
		
		return lingSer.classifyInstances(instancesGateIds, docName, targetRelationName);
	}

	public void closeBackgroundSerializer() {
		lingSer.closeBackgroundSerializer();
	}

	public void setInstanceClassTypes(MultiSet<String> instanceClassTypes) {
		this.instanceClassTypes = instanceClassTypes;
	}

	public MultiSet<String> getInstanceClassTypes() {
		return instanceClassTypes;
	}
}
