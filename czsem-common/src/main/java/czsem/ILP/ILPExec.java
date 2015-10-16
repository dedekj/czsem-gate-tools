package czsem.ILP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import czsem.utils.Config;
import czsem.utils.ProcessExec;
import czsem.utils.ProjectSetup;

public class ILPExec extends ProcessExec {

	protected File working_directory;
	protected String project_name;
	protected String learnig_examples;
	protected String testing_examples;
	private String rules_file = "learned_rules";
	private String results_file = "test_results";

	public ILPExec(File working_directory, String project_name)
	{
		this.working_directory = working_directory;
		this.project_name = project_name;
		learnig_examples = project_name;
		testing_examples = project_name + "_test";
		rules_file = renderRulesFileName(project_name);
		
		new File(working_directory.getAbsolutePath() + "/log").mkdir();
	}
	
	public ILPExec(ProjectSetup ps)
	{
		this(ps.working_directory, ps.project_name);
	}
	
	public static String renderRulesFileName(String name) {
		return  name + ".rules";
	}

	public static String renderRulesFilePathName(ProjectSetup ps) {
		return  ps.renderProjectFileName(".rules");
	}
		
	protected String makeLogPathPrefix(String log_name)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(working_directory.getAbsolutePath());
		sb.append("/log/");
		sb.append(log_name);
		sb.append('_');
		sb.append(ProjectSetup.makeTimeStamp());
		sb.append('_');
		return 	sb.toString();	
	}

	public void startErrReaderThread(String log_name) throws FileNotFoundException
	{
		String path_prefix = makeLogPathPrefix(log_name);		
		startErrReaderThread(
				new FileOutputStream(path_prefix + "err.log"));				
	}

	public void startReaderThreads(String log_name) throws FileNotFoundException
	{
		
		String path_prefix = makeLogPathPrefix(log_name);		
		super.startReaderThreads(path_prefix);
	}



	public void startPrologProcess() throws IOException, URISyntaxException
	{
		String [] exec_args = {Config.getConfig().getPrologPath()};
		
		startPrologProcess(exec_args);		
	}

	public void startPrologProcess(String file_name_to_consult) throws IOException, URISyntaxException
	{
		String [] exec_args = {Config.getConfig().getPrologPath(), "-l", file_name_to_consult};
		
		startPrologProcess(exec_args);		
	}

	
	public void startPrologProcess(String[] exec_args) throws IOException, URISyntaxException
	{
		
//		System.err.println(Config.getConfig().getPrologPath());
//		System.err.println(file_name_to_consult);
		
		exec(exec_args, working_directory);
		
/*
		process = Runtime.getRuntime().exec(exec_args, null, working_directory);
//				new String [] {"LANG=cs_CZ.UTF-8"} , working_directory);
		
		output_writer = new PrintWriter(new BufferedOutputStream(process.getOutputStream()));
		input_reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		error_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
*/				
	}

	public void startILPProcess() throws IOException, URISyntaxException
	{
		startPrologProcess(Config.getConfig().getAlephPath());
	}
		
	public void induceAndWriteRules()
	{
		setUtf8Encoding();
		
		output_writer.print("read_all('");
		output_writer.print(project_name);
		output_writer.print("','");
		output_writer.print(learnig_examples);
		output_writer.println("').");
		output_writer.flush();
				
		output_writer.println("induce.");		
		output_writer.flush();
		
		
		output_writer.println("prolog_flag(user_error, E),  set_output(E), show(theory).\n");		
		output_writer.println("open('theory.log', write, S),  set_output(S), show(theory), close(S).\n");		

		output_writer.print("write_rules('");		
		output_writer.print(getRulesFileName());		
		output_writer.println("').");		

		output_writer.flush();		

	}

	public void retractTestRule(String rule)
	{
		output_writer.print("retract((");		
		output_writer.print(rule);		
		output_writer.println(")).");		
		output_writer.flush();		
		output_writer.println();
		output_writer.flush();				
	}

	public void consultFile(String filename)
	{
		output_writer.print("consult('");		
		output_writer.print(filename);		
		output_writer.println("').");		
		output_writer.flush();		
	}

	
	public void addTestRule(String rule)
	{
		output_writer.print("assert((");		
		output_writer.print(rule);		
		output_writer.println(")).");		
		output_writer.flush();		
		output_writer.println();
		output_writer.flush();		
	}
	
	public void testRules(boolean showCoverage) throws IOException, InterruptedException	
	{
		String true_positives; 
		String false_positives; 
		String total_positives; 
		String total_negatives; 
		
		String show_flag = "noshow";
		if (showCoverage) show_flag = "show";
		
		cin_reader_thread.waitUntilNothingToRead();
		output_writer.print("test('");
		output_writer.print(testing_examples);
		output_writer.print(".f',");
		output_writer.print(show_flag);
		output_writer.println(",Covered,Total),print(Covered),print('\\n'),print(Total),print('\\n').");
		output_writer.flush();		
		cin_reader_thread.waitForInput();
		true_positives = cin_reader_thread.readLine();
		total_positives = cin_reader_thread.readLine();
		output_writer.println();
		output_writer.flush();


		cin_reader_thread.waitUntilNothingToRead();
		output_writer.print("test('");
		output_writer.print(testing_examples);
		output_writer.print(".n',");
		output_writer.print(show_flag);
		output_writer.println(",Covered,Total),print(Covered),print('\\n'),print(Total),print('\\n').");
		output_writer.flush();		
		cin_reader_thread.waitForInput();
		false_positives = cin_reader_thread.readLine();
		total_negatives = cin_reader_thread.readLine();
		output_writer.println();
		output_writer.flush();
		
		PrintWriter test_out = new PrintWriter(new FileOutputStream(working_directory + "/" + getResultsFileName(), true));
		test_out.print(true_positives); 
		test_out.print('\t'); 
		test_out.print(false_positives); 
		test_out.print('\t'); 
		test_out.print(total_positives); 
		test_out.print('\t'); 
		test_out.print(total_negatives); 
		test_out.println();
		test_out.close();
	}

	public void close() throws InterruptedException
	{
		/**/
		output_writer.println("halt.");
		output_writer.flush();

//		System.err.println("Waiting for ILP...");
			
		process.waitFor();
//		System.err.println(prolog_proc.exitValue());		
	}

	public String getLearnigExamples() {
		return learnig_examples;
	}


	public void setLearnigExamples(String learnigExamples) {
		learnig_examples = learnigExamples;
	}


	public String getTestingExamples() {
		return testing_examples;
	}


	public void setTestingExamples(String testingExamples) {
		testing_examples = testingExamples;
	}


	public void setRulesFileName(String rules_file) {
		this.rules_file = rules_file;
	}

	
	public String getRulesFileName() {
		return rules_file;
	}


	public void setResultsFileName(String results_file) {
		this.results_file = results_file;
	}


	public String getResultsFileName() {
		return results_file;
	}

	
	/**
	 * @return "true" or "false"
	 * @throws IOException
	 */
	public String applyRulesTrueFalse(String testExpession) throws IOException
	{
		output_writer.print("if(");
		output_writer.print(testExpession);
		output_writer.println(",print(true),print(false)),print('\\n').");
		output_writer.flush();
		
		return input_reader.readLine();
	}
	
	/**
	 * @return all possible variable values
	 * @throws IOException
	 */
	public List<String> applyRulesReadVarOrFalse(String testExpession, String variableName) throws IOException
	{
		output_writer.print("(");
		output_writer.print(testExpession);
		output_writer.print(",print(");
		output_writer.print(variableName);
		output_writer.print("),print('\\n')");
		output_writer.print(";");
		output_writer.print("print('nulll\\n')");
		output_writer.println("),fail.");
		output_writer.flush();
		
		List<String> ret = new ArrayList<String>(0);
		
		for (;;)
		{
			String ln = input_reader.readLine();
			
			if (ln.equals("nulll")) break;
			
			ret.add(ln);			
		}

				
		return ret;
	}


	public void initBeforeApplyRules(String backgroundFileName, String errLogName) throws IOException, URISyntaxException
	{
		startPrologProcess();
		startErrReaderThread(errLogName);
		setUtf8Encoding();
		consultFile(getRulesFileName());				
		consultFile(backgroundFileName);				
	}


	public OutputStream getOutputStream() {
		return process.getOutputStream();
	}
	

	public void setUtf8Encoding() {
		output_writer.println("set_prolog_flag(encoding,utf8).");		
	}


	/*
	public static void main(String[] args) throws InterruptedException, IOException
	{
		ReaderThread rd = new ReaderThread(new InputStreamReader(System.in), System.out);
		System.out.println("created");
		rd.start();
		System.out.println("run");
		
		Thread.sleep(4000);
		
		System.out.println("waitng");
		String s = rd.readLine();
		System.out.println("***"+s+"***");
		
		System.out.println("end");				
	}
	*/

}
