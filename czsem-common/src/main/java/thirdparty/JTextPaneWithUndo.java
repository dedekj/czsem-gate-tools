package thirdparty;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

/** @author http://faceforwardjava.blogspot.cz/2006/05/real-swing-jtextpane-undoredo.html} */
public class JTextPaneWithUndo extends JTextPane {
	private static final long serialVersionUID = 3464855138292933148L;

	protected UndoManager undo = new UndoManager();
	protected Document doc;
	protected TypingEdit lastEdit;

	public JTextPaneWithUndo() {
		
		getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent evt) {
		        if (evt.getEdit() instanceof AbstractDocument.DefaultDocumentEvent) {
		            if(lastEdit == null || !lastEdit.addEdit(evt.getEdit())) {
		                if (lastEdit != null)
		                    lastEdit.end();
		                lastEdit = new TypingEdit();
		                lastEdit.addEdit(evt.getEdit());
		                undo.addEdit(lastEdit);
		            }
		        } else {
		            undo.addEdit(evt.getEdit());
		        }
			}
		});

		getActionMap().put("Undo", new AbstractAction("Undo") {
			private static final long serialVersionUID = -45037587996530697L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				if (undo.canUndo()) {
					undo.undo();
				}
			}
		});
		getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		getActionMap().put("Redo", new AbstractAction("Redo") {
			private static final long serialVersionUID = 5987319539443324400L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				if (undo.canRedo()) {
					undo.redo();
				}
			}
		});
		getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

	}

	public static void main(String[] args) {
		JFrame myFrame = new JFrame("Undo/Redo Test Document");
		myFrame.add(new JTextPaneWithUndo());
		myFrame.setSize(640, 480);
		myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		myFrame.pack();
		myFrame.setVisible(true);
	}

}
