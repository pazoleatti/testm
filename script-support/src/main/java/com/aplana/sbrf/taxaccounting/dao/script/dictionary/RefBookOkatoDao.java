package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Справочник "ОКАТО"
 * @author Dmitriy Levykin
 */
@ScriptExposed
public interface RefBookOkatoDao {

    /**
     * Удаление родительского кода перед его обновлением
     * @param version
     */
    public void clearParentId(Date version);

    /**
     * Вычисление родительского кода ОКАТО и обновление записей
     * @param version Обновляемая версия
     */
    public int updateParentId(Date version);

    /**
     * Обновление значения атрибута "Name" по коду ОКАТО
     * @param version
     * @param recordsList
     * @return Записи, которые не нашлись в БД по ОКАТО
     */
    public List<Map<String, RefBookValue>> updateValueNames(Date version, List<Map<String, RefBookValue>> recordsList);
}
