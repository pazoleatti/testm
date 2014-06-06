package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
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
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog.DeadlineDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditCorrectionDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditDialogData;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog.EditDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opencorrectdialog.OpenCorrectDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
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
        void setCanEditPeriod(boolean canEditPeriod);
		void setCanEdit(boolean canEdit);
        void setCanOpenCorrectPeriod(boolean canOpenCorrectPeriod);
	}

	private final TaPlaceManager placeManager;
	private final DispatchAsync dispatcher;
	protected final OpenDialogPresenter openDialogPresenter;
    protected final EditDialogPresenter editDialogPresenter;
    protected final EditCorrectionDialogPresenter editCorrectionDialogPresenter;
    protected final OpenCorrectDialogPresenter openCorrectDialogPresenter;
    protected final DeadlineDialogPresenter deadlineDialogPresenter;

	@Inject
	public PeriodsPresenter(final EventBus eventBus, final MyView view,
	                        final MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher,
	                        OpenDialogPresenter openDialogPresenter, DeadlineDialogPresenter deadlineDialogPresenter,
                            OpenCorrectDialogPresenter openCorrectDialogPresenter, EditDialogPresenter editDialogPresenter,
                            EditCorrectionDialogPresenter editCorrectionDialogPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.placeManager = (TaPlaceManager) placeManager;
		this.dispatcher = dispatcher;
		this.openDialogPresenter = openDialogPresenter;
        this.openCorrectDialogPresenter = openCorrectDialogPresenter;
        this.deadlineDialogPresenter = deadlineDialogPresenter;
        this.editDialogPresenter = editDialogPresenter;
        this.editCorrectionDialogPresenter = editCorrectionDialogPresenter;
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
                Dialog.errorMessage("Закрытие периода", "Выбранный период уже закрыт!");
                return;
            } else {
                ClosePeriodAction requestData = new ClosePeriodAction();
                requestData.setTaxType(taxType);
                requestData.setReportPeriodId((int) getView().getSelectedRow().getReportPeriodId());
                requestData.setDepartmentId(getView().getSelectedRow().getDepartmentId());
                requestData.setCorrectionDate(getView().getSelectedRow().getCorrectPeriod());
                dispatcher.execute(requestData, CallbackUtils
                        .defaultCallback(new AbstractCallback<ClosePeriodResult>() {
                            @Override
                            public void onSuccess(ClosePeriodResult result) {
                                find();
                                LogAddEvent.fire(PeriodsPresenter.this, result.getUuid());
                                if (result.isErrorBeforeClose()) {
                                    Dialog.errorMessage("Закрытие периода", "Период не может быть закрыт, пока выполняется редактирование форм, относящихся к этому периоду!");
                                }
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
    public void openCorrectPeriod() {
        DepartmentPair departmentPair = getView().getDepartmentId();
        if (departmentPair == null) {
            MessageEvent.fire(this, "Не выбрано подразделение!");
            return;
        }


        final GetCorrectPeriodsAction action = new GetCorrectPeriodsAction();
        action.setTaxType(taxType);
        final int departmentId = departmentPair.getDepartmentId();
        action.setDepartmentId(departmentId);
        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<GetCorrectPeriodsResult>() {
                            @Override
                            public void onSuccess(GetCorrectPeriodsResult result) {
                                openCorrectDialogPresenter.resetToDefault();
                                openCorrectDialogPresenter.setSelectedDepartment(departmentId);
                                openCorrectDialogPresenter.setPeriodsList(result.getReportPeriod());
                                openCorrectDialogPresenter.setTaxType(taxType);
                                addToPopupSlot(openCorrectDialogPresenter);
                            }
                        }, PeriodsPresenter.this)
        );
    }

    @Override
	public void onFindButton() {
        if ((getView().getDepartmentId() == null)
                || (getView().getFromYear() == null)
                || (getView().getToYear() == null)) {
            Dialog.errorMessage("Указание параметров поиска",
                    "Не заполнены следующие обязательные к заполнению поля: "
                    + (getView().getDepartmentId() == null ? "Подразделение " : "")
                    + (getView().getFromYear() == null ? "Период с " : "")
                    + (getView().getToYear() == null ? "Период по " : "")
            );
        } else if ((getView().getFromYear() == null)
				|| (getView().getToYear() == null)
				|| (getView().getFromYear() > getView().getToYear())){
			Dialog.errorMessage("Указание параметров поиска","Интервал периода поиска указан неверно!");
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
		Dialog.confirmMessage("Удаление периода", "Вы уверены, что хотите удалить период?",
				new DialogHandler() {
					@Override
					public void yes() {
						checkAndRemovePeriod();
					}
				}
				);
	}

	private void checkAndRemovePeriod() {
		CanRemovePeriodAction action = new CanRemovePeriodAction();
		action.setReportPeriodId((int)getView().getSelectedRow().getReportPeriodId());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<CanRemovePeriodResult>() {
					@Override
					public void onSuccess(CanRemovePeriodResult result) {
						if (result.isCanRemove()) {
							removeReportPeriod();
						} else {
                            LogAddEvent.fire(PeriodsPresenter.this, result.getUuid());
                            Dialog.errorMessage("Удаление периода", "Удаление периода невозможно!");
						}
					}
				}, PeriodsPresenter.this));
	}

	private void removeReportPeriod() {
		RemovePeriodAction requestData = new RemovePeriodAction();
		requestData.setReportPeriodId((int)getView().getSelectedRow().getReportPeriodId());
        requestData.setCorrectionDate(getView().getSelectedRow().getCorrectPeriod());
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
        TableRow selectedRow = getView().getSelectedRow();
        getView().setCanChangeDeadline(!selectedRow.isSubHeader() && selectedRow.isOpen());
        getView().setCanEditPeriod(!selectedRow.isSubHeader() && selectedRow.isOpen());
        List<TaxType> ITD = new ArrayList<TaxType>();
        ITD.add(TaxType.INCOME);
        ITD.add(TaxType.TRANSPORT);
        ITD.add(TaxType.DEAL);
        if (selectedRow != null && !selectedRow.isSubHeader() && !selectedRow.isOpen() && !selectedRow.isBalance() && selectedRow.getCorrectPeriod() == null &&
                (ITD.contains(taxType) && selectedRow.getPeriodName().equals("год") || !ITD.contains(taxType))) {
            getView().setCanOpenCorrectPeriod(true);
        } else {
            getView().setCanOpenCorrectPeriod(false);
        }
	}

    @Override
    public void editPeriod() {
        final EditDialogData initData = new EditDialogData();
        initData.setYear(getView().getSelectedRow().getYear());
        initData.setBalance(getView().getSelectedRow().isBalance());
        initData.setDepartmentId(getView().getDepartmentId().getDepartmentId());
        initData.setReportPeriodId((int)getView().getSelectedRow().getReportPeriodId());
        initData.setDictTaxPeriodId((long)getView().getSelectedRow().getDictTaxPeriodId());
        initData.setPeriodName(getView().getSelectedRow().getPeriodName());
        if (getView().getSelectedRow().getCorrectPeriod() == null) {
            editDialogPresenter.init(initData);
            addToPopupSlot(editDialogPresenter);
        } else {
            final GetCorrectPeriodsAction action = new GetCorrectPeriodsAction();
            action.setTaxType(taxType);
            DepartmentPair departmentPair = getView().getDepartmentId();
            final int departmentId = departmentPair.getDepartmentId();
            action.setDepartmentId(departmentId);
            dispatcher.execute(action, CallbackUtils
                            .defaultCallback(new AbstractCallback<GetCorrectPeriodsResult>() {
                                @Override
                                public void onSuccess(GetCorrectPeriodsResult result) {
                                    initData.setCorrectionReportPeriods(result.getReportPeriod());
                                    initData.setCorrectionDate(getView().getSelectedRow().getCorrectPeriod());
                                    editCorrectionDialogPresenter.init(initData);
                                    addToPopupSlot(editCorrectionDialogPresenter);
                                }
                            }, PeriodsPresenter.this)
            );

        }

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
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);

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
						getView().setCanEdit(result.isCanEdit());
						openDialogPresenter.setDepartments(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), true);
                        openDialogPresenter.setCanChangeDepartment(result.canChangeDepartment());
                        editDialogPresenter.setDepartments(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), true);
                        editDialogPresenter.setCanChangeDepartment(result.canChangeDepartment());
                        editDialogPresenter.setTaxType(result.getTaxType());
                        openCorrectDialogPresenter.setDepartments(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), true);
                        openCorrectDialogPresenter.setCanChangeDepartment(result.canChangeDepartment());

                        editCorrectionDialogPresenter.setDepartments(result.getDepartments(), result.getAvalDepartments(), Arrays.asList(result.getSelectedDepartment()), true);
                        editCorrectionDialogPresenter.setCanChangeDepartment(result.canChangeDepartment());
                        editCorrectionDialogPresenter.setTaxType(result.getTaxType());
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
