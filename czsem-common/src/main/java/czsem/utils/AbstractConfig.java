package czsem.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.Utils;

public class AbstractConfig {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractConfig.class);
	
	protected static Map<String,Map<String, Object>> property_map = new HashMap<String, Map<String,Object>>();
	
	protected static String czsem_config_filename = "czsem_config.xml";
	protected static String config_dir = "configuration";
	
	public static String config_dir_envp = "CZSEM_CONFIG_DIR";
	
	public String getEnvOrSysProperty(String name) {
		return Utils.getEnvOrSysProperty(name);
	}

	
	public String getLoadedFrom() {
		return get("loadedFrom");
	}

	public void setLoadedFrom(String loadedFrom) {
		set("loadedFrom", loadedFrom);
	}

	protected void save() throws IOException {
		saveToFile(getDefaultLoc());		
	}

	public void updateLoadedConfigFile() throws IOException {
		saveToFile(getLoadedFrom());		
	}
	
	protected Map<String, Object> getMap() { return property_map.get(getConfigKey());}
	protected void setMap(Map<String, Object> map) { property_map.put(getConfigKey(), map);}

	protected String getConfigKey() { return "czsem_config";}
	
	protected void set(String key, Object value)
	{
		getMap().put(key, value);
	}

	protected String get(String key)
	{
		return (String) getMap().get(key);
	}

	protected Object getObj(String key)
	{
		return getMap().get(key);
	}
		
	public void saveToFile(String filename) throws IOException
	{
		// Create output stream.
		File f = new File(filename);
		logger.info("Saving config '"+getConfigKey()+"' to "+f.getAbsolutePath()+"...");
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(filename);

		// Create XML encoder.
		XMLEncoder xenc = new XMLEncoder(fos);

		// Write object.
		xenc.writeObject(getMap());
		xenc.flush();
		xenc.close();
		fos.close();
		
	}

	public static Map<String, Object> loadFromFile(String filename, ClassLoader classLoader) throws IOException
	{
		FileInputStream os = new FileInputStream(filename);
		XMLDecoder decoder = new XMLDecoder(os, null, null, classLoader);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) decoder.readObject();
		decoder.close();
		os.close();
		return map;
	}
	
	public void loadConfig(String filename, ClassLoader classLoader) throws IOException
	{
		setMap(loadFromFile(filename, classLoader));
		
		setLoadedFrom(filename);
		
		logger.info("Config '"+getConfigKey()+"' loaded from: "+new File(filename).getAbsolutePath());
	}

	protected synchronized AbstractConfig getAbstractConfig() throws ConfigLoadException {
		
		if (getMap() == null) 
			loadConfig();
		
		return this;
	}
	
	public void setMyWinValues() {};

	public void saveMyWinValues() throws IOException {
		initNullConfig();
		setMyWinValues();
		save();
		System.err.format("MyWinValues saved to '%s' !", getDefaultLoc());
	};

	
	public void initNullConfig() {
		setMap(new HashMap<String, Object>());
	}

	public void loadConfig(ClassLoader classLoader) throws ConfigLoadException
	{
		ConfigLoadException fe = new ConfigLoadException();
		
		//first: try env specific
		try {
			String env_key = getConfigKey().toUpperCase();
			String env = getEnvOrSysProperty(env_key);
			if (env == null) throw new EnvOrSysPropertyNotSetException(String.format("Environment property  '%s' not set.", env_key));
			loadConfig(env, classLoader);
			return;
		}
		catch (Exception e)	{fe.add(e);}

		//second: try env_dir
		try {
			String env_key = config_dir_envp;
			String env = getEnvOrSysProperty(env_key);
			if (env == null) throw new EnvOrSysPropertyNotSetException(String.format("Environment property  '%s' not set.", env_key));
			loadConfig(env + "/" + getConfigKey() + ".xml", classLoader);
			return;
		}
		catch (Exception e)	{fe.add(e);}

		//third: try default loc
		try {
			loadConfig(getDefaultLoc(), classLoader);				
			return;
		} catch (Exception e) {fe.add(e);}

		//fourth: try ../default loc
		try {
			loadConfig("../" +getDefaultLoc(), classLoader);			
			return;
		} catch (Exception e) {fe.add(e);}

		//fifth: try ../../default loc
		try {
			loadConfig("../../" +getDefaultLoc(), classLoader);			
			return;
		} catch (Exception e) {fe.add(e);}
		
		throw fe;
	}

	protected String getDefaultLoc() {
//		return "../" +config_dir+ '/' +czsem_config_filename;
		return "../" +config_dir+ '/' + getConfigKey() + ".xml";

	}
	
	public static class EnvOrSysPropertyNotSetException extends IOException {
		private static final long serialVersionUID = -2037380563367210974L;
		public EnvOrSysPropertyNotSetException(String msg) {
			super(msg);
		}
	};
	
	public static class ConfigLoadException extends FileNotFoundException
	{
		private static final long serialVersionUID = -5616178151757529473L;
		
		protected List<Exception> causes = new ArrayList<Exception>();
		
		public ConfigLoadException() {}

		public ConfigLoadException(Exception e) {
			add(e);
		}

		public void add(Exception e) {
			causes.add(e);
		}

		@Override
		public String getMessage() {
			StringBuilder sb = new StringBuilder(String.format("Configuration file could not be loaded for one of following (%d) reasons:\n", causes.size()));
			for (int a=0; a<causes.size(); a++)
			{
				sb.append(String.format("%d: %s\n", a+1, causes.get(a).toString()));
			}
			
			sb.append(String.format("Current working direcotry is: %s",  System.getProperty("user.dir")));
			
			return sb.toString();
		}
		
	}


	public void loadConfig() throws ConfigLoadException  {
		loadConfig(null);
	}



}
