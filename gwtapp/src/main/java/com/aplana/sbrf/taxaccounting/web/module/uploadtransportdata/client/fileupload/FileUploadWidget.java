package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.fileupload;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Виджет для загрузки файла
 * Копипаст com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget
 */
public class FileUploadWidget extends Composite implements HasHandlers, HasValue<String>, LeafValueEditor<String>, HasEnabled {

    interface Binder extends UiBinder<FormPanel, FileUploadWidget> {}
    private String value;
    private static String jsonPattern = "(<pre.*>)(.+?)(</pre>)";
    private static String UUID_STRING = "uuid";
    private static String ERROR_STRING = "<pre>error";
    private static Binder uiBinder = GWT.create(Binder.class);
    private FileUploadHandler fileUploadHandler;

    @UiField
    FileUpload uploader;

    @UiField
    FormPanel formPanel;

    @UiField
    LinkButton uploadButton;

    @UiConstructor
    public FileUploadWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String resultSring = event.getResults().toLowerCase();
                if (fileUploadHandler != null) {
                    if (resultSring.startsWith(ERROR_STRING)) {
                        fileUploadHandler.onFailure();
                    } else {
                        fileUploadHandler.onSuccess();
                    }
                }
                if (event.getResults().toLowerCase().contains(UUID_STRING)) {
                    String uuid = event.getResults().replaceAll(jsonPattern, "$2");
                    uuid = uuid.substring(uuid.indexOf(UUID_STRING) + UUID_STRING.length() + 1, uuid.length());
                    EndLoadFileEvent.fire(FileUploadWidget.this, uuid);
                    setValue(value, true);
                } else {
                    EndLoadFileEvent.fire(FileUploadWidget.this, true);
                    setValue("");
                }
                formPanel.reset();
            }
        });
        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                StartLoadFileEvent.fire(FileUploadWidget.this, uploader.getFilename());
                formPanel.submit();
            }
        });
    }

    public void setFileUploadHandler(FileUploadHandler fileUploadHandler) {
        this.fileUploadHandler = fileUploadHandler;
    }

    @Override
    public boolean isEnabled() {
        return uploadButton.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        uploadButton.setEnabled(enabled);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        this.value = value;
        if (fireEvents){
            ValueChangeEvent.fire(this, this.value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public HandlerRegistration addStartLoadHandler(StartLoadFileEvent.StartLoadFileHandler handler) {
        return addHandler(handler, StartLoadFileEvent.getType());
    }

    public HandlerRegistration addEndLoadHandler(EndLoadFileEvent.EndLoadFileHandler handler) {
        return addHandler(handler, EndLoadFileEvent.getType());
    }

    @UiHandler(value = {"uploadButton"})
    void onUploadButtonClicked(ClickEvent event) {
        uploader.getElement().<InputElement>cast().click();
    }

    public void setActionUrl(String actionUrl) {
        formPanel.setAction(actionUrl);
    }

    public void setText(String text) {
        uploadButton.setText(text);
    }

    public String getText() {
        return uploadButton.getText();
    }

    public void setWidth(String width){
        uploadButton.setWidth(width);
    }
}
