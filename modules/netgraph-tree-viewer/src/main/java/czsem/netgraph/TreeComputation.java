package czsem.netgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import czsem.netgraph.treesource.TreeSource;

public class TreeComputation<E> {
	private final TreeSource<E> treeSource;
	private final List<NodeInfo<E>> nodes = new ArrayList<>();
	private List<Integer> balacedOrder;
	private int maxDepth = -1;
	private List<Integer>[] nodesByDepth;

	public TreeComputation(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
	}
	
	public static class NodeInfo<E> {
		final public E node;
		final public int depth;
		final public int nodeIndex;
		final public int parentIndex;
		public int numDescendants = 0;
		final public List<Integer> childrenIndexes = new ArrayList<>();

		public NodeInfo(E node, int depth, int nodeIndex, int parentIndex) {
			this.node = node;
			this.depth = depth;
			this.nodeIndex = nodeIndex;
			this.parentIndex = parentIndex;
		}

		@Override
		public String toString() {
			return node.toString();
		}
	}

	
	public void compute() {
		addNodeAndCountDescendants(treeSource.getRoot(), 0, -1);
		
		findNodesByDepth();
	}


	protected void findNodesByDepth() {
		nodesByDepth = newArray(maxDepth+1);
		for (int i = 0; i < nodesByDepth.length; i++) {
			nodesByDepth[i] = new ArrayList<>();
		}
		
		for (NodeInfo<E> node : nodes) {
			nodesByDepth[node.depth].add(node.nodeIndex);
		}
	}


	protected int addNodeAndCountDescendants(E parent, int depth, int parentIndex) {
		if (depth > maxDepth) maxDepth = depth;
		
		int index = nodes.size();

		if (parentIndex != -1) {
			nodes.get(parentIndex).childrenIndexes.add(index);
		}
		
		NodeInfo<E> info = new NodeInfo<>(parent, depth, index, parentIndex);
		nodes.add(info);
		
		int descendants = 0;
		Collection<E> children = treeSource.getChildren(parent);
		if (children != null) {
			for (E ch : children) {
				descendants += 1 + addNodeAndCountDescendants(ch, depth+1, index);
			}
		}
		
		info.numDescendants = descendants;
		
		return descendants;
	}


	public int[] collectEdges() {
		int[] ret = new int[(nodes.size()-1)*2];
		
		int index = 0;
		for (NodeInfo<E> i : nodes) {
			if (i.parentIndex == -1) continue;
			
			ret[index++] = i.parentIndex;
			ret[index++] = i.nodeIndex;
		}
		
		return ret;
	}


	public E[] collectNodes() {
		E[] ret = newArray(nodes.size());
		
		int index = 0;
		for (NodeInfo<E> i : nodes) {
			ret[index++] = i.node; 
		}
		
		return ret;
	}


	public int[] contNodeOrder() {
		int[] ret = new int[nodes.size()];
		
		
		Integer[] sortOredr =
				treeSource.getOrderComparator() == null
			?
				computeBalacedOrder()
			:
				computeOrder()
		;
				
		for (int r = 0; r < ret.length; r++) {
			ret[sortOredr[r]] = r;
		}

		return ret;
	}


	protected Integer[] computeOrder() {
		Comparator<E> cmp = treeSource.getOrderComparator();
		Integer [] sortOrder = IntStream.range(0, nodes.size()).boxed().toArray(Integer[]::new);
		Arrays.sort(sortOrder, (a, b) -> cmp.compare(nodes.get(a).node, nodes.get(b).node));
		return sortOrder;
	}


	protected Integer[] computeBalacedOrder() {
		balacedOrder = new ArrayList<>(nodes.size());
		addToBalacedOrder(0);
		
		return balacedOrder.toArray(new Integer[balacedOrder.size()]);
	}


	protected void addToBalacedOrder(int nodeIndex) {
		NodeInfo<E> nodeInfo = nodes.get(nodeIndex);
		if (nodeInfo.numDescendants == 0) {
			balacedOrder.add(nodeIndex);
			return;
		}
		
		//find best split index
		int numChildern = nodeInfo.childrenIndexes.size();
		int bestSplitIndex = 0;
		int minDiff = Integer.MAX_VALUE;
		int left = 0;
		for (int splitIndex = 0; splitIndex < numChildern; splitIndex++) {
			int diff = Math.abs(nodeInfo.numDescendants - left - splitIndex);
			if (diff < minDiff) {
				minDiff = diff;
				bestSplitIndex = splitIndex;
			}
			left += 1 + nodes.get(nodeInfo.childrenIndexes.get(splitIndex)).numDescendants;
		}

		//make the split
		for (int splitIndex = 0; splitIndex < numChildern; splitIndex++) {
			if (splitIndex == bestSplitIndex) {
				balacedOrder.add(nodeIndex);
			}
			addToBalacedOrder(nodeInfo.childrenIndexes.get(splitIndex));
		}
		
	}


	public int getDepth(int j) {
		return nodes.get(j).depth;
	}
	
	
	
	@SafeVarargs
	public static <E> E[] newArray(int length, E... array)
	{
	    return Arrays.copyOf(array, length);
	}
	
}

