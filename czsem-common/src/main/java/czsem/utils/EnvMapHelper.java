package czsem.utils;

import java.util.Map;

public class EnvMapHelper {

	protected Map<String, String> env;

	public EnvMapHelper(Map<String, String> environment) {
		env = environment;
	}

	public void append(String key, String suffix) {
		String val = env.get(key);
		
		if (val == null) 
			val = suffix; 
		else 
			val = val + suffix;
		
		env.put(key, val);		
	}

	public void setIfEmpty(String key, String value) {
		if (env.get(key) == null) env.put(key, value);		
	}

}
