package de.fh_zwickau.asmplugin.editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;

/**
 * LabelProvider for the tree objects in the outlinepage.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class ASMLabelProvider extends LabelProvider {

  /** Refer to WorkbenchLabelProvider */
  private WorkbenchLabelProvider fWorkbenchLabelProvider;

  /** Default tree picture */
  private static final Image DEFAULT_IMAGE;

  /** root procedure picture */
  private static final Image PROCEDURE_ROOT_IMAGE;

  /** root macro picture */
  private static final Image MACRO_ROOT_IMAGE;

  /** root label picture */
  private static final Image LABEL_ROOT_IMAGE;

  /** root segment picture */
  private static final Image SEGMENT_ROOT_IMAGE;

  /** tree procedure picture */
  private static final Image PROCEDURE_IMAGE;

  /** tree macro picture */
  private static final Image MACRO_IMAGE;

  /** tree label picture */
  private static final Image LABEL_IMAGE;

  /** tree segment picture */
  private static final Image SEGMENT_IMAGE;

  static {
    String file = Activator.getFilePathFromPlugin("tree_default.gif");
    DEFAULT_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_root_procedure.gif");
    PROCEDURE_ROOT_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_root_macro.gif");
    MACRO_ROOT_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_root_label.gif");
    LABEL_ROOT_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_root_segment.gif");
    SEGMENT_ROOT_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_procedure.gif");
    PROCEDURE_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_macro.gif");
    MACRO_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_label.gif");
    LABEL_IMAGE = new Image(Display.getCurrent(), file);

    file = Activator.getFilePathFromPlugin("tree_segment.gif");
    SEGMENT_IMAGE = new Image(Display.getCurrent(), file);
  }

  /**
   * Constructor for ASMLabelProvider.
   * 
   * Create a custom LabelProvider to use images.
   */
  public ASMLabelProvider() {
    fWorkbenchLabelProvider = new WorkbenchLabelProvider();
  }

  /**
   * {@inheritDoc}
   */
  public String getText(Object element) {
    if (element instanceof TreeObject) {
      TreeObject elem = (TreeObject) element;
      return elem.toString();
    }

    return fWorkbenchLabelProvider.getText(element);
  }

  /**
   * {@inheritDoc}
   */
  public Image getImage(Object element) {
    if (element instanceof TreeObject) {
      TreeObject elem = (TreeObject) element;
      int type = elem.getType();

      switch (type) {
      case Constants.TREEOBJECT_TYPE_ROOT_PROCEDURE:
        return PROCEDURE_ROOT_IMAGE;
      case Constants.TREEOBJECT_TYPE_ROOT_MACRO:
        return MACRO_ROOT_IMAGE;
      case Constants.TREEOBJECT_TYPE_ROOT_LABEL:
        return LABEL_ROOT_IMAGE;
      case Constants.TREEOBJECT_TYPE_ROOT_SEGMENT:
        return SEGMENT_ROOT_IMAGE;
      case Constants.TREEOBJECT_TYPE_PROCEDURE:
        return PROCEDURE_IMAGE;
      case Constants.TREEOBJECT_TYPE_MACRO:
        return MACRO_IMAGE;
      case Constants.TREEOBJECT_TYPE_LABEL:
        return LABEL_IMAGE;
      case Constants.TREEOBJECT_TYPE_SEGMENT:
        return SEGMENT_IMAGE;
      default:
        return DEFAULT_IMAGE;
      }
    }

    return fWorkbenchLabelProvider.getImage(element);
  }

  /**
   * {@inheritDoc}
   */
  public void dispose() {
    if (fWorkbenchLabelProvider != null) {
      fWorkbenchLabelProvider.dispose();
      fWorkbenchLabelProvider = null;
    }
  }
}
