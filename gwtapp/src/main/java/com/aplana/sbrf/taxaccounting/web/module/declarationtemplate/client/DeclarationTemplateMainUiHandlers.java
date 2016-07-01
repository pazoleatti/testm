package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Set;

public interface DeclarationTemplateMainUiHandlers extends UiHandlers {
	void save();
	void reset();
	void close();
    void activate(boolean force);
    int getDeclarationId();
    void onHistoryClicked();
    void downloadDect();
    void setOnLeaveConfirmation(String msg);
}
