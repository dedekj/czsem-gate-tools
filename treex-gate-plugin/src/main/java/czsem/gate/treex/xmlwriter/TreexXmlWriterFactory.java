package czsem.gate.treex.xmlwriter;

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.xmlrpc.serializer.CharSetXmlWriterFactory;

public class TreexXmlWriterFactory extends CharSetXmlWriterFactory {

	@Override
	protected XMLWriter newXmlWriter() {
		return new FilteringCharSetXMLWriter();
	}

}
