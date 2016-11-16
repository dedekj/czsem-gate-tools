package czsem.fs.query.restrictions.eval;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import czsem.fs.query.QueryNode;
import czsem.fs.query.utils.Combinator;

public class OptionalNodesRemoval {

	public OptionalNodesRemoval(QueryNode rootNode, List<QueryNode> optionalNodes) {
		// TODO Auto-generated constructor stub
	}

	public Iterable<QueryNode> iterateModifiedQueries() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}
	
	public static void main(String[] args) {
		Combinator c = new Combinator(5);
		
		while (c.tryMove()) {
			int gs = c.getGroupSize();
			System.err.println(Arrays.toString(Arrays.copyOfRange(c.getStack(), 0, gs)));
		}
	}

}
