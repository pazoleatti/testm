package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.AbstractEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.CheckModifiedHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.OnTimerEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear.RefBookLinearPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload.UploadDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.event.BackEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
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
import java.util.List;
import java.util.Map;

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
        RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers,
        SetFormMode.SetFormModeHandler, BackEvent.BackHandler, OnTimerEvent.OnTimerHandler {

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

    private static final String NOT_EXIST_EVENT_MSG = "Не предусмотрена загрузка данных из файла в справочник ";

    private Long refBookId;

    private FormMode mode;
    private String refBookName;
    private IRefBookExecutor dataInterface;
    private boolean isVersioned;
    protected Map<FormDataEvent, Boolean> eventScriptStatus;

    private String lockId;

    private boolean timerEnabled;
    private Timer timer;

    AbstractEditPresenter editFormPresenter;
    RefBookVersionPresenter versionPresenter;
    DialogPresenter dialogPresenter;
    RefBookLinearPresenter refBookLinearPresenter;
    UploadDialogPresenter uploadDialogPresenter;

    private final HandlerRegistration[] registrations = new HandlerRegistration[2];

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;

    public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
        void setRefBookNameDesc(String desc);
        Date getRelevanceDate();
        /** Метод для получения строки с поля фильтрации*/
        String getSearchPattern();
        /** Метод для получения признака точного соответствия*/
        Boolean getExactSearch();
        /** Сброс значения поля поиска */
        void resetSearchInputBox();
        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        /** доступность  кнопки-ссылка "Создать запрос на изменение..." для справочника "Организации-участники контролируемых сделок" */
        void updateSendQuery(boolean isAvailable);

        /**
         * Устанавливает вид справочника когда мы переходим на список версий.
         * @param isVersion true - если переходим в версионное представление
         */
        void setVersionView(boolean isVersion);

        /**
         * Устанавливает вид справочника версионируемый вид справочника.
         * @param isVersioned true - версионируемый
         */
        void setIsVersion(boolean isVersioned);

        void setSpecificReportTypes(List<String> specificReportTypes);

        void setUploadAvailable(boolean uploadAvailable);

        void setLockInformation(String title);
    }

    @Inject
    public RefBookDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter,
                                RefBookVersionPresenter versionPresenter, DialogPresenter dialogPresenter,
                                RefBookLinearPresenter refBookLinearPresenter, UploadDialogPresenter uploadDialogPresenter,
                                PlaceManager placeManager, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        this.editFormPresenter = editFormPresenter;
        this.versionPresenter = versionPresenter;
        this.dialogPresenter = dialogPresenter;
        this.refBookLinearPresenter = refBookLinearPresenter;
        this.uploadDialogPresenter = uploadDialogPresenter;
        getView().setUiHandlers(this);
        this.timer = new Timer() {
            @Override
            public void run() {
                onTimer(true);
            }
        };
    }

    @Override
    protected void onHide() {
        super.onHide();
        refBookId = null;
        refBookLinearPresenter.reset();
        editFormPresenter.setIsFormModified(false);
        editFormPresenter.setMode(FormMode.READ);
        editFormPresenter.show(null);
        clearSlot(TYPE_editFormPresenter);
        clearSlot(TYPE_mainFormPresenter);
        for (HandlerRegistration han : registrations){
            if (han != null)
                han.removeHandler();
        }
        stopTimer();
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
        editFormPresenter.clean(versionPresenter.isVisible(), eventScriptStatus.get(FormDataEvent.ADD_ROW));
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
        SearchButtonEvent.fire(this, getView().getRelevanceDate(), getView().getSearchPattern(), getView().getExactSearch());
    }

    @Override
    public void onBackClicked() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(RefBookListTokens.REFBOOK_LIST).build());
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        final Long prevRefBookId = refBookId;
        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        refBookLinearPresenter.setRefBookId(refBookId);
        versionPresenter.setRefBookId(refBookId);
        versionPresenter.setHierarchy(false);
        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookId);
        checkAction.setTypeForCheck(RefBookType.LINEAR);
        dispatcher.execute(checkAction,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<CheckRefBookResult>() {
                            @Override
                            public void onSuccess(CheckRefBookResult result) {
                                editFormPresenter.init(refBookId, result.isVersioned());
                                isVersioned = result.isVersioned();
                                getView().setIsVersion(result.isVersioned());
                                getView().setUploadAvailable(result.isUploadAvailable());
                                eventScriptStatus = result.getEventScriptStatus();
                                registrations[0] = editFormPresenter.addClickHandlerForAllVersions(getClick());
                                if (result.isAvailable()) {
                                    getView().resetSearchInputBox();
                                    editFormPresenter.setVersionMode(false);
                                    getView().setVersionView(false);
                            /*editFormPresenter.setRecordId(null);*/
                                    GetRefBookAttributesAction action = new GetRefBookAttributesAction();

                                    action.setRefBookId(refBookId);
                                    dispatcher.execute(action,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetRefBookAttributesResult>() {
                                                        @Override
                                                        public void onSuccess(GetRefBookAttributesResult result) {
                                                            refBookName = result.getRefBookName();
                                                            getView().setRefBookNameDesc(result.getRefBookName());
                                                            getView().setSpecificReportTypes(result.getSpecificReportTypes());
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
                                                            refBookLinearPresenter.initState(getView().getRelevanceDate(), getView().getSearchPattern(), getView().getExactSearch());
                                                            refBookLinearPresenter.updateTable();
                                                            //т.к. не срабатывает событие onSelectionChange приповторном переходе
//                                                            editFormPresenter.show(recordId);
                                                            getProxy().manualReveal(RefBookDataPresenter.this);
                                                            startTimer();
                                                        }
                                                    }, RefBookDataPresenter.this));
                                } else {
                                    placeManager.unlock();
                                    Dialog.errorMessage("Доступ к справочнику запрещен!", new DialogHandler() {
                                        @Override
                                        public void close() {
                                            super.close();
                                            if (prevRefBookId != null) {
                                                PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(RefBookDataTokens.REFBOOK_DATA);
                                                builder.with(RefBookDataTokens.REFBOOK_DATA_ID, String.valueOf(prevRefBookId));
                                                placeManager.revealPlace(builder.build());
                                            } else {
                                                placeManager.revealPlace(new PlaceRequest(HomeNameTokens.homePage));
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                placeManager.unlock();
                                placeManager.revealErrorPlace("");
                            }
                        }, RefBookDataPresenter.this));
    }

    @Override
    public void onBind(){
        super.onBind();
        addVisibleHandler(SetFormMode.getType(), this);
        addVisibleHandler(BackEvent.getType(), this);
        addVisibleHandler(OnTimerEvent.getType(), this);
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
        SearchButtonEvent.fire(this, getView().getRelevanceDate(), getView().getSearchPattern(), getView().getExactSearch());
        /*dataInterface.initState(getView().getRelevanceDate(), getView().getSearchPattern());
        dataInterface.updateData();*/
    }

    @Override
    public void onBackToRefBookAnchorClicked() {
        if (editFormPresenter.isFormModified()) {
            Dialog.confirmMessage(AbstractEditPresenter.DIALOG_MESSAGE, new DialogHandler() {
                @Override
                public void yes() {
                    editFormPresenter.setIsFormModified(false);
                    backToRefBook();
                }

                @Override
                public void no() {
                    Dialog.hideMessage();
                }

                @Override
                public void cancel() {
                    no();
                }

                @Override
                public void close() {
                    no();
                }
            });
        } else {
            backToRefBook();
        }
    }

    private void backToRefBook() {
        clearSlot(TYPE_mainFormPresenter);
        setInSlot(TYPE_mainFormPresenter, refBookLinearPresenter);
        getView().setVersionView(false);
        getView().setRefBookNameDesc(refBookName);
        setMode(mode);
        editFormPresenter.setVersionMode(false);
        editFormPresenter.setRecordId(null);
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

    @Override
    public void onPrintClicked(String reportName) {
        CreateReportAction action = new CreateReportAction();
        action.setReportName(reportName);
        action.setRefBookId(refBookId);
        action.setVersion(refBookLinearPresenter.getRelevanceDate());
        action.setSearchPattern(refBookLinearPresenter.getSearchPattern());
        action.setExactSearch(refBookLinearPresenter.getExactSearch());
        action.setSortColumnIndex(refBookLinearPresenter.getSortColumnIndex());
        action.setAscSorting(refBookLinearPresenter.isAscSorting());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<CreateReportResult>() {
                            @Override
                            public void onSuccess(CreateReportResult result) {
                                LogAddEvent.fire(RefBookDataPresenter.this, result.getUuid());
                                if (result.getErrorMsg() != null && !result.getErrorMsg().isEmpty())
                                    Dialog.errorMessage("Ошибка", result.getErrorMsg());
                            }
                        }, RefBookDataPresenter.this));
    }

    @Override
    public void showUploadDialogClicked() {
        if (eventScriptStatus.get(FormDataEvent.IMPORT)) {
            if (!editFormPresenter.isFormModified()) {
                uploadDialogPresenter.open(refBookId, isVersioned);
            } else {
                editFormPresenter.checkModified(new CheckModifiedHandler() {
                    @Override
                    public void openLoadDialog() {
                        uploadDialogPresenter.open(refBookId, isVersioned);
                    }

                    @Override
                    public String getTitle() {
                        return "Подтверждение изменений";
                    }

                    @Override
                    public String getText() {
                        return "Выбранная запись была изменена. Сохранить изменения и загрузить файл? \"Да\" - загрузить с сохранением. \"Нет\" - загрузить без сохранения.";
                    }
                });
            }
        } else {
            Dialog.infoMessage(NOT_EXIST_EVENT_MSG + "\"" + refBookName + "\"");
        }
    }

    @Override
    public void editClicked() {
        EditRefBookAction action = new EditRefBookAction();
        action.setRefBookId(refBookId);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<EditRefBookResult>() {
                            @Override
                            public void onSuccess(EditRefBookResult result) {
                                LogAddEvent.fire(RefBookDataPresenter.this, result.getUuid());
                                if (result.isLock()) {
                                    Dialog.errorMessage("Ошибка", result.getLockMsg());
                                } else {
                                    setMode(FormMode.EDIT);
                                }
                            }
                        }, RefBookDataPresenter.this));
    }


    protected void startTimer() {
        timerEnabled = true;
        timer.scheduleRepeating(5000);
    }

    protected void stopTimer() {
        timerEnabled = false;
        timer.cancel();
    }

    @Override
    public void onTimer(OnTimerEvent event) {
        onTimer(false);
    }

    private void onTimer(final boolean isTimer) {
        TimerAction action = new TimerAction();
        action.setRefBookId(refBookId);
        dispatcher.execute(
                action,
                CallbackUtils.simpleCallback(
                        new AbstractCallback<TimerResult>() {
                            @Override
                            public void onSuccess(TimerResult result) {
                                if (result.getLockId() != lockId || !isTimer) {
                                    lockId = result.getLockId();
                                    if (result.isLock()) {
                                        getView().setLockInformation(result.getText());
                                        editFormPresenter.setLock(true);
                                    } else {
                                        getView().setLockInformation(null);
                                        editFormPresenter.setLock(false);
                                    }
                                }
                            }
                        }));
    }
}
