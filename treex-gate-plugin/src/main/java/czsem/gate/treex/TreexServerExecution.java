package czsem.gate.treex;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import czsem.Utils;
import czsem.utils.EnvMapHelper;
import czsem.utils.FirstOfTwoTasksKillsTheSecond;
import czsem.utils.FirstOfTwoTasksKillsTheSecond.HandShakeResult;
import czsem.utils.FirstOfTwoTasksKillsTheSecond.Task;
import czsem.utils.ProcessExec;

public class TreexServerExecution {
	
	static Logger logger = Logger.getLogger(TreexServerExecution.class);
	
	public static enum RedirectionType { LOG_FILES_APPEND, LOG_FILES_REPLACE, COPY_TO_STD, INHERIT_IO };
	
	protected static class TreexHandShake
	{

		protected Process process;
		protected TreexServerConnectionXmlRpc connection;
		protected String handshake_code;
		protected String handshake_return = null;


		public TreexHandShake(Process process, TreexServerConnectionXmlRpc connection, String handshake_code) {
			this.process = process;
			this.connection = connection;
			this.handshake_code = handshake_code;
		}
		
		protected boolean doServerHandShake() throws InterruptedException {
			for (long sleep = 1 ;; sleep *= 2) {
				try {
					
					handshake_return = connection.handshake();
//					System.err.println(sleep);
					return handshake_code.equals(handshake_return);
					
				} catch (XmlRpcException e) {
					
					Thread.sleep(sleep);
					continue;
					
				}
			}			
		}

		public void doHandShake() throws IOException {
			Task<HandShakeResult> taskHandShake = new Task<HandShakeResult>() {
				@Override
				public HandShakeResult run() throws InterruptedException, XmlRpcException, IOException {
					return 
						doServerHandShake()
							? HandShakeResult.HandShakeOK
							: HandShakeResult.HandShakeKO; 
				}
			};
			Task<HandShakeResult> taskWaitProc = new Task<HandShakeResult>() {
				@Override
				public HandShakeResult run() throws InterruptedException, XmlRpcException, IOException {
					process.waitFor();
					return HandShakeResult.ProcessTerminated;
				}
			};
			
			FirstOfTwoTasksKillsTheSecond<HandShakeResult> tt
				= new FirstOfTwoTasksKillsTheSecond<HandShakeResult>(
						taskHandShake, taskWaitProc);  

			HandShakeResult result = null;
			Exception e = null;
			
			try {
				result = tt.executeWithTimeout(0);
			} catch (Exception ee) {
				e = ee;
				result = HandShakeResult.ProcessTerminated;
			}

			if (result == null) result = HandShakeResult.TimeOut;
			
			if (result != HandShakeResult.HandShakeOK)			
			{
				if (ProcessExec.isProcessRunning(process)) 
					process.destroy();
				
				switch (result) {
				case HandShakeKO:
					throw new IOException(
							String.format("Handshake with Treex server failed!\n" +
									"Another server already running on the same port?\n" +
									"Expected hash: '%s'\n" +
									"Returned hash: '%s'", handshake_code, handshake_return));			
				case ProcessTerminated:
					throw new IOException("An exception raised during Treex server init.", e);			
				case TimeOut:
					throw new IOException("Treex server run out of time dutring start up.");
				default:
					throw new IOException("This sould be impossible to  happen :-)");			
				}		
				
			}
			
		}
	}

	private int portNumber = 9090;
	protected RedirectionType redirectionType = RedirectionType.LOG_FILES_REPLACE;
	protected Process process;
	protected String handshakeCode = Long.toHexString(new Random().nextLong());


	public String getHandshakeCode() {
		return handshakeCode;
	}


	public void setHandshakeCode(String handshakeCode) {
		this.handshakeCode = handshakeCode;
	}
	
	public String getLogPath() {
		switch (getRedirectionType()) {
			case LOG_FILES_APPEND:
			case LOG_FILES_REPLACE:
				return TreexAnalyserXmlRpc.constructErrLogPath(handshakeCode);
			default:
				return "n/a";
		}
	}


	public void start() throws TreexException {
		try {
			startWithoutHandshake();
			doHandshake(handshakeCode);
		} catch (IOException e) {

			TreexException treexException = new TreexException(e);
			treexException.setLogPath(getLogPath());
			throw treexException;
		}
		

	}

	
	public void start(String[] cmdarray) throws Exception {
		startWithoutHandshake(cmdarray);
		
		doHandshake(handshakeCode);

		logger.info(String.format("Treex server succsefuly started! port: %d handshake code: %s", getPortNumber(),  handshakeCode));
	}
	
	public void startWithoutHandshake() throws IOException {
		String[] cmdarray = {
				"perl", 
				TreexConfig.getConfig().getTreexOnlineDir()+"/treex_online.pl",
				Integer.toString(getPortNumber()),
				handshakeCode};
		
		startWithoutHandshake(cmdarray);		
	}


	
	public void startWithoutHandshake(String[] cmdarray) throws IOException {
		if (! Utils.portAvailable(getPortNumber()))
		{
			throw new IOException("Filed to start Treex server, port nuber: "+getPortNumber()+" is not available.");						
		}
		
		String path_sep = System.getProperty( "path.separator" );
		
		TreexConfig cfg = TreexConfig.getConfig();
		

/*		
		String[] env = {
				"PERL5LIB="+cfg.getCzsemResourcesDir()+"/Treex" + path_sep +
				cfg.getTreexDir() + "/lib" + path_sep +
				cfg.getTreexDir() + "/oldlib",
				"SystemRoot="+System.getenv("SystemRoot"),
				"Path="+System.getenv("Path"),
				"TMT_ROOT="+cfg.getTmtRoot(),
				"JAVA_HOME="+System.getProperty("java.home"),};
*/				
//				Map<String, String> env2 = System.getenv();
		
//		String[] env3 = getTredEnvp();
		
		
		ProcessBuilder pb = new ProcessBuilder(cmdarray);
		File treexDir = new File(cfg.getTreexDir());
		pb.directory(treexDir);
		EnvMapHelper eh = new EnvMapHelper(pb.environment());
		eh.append("PERL5LIB", path_sep + cfg.getTreexOnlineDir()); 
		eh.append("PERL5LIB", path_sep + cfg.getTreexDir() + "/lib"); 
		eh.append("PERL5LIB", path_sep + cfg.getTreexDir() + "/oldlib");
		eh.append("PERL5LIB", path_sep + cfg.getTmtRoot() + "/libs/other");
		eh.append("PERL5LIB", path_sep + System.getProperty("user.home") + "/perl5/lib/perl5");
		eh.append("PERL5LIB", path_sep + System.getProperty("user.home") + "/perl5/lib/site_perl");
		//The architecture specific directories are being searched by perl automatically
		
		String tmt = cfg.getTmtRoot();
		if (! new File(tmt).exists()) {
			if (new File(System.getProperty("user.home")+"/.treex").exists())
				tmt = System.getProperty("user.home")+"/.treex";
			else
				tmt = treexDir.getParent();				
		}
		eh.setIfEmpty("TMT_ROOT", tmt);
		eh.setIfEmpty("JAVA_HOME", System.getProperty("java.home"));
		
		String treexConfigDir = cfg.getTreexConfigDir();
		if (treexConfigDir != null && ! treexConfigDir.isEmpty()) {
			eh.setIfEmpty("TREEX_CONFIG", treexConfigDir);
		}		
		
		switch (getRedirectionType()) {
		case COPY_TO_STD:
			
			
			process = pb.start();
			
			ProcessExec.createSimpleCopyThread(process.getInputStream(), System.out).start();
			ProcessExec.createSimpleCopyThread(process.getErrorStream(), System.err).start();
			
			//this is important - we have to return here to avoid duplicating the process
			return;
			
		case INHERIT_IO:
			pb.inheritIO();
			break;
		case LOG_FILES_REPLACE:
			pb.redirectError(new File(TreexAnalyserXmlRpc.constructErrLogPath(handshakeCode)));
			pb.redirectOutput(new File(TreexAnalyserXmlRpc.constructStdLogPath(handshakeCode)));
			break;
		case LOG_FILES_APPEND:
			pb.redirectError(Redirect.appendTo(new File(TreexAnalyserXmlRpc.constructErrLogPath(handshakeCode))));
			pb.redirectOutput(Redirect.appendTo(new File(TreexAnalyserXmlRpc.constructStdLogPath(handshakeCode))));
			break;
		}

		process = pb.start();
	}
	
	public static void main (String args []) {
		for (Entry<Object, Object> p : System.getProperties().entrySet())
			System.err.println(p);
		System.err.println(System.getenv("PERL5LIB"));
	}
	
	public void doHandshake(String handshake_code) throws IOException {
		TreexHandShake th = new TreexHandShake(process, getConnection(), handshake_code);
		
		th.doHandShake();		
	}
	
	
	

	public TreexServerConnectionXmlRpc getConnection() {
		try {
			TreexServerConnectionXmlRpc conn = new TreexServerConnectionXmlRpc("localhost", getPortNumber());
			conn.setLogPath(getLogPath());
			return conn;
		
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public int getPortNumber() {
		return portNumber;
	}


	public void waitFor() throws InterruptedException {
		process.waitFor();
		
	}


	public RedirectionType getRedirectionType() {
		return redirectionType;
	}


	public void setRedirectionType(RedirectionType redirectionType) {
		this.redirectionType = redirectionType;
	}
}
