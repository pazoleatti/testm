package com.aplana.sbrf.taxaccounting.web.module.formsources.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.formsources.shared.*;
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

public class FormSourcesPresenter extends Presenter<FormSourcesPresenter.MyView, FormSourcesPresenter.MyProxy>
		implements FormSourcesUiHandlers {

	@ProxyCodeSplit
	@NameToken(FormSourcesTokens.sources)
	public interface MyProxy extends ProxyPlace<FormSourcesPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<FormSourcesUiHandlers> {
		void setFormSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);
		void setFormReceivers(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);
		void setFormReceiverSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);
		void setDepartments(List<Department> departments);
	}

	private final DispatchAsync dispatcher;

	@Inject
	public FormSourcesPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	/**
	 * @param request запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		setDepartments();
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	/**
	 * updateFormSources
	 */
	@Override
	public void updateFormSources(final DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds) {
		UpdateFormSourcesAction action = new UpdateFormSourcesAction();
		action.setDepartmentFormTypeId(departmentFormType.getId());
		action.setSourceDepartmentFormTypeIds(sourceDepartmentFormTypeIds);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<UpdateFormSourcesResult>() {
					@Override
					public void onSuccess(UpdateFormSourcesResult result) {
						MessageEvent.fire(FormSourcesPresenter.this, "Источники налоговой формы сохранены");
						setFormReceiverSources(departmentFormType);
					}
				}, this));
	}

	@Override
	public void setFormSources(int departmentId, TaxType taxType) {
		GetFormSourcesAction action = new GetFormSourcesAction();
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormSourcesResult>() {
					@Override
					public void onSuccess(GetFormSourcesResult result) {
						getView().setFormSources(result.getFormTypes(), result.getFormSources());
					}
				}, this).addCallback(new ManualRevealCallback<GetFormSourcesResult>(FormSourcesPresenter.this)));
	}

	@Override
	public void setFormReceivers(int departmentId, TaxType taxType) {
		GetFormReceiversAction action = new GetFormReceiversAction();
		action.setDepartmentId(departmentId);
		action.setTaxType(taxType);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormReceiversResult>() {
					@Override
					public void onSuccess(GetFormReceiversResult result) {
						getView().setFormReceivers(result.getFormTypes(), result.getFormReceivers());
					}
				}, this).addCallback(new ManualRevealCallback<GetFormReceiversResult>(FormSourcesPresenter.this)));
	}

	@Override
	public void setFormReceiverSources(DepartmentFormType departmentFormType) {
		GetFormReceiverSourcesAction action = new GetFormReceiverSourcesAction();
		action.setDepartmentId(departmentFormType.getDepartmentId());
		action.setFormTypeId(departmentFormType.getFormTypeId());
		action.setKind(departmentFormType.getKind());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormReceiverSourcesResult>() {
					@Override
					public void onSuccess(GetFormReceiverSourcesResult result) {
						getView().setFormReceiverSources(result.getFormTypes(), result.getFormReceiverSources());
					}
				}, this).addCallback(new ManualRevealCallback<GetFormReceiverSourcesResult>(FormSourcesPresenter.this)));
	}

	private void setDepartments() {
		GetDepartmentsAction action = new GetDepartmentsAction();
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentsResult>() {
					@Override
					public void onSuccess(GetDepartmentsResult result) {
						getView().setDepartments(result.getDepartments());
					}
				}, this).addCallback(new ManualRevealCallback<GetDepartmentsResult>(FormSourcesPresenter.this)));
	}

}
