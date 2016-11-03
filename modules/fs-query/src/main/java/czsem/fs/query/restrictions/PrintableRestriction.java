/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

public interface PrintableRestriction extends HasComparator {
	public abstract String getLeftArg();
	public abstract String getRightArg();
}
