package com.aplana.sbrf.taxaccounting.web.widget.codemirror;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Simple builder that allows users to configure various codemirror options.
 *
*/
public final class CodeMirrorConfig extends JavaScriptObject {

  public static native CodeMirrorConfig makeBuilder() /*-{
    return {};
  }-*/;

  protected CodeMirrorConfig() {}

  /**
   * Whether, when indenting, the first N*8 spaces should be replaced by N tabs. Default is false.
   */
  public native CodeMirrorConfig setIndentWithTabs(boolean useTabs) /*-{
    this['indentWithTabs'] = useTabs;
    return this;
  }-*/;

  /**
   * Determines whether brackets are matched whenever the cursor is moved next to a bracket.
   */
  public native CodeMirrorConfig setMatchBrackets(boolean matchBrackets) /*-{
    this['matchBrackets'] = matchBrackets;
    return this;
  }-*/;

  /**
   * Whether to show line numbers to the left of the editor.
   */
  public native CodeMirrorConfig setShowLineNumbers(boolean showLineNumbers) /*-{
    this['lineNumbers'] = showLineNumbers;
    return this;
  }-*/;

  /**
   * Sets the starting value of the editor.
   */
  public native CodeMirrorConfig setValue(String value) /*-/*-{
    this['value'] = value;
    return this;
  }-*/;

}
