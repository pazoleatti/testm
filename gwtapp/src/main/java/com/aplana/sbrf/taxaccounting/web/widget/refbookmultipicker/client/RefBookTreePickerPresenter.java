package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

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

    interface MyView extends View, HasUiHandlers<RefBookTreePickerUiHandlers> {

        void loadRoot(List<RefBookTreeItem> values);

        void insertChildrens(RefBookUiTreeItem uiTreeItem, List<RefBookTreeItem> values);

        List<RefBookTreeItem> getSelectionValues();

        void ensureVisibleSelectedItem();

        void clearSelected(boolean fireChangeEvent);

        void setSelection(List<RefBookTreeItem> values);

        Set<Long> getSelectedIds();

        /*
         * Инициирет открытие итемов по порядку чьи идентификаторы в листе
         */
        void openListItems(List<Long> ids);
    }

    public RefBookTreePickerPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        dispatcher = GINContextHolder.getDispatchAsync();
        getView().setUiHandlers(this);
        ps = new PickerState();
    }

    @Override
    public void init(final PickerState newState) {
        if (isNeedReloadHeaders(newState)) {
            // Установка новых значений после проверки на новость основных параметров
            ps.setValues(newState);
            load(null);
        } else {
            //иначе просто сеттим
            ps.setValues(newState);
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

        dispatcher.execute(createLoadAction(null, null), CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookTreeValuesResult>() {
            @Override
            public void onSuccess(GetRefBookTreeValuesResult result) {
                getView().loadRoot(result.getPage());
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
        if (stateWithIds.getSetIds() != null && stateWithIds.getSetIds().size() > 0) {
            if (getView().getSelectedIds().isEmpty() || !stateWithIds.getSetIds().containsAll(getView().getSelectedIds())) {
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
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefBookTreeValuesResult>() {
                    @Override
                    public void onSuccess(GetRefBookTreeValuesResult result) {
                        getView().setSelection(result.getPage());
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
        init(new PickerState(ps.getRefBookAttrId(), ps.getFilter(), ps.getSearchPattern(), relevanceDate, ps.isMultiSelect()));
    }

    @Override
    public void find(String searchPattern) {
        init(new PickerState(ps.getRefBookAttrId(), ps.getFilter(), searchPattern, ps.getVersionDate(), ps.isMultiSelect()));
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
        dispatcher.execute(createLoadAction(parent, null), CallbackUtils.defaultCallback(
                new AbstractCallback<GetRefBookTreeValuesResult>() {
                    @Override
                    public void onSuccess(GetRefBookTreeValuesResult result) {
                        // очищаем чилдов, так как там лежит чилд с надписью "Загрузка..."
                        uiTreeItem.removeItems();
                        if (!result.getPage().isEmpty()) {
                            getView().insertChildrens(uiTreeItem, result.getPage());
                        }
                    }
                }, this));
    }

    private boolean isNeedReloadHeaders(PickerState newPs) {
        return WidgetUtils.isWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                WidgetUtils.isWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                WidgetUtils.isWasChange(ps.getSearchPattern(), newPs.getSearchPattern()) ||
                WidgetUtils.isWasChange(ps.getVersionDate(), newPs.getVersionDate());
    }

    /* Проверка на изменения входных параметров*/
    private boolean isNewParams(PickerState newPs) {
        Boolean hasChange =
                WidgetUtils.isWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                        WidgetUtils.isWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                        WidgetUtils.isWasChange(ps.getVersionDate(), newPs.getVersionDate()) ||
                        WidgetUtils.isWasChange(ps.getFilter(), newPs.getFilter()) ||
                        WidgetUtils.isWasChange(ps.getSearchPattern(), newPs.getSearchPattern());


        if (hasChange) {
            ps.setRefBookAttrId(newPs.getRefBookAttrId());
            ps.setFilter(newPs.getFilter());
            ps.setSearchPattern(newPs.getSearchPattern());
            ps.setVersionDate(newPs.getVersionDate());
            ps.setMultiSelect(newPs.isMultiSelect());
        }

        return hasChange;
    }

    /* Создание и заполнения модели экшена для загрузки с сервера*/
    private GetRefBookTreeValuesAction createLoadAction(RefBookTreeItem parent, ArrayList<Long> longs) {
        GetRefBookTreeValuesAction action = new GetRefBookTreeValuesAction();

        action.setSearchPattern(ps.getSearchPattern());
        action.setFilter(ps.getFilter());
        action.setRefBookAttrId(ps.getRefBookAttrId());
        action.setVersion(ps.getVersionDate());
        action.setParent(parent);
        action.setIdsTofind(longs);
        return action;
    }

}
