package czsem.gate.utils;

import gate.Gate;
import gate.util.GateException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

public class RegisterGatePluginDirectories {
	
	public static void main(String[] args) throws IOException, URISyntaxException, GateException {
		GateUtils.initGateKeepLog();
		
		//work in current directory
		if (args.length > 0)
			addPluginsinDirectories(args[0]);
		else
			addPluginsinDirectories(System.getProperty("user.dir"));
		
		Gate.writeUserConfig();
	}

	public static void addPluginsinDirectories(String dir) throws MalformedURLException {
		File[] list = new File(dir).listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (int i = 0; i < list.length; i++) {
			if (new File(list[i], "creole.xml").exists())
			{
				System.err.format("Registering plugin directory: %s\n", list[i]);
				Gate.addKnownPlugin(list[i].toURI().toURL());
			}
		}
	}


}
