package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.AbstractEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.DepartmentEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.HierEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.AddItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.event.BackEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
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
import java.util.List;

/**
 * User: avanteev
 */
public class RefBookHierPresenter extends Presenter<RefBookHierPresenter.MyView, RefBookHierPresenter.MyProxy>
        implements RefBookHierUIHandlers, SetFormMode.SetFormModeHandler, BackEvent.BackHandler {

    private final DispatchAsync dispatcher;
    private PlaceManager placeManager;
    HierEditPresenter hierEditFormPresenter;
    DepartmentEditPresenter departmentEditPresenter;
    AbstractEditPresenter commonEditPresenter;
    RefBookVersionPresenter versionPresenter;
    RefBookHierDataPresenter refBookHierDataPresenter;

    public static final Object TYPE_editFormPresenter = new Object();
    public static final Object TYPE_mainFormPresenter = new Object();

    private final HandlerRegistration[] registrations = new HandlerRegistration[2];
    private IRefBookExecutor dataInterface;
    private FormMode mode;
    private Long attrId, uniqueRecordId, refBookId;
    private String refBookName;

    @Inject
    public RefBookHierPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                DispatchAsync dispatcher, PlaceManager placeManager,
                                HierEditPresenter editFormPresenter, RefBookHierDataPresenter refBookHierDataPresenter,
                                RefBookVersionPresenter versionPresenter, DepartmentEditPresenter departmentEditPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        this.hierEditFormPresenter = editFormPresenter;
        this.refBookHierDataPresenter = refBookHierDataPresenter;
        this.versionPresenter = versionPresenter;
        this.departmentEditPresenter = departmentEditPresenter;
        getView().setUiHandlers(this);
    }

    @Override
    public void onRelevanceDateChanged(Date relevanceDate) {
        commonEditPresenter.clean(false);
        commonEditPresenter.setNeedToReload();
        SearchButtonEvent.fire(this, relevanceDate, getView().getSearchPattern());
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
        SearchButtonEvent.fire(this, getView().getRelevanceDate(), getView().getSearchPattern());
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
    }

    @Override
    protected void onBind() {
        super.onBind();
        addVisibleHandler(SetFormMode.getType(), this);
        addVisibleHandler(BackEvent.getType(), this);
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
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        uniqueRecordId = null;
        //Очистка слота в случае перехода прямо по ссылке, без перерисовки
        clearSlot(TYPE_editFormPresenter);
        setInSlot(TYPE_editFormPresenter, Department.REF_BOOK_ID.equals(refBookId) ? departmentEditPresenter : hierEditFormPresenter);
        commonEditPresenter = Department.REF_BOOK_ID.equals(refBookId) ? departmentEditPresenter : hierEditFormPresenter;
        CheckHierAction checkHierAction = new CheckHierAction();
        checkHierAction.setRefBookId(refBookId);
        refBookHierDataPresenter.setRefBookId(refBookId);
        versionPresenter.setRefBookId(refBookId);
        getView().clearFilterInputBox();
        dispatcher.execute(checkHierAction, CallbackUtils.defaultCallback(new AbstractCallback<CheckHierResult>() {
            @Override
            public void onSuccess(CheckHierResult result) {
                CheckRefBookAction checkAction = new CheckRefBookAction();
                checkAction.setRefBookId(refBookId);
                dispatcher.execute(checkAction, CallbackUtils.defaultCallback(
                        new AbstractCallback<CheckRefBookResult>() {
                            @Override
                            public void onSuccess(CheckRefBookResult result) {
                                commonEditPresenter.init(refBookId, result.isVersioned());
                                getView().setIsVersion(result.isVersioned());
                                registrations[0] = commonEditPresenter.addClickHandlerForAllVersions(getClick());
                                if (result.isAvailable()) {
                                    commonEditPresenter.setVersionMode(false);
                                    /*commonEditPresenter.setRecordId(null);*/

                                    GetRefBookAttributesAction action = new GetRefBookAttributesAction(refBookId);
                                    dispatcher.execute(action, CallbackUtils.defaultCallback(
                                            new AbstractCallback<GetRefBookAttributesResult>() {
                                                @Override
                                                public void onSuccess(GetRefBookAttributesResult result) {
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
                                                    refBookHierDataPresenter.initPickerState(getView().getRelevanceDate(), getView().getSearchPattern());
                                                    refBookHierDataPresenter.loadAndSelect();
                                                    commonEditPresenter.createFields(result.getColumns());
                                                    commonEditPresenter.setMode(mode);
                                                }
                                            }, RefBookHierPresenter.this));

                                    dispatcher.execute(new GetNameAction(refBookId), CallbackUtils.defaultCallback(
                                            new AbstractCallback<GetNameResult>() {
                                                @Override
                                                public void onSuccess(GetNameResult result) {
                                                    refBookName = result.getName();
                                                    getView().setRefBookNameDesc(refBookName);
                                                }
                                            }, RefBookHierPresenter.this));
                                    getView().setVersionedFields(result.isVersioned());
                                    //hierEditFormPresenter.setCanVersion(canVersion);
                                    versionPresenter.setHierarchy(true);
                                } else {
                                    /*getProxy().manualReveal(RefBookHierPresenter.this);*/
                                    Dialog.errorMessage("Доступ к справочнику запрещен!");
                                }
                            }
                        }, RefBookHierPresenter.this));
            }

            @Override
            public void onFailure(Throwable caught) {
                placeManager.unlock();
                placeManager.revealErrorPlace("");
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
        action.setAscSorting(true);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<CreateReportResult>() {
                            @Override
                            public void onSuccess(CreateReportResult result) {
                                LogAddEvent.fire(RefBookHierPresenter.this, result.getUuid());
                            }
                        }, RefBookHierPresenter.this));
    }
}
