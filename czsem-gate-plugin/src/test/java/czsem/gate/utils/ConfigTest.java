package czsem.gate.utils;


public class ConfigTest {
/*
	@Test(groups = { "excludeByMaven" }, expectedExceptions=ConfigLoadException.class)
	public void getConfigExcept() throws Exception {
		// Run from dir where czsem_config is not reachable 
		System.err.println(System.getProperty("user.dir"));
		
		GateUtils.initGateKeepLog();
		
		Gate.setPluginsHome(new File("."));
		
		TemporaryFolder temp = new TemporaryFolder();
		temp.create();
		temp.getRoot().deleteOnExit();
		
		File f = temp.newFolder(Config.czsem_plugin_dir_name);
		temp.create();
		f.mkdir();
		f.deleteOnExit();
		
		File cf = new File(f, "creole.xml");
		FileWriter wr = new FileWriter(cf);
		wr.write("<CREOLE-DIRECTORY/>"); 
		wr.close();
		cf.deleteOnExit();
		
		Gate.getCreoleRegister().registerDirectories(f.toURI().toURL());

		try {
			Config.getConfig();
		} catch (ConfigLoadException e) {
			e.printStackTrace();
			throw e;
		}
	}
	*/
}
