package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;


import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateEventUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class FormTemplateEventPresenter extends Presenter<FormTemplateEventPresenter.MyView, FormTemplateEventPresenter.MyProxy>
		implements FormTemplateEventUiHandlers, FormTemplateSetEvent.MyHandler{

	/**
	 * {@link FormTemplateEventPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateEventPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = AdminConstants.TabLabels.formTemplateEventLabel,
			priority = AdminConstants.TabPriorities.formTemplateEventPriority)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateEventPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateEventUiHandlers> {
		public void selectEvent();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateEventPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().selectEvent();
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
}
