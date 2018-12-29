package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDocTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Repository
public class RefBookDocTypeDaoImpl extends AbstractDao implements RefBookDocTypeDao {

    @Autowired
    private RefBookMapperFactory refBookMapperFactory;

    @Override
    public List<RefBookDocType> findAllActive() {
        Date actualDate = new Date();
        String sql = "select * from (select dt.id, dt.record_id, dt.status, dt.version as start_date, (select min(version) - interval '1' day \n" +
                "from ref_book_doc_type rb\n" +
                "where status in (0, 2)\n" +
                "and rb.version > dt.version\n" +
                "and rb.record_id = dt.record_id\n" +
                ") as end_date, dt.name, dt.code, dt.priority \n" +
                "from ref_book_doc_type dt where status = 0) r\n" +
                "where r.start_date <= :actualDate and (r.end_date >= :actualDate or r.end_date is null)";
        try {
            return getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("actualDate", actualDate), refBookMapperFactory.new DocTypeMapper<>());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<RefBookDocType> findAllByIdIn(List<Long> ids) {
        if (!isEmpty(ids)) {
            return getNamedParameterJdbcTemplate().query(
                    "select dt.id, dt.name, dt.code, dt.priority \n" +
                            "from ref_book_doc_type dt \n" +
                            "where dt.id in (:ids)",
                    new MapSqlParameterSource("ids", ids),
                    refBookMapperFactory.new DocTypeMapper<>());
        }
        return new ArrayList<>();
    }
}
