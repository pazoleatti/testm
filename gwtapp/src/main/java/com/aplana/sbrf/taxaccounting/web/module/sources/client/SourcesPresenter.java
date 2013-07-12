package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
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
import java.util.Map;

public class SourcesPresenter extends Presenter<SourcesPresenter.MyView, SourcesPresenter.MyProxy>
		implements SourcesUiHandlers {

	@ProxyCodeSplit
	@NameToken(SourcesTokens.sources)
	public interface MyProxy extends ProxyPlace<SourcesPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<SourcesUiHandlers> {
		void setFormSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);
		void setFormReceivers(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);
		void setFormReceiverSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes);
		void setDepartments(List<Department> departments);
	}

	private final DispatchAsync dispatcher;
	private HandlerRegistration closeDeclarationTemplateHandlerRegistration;

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
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);

		closeDeclarationTemplateHandlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(Window.ClosingEvent event) {
				closeDeclarationTemplateHandlerRegistration.removeHandler();
			}
		});

		setDepartments();
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void onHide() {
		super.onHide();
		unlock();
		closeDeclarationTemplateHandlerRegistration.removeHandler();
	}

	/**
	 * updateFormSources
	 */
	@Override
	public void updateFormSources(final DepartmentFormType departmentFormType, List<Long> sourceDepartmentFormTypeIds) {
		UpdateSourcesAction action = new UpdateSourcesAction();
		action.setDepartmentFormTypeId(departmentFormType.getId());
		action.setSourceDepartmentFormTypeIds(sourceDepartmentFormTypeIds);
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<UpdateSourcesResult>() {
					@Override
					public void onSuccess(UpdateSourcesResult result) {
						MessageEvent.fire(SourcesPresenter.this, "Источник назначен");
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
				}, this).addCallback(new ManualRevealCallback<GetFormSourcesResult>(SourcesPresenter.this)));
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
				}, this).addCallback(new ManualRevealCallback<GetFormReceiversResult>(SourcesPresenter.this)));
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
				}, this).addCallback(new ManualRevealCallback<GetFormReceiverSourcesResult>(SourcesPresenter.this)));
	}

	private void setDepartments() {
		GetDepartmentsAction action = new GetDepartmentsAction();
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetDepartmentsResult>() {
					@Override
					public void onSuccess(GetDepartmentsResult result) {
						getView().setDepartments(result.getDepartments());
					}
				}, this).addCallback(new ManualRevealCallback<GetDepartmentsResult>(SourcesPresenter.this)));
	}

	private void unlock(){
		UnlockSourcesAction action = new UnlockSourcesAction();
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}


}
