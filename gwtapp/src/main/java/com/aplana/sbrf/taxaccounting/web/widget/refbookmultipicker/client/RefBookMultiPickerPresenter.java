package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
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
    private PickerState ps;
    private Integer sortColumnIndex;
    private boolean isSortAscending = true;

    interface MyView extends View, HasUiHandlers<RefBookMultiPickerUiHandlers> {

        void setHeaders(Map<String, Integer> headers);

        void setRowData(int start, List<RefBookItem> values, int size);

        void setSelection(List<RefBookItem> values);

        void refresh();
    }

    public RefBookMultiPickerPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        dispatcher = GINContextHolder.getDispatchAsync();
        getView().setUiHandlers(this);
        ps = new PickerState();
    }

    @Override
    public void init(final PickerState newState) {
        if (isNeedReloadHeaders(newState)) {
            // Установка новых значений после проверки на новость основных параметров
            setNewState(newState);
            if (ps.getRefBookAttrId() == null) {
                return;
            }
            if (ps.getVersionDate() == null) {
                return;
            }

            dispatcher.execute(new InitRefBookMultiAction(ps.getRefBookAttrId()),
                    CallbackUtils.defaultCallback(new AbstractCallback<InitRefBookMultiResult>() {
                        @Override
                        public void onSuccess(InitRefBookMultiResult result) {
                            getView().setHeaders(result.getHeaders());
                            getView().refresh();

                            if (newState.getSetIds().size() > 0) {
                                loadingForSelection(newState.getSetIds());
                            } else {
                                getView().setSelection(new ArrayList<RefBookItem>());
                            }
                        }
                    }, this));
        } else {
            //иначе просто сеттим
            setNewState(newState);
            if (newState.getSetIds().size() > 0) {
                loadingForSelection(newState.getSetIds());
            } else {
                getView().setSelection(new ArrayList<RefBookItem>());
            }
            getView().refresh();
        }
    }

    @Override
    public void reload(Date relevanceDate) {
        init(new PickerState(ps.getRefBookAttrId(), ps.getFilter(), ps.getSearchPattern(), relevanceDate, ps.isMultiSelect()));
    }

    @Override
    public void rangeChanged(int startIndex, int maxRows) {
        if (ps.getRefBookAttrId() == null) {
            return;
        }
        if (ps.getVersionDate() == null) {
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
    public void loadingForSelection(Collection<Long> ids) {
        if (ps.getRefBookAttrId() == null) {
            return;
        }
        if (ps.getVersionDate() == null) {
            return;
        }

        GetRefBookMultiValuesAction action = getRowsLoadAction(null, new ArrayList<Long>(ids));
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefMultiBookValuesResult>() {
                    @Override
                    public void onSuccess(GetRefMultiBookValuesResult result) {
                        getView().setSelection(result.getPage());
                    }
                }, this));

    }

    private GetRefBookMultiValuesAction getRowsLoadAction(PagingParams pagingParams, List<Long> idToFinds) {
        GetRefBookMultiValuesAction action = new GetRefBookMultiValuesAction();
        action.setSearchPattern(ps.getSearchPattern());
        action.setFilter(ps.getFilter());
        action.setSortAscending(isSortAscending);
        action.setSortAttributeIndex(sortColumnIndex);
        action.setPagingParams(pagingParams);
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setVersion(ps.getVersionDate());
        action.setIdsTofind(idToFinds);

        return action;
    }

    @Override
    public void onSort(Integer columnIndex, boolean isSortAscending) {
        sortColumnIndex = columnIndex;
        this.isSortAscending = isSortAscending;
        getView().refresh();
    }

    @Override
    public void find(String searchPattern) {
        ps.setSearchPattern(searchPattern);
    }

    private boolean isNeedReloadHeaders(PickerState newPs) {
        return RefBookPickerUtils.itWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                RefBookPickerUtils.itWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                RefBookPickerUtils.itWasChange(ps.getVersionDate(), newPs.getVersionDate());
    }

    private void setNewState(PickerState newPs) {
        ps.setRefBookAttrId(newPs.getRefBookAttrId());
        ps.setFilter(newPs.getFilter());
        ps.setSearchPattern(newPs.getSearchPattern());
        ps.setVersionDate(newPs.getVersionDate());
        ps.setMultiSelect(newPs.isMultiSelect());
    }

    /* Проверка на изменения входных параметров*/
    private boolean isNewParams(PickerState newPs) {
        Boolean hasChange =
                RefBookPickerUtils.itWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                        RefBookPickerUtils.itWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                        RefBookPickerUtils.itWasChange(ps.getVersionDate(), newPs.getVersionDate()) ||
                        RefBookPickerUtils.itWasChange(ps.getFilter(), newPs.getFilter()) ||
                        RefBookPickerUtils.itWasChange(ps.getSearchPattern(), newPs.getSearchPattern());


        if (hasChange) {
            ps.setRefBookAttrId(newPs.getRefBookAttrId());
            ps.setFilter(newPs.getFilter());
            ps.setSearchPattern(newPs.getSearchPattern());
            ps.setVersionDate(newPs.getVersionDate());
            ps.setMultiSelect(newPs.isMultiSelect());
        }

        return hasChange;
    }

}
