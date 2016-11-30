package czsem.fs.query;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import czsem.fs.NodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.fs.query.eval.FsEvaluator;

public class FSQuery {
	
	public static class QueryData {
		protected TreeIndex index;
		protected NodeAttributes nodeAttributes;		

		public QueryData(TreeIndex index, NodeAttributes nodeAttributes) {
			this.index = index;
			this.nodeAttributes = nodeAttributes;
		}

		public TreeIndex getIndex() {
			return index;
		}

		public void setIndex(TreeIndex index) {
			this.index = index;
		}

		public NodeAttributes getNodeAttributes() {
			return nodeAttributes;
		}

		public void setNodeAttributes(NodeAttributes nodeAttributes) {
			this.nodeAttributes = nodeAttributes;
		}
		
		public SortedMap<String,SortedSet<String>> buildAttrIndex() {
			SortedMap<String, SortedSet<String>> attrIndex = new TreeMap<String, SortedSet<String>>();
			
			for (int n : index.getAllNodes())
			{
				Iterable<Entry<String, Object>> entries = nodeAttributes.get(n);
				for (Entry<String, Object> e: entries)
				{ 
					SortedSet<String> previous = attrIndex.get(e.getKey());
					if (previous == null) previous = new TreeSet<String>();
					
					previous.add(e.getValue().toString());
					attrIndex.put(e.getKey(), previous);
				}
			}
			
			return attrIndex;
		}
	}
	
	public static class MatchingNode {

		protected int nodeId;

		public MatchingNode(int nodeId) {
			this.nodeId = nodeId;
		}

		public int getNodeId() {
			return nodeId;
		}		
	}

	public static class NodeMatch extends MatchingNode{
		protected QueryNode queryNode;

		public NodeMatch(int nodeId, QueryNode queryNode) {
			super(nodeId);
			this.queryNode = queryNode;
		}
		
		@Override
		public String toString() {			
			return queryNode.toString() + ": " + nodeId;
		}

		public QueryNode getQueryNode() {
			return queryNode;
		}		
	}

	public static class QueryMatch {
		private final List<NodeMatch> matchingNodes;
		private final QueryNode query;
		public QueryMatch( List<NodeMatch> matchingNodes, QueryNode query ) {
			this.matchingNodes = matchingNodes; 
			this.query = query;
		}
		public QueryMatch( NodeMatch onlyMatchingNode, QueryNode query) { 
			this.matchingNodes = Collections.singletonList(onlyMatchingNode); 
			this.query = query;
		}
		public List<NodeMatch> getMatchingNodes() {return matchingNodes; }
		public QueryNode getQuery() { return query;}
	}
	
	/*
	public static abstract class AbstractEvaluator {
		public abstract Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId);

		public void reset() {}
	}
	*/
	
	public static enum OptionalEval { ALL, MAXIMAL, MINIMAL  }; 

	public static class QueryObject {
		protected QueryNode queryNode;
		protected String queryName = null;
		protected List<QueryNode> optionalNodes;
		protected OptionalEval optionalEval = OptionalEval.MAXIMAL; 

		public QueryObject(QueryNode queryNode, List<QueryNode> optionalNodes) {
			this.queryNode = queryNode;
			this.optionalNodes = optionalNodes;
		}
		
		public static Iterable<QueryMatch> evaluatePatternPriorityList(
				List<QueryObject> objs,  QueryData data) {
			
			List<FsEvaluator> evaluators = objs.stream()
				.map(obj -> new FsEvaluator(obj.queryNode, obj.optionalNodes, data))
				.collect(Collectors.toList());
			
			return FsEvaluator.evaluatePatternPriorityList(evaluators, data);
		}
		
		public Iterable<QueryMatch> evaluate(QueryData data) {
			FsEvaluator eval = new FsEvaluator(queryNode, optionalNodes, data);
			eval.setOptionalEval(getOptionalEval());
			return eval.evaluate();
			
			/*
			List<Iterable<QueryMatch>> res = new ArrayList<Iterable<QueryMatch>>();
			
			PriorityQueue<Integer> sortedNodes = new PriorityQueue<Integer>(data.getIndex().getAllNodes()) ;
			
			while (! sortedNodes.isEmpty())
			{
				int id = sortedNodes.remove();

				queryNode.reset();
				Iterable<QueryMatch> i = queryNode.getFinalResultsFor(data, id);
				if (i != null) res.add(i);
			}
			
			@SuppressWarnings("unchecked")
			Iterable<QueryMatch>[] gt = (Iterable<QueryMatch>[]) new Iterable[0];
			
			return Iterables.concat(res.toArray(gt));
			*/
		}
		
		/*

		public boolean isNodeMatching(Integer nodeId, QueryData data) {
			queryNode.reset();
			Iterable<QueryMatch> i = queryNode.getFinalResultsFor(data, nodeId);
			return (i != null && i.iterator().hasNext());
		}

		public QueryMatch getFirstMatch(Integer nodeId, QueryData data) {
			queryNode.reset();
			Iterable<QueryMatch> i = queryNode.getFinalResultsFor(data, nodeId);
			if (i == null || ! i.iterator().hasNext()) return null;
			
			return i.iterator().next();
		}
		*/

		public String getQueryName() {
			return queryName;
		}

		public void setQueryName(String queryName) {
			this.queryName = queryName;
		}

		public QueryNode getRootNode() {
			return queryNode;
		}

		@Deprecated
		public QueryMatch getFirstMatch(Integer dataNodeId, QueryData qd) {
			return new FsEvaluator(queryNode, optionalNodes, qd).getFinalResultsFor(dataNodeId).next();
		}

		@Deprecated
		public boolean isNodeMatching(Integer dataNodeId, QueryData qd) {
			return new FsEvaluator(queryNode, optionalNodes, qd).getFinalResultsFor(dataNodeId).hasNext();
		}

		public OptionalEval getOptionalEval() {
			return optionalEval;
		}

		public void setOptionalEval(OptionalEval optionalEval) {
			this.optionalEval = optionalEval;
		}
		
	}
	
	public static QueryObject buildQuery(String queryString) throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		p.parse(queryString);
		
		QueryObject qo = new QueryObject(b.getRootNode(), b.getOptionalNodes());
		qo.setQueryName(queryString);
		return qo;		
	}
}
