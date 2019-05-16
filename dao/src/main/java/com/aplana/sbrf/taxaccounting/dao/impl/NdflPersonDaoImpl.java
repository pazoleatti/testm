package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonOperation;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isEmpty;


@Repository
@Transactional
public class NdflPersonDaoImpl extends AbstractDao implements NdflPersonDao {

    @Autowired
    DepartmentDao departmentDao;
    @Autowired
    DepartmentConfigDao departmentConfigDao;
    @Autowired
    DeclarationDataDao declarationDataDao;
    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public NdflPerson findById(long ndflPersonId) {
        try {
            NdflPerson ndflPerson = getJdbcTemplate().queryForObject("select " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id, rba.NAME as asnu_name " +
                            " from ndfl_person np " +
                            " left join REF_BOOK_PERSON r on np.person_id = r.id " +
                            " left join ref_book_asnu rba on np.asnu_id = rba.id" +
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
    public List<NdflPerson> findAllByDeclarationId(long declarationDataId) {
        return getJdbcTemplate().query("select " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id, rba.NAME as asnu_name " +
                        " from ndfl_person np " +
                        " left join REF_BOOK_PERSON r on np.person_id = r.id " +
                        " left join ref_book_asnu rba on np.asnu_id = rba.id" +
                        " where np.declaration_data_id = ?",
                new Object[]{declarationDataId},
                new NdflPersonDaoImpl.NdflPersonRowMapper());
    }

    @Override
    public List<NdflPerson> findAllByDeclarationIdIn(List<Long> declarationDataIds) {
        return selectIn("select " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id, rba.NAME as asnu_name " +
                        " from ndfl_person np " +
                        " left join REF_BOOK_PERSON r on np.person_id = r.id " +
                        " left join ref_book_asnu rba on np.asnu_id = rba.id" +
                        " where np.declaration_data_id in (:declarationDataIds)",
                declarationDataIds,
                "declarationDataIds",
                new NdflPersonDaoImpl.NdflPersonRowMapper()
        );
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByIdIn(List<Long> incomeIds) {
        String sql = "" +
                "select " + createColumns(NdflPersonIncome.COLUMNS, "income") + ", asnu.name as asnu_name, null inp " +
                "from ndfl_person_income income " +
                "   left join ref_book_asnu asnu on income.asnu_id = asnu.id " +
                "where income.id in (:ids)";

        return selectIn(sql, incomeIds, "ids", new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByDeclarationId(long declarationDataId) {
        return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", np.inp, rba.NAME as asnu_name " +
                "from ndfl_person_income npi " +
                "inner join ndfl_person np on npi.ndfl_person_id = np.id " +
                "left join ref_book_asnu rba on npi.asnu_id = rba.id " +
                "where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByDeclarationIdByOrderByRowNumAsc(long declarationDataId) {
        return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", np.inp, rba.NAME as asnu_name " +
                "from ndfl_person_income npi " +
                "inner join ndfl_person np on npi.ndfl_person_id = np.id " +
                "left join ref_book_asnu rba on npi.asnu_id = rba.id " +
                "where np.declaration_data_id = ? " +
                "order by row_num", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPersonIncome> findAllIncomesByDeclarationIds(List<Long> declarationDataIds) {
        return selectIn("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", np.inp, rba.NAME as asnu_name " +
                        "from ndfl_person_income npi "
                        + " inner join ndfl_person np on npi.ndfl_person_id = np.id"
                        + " left join ref_book_asnu rba on npi.asnu_id = rba.id"
                        + " where np.declaration_data_id in (:declarationDataIds)",
                declarationDataIds,
                "declarationDataIds",
                new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public PagingResult<NdflPersonIncomeDTO> fetchPersonIncomeByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select np.inp, rba.name as asnu_name, " + createColumns(NdflPersonIncome.COLUMNS, "outer_npi") +
                        " from NDFL_PERSON np " +
                        " inner join NDFL_PERSON_INCOME outer_npi on outer_npi.ndfl_person_id = np.id " +
                        " left join ref_book_asnu rba on outer_npi.asnu_id = rba.id" +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        appendSqlLikeCondition("operation_id", filter.getIncome().getOperationId(), queryBuilder, params);
        if (filter.getIncome().getAsnu() != null && !filter.getIncome().getAsnu().isEmpty()) {
            queryBuilder.append("and ").append(SqlUtils.transformToSqlInStatementById("outer_npi.asnu_id", filter.getIncome().getAsnu()));
        }
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());
        if (!incomeFilter.isEmpty() || !deductionFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and (ndfl_person_id, operation_id) in (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_INCOME npi where npi.id = outer_npi.id and npi.ndfl_person_id = np.id ")
                        .append(incomeFilter);
            }
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ")
                        .append(deductionFilter);
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() || !deductionFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter);
            }
            queryBuilder.append(")");
        }

        String alias = "";
        switch (pagingParams.getProperty()) {
            case "inp": {
                alias = "np.";
                break;
            }
            case "name": {
                alias = "rba.";
                break;
            }
            default: {
                alias = "outer_npi.";
            }
        }

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
                        BigDecimal rowNum = rs.getBigDecimal("row_num");
                        personIncome.setRowNum(rowNum != null ? rowNum.toString() : "");
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
                        personIncome.setTaxSumm(rs.getBigDecimal("tax_summ"));
                        personIncome.setSourceId(SqlUtils.getLong(rs, "source_id"));
                        personIncome.setInp(rs.getString("inp"));

                        personIncome.setId(SqlUtils.getLong(rs, "id"));
                        personIncome.setModifiedDate(rs.getTimestamp("modified_date"));
                        personIncome.setModifiedBy(rs.getString("modified_by"));
                        personIncome.setAsnuName(rs.getString("asnu_name"));
                        return personIncome;
                    }
                });

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<>(ndflPersonIncomeList, totalCount);
    }

    @Override
    public List<NdflPersonDeduction> findAllDeductionsByDeclarationId(long declarationDataId) {
        return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", np.inp, rba.NAME as asnu_name " +
                "from ndfl_person_deduction npd "
                + " inner join ndfl_person np on npd.ndfl_person_id = np.id"
                + " left join ref_book_asnu rba on npd.asnu_id = rba.id"
                + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
    }

    @Override
    public List<NdflPersonDeduction> findAllDeductionsByDeclarationIds(List<Long> declarationDataIds) {
        return selectIn("select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", np.inp, rba.NAME as asnu_name " +
                        "from ndfl_person_deduction npd "
                        + " inner join ndfl_person np on npd.ndfl_person_id = np.id"
                        + " left join ref_book_asnu rba on npd.asnu_id = rba.id"
                        + " where np.declaration_data_id in (:declarationDataIds)",
                declarationDataIds,
                "declarationDataIds",
                new NdflPersonDaoImpl.NdflPersonDeductionRowMapper()
        );
    }

    @Override
    public PagingResult<NdflPersonDeductionDTO> fetchPersonDeductionByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select np.inp, rba.name as asnu_name, " + createColumns(NdflPersonDeduction.COLUMNS, "outer_npd") +
                        " from ndfl_person np " +
                        " inner join ndfl_person_deduction outer_npd on outer_npd.ndfl_person_id = np.id " +
                        " left join ref_book_asnu rba on outer_npd.asnu_id = rba.id" +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        appendSqlLikeCondition("operation_id", filter.getIncome().getOperationId(), queryBuilder, params);
        if (filter.getIncome().getAsnu() != null && !filter.getIncome().getAsnu().isEmpty()) {
            queryBuilder.append("and ").append(SqlUtils.transformToSqlInStatementById("outer_npd.asnu_id", filter.getIncome().getAsnu()));
        }
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());
        if (!incomeFilter.isEmpty() || !deductionFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and (ndfl_person_id, operation_id) in (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ")
                        .append(incomeFilter);
            }
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_DEDUCTION npd where npd.id = outer_npd.id and npd.ndfl_person_id = np.id ")
                        .append(deductionFilter);
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() || !deductionFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter);
            }
            queryBuilder.append(")");
        }

        String alias = "";
        switch (pagingParams.getProperty()) {
            case "inp": {
                alias = "np.";
                break;
            }
            case "name": {
                alias = "rba.";
                break;
            }
            default: {
                alias = "outer_npd.";
            }
        }

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

                        BigDecimal rowNum = rs.getBigDecimal("row_num");
                        personDeduction.setRowNum(rowNum != null ? rowNum.toString() : "");
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
                        personDeduction.setModifiedDate(rs.getTimestamp("modified_date"));
                        personDeduction.setModifiedBy(rs.getString("modified_by"));
                        personDeduction.setAsnuName(rs.getString("asnu_name"));
                        return personDeduction;
                    }
                });

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<>(ndflPersonDeductionList, totalCount);
    }

    @Override
    public List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByDeclarationData(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", np.inp, rba.NAME as asnu_name " +
                    "from ndfl_person_prepayment npp "
                    + " inner join ndfl_person np on npp.ndfl_person_id = np.id"
                    + " left join ref_book_asnu rba on npp.asnu_id = rba.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public PagingResult<NdflPersonPrepaymentDTO> fetchPersonPrepaymentByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select np.inp, rba.name as asnu_name, " + createColumns(NdflPersonPrepayment.COLUMNS, "outer_npp") +
                        " from ndfl_person np" +
                        " inner join ndfl_person_prepayment outer_npp on outer_npp.ndfl_person_id = np.id " +
                        " left join ref_book_asnu rba on outer_npp.asnu_id = rba.id" +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        appendSqlLikeCondition("operation_id", filter.getIncome().getOperationId(), queryBuilder, params);
        if (filter.getIncome().getAsnu() != null && !filter.getIncome().getAsnu().isEmpty()) {
            queryBuilder.append("and ").append(SqlUtils.transformToSqlInStatementById("outer_npp.asnu_id", filter.getIncome().getAsnu()));
        }
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());
        if (!incomeFilter.isEmpty() || !deductionFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and (ndfl_person_id, operation_id) in (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ")
                        .append(incomeFilter);
            }
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ")
                        .append(deductionFilter);
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() || !deductionFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_PREPAYMENT npp where npp.id = outer_npp.id and npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter);
            }
            queryBuilder.append(")");
        }

        String alias = "";
        switch (pagingParams.getProperty()) {
            case "inp": {
                alias = "np.";
                break;
            }
            case "name": {
                alias = "rba.";
                break;
            }
            default: {
                alias = "outer_npp.";
            }
        }

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
                        BigDecimal rowNum = rs.getBigDecimal("row_num");
                        personPrepayment.setRowNum(rowNum != null ? rowNum.toString() : "");
                        personPrepayment.setOperationId(rs.getString("operation_id"));
                        personPrepayment.setNdflPersonId(rs.getLong("ndfl_person_id"));

                        personPrepayment.setSumm(rs.getBigDecimal("summ"));
                        personPrepayment.setNotifNum(rs.getString("notif_num"));
                        personPrepayment.setNotifDate(rs.getDate("notif_date"));
                        personPrepayment.setNotifSource(rs.getString("notif_source"));
                        personPrepayment.setSourceId(SqlUtils.getLong(rs, "source_id"));

                        personPrepayment.setInp(rs.getString("inp"));

                        personPrepayment.setId(SqlUtils.getLong(rs, "id"));
                        personPrepayment.setModifiedDate(rs.getTimestamp("modified_date"));
                        personPrepayment.setModifiedBy(rs.getString("modified_by"));
                        personPrepayment.setAsnuName(rs.getString("asnu_name"));
                        return personPrepayment;
                    }
                });

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + query + ")", params, Integer.class);

        return new PagingResult<>(ndflPersonPrepaymentList, totalCount);
    }

    @Override
    public List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp, rba.NAME as asnu_name " +
                    "from ndfl_person_income npi " +
                    " left join ref_book_asnu rba on npi.asnu_id = rba.id" +
                    " where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPersonKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo) {
        String sql = "select /*+index(npi idx_ndfl_person_inc_oktmo_kpp)*/ " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp, rba.NAME as asnu_name " +
                "from NDFL_PERSON_INCOME npi " +
                " left join ref_book_asnu rba on npi.asnu_id = rba.id" +
                " where npi.NDFL_PERSON_ID in (:ndflPersonId) and npi.OKTMO = :oktmo and npi.KPP = :kpp";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("oktmo", oktmo)
                .addValue("kpp", kpp);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<NdflPerson> findAllFor2Ndfl(long declarationId, String kpp, String oktmo, Date startDate, Date endDate) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("declarationId", declarationId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        String sql = "join ndfl_person np on op.ndfl_person_id = np.id\n" +
                "where np.declaration_data_id = :declarationId and exists(\n" +
                "  select * from ndfl_person_income\n" +
                "  where ndfl_person_id = np.id and operation_id = op.operation_id " +
                "  and kpp = :kpp and oktmo = :oktmo\n" +
                "  and income_accrued_date between :startDate AND :endDate\n" +
                ")";
        final Map<Long, NdflPerson> personsById = new HashMap<>();
        getNamedParameterJdbcTemplate().query("" +
                        "select distinct " + createColumns(NdflPerson.COLUMNS, "np") + ", null as asnu_name\n" +
                        "from ndfl_person_income op\n" +
                        sql,
                params, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        NdflPerson person = new NdflPersonDaoImpl.NdflPersonRowMapper().mapRow(rs, 0);
                        personsById.put(person.getId(), person);
                    }
                });
        getNamedParameterJdbcTemplate().query("" +
                        "select " + createColumns(NdflPersonIncome.COLUMNS, "op") + ", null as asnu_name\n" +
                        "from ndfl_person_income op\n" +
                        sql,
                params, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        NdflPersonIncome income = new NdflPersonDaoImpl.NdflPersonIncomeRowMapper().mapRow(rs, 0);
                        personsById.get(income.getNdflPersonId()).getIncomes().add(income);
                    }
                });
        getNamedParameterJdbcTemplate().query("" +
                        "select " + createColumns(NdflPersonDeduction.COLUMNS, "op") + ", null as asnu_name\n" +
                        "from ndfl_person_deduction op\n" +
                        sql,
                params, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        NdflPersonDeduction operation = new NdflPersonDaoImpl.NdflPersonDeductionRowMapper().mapRow(rs, 0);
                        personsById.get(operation.getNdflPersonId()).getDeductions().add(operation);
                    }
                });
        getNamedParameterJdbcTemplate().query("" +
                        "select " + createColumns(NdflPersonPrepayment.COLUMNS, "op") + ", null as asnu_name\n" +
                        "from ndfl_person_prepayment op\n" +
                        sql,
                params, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        NdflPersonPrepayment operation = new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper().mapRow(rs, 0);
                        personsById.get(operation.getNdflPersonId()).getPrepayments().add(operation);
                    }
                });
        return new ArrayList<>(personsById.values());
    }

    @Override
    public PagingResult<NdflPerson> fetchNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        int totalCount = getNdflPersonCountByParameters(declarationDataId, parameters);
        parameters.put("declarationDataId", declarationDataId);
        String query = buildQuery(parameters, pagingParams);
        String totalQuery = query;
        if (pagingParams != null) {
            long lastIndex = pagingParams.getStartIndex() + pagingParams.getCount();
            totalQuery = "select * from \n (" +
                    "select rating.*, rownum rnum from \n (" +
                    query + ") rating where rownum <= " +
                    lastIndex +
                    ") where rnum > " +
                    pagingParams.getStartIndex();
        }
        List<NdflPerson> result = getNamedParameterJdbcTemplate().query(totalQuery, parameters, new NdflPersonDaoImpl.NdflPersonRowMapper());
        return new PagingResult<>(result, totalCount);
    }

    @Override
    public PagingResult<NdflPerson> fetchNdflPersonByParameters(NdflFilter filter, PagingParams pagingParams) {
        StringBuilder queryBuilder = new StringBuilder(
                "select rba.name as asnu_name, " + createColumns(NdflPerson.COLUMNS, "np") +
                        " from ndfl_person np " +
                        " left join ref_book_asnu rba on np.asnu_id = rba.id" +
                        " where np.declaration_data_id = :declarationDataId ");

        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", filter.getDeclarationDataId());

        queryBuilder.append(getWhereByFilter(params, filter.getPerson()));
        String incomeFilter = getWhereByFilter(params, filter.getIncome());
        String deductionFilter = getWhereByFilter(params, filter.getDeduction());
        String prepaymentFilter = getWhereByFilter(params, filter.getPrepayment());

        // общие параметры фильтра делаем через UNION
        if (!isEmpty(filter.getIncome().getOperationId()) || !CollectionUtils.isEmpty(filter.getIncome().getAsnu())) {
            queryBuilder.append(" and exists (");
            queryBuilder.append(" select ndfl_person_id, operation_id from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ");
            if (!isEmpty(filter.getIncome().getOperationId())) {
                appendSqlLikeCondition("npi.operation_id", filter.getIncome().getOperationId(), queryBuilder, params);
            }
            if (!CollectionUtils.isEmpty(filter.getIncome().getAsnu())) {
                queryBuilder.append("and ").append(SqlUtils.transformToSqlInStatementById("npi.asnu_id", filter.getIncome().getAsnu()));
            }
            queryBuilder.append(" UNION ALL ");
            queryBuilder.append(" select ndfl_person_id, operation_id from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ");
            if (!isEmpty(filter.getIncome().getOperationId())) {
                appendSqlLikeCondition("npd.operation_id", filter.getIncome().getOperationId(), queryBuilder, params);
            }
            if (!CollectionUtils.isEmpty(filter.getIncome().getAsnu())) {
                queryBuilder.append("and ").append(SqlUtils.transformToSqlInStatementById("npd.asnu_id", filter.getIncome().getAsnu()));
            }
            queryBuilder.append(" UNION ALL ");
            queryBuilder.append(" select ndfl_person_id, operation_id from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ");
            if (!isEmpty(filter.getIncome().getOperationId())) {
                appendSqlLikeCondition("npp.operation_id", filter.getIncome().getOperationId(), queryBuilder, params);
            }
            if (!CollectionUtils.isEmpty(filter.getIncome().getAsnu())) {
                queryBuilder.append("and ").append(SqlUtils.transformToSqlInStatementById("npp.asnu_id", filter.getIncome().getAsnu()));
            }
            queryBuilder.append(")");
        }
        // остальное через INTERSECT, чтобы найденные операции из р2-4 пересекались
        if (!incomeFilter.isEmpty() || !deductionFilter.isEmpty() || !prepaymentFilter.isEmpty()) {
            queryBuilder.append(" and exists (");
            if (!incomeFilter.isEmpty()) {
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_INCOME npi where npi.ndfl_person_id = np.id ")
                        .append(incomeFilter);
            }
            if (!deductionFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_DEDUCTION npd where npd.ndfl_person_id = np.id ")
                        .append(deductionFilter);
            }
            if (!prepaymentFilter.isEmpty()) {
                queryBuilder.append(!incomeFilter.isEmpty() || !deductionFilter.isEmpty() ? " INTERSECT " : "");
                queryBuilder.append("select ndfl_person_id, operation_id from NDFL_PERSON_PREPAYMENT npp where npp.ndfl_person_id = np.id ")
                        .append(prepaymentFilter);
            }
            queryBuilder.append(")");
        }

        String alias = "";
        switch (pagingParams.getProperty()) {
            case "name": {
                alias = "rba.";
                break;
            }
            default: {
                alias = "np.";
            }
        }
        String query = queryBuilder.toString();

        queryBuilder.insert(0, "select * from (select a.*, rownum rn from (");
        String endQuery = new Formatter().format("order by %s %s) a) where rn > :startIndex and rownum <= :count",
                alias.concat(FormatUtils.convertToUnderlineStyle(pagingParams.getProperty())),
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

    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonFilter filter) {
        StringBuilder filterBuilder = new StringBuilder();
        appendSqlLikeCondition("np.inp", filter.getInp(), filterBuilder, params);
        appendSqlLikeCondition("np.last_name", filter.getLastName(), filterBuilder, params);
        appendSqlLikeCondition("np.first_name", filter.getFirstName(), filterBuilder, params);
        appendSqlLikeCondition("np.middle_name", filter.getMiddleName(), filterBuilder, params);
        appendSqlDateBetweenCondition("np.birth_day", filter.getDateFrom(), filter.getDateTo(), filterBuilder, params);
        appendSqlLikeCondition("np.id_doc_type", filter.getIdDocType(), filterBuilder, params);
        appendSqlLikeCondition("np.id_doc_number", filter.getIdDocNumber(), filterBuilder, params, true);
        appendSqlLikeCondition("np.citizenship", filter.getCitizenship(), filterBuilder, params);
        appendSqlLikeCondition("np.status", filter.getStatus(), filterBuilder, params);

        appendSqlLikeCondition("np.region_code", filter.getRegionCode(), filterBuilder, params);
        appendSqlLikeCondition("np.post_index", filter.getPostIndex(), filterBuilder, params);
        appendSqlLikeCondition("np.area", filter.getArea(), filterBuilder, params);
        appendSqlLikeCondition("np.city", filter.getCity(), filterBuilder, params);
        appendSqlLikeCondition("np.locality", filter.getLocality(), filterBuilder, params);
        appendSqlLikeCondition("np.street", filter.getStreet(), filterBuilder, params);
        appendSqlLikeCondition("np.house", filter.getHouse(), filterBuilder, params);
        appendSqlLikeCondition("np.building", filter.getBuilding(), filterBuilder, params);
        appendSqlLikeCondition("np.flat", filter.getFlat(), filterBuilder, params);

        appendSqlLikeCondition("np.snils", filter.getSnils(), filterBuilder, params, true);
        appendSqlLikeCondition("np.inn_np", filter.getInnNp(), filterBuilder, params);
        appendSqlLikeCondition("np.inn_foreign", filter.getInnForeign(), filterBuilder, params);
        appendSqlLikeCondition("np.row_num", filter.getRowNum(), filterBuilder, params);
        appendSqlLikeCondition("np.id", filter.getId(), filterBuilder, params);
        appendSqlDateBetweenCondition("np.modified_date", filter.getModifiedDateFrom(), filter.getModifiedDateTo(), filterBuilder, params);
        appendSqlLikeCondition("np.modified_by", filter.getModifiedBy(), filterBuilder, params);
        return filterBuilder.toString();
    }

    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonIncomeFilter filter) {
        StringBuilder filterBuilder = new StringBuilder();
        appendSqlLikeCondition("npi.kpp", filter.getKpp(), filterBuilder, params);
        appendSqlLikeCondition("npi.oktmo", filter.getOktmo(), filterBuilder, params);
        appendSqlEqualsCondition("npi.tax_rate", filter.getTaxRate(), filterBuilder, params);
        appendSqlLikeCondition("npi.income_code", filter.getIncomeCode(), filterBuilder, params);
        appendSqlLikeCondition("npi.income_type", filter.getIncomeAttr(), filterBuilder, params);

        appendSqlDateBetweenCondition("npi.income_accrued_date", filter.getAccruedDateFrom(), filter.getAccruedDateTo(), filterBuilder, params);
        appendSqlDateBetweenCondition("npi.income_payout_date", filter.getPayoutDateFrom(), filter.getPayoutDateTo(), filterBuilder, params);
        appendSqlDateBetweenCondition("npi.tax_date", filter.getCalculationDateFrom(), filter.getCalculationDateTo(), filterBuilder, params);
        appendSqlDateBetweenCondition("npi.tax_transfer_date", filter.getTransferDateFrom(), filter.getTransferDateTo(), filterBuilder, params);
        appendSqlDateBetweenCondition("npi.payment_date", filter.getPaymentDateFrom(), filter.getPaymentDateTo(), filterBuilder, params);
        appendSqlLikeCondition("npi.payment_number", filter.getNumberPaymentOrder(), filterBuilder, params);

        if (!filter.getUrmList().isEmpty() && !filter.getUrmList().containsAll(asList(URM.values()))) {
            List<URM> urmList = filter.getUrmList();
            List<Pair<String, String>> kppOktmoPairs = getKppOktmoPairsByURM(filter.getNdflFilter().getDeclarationDataId(), urmList);
            if (!filter.getUrmList().contains(URM.NONE_TB)) {
                filterBuilder.append(SqlUtils.pairInStatement("and (npi.kpp, npi.oktmo)", kppOktmoPairs));
            } else {
                filterBuilder.append(SqlUtils.pairInStatement("and not (npi.kpp, npi.oktmo)", kppOktmoPairs));
            }
        }
        appendSqlLikeCondition("npi.row_num", filter.getRowNum(), filterBuilder, params);
        appendSqlLikeCondition("npi.id", filter.getId(), filterBuilder, params);
        appendSqlDateBetweenCondition("npi.modified_date", filter.getModifiedDateFrom(), filter.getModifiedDateTo(), filterBuilder, params);
        appendSqlLikeCondition("npi.modified_by", filter.getModifiedBy(), filterBuilder, params);
        return filterBuilder.toString();
    }

    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonDeductionFilter filter) {
        StringBuilder filterBuilder = new StringBuilder();
        appendSqlLikeCondition("npd.type_code", filter.getDeductionCode(), filterBuilder, params);
        appendSqlDateBetweenCondition("npd.period_prev_date", filter.getPeriodPrevDateFrom(), filter.getPeriodPrevDateTo(), filterBuilder, params);
        appendSqlDateBetweenCondition("npd.period_curr_date", filter.getDeductionDateFrom(), filter.getDeductionDateTo(), filterBuilder, params);

        appendSqlLikeCondition("npd.notif_type", filter.getNotifType(), filterBuilder, params);
        appendSqlLikeCondition("npd.notif_num", filter.getNotifNum(), filterBuilder, params);
        appendSqlLikeCondition("npd.notif_source", filter.getNotifSource(), filterBuilder, params);
        appendSqlDateBetweenCondition("npd.notif_date", filter.getNotifDateFrom(), filter.getNotifDateTo(), filterBuilder, params);

        appendSqlLikeCondition("npd.row_num", filter.getRowNum(), filterBuilder, params);
        appendSqlLikeCondition("npd.id", filter.getId(), filterBuilder, params);
        appendSqlDateBetweenCondition("npd.modified_date", filter.getModifiedDateFrom(), filter.getModifiedDateTo(), filterBuilder, params);
        appendSqlLikeCondition("npd.modified_by", filter.getModifiedBy(), filterBuilder, params);
        return filterBuilder.toString();
    }


    private String getWhereByFilter(MapSqlParameterSource params, NdflPersonPrepaymentFilter filter) {
        StringBuilder filterBuilder = new StringBuilder();
        appendSqlLikeCondition("npp.notif_num", filter.getNotifNum(), filterBuilder, params);
        appendSqlLikeCondition("npp.notif_source", filter.getNotifSource(), filterBuilder, params);

        appendSqlDateBetweenCondition("npp.notif_date", filter.getNotifDateFrom(), filter.getNotifDateTo(), filterBuilder, params);

        appendSqlLikeCondition("npp.row_num", filter.getRowNum(), filterBuilder, params);
        appendSqlLikeCondition("npp.id", filter.getId(), filterBuilder, params);
        appendSqlDateBetweenCondition("npp.modified_date", filter.getModifiedDateFrom(), filter.getModifiedDateTo(), filterBuilder, params);
        appendSqlLikeCondition("npp.modified_by", filter.getModifiedBy(), filterBuilder, params);
        return filterBuilder.toString();
    }

    private void appendSqlDateBetweenCondition(String column, Date dateFrom, Date dateTo, StringBuilder queryBuilder, MapSqlParameterSource params) {
        if (dateFrom != null) {
            String dateFromParam = column.replace('.', '_') + "From";
            queryBuilder.append("and ").append(column).append(" >= trunc(:").append(dateFromParam).append(") ");
            params.addValue(dateFromParam, dateFrom);
        }
        if (dateTo != null) {
            String dateToParam = column.replace('.', '_') + "To";
            queryBuilder.append("and ").append(column).append(" <= trunc(:").append(dateToParam).append(") ");
            params.addValue(dateToParam, dateTo);
        }
    }

    private void appendSqlLikeCondition(String column, String value, StringBuilder queryBuilder, MapSqlParameterSource params) {
        appendSqlLikeCondition(column, value, queryBuilder, params, false);
    }

    private void appendSqlLikeCondition(String column, String value, StringBuilder queryBuilder, MapSqlParameterSource params, boolean onlyAlphanumeric) {
        if (value != null && !value.isEmpty()) {
            String paramName = column.replace('.', '_');
            queryBuilder.append("and lower(")
                    .append(onlyAlphanumeric && isSupportOver() ? "regexp_replace(" : "")
                    .append(column)
                    .append(onlyAlphanumeric && isSupportOver() ? ", '[^[:alnum:]]', '')" : "")
                    .append(") like '%' || ")
                    .append(onlyAlphanumeric && isSupportOver() ? "regexp_replace(:" : ":")
                    .append(paramName)
                    .append(onlyAlphanumeric && isSupportOver() ? ", '[^[:alnum:]]', '')" : "")
                    .append(" || '%' ");
            params.addValue(paramName, value.toLowerCase());
        }
    }

    private void appendSqlEqualsCondition(String column, String value, StringBuilder queryBuilder, MapSqlParameterSource params) {
        if (value != null && !value.isEmpty()) {
            String paramName = column.replace('.', '_');
            queryBuilder.append("and ").append(column).append(" = :").append(paramName).append(" ");
            params.addValue(paramName, Integer.valueOf(value));
        }
    }

    private List<Pair<String, String>> getKppOktmoPairsByURM(Long declarationDataId, List<URM> urmList) {
        if (!urmList.isEmpty() && !urmList.containsAll(asList(URM.values()))) {
            DeclarationData declarationData = declarationDataDao.get(declarationDataId);
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.fetchOne(declarationData.getDepartmentReportPeriodId());
            Integer currentTB = departmentDao.getParentTBId(departmentReportPeriod.getDepartmentId());
            List<Integer> allTB = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());

            if (urmList.size() == 1 && urmList.contains(URM.CURRENT_TB) ||
                    urmList.size() == 2 && urmList.containsAll(asList(URM.OTHERS_TB, URM.NONE_TB))) {
                return departmentConfigDao.findAllKppOktmoPairsByDepartmentIdIn(singletonList(currentTB), new Date());
            } else if (urmList.size() == 1 && urmList.contains(URM.OTHERS_TB) ||
                    urmList.size() == 2 && urmList.containsAll(asList(URM.CURRENT_TB, URM.NONE_TB))) {
                List<Integer> allTbExceptCurrent = new ArrayList<>(allTB);
                allTbExceptCurrent.remove(currentTB);
                return departmentConfigDao.findAllKppOktmoPairsByDepartmentIdIn(allTbExceptCurrent, new Date());
            } else {
                return departmentConfigDao.findAllKppOktmoPairsByDepartmentIdIn(allTB, new Date());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public int getNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters) {
        parameters.put("declarationDataId", declarationDataId);
        String query = buildCountQuery(parameters);
        return getNamedParameterJdbcTemplate().queryForObject(query, parameters, Integer.class);
    }

    public static String buildQuery(Map<String, Object> parameters, PagingParams pagingParams) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id, rba.name as asnu_name " + " \n");
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
        sb.append("left join ref_book_asnu rba on np.asnu_id = rba.id \n");
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
            return getJdbcTemplate().query("" +
                            "select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp, rba.NAME as asnu_name " +
                            "from ndfl_person_deduction npd " +
                            "   left join ref_book_asnu rba on npd.asnu_id = rba.id " +
                            "where npd.ndfl_person_id = ?",
                    new Object[]{ndflPersonId},
                    new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("" +
                            "select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp, rba.NAME as asnu_name " +
                            "from ndfl_person_prepayment npp " +
                            "   left join ref_book_asnu rba on npp.asnu_id = rba.id " +
                            "where npp.ndfl_person_id = ?",
                    new Object[]{ndflPersonId},
                    new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
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

        UpdatePersonReferenceBatch(List<NaturalPerson> ndflPersonList) {
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


    // TODO: Метод нерабочий, т.к. в нём не генерируется ID.
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
     * @deprecated TODO: Не генерирует ID объекта, в итоге не работает
     */
    @Deprecated
    private <E extends IdentityObject> void saveNewObject(E identityObject, String table, String seq, String[] columns, String[] fields) {

        if (identityObject.getId() != null) {
            throw new DaoException("Попытка перезаписать уже сохранённые данные!");
        }

        String insert = SqlUtils.createInsert(table, columns, fields);
        NamedParameterJdbcTemplate jdbcTemplate = getNamedParameterJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource sqlParameterSource = prepareParameters(identityObject, fields);

        jdbcTemplate.update(insert, sqlParameterSource, keyHolder, new String[]{"ID"});
        identityObject.setId(keyHolder.getKey().longValue());


    }

    @Override
    public void saveAll(final Collection<NdflPerson> ndflPersons) {
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


    @Override
    public void delete(Long id) {
        int count = getJdbcTemplate().update("DELETE FROM ndfl_person WHERE id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить сущность класса NdflPerson с id = %d, так как она не существует", id);
        }
    }

    @Override
    public long deleteByDeclarationId(Long declarationDataId) {
        return getJdbcTemplate().update("DELETE FROM NDFL_PERSON WHERE NDFL_PERSON.DECLARATION_DATA_ID = ?", declarationDataId);
    }

    @Override
    public List<NdflPersonDeduction> fetchNdflPersonDeductionByNdflPersonAndOperation(long ndflPersonId, String operationId) {
        String sql = "select rba.NAME as asnu_name, " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp " +
                "from NDFL_PERSON_DEDUCTION npd " +
                " left join ref_book_asnu rba on npd.asnu_id = rba.id " +
                "where npd.NDFL_PERSON_ID = :ndflPersonId " +
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
        String sql = "select rba.NAME as asnu_name, " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp " +
                "from NDFL_PERSON_PREPAYMENT npp " +
                " left join ref_book_asnu rba on npp.asnu_id = rba.id " +
                "where npp.NDFL_PERSON_ID = :ndflPersonId " +
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
    public NdflPersonIncome fetchOneNdflPersonIncome(long id) {
        try {
            String sql = "select rba.name as asnu_name, " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", null inp " +
                    "from NDFL_PERSON_INCOME npi " +
                    " left join ref_book_asnu rba on npi.asnu_id = rba.id " +
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
            String sql = "select rba.NAME as asnu_name, " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + ", null inp " +
                    "from NDFL_PERSON_DEDUCTION npd " +
                    " left join ref_book_asnu rba on npd.asnu_id = rba.id " +
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
            String sql = "select rba.NAME as asnu_name, " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + ", null inp " +
                    "from NDFL_PERSON_PREPAYMENT npp " +
                    " left join ref_book_asnu rba on npp.asnu_id = rba.id " +
                    "where npp.id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource("id", id);
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<NdflPerson> findByIdIn(List<Long> ndflPersonIdList) {
        String query = "SELECT rba.NAME as asnu_name, " + createColumns(NdflPerson.COLUMNS, "np") + ", r.record_id " +
                " FROM NDFL_PERSON np" +
                " LEFT JOIN REF_BOOK_PERSON r ON np.person_id = r.id " +
                " left join ref_book_asnu rba on np.asnu_id = rba.id" +
                " WHERE NP.ID IN (:ndflPersonIdList)";
        return selectIn(query, ndflPersonIdList, "ndflPersonIdList", new NdflPersonDaoImpl.NdflPersonRowMapper());
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
        String query = "SELECT DISTINCT operation_id FROM ndfl_person_income WHERE operation_id BETWEEN :startOperationId AND :endOperationId";
        MapSqlParameterSource params = new MapSqlParameterSource("startOperationId", startOperationId);
        params.addValue("endOperationId", endOperationId);
        return getNamedParameterJdbcTemplate().queryForList(query, params, String.class);
    }

    //TODO: если вызвать с аргументом длиной больше 1000, метод отработает неверно
    @Override
    public List<String> findIncomeOperationId(List<String> operationIdList) {
        return getNamedParameterJdbcTemplate().queryForList("SELECT DISTINCT operation_id FROM ndfl_person_income WHERE operation_id IN (:operationIdList)",
                new MapSqlParameterSource("operationIdList", operationIdList),
                String.class);
    }

    @Override
    public List<Long> fetchIncomeIdByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().queryForList("SELECT id FROM ndfl_person_income " +
                            "WHERE ndfl_person_id = ?",
                    new Object[]{ndflPersonId}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Long> fetchDeductionIdByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().queryForList("SELECT id FROM ndfl_person_deduction " +
                            "WHERE ndfl_person_id = ?",
                    new Object[]{ndflPersonId}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Long> fetchPrepaymentIdByNdflPerson(long ndflPersonId) {
        try {
            return getJdbcTemplate().queryForList("SELECT id FROM ndfl_person_prepayment " +
                            "WHERE ndfl_person_id = ?",
                    new Object[]{ndflPersonId}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteNdflPersonIncomeBatch(final List<Long> ids) {
        getJdbcTemplate().batchUpdate("DELETE FROM ndfl_person_income WHERE id = ?",
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
        getJdbcTemplate().batchUpdate("DELETE FROM ndfl_person_deduction WHERE id = ?",
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
        getJdbcTemplate().batchUpdate("DELETE FROM ndfl_person_prepayment WHERE id = ?",
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
        getJdbcTemplate().batchUpdate("DELETE FROM ndfl_person WHERE id = ?",
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
    public boolean ndflPersonExistsByDeclarationId(long declarationDataId) {
        return getJdbcTemplate().queryForObject(
                "select case when exists (select * from ndfl_person where declaration_data_id = ?) then 1 else 0 end from dual",
                Boolean.class, declarationDataId);
    }

    @Override
    public boolean incomeExistsByDeclarationId(long declarationDataId) {
        return getJdbcTemplate().queryForObject("" +
                        "select case when exists (" +
                        "   select * from ndfl_person_income npi " +
                        "   join ndfl_person np on np.id = npi.ndfl_person_id " +
                        "   where np.declaration_data_id = ?" +
                        ") then 1 else 0 end from dual",
                Boolean.class, declarationDataId);
    }

    @Override
    public boolean checkIncomeExists(long ndflPersonIncomeId, long declarationDataId) {
        String query = "SELECT count(1) FROM ndfl_person_income npi " +
                "JOIN ndfl_person np ON npi.ndfl_person_id = np.id " +
                "JOIN declaration_data dd ON np.declaration_data_id = dd.id " +
                "WHERE npi.id = :ndflPersonIncomeId AND dd.id = :declarationDataId";
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
        String query = "SELECT count(1) FROM ndfl_person_deduction npd " +
                "JOIN ndfl_person np ON npd.ndfl_person_id = np.id " +
                "JOIN declaration_data dd ON np.declaration_data_id = dd.id " +
                "WHERE npd.id = :ndflPersonDeductionId AND dd.id = :declarationDataId";
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

        String query = "SELECT count(1) FROM ndfl_person_prepayment npp " +
                "JOIN ndfl_person np ON npp.ndfl_person_id = np.id " +
                "JOIN declaration_data dd ON np.declaration_data_id = dd.id " +
                "WHERE npp.id = :ndflPersonPrepaymentId AND dd.id = :declarationDataId";
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
    public void updateNdflPersons(List<NdflPerson> persons) {
        String sql = "UPDATE " + NdflPerson.TABLE_NAME + " SET " +
                "row_num = :rowNum, inp = :inp, last_name = :lastName, first_name = :firstName, middle_name = :middleName, birth_day = :birthDay, " +
                "citizenship = :citizenship, inn_np = :innNp, inn_foreign = :innForeign, id_doc_type = :idDocType, id_doc_number = :idDocNumber, " +
                "status = :status, region_code = :regionCode, post_index = :postIndex, area = :area, city = :city, locality = :locality, street = :street, " +
                "house = :house, building = :building, flat = :flat, snils = :snils, modified_date = :modifiedDate, modified_by = :modifiedBy " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(persons.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updateIncomes(List<NdflPersonIncome> incomes) {
        String sql = "UPDATE " + NdflPersonIncome.TABLE_NAME + " SET " +
                "row_num = :rowNum, operation_id = :operationId, oktmo = :oktmo, kpp = :kpp, income_code = :incomeCode, income_type = :incomeType, " +
                "income_accrued_date = :incomeAccruedDate, income_payout_date = :incomePayoutDate, income_accrued_summ = :incomeAccruedSumm, income_payout_summ = :incomePayoutSumm, total_deductions_summ = :totalDeductionsSumm, " +
                "tax_base = :taxBase, tax_rate = :taxRate, tax_date = :taxDate, calculated_tax = :calculatedTax, withholding_tax = :withholdingTax, not_holding_tax = :notHoldingTax, overholding_tax = :overholdingTax, " +
                "refound_tax = :refoundTax, tax_transfer_date = :taxTransferDate, payment_date = :paymentDate, payment_number = :paymentNumber, tax_summ = :taxSumm, modified_date = :modifiedDate, modified_by = :modifiedBy, " +
                "operation_date = :operationDate, action_date = :actionDate, row_type = :rowType " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(incomes.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updateDeductions(List<NdflPersonDeduction> deductions) {
        String sql = "UPDATE " + NdflPersonDeduction.TABLE_NAME + " SET " +
                "row_num = :rowNum, type_code = :typeCode, notif_type = :notifType, notif_date = :notifDate, notif_num = :notifNum, notif_source = :notifSource, " +
                "notif_summ = :notifSumm, operation_id = :operationId, income_accrued = :incomeAccrued, income_code = :incomeCode, income_summ = :incomeSumm, " +
                "period_prev_date = :periodPrevDate, period_prev_summ = :periodPrevSumm, period_curr_date = :periodCurrDate, " +
                "period_curr_summ = :periodCurrSumm, modified_date = :modifiedDate, modified_by = :modifiedBy " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deductions.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updatePrepayments(List<NdflPersonPrepayment> prepayments) {
        String sql = "UPDATE " + NdflPersonPrepayment.TABLE_NAME + " SET " +
                "row_num = :rowNum, operation_id = :operationId, summ = :summ, notif_num = :notifNum, notif_date = :notifDate, notif_source = :notifSource, modified_date = :modifiedDate, modified_by = :modifiedBy " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(prepayments.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updateNdflPersonsRowNum(List<NdflPerson> persons) {
        String sql = "UPDATE " + NdflPerson.TABLE_NAME + " SET " +
                "row_num = :rowNum " +
                "WHERE id = :id";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(persons.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updateIncomesRowNum(List<NdflPersonIncome> incomes) {
        String sql = "UPDATE " + NdflPersonIncome.TABLE_NAME + " SET " +
                "row_num = :rowNum " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(incomes.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updateDeductionsRowNum(List<NdflPersonDeduction> deductions) {
        String sql = "UPDATE " + NdflPersonDeduction.TABLE_NAME + " SET " +
                "row_num = :rowNum " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deductions.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    @Override
    public void updatePrepaymentsRowNum(List<NdflPersonPrepayment> prepayments) {
        String sql = "UPDATE " + NdflPersonPrepayment.TABLE_NAME + " SET " +
                "row_num = :rowNum " +
                "WHERE id = :id";

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(prepayments.toArray());
        getNamedParameterJdbcTemplate().batchUpdate(sql, batch);
    }

    private static String createColumns(String[] columns, String alias) {
        List<String> list = new ArrayList<String>();
        for (String col : columns) {
            list.add(alias + "." + col);
        }
        String columnsNames = SqlUtils.toSqlString(list.toArray());
        return columnsNames.replace("(", "").replace(")", "");
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

            person.setModifiedDate(rs.getTimestamp("modified_date"));
            person.setModifiedBy(rs.getString("modified_by"));
            //person.setAsnuName(rs.getString("asnu_name"));
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
            personIncome.setTaxSumm(rs.getBigDecimal("tax_summ"));
            personIncome.setSourceId(SqlUtils.getLong(rs, "source_id"));

            personIncome.setModifiedDate(rs.getTimestamp("modified_date"));
            personIncome.setModifiedBy(rs.getString("modified_by"));
            personIncome.setAsnuId(SqlUtils.getLong(rs, "asnu_id"));
            personIncome.setAsnu(rs.getString("asnu_name"));
            personIncome.setOperationDate(rs.getDate("operation_date"));
            personIncome.setActionDate(rs.getDate("action_date"));
            personIncome.setRowType(rs.getInt("row_type"));
            personIncome.setOperInfoId(rs.getBigDecimal("oper_info_id"));
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

            personDeduction.setModifiedDate(rs.getTimestamp("modified_date"));
            personDeduction.setModifiedBy(rs.getString("modified_by"));
            personDeduction.setAsnu(rs.getString("asnu_name"));
            personDeduction.setAsnuId(rs.getLong("asnu_id"));

            personDeduction.setOperInfoId(rs.getBigDecimal("oper_info_id"));
            personDeduction.setOktmo(rs.getString("oktmo"));
            personDeduction.setKpp(rs.getString("kpp"));

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

            personPrepayment.setModifiedDate(rs.getTimestamp("modified_date"));
            personPrepayment.setModifiedBy(rs.getString("modified_by"));
            personPrepayment.setAsnu(rs.getString("asnu_name"));
            personPrepayment.setAsnuId(rs.getLong("asnu_id"));

            personPrepayment.setOperInfoId(rs.getBigDecimal("oper_info_id"));
            personPrepayment.setOktmo(rs.getString("oktmo"));
            personPrepayment.setKpp(rs.getString("kpp"));
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
    public void updateOneNdflIncome(NdflPersonIncome personIncome, TAUserInfo taUserInfo) {
        String sql = "UPDATE ndfl_person_income SET KPP = :kpp, OKTMO = :oktmo, INCOME_CODE = :incomeCode, INCOME_ACCRUED_DATE = :incomeAccruedDate, " +
                "INCOME_ACCRUED_SUMM = :incomeAccruedSumm, INCOME_TYPE = :incomeType, INCOME_PAYOUT_DATE = :incomePayoutDate, INCOME_PAYOUT_SUMM = :incomePayoutSumm, " +
                "TAX_BASE = :taxBase, TOTAL_DEDUCTIONS_SUMM = :totalDeductionsSumm, TAX_RATE = :taxRate, CALCULATED_TAX = :calculatedTax, WITHHOLDING_TAX = :withholdingTax, " +
                "TAX_DATE = :taxDate, NOT_HOLDING_TAX = :notHoldingTax, OVERHOLDING_TAX = :overholdingTax, REFOUND_TAX = :refoundTax, TAX_TRANSFER_DATE = :taxTransferDate," +
                "TAX_SUMM = :taxSumm, PAYMENT_DATE = :paymentDate, PAYMENT_NUMBER = :paymentNumber, MODIFIED_DATE = :modifyDate, MODIFIED_BY = :user WHERE id = :id";
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
        params.addValue("modifyDate", Calendar.getInstance(TimeZone.getTimeZone("GMT+3")).getTime());

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void updateOneNdflDeduction(NdflPersonDeduction personDeduction, TAUserInfo taUserInfo) {
        String sql = "UPDATE ndfl_person_deduction SET TYPE_CODE = :typeCode, NOTIF_TYPE = :notifType, NOTIF_NUM = :notifNum, NOTIF_SUMM = :notifSumm, " +
                "NOTIF_SOURCE = :notifSource, NOTIF_DATE = :notifDate, INCOME_CODE = :incomeCode, INCOME_SUMM = :incomeSumm, INCOME_ACCRUED = :incomeAccrued, " +
                "PERIOD_PREV_DATE = :periodPrevDate, PERIOD_PREV_SUMM = :periodPrevSumm, PERIOD_CURR_DATE = :periodCurrDate, PERIOD_CURR_SUMM = :periodCurrSumm, " +
                "MODIFIED_DATE = :modifyDate, MODIFIED_BY = :user WHERE id = :id";
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
        params.addValue("modifyDate", Calendar.getInstance(TimeZone.getTimeZone("GMT+3")).getTime());

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void updateOneNdflPrepayment(NdflPersonPrepayment personPrepayment, TAUserInfo taUserInfo) {
        String sql = "UPDATE ndfl_person_prepayment SET SUMM = :summ, NOTIF_NUM = :notifNum, NOTIF_SOURCE = :notifSource, " +
                "NOTIF_DATE = :notifDate, MODIFIED_DATE = :modifyDate, MODIFIED_BY = :user WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("summ", personPrepayment.getSumm());
        params.addValue("notifNum", personPrepayment.getNotifNum());
        params.addValue("notifSource", personPrepayment.getNotifSource());
        params.addValue("notifDate", personPrepayment.getNotifDate());
        params.addValue("user", taUserInfo.getUser().getName() + " (" + taUserInfo.getUser().getLogin() + ")");
        params.addValue("id", personPrepayment.getId());
        params.addValue("modifyDate", Calendar.getInstance(TimeZone.getTimeZone("GMT+3")).getTime());

        getNamedParameterJdbcTemplate().update(sql, params);

    }

    //TODO: если вызвать с аргументом длиной больше 1000, метод отработает неверно
    @Override
    public List<NdflPersonPrepayment> fetchPrepaymentByIncomesIdAndAccruedDate(List<Long> ndflPersonIncomeIdList, Date periodStartDate, Date periodEndDate) {
        String sql = "select DISTINCT rba.NAME as asnu_name, " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") +
                " FROM ndfl_person_income npi " +
                " JOIN ndfl_person_prepayment npp ON npp.ndfl_person_id = npi.ndfl_person_id and npp.OPERATION_ID = npi.OPERATION_ID " +
                " left join ref_book_asnu rba on npp.asnu_id = rba.id" +
                " WHERE npi.id in (:ndflPersonIncomeIdList) and npi.income_accrued_date between :periodStartDate and :periodEndDate ";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ndflPersonIncomeIdList", ndflPersonIncomeIdList)
                .addValue("periodStartDate", periodStartDate)
                .addValue("periodEndDate", periodEndDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<NdflPerson> fetchRefBookPersonsAsNdflPerson(Long declarationDataId, Date actualDate) {
        String sql = "SELECT rbp.id, rbp.record_id AS inp, rbp.last_name, rbp.first_name, rbp.middle_name, rbp.birth_date, rbc.code AS citizenship, \n" +
                "rbp.inn, rbp.inn_foreign, rbts.code AS status, rbp.snils, rbdt.code AS id_doc_type, rbid.doc_number, rbp.region_code, \n" +
                "rbp.postal_code, rbp.district, rbp.city, rbp.locality, rbp.street, rbp.house, rbp.build, \n" +
                "rbp.appartment, rbp.address_foreign as address, rbc2.code AS country_code \n" +
                "FROM ref_book_person rbp \n" +
                "LEFT JOIN ndfl_person np ON rbp.id = np.person_id \n" +
                "LEFT JOIN declaration_data dd ON np.declaration_data_id = dd.id \n" +
                "LEFT JOIN ref_book_country rbc ON rbp.citizenship = rbc.id AND rbc.status = 0 \n" +
                "LEFT JOIN ref_book_country rbc2 ON rbp.country_id = rbc2.id AND rbc2.status = 0 \n" +
                "LEFT JOIN ref_book_taxpayer_state rbts ON rbp.taxpayer_state = rbts.id AND rbts.status = 0 \n" +
                "LEFT JOIN ref_book_id_tax_payer ritp ON ritp.person_id = rbp.id \n" +
                "LEFT JOIN ref_book_id_doc rbid ON rbid.id = rbp.report_doc \n" +
                "LEFT JOIN ref_book_doc_type rbdt ON rbid.doc_id = rbdt.id AND rbdt.status = 0 \n" +
                "WHERE dd.id = :dd\n" +
                "and rbp.record_id in (select record_id from ref_book_person where id in (SELECT rbp2.id FROM ref_book_person rbp2 LEFT JOIN ndfl_person np2 ON rbp2.id = np2.person_id \n" +
                "LEFT JOIN declaration_data dd2 ON np2.declaration_data_id = dd2.id)) \n" +
                "AND (rbp.start_date <= :currentDate and (rbp.end_date >= :currentDate or rbp.end_date is null))\n" +
                "AND rbp.record_id = rbp.old_id";
        MapSqlParameterSource params = new MapSqlParameterSource("currentDate", actualDate);
        params.addValue("dd", declarationDataId);
        return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonRefBookRowMapper());
    }

    @Override
    public List<ConsolidationIncome> fetchIncomeSourcesConsolidation(ConsolidationSourceDataSearchFilter searchData) {
        String departmentConfigsKppOktmoSelect = "" +
                "  select dc.kpp, oktmo.code oktmo, max(dc.start_date) start_date \n" +
                "  from department_config dc\n" +
                "  join ref_book_oktmo oktmo on oktmo.id = dc.oktmo_id\n" +
                "  where department_id = :departmentId and (\n" +
                "    dc.start_date <= :currentDate and (dc.end_date is null or :currentDate <= dc.end_date) or (\n" +
                "      dc.start_date <= :periodEndDate and (dc.end_date is null or :periodStartDate <= dc.end_date) \n" +
                "      and not exists (select * from department_config where kpp = dc.kpp and oktmo_id = dc.oktmo_id and start_date > dc.start_date and department_id != :departmentId)\n" +
                "    )\n" +
                "  )\n" +
                "  group by dc.kpp, oktmo.code\n";
        String insertSql = "insert into tmp_cons_data(operation_id, asnu_id)\n" +
                "with kpp_oktmo as (\n" +
                departmentConfigsKppOktmoSelect +
                ")\n" +
                "select /*+ NO_INDEX(npi NDFL_PERS_INC_KPP_OKTMO) */ distinct npi.operation_id, dd.asnu_id\n" +
                "from ndfl_person_income npi \n" +
                "join kpp_oktmo on kpp_oktmo.kpp = npi.kpp and kpp_oktmo.oktmo = npi.oktmo\n" +
                "join ndfl_person np on npi.ndfl_person_id = np.id \n" +
                "join declaration_data dd on dd.id = np.declaration_data_id \n" +
                "join declaration_template dt on dd.declaration_template_id = dt.id \n" +
                "join department_report_period drp on drp.id = dd.department_report_period_id \n" +
                "join report_period rp on rp.id = drp.report_period_id \n" +
                "join tax_period tp on rp.tax_period_id = tp.id \n" +
                "left join report_period_type rpt on rp.dict_tax_period_id = rpt.id\n" +
                "where (npi.income_accrued_date between :periodStartDate and :periodEndDate \n" +
                "or npi.income_payout_date between :periodStartDate and :periodEndDate \n" +
                "or npi.tax_date between :periodStartDate and :periodEndDate \n" +
                "or npi.tax_transfer_date between :periodStartDate and :periodEndDate ) \n" +
                "and dt.declaration_type_id = :declarationType and tp.year between :dataSelectionDepth and :consolidateDeclarationDataYear";
        String selectSql = "" +
                "with kpp_oktmo as (\n" +
                departmentConfigsKppOktmoSelect +
                ")\n" +
                "select /*+ use_hash(cd npi)*/ distinct " + createColumns(NdflPersonIncome.COLUMNS, "npi") + ", dd.id as dd_id, dd.asnu_id, dd.state, np.inp, tp.year, rpt.code as period_code, drp.correction_date, rba.NAME as asnu_name " +
                "from tmp_cons_data cd \n" +
                "join ndfl_person_income npi on npi.operation_id = cd.operation_id\n" +
                "join kpp_oktmo on kpp_oktmo.kpp = npi.kpp and kpp_oktmo.oktmo = npi.oktmo\n" +
                "join ndfl_person np on npi.ndfl_person_id = np.id\n" +
                "join declaration_data dd on dd.id = np.declaration_data_id\n" +
                "join department_report_period drp on drp.id = dd.department_report_period_id\n" +
                "join report_period rp on rp.id = drp.report_period_id\n" +
                "join tax_period tp on rp.tax_period_id = tp.id\n" +
                "left join report_period_type rpt on rp.dict_tax_period_id = rpt.id\n" +
                "left join ref_book_asnu rba on npi.asnu_id = rba.id\n" +
                "where dd.asnu_id = cd.asnu_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("currentDate", searchData.getCurrentDate())
                .addValue("periodStartDate", searchData.getPeriodStartDate())
                .addValue("periodEndDate", searchData.getPeriodEndDate())
                .addValue("declarationType", searchData.getDeclarationType())
                .addValue("dataSelectionDepth", searchData.getDataSelectionDepth())
                .addValue("consolidateDeclarationDataYear", searchData.getConsolidateDeclarationDataYear())
                .addValue("departmentId", searchData.getDepartmentId());
        try {
            getNamedParameterJdbcTemplate().update(insertSql, params);
            return getNamedParameterJdbcTemplate().query(selectSql, params, new ConsolidationIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    private static class ConsolidationIncomeRowMapper implements RowMapper<ConsolidationIncome> {
        @Override
        public ConsolidationIncome mapRow(ResultSet rs, int index) throws SQLException {
            ConsolidationIncome income = new ConsolidationIncome(new NdflPersonIncomeRowMapper().mapRow(rs, index));
            income.setAsnuId(SqlUtils.getLong(rs, "asnu_id"));
            income.setAccepted(rs.getInt("state") == 3);
            income.setDeclarationDataId(SqlUtils.getLong(rs, "dd_id"));
            income.setInp(rs.getString("inp"));
            income.setYear(rs.getInt("year"));
            income.setPeriodCode(rs.getString("period_code"));
            income.setCorrectionDate(rs.getDate("correction_date"));
            return income;
        }
    }

    @Override
    public List<NdflPersonDeduction> fetchDeductionsForConsolidation(List<Long> incomeIds) {
        String sql = "with person_operation as \n" +
                "(select distinct npi.operation_id, npi.ndfl_person_id from ndfl_person_income npi where npi.id in (:incomeIds))\n" +
                "select rba.NAME as asnu_name, " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + " \n" +
                "from ndfl_person_deduction npd \n" +
                "left join person_operation po on npd.operation_id = po.operation_id \n" +
                " left join ref_book_asnu rba on npd.asnu_id = rba.id " +
                "where npd.operation_id = po.operation_id and npd.ndfl_person_id = po.ndfl_person_id";
        List<NdflPersonDeduction> result = selectIn(sql, incomeIds, "incomeIds", new NdflPersonDeductionRowMapper());

        if (result.isEmpty()) return null;
        return result;
    }

    @Override
    public List<NdflPersonPrepayment> fetchPrepaymentsForConsolidation(List<Long> incomeIds) {
        String sql = "with person_operation as \n" +
                "(select distinct npi.operation_id, npi.ndfl_person_id from ndfl_person_income npi where npi.id in (:incomeIds))\n" +
                "select rba.NAME as asnu_name, " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + " \n" +
                "from ndfl_person_prepayment npp \n" +
                "left join person_operation po on npp.operation_id = po.operation_id \n" +
                " left join ref_book_asnu rba on npp.asnu_id = rba.id " +
                "where npp.operation_id = po.operation_id and npp.ndfl_person_id = po.ndfl_person_id";
        List<NdflPersonPrepayment> result = selectIn(sql, incomeIds, "incomeIds", new NdflPersonPrepaymentRowMapper());

        if (result.isEmpty()) return null;
        return result;
    }

    @Override
    public List<NdflPerson> fetchRefBookPersonsAsNdflPerson(List<Long> personIdList, Date actualDate) {
        if (personIdList.size() > IN_CLAUSE_LIMIT) {
            List<NdflPerson> result = new ArrayList<>();
            int n = (personIdList.size() - 1) / IN_CLAUSE_LIMIT + 1;
            for (int i = 0; i < n; i++) {
                List<Long> subList = getSubList(personIdList, i);
                List<NdflPerson> subResult = fetchRefBookPersonsAsNdflPerson(subList, actualDate);
                if (subResult != null) {
                    result.addAll(subResult);
                }
            }
            if (result.isEmpty()) {
                return null;
            } else {
                return result;
            }
        }
        String sql = "SELECT rbp.id, rbp.record_id AS inp, rbp.last_name, rbp.first_name, rbp.middle_name, rbp.birth_date, rbc.code AS citizenship,\n" +
                "rbp.inn, rbp.inn_foreign, rbts.code AS status, rbp.snils, rbdt.code AS id_doc_type, rbid.doc_number, rbp.region_code,\n" +
                "rbp.postal_code, rbp.district, rbp.city, rbp.locality, rbp.street, rbp.house, rbp.build,\n" +
                "rbp.appartment, rbc2.code AS country_code, rbp.address_foreign as address\n" +
                "FROM ref_book_person rbp\n" +
                "LEFT JOIN ref_book_country rbc ON rbp.citizenship = rbc.id AND rbc.status = 0 \n" +
                "LEFT JOIN ref_book_country rbc2 ON rbp.country_id = rbc2.id AND rbc2.status = 0 \n" +
                "LEFT JOIN ref_book_taxpayer_state rbts ON rbp.taxpayer_state = rbts.id AND rbts.status = 0 \n" +
                "LEFT JOIN ref_book_id_tax_payer ritp ON ritp.person_id = rbp.id\n" +
                "LEFT JOIN ref_book_id_doc rbid ON rbid.id = rbp.report_doc \n" +
                "LEFT JOIN ref_book_doc_type rbdt ON rbid.doc_id = rbdt.id AND rbdt.status = 0 \n" +
                "WHERE rbp.record_id in (select record_id from ref_book_person where id in (:personIdList)) " +
                "AND (rbp.start_date <= :currentDate and (rbp.end_date >= :currentDate or rbp.end_date is null))" +
                "AND rbp.record_id = rbp.old_id";

        MapSqlParameterSource params = new MapSqlParameterSource("personIdList", personIdList);
        params.addValue("currentDate", actualDate);
        return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonRefBookRowMapper());
    }

    private static final class NdflPersonRefBookRowMapper implements RowMapper<NdflPerson> {
        @Override
        public NdflPerson mapRow(ResultSet rs, int i) throws SQLException {
            NdflPerson person = new NdflPerson();

            person.setPersonId(SqlUtils.getLong(rs, "id"));
            person.setRecordId(SqlUtils.getLong(rs, "inp"));
            ;
            person.setInp(String.valueOf(SqlUtils.getLong(rs, "inp")));
            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setBirthDay(rs.getDate("birth_date"));
            person.setCitizenship(rs.getString("citizenship"));
            person.setInnNp(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setStatus(rs.getString("status"));
            person.setSnils(rs.getString("snils"));
            person.setIdDocType(rs.getString("id_doc_type"));
            person.setIdDocNumber(rs.getString("doc_number"));
            person.setRegionCode(rs.getString("region_code"));
            person.setPostIndex(rs.getString("postal_code"));
            person.setArea(rs.getString("district"));
            person.setCity(rs.getString("city"));
            person.setLocality(rs.getString("locality"));
            person.setStreet(rs.getString("street"));
            person.setHouse(rs.getString("house"));
            person.setBuilding(rs.getString("build"));
            person.setFlat(rs.getString("appartment"));
            person.setCountryCode(rs.getString("country_code"));
            person.setAddress(rs.getString("address"));

            return person;
        }
    }

    @Override
    public PagingResult<KppSelect> findAllKppByDeclarationDataId(long declarationDataId, String kpp, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);
        String baseSelect = "select distinct kpp from ndfl_person_income npi " +
                "join ndfl_person np on np.id = npi.ndfl_person_id  " +
                "where np.declaration_data_id = :declarationDataId";
        if (!StringUtils.isEmpty(kpp)) {
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
    public List<NdflPersonIncome> findDeclarartionDataIncomesWithSameOperationIdAndInp(Long declarationDataId, String inp, String operationId) {
        String sql = "select rba.name asnu_name, " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " from ndfl_person_income npi\n" +
                "left join ndfl_person np on npi.ndfl_person_id = np.id\n" +
                "left join declaration_data dd on dd.id = np.declaration_data_id\n" +
                "left join ref_book_asnu rba on npi.asnu_id = rba.id\n" +
                "where dd.id = :declarationDataId\n" +
                "and np.inp = :inp\n" +
                "and npi.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", declarationDataId)
                .addValue("inp", inp)
                .addValue("operationId", operationId);
        return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPerson> findDeclarartionDataPersonWithSameOperationIdAndInp(Long declarationDataId, String inp, String operationId) {
        String sql = "select distinct " + createColumns(NdflPerson.COLUMNS, "np") + " from ndfl_person np\n" +
                "left join ndfl_person_income npi on npi.ndfl_person_id = np.id\n" +
                "left join declaration_data dd on dd.id = np.declaration_data_id\n" +
                "where dd.id = :declarationDataId\n" +
                "and np.inp = :inp\n" +
                "and npi.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", declarationDataId)
                .addValue("inp", inp)
                .addValue("operationId", operationId);
        return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonRowMapper());
    }

    @Override
    public Date findOperationDate(Long declarationDataId, String inp, String operationId) {
        String sql = "select\n" +
                "min(case\n" +
                "when npi.income_accrued_date is not null then npi.income_accrued_date\n" +
                "when npi.income_payout_date is not null then npi.income_payout_date\n" +
                "when npi.payment_date is not null then npi.payment_date\n" +
                "else null\n" +
                "end) as operation\n" +
                "from ndfl_person_income npi\n" +
                "left join ndfl_person np on npi.ndfl_person_id = np.id\n" +
                "left join declaration_data dd on dd.id = np.declaration_data_id\n" +
                "where dd.id = :declarationDataId\n" +
                "and np.inp = :inp\n" +
                "and npi.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", declarationDataId)
                .addValue("inp", inp)
                .addValue("operationId", operationId);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, Date.class);
    }

    @Override
    public List<BigDecimal> generateOperInfoIds(int count) {
        return generateIds("seq_oper_info", count, BigDecimal.class);
    }
}
