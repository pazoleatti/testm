package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
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

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class NdflPersonDaoImpl extends AbstractDao implements NdflPersonDao {

    @Autowired
    DBUtilsImpl dbUtils;

    private static final String DUPLICATE_ERORR_MSG = "Попытка перезаписать уже сохранённые данные!";

    @Override
    public NdflPerson fetchOne(long ndflPersonId) {
        try {
            NdflPerson ndflPerson = getJdbcTemplate().queryForObject("select " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " +
                            " from ndfl_person np " +
                            " left join REF_BOOK_PERSON r on np.person_id = r.id " +
                            " where np.id = ?",
                    new Object[]{ndflPersonId},
                    new NdflPersonDaoImpl.NdflPersonRowMapper());

            List<NdflPersonIncome> ndflPersonIncomes = fetchNdflPersonIncomeByNdflPerson(ndflPersonId);
            List<NdflPersonDeduction> ndflPersonDeductions = fetchNdflPersonDeductionByNdflPerson(ndflPersonId);
            List<NdflPersonPrepayment> ndflPersonPrepayments = fetchNdflPersonPrepaymentByNdflPerson(ndflPersonId);

            ndflPerson.setIncomes(ndflPersonIncomes);
            ndflPerson.setDeductions(ndflPersonDeductions);
            ndflPerson.setPrepayments(ndflPersonPrepayments);

            return ndflPerson;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Сущность класса NdflPerson с id = %d не найдена в БД", ndflPersonId);
        }
    }

    @Override
    public List<NdflPerson> fetchByDeclarationData(long declarationDataId) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeByDeclarationData(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", np.inp from ndfl_person_income npi "
                    + " inner join ndfl_person np on npi.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public PagingResult<NdflPersonIncomeDTO> fetchPersonIncomeByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select np.inp, " + createColumns(NdflPersonIncome.COLUMNS, "npi") +
                        " from NDFL_PERSON np " +
                        " inner join NDFL_PERSON_INCOME npi on npi.ndfl_person_id = np.id " +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        queryBuilder.append(getWhereByFilter(params, filter.getIncome()));

        filter.getDeduction().setOperationId(null);
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        filter.getPrepayment().setOperationId(null);
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());
        if (!deductionFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and (");
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append("exists(select * from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ")
                        .append(deductionFilter)
                        .append(")");
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!deductionFilter.isEmpty() ? " or " : "");
                queryBuilder.append("exists(select * from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter)
                        .append(")");
            }
            queryBuilder.append(")");
        }

        String alias = pagingParams.getProperty().equals("inp") ? "np." : "npi.";

        String query = queryBuilder.toString();

        queryBuilder.insert(0, "select * from (select a.*, rownum rn from(");
        String endQuery = new Formatter().format("order by %s %s) a) where rn > :startIndex and rowNum <= :count",
                alias.concat(FormatUtils.convertToUnderlineStyle(pagingParams.getProperty())),
                pagingParams.getDirection())
                .toString();

        queryBuilder.append(endQuery);
        params.addValue("startIndex", pagingParams.getStartIndex())
                .addValue("count", pagingParams.getCount());

        List<NdflPersonIncomeDTO> ndflPersonIncomeList = getNamedParameterJdbcTemplate().query(queryBuilder.toString(),
                params,
                new RowMapper<NdflPersonIncomeDTO>() {
                    @Override
                    public NdflPersonIncomeDTO mapRow(ResultSet rs, int i) throws SQLException {
                        NdflPersonIncomeDTO personIncome = new NdflPersonIncomeDTO();

                        personIncome.setRowNum(rs.getBigDecimal("row_num"));
                        personIncome.setNdflPersonId(rs.getLong("ndfl_person_id"));

                        personIncome.setOperationId(rs.getString("operation_id"));
                        personIncome.setOktmo(rs.getString("oktmo"));
                        personIncome.setKpp(rs.getString("kpp"));

                        personIncome.setIncomeCode(rs.getString("income_code"));
                        personIncome.setIncomeType(rs.getString("income_type"));
                        personIncome.setIncomeAccruedDate(rs.getDate("income_accrued_date"));
                        personIncome.setIncomePayoutDate(rs.getDate("income_payout_date"));
                        personIncome.setIncomeAccruedSumm(rs.getBigDecimal("income_accrued_summ"));
                        personIncome.setIncomePayoutSumm(rs.getBigDecimal("income_payout_summ"));
                        personIncome.setTotalDeductionsSumm(rs.getBigDecimal("total_deductions_summ"));
                        personIncome.setTaxBase(rs.getBigDecimal("tax_base"));
                        personIncome.setTaxRate(SqlUtils.getInteger(rs, "tax_rate"));
                        personIncome.setTaxDate(rs.getDate("tax_date"));

                        personIncome.setCalculatedTax(rs.getBigDecimal("calculated_tax"));
                        personIncome.setWithholdingTax(rs.getBigDecimal("withholding_tax"));
                        personIncome.setNotHoldingTax(rs.getBigDecimal("not_holding_tax"));
                        personIncome.setOverholdingTax(rs.getBigDecimal("overholding_tax"));
                        personIncome.setRefoundTax(SqlUtils.getLong(rs, "refound_tax"));

                        personIncome.setTaxTransferDate(rs.getDate("tax_transfer_date"));
                        personIncome.setPaymentDate(rs.getDate("payment_date"));
                        personIncome.setPaymentNumber(rs.getString("payment_number"));
                        personIncome.setTaxSumm(SqlUtils.getLong(rs, "tax_summ"));
                        personIncome.setSourceId(SqlUtils.getLong(rs, "source_id"));
                        personIncome.setInp(rs.getString("inp"));

                        personIncome.setId(SqlUtils.getLong(rs, "id"));
                        personIncome.setModifiedDate(rs.getDate("modified_date"));
                        personIncome.setModifiedBy(rs.getString("modified_by"));
                        return personIncome;
                    }
                });

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<>(ndflPersonIncomeList, totalCount);
    }

    @Override
    public List<NdflPersonDeduction> fetchNdflPersonDeductionByDeclarationData(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", np.inp from ndfl_person_deduction npd "
                    + " inner join ndfl_person np on npd.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public PagingResult<NdflPersonDeductionDTO> fetchPersonDeductionByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select np.inp, " + createColumns(NdflPersonDeduction.COLUMNS, "npd") +
                        " from ndfl_person np " +
                        " inner join ndfl_person_deduction npd on npd.ndfl_person_id = np.id " +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        queryBuilder.append(getWhereByFilter(params, filter.getDeduction()));

        filter.getIncome().setOperationId(null);
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        filter.getPrepayment().setOperationId(null);
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());
        if (!incomeFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("exists(select * from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ")
                        .append(incomeFilter)
                        .append(")");
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " or " : "");
                queryBuilder.append("exists(select * from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter)
                        .append(")");
            }
            queryBuilder.append(")");
        }

        String alias = pagingParams.getProperty().equals("inp") ? "np." : "npd.";

        String query = queryBuilder.toString();

        queryBuilder.insert(0, "select * from (select a.*, rownum rn from(");
        String endQuery = new Formatter().format("order by %s %s) a) where rn > :startIndex and rownum <= :count",
                alias.concat(FormatUtils.convertToUnderlineStyle(pagingParams.getProperty())),
                pagingParams.getDirection())
                .toString();

        queryBuilder.append(endQuery);
        params.addValue("startIndex", pagingParams.getStartIndex())
                .addValue("count", pagingParams.getCount());


        List<NdflPersonDeductionDTO> ndflPersonDeductionList = getNamedParameterJdbcTemplate().query(queryBuilder.toString(),
                params, new RowMapper<NdflPersonDeductionDTO>() {
                    @Override
                    public NdflPersonDeductionDTO mapRow(ResultSet rs, int i) throws SQLException {
                        NdflPersonDeductionDTO personDeduction = new NdflPersonDeductionDTO();

                        personDeduction.setRowNum(rs.getBigDecimal("row_num"));
                        personDeduction.setOperationId(rs.getString("operation_id"));
                        personDeduction.setNdflPersonId(rs.getLong("ndfl_person_id"));

                        personDeduction.setTypeCode(rs.getString("type_code"));

                        personDeduction.setNotifType(rs.getString("notif_type"));
                        personDeduction.setNotifDate(rs.getDate("notif_date"));
                        personDeduction.setNotifNum(rs.getString("notif_num"));
                        personDeduction.setNotifSource(rs.getString("notif_source"));
                        personDeduction.setNotifSumm(rs.getBigDecimal("notif_summ"));

                        personDeduction.setIncomeAccrued(rs.getDate("income_accrued"));
                        personDeduction.setIncomeCode(rs.getString("income_code"));
                        personDeduction.setIncomeSumm(rs.getBigDecimal("income_summ"));

                        personDeduction.setPeriodPrevDate(rs.getDate("period_prev_date"));
                        personDeduction.setPeriodPrevSumm(rs.getBigDecimal("period_prev_summ"));
                        personDeduction.setPeriodCurrDate(rs.getDate("period_curr_date"));
                        personDeduction.setPeriodCurrSumm(rs.getBigDecimal("period_curr_summ"));
                        personDeduction.setSourceId(SqlUtils.getLong(rs, "source_id"));
                        personDeduction.setInp(rs.getString("inp"));

                        personDeduction.setId(SqlUtils.getLong(rs, "id"));
                        personDeduction.setModifiedDate(rs.getDate("modified_date"));
                        personDeduction.setModifiedBy(rs.getString("modified_by"));
                        return personDeduction;
                    }
                });

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<>(ndflPersonDeductionList, totalCount);
    }

    @Override
    public List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByDeclarationData(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", np.inp from ndfl_person_prepayment npp "
                    + " inner join ndfl_person np on npp.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public PagingResult<NdflPersonPrepaymentDTO> fetchPersonPrepaymentByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select np.inp, " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") +
                        " from ndfl_person np" +
                        " inner join ndfl_person_prepayment npp on npp.ndfl_person_id = np.id " +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        queryBuilder.append(getWhereByFilter(params, filter.getPrepayment()));

        filter.getIncome().setOperationId(null);
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        filter.getDeduction().setOperationId(null);
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        if (!incomeFilter.isEmpty() || !deductionFilter.isEmpty()) {
            queryBuilder.append(" and (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("exists(select * from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ")
                        .append(incomeFilter)
                        .append(")");
            }
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " or " : "");
                queryBuilder.append("exists(select * from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ")
                        .append(deductionFilter)
                        .append(")");
            }
            queryBuilder.append(")");
        }

        String alias = pagingParams.getProperty().equals("inp") ? "np." : "npp.";

        String query = queryBuilder.toString();

        queryBuilder.insert(0, "select * from (select a.*, rownum rn from(");
        String endQuery = new Formatter().format("order by %s %s) a) where rn > :startIndex and rownum <= :count",
                alias.concat(FormatUtils.convertToUnderlineStyle(pagingParams.getProperty())),
                pagingParams.getDirection())
                .toString();

        queryBuilder.append(endQuery);
        params.addValue("startIndex", pagingParams.getStartIndex())
                .addValue("count", pagingParams.getCount());

        List<NdflPersonPrepaymentDTO> ndflPersonPrepaymentList = getNamedParameterJdbcTemplate().query(queryBuilder.toString(),
                params,
                new RowMapper<NdflPersonPrepaymentDTO>() {
                    @Override
                    public NdflPersonPrepaymentDTO mapRow(ResultSet rs, int i) throws SQLException {
                        NdflPersonPrepaymentDTO personPrepayment = new NdflPersonPrepaymentDTO();
                        personPrepayment.setRowNum(rs.getBigDecimal("row_num"));
                        personPrepayment.setOperationId(rs.getString("operation_id"));
                        personPrepayment.setNdflPersonId(rs.getLong("ndfl_person_id"));

                        personPrepayment.setSumm(rs.getBigDecimal("summ"));
                        personPrepayment.setNotifNum(rs.getString("notif_num"));
                        personPrepayment.setNotifDate(rs.getDate("notif_date"));
                        personPrepayment.setNotifSource(rs.getString("notif_source"));
                        personPrepayment.setSourceId(SqlUtils.getLong(rs, "source_id"));

                        personPrepayment.setInp(rs.getString("inp"));

                        personPrepayment.setId(SqlUtils.getLong(rs, "id"));
                        personPrepayment.setModifiedDate(rs.getDate("modified_date"));
                        personPrepayment.setModifiedBy(rs.getString("modified_by"));
                        return personPrepayment;
                    }
                });

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<NdflPersonPrepaymentDTO>(ndflPersonPrepaymentList, totalCount);
    }

    @Override
    public List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp from ndfl_person_income npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPersonKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPersonKppOktmoPeriod(List<Long> ndflPersonId, String kpp, String oktmo, Date startDate, Date endDate) {
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
    public List<NdflPersonIncome> fetchIncomesByPeriodAndNdflPersonIdList(List<Long> ndflPersonIdList, Date startDate, Date endDate) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeByPeriodNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeByPeriodNdflPersonIdTemp(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeByPeriodNdflPersonIdTaxDate(long ndflPersonId, int taxRate, Date startDate, Date endDate) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeByPayoutDate(long ndflPersonId, int taxRate, Date startDate, Date endDate) {
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
    public List<NdflPersonDeduction> fetchNdflPersonDeductionWithDeductionsMarkOstalnie(long ndflPersonId, Date startDate, Date endDate) {
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
    public List<NdflPersonDeduction> fetchNdflpersonDeductionWithDeductionsMarkNotOstalnie(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1) {
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
    public List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByPeriodNdflPersonId(long ndflPersonId, int taxRate, Date startDate, Date endDate, boolean prFequals1) {
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
    public PagingResult<NdflPerson> fetchNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        int totalCount = getNdflPersonCountByParameters(declarationDataId, parameters);
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
        return new PagingResult<>(result, totalCount);
    }

    @Override
    public PagingResult<NdflPerson> fetchNdflPersonByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select " + createColumns(NdflPerson.COLUMNS, "np") +
                        " from ndfl_person np " +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());
        if (!incomeFilter.isEmpty() || !deductionFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("exists(select * from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ")
                        .append(incomeFilter)
                        .append(")");
            }
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " or " : "");
                queryBuilder.append("exists(select * from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ")
                        .append(deductionFilter)
                        .append(")");
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() || !deductionFilter.isEmpty() ? " or " : "");
                queryBuilder.append("exists(select * from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter)
                        .append(")");
            }
            queryBuilder.append(")");
        }

        String query = queryBuilder.toString();

        queryBuilder.insert(0, "select * from (select a.*, rownum rn from (");
        String endQuery = new Formatter().format("order by %s %s) a) where rn > :startIndex and rownum <= :count",
                "np.".concat(FormatUtils.convertToUnderlineStyle(pagingParams.getProperty())),
                pagingParams.getDirection())
                .toString();

        queryBuilder.append(endQuery);
        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());

        List<NdflPerson> ndflPersonList = getNamedParameterJdbcTemplate().query(queryBuilder.toString(),
                params,
                new NdflPersonRowMapper());

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<>(ndflPersonList, totalCount);
    }

    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonFilter ndflPersonFilter) {
        StringBuilder queryBuilder = new StringBuilder();
        if (ndflPersonFilter.getInp() != null) {
            queryBuilder.append("and lower(np.inp) like concat(concat('%', :inp), '%') ");
            params.addValue("inp", ndflPersonFilter.getInp().toLowerCase());
        }
        if (ndflPersonFilter.getInnNp() != null) {
            queryBuilder.append("and lower(np.inn_np) like concat(concat('%', :innNp), '%') ");
            params.addValue("innNp", ndflPersonFilter.getInnNp().toLowerCase());
        }
        if (ndflPersonFilter.getInnForeign() != null) {
            queryBuilder.append("and lower(np.inn_foreign) like concat(concat('%', :innForeign), '%') ");
            params.addValue("innForeign", ndflPersonFilter.getInnForeign().toLowerCase());
        }
        if (ndflPersonFilter.getSnils() != null) {
            queryBuilder.append("and lower(np.snils) like concat(concat('%', :snils), '%') ");
            params.addValue("snils", ndflPersonFilter.getSnils().toLowerCase());
        }
        if (ndflPersonFilter.getIdDocNumber() != null) {
            queryBuilder.append("and lower(np.id_doc_number) like concat(concat('%', :idDocNumber), '%') ");
            params.addValue("idDocNumber", ndflPersonFilter.getIdDocNumber().toLowerCase());
        }
        if (ndflPersonFilter.getLastName() != null) {
            queryBuilder.append("and lower(np.last_name) like concat(concat('%', :lastName), '%') ");
            params.addValue("lastName", ndflPersonFilter.getLastName().toLowerCase());
        }
        if (ndflPersonFilter.getFirstName() != null) {
            queryBuilder.append("and lower(np.first_name) like concat(concat('%', :firstName), '%') ");
            params.addValue("firstName", ndflPersonFilter.getFirstName().toLowerCase());
        }
        if (ndflPersonFilter.getMiddleName() != null) {
            queryBuilder.append("and lower(np.middle_name) like concat(concat('%', :middleName), '%') ");
            params.addValue("middleName", ndflPersonFilter.getMiddleName().toLowerCase());
        }
        if (ndflPersonFilter.getDateFrom() != null) {
            queryBuilder.append("and np.birth_day >= trunc(:birthDayFrom) ");
            params.addValue("birthDayFrom", ndflPersonFilter.getDateFrom());
        }
        if (ndflPersonFilter.getDateTo() != null) {
            queryBuilder.append("and np.birth_day <= trunc(:birthDayTo) ");
            params.addValue("birthDayTo", ndflPersonFilter.getDateTo());
        }
        return queryBuilder.toString();
    }

    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonIncomeFilter ndflPersonIncomeFilter) {
        StringBuilder queryBuilder = new StringBuilder();
        if (ndflPersonIncomeFilter.getOperationId() != null) {
            queryBuilder.append("and lower(npi.operation_id) like concat(concat('%', :operationId), '%') ");
            params.addValue("operationId", ndflPersonIncomeFilter.getOperationId().toLowerCase());
        }
        if (ndflPersonIncomeFilter.getKpp() != null) {
            queryBuilder.append("and lower(npi.kpp) like concat(concat('%', :kpp), '%') ");
            params.addValue("kpp", ndflPersonIncomeFilter.getKpp().toLowerCase());
        }
        if (ndflPersonIncomeFilter.getOktmo() != null) {
            queryBuilder.append("and lower(npi.oktmo) like concat(concat('%', :oktmo), '%') ");
            params.addValue("oktmo", ndflPersonIncomeFilter.getOktmo().toLowerCase());
        }
        if (ndflPersonIncomeFilter.getIncomeCode() != null) {
            queryBuilder.append("and lower(npi.income_code) like concat(concat('%', :incomeCode), '%') ");
            params.addValue("incomeCode", ndflPersonIncomeFilter.getIncomeCode().toLowerCase());
        }
        if (ndflPersonIncomeFilter.getIncomeAttr() != null) {
            queryBuilder.append("and lower(npi.income_type) like concat(concat('%', :incomeType), '%') ");
            params.addValue("incomeType", ndflPersonIncomeFilter.getIncomeAttr().toLowerCase());
        }
        if (ndflPersonIncomeFilter.getTaxRate() != null) {
            queryBuilder.append("and npi.tax_rate = :taxRate ");
            params.addValue("taxRate", Integer.valueOf(ndflPersonIncomeFilter.getTaxRate()));
        }
        if (ndflPersonIncomeFilter.getNumberPaymentOrder() != null) {
            queryBuilder.append("and lower(npi.payment_number) like concat(concat('%', :paymentNumber), '%') ");
            params.addValue("paymentNumber", ndflPersonIncomeFilter.getNumberPaymentOrder().toLowerCase());
        }
        if (ndflPersonIncomeFilter.getTransferDateFrom() != null) {
            queryBuilder.append("and npi.tax_transfer_date >= trunc(:taxTransferDateFrom) ");
            params.addValue("taxTransferDateFrom", ndflPersonIncomeFilter.getTransferDateFrom());
        }
        if (ndflPersonIncomeFilter.getTransferDateTo() != null) {
            queryBuilder.append("and npi.tax_transfer_date <= trunc(:taxTransferDateTo) ");
            params.addValue("taxTransferDateTo", ndflPersonIncomeFilter.getTransferDateTo());
        }
        if (ndflPersonIncomeFilter.getCalculationDateFrom() != null) {
            queryBuilder.append("and npi.tax_date >= trunc(:taxDateFrom) ");
            params.addValue("taxDateFrom", ndflPersonIncomeFilter.getCalculationDateFrom());
        }
        if (ndflPersonIncomeFilter.getCalculationDateTo() != null) {
            queryBuilder.append("and npi.tax_date <= trunc(:taxDateTo) ");
            params.addValue("taxDateTo", ndflPersonIncomeFilter.getCalculationDateTo());
        }
        if (ndflPersonIncomeFilter.getPaymentDateFrom() != null) {
            queryBuilder.append("and npi.payment_date >= trunc(:paymentDateFrom) ");
            params.addValue("paymentDateFrom", ndflPersonIncomeFilter.getPaymentDateFrom());
        }
        if (ndflPersonIncomeFilter.getPaymentDateTo() != null) {
            queryBuilder.append("and npi.payment_date <= trunc(:paymentDateTo) ");
            params.addValue("paymentDateTo", ndflPersonIncomeFilter.getPaymentDateTo());
        }
        return queryBuilder.toString();
    }


    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonDeductionFilter ndflPersonDeductionFilter) {
        StringBuilder queryBuilder = new StringBuilder();
        if (ndflPersonDeductionFilter.getOperationId() != null) {
            queryBuilder.append("and lower(npd.operation_id) like concat(concat('%', :operationId), '%') ");
            params.addValue("operationId", ndflPersonDeductionFilter.getOperationId().toLowerCase());
        }
        if (ndflPersonDeductionFilter.getDeductionCode() != null) {
            queryBuilder.append("and lower(npd.type_code) like concat(concat('%', :typeCode), '%') ");
            params.addValue("typeCode", ndflPersonDeductionFilter.getDeductionCode().toLowerCase());
        }
        if (ndflPersonDeductionFilter.getDeductionIncomeCode() != null) {
            queryBuilder.append("and lower(npd.income_code) like concat(concat('%', :incomeCode), '%') ");
            params.addValue("incomeCode", ndflPersonDeductionFilter.getDeductionIncomeCode().toLowerCase());
        }
        if (ndflPersonDeductionFilter.getIncomeAccruedDateFrom() != null) {
            queryBuilder.append("and npd.income_accrued >= trunc(:incomeAccruedDateFrom) ");
            params.addValue("incomeAccruedDateFrom", ndflPersonDeductionFilter.getIncomeAccruedDateFrom());
        }
        if (ndflPersonDeductionFilter.getIncomeAccruedDateTo() != null) {
            queryBuilder.append("and npd.income_accrued <= trunc(:incomeAccruedDateTo) ");
            params.addValue("incomeAccruedDateTo", ndflPersonDeductionFilter.getIncomeAccruedDateTo());
        }
        if (ndflPersonDeductionFilter.getDeductionDateFrom() != null) {
            queryBuilder.append("and npd.period_curr_date >= trunc(:deductionDateFrom) ");
            params.addValue("deductionDateFrom", ndflPersonDeductionFilter.getDeductionDateFrom());
        }

        if (ndflPersonDeductionFilter.getDeductionDateTo() != null) {
            queryBuilder.append("and npd.period_curr_date <= trunc(:deductionDateTo) ");
            params.addValue("deductionDateTo", ndflPersonDeductionFilter.getDeductionDateTo());
        }
        return queryBuilder.toString();
    }


    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter) {
        StringBuilder queryBuilder = new StringBuilder();
        if (ndflPersonPrepaymentFilter.getOperationId() != null) {
            queryBuilder.append("and lower(npp.operation_id) like concat(concat('%', :operationId), '%') ");
            params.addValue("operationId", ndflPersonPrepaymentFilter.getOperationId().toLowerCase());
        }
        if (ndflPersonPrepaymentFilter.getNotifNum() != null) {
            queryBuilder.append("and lower(npp.notif_num) like concat(concat('%', :notifNum), '%') ");
            params.addValue("notifNum", ndflPersonPrepaymentFilter.getNotifNum().toLowerCase());
        }
        if (ndflPersonPrepaymentFilter.getNotifSource() != null) {
            queryBuilder.append("and lower(npp.notif_source) like concat(concat('%', :notifSource), '%') ");
            params.addValue("notifSource", ndflPersonPrepaymentFilter.getNotifSource().toLowerCase());
        }
        if (ndflPersonPrepaymentFilter.getNotifDateFrom() != null) {
            queryBuilder.append("and npp.notif_date >= trunc(:notifDateFrom) ");
            params.addValue("notifDateFrom", ndflPersonPrepaymentFilter.getNotifDateFrom());
        }
        if (ndflPersonPrepaymentFilter.getNotifDateTo() != null) {
            queryBuilder.append("and npp.notif_date <= trunc(:notifDateTo) ");
            params.addValue("notifDateTo", ndflPersonPrepaymentFilter.getNotifDateTo());
        }
        return queryBuilder.toString();
    }

    @Override
    public int getNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters) {
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
    public List<NdflPersonDeduction> fetchNdflPersonDeductionByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npi") + ", null inp from ndfl_person_deduction npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npi") + ", null inp from ndfl_person_prepayment npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public List<NdflPerson> fetchNdflPersonByPairKppOktmo(List<Long> declarationDataId, String kpp, String oktmo, boolean is2Ndfl2) {
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
    public List<NdflPersonPrepayment> fetchNdlPersonPrepaymentByNdflPersonIdList(List<Long> ndflPersonIdList) {
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
    public void save(final Collection<NdflPerson> ndflPersons) {
        saveNewObjects(ndflPersons, NdflPerson.TABLE_NAME, NdflPerson.SEQ, NdflPerson.COLUMNS, NdflPerson.FIELDS);

        saveIncomes(ndflPersons);
        saveDeductions(ndflPersons);
        savePrepayments(ndflPersons);
    }

    private void saveIncomes(Collection<NdflPerson> ndflPersons) {
        List<NdflPersonIncome> allIncomes = new ArrayList<>();
        for (NdflPerson ndflPerson : ndflPersons) {
            List<NdflPersonIncome> incomes = ndflPerson.getIncomes();
            if (incomes == null || incomes.isEmpty()) {
                throw new DaoException("Пропущены обязательные данные о доходах!");
            }
            for (NdflPersonOperation detail : incomes) {
                detail.setNdflPersonId(ndflPerson.getId());
            }
            allIncomes.addAll(incomes);
        }
        if (!allIncomes.isEmpty()) {
            saveNewObjects(allIncomes, NdflPersonIncome.TABLE_NAME, NdflPersonIncome.SEQ, NdflPersonIncome.COLUMNS, NdflPersonIncome.FIELDS);
        }
    }

    private void saveDeductions(Collection<NdflPerson> ndflPersons) {
        List<NdflPersonDeduction> allDeductions = new ArrayList<>();
        for (NdflPerson ndflPerson : ndflPersons) {
            for (NdflPersonOperation detail : ndflPerson.getDeductions()) {
                detail.setNdflPersonId(ndflPerson.getId());
            }
            allDeductions.addAll(ndflPerson.getDeductions());
        }
        if (!allDeductions.isEmpty()) {
            saveNewObjects(allDeductions, NdflPersonDeduction.TABLE_NAME, NdflPersonDeduction.SEQ, NdflPersonDeduction.COLUMNS, NdflPersonDeduction.FIELDS);
        }
    }

    private void savePrepayments(Collection<NdflPerson> ndflPersons) {
        List<NdflPersonPrepayment> allPrepayments = new ArrayList<>();
        for (NdflPerson ndflPerson : ndflPersons) {
            for (NdflPersonOperation detail : ndflPerson.getPrepayments()) {
                detail.setNdflPersonId(ndflPerson.getId());
            }
            allPrepayments.addAll(ndflPerson.getPrepayments());
        }
        if (!allPrepayments.isEmpty()) {
            saveNewObjects(allPrepayments, NdflPersonPrepayment.TABLE_NAME, NdflPersonPrepayment.SEQ, NdflPersonPrepayment.COLUMNS, NdflPersonPrepayment.FIELDS);
        }
    }

    /**
     * Метод сохраняет новый объект в БД и возвращает этот же объект с присвоенным id
     *
     * @param identityObjects объекты обладающий суррогатным ключом
     * @param table           наименование таблицы используемой для хранения данных объекта
     * @param seq             наименование последовательностт используемой для генерации ключей
     * @param columns         массив содержащий наименование столбцов таблицы для вставки в insert
     * @param fields          массив содержащий наименования параметров соответствующих столбцам
     * @param <E>             тип объекта
     */
    private <E extends IdentityObject> void saveNewObjects(Collection<E> identityObjects, String table, String seq, String[] columns, String[] fields) {
        List<Long> ids = dbUtils.getNextIds(seq, identityObjects.size());
        String insert = createInsert(table, columns, fields);
        BeanPropertySqlParameterSource[] batchArgs = new BeanPropertySqlParameterSource[identityObjects.size()];
        int i = 0;
        for (E identityObject : identityObjects) {
            identityObject.setId(ids.get(i));
            batchArgs[i] = new BeanPropertySqlParameterSource(identityObject);
            i++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(insert, batchArgs);
    }

    private static String createInsert(String table, String[] columns, String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table);
        sb.append(toSqlString(columns));
        sb.append(" VALUES ");
        sb.append(toSqlParameters(fields));
        return sb.toString();
    }

    public static String toSqlParameters(String[] fields) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < fields.length; i++) {
            result.add(":" + fields[i]);
        }
        return toSqlString(result.toArray());
    }

    @Override
    public void delete(Long id) {
        int count = getJdbcTemplate().update("delete from ndfl_person where id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить сущность класса NdflPerson с id = %d, так как она не существует", id);
        }
    }

    @Override
    public long deleteByDeclarationId(Long declarationDataId) {
        return getJdbcTemplate().update("delete from NDFL_PERSON where NDFL_PERSON.DECLARATION_DATA_ID = ?", declarationDataId);
    }

    @Override
    public List<NdflPersonIncome> fetchNdflPersonIncomeConsolidatedRNU2Ndfl(long declarationDataId, String kpp, String oktmo) {
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
    public List<NdflPersonIncome> fetchNdflPersonIncomeConsolidatedRNU6Ndfl(long declarationDataId, String kpp, String oktmo) {
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
    public List<NdflPersonDeduction> fetchNdflPersonDeductionByNdflPersonAndOperation(long ndflPersonId, String operationId) {
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
    public List<NdflPersonPrepayment> fetchNdflPeronPrepaymentByNdflPersonAndOperation(long ndflPersonId, String operationId) {
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
    public List<NdflPersonPrepayment> fetchNdflPeronPrepaymentByOperationList(List<String> operationId) {
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
    public NdflPersonIncome fetchOneNdflPersonIncome(long id) {
        try {
            String sql = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp from NDFL_PERSON_INCOME npi " +
                    "where npi.id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource("id", id);
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public NdflPersonDeduction fetchOneNdflPersonDeduction(long id) {
        try {
            String sql = "select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp from NDFL_PERSON_DEDUCTION npd " +
                    "where npd.id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource("id", id);
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public NdflPersonPrepayment fetchOneNdflPersonPrepayment(long id) {
        try {
            String sql = "select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp from NDFL_PERSON_PREPAYMENT npp " +
                    "where npp.id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource("id", id);
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<NdflPerson> fetchNdflPersonByIdList(List<Long> ndflPersonIdList) {
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
                result.addValue(paramName, value);
            }
        }
        return result;
    }

    @Override
    public List<Integer> fetchDublByRowNum(String tableName, Long declarationDataId) {
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
    public Map<Long, List<Integer>> fetchDublByRowNumMap(String tableName, Long declarationDataId) {
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

    @Override
    public List<String> fetchIncomeOperationIdRange(String startOperationId, String endOperationId) {
        String query = "select distinct operation_id from ndfl_person_income where operation_id between :startOperationId and :endOperationId";
        MapSqlParameterSource params = new MapSqlParameterSource("startOperationId", startOperationId);
        params.addValue("endOperationId", endOperationId);
        return getNamedParameterJdbcTemplate().queryForList(query, params, String.class);
    }

    @Override
    public List<String> findIncomeOperationId(List<String> operationIdList) {
        return getNamedParameterJdbcTemplate().queryForList("select distinct operation_id from ndfl_person_income where operation_id in (:operationIdList)",
                new MapSqlParameterSource("operationIdList", operationIdList),
                String.class);
    }

    @Override
    public List<Long> fetchIncomeIdByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().queryForList("select id from ndfl_person_income " +
                            "where ndfl_person_id = ?",
                    new Object[]{ndflPersonId}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Long> fetchDeductionIdByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().queryForList("select id from ndfl_person_deduction " +
                            "where ndfl_person_id = ?",
                    new Object[]{ndflPersonId}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Long> fetchPrepaymentIdByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().queryForList("select id from ndfl_person_prepayment " +
                            "where ndfl_person_id = ?",
                    new Object[]{ndflPersonId}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteNdflPersonIncomeBatch(final List<Long> ids) {
        getJdbcTemplate().batchUpdate("delete from ndfl_person_income where id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setLong(1, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                });
    }

    @Override
    public void deleteNdflPersonDeductionBatch(final List<Long> ids) {
        getJdbcTemplate().batchUpdate("delete from ndfl_person_deduction where id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setLong(1, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                });
    }

    @Override
    public void deleteNdflPersonPrepaymentBatch(final List<Long> ids) {
        getJdbcTemplate().batchUpdate("delete from ndfl_person_prepayment where id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setLong(1, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                });
    }

    @Override
    public void deleteNdflPersonBatch(final List<Long> ids) {
        getJdbcTemplate().batchUpdate("delete from ndfl_person where id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setLong(1, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                });
    }

    @Override
    public boolean checkIncomeExists(long ndflPersonIncomeId, long declarationDataId) {
        String query = "select count(1) from ndfl_person_income npi " +
                "join ndfl_person np on npi.ndfl_person_id = np.id " +
                "join declaration_data dd on np.declaration_data_id = dd.id " +
                "where npi.id = :ndflPersonIncomeId and dd.id = :declarationDataId";
        MapSqlParameterSource params = new MapSqlParameterSource("ndflPersonIncomeId", ndflPersonIncomeId);
        params.addValue("declarationDataId", declarationDataId);
        return getNamedParameterJdbcTemplate().queryForObject(query, params, new RowMapper<Boolean>() {
            @Override
            public Boolean mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getInt(1) == 1;
            }
        });
    }

    @Override
    public boolean checkDeductionExists(long ndflPersonDeductionId, long declarationDataId) {
        String query = "select count(1) from ndfl_person_deduction npd " +
                "join ndfl_person np on npd.ndfl_person_id = np.id " +
                "join declaration_data dd on np.declaration_data_id = dd.id " +
                "where npd.id = :ndflPersonDeductionId and dd.id = :declarationDataId";
        MapSqlParameterSource params = new MapSqlParameterSource("ndflPersonDeductionId", ndflPersonDeductionId);
        params.addValue("declarationDataId", declarationDataId);
        return getNamedParameterJdbcTemplate().queryForObject(query, params, new RowMapper<Boolean>() {
            @Override
            public Boolean mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getInt(1) == 1;
            }
        });
    }

    @Override
    public boolean checkPrepaymentExists(long ndflPersonPrepaymentId, long declarationDataId) {

        String query = "select count(1) from ndfl_person_prepayment npp " +
                "join ndfl_person np on npp.ndfl_person_id = np.id " +
                "join declaration_data dd on np.declaration_data_id = dd.id " +
                "where npp.id = :ndflPersonPrepaymentId and dd.id = :declarationDataId";
        MapSqlParameterSource params = new MapSqlParameterSource("ndflPersonPrepaymentId", ndflPersonPrepaymentId);
        params.addValue("declarationDataId", declarationDataId);
        return getNamedParameterJdbcTemplate().queryForObject(query, params, new RowMapper<Boolean>() {
            @Override
            public Boolean mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getInt(1) == 1;
            }
        });
    }

    @Override
    public void saveIncomes(List<NdflPersonIncome> incomes) {
        saveNewObjects(incomes, NdflPersonIncome.TABLE_NAME, NdflPersonIncome.SEQ, NdflPersonIncome.COLUMNS, NdflPersonIncome.FIELDS);
    }

    @Override
    public void saveDeductions(List<NdflPersonDeduction> deductions) {
        saveNewObjects(deductions, NdflPersonDeduction.TABLE_NAME, NdflPersonDeduction.SEQ, NdflPersonDeduction.COLUMNS, NdflPersonDeduction.FIELDS);
    }

    @Override
    public void savePrepayments(List<NdflPersonPrepayment> prepayments) {
        saveNewObjects(prepayments, NdflPersonPrepayment.TABLE_NAME, NdflPersonPrepayment.SEQ, NdflPersonPrepayment.COLUMNS, NdflPersonPrepayment.FIELDS);
    }

    @Override
    public void updateIncomes(List<NdflPersonIncome> incomes) {
        String sql = "update " + NdflPersonIncome.TABLE_NAME + " set " +
                "operation_id = :operationId, oktmo = :oktmo, kpp = :kpp, income_code = :incomeCode, income_type = :incomeType, " +
                "income_accrued_date = :incomeAccruedDate, income_payout_date = :incomePayoutDate, income_accrued_summ = :incomeAccruedSumm, income_payout_summ = :incomePayoutSumm, total_deductions_summ = :totalDeductionsSumm, " +
                "tax_base = :taxBase, tax_rate = :taxRate, tax_date = :taxDate, calculated_tax = :calculatedTax, withholding_tax = :withholdingTax, not_holding_tax = :notHoldingTax, overholding_tax = :overholdingTax, " +
                "refound_tax = :refoundTax, tax_transfer_date = :taxTransferDate, payment_date = :paymentDate, payment_number = :paymentNumber, tax_summ = :taxSumm, modified_date = :modifiedDate, modified_by = :modifiedBy " +
                "where id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(incomes.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updateDeductions(List<NdflPersonDeduction> deductions) {
        String sql = "update " + NdflPersonDeduction.TABLE_NAME + " set " +
                "type_code = :typeCode, notif_type = :notifType, notif_date = :notifDate, notif_num = :notifNum, notif_source = :notifSource, " +
                "notif_summ = :notifSumm, operation_id = :operationId, income_accrued = :incomeAccrued, income_code = :incomeCode, income_summ = :incomeSumm, " +
                "period_prev_date = :periodPrevDate, period_prev_summ = :periodPrevSumm, period_curr_date = :periodCurrDate, " +
                "period_curr_summ = :periodCurrSumm, modified_date = :modifiedDate, modified_by = :modifiedBy";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deductions.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updatePrepayments(List<NdflPersonPrepayment> prepayments) {
        String sql = "update " + NdflPersonPrepayment.TABLE_NAME + " set " +
                "operation_id = :operationId, summ = :summ, notif_num = :notifNum, notif_date = :notifDate, notif_source = :notifSource, modified_date = :modifiedDate, modified_by = :modifiedBy";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(prepayments.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
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
            person.setBirthDay(rs.getDate("birth_day"));
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

            person.setModifiedDate(rs.getDate("modified_date"));
            person.setModifiedBy(rs.getString("modified_by"));
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
            personIncome.setIncomeAccruedDate(rs.getDate("income_accrued_date"));
            personIncome.setIncomePayoutDate(rs.getDate("income_payout_date"));
            personIncome.setIncomeAccruedSumm(rs.getBigDecimal("income_accrued_summ"));
            personIncome.setIncomePayoutSumm(rs.getBigDecimal("income_payout_summ"));
            personIncome.setTotalDeductionsSumm(rs.getBigDecimal("total_deductions_summ"));
            personIncome.setTaxBase(rs.getBigDecimal("tax_base"));
            personIncome.setTaxRate(SqlUtils.getInteger(rs, "tax_rate"));
            personIncome.setTaxDate(rs.getDate("tax_date"));

            personIncome.setCalculatedTax(rs.getBigDecimal("calculated_tax"));
            personIncome.setWithholdingTax(rs.getBigDecimal("withholding_tax"));
            personIncome.setNotHoldingTax(rs.getBigDecimal("not_holding_tax"));
            personIncome.setOverholdingTax(rs.getBigDecimal("overholding_tax"));
            personIncome.setRefoundTax(SqlUtils.getLong(rs, "refound_tax"));

            personIncome.setTaxTransferDate(rs.getDate("tax_transfer_date"));
            personIncome.setPaymentDate(rs.getDate("payment_date"));
            personIncome.setPaymentNumber(rs.getString("payment_number"));
            personIncome.setTaxSumm(SqlUtils.getLong(rs, "tax_summ"));
            personIncome.setSourceId(SqlUtils.getLong(rs, "source_id"));

            personIncome.setModifiedDate(rs.getDate("modified_date"));
            personIncome.setModifiedBy(rs.getString("modified_by"));
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
            personDeduction.setNotifDate(rs.getDate("notif_date"));
            personDeduction.setNotifNum(rs.getString("notif_num"));
            personDeduction.setNotifSource(rs.getString("notif_source"));
            personDeduction.setNotifSumm(rs.getBigDecimal("notif_summ"));

            personDeduction.setIncomeAccrued(rs.getDate("income_accrued"));
            personDeduction.setIncomeCode(rs.getString("income_code"));
            personDeduction.setIncomeSumm(rs.getBigDecimal("income_summ"));

            personDeduction.setPeriodPrevDate(rs.getDate("period_prev_date"));
            personDeduction.setPeriodPrevSumm(rs.getBigDecimal("period_prev_summ"));
            personDeduction.setPeriodCurrDate(rs.getDate("period_curr_date"));
            personDeduction.setPeriodCurrSumm(rs.getBigDecimal("period_curr_summ"));
            personDeduction.setSourceId(SqlUtils.getLong(rs, "source_id"));

            personDeduction.setModifiedDate(rs.getDate("modified_date"));
            personDeduction.setModifiedBy(rs.getString("modified_by"));
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
            personPrepayment.setNotifDate(rs.getDate("notif_date"));
            personPrepayment.setNotifSource(rs.getString("notif_source"));
            personPrepayment.setSourceId(SqlUtils.getLong(rs, "source_id"));

            personPrepayment.setModifiedDate(rs.getDate("modified_date"));
            personPrepayment.setModifiedBy(rs.getString("modified_by"));
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
        Integer personCnt = (Integer) call.execute(params).get("personCnt");
        return personCnt != null ? personCnt : 0;
    }

    @Override
    public void updateOneNdflIncome(NdflPersonIncomeDTO personIncome, TAUserInfo taUserInfo) {
        String sql = "update ndfl_person_income set KPP = :kpp, OKTMO = :oktmo, INCOME_CODE = :incomeCode, INCOME_ACCRUED_DATE = :incomeAccruedDate, " +
                "INCOME_ACCRUED_SUMM = :incomeAccruedSumm, INCOME_TYPE = :incomeType, INCOME_PAYOUT_DATE = :incomePayoutDate, INCOME_PAYOUT_SUMM = :incomePayoutSumm, " +
                "TAX_BASE = :taxBase, TOTAL_DEDUCTIONS_SUMM = :totalDeductionsSumm, TAX_RATE = :taxRate, CALCULATED_TAX = :calculatedTax, WITHHOLDING_TAX = :withholdingTax, " +
                "TAX_DATE = :taxDate, NOT_HOLDING_TAX = :notHoldingTax, OVERHOLDING_TAX = :overholdingTax, REFOUND_TAX = :refoundTax, TAX_TRANSFER_DATE = :taxTransferDate," +
                "TAX_SUMM = :taxSumm, PAYMENT_DATE = :paymentDate, PAYMENT_NUMBER = :paymentNumber, MODIFIED_DATE = sysdate, MODIFIED_BY = :user where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("kpp", personIncome.getKpp());
        params.addValue("oktmo", personIncome.getOktmo());
        params.addValue("incomeCode", personIncome.getIncomeCode());
        params.addValue("incomeAccruedDate", personIncome.getIncomeAccruedDate());
        params.addValue("incomeAccruedSumm", personIncome.getIncomeAccruedSumm());
        params.addValue("incomeType", personIncome.getIncomeType());
        params.addValue("incomePayoutDate", personIncome.getIncomePayoutDate());
        params.addValue("incomePayoutSumm", personIncome.getIncomePayoutSumm());
        params.addValue("taxBase", personIncome.getTaxBase());
        params.addValue("totalDeductionsSumm", personIncome.getTotalDeductionsSumm());
        params.addValue("taxRate", personIncome.getTaxRate());
        params.addValue("calculatedTax", personIncome.getCalculatedTax());
        params.addValue("withholdingTax", personIncome.getWithholdingTax());
        params.addValue("taxDate", personIncome.getTaxDate());
        params.addValue("notHoldingTax", personIncome.getNotHoldingTax());
        params.addValue("overholdingTax", personIncome.getOverholdingTax());
        params.addValue("refoundTax", personIncome.getRefoundTax());
        params.addValue("taxTransferDate", personIncome.getTaxTransferDate());
        params.addValue("taxSumm", personIncome.getTaxSumm());
        params.addValue("paymentDate", personIncome.getPaymentDate());
        params.addValue("paymentNumber", personIncome.getPaymentNumber());
        params.addValue("user", taUserInfo.getUser().getName() + " (" + taUserInfo.getUser().getLogin() + ")");
        params.addValue("id", personIncome.getId());

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void updateOneNdflDeduction(NdflPersonDeductionDTO personDeduction, TAUserInfo taUserInfo) {
        String sql = "update ndfl_person_deduction set TYPE_CODE = :typeCode, NOTIF_TYPE = :notifType, NOTIF_NUM = :notifNum, NOTIF_SUMM = :notifSumm, " +
                "NOTIF_SOURCE = :notifSource, NOTIF_DATE = :notifDate, INCOME_CODE = :incomeCode, INCOME_SUMM = :incomeSumm, INCOME_ACCRUED = :incomeAccrued, " +
                "PERIOD_PREV_DATE = :periodPrevDate, PERIOD_PREV_SUMM = :periodPrevSumm, PERIOD_CURR_DATE = :periodCurrDate, PERIOD_CURR_SUMM = :periodCurrSumm, " +
                "MODIFIED_DATE = sysdate, MODIFIED_BY = :user where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("typeCode", personDeduction.getTypeCode());
        params.addValue("notifType", personDeduction.getNotifType());
        params.addValue("notifNum", personDeduction.getNotifNum());
        params.addValue("notifSumm", personDeduction.getNotifSumm());
        params.addValue("notifSource", personDeduction.getNotifSource());
        params.addValue("notifDate", personDeduction.getNotifDate());
        params.addValue("incomeCode", personDeduction.getIncomeCode());
        params.addValue("incomeSumm", personDeduction.getIncomeSumm());
        params.addValue("incomeAccrued", personDeduction.getIncomeAccrued());
        params.addValue("periodPrevDate", personDeduction.getPeriodPrevDate());
        params.addValue("periodPrevSumm", personDeduction.getPeriodPrevSumm());
        params.addValue("periodCurrDate", personDeduction.getPeriodCurrDate());
        params.addValue("periodCurrSumm", personDeduction.getPeriodCurrSumm());
        params.addValue("user", taUserInfo.getUser().getName() + " (" + taUserInfo.getUser().getLogin() + ")");
        params.addValue("id", personDeduction.getId());

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void updateOneNdflPrepayment(NdflPersonPrepaymentDTO personPrepayment, TAUserInfo taUserInfo) {
        String sql = "update ndfl_person_prepayment set SUMM = :summ, NOTIF_NUM = :notifNum, NOTIF_SOURCE = :notifSource, " +
                "NOTIF_DATE = :notifDate, MODIFIED_DATE = sysdate, MODIFIED_BY = :user where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("summ", personPrepayment.getSumm());
        params.addValue("notifNum", personPrepayment.getNotifNum());
        params.addValue("notifSource", personPrepayment.getNotifSource());
        params.addValue("notifDate", personPrepayment.getNotifDate());
        params.addValue("user", taUserInfo.getUser().getName() + " (" + taUserInfo.getUser().getLogin() + ")");
        params.addValue("id", personPrepayment.getId());

        getNamedParameterJdbcTemplate().update(sql, params);

    }
}
