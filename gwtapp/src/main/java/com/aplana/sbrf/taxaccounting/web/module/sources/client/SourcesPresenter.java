package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.AssignDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.AssignDialogView;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.ButtonClickHandlers;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.*;

public class SourcesPresenter extends Presenter<SourcesPresenter.MyView, SourcesPresenter.MyProxy>
		implements SourcesUiHandlers{

	@ProxyCodeSplit
	@NameToken(SourcesTokens.SOURCES)
	public interface MyProxy extends ProxyPlace<SourcesPresenter>, Place {
	}

    public interface MyView extends View, HasUiHandlers<SourcesUiHandlers> {

        /**
         * Иницализация формы.
         * Запускается один раз при открытии.
         *
         * @param taxType тип налога
         * @param types доступные типы назначений
         * @param type текущий тип назначения
         * @param year года для периода периодов О_О
         * @param periods список достных периодов
         * @param isForm форма или декларация
         */
        void init(TaxType taxType, List<AppointmentType> types, AppointmentType type, int year, List<PeriodInfo> periods,
                  boolean isForm);

        /**
         * Полуыить выбранный интервал периодов
         * @return интервал периодов
         */
        PeriodsInterval getPeriodInterval();

        /**
         * Назначаются приемники?
         */
        boolean isSource();

        /**
         * Обработка деклараций
         */
        boolean isDeclaration();

        /**
         * Установка значений для комопонента выбора подразделения
         * @param departments список подразделений
         * @param availableDepartments доступные для выбора
         */
        void setDepartments(List<Department> departments, Set<Integer> availableDepartments);

        /**
         * Обновляет на фрме таблицу с доступными для выбора типами НФ (НФ назначениями) (левая)
         *
         * @param departmentFormTypes назначения НФ подразделениям
         */
        void setAvailableFormsLeft(List<DepartmentAssign> departmentFormTypes);

        /**
         * Обновляет на фрме таблицу с доступными для выбора типами НФ (НФ назначениями) (правая)
         *
         * @param departmentDeclarationTypes назначения деклараций подразделениям
         */
        void setAvailableDecsRight(List<DepartmentAssign> departmentDeclarationTypes);

        /**
         * Обновляет на фрме таблицу с доступными для выбора типами назначений деклараций подразделению (левая)
         *
         * @param departmentDeclarationTypes назначения деклараций подразделениям
         */
        void setAvailableDecsLeft(List<DepartmentAssign> departmentDeclarationTypes);

        /**
         * Обновляет на фрме таблицу с доступными для добавления приемнику/источнику (которая справа)
         *
         * @param departmentFormTypes назначения НФ подразделениям
         */
        void setAvailableFormRight(List<DepartmentAssign> departmentFormTypes);

        /**
         * Обновляет на фрме таблицу с источниками для выбранного приемника/источника (которая внизу)
         *
         * @param departmentFormTypes назначения НФ подразделениям
         */
        void setCurrentSources(List<CurrentAssign> departmentFormTypes);
    }

	private final DispatchAsync dispatcher;

    private TaxType taxType;

    private boolean isForm = true;

    protected final AssignDialogPresenter assignDialogPresenter;

	@Inject
	public SourcesPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher,
                            AssignDialogPresenter assignDialogPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
        this.assignDialogPresenter = assignDialogPresenter;
		getView().setUiHandlers(this);
	}

    @Override
    public void openAssignDialog(AssignDialogView.State state, PeriodsInterval pi, ButtonClickHandlers handlers){
        assignDialogPresenter.open(state, pi, handlers);
    }

    @Override
    public void closeAssignDialog(){
        assignDialogPresenter.close();
    }

	/**
	 * @param request запрос
	 */
	@Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        // При инициализации формы получаем списки департаментов
        InitSourcesAction action = new InitSourcesAction();

        taxType = TaxType.valueOf(request.getParameter("nType", ""));
        isForm = Boolean.valueOf(request.getParameter("isForm", "true"));

        action.setTaxType(taxType);

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<InitSourcesResult>() {
                    @Override
                    public void onSuccess(InitSourcesResult result) {
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                        getView().init(taxType, Arrays.asList(AppointmentType.values()), AppointmentType.SOURCES, result.getYear(),
                                result.getPeriods(), isForm);
                        assignDialogPresenter.setAvailablePeriods(result.getPeriods());
                    }
                }, this).addCallback(new ManualRevealCallback<InitSourcesResult>(SourcesPresenter.this)));
    }

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void getFormsRight(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvailableFormRight(new ArrayList<DepartmentAssign>(0));
            return;
        }

		GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(true);
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
					@Override
					public void onSuccess(GetDepartmentAssignsResult result) {
						getView().setAvailableFormRight(result.getDepartmentAssigns());
					}
				}, this));
	}

	@Override
	public void getFormsLeft(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvailableFormsLeft(new ArrayList<DepartmentAssign>(0));
            return;
        }

		GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(true);
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
					@Override
					public void onSuccess(GetDepartmentAssignsResult result) {
						getView().setAvailableFormsLeft(result.getDepartmentAssigns());
					}
				}, this));
	}

    @Override
    public void getDecsLeft(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvailableDecsLeft(new ArrayList<DepartmentAssign>(0));
            return;
        }

        GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(false);
        action.setDepartmentId(departmentId);
        action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
                    @Override
                    public void onSuccess(GetDepartmentAssignsResult result) {
                        getView().setAvailableDecsLeft(result.getDepartmentAssigns());
                    }
                }, this));
    }

    @Override
    public void getDecsRight(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvailableDecsRight(new ArrayList<DepartmentAssign>(0));
            return;
        }

        GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(false);
        action.setDepartmentId(departmentId);
        action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
                    @Override
                    public void onSuccess(GetDepartmentAssignsResult result) {
                        getView().setAvailableDecsRight(result.getDepartmentAssigns());
                    }
                }, this));
    }

	@Override
	public void getCurrentAssigns(DepartmentAssign departmentAssign) {
        GetCurrentAssignsAction action = new GetCurrentAssignsAction();
        action.setDepartmentId(departmentAssign.getDepartmentId());
        action.setTypeId(departmentAssign.getTypeId());
        action.setDeclaration(getView().isDeclaration());
        action.setKind(departmentAssign.getKind());
        action.setPeriodsInterval(getView().getPeriodInterval());

		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetCurrentAssignsResult>() {
					@Override
					public void onSuccess(GetCurrentAssignsResult result) {
						getView().setCurrentSources(result.getCurrentSources());
					}
				}, this));
	}

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    @Override
    public void deleteCurrentAssign(final DepartmentAssign departmentAssign, Set<CurrentAssign> currentAssigns) {
        DeleteCurrentAssignsAction action = new DeleteCurrentAssignsAction();
        action.setDeclaration(getView().isDeclaration());
        action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
        action.setPeriodsInterval(getView().getPeriodInterval());
        action.setCurrentAssigns(currentAssigns);
        action.setDepartmentAssign(departmentAssign);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<DeleteCurrentAssignsResult>() {
                    @Override
                    public void onSuccess(DeleteCurrentAssignsResult result) {
                        getCurrentAssigns(departmentAssign);
                        LogCleanEvent.fire(SourcesPresenter.this);
                        LogAddEvent.fire(SourcesPresenter.this, result.getUuid());
                    }
                }, this));
    }

    @Override
    public void prepareUpdateAssign(final DepartmentAssign departmentAssign, final CurrentAssign currentAssign) {
        GetPeriodIntervalAction action = new GetPeriodIntervalAction();
        action.setPeriodStart(currentAssign.getStartDateAssign());
        action.setPeriodEnd(currentAssign.getEndDateAssign());
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetPeriodIntervalResult>() {
                    @Override
                    public void onSuccess(final GetPeriodIntervalResult result) {
                        openAssignDialog(AssignDialogView.State.UPDATE, result.getPeriodsInterval(), new ButtonClickHandlers() {
                            @Override
                            public void ok(PeriodsInterval periodsInterval) {
                                updateCurrentAssign(departmentAssign, currentAssign, periodsInterval);
                            }

                            @Override
                            public void cancel() {
                                closeAssignDialog();
                            }
                        });
                    }
                }, this));
    }

    @Override
    public void createAssign(final DepartmentAssign leftObject, Set<DepartmentAssign> rightSelectedObjects, PeriodsInterval periodInterval, List<Integer> leftDepartment, List<Integer> rightDepartment) {
        if (checkInterval(periodInterval)) {
            if (leftDepartment == null || leftDepartment.isEmpty()
                    || rightDepartment == null || rightDepartment.isEmpty()) {
                Dialog.errorMessage("Создание назначения", "Подразделение не выбрано!");
                return;
            }
            CreateAssignAction action = new CreateAssignAction();
            action.setDeclaration(getView().isDeclaration());
            action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
            action.setPeriodsInterval(periodInterval);
            action.setLeftObject(leftObject);
            action.setRightSelectedObjects(rightSelectedObjects);
            action.setLeftDepartmentId(leftDepartment.get(0));
            action.setRightDepartmentId(rightDepartment.get(0));
            action.setTaxType(taxType);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<CreateAssignResult>() {
                        @Override
                        public void onSuccess(CreateAssignResult result) {
                            getCurrentAssigns(leftObject);
                            LogCleanEvent.fire(SourcesPresenter.this);
                            LogAddEvent.fire(SourcesPresenter.this, result.getUuid());
                        }
                    }, this));
        }
    }

    @Override
	public void updateCurrentAssign(final DepartmentAssign departmentAssign, CurrentAssign currentAssign, PeriodsInterval periodInterval) {
        if (checkInterval(periodInterval)) {
            UpdateCurrentAssignsAction action = new UpdateCurrentAssignsAction();
            action.setDeclaration(getView().isDeclaration());
            action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
            action.setNewPeriodsInterval(periodInterval);
            action.setCurrentAssign(currentAssign);
            action.setDepartmentAssign(departmentAssign);
            action.setOldDateFrom(currentAssign.getStartDateAssign());
            action.setOldDateTo(currentAssign.getEndDateAssign());
            action.setTaxType(taxType);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<UpdateCurrentAssignsResult>() {
                        @Override
                        public void onSuccess(UpdateCurrentAssignsResult result) {
                            getCurrentAssigns(departmentAssign);
                            LogCleanEvent.fire(SourcesPresenter.this);
                            LogAddEvent.fire(SourcesPresenter.this, result.getUuid());
                        }
                    }, this));
        }
	}

    private boolean checkInterval(PeriodsInterval periodInterval) {
        PeriodInfo periodFrom = periodInterval.getPeriodFrom();
        PeriodInfo periodTo = periodInterval.getPeriodTo();
        int yearFrom = periodInterval.getYearFrom();
        int yearTo= periodInterval.getYearTo();
        if (yearFrom > yearTo || (yearFrom == yearTo && periodFrom.getStartDate().after(periodTo.getStartDate()))) {
            Dialog.errorMessage("Создание назначения", "Интервал периода указан неверно!");
            return false;
        }
        return true;
    }
}
