package com.aplana.sbrf.taxaccounting.web.widget.fileupload;

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
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

/**
 * User: avanteev
 * Виджет для загрузки файлов.
 * Поле TextBox используется для имитации выбора файла, редиректит к <input type-"file" />.
 */
public class FileUploadWidget extends Composite implements HasHandlers, HasValue<String>, LeafValueEditor<String>, HasEnabled {

    @UiField
    FileUpload uploader;

    @UiField
    FormPanel uploadFormDataXls;

    @UiField
    LinkButton uploadButton;
    @UiField
    Button justButton;

    @Override
    public boolean isEnabled() {
        return uploadButton.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        uploadButton.setEnabled(enabled);
    }

    public static interface IconResource extends ClientBundle{
        @Source("importIcon.png")
        ImageResource icon();
    }

    private String value;
    private static String actionUrl = "upload/uploadController/pattern/";
    private static String jsonPattern = "(<pre.*>)(.+?)(</pre>)";
    private static String uploadPatternIE = "C:.+fakepath?."; //паттерн для IE

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

    public void setSimpleButton(boolean simpleButton){
        uploadButton.setVisible(!simpleButton);
        justButton.setVisible(simpleButton);
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

    interface Binder extends UiBinder<FormPanel, FileUploadWidget>{
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiConstructor
    public FileUploadWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        uploadFormDataXls.setAction(actionUrl);
        uploadFormDataXls.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                if (!event.getResults().toLowerCase().contains("error") && event.getResults().toLowerCase().contains("uuid")) {
                    String uuid = event.getResults().replaceAll(jsonPattern, "$2");
                    JSONValue jsonValue = JSONParser.parseLenient(uuid);
                    String value = jsonValue.isObject().get("uuid").toString().replaceAll("\"", "").trim();
                    EndLoadFileEvent.fire(FileUploadWidget.this, value);
                    setValue(value, true);
                } else {
                    EndLoadFileEvent.fire(FileUploadWidget.this, true);
                    setValue("");
                }
                uploadFormDataXls.reset();
            }
        });
        uploader.getElement().setId("uploaderWidget");
        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                StartLoadFileEvent.fire(FileUploadWidget.this, uploader.getFilename());
                uploadFormDataXls.submit();
            }
        });
    }

    @UiHandler("uploadButton")
    void onUploadButtonClicked(ClickEvent event){
        uploaderClick();
    }

    @UiHandler("justButton")
    void onJustButtonClicked(ClickEvent event){
        uploaderClick();
    }

    private void uploaderClick(){
        uploader.getElement().<InputElement>cast().click();
    }

    /**
     * Метод для совместимости с прошлой версией.
     * Сейчас используется всегда uploadAsTemporal = false
     * @param asTemporal true - через создание временной записи, false - сохраненние в постоянное хранилище
     */
    public void setUploadAsTemporal(boolean asTemporal){
        // ignore
    }

    public void setText(String text) {
        uploadButton.setText(text);
        justButton.setText(text);
    }

    public String getText() {
        return uploadButton.getText();
    }

    public void setWidth(String width){
        uploadButton.setWidth(width);
        justButton.setWidth(width);
    }


}
