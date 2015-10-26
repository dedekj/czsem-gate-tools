package czsem.gate.treex;

import gate.Document;
import gate.util.InvalidOffsetException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import czsem.Utils;
import czsem.gate.externalannotator.Annotator;

public class TreexReturnAnalysis {

	protected List<Map<String,Object>> zones;
	protected Map<String, Map<String, Object>> nodeMap;
	protected Set<String> idAttributes;
	protected Set<String> listAttributes;

	public TreexReturnAnalysis(Object treex_ret_param) {		
		zones = Utils.objectArrayToGenericList(treex_ret_param);
		
		nodeMap = new HashMap<String, Map<String,Object>>();

		for (Map<String, Object> zone : zones) {
			List<Map<String, Object>> roots = Utils.objectArrayToGenericList(zone.get("roots"));
			extractNodesFromList(roots, true);
			List<Map<String, Object>> nodes = Utils.objectArrayToGenericList(zone.get("nodes"));
			extractNodesFromList(nodes, false);			
		}
		
		idAttributes = findIdAttributes();
		listAttributes = findListAttributes();
		

	}

	public List<Map<String, Object>> getZones() {
		return zones;
	}

	public Map<String, Map<String, Object>> getNodeMap() {
		return nodeMap;
	}

	public Set<String> getIdAttributes() {
		return idAttributes;
	}

	public Set<String> getListAttributes() {
		return listAttributes;
	}

	public Set<String> getExcludeAttributes() {
		Set<String> exclude_attrs = new HashSet<String>(getListAttributes());
		exclude_attrs.addAll(getIdAttributes());
		
		exclude_attrs.add("czsemIsRoot");
		exclude_attrs.add("nodes");
		exclude_attrs.add("roots");
		exclude_attrs.add("wild_dump");
		
		return exclude_attrs;
	}

	protected Set<String> findListAttributes() {
		Set<String> keys = new HashSet<String>();
		
		for (Map<String, Object> node : nodeMap.values())
		{
			for (Entry<String, Object> entry : node.entrySet())
			{
				if (entry.getValue() instanceof Object[])
				{
					keys.add(entry.getKey());
				}
			}
		}		
		return keys;		
	}

	protected Set<String> findIdAttributes() {
		Set<String> keys = new HashSet<String>();
		
		for (Map<String, Object> node : nodeMap.values())
		{
			for (Entry<String, Object> entry : node.entrySet())
			{
				if (nodeMap.get(entry.getValue()) != null)
				{
					keys.add(entry.getKey());
				}
			}
		}		
		return keys;				
	}
	
	protected void extractNodesFromList(List<Map<String, Object>> node_list, boolean isRoot) {
		for (Map<String, Object> node : node_list)
		{
			node.put("czsemIsRoot", isRoot);
			nodeMap.put((String) node.get("id"), node);			
		}
		
	}

	public void annotate(Document doc, String asName) throws InvalidOffsetException {
										//Make a copy! Don't modify original ones!
		Set<String> idDependencyAttrs = new HashSet<String>(getIdAttributes()); 
		
		Set<String> listAttrs = getListAttributes(); 
//		listAttrs.remove("a.rf");
		idDependencyAttrs.remove("id");
		idDependencyAttrs.remove("parent_id");
//		idAttrs.remove("a/lex.rf");
		
		
		Annotator annotator = new Annotator();
		annotator.annotate(
				new TeexAnnotationSource(
						getZones(),
						getNodeMap(),
						getExcludeAttributes(),
						listAttrs, idDependencyAttrs),
				doc, asName);			
	}
}
