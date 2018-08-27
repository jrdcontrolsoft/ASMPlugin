package de.fh_zwickau.asmplugin.ui;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import de.fh_zwickau.asmplugin.Messages;

/**
 * The console to show the user the output from the compiler an linker.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class OutputConsole {

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.console.MessageConsole
   */
  private MessageConsole console;

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.console.MessageConsoleStream
   */
  private MessageConsoleStream stream;

  /**
   * Create a new console-window.
   * 
   */
  public OutputConsole() {
    console = new MessageConsole(Messages.CONSOLE_TITLE, null);

    ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
    bringConsoleToFront();

    stream = console.newMessageStream();
    stream.print("");
  }

  /**
   * Return the console object.
   * 
   * @return console object.
   */
  public MessageConsole getConsole() {
    return stream.getConsole();
  }

  /**
   * Add a line without lf/cr to console.
   * 
   * @param message The message where print.
   */
  public void print(String message) {
    stream.print(message);
  }

  /**
   * Add a empty line to console.
   * 
   */
  public void println() {
    stream.println();
  }

  /**
   * Add a line to console.
   * 
   * @param message The message where print.
   */
  public void println(String message) {
    stream.println(message);
  }

  /**
   * Show the console if not opened.
   */
  public void bringConsoleToFront() {
    ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
  }
}
