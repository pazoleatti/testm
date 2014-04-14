package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateImpexPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class FormTemplateImpexView extends ViewWithUiHandlers<FormTemplateImpexUiHandlers>
		implements FormTemplateImpexPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateImpexView> { }

	@UiField
	FormPanel uploadFormTemplatePanel;

	@UiField
	Button downloadFormTemplateButton;

    @Inject
	public FormTemplateImpexView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

		uploadFormTemplatePanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                if(event.getResults() == null){
                    getUiHandlers().uploadFormTemplateFail("Ошибки при импорте формы.");
                    return;
                }
                if (event.getResults().contains(ERROR_RESP)) {
                    getUiHandlers().uploadDectResponseWithErrorUuid(event.getResults().replaceFirst(ERROR_RESP, ""));
                }else if (event.getResults().toLowerCase().contains(ERROR)) {
                    getUiHandlers().uploadFormTemplateFail(event.getResults().replaceFirst(ERROR, ""));
                } else {
                    getUiHandlers().uploadFormTemplateSuccess(event.getResults().replaceFirst(SUCCESS_RESP, ""));
                }
			}
		});
	}

	@Override
	public void setFormId(int formId) {
		uploadFormTemplatePanel.setAction(GWT.getHostPageBaseURL() + "download/formTemplate/upload/" + formId);
	}

	@UiHandler("downloadFormTemplateButton")
	public void onDownloadFormTemplateButton(ClickEvent event){
		getUiHandlers().downloadFormTemplate();
	}
}