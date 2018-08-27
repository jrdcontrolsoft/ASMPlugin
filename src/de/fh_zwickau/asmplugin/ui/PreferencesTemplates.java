package de.fh_zwickau.asmplugin.ui;

import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.editor.ASMEditor;

/**
 * Preference page of Templates.
 * 
 * @author Andy Reek
 * @since 30.10.2005
 */
public class PreferencesTemplates extends TemplatePreferencePage {

  /**
   * Creates the page and set a ContextTypeRegistry, TemplateStore and
   * PreferenceStore.
   */
  public PreferencesTemplates() {
    noDefaultAndApplyButton();
    setContextTypeRegistry(ASMEditor.getContextTypeRegistry());
    setTemplateStore(Activator.getTemplateStore());
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isShowFormatterSetting() {
    return false;
  }
}
