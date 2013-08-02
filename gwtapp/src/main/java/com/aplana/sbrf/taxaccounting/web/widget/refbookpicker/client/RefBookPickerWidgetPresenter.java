package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookValuesAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookValuesResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.InitRefBookAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.InitRefBookResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.RefBookItem;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class RefBookPickerWidgetPresenter extends PresenterWidget<RefBookPickerWidgetPresenter.MyView> implements RefBookPickerWidgetUiHandlers{
	
	private final DispatchAsync dispatcher;
	
	private Long value;
	private String dereferenceValue;
	
	private Long refBookAttrId;
	
	private String searchPattern;
	private Date version;
	

	@Override
	public void init(final long refBookAttrId, Date date1, Date date2) {
		
		
		InitRefBookAction initRefBookAction = new InitRefBookAction();
		initRefBookAction.setRefBookAttrId(refBookAttrId);
		initRefBookAction.setDate1(date1);
		initRefBookAction.setDate2(date2);
		
		dispatcher.execute(initRefBookAction, CallbackUtils.defaultCallback(new AbstractCallback<InitRefBookResult>() {

			@Override
			public void onSuccess(InitRefBookResult result) {
				RefBookPickerWidgetPresenter.this.refBookAttrId = refBookAttrId;
				getView().setHeaders(result.getHeaders());
				getView().setVersions(result.getVersions());
				getView().refreshData();
			}
			
		}, this));
		
	}

	
	public RefBookPickerWidgetPresenter(MyView view) {
		super(GINContextHolder.getEventBus(), view);
		dispatcher = GINContextHolder.getDispatchAsync();
		getView().setUiHandlers(this);
	}

	interface MyView extends View, HasValue<Long>, HasUiHandlers<RefBookPickerWidgetUiHandlers>{
		void setVersions(List<Date> versions);
		void setHeaders(List<String> headers);
		
		void setVersion(Date version);
		Date getVersion();
		
		String getSearchPattern();
		
		void setRowData(int start, List<RefBookItem> values, int size);
		RefBookItem getSelectionValue();
		void refreshData();
		void widgetFireChangeEvent(Long value);
		HandlerRegistration widgetAddValueHandler(ValueChangeHandler<Long> handler);
	}



	@Override
	public void rangeChanged(int startIndex, int maxRows) {
		if (refBookAttrId == null){
			return;
		}
		final int offset = startIndex;
		int max = maxRows;

		GetRefBookValuesAction action = new GetRefBookValuesAction();
		if (searchPattern != null && !searchPattern.trim().isEmpty()) {
			action.setSearchPattern(searchPattern);
		}
		action.setPagingParams(new PagingParams(offset, max));
		action.setRefBookAttrId(refBookAttrId);
		action.setVersion(getView().getVersion());

		dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
				new AbstractCallback<GetRefBookValuesResult>() {
					@Override
					public void onSuccess(GetRefBookValuesResult result) {
						getView().setRowData(offset, result.getPage().getRecords(), result.getPage().getTotalRecordCount());
					}
				}, this));
		
	}

	@Override
	public Long getValue() {
		return value;
	}

	@Override
	public void setValue(Long value) {
		this.value = value;
	}

	@Override
	public void setValue(Long value, boolean fireEvents) {
		setValue(value);
		if (fireEvents){
			getView().widgetFireChangeEvent(value);
		}
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Long> handler) {
		return getView().widgetAddValueHandler(handler);
	}

	@Override
	public void onSearchPatternChange() {
		String oldValue = searchPattern;
		String newValue = getView().getSearchPattern();
		if (oldValue != null ? !oldValue.equals(newValue) : newValue != null) {
			searchPattern = newValue;
			getView().refreshData();
		}	
	}
	

	@Override
	public void onVersionChange() {
		Date oldValue = version;
		Date newValue = getView().getVersion();
		if (oldValue != null ? !oldValue.equals(newValue) : newValue != null) {
			version = newValue;
			getView().refreshData();
		}
	}
	
	@Override
	public void onSelectionChange() {
		RefBookItem item = getView().getSelectionValue();
		dereferenceValue = item.getDereferenceValue();
		setValue(item.getId(), true);
	}


	@Override
	public String getDereferenceValue() {
		return dereferenceValue;
	}


}
