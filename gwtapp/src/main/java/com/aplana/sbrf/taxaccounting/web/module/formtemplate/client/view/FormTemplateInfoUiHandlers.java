package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.gwtplatform.mvp.client.*;

import java.util.Date;

public interface FormTemplateInfoUiHandlers extends UiHandlers {
	void setRangeRelevanceVersion(Date versionBegin, Date versionEnd);
	void setFixedRows(boolean fixedRows);
	void setMonthlyForm(boolean monthlyForm);
    void setComparative(boolean comparative);
    void setAccruing(boolean accruing);
    void setUpdating(boolean updating);
    void setName(String name);
	void setFullname(String fullName);
	void setHeader(String header);
}
