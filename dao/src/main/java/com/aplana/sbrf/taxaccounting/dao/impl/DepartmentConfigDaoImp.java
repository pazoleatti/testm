package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentConfigRowMapper;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.NamedParameterSql;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Repository
public class DepartmentConfigDaoImp extends AbstractDao implements DepartmentConfigDao {

    private final static String ALL_FIELDS = "" +
            " dc.id, dc.kpp, oktmo.id oktmo_id, oktmo.code oktmo_code, oktmo.name oktmo_name, dc.start_date, dc.end_date," +
            " dc.department_id, dep.name department_name, dc.tax_organ_code, pp.id present_place_id, pp.code present_place_code, pp.name present_place_name, dc.name, dc.phone," +
            " reorg.id reorg_id, reorg.code reorg_code, reorg.name reorg_name, dc.reorg_inn, reorg_kpp, sign.id signatory_id, sign.code signatory_code, sign.name signatory_name, " +
            " dc.signatory_surname, dc.signatory_firstname, dc.signatory_lastname, dc.approve_doc_name, dc.approve_org_name, dc.reorg_successor_kpp, dc.reorg_successor_name, " +
            " dc.related_kpp, dc.related_oktmo_id ";
    private final static String ALL_FIELDS_JOINS = "" +
            "join department dep on dep.id = dc.department_id \n" +
            "join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id \n" +
            "left join ref_book_present_place pp on pp.id = dc.present_place_id\n" +
            "left join ref_book_reorganization reorg on reorg.id = dc.reorganization_id\n" +
            "left join ref_book_signatory_mark sign on sign.id = dc.signatory_id\n";
    /**
     * Возвращяет все пары КПП/ОКТМО по настройкам подразделений, актуальные на дату relevanceDate, или
     * пересекающиеся с периодом reportPeriodId, но не имеющая старших версий из другого ТБ
     */
    private final static String ACTUAL_KPP_OKTMO_SELECT_WITH_PERIOD = "" +
            "  select dc.kpp, dc.oktmo_id, max(dc.start_date) start_date \n" +
            "  from department_config dc\n" +
            "  join report_period rp on rp.id = :reportPeriodId\n" +
            "  where (:departmentId is null or department_id = :departmentId) and (\n" +
            "    dc.start_date <= :relevanceDate and (dc.end_date is null or :relevanceDate <= dc.end_date)\n" +
            "    or dc.start_date <= rp.end_date and (dc.end_date is null or rp.start_date <= dc.end_date)\n" +
            "       and (:departmentId is null or not exists (select * from department_config where kpp = dc.kpp and oktmo_id = dc.oktmo_id and start_date > dc.start_date and department_id != :departmentId))\n" +
            "  )\n" +
            "  group by dc.kpp, dc.oktmo_id\n";

    /**
     * Возвращяет все пары КПП/ОКТМО по настройкам подразделений, актуальные на дату relevanceDate
     */
    private final static String ACTUAL_KPP_OKTMO_SELECT_WITHOUT_PERIOD =
            "select dc.kpp, dc.oktmo_id, max(dc.start_date) start_date \n" +
                    "from department_config dc\n" +
                    "where\n" +
                    "  department_id = :departmentId\n" +
                    "  and (dc.start_date <= sysdate and sysdate <= dc.end_date)\n" +
                    "group by dc.kpp, dc.oktmo_id\n";

    @Override
    public DepartmentConfig findById(long id) {
        try {
            return getJdbcTemplate().queryForObject("" +
                    "select " + ALL_FIELDS + " from department_config dc\n" +
                    ALL_FIELDS_JOINS +
                    "where dc.id = ?", new DepartmentConfigRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public SecuredEntity findSecuredEntityById(long id) {
        return findById(id);
    }

    @Override
    public List<DepartmentConfig> findAllByDepartmentId(int departmentId) {
        return getJdbcTemplate().query("" +
                "select " + ALL_FIELDS + " from department_config dc\n" +
                ALL_FIELDS_JOINS +
                "where department_id = ?", new DepartmentConfigRowMapper(), departmentId);
    }

    @Override
    public List<KppOktmoPair> findAllKppOKtmoPairsByDeclaration(long declarationId, Integer departmentId, int reportPeriodId, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationId", declarationId);
        params.addValue("departmentId", departmentId);
        params.addValue("reportPeriodId", reportPeriodId);
        params.addValue("relevanceDate", FastDateFormat.getInstance("dd.MM.yyyy").format(relevanceDate));
        return getNamedParameterJdbcTemplate().query("" +
                "with dep_cfg as \n" +
                "( select dc.kpp kpp, oktmo.code oktmo from department_config dc\n" +
                "join report_period rp on rp.id = :reportPeriodId\n" +
                "join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id\n" +
                "where oktmo.id = dc.oktmo_id and (:departmentId is null or dc.department_id = :departmentId) and (\n" +
                "dc.start_date <= to_date(:relevanceDate, 'DD.MM.YYYY') and (dc.end_date is null or to_date(:relevanceDate, 'DD.MM.YYYY') <= dc.end_date)\n" +
                "or (dc.start_date <= rp.end_date and (dc.end_date is null or rp.start_date <= dc.end_date)\n" +
                "and (:departmentId is null or not exists (select * from department_config where kpp = dc.kpp and oktmo_id = dc.oktmo_id and start_date > dc.start_date and department_id != :departmentId))\n" +
                ")\n" +
                ")\n" +
                ")\n" +
                "select npi.kpp, npi.oktmo\n" +
                "from ndfl_person np\n" +
                "join ndfl_person_income npi on npi.ndfl_person_id = np.id\n" +
                "join dep_cfg on dep_cfg.kpp=npi.kpp and dep_cfg.oktmo=npi.oktmo\n" +
                "where np.declaration_data_id = :declarationId \n" +
                "group by npi.kpp, npi.oktmo", params, new RowMapper<KppOktmoPair>() {
            @Override
            public KppOktmoPair mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new KppOktmoPair(rs.getString("kpp"), rs.getString("oktmo"));
            }
        });
    }

    @Override
    public List<KppOktmoPair> findAllKppOKtmoPairs(Integer departmentId, int reportPeriodId, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentId", departmentId);
        params.addValue("reportPeriodId", reportPeriodId);
        params.addValue("relevanceDate", FastDateFormat.getInstance("dd.MM.yyyy").format(relevanceDate));
        String sql = "select dc.kpp kpp, oktmo.code oktmo from department_config dc\n" +
                "join report_period rp on rp.id = :reportPeriodId\n" +
                "join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id\n" +
                "where oktmo.id = dc.oktmo_id and (\n" +
                "dc.start_date <= to_date(:relevanceDate, 'DD.MM.YYYY') and (dc.end_date is null or to_date(:relevanceDate, 'DD.MM.YYYY') <= dc.end_date)\n" +
                "or (dc.start_date <= rp.end_date and (dc.end_date is null or rp.start_date <= dc.end_date)))";
        return getNamedParameterJdbcTemplate().query(sql,
                params,
                new RowMapper<KppOktmoPair>() {
                    @Override
                    public KppOktmoPair mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new KppOktmoPair(rs.getString("kpp"), rs.getString("oktmo"));
                    }
                });
    }

    @Override
    public List<DepartmentConfig> findAllByKppAndOktmo(String kpp, String oktmo) {
        return getJdbcTemplate().query("" +
                "select " + ALL_FIELDS + " from department_config dc\n" +
                ALL_FIELDS_JOINS +
                "where dc.kpp = ? and oktmo.code = ?", new DepartmentConfigRowMapper(), kpp, oktmo);
    }

    @Override
    public PagingResult<DepartmentConfig> findPageByFilter(DepartmentConfigsFilter filter, PagingParams pagingParams) {
        NamedParameterSql parameterSql = createSelectByFilter(filter);
        String baseSql = parameterSql.getSql();

        if (pagingParams != null) {
            parameterSql.setSql(pagingParams.wrapQuery(parameterSql.getSql(), parameterSql.getParams()));
        }

        List<DepartmentConfig> departmentConfigs = getNamedParameterJdbcTemplate().query(parameterSql.getSql(), parameterSql.getParams(), new RowMapper<DepartmentConfig>() {
            @Override
            public DepartmentConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
                DepartmentConfig departmentConfig = new DepartmentConfigRowMapper().mapRow(rs, rowNum);
                departmentConfig.setRowOrd(rs.getInt("row_ord"));
                return departmentConfig;
            }
        });

        int total = pagingParams != null ?
                getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSql + ")", parameterSql.getParams(), Integer.class) :
                departmentConfigs.size();
        return new PagingResult<>(departmentConfigs, total);
    }

    @Override
    public int countByFilter(DepartmentConfigsFilter filter) {
        NamedParameterSql parameterSql = createSelectByFilter(filter);
        return getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + parameterSql.getSql() + ")", parameterSql.getParams(), Integer.class);
    }

    private NamedParameterSql createSelectByFilter(DepartmentConfigsFilter filter) {
        //language=sql
        String sql = "select " +
                (isSupportOver() ? "row_number() over (order by dep.name, dc.kpp, oktmo.code, dc.tax_organ_code, dc.start_date) row_ord, " : " rownum row_ord, ") +
                ALL_FIELDS +
                "from department_config dc\n" +
                ALL_FIELDS_JOINS +
                "where 1=1\n";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filter.getDepartmentId() != null) {
            params.addValue("departmentId", filter.getDepartmentId());
            sql += " and dc.department_id = :departmentId";
        }

        if (filter.getRelevanceDate() != null) {
            params.addValue("relevanceDate", filter.getRelevanceDate());
            sql += " and start_date <= :relevanceDate and (end_date is null or :relevanceDate <= end_date)";
        }
        if (!isEmpty(filter.getKpp())) {
            params.addValue("kpp", filter.getKpp().toLowerCase());
            sql += " and lower(kpp) like '%' || :kpp || '%'";
        }
        if (!isEmpty(filter.getOktmo())) {
            params.addValue("oktmo", filter.getOktmo().toLowerCase());
            sql += " and lower(oktmo.code) like '%' || :oktmo || '%'";
        }
        if (!isEmpty(filter.getTaxOrganCode())) {
            params.addValue("taxOrganCode", filter.getTaxOrganCode().toLowerCase());
            sql += " and lower(tax_organ_code) like '%' || :taxOrganCode || '%'";
        }
        return new NamedParameterSql(sql, params);
    }

    @Override
    public DepartmentConfig findPrevById(long id) {
        try {
            return getJdbcTemplate().queryForObject("" +
                            "with all_prev_vers as (\n" +
                            "  select prev_dc.* from department_config prev_dc\n" +
                            "  join department_config cur_dc on cur_dc.id = ?\n" +
                            "  where prev_dc.kpp = cur_dc.kpp and prev_dc.oktmo_id = cur_dc.oktmo_id and prev_dc.start_date < cur_dc.start_date\n" +
                            "  order by prev_dc.start_date desc\n" +
                            ")\n" +
                            "select " + ALL_FIELDS + " from all_prev_vers dc\n" +
                            ALL_FIELDS_JOINS +
                            "where rownum <= 1",
                    new DepartmentConfigRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public DepartmentConfig findNextById(long id) {
        try {
            return getJdbcTemplate().queryForObject("" +
                            "with all_next_vers as (\n" +
                            "  select next_dc.* from department_config next_dc\n" +
                            "  join department_config cur_dc on cur_dc.id = ?\n" +
                            "  where next_dc.kpp = cur_dc.kpp and next_dc.oktmo_id = cur_dc.oktmo_id and next_dc.start_date > cur_dc.start_date\n" +
                            "  order by next_dc.start_date asc\n" +
                            ")\n" +
                            "select " + ALL_FIELDS + " from all_next_vers dc\n" +
                            ALL_FIELDS_JOINS +
                            "where rownum <= 1",
                    new DepartmentConfigRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Pair<String, String>> findAllKppOktmoPairsByDepartmentIdIn(List<Integer> departmentIds, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentIds", departmentIds);
        params.addValue("relevanceDate", relevanceDate);
        return getNamedParameterJdbcTemplate().query("" +
                        "select dc.kpp, oktmo.code as oktmo " +
                        "from ( " +
                        "  select * from department_config dc " +
                        "  where department_id in (:departmentIds) and (start_date <= :relevanceDate " +
                        "  and (end_date is null or :relevanceDate <= end_date)) " +
                        ") dc " +
                        "join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id ",
                params, new RowMapper<Pair<String, String>>() {
                    @Override
                    public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<>(rs.getString("kpp"), rs.getString("oktmo"));
                    }
                });
    }

    @Override
    public PagingResult<KppSelect> findAllKppByDepartmentIdAndKppContaining(int departmentId, String kpp, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentId", departmentId);
        String baseSelect = "select distinct kpp from department_config where department_id = :departmentId";
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
    public List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration, Date relevanceDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationId", declaration.getId());
        params.addValue("reportPeriodId", declaration.getReportPeriodId());
        params.addValue("departmentId", declaration.getDepartmentId());
        params.addValue("relevanceDate", relevanceDate);
        String baseSelect = "with actual_kpp_oktmo as (" + ACTUAL_KPP_OKTMO_SELECT_WITH_PERIOD + "),\n" +
                "actual_department_config as (\n" +
                "  select " + ALL_FIELDS +
                "  from department_config dc\n" +
                "  join actual_kpp_oktmo kpp_oktmo on kpp_oktmo.kpp = dc.kpp and kpp_oktmo.oktmo_id = dc.oktmo_id and kpp_oktmo.start_date = dc.start_date\n" +
                ALL_FIELDS_JOINS +
                ")\n" +
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
    public PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(KppOktmoPairFilter filter, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reportPeriodId", filter.getReportPeriodId());
        params.addValue("departmentId", filter.getDepartmentId());
        params.addValue("relevanceDate", filter.getRelevanceDate());

        String actualKppOktmoWithSelect = filter.getReportPeriodId() != null ?
                ACTUAL_KPP_OKTMO_SELECT_WITH_PERIOD : ACTUAL_KPP_OKTMO_SELECT_WITHOUT_PERIOD;
        String withSelect = "with actual_kpp_oktmo as (" + actualKppOktmoWithSelect + "),\n" +
                "actual_department_config as (\n" +
                "  select dc.id, dc.kpp, oktmo.code oktmo_code, dc.start_date, dc.end_date\n" +
                "  from department_config dc\n" +
                "  join actual_kpp_oktmo kpp_oktmo on kpp_oktmo.kpp = dc.kpp and kpp_oktmo.oktmo_id = dc.oktmo_id and kpp_oktmo.start_date = dc.start_date\n" +
                "  join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id\n" +
                ")\n";
        String baseSelect;
        if (filter.getDeclarationId() != null) {
            baseSelect = withSelect +
                    "select rownum id, dc.id dep_conf_id, npi.kpp, npi.oktmo, dc.end_date\n" +
                    "from (\n" +
                    "   select distinct kpp, oktmo from ndfl_person np\n" +
                    "   join ndfl_person_income npi on npi.ndfl_person_id = np.id\n" +
                    "   where np.declaration_data_id = :declarationId\n" +
                    ") npi\n" +
                    "left join actual_department_config dc on dc.kpp = npi.kpp and dc.oktmo_code = npi.oktmo\n";
            params.addValue("declarationId", filter.getDeclarationId());
        } else {
            baseSelect = withSelect +
                    "select rownum id, dc.id dep_conf_id, dc.kpp, dc.oktmo_code oktmo, dc.end_date\n" +
                    "from actual_department_config dc ";
        }

        if (!isEmpty(filter.getName())) {
            baseSelect = "select * from (" + baseSelect + ") where (kpp || '/' || oktmo) like '%' || :name || '%'";
            params.addValue("name", filter.getName().replaceAll(" ?/ ?", "/"));
        }

        String sql = baseSelect;
        if (pagingParams != null) {
            sql = pagingParams.wrapQuery(baseSelect, params);
        }
        List<ReportFormCreationKppOktmoPair> kppOktmoPairs = getNamedParameterJdbcTemplate().query(
                sql,
                params, new RowMapper<ReportFormCreationKppOktmoPair>() {
                    @Override
                    public ReportFormCreationKppOktmoPair mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ReportFormCreationKppOktmoPair kppOktmoPair = new ReportFormCreationKppOktmoPair();
                        kppOktmoPair.setId(rs.getLong("id"));
                        kppOktmoPair.setKpp(rs.getString("kpp"));
                        kppOktmoPair.setOktmo(rs.getString("oktmo"));
                        Long departmentConfigId = SqlUtils.getLong(rs, "dep_conf_id");
                        Date versionEnd = rs.getDate("end_date");
                        if (departmentConfigId == null) {
                            kppOktmoPair.setRelevance("не относится к ТБ в периоде");
                        } else if (versionEnd != null && versionEnd.before(new Date())) {
                            kppOktmoPair.setRelevance("действует до " + FastDateFormat.getInstance("dd.MM.yyyy").format(versionEnd));
                        }
                        return kppOktmoPair;
                    }
                });

        int total = pagingParams != null ?
                getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSelect + ")", params, Integer.class) :
                kppOktmoPairs.size();
        return new PagingResult<>(kppOktmoPairs, total);
    }

    @Override
    public void create(DepartmentConfig departmentConfig) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        enrichParamsByDepartmentConfigValues(departmentConfig, params);
        getNamedParameterJdbcTemplate().update("" +
                        "insert into department_config(id, kpp, oktmo_id, start_date, end_date, department_id, tax_organ_code, present_place_id, name,\n" +
                        "   phone, reorganization_id, reorg_inn, reorg_kpp, signatory_id, signatory_surname, signatory_firstname, signatory_lastname, approve_doc_name,\n" +
                        "   approve_org_name, reorg_successor_kpp, reorg_successor_name, related_kpp, related_oktmo_id)" +
                        "values (seq_department_config.nextval, :kpp, :oktmo_id, :start_date, :end_date, :department_id, :tax_organ_code, :present_place_id, :name,\n" +
                        "   :phone, :reorganization_id, :reorg_inn, :reorg_kpp, :signatory_id, :signatory_surname, :signatory_firstname, :signatory_lastname, :approve_doc_name,\n" +
                        "   :approve_org_name, :reorg_successor_kpp, :reorg_successor_name, :related_kpp, :related_oktmo_id)",
                params, keyHolder, new String[]{"ID"});
        departmentConfig.setId(keyHolder.getKey().longValue());
    }

    @Override
    public void update(DepartmentConfig departmentConfig) {
        Assert.notNull(departmentConfig.getId());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", departmentConfig.getId());
        enrichParamsByDepartmentConfigValues(departmentConfig, params);
        getNamedParameterJdbcTemplate().update("" +
                        "update department_config\n" +
                        "set kpp = :kpp, oktmo_id = :oktmo_id, start_date = :start_date, end_date = :end_date, department_id = :department_id, tax_organ_code = :tax_organ_code, " +
                        "present_place_id = :present_place_id, name = :name, phone = :phone, reorganization_id = :reorganization_id, reorg_inn = :reorg_inn, reorg_kpp = :reorg_kpp, " +
                        "signatory_id = :signatory_id, signatory_surname = :signatory_surname, signatory_firstname = :signatory_firstname, signatory_lastname = :signatory_lastname, " +
                        "approve_doc_name = :approve_doc_name, approve_org_name = :approve_org_name, reorg_successor_kpp = :reorg_successor_kpp, reorg_successor_name = :reorg_successor_name, " +
                        "related_kpp = :related_kpp, related_oktmo_id = :related_oktmo_id\n" +
                        "where id = :id",
                params);
    }

    private void enrichParamsByDepartmentConfigValues(DepartmentConfig departmentConfig, MapSqlParameterSource params) {
        params.addValue("kpp", departmentConfig.getKpp());
        params.addValue("oktmo_id", departmentConfig.getOktmo() != null ?
                departmentConfig.getOktmo().getId() : null);
        params.addValue("start_date", departmentConfig.getStartDate());
        params.addValue("end_date", departmentConfig.getEndDate());
        params.addValue("department_id", departmentConfig.getDepartment() != null ?
                departmentConfig.getDepartment().getId() : null);
        params.addValue("tax_organ_code", departmentConfig.getTaxOrganCode());
        params.addValue("present_place_id", departmentConfig.getPresentPlace() != null ?
                departmentConfig.getPresentPlace().getId() : null);
        params.addValue("name", departmentConfig.getName());
        params.addValue("phone", departmentConfig.getPhone());
        params.addValue("reorganization_id", departmentConfig.getReorganization() != null ?
                departmentConfig.getReorganization().getId() : null);
        params.addValue("reorg_inn", departmentConfig.getReorgInn());
        params.addValue("reorg_kpp", departmentConfig.getReorgKpp());
        params.addValue("signatory_id", departmentConfig.getSignatoryMark() != null ?
                departmentConfig.getSignatoryMark().getId() : null);
        params.addValue("signatory_surname", departmentConfig.getSignatorySurName());
        params.addValue("signatory_firstname", departmentConfig.getSignatoryFirstName());
        params.addValue("signatory_lastname", departmentConfig.getSignatoryLastName());
        params.addValue("approve_doc_name", departmentConfig.getApproveDocName());
        params.addValue("approve_org_name", departmentConfig.getApproveOrgName());
        params.addValue("reorg_successor_kpp", departmentConfig.getReorgSuccessorKpp());
        params.addValue("reorg_successor_name", departmentConfig.getReorgSuccessorName());
        if (departmentConfig.getRelatedKppOktmo() != null) {
            params.addValue("related_kpp", departmentConfig.getRelatedKppOktmo().getKpp());
            params.addValue("related_oktmo_id", departmentConfig.getRelatedKppOktmo().getOktmo());
        } else {
            params.addValue("related_kpp", null);
            params.addValue("related_oktmo_id", null);
        }
    }

    @Override
    public void deleteById(long id) {
        getJdbcTemplate().update("delete from department_config where id = ?", id);
    }

    @Override
    public void deleteByDepartmentId(int departmentId) {
        getJdbcTemplate().update("delete from department_config where department_id = ?", departmentId);
    }

    @Override
    public void updateStartDate(long id, Date date) {
        getJdbcTemplate().update("update department_config set start_date = ? where id = ?", date, id);
    }

    @Override
    public void updateEndDate(long id, Date date) {
        getJdbcTemplate().update("update department_config set end_date = ? where id = ?", date, id);
    }
}
