package czsem.netgraph;

import gate.Annotation;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;

import java.util.Set;

import javax.swing.table.AbstractTableModel;

import czsem.netgraph.treesource.TreeIndexTreeSource;

public class GateAnnotTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1999028584101610952L;
	
	public static class ATTR {

		public static final String STRING = "_string";
		public static final String TYPE = "_type";
		public static final String STRAT = "_start";
		public static final String END = "_end";
		public static final String ID = "_id";
		
	}
	
	public static final String [] DEFAULT_ATTRS = {
		ATTR.STRING,
		ATTR.TYPE,
		ATTR.STRAT,
		ATTR.END,
		ATTR.ID,
	};
	

	protected TreeIndexTreeSource treeSource;
	protected Object[] lastSortedKeys;
	protected Annotation lastSelectedAnnot;
	
	public GateAnnotTableModel(TreeIndexTreeSource treeSource) {
		this.treeSource = treeSource;
	}


	public static Object getAnnotationAttr(Document d, Annotation a, Object attr) { 
		FeatureMap fm = a.getFeatures();
		if (fm.containsKey(attr)) return fm.get(attr);
		
		String str = attr.toString();
		
		switch (str) {
		case ATTR.STRING:	return Utils.stringFor(d, a);
		case ATTR.TYPE:		return a.getType();
		case ATTR.STRAT:	return a.getStartNode().getOffset();
		case ATTR.END:		return a.getEndNode().getOffset();
		case ATTR.ID:		return a.getId();
		default:			return null;
		}
	}
	

	@Override
	public int getRowCount() {
		return DEFAULT_ATTRS.length + treeSource.getSelectedAnnot().getFeatures().size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return treeSource.getSelectedAttributes().contains(getAttrByIndex(rowIndex));
		case 1:
			return getAttrByIndex(rowIndex);
		case 2:
			return getAnnotationAttr(treeSource.getDoc(), treeSource.getSelectedAnnot(), getAttrByIndex(rowIndex));
		}
		
		return null;
	}

	public Object getAttrByIndex(int index) {
		if (index < DEFAULT_ATTRS.length)
			return DEFAULT_ATTRS[index];
		
		updateLastSortedKeys();
		
		return lastSortedKeys[index-DEFAULT_ATTRS.length];
	}


	protected void updateLastSortedKeys() {
		
		Annotation curr = treeSource.getSelectedAnnot();
		
		if (curr.equals(lastSelectedAnnot)) return;
		
		Set<Object> keys = curr.getFeatures().keySet();
		lastSortedKeys = keys.stream().sorted((a,b) -> a.toString().compareTo(b.toString())).toArray(Object[]::new);
		
		lastSelectedAnnot = curr; 
	}


	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return ">";
		case 1:
			return "Attribute";
		default:
		case 2:
			return "Value";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		default:
			return String.class;
		}
	}


	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex != 0) return;
		
		Boolean selected = (Boolean) aValue;
		
		Object attr = getAttrByIndex(rowIndex);
		
		if (selected)
			treeSource.getSelectedAttributes().add(attr);
		else
			treeSource.getSelectedAttributes().remove(attr);
		
		
	}
	
}