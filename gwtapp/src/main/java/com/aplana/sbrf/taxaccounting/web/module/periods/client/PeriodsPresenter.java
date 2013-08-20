package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ChangeActivePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ChangeActivePeriodResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetPeriodDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
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
	public void applyFilter(int from, int to, long departmentId) {

		GetPeriodDataAction requestData = new GetPeriodDataAction();
		requestData.setTaxType(taxType);
		requestData.setFrom(from);
		requestData.setTo(to);
		requestData.setDepartmentId(departmentId);
		dispatcher.execute(requestData, CallbackUtils
				.defaultCallback(new AbstractCallback<GetPeriodDataResult>() {
					@Override
					public void onSuccess(GetPeriodDataResult result) {
						getView().setTableData(result.getRows());

					}
				}, PeriodsPresenter.this));
	}


	@Override
	public void closePeriod(TableRow reportPeriod) {
		if (!reportPeriod.isOpen()) {
			Window.alert("Период уже закрыт.");
			return;
		}
		ChangeActivePeriodAction requestData = new ChangeActivePeriodAction();
		requestData.setReportPeriodId(reportPeriod.getId());
		requestData.setActive(false);
		dispatcher.execute(requestData, CallbackUtils //TODO добавить апдейт таблицы
				.defaultCallback(new AbstractCallback<ChangeActivePeriodResult>() {
					@Override
					public void onSuccess(ChangeActivePeriodResult result) {
					}
				}, PeriodsPresenter.this)
		);

	}

	@Override
	public void openPeriod() {
		addToPopupSlot(openDialogPresenter);
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
						openDialogPresenter.setDictionaryTaxPeriod(dictionaryTaxPeriods);
						openDialogPresenter.setYear(result.getCurrentYear());
						applyFilter(result.getYearFrom(), result.getYearTo(), result.getSelectedDepartment().getId());
					}
				}, PeriodsPresenter.this)
		);
	}
}
