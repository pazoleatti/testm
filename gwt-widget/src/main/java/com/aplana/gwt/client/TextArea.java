package com.aplana.gwt.client;

/**
 * TextArea, который в задимсабленном состоянии показывает Label.
 * @author Vitaliy Samolovskikh
 */
public class TextArea extends DoubleStateWrapper<com.google.gwt.user.client.ui.TextArea, String> {
	public TextArea() {
		super(new com.google.gwt.user.client.ui.TextArea());
	}

	public int getVisibleLines() {
		return widget.getVisibleLines();
	}

	public void setVisibleLines(int lines) {
		widget.setVisibleLines(lines);
	}
}
