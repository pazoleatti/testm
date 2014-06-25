package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateImpexPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class FormTemplateImpexView extends ViewWithUiHandlers<FormTemplateImpexUiHandlers>
		implements FormTemplateImpexPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateImpexView> { }

	@UiField
	FormPanel uploadFormTemplatePanel;

    @UiField
    FileUpload uploadFormFile;

	@UiField
	Button downloadFormTemplateButton;

    @UiField
    SubmitButton importFormTemplateButton;

    private static String respPattern = "(<pre.*>)(.+?)(</pre>)";

    @Inject
	public FormTemplateImpexView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

		uploadFormTemplatePanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                if(event.getResults() == null){
                    getUiHandlers().uploadFormTemplateFail();
                    return;
                }
                if (event.getResults().contains(ERROR_RESP)) {
                    String errorUuid = event.getResults().replaceAll(respPattern, "$2");
                    getUiHandlers().uploadDectResponseWithErrorUuid(errorUuid.replaceFirst(ERROR_RESP, ""));
                }else if (event.getResults().toLowerCase().contains(ERROR)) {
                    getUiHandlers().uploadFormTemplateFail();
                } else {
                    String uuid = event.getResults().replaceAll(respPattern, "$2");
                    getUiHandlers().uploadFormTemplateSuccess(uuid.replaceFirst(SUCCESS_RESP, ""));
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

	@UiHandler("importFormTemplateButton")
	public void onImportFormTemplateButton(ClickEvent event){
        if (uploadFormFile.getFilename() == null || "".equals(uploadFormFile.getFilename())) {
            getUiHandlers().uploadFormTemplateFail();
            event.preventDefault();
            return;
        }
        uploadFormTemplatePanel.submit();
	}
}