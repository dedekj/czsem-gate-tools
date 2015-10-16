/*******************************************************************************
 * Copyright (c) 2015 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.gate.treex;

import czsem.utils.AbstractConfig;

public class TreexConfig extends AbstractConfig {

	public static TreexConfig getConfig() throws ConfigLoadException {
		return getSingleton(new TreexConfig());
	}

	@Override
	public String getConfigKey() {
		return "TREEX_GATE_PLUGIN_CONFIG";
	}
	
	public String getTreexOnlineDir() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLogFileDirectoryPathExisting() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTreexDir() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTreexConfigDir() {
		// TODO Auto-generated method stub
		return null;
	}
}
