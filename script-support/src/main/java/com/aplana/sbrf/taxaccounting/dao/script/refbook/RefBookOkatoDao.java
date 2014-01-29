package com.aplana.sbrf.taxaccounting.dao.script.refbook;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;

/**
 * Импорт справочника "ОКАТО" скриптом
 * @author Dmitriy Levykin
 */
@ScriptExposed
public interface RefBookOkatoDao {

    /**
     * Удаление родительского кода перед его обновлением
     */
    public void clearParentId(Date version);

    /**
     * Вычисление родительского кода ОКАТО, поиск актуальной родительской записи, соответствующей этому коду и
     * добавление ссылки на эту запись
     */
    public int updateParentId(Date version);
}
