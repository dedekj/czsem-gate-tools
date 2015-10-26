package czsem.fs;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import czsem.gate.utils.GateAwareTreeIndex;

public class FSTreeWriter	{

	private PrintWriter out;
	private NodeAttributes nodeAttributes;
	private GateAwareTreeIndex index = new GateAwareTreeIndex();
	private Set<String> attributes = new HashSet<String>();
	private int rootNode = -1 ;


	public FSTreeWriter(PrintStream out, NodeAttributes nodeAttributes) {
		this(
				new PrintWriter(new OutputStreamWriter(out)),
				nodeAttributes);
	}

	public FSTreeWriter(PrintWriter out, NodeAttributes nodeAttributes) {
		this.out = out;
		this.nodeAttributes = nodeAttributes;
	}


	public boolean printTree()
	{		
		int root = getRootNode();
		if (root == -1) return false;
		
		printNode(root);
		return true;
	}


	private void printCildren(int father_id)
	{
		Iterable<Integer> childern = index.getChildren(father_id);
		if (childern == null) return;

		char delim = '('; 
		for (int child_id : childern)
		{
			out.print(delim);
			delim = ',';
			printNode(child_id);
		}			
		if (delim == ',') out.print(')');
	}

	
	private void printAttribute(String attr_name, Object attr_value)		
	{
		attributes.add(attr_name);
		
		out.print(attr_name);
		out.print('=');
		
		String str_value = attr_value.toString();
		String functional_chars = "\\=,[]|<>!@~";
		
		for (int i=0; i<str_value.length(); i++)
		{
			char ch = str_value.charAt(i);
			if (functional_chars.indexOf(ch) != -1) out.print('\\');
			out.print(ch);
		}
	}
	
	private void printNode(int node_id)
	{
		out.print('[');
					
		Iterator<Entry<String, Object>> i = nodeAttributes.get(node_id).iterator();

		if (i.hasNext())
		{
			for (;;)
			{
				Entry<String, Object> entry = i.next();
				Object value = entry.getValue();
				
				if (value != null)
				{
					printAttribute(entry.getKey(), value);
					
					if (! i.hasNext()) break;
					
					out.print(',');										
				}
				
			}
		}
		
		out.print(']');
				
		printCildren(node_id);
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	
	public GateAwareTreeIndex getIndex() {
		return index;
	}
	
	public int getRootNode() {
		if (rootNode < 0)
		{
			rootNode = index.findRoot();			
		}
		return rootNode;
	}
}