package de.fh_zwickau.asmplugin.launch;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.ProgramExecuter;
import de.fh_zwickau.asmplugin.WinApi;

/**
 * Launcher for executable files.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMLauncher extends LaunchConfigurationDelegate {

  /**
   * {@inheritDoc}
   */
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
                                                                                                               throws CoreException {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    String debugger = store.getString(Constants.PREFERENCES_DEBUGGER_NAME);
    String params = store.getString(Constants.PREFERENCES_DEBUGGER_PARAMS);

    if (mode.equalsIgnoreCase("debug") && ((debugger == null) || (debugger.trim().length() < 1))) {
      return;
    }

    if (params == null) {
      params = "";
    }

    debugger = debugger.trim();
    params = params.trim();

    String workspaceFile = configuration.getAttribute(Constants.LAUNCH_FILE, "");
    if ((workspaceFile == null) || (workspaceFile.trim().length() < 1)) {
      return;
    }

    String sep = System.getProperty("file.separator");
    if (sep.charAt(0) != '/') {
      sep = "\\" + sep;
    }

    workspaceFile = workspaceFile.replaceAll("/", sep);
    String filename = ResourcesPlugin.getWorkspace().getRoot().getLocation().makeAbsolute().toOSString()
                      + System.getProperty("file.separator") + workspaceFile;

    if (mode.equalsIgnoreCase("debug")) {
      if (ProgramExecuter.isLinux()) {
        ProgramExecuter.exec(debugger, params, filename, ProgramExecuter.ALTMODEXTM, false, false, false, true);
      } else if (ProgramExecuter.isWin()) {
        if (WinApi.isWin32Exe(ProgramExecuter.getShortPathName(debugger))) {
          ProgramExecuter.exec(debugger, params, filename, ProgramExecuter.ALTMODESHL, false, false, false, true);
        } else {
          ProgramExecuter.exec(debugger, params, filename, ProgramExecuter.ALTMODEBAT, false, false, false, true);
        }
      } else {
        ProgramExecuter.exec(debugger, params, filename, ProgramExecuter.ALTMODEOFF, false, false, false, true);
      }
    } else {
      if (ProgramExecuter.isLinux()) {
        ProgramExecuter.exec(filename, ProgramExecuter.ALTMODEXTM, false, false, false, false);
      } else if (ProgramExecuter.isWin()) {
        if (WinApi.isWin32Exe(ProgramExecuter.getShortPathName(filename))
            && !(WinApi.isWinConsole(ProgramExecuter.getShortPathName(filename)))) {
          ProgramExecuter.exec(filename, ProgramExecuter.ALTMODESHL, false, false, false, false);
        } else {
          ProgramExecuter.exec(filename, ProgramExecuter.ALTMODEONK, false, false, false, false);
        }
      } else {
        ProgramExecuter.exec(filename, ProgramExecuter.ALTMODEOFF, false, false, false, false);
      }
    }
  }
}
