package de.fh_zwickau.asmplugin.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Action to show the ASCII-Table.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class AsciiAction extends Action implements IWorkbenchWindowActionDelegate {

  /**
   * {@inheritDoc}
   */
  public void init(IWorkbenchWindow window) {
  }

  /**
   * {@inheritDoc}
   */
  public void dispose() {
  }

  /**
   * {@inheritDoc}
   */
  public void run(IAction action) {
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

    AsciiDialog ad = new AsciiDialog(shell);
    ad.open();
  }

  /**
   * {@inheritDoc}
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }
}
