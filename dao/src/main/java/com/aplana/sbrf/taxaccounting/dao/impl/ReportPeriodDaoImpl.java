package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.QueryDSLOrderingUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartmentReportPeriod.departmentReportPeriod;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QReportPeriod.reportPeriod;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QReportPeriodType.reportPeriodType;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QTaxPeriod.taxPeriod;
import static com.querydsl.core.types.Projections.bean;

/**
 * Реализация DAO для работы с {@link ReportPeriod отчётными периодами}
 *
 * @author srybakov
 */
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {

    private static final Log LOG = LogFactory.getLog(ReportPeriodDaoImpl.class);

    private TaxPeriodDao taxPeriodDao;
    private SQLQueryFactory sqlQueryFactory;

    private QBean<ReportPeriodType> reportPeriodTypeQBean = bean(ReportPeriodType.class,
            reportPeriodType.id,
            reportPeriodType.code,
            reportPeriodType.name,
            reportPeriodType.calendarStartDate,
            reportPeriodType.startDate,
            reportPeriodType.endDate);
    private QBean<ReportPeriod> reportPeriodQBean = bean(ReportPeriod.class, reportPeriod.id,
            reportPeriod.name,
            reportPeriod.startDate,
            reportPeriod.endDate,
            reportPeriod.dictTaxPeriodId,
            reportPeriod.calendarStartDate,
            bean(TaxPeriod.class, taxPeriod.year, taxPeriod.id).as("taxPeriod")
    );
    private QBean<TaxPeriod> taxPeriodQBean = bean(TaxPeriod.class, taxPeriod.id, taxPeriod.year);

    @Autowired
    public ReportPeriodDaoImpl(TaxPeriodDao taxPeriodDao, SQLQueryFactory sqlQueryFactory) {
        this.taxPeriodDao = taxPeriodDao;
        this.sqlQueryFactory = sqlQueryFactory;
    }


    @Override
    public SecuredEntity getSecuredEntity(long id) {
        return get((int) id);
    }

    private class ReportPeriodMapper implements RowMapper<ReportPeriod> {
        @Override
        public ReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            ReportPeriod reportPeriod = new ReportPeriod();
            Integer id = SqlUtils.getInteger(rs, "id");
            reportPeriod.setId(id);
            reportPeriod.setName(rs.getString("name"));
            reportPeriod.setTaxPeriod(taxPeriodDao.get(SqlUtils.getInteger(rs, "tax_period_id")));
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

    @Override
    public ReportPeriod get(Integer id) {
        ReportPeriod period = sqlQueryFactory.select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(reportPeriod.id.eq(id))
                .fetchOne();

        if (period != null) {
            period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
            period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
            return reportPeriodDateFormatter(period);
        } else {
            throw new DaoException("Не существует периода с id=" + id);
        }

    }

    @Override
    public List<ReportPeriod> get(List<Integer> ids) {
        List<ReportPeriod> periods = sqlQueryFactory.select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(reportPeriod.id.in(ids))
                .transform(GroupBy.groupBy(reportPeriod.id).list(reportPeriodQBean));
        for (ReportPeriod period : periods) {
            period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
            period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
            period = reportPeriodDateFormatter(period);
        }
        return periods;
    }

    @Override
    public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
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
    public Integer save(ReportPeriod newReportPeriod) {
        Integer id = newReportPeriod.getId();
        if (id == null) {
            id = generateId("seq_report_period", Integer.class);
        }
        newReportPeriod.setOrder(getReportOrder(newReportPeriod.getCalendarStartDate(), id));

        sqlQueryFactory.insert(reportPeriod)
                .columns(
                        reportPeriod.id,
                        reportPeriod.name,
                        reportPeriod.taxPeriodId,
                        reportPeriod.dictTaxPeriodId,
                        reportPeriod.startDate,
                        reportPeriod.endDate,
                        reportPeriod.calendarStartDate)
                .values(
                        id,
                        newReportPeriod.getName(),
                        newReportPeriod.getTaxPeriod().getId(),
                        newReportPeriod.getDictTaxPeriodId(),
                        newReportPeriod.getStartDate(),
                        newReportPeriod.getEndDate(),
                        newReportPeriod.getCalendarStartDate())
                .execute();
        return id;
    }

    @Override
    public void remove(int reportPeriodId) {
        sqlQueryFactory.delete(reportPeriod)
                .where(reportPeriod.id.eq(reportPeriodId))
                .execute();
    }

    @Override
    public List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList) {


        OrderSpecifier order1 = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                taxPeriodQBean, "year", Order.DESC);
        OrderSpecifier order2 = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                reportPeriodQBean, "calendarStartDate", Order.ASC);


        SubQueryExpression<Integer> childrenQuery = sqlQueryFactory
                .select(reportPeriod.id)
                .from(departmentReportPeriod)
                .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                .where(departmentReportPeriod.correctionDate.isNull())
                .where(departmentReportPeriod.departmentId.in(departmentList))
                .distinct();

        List<ReportPeriod> res = sqlQueryFactory.select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(reportPeriod.id.in(childrenQuery))
                .where(taxPeriod.taxType.eq(String.valueOf(taxType.getCode())))
                .orderBy(order1, order2)
                .transform(GroupBy.groupBy(reportPeriod.id).list(reportPeriodQBean));

        for (ReportPeriod period : res) {
            period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
            period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
            period = reportPeriodDateFormatter(period);
        }
        return res;


//        return getJdbcTemplate().query(
//                "select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
//						"rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
//                        "(select distinct report_period_id from department_report_period " +
//                        "where correction_date is null and "+ SqlUtils.transformToSqlInStatement("department_id", departmentList)+") " +
//                        "and rp.tax_period_id = tp.id " +
//                        "and tp.tax_type = \'" + taxType.getCode() + "\' " +
//                        "order by tp.year desc, rp.calendar_start_date",
//                new ReportPeriodMapper());
    }

    @Override
    public List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList, boolean withoutCorrect) {

        OrderSpecifier order1 = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                taxPeriodQBean, "year", Order.DESC);
        OrderSpecifier order2 = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                reportPeriodQBean, "calendarStartDate", Order.ASC);

        BooleanBuilder where = new BooleanBuilder();

        where.and(departmentReportPeriod.departmentId.in(departmentList));
        where.and(departmentReportPeriod.isActive.eq(true));
        if (withoutCorrect) {
            where.and(departmentReportPeriod.correctionDate.isNull());
        }

        SubQueryExpression<Integer> childrenQuery = sqlQueryFactory
                .select(reportPeriod.id)
                .from(departmentReportPeriod)
                .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                .where(where)
                .distinct();

        List<ReportPeriod> res = sqlQueryFactory.select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(reportPeriod.id.in(childrenQuery))
                .where(taxPeriod.taxType.eq(String.valueOf(taxType.getCode())))
                .orderBy(order1, order2)
                .transform(GroupBy.groupBy(reportPeriod.id).list(reportPeriodQBean));

        for (ReportPeriod period : res) {
            period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
            period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
            period = reportPeriodDateFormatter(period);
        }
        return res;
    }

    @Override
    public PagingResult<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(taxPeriod.taxType.eq(String.valueOf(taxType.getCode())))
                .and(departmentReportPeriod.departmentId.eq(departmentId))
                .and(departmentReportPeriod.isActive.eq(false));

        List<ReportPeriod> result = sqlQueryFactory
                .select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .leftJoin(reportPeriod._depRepPerFkRepPeriodId, departmentReportPeriod)
                .where(where)
                .orderBy(taxPeriod.year.asc(), reportPeriod.dictTaxPeriodId.asc())
                .transform(GroupBy.groupBy(reportPeriod.id).list(reportPeriodQBean));

        for (ReportPeriod period : result) {
            period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
            period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
            period = reportPeriodDateFormatter(period);
        }
        return new PagingResult<>(result, getTotalCount(where));
    }

    @Override
    public List<ReportPeriod> getComparativPeriods(TaxType taxType, int departmentId) {

        OrderSpecifier order1 = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                taxPeriodQBean, "year", Order.DESC);
        OrderSpecifier order2 = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                reportPeriodQBean, "calendarStartDate", Order.ASC);

        SubQueryExpression<Integer> childrenQuery = sqlQueryFactory
                .select(reportPeriod.id)
                .from(departmentReportPeriod)
                .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                .where(departmentReportPeriod.departmentId.eq(departmentId))
                .where(departmentReportPeriod.correctionDate.isNull())
                .distinct();

        List<ReportPeriod> res =  sqlQueryFactory.select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(reportPeriod.id.in(childrenQuery))
                .where(taxPeriod.taxType.eq(String.valueOf(taxType.getCode())))
                .orderBy(order1, order2)
                .transform(GroupBy.groupBy(reportPeriod.id).list(reportPeriodQBean));

        for (ReportPeriod period : res) {
            period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
            period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
            period = reportPeriodDateFormatter(period);
        }
        return res;

//        try {
//            return getJdbcTemplate().query(
//                    "select * from REPORT_PERIOD rp " +
//                            "left join TAX_PERIOD tp on rp.TAX_PERIOD_ID=tp.ID " +
//                            "left join DEPARTMENT_REPORT_PERIOD drp on rp.ID=drp.REPORT_PERIOD_ID  " +
//                            "where tp.TAX_TYPE = ? and drp.DEPARTMENT_ID= ? " +
//                            "and CORRECTION_DATE is null " +
//                            "order by tp.year desc, rp.calendar_start_date",
//                    new Object[]{String.valueOf(taxType.getCode()), departmentId},
//                    new int[] { Types.VARCHAR, Types.NUMERIC},
//                    new ReportPeriodMapper()
//            );
//        } catch (EmptyResultDataAccessException e) {
//            return Collections.emptyList();
//        }
    }

    @Override
    public ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, long dictTaxPeriodId) {

        try {
            ReportPeriod period = sqlQueryFactory.select(reportPeriodQBean)
                    .from(reportPeriod)
                    .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                    .where(reportPeriod.dictTaxPeriodId.eq(dictTaxPeriodId).and(taxPeriod.id.eq(taxPeriodId)))
                    .fetchOne();
            if (period != null) {
                period.setOrder(getReportOrder(period.getCalendarStartDate(), period.getId()));
                period.setAccName(FormatUtils.getAccName(period.getName(), period.getCalendarStartDate()));
                return reportPeriodDateFormatter(period);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public ReportPeriod getReportPeriodByDate(TaxType taxType, Date date) {
        try {
            ReportPeriod result = sqlQueryFactory.select(reportPeriodQBean)
                    .from(reportPeriod)
                    .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                    .where(taxPeriod.taxType.eq(String.valueOf(taxType.getCode())))
                    .where(reportPeriod.endDate.eq(date))
                    .fetchOne();

//			getJdbcTemplate().query(
//					"select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
//							"rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
//							"where tp.tax_type = ? and rp.end_date=?",
//					new Object[]{taxType.getCode(), date},
//					new int[] { Types.VARCHAR, Types.DATE },
//					new ReportPeriodMapper()
//			);
            if (result == null) {
                throw new DaoException(String.format("Не найден отчетный период с типом = \"%s\" на дату \"%tD\"", taxType.getCode(), date));
            }else {
                result.setOrder(getReportOrder(result.getCalendarStartDate(), result.getId()));
                result.setAccName(FormatUtils.getAccName(result.getName(), result.getCalendarStartDate()));
                return reportPeriodDateFormatter(result);
            }
        } catch (Exception e) {
            // изменить форматирование для даты на "%td %<tm,%<tY" (31.12.2013) вместо "%tD" (12/31/13)(Marat Fayzullin 02.18.2014)
            LOG.error("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", e);
            throw new DaoException(String.format("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", taxType.getCode(), date));
        }
    }

    @Override
    public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate) {
        try {
            return getJdbcTemplate().query(
                    "select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
                            "rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
                            "where tp.tax_type = ? and rp.end_date>=? and (rp.calendar_start_date<=? or ? is null)",
                    new Object[]{String.valueOf(taxType.getCode()), startDate, endDate, endDate},
                    new int[]{Types.VARCHAR, Types.DATE, Types.DATE, Types.DATE},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдены отчетные периоды с типом = \"%s\" за период (%s; %s)", taxType.getCode(), startDate, endDate));
        }
    }

    private static final String RP_BY_DATE_AND_DEPARTMENT =
            "SELECT\n" +
                    "  rp.id,\n" +
                    "  rp.name,\n" +
                    "  rp.tax_period_id,\n" +
                    "  rp.start_date,\n" +
                    "  rp.end_date,\n" +
                    "  rp.dict_tax_period_id,\n" +
                    "  rp.calendar_start_date\n" +
                    "FROM report_period rp\n" +
                    "  JOIN tax_period tp ON rp.tax_period_id = tp.id\n" +
                    "  JOIN department_report_period drp ON drp.report_period_id = rp.id\n" +
                    "WHERE tp.tax_type = ? " +
                    "  AND rp.end_date >= ? " +
                    "  AND (? IS NULL OR rp.calendar_start_date <= ?)\n" +
                    "  AND drp.department_id = ?\n" +
                    "ORDER BY rp.end_date";

    @Override
    public List<ReportPeriod> getReportPeriodsByDateAndDepartment(TaxType taxType, int depId, Date startDate, Date endDate) {
        return getJdbcTemplate().query(
                RP_BY_DATE_AND_DEPARTMENT,
                new Object[]{String.valueOf(taxType.getCode()), startDate, endDate, endDate, depId},
                new ReportPeriodMapper()
        );
    }

    @Override
    public ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("code", code);
            params.addValue("year", year);
            params.addValue("tax", String.valueOf(taxType.getCode()));

            return getNamedParameterJdbcTemplate().queryForObject(
                    "SELECT rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, rp.calendar_start_date  " +
                            "FROM report_period rp  " +
                            "JOIN report_period_type rpt ON (rpt.id = rp.dict_tax_period_id AND rpt.code = :code)" +
                            "JOIN tax_period tp ON (tp.id = rp.tax_period_id AND tp.year = :year AND tp.tax_type = :tax)",
                    params,
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public PagingResult<ReportPeriodType> getPeriodType(PagingParams pagingParams) {

        String orderingProperty = "ID";
        Order ascDescOrder = Order.valueOf(Order.ASC.name());

        OrderSpecifier order = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                reportPeriodTypeQBean, orderingProperty, ascDescOrder, reportPeriodType.id.asc());

        List<ReportPeriodType> result = sqlQueryFactory.select(reportPeriodType.id,
                reportPeriodType.code,
                reportPeriodType.name,
                reportPeriodType.calendarStartDate,
                reportPeriodType.startDate,
                reportPeriodType.endDate)
                .from(reportPeriodType)
                .orderBy(order)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(reportPeriodType.id).list(reportPeriodTypeQBean));
        for (ReportPeriodType type : result) {
            type = reportPeriodTypeDateFormatter(type);
        }
        return new PagingResult<>(result, result.size());
    }

    @Override
    public ReportPeriodType getReportPeriodType(Long id) {
        ReportPeriodType result = sqlQueryFactory.select(reportPeriodTypeQBean)
                .from(reportPeriodType)
                .where(reportPeriodType.id.eq(id))
                .fetchOne();

        return reportPeriodTypeDateFormatter(result);
    }

    @Override
    public ReportPeriodType getPeriodTypeById(Long id) {
        return reportPeriodTypeDateFormatter(sqlQueryFactory.select(reportPeriodTypeQBean)
                .from(reportPeriodType)
                .where(reportPeriodType.id.eq(id))
                .fetchOne());
    }

    private int getTotalCount(BooleanBuilder where) {
        return (int) sqlQueryFactory.select(reportPeriodQBean)
                .from(reportPeriod)
                .leftJoin(reportPeriod._depRepPerFkRepPeriodId, departmentReportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(where)
                .fetchCount();

    }

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

    /**
     * Преобразовывает {@link java.sql.Timestamp}, который выдает QueryDSL в {@link java.util.Date}
     *
     * @param period {@link ReportPeriod} период из БД
     * @return отформатированный период
     */
    private ReportPeriod reportPeriodDateFormatter(ReportPeriod period) {
        if (period != null) {
            if (period.getStartDate() != null) {
                period.setStartDate(new Date(period.getStartDate().getTime()));
            }
            if (period.getEndDate() != null) {
                period.setEndDate(new Date(period.getEndDate().getTime()));
            }
            if (period.getCalendarStartDate() != null) {
                period.setCalendarStartDate(new Date(period.getCalendarStartDate().getTime()));
            }
        }
        return period;
    }

    /**
     * Преобразовывает {@link Timestamp}, который выдает QueryDSL в {@link Date}
     *
     * @param type {@link ReportPeriodType} тип периода из БД
     * @return отформатированный тип периода
     */
    private ReportPeriodType reportPeriodTypeDateFormatter(ReportPeriodType type) {
        if (type != null) {
            if (type.getStartDate() != null) {
                type.setStartDate(new Date(type.getStartDate().getTime()));
            }
            if (type.getEndDate() != null) {
                type.setEndDate(new Date(type.getEndDate().getTime()));
            }
            if (type.getCalendarStartDate() != null) {
                type.setCalendarStartDate(new Date(type.getCalendarStartDate().getTime()));
            }
        }
        return type;
    }

}