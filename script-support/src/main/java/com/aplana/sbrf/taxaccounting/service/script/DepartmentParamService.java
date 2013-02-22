package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/*
 * Сервис для получения информации по основным параметрам подразделения банка
 *
 * @author auldanov
 */
@ScriptExposed
public interface DepartmentParamService {
	
	/**
	 * Получить по id подразделения его основные параметры
	 * @param departmentId идентфикатор подразделения
	 * @return основные параметры подразделения
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParam getDepartmentParam(int departmentId);
}
