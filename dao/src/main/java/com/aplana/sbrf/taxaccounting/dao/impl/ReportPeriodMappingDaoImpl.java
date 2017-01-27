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
    @Override
    public Integer getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId) {
        StringBuilder sql = new StringBuilder("select * from ( ");
        sql.append("select rp.id ");
        sql.append("from report_period rp, ref_book_record rfr, ref_book_value rfv ");
        sql.append("where rp.dict_tax_period_id = rfr.id AND rfv.record_id = rp.dict_tax_period_id ");
        sql.append("AND rfv.attribute_id = 25 AND rfr.ref_book_id = 8 ");
        sql.append("AND rp.tax_period_id = ? AND rfv.string_value = ? ");
        sql.append("order by rfr.version desc ");
        sql.append(") where rownum = 1");
        try {
            return getJdbcTemplate().queryForObject(
                    sql.toString(),
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
					Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с tax_period_id=" + taxPeriodId + " и dict_tax_period_id = " + dictTaxPeriodId);
        }
    }

    @Override
    public Integer getTaxPeriodByDate(String year) {
        try {
            return getJdbcTemplate().queryForObject("select id from tax_period where tax_type = ? and year = ?",
					new Object[]{TaxType.INCOME.getCode(), year},
					new int[]{Types.VARCHAR, Types.NUMERIC}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует налогового периода типа I для года " + year);
        }
    }
}
