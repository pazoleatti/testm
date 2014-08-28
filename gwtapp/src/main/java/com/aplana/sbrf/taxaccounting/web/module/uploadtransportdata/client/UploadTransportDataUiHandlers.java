package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Загрузка ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 */
public interface UploadTransportDataUiHandlers extends UiHandlers {
    static final String ACTION_URL = "/upload/transportData/upload/";
    void onStartLoad(StartLoadFileEvent event);
    void onEndLoad(EndLoadFileEvent event);
    void onLoadAll();
    void onSuccess();
    void onFailure();
}
