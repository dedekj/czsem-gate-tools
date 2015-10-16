package czsem.utils;

import java.io.FileNotFoundException;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.utils.AbstractConfig.ConfigLoadException;

public class AbstractConfigTest {
	
	AbstractConfig cfg = new AbstractConfig() {

		@Override
		protected String getConfigKey() {
			return "nonexisting_file_name";
		}
	
	};

	@Test
	public void testFail() {
		
		try {
			cfg.loadConfig();
		} catch (ConfigLoadException e) {
			System.err.println(e);
			Assert.assertTrue(e.causes.size() >= 4);
			return;
		}
		
		Assert.fail();
	}
	
	@Test
	public void testSystemProp() {
		System.setProperty(AbstractConfig.config_dir_envp, "/opt/czsem_suite_2.5-SNAPSHOT/configuration");

		try {
			cfg.loadConfig();
		} catch (ConfigLoadException e) {
			System.err.println(e);
			Exception dirPropError = e.causes.get(1);
			System.err.println(dirPropError);
			Assert.assertTrue(dirPropError instanceof FileNotFoundException);
			return;
		}
		
		Assert.fail();
	}


}
