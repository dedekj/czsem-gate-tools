package czsem.fs.query.restrictions.eval;

import java.util.Arrays;
import java.util.List;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.restrictions.DirectAttrRestriction;
import czsem.fs.query.QueryNode;

public class RestrictioinsConjunctionEvaluator extends AbstractEvaluator {
	
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
				new NodeMatch[] {new NodeMatch(dataNodeId, queryNode)} );
		
		return Arrays.asList(
				new QueryMatch [] {
						new QueryMatch(matchingNodes)}); 
	}


}
