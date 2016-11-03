/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

import java.util.Map;

import czsem.fs.query.FSQuery.QueryData;

public abstract class ReferencingRestriction implements PrintableRestriction {

	public boolean evaluate(QueryData data, int nodeId, Map<String, Integer> dataBindings) {
		// TODO Auto-generated method stub
		return false;
	}

}
