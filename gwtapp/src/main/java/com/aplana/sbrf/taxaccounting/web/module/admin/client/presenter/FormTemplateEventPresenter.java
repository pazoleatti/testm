package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateEventUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

public class FormTemplateEventPresenter extends Presenter<FormTemplateEventPresenter.MyView, FormTemplateEventPresenter.MyProxy>
		implements FormTemplateEventUiHandlers, FormTemplateSetEvent.MyHandler{

	/**
	 * {@link FormTemplateEventPresenter}'s proxy.
	 */
	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateEventPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = "События",
			priority = 1) // The 2 tab
	public interface MyProxy extends TabContentProxyPlace<FormTemplateEventPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateEventUiHandlers> {
		public void selectEvent();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateEventPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().selectEvent();
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);

		if (formTemplate != null) {
			getView().selectEvent();
		}
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
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
