package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.model.util.QueryDSLOrderingUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QNdflPerson.ndflPerson;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QNdflPersonDeduction.ndflPersonDeduction;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QNdflPersonIncome.ndflPersonIncome;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QNdflPersonPrepayment.ndflPersonPrepayment;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QRefBookPerson.refBookPerson;
import static com.querydsl.core.types.Projections.bean;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class NdflPersonDaoImpl extends AbstractDao implements NdflPersonDao {
    @Autowired
    SQLQueryFactory sqlQueryFactory;

    private final QBean<NdflPerson> ndflPersonBean = bean(NdflPerson.class, ndflPerson.all());

    private final QBean<NdflPersonIncome> ndflPersonIncomeBean = bean(NdflPersonIncome.class, ndflPerson.inp, ndflPersonIncome.incomeCode,
            ndflPersonIncome.incomeType, ndflPersonIncome.incomeAccruedDate, ndflPersonIncome.incomePayoutDate, ndflPersonIncome.kpp, ndflPersonIncome.oktmo,
            ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.incomePayoutSumm, ndflPersonIncome.totalDeductionsSumm, ndflPersonIncome.taxBase,
            ndflPersonIncome.taxRate, ndflPersonIncome.taxDate, ndflPersonIncome.calculatedTax, ndflPersonIncome.withholdingTax, ndflPersonIncome.notHoldingTax,
            ndflPersonIncome.overholdingTax, ndflPersonIncome.refoundTax, ndflPersonIncome.taxTransferDate, ndflPersonIncome.paymentDate, ndflPersonIncome.paymentNumber,
            ndflPersonIncome.taxSumm, ndflPersonIncome.operationId, ndflPersonIncome.sourceId, ndflPersonIncome.rowNum);

    private final QBean<NdflPersonDeduction> ndflPersonDeductionBean = bean(NdflPersonDeduction.class, ndflPerson.inp, ndflPersonDeduction.operationId, ndflPersonDeduction.sourceId,
            ndflPersonDeduction.rowNum, ndflPersonDeduction.typeCode, ndflPersonDeduction.notifType, ndflPersonDeduction.notifDate, ndflPersonDeduction.notifNum, ndflPersonDeduction.notifSource,
            ndflPersonDeduction.notifSumm, ndflPersonDeduction.incomeAccrued, ndflPersonDeduction.incomeCode, ndflPersonDeduction.incomeSumm, ndflPersonDeduction.periodCurrDate,
            ndflPersonDeduction.periodCurrSumm, ndflPersonDeduction.periodPrevDate, ndflPersonDeduction.periodPrevSumm);

    private final QBean<NdflPersonPrepayment> ndflPersonPrepaymentBean = bean(NdflPersonPrepayment.class, ndflPerson.inp, ndflPersonPrepayment.operationId, ndflPersonPrepayment.sourceId,
            ndflPersonPrepayment.rowNum, ndflPersonPrepayment.summ, ndflPersonPrepayment.notifNum, ndflPersonPrepayment.notifDate, ndflPersonPrepayment.notifSource);

    private static final String DUPLICATE_ERORR_MSG = "Попытка перезаписать уже сохранённые данные!";

    @Override
    public NdflPerson get(long ndflPersonId) {
        try {
            NdflPerson ndflPerson = getJdbcTemplate().queryForObject("select " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " +
                            " from ndfl_person np " +
                            " left join REF_BOOK_PERSON r on np.person_id = r.id " +
                            " where np.id = ?",
                    new Object[]{ndflPersonId},
                    new NdflPersonDaoImpl.NdflPersonRowMapper());

            List<NdflPersonIncome> ndflPersonIncomes = findIncomes(ndflPersonId);
            List<NdflPersonDeduction> ndflPersonDeductions = findDeductions(ndflPersonId);
            List<NdflPersonPrepayment> ndflPersonPrepayments = findPrepayments(ndflPersonId);

            ndflPerson.setIncomes(ndflPersonIncomes);
            ndflPerson.setDeductions(ndflPersonDeductions);
            ndflPerson.setPrepayments(ndflPersonPrepayments);

            return ndflPerson;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Сущность класса NdflPerson с id = %d не найдена в БД", ndflPersonId);
        }
    }

    @Override
    public List<NdflPerson> findPerson(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " +
                            " from ndfl_person np " +
                            " left join REF_BOOK_PERSON r on np.person_id = r.id " +
                            " where np.declaration_data_id = ?",
                    new Object[]{declarationDataId},
                    new NdflPersonDaoImpl.NdflPersonRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPerson>();
        }
    }

    @Override
    public int findPersonCount(long declarationDataId) {
        return (int) sqlQueryFactory.from(ndflPerson)
                .leftJoin(ndflPerson.ndflPersonFkPersonId, refBookPerson)
                .where(ndflPerson.declarationDataId.eq(declarationDataId))
                .fetchCount();
    }

    @Override
    public List<NdflPersonIncome> findPersonIncome(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", np.inp from ndfl_person_income npi "
                    + " inner join ndfl_person np on npi.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public int findPersonIncomeCount(long declarationDataId) {
        return (int) sqlQueryFactory.from(ndflPersonIncome)
                .innerJoin(ndflPersonIncome.ndflPersonIFkNp, ndflPerson)
                .where(ndflPerson.declarationDataId.eq(declarationDataId))
                .fetchCount();
    }

    @Override
    public PagingResult<NdflPersonIncome> findPersonIncomeByParameters(long declarationDataId, NdflPersonIncomeFilter ndflPersonIncomeFilter, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(ndflPerson.declarationDataId.eq(declarationDataId));

        if (ndflPersonIncomeFilter != null) {
            if (ndflPersonIncomeFilter.getInp() != null) {
                where.and(ndflPerson.inp.toLowerCase().contains(ndflPersonIncomeFilter.getInp().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getOperationId() != null) {
                where.and(ndflPersonIncome.operationId.toLowerCase().contains(ndflPersonIncomeFilter.getOperationId().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getKpp() != null) {
                where.and(ndflPersonIncome.kpp.toLowerCase().contains(ndflPersonIncomeFilter.getKpp().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getOktmo() != null) {
                where.and(ndflPersonIncome.oktmo.toLowerCase().contains(ndflPersonIncomeFilter.getOktmo().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getIncomeCode() != null) {
                where.and(ndflPersonIncome.incomeCode.toLowerCase().contains(ndflPersonIncomeFilter.getIncomeCode().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getIncomeAttr() != null) {
                where.and(ndflPersonIncome.incomeType.toLowerCase().contains(ndflPersonIncomeFilter.getIncomeAttr().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getTaxRate() != null) {
                where.and(ndflPersonIncome.taxRate.stringValue().contains(ndflPersonIncomeFilter.getTaxRate()));
            }
            if (ndflPersonIncomeFilter.getNumberPaymentOrder() != null) {
                where.and(ndflPersonIncome.paymentNumber.toLowerCase().contains(ndflPersonIncomeFilter.getNumberPaymentOrder().toLowerCase()));
            }
            if (ndflPersonIncomeFilter.getTransferDateFrom() != null) {
                where.and(ndflPersonIncome.taxTransferDate.isNull().or(ndflPersonIncome.taxTransferDate.goe(new LocalDateTime(ndflPersonIncomeFilter.getTransferDateFrom()))));
            }

            if (ndflPersonIncomeFilter.getTransferDateTo() != null) {
                where.and(ndflPersonIncome.taxTransferDate.isNull().or(ndflPersonIncome.taxTransferDate.loe(new LocalDateTime(ndflPersonIncomeFilter.getTransferDateTo()))));
            }
            if (ndflPersonIncomeFilter.getCalculationDateFrom() != null) {
                where.and(ndflPersonIncome.taxDate.isNull().or(ndflPersonIncome.taxDate.goe(new LocalDateTime(ndflPersonIncomeFilter.getCalculationDateFrom()))));
            }

            if (ndflPersonIncomeFilter.getCalculationDateTo() != null) {
                where.and(ndflPersonIncome.taxDate.isNull().or(ndflPersonIncome.taxDate.loe(new LocalDateTime(ndflPersonIncomeFilter.getCalculationDateTo()))));
            }
            if (ndflPersonIncomeFilter.getPaymentDateFrom() != null) {
                where.and(ndflPersonIncome.paymentDate.isNull().or(ndflPersonIncome.paymentDate.goe(new LocalDateTime(ndflPersonIncomeFilter.getPaymentDateFrom()))));
            }

            if (ndflPersonIncomeFilter.getPaymentDateTo() != null) {
                where.and(ndflPersonIncome.paymentDate.isNull().or(ndflPersonIncome.paymentDate.loe(new LocalDateTime(ndflPersonIncomeFilter.getPaymentDateTo()))));
            }
        }
        //Определяем способ сортировки
        String orderingProperty = pagingParams.getProperty();
        Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());

        OrderSpecifier order = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                ndflPersonIncomeBean, orderingProperty, ascDescOrder, ndflPersonIncome.rowNum.asc());

        List<NdflPersonIncome> ndflPersonIncomeList = sqlQueryFactory.from(ndflPersonIncome)
                .innerJoin(ndflPersonIncome.ndflPersonIFkNp, ndflPerson)
                .where(where)
                .orderBy(order)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(ndflPersonIncome.id).list(ndflPersonIncomeBean));

        int totalCount = findPersonIncomeCount(declarationDataId);

        return new PagingResult<NdflPersonIncome>(ndflPersonIncomeList, totalCount);
    }

    @Override
    public List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", np.inp from ndfl_person_deduction npd "
                    + " inner join ndfl_person np on npd.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public int findPersonDeductionsCount(long declarationDataId) {
        return (int) sqlQueryFactory.from(ndflPersonDeduction)
                .innerJoin(ndflPersonDeduction.ndflPdFkNp, ndflPerson)
                .where(ndflPerson.declarationDataId.eq(declarationDataId))
                .fetchCount();
    }

    @Override
    public PagingResult<NdflPersonDeduction> findPersonDeductionByParameters(long declarationDataId, NdflPersonDeductionFilter ndflPersonDeductionFilter, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(ndflPerson.declarationDataId.eq(declarationDataId));

        if (ndflPersonDeductionFilter != null) {
            if (ndflPersonDeductionFilter.getInp() != null) {
                where.and(ndflPerson.inp.toLowerCase().contains(ndflPersonDeductionFilter.getInp().toLowerCase()));
            }
            if (ndflPersonDeductionFilter.getOperationId() != null) {
                where.and(ndflPersonDeduction.operationId.toLowerCase().contains(ndflPersonDeductionFilter.getOperationId().toLowerCase()));
            }
            if (ndflPersonDeductionFilter.getDeductionCode() != null) {
                where.and(ndflPersonDeduction.typeCode.toLowerCase().contains(ndflPersonDeductionFilter.getDeductionCode().toLowerCase()));
            }
            if (ndflPersonDeductionFilter.getIncomeCode() != null) {
                where.and(ndflPersonDeduction.incomeCode.toLowerCase().contains(ndflPersonDeductionFilter.getIncomeCode().toLowerCase()));
            }
            if (ndflPersonDeductionFilter.getCalculationDateFrom() != null) {
                where.and(ndflPersonDeduction.incomeAccrued.isNull().or(ndflPersonDeduction.incomeAccrued.goe(new LocalDateTime(ndflPersonDeductionFilter.getCalculationDateFrom()))));
            }
            if (ndflPersonDeductionFilter.getCalculationDateTo() != null) {
                where.and(ndflPersonDeduction.incomeAccrued.isNull().or(ndflPersonDeduction.incomeAccrued.loe(new LocalDateTime(ndflPersonDeductionFilter.getCalculationDateTo()))));
            }
            if (ndflPersonDeductionFilter.getDeductionDateFrom() != null) {
                where.and(ndflPersonDeduction.periodCurrDate.isNull().or(ndflPersonDeduction.periodCurrDate.goe(new LocalDateTime(ndflPersonDeductionFilter.getDeductionDateFrom()))));
            }

            if (ndflPersonDeductionFilter.getDeductionDateTo() != null) {
                where.and(ndflPersonDeduction.periodCurrDate.isNull().or(ndflPersonDeduction.periodCurrDate.loe(new LocalDateTime(ndflPersonDeductionFilter.getDeductionDateTo()))));
            }
        }

        //Определяем способ сортировки
        String orderingProperty = pagingParams.getProperty();
        Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());

        OrderSpecifier order = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                ndflPersonDeductionBean, orderingProperty, ascDescOrder, ndflPersonDeduction.rowNum.asc());

        List<NdflPersonDeduction> ndflPersonDeductionList = sqlQueryFactory.from(ndflPersonDeduction)
                .innerJoin(ndflPersonDeduction.ndflPdFkNp, ndflPerson)
                .where(where)
                .orderBy(order)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(ndflPersonDeduction.id).list(ndflPersonDeductionBean));

        int totalCount = findPersonDeductionsCount(declarationDataId);

        return new PagingResult<NdflPersonDeduction>(ndflPersonDeductionList, totalCount);
    }

    @Override
    public List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", np.inp from ndfl_person_prepayment npp "
                    + " inner join ndfl_person np on npp.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public int findPersonPrepaymentCount(long declarationDataId) {
        return (int) sqlQueryFactory.from(ndflPersonPrepayment)
                .innerJoin(ndflPersonPrepayment.ndflPpFkNp, ndflPerson)
                .where(ndflPerson.declarationDataId.eq(declarationDataId))
                .fetchCount();
    }

    @Override
    public PagingResult<NdflPersonPrepayment> findPersonPrepaymentByParameters(long declarationDataId, NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(ndflPerson.declarationDataId.eq(declarationDataId));

        if (ndflPersonPrepaymentFilter != null) {
            if (ndflPersonPrepaymentFilter.getInp() != null) {
                where.and(ndflPerson.inp.toLowerCase().contains(ndflPersonPrepaymentFilter.getInp().toLowerCase()));
            }
            if (ndflPersonPrepaymentFilter.getOperationId() != null) {
                where.and(ndflPersonPrepayment.operationId.toLowerCase().contains(ndflPersonPrepaymentFilter.getOperationId().toLowerCase()));
            }
            if (ndflPersonPrepaymentFilter.getNotifNum() != null) {
                where.and(ndflPersonPrepayment.notifNum.toLowerCase().contains(ndflPersonPrepaymentFilter.getNotifNum().toLowerCase()));
            }
            if (ndflPersonPrepaymentFilter.getNotifSource() != null) {
                where.and(ndflPersonPrepayment.notifSource.toLowerCase().contains(ndflPersonPrepaymentFilter.getNotifSource().toLowerCase()));
            }
            if (ndflPersonPrepaymentFilter.getNotifDateFrom() != null) {
                where.and(ndflPersonPrepayment.notifDate.isNull().or(ndflPersonPrepayment.notifDate.goe(new LocalDateTime(ndflPersonPrepaymentFilter.getNotifDateFrom()))));
            }
            if (ndflPersonPrepaymentFilter.getNotifDateTo() != null) {
                where.and(ndflPersonPrepayment.notifDate.isNull().or(ndflPersonPrepayment.notifDate.loe(new LocalDateTime(ndflPersonPrepaymentFilter.getNotifDateTo()))));
            }
        }

        //Определяем способ сортировки
        String orderingProperty = pagingParams.getProperty();
        Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());

        OrderSpecifier order = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                ndflPersonPrepaymentBean, orderingProperty, ascDescOrder, ndflPersonPrepayment.rowNum.asc());

        List<NdflPersonPrepayment> ndflPersonPrepaymentList = sqlQueryFactory.from(ndflPersonPrepayment)
                .innerJoin(ndflPersonPrepayment.ndflPpFkNp, ndflPerson)
                .where(where)
                .orderBy(order)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(ndflPersonPrepayment.id).list(ndflPersonPrepaymentBean));

        int totalCount = findPersonPrepaymentCount(declarationDataId);

        return new PagingResult<NdflPersonPrepayment>(ndflPersonPrepaymentList, totalCount);
    }

    @Override
    public List<NdflPersonIncome> findIncomes(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp from ndfl_person_income npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesForPersonByKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo) {
        String sql = "select /*+index(npi idx_ndfl_person_inc_oktmo_kpp)*/ " + createColumns(NdflPersonIncome.COLUMNS, "npi") +
                ", null inp from NDFL_PERSON_INCOME npi where " +
                "npi.NDFL_PERSON_ID in (:ndflPersonId) and npi.OKTMO = :oktmo and npi.KPP = :kpp";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("oktmo", oktmo)
                .addValue("kpp", kpp);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesForPersonByKppOktmoAndPeriod(List<Long> ndflPersonId, String kpp, String oktmo, Date startDate, Date endDate) {
        String sql = "select /*+index(npi idx_ndfl_person_inc_oktmo_kpp)*/ " + createColumns(NdflPersonIncome.COLUMNS, "npi") +
                " from NDFL_PERSON_INCOME npi where " +
                "npi.NDFL_PERSON_ID in (:ndflPersonId) and npi.OKTMO = :oktmo and npi.KPP = :kpp " +
                "AND npi.INCOME_ACCRUED_DATE between :startDate AND :endDate " +
                "UNION SELECT /*+index(npi idx_ndfl_person_inc_oktmo_kpp)*/ " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " FROM ndfl_person_income npi" +
                " WHERE npi.ndfl_person_id in (:ndflPersonId)" +
                " AND npi.tax_date between :startDate AND :endDate" +
                " and npi.OKTMO = :oktmo and npi.KPP = :kpp " +
                " UNION SELECT /*+index(npi idx_ndfl_person_inc_oktmo_kpp)*/ " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " FROM ndfl_person_income npi" +
                " WHERE npi.ndfl_person_id in (:ndflPersonId)" +
                " AND npi.tax_transfer_date between :startDate AND :endDate" +
                " and npi.OKTMO = :oktmo and npi.KPP = :kpp ";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("oktmo", oktmo)
                .addValue("kpp", kpp)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdList(List<Long> ndflPersonIdList, Date startDate, Date endDate) {
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp FROM ndfl_person_income npi" +
                " WHERE npi.ndfl_person_id in (:ndflPersonIdList)" +
                " AND npi.tax_date between :startDate AND :endDate" +
                " UNION SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " FROM ndfl_person_income npi" +
                " WHERE npi.ndfl_person_id in (:ndflPersonIdList)" +
                " AND npi.payment_date between :startDate AND :endDate";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonIdList", ndflPersonIdList)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
        String priznakFClause;
        if (prFequals1) {
            // В качестве временного решения не используется https://conf.aplana.com/pages/viewpage.action?pageId=27176125 п.18
            priznakFClause = /*" AND npi.INCOME_ACCRUED_SUMM <> 0"*/ "";
        } else {
            priznakFClause = " AND npi.NOT_HOLDING_TAX > 0";
        }
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp FROM ndfl_person_income npi " +
                " WHERE npi.ndfl_person_id = :ndflPersonId" +
                " AND npi.INCOME_ACCRUED_DATE between :startDate AND :endDate" + priznakFClause;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdTemp(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
        String priznakFClause;
        if (prFequals1) {
            priznakFClause = "";
        } else {
            priznakFClause = " AND npi.NOT_HOLDING_TAX > 0";
        }
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp FROM ndfl_person_income npi " +
                " WHERE npi.ndfl_person_id = :ndflPersonId" +
                " AND npi.INCOME_ACCRUED_DATE between :startDate AND :endDate" + priznakFClause;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdAndTaxDate(long ndflPersonId, int taxRate, Date startDate, Date endDate) {
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp FROM ndfl_person_income npi " +
                "WHERE npi.operation_id in " +
                "(select npi.operation_id " +
                "from ndfl_person_income npi " +
                "where npi.ndfl_person_id = :ndflPersonId " +
                "AND npi.tax_rate = :taxRate " +
                "AND npi.tax_date between :startDate AND :endDate) " +
                "AND npi.ndfl_person_id = :ndflPersonId";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("taxRate", taxRate)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPayoutDate(long ndflPersonId, int taxRate, Date startDate, Date endDate) {
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " FROM ndfl_person_income npi " +
                "WHERE npi.operation_id in " +
                "(select npi.operation_id " +
                "from ndfl_person_income npi " +
                "where npi.ndfl_person_id = :ndflPersonId " +
                "AND npi.tax_rate = :taxRate " +
                "AND npi.income_payout_date between :startDate AND :endDate) " +
                "AND npi.ndfl_person_id = :ndflPersonId";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("taxRate", taxRate)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsWithDeductionsMarkOstalnie(long ndflPersonId, Date startDate, Date endDate) {
        String sql = "SELECT DISTINCT " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp FROM ndfl_person_deduction npd, (SELECT operation_id, INCOME_ACCRUED_DATE, INCOME_CODE" +
                " FROM NDFL_PERSON_INCOME WHERE ndfl_person_id = :ndflPersonId) i_data  WHERE npd.ndfl_person_id = :ndflPersonId" +
                " AND npd.OPERATION_ID in i_data.operation_id AND npd.INCOME_ACCRUED in i_data.INCOME_ACCRUED_DATE" +
                " AND npd.INCOME_CODE in i_data.INCOME_CODE AND npd.TYPE_CODE in " +
                "(SELECT CODE FROM REF_BOOK_DEDUCTION_TYPE WHERE DEDUCTION_MARK in " +
                "(SELECT ID FROM REF_BOOK_DEDUCTION_MARK WHERE NAME = 'Остальные' AND STATUS = 0)  AND STATUS = 0) AND npd.PERIOD_CURR_DATE between :startDate AND :endDate";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsWithDeductionsMarkNotOstalnie(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
        String priznakFClause = "";
        if (!prFequals1) {
            priznakFClause = " AND npi.not_holding_tax > 0";
        }
        String sql = "SELECT DISTINCT " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp FROM ndfl_person_deduction npd" +
                " INNER JOIN ndfl_person_income npi ON npi.ndfl_person_id = npd.ndfl_person_id AND npi.operation_id = npd.operation_id" +
                " WHERE npd.ndfl_person_id = :ndflPersonId" +
                " AND npd.TYPE_CODE in (SELECT CODE FROM REF_BOOK_DEDUCTION_TYPE WHERE DEDUCTION_MARK in " +
                "(SELECT ID FROM REF_BOOK_DEDUCTION_MARK WHERE NAME in ('Стандартный', 'Социальный', 'Инвестиционный', 'Имущественный') AND STATUS = 0)  AND STATUS = 0)" +
                " AND npd.PERIOD_CURR_DATE between :startDate AND :endDate " + priznakFClause;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, int taxRate, Date startDate, Date endDate, boolean prFequals1) {
        String priznakFClause = "";
        if (!prFequals1) {
            priznakFClause = " AND npi.not_holding_tax > 0";
        }
        String sql = "SELECT " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + " FROM ndfl_person_prepayment npp WHERE npp.ndfl_person_id = :ndflPersonId " +
                "AND npp.operation_id IN (SELECT npi.operation_id FROM ndfl_person_income npi " +
                "WHERE npi.ndfl_person_id = :ndflPersonId " +
                "AND npi.tax_rate = :taxRate " +
                "AND npi.tax_date BETWEEN :startDate AND :endDate" + priznakFClause + ")";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("taxRate", taxRate)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }


    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        parameters.put("declarationDataId", declarationDataId);
        String query = buildQuery(parameters, pagingParams);
        String totalQuery = query;
        if (pagingParams != null) {
            long lastindex = pagingParams.getStartIndex() + pagingParams.getCount();
            totalQuery = "select * from \n (" +
                    "select rating.*, rownum rnum from \n (" +
                    query + ") rating where rownum < " +
                    lastindex +
                    ") where rnum >= " +
                    pagingParams.getStartIndex();
        }
        List<NdflPerson> result = getNamedParameterJdbcTemplate().query(totalQuery, parameters, new NdflPersonDaoImpl.NdflPersonRowMapper());
        return new PagingResult<NdflPerson>(result, getCount(totalQuery, parameters));
    }

    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(NdflPersonFilter ndflPersonFilter, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(ndflPerson.declarationDataId.eq(ndflPersonFilter.getDeclarationDataId()));

        if (ndflPersonFilter != null) {
            if (ndflPersonFilter.getInp() != null) {
                where.and(ndflPerson.inp.toLowerCase().contains(ndflPersonFilter.getInp().toLowerCase()));
            }
            if (ndflPersonFilter.getInnNp() != null) {
                where.and(ndflPerson.innNp.toLowerCase().contains(ndflPersonFilter.getInnNp().toLowerCase()));
            }
            if (ndflPersonFilter.getInnForeign() != null) {
                where.and(ndflPerson.innForeign.toLowerCase().contains(ndflPersonFilter.getInnForeign().toLowerCase()));
            }
            if (ndflPersonFilter.getSnils() != null) {
                where.and(ndflPerson.snils.toLowerCase().contains(ndflPersonFilter.getSnils().toLowerCase()));
            }
            if (ndflPersonFilter.getIdDocNumber() != null) {
                where.and(ndflPerson.idDocNumber.toLowerCase().contains(ndflPersonFilter.getIdDocNumber().toLowerCase()));
            }
            if (ndflPersonFilter.getLastName() != null) {
                where.and(ndflPerson.lastName.toLowerCase().contains(ndflPersonFilter.getLastName().toLowerCase()));
            }
            if (ndflPersonFilter.getFirstName() != null) {
                where.and(ndflPerson.firstName.toLowerCase().contains(ndflPersonFilter.getFirstName().toLowerCase()));
            }
            if (ndflPersonFilter.getMiddleName() != null) {
                where.and(ndflPerson.middleName.toLowerCase().contains(ndflPersonFilter.getMiddleName().toLowerCase()));
            }
            if (ndflPersonFilter.getDateFrom() != null) {
                where.and(ndflPerson.birthDay.isNull().or(ndflPerson.birthDay.goe(new LocalDateTime(ndflPersonFilter.getDateFrom()))));
            }
            if (ndflPersonFilter.getDateTo() != null) {
                where.and(ndflPerson.birthDay.isNull().or(ndflPerson.birthDay.loe(new LocalDateTime(ndflPersonFilter.getDateTo()))));
            }
        }

        //Определяем способ сортировки
        String orderingProperty = pagingParams.getProperty();
        Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());

        OrderSpecifier order = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(
                ndflPersonBean, orderingProperty, ascDescOrder, ndflPerson.rowNum.asc());

        List<NdflPerson> ndflPersonList = sqlQueryFactory.from(ndflPerson)
                .leftJoin(ndflPerson.ndflPersonFkPersonId, refBookPerson)
                .where(where)
                .orderBy(order)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(ndflPerson.id).list(ndflPersonBean));

        int totalCount = findPersonCount(ndflPersonFilter.getDeclarationDataId());

        return new PagingResult<NdflPerson>(ndflPersonList, totalCount);
    }


    @Override
    public int findNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters) {
        parameters.put("declarationDataId", declarationDataId);
        String query = buildCountQuery(parameters);
        return getNamedParameterJdbcTemplate().queryForObject(query, parameters, Integer.class);
    }

    public static String buildQuery(Map<String, Object> parameters, PagingParams pagingParams) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " + " \n");
        sb.append(buildQueryBody(parameters));
        if (pagingParams != null && pagingParams.getProperty() != null) {
            sb.append("order by ").append(pagingParams.getProperty()).append(" ").append(pagingParams.getDirection());
        } else {
            sb.append("ORDER BY np.row_num \n");
        }
        return sb.toString();
    }

    public static String buildCountQuery(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) \n")
                .append(buildQueryBody(params));
        return sb.toString();
    }

    private static String buildQueryBody(Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("FROM ndfl_person np \n");
        sb.append("LEFT JOIN REF_BOOK_PERSON r ON np.person_id = r.id \n");
        sb.append("WHERE np.declaration_data_id = :declarationDataId \n");

        if (parameters != null && !parameters.isEmpty()) {

            if (contains(parameters, SubreportAliasConstants.LAST_NAME)) {
                sb.append("AND lower(np.last_name) like lower(:lastName) \n");
            }

            if (contains(parameters, SubreportAliasConstants.FIRST_NAME)) {
                sb.append("AND lower(np.first_name) like lower(:firstName) \n");
            }

            if (contains(parameters, SubreportAliasConstants.MIDDLE_NAME)) {
                sb.append("AND lower(np.middle_name) like lower(:middleName) \n");
            }

            if (contains(parameters, SubreportAliasConstants.SNILS)) {
                sb.append("AND (translate(lower(np.snils), '0-, ', '0') like translate(lower(:snils), '0-, ', '0')) \n");
            }

            if (contains(parameters, SubreportAliasConstants.INN)) {
                sb.append("AND (translate(lower(np.inn_np), '0-, ', '0') like translate(lower(:inn), '0-, ', '0') OR " +
                        "translate(lower(np.inn_foreign), '0-, ', '0') like translate(lower(:inn), '0-, ', '0')) \n");
            }

            //добавляю для страницы РНУ НДФЛ на angular
            if (contains(parameters, "innNp")) {
                sb.append("AND (translate(lower(np.inn_np), '0-, ', '0') like translate(lower(:innNp), '0-, ', '0')) \n");
            }
            if (contains(parameters, "innForeign")) {
                sb.append("AND (translate(lower(np.inn_foreign), '0-, ', '0') like translate(lower(:innForeign), '0-, ', '0')) \n");
            }

            if (contains(parameters, SubreportAliasConstants.INP)) {
                sb.append("AND lower(np.inp) like lower(:inp) \n");
            }

            if (contains(parameters, SubreportAliasConstants.FROM_BIRTHDAY)) {
                sb.append("AND (np.birth_day is null OR np.birth_day >= :fromBirthDay) \n");
            }

            if (contains(parameters, SubreportAliasConstants.TO_BIRTHDAY)) {
                sb.append("AND (np.birth_day is null OR np.birth_day <= :toBirthDay) \n");
            }

            if (contains(parameters, SubreportAliasConstants.ID_DOC_NUMBER)) {
                sb.append("AND (translate(lower(np.id_doc_number), '0-, ', '0') like translate(lower(:idDocNumber), '0-, ', '0')) \n");
            }
        }
        return sb.toString();
    }

    private static boolean contains(Map<String, Object> param, String key) {
        return param.containsKey(key) && param.get(key) != null;
    }


    /**
     * Метод вернет количество строк в запросе
     *
     * @param sqlQuery   запрос
     * @param parameters параметры сапроса
     * @return количество строк
     */
    @Override
    public int getCount(String sqlQuery, Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from (");
        sb.append(sqlQuery);
        sb.append(")");
        return getNamedParameterJdbcTemplate().queryForObject(sb.toString(), parameters, Integer.class);
    }

    @Override
    public List<NdflPersonDeduction> findDeductions(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npi") + ", null inp from ndfl_person_deduction npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepayments(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npi") + ", null inp from ndfl_person_prepayment npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public List<NdflPerson> findNdflPersonByPairKppOktmo(List<Long> declarationDataId, String kpp, String oktmo, boolean is2Ndfl2) {
        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT /*+rule */")
                .append(createColumns(NdflPerson.COLUMNS, "np"))
                .append(", r.record_id ")
                .append(" FROM ndfl_person np ")
                .append(" JOIN REF_BOOK_PERSON r ON np.person_id = r.id ")
                .append(" JOIN ndfl_person_income npi ")
                .append(" ON np.id = npi.ndfl_person_id ")
                .append(" WHERE npi.kpp = :kpp ")
                .append(" AND npi.oktmo = :oktmo ")
                .append(" AND np.DECLARATION_DATA_ID in (:declarationDataId)");
        if (is2Ndfl2) {
            queryBuilder.append(" AND npi.NOT_HOLDING_TAX IS NOT NULL")
                    .append(" AND npi.NOT_HOLDING_TAX > 0");
        }
        /*String sql = "SELECT DISTINCT *//*+rule *//*" + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " +
                " FROM ndfl_person np " +
                " LEFT JOIN REF_BOOK_PERSON r ON np.person_id = r.id " +
                " INNER JOIN ndfl_person_income npi " +
                " ON np.id = npi.ndfl_person_id " +
                " WHERE npi.kpp = :kpp " +
                " AND npi.oktmo = :oktmo " +
                " AND np.DECLARATION_DATA_ID in (:declarationDataId)";*/
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("declarationDataId", declarationDataId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        try {
            return getNamedParameterJdbcTemplate().query(queryBuilder.toString(), params, new NdflPersonDaoImpl.NdflPersonRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPerson>();
        }
    }


    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByNdflPersonIdList(List<Long> ndflPersonIdList) {
        String sql = "SELECT " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp FROM ndfl_person_prepayment npp " +
                " WHERE npp.ndfl_person_id in (:ndflPersonIdList) ";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonIdList", ndflPersonIdList);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public int[] updateRefBookPersonReferences(List<NaturalPerson> personList) {
        String updateSql = "UPDATE ndfl_person SET person_id = ? WHERE id = ?";
        try {
            int[] result = getJdbcTemplate().batchUpdate(updateSql, new UpdatePersonReferenceBatch(personList));
            return result;
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при обновлении идентификаторов физлиц", e);
        }
    }

    class UpdatePersonReferenceBatch implements BatchPreparedStatementSetter {
        final List<NaturalPerson> personList;

        public UpdatePersonReferenceBatch(List<NaturalPerson> ndflPersonList) {
            this.personList = ndflPersonList;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            NaturalPerson person = personList.get(i);
            ps.setLong(1, person.getId()); //идентификатор записи в справочнике ФЛ
            ps.setLong(2, person.getPrimaryPersonId()); //идентификатор записи в ПНФ
        }

        @Override
        public int getBatchSize() {
            return personList.size();
        }
    }


    @Override
    public Long save(final NdflPerson ndflPerson) {

        saveNewObject(ndflPerson, NdflPerson.TABLE_NAME, NdflPerson.SEQ, NdflPerson.COLUMNS, NdflPerson.FIELDS);

        List<NdflPersonIncome> ndflPersonIncomes = ndflPerson.getIncomes();
        if (ndflPersonIncomes == null || ndflPersonIncomes.isEmpty()) {
            throw new DaoException("Пропущены обязательные данные о доходах!");
        }
        saveDetails(ndflPerson, ndflPersonIncomes);

        List<NdflPersonDeduction> ndflPersonDeductions = ndflPerson.getDeductions();
        saveDetails(ndflPerson, ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = ndflPerson.getPrepayments();
        saveDetails(ndflPerson, ndflPersonPrepayments);

        return ndflPerson.getId();
    }

    private void saveDetails(NdflPerson ndflPerson, List<? extends NdflPersonOperation> details) {
        for (NdflPersonOperation detail : details) {
            detail.setNdflPersonId(ndflPerson.getId());
            saveNewObject(detail, detail.getTableName(), detail.getSeq(), detail.getColumns(), detail.getFields());
        }
    }

    /**
     * Метод сохраняет новый объект в БД и возвращает этот же объект с присвоенным id
     *
     * @param identityObject объект обладающий суррогатным ключом
     * @param table          наименование таблицы используемой для хранения данных объекта
     * @param seq            наименование последовательностт используемой для генерации ключей
     * @param columns        массив содержащий наименование столбцов таблицы для вставки в insert
     * @param fields         массив содержащий наименования параметров соответствующих столбцам
     * @param <E>            тип объекта
     */
    private <E extends IdentityObject> void saveNewObject(E identityObject, String table, String seq, String[] columns, String[] fields) {

        if (identityObject.getId() != null) {
            throw new DaoException(DUPLICATE_ERORR_MSG);
        }

        String insert = createInsert(table, seq, columns, fields);
        NamedParameterJdbcTemplate jdbcTemplate = getNamedParameterJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource sqlParameterSource = prepareParameters(identityObject, fields);

        jdbcTemplate.update(insert, sqlParameterSource, keyHolder, new String[]{"ID"});
        identityObject.setId(keyHolder.getKey().longValue());


    }

    @Override
    public void delete(Long id) {
        int count = getJdbcTemplate().update("delete from ndfl_person where id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить сущность класса NdflPerson с id = %d, так как она не существует", id);
        }
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU2Ndfl(long declarationDataId, String kpp, String oktmo) {
        String oktmoNull = null;
        if (oktmo == null) {
            oktmoNull = "npi.OKTMO is null";
        } else {
            oktmoNull = "npi.OKTMO = :oktmo";
        }
        String query = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp from NDFL_PERSON_INCOME npi " +
                "where npi.KPP = :kpp and " + oktmoNull + " and npi.NDFL_PERSON_ID in " +
                "(select np.id from NDFL_PERSON np where np.PERSON_ID in (select nr.person_id from NDFL_REFERENCES nr " +
                "where nr.DECLARATION_DATA_ID = :declarationDataId and nr.ERRTEXT is not null) " +
                "and np.DECLARATION_DATA_ID in (select dd.id from declaration_data dd " +
                "inner join DECLARATION_DATA_CONSOLIDATION ddc on dd.id = ddc.SOURCE_DECLARATION_DATA_ID " +
                "where ddc.TARGET_DECLARATION_DATA_ID = :declarationDataId))";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        return getNamedParameterJdbcTemplate().query(query, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU6Ndfl(long declarationDataId, String kpp, String oktmo) {
        String oktmoNull = null;
        if (oktmo == null) {
            oktmoNull = "npi.OKTMO is null";
        } else {
            oktmoNull = "npi.OKTMO = :oktmo";
        }
        String query = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp from NDFL_PERSON_INCOME npi " +
                "where npi.KPP = :kpp and " + oktmoNull + " and npi.NDFL_PERSON_ID in " +
                "(select np.id from NDFL_PERSON np where np.DECLARATION_DATA_ID in (select dd.id from declaration_data dd " +
                "inner join DECLARATION_DATA_CONSOLIDATION ddc on dd.id = ddc.SOURCE_DECLARATION_DATA_ID " +
                "where ddc.TARGET_DECLARATION_DATA_ID = :declarationDataId))";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        return getNamedParameterJdbcTemplate().query(query, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsByNdflPersonAndOperation(long ndflPersonId, String operationId) {
        String sql = "select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp " +
                "from NDFL_PERSON_DEDUCTION npd where npd.NDFL_PERSON_ID = :ndflPersonId " +
                "and npd.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ndflPersonId", ndflPersonId)
                .addValue("operationId", operationId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByNdflPersonAndOperation(long ndflPersonId, String operationId) {
        String sql = "select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp " +
                "from NDFL_PERSON_PREPAYMENT npp where npp.NDFL_PERSON_ID = :ndflPersonId " +
                "and npp.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ndflPersonId", ndflPersonId)
                .addValue("operationId", operationId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByOperationList(List<String> operationId) {
        String sql = "select distinct " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp " +
                " from NDFL_PERSON_PREPAYMENT npp join NDFL_PERSON_INCOME npi ON npi.ndfl_person_id = npp.ndfl_person_id where npi.id in (:operationId)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("operationId", operationId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public NdflPersonIncome getIncome(long id) {
        String sql = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp from NDFL_PERSON_INCOME npi " +
                "where npi.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public NdflPersonDeduction getDeduction(long id) {
        String sql = "select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp from NDFL_PERSON_DEDUCTION npd " +
                "where npd.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
    }

    @Override
    public NdflPersonPrepayment getPrepayment(long id) {
        String sql = "select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp from NDFL_PERSON_PREPAYMENT npp " +
                "where npp.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
    }

    @Override
    public List<NdflPerson> findByIdList(List<Long> ndflPersonIdList) {
        String query = "SELECT " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " + " FROM NDFL_PERSON np" +
                " LEFT JOIN REF_BOOK_PERSON r ON np.person_id = r.id " +
                " WHERE NP.ID IN (:ndflPersonIdList)";
        MapSqlParameterSource params = new MapSqlParameterSource("ndflPersonIdList", ndflPersonIdList);
        return getNamedParameterJdbcTemplate().query(query, params, new NdflPersonDaoImpl.NdflPersonRowMapper());
    }

    private <E> MapSqlParameterSource prepareParameters(E entity, String[] fields) {
        MapSqlParameterSource result = new MapSqlParameterSource();
        BeanPropertySqlParameterSource defaultSource = new BeanPropertySqlParameterSource(entity);
        Set fieldsSet = new HashSet<String>();
        fieldsSet.addAll(Arrays.asList(fields));
        for (String paramName : defaultSource.getReadablePropertyNames()) {
            if (fieldsSet.contains(paramName)) {
                Object value = defaultSource.getValue(paramName);
                if (value instanceof LocalDateTime) {
                    value = ((LocalDateTime) value).toDate();
                }
                result.addValue(paramName, value);
            }
        }
        return result;
    }

    @Override
    public List<Integer> findDublRowNum(String tableName, Long declarationDataId) {
        String sql = null;
        if (tableName.equals("NDFL_PERSON")) {
            sql = "SELECT t.row_num FROM " + tableName + " t " +
                    " WHERE t.declaration_data_id =:declaration_data_id AND t.row_num IN " +
                    "(SELECT i.row_num FROM " + tableName + " i WHERE i.row_num IS NOT NULL GROUP BY i.row_num HAVING COUNT(i.row_num) > 1) " +
                    " GROUP BY t.row_num " +
                    " ORDER BY t.row_num";
        } else {
            sql = "SELECT t.row_num FROM " + tableName + " t " +
                    " WHERE t.row_num IN " +
                    "(SELECT i.row_num FROM " + tableName + " i " +
                    " INNER JOIN NDFL_PERSON p ON i.ndfl_person_id = p.id" +
                    " WHERE p.declaration_data_id =:declaration_data_id AND i.row_num IS NOT NULL GROUP BY i.row_num HAVING COUNT(i.row_num) > 1) " +
                    " GROUP BY t.row_num " +
                    " ORDER BY t.row_num";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declaration_data_id", declarationDataId);
        return getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("row_num");
            }
        });
    }

    @Override
    public Map<Long, List<Integer>> findDublRowNumMap(String tableName, Long declarationDataId) {
        String sql = "SELECT i.row_num, i.NDFL_PERSON_ID p_id FROM " + tableName + " i \n" +
                "INNER JOIN NDFL_PERSON p ON i.ndfl_person_id = p.ID \n" +
                "WHERE p.declaration_data_id =:declaration_data_id AND i.row_num IS NOT NULL \n" +
                "GROUP BY i.row_num, i.NDFL_PERSON_ID \n" +
                "HAVING COUNT(i.row_num) > 1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declaration_data_id", declarationDataId);
        final Map<Long, List<Integer>> result = new HashMap<Long, List<Integer>>();
        getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long p_id = rs.getLong("p_id");
                if (!result.containsKey(p_id)) {
                    result.put(p_id, new ArrayList<Integer>());
                }
                result.get(p_id).add(rs.getInt("row_num"));
                return null;
            }
        });

        return result;
    }

    @Override
    public List<Integer> findMissingRowNum(String tableName, Long declarationDataId) {
        String sql = null;
        if (tableName.equals("NDFL_PERSON")) {
            sql = "WITH t AS (SELECT t.row_num FROM " + tableName + " t " +
                    " WHERE t.row_num IS NOT NULL AND t.declaration_data_id =:declaration_data_id ORDER BY t.row_num) " +
                    " SELECT LEVEL AS row_num FROM (SELECT MAX(row_num) AS max_x FROM t) " +
                    " CONNECT BY LEVEL <= max_x MINUS SELECT row_num FROM t " +
                    " MINUS SELECT (CASE WHEN count(*) = 0 THEN 1 ELSE 0 END) row_num  FROM t";
        } else {
            sql = "WITH t AS (SELECT t.row_num FROM " + tableName + " t " +
                    " INNER JOIN NDFL_PERSON p ON t.ndfl_person_id = p.id " +
                    " WHERE t.row_num IS NOT NULL AND p.declaration_data_id =:declaration_data_id ORDER BY t.row_num) " +
                    " SELECT LEVEL AS row_num FROM (SELECT MAX(row_num) AS max_x FROM t) " +
                    " CONNECT BY LEVEL <= max_x MINUS SELECT row_num FROM t " +
                    " MINUS SELECT (CASE WHEN count(*) = 0 THEN 1 ELSE 0 END) row_num  FROM t";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declaration_data_id", declarationDataId);
        return getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("row_num");
            }
        });
    }

    @Override
    public Map<Long, List<Integer>> findMissingRowNumMap(String tableName, Long declarationDataId) {
        String sql = "WITH t AS (SELECT t.row_num, p.ID p_id FROM " + tableName + " t " +
                " INNER JOIN ndfl_person p ON t.ndfl_person_id = p.ID \n" +
                " WHERE t.row_num IS NOT NULL AND p.declaration_data_id =:declaration_data_id \n" +
                " ORDER BY t.row_num, p.ID),\n" +
                " t_cnt AS (SELECT MAX(row_num) cnt, p_id  FROM t GROUP BY p_id)\n" +
                " ,pivot_data_params AS (SELECT MAX(cnt) AS pv_hi_limit FROM t_cnt)\n" +
                " ,pivot_data AS (SELECT ROWNUM AS nr FROM pivot_data_params CONNECT BY ROWNUM < pv_hi_limit+1)\n" +
                " SELECT p_id, nr row_num\n" +
                " FROM t_cnt, pivot_data\n" +
                " WHERE cnt >= nr\n" +
                " MINUS (SELECT p_id, row_num FROM t)\n" +
                " ORDER BY 2 ";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declaration_data_id", declarationDataId);
        final Map<Long, List<Integer>> result = new HashMap<Long, List<Integer>>();
        getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long p_id = rs.getLong("p_id");
                if (!result.containsKey(p_id)) {
                    result.put(p_id, new ArrayList<Integer>());
                }
                result.get(p_id).add(rs.getInt("row_num"));
                return null;
            }
        });

        return result;
    }

    private static String createColumns(String[] columns, String alias) {
        List<String> list = new ArrayList<String>();
        for (String col : columns) {
            list.add(alias + "." + col);
        }
        String columnsNames = toSqlString(list.toArray());
        return columnsNames.replace("(", "").replace(")", "");
    }

    private static String createInsert(String table, String seq, String[] columns, String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table);
        sb.append(toSqlString(columns));
        sb.append(" VALUES ");
        sb.append(toSqlParameters(fields, seq));
        return sb.toString();
    }

    /**
     * Метод преобразует массив {"a", "b", "c"} в строку "(a, b, c)"
     *
     * @param a исходный массив
     * @return строка
     */
    public static String toSqlString(Object[] a) {
        if (a == null) {
            return "";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append('(');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax) {
                return b.append(')').toString();
            }
            b.append(", ");
        }
    }

    public static String toSqlParameters(String[] fields, String seq) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals("id")) {
                result.add(seq + ".nextval");
            } else {
                result.add(":" + fields[i]);
            }
        }
        return toSqlString(result.toArray());
    }

    //>-------------------------<The DAO row mappers>-----------------------------<

    private static final class NdflPersonRowMapper implements RowMapper<NdflPerson> {
        @Override
        public NdflPerson mapRow(ResultSet rs, int index) throws SQLException {

            NdflPerson person = new NdflPerson();

            person.setId(SqlUtils.getLong(rs, "id"));
            person.setDeclarationDataId(SqlUtils.getLong(rs, "declaration_data_id"));
            person.setRowNum(SqlUtils.getLong(rs, "row_num"));
            person.setPersonId(SqlUtils.getLong(rs, "person_id"));

            // Идентификатор ФЛ REF_BOOK_PERSON.RECORD_ID
            if (SqlUtils.isExistColumn(rs, "record_id")) {
                person.setRecordId(SqlUtils.getLong(rs, "record_id"));
            }

            person.setInp(rs.getString("inp"));
            person.setSnils(rs.getString("snils"));
            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setBirthDay(SqlUtils.getLocalDateTime(rs, "birth_day"));
            person.setCitizenship(rs.getString("citizenship"));

            person.setInnNp(rs.getString("inn_np"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setIdDocType(rs.getString("id_doc_type"));
            person.setIdDocNumber(rs.getString("id_doc_number"));
            person.setStatus(rs.getString("status"));
            person.setPostIndex(rs.getString("post_index"));
            person.setRegionCode(rs.getString("region_code"));
            person.setArea(rs.getString("area"));
            person.setCity(rs.getString("city"));

            person.setLocality(rs.getString("locality"));
            person.setStreet(rs.getString("street"));
            person.setHouse(rs.getString("house"));
            person.setBuilding(rs.getString("building"));
            person.setFlat(rs.getString("flat"));
            person.setCountryCode(rs.getString("country_code"));
            person.setAddress(rs.getString("address"));
            person.setAdditionalData(rs.getString("additional_data"));

            return person;
        }
    }


    private static final class NdflPersonIncomeRowMapper implements RowMapper<NdflPersonIncome> {
        @Override
        public NdflPersonIncome mapRow(ResultSet rs, int index) throws SQLException {

            NdflPersonIncome personIncome = new NdflPersonIncome();

            personIncome.setId(SqlUtils.getLong(rs, "id"));
            personIncome.setRowNum(rs.getBigDecimal("row_num"));
            personIncome.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));

            personIncome.setOperationId(rs.getString("operation_id"));
            personIncome.setOktmo(rs.getString("oktmo"));
            personIncome.setKpp(rs.getString("kpp"));

            personIncome.setIncomeCode(rs.getString("income_code"));
            personIncome.setIncomeType(rs.getString("income_type"));
            personIncome.setIncomeAccruedDate(SqlUtils.getLocalDateTime(rs, "income_accrued_date"));
            personIncome.setIncomePayoutDate(SqlUtils.getLocalDateTime(rs, "income_payout_date"));
            personIncome.setIncomeAccruedSumm(rs.getBigDecimal("income_accrued_summ"));
            personIncome.setIncomePayoutSumm(rs.getBigDecimal("income_payout_summ"));
            personIncome.setTotalDeductionsSumm(rs.getBigDecimal("total_deductions_summ"));
            personIncome.setTaxBase(rs.getBigDecimal("tax_base"));
            personIncome.setTaxRate(SqlUtils.getInteger(rs, "tax_rate"));
            personIncome.setTaxDate(SqlUtils.getLocalDateTime(rs, "tax_date"));

            personIncome.setCalculatedTax(rs.getBigDecimal("calculated_tax"));
            personIncome.setWithholdingTax(rs.getBigDecimal("withholding_tax"));
            personIncome.setNotHoldingTax(rs.getBigDecimal("not_holding_tax"));
            personIncome.setOverholdingTax(rs.getBigDecimal("overholding_tax"));
            personIncome.setRefoundTax(SqlUtils.getLong(rs, "refound_tax"));

            personIncome.setTaxTransferDate(SqlUtils.getLocalDateTime(rs, "tax_transfer_date"));
            personIncome.setPaymentDate(SqlUtils.getLocalDateTime(rs, "payment_date"));
            personIncome.setPaymentNumber(rs.getString("payment_number"));
            personIncome.setTaxSumm(SqlUtils.getLong(rs, "tax_summ"));
            personIncome.setSourceId(SqlUtils.getLong(rs, "source_id"));

            return personIncome;
        }
    }

    private static final class NdflPersonDeductionRowMapper implements RowMapper<NdflPersonDeduction> {

        @Override
        public NdflPersonDeduction mapRow(ResultSet rs, int i) throws SQLException {

            NdflPersonDeduction personDeduction = new NdflPersonDeduction();
            personDeduction.setId(SqlUtils.getLong(rs, "id"));
            personDeduction.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personDeduction.setRowNum(rs.getBigDecimal("row_num"));
            personDeduction.setOperationId(rs.getString("operation_id"));

            personDeduction.setTypeCode(rs.getString("type_code"));

            personDeduction.setNotifType(rs.getString("notif_type"));
            personDeduction.setNotifDate(SqlUtils.getLocalDateTime(rs, "notif_date"));
            personDeduction.setNotifNum(rs.getString("notif_num"));
            personDeduction.setNotifSource(rs.getString("notif_source"));
            personDeduction.setNotifSumm(rs.getBigDecimal("notif_summ"));

            personDeduction.setIncomeAccrued(SqlUtils.getLocalDateTime(rs, "income_accrued"));
            personDeduction.setIncomeCode(rs.getString("income_code"));
            personDeduction.setIncomeSumm(rs.getBigDecimal("income_summ"));

            personDeduction.setPeriodPrevDate(SqlUtils.getLocalDateTime(rs, "period_prev_date"));
            personDeduction.setPeriodPrevSumm(rs.getBigDecimal("period_prev_summ"));
            personDeduction.setPeriodCurrDate(SqlUtils.getLocalDateTime(rs, "period_curr_date"));
            personDeduction.setPeriodCurrSumm(rs.getBigDecimal("period_curr_summ"));
            personDeduction.setSourceId(SqlUtils.getLong(rs, "source_id"));

            return personDeduction;
        }
    }

    private static final class NdflPersonPrepaymentRowMapper implements RowMapper<NdflPersonPrepayment> {

        @Override
        public NdflPersonPrepayment mapRow(ResultSet rs, int i) throws SQLException {

            NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
            personPrepayment.setId(SqlUtils.getLong(rs, "id"));
            personPrepayment.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personPrepayment.setRowNum(rs.getBigDecimal("row_num"));
            personPrepayment.setOperationId(rs.getString("operation_id"));

            personPrepayment.setSumm(rs.getBigDecimal("summ"));
            personPrepayment.setNotifNum(rs.getString("notif_num"));
            personPrepayment.setNotifDate(SqlUtils.getLocalDateTime(rs, "notif_date"));
            personPrepayment.setNotifSource(rs.getString("notif_source"));
            personPrepayment.setSourceId(SqlUtils.getLong(rs, "source_id"));

            return personPrepayment;
        }
    }

    @Override
    public int getNdflPersonCount(Long declarationDataId) {
        String query = "select np.id from ndfl_person np where declaration_data_id = :declarationDataId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("declarationDataId", declarationDataId);
        return getCount(query, parameters);
    }

    @Override
    public int getNdflPersonReferencesCount(Long declarationDataId) {
        String query = "select nr.id from ndfl_references nr where declaration_data_id = :declarationDataId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("declarationDataId", declarationDataId);
        return getCount(query, parameters);
    }

    @Override
    public int get6NdflPersonCount(Long declarationDataId) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("report_pkg").withFunctionName("GetNDFL6PersCnt");
        call.declareParameters(new SqlOutParameter("personCnt", Types.INTEGER), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        return (Integer) call.execute(params).get("personCnt");
    }
}
