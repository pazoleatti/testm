package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.Script;

public interface ScriptDao {
    public int create(Script script);
    public Script select(int id);
    public void update(Script script);
    public void delete(Script script);
    public void delete(int scriptId);

	void fillFormScripts(Form form);
	void saveFormScripts(Form form);
}