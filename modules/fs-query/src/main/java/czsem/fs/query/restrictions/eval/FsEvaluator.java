package czsem.fs.query.restrictions.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Iterables;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.QueryNode;
import czsem.fs.query.restrictions.DirectAttrRestriction;
import czsem.fs.query.utils.CloneableIterator;
import czsem.fs.query.utils.SingletonIterator;

public class FsEvaluator {
	
	protected QueryNode rootNode;
	protected List<QueryNode> optionalNodes;
	protected QueryData data;

	public FsEvaluator(QueryNode rootNode, List<QueryNode> optionalNodes, QueryData data) {
		this.rootNode = rootNode;
		this.optionalNodes = optionalNodes;
		this.data = data;
	}

	public Iterable<QueryMatch> evaluate() {
		PriorityQueue<Integer> sortedDataNodes = new PriorityQueue<>(data.getIndex().getAllNodes());
		List<Iterable<QueryMatch>> iterables = new ArrayList<>();

		
		while (! sortedDataNodes.isEmpty())
		{
			int dataNodeId = sortedDataNodes.remove();
			
			CloneableIterator<QueryMatch> r = getFinalResultsFor(dataNodeId);
			if (r != null) iterables.add(r.toIterable());

		}

		@SuppressWarnings("unchecked")
		Iterable<QueryMatch>[] array = new Iterable[iterables.size()];
		
		return Iterables.concat(iterables.toArray(array));
	}

	public CloneableIterator<QueryMatch> getFinalResultsFor(int dataNodeId) {
		CloneableIterator<QueryMatch> res = getFilteredResultsFor(rootNode, dataNodeId);
		if ((res != null && res.hasNext()) || optionalNodes.isEmpty()) return res;
		
		for (QueryNode queryNode : OptionalNodesRemoval.iterateModifiedQueries(rootNode, optionalNodes)) {
			//System.err.println(queryNode.toStringDeep());
			res = getFilteredResultsFor(queryNode, dataNodeId);
			if (res != null && res.hasNext()) return res;
		}
		
		return null;
		
	}

	public CloneableIterator<QueryMatch> getFilteredResultsFor(QueryNode queryNode, int dataNodeId) {
		return ReferencingRestrictionsResultsIteratorFilter.filter(
				getDirectResultsFor(queryNode, dataNodeId), 
				data);
	}

	protected CloneableIterator<QueryMatch> getDirectResultsFor(QueryNode queryNode, int dataNodeId) {
		if (! evalDirectRestricitons(queryNode, dataNodeId))
			return null;
		
		NodeMatch thisMatch = new NodeMatch(dataNodeId, queryNode);
				
		if (queryNode.getChildren().isEmpty())
			return new SingletonIterator<>(new QueryMatch(thisMatch));
			
		List<QueryNode> chQueryNodes = queryNode.getChildren();
		Set<Integer> chDataNodes = data.getIndex().getChildren(dataNodeId);
		if (chDataNodes == null || chDataNodes.isEmpty()) return null;
		
		ChildrenMatchesIterator childrenMatches = 
				ChildrenMatchesIterator.getNonEmpty(thisMatch, chQueryNodes, chDataNodes, this);
		
		if (childrenMatches == null || ! childrenMatches.hasNext())
			return null;
		
		return childrenMatches;
	}
	
	public boolean evalDirectRestricitons(QueryNode queryNode, int dataNodeId)
	{
		for (DirectAttrRestriction r : queryNode.getDirectRestrictions())
		{
			if (! r.evaluate(data, dataNodeId)) return false;
		}
		return true;
	}


}
