package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, который содержит в себе хандлеры для виджетов. Создан для унификации поведения и переиспользования кода.
 *
 * @author Vitaliy Samolovskikh
 */
class ValueChangeHandlerHolder<T> implements HasValueChangeHandlers<T> {
	/**
	 * Обработчики событий изменения значения виджета.
	 */
	private List<ValueChangeHandler<T>> handlers = new ArrayList<ValueChangeHandler<T>>();

	/**
	 * Добавляет обработчик событий
	 */
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		handlers.add(handler);
		return new ValueChangeHandlerRegistration<T>(this, handler);
	}

	/**
	 * Кидает событие всем обработчикам, если это событие ValueChangeEvent
	 *
	 * @param event событие
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (event != null && event instanceof ValueChangeEvent) {
			for (ValueChangeHandler handler : handlers) {
				handler.onValueChange((ValueChangeEvent) event);
			}
		}
	}

	/**
	 * Удаляет обработчик событий.
	 *
	 * @param handler обработчик событий.
	 */
	void removeHandler(ValueChangeHandler<T> handler) {
		handlers.remove(handler);
	}
}
