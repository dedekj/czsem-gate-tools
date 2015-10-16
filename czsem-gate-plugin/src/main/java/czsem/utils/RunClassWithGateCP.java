package czsem.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import czsem.Utils;
import czsem.gate.utils.CzsemConfig;

public class RunClassWithGateCP {
	
	public static class Launcher
	{
		protected String pSep = System.getProperty( "path.separator" );
		protected String gHome = CzsemConfig.getConfig().getGateHome();
		
		public Launcher() throws IOException, URISyntaxException
		{}

		public void launch(String[] args) throws IOException, InterruptedException {
			String [] cmdarray = buildCmdArrayPrfix();
			
			ArrayList<String> finallArgs = new ArrayList<String>(cmdarray.length + args.length);
			finallArgs.addAll(Arrays.asList(cmdarray));
			finallArgs.addAll(Arrays.asList(args));
			
			System.out.println(finallArgs);
			

			ProcessExec proc = new ProcessExec();
			proc.execWithProcessBuilder(new ProcessBuilder(finallArgs));
			proc.startStdoutReaderThreads();
						
			
//			BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
			
			proc.waitFor();
		}

		private String[] buildCmdArrayPrfix() {
			return new String [] {
				"java",
				"-classpath",
				System.getProperty("java.class.path", "") +
					pSep + 
					gHome + "/bin/gate.jar" + 
					pSep + 
					findGateLibs(),
//					System.getProperty( "path.separator" ) +
//					Config.getConfig().getWekaRunFuzzyILPClassPath(),
				RunClass.class.getCanonicalName()
			};		
		}

		private String findGateLibs() {
			File[] list = new File(gHome + "/lib").listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".jar");
				}
			});
						
			return Utils.listToStr(Arrays.asList(list), pSep);
		}
		
	}

	public static void main(String[] args) throws Exception {		
		Launcher l = new Launcher();
		l.launch(args);		
	}

}

