package com.aplana.gwt.client;

/**
 * LongBox, в задисабленном состоянии показывает Label
 * @author Vitaliy Samolovskikh
 */
public class LongBox extends DoubleStateWrapper<com.google.gwt.user.client.ui.LongBox, Long> {
	public LongBox() {
		super(new com.google.gwt.user.client.ui.LongBox());
		widget.addStyleName("gwt-TextBox");
	}
}
