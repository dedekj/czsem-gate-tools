package czsem.ILP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import czsem.Utils;
import czsem.ILP.Serializer.Relation;
import czsem.utils.ProjectSetup;

public class LinguisticSerializer 
{
	protected Serializer ser_bkg;
	protected Serializer ser_pos;
	protected Serializer ser_neg;
	
	protected List<Relation> featRels;
	protected List<Relation> treeDepRels;
	protected List<Relation> one2oneDepRels;
	protected List<Relation> overlapRels;
	
	protected File workingDirectory;
	protected String projectName;
	protected List<Example> examples;
	protected Set<String> exampleClassValues;
	
	protected static class Example
	{
		public Example(String instance_id, String instanceTypeName,	String class_attribute_value)
		{
			this.instance_id = instance_id;
			this.instanceTypeName = instanceTypeName;
			this.class_attribute_value = class_attribute_value;
		}
		public String instance_id;
		public String instanceTypeName;
		public String class_attribute_value;		
	}
	
	public LinguisticSerializer(String projectDir, String projectName) throws FileNotFoundException, UnsupportedEncodingException
	{
		workingDirectory = new File(projectDir + "/savedFiles");
		workingDirectory.mkdir();
		
		this.projectName = projectName;
		
		String path_name_perfix = projectDir + "/savedFiles/" +projectName;
		
		ser_bkg = new Serializer(path_name_perfix + ".b");
		ser_pos = new Serializer(path_name_perfix + ".f");
		ser_neg = new Serializer(path_name_perfix + ".n");
		
		featRels = new ArrayList<Relation>();
		treeDepRels = new ArrayList<Relation>(5);
		overlapRels = new ArrayList<Relation>();
		one2oneDepRels = new ArrayList<Relation>(3);
		examples = new ArrayList<Example>();
		exampleClassValues = new HashSet<String>();
	}

	
	
	
	public void createOverlapRelsDependencyType(int index, String dependencyName, String parentType, String childType)
	{
		Relation rel = createDependencyType(index, dependencyName, parentType, childType);
		overlapRels.add(index, rel);
	};
	public void createTreeDependencyType(int index, String dependencyName, String parentType, String childType)
	{
		Relation rel = createDependencyType(index, dependencyName, parentType, childType);
		treeDepRels.add(index, rel);
	};
	public void createOneToOneDependencyType(int index, String dependencyName, String parentType, String childType)
	{
		Relation rel = createDependencyType(index, dependencyName, parentType, childType);
		one2oneDepRels.add(index, rel);
	};
	
	protected Relation createDependencyType(int index, String dependencyName, String parentType, String childType)
	{
		Relation rel = ser_bkg.addBinRelation(dependencyName, parentType, childType);
		return rel;
	};

	public void createFeatureType(int index, String annotationType, String featureName)
	{
		Relation rel = ser_bkg.addBinRelation(/*"has_" + */featureName, annotationType, featureName+'T');
		featRels.add(index, rel);
	};
	
	
	public void putOverlapDependency(int dependencyIndex, String parentId, String childId)
	{
		ser_bkg.putBinTuple(overlapRels.get(dependencyIndex), parentId, childId);
	};
	public void putTreeDependency(int dependencyIndex, String parentId, String childId)
	{
		ser_bkg.putBinTuple(treeDepRels.get(dependencyIndex), parentId, childId);
	};	
	public void putOneToOneDependency(int dependencyIndex, String parentId, String childId)
	{
		ser_bkg.putBinTuple(one2oneDepRels.get(dependencyIndex), parentId, childId);
	};
	
	public void putFeature(int featureIndex, String annotationId, String featureValue)
	{
		ser_bkg.putBinTuple(featRels.get(featureIndex), annotationId, featureValue);
	};
	
	public void putModes()
	{
		ser_bkg.putCommentLn("-------------------- Modes --------------------");
		
		for (Relation rel : featRels)
		{
			ser_bkg.putBinaryMode(rel, "1", '+', '#');
		}
		
		for (Relation rel : one2oneDepRels)
		{
			ser_bkg.putBinaryMode(rel, "1", '+', '-');
			ser_bkg.putBinaryMode(rel, "1", '-', '+');
		}

		for (Relation rel : treeDepRels)
		{
			ser_bkg.putBinaryMode(rel, "*", '+', '-');
			ser_bkg.putBinaryMode(rel, "1", '-', '+');		
//			ser_bkg.putBinaryMode(rel, "1", '+', '+');		
//			ser_bkg.putBinaryMode(rel, "1", '-', '-');		
		}
		for (Relation rel : overlapRels)
		{
			ser_bkg.putBinaryMode(rel, "*", '+', '-');
			ser_bkg.putBinaryMode(rel, "*", '-', '+');		
//			ser_bkg.putBinaryMode(rel, "1", '+', '+');		
//			ser_bkg.putBinaryMode(rel, "1", '-', '-');		
		}
		ser_bkg.putCommentLn("-------------------- Modes END --------------------");		
	}

	public void putDeterminations(String targetRelationName, String targetRelationArgTypeName)
	{
		Relation target = ser_bkg.addRealtion(targetRelationName, new String[]{"class_attribute_value", targetRelationArgTypeName});
		ser_bkg.putMode(target, "1", new char[] {'#','+'});


		ser_bkg.putCommentLn("-------------------- Determinations --------------------");
		
		for (Relation rel : featRels)
		{
			ser_bkg.putDetermination(target, rel);			
		}
		
		for (Relation rel : one2oneDepRels)
		{
			ser_bkg.putDetermination(target, rel);			
		}

		for (Relation rel : treeDepRels)
		{
			ser_bkg.putDetermination(target, rel);			
		}
		for (Relation rel : overlapRels)
		{
			ser_bkg.putDetermination(target, rel);			
		}
		ser_bkg.putCommentLn("-------------------- Determinations END --------------------");
		
	}

	
	public void putExample(String instance_id, String instanceTypeName,	String class_attribute_vlaue)
	{
		examples.add(new Example(instance_id, instanceTypeName, class_attribute_vlaue));
		exampleClassValues.add(class_attribute_vlaue);
		
	}

	protected void flushExamples()
	{
		exampleClassValues.remove(null);
		
		for (Example ex : examples)
		{
			for (String cls_val : exampleClassValues)
			{
				if (cls_val.equals(ex.class_attribute_value))
					putPositiveExample(ex.instance_id, ex.instanceTypeName, cls_val);
				else
					putNegativeExample(ex.instance_id, ex.instanceTypeName, cls_val);
			}
			
		}
		
	}

	public static void putExampleInlineWithoutClassValueCheck(Serializer ser, String instanceId, String instanceTypeName, String class_attribute_vlaue)
	{
		
		ser.renderInlineTupleWithoutValueCheck(
				Serializer.encodeValue(instanceTypeName),
				new String[]{
					class_attribute_vlaue, 
					Serializer.encodeValue(instanceId)});        				
	}

	public static void putExample(Serializer ser, String instanceId, String instanceTypeName, String class_attribute_vlaue)
	{
		putExampleInlineWithoutClassValueCheck(
				ser,
				instanceId,
				instanceTypeName,
				Serializer.encodeValue(class_attribute_vlaue));
		ser.print(".\n");
	}
	
	public void putPositiveExample(String instanceId, String instanceTypeName, String class_attribute_vlaue)
	{
		putExample(ser_pos, instanceId, instanceTypeName, class_attribute_vlaue);
	};
	public void putNegativeExample(String instanceId, String instanceTypeName, String class_attribute_vlaue)
	{
		putExample(ser_neg, instanceId, instanceTypeName, class_attribute_vlaue);
	}


	public void flushAndClose()
	{
		flushExamples();
		
		ser_bkg.putCommentLn("-------------------- outputAllTypes --------------------");
		ser_bkg.outputAllTypes();
		ser_bkg.close();
		
		ser_pos.close();
		ser_neg.close();		
	}


	public void train() throws IOException, InterruptedException, URISyntaxException
	{
		ILPExec ilp_exec = new ILPExec(workingDirectory, projectName);
		ilp_exec.startILPProcess();
		ilp_exec.startReaderThreads("train");
		ilp_exec.induceAndWriteRules();
		ilp_exec.close();
		
	
		//Rule serialization
		//TODO: missing support for overlap dependencies
		RulesSerializer rs = new RulesSerializer(workingDirectory, projectName);
		Utils.mkdirsIfNotExists(workingDirectory + "/../rules/");
		rs.setOutputRulesFileName("../rules/" + projectName + ProjectSetup.makeTimeStamp() +"_rules.owl");
		rs.setOntologyURIFromOutpuRulesFileNameAndWorkingDir();

//		String[] object_props = {"'lex.rf'", "tDependency"};
		String[] object_props = new String[treeDepRels.size() + one2oneDepRels.size()];
		for (int a=0; a<treeDepRels.size(); a++)
		{
			object_props[a] = treeDepRels.get(a).getName();			
		}

		for (int a=0; a<one2oneDepRels.size(); a++)
		{
			object_props[treeDepRels.size() + a] = one2oneDepRels.get(a).getName();			
		}
		
		
		rs.serializeToSwrlx(object_props);
		rs.close();

		
		
	}


	public void closeBackgroundSerializer()
	{
		ser_bkg.close();
	}

	public void setBackgroundSerializerFileName(String fileName) throws FileNotFoundException, UnsupportedEncodingException
	{		
		ser_bkg.setOutput(workingDirectory.getAbsolutePath() + '/' + fileName);		
	}


	public Collection<String>[] classifyInstances(String[] instancesIds, String backgroundFileName, String targetRelationName) throws IOException, InterruptedException, URISyntaxException
	{
		@SuppressWarnings("unchecked")
		Collection<String> [] ret = new HashSet[instancesIds.length];
		
		ILPExec test = new ILPExec(workingDirectory, projectName);
		test.initBeforeApplyRules(backgroundFileName, "classify");
		
		for (int i = 0; i < instancesIds.length; i++)
		{
			ByteArrayOutputStream str_ser = new ByteArrayOutputStream();
			putExampleInlineWithoutClassValueCheck(
					new Serializer(str_ser),
					instancesIds[i],
					Serializer.encodeRelationName(targetRelationName),
					"ClassValueVar");
			
			
			List<String> inst = test.applyRulesReadVarOrFalse(str_ser.toString("utf8"), "ClassValueVar");
			
			//make class values unique
			ret[i] = new HashSet<String>(inst);
//			ret[i] = test.applyRulesTrueFalse(testExpession.toString());
		}
				
		test.close();				
		return ret;
	}


	public void putLearningSettings(String learningSettings)
	{
		ser_bkg.putLearningSettings(learningSettings);
	}

}
