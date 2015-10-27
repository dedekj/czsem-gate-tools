package czsem.fs.query;

import java.util.List;

import czsem.fs.FSTokenizer;

public class FSQueryParser {

	protected List<Character> chars;
	protected List<String> strings;
	
	protected int charIndex = 0;
	protected int stringIndex = 0;
	
	protected FSQueryBuilder builder;

	public FSQueryParser(FSQueryBuilder builder) {
		this.builder = builder;
	}
	
	public static class SyntaxError extends Exception {
		public SyntaxError(String message) {
			super(message);
		}

		private static final long serialVersionUID = 595782365757384397L;		
	}

	public void parse(String input) throws SyntaxError {
		FSTokenizer tokenizer = new FSTokenizer(input);
		chars = tokenizer.getCharList();
		strings = tokenizer.getStringList();
		
		parseNode();
	}
	protected void parseNode() throws SyntaxError {
		expectChar('[');
		
		builder.addNode();
		
		parseRestrictions();
		
		expectChar(']');
		
		if (moreCharsAvailable() && nextCharIs('('))
		{
			parseChildren();					
		}
	}

	protected void parseChildren() throws SyntaxError {
		expectChar('(');		
		builder.beginChildren();
		
		for (;;)
		{
			parseNode();
			if (! nextCharIs(',')) break;
			expectChar(',');			
		}
				
		expectChar(')');		
		builder.endChildren();
	}

	protected void parseRestrictions() throws SyntaxError {
		for (;;)
		{
			parseRestriction();
			if (! nextCharIs(',')) break;
			expectChar(',');			
		}
	}

	protected void parseRestriction() throws SyntaxError {
		if (nextCharIs(']')) return;
		expectChar(null);
		
		StringBuilder comparator = new StringBuilder();
		comparator.append(expectCompratorChar());
		
		if (! nextCharIs(null))
		{
			comparator.append(expectCompratorChar());			
		}

		expectChar(null);
		
		builder.addRestriction(comparator.toString(), getStringAndMove().trim(), getStringAndMove());
		
	}

	protected char expectCompratorChar() throws SyntaxError {
		Character ch = getCurrentCharAndMove();
		
		if (	ch == null ||
				FSTokenizer.isSpecialChar(ch) != FSTokenizer.SpecialChar.EVEN_STRING_COMPARATOR)
			
			throw new SyntaxError(String.format("Comparator expected but '%c' found!", ch));
		
		return ch;
	}

	protected boolean nextCharIs(Character next) {
		if (next == getCurrentChar()) return true; //mainly if both are null
		if (next == null) return false; //because of previous
		return next.equals(getCurrentChar());
	}
	
	protected void expectChar(Character expected) throws SyntaxError {
		Character ch = getCurrentCharAndMove();
		
		if (expected == ch) return; //mainly if both are null - return ok;
		
		if (expected == null || !expected.equals(ch)) 
			throw new SyntaxError(String.format("Character '%c' expected but '%c' found!", expected, ch));		
	}

	protected Character getCurrentCharAndMove() {
		Character ch = getCurrentChar();
		charIndex++;
		return ch;		
	}
	
	protected Character findNextChar() {
		Character ch;
		do {
			ch = chars.get(++charIndex);
			if (ch == null) return ch;
		} while (ch == ' ');
		
		return ch;
	}

	protected Character getCurrentChar() {
		if (charIndex >= chars.size()) return null;
		Character ch = chars.get(charIndex);
		if (ch == null) return ch;
		if (ch == ' ') ch = findNextChar();
		return ch;
	}

	protected boolean moreCharsAvailable() {
		for (int i = charIndex+1; i<chars.size(); i++)
		{
			if (chars.get(i) != ' ') return true; 
		}
			
		return false;
	}

	protected String getStringAndMove() {
		return strings.get(stringIndex++); 
	}
}
