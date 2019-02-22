package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeductionTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionType;
import com.aplana.sbrf.taxaccounting.model.util.SubResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class RefBookDeductionTypeDaoImpl extends AbstractDao implements RefBookDeductionTypeDao {

    @Autowired
    private RefBookMapperFactory refBookMapperFactory;

    @Override
    public List<RefBookDeductionType> findAllByVersion(Date version) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("version", version);
        return getNamedParameterJdbcTemplate().query("" +
                        "select t.id, t.code, t.name, m.id mark_id, m.code mark_code, m.name mark_name from (\n" +
                        "  select t.*, lead(version) over(partition BY record_id order by version) - interval '1' DAY version_end from ref_book_deduction_type t\n" +
                        "  where status != -1\n" +
                        ") t\n" +
                        "join (\n" +
                        "  select t.*, lead(version) over(partition BY record_id order by version) - interval '1' DAY version_end from ref_book_deduction_mark t\n" +
                        "  where status != -1\n" +
                        ") m on t.deduction_mark = m.id\n" +
                        "where t.status = 0 and t.version <= :version and (t.version_end is null or :version <= t.version_end) \n" +
                        "and m.status = 0 and m.version <= :version and (m.version_end is null or :version <= m.version_end)",
                params,
                new RowMapper<RefBookDeductionType>() {
                    @Override
                    public RefBookDeductionType mapRow(ResultSet rs, int rowNum) throws SQLException {
                        RefBookDeductionType type = refBookMapperFactory.new DeductionTypeMapper().mapRow(rs, rowNum);
                        type.setMark(refBookMapperFactory.new DeductionMarkMapper().mapRow(new SubResultSet("mark_", rs), rowNum));
                        return type;
                    }
                });
    }
}
