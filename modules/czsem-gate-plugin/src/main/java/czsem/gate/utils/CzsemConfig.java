package czsem.gate.utils;

//import gate.Gate;

import czsem.utils.AbstractConfig;

public class CzsemConfig extends AbstractConfig
{
	
	public static CzsemConfig getConfig() throws ConfigLoadException {
		return new CzsemConfig().getSingleton();
	}

	public String getGateHome() {
		return get("gateHome");
	}
	
	@Override
	protected void updateDefaults() {
		setDefaultVal("gateHome", null);
	}

	
	@Override
	public String getConfigKey() {
		return "czsem_config";
	}

	@Override
	public void loadConfig() throws ConfigLoadException {
		try {
			Class.forName("gate.Gate");
			loadConfig(GateUtils.getGateClassLoader());
			updateDefaults();
		} catch (ClassNotFoundException e) {
			super.loadConfig();
		}
	}
}
