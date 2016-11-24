package czsem.gate.treex.factory;

import czsem.gate.treex.TreexConfig;
import czsem.utils.AbstractConfig.ConfigLoadException;


public class TreexCloudFactory {
	
	private static volatile TreexCloudFactoryInterface instance;

	public static synchronized TreexCloudFactoryInterface getInstance() {
		if (instance == null) {
			try {
				TreexLocalAnalyserFactory f = new TreexLocalAnalyserFactory();
				String[] cmds = TreexConfig.getConfig().getTreexCloudFactoryCommands();
				if (cmds != null)
					f.setCmdArray(cmds);
						
				instance = f;
			} catch (ConfigLoadException e) {
				throw new RuntimeException(e);
			}
		}
		
		return instance;
	}

	public static synchronized void setInstance(TreexCloudFactoryInterface instance) {
		TreexCloudFactory.instance = instance;
	}


}
