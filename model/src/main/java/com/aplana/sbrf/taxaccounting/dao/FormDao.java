package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Form;

public interface FormDao {
	List<Form> listForms();
	Form getForm(int formId);
	int saveForm(Form form);
}
