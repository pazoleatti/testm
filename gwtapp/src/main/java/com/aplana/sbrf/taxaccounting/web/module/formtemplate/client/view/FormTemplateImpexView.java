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

    // TODO не работает патерн в хроме. Пример: <pre style="word-wrap: break-word; white-space: pre-wrap;"></pre>
    private static String respPattern = "(<pre.*>)(.+?)(</pre>)";

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
                if (event.getResults().toLowerCase().contains(ERROR_RESP)) {
                    String errorUuid = event.getResults().toLowerCase().replaceAll(respPattern, "$2");
                    getUiHandlers().uploadDectResponseWithErrorUuid(errorUuid.replaceFirst(ERROR_RESP, ""));
                }else if (event.getResults().toLowerCase().contains(ERROR)) {
                    String errorText = event.getResults().toLowerCase().replaceAll(respPattern, "$2");
                    getUiHandlers().uploadFormTemplateFail(errorText.replaceFirst(ERROR, ""));
                } else {
                    String uuid = event.getResults().toLowerCase().replaceAll(respPattern, "$2");
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
}