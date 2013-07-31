package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;
import java.util.Map;


/**
 * Диалог открытия периода
 */

public class OpenDialogPresenter extends PresenterWidget<OpenDialogPresenter.MyView> implements OpenDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<OpenDialogUiHandlers> {
		void setReportPeriods(List<ReportPeriod> reportPeriods);
		void setTaxPeriods(List<TaxPeriod> taxPeriods);
		void setDepartments(List<Department> departments, Map<String, Integer> selectedDepartments);
		void setCurrentReportPeriod(ReportPeriod reportPeriod);
		void setDictionaryTaxPeriod(List<DictionaryTaxPeriod> dictionaryTaxPeriod);
		void setYear(int year);
	}

	private DispatchAsync dispatcher;
	private PlaceManager placeManager;
	private TaxType taxType;

	@Inject
	public OpenDialogPresenter(final EventBus eventBus, final MyView view,
	                           DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onHide() {
		getView().hide();
	}

	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		getView().setTaxPeriods(taxPeriods);
	}

	public void setDepartments(List<Department> departments, Map<String, Integer> selectedDepartments) {
		getView().setDepartments(departments, selectedDepartments);
	}

	public void setDictionaryTaxPeriod(List<DictionaryTaxPeriod> dictionaryTaxPeriod) {
		getView().setDictionaryTaxPeriod(dictionaryTaxPeriod);
	}

	public void setCurrentReportPeriod(ReportPeriod currentReportPeriod) {
		getView().setCurrentReportPeriod(currentReportPeriod);
	}

	public void setYear(int year) {
		getView().setYear(year);
	}

	@Override
	public void onContinue(OpenFilterData openFilterData) {
		OpenPeriodAction action = new OpenPeriodAction();
		action.setYear(openFilterData.getYear());

		action.setEndDate(openFilterData.getEndDate());
		action.setTaxType(this.taxType);
		action.setDepartmentId(openFilterData.getDepartmentId());
		action.setBalancePeriod(openFilterData.isBalancePeriod());
		action.setActive(true);
		action.setDictionaryTaxPeriodId(openFilterData.getDictionaryTaxPeriod().getCode());
		action.setMonths(openFilterData.getDictionaryTaxPeriod().getMonths());
		dispatcher.execute(action, CallbackUtils
				.simpleCallback(new AbstractCallback<OpenPeriodResult>() {
					@Override
					public void onSuccess(OpenPeriodResult result) {
						getView().hide();
					}

					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof OpenException) {
							OpenException openException = (OpenException) caught;
							switch (openException.getErrorCode()) {
								case EXIST_OPEN:
								case PREVIOUS_ACTIVE:
									Window.alert(openException.getErrorMsg());
									break;
								case EXIST_CLOSED:
									if (Window.confirm(openException.getErrorMsg())) {
										ChangeActivePeriodAction requestData = new ChangeActivePeriodAction();
										requestData.setReportPeriodId(openException.getReportPeriodId());
										requestData.setActive(true);
										dispatcher.execute(requestData, CallbackUtils //TODO добавить апдейт таблицы
												.defaultCallback(new AbstractCallback<ChangeActivePeriodResult>() {
													@Override
													public void onSuccess(ChangeActivePeriodResult result) {
														getView().hide();
													}
												}, OpenDialogPresenter.this)
										);
									}
									break;
							}
						} else {
							Window.alert(caught.getMessage());
						}
					}
				})
		);
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		GetReportPeriods action = new GetReportPeriods();
		action.setTaxPeriod(taxPeriod);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
					@Override
					public void onSuccess(GetReportPeriodsResult result) {
						getView().setReportPeriods(result.getReportPeriods());
					}
				}, OpenDialogPresenter.this));
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
}
