package czsem.fs;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface NodeAttributes 
{
	Iterable<Map.Entry<String, Object>> get(int node_id);
	Object getValue(int node_id, String attrName);
	
	
	public static class IdNodeAttributes implements NodeAttributes {
		@Override
		public Object getValue(int node_id, String attrName) {
			return node_id;
		}
		
		@Override
		public Iterable<Entry<String, Object>> get(int node_id) {
			List<Entry<String,Object>> ret = new ArrayList<Map.Entry<String,Object>>(1);
			ret.add(new AbstractMap.SimpleEntry<String, Object>("id", node_id));
			return ret;
		}

		@Override
		public boolean isSubClassOf(Object dataValue, String restricitonString) {
			return restricitonString.equals(dataValue);
		}
	}


	boolean isSubClassOf(Object dataValue, String restricitonString);

}