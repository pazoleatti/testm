package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.Arrays;
import java.util.List;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog.DeadlineDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.UpdateForm;
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
								implements PeriodsUiHandlers, PeriodCreated.OpenPeriodHandler, UpdateForm.UpdateFormHandler {

	private TaxType taxType;

	@ProxyCodeSplit
	@NameToken(PeriodsTokens.PERIODS)
	public interface MyProxy extends ProxyPlace<PeriodsPresenter>,
			Place {
	}

	public interface MyView extends View,
			HasUiHandlers<PeriodsUiHandlers> {
		void setTitle(String title);
        void setTaxTitle(String title);
        void setTableData(List<TableRow> data);
		void setFilterData(List<Department> departments, List<DepartmentPair> selectedDepartments, int yearFrom, int yearTo);
		void setYear(int year);
		Integer getFromYear();
		Integer getToYear();
        DepartmentPair getDepartmentId();
		TableRow getSelectedRow();
        void setCanChangeDepartment(boolean canChange);
		void setCanChangeDeadline(boolean canChangeDeadline);
	}

	private final TaPlaceManager placeManager;
	private final DispatchAsync dispatcher;
	protected final OpenDialogPresenter openDialogPresenter;
    protected final DeadlineDialogPresenter deadlineDialogPresenter;

	@Inject
	public PeriodsPresenter(final EventBus eventBus, final MyView view,
	                        final MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher,
	                        OpenDialogPresenter openDialogPresenter, DeadlineDialogPresenter deadlineDialogPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.placeManager = (TaPlaceManager) placeManager;
		this.dispatcher = dispatcher;
		this.openDialogPresenter = openDialogPresenter;
        this.deadlineDialogPresenter = deadlineDialogPresenter;
		getView().setUiHandlers(this);
	}

	@Override
	public void onBind(){
		addRegisteredHandler(PeriodCreated.getType(), this);
		addRegisteredHandler(UpdateForm.getType(), this);
	}

	@Override
	public void closePeriod() {
        if (getView().getSelectedRow() == null) {
            MessageEvent.fire(this, "В списке не выбран период");
            return;
        }
        if (!getView().getSelectedRow().isSubHeader()) {
            if (!getView().getSelectedRow().isOpen()) {
                Dialog.warningMessage("Период уже закрыт.");
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
                                LogAddEvent.fire(PeriodsPresenter.this, result.getUuid());
                            }
                        }, PeriodsPresenter.this));
            }
        }
    }

	@Override
	public void openPeriod() {
        DepartmentPair departmentPair = getView().getDepartmentId();
        if (departmentPair == null) {
            MessageEvent.fire(this, "Не выбрано подразделение!");
            return;
        }
        openDialogPresenter.resetToDefault();
        openDialogPresenter.setSelectedDepartment(departmentPair.getDepartmentId());
        openDialogPresenter.setYear(getView().getFromYear());
        addToPopupSlot(openDialogPresenter);
	}

	@Override
	public void onFindButton() {
        if (getView().getDepartmentId() == null) {
            Dialog.warningMessage("Не выбрано подразделение!");
        } else if ((getView().getFromYear() == null)
				|| (getView().getToYear() == null)
				|| (getView().getFromYear() > getView().getToYear())){
			Dialog.warningMessage("Интервал периода поиска указан неверно!");
		} else {
			find();
		}
	}

    @Override
    public void setDeadline() {
        if (getView().getSelectedRow() == null) {
            MessageEvent.fire(this, "В списке не выбран период");
            return;
        }
        final DepartmentPair departmentPair = getView().getDepartmentId();
        if (departmentPair == null) {
            MessageEvent.fire(this, "Не выбрано подразделение!");
            return;
        }

	    GetDeadlineDepartmentsAction getFilterData = new GetDeadlineDepartmentsAction();
        getFilterData.setTaxType(taxType);
	    getFilterData.setDepartment(getView().getDepartmentId());
        dispatcher.execute(getFilterData, CallbackUtils
		        .defaultCallback(new AbstractCallback<GetDeadlineDepartmentsResult>() {
			        @Override
			        public void onSuccess(GetDeadlineDepartmentsResult result) {
				        TableRow selectedPeriod = getView().getSelectedRow();
				        PeriodsPresenter.this.deadlineDialogPresenter.setTitle(selectedPeriod.getPeriodName(), selectedPeriod.getYear());
				        deadlineDialogPresenter.setDepartments(result.getDepartments(), Arrays.asList(result.getSelectedDepartment()));
				        deadlineDialogPresenter.setDeadLine(selectedPeriod.getDeadline());
				        deadlineDialogPresenter.setSelectedPeriod(selectedPeriod);
				        deadlineDialogPresenter.setTaxType(taxType);
			        }
		        }, PeriodsPresenter.this)
        );
        addToPopupSlot(deadlineDialogPresenter);
    }

	@Override
	public void removePeriod() {
		RemovePeriodAction requestData = new RemovePeriodAction();
		requestData.setReportPeriodId((int)getView().getSelectedRow().getReportPeriodId());
		requestData.setTaxType(taxType);
		requestData.setDepartmentId(getView().getSelectedRow().getDepartmentId());
		dispatcher.execute(requestData, CallbackUtils
				.defaultCallback(new AbstractCallback<RemovePeriodResult>() {
					@Override
					public void onSuccess(RemovePeriodResult result) {
						find();
						LogAddEvent.fire(PeriodsPresenter.this, result.getUuid());
					}
				}, PeriodsPresenter.this));
	}

	@Override
	public void selectionChanged() {
		getView().setCanChangeDeadline(getView().getSelectedRow().isOpen());
	}

	public void find() {
		GetPeriodDataAction requestData = new GetPeriodDataAction();
		requestData.setTaxType(taxType);
		requestData.setFrom(getView().getFromYear());
		requestData.setTo(getView().getToYear());
		requestData.setDepartmentId(getView().getDepartmentId().getDepartmentId());
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
                        getView().setTaxTitle(taxType.getName());
						getView().setTitle("Ведение периодов");
						PeriodsPresenter.this.openDialogPresenter.setTaxType(result.getTaxType());
                        getView().setFilterData(result.getDepartments(), Arrays.asList(result.getSelectedDepartment()), result.getYearFrom(), result.getYearTo());
                        getView().setCanChangeDepartment(result.canChangeDepartment());
						openDialogPresenter.setDepartments(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), true);
						openDialogPresenter.setCanChangeDepartment(result.canChangeDepartment());
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
		getView().setYear(event.getYear());
		find();
	}

	@Override
	public void onUpdateFormHandler(UpdateForm event) {
		find();
	}
}
