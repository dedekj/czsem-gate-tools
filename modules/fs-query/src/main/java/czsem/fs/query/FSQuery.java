package czsem.fs.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Iterables;

import czsem.fs.NodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQueryParser.SyntaxError;

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
		private List<NodeMatch> matchingNodes;		
		public QueryMatch( List<NodeMatch> matchingNodes ) {this.matchingNodes = matchingNodes; }
		public List<NodeMatch> getMatchingNodes() {return matchingNodes; }

		public boolean evalReferencingRestrictions() {
			//TODO
			return true;
		}
	}
	
	public static abstract class AbstractEvaluator {
		public abstract Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId);

		public void reset() {}
	}
	

	public static class QueryObject {
		protected QueryNode queryNode;
		protected String queryName = null; 

		public QueryObject(QueryNode queryNode) {
			this.queryNode = queryNode;
		}
		
		public Iterable<QueryMatch> evaluate(QueryData data) {
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
		}

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

		public String getQueryName() {
			return queryName;
		}

		public void setQueryName(String queryName) {
			this.queryName = queryName;
		}

		public QueryNode getRootNode() {
			return queryNode;
		}
		
	}
	
	public static QueryObject buildQuery(String queryString) throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		p.parse(queryString);
		
		QueryObject qo = new QueryObject(b.getRootNode());
		qo.setQueryName(queryString);
		return qo;		
	}
}
