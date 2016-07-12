package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateMainUiHandlers extends UiHandlers {
	void save();
	void reset();
	void close();
    void activate(boolean force);
    void onReturnClicked();
    void onHistoryClicked();
    void setOnLeaveConfirmation(String msg);
}
