package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
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

        void clearSelected(boolean fireChangeEvent);

        void setSelection(List<RefBookTreeItem> values);
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
        } else {
            //иначе просто сеттим
            ps.setValues(newState);
            trySelect(ps);
        }
    }

    private void trySelect(PickerState stateWithIds) {
        if (stateWithIds.getSetIds() != null && stateWithIds.getSetIds().size() > 0) {
            loadingForSelection(stateWithIds.getSetIds());
        } else {
            getView().setSelection(new ArrayList<RefBookTreeItem>());
        }
    }


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
    public void reload(Date relevanceDate) {
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
        dispatcher.execute(createLoadAction(parent, null), CallbackUtils.defaultCallbackNoLock(
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
        return RefBookPickerUtils.itWasChange(ps.getRefBookAttrId(), newPs.getRefBookAttrId()) ||
                RefBookPickerUtils.itWasChange(ps.isMultiSelect(), newPs.isMultiSelect()) ||
                RefBookPickerUtils.itWasChange(ps.getSearchPattern(), newPs.getSearchPattern()) ||
                RefBookPickerUtils.itWasChange(ps.getVersionDate(), newPs.getVersionDate());
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
