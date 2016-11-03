package czsem.fs.query.restrictions;

import java.util.HashMap;
import java.util.Map;

import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.QueryNode;
import czsem.fs.query.restrictions.DirectAttrRestriction.DirectAttrRestrictionDecorator;

public abstract class Restrictions {
	
	public static final class CMP {
		public static final String EQ = "="; 
		public static final String NEQ = "!="; 

		public static final String GT = ">"; 
		public static final String GTEQ = ">="; 
		public static final String LT = "<"; 
		public static final String LTEQ = "<="; 
		
		public static final String REG_EXP = "~="; 
		public static final String IN_LIST = "@="; 
		public static final String SUBCLASS = "<<"; 
	}
	
	public static final Map<String, BasicRestriction> comparartor2restrictioin = new HashMap<>(6);
	
	static {
		registerBasicRestriction(BasicRestriction.BR_EQ);
		registerBasicRestriction(BasicRestriction.BR_NEQ);
		registerBasicRestriction(BasicRestriction.BR_GT);
		registerBasicRestriction(BasicRestriction.BR_GTEQ);
		registerBasicRestriction(BasicRestriction.BR_LT);
		registerBasicRestriction(BasicRestriction.BR_LTEQ);
	};
	
	public static void addRestriction(QueryNode queryNode, String comparartor, String arg1,	String arg2) {
		switch (comparartor) {
		case CMP.REG_EXP:
			queryNode.addDirectRestriction(new RegExpRestrictioin(arg1, arg2));
			return;
		case CMP.IN_LIST:
			queryNode.addDirectRestriction(new InListRestrictioin(arg1, arg2));
			return;
		case CMP.SUBCLASS:
			queryNode.addDirectRestriction(new SubclassRestrictioin(arg1, arg2));
			return;
		}
		
		BasicRestriction r = comparartor2restrictioin.get(comparartor);
		if (r == null)
			throw new RuntimeException(String.format("Restricition not supported: %s", comparartor));
		
		if (ReferencingRestriction.tryParseRefString(arg2) != null) {
			queryNode.addReferencingRestriction(
					new ReferencingRestriction(r, arg1, arg2));
		} else {
			queryNode.addDirectRestriction(
					new DirectAttrRestrictionDecorator(r, arg1, arg2));
		}
	}
	
	
	public static class SubclassRestrictioin extends DirectAttrRestriction {

		public SubclassRestrictioin(String attr, String value) {
			super(attr, value);
		}

		@Override
		public boolean evaluate(Object dataValue) {
			throw new UnsupportedOperationException("Use evaluate with QueryData instead");
		}

		@Override
		public String getComparator() { return CMP.SUBCLASS; }

		@Override
		public boolean evaluate(QueryData data, int nodeID) {
			Object dataValue = data.getNodeAttributes().getValue(nodeID, attr);
			
			return data.getNodeAttributes().isSubClassOf(dataValue, getValueString());
		}

	}

	public static class InListRestrictioin extends DirectAttrRestriction {

		private String[] values;

		@Override
		public String getComparator() { return CMP.IN_LIST; }

		public InListRestrictioin(String attr, String value) {
			super(attr, value);
			values = value.split(";");
		}

		@Override
		public boolean evaluate(Object dataValue) {
			if (dataValue == null) return false;
			
			String dvString = dataValue.toString();
			
			for (String v : values) {
				if (v.equals(dvString)) return true;
			}
			
			return false;			
		}
	}

	public static class RegExpRestrictioin extends DirectAttrRestriction {

		@Override
		public String getComparator() { return CMP.REG_EXP; }

		public RegExpRestrictioin(String attr, String value) {
			super(attr, value);
		}

		@Override
		public boolean evaluate(Object dataValue) {
			if (dataValue == null) return false;
			return dataValue.toString().matches(value);
		}
	}
	
	public static void registerBasicRestriction(BasicRestriction r) {
		comparartor2restrictioin.put(r.getComparator(), r);
	}
}