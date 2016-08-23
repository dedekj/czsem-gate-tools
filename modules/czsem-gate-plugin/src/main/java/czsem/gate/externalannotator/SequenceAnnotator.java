package czsem.gate.externalannotator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gate.Document;

public class SequenceAnnotator
{
	private static final Logger logger = LoggerFactory.getLogger(SequenceAnnotator.class);

	private String string_content;
	private int last_start_index;
	private int last_length=0;
	private int correction=0;

	private int backup_last_index;
	private int backup_last_length=0;
	
	public long lastStart() {return last_start_index-last_length;}
	public long lastEnd() {return last_start_index;}
	public int lastEndInt() {return last_start_index;}
	
	public void backup()
	{
		backup_last_index = last_start_index;		
		backup_last_length = last_length;		
	}
	
	public void restoreToLastStartAndBackupCurrent() {
		backup();
		last_start_index = (int) lastStart();
		//last_length = last_length;
	}
	
	public void restorePreviousAndBackupCurrent()
	{
		int swap;
		
		swap = last_start_index;
		last_start_index = backup_last_index ;
		backup_last_index = swap;
		
		swap = last_length;
		last_length = backup_last_length;
		backup_last_length = swap;
	}
	
	public SequenceAnnotator(Document doc)
	{
		this(doc, 0);
	}

	public SequenceAnnotator(Document doc, int start_index)
	{
		this(doc.getContent().toString(), start_index);
	}
	
	public SequenceAnnotator(String stringContent, int start_index) {
		string_content = stringContent;
		last_start_index = start_index;
	}
	
	
	public static class CannotAnnotateCharacterSequence extends StringIndexOutOfBoundsException
	{
		private static final long serialVersionUID = -6540653583278823750L;						
		protected String token;
		protected String annotator_content;
		protected int last_start_index;

		public CannotAnnotateCharacterSequence(String token, String annotator_content, int last_start_index)
		{
			super(annotator_content.substring(last_start_index, 
					Math.min(
							annotator_content.length(), 
							last_start_index + token.length())));
			this.token = token;
			this.annotator_content = annotator_content;
			this.last_start_index = last_start_index;
		}

		@Override
		public String getMessage() {
			return String.format(
					"Cannot annotate original character sequence \"%1.30s...\" with annotation \"%1.30s...\".",
					annotator_content.substring(last_start_index), token);
		}
	}
	
	public void nextToken(String token) throws StringIndexOutOfBoundsException
	{
		correction=0;
		int new_index = indexOf(token);
//		int new_index = string_content.indexOf(token, last_index);
		if (new_index == -1) 
			throw new CannotAnnotateCharacterSequence(token, string_content, last_start_index);
		
		if (new_index - last_start_index > 5)
		{
			logger.debug(
					String.format(
							"Big space in annotations dedtected! "+
							"last_index: %d, new_index: %d, diff: %d",
							last_start_index, new_index, new_index-last_start_index));
		}
		
		last_length = token.length() + correction;
		last_start_index = new_index + last_length; 
	}
	
	private class MoveLocalStartIndexContinueLoopException extends Throwable
	{private static final long serialVersionUID = 1L;};	
	
	private int indexOf(String token)
	{
//		int new_index = string_content.indexOf(token, last_index);
		int token_index, local_index;

		//moves start
		for (int local_start_index=last_start_index; ; local_start_index++)
		{
			try {
				//checks strings
				for (token_index=0, local_index=local_start_index; token_index<token.length(); local_index++, token_index++)
				{
					if (local_index>=string_content.length()) 
						return -1;
					
					char loc_ch = string_content.charAt(local_index);
					char toc_ch = token.charAt(token_index);
					
					if (loc_ch != toc_ch)
					{
						//loc_ch < 32 encoded as space, caused by czsem.gate.treex.xmlwriter.FilteringCharSetXMLWriter
						if (toc_ch == ' ' && loc_ch < 32) {
							continue;							
						}

						if (toc_ch == '\'' && loc_ch == '’') {
							continue;							
						}
						
						//multiple hyphens
						if (
								(loc_ch == '-')
								&&
								(Character.isWhitespace(toc_ch) || Character.isSpaceChar((int)toc_ch)) 
								&&
								(local_index > 0) &&
								(string_content.charAt(local_index-1) == '-'))
						{
//							local_index++;
							continue;
						}
						
						//long hyphen
						if (
								(loc_ch == '—') && 
								(toc_ch == '-'))
						{
							if (token_index+1<token.length() && token.charAt(token_index+1)==toc_ch)
								token_index++;
							continue;
						}


						//Angle Brackets skipped by TectoMT
						if (loc_ch == '<')
						{
							int ahead_index = token.indexOf('<', token_index);
							int found_index = string_content.indexOf('>',local_index);
							if (found_index != -1 && ahead_index == -1) {
								local_index = found_index;
								token_index--;

								continue;
							}
						}
						
						//DOT
						if ((loc_ch == '.') && (token.equals("DOT")))
						{
							token_index += 2;
							continue;
						}

						//<<<DOT>>
						if ((loc_ch == '.') && (token.startsWith("<<<DOT>>", token_index)))
						{
							token_index += 7;
							continue;
						}

						//quotation correction
						if (
								(loc_ch == '"' || loc_ch == '\'' || loc_ch == '“' || loc_ch == '”') && 
								(toc_ch == '\'' || toc_ch == '`'))
						{
							if (token_index+1<token.length() && token.charAt(token_index+1)==toc_ch)
								token_index++;
							continue;
						}

						if (token_index > 0) //otherwise move start
						{
							if (Character.isWhitespace(loc_ch) || Character.isSpaceChar((int)loc_ch) || loc_ch < 32)
							{
								token_index--;
								continue;
							}
							
							if (Character.isWhitespace(toc_ch) || Character.isSpaceChar((int)toc_ch))	
							{
								local_index--;
								continue;
							}
														
							throw new MoveLocalStartIndexContinueLoopException();
						}
						
						throw new MoveLocalStartIndexContinueLoopException();
					}					
				}//checks strings loop
				
				correction = local_index - local_start_index - token.length();
				if (correction != 0)
				{
					logger.debug("correction: " + correction);
				}
				return local_start_index;
				
			} catch (MoveLocalStartIndexContinueLoopException e) {/*contine*/} 			
		
		} //local_start_index loop		
	
	}//method indexOf	
	
	
	public char charAt(int index) {
		return string_content.charAt(index);
	}

}
