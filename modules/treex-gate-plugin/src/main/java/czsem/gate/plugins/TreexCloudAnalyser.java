package czsem.gate.plugins;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.gui.MainFrame;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import czsem.gate.treex.TreexAnalyserBase;
import czsem.gate.treex.TreexException;
import czsem.gate.treex.TreexServerConnection;
import czsem.gate.treex.factory.TreexCloudFactory;
import czsem.gate.utils.GateUtils;

@CreoleResource(name = "czsem TreexCloudAnalyser", comment = "Analyzes givem corpus by arbitrary Treex server.", 
	helpURL="https://czsem-suite.atlassian.net/wiki/display/DOC/Treex+GATE+Plugin")
public class TreexCloudAnalyser extends TreexAnalyserBase {	
	private static final Logger logger = Logger.getLogger(TreexCloudAnalyser.class);
	
	private static final long serialVersionUID = 497874336110323651L;

	@Override
	public void cleanup() {
		logger.info("Going to close treex ServerConnection...");
		getServerConnection().close();
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		setScenarioString(computeScenarioString());
		
		try {
			TreexServerConnection conn = TreexCloudFactory.getInstance()
					.prepareTreexServerConnection(getLanguageCode(), getScenarioString());
			
			setServerConnection(conn);
		} catch (TreexException e) {
			throw formatInitException(e);
		}
		
		return super.init();
	}

	public static void main(String [] args) throws Exception {
		GateUtils.initGateKeepLog();
		GateUtils.registerComponentIfNot(TreexCloudAnalyser.class);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainFrame.getInstance().setVisible(true);
			}
		});
	}
}
