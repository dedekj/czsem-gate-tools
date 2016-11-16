package czsem.fs.query.restrictions.eval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.utils.CloneableIterator;
import czsem.fs.query.QueryNode;

public class ChildrenMatchesIterator implements CloneableIterator<QueryMatch> {
	
	protected static class QueryNodeState {
		final protected QueryNode queryNode;
		
		protected Iterator<Integer> dataBindingIterator;
		protected Iterator<QueryMatch> matchesIterator;
		protected QueryMatch lastMatch;

		public QueryNodeState(QueryNode queryNode) {
			this.queryNode = queryNode;
		}

		public void loadNextMatch() {
			lastMatch = matchesIterator.next();
			//System.err.println(lastMatch);
		}
	};

	protected final NodeMatch parentNodeMatch;
	protected final List<QueryNode> queryNodes;
	protected final Set<Integer> dataNodes;
	protected final FsEvaluator evaluator;
	protected final QueryNodeState[] queryNodeState;

	protected boolean foundNext = true;

	
	//private constructor
	private ChildrenMatchesIterator ( 
			NodeMatch parentNodeMatch, 
			List<QueryNode> queryNodes, 
			Set<Integer> chDataNodes, 
			FsEvaluator evaluator) 
	{
		this.queryNodes = queryNodes;
		this.parentNodeMatch = parentNodeMatch;
		this.dataNodes = chDataNodes;
		this.evaluator = evaluator;
		this.queryNodeState = new QueryNodeState[queryNodes.size()];
	}
	
	//public factory method
	public static ChildrenMatchesIterator getNonEmpty (			
			NodeMatch parentNodeMatch, 
			List<QueryNode> queryNodes, 
			Set<Integer> chDataNodes, 
			FsEvaluator evaluator) 
	{
		ChildrenMatchesIterator ret = new ChildrenMatchesIterator(parentNodeMatch, queryNodes, chDataNodes, evaluator);
		
		if (! ret.findFirstMatch()) 
			return null;
		
		return ret;
	}

	protected boolean findFirstMatch() {
		for (int i = 0; i < queryNodeState.length; i++) {
			QueryNodeState state = new QueryNodeState(queryNodes.get(i));
			rewind(state);
			if (state.matchesIterator == null) 
				return false;

			state.lastMatch = state.matchesIterator.next();
			
			queryNodeState[i] = state;
		}
		
		foundNext = true;
		return true;
	}

	protected void rewind(QueryNodeState state) {
		state.dataBindingIterator = dataNodes.iterator();
		state.matchesIterator = getResultsFor(state);
	}


	@Override
	public boolean hasNext() {
		if (foundNext) return true;
		
		if (tryMove(queryNodeState.length-1)) {
			return foundNext = true;
		}
	
		return false;
	}

	protected boolean tryMove(int i) {
		if (i < 0) return false;
		
		QueryNodeState state = queryNodeState[i];
		
		if (state.matchesIterator == null) return false;
		
		//try next result for same data
		if (state.matchesIterator.hasNext())
		{
			state.loadNextMatch();
			return true;
		}

		//try new result for new data
		state.matchesIterator = getResultsFor(state);
		if (state.matchesIterator != null) {
			state.loadNextMatch();
			return true;
		}

		
		//rewind and try new result for previous iterator group
		rewind(state);
		if (state.matchesIterator == null) {
			return false; //rewind failed
		}
		state.loadNextMatch();
		
		//try new result for previous iterator group
		return tryMove(i-1);		
	}


	protected Iterator<QueryMatch> getResultsFor(QueryNodeState state) {
		while (state.dataBindingIterator.hasNext())
		{
			CloneableIterator<QueryMatch> iterator = 
					evaluator.getDirectResultsFor(state.queryNode, state.dataBindingIterator.next());
			
			if (iterator != null && iterator.hasNext())
				return iterator;
		}
		return null;
	}


	@Override
	public QueryMatch next() {
		if (! hasNext()) throw new NoSuchElementException();
		
		foundNext = false;
		
		List<NodeMatch> matchingNodes = new ArrayList<>();
		matchingNodes.add(parentNodeMatch);
		
		for (QueryNodeState state : queryNodeState) {
			matchingNodes.addAll(state.lastMatch.getMatchingNodes());
			
		}
		
		return new QueryMatch(matchingNodes);
	}
	
	@Override
	public ChildrenMatchesIterator cloneInitial() {
		return getNonEmpty(parentNodeMatch, queryNodes, dataNodes, evaluator);
	}

}
