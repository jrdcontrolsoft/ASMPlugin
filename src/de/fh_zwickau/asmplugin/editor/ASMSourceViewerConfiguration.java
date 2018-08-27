package de.fh_zwickau.asmplugin.editor;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import de.fh_zwickau.asmplugin.Constants;

/**
 * Configurations for the ASMEditor. Provides sytax hightlighting.
 * 
 * @author Andy Reek
 * @since 15.11.2005
 */
public class ASMSourceViewerConfiguration extends SourceViewerConfiguration {

  /**
   * The underlying ASM editor.
   */
  private ASMEditor editor;

  /**
   * Rule scanner for default ASM code.
   */
  private ASMCodeScanner asmcodescanner = null;

  /**
   * Rule scanner for comment, multi-comment and string parts.
   */
  private PropertyChangeRuleBaseScanner[] scanner = new PropertyChangeRuleBaseScanner[3];

  /**
   * The constructor.
   * 
   * @param editor The underlying ASM editor.
   */
  public ASMSourceViewerConfiguration(ASMEditor editor) {
    this.editor = editor;
  }

  /**
   * {@inheritDoc}
   */
  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
    ContentAssistant assistant = new ContentAssistant();
    assistant.setContentAssistProcessor(new ASMCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
    assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    assistant.enableAutoActivation(true);
    assistant.enableAutoInsert(true);
    assistant.setAutoActivationDelay(500);

    return assistant;
  }

  /**
   * {@inheritDoc}
   */
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();

    asmcodescanner = new ASMCodeScanner(editor);
    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(asmcodescanner);
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    scanner[0] = new PropertyChangeRuleBaseScanner(editor, Constants.PREFERENCES_TEXTCOLOR_COMMENT);
    dr = new DefaultDamagerRepairer(scanner[0]);
    reconciler.setDamager(dr, Constants.PARTITION_COMMENT_MULTI);
    reconciler.setRepairer(dr, Constants.PARTITION_COMMENT_MULTI);

    scanner[1] = new PropertyChangeRuleBaseScanner(editor, Constants.PREFERENCES_TEXTCOLOR_COMMENT);
    dr = new DefaultDamagerRepairer(scanner[1]);
    reconciler.setDamager(dr, Constants.PARTITION_COMMENT_SINGLE);
    reconciler.setRepairer(dr, Constants.PARTITION_COMMENT_SINGLE);

    scanner[2] = new PropertyChangeRuleBaseScanner(editor, Constants.PREFERENCES_TEXTCOLOR_STRING);
    dr = new DefaultDamagerRepairer(scanner[2]);
    reconciler.setDamager(dr, Constants.PARTITION_STRING);
    reconciler.setRepairer(dr, Constants.PARTITION_STRING);

    return reconciler;
  }

  /**
   * {@inheritDoc}
   */
  public IReconciler getReconciler(ISourceViewer sourceViewer) {
    IReconcilingStrategy reconcilingStrategy = new ASMReconcilingStategy(editor);

    MonoReconciler reconciler = new MonoReconciler(reconcilingStrategy, false);
    reconciler.setProgressMonitor(new NullProgressMonitor());
    reconciler.setDelay(500);

    return reconciler;
  }

  /**
   * Remove all rule scanners from property change listener.
   */
  public void dispose() {
    if (asmcodescanner != null) {
      asmcodescanner.dispose();
    }

    for (int i = 0; i < scanner.length; i++) {
      if (scanner[i] != null) {
        scanner[i].dispose();
      }
    }
  }
}
