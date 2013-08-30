package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.ClosePeriodResult;
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
								implements PeriodsUiHandlers, PeriodCreated.OpenPeriodHandler {

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
		void setFilterData(List<Department> departments, Set<Integer> avalDepartments, List<Integer> selectedDepartments, int yearFrom, int yearTo);
		Integer getFromYear();
		Integer getToYear();
		long getDepartmentId();
		TableRow getSelectedRow();
		void setReadOnly(boolean readOnly);
		boolean isFromYearEmpty();
		boolean isToYearEmpty();
	}

	private final TaPlaceManager placeManager;
	private final DispatchAsync dispatcher;
	protected final OpenDialogPresenter openDialogPresenter;

	@Inject
	public PeriodsPresenter(final EventBus eventBus, final MyView view,
	                        final MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher,
	                        OpenDialogPresenter openDialogPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.placeManager = (TaPlaceManager) placeManager;
		this.dispatcher = dispatcher;
		this.openDialogPresenter = openDialogPresenter;
		getView().setUiHandlers(this);
	}

	@Override
	public void onBind(){
		addRegisteredHandler(PeriodCreated.getType(), this);
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
	public void onFindButton() {
		if (getView().isFromYearEmpty() || getView().isToYearEmpty()) {
			Window.alert("Не заданы все обязательные параметры!");
			return;
		} else if ((getView().getFromYear() == null)
				|| (getView().getToYear() == null)
				|| (getView().getFromYear() > getView().getToYear())){
			Window.alert("Интервал периода поиска указан неверно!");
			return;
		} else {
			find();
		}
	}

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
		PeriodsGetFilterData getFilterData = new PeriodsGetFilterData();
		getFilterData.setTaxType(TaxType.valueOf(request.getParameter("nType", "")));
		dispatcher.execute(getFilterData, CallbackUtils
				.defaultCallback(new AbstractCallback<PeriodsGetFilterDataResult>() {
					@Override
					public void onSuccess(PeriodsGetFilterDataResult result) {
						PeriodsPresenter.this.taxType = result.getTaxType();
						getView().setTitle(taxType.getName() + " / Ведение периодов");
						PeriodsPresenter.this.openDialogPresenter.setTaxType(result.getTaxType());
						getView().setFilterData(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), result.getYearFrom(), result.getYearTo());
						getView().setReadOnly(result.isReadOnly());
						openDialogPresenter.setDepartments(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), result.isEnableDepartmentPicker());
						openDialogPresenter.setYear(result.getCurrentYear());
						find();
					}
				}, PeriodsPresenter.this).addCallback(TaManualRevealCallback.create(this, this.placeManager))
		);
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
	
	@Override
	public void onPeriodCreated(PeriodCreated event) {
		find();
	}
}
