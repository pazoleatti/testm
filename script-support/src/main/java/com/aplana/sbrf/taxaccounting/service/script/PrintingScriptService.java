package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface PrintingScriptService {
    /**
     * Формирует Excel-отчет НФ, сохраняет его в таблицы BLOB_DATA и возвращает uuid
     * @param userInfo
     * @param formDataId
     * @param manual
     * @param isShowChecked
     * @param deleteHiddenColumns признак того, что из печатного представления надо удалить скрытые столбцы
     * @return uuid записи с данными из таблицы BLOB_DATA
     */
    String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, boolean deleteHiddenColumns, LockStateLogger stateLogger);
}
