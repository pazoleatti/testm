package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class RefBookMultiPickerPresenter extends PresenterWidget<RefBookMultiPickerPresenter.MyView>
        implements RefBookMultiPickerUiHandlers {
	
	private final DispatchAsync dispatcher;

    private Long refBookAttrId;
	private String filter;
    private Date relevanceDate;
    private int sortColumnIndex;
    private boolean isSortAscending;
	

	@Override
	public void init(final long refBookAttrId, final String filter, Date relevanceDate) {
        this.refBookAttrId = refBookAttrId;
        this.relevanceDate = relevanceDate;
        this.filter = filter;

		InitRefBookMultiAction initRefBookMultiAction = new InitRefBookMultiAction();
		initRefBookMultiAction.setRefBookAttrId(refBookAttrId);
		
		dispatcher.execute(initRefBookMultiAction, CallbackUtils.defaultCallback(new AbstractCallback<InitRefBookMultiResult>() {
			@Override
			public void onSuccess(InitRefBookMultiResult result) {
				getView().setHeaders(result.getHeaders());
				getView().refreshDataAndGoToFirstPage();
			}
		}, this));
		isSortAscending = true;
	}

	
	public RefBookMultiPickerPresenter(MyView view) {
		super(GINContextHolder.getEventBus(), view);
		dispatcher = GINContextHolder.getDispatchAsync();
		getView().setUiHandlers(this);
	}

	interface MyView extends View, HasValue<List<Long>>, HasUiHandlers<RefBookMultiPickerUiHandlers>{
		void setHeaders(Map<String, Integer> headers);
		
		void setVersion(Date version);
		
		String getSearchPattern();
		
		void setRowData(int start, List<RefBookItem> values, int size);

        List<RefBookItem> getSelectionValues();

        void refreshDataAndGoToFirstPage();

        void widgetFireChangeEvent(List<Long> value);

        HandlerRegistration widgetAddValueHandler(ValueChangeHandler<List<Long>> handler);
	}

	@Override
	public void rangeChanged(int startIndex, int maxRows) {
		if (refBookAttrId == null){
			return;
		}
		if (relevanceDate == null){
            getView().setRowData(0, new ArrayList<RefBookItem>(), 0);
            return;
		}
		final int offset = startIndex;

		GetRefBookMultiValuesAction action = new GetRefBookMultiValuesAction();
		action.setSearchPattern(getView().getSearchPattern());
		action.setFilter(filter);
        action.setSortAscending(isSortAscending);
        action.setSortAttributeIndex(sortColumnIndex);
		action.setPagingParams(new PagingParams(offset + 1, maxRows));
		action.setRefBookAttrId(refBookAttrId);
		action.setVersion(relevanceDate);

		dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
				new AbstractCallback<GetRefMultiBookValuesResult>() {
					@Override
					public void onSuccess(GetRefMultiBookValuesResult result) {
						getView().setRowData(offset, result.getPage(), result.getPage().getTotalCount());
					}
				}, this));
		
	}

    @Override
    public void onSort(Integer columnIndex, boolean isSortAscending){
        sortColumnIndex = columnIndex;
        this.isSortAscending = isSortAscending;
        getView().refreshDataAndGoToFirstPage();
    }

	@Override
	public void searche() {
		getView().refreshDataAndGoToFirstPage();
	}
	

	@Override
	public void versionChange() {
		getView().refreshDataAndGoToFirstPage();
	}
}
