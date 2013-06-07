package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.Income102;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;
/**
 * Дао для отчета о прибыли и убытках
 */
@ScriptExposed
public interface Income102Dao {
	
	/**
	 * Получение данных отчета о прибылях и убытках
	 * @param reportPeriodId идентификатор отчетного периода
	 * @param opuCode код ОПУ
	 */
	public List<Income102> getIncome102(int reportPeriodId, String opuCode, int departmentId);

}
