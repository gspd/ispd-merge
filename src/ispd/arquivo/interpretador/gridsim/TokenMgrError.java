/* Generated By:JavaCC: Do not edit this line. TokenMgrError.java Version 5.0 */
/* JavaCCOptions: */
/*
 * Copyright © 2002 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * California 95054, U.S.A. All rights reserved.  Sun Microsystems, Inc. has
 * intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation,
 * these intellectual property rights may include one or more of the U.S.
 * patents listed at http://www.sun.com/patents and one or more additional
 * patents or pending patent applications in the U.S. and in other countries.
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.  Use is subject to license terms.
 * Sun,  Sun Microsystems,  the Sun logo and  Java are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries.  This
 * product is covered and controlled by U.S. Export Control laws and may be
 * subject to the export or import laws in other countries.  Nuclear, missile,
 * chemical biological weapons or nuclear maritime end uses or end users,
 * whether direct or indirect, are strictly prohibited.  Export or reexport
 * to countries subject to U.S. embargo or to entities identified on U.S.
 * export exclusion lists, including, but not limited to, the denied persons
 * and specially designated nationals lists is strictly prohibited.
 */
package ispd.arquivo.interpretador.gridsim;

/** Token Manager Error. */
public class TokenMgrError extends Error
{

  /**
   * The version identifier for this Serializable class.
   * Increment only if the <i>serialized</i> form of the
   * class changes.
   */
  private static final long serialVersionUID = 1L;

  /*
   * Ordinals for various reasons why an Error of this type can be thrown.
   */

  /**
   * Lexical error occurred.
   */
  static final int LEXICAL_ERROR = 0;

  /**
   * An attempt was made to create a second instance of a static token manager.
   */
  static final int STATIC_LEXER_ERROR = 1;

  /**
   * Tried to change to an invalid lexical state.
   */
  static final int INVALID_LEXICAL_STATE = 2;

  /**
   * Detected (and bailed out of) an infinite loop in the token manager.
   */
  static final int LOOP_DETECTED = 3;

  /**
   * Indicates the reason why the exception is thrown. It will have
   * one of the above 4 values.
   */
  int errorCode;

  /**
   * Replaces unprintable characters by their escaped (or unicode escaped)
   * equivalents in the given string
   */
  protected static final String addEscapes(String str) {
    StringBuffer retval = new StringBuffer();
    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i))
      {
        case 0 :
          continue;
        case '\b':
          retval.append("\\b");
          continue;
        case '\t':
          retval.append("\\t");
          continue;
        case '\n':
          retval.append("\\n");
          continue;
        case '\f':
          retval.append("\\f");
          continue;
        case '\r':
          retval.append("\\r");
          continue;
        case '\"':
          retval.append("\\\"");
          continue;
        case '\'':
          retval.append("\\\'");
          continue;
        case '\\':
          retval.append("\\\\");
          continue;
        default:
          if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
            String s = "0000" + Integer.toString(ch, 16);
            retval.append("\\u" + s.substring(s.length() - 4, s.length()));
          } else {
            retval.append(ch);
          }
          continue;
      }
    }
    return retval.toString();
  }

  /**
   * Returns a detailed message for the Error when it is thrown by the
   * token manager to indicate a lexical error.
   * Parameters :
   *    EOFSeen     : indicates if EOF caused the lexical error
   *    curLexState : lexical state in which this error occurred
   *    errorLine   : line number when the error occurred
   *    errorColumn : column number when the error occurred
   *    errorAfter  : prefix that was seen before this error occurred
   *    curchar     : the offending character
   * Note: You can customize the lexical error message by modifying this method.
   */
  protected static String LexicalError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar) {
    return("Lexical error at line " +
          errorLine + ", column " +
          errorColumn + ".  Encountered: " +
          (EOFSeen ? "<EOF> " : ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int)curChar + "), ") +
          "after : \"" + addEscapes(errorAfter) + "\"");
  }

  /**
   * You can also modify the body of this method to customize your error messages.
   * For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not
   * of end-users concern, so you can return something like :
   *
   *     "Internal Error : Please file a bug report .... "
   *
   * from this method for such cases in the release version of your parser.
   */
  public String getMessage() {
    return super.getMessage();
  }

  /*
   * Constructors of various flavors follow.
   */

  /** No arg constructor. */
  public TokenMgrError() {
  }

  /** Constructor with message and reason. */
  public TokenMgrError(String message, int reason) {
    super(message);
    errorCode = reason;
  }

  /** Full Constructor. */
  public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
    this(LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);
  }
}
/* JavaCC - OriginalChecksum=0d875b6620add87afe506871d6a2d45d (do not edit this line) */
