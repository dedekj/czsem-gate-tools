package czsem.fs.query;

import java.util.Collections;
import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.NodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryObject;
import czsem.fs.query.eval.FsEvaluator;
import czsem.fs.query.utils.CloneableIterator;

public class FSQueryTest {
	@Test
	public static void getResultsFor() {
		QueryData data = buidQueryObject();
		
		QueryNode qn = new QueryNode();
		Iterable<QueryMatch> res = getFinalResultsFor(qn, data, 0);
		Assert.assertNotEquals(res, null);
		Assert.assertTrue(res.iterator().hasNext());

		qn.addRestriction("=", "id", "xxx");
		
		res = getFinalResultsFor(qn, data, 0);
		Assert.assertEquals(res, null);

		QueryNode qn1 = new QueryNode();
		QueryNode qn2 = new QueryNode();
		qn1.addChild(qn2);

		res = getFinalResultsFor(qn1, data, 0);
		Assert.assertNotEquals(res, null);
		Assert.assertTrue(res.iterator().hasNext());

		qn2.addRestriction("=", "id", "xxx");

		res = getFinalResultsFor(qn1, data, 0);
		Assert.assertEquals(res, null);
	}

	//TODO subtreeEval not working
	/*
	@Test
	public static void subtreeEval() {
		QueryData data = buidQueryObject();
		
		QueryNode qn = new QueryNode();
		qn.setSubtreeDepth(100);
		
		Iterable<QueryMatch> res = getFinalResultsFor(qn, data, 3);
		
		debugPrintResults(res);
		
		int[] results = {
				4,
				3,
				3, 4,
				3, 6,
				3, 6, 4 
				};
		
		evaluateQuery(data, qn, 3, results);
	}

	@Test
	public static void subtreeEvalMaxDepth() {
		QueryData data = buidQueryObject();
		
		QueryNode qn = new QueryNode();
		qn.setSubtreeDepth(2);
		
		Iterable<QueryMatch> res = getFinalResultsFor(qn, data, 1);
		
		
		debugPrintResults(res);
		int[] results = {
				7,
				2,
				2, 7,
				1, 
				1, 7,
				1, 2,
				1, 2, 7
				};
		
		evaluateQuery(data, qn, 1, results);
	}
	*/
	
	public static void debugPrintResults(Iterable<QueryMatch> res) {
		System.err.println("---------------------------------");
		for (QueryMatch r : res) {
			System.err.println(r.getMatchingNodes());
		}
	}

	public static int getNextNodeId(Iterator<QueryMatch> i, int matchIndex)
	{
		return i.next().getMatchingNodes().get(matchIndex).getNodeId();		
	}

	@Test
	public static void getResultsForConcurentIterators() {		 
		QueryData data = buidQueryObject();
		
		QueryNode qn1 = new QueryNode();
		QueryNode qn2 = new QueryNode();
		qn1.addChild(qn2);
		QueryNode qn3 = new QueryNode();
		qn2.addChild(qn3);

		Iterable<QueryMatch> res = getFinalResultsFor(qn1, data, 0);
		Assert.assertNotEquals(res, null);
		Iterator<QueryMatch> i = res.iterator();
		Assert.assertTrue(i.hasNext());
		Assert.assertTrue(i.hasNext());
		Assert.assertTrue(i.hasNext());
		
		for (QueryMatch queryMatch : res) {
			System.err.println(queryMatch.getMatchingNodes());
		}
		
		
		Iterator<QueryMatch> i1 = res.iterator();
		Iterator<QueryMatch> i2 = res.iterator();
		
		
		Assert.assertEquals(getNextNodeId(i1,2), 3);
		Assert.assertEquals(getNextNodeId(i1,2), 4);
		Assert.assertEquals(getNextNodeId(i2,2), 3);
		Assert.assertEquals(getNextNodeId(i1,2), 5);
		Assert.assertEquals(getNextNodeId(i2,2), 4);
		Assert.assertEquals(getNextNodeId(i2,2), 5);
	}

	
	@Test
	public static void testQuery() {
		QueryNode qn1 = new QueryNode();

		QueryNode qn2 = new QueryNode();
		
		qn1.addChild(qn2);
		qn2.addChild(new QueryNode());
		
		evaluateQuery(qn1);
	}

	public static void evaluateQuery(QueryNode q) {
		int results[] = {
				0, 1, 3,  
				0, 1, 4,
				0, 2, 5};
		
		evaluateQuery(q, results);		
	}

	public static void evaluateQuery(QueryNode queryNode, int[] results) {
		 QueryData data = buidQueryObject();
		 evaluateQuery(data, queryNode, results);
	}

	public static void evaluateQuery(QueryData data, QueryNode queryNode, int[] results) {
		evaluateQuery(data, queryNode, 0, results);
	}

	public static void evaluateQuery(QueryData data, QueryObject qo, int[] results) {
		Iterable<QueryMatch> res = qo.evaluate(data);
		/*
		CloneableIterator<QueryMatch> res = 
				new FsEvaluator(qo.queryNode, qo.optionalNodes, data)
					.getFinalResultsFor(0);
		
		checkResults(res == null? null : res.toIterable(), results);
		*/
		
		checkResults(res, results);
	}

	public static void evaluateQuery(QueryData data, QueryNode queryNode, int dataNodeId, int[] results) {
		Iterable<QueryMatch> res = getFinalResultsFor(queryNode, data, dataNodeId);
		checkResults(res, results);
	}

	public static void checkResults(Iterable<QueryMatch> res, int[] results) {
		int i = 0;
		int finishedNodeMatches = 0;
		if (res != null) {
			for (QueryMatch queryMatch : res) {
				
				System.err.println(queryMatch.getMatchingNodes());
				
				for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
					Assert.assertTrue(results.length > i, results.length +" - expected results are smaller - "+ i);
					Assert.assertEquals(nodeMatch.nodeId, results[i]);
					i++;
					finishedNodeMatches++;
				}
			}
		}
		
		Assert.assertEquals(finishedNodeMatches, results.length);
	}

	public static QueryData buidQueryObject() {
		TreeIndex index = new TreeIndex();
		
		index.addDependency(0,1);
		index.addDependency(0,2);
		index.addDependency(1,3);
		index.addDependency(1,4);
		index.addDependency(2,5);
		index.addDependency(3,6);
		index.addDependency(0,7);

		return new FSQuery.QueryData(index, new NodeAttributes.IdNodeAttributes());
	}
	
	public static Iterable<QueryMatch> getFinalResultsFor(QueryNode queryNode, QueryData data, int dataNodeId) {
		CloneableIterator<QueryMatch> i = new FsEvaluator(queryNode, Collections.emptyList(), data).getFinalResultsFor(dataNodeId);
		if (i == null) return null;
		return i.toIterable();
	}
}
