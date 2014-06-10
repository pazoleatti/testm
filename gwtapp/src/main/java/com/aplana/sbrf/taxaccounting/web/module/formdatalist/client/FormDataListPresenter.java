package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create.CreateFormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterFormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterFormDataReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FormDataListApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FormDataListCreateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetKindListAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetKindListResult;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.HashMap;
import java.util.Map;

public class FormDataListPresenter extends FormDataListPresenterBase<FormDataListPresenter.MyProxy> implements
		FormDataListUiHandlers, FilterFormDataReadyEvent.MyHandler, FormDataListCreateEvent.FormDataCreateHandler,
		FormDataListApplyEvent.FormDataApplyHandler {
	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter}
	 * 's proxy.
	 */
	@ProxyCodeSplit
	@NameToken(FormDataListNameTokens.FORM_DATA_LIST)
	public interface MyProxy extends ProxyPlace<FormDataListPresenter>, Place {
	}

	/**
	 * Текущий тип налога
	 */
	private TaxType taxType;

	/**
	 * Текущее состояние фильтров для всех типов налогов.
	 * Обновляться из фильтра при FormDataListApplyEvent.
	 * Сетится в фильтр при открытии формы.
	 * Используется при заполнении начальных значений фильтра поиска
	 */
	private Map<TaxType, FormDataFilter> filterStates = new HashMap<TaxType, FormDataFilter>();

    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();

    private Long selectedItemId;

	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher,
			FilterFormDataPresenter filterPresenter, CreateFormDataPresenter dialogPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, dialogPresenter);
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
	protected void onBind() {
		addRegisteredHandler(FilterFormDataReadyEvent.getType(), this);
		addRegisteredHandler(FormDataListCreateEvent.getType(), this);
		addRegisteredHandler(FormDataListApplyEvent.getType(), this);
		super.onBind();
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		LogCleanEvent.fire(this);
		LogShowEvent.fire(this, false);
        TaxType taxTypeOld = taxType;
		taxType = TaxType.valueOf(request.getParameter("nType", ""));
		filterPresenter.changeFilterElementNames(taxType);
        getView().updatePageSize(taxType);
        if (taxTypeOld == null || !taxType.equals(taxTypeOld)) {
            filterStates.clear();
            getView().updateFormDataTable(taxType);
            selectedItemId = null;
        }
        String url = FormDataPresenter.NAME_TOKEN + ";" + FormDataPresenter.FORM_DATA_ID;
        if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
            filterPresenter.getView().clean();
            filterStates.clear();
            selectedItemId = null;
        }
        // Передаем типы налоговых форм
        GetKindListAction kindListAction = new GetKindListAction();
        kindListAction.setTaxType(taxType);
                dispatcher.execute(kindListAction, CallbackUtils
                .defaultCallback(new AbstractCallback<GetKindListResult>() {
                    @Override
                    public void onSuccess(GetKindListResult kindListResult) {
                        filterPresenter.initFilter(taxType, filterStates.get(taxType), kindListResult);
                    }
                }, this));
        super.prepareFromRequest(request);
	}

    @Override
	public void onClickCreate(FormDataListCreateEvent event) {
		// При создании формы берем не последний примененный фильтр, а фильтр который сейчас выставлен в форме фильтрации
		// Если это поведение не устаривает то нужно получить фильтр из состояни формы getFilterState
		dialogPresenter.initAndShowDialog(filterPresenter.getFilterData(), this);
	}

	@Override
	public void onClickFind(FormDataListApplyEvent event) {
		FormDataFilter filter = filterPresenter.getFilterData();
		saveFilterState(filter.getTaxType(), filter);
		getView().updateData(0);
	}

	@Override
	public void onFilterReady(FilterFormDataReadyEvent event) {
		if (event.getSource() == filterPresenter) {
			if (event.isSuccess()){
				FormDataFilter filter = filterPresenter.getFilterData();
				getView().updateTitle(filter.getTaxType().getName());
				// TODO Нужно переделать
				if (filter.getTaxType() == TaxType.DEAL) {
					getView().updateHeader("Список форм");
				} else {
					getView().updateHeader("Список налоговых форм");
				}
				this.taxType = filter.getTaxType();
				saveFilterState(filter.getTaxType(), filter);
				getView().updateData(0);

				// Презентор фильтра успешно проинициализировался - делаем ревал
				getProxy().manualReveal(FormDataListPresenter.this);
			} else {
				// Отменяем отображение формы
				getProxy().manualRevealFailed();
			}
		}
	}

	@Override
	public void onSortingChanged(){
		getView().updateData();
	}

	private void saveFilterState(TaxType taxType, FormDataFilter filter){
		// Это ворк эраунд.
		// Нужно клонировать состояние т.к. в FilterFormDataPresenter
		// может менять значения в этом объекте, что нужно не всегда.
		// Здесь должны быть добавлены все поля для которых мы хотим сохранять состояние
		// при переходах между формами
		FormDataFilter cloneFilter = new FormDataFilter();
		cloneFilter.setTaxType(filter.getTaxType());
		cloneFilter.setFormTypeId(filter.getFormTypeId());
		cloneFilter.setFormDataKind(filter.getFormDataKind());
		cloneFilter.setReportPeriodIds(filter.getReportPeriodIds());
		cloneFilter.setDepartmentIds(filter.getDepartmentIds());
		cloneFilter.setFormState(filter.getFormState());
		cloneFilter.setReturnState(filter.getReturnState());
		// Если мы захотим чтобы для каждого налога запоминались другие параметры поиска (сортировка...),
		// то вместо создания нового мы должны будем получать фильтр из мапки и обновлять.

		filterStates.put(taxType, cloneFilter);
	}

	private FormDataFilter getFilterState(TaxType taxType){
		return filterStates.get(taxType);
	}

	@Override
	public void onRangeChange(final int start, int length) {
		FormDataFilter filter = getFilterState(taxType);
        if (filter == null) {
            return;
        }
        filter.setFormDataId(selectedItemId);
		filter.setCountOfRecords(length);
		filter.setStartIndex(start);
		filter.setAscSorting(getView().isAscSorting());
		filter.setSearchOrdering(getView().getSearchOrdering());
		GetFormDataList request = new GetFormDataList();
		request.setFormDataFilter(filter);
		dispatcher.execute(request, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormDataListResult>() {
					@Override
					public void onSuccess(GetFormDataListResult result) {
                        if (result.getPage() != null && !result.getPage().equals(getView().getPage())) { // находим и устанавливаем нужную Page
                            getView().setPage(result.getPage());
                        } else {
                            getView().setTableData(start, result.getTotalCountOfRecords(), result.getRecords(), result.getDepartmentFullNames(), selectedItemId);
                            selectedItemId = null;
                        }
					}
				}, FormDataListPresenter.this));
	}

    @Override
    public void onCreateClicked() {
        FormDataListCreateEvent.fire(this);
    }

    @Override
    protected void onHide() {
        super.onHide();
        selectedItemId = getView().getSelectedId();
    }
}
