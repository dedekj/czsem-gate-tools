package czsem.gate.treex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.serializer.XmlWriterFactory;

import czsem.gate.treex.xmlwriter.TreexXmlWriterFactory;

public class TreexServerConnectionXmlRpc implements TreexServerConnection {
	static Logger logger = Logger.getLogger(TreexServerConnectionXmlRpc.class); 

	XmlRpcClient rpcClient;

	private boolean terminateOnClose = false;
	
	private String logPath = "n/a";

	public TreexServerConnectionXmlRpc(String hostname, int portNumber) throws MalformedURLException {
		this(new URL("http", hostname, portNumber, "/RPC2"));
	}

	public TreexServerConnectionXmlRpc(URL treexServerUrl) {
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(treexServerUrl);
			
			rpcClient = new XmlRpcClient();
			rpcClient.setConfig(config);
			
			XmlWriterFactory factory = new TreexXmlWriterFactory();			
			rpcClient.setXmlWriterFactory(factory);
		} 
		catch (IncompatibleClassChangeError e) {
			throw new RuntimeException(
					"Filed to start Treex server, due to IncompatibleClassChangeError, " +
					"this is usually caused by the presence of a different version of XML-RPC library, " +
					"e.g. if tecto-mt-gate-plugin is loaded in the same time...", e);						
		}
	}

	public void terminateServer() {
		logger.info("Sending termination request to Treex server url: " + getServerURL());
		
		String handshake = "";
		try {
			handshake = handshake();
			rpcClient.execute("treex.terminate", new Vector<Object>());
		} catch (XmlRpcException e) {
			logger.info(String.format("Treex server termination registered. url: %s handshake code: '%s'", 
					getServerURL(),	handshake));
		}		
	}

	public URL getServerURL() {
		return ((XmlRpcClientConfigImpl)rpcClient.getClientConfig()).getServerURL();
	}

	public Object encodeTreexFile(String treexFileName) throws XmlRpcException, IOException {
		Vector<String> params = new Vector<String>(1);
		params.add(treexFileName);
		Object ret = rpcClient.execute("treex.encodeDoc", params);

		return ret;
	}

	public void initScenario(String languageCode, String ... scenStrings) throws TreexException {
		String scenString = StringUtils.join(scenStrings, ' ');
		
		Vector<Object> params = new Vector<Object>(1);
		params.add(languageCode);
		params.add(scenString);
		
		try {
			rpcClient.execute("treex.initScenario", params);
		} catch (XmlRpcException e) {
			throw createTreexException(e);
		}		
	}

	public Object analyzeText(String text) throws TreexException {
		Vector<String> params = new Vector<String>(1);
		params.add(text);

		try {
			return rpcClient.execute("treex.analyzeText", params);
		} catch (XmlRpcException e) {
			throw createTreexException(e);
		}
	}

	public String handshake() throws XmlRpcException {
		Object ret = rpcClient.execute("treex.handshake", new Object[0]);
		return (String) ret;
	}

	@Override
	public Object analyzePreprocessedDoc(String docText, Map<String, Object>[] inputDocData) throws TreexException {
		Vector<Object> params = new Vector<Object>(2);
		params.add(docText);
		params.add(inputDocData);
		try {
			return rpcClient.execute("treex.analyzePreprocessedDoc", params);
		} catch (XmlRpcException e) {
			throw createTreexException(e);
		}
	}

	protected TreexException createTreexException(Exception cause) {
		TreexException ret = new TreexException(cause);
		ret.setLogPath(getLogPath());
		return ret;
	}

	public boolean isScenarioInitialized() throws TreexException {
		try {
			return (boolean) rpcClient.execute("treex.isScenarioInitialized", new Object[0]);
		} catch (XmlRpcException e) {
			throw createTreexException(e);
		}
	}

	@Override
	public void close() {
		if (terminateOnClose) terminateServer();
	}

	public boolean getTerminateOnClose() {
		return terminateOnClose;
	}

	public void setTerminateOnClose(boolean terminateOnClose) {
		this.terminateOnClose = terminateOnClose;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

}
