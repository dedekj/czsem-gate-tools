package czsem.fs.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.restrictions.DirectAttrRestriction;
import czsem.fs.query.restrictions.PrintableRestriction;
import czsem.fs.query.restrictions.ReferencingRestriction;
import czsem.fs.query.restrictions.Restrictions;
import czsem.fs.query.restrictions.eval.FsEvaluator;
import czsem.fs.query.utils.CloneableIterator;

public class QueryNode  {
	
	protected final List<PrintableRestriction> restrictions = new ArrayList<>();
	protected final List<DirectAttrRestriction> directRestrictions = new ArrayList<>();
	protected final List<ReferencingRestriction> referencingRestrictions = new ArrayList<>();
	
	protected List<QueryNode> children = new ArrayList<QueryNode>();
	private QueryNode prent; 
	
	//protected AbstractEvaluator evaluator;
	protected String name;
	protected boolean optional = false;
	protected int subtreeDepth = -1;
	
	/*
	public QueryNode(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public QueryNode() {this(ChildrenEvaluator.childrenEvaluatorInstance);}

	public void setEvaluator(AbstractEvaluator evaluator) {		
		if (evaluator.getClass().isInstance(this.evaluator)) return;
		
		this.evaluator = evaluator;
	}

	

	public Iterable<QueryMatch> getFinalResultsFor(QueryData data, int nodeId) {
		return ReferencingRestrictionsResultsIteratorFilter.filter(getResultsFor(data, nodeId), data);
	}
	
	public Iterable<QueryMatch> getResultsFor(QueryData data, int nodeId) {
		return evaluator.getResultsFor(data, this, nodeId);
	}
	*/

	public void addChild(QueryNode queryNode) {
		children.add(queryNode);
		queryNode.setPrent(this);
	}

	public void addRestriction(String comparartor, String arg1,	String arg2) {
		Restrictions.addRestriction(this, comparartor, arg1, arg2);
	}
	
	public void addDirectRestriction(DirectAttrRestriction restriction) {
		restrictions.add(restriction);
		directRestrictions.add(restriction);			
	}

	public void addReferencingRestriction(ReferencingRestriction restriction) {
		restrictions.add(restriction);
		referencingRestrictions.add(restriction);			
	}

	@Override
	public String toString() {
		if (getName() != null) return getName();
		return "QN_"+Integer.toString(hashCode(), Character.MAX_RADIX);
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
	
	/*
	
	public void reset() {
		evaluator.reset();
		for (QueryNode ch : getChildren())
		{
			ch.reset();
		}
	}
	*/

	public List<DirectAttrRestriction> getDirectRestrictions() {
		return directRestrictions;
	}

	public List<ReferencingRestriction> getReferencingRestrictions() {
		return referencingRestrictions;
	}

	public Collection<PrintableRestriction> getAllRestricitions() {
		return restrictions;
	}

	public QueryNode getPrent() {
		return prent;
	}

	public void setPrent(QueryNode prent) {
		this.prent = prent;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isOptional() {
		return optional;
	}

	public int getSubtreeDepth() {
		return subtreeDepth;
	}

	public void setSubtreeDepth(int subtreeDepth) {
		this.subtreeDepth = subtreeDepth;
	}

	@Deprecated
	public Iterable<QueryMatch> getFinalResultsFor(QueryData data, List<QueryNode> optionalNodes, int dataNodeId) {
		CloneableIterator<QueryMatch> i = new FsEvaluator(this, optionalNodes, data).getFinalResultsFor(dataNodeId);
		if (i == null) return null;
		return i.toIterable();
	}
	
}