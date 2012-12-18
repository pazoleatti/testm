package com.aplana.sbrf.taxaccounting.web.widget.codemirror;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * A javascript overlay object over a CodeMirror object.
 *
 * (See http://codemirror.net/2/manual.html for the codemirror2 documentation).
 */
public final class CodeMirrorWrapper extends JavaScriptObject {

  protected CodeMirrorWrapper() { }

  /**
   * Get the current editor content.
   */
  public native String getValue() /*-{
    return this.getValue();
  }-*/;

  /**
   * Set the editor content.
   */
  public native void setValue(String code) /*-{
    this.setValue(code);
  }-*/;
  /**
   * Creates a new CodeMirror instance attached to a DOM element.
   *
   * @param hostElement The {@code Element} object the new CodeMirror instance should be added to.
   *
   * @return An overlay type representing a codemirror2 object.
   */
  public static final native CodeMirrorWrapper createEditor(
      Element hostElement,
      com.aplana.sbrf.taxaccounting.web.widget.codemirror.CodeMirrorConfig config) /*-{
    return $wnd.CodeMirror(hostElement, config);
  }-*/;

}
