package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateUiHandlers extends UiHandlers {
	void save();
	void reset();
	void close();
    void activate(boolean force);
    int getDeclarationId();
    void onHistoryClicked();
}
