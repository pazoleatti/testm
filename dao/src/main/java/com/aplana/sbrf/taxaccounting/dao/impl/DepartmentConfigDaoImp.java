package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentConfigMapper;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.time.FastDateFormat;
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

    private DepartmentConfig getById(long id) {
        return getJdbcTemplate().queryForObject("select department_id from ref_book_ndfl_detail where id = ?", new RowMapper<DepartmentConfig>() {
            @Override
            public DepartmentConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
                DepartmentConfig departmentConfig = new DepartmentConfig();
                RefBookDepartment department = new RefBookDepartment();
                department.setId(rs.getInt("department_id"));
                departmentConfig.setDepartment(department);
                return departmentConfig;
            }
        }, id);
    }

    @Override
    public SecuredEntity getSecuredEntity(long id) {
        return getById(id);
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
                                " lead(version) OVER(PARTITION BY rbnd.record_id ORDER BY version) - interval '1' DAY version_end "
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
    public DepartmentConfig findByKppAndOktmoAndDate(String kpp, String oktmoCode, Date date) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("kpp", kpp);
        params.addValue("oktmoCode", oktmoCode);
        params.addValue("date", date);
        return getNamedParameterJdbcTemplate().queryForObject("" +
                        "with department_config as (\n" +
                        "  select * from (\n" +
                        "    select rbnd.*, lead(version) over(partition by rbnd.record_id order by version) - interval '1' DAY version_end\n" +
                        "    from ref_book_ndfl_detail rbnd\n" +
                        "    where status != -1\n" +
                        "  ) where status = 0\n" +
                        ")\n" +
                        "select " + DepartmentConfigMapper.FIELDS + " \n" +
                        "from department_config dc\n" +
                        "join ref_book_oktmo oktmo on oktmo.id = dc.oktmo \n" +
                        "left join ref_book_present_place pp on pp.id = dc.present_place\n" +
                        "left join ref_book_reorganization reorg on reorg.id = dc.reorg_form_code\n" +
                        "left join ref_book_signatory_mark sign on sign.id = dc.signatory_id\n" +
                        "where kpp = :kpp and oktmo.code = :oktmoCode and :date between dc.version and dc.version_end ",
                params, new DepartmentConfigMapper());
    }

    private final static String DEPARTMENT_CONFIG_BY_TB_AND_PERIOD_SQL = "" +
            "with period as (\n" +
            "  select * from report_period where id = :reportPeriodId\n" +
            "),\n" +
            "department_config as (\n" +
            "  select * from (\n" +
            "    select rbnd.*, lead(version) over(partition by rbnd.record_id order by version) - interval '1' DAY version_end\n" +
            "    from ref_book_ndfl_detail rbnd\n" +
            "    where status != -1\n" +
            "  ) where status = 0\n" +
            "),\n" +
            "actual_vers as (\n" +
            "  select dc.kpp, dc.oktmo, max(version) version \n" +
            "  from department_config dc, period\n" +
            "  where department_id = :departmentId and (\n" +
            "    dc.version <= sysdate and (dc.version_end is null or sysdate <= dc.version_end) or\n" +
            "    dc.version <= period.end_date and (dc.version_end is null or dc.version_end >= period.start_date)\n" +
            "  )\n" +
            "  group by dc.kpp, dc.oktmo\n" +
            "),\n" +
            "actual_department_config as (\n" +
            "  select " + DepartmentConfigMapper.FIELDS +
            "  from department_config dc\n" +
            "  join actual_vers av on av.kpp = dc.kpp and av.oktmo = dc.oktmo and av.version = dc.version\n" +
            "  join ref_book_oktmo oktmo on oktmo.id = dc.oktmo\n" +
            "  left join ref_book_present_place pp on pp.id = dc.present_place\n" +
            "  left join ref_book_reorganization reorg on reorg.id = dc.reorg_form_code\n" +
            "  left join ref_book_signatory_mark sign on sign.id = dc.signatory_id\n" +
            ")\n";

    @Override
    public List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportPeriodId", declaration.getReportPeriodId());
        params.addValue("departmentId", declaration.getDepartmentId());
        String baseSelect = DEPARTMENT_CONFIG_BY_TB_AND_PERIOD_SQL;
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
        params.addValue("declarationId", declaration.getId());

        return getNamedParameterJdbcTemplate().query(baseSelect, params, new RowMapper<Pair<KppOktmoPair, DepartmentConfig>>() {
            @Override
            public Pair<KppOktmoPair, DepartmentConfig> mapRow(ResultSet rs, int rowNum) throws SQLException {
                KppOktmoPair kppOktmoPair = null;
                if (rs.getString("dec_kpp") != null) {
                    kppOktmoPair = new KppOktmoPair(rs.getString("dec_kpp"), rs.getString("dec_oktmo"));
                }
                DepartmentConfig departmentConfig = null;
                if (SqlUtils.getLong(rs, "id") != null) {
                    departmentConfig = new DepartmentConfigMapper().mapRow(rs, rowNum);
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
        String baseSelect = DEPARTMENT_CONFIG_BY_TB_AND_PERIOD_SQL;
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
