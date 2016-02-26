package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateSubreportUiHandlers extends UiHandlers {
    void addSubreport(DeclarationSubreport subreport);
    void removeSubreport(DeclarationSubreport subreport);
    void flushSubreport(DeclarationSubreport subreport);
    void onStartLoad(StartLoadFileEvent event);
    void onEndLoad(EndLoadFileEvent event);
    void downloadFile();
}
