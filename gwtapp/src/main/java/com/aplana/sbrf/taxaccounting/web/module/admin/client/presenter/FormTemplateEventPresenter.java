package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateCloseEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateResetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSaveEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateEventUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUtil.saveFormTemplate;

public class FormTemplateEventPresenter extends Presenter<FormTemplateEventPresenter.MyView, FormTemplateEventPresenter.MyProxy>
		implements FormTemplateEventUiHandlers, FormTemplateResetEvent.MyHandler, FormTemplateSaveEvent.MyHandler,
		FormTemplateCloseEvent.MyHandler {

	/**
	 * {@link FormTemplateEventPresenter}'s proxy.
	 */
	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateEventPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = "События",
			priority = 1) // The second tab in the home tab
	public interface MyProxy extends TabContentProxyPlace<FormTemplateEventPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateEventUiHandlers> {
		public void selectEvent();
	}

	private DispatchAsync dispatcher;
	private FormTemplate formTemplate;
	private int formId;
	private boolean isSelected = false;

	@Inject
	public FormTemplateEventPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, final DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onReset(FormTemplateResetEvent event) {
		selectEvent();
	}

	@ProxyEvent
	@Override
	public void onSave(FormTemplateSaveEvent event) {
		if (isSelected) {
			saveFormTemplate(this, formTemplate, dispatcher);
		}
	}

	@ProxyEvent
	@Override
	public void onClose(FormTemplateCloseEvent event) {
		selectEvent();
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		formId = Integer.valueOf(request.getParameter(AdminNameTokens.formTemplateId, "0"));
		selectEvent();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
	}

	@Override
	protected void onReveal() {
		isSelected = true;
	}

	@Override
	protected void onHide() {
		isSelected = false;
	}

	@Override
	public void removeEventScript(FormDataEvent event, Script script) {
		formTemplate.removeEventScript(event, script);
		getView().selectEvent();
	}

	@Override
	public void addEventScript(FormDataEvent event, Script script) {
		formTemplate.addEventScript(event, script);
		getView().selectEvent();
	}

	@Override
	public List<Script> getScriptsByEvent(FormDataEvent event) {
		return formTemplate.getScriptsByEvent(event);
	}

	@Override
	public List<Script> getScripts() {
		return formTemplate.getScripts();
	}

	private void selectEvent() {
		if (formId != 0) {
			GetFormAction action = new GetFormAction();
			action.setId(formId);
			dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
				@Override
				public void onReqSuccess(GetFormResult result) {
					formTemplate = result.getForm();
					getView().selectEvent();
				}
			});
		}
	}
}
