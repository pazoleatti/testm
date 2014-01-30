package com.aplana.gwt.client;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Обертка для виджетов, которая в задисабленном состоянии вместо виджета выводит лабел со значением.
 *
 * @param <T> тип возвращаемого виджетом значения
 *
 * @author Vitaliy Samolovskikh
 */
public class DoubleStateWrapper<W extends Widget, T> extends DoubleStateComposite implements HasValue<T>, HasName, IsEditor<ValueBoxEditor<T>>, LeafValueEditor<T> {
	/**
	 * Исходный виджет, к нему будут делегироваться все обращения.
	 */
	protected W widget;

	/**
	 * Имя поля виджета
	 */
	private String name;

	public DoubleStateWrapper(W widget) {
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
			setLabelValue(value);
			((HasValue)widget).setValue(value);
		} else {
			throw new UnsupportedOperationException("The widget is not instance of HasValue.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(T value, boolean fireEvents) {
		if(widget instanceof HasValue){
			setLabelValue(value);
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ValueBoxEditor<T> asEditor() {
		if(widget instanceof IsEditor){
			return ((IsEditor<ValueBoxEditor<T>>) widget).asEditor();
		} else {
			throw new UnsupportedOperationException("The widget is not IsEditor.");
		}
	}
}
