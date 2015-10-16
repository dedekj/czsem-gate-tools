package czsem.gate.treex.factory;

import czsem.gate.plugins.TreexLocalAnalyser;
import czsem.gate.treex.TreexException;
import czsem.gate.treex.TreexServerConnection;
import czsem.gate.treex.TreexServerConnectionXmlRpc;

public class TreexLocalAnalyserFactory implements TreexCloudFactoryInterface {
	
	private TreexPortPool treexPortPool = new TreexPortPool();

	@Override
	public TreexServerConnection prepareTreexServerConnection (
			String languageCode, String scenarioString) throws TreexException
	{
		TreexLocalAnalyser tla = new TreexLocalAnalyser();
		tla.setLanguageCode(languageCode);
		tla.setScenarioString(scenarioString);
		tla.setServerPortNumber(treexPortPool.getNextTreexPort());
		TreexServerConnectionXmlRpc conn = tla.initConnection();
		conn.setTerminateOnClose(true);
		return conn;
	}

}

