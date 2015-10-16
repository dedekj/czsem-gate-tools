package czsem.gate.utils;

import gate.Gate;
import gate.util.GateException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import czsem.Utils;
import czsem.utils.AbstractConfig;


public class Config extends czsem.utils.Config
{
	public static ClassLoader classLoader = null;
	public static String czsem_plugin_dir_name = "czsem-gate-plugin";
	
	public static void main(String[] args) throws IOException, GateException, URISyntaxException
	{
		
		if (args.length <= 0)
		{
			Config ps = new Config();
			ps.initNullConfig();
			ps.setMyWinValues();
			ps.save();
			System.err.format("MyWinValues saved to '%s' !", ps.getDefaultLoc());
		} else {
			Config ps = new Config();
			ps.initNullConfig();
			ps.setMyWinValues();
			ps.setInstallDefaults();
			String target = args[0]+ '/' +AbstractConfig.czsem_config_filename;
			ps.saveToFile(target);
			System.err.format("InstallDefaults saved to '%s' !\n", target);
		}
		/*
		Config cfg = getConfig();
//		cfg.setMyWinValues();
		cfg.setGateHome();
		Gate.init();
	    czsem.gate.GateUtils.registerCzsemPlugin();;
	    System.err.println(getConfig().getGateHome());
		/**/

/**/
//		ps.setInstallDefaults();
//		ps.saveToFile(czsem_plugin_dir_name+ '/' +config_filename_install);
//		ps.saveToFile(czsem_plugin_dir_name+ '/' +config_filename);
		
/*				
		Config ps2 = new Config();
		ps2 = loadFromFile("config1.xml");
/**/				
	}


	protected static URL findCzesemPluginDirectoryURL()
	{
		Collection<URL> dirs = Gate.getKnownPlugins();
		for (Iterator<URL> iterator = dirs.iterator(); iterator.hasNext();)
		{
			URL url = iterator.next();
			//System.err.println(url);
			if (url.toString().endsWith(czsem_plugin_dir_name + '/'))
				return url;
		}
		return null;		
	}


	@Override
	public void loadConfig() throws ConfigLoadException 
	{
		if (Gate.isInitialised() && Config.classLoader == null)
			Config.classLoader = Gate.getClassLoader();
		
		loadConfig(classLoader);
	}

	@Override
	public void loadConfig(ClassLoader classLoader) throws ConfigLoadException
	{
		ConfigLoadException fe;
		try {
			super.loadConfig(classLoader);
			return;
		} catch (ConfigLoadException e) {fe = e;}
		
		try {
			if (Gate.isInitialised())
			{
				URL url = findCzesemPluginDirectoryURL();
				if (url != null)
				{
					super.loadConfig(
							Utils.URLToFilePath(url)+ "/../configuration/" +czsem_config_filename, classLoader);
					return;
				}
			}
		} catch (Exception e) {fe.add(e);}
		
		throw fe;
	}

		
	public static Config getConfig() throws ConfigLoadException 
	{
		return (Config) new Config().getAbstractConfig();
	}
			
	public void setInstallDefaults()
	{
		setCzsemProjectRelativePaths("$INSTALL_PATH");
		setTempRelativePaths("$projects");
		setAlephPath("$aleph");
		setPrologPath("$prolog");
		setWekaJarPath("$weka");
		setTmtRoot("$tmtRoot");
		setTredRoot("$tredRoot");
		setTreexDir("$treexRoot");
		setGateHome("$gateHome");
	}

	public void setMyWinValues()
	{
		setAlephPath("C:\\Program Files\\aleph\\aleph.pl");
		setPrologPath("C:\\Program Files\\Yap\\bin\\yap.exe");
		setWekaJarPath("C:\\Program Files\\Weka-3-6\\weka.jar");
		setTmtRoot("C:\\software\\Treex\\TMT_root");
		setTreexDir("C:\\software\\Treex\\TMT_root\\treex");
		setTredRoot("C:\\tred");
		setGateHome("C:\\Program Files\\GATE_Developer_7.1");
		setCzsemProjectRelativePaths("C:\\workspace\\czsem\\src\\czsem");
		setCzsemPluginDir("C:\\workspace\\czsem\\src\\czsem" + "/czsem-gate-plugin/target/prepared-installer-files/czsem-gate-plugin");
		setTempRelativePaths("C:\\workspace\\czsem\\src\\czsem\\temp");
	}

	protected void setCzsemProjectRelativePaths(String projPath) {
		setLearnigConfigDirectoryForGate(projPath + "/resources/Gate/learning");
		setCzsemPluginDir(projPath + "/czsem-gate-plugin");
		setCzsemResourcesDir(projPath + "/resources");
	}

	protected void setTempRelativePaths(String tempPath) {
		setTmtSerializationDirectoryPath(tempPath + "/TmT_serializations");
		setLogFileDirectoryPath(tempPath + "/log");
		setIlpProjestsPath(tempPath + "/ILP_serializations");		
	}

	public void setCzsemPluginDir(String czsemPluginDir) {
		set("czsemPluginDir", czsemPluginDir);
	}

	public String getCzsemPluginDir() {
		return get("czsemPluginDir");
	}
	
	public String getTmtRoot() {
		return get("tmtRoot");
	}

	public void setTmtRoot(String tmtRoot) {
		set("tmtRoot", tmtRoot);
	}

	public String getTredRoot() {
		return get("tredRoot");
	}

	public void setTredRoot(String tredRoot) {
		set("tredRoot", tredRoot);
	}


	public void setGateHome()
	{
		if (Gate.getGateHome() == null)
			Gate.setGateHome(new File(getGateHome()));
	}


	public void setTmtSerializationDirectoryPath(String tmtSerializationDirectoryPath) {
		set("tmtSerializationDirectoryPath", tmtSerializationDirectoryPath);
	}


	public URL getTmtSerializationDirectoryURL() throws MalformedURLException
	{
		return new File(getTmtSerializationDirectoryPath()).toURI().toURL();
	}

	public String getTmtSerializationDirectoryPath() {
		return get("tmtSerializationDirectoryPath");
	}


	public void setLogFileDirectoryPath(String logFileDirectoryPath) {
		set("logFileDirectoryPath", logFileDirectoryPath);
	}

	public String getLogFileDirectoryPathExisting() {
		String ret = getLogFileDirectoryPath();
		File f = new File(ret);
		if (! f.exists())
		{
			f.mkdirs();
		}
		return ret;
	}


	public String getLogFileDirectoryPath() {
		return get("logFileDirectoryPath");
	}

	public void setLearnigConfigDirectoryForGate(String learnigConfigDirectoryForGate) {
		set("learnigConfigDirectoryForGate", learnigConfigDirectoryForGate);
	}


	public String getLearnigConfigDirectoryForGate() {
		return get("learnigConfigDirectoryForGate");
	}


	public String getTreexDir() {
		return get("treexDir");
	}

	public void setTreexDir(String treeDir) {
		set("treexDir", treeDir);
	}


	public String getTreexConfigDir() {
		return get("treexConfigDir");
	}


	public void setTreexConfigDir(String treexConfigDir) {
		set("treexConfigDir", treexConfigDir);
	}
}
