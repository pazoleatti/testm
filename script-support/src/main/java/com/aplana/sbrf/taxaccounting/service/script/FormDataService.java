package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Map;

/**
 * DAO для работы с данными по налоговым формам для скриптов 
 * @author auldanov
 */
@ScriptExposed
public interface FormDataService {

    /**
     * Поиск налоговой формы
     * @param formTypeId Тип формы
     * @param kind Вид формы
     * @param departmentId Подразделение
     * @param reportPeriodId Отчетный период
     * @return
     */
	FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId);

    /**
     * Посредник для работы со строками налоговой формы во временном и постоянном срезах
     * @param fd
     * @return
     */
	DataRowHelper getDataRowHelper(FormData fd);

    /**
     * Заполнение кэша значений справочника
     * @param formDataId
     * @param refBookCache
     */
    void fillRefBookCache(Long formDataId, Map<Long, Map<String, RefBookValue>> refBookCache);
}
