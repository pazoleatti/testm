package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * DAO для работы с данными по налоговым формам для скриптов 
 * @author auldanov
 */
@ScriptExposed
public interface FormDataService {
	
	public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);

}
