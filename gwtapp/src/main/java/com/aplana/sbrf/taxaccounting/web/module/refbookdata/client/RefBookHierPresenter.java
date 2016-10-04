package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.AbstractEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.CheckModifiedHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.DepartmentEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.HierEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.AddItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.OnTimerEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload.UploadDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.event.BackEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
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

/**
 * User: avanteev
 */
public class RefBookHierPresenter extends Presenter<RefBookHierPresenter.MyView, RefBookHierPresenter.MyProxy>
        implements RefBookHierUIHandlers, SetFormMode.SetFormModeHandler, BackEvent.BackHandler, OnTimerEvent.OnTimerHandler {

    private final DispatchAsync dispatcher;
    private PlaceManager placeManager;
    HierEditPresenter hierEditFormPresenter;
    DepartmentEditPresenter departmentEditPresenter;
    AbstractEditPresenter commonEditPresenter;
    RefBookVersionPresenter versionPresenter;
    RefBookHierDataPresenter refBookHierDataPresenter;
    UploadDialogPresenter uploadDialogPresenter;

    public static final Object TYPE_editFormPresenter = new Object();
    public static final Object TYPE_mainFormPresenter = new Object();

    private static final String NOT_EXIST_EVENT_MSG = "Не предусмотрена загрузка данных из файла в справочник ";

    private final HandlerRegistration[] registrations = new HandlerRegistration[2];
    private IRefBookExecutor dataInterface;
    private FormMode mode;
    private Long attrId, uniqueRecordId, refBookId;
    private String refBookName;
    private boolean importScriptStatus;
    private boolean isVersioned;

    private String lockId;

    private boolean timerEnabled;
    private Timer timer;

    @Inject
    public RefBookHierPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                DispatchAsync dispatcher, PlaceManager placeManager,
                                HierEditPresenter editFormPresenter, RefBookHierDataPresenter refBookHierDataPresenter,
                                RefBookVersionPresenter versionPresenter, DepartmentEditPresenter departmentEditPresenter,
                                UploadDialogPresenter uploadDialogPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        this.hierEditFormPresenter = editFormPresenter;
        this.refBookHierDataPresenter = refBookHierDataPresenter;
        this.versionPresenter = versionPresenter;
        this.departmentEditPresenter = departmentEditPresenter;
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
    public void onRelevanceDateChanged(Date relevanceDate) {
        commonEditPresenter.clean(false);
        commonEditPresenter.setNeedToReload();
        SearchButtonEvent.fire(this, relevanceDate, getView().getSearchPattern(), getView().getExactSearch());
    }

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        commonEditPresenter.setMode(mode);
        getView().updateView(mode);
        dataInterface.setMode(mode);
    }

    @Override
    public void cancelChanges() {
        commonEditPresenter.setIsFormModified(false);
        commonEditPresenter.onCancelClicked();
    }

    @Override
    public boolean isFormModified() {
        return commonEditPresenter.isFormModified();
    }

    @Override
    public void saveChanges() {
        commonEditPresenter.onSaveClicked(true);
    }

    @Override
    public void onDeleteRowClicked() {
        DeleteItemEvent.fire(this);
    }

    @Override
    public void searchButtonClicked() {
        SearchButtonEvent.fire(this, getView().getRelevanceDate(), getView().getSearchPattern(), getView().getExactSearch());
    }

    @Override
    public void onAddRowClicked() {
        /*refBookHierDataPresenter.onAddRowClicked();*/
        getView().updateView(FormMode.CREATE);
        commonEditPresenter.setMode(FormMode.CREATE);
        dataInterface.setMode(FormMode.CREATE);
        if (versionPresenter.isVisible()) {
            commonEditPresenter.clean(true);
        } else {
            commonEditPresenter.clean(false);
            AddItemEvent.fire(RefBookHierPresenter.this);
        }
    }

    @Override
    public void onBackToRefBookAnchorClicked() {
        if (commonEditPresenter.isFormModified()) {
            Dialog.confirmMessage(AbstractEditPresenter.DIALOG_MESSAGE, new DialogHandler() {
                @Override
                public void yes() {
                    commonEditPresenter.setIsFormModified(false);
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
        setInSlot(TYPE_mainFormPresenter, refBookHierDataPresenter);
        getView().setVersionView(false);
        getView().setRefBookNameDesc(refBookName);
        setMode(mode);
        commonEditPresenter.setVersionMode(false);
        commonEditPresenter.show(uniqueRecordId);
        //registrations[1].removeHandler();
    }

    @Override
    public void onBackClicked() {
        hierEditFormPresenter.setPreviousURId(null);
        departmentEditPresenter.setPreviousURId(null);
        uniqueRecordId = null;
        commonEditPresenter.show(uniqueRecordId);
    }

    @Override
    public void onSetFormMode(SetFormMode event) {
        dataInterface.setMode(event.getFormMode());
        getView().updateView(event.getFormMode());
    }

    @Override
    public void onBack(BackEvent event) {
        refBookHierDataPresenter.onDeleteItem(new DeleteItemEvent());
        onBackToRefBookAnchorClicked();
    }

    /*@Override
    public void onUpdateForm(UpdateForm event) {
        if (event.isSuccess()) {
            getView().clearFilterInputBox();
        }
    }*/

    @ProxyCodeSplit
    @NameToken(RefBookDataTokens.REFBOOK_HIER_DATA)
    public interface MyProxy extends ProxyPlace<RefBookHierPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<RefBookHierUIHandlers> {
        void clearFilterInputBox();
        Date getRelevanceDate();
        void updateView(FormMode mode);
        String getSearchPattern();
        Boolean getExactSearch();
        //Показывает/скрывает поля, которые необходимы только для версионирования
        void setVersionedFields(boolean isVisible);
        void setRefBookNameDesc(String desc);

        /**
         * Устанавливает версионный вид справочника.
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

    @Override
    protected void onBind() {
        super.onBind();
        addVisibleHandler(SetFormMode.getType(), this);
        addVisibleHandler(BackEvent.getType(), this);
        addVisibleHandler(OnTimerEvent.getType(), this);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_editFormPresenter);
        clearSlot(TYPE_mainFormPresenter);
        for (HandlerRegistration han : registrations) {
            if (han != null)
                han.removeHandler();
        }
        stopTimer();
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        uniqueRecordId = null;
        //Очистка слота в случае перехода прямо по ссылке, без перерисовки
        clearSlot(TYPE_editFormPresenter);
        setInSlot(TYPE_editFormPresenter, Department.REF_BOOK_ID.equals(refBookId) ? departmentEditPresenter : hierEditFormPresenter);
        commonEditPresenter = Department.REF_BOOK_ID.equals(refBookId) ? departmentEditPresenter : hierEditFormPresenter;
        refBookHierDataPresenter.setRefBookId(refBookId);
        versionPresenter.setRefBookId(refBookId);
        getView().clearFilterInputBox();
        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookId);
        checkAction.setTypeForCheck(RefBookType.HIERARCHICAL);
        dispatcher.execute(checkAction, CallbackUtils.defaultCallback(
                new AbstractCallback<CheckRefBookResult>() {
                    @Override
                    public void onSuccess(CheckRefBookResult result) {
                        commonEditPresenter.init(refBookId, result.isVersioned());
                        importScriptStatus = result.isScriptStatus();
                        isVersioned = result.isVersioned();
                        getView().setIsVersion(result.isVersioned());
                        getView().setUploadAvailable(result.isUploadAvailable());
                        registrations[0] = commonEditPresenter.addClickHandlerForAllVersions(getClick());
                        if (result.isAvailable()) {
                            commonEditPresenter.setVersionMode(false);
                            getView().setVersionView(false);
                            /*commonEditPresenter.setRecordId(null);*/

                            GetRefBookAttributesAction action = new GetRefBookAttributesAction(refBookId);
                            dispatcher.execute(action, CallbackUtils.defaultCallback(
                                    new AbstractCallback<GetRefBookAttributesResult>() {
                                        @Override
                                        public void onSuccess(GetRefBookAttributesResult result) {
                                            refBookName = result.getRefBookName();
                                            getView().setRefBookNameDesc(refBookName);
                                            getView().setSpecificReportTypes(result.getSpecificReportTypes());
                                            /*if (canVersion)checkRecord();*/
                                            for (RefBookColumn refBookColumn : result.getColumns()) {
                                                if (refBookColumn.getAlias().toLowerCase().equals("name")) {
                                                    attrId = refBookColumn.getId();
                                                }
                                            }
                                            /*refBookHierDataPresenter.initPickerState(attrId);
                                            refBookHierDataPresenter.initPickerState(getView().getRelevanceDate(), getView().getSearchPattern());*/
                                            if (result.isReadOnly()) {
                                                mode = FormMode.READ;
                                                //updateMode();
                                            } else {
                                                mode = FormMode.VIEW;
                                            }
                                            getView().updateView(mode);
                                            refBookHierDataPresenter.clearAll();
                                            refBookHierDataPresenter.setAttributeId(attrId);
                                            refBookHierDataPresenter.setMode(mode);
                                            refBookHierDataPresenter.initPickerState(getView().getRelevanceDate(), getView().getSearchPattern(), getView().getExactSearch());
                                            refBookHierDataPresenter.loadAndSelect();
                                            commonEditPresenter.createFields(result.getColumns());
                                            commonEditPresenter.setMode(mode);
                                        }
                                    }, RefBookHierPresenter.this));

                            getView().setVersionedFields(result.isVersioned());
                            //hierEditFormPresenter.setCanVersion(canVersion);
                            versionPresenter.setHierarchy(true);
                        } else {
                            /*getProxy().manualReveal(RefBookHierPresenter.this);*/
                            Dialog.errorMessage("Доступ к справочнику запрещен!");
                        }
                        startTimer();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        placeManager.unlock();
                        placeManager.revealErrorPlace("");
                        stopTimer();
                    }
                }, RefBookHierPresenter.this));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        LogCleanEvent.fire(this);
        setInSlot(TYPE_editFormPresenter, Department.REF_BOOK_ID.equals(refBookId) ? departmentEditPresenter : hierEditFormPresenter);
        setInSlot(TYPE_mainFormPresenter, refBookHierDataPresenter);
    }

    @Override
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);
        if (slot.equals(TYPE_mainFormPresenter)) {
            if (content == refBookHierDataPresenter) {
                dataInterface = new HierRefBookExecutor(refBookHierDataPresenter);
            } else if (content == versionPresenter) {
                dataInterface = new LinearRefBookExecutor(versionPresenter);
            }
        }
    }

    private ClickHandler getClick(){
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setInSlot(TYPE_mainFormPresenter, versionPresenter);
                versionPresenter.setUniqueRecordId(refBookHierDataPresenter.getSelectedId());

                /*refBookLinearPresenter.changeProvider(true);*/
                commonEditPresenter.setVersionMode(true);
                /*commonEditPresenter.setRecordId(null);*/
                uniqueRecordId = refBookHierDataPresenter.getSelectedId();
                dataInterface.setMode(mode);

                GetRefBookAttributesAction action = new GetRefBookAttributesAction();
                action.setRefBookId(refBookId);
                dispatcher.execute(action,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<GetRefBookAttributesResult>() {
                                    @Override
                                    public void onSuccess(GetRefBookAttributesResult result) {
                                                            /*getView().resetRefBookElements();
                                                            refBookLinearPresenter.setTableColumns(result.getColumns());*/
                                        getView().setVersionView(true);
                                        versionPresenter.setTableColumns(result.getColumns());
                                        commonEditPresenter.setMode(mode);

                                        //hierEditFormPresenter.init(refBookId);
                                                            /*getProxy().manualReveal(RefBookDataPresenter.this);*/
                                        GetRefBookRecordIdAction recordIdAction = new GetRefBookRecordIdAction();
                                        recordIdAction.setRefBookId(refBookId);
                                        recordIdAction.setUniqueRecordId(refBookHierDataPresenter.getSelectedId());
                                        dispatcher.execute(recordIdAction, CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookRecordIdResult>() {
                                            @Override
                                            public void onSuccess(GetRefBookRecordIdResult result) {
                                                commonEditPresenter.setRecordId(result.getRecordId());
                                                versionPresenter.setRecordId(result.getRecordId());
                                                versionPresenter.updateTable();
                                            }
                                        }, RefBookHierPresenter.this));
                                    }
                                }, RefBookHierPresenter.this));
            }
        };
    }

    /*private void checkRecord() {
        CheckRecordExistenceAction action = new CheckRecordExistenceAction();
        action.setRefBookId(refBookId);
        action.setRecordId(uniqueRecordId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<CheckRecordExistenceResult>() {
                    @Override
                    public void onSuccess(CheckRecordExistenceResult result) {
                        if (result.isRecordExistence()) {
                            uniqueRecordId = null;
                            refBookHierDataPresenter.reload();
                            getView().loadAndSelect();
                        } else {
                            refBookHierDataPresenter.reload();
                            refBookHierDataPresenter.setSelected(uniqueRecordId);
                            hierEditFormPresenter.show(uniqueRecordId);
                        }
                    }
                }, this));
    }*/


    @Override
    public void onPrintClicked(String reportName) {
        CreateReportAction action = new CreateReportAction();
        action.setReportName(reportName);
        action.setRefBookId(refBookId);
        action.setVersion(refBookHierDataPresenter.getRelevanceDate());
        action.setSearchPattern(refBookHierDataPresenter.getSearchPattern());
        action.setExactSearch(refBookHierDataPresenter.isExactSearch());
        action.setAscSorting(true);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<CreateReportResult>() {
                            @Override
                            public void onSuccess(CreateReportResult result) {
                                LogAddEvent.fire(RefBookHierPresenter.this, result.getUuid());
                                if (result.getErrorMsg() != null && !result.getErrorMsg().isEmpty())
                                    Dialog.errorMessage("Ошибка", result.getErrorMsg());
                            }
                        }, RefBookHierPresenter.this));
    }

    @Override
    public void showUploadDialogClicked() {
        if (importScriptStatus) {
            if (!commonEditPresenter.isFormModified()) {
                uploadDialogPresenter.open(refBookId, isVersioned);
            } else {
                commonEditPresenter.checkModified(new CheckModifiedHandler() {
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
                                LogAddEvent.fire(RefBookHierPresenter.this, result.getUuid());
                                if (result.isLock()) {
                                    Dialog.errorMessage("Ошибка", result.getLockMsg());
                                } else {
                                    setMode(FormMode.EDIT);
                                }
                            }
                        }, RefBookHierPresenter.this));

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
                                        commonEditPresenter.setLock(true);
                                    } else {
                                        getView().setLockInformation(null);
                                        commonEditPresenter.setLock(false);
                                    }
                                }
                            }
                        }));
    }
}
