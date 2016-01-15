package czsem.netgraph.treesource;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface TreeSource<E> {
	
	public static interface NodeLabel {
		String getLeftPart();
		String getMiddle();
		String getRightPart();
	}
	
	public E getRoot();
	public Collection<E> getChildren(E parent);
	public List<NodeLabel> getLabels(E node);
	public Comparator<E> getOrderComparator();
	
	public static class StaticLabel implements NodeLabel {

		private final String l;
		private final String m;
		private final String r;

		public StaticLabel(String l, String m, String r) {
			this.l = l;
			this.m = m;
			this.r = r;
		}

		public StaticLabel(String m) {
			this("", m, "");
		}
		
		@Override
		public String getMiddle() {	return m; }
		@Override
		public String getLeftPart() { return l; }
		@Override
		public String getRightPart() { return r; }
	}

}
