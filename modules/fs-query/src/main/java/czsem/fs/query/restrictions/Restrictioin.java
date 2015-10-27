package czsem.fs.query.restrictions;

import czsem.fs.query.FSQuery.QueryData;

public class Restrictioin {
	public boolean evaluate(QueryData data, int nodeID) {
		return true;
	}
	
	public static Restrictioin createRestriction(String comparartor, String arg1,	String arg2) {
		if (comparartor.equals("="))
			return new EqualRestrictioin(arg1, arg2);
		if (comparartor.equals("~="))
			return new RegExpRestrictioin(arg1, arg2);
		if (comparartor.equals("!="))
			return new NotEqualRestrictioin(arg1, arg2);
		if (comparartor.equals("@="))
			return new InListRestrictioin(arg1, arg2);
		
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

		protected abstract boolean evaluate(Object dataValue);

	}

	public static class EqualRestrictioin extends AttrRestrictioin {

		public EqualRestrictioin(String attr, String value) {
			super(attr, value);
		}

		@Override
		protected boolean evaluate(Object dataValue) {
			if (dataValue == null) return false;
			return value.equals(dataValue.toString());
		}

	}

	public static class InListRestrictioin extends AttrRestrictioin {

		private String[] values;

		public InListRestrictioin(String attr, String value) {
			super(attr, value);
			values = value.split(";");
		}

		@Override
		protected boolean evaluate(Object dataValue) {
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

		public RegExpRestrictioin(String attr, String value) {
			super(attr, value);
		}

		@Override
		protected boolean evaluate(Object dataValue) {
			if (dataValue == null) return false;
			return dataValue.toString().matches(value);
		}
	}

}