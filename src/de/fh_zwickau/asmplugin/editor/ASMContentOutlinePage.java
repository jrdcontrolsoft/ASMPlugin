package de.fh_zwickau.asmplugin.editor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.Messages;

/**
 * Outlinepage for the ASM-Editor.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMContentOutlinePage extends ContentOutlinePage {

  private IEditorInput input;

  private ASMEditor editor;

  /**
   * The constructor.
   * 
   * @param editor An ASM-Editor.
   */
  public ASMContentOutlinePage(ASMEditor editor) {
    this.editor = editor;
  }

  /**
   * Sets the input for the outlinepage.
   * 
   * @param input The new input.
   */
  public void setInput(IEditorInput input) {
    this.input = input;

    final TreeViewer viewer = getTreeViewer();

    if ((viewer != null) && (viewer.getContentProvider() != null)) {
      editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          Control control = viewer.getControl();
          control.setRedraw(false);

          ITextSelection textselect = (ITextSelection) editor.getSelectionProvider().getSelection();

          TreeObject treeobj = getSelectedTreeObject(viewer);
          viewer.setInput(ASMContentOutlinePage.this.input);
          viewer.refresh();
          viewer.expandAll();
          selectTreeObject(viewer, treeobj);

          editor.getSelectionProvider().setSelection(textselect);

          control.setRedraw(true);
        }
      });
    }
  }

  /**
   * Returns the selected element in the tree viewer.
   * 
   * @param viewer The tree viewer.
   * 
   * @return The selected element.
   */
  private TreeObject getSelectedTreeObject(TreeViewer viewer) {
    ISelection selection = viewer.getSelection();

    if (!selection.isEmpty()) {
      if (selection instanceof IStructuredSelection) {
        Object object = ((IStructuredSelection) selection).getFirstElement();

        if (object instanceof TreeObject) {
          return (TreeObject) object;
        }
      }
    }

    return null;
  }

  /**
   * Select a given TreeObject in the given TreeViewer.
   * 
   * @param viewer The given TreeViewer.
   * @param treeobj The given TreeObject.
   */
  private void selectTreeObject(TreeViewer viewer, TreeObject treeobj) {
    if (treeobj == null) {
      return;
    }

    IContentProvider icp = viewer.getContentProvider();
    if (icp instanceof ContentProvider) {
      ContentProvider cp = (ContentProvider) icp;
      TreeObject fto = cp.findEqualTreeObject(treeobj);

      if (fto != null) {
        ArrayList<Object> newSelection = new ArrayList<Object>();
        newSelection.add(fto);
        viewer.setSelection(new StructuredSelection(newSelection));
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void createControl(Composite parent) {
    super.createControl(parent);

    TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(new ContentProvider());
    viewer.setLabelProvider(new ASMLabelProvider());
    viewer.addSelectionChangedListener(this);
    viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

    if (input != null) {
      viewer.setInput(input);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void selectionChanged(SelectionChangedEvent event) {
    super.selectionChanged(event);

    ISelection selection = event.getSelection();

    if (selection.isEmpty()) {
      editor.resetHighlightRange();
    } else {
      if (selection instanceof IStructuredSelection) {
        Object object = ((IStructuredSelection) selection).getFirstElement();

        if (object instanceof TreeObject) {
          try {
            Position position = (Position) ((TreeObject) object).getData();

            if (position != null) {
              editor.setHighlightRange(position.offset, 0, true);
              editor.getSelectionProvider().setSelection(new TextSelection(position.offset, position.length));
            }
          } catch (IllegalArgumentException x) {
            editor.resetHighlightRange();
          }
        }
      }
    }
  }

  /**
   * The ContentProvider for the TreeViewer in ContentOutlinePage.
   * 
   * @author Andy Reek
   * @since 08.09.2006
   */
  private class ContentProvider implements ITreeContentProvider {

    private IEditorInput input;

    private TreeObject procedures = new TreeObject(Messages.TREEOBJECT_PROCEDURE_NAME,
                                                   Constants.TREEOBJECT_TYPE_ROOT_PROCEDURE);

    private TreeObject macros = new TreeObject(Messages.TREEOBJECT_MACRO_NAME, Constants.TREEOBJECT_TYPE_ROOT_MACRO);

    private TreeObject labels = new TreeObject(Messages.TREEOBJECT_LABEL_NAME, Constants.TREEOBJECT_TYPE_ROOT_LABEL);

    private TreeObject segments = new TreeObject(Messages.TREEOBJECT_SEGMENT_NAME,
                                                 Constants.TREEOBJECT_TYPE_ROOT_SEGMENT);

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof TreeObject) {
        return ((TreeObject) parentElement).getChildren();
      }

      return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
      if (element instanceof TreeObject) {
        return ((TreeObject) element).getParent();
      }

      return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
      if (element instanceof TreeObject) {
        return (((TreeObject) element).getChildren().length > 0);
      }

      return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
      if (inputElement == input) {
        ArrayList<TreeObject> objects = new ArrayList<TreeObject>();

        if (procedures.getChildren().length > 0) {
          objects.add(procedures);
        }

        if (macros.getChildren().length > 0) {
          objects.add(macros);
        }

        if (labels.getChildren().length > 0) {
          objects.add(labels);
        }

        if (segments.getChildren().length > 0) {
          objects.add(segments);
        }

        return objects.toArray(new Object[0]);
      }

      return null;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
      input = null;
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (oldInput instanceof IEditorInput) {
        input = null;
      }

      if (newInput instanceof IEditorInput) {
        input = (IEditorInput) newInput;
        parse();
      }
    }

    /**
     * Finds an existing TreeObject in the tree model.
     * 
     * @param treeobj The given TreeObject.
     * 
     * @return The TreeObject, if found. Or null if not found.
     */
    private TreeObject findEqualTreeObject(TreeObject treeobj) {
      if (treeobj == null) {
        return null;
      }

      if (procedures.equals(treeobj)) {
        return procedures;
      }

      if (macros.equals(treeobj)) {
        return macros;
      }

      if (labels.equals(treeobj)) {
        return labels;
      }

      if (segments.equals(treeobj)) {
        return segments;
      }

      int i = 0;
      TreeObject to = null;
      Object[] o = procedures.getChildren();

      for (i = 0; i < o.length; i++) {
        if (o[i] instanceof TreeObject) {
          to = (TreeObject) o[i];

          if (to.equals(treeobj)) {
            return to;
          }
        }
      }

      o = macros.getChildren();

      for (i = 0; i < o.length; i++) {
        if (o[i] instanceof TreeObject) {
          to = (TreeObject) o[i];

          if (to.equals(treeobj)) {
            return to;
          }
        }
      }

      o = labels.getChildren();

      for (i = 0; i < o.length; i++) {
        if (o[i] instanceof TreeObject) {
          to = (TreeObject) o[i];

          if (to.equals(treeobj)) {
            return to;
          }
        }
      }

      o = segments.getChildren();

      for (i = 0; i < o.length; i++) {
        if (o[i] instanceof TreeObject) {
          to = (TreeObject) o[i];

          if (to.equals(treeobj)) {
            return to;
          }
        }
      }

      return null;
    }

    /**
     * Parse the new input and build up the tree.
     */
    private void parse() {
      procedures.setChildren(null);
      macros.setChildren(null);
      labels.setChildren(null);
      segments.setChildren(null);

      IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

      if (document != null) {
        int lines = document.getNumberOfLines();
        int lineOffset, pos, linelen, matchStart, matchEnd, startOffset, length;
        String name = "";
        String stringLine = "";
        String stringLineLower = "";
        StringBuffer filterBuffer = new StringBuffer();
        Pattern pattern = null;
        Matcher matcher = null;
        TreeObject child = null;

        lineOffset = 0;
        pos = 0;
        linelen = 0;
        matchStart = 0;
        matchEnd = 0;
        startOffset = 0;
        length = 0;

        for (int line = 0; line < lines; line++) {
          try {
            lineOffset = document.getLineOffset(line);
            linelen = document.getLineLength(line);
            stringLine = document.get(lineOffset, linelen);
            filterBuffer.setLength(0);

            for (pos = 0; pos < linelen; pos++) {
              if (document.getPartition(lineOffset + pos).getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                filterBuffer.append(stringLine.substring(pos, pos + 1));
              } else {
                filterBuffer.append(" ");
              }
            }

            stringLine = filterBuffer.toString();
            stringLineLower = stringLine.toLowerCase();

            if (stringLineLower.indexOf("proc") > -1) {
              pattern = Pattern.compile("(\\A|\\W)\\s*proc\\s+(\\w+)");
              matcher = pattern.matcher(stringLineLower);

              if (matcher.find()) {
                matchStart = matcher.start(2);
                matchEnd = matcher.end(2);
                startOffset = lineOffset + matchStart;
                length = lineOffset + matchEnd - startOffset;

                child = new TreeObject(stringLine.substring(matchStart, matchEnd), Constants.TREEOBJECT_TYPE_PROCEDURE);
                child.setData(new Position(startOffset, length));

                procedures.addChild(child);
              }
            }

            if (stringLineLower.indexOf("macro") > -1) {
              pattern = Pattern.compile("(\\A|\\W)\\s*(\\w+)\\s+macro");
              matcher = pattern.matcher(stringLineLower);

              if (matcher.find()) {
                matchStart = matcher.start(2);
                matchEnd = matcher.end(2);
                startOffset = lineOffset + matchStart;
                length = lineOffset + matchEnd - startOffset;

                child = new TreeObject(stringLine.substring(matchStart, matchEnd), Constants.TREEOBJECT_TYPE_MACRO);
                child.setData(new Position(startOffset, length));

                macros.addChild(child);
              }
            }

            if (stringLineLower.indexOf(":") > -1) {
              pattern = Pattern.compile("\\A\\s*\\.*(\\w+):");
              matcher = pattern.matcher(stringLineLower);

              if (matcher.find()) {
                matchStart = matcher.start(1);
                matchEnd = matcher.end(1);
                startOffset = lineOffset + matchStart;
                length = lineOffset + matchEnd - startOffset;

                child = new TreeObject(stringLine.substring(matchStart, matchEnd), Constants.TREEOBJECT_TYPE_LABEL);
                child.setData(new Position(startOffset, length));

                labels.addChild(child);
              }
            }

            if (stringLineLower.indexOf(".") > -1) {
              pattern = Pattern.compile("(\\A|\\W)\\.(\\w+[:]*)");
              matcher = pattern.matcher(stringLineLower);

              if (matcher.find()) {
                matchStart = matcher.start(2);
                matchEnd = matcher.end(2);
                startOffset = lineOffset + matchStart;
                length = lineOffset + matchEnd - startOffset;

                name = stringLine.substring(matchStart, matchEnd);

                if (name.codePointAt(name.length() - 1) != ':') {
                  child = new TreeObject(name, Constants.TREEOBJECT_TYPE_SEGMENT);
                  child.setData(new Position(startOffset, length));

                  segments.addChild(child);
                }
              }
            }
          } catch (BadLocationException e) {
            Activator.getDefault().getLog().log(
                                                new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                           Messages.BADLOCATION_ERROR, e));
          }
        }
      }
    }
  }
}
