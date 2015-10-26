package czsem.gate.treex;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;
import gate.util.InvalidOffsetException;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class TreexInputDocPrepare {
	private AnnotationSet inputAS;
	private Document doc;
	
	public TreexInputDocPrepare(Document doc, String input_AS_name)
	{
		inputAS = doc.getAnnotations(input_AS_name);
		this.doc = doc;
	}
	
	public Map<String,Object>[] createInputDocData() throws InvalidOffsetException {
		
		AnnotationSet sentences = inputAS.get("Sentence");
		List<Annotation> ord_sents = gate.Utils.inDocumentOrder(sentences);

		@SuppressWarnings("unchecked")
		Map<String,Object> [] zones = new Hashtable[ord_sents.size()];
		
		for (int a=0; a< ord_sents.size(); a++)
		{
			zones[a] = createSentenceInputData(ord_sents.get(a));
			
		}
		
		return zones;
	}

	protected Map<String, Object> createSentenceInputData(Annotation sentAnn) throws InvalidOffsetException {
		Map<String, Object> ret = new Hashtable<String, Object>();
		ret.put("sentence", Utils.stringFor(doc, sentAnn));
		
		AnnotationSet tocs = inputAS.get("Token").getContained(sentAnn.getStartNode().getOffset(), sentAnn.getEndNode().getOffset());
		List<Annotation> ord_tocs = gate.Utils.inDocumentOrder(tocs);
		
		@SuppressWarnings("unchecked")
		Hashtable<Object, Object>[] retTocs = new Hashtable[ord_tocs.size()];
		
		for (int t=0; t<ord_tocs.size(); t++) {
			Hashtable<Object, Object> tFTable = new Hashtable<Object, Object>(ord_tocs.get(t).getFeatures());
			retTocs[t] = tFTable;
		}

		ret.put("tokens", retTocs);
		
		return ret;
	}
}
