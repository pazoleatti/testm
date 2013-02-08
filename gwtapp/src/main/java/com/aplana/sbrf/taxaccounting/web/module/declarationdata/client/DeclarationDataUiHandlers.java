package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationDataUiHandlers extends UiHandlers {
	void accept();
	void cancel();
	void downloadExcel();
	void downloadAsLegislator();
}
