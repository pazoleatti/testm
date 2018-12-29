package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookTaxpayerStateDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Repository
public class RefBookTaxpayerStateDaoImpl extends AbstractDao implements RefBookTaxpayerStateDao {

    @Autowired
    private RefBookMapperFactory refBookMapperFactory;

    @Override
    public List<RefBookTaxpayerState> findAllActive() {
        Date actualDate = new Date();
        String sql = "select * from (select ts.id, ts.record_id, ts.status, ts.version as start_date, (select min(version) - interval '1' day \n" +
                "from ref_book_taxpayer_state rb\n" +
                "where status in (0, 2)\n" +
                "and rb.version > ts.version\n" +
                "and rb.record_id = ts.record_id\n" +
                ") as end_date, ts.name, ts.code\n" +
                "from ref_book_taxpayer_state ts where status = 0) r\n" +
                "where r.start_date <= :actualDate and (r.end_date >= :actualDate or r.end_date is null)";
        try {
            return getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("actualDate", actualDate), refBookMapperFactory.new TaxPayerStatusMapper<RefBookTaxpayerState>());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<RefBookTaxpayerState> findAllByIdIn(List<Long> ids) {
        if (!isEmpty(ids)) {
            return getNamedParameterJdbcTemplate().query(
                    "select ts.id, ts.name, ts.code \n" +
                            "from ref_book_taxpayer_state ts \n" +
                            "where ts.id in (:ids)",
                    new MapSqlParameterSource("ids", ids),
                    refBookMapperFactory.new TaxPayerStatusMapper<>());
        }
        return new ArrayList<>();
    }
}
