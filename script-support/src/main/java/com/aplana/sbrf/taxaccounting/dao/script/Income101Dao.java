package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;
/**
 * Дао для оборотной ведомости
 */
@ScriptExposed
public interface Income101Dao {
	
	/**
	 * Получение данных оборотной ведомости
	 * @param reportPeriodId идентификатор отчетного периода
	 * @param account номер счета
	 */
	public List<Income101> getIncome101(int reportPeriodId, String account, int departmentId);

}
