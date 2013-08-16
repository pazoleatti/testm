package com.aplana.sbrf.taxaccounting.web.widget.fileupload;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;

/**
 * User: avanteev
 */
public class FileUploadWidget extends Composite implements HasHandlers{

    @UiField
    FormPanel uploadFormDataXls;

    private FileUploadHandler uploadHandler;

    interface Binder extends UiBinder<FormPanel, FileUploadWidget>{
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiConstructor
    public FileUploadWidget(final FileUploadHandler uploadHandler, String actionUrl) {
        initWidget(uiBinder.createAndBindUi(this));
        uploadFormDataXls.setAction(actionUrl);
        uploadFormDataXls.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                if (!event.getResults().toLowerCase().contains("error") && event.getResults().toLowerCase().contains("uuid")) {
                    String pattern = "(<pre>)(.+?)(</pre>)";
                    String uuid = event.getResults().replaceAll(pattern, "$2");
                    JSONValue jsonValue = JSONParser.parseLenient(uuid);
                    uploadHandler.onFileUploadSuccess(jsonValue.isObject().get("uuid").toString().replaceAll("\"","").trim());
                } else {
                    executeEvent(event.getResults().replaceFirst("error ", ""));
                }
            }
        });
        this.uploadHandler = uploadHandler;
    }

    @UiHandler("uploadButton")
    void onUploadButtonClicked(ClickEvent event){
        uploadFormDataXls.submit();
    }

    private void executeEvent(String msg){
        MessageEvent.fire(this, "Не удалось импортировать данные для формы. Ошибка: " + msg);
    }
}
