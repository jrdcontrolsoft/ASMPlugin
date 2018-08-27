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
 * Preferences Page for Compiler.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class PreferencesCompiler extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  /**
   * The constructor.
   */
  public PreferencesCompiler() {
    super(FieldEditorPreferencePage.GRID); // use GRID-Layout

    noDefaultAndApplyButton(); // disable Default and Apply

    setPreferenceStore(Activator.getDefault().getPreferenceStore()); // set
  }

  /**
   * {@inheritDoc}
   */
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    // Field for Compiler-Executable
    FileFieldEditor compiler = new FileFieldEditor(Constants.PREFERENCES_COMPILER_NAME, Messages.COMPILER_NAME + ": ",
                                                   true, parent);
    addField(compiler);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;

    Label label = new Label(parent, SWT.LEFT);
    label.setText(" \n" + Messages.PARAMS_TEMPLATE_COMPILER + "\n ");
    label.setLayoutData(gd);

    // Field for parameters to use Compiler-Executable
    MultiLineStringFieldEditor params = new MultiLineStringFieldEditor(Constants.PREFERENCES_COMPILER_PARAMS,
                                                                       Messages.PARAMS_NAME + ": ", parent);
    addField(params);
  }

  /**
   * {@inheritDoc}
   */
  public void init(IWorkbench workbench) {
  }
}
