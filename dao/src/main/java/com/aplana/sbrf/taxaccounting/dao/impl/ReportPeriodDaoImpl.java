package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Реализация DAO для работы с {@link ReportPeriod}
 *
 * @author srybakov
 */
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {

    private TaxPeriodDao taxPeriodDao;

    @Autowired
    public ReportPeriodDaoImpl(TaxPeriodDao taxPeriodDao) {
        this.taxPeriodDao = taxPeriodDao;
    }


    @Override
    public SecuredEntity getSecuredEntity(long id) {
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
            reportPeriod.setTaxPeriod(taxPeriodDao.fetchOne(SqlUtils.getInteger(rs, "tax_period_id")));
            reportPeriod.setStartDate(rs.getDate("start_date"));
            reportPeriod.setEndDate(rs.getDate("end_date"));
            reportPeriod.setDictTaxPeriodId(SqlUtils.getInteger(rs, "dict_tax_period_id"));
            Date calendarStartDate = rs.getDate("calendar_start_date");
            reportPeriod.setCalendarStartDate(calendarStartDate);
            reportPeriod.setOrder(getReportOrder(calendarStartDate, id));
            reportPeriod.setAccName(FormatUtils.getAccName(reportPeriod.getName(), reportPeriod.getCalendarStartDate()));
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
                            "select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date  " +
                                    "from report_period where id = ?",
                            new Object[]{id},
                            new int[]{Types.NUMERIC},
                            new ReportPeriodMapper()
                    );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с id=" + id);
        }

    }

    @Override
    public List<ReportPeriod> fetchAllByTaxPeriod(int taxPeriodId) {
        return getJdbcTemplate().query(
                "select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date " +
                        "from report_period where tax_period_id = ? order by end_date, calendar_start_date desc",
                new Object[]{taxPeriodId},
                new int[]{Types.NUMERIC},
                new ReportPeriodMapper()
        );
    }

    @Override
    @Transactional(readOnly = false)
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
    public List<ReportPeriod> fetchAllByDepartments(List<Integer> departmentList) {
        return getJdbcTemplate().query(
                "select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                        "rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
                        "(select distinct report_period_id from department_report_period " +
                        "where correction_date is null and " + SqlUtils.transformToSqlInStatement("department_id", departmentList) + ") " +
                        "and rp.tax_period_id = tp.id " +
                        "order by tp.year desc, rp.calendar_start_date",
                new ReportPeriodMapper());
    }

    @Override
    public List<ReportPeriod> getOpenPeriodsAndDepartments(List<Integer> departmentList, boolean withoutCorrect) {
        return getJdbcTemplate().query(
                "select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                        "rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
                        "(select distinct report_period_id from department_report_period " +
                        "where " + SqlUtils.transformToSqlInStatement("department_id", departmentList) + " and is_active=1 " +
                        (withoutCorrect ? "and correction_date is null" : "") + " ) " +
                        "and rp.tax_period_id = tp.id " +
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
                            "order by year",
                    new Object[]{departmentId},
                    new int[]{Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public ReportPeriod fetchOneByTaxPeriodAndDict(int taxPeriodId, long dictTaxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date " +
                            "from report_period where tax_period_id = ? and dict_tax_period_id = ?",
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<ReportPeriod> getReportPeriodsByDate(Date startDate, Date endDate) {
        try {
            return getJdbcTemplate().query(
                    "select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                            "rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
                            "where rp.end_date>=? and (rp.calendar_start_date<=? or ? is null)",
                    new Object[]{startDate, endDate, endDate},
                    new int[]{Types.DATE, Types.DATE, Types.DATE},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдены отчетные периоды за период (%s; %s)", startDate, endDate));
        }
    }

    @Override
    public ReportPeriod getByTaxTypedCodeYear(String code, int year) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("code", code);
            params.addValue("year", year);

            return getNamedParameterJdbcTemplate().queryForObject(
                    "SELECT rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, rp.calendar_start_date  " +
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

}