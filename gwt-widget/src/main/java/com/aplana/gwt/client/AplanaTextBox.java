package com.aplana.gwt.client;

import com.google.gwt.user.client.ui.TextBox;

/**
 * TextBox, который в задимсабленном состоянии показывает Label.
 * @author Vitaliy Samolovskikh
 */
public class AplanaTextBox extends DoubleStateWrapper<String> {
	public AplanaTextBox() {
		super(new TextBox());
	}
}
