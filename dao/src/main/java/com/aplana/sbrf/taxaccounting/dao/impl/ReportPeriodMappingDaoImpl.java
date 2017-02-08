package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.Types;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodMappingDao;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация DAO для маппинга отчетного периода
 * @author Alexander Ivanov
 */
@Repository
@Transactional(readOnly = true)
public class ReportPeriodMappingDaoImpl extends AbstractDao implements ReportPeriodMappingDao {

    // TODO Левыкин: А почему берем только первую попавшуюся запись? Должен учитываться год.
    // TODO Левыкин: На входе dictTaxPeriodId (ссылка на значение справочника), а в запросе сравнивается с ref_book_value.string_value!
	private static final String GET_BY_TAX_PERIOD_AND_TYPE_SQL =
			"SELECT id FROM (" +
					"    SELECT rp.id "+
					"    FROM report_period rp JOIN report_period_type rpt ON rp.dict_tax_period_id = rpt.id " +
					"    WHERE rp.tax_period_id = ? AND rpt.code = ? " +
					") WHERE ROWNUM = 1";
    @Override
    public Integer getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
					GET_BY_TAX_PERIOD_AND_TYPE_SQL,
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
					Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с tax_period_id=" + taxPeriodId + " и dict_tax_period_id = " + dictTaxPeriodId);
        }
    }

	private static final String GET_TAX_PERIOD_BY_DATE_SQL =
			"SELECT id FROM tax_period WHERE tax_type = ? AND year = ?";
    @Override
    public Integer getTaxPeriodByDate(String year) {
        try {
            return getJdbcTemplate().queryForObject(GET_TAX_PERIOD_BY_DATE_SQL,
					new Object[]{TaxType.INCOME.getCode(), year},
					new int[]{Types.VARCHAR, Types.NUMERIC}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует налогового периода типа I для года " + year);
        }
    }
}