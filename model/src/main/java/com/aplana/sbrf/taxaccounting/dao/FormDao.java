package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Form;

public interface FormDao {
	Form getForm(int formId);
	int saveForm(Form form);
}
