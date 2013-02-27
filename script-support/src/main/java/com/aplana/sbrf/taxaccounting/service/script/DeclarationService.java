package com.aplana.sbrf.taxaccounting.service.script;



import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;


@ScriptExposed
public interface DeclarationService {
	
	/**
	 * Ищет декларацию по заданным параметрам.
	 * @param declarationTypeId идентификатор типа декларации
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 * @param reportPeriodId идентификатор {@link com.aplana.sbrf.taxaccounting.model.ReportPeriod отчетного периода}
	 * @return декларацию или null, если такой декларации не найдено
	 * @throws DaoException если будет найдено несколько записей, удовлетворяющих условию поиска
	 */
	DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId);
}
