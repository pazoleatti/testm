package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class NdflPersonDaoImpl extends AbstractDao implements NdflPersonDao {

    private static final Log LOG = LogFactory.getLog(NdflPersonDaoImpl.class);

    private static final String DUPLICATE_ERORR_MSG = "Попытка перезаписать уже сохранённые данные!";

    @Override
    public List<NdflPerson> findAll() {
        try {
            return getJdbcTemplate().query("select * from ndfl_person np", new NdflPersonDaoImpl.NdflPersonRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public NdflPerson get(long ndflPersonId) {
        try {
            NdflPerson ndflPerson = getJdbcTemplate().queryForObject("select * from ndfl_person np where np.id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonRowMapper());

            List<NdflPersonIncome> ndflPersonIncomes = findNdflPersonIncomesByNdfPersonId(ndflPersonId);
            List<NdflPersonDeduction> ndflPersonDeductions = findNdflPersonDeductionByNdfPersonId(ndflPersonId);
            List<NdflPersonPrepayment> ndflPersonPrepayments = findNdflPersonPrepaymentByNdfPersonId(ndflPersonId);

            ndflPerson.setNdflPersonIncomes(ndflPersonIncomes);
            ndflPerson.setNdflPersonDeductions(ndflPersonDeductions);
            ndflPerson.setNdflPersonPrepayments(ndflPersonPrepayments);

            return ndflPerson;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Сущность класса NdflPerson с id = %d не найдена в БД", ndflPersonId);
        }
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomesByNdfPersonId(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select * from ndfl_person_income npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonDeduction> findNdflPersonDeductionByNdfPersonId(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select * from ndfl_person_deduction npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findNdflPersonPrepaymentByNdfPersonId(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select * from ndfl_person_prepayment npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }


    @Override
    public Long save(NdflPerson ndflPerson) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        if (ndflPerson.getId() != null) {
            throw new DaoException(DUPLICATE_ERORR_MSG);
        }

        ndflPerson.setId(generateId(NdflPerson.SEQ, Long.class));
        jdbcTemplate.update("insert into ndfl_person " + createColumnsAndSource(NdflPerson.COLUMNS), ndflPerson.createPreparedStatementArgs());

        //
        List<NdflPersonIncome> ndflPersonIncomes = ndflPerson.getNdflPersonIncomes();

        if (ndflPersonIncomes == null || ndflPersonIncomes.isEmpty()) {
            throw new DaoException("Пропущены обязательные данные о доходах!");
        }

        saveNdflPersonDetail(jdbcTemplate, insert(NdflPersonIncome.TABLE_NAME, NdflPersonIncome.COLUMNS), NdflPersonIncome.SEQ, ndflPerson, ndflPersonIncomes);


        List<NdflPersonDeduction> ndflPersonDeductions = ndflPerson.getNdflPersonDeductions();
        saveNdflPersonDetail(jdbcTemplate, insert(NdflPersonDeduction.TABLE_NAME, NdflPersonDeduction.COLUMNS), NdflPersonDeduction.SEQ, ndflPerson, ndflPersonDeductions);


        List<NdflPersonPrepayment> ndflPersonPrepayments = ndflPerson.getNdflPersonPrepayments();
        saveNdflPersonDetail(jdbcTemplate, insert(NdflPersonPrepayment.TABLE_NAME, NdflPersonPrepayment.COLUMNS), NdflPersonPrepayment.SEQ, ndflPerson, ndflPersonPrepayments);

        return ndflPerson.getId();
    }

    private void saveNdflPersonDetail(JdbcTemplate jdbcTemplate, String query, String seq, NdflPerson ndflPerson, List<? extends NdflPersonDetail> details) {
        for (NdflPersonDetail detail : details) {
            if (detail.getId() != null) {
                throw new DaoException(DUPLICATE_ERORR_MSG);
            }
            detail.setId(generateId(seq, Long.class));
            detail.setNdflPersonId(ndflPerson.getId());
            jdbcTemplate.update(query, detail.createPreparedStatementArgs());
        }
    }

    private static String insert(String table, String[] columns) {
        StringBuilder sb = new StringBuilder();
        return sb.append("insert into ").append(table).append(" ").append(createColumnsAndSource(columns)).toString();
    }

    @Override
    public void delete(Long id) {
        int count = getJdbcTemplate().update("delete from ndfl_person where id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить сущность класса NdflPerson с id = %d, так как она не существует", id);
        }
    }

    @Override
    public List<NdflPerson> findNdflPersonByDeclarationDataId(long declarationDataId) {
        //TODO
        return null;
    }

    public static String createColumnsAndSource(String[] columnDescriptors) {
        int iMax = columnDescriptors.length - 1;
        StringBuilder columns = new StringBuilder();
        StringBuilder source = new StringBuilder();
        columns.append(" (");
        source.append(" VALUES (");

        if (iMax == -1) {
            return "";
        }

        for (int i = 0; ; i++) {
            columns.append(columnDescriptors[i]);
            source.append("?");
            if (i == iMax) {
                return columns.append(')').toString() + source.append(')').toString();
            }
            columns.append(", ");
            source.append(", ");
        }
    }

    //>-------------------------<The DAO row mappers>-----------------------------<

    private static final class NdflPersonRowMapper implements RowMapper<NdflPerson> {
        @Override
        public NdflPerson mapRow(ResultSet rs, int index) throws SQLException {

            NdflPerson person = new NdflPerson();
            person.setId(SqlUtils.getLong(rs, "id"));
            person.setDeclarationDataId(SqlUtils.getLong(rs, "declaration_data_id"));
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

            return person;
        }
    }


    private static final class NdflPersonIncomeRowMapper implements RowMapper<NdflPersonIncome> {
        @Override
        public NdflPersonIncome mapRow(ResultSet rs, int index) throws SQLException {

            NdflPersonIncome personIncome = new NdflPersonIncome();

            personIncome.setId(SqlUtils.getLong(rs, "id"));
            personIncome.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personIncome.setRowNum(rs.getInt("row_num"));

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

            personIncome.setCalculatedTax(SqlUtils.getInteger(rs, "calculated_tax"));
            personIncome.setWithholdingTax(SqlUtils.getInteger(rs, "withholding_tax"));
            personIncome.setNotHoldingTax(SqlUtils.getInteger(rs, "not_holding_tax"));
            personIncome.setOverholdingTax(SqlUtils.getInteger(rs, "overholding_tax"));
            personIncome.setRefoundTax(SqlUtils.getInteger(rs, "refound_tax"));

            personIncome.setTaxTransferDate(rs.getDate("tax_transfer_date"));
            personIncome.setPaymentDate(rs.getDate("payment_date"));
            personIncome.setPaymentNumber(rs.getString("payment_number"));
            personIncome.setTaxSumm(SqlUtils.getInteger(rs, "tax_summ"));

            return personIncome;
        }
    }

    private static final class NdflPersonDeductionRowMapper implements RowMapper<NdflPersonDeduction> {

        @Override
        public NdflPersonDeduction mapRow(ResultSet rs, int i) throws SQLException {

            NdflPersonDeduction personDeduction = new NdflPersonDeduction();
            personDeduction.setId(SqlUtils.getLong(rs, "id"));
            personDeduction.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personDeduction.setRowNum(rs.getInt("row_num"));

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

            return personDeduction;
        }
    }

    private static final class NdflPersonPrepaymentRowMapper implements RowMapper<NdflPersonPrepayment> {

        @Override
        public NdflPersonPrepayment mapRow(ResultSet rs, int i) throws SQLException {

            NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
            personPrepayment.setId(SqlUtils.getLong(rs, "id"));
            personPrepayment.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personPrepayment.setRowNum(rs.getInt("row_num"));

            personPrepayment.setSumm(rs.getBigDecimal("summ"));
            personPrepayment.setNotifNum(rs.getString("notif_num"));
            personPrepayment.setNotifDate(rs.getDate("notif_date"));
            personPrepayment.setNotifSource(rs.getString("notif_source"));

            return personPrepayment;
        }
    }


}
