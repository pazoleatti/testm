package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookCountryDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class RefBookCountryDaoImpl extends AbstractDao implements RefBookCountryDao{

    @Autowired
    private RefBookMapperFactory refBookMapperFactory;

    @Override
    public List<RefBookCountry> findAllActive() {
        Date actualDate = new Date();
        String sql = "select * from (select c.id, c.record_id, c.status, c.version as start_date, (select min(version) - interval '1' day \n" +
                "from ref_book_country rb\n" +
                "where status in (0, 2)\n" +
                "and rb.version > c.version\n" +
                "and rb.record_id = c.record_id\n" +
                ") as end_date, c.name, c.code\n" +
                "from ref_book_country c where status = 0) r\n" +
                "where r.start_date <= :actualDate and (r.end_date >= :actualDate or r.end_date is null)";
        try {
            return getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("actualDate", actualDate), refBookMapperFactory.new CountryMapper<>() );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }
}
