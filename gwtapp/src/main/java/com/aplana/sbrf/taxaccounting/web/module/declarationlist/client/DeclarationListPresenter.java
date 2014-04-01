package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterCreateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationList;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListResult;
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

import java.util.HashMap;
import java.util.Map;

public class DeclarationListPresenter extends
		DeclarationListPresenterBase<DeclarationListPresenter.MyProxy> implements
		DeclarationListUiHandlers, DeclarationFilterReadyEvent.MyHandler, DeclarationFilterApplyEvent.DeclarationFilterApplyHandler {

    /**
     * Текущее состояние фильтров для всех типов деклараций.
     * Обновляться из фильтра при FormDataListApplyEvent.
     * Сетится в фильтр при открытии формы.
     * Используется при заполнении начальных значений фильтра поиска
     */
    private Map<TaxType, DeclarationDataFilter> filterStates = new HashMap<TaxType, DeclarationDataFilter>();
    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();

    @ProxyEvent
    @Override
    public void onClickApply(DeclarationFilterApplyEvent event) {
        DeclarationDataFilter dataFilter = filterPresenter.getFilterData();
        saveFilterState(dataFilter.getTaxType(), dataFilter);
        updateTitle(dataFilter.getTaxType());
        getView().updateData(0);
    }

    @Override
    public void onCreateClicked() {
        creationPresenter.initAndShowDialog(filterPresenter.getFilterData(), this);
    }

    @ProxyCodeSplit
	@NameToken(DeclarationListNameTokens.DECLARATION_LIST)
	public interface MyProxy extends ProxyPlace<DeclarationListPresenter>, Place {
	}

	@Inject
	public DeclarationListPresenter(EventBus eventBus, DeclarationListPresenterBase.MyView view, MyProxy proxy,
	                         PlaceManager placeManager, DispatchAsync dispatcher,
	                         DeclarationFilterPresenter filterPresenter, DeclarationCreationPresenter creationPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, creationPresenter);
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
			TaxType taxType = TaxType.valueOf(request.getParameter("nType", ""));
			filterPresenter.initFilter(taxType, filterStates.get(taxType));
			getView().updateTitle(taxType);
            filterPresenter.getView().updateFilter(taxType);
            getView().updatePageSize(taxType);
		} catch (Exception e) {
			ErrorEvent.fire(this, "Не удалось открыть список деклараций", e);
		}
	}

	@ProxyEvent
	@Override
	public void onFilterReady(DeclarationFilterReadyEvent event) {
        if (event.getSource() == filterPresenter) {
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
	public void onSortingChanged(){
        getView().updateData();
	}

    @Override
    public void onRangeChange(final int start, final int length) {
        DeclarationDataFilter filter = filterPresenter.getFilterData();
        filter.setCountOfRecords(length);
        filter.setStartIndex(start);
        filter.setAscSorting(getView().isAscSorting());
        filter.setSearchOrdering(getView().getSearchOrdering());
        GetDeclarationList requestData = new GetDeclarationList();
        requestData.setDeclarationFilter(filter);

        dispatcher.execute(requestData, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDeclarationListResult>() {
                    @Override
                    public void onSuccess(GetDeclarationListResult result) {
                        getView().setTableData(start, result.getTotalCountOfRecords(),
                                result.getRecords(), result.getDepartmentFullNames());
                    }
                }, DeclarationListPresenter.this));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        String url = DeclarationDataTokens.declarationData + ";" +DeclarationDataTokens.declarationId;
        if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
            filterPresenter.getView().clean();
        }
    }

    private void updateTitle(TaxType taxType){
        // TODO Зачем это? В постановке нет. Виды налога перечислены не все.
		String description = "";
        String title = "Список деклараций";
		if(taxType.getName().equals(TaxType.VAT.getName())){
			description = "Деклараци по НДС";
		}  else if (taxType.getName().equals(TaxType.PROPERTY.getName())){
			description = "Деклараци по налогу на имущество";
		}  else if (taxType.getName().equals(TaxType.TRANSPORT.getName())){
			description = "Деклараци по транспортному налогу";
		}  else if (taxType.getName().equals(TaxType.INCOME.getName())){
			description = "Деклараци по налогу на прибыль";
		} else if (taxType.getName().equals(TaxType.DEAL.getName())) {
            title = "Список уведомлений";
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
        cloneFilter.setDeclarationTypeId(filter.getDeclarationTypeId());
        // Если мы захотим чтобы для каждого налога запоминались другие параметры поиска (сортировка...),
        // то вместо создания нового мы должны будем получать фильтр из мапки и обновлять.

        filterStates.put(taxType, cloneFilter);
    }
}
