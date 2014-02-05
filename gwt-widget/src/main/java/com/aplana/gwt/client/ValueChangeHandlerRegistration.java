package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Регистратор для обработчика событий изменения значения виджета.
 *
 * @author Vitaliy Samolovskikh
 * @see com.aplana.gwt.client.ValueChangeHandlerHolder
 */
public class ValueChangeHandlerRegistration<T> implements HandlerRegistration {
	/**
	 * Держатель обработчиков событий.
	 */
	private final ValueChangeHandlerHolder<T> handlerHolder;

	/**
	 * Обработчик события.
	 */
	private final ValueChangeHandler<T> handler;

	/**
	 * @param handlerHolder держатель обработчиков событий
	 * @param handler       обработчик событий
	 */
	public ValueChangeHandlerRegistration(ValueChangeHandlerHolder<T> handlerHolder, ValueChangeHandler<T> handler) {
		this.handlerHolder = handlerHolder;
		this.handler = handler;
	}

	/**
	 * Удаляет обработчик событий, связанный с этим регистратором из списка обработчиков событий виджета.
	 */
	@Override
	public void removeHandler() {
		handlerHolder.removeHandler(handler);
	}
}

