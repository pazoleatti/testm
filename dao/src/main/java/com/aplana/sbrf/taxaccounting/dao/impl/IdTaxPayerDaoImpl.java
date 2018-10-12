package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.IdTaxPayerDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class IdTaxPayerDaoImpl extends AbstractDao implements IdTaxPayerDao {
    @Override
    public void saveBatch(Collection<PersonIdentifier> personIdentifiers) {
        saveNewObjects(personIdentifiers, PersonIdentifier.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), PersonIdentifier.COLUMNS, PersonIdentifier.FIELDS);
    }
}
