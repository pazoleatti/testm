package com.aplana.gwt.client;

import com.google.gwt.user.client.ui.TextArea;

/**
 * TextArea, который в задимсабленном состоянии показывает Label.
 * @author Vitaliy Samolovskikh
 */
public class AplanaTextArea extends DoubleStateWrapper<TextArea, String> {
	public AplanaTextArea() {
		super(new TextArea());
	}

	public int getVisibleLines() {
		return widget.getVisibleLines();
	}

	public void setVisibleLines(int lines) {
		widget.setVisibleLines(lines);
	}
}
