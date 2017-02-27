package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateFilesUiHandlers extends UiHandlers {
    void onStartLoad(StartLoadFileEvent event);
    void onEndLoad(EndLoadFileEvent event);
    void downloadFile();
    void onTemplateFilesChanged();
}
