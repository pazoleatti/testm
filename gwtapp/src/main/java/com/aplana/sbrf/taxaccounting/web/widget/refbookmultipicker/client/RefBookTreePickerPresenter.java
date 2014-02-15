package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.Date;
import java.util.List;

public class RefBookTreePickerPresenter extends PresenterWidget<RefBookTreePickerPresenter.MyView>
        implements RefBookTreePickerUiHandlers {

    private final DispatchAsync dispatcher;

    private Long refBookAttrId;
    private String filter;
    private Date relevanceDate;

    interface MyView extends View, HasUiHandlers<RefBookTreePickerUiHandlers> {

        Date getVersion();

        void setVersion(Date version);

        String getSearchPattern();

        Long getAttributeId();

        String getFilter();

        void loadRoot(List<RefBookTreeItem> values);

        void insertChildrens(RefBookUiTreeItem uiTreeItem, List<RefBookTreeItem> values);

        List<RefBookTreeItem> getSelectionValues();

        void widgetFireChangeEvent(List<Long> value);
    }

    public RefBookTreePickerPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        dispatcher = GINContextHolder.getDispatchAsync();
        getView().setUiHandlers(this);
    }

    @Override
    public void init(final long refBookAttrId, final String filter, Date relevanceDate) {
        if (isNewParams()) {
            if (getView().getAttributeId() == null) {
                return;
            }
            if (getView().getVersion() == null) {
                return;
            }
            dispatcher.execute(createLoadAction(null), CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookTreeValuesResult>() {
                @Override
                public void onSuccess(GetRefBookTreeValuesResult result) {
                    getView().loadRoot(result.getPage());
                }
            }, this));
        }
    }

    @Override
    public void reload() {
        this.init(refBookAttrId, getView().getSearchPattern(), getView().getVersion());
    }

    @Override
    public void loadForItem(final RefBookUiTreeItem uiTreeItem) {
        if (refBookAttrId == null) {
            return;
        }
        if (relevanceDate == null) {
            return;
        }
        RefBookTreeItem parent = uiTreeItem.getRefBookTreeItem();
        dispatcher.execute(createLoadAction(parent), CallbackUtils.defaultCallbackNoLock(
                new AbstractCallback<GetRefBookTreeValuesResult>() {
                    @Override
                    public void onSuccess(GetRefBookTreeValuesResult result) {
                        // очищаем чилдов, так как там лежит чил с надписью "Загрузка..."
                        uiTreeItem.removeItems();
                        if (!result.getPage().isEmpty()) {
                            getView().insertChildrens(uiTreeItem, result.getPage());
                        }
                    }
                }, this));
    }

    @Override
    public void search() {
    }

    @Override
    public void versionChange() {

    }

    /* Проверка на изменения входных параметров*/
    // TODO (aivanov) вынести в RefBookPickerUtils
    private boolean isNewParams() {
        Long refBookAttrId = getView().getAttributeId();
        String filter = getView().getFilter();
        Date relevanceDate = getView().getVersion();
        Boolean hasChange = (refBookAttrId == null && this.refBookAttrId != null)
                || (refBookAttrId != null && this.refBookAttrId == null)
                || (refBookAttrId != null && this.refBookAttrId != null && !refBookAttrId.equals(this.refBookAttrId))
                || (filter == null && this.filter != null)
                || (filter != null && this.filter == null)
                || (filter != null && this.filter != null && !filter.equals(this.filter))
                || (relevanceDate == null && this.relevanceDate != null)
                || (relevanceDate != null && this.relevanceDate == null)
                || (relevanceDate != null && this.relevanceDate != null && relevanceDate.compareTo(this.relevanceDate) != 0);
        if(hasChange){
            this.refBookAttrId = refBookAttrId;
            this.filter = filter;
            this.relevanceDate = relevanceDate;
        }
        return hasChange;
    }

    /* Создание и заполнения модели экшена для загрузки с сервера*/
    private GetRefBookTreeValuesAction createLoadAction(RefBookTreeItem parent) {
        GetRefBookTreeValuesAction action = new GetRefBookTreeValuesAction();

        action.setSearchPattern(getView().getSearchPattern());
        action.setFilter(filter);
        action.setRefBookAttrId(refBookAttrId);
        action.setVersion(relevanceDate);
        action.setParent(parent);
        action.setSearchPattern(getView().getSearchPattern());
        return action;
    }

}
