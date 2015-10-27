/*******************************************************************************
 * Copyright (c) 2015 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.gate.treex;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.Utils;
import czsem.utils.AbstractConfig;

public class TreexConfig extends AbstractConfig {
	private static final Logger logger = LoggerFactory.getLogger(TreexConfig.class);
	
	public static final String TREEX_ONLINE = "treex_online";
	
	private static volatile URL treexOnlineDirFromPlugin = null;

	public static TreexConfig getConfig() throws ConfigLoadException {
		return new TreexConfig().getSingleton();
	}

	@Override
	public String getConfigKey() {
		return "treex_gate_plugin_config";
	}

	public String getTreexOnlineDir() { 				return get("treexOnlineDir"); }
	public String getLogFileDirectoryPathExisting() { 	return get("treexLogDir"); }
	public String getTreexDir() { 						return get("treexDir"); }
	public String getTreexConfigDir() {					return get("treexConfigDir"); }

	@Override
	protected void updateDefaults() {
		setDefaultVal("treexConfigDir", 	null);
		setDefaultVal("treexDir", 			null);
		
		//call only if not found
		setDefaultFun("treexOnlineDir", 	this::findTreexOnlineDir);
		
		//call always
		setDefaultVal("treexLogDir", 		unsureLogFileDirectoryPathExisting());
	}

	
	public String findTreexOnlineDir() {
		File f = new File(TREEX_ONLINE);
		if (f.exists())
			return f.getAbsolutePath();

		URL url = getTreexOnlineDirFromPlugin();
		if (url != null) {
			try {
				return Utils.URLToFile(url).getAbsolutePath();
			} catch (IOException | IllegalArgumentException | URISyntaxException e) {
				logger.warn("Failed to get abs path from URL: {}\n{}", url, e.getMessage());
			}
		}
		
		return null;
	}


	public String unsureLogFileDirectoryPathExisting() {
		String path = get("treexLogDir");
		
		if (path != null)
			new File(path).mkdirs();
		else {
			try {
				path = Files.createTempDirectory("czsem_treex_logs_").toAbsolutePath().toString();
			} catch (IOException e) {
				throw new RuntimeException("Failed to create a temporary directory...", e);
			}
		}
		return path;
	}

	public static URL getTreexOnlineDirFromPlugin() {
		return treexOnlineDirFromPlugin;
	}

	public static void setTreexOnlineDirFromPlugin(URL treexOnlineDirFromPlugin) {
		TreexConfig.treexOnlineDirFromPlugin = treexOnlineDirFromPlugin;
	}

	@Override
	public String getDefaultLoc() {
		URL tolUrl = getTreexOnlineDirFromPlugin();
		if (tolUrl != null) {
			try {
				File tolFile = Utils.URLToFile(tolUrl);
				File cfgFile = new File(tolFile.getParentFile(), getConfigKey() + ".xml");
				if (cfgFile.exists())
					return cfgFile.getAbsolutePath();
			} catch (IOException | IllegalArgumentException | URISyntaxException e) {
				return super.getDefaultLoc();
			}
		}

		return super.getDefaultLoc();
	}
}
