package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class DepartmentConfigDaoImp extends AbstractDao implements DepartmentConfigDao {

    private RowMapper<Pair<String, String>> kppOktmoPairsRowMapper = new RowMapper<Pair<String, String>>() {
        @Override
        public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Pair<>(rs.getString("kpp"), rs.getString("oktmo"));
        }
    };

    @Override
    public List<Pair<String, String>> fetchKppOktmoPairs(List<Integer> departmentIds, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentIds", departmentIds);
        params.addValue("relevanceDate", relevanceDate);
        return getNamedParameterJdbcTemplate().query(
                "SELECT rbnd.kpp, oktmo.code AS oktmo " +
                        "FROM ( " +
                        "  SELECT * FROM ( " +
                        "    SELECT rbnd.*, " +
                        (isSupportOver() ?
                                " lead(version) OVER(PARTITION BY rbnd.department_id, rbnd.record_id ORDER BY version) - interval '1' DAY version_end "
                                : " null version_end ") +
                        "    FROM REF_BOOK_NDFL_DETAIL rbnd " +
                        "    WHERE status != -1 " +
                        "  ) " +
                        "  WHERE department_id IN (:departmentIds) AND (version <= :relevanceDate " +
                        "  AND (version_end IS NULL OR :relevanceDate <= version_end)) " +
                        ") rbnd " +
                        "JOIN ref_book_oktmo oktmo ON oktmo.id = rbnd.oktmo ",
                params, kppOktmoPairsRowMapper);
    }
}
