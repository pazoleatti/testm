package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;

import com.aplana.sbrf.taxaccounting.service.script.api.DataRowService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * DAO для работы с данными по налоговым формам для скриптов 
 * @author auldanov
 */
@ScriptExposed
public interface FormDataService {
	
	FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);
	
	DataRowService getDataRowService(FormData fd);

}
