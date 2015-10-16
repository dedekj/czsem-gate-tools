package czsem.gate.plugins;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;

import czsem.gate.treex.TreexAnalyserXmlRpc;
import czsem.gate.treex.TreexException;
import czsem.gate.treex.TreexServerConnectionXmlRpc;

@CreoleResource(name = "czsem TreexRemoteAnalyser", comment = "Analyzes givem corpus by Treex remote (or local) server.", 
	helpURL="https://czsem-suite.atlassian.net/wiki/display/DOC/Treex+GATE+Plugin")
public class TreexRemoteAnalyser extends TreexAnalyserXmlRpc {	
	
	private static final long serialVersionUID = -5182317059444320543L;

	private boolean resetServerScenario;
	private boolean terminateServerOnCleanup;
	private URL treexServerUrl;
	
	@Override
	public void cleanup() {
		if (getTerminateServerOnCleanup() && getServerConnection() != null)
		{
			getServerConnection().terminateServer();			
		}
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		setScenarioString(computeScenarioString());
		
		setServerConnection(new TreexServerConnectionXmlRpc(getTreexServerUrl()));
		
		try {

			if (getResetServerScenario() || ! getServerConnection().isScenarioInitialized())
			{
				String lang = getLanguageCode();
				String scenString = getScenarioString();
				if (
						lang != null && ! lang.trim().isEmpty() && 
						scenString != null && ! scenString.trim().isEmpty())
				{
					initScenario();
				}
			}
		} catch (TreexException e) {
			setServerConnection(null);
			throw formatInitException(e);
		}
		
		return super.init();
	}

	
	
	@CreoleParameter(comment="The remote Treex server can already have an initialized scenario ready, do you want to replace it with the current one?",	
			defaultValue="true")			
	public void setResetServerScenario(Boolean resetServerScenario) {
		this.resetServerScenario = resetServerScenario;
	}

	public Boolean getResetServerScenario() {
		return resetServerScenario;
	}


	@CreoleParameter(defaultValue="true")			
	public void setTerminateServerOnCleanup(Boolean terminateServerOnCleanup) {
		this.terminateServerOnCleanup = terminateServerOnCleanup;
	}

	public Boolean getTerminateServerOnCleanup() {
		return terminateServerOnCleanup;
	}

	@CreoleParameter(defaultValue="http://localhost:9090")			
	public void setTreexServerUrl(URL treexServerUrl) {
		this.treexServerUrl = treexServerUrl;
	}

	public URL getTreexServerUrl() {
		return treexServerUrl;
	}
}
