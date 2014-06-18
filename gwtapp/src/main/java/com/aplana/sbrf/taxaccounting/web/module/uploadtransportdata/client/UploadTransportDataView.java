package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.fileupload.FileUploadHandler;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
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

    @UiField
    FileUploadWidget uploadWidget;

    @Inject
    @UiConstructor
    public UploadTransportDataView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initListeners();
    }

    private void initListeners() {
        uploadWidget.addStartLoadHandler(new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                getUiHandlers().onStartLoad(event);
            }
        });
        uploadWidget.addEndLoadHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                getUiHandlers().onEndLoad(event);
            }
        });
    }

    @Override
    public void setUploadActionUrl(String actionUrl) {
        uploadWidget.setActionUrl(actionUrl);
    }

    @Override
    public void setFileUploadHandler(FileUploadHandler fileUploadHandler) {
        uploadWidget.setFileUploadHandler(fileUploadHandler);
    }
}