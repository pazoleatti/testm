package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.FocusActionEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused.ChangeStatusEDPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download.DeclarationDownloadReportsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create.CommentEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create.MoveToCreateListPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.event.UpdateNotificationCount;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.*;

public class DeclarationListPresenter extends
        DeclarationListPresenterBase<DeclarationListPresenter.MyProxy> implements
        DeclarationListUiHandlers, DeclarationFilterReadyEvent.MyHandler, DeclarationFilterApplyEvent.DeclarationFilterApplyHandler,
        UpdateNotificationCount.UpdateNotificationCountHandler {

    private static final String TYPE = "nType";
    public static final String REPORTS = "isReports";

    /**
     * Текущее состояние фильтров для всех типов деклараций.
     * Обновляться из фильтра при FormDataListApplyEvent.
     * Сетится в фильтр при открытии формы.
     * Используется при заполнении начальных значений фильтра поиска
     */
    private Map<String, DeclarationDataFilter> filterStates = new HashMap<String, DeclarationDataFilter>();
    private List<Long> selectedItemIds;
    /**
     * Выбранная в таблице запись
     */
    private Long selectedItemDeclarationDataId;
    private TaxType taxType;
    private Boolean isReports;
    private boolean ready = false;
    private EventBus eventBus;

    @ProxyEvent
    @Override
    public void onClickApply(DeclarationFilterApplyEvent event) {
        onFind();
    }

    @Override
    protected void onBind() {
        super.onBind();
        addRegisteredHandler(UpdateNotificationCount.getType(), this);
    }

    private void onFind() {
        DeclarationDataFilter dataFilter = filterPresenter.getFilterData();
        saveFilterState(dataFilter.getTaxType(), dataFilter);
        updateTitle(dataFilter.getTaxType());
        getView().updateData(0);
    }


    @Override
    public void onCreateClicked() {
        eventBus.fireEvent(new FocusActionEvent(true));
        List<DeclarationFormKind> declarationFormKindList = new ArrayList<DeclarationFormKind>();
        declarationFormKindList.add(DeclarationFormKind.PRIMARY);
        declarationFormKindList.add(DeclarationFormKind.CONSOLIDATED);
        creationPresenter.initAndShowDialog(filterPresenter.getFilterData(), declarationFormKindList, this);
    }

    @Override
    public void onCreateReportsClicked() {
        List<DeclarationFormKind> declarationFormKindList = new ArrayList<DeclarationFormKind>();
        declarationFormKindList.add(DeclarationFormKind.REPORTS);
        creationPresenter.initAndShowDialog(filterPresenter.getFilterData(), declarationFormKindList, this);
    }

    @Override
    public void onDownloadReportsClicked() {
        declarationDownloadReportsPresenter.initAndShowDialog(filterPresenter.getFilterData(), this);
    }

    @ProxyCodeSplit
    @NameToken(DeclarationListNameTokens.DECLARATION_LIST)
    public interface MyProxy extends ProxyPlace<DeclarationListPresenter>, Place {
    }

    @Inject
    public DeclarationListPresenter(EventBus eventBus, DeclarationListPresenterBase.MyView view, MyProxy proxy,
                                    PlaceManager placeManager, final DispatchAsync dispatcher,
                                    DeclarationFilterPresenter filterPresenter, DeclarationCreationPresenter creationPresenter,
                                    DeclarationDownloadReportsPresenter declarationDownloadReportsPresenter, ChangeStatusEDPresenter changeStatusEDPresenter,
                                    MoveToCreateListPresenter moveToCreateListPresenter) {
        super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, creationPresenter, declarationDownloadReportsPresenter, changeStatusEDPresenter, moveToCreateListPresenter);
        this.eventBus = eventBus;
        eventBus.addHandler(CommentEvent.TYPE, new CommentEvent.CommentEventHandler() {
            @Override
            public void update(final CommentEvent event) {
                LogCleanEvent.fire(DeclarationListPresenter.this);
                OperationInfoAction actionInfo = new OperationInfoAction();
                actionInfo.setDeclarationDataReportType(DeclarationDataReportType.TO_CREATE_DEC.getReportType().getDescription());
                actionInfo.setDeclarationDataIdList(event.getDeclarationDataIdList());
                dispatcher.execute(actionInfo, CallbackUtils.defaultCallback(new AbstractCallback<OperationInfoResult>() {
                    @Override
                    public void onSuccess(OperationInfoResult result) {
                        LogAddEvent.fire(DeclarationListPresenter.this, result.getUuid());
                        AcceptDeclarationListAction action = new AcceptDeclarationListAction();
                        action.setDeclarationIds(getView().getSelectedIds());
                        action.setTaxType(taxType);
                        action.setAccepted(false);
                        action.setReasonForReturn(event.getComment());
                        dispatcher.execute(action, CallbackUtils
                                .defaultCallback(new AbstractCallback<AcceptDeclarationListResult>() {
                                    @Override
                                    public void onSuccess(AcceptDeclarationListResult result) {
                                        onFind();
                                    }
                                }, DeclarationListPresenter.this));
                    }
                }, DeclarationListPresenter.this));
            }
        });
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        try {
            LogCleanEvent.fire(this);
            LogShowEvent.fire(this, false);
            super.prepareFromRequest(request);
            TaxType taxTypeOld = taxType;
            Boolean isReportsOld = isReports;
            taxType = TaxType.valueOf(request.getParameter(TYPE, ""));
            isReports = Boolean.parseBoolean(request.getParameter(REPORTS, "false"));
            getView().initTable(taxType, isReports);
            if (taxTypeOld == null || !taxType.equals(taxTypeOld) || isReportsOld == null || !isReportsOld.equals(isReports)) {
                getView().updateTitle(taxType);
                selectedItemIds = null;
                selectedItemDeclarationDataId = null;
            }
            //String url = DeclarationDataTokens.declarationData + ";" +DeclarationDataTokens.declarationId;
            filterPresenter.setDeclarationListPresenter(this);
            filterPresenter.initFilter(taxType, isReports, filterStates.get(taxType.name() + "_" + isReports));
            filterPresenter.getView().updateFilter(taxType, isReports);
            getView().updatePageSize(taxType);

            getView().updateButton();
            ready = false;

            DetectUserRoleAction action = new DetectUserRoleAction();
            action.setTaxType(taxType);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<DetectUserRoleResult>() {
                        @Override
                        public void onSuccess(DetectUserRoleResult result) {
                            getView().setVisibleCancelButton(result.isControl());
                            getView().setVisibleCreateButton((result.isControl() || result.isHasRoleOperator()) && !isReports);
                            getView().showCheck(result.isControl() || result.isHasRoleOperator());
                            getView().showRecalculate(!isReports && (result.isControl() || result.isHasRoleOperator()));
                            getView().showAccept(result.isControl());
                            getView().showDelete(result.isControl() || result.isHasRoleOperator());
                        }
                    }, this));
        } catch (Exception e) {
            ErrorEvent.fire(this, "Не удалось открыть список налоговых форм", e);
        }
    }

    @ProxyEvent
    @Override
    public void onFilterReady(DeclarationFilterReadyEvent event) {
        if (event.getSource() == filterPresenter) {
            ready = true;
            DeclarationDataFilter dataFilter = filterPresenter.getFilterData();
            updateTitle(dataFilter.getTaxType());
            getView().updateData(0);
            /*Вручную вызывается onReveal. Вызываем его всегда,
             даже когда презентер в состоянии visible, т.к. нам необходима
             его разблокировка.
             Почему GWTP вызывает блокировку даже если страница
             уже видна - непонятно.*/
            getProxy().manualReveal(DeclarationListPresenter.this);
        }
    }

    @Override
    public void onRangeChange(final int start, final int length) {
        if (!ready) {
            return;
        }
        DeclarationDataFilter filter = filterPresenter.getFilterData();
        filter.setDeclarationDataId(selectedItemDeclarationDataId);
        filter.setCountOfRecords(length);
        filter.setStartIndex(start);
        filter.setAscSorting(getView().isAscSorting());
        filter.setSearchOrdering(getView().getSearchOrdering());

        if (isReports) {
            filter.setFormKindIds(Arrays.asList(DeclarationFormKind.REPORTS.getId()));
        } else {
            if (filter.getFormKindIds() == null || filter.getFormKindIds().isEmpty()) {
                filter.setFormKindIds(Arrays.asList(DeclarationFormKind.PRIMARY.getId(), DeclarationFormKind.CONSOLIDATED.getId()));
            } else {
                filter.getFormKindIds().remove(DeclarationFormKind.REPORTS.getId());
            }
        }

        GetDeclarationList requestData = new GetDeclarationList();
        requestData.setDeclarationFilter(filter);
        requestData.setReports(isReports);

        dispatcher.execute(requestData, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDeclarationListResult>() {
                    @Override
                    public void onSuccess(GetDeclarationListResult result) {
                        if (result.getPage() != null && !result.getPage().equals(getView().getPage())) {
                            getView().setPage(result.getPage());
                        } else {
                            getView().setTableData(start, result.getTotalCountOfRecords(),
                                    result.getRecords(), result.getDepartmentFullNames(), result.getAsnuNames(),
                                    selectedItemIds);
                            selectedItemIds = null;
                            selectedItemDeclarationDataId = null;
                        }
                    }
                }, DeclarationListPresenter.this));
    }

    private void updateTitle(TaxType taxType) {
        String description = "Список налоговых форм";
        String title = "Список налоговых форм";
        if (taxType != null) {
            switch (taxType) {
                case NDFL:
                    description = "Налоговые формы по НДФЛ";
                    break;
            }
        }
        TitleUpdateEvent.fire(this, title, description);
    }

    private void saveFilterState(TaxType taxType, DeclarationDataFilter filter) {
        // Это ворк эраунд.
        // Нужно клонировать состояние т.к. в DeclarationDataPresenter
        // может менять значения в этом объекте, что нужно не всегда.
        // Здесь должны быть добавлены все поля для которых мы хотим сохранять состояние
        // при переходах между формами
        DeclarationDataFilter cloneFilter = new DeclarationDataFilter();
        cloneFilter.setTaxType(filter.getTaxType());
        cloneFilter.setReportPeriodIds(filter.getReportPeriodIds());
        cloneFilter.setDepartmentIds(filter.getDepartmentIds());
        cloneFilter.setDeclarationTypeIds(filter.getDeclarationTypeIds());
        cloneFilter.setFormState(filter.getFormState());
        cloneFilter.setCorrectionTag(filter.getCorrectionTag());
        cloneFilter.setAsnuIds(filter.getAsnuIds());
        cloneFilter.setFormKindIds(filter.getFormKindIds());
        cloneFilter.setFileName(filter.getFileName());
        cloneFilter.setDeclarationDataIdStr(filter.getDeclarationDataIdStr());
        cloneFilter.setDocStateIds(filter.getDocStateIds());
        cloneFilter.setNote(filter.getNote());
        cloneFilter.setOktmo(filter.getOktmo());
        cloneFilter.setTaxOrganCode(filter.getTaxOrganCode());
        cloneFilter.setTaxOrganKpp(filter.getTaxOrganKpp());

        // Если мы захотим чтобы для каждого налога запоминались другие параметры поиска (сортировка...),
        // то вместо создания нового мы должны будем получать фильтр из мапки и обновлять.

        filterStates.put(taxType.name() + "_" + isReports, cloneFilter);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        eventBus.fireEvent(new FocusActionEvent(false));
    }

    @Override
    protected void onHide() {
        super.onHide();
        selectedItemIds = getView().getSelectedIds();
        selectedItemDeclarationDataId = getView().getSelectedItemDeclarationDataId();
        eventBus.fireEvent(new FocusActionEvent(true));
    }

    @Override
    public List<Long> getSelectedItemIds() {
        return selectedItemIds;
    }

    @Override
    public void check() {
        LogCleanEvent.fire(this);
        CheckDeclarationListAction action = new CheckDeclarationListAction();
        action.setDeclarationIds(getView().getSelectedIds());
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CheckDeclarationListResult>() {
                    @Override
                    public void onSuccess(CheckDeclarationListResult result) {
                        LogAddEvent.fire(DeclarationListPresenter.this, result.getUuid());
                        onFind();
                    }
                }, DeclarationListPresenter.this));
    }

    @Override
    public void delete() {
        LogCleanEvent.fire(this);
        Dialog.confirmMessageYesClose(Dialog.CONFIRM_MESSAGE, "Вы уверены, что хотите удалить форму?", new DialogHandler() {
            @Override
            public void yes() {

                DeleteDeclarationListAction action = new DeleteDeclarationListAction();
                action.setDeclarationIds(getView().getSelectedIds());
                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<DeleteDeclarationListResult>() {
                            @Override
                            public void onSuccess(DeleteDeclarationListResult result) {
                                LogAddEvent.fire(DeclarationListPresenter.this, result.getUuid());
                                onFind();
                            }
                        }, DeclarationListPresenter.this));
            }
        });
    }

    @Override
    public void accept(final boolean accepted) {
        if (accepted) {
            LogCleanEvent.fire(this);
            AcceptDeclarationListAction action = new AcceptDeclarationListAction();
            action.setDeclarationIds(getView().getSelectedIds());
            action.setTaxType(taxType);
            action.setAccepted(accepted);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<AcceptDeclarationListResult>() {
                        @Override
                        public void onSuccess(AcceptDeclarationListResult result) {
                            LogAddEvent.fire(DeclarationListPresenter.this, result.getUuid());
                            onFind();
                        }
                    }, DeclarationListPresenter.this));
        } else {
            LogCleanEvent.fire(DeclarationListPresenter.this);
            moveToCreateListPresenter.setDeclarationDataIdList(getView().getSelectedIds());
            addToPopupSlot(moveToCreateListPresenter);
        }
    }

    @Override
    public void onRecalculateClicked() {
        LogCleanEvent.fire(this);
        RecalculateDeclarationListAction action = new RecalculateDeclarationListAction();
        action.setDeclarationIds(getView().getSelectedIds());
        action.setTaxType(taxType);
        action.setDocDate(new Date());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<RecalculateDeclarationListResult>() {
                    @Override
                    public void onSuccess(RecalculateDeclarationListResult result) {
                        LogAddEvent.fire(DeclarationListPresenter.this, result.getUuid());
                        onFind();
                    }
                }, DeclarationListPresenter.this));
    }

    @Override
    public void changeStatusED() {
        changeStatusEDPresenter.init(null, new ChangeStatusEDPresenter.ChangeStatusHandler() {
            @Override
            public void setDocState(Long docStateId) {
                LogCleanEvent.fire(DeclarationListPresenter.this);
                ChangeStatusEDDeclarationListAction action = new ChangeStatusEDDeclarationListAction();
                action.setDeclarationIds(getView().getSelectedIds());
                action.setDocStateId(docStateId);
                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<ChangeStatusEDDeclarationListResult>() {
                            @Override
                            public void onSuccess(ChangeStatusEDDeclarationListResult result) {
                                LogAddEvent.fire(DeclarationListPresenter.this, result.getUuid());
                                changeStatusEDPresenter.hide();
                                onFind();
                            }
                        }, DeclarationListPresenter.this));
            }
        });
        LogCleanEvent.fire(DeclarationListPresenter.this);
        addToPopupSlot(changeStatusEDPresenter);
    }

    public TaxType getTaxType() {
        return taxType;
    }

    @Override
    public Boolean getIsReports() {
        return isReports;
    }

    @Override
    public void updateNotificationCountHandler(UpdateNotificationCount event) {
        GetDeclarationListStateAction action = new GetDeclarationListStateAction();
        action.setDeclarationIds(getView().getVisibleItemIds());
        dispatcher.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<GetDeclarationListStateResult>() {
                    @Override
                    public void onSuccess(GetDeclarationListStateResult result) {
                        getView().updateStatus(result.getStateMap());
                    }
                }));
    }
}
