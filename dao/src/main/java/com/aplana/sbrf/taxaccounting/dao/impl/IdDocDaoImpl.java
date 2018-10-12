package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.IdDocDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonDocument;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.in;

@Repository
public class IdDocDaoImpl extends AbstractDao implements IdDocDao {

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null) throw new NullPointerException("Ids list is null");
        if (ids.isEmpty()) throw new DaoException("Ids list is empty");

        String resetReportDocsQuery = "update ref_book_person set report_doc = null where " + in("report_doc", ids);
        getJdbcTemplate().update(resetReportDocsQuery);

        String deleteDocsQuery = "delete from ref_book_id_doc where " + in("id", ids);
        getJdbcTemplate().update(deleteDocsQuery);
    }

    public void saveBatch(final Collection<PersonDocument> idDocs) {
        saveNewObjects(idDocs, PersonDocument.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), PersonDocument.COLUMNS, PersonDocument.FIELDS);
    }
}
