package czsem.fs;

import java.util.ArrayList;
import java.util.List;

public class FSTokenizer {
	
	List<Character> charList = new ArrayList<Character>(); 
	List<String> stringList = new ArrayList<String>();
	protected String input;
	protected int i; 
	
	public FSTokenizer(String input)
	{
		this.input = input;
		processInput();
	}
	
	public static class SpecialChar 
	{
		public static final int NO = 0; 
		public static final int SPACE = 5; 		
		public static final int ORDINARY = 10; 
		public static final int ALSO_IN_STRING = 30; 
		public static final int EVEN_STRING_COMPARATOR = 40; 
	}
	
	public static int isSpecialChar(char ch)
	{
		switch (ch) {
			case '=':
			case '!':
			case '<':
			case '>':
			case '~':	//regexp
			case '@':	//in list
				return SpecialChar.EVEN_STRING_COMPARATOR;

			case '[':
			case ']':
			case ',':
				return SpecialChar.ALSO_IN_STRING;

			case ')':
			case '(':
				return SpecialChar.ORDINARY;

			case ' ':
				return SpecialChar.SPACE;	
	
			default:
				return SpecialChar.NO;
		}		
	}
	
	protected void processInput() {
		char lastChar = 'x';
		for (i=0; i<input.length(); i++)
		{
			char ch = getChar();

			if (	isSpecialChar(lastChar) >= SpecialChar.EVEN_STRING_COMPARATOR &&
					isSpecialChar(ch) < SpecialChar.ALSO_IN_STRING)
			{
					charList.add(null);
					readString();				
			}
			else if (isSpecialChar(ch) >= SpecialChar.SPACE)
			{
					charList.add(ch);
			} 
			else
			{
					charList.add(null);
					readString();
			}
			
			lastChar = ch;
		}		
	}

	protected void readString() {
		StringBuilder buf = new StringBuilder();
		
		for (; i<input.length(); i++)
		{
			char ch = getChar();
			
			if (ch == '\\')
			{
				i++;
				buf.append(getChar());
				continue; //Always remove first '\\' - consistent with NGTree.readTree
			}
			
			if (isSpecialChar(ch) >= SpecialChar.ALSO_IN_STRING)
			{
				i--;
				stringList.add(buf.toString());
				return;				
			}			

			buf.append(ch);
		}
		
	}

	protected char getChar(int j) {
		return input.charAt(j);
	}

	protected char getChar() {
		return getChar(i);
	}

	public List<Character> getCharList() {
		return charList;
	}

	public List<String> getStringList() {		
		return stringList;
	}

}
