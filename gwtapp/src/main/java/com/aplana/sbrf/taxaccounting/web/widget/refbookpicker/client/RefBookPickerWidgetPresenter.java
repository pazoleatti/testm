package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.*;
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

    private RefBookItem selectedItem;

    private Long refBookAttrId;
	private String filter;
    private int sortColumnIndex;
    private boolean isSortAscending;
	

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
					getView().setVersions(result.getVersions(), result.getDefaultValue());
				}
				RefBookPickerWidgetPresenter.this.filter = filter;
				getView().refreshDataAndGoToFirstPage();
			}
			
		}, this));
		isSortAscending = true;
	}

	
	public RefBookPickerWidgetPresenter(MyView view) {
		super(GINContextHolder.getEventBus(), view);
		dispatcher = GINContextHolder.getDispatchAsync();
		getView().setUiHandlers(this);
	}

	interface MyView extends View, HasValue<Long>, HasUiHandlers<RefBookPickerWidgetUiHandlers>{
		void setVersions(List<Date> versions, Date defaultValue);
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
            getView().setRowData(0, new ArrayList<RefBookItem>(), 0);
            return;
		}
		final int offset = startIndex;
		int max = maxRows;

		GetRefBookValuesAction action = new GetRefBookValuesAction();
		action.setSearchPattern(getView().getSearchPattern());
		action.setFilter(filter);
        action.setSortAscending(isSortAscending);
        action.setSortAttributeIndex(sortColumnIndex);
		action.setPagingParams(new PagingParams(offset + 1, max));
		action.setRefBookAttrId(refBookAttrId);
		action.setVersion(version);

		dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
				new AbstractCallback<GetRefBookValuesResult>() {
					@Override
					public void onSuccess(GetRefBookValuesResult result) {
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
		this.selectedItem = getView().getSelectionValue();
		dereferenceValue = selectedItem.getDereferenceValue();
        setValue(selectedItem.getId(), true);
	}

	@Override
	public String getDereferenceValue() {
		return dereferenceValue;
	}

    @Override
    public String getOtherDereferenceValue(String alias) {
        if (selectedItem != null && alias!= null && !alias.isEmpty()) {

            Integer key = null;
            List<String> attrAliases = selectedItem.getValuesAttrAlias();
            for (int i = 0; i < attrAliases.size(); i++) {
                if (alias.equals(attrAliases.get(i))) {
                    key = i;
                    break;
                }
            }

            if(key != null){
                return selectedItem.getValues().get(key);
            }
            return null;
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long id) {
        if (selectedItem != null && id!= null) {

            Integer key = null;
            List<Long> attrIds = selectedItem.getValuesAttrId();
            for (int i = 0; i < attrIds.size(); i++) {
                if (id.equals(attrIds.get(i))) {
                    key = i;
                    break;
                }
            }

            if(key != null){
                return selectedItem.getValues().get(key);
            }
            return null;
        }
        return null;
    }

    @Override
	public void clearValue() {
		dereferenceValue = null;
        selectedItem = null;
		setValue(null, true);
	}
}
