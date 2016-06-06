package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Вью диалогового окна "Период применения изменений в печатных формах"
 *
 * @author aivanov
 */
public class UploadDialogView extends ViewWithUiHandlers<UploadDialogUiHandlers>
        implements UploadDialogPresenter.MyView {

    public interface Binder extends UiBinder<ModalWindow, UploadDialogView> {
    }

    @UiField
    ModalWindow deadlineDialog;
    @UiField
    DateMaskBoxPicker dateFrom;
    @UiField
    DateMaskBoxPicker dateTo;
    @UiField
    Button okButton;
    @UiField
    FileUpload uploader;
    @UiField
    FormPanel formPanel;
    @UiField
    HorizontalPanel datePanel;
    private static String respPattern = "(<pre.*?>|<PRE.*?>)(.+?)(</pre>|</PRE>)(.*)";

    @Inject
    public UploadDialogView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        formPanel.setAction(GWT.getHostPageBaseURL() + "upload/uploadController/pattern/");
        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                getUiHandlers().onEndLoad();
                String result = event.getResults().replaceAll(respPattern, "$2");
                JSONObject answer = JSONParser.parseLenient(result).isObject();
                if (answer.get(UuidEnum.UUID.toString()) != null) {
                    getUiHandlers().createTask(answer.get(UuidEnum.UUID.toString()).isString().stringValue(), dateFrom.getValue(), dateTo.getValue(), false);
                }
            }
        });
        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                getUiHandlers().onStartLoad();
            }
        });
    }

    @UiHandler("okButton")
    public void handleClick(ClickEvent event) {
        getUiHandlers().preLoadCheck(uploader.getFilename(), dateFrom.getValue(), dateTo.getValue());
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event){
        deadlineDialog.hide();
    }

    @Override
    public void open(boolean isVersioned) {
        deadlineDialog.setMinHeight("100px");
        formPanel.remove(uploader);
        datePanel.setVisible(isVersioned);
        uploader = new FileUpload();
        uploader.setWidth("400px");
        uploader.setVisible(true);
        uploader.setName("uploader");
        formPanel.add(uploader);

        dateFrom.setValue(null);
        dateTo.setValue(null);
        deadlineDialog.center();
    }

    @Override
    public void load() {
        formPanel.submit();
    }

    @Override
    public void hide() {
        deadlineDialog.hide();
    }
}
