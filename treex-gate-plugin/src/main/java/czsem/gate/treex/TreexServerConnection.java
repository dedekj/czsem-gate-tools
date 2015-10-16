package czsem.gate.treex;

import java.util.Map;

public interface TreexServerConnection {

	Object analyzePreprocessedDoc(String text, Map<String, Object>[] metaData) throws TreexException;
	void close();

}
