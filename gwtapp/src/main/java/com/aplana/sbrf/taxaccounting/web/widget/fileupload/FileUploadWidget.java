package com.aplana.sbrf.taxaccounting.web.widget.fileupload;

import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
public class FileUploadWidget extends Composite implements HasHandlers, HasValue<String>{

    @UiField
    FileUpload uploader;

    @UiField
    FormPanel uploadFormDataXls;

    @UiField
    LinkButton uploadButton;

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

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
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
                    setValue(jsonValue.isObject().get("uuid").toString().replaceAll("\"", "").trim(), true);
                } else {
                    setValue("");
                }
                uploadFormDataXls.reset();
            }
        });
        uploader.getElement().setId("uploaderWidget");
        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                uploadFormDataXls.submit();
            }
        });
    }

    @UiHandler("uploadButton")
    void onUploadButtonClicked(ClickEvent event){
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

}
