/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

import czsem.fs.query.FSQuery.QueryData;

public abstract class DirectAttrRestriction extends AttrRestriction {

	protected String value;
	
	public DirectAttrRestriction(String attr, String value) {
		super(attr);
		this.value = value;
	}

	public String getValueString() {
		return value;
	}
	
	@Override
	public String getRightArg() {
		return getValueString();
	}

	public boolean evaluate(QueryData data, int nodeID) {
		Object dataValue = data.getNodeAttributes().getValue(nodeID, attr);
		return evaluate(dataValue);
	}

	public abstract boolean evaluate(Object dataValue);
	
	
	
	public static class DirectAttrRestrictionDecorator extends DirectAttrRestriction {
		
		protected final BasicRestriction parent;

		public DirectAttrRestrictionDecorator(BasicRestriction parent, String attr, String value) {
			super(attr, value);
			this.parent = parent;
		}

		@Override
		public String getComparator() {
			return parent.getComparator();
		}

		@Override
		public boolean evaluate(Object dataValue) {
			return parent.evaluate(dataValue, getValueString());
		}
		
	}
}
