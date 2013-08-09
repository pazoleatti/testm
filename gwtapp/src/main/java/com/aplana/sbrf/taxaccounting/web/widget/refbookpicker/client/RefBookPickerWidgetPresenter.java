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
	private String filter;
	

	@Override
	public void init(final long refBookAttrId, final String filter, Date date1, Date date2) {
		
		
		InitRefBookAction initRefBookAction = new InitRefBookAction();
		initRefBookAction.setRefBookAttrId(refBookAttrId);
		initRefBookAction.setDate1(date1);
		initRefBookAction.setDate2(date2);
		
		dispatcher.execute(initRefBookAction, CallbackUtils.defaultCallback(new AbstractCallback<InitRefBookResult>() {

			@Override
			public void onSuccess(InitRefBookResult result) {
				RefBookPickerWidgetPresenter.this.refBookAttrId = refBookAttrId;
				getView().setHeaders(result.getHeaders());
				if (!result.getVersions().isEmpty()){
					getView().setVersions(result.getVersions());
				}
				RefBookPickerWidgetPresenter.this.filter = filter;
				getView().refreshDataAndGoToFirstPage();
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
		void refreshDataAndGoToFirstPage();
		void widgetFireChangeEvent(Long value);
		HandlerRegistration widgetAddValueHandler(ValueChangeHandler<Long> handler);
	}



	@Override
	public void rangeChanged(int startIndex, int maxRows) {
		if (refBookAttrId == null){
			return;
		}
		Date version = getView().getVersion();
		if (version == null){
			return;
		}
		final int offset = startIndex;
		int max = maxRows;

		GetRefBookValuesAction action = new GetRefBookValuesAction();
		action.setSearchPattern(getView().getSearchPattern());
		action.setFilter(filter);
		action.setPagingParams(new PagingParams(offset, max));
		action.setRefBookAttrId(refBookAttrId);
		action.setVersion(version);

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
	public void searche() {
		getView().refreshDataAndGoToFirstPage();
	}
	

	@Override
	public void versionChange() {
		getView().refreshDataAndGoToFirstPage();
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


	@Override
	public void clearValue() {
		dereferenceValue = null;
		setValue(null, true);
	}


}
