package de.fh_zwickau.asmplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

/**
 * Executes programms platform independent.
 * 
 * @author Daniel Mitte
 * @since 01.03.2006
 */
public final class ProgramExecuter {

  /** Execute file direct on Windows-Platform */
  public static final int ALTMODEOFF = 0;

  /** Execute file by command.com/cmd.exe with parameter /C on Windows-Platform */
  public static final int ALTMODEONC = 1;

  /**
   * Execute file by API ShellExecute and command.com/cmd.exe with parameter /K
   * on Windows-Platform
   */
  public static final int ALTMODEONK = 2;

  /**
   * Execute file by API ShellExecute and command.com/cmd.exe with parameter /K
   * on Windows-Platform
   */
  public static final int ALTMODEBAT = 3;

  /** Execute file by API ShellExecute on Windows-Platform */
  public static final int ALTMODESHL = 4;

  /** Execute file by xterm on Linux-Platform */
  public static final int ALTMODEXTM = 5;

  /**
   * Must not be instantiated.
   */
  private ProgramExecuter() {
    // Must not be instantiated.
  }

  /**
   * Execute a program.
   * 
   * @param execFile Path to executing file.
   * @param altmode If true then execute on Windows-Platform with
   *          command.com/cmd.exe .
   * @param wait If true then wait process for application terminate.
   * @param stdout Enable/Disable STDOUT output
   * @param verbose More Information of execute-process.
   * @param inbuilddir If true executable is in build directory.
   * @return Stdout from executing file.
   */
  public static String exec(String execFile, int altmode, boolean wait, boolean stdout, boolean verbose,
                            boolean inbuilddir) {
    String dir = "";

    int pos = execFile.lastIndexOf(System.getProperty("file.separator"));

    if (pos > 2) {
      dir = execFile.substring(0, pos);
    }

    return exec(execFile, "", new String[0], dir, altmode, wait, stdout, verbose, inbuilddir);
  }

  /**
   * Execute a program.
   * 
   * @param execFile Path to executing file.
   * @param params Params for executing file.
   * @param paramFile File-param executing file.
   * @param altmode If true then execute on Windows-Plattform with
   *          command.com/cmd.exe .
   * @param wait If true then wait process for application terminate.
   * @param stdout Enable/Disable STDOUT output
   * @param verbose More Information of execute-process.
   * @param inbuilddir If true executable is in build directory.
   * @return Stdout from executing file.
   */
  public static String exec(String execFile, String params, String paramFile, int altmode, boolean wait,
                            boolean stdout, boolean verbose, boolean inbuilddir) {
    String dir = "";

    int pos = paramFile.lastIndexOf(System.getProperty("file.separator"));

    if (pos > 2) {
      dir = paramFile.substring(0, pos);
    }

    return exec(execFile, params, paramFile, dir, altmode, wait, stdout, verbose, inbuilddir);
  }

  /**
   * Execute a program.
   * 
   * @param execFile Path to executing file.
   * @param params Params for executing file.
   * @param paramFile File-param executing file.
   * @param execDirectory Execute-Directory for executing file.
   * @param altmode If true then execute on Windows-Plattform with
   *          command.com/cmd.exe .
   * @param wait If true then wait process for application terminate.
   * @param stdout Enable/Disable STDOUT output
   * @param verbose More Information of execute-process.
   * @param inbuilddir If true executable is in build directory.
   * @return Stdout from executing file.
   */
  public static String exec(String execFile, String params, String paramFile, String execDirectory, int altmode,
                            boolean wait, boolean stdout, boolean verbose, boolean inbuilddir) {
    String[] item = new String[1];
    item[0] = paramFile;

    return exec(execFile, params, item, execDirectory, altmode, wait, stdout, verbose, inbuilddir);
  }

  /**
   * Execute a program.
   * 
   * @param execFile Path to executing file.
   * @param params Params for executing file.
   * @param paramFiles File-params executing file.
   * @param execDirectory Execute-Directory for executing file.
   * @param altmode If true then execute on Windows-Plattform with
   *          command.com/cmd.exe .
   * @param wait If true then wait process for application terminate.
   * @param stdout Enable/Disable STDOUT output
   * @param verbose More Information of execute-process.
   * @param inbuilddir If true executable is in build directory.
   * @return Stdout from executing file.
   */
  public static String exec(String execFile, String params, String[] paramFiles, String execDirectory, int altmode,
                            boolean wait, boolean stdout, boolean verbose, boolean inbuilddir) {
    if (!fileExists(execFile)) {
      return "File not found!";
    }

    String execstr = "";
    String dir = getShortPathName(execDirectory);

    String fileparams = "";
    String fileparamsname = "";
    int pos = 0;
    boolean shexec = false;

    if (isWin()) {
      if (altmode == ALTMODEONC) {
        execstr = getWindowsCommandLineInterpreter() + " /C " + getShortPathName(execFile);
      } else if (altmode == ALTMODEONK) {
        shexec = true;
        execstr = getWindowsCommandLineInterpreter() + " /K " + getShortPathName(execFile);
      } else if (altmode == ALTMODEBAT) {
        shexec = true;
        String batch = getShortPathName(Activator.getFilePathFromPlugin("exec.bat"));
        execstr = getWindowsCommandLineInterpreter() + " /K " + batch + " " + getShortPathName(execFile);
      } else if (altmode == ALTMODESHL) {
        shexec = true;
        execstr = getShortPathName(execFile);
      } else {
        execstr = getShortPathName(execFile);
      }
    } else {
      if (altmode == ALTMODEXTM) {
        execstr = "xterm -hold -e " + execFile;
      } else {
        execstr = execFile;
      }
    }

    if (dir.length() > 0) {
      pos = dir.lastIndexOf(System.getProperty("file.separator"));

      if (pos == (dir.length() - System.getProperty("file.separator").length())) {
        dir = dir.substring(0, pos);
      }
    }

    for (int i = 0; i < paramFiles.length; i++) {
      if ((fileparams.length() > 0) && (fileparams.charAt(fileparams.length()) != ' ')) {
        fileparams += " ";
      }

      if ((fileparamsname.length() > 0) && (fileparamsname.charAt(fileparamsname.length()) != ' ')) {
        fileparamsname += " ";
      }

      if (fileExists(paramFiles[i])) {
        if (isWin()) {
          fileparams += getShortPathName(paramFiles[i]);
        } else {
          fileparams += paramFiles[i];
        }

        pos = paramFiles[i].lastIndexOf(System.getProperty("file.separator"));

        if (pos > 1) {
          paramFiles[i] = paramFiles[i].substring(pos + 1, paramFiles[i].length());
        }

        pos = paramFiles[i].lastIndexOf(".");

        if (pos > 0) {
          fileparamsname += paramFiles[i].substring(0, pos);
        } else {
          fileparamsname += paramFiles[i];
        }
      }
    }

    if (inbuilddir) {
      if (params.toLowerCase().indexOf("{builddir}") > -1) {
        params = replace(params, "{builddir}", dir, true);
      }

      if (params.toLowerCase().indexOf("{workdir}") > -1) {
        String wrkdir = dir;
        pos = wrkdir.toLowerCase().lastIndexOf(System.getProperty("file.separator") + "build");
        if (pos > -1) {
          wrkdir = wrkdir.substring(0, pos);
        }
        params = replace(params, "{workdir}", wrkdir, true);
      }
    } else {
      if (params.toLowerCase().indexOf("{builddir}") > -1) {
        params = replace(params, "{builddir}", dir + "\\build", true);
      }

      if (params.toLowerCase().indexOf("{workdir}") > -1) {
        params = replace(params, "{workdir}", dir, true);
      }
    }

    if (params.toLowerCase().indexOf("{filename}") > -1) {
      params = replace(params, "{filename}", fileparamsname, true);
    }

    if (params.toLowerCase().indexOf("{srcfile}") > -1) {
      params = replace(params, "{srcfile}", fileparams, true);

      if (params.length() > 0) {
        execstr = execstr + " " + params;
      }
    } else {
      if (params.length() > 0) {
        execstr = execstr + " " + params;
      }

      if (fileparams.length() > 0) {
        execstr = execstr + " " + fileparams;
      }
    }

    String result = "";

    if (verbose) {
      result = Messages.RUN + ": " + execstr + "\n";

      if (dir.length() > 0) {
        result += Messages.FROM + ": " + dir + "\n";
      }

      result += "\n";
    }

    result += execFile(execstr, dir, wait, stdout, shexec);

    return result;
  }

  /**
   * Execute a program.
   * 
   * @param fileName Path to executing file.
   * @param directory Execute-Directory for executing file.
   * @param wait If true then wait process for application terminate.
   * @param stdout Enable/Disable STDOUT output
   * @param shexec Run with ShellExecute on Windows Platform.
   * @return Stdout from executing file.
   */
  private static String execFile(String fileName, String directory, boolean wait, boolean stdout, boolean shexec) {
    if (shexec && isWin()) {
      String[] parts = fileName.split(" ");

      if (parts.length > 1) {
        String params = parts[1];
        for (int i = 2; i < parts.length; i++) {
          params += " " + parts[i];
        }
        WinApi.shExecute(parts[0], params, directory, WinApi.SW_SHOWNORMAL);
      } else {
        WinApi.shExecute(fileName, "", directory, WinApi.SW_SHOWNORMAL);
      }

      return "";
    } else {
      String line;
      StringBuffer text = null;
      InputStream inputstream = null;
      BufferedReader bufferedreader = null;
      Process process = null;

      try {
        if (directory.length() < 1) {
          process = Runtime.getRuntime().exec(fileName);
        } else {
          Map<String, String> envs = System.getenv();
          String[] execenvs = new String[envs.size()];
          int i = 0;

          for (String env : envs.keySet()) {
            execenvs[i] = new String(env + "=" + envs.get(env));
            i++;
          }

          process = Runtime.getRuntime().exec(fileName, execenvs, new File(directory));
        }

        if (stdout) {
          text = new StringBuffer("");
          inputstream = process.getInputStream();
          bufferedreader = new BufferedReader(new InputStreamReader(inputstream));

          while ((line = bufferedreader.readLine()) != null) {
            text.append(line);

            if ((line.length() < 1) || ((line.length() > 0) && (line.charAt(line.length() - 1) != '\n'))) {
              text.append("\n");
            }
          }
        }

        if (wait) {
          process.waitFor();
        }

        if (stdout) {
          inputstream.close();
          bufferedreader.close();
        }
      } catch (Exception e) {
        Activator.getDefault().getLog().log(
                                            new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                       Messages.LAUNCH_ERROR, e));
      }

      if (text != null) {
        return text.toString();
      }

      return "";
    }
  }

  /**
   * Opens a console.
   */
  public static void openConsole() {
    String initdir = "";
    IEditorInput editin = null;

    try {
      editin = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
    } catch (Exception e) {
      Activator.getDefault().getLog().log(
                                          new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                     Messages.OPEN_CONSOLE_ERROR, e));
    }

    if (editin instanceof IFileEditorInput) {
      IFile file = ((IFileEditorInput) editin).getFile();
      String fname = file.getLocation().makeAbsolute().toOSString();
      boolean endasm = fname.toLowerCase().endsWith(".asm");

      if (endasm) {
        fname = getShortPathName(fname);
      }

      if (endasm || fname.toLowerCase().endsWith(System.getProperty("file.separator"))) {
        int pos = fname.lastIndexOf(System.getProperty("file.separator"));
        initdir = fname.substring(0, pos);

        if (initdir.length() > 0) {
          File fl = new File(initdir + System.getProperty("file.separator") + Constants.BUILD_DIRECTORY);

          if (fl.exists() && fl.isDirectory()) {
            initdir = initdir + System.getProperty("file.separator") + Constants.BUILD_DIRECTORY;
          }
        }
      }
    }

    if (isWin()) {
      WinApi.shExecute(getWindowsCommandLineInterpreter(), "/K", initdir, WinApi.SW_SHOWNORMAL);
    } else if (isLinux()) {
      execFile("xterm -T Console", initdir, false, false, false);
    } else {
      MessageDialog.openInformation(Activator.getDefault().getWorkbench().getDisplay().getActiveShell(),
                                    Messages.MSGBOX_INFORMATION, Messages.FUNCTION_NOT_SUPPORTED);
    }
  }

  /**
   * Detect if file exists.
   * 
   * @param fn Filename (with Path).
   * @return Result true if File exists.
   */
  private static boolean fileExists(String fn) {
    if (fn.length() < 1) {
      return false;
    }

    File f = new File(fn);

    return f.exists();
  }

  /**
   * Returns the short path name from a given long path name.
   * 
   * @param longName The given long path name.
   * 
   * @return The short path name.
   */
  public static String getShortPathName(String longName) {
    if ((longName.length() < 4) || (longName.charAt(1) != ':') || (!fileExists(longName)) || (!isWin())) {
      return longName;
    }

    return WinApi.getShortName(longName).toUpperCase();
  }

  /**
   * Return path to command-line interpreter on Windows Platform.
   * 
   * @return path to command-line interpreter.
   */
  private static String getWindowsCommandLineInterpreter() {
    if (!isWin()) {
      return "";
    }

    String windir = WinApi.getWindowsDirectory();
    String winsysdir = WinApi.getWindowsSystemDirectory();

    if (fileExists(winsysdir + "cmd.exe")) {
      return winsysdir + "cmd.exe";
    } else if (fileExists(windir + "cmd.exe")) {
      return windir + "cmd.exe";
    } else if (fileExists(windir + "command.com")) {
      return windir + "command.com";
    }

    if (isWinNT()) {
      return "cmd.exe";
    }

    return "command.com";
  }

  /**
   * Detect Windows/Windows NT Platform.
   * 
   * @return Result true if Windows
   */
  public static boolean isWin() {
    if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
      return true;
    }

    return false;
  }

  /**
   * Detect Windows NT Platform.
   * 
   * @return Result true if Windows NT.
   */
  public static boolean isWinNT() {
    if (!isWin()) {
      return false;
    }

    return WinApi.isWinNT();
  }

  /**
   * Detect UNIX/Linux Platform.
   * 
   * @return Result true if a unix system.
   */
  public static boolean isLinux() {
    String os = System.getProperty("os.name").toLowerCase();

    if ((os.indexOf("linux") > -1) || (os.indexOf("solaris") > -1) || (os.indexOf("aix") > -1)
        || (os.indexOf("irix") > -1) || (os.indexOf("hp-ux") > -1)) {
      return true;
    }

    return false;
  }

  /**
   * Simple Search-Replace-Function. Need because String.replaceAll destroy
   * filepath.
   * 
   * @param text Inputtext
   * @param search Searchtext
   * @param replace Replacetext
   * @param insensitive If true, ignore case-sensitive.
   * @return Inputtext with replaced text.
   */
  private static String replace(String text, String search, String replace, boolean insensitive) {
    String result = text;
    int pos = 0;
    int slen = search.length();
    int rlen = replace.length();
    int oldpos = 0;

    if (slen > 0) {
      if (insensitive) {
        pos = result.toLowerCase().indexOf(search);

        while (pos > -1) {
          result = result.substring(0, pos) + replace + result.substring(pos + slen, result.length());
          oldpos = pos;
          pos = result.toLowerCase().indexOf(search, oldpos + rlen);
        }
      } else {
        pos = result.indexOf(search);

        while (pos > -1) {
          result = result.substring(0, pos) + replace + result.substring(pos + slen, result.length());
          oldpos = pos;
          pos = result.indexOf(search, oldpos + rlen);
        }
      }
    }

    return result;
  }
}
