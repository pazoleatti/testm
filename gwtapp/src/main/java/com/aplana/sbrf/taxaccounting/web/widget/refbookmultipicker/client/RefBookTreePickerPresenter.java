package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookUiTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.dispatch.shared.DispatchRequest;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

import java.util.*;

/**
 * Презентер для представления отображения иерархического справочника
 *
 * @author aivanov
 */
public class RefBookTreePickerPresenter extends PresenterWidget<RefBookTreePickerPresenter.MyView>
        implements RefBookTreePickerUiHandlers {

    private final DispatchAsync dispatcher;

    private PickerState ps;
    private boolean isNeedSelectFirstItem = false;
    private DispatchRequest dispatchRequest;

    public interface MyView extends View, HasUiHandlers<RefBookTreePickerUiHandlers> {

        void setRefBookId(Long refBookId);

        void loadRoot(List<RefBookTreeItem> values, boolean openOnLoad);

        void insertChildrens(RefBookUiTreeItem uiTreeItem, List<RefBookTreeItem> values, boolean openOnLoad);

        List<RefBookTreeItem> getSelectionValues();

        void ensureVisibleSelectedItem();

        void clearSelected(boolean fireChangeEvent);

        void setSelection(List<RefBookTreeItem> values);

        Set<Long> getSelectedIds();

        /*
         * Инициирет открытие итемов по порядку чьи идентификаторы в листе
         */
        void openListItems(List<Long> ids);

        RefBookUiTreeItem getUiTreeItem(Long id);
    }

    public RefBookTreePickerPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        dispatcher = GINContextHolder.getDispatchAsync();
        getView().setUiHandlers(this);
        ps = new PickerState();
    }

    @Override
    public void init(final PickerState newState, boolean force) {
        if (isNeedReloadHeaders(newState)) {
            // Установка новых значений после проверки на новость основных параметров
            ps.setValues(newState);
            load(null);
        } else {
            //иначе просто сеттим
            if (force) {
                ps.setValues(newState);
            }
            trySelect(ps);
        }
    }

    @Override
    public void reload() {
        load(null);
    }

    @Override
    public void reload(List<Long> needToSelectIds) {
        load(needToSelectIds);
    }

    /* Загрузка верхушки дерева */
    private void load(List<Long> needToSelectIds) {
        if (needToSelectIds != null) {
            ps.getSetIds().clear();
            ps.getSetIds().addAll(needToSelectIds);
        }
        if (ps.getRefBookAttrId() == null) {
            return;
        }
        if (ps.getVersionDate() == null) {
            return;
        }

        dispatchRequest = dispatcher.execute(createLoadAction(null, null), CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookTreeValuesResult>() {
            @Override
            public void onSuccess(GetRefBookTreeValuesResult result) {
                getView().setRefBookId(result.getRefBookId());
                getView().loadRoot(result.getPage(), false);
                if (isNeedSelectFirstItem && !result.getPage().isEmpty()) {
                    isNeedSelectFirstItem = false;
                    ps.getSetIds().clear();
                    ps.getSetIds().add(result.getPage().get(0).getId());
                }
                trySelect(ps);
            }
        }, this));
    }

    /**
     * Проверка и выделение переданных идентификаторов
     *
     * @param stateWithIds объект в котором содержится списко идентификаторов которые нужно выделить
     */
    private void trySelect(PickerState stateWithIds) {
        if (stateWithIds.getSetIds() != null && !stateWithIds.getSetIds().isEmpty()) {
            if (getView().getSelectedIds().isEmpty() || !stateWithIds.getSetIds().containsAll(getView().getSelectedIds())) {
                // TODO сделать проверку что эти итемы уже загружены в дерево.
                // загрузим объекты которые должны быть подсвечены как выделенные
                loadingForSelection(stateWithIds.getSetIds());
            } else {
                getView().ensureVisibleSelectedItem();
            }
        } else {
            getView().setSelection(new ArrayList<RefBookTreeItem>());
        }
    }

    /**
     * Загрузка объектов которые будут помешены в модель для выделения
     *
     * @param ids ид записей
     */
    public void loadingForSelection(Collection<Long> ids) {
        if (ps.getRefBookAttrId() == null) {
            return;
        }
        if (ps.getVersionDate() == null) {
            return;
        }
        GetRefBookTreeValuesAction action = createLoadAction(null, new ArrayList<Long>(ids));
        if (ps.getPickerContext() != null) {
            action.setFormDataId(ps.getPickerContext().getFormDataId());
        }
        dispatchRequest = dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefBookTreeValuesResult>() {
                    @Override
                    public void onSuccess(GetRefBookTreeValuesResult result) {
                        getView().setSelection(result.getPage());
                        LogAddEvent.fire(RefBookTreePickerPresenter.this, result.getUuid());
                    }
                }, this));

    }

    @Override
    public void openFor(final Long uniqueRecordId, final boolean isChild) {
        GetHierarchyPathAction action = new GetHierarchyPathAction();
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setUniqueRecordId(uniqueRecordId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<GetHierarchyPathResult>() {
                    @Override
                    public void onSuccess(GetHierarchyPathResult result) {
                        if (!isChild) {
                            result.getIds().add(uniqueRecordId);
                        }
                        getView().openListItems(result.getIds());
                    }
                }, this));
    }

    @Override
    public void reloadForDate(Date relevanceDate) {
        init(new PickerState(ps.getRefBookAttrId(), ps.getFilter(), ps.getSearchPattern(), relevanceDate, ps.isMultiSelect(), ps.isExactSearch()), false);
    }

    @Override
    public void selectFirstItenOnLoad() {
        isNeedSelectFirstItem = true;
    }

    @Override
    public void highLightItem(RefBookUiTreeItem uiTreeItem) {
        uiTreeItem.highLightText(ps.getSearchPattern());
    }

    @Override
    public void getValuesCount(String text, Date versionDate, boolean exactSearch, final CheckValuesCountHandler checkValuesCountHandler) {
        GetCountFilterValuesAction action = new GetCountFilterValuesAction();
        action.setHierarchy(true);
        action.setSearchPattern(text);
        action.setExactSearch(exactSearch);
        action.setFilter(ps.getFilter());
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setVersion(versionDate);
        action.setContext(ps.getPickerContext());
        dispatchRequest = dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetCountFilterValuesResult>() {
            @Override
            public void onSuccess(GetCountFilterValuesResult result) {
                checkValuesCountHandler.onGetValuesCount(result.getCount());
            }
        }, this));
    }

    @Override
    public void cleanPS() {
        ps.setVersionDate(null);
        ps.setSearchPattern(null);
        ps.setExactSearch(false);
    }

    @Override
    public void find(String searchPattern, boolean exactSearch) {
        init(new PickerState(ps.getRefBookAttrId(), ps.getFilter(), searchPattern, ps.getVersionDate(), ps.isMultiSelect(), exactSearch), false);
    }

    @Override
    public void loadForItem(final RefBookUiTreeItem uiTreeItem) {
        if (ps.getRefBookAttrId() == null) {
            return;
        }
        if (ps.getVersionDate() == null) {
            return;
        }
        RefBookTreeItem parent = uiTreeItem.getRefBookTreeItem();
        dispatchRequest = dispatcher.execute(createLoadAction(parent, null), CallbackUtils.defaultCallback(
                new AbstractCallback<GetRefBookTreeValuesResult>() {
                    @Override
                    public void onSuccess(GetRefBookTreeValuesResult result) {
                        // очищаем чилдов, так как там лежит чилд с надписью "Загрузка..."
                        uiTreeItem.removeItems();
                        if (!result.getPage().isEmpty()) {
                            // если у нас searchPattern не пуст то будет загрузка каскадная так как результаты будут фильтровать по нему
                            getView().insertChildrens(uiTreeItem, result.getPage(), false);
                        }

                    }
                }, this));
    }

    private boolean isNeedReloadHeaders(PickerState newPs) {
        return WidgetUtils.isWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                WidgetUtils.isWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                WidgetUtils.isWasChange(ps.getSearchPattern(), newPs.getSearchPattern()) ||
                WidgetUtils.isWasChange(ps.getVersionDate(), newPs.getVersionDate()) ||
                WidgetUtils.isWasChange(ps.isExactSearch(), newPs.isExactSearch()) ||
                newPs.isNeedReload();
    }

    /* Создание и заполнения модели экшена для загрузки с сервера*/
    private GetRefBookTreeValuesAction createLoadAction(RefBookTreeItem parent, List<Long> longs) {
        GetRefBookTreeValuesAction action = new GetRefBookTreeValuesAction();

        action.setSearchPattern(ps.getSearchPattern());
        action.setExactSearch(ps.isExactSearch());
        action.setFilter(ps.getFilter());
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setVersion(ps.getVersionDate());
        action.setParent(parent);
        action.setIdsTofind(longs);
        if (ps.getPickerContext() != null) {
            action.setFormDataId(ps.getPickerContext().getFormDataId());
        }
        return action;
    }

    @Override
    public void cancelRequest() {
        if (dispatchRequest != null && dispatchRequest.isPending()) {
            dispatchRequest.cancel();
            LockInteractionEvent.fire(this, false);
            dispatchRequest = null;
        }
    }
}
