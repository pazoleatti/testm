package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;

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
    void createBatch(final Collection<IdDoc> idDocs);

    /**
     * Массовое обновление ДУЛ
     * @param idDocs коллекция ДУЛ
     */
    void updateBatch(final Collection<IdDoc> idDocs);

    /**
     * Получить список ДУЛ физлица. Метод возвращает ДУЛы версии физлица переданной в аргумента, также ДУЛы дубликатов и неактуальных версий
     * @param person объект реестра ФЛ
     * @return  список ДУЛ физлица
     */
    List<IdDoc> getByPerson(RegistryPerson person);

    /**
     * Найти общее количество ДУЛ для ФЛ
     * @param personRecordId идентификатор ФЛ
     * @return количество ДУЛ ФЛ
     */
    int findIdDocCount(Long personRecordId);

}
