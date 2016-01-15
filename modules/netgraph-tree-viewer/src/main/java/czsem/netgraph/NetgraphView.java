package czsem.netgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;

import czsem.netgraph.treesource.TreeSource;
import czsem.netgraph.treesource.TreeSource.NodeLabel;

public class NetgraphView<E> extends JComponent {
	private static final long serialVersionUID = 5301240098183332164L;
	private final TreeSource<E> treeSource;
	
	public static interface StringDrwer {
		void drawString(Graphics2D g, String str, int x, int y, int w, int h);
	}
	
	public static final StringDrwer REAL_DRAWER = new StringDrwer() {
		@Override
		public void drawString(Graphics2D g, String str, int x, int y, int w, int h) {
			/**/
			g.setColor(new Color(255, 255, 255, 200));
			g.drawString(str, x-1, y-1);
			g.drawString(str, x+1, y-1);
			g.drawString(str, x-1, y+1);
			g.drawString(str, x+1, y+1);

			g.setColor(Color.BLACK);
			/**/
			g.drawString(str, x, y);
		}
	};
	
	public static class Sizing {

		public static final int BORDER = 8;
		public static final int NODE_V_SPACE = 60;
		public static final int NODE_H_SPACE = 40;
		public static final int NODE_DIAM = 15; 
		
	}
	
	private E [] nodes;
	private int [] edges;
	private int [] x;
	private int [] y;
	private int max_x;
	private int max_y;
	private int min_x;
	private int min_y;
	private int x_shift;

	public NetgraphView(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
		
		addHierarchyListener(e -> {
			if((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0l) 
				computePaintData();
		});
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

		x_shift = 0;
		min_x = Integer.MAX_VALUE; min_y = Integer.MAX_VALUE;
		max_x = Integer.MIN_VALUE; max_y = Integer.MIN_VALUE;
		FontMetrics m = getFontMetrics(getFont());
		
		for (int j = 0; j < nodes.length; j++) {
			this.y[j] = Sizing.BORDER/2+Sizing.NODE_DIAM/2  +  cmp.getDepth(j) * Sizing.NODE_V_SPACE;
			this.x[j] = Sizing.BORDER/2+Sizing.NODE_DIAM/2  +  nodeOrder[j] * Sizing.NODE_H_SPACE;
			
			drawLabels(j, null, m, this::updateMinMax);
			
		}
		//System.err.format("%d %d %d %d\n", min_x, min_y, max_x, max_y);
		
		x_shift = Sizing.BORDER/2 - min_x;

		setPreferredSize(new Dimension(max_x + x_shift + Sizing.BORDER/2, max_y-Sizing.BORDER));
	}

	protected void updateMinMax(Graphics2D g, String str, int x, int y, int w, int h) {
		min_x = Math.min(min_x, x);
		max_x = Math.max(max_x, x+w);
		
		min_y = Math.min(min_y, y);
		max_y = Math.max(max_y, y+h);
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
		
		//debug border
		//Dimension pref = getPreferredSize();
		//g.drawRect(Sizing.BORDER/2, Sizing.BORDER/2, pref.width-Sizing.BORDER, pref.height-Sizing.BORDER);

		g.setStroke(new BasicStroke(2));

		g.setColor(Color.GRAY);
		
		//edges
		for (int i = 0; i < edges.length; i+=2) {
			int a = edges[i];
			int b = edges[i+1];
			g.drawLine(x[a]+x_shift, y[a], x[b]+x_shift, y[b]);
		}

		g.setColor(Color.BLACK);
		FontMetrics m = g.getFontMetrics();
		
		//nodes
		for (int i = 0; i < nodes.length; i++) {
			int nodeX = x[i]+x_shift;
			int nodeY = y[i];
			
			drowNodeCyrcle(g, nodeX, nodeY, Color.LIGHT_GRAY, Color.DARK_GRAY);
			
			drawLabels(i, g, m, REAL_DRAWER);
		}
	}

	protected void drawLabels(int i, Graphics2D g, FontMetrics m, StringDrwer strDrawer) {
		int nodeX = x[i]+x_shift;
		int nodeY = y[i];
		int fontSize = m.getHeight();

		List<NodeLabel> labels = treeSource.getLabels(this.nodes[i]);
		if (labels.isEmpty()) {
			strDrawer.drawString(g, "",	 nodeX, nodeY, 0, 0);
			return;
		}
		
		int max_msw = labels.stream().mapToInt(l -> m.stringWidth(l.getMiddle())).max().orElse(0);
		for (int l = 0; l < labels.size(); l++) {
			NodeLabel lbl = labels.get(l);
			
			int lsw = m.stringWidth(lbl.getLeftPart());
			int msw = m.stringWidth(lbl.getMiddle());
			int rsw = m.stringWidth(lbl.getRightPart());
			
			int dx = max_msw/2 + lsw;
			int y = nodeY + (Sizing.NODE_DIAM*16)/10 + l*fontSize;
			
			strDrawer.drawString(g, lbl.getLeftPart(),	 nodeX - dx,        y, lsw, fontSize);
			strDrawer.drawString(g, lbl.getMiddle(),	 nodeX - msw/2,     y, msw, fontSize);
			strDrawer.drawString(g, lbl.getRightPart(),  nodeX + max_msw/2, y, rsw, fontSize);
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

	public void updateData() {
		computePaintData();
	}

	public int selectNode(MouseEvent e) {
		// TODO Auto-generated method stub
		return -1;
	}

}
