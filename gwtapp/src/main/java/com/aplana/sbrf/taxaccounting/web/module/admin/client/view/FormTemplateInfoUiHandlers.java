package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.gwtplatform.mvp.client.*;

public interface FormTemplateInfoUiHandlers extends UiHandlers {
	void setNumberedColumns(boolean numberedColumns);
	void setVersion(String version);
	void setFixedRows(boolean fixedRows);
}
