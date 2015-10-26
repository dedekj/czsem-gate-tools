/*******************************************************************************
 * Copyright (c) 2015 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfig {

	public static String CONFIG_DIR_ENVP = "CZSEM_CONFIG_DIR";
	
	private static final Map<String,AbstractConfig> all_config_singletons = new HashMap<String, AbstractConfig>();

	public abstract String getConfigKey();
	
	protected static synchronized <E extends AbstractConfig> E getSingleton(E instance) throws ConfigLoadException {
		if (all_config_singletons.containsKey(instance.getConfigKey())) {
			
			@SuppressWarnings("unchecked")
			E old = (E) all_config_singletons.get(instance.getConfigKey());
			
			return old ;
		}
		
		instance.loadConfig();
		
		all_config_singletons.put(instance.getConfigKey(), instance);
		
		return instance;
	}
	
	protected void loadConfig() throws ConfigLoadException {
		//TODO
	}

	
	public static class ConfigLoadException extends IOException {
		private static final long serialVersionUID = -4928216820188606658L;
		
	}

}
