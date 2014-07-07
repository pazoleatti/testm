package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSaveEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateImpexUiHandlers;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;


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
        static final String ERROR_RESP = "erroruuid ";
        static final String SUCCESS_RESP = "uuid ";
        static final String ERROR = "error ";

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
		formTemplate = event.getFormTemplateExt().getFormTemplate();
		getView().setFormId(formTemplate.getId() != null?formTemplate.getId():0);
	}

    @Override
    public void uploadFormTemplateSuccess() {
        MessageEvent.fire(FormTemplateImpexPresenter.this, "Форма сохранена");
        FormTemplateSaveEvent.fire(this);
    }

    @Override
	public void uploadFormTemplateSuccess(String uuid) {
        if (uuid != null && !uuid.isEmpty() && !uuid.toLowerCase().equals("<pre></pre>")){
            LogAddEvent.fire(this, uuid);
        }else {
            Dialog.infoMessage("Макет загружен");
        }
		FormTemplateSaveEvent.fire(this);
	}

	@Override
	public void downloadFormTemplate() {
		Window.open(GWT.getHostPageBaseURL() + "download/formTemplate/download/" + formTemplate.getId(), null, null);
	}

    @Override
    public void uploadDectResponseWithErrorUuid(String uuid) {
        LogAddEvent.fire(this, uuid);
        uploadFormTemplateFail();
    }

    @Override
    public void uploadFormTemplateFail() {
        Dialog.errorMessage("Загрузить макет не удалось. Проверьте источник данных");
    }
}
