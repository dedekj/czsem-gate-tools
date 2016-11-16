package czsem.fs.query.restrictions.eval;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import czsem.fs.query.QueryNode;

public class OptionalNodesRemoval {

	public OptionalNodesRemoval(QueryNode rootNode, List<QueryNode> optionalNodes) {
		// TODO Auto-generated constructor stub
	}

	public Iterable<QueryNode> iterateModifiedQueries() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}
	
	public static class Combinator {

		protected final int stack[];
		protected final int size;
		protected int groupSize = 1;

		public Combinator(int size) {
			this.size = size;
			stack = new int[size+1];
			initGroup(1);
		}

		public boolean tryMove() {
			if (groupSize > size) return false;
			
			if (tryMove(groupSize-1)) return true;
			
			initGroup(groupSize+1);
			
			return tryMove();
		}


		protected void initGroup(int groupSize) {
			this.groupSize = groupSize;
			for (int j = 0; j < groupSize; j++) {
				stack[j] = j;
			}
			stack[groupSize-1]--;
		}

		protected boolean tryMove(int groupNum) {
			if (stack[groupNum] < size-groupSize+groupNum) {
				stack[groupNum]++;
				return true;
			}
			
			if (groupNum < 1) return false;
			
			//rewind
			if (! tryMove(groupNum-1)) return false;
			
			stack[groupNum] = stack[groupNum-1]+1;
			if (stack[groupNum] >= size) 
				return false;
			
			return true;
		}

		public int getGroupSize() {
			return groupSize;
		}

		public int[] getStack() {
			return stack;
		}
	}
	
	public static void main(String[] args) {
		Combinator c = new Combinator(5);
		
		while (c.tryMove()) {
			int gs = c.getGroupSize();
			System.err.println(Arrays.toString(Arrays.copyOfRange(c.getStack(), 0, gs)));
		}
	}

}
