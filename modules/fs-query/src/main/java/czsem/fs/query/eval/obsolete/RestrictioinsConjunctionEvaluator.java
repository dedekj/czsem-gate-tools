package czsem.fs.query.eval.obsolete;


public class RestrictioinsConjunctionEvaluator /*extends AbstractEvaluator*/ {
	/*
	
	public static RestrictioinsConjunctionEvaluator restrictioinsConjunctionEvaluatorInstance = new RestrictioinsConjunctionEvaluator();
	
	public static boolean evalRestricitons(QueryData data, QueryNode queryNode, int dataNodeId)
	{
		for (DirectAttrRestriction r : queryNode.getDirectRestrictions())
		{
			if (! r.evaluate(data, dataNodeId)) return false;
		}
		return true;
	}
	
	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		if (! evalRestricitons(data, queryNode, dataNodeId))
			return null;
		
		List<NodeMatch> matchingNodes = Arrays.asList(
				new NodeMatch(dataNodeId, queryNode));
		
		return Arrays.asList(new QueryMatch(matchingNodes)); 
	}

*/
}
