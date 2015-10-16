package czsem.ILP;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import czsem.utils.Config;

public class RulesSerializer extends ILPExec
{
	private String output_rules_file_name;
	protected String ontologyURI = null;
	
	
	public RulesSerializer(File working_directory, String project_name)
	{super(working_directory, project_name);}
	
	public void serializeToSwrlx(String [] objectProperties) throws IOException, URISyntaxException
	{
		startPrologProcess(Config.getConfig().getPrologRuleXmlSerializer());
		startReaderThreads("RulesSerializer");
//		startStdoutReaderThreads();
		
		setUtf8Encoding();
		
		callRuleSrialization(getRulesFileName(), getOutputRulesFileName(), getOntologyURI(), objectProperties);

		
	}
	
	private void callRuleSrialization(
			String inputRulesFileName, 
			String outputRulesFileName, 
			String ontologyURI, 
			String[] objectProperties)
	{
		output_writer.print("serialize_rule_file('");		
		output_writer.print(inputRulesFileName);		
		output_writer.print("','");		
		output_writer.print(outputRulesFileName);		
		output_writer.print("','");		
		output_writer.print(ontologyURI);	 //http://czsem.berlios.de/ontologies	
		output_writer.print("',[");
		
		for (int i = 0; i < objectProperties.length; i++)
		{
			output_writer.print(objectProperties[i]);
			if (i < objectProperties.length-1)
				output_writer.print(',');
		} 
		
		output_writer.println("]).");		
		output_writer.flush();				
	}

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException
	{
		RulesSerializer rs = new RulesSerializer(				
				new File(Config.getConfig().getCzsemResourcesDir() + 
						"/Gate/learning/czech_fireman/savedFiles"), "RulesSerializer");
//		rs.setRulesFileName("acquisitions-v1.1/rules/learned_rules");
		rs.setRulesFileName("learned_rules");
		rs.setOutputRulesFileName("../rules/learned_rules_test1.owl");
		rs.setOntologyURIFromOutpuRulesFileNameAndWorkingDir();
		String[] object_props = {"'lex.rf'", "tDependency"};
		rs.serializeToSwrlx(object_props);
		rs.close();

	}

	public void setOutputRulesFileName(String output_rules_file_name) {
		this.output_rules_file_name = output_rules_file_name;
	}

	public String getOutputRulesFileName() {
		return output_rules_file_name;
	}
	
	public void setOntologyURI(String uri)
	{
		ontologyURI = uri;		
	}

	public void setOntologyURIFromOutpuRulesFileNameAndWorkingDir()
	{
		String dir_name = working_directory.getAbsoluteFile().getParentFile().getName();
		ontologyURI = 
			"http://czsem.berlios.de/ontologies/" + 
			dir_name + 
			"/rules/" + 
			new File(getOutputRulesFileName()).getName();		
	}


	public String getOntologyURI()
	{
		return ontologyURI ;
	}


}
