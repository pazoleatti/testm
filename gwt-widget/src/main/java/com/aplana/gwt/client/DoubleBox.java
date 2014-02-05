package com.aplana.gwt.client;

/**
 * DoubleBox, в задисабленном состоянии показывает Label
 * @author Vitaliy Samolovskikh
 */
public class DoubleBox extends DoubleStateWrapper<com.google.gwt.user.client.ui.DoubleBox, Double> {
	public DoubleBox() {
		super(new com.google.gwt.user.client.ui.DoubleBox());
		widget.addStyleName("gwt-TextBox");
	}
}
