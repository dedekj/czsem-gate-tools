package czsem.fs.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;

public class SubtreeQueryNodeIterator implements Iterator<QueryMatch> {
	
	protected QueryData data;
	protected int parentDataNodeId;
	protected QueryNode queryNode;
	protected SubtreeQueryNodeIterator [] childrenIterators;
	protected QueryMatch[] lastResults;
	protected int[] children;
	protected boolean finish = false;
	
	
	protected State parentState = new ParentState();
	protected State childrenState = new ChildrenState();
	
	protected State state = parentState;
	protected boolean initialParentSate;
	private int maxDepth;
	
	public abstract class State { 
		public abstract State nextState();
		public boolean hasNext() {return ! finish; };
		public abstract QueryMatch next();
	}
	
	public class ParentState extends State {
		@Override
		public State nextState() {
			return childrenState;
		}

		@Override
		public QueryMatch next() {
			return new QueryMatch(new ArrayList<FSQuery.NodeMatch>(0));
		}
	}

	public class ChildrenState extends State {
		@Override
		public State nextState() {
			return childrenState;
		}

		@Override
		public QueryMatch next() {
			List<NodeMatch> ret = new ArrayList<FSQuery.NodeMatch>();
			
			if (initialParentSate)
				ret.add(new NodeMatch(parentDataNodeId, queryNode));
			
			for (QueryMatch m : lastResults) {
				ret.addAll(m.getMatchingNodes());
			}
			
			finish = ! tryMove(childrenIterators.length-1);
			
			return new QueryMatch(ret);
		}
	}
	

	public SubtreeQueryNodeIterator(int parentdataNodeId, QueryData data, QueryNode queryNode, boolean initialParentSate, int maxDepth) {
		this.data = data;
		this.parentDataNodeId = parentdataNodeId;
		this.queryNode = queryNode;
		this.initialParentSate = initialParentSate;
		this.maxDepth = maxDepth;
		
		if (maxDepth <= 0)
		{
			finish = true;
			return;
		}
		
		if (! initialParentSate) state = childrenState;
		
		Collection<Integer> childrenCollection = data.getIndex().getChildren(parentdataNodeId);
		if (childrenCollection == null) childrenCollection = new ArrayList<Integer>(0); 
		
		childrenIterators = new SubtreeQueryNodeIterator[childrenCollection.size()];
		lastResults = new QueryMatch[childrenCollection.size()];
		children = new int [childrenCollection.size()];
		
		int i = 0;
		for (int ch : childrenCollection) {
			childrenIterators[i] = new SubtreeQueryNodeIterator(ch, data, queryNode, true, maxDepth-1);
			children[i] = ch;
			lastResults[i] = childrenIterators[i].next();
			i++;
		}
	}

	public boolean tryMove(int i) {
		if (i < 0) return false;

		if (childrenIterators[i].hasNext()) {
			lastResults[i] = childrenIterators[i].next();
			return true;
		}
		
		//rewind
		childrenIterators[i] = new SubtreeQueryNodeIterator(children[i], data, queryNode, true, maxDepth-1);
		lastResults[i] = childrenIterators[i].next();
			
		return tryMove(i-1);
	}

	@Override
	public boolean hasNext() {
		return state.hasNext();
	}

	@Override
	public QueryMatch next() {
		QueryMatch ret = state.next();
		
		state = state.nextState();
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public SubtreeQueryNodeIterator createCopyOfInitialIteratorState() {
		return new SubtreeQueryNodeIterator(parentDataNodeId, data, queryNode, initialParentSate, maxDepth);
	}
}
