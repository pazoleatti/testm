package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationDataUiHandlers extends UiHandlers {
	void refreshDeclaration();
	void accept(boolean accepted);
	void delete();
	void downloadExcel();
	void downloadXml();
}
