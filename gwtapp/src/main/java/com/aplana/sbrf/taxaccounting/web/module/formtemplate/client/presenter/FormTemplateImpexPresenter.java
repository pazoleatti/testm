package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSaveEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.UpdateFTIdEvent;
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
		implements FormTemplateImpexUiHandlers, UpdateFTIdEvent.MyHandler {

	private int ftId;

    @ProxyEvent
    @Override
    public void onUpdateId(UpdateFTIdEvent event) {
        ftId = event.getFtId();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().setFormId(ftId);
    }

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
        String ERROR_RESP = "erroruuid ";
        String SUCCESS_RESP = "uuid ";
        String ERROR = "error ";

		void setFormId(int formId);
	}

    private FormTemplateMainPresenter formTemplateMainPresenter;

	@Inject
	public FormTemplateImpexPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, FormTemplateMainPresenter formTemplateMainPresenter) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
        getView().setFormId(ftId);
        this.formTemplateMainPresenter = formTemplateMainPresenter;
	}

    @Override
    public void uploadFormTemplateSuccess() {
        MessageEvent.fire(FormTemplateImpexPresenter.this, "Форма сохранена");
        FormTemplateSaveEvent.fire(this);
    }

    @Override
	public void uploadFormTemplateSuccess(String uuid) {
        if (uuid != null && !uuid.isEmpty() && !uuid.equalsIgnoreCase("<pre></pre>") && !uuid.equalsIgnoreCase("null")){
            LogAddEvent.fire(this, uuid);
        }else {
            Dialog.infoMessage("Макет загружен");
        }
		FormTemplateSaveEvent.fire(this);
	}

	@Override
	public void downloadFormTemplate() {
		Window.open(GWT.getHostPageBaseURL() + "download/formTemplate/download/" + ftId, null, null);
	}

    @Override
    public void uploadDectResponseWithErrorUuid(String uuid) {
        LogAddEvent.fire(this, uuid);
        Dialog.errorMessage("Загрузить макет не удалось.");
    }

    public void onDataViewChanged(){
        formTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
    }
}
