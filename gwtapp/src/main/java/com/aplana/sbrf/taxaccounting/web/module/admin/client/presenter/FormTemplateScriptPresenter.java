package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateScriptUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;


public class FormTemplateScriptPresenter extends Presenter<FormTemplateScriptPresenter.MyView, FormTemplateScriptPresenter.MyProxy>
		implements FormTemplateScriptUiHandlers, FormTemplateSetEvent.MyHandler {

	/**
	 * {@link FormTemplateMainPresenter}'s proxy.
	 */
	@Title("Администрирование")
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

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateScriptPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		FormTemplateMainPresenter source = (FormTemplateMainPresenter) event.getSource();
		formTemplate = source.getFormTemplate();
		getView().bindScripts(formTemplate.getScripts());
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
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