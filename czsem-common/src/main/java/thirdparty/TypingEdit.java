package thirdparty;

import javax.swing.event.DocumentEvent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

/** @author http://faceforwardjava.blogspot.cz/2006/05/real-swing-jtextpane-undoredo.html} */
public class TypingEdit extends CompoundEdit {
	private static final long serialVersionUID = -5293411826178030152L;

	public TypingEdit() {
    }

    @Override
	public boolean addEdit(UndoableEdit edit) {
        if(!(edit instanceof DocumentEvent))
            return false;

        if(0 == edits.size()) {
            edits.add(edit);
            return true;
        } else if(Math.abs((((DocumentEvent)edits.lastElement()).getOffset() - 
                ((DocumentEvent)edit).getOffset())) < 2) {
            edits.add(edit);
            return true;
        }
        return false;
    }

    @Override
    public String getPresentationName() {
        return "Typing";
    }

    @Override
    public String getUndoPresentationName() {
        return "Undo Typing";
    }

    @Override
    public String getRedoPresentationName() {
        return "Redo Typing";
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public boolean canRedo() {
        return true;
    }
}