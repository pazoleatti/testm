package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateUiHandlers extends UiHandlers {
	void save();
	void reset();
	void close();
	void downloadJrxml();
	void downloadDect();
	void uploadJrxmlFail(String msg);
	void uploadDectSuccess();
	void uploadDectFail(String msg);
    void setXsdId(String xsdId);
}
