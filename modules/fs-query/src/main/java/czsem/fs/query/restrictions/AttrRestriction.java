/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

import czsem.fs.query.FSQuery.QueryData;

public abstract class AttrRestriction implements HasComparator {
	protected String attr;

	public AttrRestriction(String attr) {
		this.attr = attr;
	}
	
	public boolean evaluate(QueryData data, int nodeID) {
		Object dataValue = data.getNodeAttributes().getValue(nodeID, attr);
		return evaluate(dataValue);
	}

	public abstract boolean evaluate(Object dataValue);

	public String getAttrName() {
		return attr;
	}


}