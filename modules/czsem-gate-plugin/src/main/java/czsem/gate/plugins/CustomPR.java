package czsem.gate.plugins;

import gate.Corpus;
import gate.Document;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import czsem.gate.utils.PRSetup;

@CreoleResource(name = "czsem CustomPR", comment = "Usable within GATE embeded only.")
public class CustomPR extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = -1412485629846167332L;
	private AnalyzeDocDelegate executionDelegate;
	//private DataSet ds;
	private List<PRSetup> preprocess = new ArrayList<PRSetup>();
	
	public interface AnalyzeDocDelegate
	{
		public void analyzeDoc(Document doc) throws Exception;
	}
	
	@Override
	public void execute() throws ExecutionException
	{
		if (getExecutionDelegate() == null) throw new ExecutionException("executionDelegate is null", new NullPointerException()); 
		try {
			getExecutionDelegate().analyzeDoc(getDocument());
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	public void executeAnalysis(Corpus corpusParam) throws ResourceInstantiationException, ExecutionException
	{
		
		
		SerialAnalyserController a = PRSetup.buildGatePipeline(preprocess, "CustomPR pipeline"); 
		
		a.add(this);
		
		a.setCorpus(corpusParam);
		a.execute();
	}
    
	public static CustomPR createInstance(AnalyzeDocDelegate delegate) throws ResourceInstantiationException {
		CustomPR ret = (CustomPR) new PRSetup.SinglePRSetup(CustomPR.class).createPR();
		ret.setExecutionDelegate(delegate);
		return ret;		
	}

	public void setPreprocess(PRSetup ... preprocess) {
		setPreprocessList(Arrays.asList(preprocess));
	}

	public void setPreprocessList(List<PRSetup> preprocess) {
		this.preprocess = preprocess;
	}

	@Optional
	@RunTime
	@CreoleParameter
	public void setExecutionDelegate(AnalyzeDocDelegate executionDelegate) {
		this.executionDelegate = executionDelegate;
	}

	public AnalyzeDocDelegate getExecutionDelegate() {
		return executionDelegate;
	}



}
