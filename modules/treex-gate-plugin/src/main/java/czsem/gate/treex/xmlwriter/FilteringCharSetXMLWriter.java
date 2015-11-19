package czsem.gate.treex.xmlwriter;

import java.io.Writer;

import org.apache.ws.commons.serialize.CharSetXMLWriter;

import czsem.gate.xmlwriter.InvalidXmlCharacterFilterWriter;

public class FilteringCharSetXMLWriter extends CharSetXMLWriter {

	@Override
	public void setWriter(Writer pWriter) {
		Writer filter = new InvalidXmlCharacterFilterWriter(pWriter);
		super.setWriter(filter);
	}

}
