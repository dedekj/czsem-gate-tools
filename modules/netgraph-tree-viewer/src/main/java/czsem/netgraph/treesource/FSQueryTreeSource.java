package czsem.netgraph.treesource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import czsem.fs.query.FSQuery.QueryObject;
import czsem.fs.query.QueryNode;
import czsem.fs.query.restrictions.Restrictioin;

public class FSQueryTreeSource implements TreeSource<QueryNode> {
	
	protected QueryObject queryObject;

	public FSQueryTreeSource() {
		this(null);
	}
	
	public FSQueryTreeSource(QueryObject qo) {
		this.queryObject = qo;
	}

	@Override
	public QueryNode getRoot() {
		return queryObject.getRootNode();
	}

	@Override
	public List<QueryNode> getChildren(QueryNode parent) {
		return parent.getChildren();
	}
	
	public static class RestricitonLabel implements NodeLabel {

		protected final Restrictioin r;

		public RestricitonLabel(Restrictioin r) {
			this.r = r;
		}

		@Override
		public String getLeftPart() {
			return r.getAttrName();
		}

		@Override
		public String getMiddle() {
			return r.getComparator();
		}

		@Override
		public String getRightPart() {
			return r.getValueString();
		} 
		
	}

	@Override
	public List<NodeLabel> getLabels(QueryNode node) {
		return node.getRestricitions().stream()
				.map(r -> new RestricitonLabel(r))
			.collect(Collectors.toList());
	}

	@Override
	public Comparator<QueryNode> getOrderComparator() {
		return null;
	}

	public QueryObject getQueryObject() {
		return queryObject;
	}

	public void setQueryObject(QueryObject queryObject) {
		this.queryObject = queryObject;
	}

}
