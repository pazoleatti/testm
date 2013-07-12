package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;


/**
 * Диалог открытия периода
 */

public class OpenDialogPresenter extends PresenterWidget<OpenDialogPresenter.MyView> implements OpenDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<OpenDialogUiHandlers> {
		void setDeclarationFilter(DeclarationDataFilter filter);
		void setDeclarationFilterValues(DeclarationDataFilterAvailableValues filterValues);
		void setReportPeriods(List<ReportPeriod> reportPeriods);
		void setTaxPeriods(List<TaxPeriod> taxPeriods);
		void setDepartments(List<Department> departments);
		void setCurrentReportPeriod(ReportPeriod reportPeriod);
		void setDictionaryTaxPeriod(List<DictionaryTaxPeriod> dictionaryTaxPeriod);
		DeclarationDataFilter updateAndGetDeclarationFilter();
	}

	private DispatchAsync dispatcher;
	private PlaceManager placeManager;

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

	public void setDeclarationFilter(DeclarationDataFilter filter) {
		getView().setDeclarationFilter(filter);
	}

	public void setFilterValues(DeclarationDataFilterAvailableValues filterValues) {
		getView().setDeclarationFilterValues(filterValues);
	}

	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		getView().setTaxPeriods(taxPeriods);
	}

	public void setDepartments(List<Department> departments) {
		getView().setDepartments(departments);
	}

	public void setDictionaryTaxPeriod(List<DictionaryTaxPeriod> dictionaryTaxPeriod) {
		getView().setDictionaryTaxPeriod(dictionaryTaxPeriod);
	}

	public void setCurrentReportPeriod(ReportPeriod currentReportPeriod) {
		getView().setCurrentReportPeriod(currentReportPeriod);
	}

	@Override
	public void onContinue() {
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
}
