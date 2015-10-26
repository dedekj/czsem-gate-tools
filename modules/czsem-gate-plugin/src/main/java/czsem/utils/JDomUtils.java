package czsem.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class JDomUtils
{
	public static void printXml(Element element) throws IOException
	{
		printXml(System.out, element);
	}
		
	

	public static void printXml(PrintStream out, Element element) throws IOException
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.outputElementContent(element, out);				
	}
	
	public static Document getJdomDoc(URL doc_url) throws JDOMException, IOException
	{
		SAXBuilder parser = new SAXBuilder();
		return parser.build(doc_url);		
	}


}
