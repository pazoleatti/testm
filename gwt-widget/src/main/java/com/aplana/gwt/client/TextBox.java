package com.aplana.gwt.client;

/**
 * TextBox, который в задимсабленном состоянии показывает Label.
 * @author Vitaliy Samolovskikh
 */
public class TextBox extends DoubleStateWrapper<com.google.gwt.user.client.ui.TextBox, String> {
	public TextBox() {
		super(new com.google.gwt.user.client.ui.TextBox());
	}
}
