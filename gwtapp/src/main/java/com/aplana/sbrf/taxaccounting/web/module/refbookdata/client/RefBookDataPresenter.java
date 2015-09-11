package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.AbstractEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear.RefBookLinearPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Date;

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers, SetFormMode.SetFormModeHandler {

    @Override
    public void onSetFormMode(SetFormMode event) {
        setMode(event.getFormMode());
        dataInterface.setMode(event.getFormMode());
    }

    /*@Override
    public void onUpdateForm(UpdateForm event) {
        if (event.isSuccess()) {
            getView().resetSearchInputBox();
            recordId = event.getRecordChanges().getId();
            *//*dataInterface.updateData();*//*
        }
    }*/

    @ProxyCodeSplit
    @NameToken(RefBookDataTokens.refBookData)
    public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
    }

    static final Object TYPE_editFormPresenter = new Object();
    static final Object TYPE_mainFormPresenter = new Object();

    private Long refBookId;

    private Long recordId;
    private FormMode mode;
    private String refBookName;
    private IRefBookExecutor dataInterface;
    /** Признак того, что справочник версионируемый */
    private boolean versioned;

    EditFormPresenter editFormPresenter;
    AbstractEditPresenter commonEditPresenter;
    RefBookVersionPresenter versionPresenter;
    DialogPresenter dialogPresenter;
    RefBookLinearPresenter refBookLinearPresenter;

    private final HandlerRegistration[] registrations = new HandlerRegistration[1];

    private final DispatchAsync dispatcher;
    private final TaPlaceManager placeManager;

    public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
        void setRefBookNameDesc(String verCount, Date relDate);
        void setRefBookNameDesc(String desc);
        Date getRelevanceDate();
        /** Метод для получения строки с поля фильтрации*/
        String getSearchPattern();
        /** Сброс значения поля поиска */
        void resetSearchInputBox();
        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        /** доступность  кнопки-ссылка "Создать запрос на изменение..." для справочника "Организации-участники контролируемых сделок" */
        void updateSendQuery(boolean isAvailable);
        //Показывает/скрывает поля, которые необходимы только для версионирования
        void setVersionedFields(boolean isVisible);

        /**
         * Устанавливает версионный вид справочника.
         * @param isVersion true - если переходим в версионное представление
         */
        void setVersionView(boolean isVersion);
    }

    @Inject
    public RefBookDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter,
                                RefBookVersionPresenter versionPresenter, DialogPresenter dialogPresenter,
                                RefBookLinearPresenter refBookLinearPresenter,
                                PlaceManager placeManager, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = (TaPlaceManager)placeManager;
        this.editFormPresenter = editFormPresenter;
        this.versionPresenter = versionPresenter;
        this.dialogPresenter = dialogPresenter;
        this.refBookLinearPresenter = refBookLinearPresenter;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_editFormPresenter);
        clearSlot(TYPE_mainFormPresenter);
        for (HandlerRegistration han : registrations){
            han.removeHandler();
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        LogCleanEvent.fire(this);
        setInSlot(TYPE_editFormPresenter, editFormPresenter);
        setInSlot(TYPE_mainFormPresenter, refBookLinearPresenter);

        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookId);
        dispatcher.execute(checkAction, CallbackUtils.defaultCallback(
                new AbstractCallback<CheckRefBookResult>() {
                    @Override
                    public void onSuccess(CheckRefBookResult result) {
                        versioned = result.isVersioned();
                        if (result.isAvailable()) {
                            registrations[0] = commonEditPresenter.addClickHandlerForAllVersions(new ClickHandler() {
                                @Override
                                public void onClick(ClickEvent event) {
                                    recordId = refBookLinearPresenter.getSelectedRow().getRefBookRowId();
                                    getView().setVersionView(true);
                                    clearSlot(TYPE_mainFormPresenter);
                                    setInSlot(TYPE_mainFormPresenter, versionPresenter);
                                    versionPresenter.setUniqueRecordId(recordId);

                                    /*refBookLinearPresenter.changeProvider(true);*/
                                    commonEditPresenter.setVersionMode(true);
                                    commonEditPresenter.setCurrentUniqueRecordId(null);
                                    commonEditPresenter.setRecordId(null);

                                    GetRefBookAttributesAction action = new GetRefBookAttributesAction();
                                    action.setRefBookId(refBookId);
                                    dispatcher.execute(action,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetRefBookAttributesResult>() {
                                                        @Override
                                                        public void onSuccess(GetRefBookAttributesResult result) {
                                                            /*getView().resetRefBookElements();
                                                            refBookLinearPresenter.setTableColumns(result.getColumns());*/
                                                            versionPresenter.setTableColumns(result.getColumns());
                                                            versionPresenter.setMode(mode);
                                                            //commonEditPresenter.init(refBookId, result.getColumns());
                                                            commonEditPresenter.setMode(mode);
                                                            /*hierEditFormPresenter.show(recordId);*/
                                                            versionPresenter.updateTable();
                                                        }
                                                    }, RefBookDataPresenter.this));

                                    GetNameAction nameAction = new GetNameAction();
                                    nameAction.setRefBookId(refBookId);
                                    nameAction.setUniqueRecordId(recordId);
                                    dispatcher.execute(nameAction,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetNameResult>() {
                                                        @Override
                                                        public void onSuccess(GetNameResult result) {
                                                            getView().setRefBookNameDesc(result.getUniqueAttributeValues(), getView().getRelevanceDate());
                                                            commonEditPresenter.setRecordId(result.getRecordId());
                                                        }
                                                    }, RefBookDataPresenter.this));
                                }
                            });
                        } else {
                            Dialog.errorMessage("Доступ к справочнику запрещен!");
                        }
                    }
                }, RefBookDataPresenter.this));
    }

    @Override
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);
        if (content == refBookLinearPresenter){
            dataInterface = new LinearRefBookExecutor(refBookLinearPresenter);
        } else if(content == versionPresenter){
            dataInterface = new LinearRefBookExecutor(versionPresenter);
        }
    }

    @Override
    public void onAddRowClicked() {
        getView().updateMode(FormMode.CREATE);
        commonEditPresenter.setMode(FormMode.CREATE);
        commonEditPresenter.clean();
        dataInterface.setMode(FormMode.CREATE);
    }

    @Override
    public void onDeleteRowClicked() {
        /*dataInterface.deleteRow();*/
        DeleteItemEvent.fire(this);
    }

    @Override
    public void onRelevanceDateChanged() {
        /*dataInterface.initState(getView().getRelevanceDate(), getView().getSearchPattern());*/
        SearchButtonEvent.fire(this, getView().getRelevanceDate(), getView().getSearchPattern());
    }

    @Override
    public void onBackClicked() {
        refBookId = null;
        recordId = null;
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(RefBookListTokens.REFBOOK_LIST).build());
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        refBookLinearPresenter.setRefBookId(refBookId);
        versionPresenter.setRefBookId(refBookId);
        commonEditPresenter = editFormPresenter;
        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookId);

        dispatcher.execute(checkAction,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<CheckRefBookResult>() {
                            @Override
                            public void onSuccess(CheckRefBookResult result) {
                                versioned = result.isVersioned();
                                commonEditPresenter.init(refBookId, result.isVersioned());
                                if (result.isAvailable()) {
                                    getView().resetSearchInputBox();
                                    commonEditPresenter.setVersionMode(false);
                                    commonEditPresenter.setCurrentUniqueRecordId(null);
                                    commonEditPresenter.setRecordId(null);
                                    GetRefBookAttributesAction action = new GetRefBookAttributesAction();

                                    action.setRefBookId(refBookId);
                                    dispatcher.execute(action,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetRefBookAttributesResult>() {
                                                        @Override
                                                        public void onSuccess(GetRefBookAttributesResult result) {
                                                            refBookLinearPresenter.setTableColumns(result.getColumns());
                                                            getView().updateSendQuery(result.isSendQuery());
                                                            commonEditPresenter.createFields(result.getColumns());
                                                            if (result.isReadOnly()) {
                                                                mode = FormMode.READ;
                                                            } else {
                                                                mode = FormMode.VIEW;
                                                            }
                                                            commonEditPresenter.setMode(mode);
                                                            getView().updateMode(mode);
                                                            refBookLinearPresenter.setMode(mode);
                                                            /*refBookLinearPresenter.setRange(new Range(0, 500));*/
                                                            refBookLinearPresenter.initState(getView().getRelevanceDate(), getView().getSearchPattern());
                                                            refBookLinearPresenter.updateTable();
                                                            //т.к. не срабатывает событие onSelectionChange приповторном переходе
                                                            commonEditPresenter.show(recordId);
                                                            getProxy().manualReveal(RefBookDataPresenter.this);
                                                        }
                                                    }, RefBookDataPresenter.this));

                                    GetNameAction nameAction = new GetNameAction();
                                    nameAction.setRefBookId(refBookId);
                                    dispatcher.execute(nameAction,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetNameResult>() {
                                                        @Override
                                                        public void onSuccess(GetNameResult result) {
                                                            refBookName = result.getName();
                                                            getView().setRefBookNameDesc(result.getName());
                                                        }
                                                    }, RefBookDataPresenter.this));
                                    getView().setVersionedFields(versioned);
                                    commonEditPresenter.setVersioned(versioned);
                                    versionPresenter.setHierarchy(false);
                                } else {
                                    getProxy().manualReveal(RefBookDataPresenter.this);
                                    Dialog.errorMessage("Доступ к справочнику запрещен!");
                                }
                            }
                        }, this));
    }

    @Override
    public void onBind(){
        super.onBind();
        addVisibleHandler(SetFormMode.getType(), this);
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
        for (HandlerRegistration han : registrations){
            han.removeHandler();
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        commonEditPresenter.setMode(mode);
        /*versionPresenter.setMode(mode);*/
        getView().updateMode(mode);
        dataInterface.setMode(mode);
    }

    @Override
    public void saveChanges() {
        commonEditPresenter.onSaveClicked(true);
    }

    @Override
    public void cancelChanges() {
        commonEditPresenter.setIsFormModified(false);
        commonEditPresenter.onCancelClicked();
    }

    @Override
    public boolean isFormModified() {
        return editFormPresenter.isFormModified();
    }

    @Override
    public void sendQuery() {
        addToPopupSlot(dialogPresenter);
    }

    @Override
    public void onSearchClick() {
        SearchButtonEvent.fire(this, getView().getRelevanceDate(), getView().getSearchPattern());
        /*dataInterface.initState(getView().getRelevanceDate(), getView().getSearchPattern());
        dataInterface.updateData();*/
    }

    @Override
    public void onBackToRefBookAnchorClicked() {
        clearSlot(TYPE_mainFormPresenter);
        setInSlot(TYPE_mainFormPresenter, refBookLinearPresenter);
        getView().setVersionView(false);
        getView().setRefBookNameDesc(refBookName);
        setMode(mode);
        commonEditPresenter.setVersionMode(false);
        ShowItemEvent.fire(RefBookDataPresenter.this, null, recordId);
    }

    @Override
    public void onReset(){
        this.dialogPresenter.getView().hide();
    }
}
