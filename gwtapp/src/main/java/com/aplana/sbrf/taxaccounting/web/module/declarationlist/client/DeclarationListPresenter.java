package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download.DeclarationDownloadReportsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
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
		DeclarationListUiHandlers, DeclarationFilterReadyEvent.MyHandler, DeclarationFilterApplyEvent.DeclarationFilterApplyHandler {

    private static final String TYPE = "nType";
    public static final String REPORTS = "isReports";

    /**
     * Текущее состояние фильтров для всех типов деклараций.
     * Обновляться из фильтра при FormDataListApplyEvent.
     * Сетится в фильтр при открытии формы.
     * Используется при заполнении начальных значений фильтра поиска
     */
    private Map<TaxType, DeclarationDataFilter> filterStates = new HashMap<TaxType, DeclarationDataFilter>();
    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();
    private List<Long> selectedItemIds;
    private TaxType taxType;
    private Boolean isReports;
    private boolean ready = false;

    @ProxyEvent
    @Override
    public void onClickApply(DeclarationFilterApplyEvent event) {
        onFind();
    }

    private void onFind() {
        DeclarationDataFilter dataFilter = filterPresenter.getFilterData();
        saveFilterState(dataFilter.getTaxType(), dataFilter);
        updateTitle(dataFilter.getTaxType());
        getView().updateData(0);
    }


    @Override
    public void onCreateClicked() {
        creationPresenter.initAndShowDialog(filterPresenter.getFilterData(), DeclarationFormKind.CONSOLIDATED, this);
    }

    @Override
    public void onCreateReportsClicked() {
        creationPresenter.initAndShowDialog(filterPresenter.getFilterData(), DeclarationFormKind.REPORTS, this);
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
	                         PlaceManager placeManager, DispatchAsync dispatcher,
	                         DeclarationFilterPresenter filterPresenter, DeclarationCreationPresenter creationPresenter,
                             DeclarationDownloadReportsPresenter declarationDownloadReportsPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, creationPresenter, declarationDownloadReportsPresenter);
		getView().setUiHandlers(this);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                lstHistory.put(0, lstHistory.get(1));
                lstHistory.put(1, event.getValue());
            }
        });
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
                filterStates.clear();
                getView().updateTitle(taxType);
                selectedItemIds = null;
            }
            String url = DeclarationDataTokens.declarationData + ";" +DeclarationDataTokens.declarationId;
            if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                    (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
                filterPresenter.getView().clean();
                filterStates.clear();
                selectedItemIds = null;
            }
			filterPresenter.initFilter(taxType, isReports, filterStates.get(taxType));
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
        filter.setDeclarationDataId((selectedItemIds != null && !selectedItemIds.isEmpty()) ? selectedItemIds.get(0) : null);
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
                        }
                    }
                }, DeclarationListPresenter.this));
    }

    private void updateTitle(TaxType taxType){
		String description = "Список налоговых форм";
        String title = "Список налоговых форм";
        if (taxType != null) {
            switch (taxType) {
                case NDFL:
                    description = "Налоговые формы по НДФЛ";
                    break;
                case PFR:
                    title = "Список налоговых форм";
                    break;
            }
        }
		TitleUpdateEvent.fire(this, title, description);
	}

    private void saveFilterState(TaxType taxType, DeclarationDataFilter filter){
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
        cloneFilter.setAsnuId(filter.getAsnuId());
        cloneFilter.setFormKindIds(filter.getFormKindIds());
        cloneFilter.setFileName(filter.getFileName());
        cloneFilter.setDeclarationDataIdStr(filter.getDeclarationDataIdStr());
        cloneFilter.setDocStateId(filter.getDocStateId());
        cloneFilter.setNote(filter.getNote());
        cloneFilter.setOktmo(filter.getOktmo());
        cloneFilter.setTaxOrganCode(filter.getTaxOrganCode());
        cloneFilter.setTaxOrganKpp(filter.getTaxOrganKpp());

        // Если мы захотим чтобы для каждого налога запоминались другие параметры поиска (сортировка...),
        // то вместо создания нового мы должны будем получать фильтр из мапки и обновлять.

        filterStates.put(taxType, cloneFilter);
    }

    @Override
    protected void onHide() {
        super.onHide();
        selectedItemIds = getView().getSelectedIds();
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
        Dialog.confirmMessageYesClose(Dialog.CONFIRM_MESSAGE, "Вы уверены, что хотите удалить налоговую форму?", new DialogHandler() {
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
            Dialog.confirmMessageYesClose(Dialog.CONFIRM_MESSAGE, "Вы действительно хотите вернуть в статус \"Создана\" формы?", new DialogHandler() {
                @Override
                public void yes() {
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
                    super.yes();
                }
            });
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
}
