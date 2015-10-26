package czsem.gate.debug;

import java.io.File;

import gate.Gate;
import gate.util.GateException;

public class RunGateGui {

	public static void main(String[] args) throws GateException {
		Gate.setGateHome(new File("C:\\Program Files\\gate\\GATE-7.0"));
		gate.Main.main(args);
	}

}
