package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSaveEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateImpexUiHandlers;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;


public class FormTemplateImpexPresenter extends Presenter<FormTemplateImpexPresenter.MyView, FormTemplateImpexPresenter.MyProxy>
		implements FormTemplateImpexUiHandlers, FormTemplateSetEvent.MyHandler {

	private FormTemplate formTemplate;

	/**
	 * {@link FormTemplateImpexPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateImpexPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = AdminConstants.TabLabels.formTemplateImpexLabel,
			priority = AdminConstants.TabPriorities.formTemplateImpexPriority)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateImpexPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateImpexUiHandlers> {
		void setFormId(int formId);
	}

	@Inject
	public FormTemplateImpexPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().setFormId(formTemplate.getId());
	}

	@Override
	public void uploadFormTemplateSuccess() {
		MessageEvent.fire(this, "Форма импортирована");
		FormTemplateSaveEvent.fire(this);
	}

	@Override
	public void uploadFormTemplateFail(String msg) {
		MessageEvent.fire(this, "Не удалось импортировать шаблон. Ошибка: " + msg);
	}

	@Override
	public void downloadFormTemplate() {
		Window.open(GWT.getHostPageBaseURL() + "download/formTemplate/download/" + formTemplate.getId(), null, null);
	}
}
