package czsem.gate.utils;

import gate.Gate;
import gate.util.GateException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class AddGatePluginDirectory {

	public static void main(String[] args) throws IOException, URISyntaxException, GateException {
		if (args.length < 1	|| args[0] == null || args[0].equals(""))
		{
			System.err.println("Use me with one single argument: <path to new Gate plugin directory>");
			return;
		}
		
		GateUtils.initGateKeepLog();
		Gate.addKnownPlugin(new File(args[0]).toURI().toURL());
		
		Gate.writeUserConfig();
	}

}
