package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

/**
 * Методы работы с таблицей ДУЛ ref_book_id_doc
 */
public interface IdDocDao {

    /**
     * Удаление ДУЛ-ов.
     */
    void deleteByIds(List<Long> ids);
}
