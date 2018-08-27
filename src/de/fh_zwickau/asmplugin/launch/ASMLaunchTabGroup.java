package de.fh_zwickau.asmplugin.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * TabGroup of the ASM-Launches.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

  /**
   * {@inheritDoc}
   */
  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    setTabs(new ILaunchConfigurationTab[] { new MainTab(), new ArgumentsTab() });
  }
}
