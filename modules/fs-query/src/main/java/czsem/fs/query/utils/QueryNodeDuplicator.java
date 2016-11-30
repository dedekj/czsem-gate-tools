package czsem.fs.query.utils;

import java.util.HashSet;
import java.util.Set;

import czsem.fs.query.QueryNode;

public class QueryNodeDuplicator {

	private final Set<QueryNode> toRemove;
	private final Set<QueryNode> toRemoveDup;

	public QueryNodeDuplicator(Set<QueryNode> toRemove) {
		this.toRemove = toRemove;
		toRemoveDup = new HashSet<>(toRemove.size());
	}
	
	public QueryNode duplicate(QueryNode node) {
		
		QueryNode ret = new QueryNode(node.getData());
		
		for (QueryNode chOrig : node.getChildren()) {
			QueryNode chDup = duplicate(chOrig);
			ret.addChild(chDup);
		}

		if (toRemove.contains(node))
			getToRemoveDup().add(ret);
		
		return ret;
	}

	public Set<QueryNode> getToRemoveDup() {
		return toRemoveDup;
	}
}
