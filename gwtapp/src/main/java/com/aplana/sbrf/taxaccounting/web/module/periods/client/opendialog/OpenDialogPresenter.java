package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;


/**
 * Диалог открытия периода
 */

public class OpenDialogPresenter extends PresenterWidget<OpenDialogPresenter.MyView> implements OpenDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<OpenDialogUiHandlers> {
		void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<Integer> selectedDepartments, boolean enable);
		void setYear(int year);
		void setTaxType(TaxType taxType);
		void setBalance(boolean balance);
		void setSelectedDepartment(Department dep);
		boolean isYearEmpty();
	}

	private DispatchAsync dispatcher;
	private TaxType taxType;
	private Department defaultDep;

	@Inject
	public OpenDialogPresenter(final EventBus eventBus, final MyView view,
	                           DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onHide() {
		getView().hide();
	}

	@Override
	protected void onReveal() {
		resetToDefault();
	}

	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<Integer> selectedDepartments, boolean enable) {
		getView().setDepartments(departments, avalDepartments, selectedDepartments, enable);
		defaultDep = departments.get(0);
	}

	public void setYear(int year) {
		getView().setYear(year);
	}

	@Override
	public void onContinue(final OpenFilterData openFilterData) {
		if (getView().isYearEmpty() || (openFilterData.getDictionaryTaxPeriod() == null)) {
			Window.alert("Не заданы все обязательные параметры!");
			return;
		}
		OpenPeriodAction action = new OpenPeriodAction();
		action.setYear(openFilterData.getYear());
		action.setEndDate(openFilterData.getEndDate());
		action.setTaxType(this.taxType);
		action.setDepartmentId(openFilterData.getDepartmentId());
		action.setBalancePeriod(openFilterData.isBalancePeriod());
		System.out.println(openFilterData.isBalancePeriod());
		action.setDictionaryTaxPeriodId(openFilterData.getDictionaryTaxPeriod());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<OpenPeriodResult>() {
					@Override
					public void onSuccess(OpenPeriodResult result) {
						PeriodCreated.fire(OpenDialogPresenter.this, true, openFilterData.getYear());
						LogAddEvent.fire(OpenDialogPresenter.this, result.getLogEntries());
						getView().hide();
					}
				}, OpenDialogPresenter.this)
		);
	}


	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
		getView().setTaxType(taxType);
	}

	public void resetToDefault() {
		getView().setBalance(false);
		getView().setSelectedDepartment(defaultDep);
		Date current = new Date();
		getView().setYear(current.getYear());
	}
}
