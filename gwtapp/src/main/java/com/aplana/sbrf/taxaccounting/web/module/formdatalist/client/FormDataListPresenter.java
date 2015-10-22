package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create.CreateFormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create.CreateFormDataSuccessHandler;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterFormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterFormDataReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FormDataListApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FormDataListCreateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.*;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest.Builder;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

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
	private FormDataFilter filterState = null;

    // Ссылка, по которой перешли из данной формы. Если вернулись оттуда же, то фильтр не сбрасывается
    private String historyRef;

    private Long selectedItemId;

	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher,
			FilterFormDataPresenter filterPresenter, CreateFormDataPresenter dialogPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, dialogPresenter);
		getView().setUiHandlers(this);
        // Осуществился переход по ссылке
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String ref = event.getValue();
                String currentUrl = FormDataListNameTokens.FORM_DATA_LIST + ";nType=" + taxType.name();
                // Для правильной работы браузерной кнопки «Назад»
                if (ref != null && ref.equals(currentUrl)) {
                    return;
                }
                historyRef = ref;
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
            filterState = null;
            getView().updateFormDataTable(taxType);
            selectedItemId = null;
        } else {
            // Id экземпляра не передается, т.к. не важно из какого именно экземпляра НФ мы перешли к списку,
            // фильтры все равно не будут сброшены
            String url = FormDataPresenter.NAME_TOKEN + ";" + FormDataPresenter.FORM_DATA_ID;

            // Переход обратно из этой же формы
            if (historyRef == null || !historyRef.startsWith(url)) {
                filterPresenter.getView().clean();
                filterState = null;
                selectedItemId = null;
            }
        }
        // Передаем типы налоговых форм
        GetKindListAction kindListAction = new GetKindListAction();
        kindListAction.setTaxType(taxType);
                dispatcher.execute(kindListAction, CallbackUtils
                .defaultCallback(new AbstractCallback<GetKindListResult>() {
                    @Override
                    public void onSuccess(GetKindListResult kindListResult) {
                        filterPresenter.initFilter(taxType, filterState, kindListResult);
                    }
                }, this));
        super.prepareFromRequest(request);
	}

    @Override
    public void onClickCreate(FormDataListCreateEvent event) {
        // При создании формы берем не последний примененный фильтр, а фильтр который сейчас выставлен в форме фильтрации
        // Если это поведение не устаривает то нужно получить фильтр из состояни формы getFilterState
        dialogPresenter.initAndShowDialog(filterPresenter.getFilterData(), this, new CreateFormDataSuccessHandler() {
            @Override
            public void onSuccess(CreateFormDataResult result) {
                String uuid = result.getUuid();
                // По какой-то причине переход к экземпляру НФ не попадает в историю, добавляем принудительно
                historyRef = FormDataPresenter.NAME_TOKEN + ";" + FormDataPresenter.FORM_DATA_ID + "=" + result.getFormDataId();
                // Переход к созданному экземпляру. Режим редактирования.
                Builder builder = new Builder().nameToken(FormDataPresenter.NAME_TOKEN)
                        .with(FormDataPresenter.READ_ONLY, "false")
                        .with(FormDataPresenter.FORM_DATA_ID, String.valueOf(result.getFormDataId()));
                if (uuid != null && !uuid.isEmpty()) {
                    builder.with(FormDataPresenter.UUID, String.valueOf(uuid));
                }
                placeManager.revealPlace(builder.build());
            }
        });
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
				if (TaxType.DEAL.equals(filter.getTaxType()) || TaxType.ETR.equals(filter.getTaxType())) {
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
        cloneFilter.setCorrectionTag(filter.getCorrectionTag());
		// Если мы захотим чтобы для каждого налога запоминались другие параметры поиска (сортировка...),
		// то вместо создания нового мы должны будем получать фильтр из мапки и обновлять.

        filterState = cloneFilter;
	}

	private FormDataFilter getFilterState(){
		return filterState;
	}

	@Override
	public void onRangeChange(final int start, int length) {
		FormDataFilter filter = getFilterState();
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
