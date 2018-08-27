package de.fh_zwickau.asmplugin.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Device;

import de.fh_zwickau.asmplugin.Activator;
import de.fh_zwickau.asmplugin.Constants;
import de.fh_zwickau.asmplugin.TextAttributeConverter;

/**
 * RuleBasedScanner for the ASMEditor.
 * 
 * @author Andy Reek
 * @since 15.11.2005
 */
public class ASMCodeScanner extends RuleBasedScanner implements IPropertyChangeListener {

  private Token instructionToken;

  private Token segmentToken;

  private ASMEditor editor;

  /**
   * The constructor.
   * 
   * @param editor The underlying ASMEditor for the CodeScanner.
   */
  public ASMCodeScanner(final ASMEditor editor) {
    this.editor = editor;

    ArrayList<IRule> rules = new ArrayList<IRule>();
    createTokens(editor.getSite().getShell().getDisplay());

    Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    WordRuleCaseInsensitive wordRule = new WordRuleCaseInsensitive();
    HashMap<String, String> instructions = ASMInstructionSet.getInstructions();

    if (instructions != null) {
      for (String instruction : instructions.keySet()) {
        wordRule.addWord(instruction, instructionToken);
      }
    }
    rules.add(wordRule);

    wordRule = new WordRuleCaseInsensitive();
    HashMap<String, String> segments = ASMInstructionSet.getSegments();
    if (segments != null) {
      for (String segment : segments.keySet()) {
        wordRule.addWord(segment, segmentToken);
      }
    }
    rules.add(wordRule);

    setRules(rules.toArray(new IRule[] {}));
  }

  /**
   * Disposes the PropertyChangeListener from the PreferenceStore.
   */
  public void dispose() {
    Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
  }

  /**
   * Create all Tokens.
   * 
   * @param device The device is needed for the color of the Tokens.
   */
  private void createTokens(Device device) {
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    instructionToken = new Token(TextAttributeConverter
                         .preferenceDataToTextAttribute(store.getString(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION)));

    segmentToken = new Token(TextAttributeConverter
                     .preferenceDataToTextAttribute(store.getString(Constants.PREFERENCES_TEXTCOLOR_SEGMENT)));
  }

  /**
   * {@inheritDoc}
   */
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION)) {
      instructionToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
    } else if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_SEGMENT)) {
      segmentToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
    }

    editor.refreshSourceViewer();
  }
}
