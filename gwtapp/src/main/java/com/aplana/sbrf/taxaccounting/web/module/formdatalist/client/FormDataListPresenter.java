package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataListPresenter extends
		Presenter<FormDataListPresenter.MyView, FormDataListPresenter.MyProxy>
		implements FormDataListUiHandlers, FilterReadyEvent.MyHandler {
	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter}
	 * 's proxy.
	 */
	@ProxyCodeSplit
	@NameToken(nameToken)
	public interface MyProxy extends ProxyPlace<FormDataListPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter}
	 * 's view.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataListUiHandlers> {
		void setFormDataList(List<FormData> records);
		void setDepartmentMap(Map<Integer, String> departmentMap);
		void setReportPeriodMap(Map<Integer, String> reportPeriodMap);

		public <C> Column<FormData, C> addTableColumn(Cell<C> cell,
				String headerText, final ValueGetter<C> getter,
				FieldUpdater<FormData, C> fieldUpdater);
	}

	public static final String nameToken = "!formDataList";

	private final PlaceManager placeManager;
	private final DispatchAsync dispatcher;

	private final FilterPresenter filterPresenter;
	static final Object TYPE_filterPresenter = new Object();

	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher,
			FilterPresenter filterPresenter) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
		this.filterPresenter = filterPresenter;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		// TODO: довольно коряво, проблема в том, у ActionCell нет методов для
		// переопределения делегатов
		// нужно как-то передалать.
		// Также, нужно разобраться, нужно ли здесь делать registerHandler???
		ActionCell<FormData> openFormDataCell = new ActionCell<FormData>(
				"Открыть", new ActionCell.Delegate<FormData>() {
					@Override
					public void execute(FormData formData) {
						String formDataId = formData.getId().toString();
						placeManager.revealPlace(new PlaceRequest(
								FormDataPresenter.NAME_TOKEN).with(
								FormDataPresenter.FORM_DATA_ID, formDataId));

					}
				});

		getView().addTableColumn(openFormDataCell, "Action",
				new ValueGetter<FormData>() {
					@Override
					public FormData getValue(FormData contact) {
						return contact;
					}
				}, null);
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.Presenter#useManualReveal()
	 */
	@Override
	public boolean useManualReveal() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.Presenter#prepareFromRequest(com.gwtplatform.mvp.client.proxy.PlaceRequest)
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		//System.out.println("NAME TOKEN:" + request.getNameToken());
		filterPresenter.initFilter();
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		setInSlot(TYPE_filterPresenter, filterPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_filterPresenter);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
				this);
	}

	/**
	 * Применение фильтра, обновление списка форм
	 * 
	 * @param filterFormData
	 */
	private void loadFormDataList(FormDataFilter filterFormData) {
		GetFormDataList action = new GetFormDataList();
		action.setFormDataFilter(filterFormData);
		//TODO: убрать хардкод!
		action.setTaxType(TaxType.TRANSPORT);

		dispatcher.execute(action,
				new AbstractCallback<GetFormDataListResult>() {
					@Override
					public void onSuccess(GetFormDataListResult result) {
						Map<Integer, String> departmentMap = new HashMap<Integer, String>();
						for(Department department : result.getDepartments()){
							departmentMap.put(department.getId(), department.getName());
						}

						Map<Integer, String> reportPeriodMap = new HashMap<Integer, String>();
						for(ReportPeriod period : result.getReportPeriods()){
							reportPeriodMap.put(period.getId(), period.getName());
						}

						getView().setFormDataList(result.getRecords());
						getView().setDepartmentMap(departmentMap);
						getView().setReportPeriodMap(reportPeriodMap);

						if (!isVisible()) {
							// Вручную вызывается onReveal
							getProxy().manualReveal(FormDataListPresenter.this);
						}
						super.onSuccess(result);
					}
				});
	}

	@Override
	public void onApplyFilter() {
		FormDataFilter filterFormData = filterPresenter.getFilterData();
		loadFormDataList(filterFormData);
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent.MyHandler#onFilterReady(com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent)
	 */
	@ProxyEvent
	@Override
	public void onFilterReady(FilterReadyEvent event) {
		if (event.getSource() == filterPresenter) {
			FormDataFilter filterFormData = filterPresenter.getFilterData();
			loadFormDataList(filterFormData);
		}
	}

	@Override
	public void onCreateClicked() {

	}

}
