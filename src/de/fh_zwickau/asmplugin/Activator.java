package de.fh_zwickau.asmplugin;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fh_zwickau.asmplugin.editor.ASMEditor;
import de.fh_zwickau.asmplugin.ui.OutputConsole;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {

  /**
   * The shared instance.
   */
  private static Activator plugin;

  /**
   * The TemplateStore.
   */
  private static TemplateStore templateStore;

  /**
   * The Output-Console.
   */
  private static OutputConsole console = new OutputConsole();

  /**
   * The constructor.
   */
  public Activator() {
	super();
    plugin = this;
  }

  /**
   * {@inheritDoc}
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this; // Nur wenn "plugin = null" bei "public void stop(BundleContext context)" wirklich notwendig ist 
  }

  /**
   * {@inheritDoc}
   */
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    plugin = null;  // ??? Warum notwendig, wird doch auch bei "public void start(BundleContext context)" NICHT initialisiert
  }

  /**
   * Returns the shared instance.
   * 
   * @return The plug-in.
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Loads and returns the TemplateStore.
   * 
   * @return The TemplateStore.
   */
  public static TemplateStore getTemplateStore() {
    if (templateStore == null) {
      templateStore = new TemplateStore(ASMEditor.getContextTypeRegistry(), plugin.getPreferenceStore(),
                                        Constants.PROPERTY_TEMPLATES);
    }

    try {
      templateStore.load();
    } catch (IOException e) {
      plugin.getLog().log(new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK, Messages.LOADTEMPSTORE_ERROR, e));
    }

    return templateStore;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in
   * relative path.
   * 
   * @param path The path.
   * 
   * @return The image descriptor.
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(Constants.PLUGIN_ID, path);
  }

  /**
   * Returns the absolut path of a entrie from the plugin's directory.
   * 
   * @param entrie a file or directory (don't use "dir1\dir2" or "dir1\file1")
   * 
   * @return Returns the path from the plugin.
   */
  public static String getFilePathFromPlugin(String entrie) {
    URL url = null;
    IPath path = null;
    String result = "";

    Enumeration enu = Activator.getDefault().getBundle().findEntries("/", entrie, true);
    if (enu.hasMoreElements()) {
      url = (URL) enu.nextElement();
    }

    if (url == null) {
      return "";
    }

    try {
      path = new Path(FileLocator.toFileURL(url).getPath());
      result = path.makeAbsolute().toOSString();
    } catch (Exception e) {
      result = "";
    }

    return result;
  }

  /**
   * Return the Output-Console object for text output.
   * 
   * @return Output-Console object
   */
  public static OutputConsole getConsole() {
    return console;
  }

  /**
   * {@inheritDoc}
   */
  protected void initializeDefaultPreferences(IPreferenceStore store) {
    Display display = Display.getCurrent();

    String textAttribute = TextAttributeConverter.textAttributesToPreferenceData(new Color(display, 0, 0, 255), false,
                                                                                 false);
    store.setDefault(Constants.PREFERENCES_TEXTCOLOR_STRING, textAttribute);

    textAttribute = TextAttributeConverter.textAttributesToPreferenceData(new Color(display, 0, 128, 0), false, true);
    store.setDefault(Constants.PREFERENCES_TEXTCOLOR_COMMENT, textAttribute);

    textAttribute = TextAttributeConverter.textAttributesToPreferenceData(new Color(display, 0, 0, 128), true, false);
    store.setDefault(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION, textAttribute);

    textAttribute = TextAttributeConverter.textAttributesToPreferenceData(new Color(display, 128, 64, 0), true, false);
    store.setDefault(Constants.PREFERENCES_TEXTCOLOR_SEGMENT, textAttribute);

    store.setDefault(Constants.PROPERTY_TEMPLATES,
                     "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                         + "<templates>"
                         + "<template autoinsert=\"true\" context=\"asm.editor.context\" deleted=\"false\" description=\"End of program.\" enabled=\"true\" name=\"Exitcode\">&#9;mov ax, 4C00h&#13;&#9;int 21h</template>"
                         + "<template autoinsert=\"true\" context=\"asm.editor.context\" deleted=\"false\" description=\"Init ASM program\" enabled=\"true\" name=\"Initcode\">&#9;mov ax, @code&#13;&#9;mov ds, ax</template>"
                         + "<template autoinsert=\"true\" context=\"asm.editor.context\" deleted=\"false\" description=\"Output Text on screen\" enabled=\"true\" name=\"Textoutput\">&#9;mov ah, 9h&#13;&#9;mov dx, offset &lt;Message&gt;&#13;&#9;int 21h</template>"
                         + "<template autoinsert=\"true\" context=\"asm.editor.context\" deleted=\"false\" description=\"Small model\" enabled=\"true\" name=\"Smallmodel\">.MODEL SMALL</template>"
                         + "<template autoinsert=\"true\" context=\"asm.editor.context\" deleted=\"false\" description=\"Default Stacksize\" enabled=\"true\" name=\"Defaultstack\">.STACK 100h</template>"
                         + "</templates>");

    store.setDefault(Constants.PREFERENCES_COMPILER_NAME, "");
    store.setDefault(Constants.PREFERENCES_COMPILER_PARAMS, "");
    store.setDefault(Constants.PREFERENCES_LINKER_NAME, "");
    store.setDefault(Constants.PREFERENCES_LINKER_PARAMS, "");
    store.setDefault(Constants.PREFERENCES_LINKER_EXT, "");
    store.setDefault(Constants.PREFERENCES_DEBUGGER_NAME, "");
    store.setDefault(Constants.PREFERENCES_DEBUGGER_PARAMS, "");
  }
}
