package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonDocument;

import java.util.Collection;
import java.util.List;

/**
 * Методы работы с таблицей ДУЛ ref_book_id_doc
 */
public interface IdDocDao {

    /**
     * Удаление ДУЛ-ов.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Сохранить группу ДУЛ
     * @param idDocs коллекция ДУЛ
     */
    void saveBatch(final Collection<PersonDocument> idDocs);

}
