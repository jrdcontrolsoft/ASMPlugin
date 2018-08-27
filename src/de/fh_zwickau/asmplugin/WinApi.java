package de.fh_zwickau.asmplugin;

/**
 * Java-Native-Interface (JNI) Class for a lot of Windows API functions.
 * 
 * @author Daniel Mitte
 * @since 01.03.2006
 */
public final class WinApi {

  /** Hides the window and activates another window. */
  public static final long SW_HIDE = 0;

  /** Maximizes the specified window. */
  public static final long SW_MAXIMIZE = 3;

  /**
   * Minimizes the specified window and activates the next top-level window in
   * the Z order.
   */
  public static final long SW_MINIMIZE = 6;

  /**
   * Activates the window and displays it in its current size and position.
   */
  public static final long SW_SHOW = 5;

  /**
   * Sets the show state based on the SW_ value specified in the STARTUPINFO
   * structure passed to the CreateProcess function by the program that started
   * the application.
   */
  public static final long SW_SHOWDEFAULT = 10;

  /**
   * Activates the window and displays it as a maximized window.
   */
  public static final long SW_SHOWMAXIMIZED = 3;

  /**
   * Activates the window and displays it as a minimized window.
   */
  public static final long SW_SHOWMINIMIZED = 2;

  /**
   * Displays the window as a minimized window. This value is similar to
   * SW_SHOWMINIMIZED, except the window is not activated.
   */
  public static final long SW_SHOWMINNOACTIVE = 7;

  /**
   * Displays the window in its current size and position. This value is similar
   * to SW_SHOW, except the window is not activated.
   */
  public static final long SW_SHOWNA = 8;

  /**
   * Displays a window in its most recent size and position. This value is
   * similar to SW_SHOWNORMAL, except the window is not actived.
   */
  public static final long SW_SHOWNOACTIVATE = 4;

  /**
   * Activates and displays a window. If the window is minimized or maximized,
   * the system restores it to its original size and position. An application
   * should specify this flag when displaying the window for the first time.
   */
  public static final long SW_SHOWNORMAL = 1;

  static {
    String dll = Activator.getFilePathFromPlugin("winapi.dll");
    String sep = System.getProperty("file.separator");
    if (sep.charAt(0) != '/') {
      sep = "\\" + sep;
    }
    dll = dll.replaceAll(sep, "/");
    System.load(dll);
  }

  /**
   * Must not be instantiated.
   */
  private WinApi() {
    // Must not be instantiated.
  }

  /**
   * Convert Longname to Shortname on Windows Platform.
   * 
   * @param longname Longname, e.g. C:\Program Files .
   * @return Shortname, e.g. C:\PROGRA~1 .
   */
  public static native String getShortName(String longname);

  /**
   * Open a File or Document on Windows Platform.
   * 
   * @param file Filename to open.
   * @param params Parameters for executable.
   * @param directory Working-Directory for executable.
   * @param show Show-Window-Parameter.
   */
  public static native void shExecute(String file, String params, String directory, long show);

  /**
   * Detect Windows NT Platform.
   * 
   * @return Result true if Windows NT.
   */
  public static native boolean isWinNT();

  /**
   * Detect Windows 32-Bit Executable.
   * 
   * @param file Exe-Filename
   * @return Result of detection.
   */
  public static native boolean isWin32Exe(String file);

  /**
   * Detect Windows Console Executable.
   * 
   * @param file Exe-Filename
   * @return Result of detection.
   */
  public static native boolean isWinConsole(String file);

  /**
   * Return Windows Directory.
   * 
   * @return Windows Directory.
   */
  private static native String getWindowsDirectoryAPI();

  /**
   * Return Windows-System Directory.
   * 
   * @return Windows-System Directory.
   */
  private static native String getWindowsSystemDirectoryAPI();

  /**
   * Append \ to end if needed.
   * 
   * @param in Input text.
   * @return Modified text.
   */
  private static String addEndBackslash(String in) {
    if (in.charAt(in.length() - 1) != '\\') {
      return in + "\\";
    }

    return in;
  }

  /**
   * Return Windows Directory.
   * 
   * @return Windows Directory.
   */
  public static String getWindowsDirectory() {
    return addEndBackslash(getWindowsDirectoryAPI());
  }

  /**
   * Return Windows-System Directory.
   * 
   * @return Windows-System Directory.
   */
  public static String getWindowsSystemDirectory() {
    return addEndBackslash(getWindowsSystemDirectoryAPI());
  }
}
