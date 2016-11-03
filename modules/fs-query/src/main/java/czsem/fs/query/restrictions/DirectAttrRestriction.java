/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

public abstract class DirectAttrRestriction extends AttrRestriction implements PrintableRestriction {

	protected String value;
	
	public DirectAttrRestriction(String attr, String value) {
		super(attr);
		this.value = value;
	}

	public String getValueString() {
		return value;
	}
	
	@Override
	public String getLeftArg() {
		return getAttrName();
	}

	@Override
	public String getRightArg() {
		return getValueString();
	}
	
	
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
