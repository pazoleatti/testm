package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface DepartmentService {
	
	/**
	 * Получить по id подразделения его основные параметры
	 * @param departmentId идентфикатор подразделения
	 * @return основные параметры подразделения
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParam getDepartmentParam(int departmentId);
	
	/**
	 * Получить по id подразделения его параметры по налогу на прибыль
	 * @param departmentId идентфикатор подразделения
	 * @return параметры подразделения по налогу на прибыль
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParamIncome getDepartmentParamIncome(int departmentId);
	
	/**
	 * Получить по id подразделения его параметры по транспортному налогу
	 * @param departmentId идентфикатор подразделения
	 * @return параметры подразделения по налогу на прибыль
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParamTransport getDepartmentParamTransport(int departmentId);
	
    /**
     * Проверка по НСИ. Проверка существования записи со значением «Код подразделения в нотации Сбербанка» 
     */
    Boolean issetSbrfCode(String sbrfCode);
    
    /**
     * Проверка по НСИ. Проверка существования записи со значением ««Наименование подразделения»» 
     */
    Boolean issetName(String name);

    /**
     * Этот метод рождеённый deprecated, потому что не будет одназначного соответсвия между именем подразделения и его id (имя не уникально)
     * Получает department по его имени
     * @deprecated
     * @param name
     * @return
     */
    Department get(String name) throws IllegalArgumentException;

    /**
     * Получает Department по его id
     * @param id
     * @return
     * @throws IllegalArgumentException
     */
    Department get(Integer id) throws IllegalArgumentException;
}
