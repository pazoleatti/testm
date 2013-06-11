package com.aplana.sbrf.taxaccounting.web.widget.incrementbutton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Поле ввода с кнопками инкремента/декремента
 * @author Eugene Stetsenko
 */
public class IncrementButton extends Composite implements IncrementButtonView, HasEnabled, HasVisibility, HasValue<Integer> {


	interface IncrementButtonUiBinder extends UiBinder<Widget, IncrementButton> {}

	private static IncrementButtonUiBinder ourUiBinder = GWT.create(IncrementButtonUiBinder.class);

	private final Integer MIN_VALUE;
	private final Integer MAX_VALUE;

	@UiField
	TextBox textBox;

	@UiField
	Button upButton;

	@UiField
	Button downButton;

	@UiConstructor
	public IncrementButton(Integer minValue, Integer maxValue) {
		initWidget(ourUiBinder.createAndBindUi(this));
		MIN_VALUE = minValue;
		MAX_VALUE = maxValue;
	}

	@Override
	public Integer incValue() {
		Integer nextValue = this.getValue() + 1;
		this.setValue(nextValue);
		return nextValue;
	}

	@Override
	public Integer decValue() {
		Integer nextValue = this.getValue() - 1;
		this.setValue(nextValue);
		return nextValue;
	}

	@Override
	public boolean isEnabled() {
		return textBox.isEnabled() &&
				upButton.isEnabled() &&
				downButton.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		textBox.setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}

	@Override
	public Integer getValue() {
		return Integer.parseInt(textBox.getValue().isEmpty() ? MIN_VALUE.toString() : textBox.getValue());
	}

	@Override
	public void setValue(Integer value) {
		if (value>=MIN_VALUE && value<=MAX_VALUE) {
			textBox.setValue(value.toString());
		}
	}

	@Override
	public void setValue(Integer value, boolean fireEvents) {
		if (value>=MIN_VALUE && value<=MAX_VALUE) {
			textBox.setValue(value.toString(), fireEvents);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@UiHandler("upButton")
	public void onUpButton(ClickEvent event){
		incValue();
	}

	@UiHandler("downButton")
	public void onDownButton(ClickEvent event){
		decValue();
	}
}
