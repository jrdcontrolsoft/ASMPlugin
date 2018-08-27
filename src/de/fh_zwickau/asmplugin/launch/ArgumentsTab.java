package de.fh_zwickau.asmplugin.launch;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.Messages;

/**
 * Tab for the arguments.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ArgumentsTab extends AbstractLaunchConfigurationTab {

  private Text textArguments;

  private Text textWorkingDirectory;

  private ModifyListener listener;

  /**
   * {@inheritDoc}
   */
  public void createControl(Composite parent) {
    listener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateLaunchConfigurationDialog();
      }
    };

    Composite composite = new Composite(parent, parent.getStyle());
    composite.setLayout(new GridLayout(1, true));
    GridData data = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(data);
    setControl(composite);

    Group group = new Group(composite, SWT.NONE);
    group.setLayout(new GridLayout());
    data = new GridData(GridData.FILL_BOTH);
    group.setLayoutData(data);
    group.setText("Arguments");

    textArguments = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
    data = new GridData(GridData.FILL_BOTH);
    data.heightHint = 40;
    data.widthHint = 100;
    textArguments.setLayoutData(data);
    textArguments.addModifyListener(listener);

    group = new Group(composite, SWT.NONE);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setText(Messages.LAUNCH_WORKDIR);

    textWorkingDirectory = new Text(group, SWT.SINGLE | SWT.BORDER);
    textWorkingDirectory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    textWorkingDirectory.addModifyListener(listener);

    Button button = new Button(group, SWT.PUSH);
    button.setText(Messages.BROWSE);

    button.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        DirectoryDialog dialog = new DirectoryDialog(getShell());
        dialog.setMessage(Messages.LAUNCH_SELWORKDIR);
        String currentWorkingDir = textWorkingDirectory.getText();

        if (currentWorkingDir.trim().length() > 0) {
          File path = new File(currentWorkingDir);
          if (path.exists()) {
            dialog.setFilterPath(currentWorkingDir);
          }
        }
        String result = dialog.open();

        if (result != null) {
          textWorkingDirectory.setText(result);
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(Constants.LAUNCH_ARGUMENTS, "");
    configuration.setAttribute(Constants.LAUNCH_WORKING_DIRECTORY, "");
  }

  /**
   * {@inheritDoc}
   */
  public void initializeFrom(ILaunchConfiguration configuration) {
    try {
      textArguments.removeModifyListener(listener);
      textWorkingDirectory.removeModifyListener(listener);
      textArguments.setText(configuration.getAttribute(Constants.LAUNCH_ARGUMENTS, ""));
      textWorkingDirectory.setText(configuration.getAttribute(Constants.LAUNCH_WORKING_DIRECTORY, ""));
      textArguments.addModifyListener(listener);
      textWorkingDirectory.addModifyListener(listener);
    } catch (CoreException e) {
      Activator.getDefault().getLog().log(
                                          new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                     Messages.LOAD_LAUNCH_CONFIG_ERROR, e));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(Constants.LAUNCH_ARGUMENTS, textArguments.getText());
    configuration.setAttribute(Constants.LAUNCH_WORKING_DIRECTORY, textWorkingDirectory.getText());
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return Messages.ARGLAUNCHTAB;
  }
}
