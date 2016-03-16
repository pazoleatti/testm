package com.aplana.sbrf.taxaccounting.web.widget.fileupload;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.CheckHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
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
    FormPanel uploadData;

    @UiField
    LinkButton uploadButton;

    private CheckHandler checkHandler = new CheckHandler() {
        @Override
        public boolean onCheck() {
            return true;
        }
    };

    private HandlerRegistration uploadReg;

    @Override
    public boolean isEnabled() {
        return uploadButton.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        uploadButton.setEnabled(enabled);
    }

    public interface IconResource extends ClientBundle{
        @Source("up_cc.png")
        ImageResource icon();
    }

    private String value;
    private static String actionUrl = "upload/uploadController/pattern/";
    private static String respPattern = "(<pre.*?>|<PRE.*?>)(.+?)(</pre>|</PRE>)(.*)";
    private String extension;

    @Override
    public String getValue() {
        return value;
    }

    /**
     * Устанавливает расширение файлов желаемых для загрузки
     * @param extension расширение
     */
    public void setExtension(String extension) {
        this.extension = extension;
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

    /**
     * По умолчанию используются стандартные url для загрузки файлов на сервер,
     * но возможны специфичные контроллеры(например, для деклараций)
     * @param actionUrl - url контроллера, который будет обрабатывать запрос
     */
    public void setActionUrl(String actionUrl){
        uploadData.setAction(GWT.getHostPageBaseURL() + actionUrl);
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

    public HandlerRegistration addJrxmlLoadHandler(JrxmlFileExistEvent.JrxmlFileExistHandler handler) {
        return addHandler(handler, JrxmlFileExistEvent.getType());
    }

    interface Binder extends UiBinder<FormPanel, FileUploadWidget>{
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiConstructor
    public FileUploadWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        uploadData.setAction(GWT.getHostPageBaseURL() + actionUrl);
        uploadData.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                String fileName = uploader.getFilename();
                int dotPos = fileName.lastIndexOf('.') + 1;
                String ext = fileName.substring(dotPos);
                if (extension != null && !ext.equals(extension)) {
                    event.cancel();
                    Dialog.errorMessage("Необходимо расширение файла " + extension);
                    EndLoadFileEvent.fire(FileUploadWidget.this, null, false);
                }
            }
        });

        uploader.getElement().setId("uploaderWidget");
        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                StartLoadFileEvent.fire(FileUploadWidget.this, uploader.getFilename());
                uploadData.submit();
            }
        });

        uploadReg = uploadData.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String result = event.getResults().replaceAll(respPattern, "$2");
                LogCleanEvent.fire(FileUploadWidget.this);
                JSONObject answer = JSONParser.parseLenient(result).isObject();
                if (answer.get(UuidEnum.UUID.toString()) != null) {
                    setValue(answer.get(UuidEnum.UUID.toString()).isString().stringValue(), true);
                }
                String uuid = null;
                boolean isErrors = false;
                if (answer.get(UuidEnum.SUCCESS_UUID.toString()) != null) {
                    uuid = answer.get(UuidEnum.SUCCESS_UUID.toString()).isString().stringValue();
                } else if (answer.get(UuidEnum.ERROR_UUID.toString()) != null) {
                    uuid = answer.get(UuidEnum.ERROR_UUID.toString()).isString().stringValue();
                    isErrors = true;
                }
                uploadData.reset();

                EndLoadFileEvent.fire(FileUploadWidget.this, uuid, isErrors);
            }
        });
    }

    @UiHandler(value = {"uploadButton"})
    void onUploadButtonClicked(ClickEvent event) {
        if (checkHandler != null && checkHandler.onCheck()) {
            uploaderClick();
        }
    }

    private void uploaderClick(){
        uploader.getElement().<InputElement>cast().click();
    }

    public void setText(String text) {
        uploadButton.setText(text);
    }

    public String getText() {
        return uploadButton.getText();
    }

    @Override
    public void setWidth(String width){
        uploadButton.setWidth(width);
    }

    /**
     * Если нужна какая либо проверка перед тем как вызвать окно выбора файла
     * Вызывается в момент клика на кнопку перед тем как вызвать открытие окна
     * @param checkHandler хендлер проверки
     */
    public void setCheckHandler(CheckHandler checkHandler){
        this.checkHandler = checkHandler;
    }

    /**
     * Допфункционал для загрузки jrxml в макетах деклараций
     * @param isJrxml будет ли загружаться jrxml
     */
    public void setIsJrxml(boolean isJrxml) {
        if (isJrxml && uploadReg != null){
            uploadReg.removeHandler();
            uploadReg = uploadData.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
                @Override
                public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                    String result = event.getResults().replaceAll(respPattern, "$2");
                    LogCleanEvent.fire(FileUploadWidget.this);
                    JSONObject answer = JSONParser.parseLenient(result).isObject();
                    if (answer.get(UuidEnum.UUID.toString()) != null) {
                        setValue(answer.get(UuidEnum.UUID.toString()).isString().stringValue(), true);
                    }
                    String uuid;
                    if (answer.get(UuidEnum.SUCCESS_UUID.toString()) != null) {
                        uuid = answer.get(UuidEnum.SUCCESS_UUID.toString()).isString().stringValue();
                        EndLoadFileEvent.fire(FileUploadWidget.this, uuid, false);
                    } else if (answer.get(UuidEnum.UPLOADED_FILE.toString()) != null) {
                        JrxmlFileExistEvent.fire(
                                FileUploadWidget.this,
                                answer.get(UuidEnum.UPLOADED_FILE.toString()).isString().stringValue(),
                                answer.get(UuidEnum.ERROR_UUID.toString()).isString().stringValue()
                        );
                    } else if (answer.get(UuidEnum.ERROR_UUID.toString()) != null) {
                        uuid = answer.get(UuidEnum.ERROR_UUID.toString()).isString().stringValue();
                        EndLoadFileEvent.fire(FileUploadWidget.this, uuid, true);
                    } else {
                        EndLoadFileEvent.fire(FileUploadWidget.this, null, false);
                    }
                    uploadData.reset();
                }
            });
        }
    }

    public void setImg(String url) {
        uploadButton.setImg(url);
    }

    public void setMultiple(boolean multiple) {
        if (multiple)
            uploader.getElement().setAttribute("multiple", "multiple");
        else
            uploader.getElement().removeAttribute("multiple");
    }
}
