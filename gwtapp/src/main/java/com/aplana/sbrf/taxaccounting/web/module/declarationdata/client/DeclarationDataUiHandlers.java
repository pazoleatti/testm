package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

public interface DeclarationDataUiHandlers extends UiHandlers {
	void refreshDeclaration(Date docDate);
	void accept(boolean accepted);
	void delete();
	void check();
	void downloadExcel();
	void downloadXml();
	void onInfoClicked();
}
