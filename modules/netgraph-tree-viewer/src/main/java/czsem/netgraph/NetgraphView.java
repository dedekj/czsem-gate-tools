package czsem.netgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JComponent;

import czsem.netgraph.TreeSource.NodeLabel;

public class NetgraphView<E> extends JComponent {
	private static final long serialVersionUID = 5301240098183332164L;
	private final TreeSource<E> treeSource;
	
	public static class Sizing {

		public static final int BORDER = 20;
		public static final int NODE_V_SPACE = 80;
		public static final int NODE_H_SPACE = 80;
		public static final int NODE_DIAM = 20; 
		
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
		public int parentIndex;
		public int numDescendants = 0;

		public Depth(E node, int depth, int nodeIndex, int parentIndex) {
			this.node = node;
			this.depth = depth;
			this.nodeIndex = nodeIndex;
			this.parentIndex = parentIndex;
		}
	}
	
	protected void computePaintData() {
		TreeComputation<E> cmp = new TreeComputation<>(treeSource);
		cmp.compute();
		
		this.edges = cmp.collectEdges();
		this.nodes = cmp.collectNodes();
		
		int [] nodeOrder = cmp.contNodeOrder();

		//compute coordinates
		this.x = new int[nodes.length];
		this.y = new int[nodes.length];
		
		for (int j = 0; j < nodes.length; j++) {
			//Depth<E> jNodeDepth = list.get(j);
			this.y[j] = Sizing.BORDER+Sizing.NODE_DIAM/2  +  cmp.getDepth(j) * Sizing.NODE_V_SPACE;
			this.x[j] = Sizing.BORDER+Sizing.NODE_DIAM/2  +  nodeOrder[j] * Sizing.NODE_H_SPACE;
		}
	}

	@Override
	protected void paintComponent(Graphics gParam) {
		Graphics2D g = (Graphics2D) gParam;
		
		g.setRenderingHint(	RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(	RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		/*
		g.setRenderingHint(	RenderingHints.KEY_FRACTIONALMETRICS,
							RenderingHints.VALUE_FRACTIONALMETRICS_ON);	 
		*/
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.BLACK);
		
		Dimension pref = getPreferredSize();
		
		//debug border
		g.drawRect(Sizing.BORDER, Sizing.BORDER, pref.width-Sizing.BORDER*2, pref.height-Sizing.BORDER*2);

		g.setStroke(new BasicStroke(2));

		g.setColor(Color.GRAY);
		
		//edges
		for (int i = 0; i < edges.length; i+=2) {
			int a = edges[i];
			int b = edges[i+1];
			g.drawLine(x[a], y[a], x[b], y[b]);
		}

		g.setColor(Color.BLACK);
		
		//nodes
		for (int i = 0; i < nodes.length; i++) {
			int nodeX = x[i];
			int nodeY = y[i];
			
			drowNodeCyrcle(g, nodeX, nodeY, Color.DARK_GRAY, Color.LIGHT_GRAY);
			
			List<NodeLabel> labels = treeSource.getLabels(this.nodes[i]);
			for (int l = 0; l < labels.size(); l++) {
				NodeLabel lbl = labels.get(l);
				g.drawString(
						lbl.getLeftPart() + lbl.getMiddle() + lbl.getRightPart(), 
						nodeX, nodeY + 25);
			}
		}
	}

	private static void drowNodeCyrcle(Graphics2D g, int nodeX, int nodeY, Color fill, Color border) {
		Color back = g.getColor();
		g.setColor(fill);
		g.fillOval(nodeX - Sizing.NODE_DIAM/2, nodeY - Sizing.NODE_DIAM/2, Sizing.NODE_DIAM, Sizing.NODE_DIAM);
		g.setColor(border);
		g.drawOval(nodeX - Sizing.NODE_DIAM/2, nodeY - Sizing.NODE_DIAM/2, Sizing.NODE_DIAM, Sizing.NODE_DIAM);
		g.setColor(back);
	}

}
