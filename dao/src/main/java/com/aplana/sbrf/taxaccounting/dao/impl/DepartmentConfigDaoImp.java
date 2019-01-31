package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentConfigRowMapper;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Repository
public class DepartmentConfigDaoImp extends AbstractDao implements DepartmentConfigDao {

    @Override
    public DepartmentConfig getById(long id) {
        try {
            return getJdbcTemplate().queryForObject("" +
                    "select " + DepartmentConfigRowMapper.FIELDS + " from department_config dc\n" +
                    DepartmentConfigRowMapper.JOINS +
                    "where id = ?", new DepartmentConfigRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не найдена настройка подразделений по ид = " + id);
        }
    }

    @Override
    public SecuredEntity getSecuredEntity(long id) {
        return getById(id);
    }

    @Override
    public DepartmentConfig findPrev(DepartmentConfig departmentConfig) {
        try {
            return getJdbcTemplate().queryForObject("" +
                            "with all_prev_vers as (\n" +
                            "  select * from department_config dc\n" +
                            "  where kpp = ? and oktmo = ? and version < ?\n" +
                            "  order by version desc\n" +
                            ")\n" +
                            "select " + DepartmentConfigRowMapper.FIELDS + " from all_prev_vers dc\n" +
                            DepartmentConfigRowMapper.JOINS +
                            "where rownum <= 1",
                    new DepartmentConfigRowMapper(), departmentConfig.getKpp(), departmentConfig.getOktmo().getId(), departmentConfig.getStartDate());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Pair<String, String>> findKppOktmoPairs(List<Integer> departmentIds, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentIds", departmentIds);
        params.addValue("relevanceDate", relevanceDate);
        return getNamedParameterJdbcTemplate().query(
                "SELECT rbnd.kpp, oktmo.code AS oktmo " +
                        "FROM ( " +
                        "  SELECT * FROM department_config dc " +
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
        String baseSelect = "select distinct kpp from ref_book_ndfl_detail where department_id = :departmentId and status = 0";
        if (!isEmpty(kpp)) {
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

    @Override
    public DepartmentConfig findByKppAndOktmoAndDate(String kpp, String oktmoCode, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("kpp", kpp);
        params.addValue("oktmoCode", oktmoCode);
        params.addValue("relevanceDate", relevanceDate);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("" +
                            "select " + DepartmentConfigRowMapper.FIELDS + " \n" +
                            "from department_config dc\n" + DepartmentConfigRowMapper.JOINS +
                            "where kpp = :kpp and oktmo.code = :oktmoCode and (dc.version <= :relevanceDate \n" +
                            " and (version_end is null or :relevanceDate <= version_end)) ",
                    params, new DepartmentConfigRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не найдена настройка подразделений по КПП = " + kpp + ", ОКТМО = " + oktmoCode + " и дате актуальности = " + relevanceDate);
        }
    }

    /**
     * actual_department_config - настройки подразделений для departmentId, актуальные на дату relevanceDate или пересекающиеся с периодом reportPeriodId
     */
    public final static String WITH_DEPARTMENT_CONFIG_BY_TB_AND_PERIOD_SQL = "" +
            "with period as (\n" +
            "  select * from report_period where id = :reportPeriodId\n" +
            "),\n" +
            "actual_vers as (\n" +
            "  select dc.kpp, dc.oktmo, max(version) version \n" +
            "  from department_config dc, period\n" +
            "  where department_id = :departmentId and (\n" +
            "    dc.version <= :relevanceDate and (dc.version_end is null or :relevanceDate <= dc.version_end) or\n" +
            "    dc.version <= period.end_date and (dc.version_end is null or dc.version_end >= period.start_date)\n" +
            "  )\n" +
            "  group by dc.kpp, dc.oktmo\n" +
            "),\n" +
            "actual_department_config as (\n" +
            "  select " + DepartmentConfigRowMapper.FIELDS +
            "  from department_config dc\n" +
            "  join actual_vers av on av.kpp = dc.kpp and av.oktmo = dc.oktmo and av.version = dc.version\n" +
            DepartmentConfigRowMapper.JOINS +
            ")\n";

    @Override
    public List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationId", declaration.getId());
        params.addValue("reportPeriodId", declaration.getReportPeriodId());
        params.addValue("departmentId", declaration.getDepartmentId());
        params.addValue("relevanceDate", relevanceDate);
        String baseSelect = WITH_DEPARTMENT_CONFIG_BY_TB_AND_PERIOD_SQL;
        baseSelect += "" +
                "select * from (\n" +
                "   select npi.kpp dec_kpp, npi.oktmo dec_oktmo, dc.* \n" +
                "   from (" +
                "       select distinct kpp, oktmo from ndfl_person np\n" +
                "       join ndfl_person_income npi on npi.ndfl_person_id = np.id" +
                "       where np.declaration_data_id = :declarationId" +
                "   ) npi\n" +
                "   full join actual_department_config dc on dc.kpp = npi.kpp and dc.oktmo_code = npi.oktmo\n" +
                ")";

        return getNamedParameterJdbcTemplate().query(baseSelect, params, new RowMapper<Pair<KppOktmoPair, DepartmentConfig>>() {
            @Override
            public Pair<KppOktmoPair, DepartmentConfig> mapRow(ResultSet rs, int rowNum) throws SQLException {
                KppOktmoPair kppOktmoPair = null;
                if (rs.getString("dec_kpp") != null) {
                    kppOktmoPair = new KppOktmoPair(rs.getString("dec_kpp"), rs.getString("dec_oktmo"));
                }
                DepartmentConfig departmentConfig = null;
                if (SqlUtils.getLong(rs, "id") != null) {
                    departmentConfig = new DepartmentConfigRowMapper().mapRow(rs, rowNum);
                }
                return new Pair<>(kppOktmoPair, departmentConfig);
            }
        });
    }

    @Override
    public PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter filter, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportPeriodId", filter.getReportPeriodId());
        params.addValue("departmentId", filter.getDepartmentId());
        params.addValue("relevanceDate", filter.getRelevanceDate());
        String baseSelect = WITH_DEPARTMENT_CONFIG_BY_TB_AND_PERIOD_SQL;
        if (filter.getDeclarationId() != null) {
            baseSelect += "" +
                    "select * from (\n" +
                    "   select dc.id, npi.kpp kpp_, npi.oktmo oktmo_, dc.version_end\n" +
                    "   from (" +
                    "       select distinct kpp, oktmo from ndfl_person np\n" +
                    "       join ndfl_person_income npi on npi.ndfl_person_id = np.id" +
                    "       where np.declaration_data_id = :declarationId" +
                    "   ) npi\n" +
                    "   left join actual_department_config dc on dc.kpp = npi.kpp and dc.oktmo_code = npi.oktmo\n" +
                    ")";
            params.addValue("declarationId", filter.getDeclarationId());
        } else {
            baseSelect += "select dc.id, dc.kpp kpp_, dc.oktmo_code oktmo_, dc.version_end\n" +
                    "from actual_department_config dc ";
        }

        if (!isEmpty(filter.getName())) {
            baseSelect += " where (kpp || ' / ' || oktmo) like '%' || :name || '%'";
            params.addValue("name", filter.getName());
        }

        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = getNamedParameterJdbcTemplate().query(
                pagingParams.wrapQuery(baseSelect, params),
                params, new RowMapper<ReportFormCreationKppOktmoPair>() {
                    @Override
                    public ReportFormCreationKppOktmoPair mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ReportFormCreationKppOktmoPair kppOktmoPair = new ReportFormCreationKppOktmoPair();
                        kppOktmoPair.setId(rs.getLong("rn"));
                        kppOktmoPair.setKpp(rs.getString("kpp_"));
                        kppOktmoPair.setOktmo(rs.getString("oktmo_"));
                        Long departmentConfigId = SqlUtils.getLong(rs, "id");
                        Date versionEnd = rs.getDate("version_end");
                        if (departmentConfigId == null) {
                            kppOktmoPair.setRelevance("не относится к ТБ в периоде");
                        } else if (versionEnd != null && versionEnd.before(new Date())) {
                            kppOktmoPair.setRelevance("действует до " + FastDateFormat.getInstance("dd.MM.yyyy").format(versionEnd));
                        }
                        return kppOktmoPair;
                    }
                });

        int count = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSelect + ")", params, Integer.class);
        return new PagingResult<>(kppOktmoPairs, count);
    }
}
