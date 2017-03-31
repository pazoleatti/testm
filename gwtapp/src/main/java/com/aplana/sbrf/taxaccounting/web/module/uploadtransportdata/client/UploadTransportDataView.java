package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Загрузка ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 */
public class UploadTransportDataView extends ViewWithUiHandlers<UploadTransportDataUiHandlers>
        implements UploadTransportDataPresenter.MyView {

    interface Binder extends UiBinder<Widget, UploadTransportDataView> {
    }

    private static final String UUID_STRING = "uuid";
    private static final String ERROR_STRING_1 = "error";
    private static final String ERROR_STRING_2 = "<pre>error";
    private static final String jsonPattern = "(<pre.*>)(.+?)(</pre>)";

    @UiField
    HorizontalPanel uploadPanel, loadPanel;

    @UiField
    FileUpload uploader;

    @UiField
    FormPanel formPanel;

    @UiField
    Button uploadButton, loadButton;

    @Inject
    @UiConstructor
    public UploadTransportDataView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        formPanel.setAction(GWT.getHostPageBaseURL() + getUiHandlers().ACTION_URL);
        initListeners();
    }

    private void initListeners() {
        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String resultSring = event.getResults().toLowerCase();
                if (resultSring.startsWith(ERROR_STRING_1) || resultSring.startsWith(ERROR_STRING_2)) {
                    getUiHandlers().onFailure();
                } else {
                    getUiHandlers().onSuccess();
                }
                if (event.getResults().toLowerCase().contains(UUID_STRING)) {
                    String uuid = event.getResults().replaceAll(jsonPattern, "$2");
                    int startIndex = uuid.indexOf(UUID_STRING) + UUID_STRING.length() + 1;
                    int endIndex = startIndex + 36;
                    if (endIndex <= uuid.length()) {
                        uuid = uuid.substring(startIndex, endIndex);
                    }
                    getUiHandlers().onEndLoad(new EndLoadFileEvent(uuid));
                } else {
                    getUiHandlers().onEndLoad(new EndLoadFileEvent(true));
                }
                formPanel.reset();
            }
        });

        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                getUiHandlers().onStartLoad(new StartLoadFileEvent(uploader.getFilename()));
                formPanel.submit();
            }
        });
    }

    @UiHandler("uploadButton")
    void onUploadClick(ClickEvent event) {
        uploader.getElement().<InputElement>cast().click();
    }

    @UiHandler("loadButton")
    void onLoadClick(ClickEvent event) {
        getUiHandlers().onLoadAll(false);
    }

    @Override
    public void showUpload(boolean show) {
        uploadPanel.setVisible(show);
    }

    @Override
    public void showLoad(boolean show) {
        loadPanel.setVisible(show);
    }
}