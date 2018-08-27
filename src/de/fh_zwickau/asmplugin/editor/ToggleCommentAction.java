package de.fh_zwickau.asmplugin.editor;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.Messages;

/**
 * Action to toggle a comment.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ToggleCommentAction extends Action {

  /** Refer to SourceViewer */
  private SourceViewer viewer;

  /**
   * The constructor.
   * 
   * @param viewer The SourceViewer for the corresponding ASM-Editor.
   */
  public ToggleCommentAction(SourceViewer viewer) {
    this.viewer = viewer;
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    IDocument document = viewer.getDocument();
    ISelection selection = viewer.getSelection();
    TextSelection textSelection;
    if (selection instanceof TextSelection) {
      textSelection = (TextSelection) selection;
      boolean isCommented = isCommented(document, textSelection);
      if (isCommented) {
        viewer.doOperation(ITextOperationTarget.STRIP_PREFIX);
      } else {
        viewer.doOperation(ITextOperationTarget.PREFIX);
      }
    }
  }

  /**
   * Check, if the selection in the given document is commented.
   * 
   * @param document The document.
   * @param selection The selection.
   * 
   * @return true, if commented. Otherwise false.
   */
  private boolean isCommented(IDocument document, TextSelection selection) {
    try {
      int startLine = selection.getStartLine();
      int endLine = selection.getEndLine();
      String firstChar;
      for (int line = startLine; line <= endLine; line++) {
        firstChar = document.get(document.getLineOffset(line), 1);
        if (!firstChar.equals(";")) {
          return false;
        }
      }
    } catch (BadLocationException e) {
      Activator.getDefault().getLog().log(
                                          new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                     Messages.BADLOCATION_ERROR, e));
    }

    return true;
  }
}
