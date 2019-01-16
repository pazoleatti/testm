package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
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
    public PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter filter, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportPeriodId", filter.getReportPeriodId());
        params.addValue("departmentId", filter.getDepartmentId());
        String baseSelect = "" +
                "with \n" +
                "period as (\n" +
                "  select * from report_period where id = :reportPeriodId\n" +
                "),\n" +
                "dep_conf as (\n" +
                "  select * from (\n" +
                "    select rbnd.*, lead(version) over(partition by rbnd.record_id order by version) - interval '1' DAY version_end\n" +
                "    from ref_book_ndfl_detail rbnd\n" +
                "    where status != -1\n" +
                "  ) where status = 0\n" +
                "),\n" +
                "actual_vers as (\n" +
                "  select dep_conf.kpp, dep_conf.oktmo, max(version) version \n" +
                "  from dep_conf, period\n" +
                "  where department_id = :departmentId and (\n" +
                "    dep_conf.version <= sysdate and (dep_conf.version_end is null or sysdate <= dep_conf.version_end) or\n" +
                "    dep_conf.version <= period.end_date and (dep_conf.version_end is null or dep_conf.version_end >= period.start_date)\n" +
                "  )\n" +
                "  group by dep_conf.kpp, dep_conf.oktmo\n" +
                ")\n" +
                ", actual_dep_conf as (\n" +
                "  select dep_conf.id, dep_conf.kpp, oktmo.code oktmo, " +
                "   case when dep_conf.version_end < sysdate then 'действует до ' || to_char(dep_conf.version_end, 'dd.mm.yyyy') end as relevance" +
                "  from dep_conf\n" +
                "  join actual_vers av on av.kpp = dep_conf.kpp and av.oktmo = dep_conf.oktmo and av.version = dep_conf.version\n" +
                "  join ref_book_oktmo oktmo on oktmo.id = dep_conf.oktmo\n" +
                ")\n";
        if (filter.getDeclarationId() != null) {
            baseSelect += "select * from (\n" +
                    "   select dep_conf.id, npi.kpp, npi.oktmo, \n" +
                    "      case when dep_conf.id is null then 'не относится к ТБ в периоде' \n" +
                    "      else dep_conf.relevance end as relevance \n" +
                    "   from (" +
                    "       select distinct kpp, oktmo from ndfl_person np\n" +
                    "       join ndfl_person_income npi on npi.ndfl_person_id = np.id" +
                    "       where np.declaration_data_id = :declarationId" +
                    "   ) npi\n" +
                    "   left join actual_dep_conf dep_conf on dep_conf.kpp = npi.kpp and dep_conf.oktmo = npi.oktmo\n" +
                    ")";
            params.addValue("declarationId", filter.getDeclarationId());
        } else {
            baseSelect += "select dep_conf.id, dep_conf.kpp, dep_conf.oktmo, dep_conf.relevance\n" +
                    "from actual_dep_conf dep_conf ";
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
                        kppOktmoPair.setKpp(rs.getString("kpp"));
                        kppOktmoPair.setOktmo(rs.getString("oktmo"));
                        kppOktmoPair.setRelevance(rs.getString("relevance"));
                        return kppOktmoPair;
                    }
                });

        int count = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSelect + ")", params, Integer.class);
        return new PagingResult<>(kppOktmoPairs, count);
    }
}
