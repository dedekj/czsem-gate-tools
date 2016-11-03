/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;


public abstract class AttrRestriction implements HasComparator, PrintableRestriction {
	protected String attr;

	public AttrRestriction(String attr) {
		this.attr = attr;
	}
	
	public String getAttrName() {
		return attr;
	}

	@Override
	public String getLeftArg() {
		return getAttrName();
	}


}