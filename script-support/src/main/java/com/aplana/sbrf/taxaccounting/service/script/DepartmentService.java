package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface DepartmentService {

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

    Department getTB (String tbIndex) throws IllegalArgumentException;
}
