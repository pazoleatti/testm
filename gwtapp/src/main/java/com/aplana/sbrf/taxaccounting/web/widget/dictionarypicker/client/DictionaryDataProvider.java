package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ApplicationContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryResult;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Асинхронный провайдер для таблицы, получающий даные с сервера
 * с использованием фильтра и кода справочника.
 *
 * @param <A> действие
 * @param <T> тип значения справочника
 * @author Eugene Stetsenko
 */
public abstract class DictionaryDataProvider<A extends DictionaryAction<T>, T extends Serializable>
		extends AsyncDataProvider<DictionaryItem<T>> implements HasHandlers {

	private final EventBus eventBus;
	private final DispatchAsync dispatcher;
	
	private final String dictionaryCode;

	protected String searchPattern = null;

	/**
	 * @param dictionaryCode Код справочника.
	 */
	public DictionaryDataProvider(String dictionaryCode) {
		this.eventBus = ApplicationContextHolder.getEventBus();
		this.dispatcher = ApplicationContextHolder.getDispatchAsync();
		this.dictionaryCode = dictionaryCode;
	}

	@Override
	protected void onRangeChanged(final HasData<DictionaryItem<T>> display) {
		load(display.getVisibleRange());
	}

	/**
	 * Загружает значения справочника в определенном диапазоне.
	 *
	 * @param range диапазон данных
	 */
	public void load(Range range) {
		final int offset = range.getStart();
		int max = range.getLength();

		A action = createAction();
		if (searchPattern != null && !searchPattern.isEmpty()) {
			action.setSearchPattern(getSearchPattern());
		}
		action.setOffset(offset);
		action.setMax(max);
		action.setDictionaryCode(dictionaryCode);
		dispatcher.execute(action, CallbackUtils
			.defaultCallbackNoLock(new AbstractCallback<DictionaryResult<T>>() {
				@Override
				public void onSuccess(DictionaryResult<T> result) {
					updateRowData(offset, result.getDictionaryItems());
					updateRowCount(result.getSize().intValue(), true);
				}
			}, this));
	}

	/**
	 * Создает действие по получению данных. Так как это класс абстрактный для всех типов справочников,
	 * то создание действия перенесено в дочерние классы.
	 */
	protected abstract A createAction();

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEventFromSource(event, this);
	}
}
