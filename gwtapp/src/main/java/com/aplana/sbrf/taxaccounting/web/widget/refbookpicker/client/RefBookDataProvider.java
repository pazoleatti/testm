package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.RefBookItem;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Асинхронный провайдер для таблицы, получающий даные с сервера с
 * использованием фильтра и кода справочника.
 * 
 * 
 * @author Eugene Stetsenko, Semyon Goryachkin
 */
public class RefBookDataProvider extends AsyncDataProvider<RefBookItem>
		implements HasHandlers {

	private final EventBus eventBus;
	private final DispatchAsync dispatcher;
	private final long refBookId;

	private String searchPattern;

	/**
	 * @param refBookId
	 */
	public RefBookDataProvider(Long refBookId) {
		this.eventBus = GINContextHolder.getEventBus();
		this.dispatcher = GINContextHolder.getDispatchAsync();
		this.refBookId = refBookId;
	}

	@Override
	protected void onRangeChanged(final HasData<RefBookItem> display) {
		load(display.getVisibleRange());
	}

	/**
	 * Загружает значения справочника в определенном диапазоне.
	 * 
	 * @param range
	 *            диапазон данных
	 */
	public void load(Range range) {
		final int offset = range.getStart();
		int max = range.getLength();

		GetRefBookAction action = new GetRefBookAction();
		if (searchPattern != null && !searchPattern.trim().isEmpty()) {
			action.setSearchPattern(searchPattern);
		}
		action.setPagingParams(new PagingParams(offset, max));
		action.setRefBookId(refBookId);

		dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
				new AbstractCallback<GetRefBookResult>() {
					@Override
					public void onSuccess(GetRefBookResult result) {
						updateRowData(offset, result.getPage().getRecords());
						updateRowCount(result.getPage().getTotalRecordCount(),
								true);
					}
				}, this));
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}
	
	
	public String getSearchPattern() {
		return searchPattern;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEventFromSource(event, this);
	}
}
