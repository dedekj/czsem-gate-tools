package czsem.gate.treex;

import gate.Document;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;

@SuppressWarnings("serial")
public abstract class TreexAnalyserBase extends AbstractLanguageAnalyserWithInputOutputAS {
	private String languageCode;
	private List<String> scenarioSetup;
	private URL scenarioSetupFileToBeAppended;
	private boolean verifyOnInit = false;

	private String scenarioString = null;
	
	private TreexServerConnection serverConnection = null;
	
	@Optional
	@RunTime
	@Override
	@CreoleParameter(comment="Annotation set name from which sentences and tokens will be exported for Treex.", defaultValue="treex input AS")
	public void setInputASName(String inputASName) {
		super.setInputASName(inputASName);
	}

	
	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		
		try {
			
			TreexInputDocPrepare ip = new TreexInputDocPrepare(doc, getInputASName());
			Object treexRet = getServerConnection().analyzePreprocessedDoc(doc.getContent().toString(), ip.createInputDocData());
			TreexReturnAnalysis tra = new TreexReturnAnalysis(treexRet);
			tra.annotate(doc, getOutputASName());
			
		} catch (TreexException e) {
			throw new ExecutionException("Error occured during run of Treex server.\nSee remote server's output, or local server's log file: " + e.getLogPath(), e);
		} catch (InvalidOffsetException e) {
			throw new ExecutionException(e);
		}
	}
	
	public static ResourceInstantiationException formatInitException(TreexException cause) {
		return new ResourceInstantiationException(
				"Error occured during Treex server init.\nSee server's output in console, or server's log file: " + cause.getLogPath(), cause);
	}
	
	@CreoleParameter(
			comment="List of blocks to be used in the analysis. Each element can be either a Treex block or a .scen file.",
			defaultValue="W2A::CS::Segment;W2A::CS::Tokenize")
	public void setScenarioSetup(List<String> scenarioSetup) {
		this.scenarioSetup = scenarioSetup;
	}

	@CreoleParameter(comment="LangCode must be valid ISO 639-1 code. E.g. en, de, cs",	defaultValue="cs")			
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public String computeScenarioString() throws ResourceInstantiationException {
		return computeScenarioStringStatic(getScenarioSetup(), getScenarioSetupFileToBeAppended());
	}

	public static String computeScenarioStringStatic(List<String> scenarioSetup, URL scenarioSetupFileToBeAppended) throws ResourceInstantiationException {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.join(scenarioSetup, ' '));
		
		if (scenarioSetupFileToBeAppended != null) {
			String content;
			try {
				content = IOUtils.toString(scenarioSetupFileToBeAppended, "utf8");
			} catch (IOException e) {
				throw new ResourceInstantiationException(
						"Failed to load content of 'ScenarioSetupFileToBeAppended': "
								+ scenarioSetupFileToBeAppended, e);
			} 
			sb.append(' ');
			sb.append(content);
		}
		
		return sb.toString();
	}

	
	public List<String> getScenarioSetup() {
		return scenarioSetup;
	}

	public String getLanguageCode() {
		return languageCode;
	}


	public Boolean getVerifyOnInit() {
		return verifyOnInit;
	}


	@CreoleParameter(defaultValue="false")
	public void setVerifyOnInit(Boolean verifyOnInit) {
		this.verifyOnInit = verifyOnInit;
	}


	protected TreexServerConnection getServerConnection() {
		return serverConnection;
	}

	protected void setServerConnection(TreexServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}


	public URL getScenarioSetupFileToBeAppended() {
		return scenarioSetupFileToBeAppended;
	}


	@Optional
	@CreoleParameter(comment="Path to a scenario file whose contents will be appended to the sceanario setup.")
	public void setScenarioSetupFileToBeAppended(URL scenarioSetupFileToBeAppended) {
		this.scenarioSetupFileToBeAppended = scenarioSetupFileToBeAppended;
	}


	public String getScenarioString() {
		return scenarioString;
	}
	
	public void setScenarioString(String scenarioString) {
		this.scenarioString = scenarioString;
	}
}