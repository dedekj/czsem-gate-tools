package czsem.gate.utils;

import gate.util.Benchmark;
import gate.util.reporting.PRTimeReporter;
import gate.util.reporting.exceptions.BenchmarkReportInputFileFormatException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class TimeBenchmarkUtils {

	private static class TimeBenchmarkPrintMapSimple implements TimeBenchmarkReporter
	{
		public TimeBenchmarkPrintMapSimple(PrintStream out) {this.out = out;}
	
		private PrintStream out;
	
		@SuppressWarnings("unchecked")
		private void printMap(Map<String, Object> map, String prefix)
		{
			
			for (String pr_name : map.keySet()) {
				if (pr_name.startsWith("doc") || pr_name.equals("systotal"))
					continue;
	
				out.print(prefix + pr_name);
	
				Object child = map.get(pr_name);
				Map<String, Object> ch_map = null;
								
				if (child instanceof String)
				{
					out.println("\t" + child);
					continue;
				}
				else
				{
					ch_map = (Map<String, Object>) child;
					out.println("\t" + ch_map.get("systotal"));
				}
				
				
				//recursive call
				printMap(ch_map, prefix + "\t");
	
			}
		}
	
		@Override
		public void report(Map<String, Object> report1Container1)
		{
			printMap(report1Container1, "");
		}
	}

	public interface TimeBenchmarkReporter
	{
	
		void report(Map<String, Object> report1Container1);
		
	}

	public static String getTimeBenchmarkLogFileName() throws URISyntaxException, IOException
	{
		return /*Config.getConfig().getLogFileDirectoryPathExisting()+*/"benchmark.txt";
	}

	public static void deleteGateTimeBenchmarkFile() throws URISyntaxException, IOException
	{
		new File(getTimeBenchmarkLogFileName()).delete();
	}

	public static void enableGateTimeBenchmark() throws URISyntaxException, IOException
	{
		RollingFileAppender appender = new RollingFileAppender();
		appender.setThreshold(Level.DEBUG);
		appender.setFile(getTimeBenchmarkLogFileName());
		appender.setAppend(false);
		appender.setMaxFileSize("10MB");
		appender.setMaxBackupIndex(1);
		appender.setLayout(new PatternLayout("%m%n"));
		
		appender.activateOptions();
		
		Logger bl = Logger.getLogger(Benchmark.class);
		bl.removeAllAppenders();
		bl.addAppender(appender);
		bl.setAdditivity(false);
		bl.setLevel(Level.DEBUG);
		
		
		Benchmark.setBenchmarkingEnabled(true);
	}

	public static void doGateTimeBenchmarkReport(TimeBenchmarkReporter reporter) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
		doGateTimeBenchmarkReport(
				reporter,
				getTimeBenchmarkLogFileName(),
				/*Config.getConfig().getLogFileDirectoryPathExisting()+*/"benchmark_report.txt",
				PRTimeReporter.MEDIA_TEXT,
				PRTimeReporter.SORT_EXEC_ORDER
				);		
		
	}

	public static String createGateTimeBenchmarkReport() throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 
		 doGateTimeBenchmarkReport(new TimeBenchmarkPrintMapSimple(new PrintStream(out)));
		 
		 return out.toString();			
	}

	@SuppressWarnings("unchecked")
		public static void doGateTimeBenchmarkReport(TimeBenchmarkReporter reporter, String benchmarkInputFileName, String reportFileName, String outputMediaType, String sortOrder) throws BenchmarkReportInputFileFormatException
		{
			// Report on processing resources
			// http://gate.ac.uk/sale/tao/splitch11.html#x15-30600011.4.4
	
			// 1. Instantiate the Class PRTimeReporter
			PRTimeReporter report = new PRTimeReporter();
			// 2. Set the input benchmark file
			File benchmarkFile = new File(benchmarkInputFileName);
			report.setBenchmarkFile(benchmarkFile);
			// 3. Set the output report file
			File reportFile = new File(reportFileName);
			report.setReportFile(reportFile);
			// 4. Set the output format: in html or text format (default:
			// MEDIA_HTML)
			report.setPrintMedia(outputMediaType);
			// 5. Set the sorting order: Sort in order of execution or descending
			// order of time taken (default: EXEC_ORDER)
			report.setSortOrder(sortOrder);
			// 6. Set if suppress zero time entries: True/False (default: True).
			// Parameter ignored if SortOrder specified is ‘SORT_TIME_TAKEN’
			report.setSuppressZeroTimeEntries(true);
			// 7. Set the logical start: A string indicating the logical start to be
			// operated upon for generating reports
			// report.setLogicalStart("");
			// 8. Generate the text/html report
	//		report.executeReport();
			
			 Object report1Container1 = report.store(report.getBenchmarkFile());
	//		 System.err.println(report1Container1);
			 		 
			 reporter.report((Map<String, Object>) report1Container1);		 		 
		}

	public static void main(String [] args) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{		
		System.out.println(createGateTimeBenchmarkReport());
	}

}
