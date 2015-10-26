package czsem.gate.utils;

import gate.*;

import java.util.Random;

import org.apache.log4j.Level;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class GateUtilsTest {
	
	@BeforeClass
	public static void intiGate() throws Exception{
		GateUtils.initGate(Level.INFO);
	}
	
	public static String createRandomString(int length) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(r.nextInt());
		}
		return sb.toString();
	}
	
	@Test
	public void leakyTest() throws Exception {
		
		int numTests = 20000;
		int logLine = 2000;
		
		for (int i = 0; i < numTests; i++) {
			Corpus obj = Factory.newCorpus(createRandomString(5000));
			//Document obj = Factory.newDocument(createRandomString(5000));
			
			/**/
			GateUtils.releseGateReference(obj);
			/**/
			
			if (i % logLine == 0) {
				System.err.println(i);
			}
		}
		
	}
	

}
