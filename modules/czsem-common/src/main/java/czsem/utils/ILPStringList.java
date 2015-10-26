package czsem.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ILPStringList
{
	private ArrayList<String> lines = new ArrayList<String>();
	private static Random rnd = new Random();
	
	/**
	 * @return actual count of lines 
	 */
	public int getLines()
	{
		return lines.size();
	}
	
	public String removeRandomLine()
	{
		return lines.remove(rnd.nextInt(getLines()));
	}
	
	public void readFromFile(String filename) throws IOException
	{				
		BufferedReader fr = new BufferedReader( new FileReader(filename));
		
		lines.clear();		
		for (;;)
		{
			String ln = fr.readLine();
			if (ln == null)
			{
				fr.close();
				return;
			}
			
			//lines starting with comments are removed
			if (ln.charAt(0) != '%') lines.add(ln);
		}
	}

}
