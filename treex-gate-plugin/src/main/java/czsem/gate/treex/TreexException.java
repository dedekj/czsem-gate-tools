package czsem.gate.treex;

import java.io.IOException;

public class TreexException extends IOException {
	private static final long serialVersionUID = -3992130181991125854L;
	
	private String logPath = "n/a";

	public TreexException(Throwable cause) {
		super(cause);
	}

	public TreexException(String msg, String logPath) {
		super(msg);
		setLogPath(logPath);
	}

	public TreexException(String msg, String logPath, Throwable cause) {
		super(msg, cause);
		setLogPath(logPath);
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}


}
