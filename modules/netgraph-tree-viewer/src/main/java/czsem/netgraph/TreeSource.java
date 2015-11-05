package czsem.netgraph;

import java.util.Comparator;
import java.util.List;

public interface TreeSource<E> {
	
	public interface NodeLabel {
		String getLeftPart();
		String getMiddle();
		String getRightPart();
	}
	
	public E getRoot();
	public List<E> getChildren(E parent);
	public List<NodeLabel> getLabels(E node);
	public Comparator<E> getOrderComparator();
	
	public static class MiddleLabel implements NodeLabel {

		private final String text;

		public MiddleLabel(String text) {
			this.text = text;
		}
		
		@Override
		public String getMiddle() {	return text; }
		@Override
		public String getLeftPart() { return ""; }
		@Override
		public String getRightPart() { return ""; }
	}

}
