package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentFormTypeShared;
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
import java.util.Map;
import java.util.Set;

public class SourcesPresenter extends Presenter<SourcesPresenter.MyView, SourcesPresenter.MyProxy>
		implements SourcesUiHandlers {

	@ProxyCodeSplit
	@NameToken(SourcesTokens.SOURCES)
	public interface MyProxy extends ProxyPlace<SourcesPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<SourcesUiHandlers> {
		
		void init(boolean isForm);
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

	}

	private final DispatchAsync dispatcher;

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

		// При инициализации формы получаем списки департаментов и 
		// чистим все данные на форме
		GetDepartmentsAction action = new GetDepartmentsAction();
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentsResult>() {
					@Override
					public void onSuccess(GetDepartmentsResult result) {
						getView().init(Boolean.valueOf(request.getParameter("isForm", "")));
						getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
					}
				}, this).addCallback(new ManualRevealCallback<GetDepartmentsResult>(SourcesPresenter.this)));
		
	}


	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void getFormSources(int departmentId, TaxType taxType) {
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
	public void getFormReceivers(int departmentId, TaxType taxType) {
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
	public void getDeclarationReceivers(int departmentId, TaxType taxType) {
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
	public void showAssignErrorMessage(boolean isForm) {
		if (isForm) {
			MessageEvent.fire(SourcesPresenter.this, "Налоговая форма уже назначена в качестве источника");
		} else {
			MessageEvent.fire(SourcesPresenter.this, "Источник уже назначен декларации");
		}
	}


	@Override
	public void updateFormSources(final DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds) {
		UpdateFormSourcesAction action = new UpdateFormSourcesAction();
		action.setDepartmentFormTypeId(departmentFormType.getId());
		action.setSourceDepartmentFormTypeIds(sourceDepartmentFormTypeIds);
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
