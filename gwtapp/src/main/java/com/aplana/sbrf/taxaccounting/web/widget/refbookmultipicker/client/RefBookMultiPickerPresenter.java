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
import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Презентор представления компонента выбора из линейного српавончика
 *
 * @author sgoryachkin
 * @author aivanov
 */
public class RefBookMultiPickerPresenter extends PresenterWidget<RefBookMultiPickerPresenter.MyView>
        implements RefBookMultiPickerUiHandlers {

    private final DispatchAsync dispatcher;

    private Long refBookAttrId;
    private String filter;
    private Date relevanceDate;
    private Boolean multiSelect;
    private Integer sortColumnIndex;
    private boolean isSortAscending = true;

    interface MyView extends View, HasValue<List<Long>>, HasUiHandlers<RefBookMultiPickerUiHandlers> {
        void setHeaders(Map<String, Integer> headers);

        void setVersion(Date version);

        Date getVersion();

        Boolean getMultiSelect();

        String getSearchPattern();

        Long getAttributeId();

        String getFilter();

        void setRowData(int start, List<RefBookItem> values, int size);

        void trySetSelection(List<RefBookItem> values);

        List<RefBookItem> getSelectionValues();

        void refreshDataAndGoToFirstPage();

        void widgetFireChangeEvent(List<Long> value);
    }

    public RefBookMultiPickerPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        dispatcher = GINContextHolder.getDispatchAsync();
        getView().setUiHandlers(this);
        this.multiSelect = getView().getMultiSelect();
    }

    @Override
    public void init(final long refBookAttrId, final String filter, Date relevanceDate, Boolean multiSelect) {
        if (isNewParams()) {
            if (getView().getAttributeId() == null) {
                return;
            }
            if (getView().getVersion() == null) {
                return;
            }

            InitRefBookMultiAction initRefBookMultiAction = new InitRefBookMultiAction();
            initRefBookMultiAction.setRefBookAttrId(getView().getAttributeId());

            dispatcher.execute(initRefBookMultiAction, CallbackUtils.defaultCallback(new AbstractCallback<InitRefBookMultiResult>() {
                @Override
                public void onSuccess(InitRefBookMultiResult result) {
                    getView().setHeaders(result.getHeaders());
                    getView().refreshDataAndGoToFirstPage();
                }
            }, this));
        }
    }

    @Override
    public void reload(Date relevanceDate) {
        init(getView().getAttributeId(), getView().getFilter(), relevanceDate, multiSelect);
    }

    @Override
    public void rangeChanged(int startIndex, int maxRows) {
        if (getView().getAttributeId() == null) {
            return;
        }
        if (getView().getVersion() == null) {
            getView().setRowData(0, new ArrayList<RefBookItem>(), 0);
            return;
        }
        final int offset = startIndex;

        GetRefBookMultiValuesAction action = getRowsLoadAction(new PagingParams(offset + 1, maxRows), null);
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefMultiBookValuesResult>() {
                    @Override
                    public void onSuccess(GetRefMultiBookValuesResult result) {
                        getView().setRowData(offset, result.getPage(), result.getPage().getTotalCount());
                    }
                }, this));

    }

    @Override
    public void loadingForSelection(List<Long> ids) {
        if (getView().getAttributeId() == null) {
            return;
        }
        if (getView().getVersion() == null) {
            return;
        }

        GetRefBookMultiValuesAction action = getRowsLoadAction(null, ids);
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefMultiBookValuesResult>() {
                    @Override
                    public void onSuccess(GetRefMultiBookValuesResult result) {
                        getView().trySetSelection(result.getPage());
                    }
                }, this));

    }

    private GetRefBookMultiValuesAction getRowsLoadAction(PagingParams pagingParams, List<Long> idToFinds) {
        GetRefBookMultiValuesAction action = new GetRefBookMultiValuesAction();
        action.setSearchPattern(getView().getSearchPattern());
        action.setFilter(getView().getFilter());
        action.setSortAscending(isSortAscending);
        action.setSortAttributeIndex(sortColumnIndex);
        action.setPagingParams(pagingParams);
        action.setRefBookAttrId(getView().getAttributeId());
        action.setVersion(getView().getVersion());
        action.setIdsTofind(idToFinds);

        return action;
    }

    @Override
    public void onSort(Integer columnIndex, boolean isSortAscending) {
        sortColumnIndex = columnIndex;
        this.isSortAscending = isSortAscending;
        getView().refreshDataAndGoToFirstPage();
    }

    @Override
    public void search() {
        getView().refreshDataAndGoToFirstPage();
    }


    @Override
    public void versionChange() {
        getView().refreshDataAndGoToFirstPage();
    }


    /* Проверка на изменения входных параметров*/
    // TODO (aivanov) вынести в RefBookPickerUtils
    private boolean isNewParams() {
        Long refBookAttrId = getView().getAttributeId();
        String filter = getView().getFilter();
        Date relevanceDate = getView().getVersion();
        Boolean multiSelect = getView().getMultiSelect();

        Boolean hasChange = (refBookAttrId == null && this.refBookAttrId != null)
                || (refBookAttrId != null && this.refBookAttrId == null)
                || (refBookAttrId != null && this.refBookAttrId != null && !refBookAttrId.equals(this.refBookAttrId))
                || (filter == null && this.filter != null)
                || (filter != null && this.filter == null)
                || (filter != null && this.filter != null && !filter.equals(this.filter))
                || (multiSelect == null && this.multiSelect != null)
                || (multiSelect != null && this.multiSelect == null)
                || (multiSelect != null && this.multiSelect != null && !multiSelect.equals(this.multiSelect))
                || (relevanceDate == null && this.relevanceDate != null)
                || (relevanceDate != null && this.relevanceDate == null)
                || (relevanceDate != null && this.relevanceDate != null && relevanceDate.compareTo(this.relevanceDate) != 0);
        if(hasChange){
            this.refBookAttrId = refBookAttrId;
            this.filter = filter;
            this.relevanceDate = relevanceDate;
            this.multiSelect = multiSelect;
        }


        return hasChange;
    }

}
