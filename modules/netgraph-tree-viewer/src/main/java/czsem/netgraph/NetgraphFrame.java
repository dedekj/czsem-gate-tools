package czsem.netgraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class NetgraphFrame {
	
	public static class TestSource implements TreeSource<Integer> {

		@Override
		public Integer getRoot() {
			return 0;
		}

		@Override
		public List<Integer> getChildren(Integer parent) {
			switch (parent) {
			case 0:
				return Arrays.asList(1, 2, 3);
			case 1:
				return Arrays.asList(4, 5);
			case 2:
				return Arrays.asList(6);
			case 3:
				return Arrays.asList(7, 8);
			case 8:
				return Arrays.asList(9);
			}
			
			return Collections.emptyList();
		}

		@Override
		public List<NodeLabel> getLabels(Integer node) {
			return Collections.singletonList(new MiddleLabel(node.toString()));
		}

		@Override
		public Comparator<Integer> getOrderComparator() {
			return null;
			//return Integer::compare;
		}
		
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("NetgraphFrame");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
		frame.setSize(700, 500);
		frame.add(new NetgraphView<>(new TestSource()));
		frame.pack();
		frame.setVisible(true);

	}

}
