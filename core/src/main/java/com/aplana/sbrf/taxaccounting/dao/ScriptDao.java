package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.Script;

public interface ScriptDao {
	void fillFormScripts(Form form);
	void saveFormScripts(Form form);
}