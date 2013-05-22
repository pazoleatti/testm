package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateScriptUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;


public class FormTemplateScriptPresenter extends Presenter<FormTemplateScriptPresenter.MyView, FormTemplateScriptPresenter.MyProxy>
		implements FormTemplateScriptUiHandlers, FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler {
	/**
	 * {@link FormTemplateMainPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateScriptPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = AdminConstants.TabLabels.formTemplateScriptLabel,
			priority = AdminConstants.TabPriorities.formTemplateScriptPriority)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateScriptPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateScriptUiHandlers>{
		void bindScripts(List<Script> scriptList, boolean isFormChanged);
		void flush();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateScriptPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(FormTemplateFlushEvent.getType(), this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		boolean isFormChanged = true;
		if (formTemplate != null) {
			isFormChanged = !formTemplate.getId().equals(event.getFormTemplate().getId());
		}
		formTemplate = event.getFormTemplate();
		getView().bindScripts(formTemplate.getScripts(), isFormChanged);
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().flush();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
		
		// TODO: [sgoryachkin] 
		// 1) В перегрузке этого метода нет необходимости
		// 2) В этом методе не должно быть логики  (для этого есть события - onReveal)

		if(formTemplate != null) {
			getView().bindScripts(formTemplate.getScripts(), false);
		}
	}

	@Override
	public void createScript() {
		Script script = new Script();
		script.setName("Новый");
		formTemplate.addScript(script);
		getView().bindScripts(formTemplate.getScripts(), false);
	}

	@Override
	public void deleteScript(Script selectedScript) {
		formTemplate.removeScript(selectedScript);
		getView().bindScripts(formTemplate.getScripts(), false);
	}
}