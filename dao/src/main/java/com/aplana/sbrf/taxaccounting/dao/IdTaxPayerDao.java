package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;

import java.util.Collection;

/**
 * DAO для работы с объектами ИНП ref_book_id_tax_payer
 */
public interface IdTaxPayerDao {

    /**
     * Сохранить группу ИНП
     * @param personIdentifiers коллекция ИНП
     */
    void saveBatch(final Collection<PersonIdentifier> personIdentifiers);
}
