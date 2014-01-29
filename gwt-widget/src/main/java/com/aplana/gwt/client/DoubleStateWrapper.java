package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Обертка для виджетов, которая в задисабленном состоянии вместо виджета выводит лабел со значением.
 *
 * @param <T> тип возвращаемого виджетом значения
 *
 * @author Vitaliy Samolovskikh
 */
public class DoubleStateWrapper<T> extends DoubleStateComposite implements HasValue<T>, HasValueChangeHandlers<T> {
	/**
	 * Исходный виджет, к нему будут делегироваться все обращения.
	 */
	private Widget widget;

	public DoubleStateWrapper(Widget widget) {
		this.widget = widget;
		initWidget(widget);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue() {
		if(widget instanceof HasValue){
			return (T)((HasValue)widget).getValue();
		} else {
			throw new UnsupportedOperationException("The widget is not instance of HasValue.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(T value) {
		if(widget instanceof HasValue){
			((HasValue)widget).setValue(value);
		} else {
			throw new UnsupportedOperationException("The widget is not instance of HasValue.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(T value, boolean fireEvents) {
		if(widget instanceof HasValue){
			((HasValue)widget).setValue(value, fireEvents);
		} else {
			throw new UnsupportedOperationException("The widget is not instance of HasValue.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		if(widget instanceof ValueChangeHandler){
			return ((HasValueChangeHandlers)widget).addValueChangeHandler(handler);
		} else {
			throw new UnsupportedOperationException("The widget is not instance of HasValueChangeHandlers.");
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		if(widget instanceof HasEnabled){
			((HasEnabled) widget).setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
}
