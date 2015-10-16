package czsem.gate.treex.xmlwriter;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class InvalidXmlCharacterFilterWriter extends FilterWriter {

	public InvalidXmlCharacterFilterWriter(Writer out) {
		super(out);
	}
	
	@Override
	public void write(int c) throws IOException {
		
		if (c < 32) {				
			switch (c) {
				case 9: break;
				case 10: break;
				case 13: break;				
				default:
					//replace by space
					super.write(' ');
					return;
			}
		}

		super.write(c);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int i=0; i<len; i++){
			write(cbuf[i+off]);
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(), off, len);
	}
}
