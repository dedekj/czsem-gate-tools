package czsem.fs.query.eval;



public class IterateSubtreeEvaluator /*extends AbstractEvaluator*/ {
	public static final String META_ATTR_SUBTREE_DEPTH = "_subtree_eval_depth";
	/*
	protected Map<Integer,Integer> parentIdsAlreadyEvaluatedOnChild = new HashMap<Integer,Integer>();

	protected int depth = Integer.MAX_VALUE;
	
	public IterateSubtreeEvaluator(int depth) {
		this.depth = depth;
	}


	@Override
	public void reset() {
		parentIdsAlreadyEvaluatedOnChild = new HashMap<Integer,Integer>();		
	}


	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		if (! RestrictioinsConjunctionEvaluator.evalRestricitons(data, queryNode, dataNodeId))		
			return null;
		
		Integer parentId = data.getIndex().getParent(dataNodeId);
		if (parentIdsAlreadyEvaluatedOnChild.containsKey(parentId) && parentIdsAlreadyEvaluatedOnChild.get(parentId) != dataNodeId)
		{
			return null;
		}
		parentIdsAlreadyEvaluatedOnChild.put(parentId, dataNodeId);
		
		final SubtreeQueryNodeIterator mainIterator = new SubtreeQueryNodeIterator(parentId, data, queryNode, false, depth);
		
		if (! mainIterator.hasNext()) return null;
		
		return new Iterable<QueryMatch>(){

			@Override
			public Iterator<QueryMatch> iterator() {
				return mainIterator.createCopyOfInitialIteratorState();
			}
			
		};		
	}
	*/
}
