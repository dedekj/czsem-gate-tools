package czsem.fs.query.eval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import czsem.fs.query.QueryNode;
import czsem.fs.query.utils.Combinator;
import czsem.fs.query.utils.QueryNodeDuplicator;
import czsem.fs.query.utils.ReverseCombinator;

public class OptionalNodesRemoval implements Iterator<QueryNode> {

	protected final QueryNode rootNode;
	protected final List<QueryNode> optionalNodes;
	protected Combinator combinator;
	protected boolean loaded = false;
	
	public OptionalNodesRemoval(QueryNode rootNode, List<QueryNode> optionalNodes) {
		this.rootNode = rootNode;
		this.optionalNodes = optionalNodes;
		this.combinator = new Combinator(optionalNodes.size());
	}

	public static Iterable<QueryNode> iterateModifiedQueries(QueryNode rootNode, List<QueryNode> optionalNodes) {
		return () -> new OptionalNodesRemoval(rootNode, optionalNodes);
	}
	
	@Override
	public boolean hasNext() {
		if (loaded) return true;
		loaded = combinator.tryMove(); 
		return loaded;
	}

	@Override
	public QueryNode next() {
		if (! hasNext()) throw new NoSuchElementException();
		loaded = false;
		
		int removalSize = combinator.getGroupSize();
		int[] removalIndcies = combinator.getStack();
		
		Set<QueryNode> toRemove = new HashSet<>();
		for (int i = 0; i < removalSize; i++) {
			toRemove.add(optionalNodes.get(removalIndcies[i]));
		}
		
		QueryNodeDuplicator dup = new QueryNodeDuplicator(toRemove);
		QueryNode dupNode = dup.duplicate(rootNode);
		
		for (QueryNode toRemoveNode : dup.getToRemoveDup()) {
			dupNode = removeNode(dupNode, toRemoveNode);
		}
		
		return dupNode;
	}

	protected QueryNode removeNode(QueryNode rootNode, QueryNode toRemove) {
		QueryNode parent = toRemove.getPrent();
		List<QueryNode> children = toRemove.getChildren();
		
		if (parent == null) {
			if (children.size() != 1)
				throw new IllegalArgumentException("Optional root node has to have exactly one child, but found: " + toRemove.getChildren());
			else
				return children.get(0);
		}
		
		parent.getChildren().remove(toRemove);
		
		for (QueryNode ch : children) {
			parent.addChild(ch);
		}
			
		return rootNode;
	}

	public static void main(String[] args) {
		Combinator c = new ReverseCombinator(5);
		
		while (c.tryMove()) {
			int gs = c.getGroupSize();
			System.err.println(Arrays.toString(Arrays.copyOfRange(c.getStack(), 0, gs)));
		}
	}


}
