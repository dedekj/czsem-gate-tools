package czsem.fs.query.restrictions;

import czsem.fs.query.FSQuery.QueryData;

public abstract class Restrictioin {
	
	public static final class CMP {
		public static final String EQ = "="; 
		public static final String NEQ = "!="; 
		public static final String REG_EXP = "~="; 
		public static final String IN_LIST = "@="; 
		public static final String SUBCLASS = "<<"; 
	}
	
	public abstract boolean evaluate(QueryData data, int nodeID);

	public abstract String getComparator();
	public abstract String getAttrName();
	public abstract String getValueString();
	
	public static Restrictioin createRestriction(String comparartor, String arg1, String arg2) {
		if (comparartor.equals(CMP.EQ))
			return new EqualRestrictioin(arg1, arg2);
		if (comparartor.equals(CMP.REG_EXP))
			return new RegExpRestrictioin(arg1, arg2);
		if (comparartor.equals(CMP.NEQ))
			return new NotEqualRestrictioin(arg1, arg2);
		if (comparartor.equals(CMP.IN_LIST))
			return new InListRestrictioin(arg1, arg2);
		if (comparartor.equals(CMP.SUBCLASS))
			return new SubclassRestrictioin(arg1, arg2);
		
		throw new RuntimeException(String.format("Restricition not supported: %s", comparartor));
	}

	
	
	public abstract static class AttrRestrictioin extends Restrictioin {
		protected String attr, value;

		public AttrRestrictioin(String attr, String value) {
			this.attr = attr;
			this.value = value;
		}
		
		@Override
		public boolean evaluate(QueryData data, int nodeID) {
			Object dataValue = data.getNodeAttributes().getValue(nodeID, attr);
			return evaluate(dataValue);
		}

		public abstract boolean evaluate(Object dataValue);

		@Override
		public String getAttrName() {
			return attr;
		}

		@Override
		public String getValueString() {
			return value;
		}

	}

	public static class EqualRestrictioin extends AttrRestrictioin {

		public EqualRestrictioin(String attr, String value) {
			super(attr, value);
		}

		@Override
		public boolean evaluate(Object dataValue) {
			if (dataValue == null) return false;
			return value.equals(dataValue.toString());
		}

		@Override
		public String getComparator() { return CMP.EQ; }

	}

	public static class SubclassRestrictioin extends AttrRestrictioin {

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

	public static class InListRestrictioin extends AttrRestrictioin {

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

	public static class NotEqualRestrictioin extends EqualRestrictioin {

		public NotEqualRestrictioin(String attr, String value) {
			super(attr, value);
		}
		
		@Override
		public boolean evaluate(QueryData data, int nodeID) {
			return ! super.evaluate(data, nodeID);
		}

	}

	public static class RegExpRestrictioin extends AttrRestrictioin {

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
}