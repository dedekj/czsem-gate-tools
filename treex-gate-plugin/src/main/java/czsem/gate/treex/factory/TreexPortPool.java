package czsem.gate.treex.factory;

import czsem.Utils;

public class TreexPortPool {

	protected final int minPort;
	protected final int maxPort;
	protected int nextPort;

	public TreexPortPool() {
		this(7000, 7777);
	}
	
	public TreexPortPool(int minPort, int maxPort) {
		this.minPort = minPort;
		this.maxPort = maxPort;
		
		nextPort = minPort;
	}

	protected void increasePort() {
		nextPort++;
		if (nextPort > maxPort) nextPort = minPort;
	}

	public synchronized int getNextTreexPort() {
		
		while (! Utils.portAvailable(nextPort)) {
			increasePort();
		}
		
		int ret = nextPort;
		increasePort();
		
		return ret;
	}

	
}
