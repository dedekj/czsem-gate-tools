/*******************************************************************************
 * Copyright (c) 2015 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.gate.treex;

import java.io.File;

import czsem.utils.AbstractConfig;

public class TreexConfig extends AbstractConfig {

	public static TreexConfig getConfig() throws ConfigLoadException {
		return new TreexConfig().getSingleton();
	}

	@Override
	public String getConfigKey() {
		return "treex_gate_plugin_config";
	}
	
	public String getTreexOnlineDir() {
		String ret = get("treexOnlineDir");
		//TODO try guess
		return ret;
	}


	public String getLogFileDirectoryPathExisting() {
		String path = get("treexLogDir");
		
		if (path != null)
			new File(path).mkdirs();
		else {
			//TODO try create temp
		}
		
		return path;
	}

	public String getTreexDir() {
		return get("treexDir");
	}

	public String getTreexConfigDir() {
		return get("treexConfigDir");
	}
}
