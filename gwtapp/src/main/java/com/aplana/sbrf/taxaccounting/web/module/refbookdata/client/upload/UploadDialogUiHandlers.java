package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

public interface UploadDialogUiHandlers extends UiHandlers {
    void createTask(String uuid, Date dateFrom, Date dateTo, boolean force);
    void preLoadCheck(String fileName, Date dateFrom, Date dateTo);
    void onStartLoad();
    void onEndLoad();
}