package de.fh_zwickau.asmplugin.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.Messages;

/**
 * Wizard for creating new files.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class WizardNewFile extends Wizard implements INewWizard {

  /**
   * The first page of the wizard.
   */
  private WizardNewFileCreationPage page1;

  /**
   * The second page of the wizard.
   */
  private TemplateFileWizardPage page2;

  private IStructuredSelection selection;

  /**
   * The constructor.
   */
  public WizardNewFile() {
    super();
    setWindowTitle(Messages.WIZARD_NEW_FILE_TITLE);
  }

  /**
   * {@inheritDoc}
   */
  public void addPages() {
    super.addPages();

    page1 = new WizardNewFileCreationPage(Messages.WIZARD_NEW_FILE_PAGE1_TITLE, selection) {
      protected boolean validatePage() {
        if (!getFileName().toLowerCase().endsWith(".asm")) {
          setErrorMessage(Messages.WIZARD_NEW_FILE_PAGE1_INVALID_FILE);
          return false;
        }
        return super.validatePage();
      }
    };
    page1.setTitle(Messages.WIZARD_NEW_FILE_PAGE1_TITLE);
    page1.setImageDescriptor(Constants.WIZARD_NEW);
    page1.setDescription(Messages.WIZARD_NEW_FILE_PAGE1_DESCRIPTION);

    page2 = new TemplateFileWizardPage(Messages.WIZARD_NEW_FILE_PAGE2_TITLE);
    page2.setTitle(Messages.WIZARD_NEW_FILE_PAGE2_TITLE);
    page2.setImageDescriptor(Constants.WIZARD_NEW);
    page2.setDescription(Messages.WIZARD_NEW_FILE_PAGE2_DESCRIPTION);

    addPage(page1);
    addPage(page2);
  }

  /**
   * {@inheritDoc}
   */
  public boolean performFinish() {
    boolean template = page2.isTemplateFile();
    File tfile = template ? page2.getSelectedFile() : null;
    IFile file = page1.createNewFile();

    if (file == null) {
      return false;
    }

    IEditorPart editpart = null;
    IDE.setDefaultEditor(file, "ASMPlugin.editor1");

    try {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      editpart = IDE.openEditor(page, file, true);
    } catch (PartInitException ex) {
      return false;
    }

    if ((editpart != null) && template && (tfile != null)) {
      if (editpart instanceof AbstractDecoratedTextEditor) {
        AbstractDecoratedTextEditor adedit = (AbstractDecoratedTextEditor) editpart;

        if (adedit != null) {
          IDocument doc = adedit.getDocumentProvider().getDocument(adedit.getEditorInput());

          if (doc != null) {
            String line;
            StringBuffer text = new StringBuffer("");

            try {
              FileInputStream fis = new FileInputStream(tfile.getPath());
              BufferedReader br = new BufferedReader(new InputStreamReader(fis));

              while ((line = br.readLine()) != null) {
                text.append(line);
                if ((line.length() < 1) || ((line.length() > 0) && (line.charAt(line.length() - 1) != '\n'))) {
                  text.append("\n");
                }
              }

              fis.close();
              br.close();
            } catch (Exception e) {
              Activator.getDefault().getLog().log(
                                                  new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
                                                             Messages.ASMCREATEFROMTEMP_ERROR, e));
            }

            doc.set(text.toString());
          }
        }
      }
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.selection = selection;
  }
}
