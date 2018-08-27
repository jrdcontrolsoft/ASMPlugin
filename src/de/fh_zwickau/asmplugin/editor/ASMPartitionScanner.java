package de.fh_zwickau.asmplugin.editor;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import de.fh_zwickau.asmplugin.Constants;

/**
 * A partition scanner for the ASM editor.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMPartitionScanner extends RuleBasedPartitionScanner {

  /**
   * Partition scanner for the ASMEditor.
   */
  public ASMPartitionScanner() {
    IToken string = new Token(Constants.PARTITION_STRING);
    IToken commentM = new Token(Constants.PARTITION_COMMENT_MULTI);
    IToken commentS = new Token(Constants.PARTITION_COMMENT_SINGLE);

    ArrayList<IRule> rules = new ArrayList<IRule>();
    rules.add(new EndOfLineRule(";", commentS));
    rules.add(new SingleLineRule("\"", "\"", string));
    rules.add(new SingleLineRule("'", "'", string));
    rules.add(new MultiLineRule("comment *", "*", commentM));

    setPredicateRules(rules.toArray(new IPredicateRule[] {}));
  }

  /**
   * Convert all characters to lower case, need for case insensitive
   * MultiLineRule.
   * 
   * {@inheritDoc}
   */
  public int read() {
    int c = super.read();

    if (c != EOF) {
      c = Character.toLowerCase((char) c);
    }

    return c;
  }
}
