package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.model.util.QueryDSLOrderingUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartmentReportPeriod.departmentReportPeriod;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QNotification.notification;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QReportPeriod.reportPeriod;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QTaxPeriod.taxPeriod;
import static com.querydsl.core.types.Projections.bean;

@Repository
@Transactional(readOnly = true)
public class DepartmentReportPeriodDaoImpl extends AbstractDao implements DepartmentReportPeriodDao {

    private static final Log LOG = LogFactory.getLog(DepartmentReportPeriodDaoImpl.class);

    private ReportPeriodDao reportPeriodDao;

    private SQLQueryFactory sqlQueryFactory;

    public DepartmentReportPeriodDaoImpl(ReportPeriodDao reportPeriodDao, SQLQueryFactory sqlQueryFactory) {
        this.reportPeriodDao = reportPeriodDao;
        this.sqlQueryFactory = sqlQueryFactory;
    }

    private final QBean<DepartmentReportPeriodJournalItem> qDepartmentReportPeriodJournalItem = bean(DepartmentReportPeriodJournalItem.class,
            departmentReportPeriod.id,
            reportPeriod.name,
            taxPeriod.year,
            departmentReportPeriod.isActive,
            departmentReportPeriod.correctionDate,
            notification.deadline,
            reportPeriod.dictTaxPeriodId);

    private final QBean<DepartmentReportPeriod> departmentReportPeriodQBean = bean(DepartmentReportPeriod.class,
            departmentReportPeriod.id,
            departmentReportPeriod.departmentId,
            departmentReportPeriod.correctionDate,
            departmentReportPeriod.isActive,
            bean(ReportPeriod.class,
                    reportPeriod.id,
                    reportPeriod.name,
                    reportPeriod.calendarStartDate,
                    reportPeriod.dictTaxPeriodId,
                    reportPeriod.startDate,
                    reportPeriod.endDate,
                    bean(TaxPeriod.class, taxPeriod.id, taxPeriod.year).as("taxPeriod"))
                    .as("reportPeriod"));

    private final QBean<Notification> notificationQBean = bean(Notification.class, notification.all());

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private final RowMapper<DepartmentReportPeriod> mapper = new RowMapper<DepartmentReportPeriod>() {
        @Override
        public DepartmentReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            DepartmentReportPeriod reportPeriod = new DepartmentReportPeriod();
            reportPeriod.setId(SqlUtils.getLong(rs, "id"));
            reportPeriod.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            reportPeriod.setReportPeriod(reportPeriodDao.get(SqlUtils.getInteger(rs, "report_period_id")));
            reportPeriod.setIsActive(SqlUtils.getByte(rs, "is_active"));
            reportPeriod.setCorrectionDate(new LocalDateTime(rs.getDate("correction_date")));
            return reportPeriod;
        }
    };

    private static final String QUERY_TEMPLATE_SIMPLE = "select id, department_id, report_period_id, is_active, " +
            "correction_date from department_report_period";

    private static final String QUERY_TEMPLATE_COMPOSITE_SORT = "select drp.id, drp.department_id, drp.report_period_id, drp.is_active, drp.correction_date \n" +
            "          from \n" +
            "          department_report_period drp \n" +
            "          join report_period rp on drp.report_period_id = rp.id \n" +
            "          join tax_period tp on rp.tax_period_id = tp.id \n" +
            "            %s \n" +
            "            order by tp.year, drp.CORRECTION_DATE NULLS FIRST";

    private String getFilterString(DepartmentReportPeriodFilter filter) {
        if (filter == null) {
            return "";
        }

        List<String> causeList = new LinkedList<String>();

        if (filter.isCorrection() != null) {
            causeList.add("drp.correction_date is " + (filter.isCorrection() ? " not " : "") + " null");
        }

        if (filter.isActive() != null) {
            causeList.add("drp.is_active " + (filter.isActive() ? "<>" : "=") + " 0");
        }

        if (filter.getCorrectionDate() != null) {
            causeList.add("drp.correction_date = to_date('" +
                    SIMPLE_DATE_FORMAT.get().format(filter.getCorrectionDate()) +
                    "', 'DD.MM.YYYY')");
        }

        if (filter.getDepartmentIdList() != null) {
            causeList.add(SqlUtils.transformToSqlInStatement("drp.department_id",
                    filter.getDepartmentIdList()));
        }

        if (filter.getReportPeriodIdList() != null) {
            causeList.add(SqlUtils.transformToSqlInStatement("drp.report_period_id",
                    filter.getReportPeriodIdList()));
        }

        if (filter.getTaxTypeList() != null) {
            causeList.add("tp.tax_type in " +
                    SqlUtils.transformTaxTypeToSqlInStatement(filter.getTaxTypeList()));
        }
        if (filter.getYearStart() != null || filter.getYearEnd() != null) {
            causeList.add("(:yearStart is null or tp.year >= :yearStart) and (:yearEnd is null or tp.year <= :yearEnd)");
        }

        if (causeList.isEmpty()) {
            return "";
        }

        return " where " + StringUtils.join(causeList, " and ");
    }

    @Override
    public DepartmentReportPeriod get(int id) {
        return findOne((long) id);
    }

    @Override
    public List<DepartmentReportPeriod> getListByFilter(final DepartmentReportPeriodFilter filter) {
        try {
            BooleanBuilder where = new BooleanBuilder();

            if (filter.isCorrection() != null) {
                if (filter.isCorrection()) {
                    where.and(departmentReportPeriod.correctionDate.isNotNull());
                } else {
                    where.and(departmentReportPeriod.correctionDate.isNull());
                }
            }

            if (filter.isActive() != null) {
                if (filter.isActive()) {
                    where.and(departmentReportPeriod.isActive.eq((byte) 1));
                } else {
                    where.and(departmentReportPeriod.isActive.eq((byte) 0));
                }
            }

            if (filter.getCorrectionDate() != null) {
                where.and(departmentReportPeriod.correctionDate.eq(filter.getCorrectionDate()));
            }

            if (filter.getDepartmentIdList() != null) {
                where.and(departmentReportPeriod.departmentId.in(filter.getDepartmentIdList()));
            }

            if (filter.getReportPeriodIdList() != null) {
                where.and(departmentReportPeriod.reportPeriodId.in(filter.getReportPeriodIdList()));
            }

            if (filter.getReportPeriod() != null) {
                where.and(departmentReportPeriod.reportPeriodId.eq(filter.getReportPeriod().getId()));
            }

            if (filter.getYearStart() != null) {
                where.and(taxPeriod.year.goe(filter.getYearStart()));
            }

            if (filter.getYearEnd() != null) {
                where.and(taxPeriod.year.loe(filter.getYearEnd()));
            }

            if (filter.getDepartmentId() != null) {
                where.and(departmentReportPeriod.departmentId.eq(filter.getDepartmentId()));

            }


            return sqlQueryFactory.select(departmentReportPeriodQBean)
                    .from(departmentReportPeriod)
                    .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                    .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                    .where(where)
                    .transform(GroupBy.groupBy(departmentReportPeriod.id).list(departmentReportPeriodQBean));
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private static final String QUERY_TEMPLATE_COMPOSITE_SORT_ID =
            "select drp.id from department_report_period drp " +
                    "join report_period rp on drp.report_period_id = rp.id \n" +
                    "join tax_period tp on rp.tax_period_id = tp.id " +
                    " %s ";

    @Override
    public List<Long> getListIdsByFilter(final DepartmentReportPeriodFilter filter) {
        try {
            BooleanBuilder where = new BooleanBuilder();

            if (filter.isCorrection() != null) {
                if (filter.isCorrection()) {
                    where.and(departmentReportPeriod.correctionDate.isNotNull());
                } else {
                    where.and(departmentReportPeriod.correctionDate.isNull());
                }
            }

            if (filter.isActive() != null) {
                if (filter.isActive()) {
                    where.and(departmentReportPeriod.isActive.eq((byte) 1));
                } else {
                    where.and(departmentReportPeriod.isActive.eq((byte) 0));
                }
            }

            if (filter.getCorrectionDate() != null) {
                where.and(departmentReportPeriod.correctionDate.eq(filter.getCorrectionDate()));
            }

            if (filter.getDepartmentIdList() != null) {
                where.and(departmentReportPeriod.departmentId.in(filter.getDepartmentIdList()));
            }

            if (filter.getReportPeriodIdList() != null) {
                where.and(departmentReportPeriod.reportPeriodId.in(filter.getReportPeriodIdList()));
            }

            if (filter.getReportPeriod() != null) {
                where.and(departmentReportPeriod.reportPeriodId.eq(filter.getReportPeriod().getId()));
            }

            if (filter.getYearStart() != null) {
                where.and(taxPeriod.year.goe(filter.getYearStart()));
            }

            if (filter.getYearEnd() != null) {
                where.and(taxPeriod.year.loe(filter.getYearEnd()));
            }

            if (filter.getDepartmentId() != null) {
                where.and(departmentReportPeriod.departmentId.eq(filter.getDepartmentId()));

            }


            return sqlQueryFactory.select(departmentReportPeriodQBean)
                    .from(departmentReportPeriod)
                    .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                    .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                    .where(where)
                    .transform(GroupBy.groupBy(departmentReportPeriod.id).list(departmentReportPeriod.id));
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }

//        try {
//            return getNamedParameterJdbcTemplate().queryForList(
//                    String.format(QUERY_TEMPLATE_COMPOSITE_SORT_ID, getFilterString(filter)),
//                    new HashMap<String, Object>(2) {{
//                        put("yearStart", filter.getYearStart());
//                        put("yearEnd", filter.getYearEnd());
//                    }}, Long.class);
//        } catch (DataAccessException e) {
//            LOG.error("", e);
//            throw new DaoException("", e);
//        }
    }

    @Override
    @Transactional(readOnly = false)
    public DepartmentReportPeriod save(DepartmentReportPeriod departmentReportPeriodItem) {
        departmentReportPeriodItem.setId(generateId("seq_department_report_period", Long.class));

        sqlQueryFactory.insert(departmentReportPeriod)
                .columns(departmentReportPeriod.id,
                        departmentReportPeriod.departmentId,
                        departmentReportPeriod.reportPeriodId,
                        departmentReportPeriod.isActive,
                        departmentReportPeriod.correctionDate)
                .values(departmentReportPeriodItem.getId(),
                        departmentReportPeriodItem.getDepartmentId(),
                        departmentReportPeriodItem.getReportPeriod().getId(),
                        true,
                        departmentReportPeriodItem.getCorrectionDate())
                .execute();
        return departmentReportPeriodItem;

    }

    @Override
    @Transactional(readOnly = false)
    public void save(final DepartmentReportPeriod departmentReportPeriod, final List<Integer> departmentIds) {
        for (Integer id : departmentIds) {
            departmentReportPeriod.setDepartmentId(id);
            save(departmentReportPeriod);
        }
    }

    @Override
    public void updateActive(long id, boolean active) {
        getJdbcTemplate().update(
                "update department_report_period set is_active = ? where id = ?",
                new Object[]{active ? 1 : 0, id},
                new int[]{Types.NUMERIC, Types.NUMERIC}
        );
    }

    @Override
    public void updateActive(final List<Long> ids, final Integer report_period_id, final boolean active) {
        try {
            getJdbcTemplate().batchUpdate("update department_report_period set is_active = ? where report_period_id = ? AND id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, active ? 1 : 0);
                    ps.setInt(2, report_period_id);
                    ps.setLong(3, ids.get(i));
                }

                @Override
                public int getBatchSize() {
                    return ids.size();
                }
            });
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void updateCorrectionDate(Long id, LocalDateTime correctionDate) {
        sqlQueryFactory.update(departmentReportPeriod)
                .set(departmentReportPeriod.correctionDate, correctionDate)
                .where(departmentReportPeriod.id.eq(id))
                .execute();
    }

    @Override
    public void delete(long id) {
        sqlQueryFactory.delete(departmentReportPeriod)
                .where(departmentReportPeriod.id.eq(id))
                .execute();
    }

    @Override
    public void delete(final List<Long> ids) {
        try {
            getJdbcTemplate().batchUpdate("delete from department_report_period where id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, ids.get(i));
                        }

                        @Override
                        public int getBatchSize() {
                            return ids.size();
                        }
                    }
            );
        } catch (DataAccessException e) {
            LOG.error("", e);
        }
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
        Integer count = getJdbcTemplate().queryForObject(
                "select count(*) from department_report_period where department_id = ? and report_period_id = ?",
                new Object[]{departmentId, reportPeriodId},
                new int[]{Types.NUMERIC, Types.NUMERIC},
                Integer.class
        );
        return count != 0;
    }

    @Override
    public DepartmentReportPeriod getLast(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "from " +
                            "department_report_period drp, " +
                            "(select max(correction_date) as correction_date, department_id, report_period_id " +
                            "from department_report_period " +
                            "where department_id = ? and report_period_id = ? " +
                            "group by department_id, report_period_id) m " +
                            "where drp.department_id = m.department_id " +
                            "and drp.report_period_id = m.report_period_id " +
                            "and (drp.correction_date = m.correction_date or (m.correction_date is null " +
                            "and drp.correction_date is null))",
                    new Object[]{departmentId, reportPeriodId}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public DepartmentReportPeriod getPrevLast(int departmentId, int reportPeriodId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("departmentId", departmentId).
                    addValue("reportPeriodId", reportPeriodId);
            return getNamedParameterJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date from department_report_period drp " +
                            "where drp.REPORT_PERIOD_ID = :reportPeriodId and drp.DEPARTMENT_ID = :departmentId and " +
                            "drp.CORRECTION_DATE in (select max(CORRECTION_DATE) from department_report_period where " +
                            "REPORT_PERIOD_ID = :reportPeriodId and DEPARTMENT_ID = :departmentId and CORRECTION_DATE " +
                            "not in (select max(CORRECTION_DATE) from department_report_period where " +
                            "REPORT_PERIOD_ID = :reportPeriodId and DEPARTMENT_ID = :departmentId))",
                    params, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "from " +
                            "department_report_period drp " +
                            "where drp.department_id = ? " +
                            "and drp.report_period_id = ? " +
                            "and drp.correction_date is null",
                    new Object[]{departmentId, reportPeriodId}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Integer getCorrectionNumber(int id) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select num from (\n" +
                            "select id, correction_date as corr_date,\n" +
                            "row_number() over(partition by report_period_id, department_id order by correction_date nulls first) - 1 as num\n" +
                            "from department_report_period drp) where id = ?",
                    new Object[]{id},
                    new int[]{Types.NUMERIC}
                    , Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean existLargeCorrection(int departmentId, int reportPeriodId, LocalDateTime correctionDate) {
        try {
            return sqlQueryFactory.select(departmentReportPeriodQBean)
                    .from(departmentReportPeriod)
                    .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                    .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                    .where(departmentReportPeriod.departmentId.eq(departmentId).and(reportPeriod.id.eq(reportPeriodId).and(departmentReportPeriod.correctionDate.gt(correctionDate))))
                    .fetchCount() > 0;
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private class CorrectionDateRowMapper implements RowMapper<Pair<Integer, Date>> {
        @Override
        public Pair<Integer, Date> mapRow(ResultSet rs, int rowNum) throws SQLException {
            int reportPeriodId = rs.getInt("report_period_id");
            Date correctionDate = rs.getDate("correction_date");
            return new Pair<Integer, Date>(reportPeriodId, correctionDate);
        }
    }

    @Override
    public Map<Integer, List<Date>> getCorrectionDateListByReportPeriod(final Collection<Integer> reportPeriodIdList) {
        Map<Integer, List<Date>> retVal = new HashMap<Integer, List<Date>>();
        if (reportPeriodIdList == null) {
            return retVal;
        }
        try {
            MapSqlParameterSource source = new MapSqlParameterSource();
            List<Pair<Integer, Date>> list = getNamedParameterJdbcTemplate().query("select distinct report_period_id, " +
                            "correction_date " +
                            "from department_report_period where " +
                            SqlUtils.transformToSqlInStatement("report_period_id", reportPeriodIdList) + " and correction_date is not null",
                    source, new CorrectionDateRowMapper());

            for (Pair<Integer, Date> pair : list) {
                if (!retVal.containsKey(pair.getFirst())) {
                    retVal.put(pair.getFirst(), new LinkedList<Date>());
                }
                retVal.get(pair.getFirst()).add(pair.getSecond());
            }
            return retVal;

        } catch (EmptyResultDataAccessException e) {
            return retVal;
        }
    }

    @Override
    public List<DepartmentReportPeriod> getClosedForFormTemplate(final int formTemplateId) {
        try {
            return getNamedParameterJdbcTemplate().query("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "from department_report_period drp, form_data fd " +
                            "where drp.id = fd.department_report_period_id " +
                            "and drp.is_active = 0 and fd.form_template_id = :formTemplateId",
                    new HashMap<String, Object>(2) {{
                        put("formTemplateId", formTemplateId);
                    }}, mapper);
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Integer> getIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId) {
        String query = "select drp.id from department_report_period drp \n" +
                "join department_report_period drp2 on drp2.id = :departmentReportPeriodId\n" +
                "where drp.department_id in (select id from department where type = :departmentType) \n" +
                "and drp.report_period_id = drp2.report_period_id and (drp.correction_date is null and drp2.correction_date is null or drp.correction_date = drp2.correction_date)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentType", departmentTypeCode);
        params.addValue("departmentReportPeriodId", departmentReportPeriodId);
        try {
            return getNamedParameterJdbcTemplate().queryForList(query, params, Integer.class);
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<Integer>();
        }
    }

    @Override
    public PagingResult<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter filter, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();
        if (filter.getDepartmentId() != null) {
            where.and(departmentReportPeriod.departmentId.eq(filter.getDepartmentId()));
        } else {
            where.and(departmentReportPeriod.departmentId.eq(0));
        }
        where.and(taxPeriod.year.between(filter.getYearStart().shortValue(), filter.getYearEnd().shortValue()))
                .and(taxPeriod.taxType.eq(String.valueOf(TaxType.NDFL.getCode())));
        if (filter.getDepartmentId() != null) {
            where.and(departmentReportPeriod.departmentId.eq(filter.getDepartmentId()));
        }

        String orderingProperty = pagingParams.getProperty();
        Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());

        OrderSpecifier order = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                qDepartmentReportPeriodJournalItem, orderingProperty, ascDescOrder, departmentReportPeriod.id.asc());


        List<DepartmentReportPeriodJournalItem> result = sqlQueryFactory.select(
                departmentReportPeriod.id,
                reportPeriod.name,
                taxPeriod.year,
                departmentReportPeriod.isActive,
                departmentReportPeriod.correctionDate,
                notification.deadline,
                reportPeriod.dictTaxPeriodId)
                .from(departmentReportPeriod)
                .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .leftJoin(reportPeriod._notificationFkReportPeriod, notification)
                .where(where)
                .orderBy(order)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(departmentReportPeriod.id).list(qDepartmentReportPeriodJournalItem));

        return new PagingResult<>(result, getTotalCount(where));

    }

    @Override
    public DepartmentReportPeriod findOne(Long id) {
        return sqlQueryFactory.select(departmentReportPeriodQBean)
                .from(departmentReportPeriod)
                .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(departmentReportPeriod.id.eq(id))
                .fetchOne();
    }

    private int getTotalCount(BooleanBuilder where) {
        return (int) sqlQueryFactory.select(
                departmentReportPeriod.id,
                reportPeriod.name,
                taxPeriod.year,
                departmentReportPeriod.isActive,
                departmentReportPeriod.correctionDate)
                .from(departmentReportPeriod)
                .leftJoin(departmentReportPeriod.depRepPerFkRepPeriodId, reportPeriod)
                .leftJoin(reportPeriod.reportPeriodFkTaxperiod, taxPeriod)
                .where(where)
                .fetchCount();

    }

    @Override
    @Transactional(readOnly = false)
    public DepartmentReportPeriod update(DepartmentReportPeriod departmentReportPeriodItem) {

        sqlQueryFactory.update(departmentReportPeriod)
                .where(departmentReportPeriod.id.eq(departmentReportPeriodItem.getId()))
                .set(departmentReportPeriod.reportPeriodId, departmentReportPeriodItem.getReportPeriod().getId())
                .set(taxPeriod.year, departmentReportPeriodItem.getReportPeriod().getTaxPeriod().getYear())
                .execute();

        return departmentReportPeriodItem;

    }
}
