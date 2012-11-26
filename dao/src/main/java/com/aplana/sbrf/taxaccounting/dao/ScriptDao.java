package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;

public interface ScriptDao {
	void fillFormScripts(FormTemplate form);
	void saveFormScripts(FormTemplate form);
}