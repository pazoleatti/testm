package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class DepartmentConfigDaoImp extends AbstractDao implements DepartmentConfigDao {

    private RowMapper<DepartmentConfig> rowMapper = new RowMapper<DepartmentConfig>() {
        @Override
        public DepartmentConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentConfig departmentConfig = new DepartmentConfig();
            RefBookDepartment department = new RefBookDepartment();
            department.setId(rs.getInt("department_id"));
            departmentConfig.setDepartment(department);
            return departmentConfig;
        }
    };

    @Override
    public SecuredEntity getSecuredEntity(long id) {
        return getJdbcTemplate().queryForObject("select department_id from ref_book_ndfl_detail where id = ?", rowMapper, id);
    }

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
                params, new RowMapper<Pair<String, String>>() {
                    @Override
                    public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<>(rs.getString("kpp"), rs.getString("oktmo"));
                    }
                });
    }

    @Override
    public PagingResult<KppSelect> findAllKppByDepartmentIdAndKpp(int departmentId, String kpp, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentId", departmentId);
        String baseSelect = "select distinct kpp from ref_book_ndfl_detail where department_id = :departmentId";
        if (!StringUtils.isEmpty(kpp)) {
            baseSelect += " and kpp like '%' || :kpp || '%'";
            params.addValue("kpp", kpp);
        }

        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());
        List<KppSelect> kppSelects = getNamedParameterJdbcTemplate().query("" +
                        "select * from (\n" +
                        "   select rownum rn, t.* from (" + baseSelect + " order by kpp) t\n" +
                        ") where rn > :startIndex and rownum <= :count",
                params, new RowMapper<KppSelect>() {
                    @Override
                    public KppSelect mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new KppSelect(rs.getInt("rn"),
                                rs.getString("kpp"));
                    }
                });

        int count = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSelect + ")", params, Integer.class);
        return new PagingResult<>(kppSelects, count);
    }
}
