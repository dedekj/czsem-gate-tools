package czsem.gate.treex.factory;

import czsem.gate.plugins.TreexLocalAnalyser;
import czsem.gate.treex.TreexException;
import czsem.gate.treex.TreexServerConnection;
import czsem.gate.treex.TreexServerConnectionXmlRpc;

public class TreexLocalAnalyserFactory implements TreexCloudFactoryInterface {
	
	private TreexPortPool treexPortPool = new TreexPortPool();
	
	private String[] cmdArray = {
			/*
			"docker",
			"run",
			"--rm",
			"-w=/app/czsem/treex-gate-plugin/treex_online/",
			"-p",
			"${port}:${port}",
			"datlowe/treex",
			"perl",
			"treex_online.pl",
			"${port}",
			"${handshakeCode}"			
			/**/
			"perl", 
			"${treexOnlineDir}/treex_online.pl",
			"${port}",
			"${handshakeCode}"
			/**/
	};


	@Override
	public TreexServerConnection prepareTreexServerConnection (
			String languageCode, String scenarioString) throws TreexException
	{
		TreexLocalAnalyser tla = new TreexLocalAnalyser();
		tla.setLanguageCode(languageCode);
		tla.setScenarioString(scenarioString);
		tla.setServerPortNumber(treexPortPool.getNextTreexPort());
		tla.setCmdArray(getCmdArray());
		TreexServerConnectionXmlRpc conn = tla.initConnection();
		conn.setTerminateOnClose(true);
		return conn;
	}


	public String[] getCmdArray() {
		return cmdArray;
	}


	public void setCmdArray(String[] cmdArray) {
		this.cmdArray = cmdArray;
	}
}

