package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;

public interface ScriptDao {
	void fillFormScripts(FormTemplate form);
	void saveFormScripts(FormTemplate form);
}