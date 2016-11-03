/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions.eval;

import java.util.Iterator;
import java.util.NoSuchElementException;

import czsem.fs.query.FSQuery.QueryMatch;

public class ReferencingRestrictionsResultsIteratorFilter implements Iterator<QueryMatch>{
	
	protected final Iterator<QueryMatch> parent;

	public ReferencingRestrictionsResultsIteratorFilter(Iterator<QueryMatch> parent) {
		this.parent = parent;
	}
	
	protected QueryMatch cachedValue = null; 

	@Override
	public boolean hasNext() {
		
		while (cachedValue == null) {
			if (! parent.hasNext()) return false;
			
			cachedValue = parent.next();

			if (! cachedValue.evalReferencingRestrictions()) {
				cachedValue = null;
			}
		}
		
		return true;
	}

	@Override
	public QueryMatch next() {
		if (! hasNext())
			throw new NoSuchElementException();
		
		QueryMatch ret = cachedValue;
		cachedValue = null;
		return ret;
	}

	public static Iterable<QueryMatch> filter(Iterable<QueryMatch> resultsFor) {
		if (resultsFor == null) return null;
		
		return new Iterable<QueryMatch>() {
			@Override
			public Iterator<QueryMatch> iterator() {
				return new ReferencingRestrictionsResultsIteratorFilter(resultsFor.iterator());
			}
		};
	} 
		
}
