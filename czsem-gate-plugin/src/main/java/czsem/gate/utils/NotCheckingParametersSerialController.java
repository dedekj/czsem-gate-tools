package czsem.gate.utils;

import gate.FeatureMap;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

@CreoleResource
public class NotCheckingParametersSerialController extends SerialAnalyserController {
	private static final long serialVersionUID = 268604948092923729L;
	
	private boolean checkParameters = true;

	@Override
	protected void checkParameters() throws ExecutionException {
		if (getCheckParameters())
		{
			super.checkParameters();
		}
	}

	public Boolean getCheckParameters() {
		return checkParameters;
	}

	@RunTime
	@CreoleParameter(defaultValue="true")
	public void setCheckParameters(Boolean checkParameters) {
		this.checkParameters = checkParameters;
		
		FeatureMap fm = getFeatures();
		if (fm != null)
			fm.put("checkParameters", checkParameters);
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		Object check = getFeatures().get("checkParameters");
		if (check != null && check instanceof Boolean) {
			setCheckParameters((Boolean) check);
		}
		return super.init();
	}
}