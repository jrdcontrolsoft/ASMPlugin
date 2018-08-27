package de.fh_zwickau.asmplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Builder for ASM-Files.
 * 
 * @author Andy Reek
 * @since 25.11.2005
 */
public class ASMBuilder extends IncrementalProjectBuilder {

  /**
   * {@inheritDoc}
   */
  protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    String compiler = store.getString(Constants.PREFERENCES_COMPILER_NAME).trim();

    if (compiler.length() < 1) {
      return null;
    }

    Activator.getConsole().bringConsoleToFront();
    Activator.getConsole().println(Messages.BUILDING_TEXT_CONSOLE);
    Activator.getConsole().println();

    if (kind == IncrementalProjectBuilder.FULL_BUILD) {
      fullBuild(monitor);
    } else {
      IResourceDelta delta = getDelta(getProject());
      if (delta == null) {
        fullBuild(monitor);
      } else {
        incrementalBuild(delta, monitor);
      }
    }

    return null;
  }

  /**
   * Builds the hole project.
   * 
   * @param monitor A monitor.
   */
  private void fullBuild(IProgressMonitor monitor) {
    try {
      IProject project = getProject();
      monitor.beginTask(Messages.BUILDING_TITLE, 100);
      monitor.subTask(Messages.BUILDING_TEXT_COMPILE);
      project.accept(new MyFullBuildVisitor());
      linkFiles(project, monitor);
      monitor.done();
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      Activator.getDefault().getLog().log(e.getStatus());
    }
  }

  /**
   * Builds only the projekt delta.
   * 
   * @param delta The given delta.
   * @param monitor A monitor.
   */
  private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
    try {
      IProject project = getProject();
      monitor.beginTask(Messages.BUILDING_TITLE, 100);
      monitor.subTask(Messages.BUILDING_TEXT_COMPILE);
      delta.accept(new MyIncrementalBuildVisitor());
      linkFiles(project, monitor);
      monitor.done();
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      Activator.getDefault().getLog().log(e.getStatus());
    }
  }

  /**
   * Compiles a given file.
   * 
   * @param file The file to be compiled.
   */
  private void compileFile(IFile file) {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    String compiler = store.getString(Constants.PREFERENCES_COMPILER_NAME);
    String params = store.getString(Constants.PREFERENCES_COMPILER_PARAMS);

    if ((compiler == null) || (compiler.trim().length() < 1)) {
      return;
    }

    if (params == null) {
      params = "";
    }

    compiler = compiler.trim();
    params = params.trim();

    String dir = file.getLocation().toOSString();

    int pos = dir.lastIndexOf(System.getProperty("file.separator"));

    if (pos > 0) {
      dir = dir.substring(0, pos);
    }

    dir += System.getProperty("file.separator") + Constants.BUILD_DIRECTORY;

    boolean success = true;

    if (!(new File(dir)).exists()) {  // If directory not exists create new.
      success = (new File(dir)).mkdir();
    }

    if (success) {
      String dest = dir + System.getProperty("file.separator") + file.getName();
      File fdest = new File(dest);

      copyFile(new File(file.getLocation().toOSString()), fdest, true);

      String output = ProgramExecuter.exec(compiler.trim(), params.trim(), dest, dir, ProgramExecuter.ALTMODEONC, true,
                                           true, true, false);
      Activator.getConsole().println(output);

      if (fdest.exists()) {
        fdest.delete();
      }
    }
  }

  /**
   * Copy a file from source to destination.
   * 
   * @param src Source File
   * @param dst Destination File
   * @param force If destination exists than delete it.
   * @return True if copy successful.
   */
  private boolean copyFile(File src, File dst, boolean force) {
    if (!src.exists()) { // If source not exists don't copy them.
      return false;
    }

    if (dst.exists()) {
      dst.delete();
    }

    try {
      FileInputStream fis = new FileInputStream(src);
      FileOutputStream fos = new FileOutputStream(dst);

      byte[] buf = new byte[1024];
      int i = 0;

      while ((i = fis.read(buf)) != -1) {
        fos.write(buf, 0, i);
      }

      fis.close();
      fos.close();
    } catch (Exception e) {
      return false;
    }

    if (dst.exists()) {
      return true;
    }

    return false;
  }

  /**
   * Link a given project.
   * 
   * @param project The project for linking-process.
   * @param monitor The monitor.
   */
  private void linkFiles(IProject project, IProgressMonitor monitor) {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    String dir = project.getLocation().makeAbsolute().toOSString();
    String linker = store.getString(Constants.PREFERENCES_LINKER_NAME);
    String params = store.getString(Constants.PREFERENCES_LINKER_PARAMS);
    String ext = store.getString(Constants.PREFERENCES_LINKER_EXT);

    if ((linker == null) || (linker.trim().length() < 1)) {
      return;
    }

    if ((ext == null) || (ext.trim().length() < 1)) {
      ext = "obj";
    }

    if (ext.charAt(0) == '.') {
      ext = ext.substring(1, ext.length());
    }

    if (params == null) {
      params = "";
    }

    linker = linker.trim();
    params = params.trim();
    ext = ext.trim();

    ArrayList<String[]> lnkfiles = new ArrayList<String[]>();

    linkFilesInDirectory(dir, linker.trim(), params.trim(), "." + ext, lnkfiles);

    if (lnkfiles.size() > 0) {
      monitor.subTask(Messages.BUILDING_TEXT_LINKING);

      String output = "";
      int cnt = 0;
      int pos = 0;
      int i = 0;
      int p = 0;
      String subdir = "";

      for (String[] items : lnkfiles) {
        pos = (cnt * 49) / lnkfiles.size();
        cnt++;
        monitor.worked(50 + pos);

        subdir = "";

        if (items.length > 0) {
          i = 0;

          while ((items[i].length() < 1) && (i < items.length)) {
            i++;
          }

          if (items[i].length() > 0) {
            p = items[i].lastIndexOf(System.getProperty("file.separator"));

            if (p > 0) {
              subdir = items[i].substring(0, p);
            }
          }
        }

        if (subdir.length() < 1) {
          subdir = dir;
        }

        output = ProgramExecuter.exec(linker, params, items, subdir, ProgramExecuter.ALTMODEONC, true, true, true,
                                      false);
        Activator.getConsole().println(output);
      }
    }
  }

  /**
   * Recursive function to link files in a directory
   * 
   * @param dir Working directory.
   * @param linker Path to linker.
   * @param params Params of linker.
   * @param ext Input-file-extension of linker.
   * @param lnkfiles Files to be linked.
   */
  private void linkFilesInDirectory(String dir, String linker, String params, String ext, ArrayList<String[]> lnkfiles) {
    File file = new File(dir);

    if (file == null) {
      return;
    }

    File[] files = file.listFiles();

    if (files == null) {
      return;
    }

    ArrayList<String> itemlist = new ArrayList<String>();
    String item = "";

    for (int i = 0; i < files.length; i++) {
      item = files[i].getPath();

      if (files[i].isFile()) {
        if (item.toLowerCase().endsWith(ext.toLowerCase())) {
          itemlist.add(item);
        }
      } else {
        linkFilesInDirectory(item, linker, params, ext, lnkfiles);
      }
    }

    if (itemlist.size() > 0) {
      String[] items = itemlist.toArray(new String[0]);
      lnkfiles.add(items);
    }
  }

  /**
   * Class for visiting all resources when a full build is initiated.
   * 
   * @author andre
   * @since 25.11.2005
   */
  private class MyFullBuildVisitor implements IResourceVisitor {
    /**
     * {@inheritDoc}
     */
    public boolean visit(IResource resource) throws CoreException {
      int resourceType = resource.getType();

      if ((resourceType == IResource.FOLDER) || (resourceType == IResource.PROJECT)) {
        return true;
      } else if (resourceType == IResource.FILE) {
        String extension = resource.getFileExtension();
        if ((extension != null) && extension.equalsIgnoreCase("asm")) {
          compileFile((IFile) resource);
        }
      }

      return false;
    }
  }

  /**
   * Class for visiting all resources when a incremental build is initiated.
   * 
   * @author andre
   * @since 25.11.2005
   */
  private class MyIncrementalBuildVisitor implements IResourceDeltaVisitor {
    /**
     * {@inheritDoc}
     */
    public boolean visit(IResourceDelta delta) throws CoreException {
      int deltaKind = delta.getKind();

      if ((deltaKind == IResourceDelta.ADDED) || (deltaKind == IResourceDelta.CHANGED)) {
        IResource resource = delta.getResource();
        int resourceType = resource.getType();

        if ((resourceType == IResource.FOLDER) || (resourceType == IResource.PROJECT)) {
          return true;
        } else if (resourceType == IResource.FILE) {
          String extension = resource.getFileExtension();
          if ((extension != null) && extension.equalsIgnoreCase("asm")) {
            compileFile((IFile) resource);
          }
        }
      }

      return false;
    }
  }
}
