package czsem.ILP;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

public class Serializer {
	public static class Type implements Serializable
	{
		private static final long serialVersionUID = 5417325656555778091L;
		private String name;
		private Set<String> values = new HashSet<String>();		

		public Type(String typeName) {name = typeName;}
		public boolean addValue(String value) {return values.add(value);}
	}
	
	public static class Relation implements Serializable
	{
		private static final long serialVersionUID = -8062479896511079162L;
		private String name;
		private Type[] argTypes;
		
		private Relation(String name, Type[] argTypes) {
			this.setName(name);
			this.argTypes = argTypes;
		}

		private void setName(String name) {
			this.name = name;
		}

		public String getArgTypeName(int index) {
			return argTypes[index].name;
		}		

		public String getName() {
			return name;
		}		
	}
	
	protected PrintStream output = System.out;
	private Map<String, Relation> relationMap = new HashMap<String, Relation>();
	private Map<String, Type> typeList = new HashMap<String, Type>();
	
	public Serializer(String output_filename, boolean append) throws FileNotFoundException, UnsupportedEncodingException
	{
		output = new PrintStream(new FileOutputStream(output_filename, append));		
	}

	
	public Serializer(String output_filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		setOutput(output_filename);
	}
	
	public Serializer() {
		//Do nothing
	}

//	public static final String deafult_encoding = "ISO8859_1";
	public static final String deafult_encoding = "utf-8";
	
	public Serializer(OutputStream output) throws UnsupportedEncodingException
	{
		this.output = new PrintStream(output, false, deafult_encoding);
	}
	
	public void setOutput(String output_filename) throws FileNotFoundException, UnsupportedEncodingException
	{
//		output = new PrintStream(output_filename);
		output = new PrintStream(output_filename, deafult_encoding);
	/**	
		output.println(":- encoding(utf8).");
	/**/		
	}

	private Type getType(String typeName)
	{
		return typeList.get(typeName);
	}

	private Type addType(String typeName)
	{
		Type type = new Type(typeName);
		typeList.put(typeName, type);
		return type;
	}
	
	public Relation addRealtion(String relationName, String[] typeNames)
	{
		relationName = encodeRelationName(relationName);
		
		Relation ret = relationMap.get(relationName); 
		if (ret != null) return ret;
		
		Type [] types = new Type[typeNames.length];
		
		for (int i=0; i<typeNames.length; i++) {
			String typename = encodeRelationName(typeNames[i]);
			Type type = getType(typename);
			if (type == null) type = addType(typename);
			types[i] = type;
		}
		
		ret = new Relation(relationName, types);
		relationMap.put(relationName, ret);
		return ret;
	}
	
	public Relation addBinRelation(String relationName, String arg1TypeName, String arg2TypeName)
	{		
		return addRealtion(relationName, 
				new String[] {arg1TypeName, arg2TypeName});
	}

	public void renderTupleWithWariables(Relation rel)
	{
		String suff[] = new String[rel.argTypes.length];
		for (int i = 0; i < suff.length; i++) {
			suff[i]="";
		}
		
		renderTupleWithWariables(rel, suff);
	}

	public void renderTupleWithWariables(Relation rel, String [] suffix)
	{
		String values[] = new String[rel.argTypes.length];
		
		for (int i=0; i < rel.argTypes.length; i++)
		{
			values[i] = rel.argTypes[i].name.toUpperCase()+suffix[i];			
		}
		renderInlineTupleWithoutValueCheck(rel.getName(), values);
	}

	public void putInlineBinTuple(Relation rel, String value1, String value2)
	{
		putInlineTypedTuple(rel, 
				new String[] {value1, value2});
	}

	
	public void putBinTuple(Relation rel, String value1, String value2)
	{
		putInlineBinTuple(rel, value1, value2);
		output.println('.');		
	}

	public void putTuple(String relationName, String[] values)
	{
		putInlineTuple(relationName, values);
		output.println('.');		
	}

	public void putInlineTuple(String relationName, String[] values)
	{
		for (int i=0; i<values.length; i++)
		{
			values[i] = encodeValue(values[i]); 
		}
		renderInlineTupleWithoutValueCheck(encodeValue(relationName), values);
	}

	public void renderInlineTupleWithoutValueCheck(String relationName, String[] values)
	{
		output.print(relationName);
		
		char sep = '(';
		for (int i=0; i<values.length; i++)
		{
			output.print(sep);
			output.print(values[i]);
			sep=',';
		}		
		output.print(")");				
	}
	
	public void renderTupleWithoutValueCheck(String relationName, String[] values)
	{
		renderInlineTupleWithoutValueCheck(relationName, values);
		output.println('.');		
	}

	public static String encodeRelationName(String relationName)
	{
		if (	relationName.indexOf('.') != -1 || 
				Character.isUpperCase(relationName.charAt(0)))
		{
			return String.format("'%s'", relationName);
		}
		
/*		char ch = relationName.charAt(0);
		if (Character.isUpperCase(ch))
		{
			return Character.toLowerCase(ch) + relationName.substring(1);
		}
*/		
		return relationName;
	}
	
	public static boolean isNumber(String value)
	{
		try 
		{
			Double.parseDouble(value);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static String encodeValue(String value)
	{

		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		StringCharacterIterator iter = new StringCharacterIterator(value);

		boolean all_digits = true;
		boolean all_lo_alpha = Character.isLowerCase(iter.first());

		for (char ch = iter.first(); ch != CharacterIterator.DONE; ch = iter.next())
		{
			all_digits = all_digits & (Character.isDigit(ch) || ch == '.');
			all_lo_alpha = all_lo_alpha & (Character.isLowerCase(ch) | Character.isDigit(ch) | ch == '_');
			
			if (ch > 127 )
			{
				sb.append((int) ch);
				all_lo_alpha = false;
			}
			else
			{						
				switch (ch) {
				case '\'':
				case '\\':
					sb.append('\\');
				default:
					sb.append(ch);				
				}
			}
		}

		if ((all_digits && isNumber(value)) || all_lo_alpha) return value;
		
		sb.append('\'');
//		return sb.toString();
		if (value.matches("'.*'")) 
			value = value.substring(1, value.length()-1); 		
		return String.format("'%s'", value.replace("'", "\\'"));
	}
	
	public void putTypedTuple(Relation rel, String[] values)
	{
		putInlineTypedTuple(rel, values);
		output.println('.');		
	}

	public void putInlineTypedTuple(Relation rel, String[] values)
	{		
		for (int i=0; i<rel.argTypes.length; i++)
		{
			values[i] = encodeValue(values[i]); 
			rel.argTypes[i].addValue(values[i]);
		}
		
		renderInlineTupleWithoutValueCheck(rel.getName(), values);
	}
	
	protected void putType(Type type)
	{
		for (String value : type.values) {
			renderTupleWithoutValueCheck(type.name, new String[]{value});
		}
	}
	
	public void outputAllTypes()
	{
		for (Type type : typeList.values()) {
			putType(type);
		}
	}

	public void putDetermination(Relation targetRealtion, Relation backgroundRealtion)
	{
		//:- determination(eastbound/1,has_car/2).

		output.print(":- determination(");
		output.print(targetRealtion.getName());
		output.print('/');
		output.print(targetRealtion.argTypes.length);
		output.print(',');
		output.print(backgroundRealtion.getName());
		output.print('/');
		output.print(backgroundRealtion.argTypes.length);
		output.println(").");		
	}

	/**
	 * @see Serializer#putMode(czsem.ILP.Serializer.Relation, String, char[]) 
	 */
	public void putBinaryMode(Relation rel, String recallNumber, char argument1Mode, char argument2Mode)
	{
		putMode(rel, recallNumber, new char[] {argument1Mode, argument2Mode});
	}

	/**
	 * 
	 * @param rel
	 * @param recallNumber bounds the non-determinacy of a form of predicate call.
	 * RecallNumber can be either (a) a number specifying the number of successful calls to the predicate; or (b) * specifying that the predicate has bounded non-determinacy. It is usually easiest to specify RecallNumber as *.
	 * @param argumentModes specifies a legal form for calling a predicate. For each argument of the relation there should be single character: '+' , '-' or '#'.  
	 * A simple argument mode character is one of: (a) '+'T specifying that when a literal with predicate symbol p appears in a hypothesised clause, the corresponding argument should be an "input" variable of type T; (b) '-'T specifying that the argument is an "output" variable of type T; or (c) '#'T specifying that it should be a constant of type T.
	 */
	public void putMode(Relation rel, String recallNumber, char [] argumentModes)
	{
		output.print(":- mode(");
		output.print(recallNumber);
		output.print(',');
		output.print(rel.getName());
		
		char delim = '(';
		
		for (int i=0; i<rel.argTypes.length; i++)
		{
			output.print(delim);
			delim = ',';
			output.print(argumentModes[i]);
			output.print(rel.argTypes[i].name);
		}
		
		output.println(")).");		
	}

	public void print(String what)
	{
		output.print(what);		
	}

	public void putCommentLn(String comment)
	{
		output.print("% ");
		output.println(comment);
	}
	
	public void close()
	{
		output.close();
	}

	public void putLearningSettings(String learningSettings)
	{
		putCommentLn("-------------------- learningSettings --------------------");
		print(learningSettings);
		print("\n");
	}
	
	public static void main(String[] argv)
	{
		System.err.println(isNumber("0.10.2"));
		
		Serializer ser = new Serializer();
		
		Relation rel = ser.addBinRelation("edge", "node", "node");
		ser.putBinTuple(rel, "node01", "002.2");
		ser.putBinTuple(rel, "node01", "345�");
		ser.putBinTuple(rel, "123node", "id_123");
		ser.putBinTuple(rel, "A123node", "_id_123");
		ser.outputAllTypes();
		ser.putDetermination(rel, rel);
		ser.putBinaryMode(rel, "10", '+', '-');
		ser.putBinaryMode(rel, "*", '+', '#');		
	}

}
