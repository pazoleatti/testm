package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryResult;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryResult;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

import java.io.Serializable;

/**
 * Асинхронный провайдер для таблицы, получающий даные с сервера
 * с использованием фильтра и кода справочника.
 *
 * @author Eugene Stetsenko
 */
public abstract class DictionaryDataProvider<A extends DictionaryAction<R, T>, R extends DictionaryResult<T>, T extends Serializable>
		extends AsyncDataProvider<DictionaryItem<T>> {

	private final DispatchAsync dispatcher;
	private final String dictionaryCode;

	/**
	 * @param dictionaryCode Код справочника.
	 */
	public DictionaryDataProvider(String dictionaryCode) {
		dispatcher = ((ClientGinjector) DelayedBindRegistry.getGinjector()).getDispatchAsync();
		this.dictionaryCode = dictionaryCode;
	}

	@Override
	protected void onRangeChanged(final HasData<DictionaryItem<T>> display) {
		final Range range = display.getVisibleRange();
		final int offset = range.getStart();
		final int max = range.getLength();

		A action = createAction();
		action.setOffset(offset);
		action.setMax(max);
		action.setDictionaryCode(dictionaryCode);
		dispatcher.execute(action, new AbstractCallback<R>() {
			@Override
			public void onReqSuccess(R result) {
				updateRowData(offset, result.getDictionaryItems());
				updateRowCount(result.getSize(), true);
			}
		});
	}

	protected abstract A createAction();
}
