package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

/**
 * Методы работы с таблицей ДУЛ ref_book_id_doc
 */
public interface IdDocDao {

    /**
     * Удаление ДУЛ.
     */
    void deleteByIds(List<Long> ids);
}
