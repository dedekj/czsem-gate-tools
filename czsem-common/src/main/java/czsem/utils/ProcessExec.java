package czsem.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessExec {
	static Logger logger = LoggerFactory.getLogger(ProcessExec.class);

	public static void copyStream(InputStream input, OutputStream output) throws IOException
	{
	    byte[] buffer = new byte[1024];
	    int bytesRead;
	    while ((bytesRead = input.read(buffer)) != -1)
	    {
	        output.write(buffer, 0, bytesRead);
	    }
	}
	
	public static Thread createSimpleCopyThread(final InputStream from, final OutputStream to) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					copyStream(from, to);
				} catch (IOException e) {
					logger.warn("Exception in SimpleCopyThread. Terminating...\nStreams: from: "+from+" to: "+to, e); 
				}
			}
		});
		
		return thread;
	}
	
	public static class ReaderThread extends Thread {
			
			private Reader is;
			private Writer os;
	
			private char [] buf = new char[10000];
			private BufferedReader buf_read;
	
			private long last_read = Long.MIN_VALUE;
			private long last_nothing_toread = Long.MIN_VALUE;
			
	
			public ReaderThread(Reader is, Writer os) {
				this.is = is;
				this.os = os;			
			}
			
			public ReaderThread(Reader is, OutputStream os) {
				this(is, new PrintWriter(os));
			}
	
			public String readLine() throws IOException
			{
				return buf_read.readLine();				
			}
	
			public void waitUntilNothingToRead() throws InterruptedException
			{
				long timeout = 100;
				while (System.currentTimeMillis() - last_read < timeout) Thread.sleep(timeout);
				
				last_nothing_toread = System.currentTimeMillis();
			}
	
			public void waitForInput() throws InterruptedException
			{			
	//			Thread.sleep(500);
				
				synchronized (buf)
				{
					while (last_nothing_toread > last_read)	buf.wait(); 
				}
			}
			
			private int readbuf() throws IOException, InterruptedException
			{
				int ret = is.read(buf); 
				synchronized (buf)
				{
					buf_read = new BufferedReader(new CharArrayReader(buf));
					last_read = System.currentTimeMillis();
					buf.notify();
					return ret;
				}
			}
			
			protected void writeBuf(char[] buffer, int chars_to_write) throws IOException
			{
				os.write(buffer, 0, chars_to_write);
				os.flush();
			}
			
			@Override
			public void run() {
				try {
/*
					int runcnt = 0;
					int sum = 0;
/**/					
					for (int i=readbuf(); i>=0; i=readbuf())
					{
/*
						sum += i;
						runcnt++;
						int mod = 100;
						if (runcnt % mod == 1)
						{
							System.out.println(sum / mod);
							sum = 0;
						}
/**/						
						writeBuf(buf, i);
//						Thread.sleep(0);
					}
				} 
				catch (Exception e) {
					logger.warn("Exception in ReaderThread. Terminating...", e); 
				}
			}

		}

	public static class NullReaderThread extends ReaderThread
	{

		public NullReaderThread(Reader is) {
			super(is, (Writer) null);
		}

		@Override
		protected void writeBuf(char[] buffer, int i) throws IOException 
		{}		
	}


	private ReaderThread err_reader_thread;
	protected ReaderThread cin_reader_thread;
	protected Process process = null;
	protected PrintWriter output_writer;
	protected BufferedReader input_reader;
	protected BufferedReader error_reader;

	public ProcessExec() {
	}

	public void startErrReaderThread()
	{
		startErrReaderThread(System.err);		
	}

	public void startNullErrReaderThread() {
		err_reader_thread = new NullReaderThread(error_reader);
		err_reader_thread.start();		
	}

	public void startErrReaderThread(OutputStream output) {
		err_reader_thread = new ReaderThread(error_reader, output);
		err_reader_thread.start();
	}

	public void startReaderThreads(OutputStream std_output, OutputStream err_output) {		
		cin_reader_thread = new ReaderThread(input_reader, std_output);
		err_reader_thread = new ReaderThread(error_reader, err_output);
	
		cin_reader_thread.start();
		err_reader_thread.start();
	}
	
	public void startReaderThreads(String log_filename_prefix) throws FileNotFoundException
	{
		
		startReaderThreads(
				new FileOutputStream(log_filename_prefix + "std.log"),
				new FileOutputStream(log_filename_prefix + "err.log"));	
	}


	public void startNullReaderThreads() {		
		cin_reader_thread = new NullReaderThread(input_reader);
		err_reader_thread = new NullReaderThread(error_reader);
	
		cin_reader_thread.start();
		err_reader_thread.start();
	}

	public void startStdoutReaderThreads() {
		startReaderThreads(System.out, System.err);
	}

	protected void initBuffers()
	{
		output_writer = new PrintWriter(new BufferedOutputStream(process.getOutputStream()));
		input_reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		error_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));		
	}

	public void execWithProcessBuilder(ProcessBuilder pb) throws IOException
	{
		process = pb.start();
		initBuffers();
	}

	
	public void exec(String[] cmdarray) throws IOException
	{
		process = Runtime.getRuntime().exec(cmdarray);		
		initBuffers();
	}

	public void exec(String[] exec_args, File working_directory) throws IOException
	{
		exec(exec_args, null, working_directory);		
	}

	public void exec(String[] exec_args, String[] envp, File working_directory) throws IOException
	{
		process = Runtime.getRuntime().exec(exec_args, envp, working_directory);
		initBuffers();
	}

	public void exec(String[] exec_args, String[] envp) throws IOException
	{		
		process = Runtime.getRuntime().exec(exec_args, envp);
		initBuffers();
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();		
	}
	
	public String readLine() throws IOException
	{
		return input_reader.readLine();
	}

	public void format(String format, Object ... args)
	{
		output_writer.format(format, args);		
	}

	public void writeString(String text) throws IOException
	{
		output_writer.write(text);
	}

	public static boolean isProcessRunning(Process process) {
		try 
		{		
			if (process == null) return false;
			process.exitValue();
			return false;

			//Integer exit = process.exitValue();
			//Logger.getLogger(getClass()).debug(exit);
		} catch (IllegalThreadStateException e)
		{
			return true;
		}
	}
	
	public boolean isRunning()
	{
		return isProcessRunning(process);
	}

	public void destroy() {
		process.destroy();		
	}
}