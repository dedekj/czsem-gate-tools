package czsem.gate.treex.factory;

import czsem.gate.treex.TreexException;
import czsem.gate.treex.TreexServerConnection;

public interface TreexCloudFactoryInterface {
	TreexServerConnection prepareTreexServerConnection(String languageCode, String scenarioString) throws TreexException;
}
