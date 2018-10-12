package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.PersonTbDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class PersonTbDaoImpl extends AbstractDao implements PersonTbDao{
    @Override
    public void saveBatch(Collection<PersonTb> personTbs) {
        saveNewObjects(personTbs, PersonTb.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), PersonTb.COLUMNS, PersonTb.FIELDS);
    }
}
