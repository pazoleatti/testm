package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Form;

public interface ScriptDao {
	void fillFormScripts(Form form);
	void saveFormScripts(Form form);
}