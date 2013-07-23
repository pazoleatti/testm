package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookValuesAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookValuesResult;
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
	private final long refBookId; 
	
	private String searchPattern;
	private Date version;
	
	public RefBookPickerWidgetPresenter(MyView view, long refBookId, Long formDataId) {
		super(GINContextHolder.getEventBus(), view);
		dispatcher = GINContextHolder.getDispatchAsync();
		this.refBookId = refBookId;
		getView().setUiHandlers(this);
		
		
		GetRefBookAction getRefBookAction = new GetRefBookAction();
		getRefBookAction.setRefBookId(refBookId);
		getRefBookAction.setFormDataId(formDataId);
		
		dispatcher.execute(getRefBookAction, CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookResult>() {

			@Override
			public void onSuccess(GetRefBookResult result) {
				getView().initView(result.getHeaders(), result.getVersions());
			}
			
		}, this));
	
	}

	interface MyView extends View, HasValue<Long>, HasUiHandlers<RefBookPickerWidgetUiHandlers>{
		void initView(List<String> headers, List<Date> versions);
		void setVersion(Date version);
		Date getVersion();
		String getSearchPattern();
		void updateRowData(int start, List<RefBookItem> values, int size);
		void goToFirstPage();
	}



	@Override
	public void rangeChanged(int startIndex, int maxRows) {
		final int offset = startIndex;
		int max = maxRows;

		GetRefBookValuesAction action = new GetRefBookValuesAction();
		if (searchPattern != null && !searchPattern.trim().isEmpty()) {
			action.setSearchPattern(searchPattern);
		}
		action.setPagingParams(new PagingParams(offset, max));
		action.setRefBookId(refBookId);
		action.setVersion(getView().getVersion());

		dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
				new AbstractCallback<GetRefBookValuesResult>() {
					@Override
					public void onSuccess(GetRefBookValuesResult result) {
						getView().updateRowData(offset, result.getPage().getRecords(), result.getPage().getTotalRecordCount());
					}
				}, this));
		
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Long> handler) {
		// TODO Auto-generated method stub
		return null;
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
		//if (fireEvents){
		//	getView().fireEvent(new );
		//}
	}

	@Override
	public void onSearchPatternChange() {
		String oldValue = searchPattern;
		String newValue = getView().getSearchPattern();
		if (oldValue != null ? !oldValue.equals(newValue) : newValue != null) {
			searchPattern = newValue;
			getView().goToFirstPage();
		}	
	}
	

	@Override
	public void onVersionChange() {
		Date oldValue = version;
		Date newValue = getView().getVersion();
		if (oldValue != null ? !oldValue.equals(newValue) : newValue != null) {
			version = newValue;
			getView().goToFirstPage();
		}
	}

}
