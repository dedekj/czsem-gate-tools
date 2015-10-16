package czsem.gate.treex;

import java.io.File;

import org.apache.xmlrpc.XmlRpcException;

import czsem.gate.utils.Config;
import czsem.utils.AbstractConfig.ConfigLoadException;

@SuppressWarnings("serial")
public abstract class TreexAnalyserXmlRpc extends TreexAnalyserBase {
	
	protected String getHandshakeCode() {
		try {
			return getServerConnection().handshake();
		} catch (XmlRpcException e) {
			return "#default";
		}
	}

	protected void initScenario() throws TreexException
	{
		getServerConnection().initScenario(getLanguageCode(), getScenarioString());
		
		if (getVerifyOnInit()) {
			Object ret = getServerConnection().analyzeText("robot");
			if (ret == null) throw new NullPointerException("Server returned null response!");
		}
	}
	
	public static String constructErrLogPath(String handshakeCode) {
		return constructLogPathPrefix(handshakeCode)+ "_err.log";
	}

	public static String constructStdLogPath(String handshakeCode) {
		return constructLogPathPrefix(handshakeCode)+ "_std.log";
	}

	public static String constructLogPathPrefix(String handshakeCode) {
		String prefix; 
		try {
			prefix = Config.getConfig().getLogFileDirectoryPathExisting() + "/";
		} catch (ConfigLoadException e) {
			prefix = ""; 
		}
		
		return new File(prefix + "TREEX_" + handshakeCode).getAbsolutePath();
	}

	@Override
	protected TreexServerConnectionXmlRpc getServerConnection() {
		return (TreexServerConnectionXmlRpc) super.getServerConnection();
	}

}
