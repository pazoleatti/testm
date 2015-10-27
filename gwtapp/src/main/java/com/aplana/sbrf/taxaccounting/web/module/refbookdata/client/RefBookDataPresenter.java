package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.AbstractEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear.RefBookLinearPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.event.BackEvent;
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
        RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers,
        SetFormMode.SetFormModeHandler, BackEvent.BackHandler {

    @Override
    public void onSetFormMode(SetFormMode event) {
        setMode(event.getFormMode());
        dataInterface.setMode(event.getFormMode());
    }

    @Override
    public void onBack(BackEvent event) {
        onBackToRefBookAnchorClicked();
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
    @NameToken(RefBookDataTokens.REFBOOK_DATA)
    public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
    }

    public static final Object TYPE_editFormPresenter = new Object();
    public static final Object TYPE_mainFormPresenter = new Object();

    private Long refBookId;

    private FormMode mode;
    private String refBookName;
    private IRefBookExecutor dataInterface;

    AbstractEditPresenter editFormPresenter;
    RefBookVersionPresenter versionPresenter;
    DialogPresenter dialogPresenter;
    RefBookLinearPresenter refBookLinearPresenter;

    private final HandlerRegistration[] registrations = new HandlerRegistration[2];

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;

    public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
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
        this.placeManager = placeManager;
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
            if (han != null)
                han.removeHandler();
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        LogCleanEvent.fire(this);
        setInSlot(TYPE_editFormPresenter, editFormPresenter);
        setInSlot(TYPE_mainFormPresenter, refBookLinearPresenter);
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
        editFormPresenter.setMode(FormMode.CREATE);
        editFormPresenter.clean(versionPresenter.isVisible());
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
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(RefBookListTokens.REFBOOK_LIST).build());
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        refBookLinearPresenter.setRefBookId(refBookId);
        versionPresenter.setRefBookId(refBookId);
        versionPresenter.setHierarchy(false);
        CheckLinearAction linearAction = new CheckLinearAction();
        linearAction.setRefBookId(refBookId);
        dispatcher.execute(linearAction, CallbackUtils.defaultCallback(new AbstractCallback<CheckLinearResult>() {
            @Override
            public void onSuccess(CheckLinearResult result) {
                CheckRefBookAction checkAction = new CheckRefBookAction();
                checkAction.setRefBookId(refBookId);

                dispatcher.execute(checkAction,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<CheckRefBookResult>() {
                                    @Override
                                    public void onSuccess(CheckRefBookResult result) {
                                        editFormPresenter.init(refBookId, result.isVersioned());
                                        registrations[0] = editFormPresenter.addClickHandlerForAllVersions(getClick());
                                        if (result.isAvailable()) {
                                            getView().resetSearchInputBox();
                                            editFormPresenter.setVersionMode(false);
                                    /*editFormPresenter.setRecordId(null);*/
                                            GetRefBookAttributesAction action = new GetRefBookAttributesAction();

                                            action.setRefBookId(refBookId);
                                            dispatcher.execute(action,
                                                    CallbackUtils.defaultCallback(
                                                            new AbstractCallback<GetRefBookAttributesResult>() {
                                                                @Override
                                                                public void onSuccess(GetRefBookAttributesResult result) {
                                                                    refBookLinearPresenter.setTableColumns(result.getColumns());
                                                                    getView().updateSendQuery(result.isSendQuery());
                                                                    editFormPresenter.createFields(result.getColumns());
                                                                    if (result.isReadOnly()) {
                                                                        mode = FormMode.READ;
                                                                    } else {
                                                                        mode = FormMode.VIEW;
                                                                    }
                                                                    editFormPresenter.setMode(mode);
                                                                    getView().updateMode(mode);
                                                                    refBookLinearPresenter.setMode(mode);
                                                            /*refBookLinearPresenter.setRange(new Range(0, 500));*/
                                                                    refBookLinearPresenter.initState(getView().getRelevanceDate(), getView().getSearchPattern());
                                                                    refBookLinearPresenter.updateTable();
                                                                    //т.к. не срабатывает событие onSelectionChange приповторном переходе
//                                                            editFormPresenter.show(recordId);
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
                                        } else {
                                            getProxy().manualReveal(RefBookDataPresenter.this);
                                            Dialog.errorMessage("Доступ к справочнику запрещен!");
                                        }
                                    }
                                }, RefBookDataPresenter.this));
            }

            @Override
            public void onFailure(Throwable caught) {
                placeManager.unlock();
                placeManager.revealErrorPlace("");
            }
        }, this));

    }

    @Override
    public void onBind(){
        super.onBind();
        addVisibleHandler(SetFormMode.getType(), this);
        addVisibleHandler(BackEvent.getType(), this);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        editFormPresenter.setMode(mode);
        /*versionPresenter.setMode(mode);*/
        getView().updateMode(mode);
        dataInterface.setMode(mode);
    }

    @Override
    public void saveChanges() {
        editFormPresenter.onSaveClicked(true);
    }

    @Override
    public void cancelChanges() {
        editFormPresenter.setIsFormModified(false);
        editFormPresenter.onCancelClicked();
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
        editFormPresenter.setVersionMode(false);
        //ShowItemEvent.fire(RefBookDataPresenter.this, null, versionPresenter.getSelectedRow().getRefBookRowId());
        refBookLinearPresenter.updateTable();
        /*registrations[1].removeHandler();*/
    }

    @Override
    public void onReset(){
        this.dialogPresenter.getView().hide();
    }

    /**
     * Событие нажатия на ссылку "Все версии элемента"
     * Событие удаляется как только происходит нажатие на кнопку и добавляется обратно
     * при переходе на страницу справоника
     */
    private ClickHandler getClick(){
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().setVersionView(true);
                clearSlot(TYPE_mainFormPresenter);
                setInSlot(TYPE_mainFormPresenter, versionPresenter);
                versionPresenter.setUniqueRecordId(refBookLinearPresenter.getSelectedRow().getRefBookRowId());

                editFormPresenter.setVersionMode(true);
                /*editFormPresenter.setRecordId(null);*/

                GetRefBookAttributesAction action = new GetRefBookAttributesAction();
                action.setRefBookId(refBookId);
                getView().setRefBookNameDesc("Все версии записи");
                dispatcher.execute(action,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<GetRefBookAttributesResult>() {
                                    @Override
                                    public void onSuccess(GetRefBookAttributesResult result) {
                                                            /*getView().resetRefBookElements();
                                                            refBookLinearPresenter.setTableColumns(result.getColumns());*/
                                        versionPresenter.setTableColumns(result.getColumns());
                                        versionPresenter.setMode(mode);
                                        //editFormPresenter.init(refBookId, result.getColumns());
                                        editFormPresenter.setMode(mode);

                                        //Получение группы для версий, реального record_id
                                        GetRefBookRecordIdAction recordIdAction = new GetRefBookRecordIdAction();
                                        recordIdAction.setRefBookId(refBookId);
                                        recordIdAction.setUniqueRecordId(refBookLinearPresenter.getSelectedRow().getRefBookRowId());
                                        dispatcher.execute(recordIdAction, CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookRecordIdResult>() {
                                            @Override
                                            public void onSuccess(GetRefBookRecordIdResult result) {
                                                editFormPresenter.setRecordId(result.getRecordId());
                                                versionPresenter.setRecordId(result.getRecordId());
                                                versionPresenter.updateTable();
                                            }
                                        }, RefBookDataPresenter.this));
                                    }
                                }, RefBookDataPresenter.this));

                //Убираем изменение наименования в связи с изменениями в постановке
                //http://jira.aplana.com/browse/SBRFACCTAX-12015?focusedCommentId=125046&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-125046
                /*GetNameAction nameAction = new GetNameAction();
                nameAction.setRefBookId(refBookId);
                nameAction.setUniqueRecordId(recordId);
                dispatcher.execute(nameAction,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<GetNameResult>() {
                                    @Override
                                    public void onSuccess(GetNameResult result) {
                                        getView().setRefBookNameDesc(result.getUniqueAttributeValues(), getView().getRelevanceDate());
                                        editFormPresenter.setRecordId(result.getRecordId());
                                    }
                                }, RefBookDataPresenter.this));*/

                //Изменение заголовка формы при изменение неких атрибутов
                /*registrations[1] = editFormPresenter.addUpdateFormHandler(new UpdateForm.UpdateFormHandler() {
                    @Override
                    public void onUpdateForm(UpdateForm event) {
                        GetNameAction nameAction = new GetNameAction();
                        nameAction.setRefBookId(refBookId);
                        nameAction.setUniqueRecordId(recordId);
                        dispatcher.execute(nameAction,
                                CallbackUtils.defaultCallback(
                                        new AbstractCallback<GetNameResult>() {
                                            @Override
                                            public void onSuccess(GetNameResult result) {
                                                getView().setRefBookNameDesc(result.getUniqueAttributeValues(), getView().getRelevanceDate());
                                            }
                                        }, RefBookDataPresenter.this));
                    }
                });*/
            }
        };
    }
}
