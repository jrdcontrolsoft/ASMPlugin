package de.fh_zwickau.asmplugin.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.Messages;

/**
 * Preferences Page for Debugger.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class PreferencesDebugger extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  /**
   * The constructor.
   */
  public PreferencesDebugger() {
    super(FieldEditorPreferencePage.GRID); // use GRID-Layout

    noDefaultAndApplyButton(); // disable Default and Apply

    setPreferenceStore(Activator.getDefault().getPreferenceStore()); // set
  }

  /**
   * {@inheritDoc}
   */
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    // Field for Debugger-Executable
    FileFieldEditor linker = new FileFieldEditor(Constants.PREFERENCES_DEBUGGER_NAME, Messages.DEBUGGER_NAME + ": ",
                                                 true, parent);
    addField(linker);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;

    Label label = new Label(parent, SWT.LEFT);
    label.setText(" \n" + Messages.PARAMS_TEMPLATE_DEBUGGER + "\n ");
    label.setLayoutData(gd);

    // Field for parameters to use Compiler-Executable
    MultiLineStringFieldEditor params = new MultiLineStringFieldEditor(Constants.PREFERENCES_DEBUGGER_PARAMS,
                                                                       Messages.PARAMS_NAME + ": ", parent);
    addField(params);
  }

  /**
   * {@inheritDoc}
   */
  public void init(IWorkbench workbench) {
  }
}
