package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.AppointmentType;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentFormTypeShared;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
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

		void init(TaxType taxType, AppointmentType type, int year, List<PeriodInfo> periods,
                  boolean isForm, Integer selectedReceiverId, Integer selectedSourceId);
		void setDepartments(List<Department> departments, Set<Integer> availableDepartments);

		/**
		 * Обновляет на фрме таблицу с доступными для выбора типами НФ приемников (НФ назначениями) (левая)
		 *
		 * @param formTypes
		 * @param departmentFormTypes
		 */
		void setAvalibleFormReceivers(Map<Integer, FormType> formTypes,
				List<DepartmentFormType> departmentFormTypes);

		/**
		 * Обновляет на фрме таблицу с доступными для выбора типами деклараций приемников  (левая)
		 *
		 * @param declarationTypes
		 * @param departmentDeclarationTypes
		 */
		void setAvalibleDeclarationReceivers(Map<Integer, DeclarationType> declarationTypes,
				List<DepartmentDeclarationType> departmentDeclarationTypes);

		/**
		 * Обновляет на фрме таблицу с доступными для добавления источниками (которая справа)
		 *
		 * @param formTypes
		 * @param departmentFormTypes
		 */
		void setAvalibleSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);

		/**
		 * Обновляет на фрме таблицу с источниками для выбранного приемника (которая внизу)
		 *
		 * @param departmentFormTypes
		 */
		void setCurrentSources(List<DepartmentFormTypeShared> departmentFormTypes);

        PeriodInfo getPeriodFrom();
        PeriodInfo getPeriodTo();
        int getYearFrom();
        int getYearTo();

        Map<Integer, FormType> getSourcesFormTypes();

        Map<Integer, FormType> getReceiversFormTypes();

        Map<Integer, DeclarationType> getReceiversDeclarationTypes();
    }

	private final DispatchAsync dispatcher;

    private TaxType taxType;

    private boolean isForm = true;

	@Inject
	public SourcesPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
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
        isForm = Boolean.valueOf(request.getParameter("isForm", ""));

        action.setTaxType(taxType);

        // Выбранные подразделения
        String selectedReceiverStr = request.getParameter("dst", null);
        String selectedSourceStr = request.getParameter("src", null);
        final Integer selectedReceiverId = selectedReceiverStr == null ? null : Integer.valueOf(selectedReceiverStr);
        final Integer selectedSourceId = selectedSourceStr == null ? null : Integer.valueOf(selectedSourceStr);

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<InitSourcesResult>() {
                    @Override
                    public void onSuccess(InitSourcesResult result) {
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                        getView().init(taxType, AppointmentType.SOURCES, result.getYear(),
                                result.getPeriods(), isForm, selectedReceiverId, selectedSourceId);
                    }
                }, this).addCallback(new ManualRevealCallback<InitSourcesResult>(SourcesPresenter.this)));
    }

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void getFormSources(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvalibleSources(new HashMap<Integer, FormType>(0), new ArrayList<DepartmentFormType>(0));
            return;
        }

		GetFormDFTAction action = new GetFormDFTAction();
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormDFTResult>() {
					@Override
					public void onSuccess(GetFormDFTResult result) {
						getView().setAvalibleSources(result.getFormTypes(), result.getDepartmentFormTypes());
					}
				}, this));
	}

	@Override
	public void getFormReceivers(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvalibleFormReceivers(new HashMap<Integer, FormType>(0), new ArrayList<DepartmentFormType>(0));
            return;
        }

		GetFormDFTAction action = new GetFormDFTAction();
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormDFTResult>() {
					@Override
					public void onSuccess(GetFormDFTResult result) {
						getView().setAvalibleFormReceivers(result.getFormTypes(), result.getDepartmentFormTypes());
					}
				}, this));
	}

	@Override
	public void getFormReceiverSources(DepartmentFormType departmentFormType) {
		GetCurrentSourcesForFormAction action = new GetCurrentSourcesForFormAction();
		action.setDepartmentId(departmentFormType.getDepartmentId());
		action.setFormTypeId(departmentFormType.getFormTypeId());
		action.setKind(departmentFormType.getKind());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetCurrentSourcesResult>() {
					@Override
					public void onSuccess(GetCurrentSourcesResult result) {
						getView().setCurrentSources(result.getCurrentSources());
					}
				}, this));
	}

	@Override
	public void getDeclarationReceiverSources(DepartmentDeclarationType departmentDeclarationType) {
		GetCurrentSourcesForDeclaratonAction action = new GetCurrentSourcesForDeclaratonAction();
		action.setDepartmentId(departmentDeclarationType.getDepartmentId());
		action.setDeclarationTypeId(departmentDeclarationType.getDeclarationTypeId());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetCurrentSourcesResult>() {
					@Override
					public void onSuccess(GetCurrentSourcesResult result) {
						getView().setCurrentSources(result.getCurrentSources());
					}
				}, this));
	}

	@Override
	public void getDeclarationReceivers(Integer departmentId) {
        if (departmentId == null) {
            getView().setAvalibleDeclarationReceivers(new HashMap<Integer, DeclarationType>(0), new ArrayList<DepartmentDeclarationType>(0));
            return;
        }

		GetDeclarationDDTAction action = new GetDeclarationDDTAction();
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDeclarationDDTResult>() {
					@Override
					public void onSuccess(GetDeclarationDDTResult result) {
						getView().setAvalibleDeclarationReceivers(result.getDeclarationTypes(), result.getDeclarationReceivers());
					}
				}, this));
	}

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    @Override
	public void showAssignErrorMessage() {
        // TODO Заменить на http://jira.aplana.com/browse/SBRFACCTAX-5398 по готовности
        Dialog.warningMessage("Выбранное назначение налоговой формы уже является источником " +
                "для выбранного приемника!");
    }

	@Override
	public void updateFormSources(final DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds) {
        PeriodInfo periodFrom = getView().getPeriodFrom();
        PeriodInfo periodTo = getView().getPeriodTo();
        int yearFrom = getView().getYearFrom();
        int yearTo= getView().getYearTo();
        if (yearFrom > yearTo || (yearFrom == yearTo && periodFrom.getStartDate().after(periodTo.getStartDate()))) {
            Dialog.errorMessage("Создание назначения", "Интервал периода указан неверно!");
            return;
        }
		UpdateFormSourcesAction action = new UpdateFormSourcesAction();
		action.setDepartmentFormTypeId(departmentFormType.getId());
		action.setSourceDepartmentFormTypeIds(sourceDepartmentFormTypeIds);
        action.setPeriodFrom(periodFrom);
        action.setPeriodTo(periodTo);
        action.setYearFrom(yearFrom);
        action.setYearTo(yearTo);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<UpdateSourcesResult>() {
					@Override
					public void onSuccess(UpdateSourcesResult result) {
						getFormReceiverSources(departmentFormType);
					}
				}, this));
	}

	@Override
	public void updateDeclarationSources(final DepartmentDeclarationType departmentDeclarationType,
										 List<Long> sourceDepartmentFormTypeIds) {
		UpdateDeclarationSourcesAction action = new UpdateDeclarationSourcesAction();
		action.setDepartmentDeclarationTypeId(departmentDeclarationType.getId());
		action.setSourceDepartmentFormTypeIds(sourceDepartmentFormTypeIds);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<UpdateSourcesResult>() {
					@Override
					public void onSuccess(UpdateSourcesResult result) {
						getDeclarationReceiverSources(departmentDeclarationType);
					}
				}, this));
	}
}
