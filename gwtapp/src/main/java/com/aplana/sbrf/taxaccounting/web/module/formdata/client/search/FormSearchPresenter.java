package com.aplana.sbrf.taxaccounting.web.module.formdata.client.search;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.event.SetFocus;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SearchAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SearchResult;
import com.google.gwt.user.client.Cookies;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * Created by auldanov on 27.03.2014.
 */
public class FormSearchPresenter extends PresenterWidget<FormSearchPresenter.MyView> implements FormSearchUiHandlers {
    public interface MyView extends PopupView, HasUiHandlers<FormSearchUiHandlers> {
        String getSearchKey();
        void setSearchKey(String searchKey);
        void setReadOnlyMode(boolean readOnlyMode);
        void setManual(boolean manual);
        void setTableData(int start, List<FormDataSearchResult> resultList, int size);
        void updateData();
        void updatePageSize();
        void updateData(int pageNumber);
        void clearTableData();
        void clearSearchInput();

        boolean isCaseSensitive();
        boolean isReadOnlyMode();
        boolean isManual();
        void clearSelection();
    }

    private final DispatchAsync dispatcher;
    private Long formDataId;
    private Integer formTemplateId;
    private List<Integer> hiddenColumns;
    private boolean absoluteView;
    private int sessionId;

    @Inject
    public FormSearchPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        generateNewSessionId();
    }

    @Override
    public void setFormTemplateId(Integer formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    @Override
    public void setFormDataId(Long formDataId){
        this.formDataId = formDataId;
    }

    @Override
    public int generateNewSessionId() {
        sessionId = Math.abs((int) System.currentTimeMillis());
        return sessionId;
    }

    @Override
    public void open(boolean readOnlyMode, boolean manual, boolean absoluteView) {
        this.absoluteView = absoluteView;
        String searchKey = getView().getSearchKey();
        if (searchKey == null || searchKey.isEmpty()) {
            getView().setSearchKey(Cookies.getCookie(formDataId.toString()));
        }
        getView().setReadOnlyMode(readOnlyMode);
        getView().setManual(manual);
        getView().clearTableData();
        getView().clearSelection();
    }

    @Override
    public void onRangeChange(final int start, int count) {
        SearchAction action = new SearchAction();
        action.setKey(getView().getSearchKey());
        action.setFrom(start + 1);
        action.setTo(start + count);
        action.setFormDataId(formDataId);
        action.setFormTemplateId(formTemplateId);
        action.setCaseSensitive(getView().isCaseSensitive());
        action.setManual(getView().isManual());
        action.setCorrectionDiff(!absoluteView);
        action.setSessionId(sessionId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SearchResult>() {
            @Override
            public void onSuccess(SearchResult result) {
            getView().setTableData(start, result.getResults(), result.getSize());
            }
        }, this));
    }

    @Override
    public void onHide() {
        clearSearchResults();
    }

    public void clearSearchResults() {
        SearchAction action = new SearchAction();
        action.setSessionId(sessionId);
        action.setJustDelete(true);
        dispatcher.execute(action, CallbackUtils.emptyCallback());
    }

    @Override
    public void onClickFoundItem(Long rowIndex) {
        SetFocus.fire(this, rowIndex);
    }

    @Override
    public void setHiddenColumns(List<Integer> hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    @Override
    public int getHiddenColumnsCountBefore(Integer columnId) {
        int count = 0;
        for (Integer i: hiddenColumns){
            if (i < columnId){
                count++;
            }
        }

        return count;
    }

    @Override
    public void close() {
        Cookies.setCookie(formDataId.toString(), getView().getSearchKey());
        getView().clearSearchInput();
        getView().clearTableData();
    }
}
