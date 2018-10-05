package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.IdDocDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

@Repository
public class IdDocDaoImpl extends AbstractDao implements IdDocDao {

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        if (ids == null) throw new NullPointerException("Ids list is null");
        if (ids.isEmpty()) throw new DaoException("Ids list is empty");

        String resetReportDocsQuery = "update ref_book_person set report_doc = null where " + in("report_doc", ids);
        getJdbcTemplate().update(resetReportDocsQuery);

        String deleteDocsQuery = "delete from ref_book_id_doc where " + in("id", ids);
        getJdbcTemplate().update(deleteDocsQuery);
    }
}
