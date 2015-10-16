package czsem.gate.treex.factory;


public class TreexCloudFactory {
	
	private static TreexCloudFactoryInterface instance = new TreexLocalAnalyserFactory();

	public static synchronized TreexCloudFactoryInterface getInstance() {
		return instance;
	}

	public static synchronized void setInstance(TreexCloudFactoryInterface instance) {
		TreexCloudFactory.instance = instance;
	}


}
