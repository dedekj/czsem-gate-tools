package czsem.netgraph;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryObject;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.fs.query.QueryNode;
import czsem.netgraph.treesource.FSQueryTreeSource;
import thirdparty.JTextPaneWithUndo;
import thirdparty.ListAction;

public class NetgraphQueryDesigner extends Container  {
	private static final long serialVersionUID = 3771937513564105054L;

	private static final String DEFAULT_QUERY_STRING = "[attr_name=attr_value]";

	public static void main(String[] args) {
		JFrame fr = new JFrame(NetgraphQueryDesigner.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
	    NetgraphQueryDesigner qd = new NetgraphQueryDesigner();
		qd.initComponents();
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);

	}
	
	protected final FSQueryTreeSource treeSource = new FSQueryTreeSource();

	private NetgraphView<QueryNode> forestDispaly; 
	private JTextPaneWithUndo queryString;
	private JPanel panelBottom;
	private JList<String> attrNames;
	private JList<String> attrValues;
	
	//public AsIndexHelper asIndexHelper = new AsIndexHelper();
	private SortedMap<String, SortedSet<String>> attrIndex;

	public void initComponents() {
		trySetQueryString(DEFAULT_QUERY_STRING);
		
		setLayout(new BorderLayout());

		//forest
        forestDispaly = new NetgraphView<>(treeSource);
        //forestDispaly.setPreferredSize(new Dimension(500,500));
        
        //TODO
        //forestDispaly.setEmphasizeChosenNode(true);

        JScrollPane query_tree_view_scroll_pane = new JScrollPane(forestDispaly);
        query_tree_view_scroll_pane.setPreferredSize(new Dimension(500,400));
        query_tree_view_scroll_pane.setBorder(BorderFactory.createTitledBorder("query tree:"));
        
        attrNames = new JList<>();
        attrValues = new JList<>();
        JSplitPane attrsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, attrNames, attrValues);
        JScrollPane attrsScrollPane = new JScrollPane(attrsSplit);
        query_tree_view_scroll_pane.setPreferredSize(new Dimension(200,0));
        attrsScrollPane.setBorder(BorderFactory.createTitledBorder("attributes:"));
        //attrsPanel.setBorder(BorderFactory.createTitledBorder("attributes:"));

        initAttrListEvents();
        
        
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, attrsScrollPane, query_tree_view_scroll_pane); 
        
        add(centerSplit, BorderLayout.CENTER);
        
        
        panelBottom = new JPanel(new BorderLayout());
        add(panelBottom, BorderLayout.SOUTH);
        
        //query string
		queryString = new JTextPaneWithUndo();        
        queryString.setText(DEFAULT_QUERY_STRING);
		JPanel panel_query = new JPanel(new BorderLayout());
        panel_query.setBorder(BorderFactory.createTitledBorder("query string:"));
        panel_query.setLayout(new BorderLayout());
        panel_query.add(queryString, BorderLayout.CENTER);        
        panelBottom.add(panel_query, BorderLayout.CENTER);

        //buttonUpdate
        JButton buttonUpdate = new JButton("Update");
        buttonUpdate.addActionListener(new ActionListener() {
        	@Override public void actionPerformed(ActionEvent e) {onUpdateQueryButton();}});
        panelBottom.add(buttonUpdate, BorderLayout.EAST);

	}
	
	@SuppressWarnings("serial")
	protected void initAttrListEvents() {
		attrNames.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Object v = attrNames.getSelectedValue();
				if (v == null) {
					attrValues.setModel(emptyModel);
					return;
				}
				
				SortedSet<String> values = attrIndex.get(v.toString());
				if (values.size() < 3000){					
					attrValues.setModel(new ArrayListModel(values));
				} else {
					attrValues.setModel(emptyModel);					
				}
			}
		});
		
		new ListAction(attrNames, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) { onSelectAttrName(); }});

		new ListAction(attrValues, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) { onSelectAttrValue(); }});
	}

	protected void onSelectAttrValue() {
		insertTextToQuery(
				attrNames.getSelectedValue().toString()+
				"="+
				attrValues.getSelectedValue().toString()
				);		
	}

	protected void insertTextToQuery(String text) {
		int pos = queryString.getCaretPosition();
		String newString = new StringBuffer(queryString.getText()).insert(pos, text).toString();
		queryString.setText(newString);
		queryString.setCaretPosition(pos + text.length());
		queryString.requestFocusInWindow();
	}

	protected void onSelectAttrName() {
		insertTextToQuery(attrNames.getSelectedValue().toString());		
	}

	public JButton addSearchButton() {
        //buttonSearch
        JButton buttonSearch = new JButton("   Search !   ");
        JPanel p = new JPanel();
        p.add(buttonSearch);
        panelBottom.add(p, BorderLayout.SOUTH);
        
        return buttonSearch;
	}

	public String getQueryString()
	{
		return queryString.getText();
	}

	protected void onUpdateQueryButton() {
		trySetQueryString(getQueryString());
		forestDispaly.updateData();
		forestDispaly.repaint();
	}

	protected void trySetQueryString(String queryString) {
		try {
			QueryObject q = FSQuery.buildQuery(queryString);
			treeSource.setQueryObject(q);
		} catch (SyntaxError e) {
			//TODO create err view
			throw new RuntimeException(e);
		}
		
		/*
		try {
			String[] attrs = AttrsCollectorFSQB.collectAttributes(getQueryString());
			if (attrs.length == 0) attrs = new String [] {""};
			
			forestDispaly.setForest(attrs, getQueryString());
			
			for (int i = 0; i < attrs.length; i++) {
		        forestDispaly.addShownAttribute(attrs[i]);				
			}
			
			forestDispaly.repaint();		
		} catch (SyntaxError e) {
			throw new RuntimeException(e);
		}
		*/
	}

	public void setQueryString(String queryString) {
		this.queryString.setText(queryString);
		trySetQueryString(queryString);
	}

	/*
	public void setAs(AnnotationSet annotation_set) {
		asIndexHelper.setSourceAS(annotation_set);
		asIndexHelper.initIndex();
		fillAttrIndexAndNamesList();
	}
	*/
	
	@SuppressWarnings("serial")
	private static final class ArrayListModel extends AbstractListModel<String> {

		String [] values;

		public ArrayListModel(Collection<String> data) {
			this(data.toArray(new String[0]));
		}

		public ArrayListModel(String[] values) {
			this.values = values;
		}

		@Override
		public int getSize() {
			return values.length;
		}

		@Override
		public String getElementAt(int index) {
			return values[index];
		}
	}
	
	@SuppressWarnings("serial")
	public static final ListModel<String> emptyModel = new AbstractListModel<String>() {
		@Override
		public int getSize() { return 0; }
		@Override
		public String getElementAt(int index) {	return null;}
	};

	/*
	protected void fillAttrIndexAndNamesList() {
		attrIndex = asIndexHelper.createQueryData().buildAttrIndex();
		
		attrIndex.put(NGTreeHead.META_ATTR_NODE_NAME, new TreeSet<String>(Arrays.asList(new String [] {"subject", "predicate", "object"})));
		attrIndex.put(NGTreeHead.META_ATTR_OPTIONAL, new TreeSet<String>(Arrays.asList(new String [] {NGTreeHead.META_ATTR_OPTIONAL_TRUE, "false"})));
		
		attrNames.setModel(new ArrayListModel(attrIndex.keySet()));
	}
	*/

}
