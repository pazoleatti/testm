package com.aplana.sbrf.taxaccounting.dao.script.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Импорт справочника "ОКАТО" скриптом
 * @author Dmitriy Levykin
 */
@ScriptExposed
public interface RefBookOkatoDao {

    /**
     * Обновление значения атрибута "Name" по коду ОКАТО
     * @param version
     * @param recordsList
     * @return Записи, которые не нашлись в БД по ОКАТО
     */
    List<Map<String, RefBookValue>> updateValueNames(Date version, List<Map<String, RefBookValue>> recordsList);
}
