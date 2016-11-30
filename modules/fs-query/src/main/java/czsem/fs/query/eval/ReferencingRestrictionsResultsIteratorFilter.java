/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.eval;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.restrictions.ReferencingRestriction;
import czsem.fs.query.utils.CloneableIterator;

public class ReferencingRestrictionsResultsIteratorFilter implements CloneableIterator<QueryMatch>{
	
	protected final CloneableIterator<QueryMatch> parent;
	protected final QueryData data;
	protected final int patternIndex;


	public ReferencingRestrictionsResultsIteratorFilter(CloneableIterator<QueryMatch> parent, QueryData data, int patternIndex) {
		this.parent = parent;
		this.data = data;
		this.patternIndex = patternIndex;
	}
	
	protected QueryMatch cachedValue = null; 

	@Override
	public boolean hasNext() {
		
		while (cachedValue == null) {
			if (! parent.hasNext()) return false;
			
			cachedValue = parent.next();

			if (! evalReferencingRestrictions(cachedValue)) {
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
		ret.setPatternIndex(patternIndex);
		return ret;
	}

	public static Map<String, Integer>  createDataBindings(QueryMatch queryMatch) {
		return queryMatch.getMatchingNodes().stream()
				
			.filter(n -> n.getQueryNode().getName() != null)
			.collect(Collectors.toMap(
					n -> n.getQueryNode().getName(), 
					NodeMatch::getNodeId)) 
		;
	}
	
	protected boolean evalReferencingRestrictions(QueryMatch queryMatch) {
		Map<String, Integer> dataBindings;
		
		try {
			dataBindings = createDataBindings(queryMatch);
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Failed to collect dataBindings, check for duplicated node name: "+ queryMatch.getMatchingNodes());
		}
		
		for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
			if (!evalReferencingRestrictions(nodeMatch, dataBindings))
				return false;
		}
		return true;
	}

	protected boolean evalReferencingRestrictions(NodeMatch nodeMatch, Map<String, Integer>  dataBindings) {
		for (ReferencingRestriction r : nodeMatch.getQueryNode().getReferencingRestrictions()) {
			if (! r.evaluate(data, nodeMatch.getNodeId(), dataBindings))
				return false;
		}
		
		return true;
	}

	public static CloneableIterator<QueryMatch> filter(CloneableIterator<QueryMatch> resultsFor, QueryData data, int patternIndex) {
		if (resultsFor == null) return null;
		
		return new ReferencingRestrictionsResultsIteratorFilter(resultsFor, data, patternIndex);
	}

	@Override
	public ReferencingRestrictionsResultsIteratorFilter cloneInitial() {
		return new ReferencingRestrictionsResultsIteratorFilter(
				parent.cloneInitial(), data, patternIndex);	
	} 
		
}
