package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevelType;
import com.aplana.sbrf.taxaccounting.model.result.LogPeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.ReportPeriodResult;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Реализация DAO для работы с {@link ReportPeriod}
 *
 * @author srybakov
 */
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {

    @Override
    public SecuredEntity findSecuredEntityById(long id) {
        return fetchOne((int) id);
    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link ReportPeriod}
     */
    private class ReportPeriodMapper implements RowMapper<ReportPeriod> {
        @Override
        public ReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            ReportPeriod reportPeriod = new ReportPeriod();
            Integer id = SqlUtils.getInteger(rs, "id");
            reportPeriod.setId(id);
            reportPeriod.setName(rs.getString("name"));

            TaxPeriod taxPeriod = new TaxPeriod();
            taxPeriod.setId(SqlUtils.getInteger(rs, "tax_period_id"));
            taxPeriod.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            taxPeriod.setYear(SqlUtils.getInteger(rs, "year"));
            reportPeriod.setTaxPeriod(taxPeriod);

            reportPeriod.setStartDate(rs.getDate("start_date"));
            reportPeriod.setEndDate(rs.getDate("end_date"));
            reportPeriod.setDictTaxPeriodId(SqlUtils.getInteger(rs, "dict_tax_period_id"));
            Date calendarStartDate = rs.getDate("calendar_start_date");
            reportPeriod.setCalendarStartDate(calendarStartDate);
            reportPeriod.setOrder(getReportOrder(calendarStartDate, id));
            reportPeriod.setAccName(FormatUtils.getAccName(reportPeriod.getName(), reportPeriod.getCalendarStartDate()));
            reportPeriod.setReportPeriodTaxFormTypeId(SqlUtils.getInteger(rs, "form_type_id"));
            return reportPeriod;
        }

        /**
         * Получить порядковый номер отчётного периода в налоговом, основываясь на календарную дату начала ОП:
         * 1 января	 - 1
         * 1 апреля	 - 2
         * 1 июля	 - 3
         * 1 октября - 4
         *
         * @return порядковый номер отчётного периода в налоговом
         */
        private int getReportOrder(Date date, Integer id) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int monthNumber = calendar.get(Calendar.MONTH);
            List<Integer> correctMonths = Arrays.asList(0, 3, 6, 9);
            // день всегда первое число месяца; месяцы только янв, апр, июл и окт
            if (calendar.get(Calendar.DAY_OF_MONTH) != 1 || (!correctMonths.contains(monthNumber))) {
                throw new DaoException("Неверная календарная дата начала отчетного периода с id=" + id);
            }
            return (monthNumber + 3) / 3;
        }
    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link ReportPeriodType}
     */
    private class ReportPeriodTypeMapper implements RowMapper<ReportPeriodType> {

        @Override
        public ReportPeriodType mapRow(ResultSet resultSet, int index) throws SQLException {
            ReportPeriodType reportPeriodType = new ReportPeriodType();
            reportPeriodType.setId(SqlUtils.getLong(resultSet, "id"));
            reportPeriodType.setName(resultSet.getString("name"));
            reportPeriodType.setStartDate(resultSet.getDate("start_date"));
            reportPeriodType.setEndDate(resultSet.getDate("end_date"));
            reportPeriodType.setCalendarStartDate(resultSet.getDate("calendar_start_date"));
            reportPeriodType.setCode(resultSet.getString("code"));
            return reportPeriodType;
        }
    }

    @Override
    public ReportPeriod fetchOne(Integer id) {
        try {
            return 0 == id ? null :
                    getJdbcTemplate().queryForObject(
                            "select rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, " +
                                    "rp.end_date, rp.dict_tax_period_id, rp.calendar_start_date, rp.form_type_id " +
                                    "from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
                                    "where rp.id = ?",
                            new Object[]{id},
                            new int[]{Types.NUMERIC},
                            new ReportPeriodMapper()
                    );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с id=" + id);
        }

    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link TaxPeriod}
     */
    private final class TaxPeriodRowMapper implements RowMapper<TaxPeriod> {
        @Override
        public TaxPeriod mapRow(ResultSet rs, int index) throws SQLException {
            TaxPeriod t = new TaxPeriod();
            t.setId(SqlUtils.getInteger(rs, "id"));
            t.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            t.setYear(SqlUtils.getInteger(rs, "year"));
            return t;
        }
    }

    @Override
    public TaxPeriod fetchOrCreateTaxPeriod(int year) {
        TaxPeriod taxPeriod;
        try {
            taxPeriod = getJdbcTemplate().queryForObject(
                    "SELECT id, tax_type, year FROM tax_period WHERE year = ?",
                    new Object[]{year},
                    new int[]{Types.NUMERIC},
                    new TaxPeriodRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            taxPeriod = new TaxPeriod();
            taxPeriod.setYear(year);

            Integer id = generateId("seq_tax_period", Integer.class);
            taxPeriod.setId(id);

            getJdbcTemplate().update(
                    "INSERT INTO tax_period (id, tax_type, year)" +
                            " VALUES (?, ?, ?)",
                    new Object[]{
                            taxPeriod.getId(),
                            taxPeriod.getTaxType().getCode(),
                            taxPeriod.getYear()
                    },
                    new int[]{Types.NUMERIC, Types.VARCHAR, Types.NUMERIC}

            );
        }
        return taxPeriod;
    }

    @Override
    public List<ReportPeriod> findAll() {
        return getJdbcTemplate().query(
                "select rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, " +
                        "rp.dict_tax_period_id, rp.calendar_start_date, rp.form_type_id \n" +
                        "from report_period rp, tax_period tp \n" +
                        "where rp.tax_period_id = tp.id \n" +
                        "order by tp.year desc, calendar_start_date",
                new ReportPeriodMapper()
        );
    }

    @Override
    public List<ReportPeriod> findAllFor2NdflFL() {
        return getJdbcTemplate().query(
                "select \n" +
                        "rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, \n" +
                        "rp.dict_tax_period_id, rp.calendar_start_date, rp.form_type_id \n" +
                        "from report_period rp \n" +
                        "join tax_period tp on tp.id = rp.tax_period_id \n" +
                        "join report_period_type rpt on rpt.id = rp.dict_tax_period_id \n" +
                        "where rp.tax_period_id = tp.id and rpt.code = '34' \n" +
                        "order by tp.year desc, calendar_start_date",
                new ReportPeriodMapper()
        );
    }

    @Override
    public List<ReportPeriod> fetchAllByTaxPeriod(int taxPeriodId) {
        return getJdbcTemplate().query(
                "select rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, " +
                        "rp.dict_tax_period_id, rp.calendar_start_date, rp.form_type_id " +
                        "from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
                        "where rp.tax_period_id = ? order by rp.end_date, rp.calendar_start_date desc",
                new Object[]{taxPeriodId},
                new int[]{Types.NUMERIC},
                new ReportPeriodMapper()
        );
    }

    @Override
    @Transactional
    public Integer create(ReportPeriod reportPeriod) {
        JdbcTemplate jt = getJdbcTemplate();

        Integer id = reportPeriod.getId();
        if (id == null) {
            id = generateId("seq_report_period", Integer.class);
        }

        jt.update(
                "insert into report_period (id, name, tax_period_id, " +
                        " dict_tax_period_id, start_date, end_date, calendar_start_date)" +
                        " values (?, ?, ?, ?, ?, ?, ?)",
                id,
                reportPeriod.getName(),
                reportPeriod.getTaxPeriod().getId(),
                reportPeriod.getDictTaxPeriodId(),
                reportPeriod.getStartDate(),
                reportPeriod.getEndDate(),
                reportPeriod.getCalendarStartDate()
        );
        reportPeriod.setId(id);
        return id;
    }

    @Override
    public void remove(int reportPeriodId) {
        getJdbcTemplate().update(
                "delete from report_period where id = ?",
                new Object[]{reportPeriodId},
                new int[]{Types.NUMERIC}
        );
    }

    @Override
    public void removeTaxPeriod(int taxPeriodId) {
        getJdbcTemplate().update(
                "delete from tax_period where id = ?",
                new Object[]{taxPeriodId},
                new int[]{Types.NUMERIC}
        );
    }

    @Override
    public List<ReportPeriod> fetchAllByDepartments(List<Integer> departmentList) {
        return getJdbcTemplate().query(
                "select rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                        "rp.calendar_start_date, rp.form_type_id from report_period rp, tax_period tp where rp.id in " +
                        "(select distinct report_period_id from department_report_period " +
                        "where correction_date is null and " + SqlUtils.transformToSqlInStatement("department_id", departmentList) + ") " +
                        "and rp.tax_period_id = tp.id " +
                        "order by tp.year desc, rp.calendar_start_date",
                new ReportPeriodMapper());
    }

    @Override
    public List<ReportPeriod> findAllActive(List<Integer> departmentIds) {
        return getJdbcTemplate().query("" +
                        "select\n " +
                        "  rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date,\n " +
                        "  rp.dict_tax_period_id, rp.calendar_start_date, rp.form_type_id\n " +
                        "from report_period rp\n" +
                        "join tax_period tp on tp.id = rp.tax_period_id\n" +
                        "where rp.id in (\n" +
                        "  select drp.report_period_id from department_report_period drp\n" +
                        "  where drp.is_active = 1 and " + SqlUtils.transformToSqlInStatement("drp.department_id", departmentIds) + "\n" +
                        ")" +
                        "order by tp.year desc, rp.calendar_start_date",
                new ReportPeriodMapper());
    }

    @Override
    public List<ReportPeriod> getCorrectPeriods(int departmentId) {
        try {
            return getJdbcTemplate().query(
                    "select * from REPORT_PERIOD rp " +
                            "left join TAX_PERIOD tp on rp.TAX_PERIOD_ID=tp.ID " +
                            "left join DEPARTMENT_REPORT_PERIOD drp on rp.ID=drp.REPORT_PERIOD_ID  " +
                            "where drp.DEPARTMENT_ID= ? " +
                            "and drp.IS_ACTIVE=0 and CORRECTION_DATE is null " +
                            "order by year ASC , rp.dict_tax_period_id asc",
                    new Object[]{departmentId},
                    new int[]{Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public ReportPeriod fetchOneByTaxPeriodAndDict(int taxPeriodId, long dictTaxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                            "rp.calendar_start_date, rp.form_type_id from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
                            "where rp.tax_period_id = ? and rp.dict_tax_period_id = ?",
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public ReportPeriod getByTaxTypedCodeYear(String code, int year) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("code", code);
            params.addValue("year", year);

            return getNamedParameterJdbcTemplate().queryForObject(
                    "SELECT rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, " +
                            "rp.dict_tax_period_id, rp.calendar_start_date, rp.form_type_id " +
                            "FROM report_period rp  " +
                            "JOIN report_period_type rpt ON (rpt.id = rp.dict_tax_period_id AND rpt.code = :code)" +
                            "JOIN tax_period tp ON (tp.id = rp.tax_period_id AND tp.year = :year)",
                    params,
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<ReportPeriodType> getPeriodType() {

        try {
            return getJdbcTemplate().query("select * from report_period_type order by id", new ReportPeriodTypeMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public ReportPeriodType getReportPeriodTypeById(Long id) {
        try {
            return getJdbcTemplate().queryForObject("select * from report_period_type WHERE ID = ? order by id",
                    new Object[]{id},
                    new int[]{Types.NUMERIC},
                    new ReportPeriodTypeMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует типа отчетного периода с id=" + id);
        }
    }

    @Override
    public List<ReportPeriodResult> fetchActiveByDepartment(Integer departmentId) {
        String sql = "select rp.id, rp.name, rp.tax_period_id, tp.tax_type, tp.year, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                "rp.calendar_start_date, rp.form_type_id, drp.correction_date from report_period rp " +
                "left join department_report_period drp on rp.id = drp.report_period_id " +
                "left join tax_period tp on tp.id = rp.tax_period_id " +
                "left join report_period_type rpt on rpt.id = rp.dict_tax_period_id " +
                "where drp.is_active = 1 and drp.department_id = :departmentId " +
                "order by tp.year desc, rpt.code asc";
        MapSqlParameterSource params = new MapSqlParameterSource("departmentId", departmentId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<ReportPeriodResult>() {

                private ReportPeriodMapper reportPeriodMapper = new ReportPeriodMapper();

                @Override
                public ReportPeriodResult mapRow(ResultSet resultSet, int i) throws SQLException {
                    ReportPeriod reportPeriod = reportPeriodMapper.mapRow(resultSet, i);
                    ReportPeriodResult reportPeriodResult = new ReportPeriodResult();
                    reportPeriodResult.setCorrectionDate(resultSet.getDate("correction_date"));
                    reportPeriodResult.setId(reportPeriod.getId());
                    reportPeriodResult.setName(reportPeriod.getName());
                    reportPeriodResult.setTaxPeriod(reportPeriod.getTaxPeriod());
                    reportPeriodResult.setStartDate(reportPeriod.getStartDate());
                    reportPeriodResult.setEndDate(reportPeriod.getEndDate());
                    reportPeriodResult.setDictTaxPeriodId(reportPeriod.getDictTaxPeriodId());
                    reportPeriodResult.setCalendarStartDate(reportPeriod.getCalendarStartDate());
                    reportPeriodResult.setReportPeriodTaxFormTypeId(reportPeriod.getReportPeriodTaxFormTypeId());
                    return reportPeriodResult;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта LogPeriodResult
     */
    private class LogPeriodMapper implements RowMapper<LogPeriodResult> {
        @Override
        public LogPeriodResult mapRow(ResultSet rs, int index) throws SQLException {
            LogPeriodResult logPeriodResult = new LogPeriodResult();
            Integer id = SqlUtils.getInteger(rs, "id");
            logPeriodResult.setId(id);
            logPeriodResult.setCorrectionDate(rs.getDate("correction_date"));
            logPeriodResult.setName(rs.getString("name"));
            logPeriodResult.setYear(rs.getInt("year"));
            logPeriodResult.setEndDate(rs.getDate("end_date"));
            return logPeriodResult;
        }
    }

    @Override
    public List<LogPeriodResult> createLogPeriodFormatById(Long id, Integer logLevelType) {
        String sql = "select npo.id, drp.correction_date, rp.name, tp.year, rp.end_date\n";
        if (logLevelType == LogLevelType.INCOME.getId()) sql = sql + "from NDFL_PERSON_INCOME npo\n";
        if (logLevelType == LogLevelType.DEDUCTION.getId()) sql = sql + "from NDFL_PERSON_DEDUCTION npo\n";
        if (logLevelType == LogLevelType.PREPAYMENT.getId()) sql = sql + "from NDFL_PERSON_PREPAYMENT npo\n";
        sql = sql + "left join ndfl_person np on np.id = npo.NDFL_PERSON_ID\n" +
                "left join declaration_data dd on dd.id = np.DECLARATION_DATA_ID\n" +
                "left join department_report_period drp on drp.id = dd.department_report_period_id\n" +
                "left join report_period rp on rp.id = drp.report_period_id\n" +
                "left join tax_period tp on tp.id = rp.tax_period_id\n" +
                "where npo.id = (\n" +
                "    select npo.SOURCE_ID \n";
        if (logLevelType == LogLevelType.INCOME.getId()) sql = sql + "    from NDFL_PERSON_INCOME npo\n";
        if (logLevelType == LogLevelType.DEDUCTION.getId()) sql = sql + "    from NDFL_PERSON_DEDUCTION npo\n";
        if (logLevelType == LogLevelType.PREPAYMENT.getId()) sql = sql + "    from NDFL_PERSON_PREPAYMENT npo\n";
        sql = sql + "    left join ndfl_person np on np.id = npo.NDFL_PERSON_ID\n" +
                "    left join declaration_data dd on dd.id = np.DECLARATION_DATA_ID\n" +
                "    left join department_report_period drp on drp.id = dd.department_report_period_id\n" +
                "    where npo.id = :id)\n" +
                "ORDER BY rp.end_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<LogPeriodResult>() {
                private LogPeriodMapper logPeriodMapper = new LogPeriodMapper();

                @Override
                public LogPeriodResult mapRow(ResultSet resultSet, int i) throws SQLException {
                    LogPeriodResult logPeriod = logPeriodMapper.mapRow(resultSet, i);
                    LogPeriodResult lpr = new LogPeriodResult();
                    lpr.setId(logPeriod.getId());
                    lpr.setCorrectionDate(logPeriod.getCorrectionDate());
                    lpr.setName(logPeriod.getName());
                    lpr.setYear(logPeriod.getYear());
                    lpr.setEndDate(logPeriod.getEndDate());
                    return lpr;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }
}