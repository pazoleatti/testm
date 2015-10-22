package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.SourcesSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
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

    public interface MyView extends View, HasUiHandlers<SourcesUiHandlers>, ViewWithSortableTable {

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
        void setDepartments(List<Department> departments, Set<Integer> availableDepartments, Integer departmentId);

        /**
         * Обновляет на форме таблицу с доступными для выбора типами НФ (НФ назначениями) (левая)
         *
         * @param departmentFormTypes назначения НФ подразделениям
         * @param selectedLeftRecord
         */
        void setAvailableFormsLeft(List<DepartmentAssign> departmentFormTypes, DepartmentAssign selectedLeftRecord);

        /**
         * Обновляет на форме таблицу с доступными для выбора типами НФ (НФ назначениями) (правая)
         *
         * @param departmentDeclarationTypes назначения деклараций подразделениям
         */
        void setAvailableDecsRight(List<DepartmentAssign> departmentDeclarationTypes);

        /**
         * Обновляет на форме таблицу с доступными для выбора типами назначений деклараций подразделению (левая)
         *
         * @param departmentDeclarationTypes назначения деклараций подразделениям
         * @param selectedLeftRecord
         */
        void setAvailableDecsLeft(List<DepartmentAssign> departmentDeclarationTypes, DepartmentAssign selectedLeftRecord);

        /**
         * Обновляет на форме таблицу с доступными для добавления приемнику/источнику (которая справа)
         *
         * @param departmentFormTypes назначения НФ подразделениям
         */
        void setAvailableFormRight(List<DepartmentAssign> departmentFormTypes);

        /**
         * Обновляет на форме таблицу с источниками для выбранного приемника/источника (которая внизу)
         *
         * @param departmentFormTypes назначения НФ подразделениям
         */
        void setCurrentSources(List<CurrentAssign> departmentFormTypes);

        /**
         * Получить столбец, по которому сортировать левую таблицу
         * @return
         */
        SourcesSearchOrdering getSearchOrderingLeftTable();

        /**
         * Получить столбец, по которому сортировать правую таблицу
         * @return
         */
        SourcesSearchOrdering getSearchOrderingRightTable();

        /**
         * Получить столбец, по которому сортировать нижнюю таблицу
         * @return
         */
        SourcesSearchOrdering getSearchOrderingDownTable();

        boolean isAscSorting(SourcesView.Table table);

        SourcesView.Table getTable();

        void loadLeftData(DepartmentAssign leftSelectedRecord);

        void loadRightData();

        void setPanelWidth();

        void clearSelection();
    }

	private final DispatchAsync dispatcher;

    private TaxType taxType;

    private boolean isForm = true;

    protected final AssignDialogPresenter assignDialogPresenter;

    private DepartmentAssign departmentAssign;

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
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);

        // При инициализации формы получаем списки департаментов
        InitSourcesAction action = new InitSourcesAction();

        taxType = TaxType.valueOf(request.getParameter("nType", ""));
        isForm = Boolean.valueOf(request.getParameter("isForm", "true"));

        action.setTaxType(taxType);

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<InitSourcesResult>() {
                    @Override
                    public void onSuccess(InitSourcesResult result) {
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments(), result.getDefaultDepartment());
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
	public void getFormsRight(Integer departmentId, DepartmentAssign selectedLeft) {
        if (departmentId == null) {
            getView().setAvailableFormRight(new ArrayList<DepartmentAssign>(0));
            return;
        }

		GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(true);
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
        action.setSelectedLeft(selectedLeft);
        action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
        action.setOrdering(getView().getSearchOrderingRightTable());
        action.setAscSorting(getView().isAscSorting(SourcesView.Table.RIGHT));
        dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
					@Override
					public void onSuccess(GetDepartmentAssignsResult result) {
						getView().setAvailableFormRight(result.getDepartmentAssigns());
					}
				}, this));
	}

	@Override
	public void getFormsLeft(Integer departmentId, final DepartmentAssign selectedLeftRecord) {
        if (departmentId == null) {
            getView().setAvailableFormsLeft(new ArrayList<DepartmentAssign>(0), selectedLeftRecord);
            return;
        }

		GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(true);
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
        action.setOrdering(getView().getSearchOrderingLeftTable());
        action.setAscSorting(getView().isAscSorting(SourcesView.Table.LEFT));
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
					@Override
					public void onSuccess(GetDepartmentAssignsResult result) {
						getView().setAvailableFormsLeft(result.getDepartmentAssigns(), selectedLeftRecord);
					}
				}, this));
	}

    @Override
    public void getDecsLeft(Integer departmentId, final DepartmentAssign selectedLeftRecord) {
        if (departmentId == null) {
            getView().setAvailableDecsLeft(new ArrayList<DepartmentAssign>(0), selectedLeftRecord);
            return;
        }

        GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(false);
        action.setDepartmentId(departmentId);
        action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
        action.setOrdering(getView().getSearchOrderingLeftTable());
        action.setAscSorting(getView().isAscSorting(SourcesView.Table.LEFT));
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentAssignsResult>() {
                    @Override
                    public void onSuccess(GetDepartmentAssignsResult result) {
                        getView().setAvailableDecsLeft(result.getDepartmentAssigns(), selectedLeftRecord);
                    }
                }, this));
    }

    @Override
    public void getDecsRight(Integer departmentId, DepartmentAssign selectedLeft) {
        if (departmentId == null) {
            getView().setAvailableDecsRight(new ArrayList<DepartmentAssign>(0));
            return;
        }

        GetDepartmentAssignsAction action = new GetDepartmentAssignsAction();
        action.setForm(false);
        action.setDepartmentId(departmentId);
        action.setTaxType(taxType);
        action.setPeriodsInterval(getView().getPeriodInterval());
        action.setSelectedLeft(selectedLeft);
        action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
        action.setOrdering(getView().getSearchOrderingRightTable());
        action.setAscSorting(getView().isAscSorting(SourcesView.Table.RIGHT));
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

        this.departmentAssign = departmentAssign;
        GetCurrentAssignsAction action = new GetCurrentAssignsAction();
        action.setDepartmentId(departmentAssign.getDepartmentId());
        action.setTypeId(departmentAssign.getTypeId());
        action.setDeclaration(getView().isDeclaration());
        action.setKind(departmentAssign.getKind());
        action.setPeriodsInterval(getView().getPeriodInterval());
        action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
        action.setOrdering(getView().getSearchOrderingDownTable());
        action.setAscSorting(getView().isAscSorting(SourcesView.Table.DOWN));

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
        action.setTaxType(getTaxType());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<DeleteCurrentAssignsResult>() {
                    @Override
                    public void onSuccess(DeleteCurrentAssignsResult result) {
                        getCurrentAssigns(departmentAssign);
                        getView().loadRightData();
                        LogCleanEvent.fire(SourcesPresenter.this);
                        LogAddEvent.fire(SourcesPresenter.this, result.getUuid());
                    }
                }, this));
    }

    @Override
    public void prepareUpdateAssign(final DepartmentAssign departmentAssign, final Set<CurrentAssign> currentAssigns) {
        GetPeriodIntervalAction action = new GetPeriodIntervalAction();
        action.setCurrentAssigns(currentAssigns);
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetPeriodIntervalResult>() {
                    @Override
                    public void onSuccess(final GetPeriodIntervalResult result) {
                        openAssignDialog(AssignDialogView.State.UPDATE, result.getPeriodsInterval(), new ButtonClickHandlers() {
                            @Override
                            public void ok(PeriodsInterval periodsInterval) {
                                updateCurrentAssign(departmentAssign, currentAssigns,
                                        result.getPeriodsInterval(), result.getPeriodsIntervals());
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
                            getView().clearSelection();
                            getCurrentAssigns(leftObject);
                            getView().loadRightData();
                            LogCleanEvent.fire(SourcesPresenter.this);
                            LogAddEvent.fire(SourcesPresenter.this, result.getUuid());
                        }
                    }, this));
        }
    }

    @Override
	public void updateCurrentAssign(final DepartmentAssign departmentAssign, Set<CurrentAssign> currentAssigns, PeriodsInterval periodsInterval, Map<CurrentAssign, PeriodsInterval> periodIntervals) {
        if (checkInterval(periodsInterval)) {
            UpdateCurrentAssignsAction action = new UpdateCurrentAssignsAction();
            action.setDeclaration(getView().isDeclaration());
            action.setMode(getView().isSource() ? SourceMode.SOURCES : SourceMode.DESTINATIONS);
            action.setNewPeriodsInterval(periodsInterval);
            action.setCurrentAssigns(currentAssigns);
            action.setDepartmentAssign(departmentAssign);
            action.setTaxType(taxType);
            action.setLeftDepartmentId(departmentAssign.getDepartmentId());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<UpdateCurrentAssignsResult>() {
                        @Override
                        public void onSuccess(UpdateCurrentAssignsResult result) {
                            getCurrentAssigns(departmentAssign);
                            getView().loadRightData();
                            LogCleanEvent.fire(SourcesPresenter.this);
                            LogAddEvent.fire(SourcesPresenter.this, result.getUuid());
                        }
                    }, this));
        }
	}

    @Override
    public void onRangeChange(int start, int length) {
        SourcesView.Table table = getView().getTable();

        switch (table) {
            case LEFT:
                getView().loadLeftData(null);
                break;
            case RIGHT:
                getView().loadRightData();
                break;
            case DOWN:
                getCurrentAssigns(departmentAssign);
                break;
        }
    }

    private boolean checkInterval(PeriodsInterval periodInterval) {
        PeriodInfo periodFrom = periodInterval.getPeriodFrom();
        PeriodInfo periodTo = periodInterval.getPeriodTo();
        int yearFrom = periodInterval.getYearFrom();
        if (periodInterval.getYearTo() != null) {
            int yearTo = periodInterval.getYearTo();
            if (yearFrom > yearTo || (yearFrom == yearTo && periodFrom.getStartDate().after(periodTo.getStartDate()))) {
                Dialog.errorMessage("Создание назначения", "Неверно задан период!");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().setPanelWidth();
    }
}
