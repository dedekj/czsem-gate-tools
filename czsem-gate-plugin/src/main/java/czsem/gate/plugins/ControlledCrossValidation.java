package czsem.gate.plugins;

import gate.Corpus;
import gate.Document;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.gui.MainFrame;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import czsem.gate.utils.GateUtils;

@CreoleResource(name = "czsem ControlledCrossValidation", comment = "Does k-fold cross validation with predefined folds")
public class ControlledCrossValidation extends CrossValidation {
	private static final long serialVersionUID = -1545682883811561185L;
	
	private URL foldDefinitionDirectoryUrl;

	@Override
	protected void intitFolds() throws ResourceInstantiationException {
		try {
			File parentDir = new File(getFoldDefinitionDirectoryUrl().toURI());
			
			Corpus c = getCorpus();
			
			Map<String, Document> nameDocMap = new HashMap<String, Document>(c.size());
			for (Document d : c) {
				String docName = d.getName();
				
				nameDocMap.put(
						docName.substring(0, docName.indexOf('.')),
						d);
			}
			

			for (int i = 0; i < numberOfFolds; i++)
			{
				corpusFolds[i] = createFold(i);
				if (
						fillFoldFullName(corpusFolds[i][0], parentDir, nameDocMap)
						&&
						fillFoldFullName(corpusFolds[i][1], parentDir, nameDocMap))
				{ /* OK both folds are filled. */}
				else
				{
					fillFoldsByNumber(i, parentDir, nameDocMap);
				}
			}		
		} catch (URISyntaxException e) {
			throw new ResourceInstantiationException(e);
		}
	}

	protected void fillFoldsByNumber(int foldNumber, File parentDir, Map<String, Document> nameDocMap) {
		File f = new File(parentDir, Integer.toString(foldNumber));
		String[] nameList = f.list();				
		if (nameList == null) throw new IllegalArgumentException(
				String.format("No files found in fold definition directory: %s", f.toString()));
		
		Set<String> nameSet = new HashSet<String>(nameList.length, 1);		
		for (int i = 0; i < nameList.length; i++) {
			String docName = nameList[i];
			nameSet.add(docName.substring(0, docName.indexOf('.')));
		}
		
		for (Entry<String, Document> e : nameDocMap.entrySet())
		{
			if (nameSet.contains(e.getKey()))
			{
				//add to test fold				
				corpusFolds[foldNumber][0].add(e.getValue());
			} else {
				//add to train fold				
				corpusFolds[foldNumber][1].add(e.getValue());
			}			
		}
	}

	protected boolean fillFoldFullName(Corpus fold, File parentDir, Map<String, Document> nameDocMap) {
		String[] nameList = new File(parentDir, fold.getName().replace(' ', '_')).list();
		if (nameList == null) return false;
		for (int i = 0; i < nameList.length; i++) {
			String docName = nameList[i];
			fold.add(nameDocMap.get(
					docName.substring(0, docName.indexOf('.'))));
		}
		return true;
	}


	public URL getFoldDefinitionDirectoryUrl() {
		return foldDefinitionDirectoryUrl;
	}

	@CreoleParameter
	public void setFoldDefinitionDirectoryUrl(URL foldDefinitionDirectoryUrl) {
		this.foldDefinitionDirectoryUrl = foldDefinitionDirectoryUrl;
	}
	
	public static void main(String [] args) throws Exception	
	{
		GateUtils.initGate();
		GateUtils.registerCzsemPlugin();
		
		MainFrame.getInstance().setVisible(true);
		
	}
}
