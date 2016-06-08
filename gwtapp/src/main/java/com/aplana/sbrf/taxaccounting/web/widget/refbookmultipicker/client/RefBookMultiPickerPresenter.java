package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.handler.DeferredInvokeHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

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

        void setAttributes(List<RefBookAttribute> attributes);

        void setRowData(int start, List<RefBookItem> values, int size);

        void setSelection(List<RefBookItem> values);

        Set<Long> getSelectedIds();

        /**
         * Перезагрузка данных если таблица уже отображается
         * и если данные еще не были загружены
          * @param force флаг нужна ли силовая перезагрузка если даже данные уже загружены
         */
        void refresh(boolean force);

        void showVersionDate(boolean versioned);
    }

    public RefBookMultiPickerPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        dispatcher = GINContextHolder.getDispatchAsync();
        getView().setUiHandlers(this);
        ps = new PickerState();
    }

    @Override
    public void init(final PickerState newState) {
        if (newState.getPickerContext() == null) {
            newState.setPickerContext(ps.getPickerContext());
        }
        //Убрал кэширование, т.к есть вероятность отображения неактуальных данных
        // Установка новых значений после проверки на новость основных параметров
        ps.setValues(newState);
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
                        getView().setAttributes(result.getAttributes());
                        getView().refresh(true);
                        getView().showVersionDate(result.isVersioned());
                        trySelect(ps);
                    }
                }, this)
        );
        /*if (isNeedReloadHeaders(newState)) {

        } else {
            //иначе просто сеттим
            ps.setValues(newState);
            trySelect(ps);
            getView().refresh(false);
        }*/
    }

    private void trySelect(PickerState stateWithIds){
        if (stateWithIds.getSetIds()!= null && !stateWithIds.getSetIds().isEmpty()) {
            if (getView().getSelectedIds().isEmpty() || !stateWithIds.getSetIds().containsAll(getView().getSelectedIds())){
                loadingForSelection(stateWithIds.getSetIds(), null);
            }
            ps.getSetIds().clear();
        } else {
            getView().setSelection(new ArrayList<RefBookItem>(0));
        }
    }

    @Override
    public void reload(Date relevanceDate) {
        init(new PickerState(ps.getRefBookAttrId(), ps.getFilter(), ps.getSearchPattern(), relevanceDate, ps.isMultiSelect(), ps.isExactSearch()));
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
        action.setContext(ps.getPickerContext());
        dispatcher.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<GetRefMultiBookValuesResult>() {
                    @Override
                    public void onSuccess(GetRefMultiBookValuesResult result) {
                        getView().setRowData(offset, result.getPage(), result.getPage().getTotalCount());
                        LogAddEvent.fire(RefBookMultiPickerPresenter.this, result.getUuid());
                    }
                }, this
        ));

    }

    @Override
    public void loadingForSelection(Collection<Long> ids, final DeferredInvokeHandler handler) {
        if (ps.getRefBookAttrId() == null) {
            return;
        }
        if (ps.getVersionDate() == null) {
            return;
        }
        GetRefBookMultiValuesAction action = getRowsLoadAction(null, new ArrayList<Long>(ids));
        action.setContext(ps.getPickerContext());
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefMultiBookValuesResult>() {
                    @Override
                    public void onSuccess(GetRefMultiBookValuesResult result) {
                        if (result.getUuid() == null) {
                            getView().setSelection(result.getPage());
                            LogAddEvent.fire(RefBookMultiPickerPresenter.this, result.getUuid());
                            if (handler != null) {
                                handler.onInvoke();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        if (handler != null) {
                            handler.onInvoke();
                        }
                    }
                }, this
        ));
    }

    private GetRefBookMultiValuesAction getRowsLoadAction(PagingParams pagingParams, List<Long> idToFinds) {
        GetRefBookMultiValuesAction action = new GetRefBookMultiValuesAction();
        action.setSearchPattern(ps.getSearchPattern());
        action.setExactSearch(ps.isExactSearch());
        action.setFilter(ps.getFilter());
        action.setSortAscending(isSortAscending);
        action.setSortAttributeIndex(sortColumnIndex);
        action.setPagingParams(pagingParams);
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setVersion(ps.getVersionDate());
        action.setIdsTofind(idToFinds);
        action.setContext(ps.getPickerContext());
        return action;
    }

    @Override
    public void onSort(Integer columnIndex, boolean isSortAscending) {
        sortColumnIndex = columnIndex;
        this.isSortAscending = isSortAscending;
        getView().refresh(true);
    }

    @Override
    public void find(String searchPattern, boolean exactSearch) {
        ps.setSearchPattern(searchPattern);
        ps.setExactSearch(exactSearch);
    }

    @Override
    public void getValuesCount(String text, boolean exactSearch, final CheckValuesCountHandler checkValuesCountHandler) {
        GetCountFilterValuesAction action = new GetCountFilterValuesAction();
        action.setSearchPattern(text);
        action.setExactSearch(exactSearch);
        action.setFilter(ps.getFilter());
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setVersion(ps.getVersionDate());
        action.setContext(ps.getPickerContext());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetCountFilterValuesResult>() {
            @Override
            public void onSuccess(GetCountFilterValuesResult result) {
                checkValuesCountHandler.onGetValuesCount(result.getCount());
            }
        }, this));

    }

    private boolean isNeedReloadHeaders(PickerState newPs) {
        return WidgetUtils.isWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                WidgetUtils.isWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                WidgetUtils.isWasChange(ps.getVersionDate(), newPs.getVersionDate()) ||
                WidgetUtils.isWasChange(ps.getFilter(), newPs.getFilter()) ||
                WidgetUtils.isWasChange(ps.getSearchPattern(), newPs.getSearchPattern()) ||
                WidgetUtils.isWasChange(ps.isExactSearch(), newPs.isExactSearch());
    }
}
