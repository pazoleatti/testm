package com.aplana.sbrf.taxaccounting.web.module.formdata.client.search;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.event.SetFocus;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SearchAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SearchResult;
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
        void setTableData(int start, List<FormDataSearchResult> resultList, int size);
        void updateData();
        void updatePageSize();
        void updateData(int pageNumber);
        void clearTableData();
        void clearSearchInput();

        boolean isCaseSensitive();
    }

    private final DispatchAsync dispatcher;
    private Long formDataId;
    private Integer formTemplateId;
    private List<Integer> hiddenColumns;

    @Inject
    public FormSearchPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void setFormTemplateId(Integer formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    @Override
    public void setFormDataId(Long formDataId){
        this.formDataId = formDataId;
        getView().clearSearchInput();
    }

    @Override
    public void open(){
        getView().clearTableData();
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
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SearchResult>() {
            @Override
            public void onSuccess(SearchResult result) {
            getView().setTableData(start, result.getResults(), result.getSize());
            }
        }, this));
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
    public void close(){
        getView().clearSearchInput();
        getView().clearTableData();
    }
}
