package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class PeriodsPresenter extends Presenter<PeriodsPresenter.MyView, PeriodsPresenter.MyProxy>
								implements PeriodsUiHandlers {

	private TaxType taxType;

	@ProxyCodeSplit
	@NameToken(PeriodsTokens.PERIODS)
	public interface MyProxy extends ProxyPlace<PeriodsPresenter>,
			Place {
	}

	public interface MyView extends View,
			HasUiHandlers<PeriodsUiHandlers> {
		void setTitle(String title);
		void setTableData(List<TableRow> data);
		void setDepartmentPickerEnable(boolean enable);
		void setFilterData(List<Department> departments, List<Integer> selectedDepartments, int yearFrom, int yearTo);
		int getFromYear();
		int getToYear();
		long getDepartmentId();
		TableRow getSelectedRow();
	}

	private final DispatchAsync dispatcher;
	protected final OpenDialogPresenter openDialogPresenter;
	private List<Department> departments;
	private List<DictionaryTaxPeriod> dictionaryTaxPeriods;

	@Inject
	public PeriodsPresenter(final EventBus eventBus, final MyView view,
	                        final MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher,
	                        OpenDialogPresenter openDialogPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.openDialogPresenter = openDialogPresenter;
		getView().setUiHandlers(this);
	}

	@Override
	public void closePeriod() {
		if (((taxType == TaxType.INCOME) || (taxType == TaxType.VAT)) && !getView().getSelectedRow().isOpen()) {
			Window.alert("Период уже закрыт.");
			return;
		} else {
			ClosePeriodAction requestData = new ClosePeriodAction();
			requestData.setTaxType(taxType);
			requestData.setReportPeriodId((int) getView().getSelectedRow().getReportPeriodId());
			requestData.setDepartmentId(getView().getSelectedRow().getDepartmentId());
			dispatcher.execute(requestData, CallbackUtils
					.defaultCallback(new AbstractCallback<ClosePeriodResult>() {
						@Override
						public void onSuccess(ClosePeriodResult result) {
							find();
							LogAddEvent.fire(PeriodsPresenter.this, result.getLogEntries());
						}
					}, PeriodsPresenter.this));
		}
	}

	@Override
	public void openPeriod() {
		addToPopupSlot(openDialogPresenter);
	}

	@Override
	public void find() {
		GetPeriodDataAction requestData = new GetPeriodDataAction();
		requestData.setTaxType(taxType);
		requestData.setFrom(getView().getFromYear());
		requestData.setTo(getView().getToYear());
		requestData.setDepartmentId(getView().getDepartmentId());
		dispatcher.execute(requestData, CallbackUtils
				.defaultCallback(new AbstractCallback<GetPeriodDataResult>() {
					@Override
					public void onSuccess(GetPeriodDataResult result) {
						getView().setTableData(result.getRows());
					}
				}, PeriodsPresenter.this));
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		taxType = TaxType.valueOf(request.getParameter("nType", ""));
		getView().setTitle(taxType.getName() + " / Ведение периодов");
		this.openDialogPresenter.setTaxType(taxType);
		GetPeriodDataAction requestData = new GetPeriodDataAction();
		requestData.setTaxType(TaxType.valueOf(request.getParameter("nType", "")));

		PeriodsGetFilterData getFilterData = new PeriodsGetFilterData();
		getFilterData.setTaxType(taxType);
		dispatcher.execute(getFilterData, CallbackUtils
				.defaultCallback(new AbstractCallback<PeriodsGetFilterDataResult>() {
					@Override
					public void onSuccess(PeriodsGetFilterDataResult result) {
						departments = result.getDepartments();
						dictionaryTaxPeriods = result.getDictionaryTaxPeriods();
						List<Integer> selectedDepartments = new ArrayList<Integer>();
						selectedDepartments.add(result.getSelectedDepartment().getId());
						getView().setFilterData(departments, selectedDepartments, result.getYearFrom(), result.getYearTo());
						getView().setDepartmentPickerEnable(result.isEnableDepartmentPicker());
						openDialogPresenter.setDepartments(departments, selectedDepartments);
						openDialogPresenter.setYear(result.getCurrentYear());
						find();
					}
				}, PeriodsPresenter.this)
		);
	}
}
