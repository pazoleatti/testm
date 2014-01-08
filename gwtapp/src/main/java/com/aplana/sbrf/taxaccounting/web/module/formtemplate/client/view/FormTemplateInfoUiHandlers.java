package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.gwtplatform.mvp.client.*;

import java.util.Date;

public interface FormTemplateInfoUiHandlers extends UiHandlers {
	void setNumberedColumns(boolean numberedColumns);
	void setVersion(Date version);
	void setFixedRows(boolean fixedRows);
	void setName(String name);
	void setFullname(String fullName);
	void setCode(String code);
}
