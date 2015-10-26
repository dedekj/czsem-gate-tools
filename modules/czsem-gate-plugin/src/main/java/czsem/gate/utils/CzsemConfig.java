package czsem.gate.utils;

import java.io.File;

import gate.Gate;

public class CzsemConfig
{
	
	private static CzsemConfig inst = new CzsemConfig();

	public static CzsemConfig getConfig() {
		return inst;
	}

	public String getGateHome() {
		return "C:/Program Files/GATE_Developer_8.1";
	}
	
	public void setGateHome()
	{
		if (Gate.getGateHome() == null)
			Gate.setGateHome(new File(getGateHome()));
	}
}
