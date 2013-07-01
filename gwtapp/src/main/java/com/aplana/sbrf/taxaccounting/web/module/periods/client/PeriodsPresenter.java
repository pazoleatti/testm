package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
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
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;

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
		void setFilterData(List<Department> departments);
	}

	private final DispatchAsync dispatcher;
	protected final OpenDialogPresenter openDialogPresenter;
	private List<Department> departments;
	private final TaPlaceManager placeManager;
	private List<DictionaryTaxPeriod> dictionaryTaxPeriods;
	private long declarationId;
	private String taxName;

	@Inject
	public PeriodsPresenter(final EventBus eventBus, final MyView view,
	                        final MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher,
	                        OpenDialogPresenter openDialogPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.openDialogPresenter = openDialogPresenter;
		this.placeManager = (TaPlaceManager) placeManager;
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
		ClosePeriodAction requestData = new ClosePeriodAction();
		requestData.setReportPeriodId(reportPeriod.getId());
		dispatcher.execute(requestData, CallbackUtils //TODO добавить апдейт таблицы
				.defaultCallback(new AbstractCallback<ClosePeriodResult>() {
					@Override
					public void onSuccess(ClosePeriodResult result) {
						System.out.println("Close: " + result);


					}
				}, PeriodsPresenter.this)
		);

	}

	@Override
	public void openPeriod() {
//		if (reportPeriod.isOpen()) {
//			Window.alert("Период уже открыт.");
//			return;
//		}
		addToPopupSlot(openDialogPresenter);
		openDialogPresenter.setDepartments(departments);
		openDialogPresenter.setDictionaryTaxPeriod(dictionaryTaxPeriods);

//		OpenPeriodAction requestData = new OpenPeriodAction();
//		requestData.setReportPeriodId(reportPeriod.getId());
//		dispatcher.execute(requestData, CallbackUtils //TODO добавить апдейт таблицы
//				.defaultCallback(new AbstractCallback<OpenPeriodResult>() {
//					@Override
//					public void onSuccess(OpenPeriodResult result) {
//						System.out.println("Open: " + result);
//						getProxy().manualReveal(PeriodsPresenter.this);
//					}
//				}, PeriodsPresenter.this)
//		);
	}

	/**
	 * Здесь происходит подготовка декларации.
	 *
	 * @param request
	 *            запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		taxType = TaxType.valueOf(request.getParameter("nType", ""));
		getView().setTitle(taxType.getName() + " / Ведение периодов");
		if ((taxType == TaxType.INCOME) || (taxType == TaxType.VAT)) {
			getView().setDepartmentPickerEnable(false);
		} else {
			getView().setDepartmentPickerEnable(true);
		}
		GetPeriodDataAction requestData = new GetPeriodDataAction();
		requestData.setTaxType(TaxType.valueOf(request.getParameter("nType", "")));
//		applyFilter(1900, 2100);
		PeriodsGetFilterData getFilterData = new PeriodsGetFilterData();
		getFilterData.setTaxType(taxType);
		dispatcher.execute(getFilterData, CallbackUtils
				.defaultCallback(new AbstractCallback<PeriodsGetFilterDataResult>() {
					@Override
					public void onSuccess(PeriodsGetFilterDataResult result) {
						departments = result.getDepartments();
						dictionaryTaxPeriods = result.getDictionaryTaxPeriods();
						getView().setFilterData(departments);
					}
				}, PeriodsPresenter.this)
		);
	}
}
