package de.fh_zwickau.asmplugin.editor;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.Messages;

/**
 * The Reconciling Strategy for the ASM-Editor. Build the folding structure for
 * Folding.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMReconcilingStategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

  private IDocument document;

  private ASMEditor editor;

  /**
   * The constructor.
   * 
   * @param editor The ASM-Editor.
   */
  public ASMReconcilingStategy(ASMEditor editor) {
    this.editor = editor;
  }

  /**
   * {@inheritDoc}
   */
  public void setDocument(IDocument document) {
    this.document = document;
  }

  /**
   * {@inheritDoc}
   */
  public final void setProgressMonitor(final IProgressMonitor monitor) {
  }

  /**
   * Returns the document.
   * 
   * @return The document.
   */
  public IDocument getDocument() {
    return document;
  }

  /**
   * {@inheritDoc}
   */
  public void initialReconcile() {
    parse();
  }

  /**
   * {@inheritDoc}
   */
  public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
    parse();
  }

  /**
   * {@inheritDoc}
   */
  public void reconcile(IRegion partition) {
    parse();
  }

  /**
   * Parse the document for the folding structure.
   */
  private void parse() {
    if (document == null) {
      return;
    }

    editor.updateContentOutlinePage();

    final ArrayList<Position> positions = new ArrayList<Position>();

    try {
      int numberOfLines = document.getNumberOfLines();
      int procOffset, procLength, macroOffset, macroLength, lineOffset, index;
      boolean findProc = true;
      boolean findMacro = true;
      String line = "";

      procOffset = 0;
      procLength = 0;
      macroOffset = 0;
      macroLength = 0;
      lineOffset = 0;
      index = 0;

      for (int lineNumber = 0; lineNumber < numberOfLines; lineNumber++) {
        lineOffset = document.getLineOffset(lineNumber);
        line = document.get(lineOffset, document.getLineLength(lineNumber));
        line = line.toLowerCase();

        if (findProc) {
          index = line.indexOf("proc");

          if ((index > -1)
              && (document.getContentType(lineOffset + index).equals(Document.DEFAULT_CONTENT_TYPE))) {
            procOffset = lineOffset;
            findProc = false;
          }
        } else {
          index = line.indexOf("endp");

          if ((index > -1)
              && (document.getContentType(lineOffset + index).equals(Document.DEFAULT_CONTENT_TYPE))) {
            if ((lineNumber + 1) >= numberOfLines) {
              procLength = lineOffset + document.getLineLength(lineNumber) - procOffset;
            } else {
              procLength = document.getLineOffset(lineNumber + 1) - procOffset;
            }

            positions.add(new Position(procOffset, procLength));
            findProc = true;
          }
        }

        if (findMacro) {
          index = line.indexOf("macro");

          if ((index > -1)
              && (document.getContentType(lineOffset + index).equals(Document.DEFAULT_CONTENT_TYPE))) {
            macroOffset = lineOffset;
            findMacro = false;
          }
        } else {
          index = line.toLowerCase().indexOf("endm");

          if ((index > -1)
              && (document.getContentType(lineOffset + index).equals(Document.DEFAULT_CONTENT_TYPE))) {
            if ((lineNumber + 1) >= numberOfLines) {
              macroLength = lineOffset + document.getLineLength(lineNumber) - macroOffset;
            } else {
              macroLength = document.getLineOffset(lineNumber + 1) - macroOffset;
            }

            positions.add(new Position(macroOffset, macroLength));
            findMacro = true;
          }
        }
      }
    } catch (BadLocationException e) {
      Activator.getDefault().getLog().log(
                                          new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                     Messages.BADLOCATION_ERROR, e));
    }

    editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        editor.updateFoldingStructure(positions);
      }
    });
  }
}
