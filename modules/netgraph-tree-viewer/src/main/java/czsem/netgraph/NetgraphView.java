package czsem.netgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

import javax.swing.JComponent;

import czsem.netgraph.TreeSource.NodeLabel;

public class NetgraphView<E> extends JComponent {
	private static final long serialVersionUID = 5301240098183332164L;
	private final TreeSource<E> treeSource;
	
	public static class Sizing {

		public static final int BORDER = 20;
		public static final int NODE_V_SPACE = 60;
		public static final int NODE_H_SPACE = 40;
		public static final int NODE_DIAM = 30; 
		
	}
	
	private E [] nodes;
	private int [] edges;
	private int [] x;
	private int [] y;

	public NetgraphView(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
		setPreferredSize(new Dimension(900, 500));
		computePaintData();
	}

	public static class Depth<E> {
		public E node;
		public int depth;
		public int nodeIndex;

		public Depth(E node, int depth, int nodeIndex) {
			this.node = node;
			this.depth = depth;
			this.nodeIndex = nodeIndex;
		}
	}
	
	protected void computePaintData() {
		List<Integer> edges = new ArrayList<>();
		List<Depth<E>> list = new ArrayList<>();
		Stack<Depth<E>> stack = new Stack<>();
		E root = treeSource.getRoot();

		int index = 0;

		stack.push(new Depth<>(root, 0, index));
		list.add(stack.peek());
		
		while (! stack.isEmpty()) {
			Depth<E> pop = stack.pop();
			
			List<E> children = treeSource.getChildren(pop.node);
			
			for (E ch : children) {
				index++;
				stack.push(new Depth<E>(ch, pop.depth+1, index));
				list.add(stack.peek());
				
				edges.add(pop.nodeIndex);
				edges.add(index);
			}
		}
		
		this.edges = edges.stream().mapToInt(Integer::intValue).toArray();
		
		//copy nodes
		@SuppressWarnings("unchecked")
		E[] tmp = (E[]) new Object[list.size()];
		this.nodes = tmp;
		for (int j = 0; j < tmp.length; j++) {
			this.nodes[j] = list.get(j).node;
		}

		//sort nodes
		Comparator<E> cmp = treeSource.getOrderComparator();
		Integer [] sortOrder = IntStream.range(0, tmp.length).boxed().toArray(Integer[]::new);
		Arrays.sort(sortOrder, (a, b) -> cmp.compare(this.nodes[a], this.nodes[b]));
		int [] nodeOrder =  new int[tmp.length];
		for (int r = 0; r < nodeOrder.length; r++) {
			nodeOrder[sortOrder[r]] = r;
		}
		

		//compute coordinates
		this.x = new int[tmp.length];
		this.y = new int[tmp.length];
		
		for (int j = 0; j < tmp.length; j++) {
			Depth<E> jNodeDepth = list.get(j);
			this.y[j] = Sizing.BORDER+Sizing.NODE_DIAM/2  +  jNodeDepth.depth * Sizing.NODE_V_SPACE;
			this.x[j] = Sizing.BORDER+Sizing.NODE_DIAM/2  +  nodeOrder[j] * Sizing.NODE_H_SPACE;
		}
	}

	@Override
	protected void paintComponent(Graphics gParam) {
		Graphics2D g = (Graphics2D) gParam;
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.BLACK);
		
		Dimension pref = getPreferredSize();
		
		//debug border
		g.drawRect(Sizing.BORDER, Sizing.BORDER, pref.width-Sizing.BORDER*2, pref.height-Sizing.BORDER*2);

		g.setStroke(new BasicStroke(3));
		
		//edges
		for (int i = 0; i < edges.length; i+=2) {
			int a = edges[i];
			int b = edges[i+1];
			g.drawLine(x[a], y[a], x[b], y[b]);
		}
		
		//nodes
		for (int i = 0; i < nodes.length; i++) {
			int nodeX = x[i];
			int nodeY = y[i];
			g.drawOval(nodeX - Sizing.NODE_DIAM/2, nodeY - Sizing.NODE_DIAM/2, Sizing.NODE_DIAM, Sizing.NODE_DIAM);
			
			List<NodeLabel> labels = treeSource.getLabels(this.nodes[i]);
			for (int l = 0; l < labels.size(); l++) {
				NodeLabel lbl = labels.get(l);
				g.drawString(
						lbl.getLeftPart() + lbl.getMiddle() + lbl.getRightPart(), 
						nodeX, nodeY + 25);
			}
		}
	}

}
