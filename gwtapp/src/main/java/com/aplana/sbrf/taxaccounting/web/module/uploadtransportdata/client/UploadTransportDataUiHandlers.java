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
    public static final String ACTION_URL = "/upload/transportData/upload/";
    public void onStartLoad(StartLoadFileEvent event);
    public void onEndLoad(EndLoadFileEvent event);
    public void onLoadAll();
    public void onSuccess();
    public void onFailure();
}
