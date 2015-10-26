package czsem.fs;

import gate.Annotation;
import gate.AnnotationSet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import czsem.Utils;
import czsem.utils.NetgraphConstants;

public class FSFileWriter {
	private PrintWriter out;
	
	private String[] attributes = null;

	public static final String[] token_annotation_types =
	{		
		"tToken",
		"t-node",
		"Token",
	};
			
	public FSFileWriter(String filename) throws UnsupportedEncodingException, FileNotFoundException 
	{
		out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf8"));
	};
	

	private void printHead()
	{		
		for (String attr : attributes)
		{
			out.print("@P ");			
			out.println(attr);			
		}
		
		out.print("@N ");
		out.println(NetgraphConstants.ORD_FEATURENAME);
		out.print("@V ");
		out.println(NetgraphConstants.STRING_FEATURENAME);
		out.print("@H ");
		out.println(NetgraphConstants.HIDE_FEATURENAME);

		out.println();		

/*			
		out.println("@P ord");
		out.println("@N ord");
		out.println("@P kind");		
		out.println("@P string");
		out.println("@V string");
*/
	}
	
	public static String[] guessAtttributes(AnnotationSet as)
	{		
		AnnotationSet tokens = as.get(
				Utils.setFromArray(FSFileWriter.token_annotation_types));
		
		Set<String> attr_set = new HashSet<String>();
		
		attr_set.add(NetgraphConstants.ID_FEATURENAME);
		
		for (Annotation token : tokens)
		{
			for (Object feature : token.getFeatures().keySet())
			{
				attr_set.add((String) feature);				
			}			
		}
		
		String[] attrs = attr_set.toArray(new String[0]);
		
		Arrays.sort(attrs);
//		for (int i = 0; i < attrs.length; i++) {
//			System.err.println(attrs[i]);
//		}
		
		return attrs;
	}
	
	
	public void PrintAll(AnnotationSet annotations)
	{
		attributes = guessAtttributes(annotations);

		printHead();
		
		for (Annotation sentence : annotations.get("Sentence"))
		{
			AnnotationSet sentence_set = annotations.getContained(
					sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
						
			FSSentenceWriter wr = new FSSentenceWriter(sentence_set, out);
			wr.printTree();
		}		
	}
	
	public void close()
	{
		out.close();
	}
}
