package czsem.gate.utils;

import gate.Gate;

import java.io.File;

import czsem.utils.AbstractConfig;

public class CzsemConfig extends AbstractConfig
{
	
	public static CzsemConfig getConfig() throws ConfigLoadException {
		return new CzsemConfig().getSingleton();
	}

	public String getGateHome() {
		return get("gateHome");
	}
	
	public void setGateHome()
	{
		if (Gate.getGateHome() == null)
			Gate.setGateHome(new File(getGateHome()));
	}

	@Override
	public String getConfigKey() {
		return "czsem_config";
	}

	@Override
	public void loadConfig() throws ConfigLoadException {
		if (Gate.isInitialised()) {
			loadConfig(Gate.getClassLoader());
		} else {
			super.loadConfig();
		}
	}
}
