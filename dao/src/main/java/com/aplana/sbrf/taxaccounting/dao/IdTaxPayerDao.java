package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;

import java.util.Collection;
import java.util.List;

/**
 * DAO для работы с объектами ИНП ref_book_id_tax_payer
 */
public interface IdTaxPayerDao {

    /**
     * Сохранить группу ИНП
     * @param personIdentifiers коллекция ИНП
     */
    void createBatch(final Collection<PersonIdentifier> personIdentifiers);

    /**
     * Обновление списка ИНП
     * @param personIdentifiers коллекция ИНП
     */
    void updateBatch(final Collection<PersonIdentifier> personIdentifiers);

    /**
     * Удаление ИНП по идентификатору
     * @param ids   список идентификаторов объектов ИНП
     */
    void deleteByIds(final Collection<Long> ids);

    /**
     * Получить список ИНП физлица. Метод возвращает ИНП версии физлица переданной в аргумента, также ИНП дубликатов и неактуальных версий
     * @param person    объект реестра ФЛ
     * @return  список ДУЛ физлица
     */
    List<PersonIdentifier> getByPerson(RegistryPerson person);
}
