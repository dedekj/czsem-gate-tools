package czsem.fs.query;

import java.util.ArrayList;
import java.util.List;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.restrictions.ChildrenEvaluator;
import czsem.fs.query.restrictions.Restrictioin;

public class QueryNode  {
	
	protected List<Restrictioin> restricitions = new ArrayList<Restrictioin>();
	protected List<QueryNode> children = new ArrayList<QueryNode>();
	protected AbstractEvaluator evaluator;
	protected String name;
	
	public QueryNode(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	
	public QueryNode() {this(ChildrenEvaluator.childrenEvaluatorInstance);}

	public void setEvaluator(AbstractEvaluator evaluator) {		
		if (evaluator.getClass().isInstance(this.evaluator)) return;
		
		this.evaluator = evaluator;
	}

	public Iterable<QueryMatch> getResultsFor(QueryData data, int nodeId) {
		return evaluator.getResultsFor(data, this, nodeId);
	}

	public void addChild(QueryNode queryNode) {
		children.add(queryNode);			
	}

	public void addRestriction(String comparartor, String arg1,	String arg2) {
		addRestriction(Restrictioin.createRestriction(comparartor, arg1, arg2));
	}
	
	public void addRestriction(Restrictioin restrictioin) {
		restricitions.add(restrictioin);			
	}

	@Override
	public String toString() {
		if (getName() != null) return getName();
		return "QN_"+Integer.toString(hashCode(), Character.MAX_RADIX);
	}

	public List<Restrictioin> getRestricitions() {
		return restricitions;
	}

	public List<QueryNode> getChildren() {
		return children;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void reset() {
		evaluator.reset();
		for (QueryNode ch : getChildren())
		{
			ch.reset();
		}
	}
	
}