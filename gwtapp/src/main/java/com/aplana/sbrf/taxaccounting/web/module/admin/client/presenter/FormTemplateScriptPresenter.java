package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateScriptUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;

public class FormTemplateScriptPresenter extends Presenter<FormTemplateScriptPresenter.MyView, FormTemplateScriptPresenter.MyProxy> implements FormTemplateScriptUiHandlers {
	/**
	 * {@link FormTemplateMainPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateScriptPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = "Скрипты",
			priority = 0)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateScriptPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateScriptUiHandlers>{
		void bindScripts(List<Script> scriptList);
	}

	public static final String PARAM_FORM_TEMPLATE_SCRIPT_ID = "formTemplateScriptId";
	private int formId;
	private DispatchAsync dispatcher;
	private FormTemplate formTemplate;

	@Inject
	public FormTemplateScriptPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		formId = Integer.valueOf(request.getParameter(PARAM_FORM_TEMPLATE_SCRIPT_ID, "0"));
		GetFormAction action = new GetFormAction();
		action.setId(formId);
		dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
			@Override
			public void onReqSuccess(GetFormResult result) {
				formTemplate = result.getForm();
				getView().bindScripts(formTemplate.getScripts());
			}
		});
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
	}

	@Override
	public void createScript() {
		Script script = new Script();
		script.setName("Новый");
		formTemplate.addScript(script);
		getView().bindScripts(formTemplate.getScripts());
	}

	@Override
	public void deleteScript(Script selectedScript) {
		formTemplate.removeScript(selectedScript);
		getView().bindScripts(formTemplate.getScripts());
	}
}