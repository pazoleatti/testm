package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodMappingDao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация DAO для маппинга отчетного периода
 * @author Alexander Ivanov
 */
@Repository
@Transactional(readOnly = true)
public class ReportPeriodMappingDaoImpl extends AbstractDao implements ReportPeriodMappingDao {

    private static final String dateFormat = "dd.MM.yyyy";
    private static final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    @Override
    public Integer getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId) {
        StringBuilder sql = new StringBuilder("select rp.id ");
        sql.append("from report_period rp, ref_book_record rfr, ref_book_value rfv ");
        sql.append("where rp.dict_tax_period_id = rfr.id AND rfv.record_id = rp.dict_tax_period_id ");
        sql.append("AND rfv.attribute_id = 25 AND rfr.ref_book_id = 8 ");
        sql.append("AND rp.tax_period_id = ? AND rfv.string_value = ?");
        try {
            return getJdbcTemplate().queryForInt(
                    sql.toString(),
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC}
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с tax_period_id=" + taxPeriodId + " и dict_tax_period_id = " + dictTaxPeriodId);
        }
    }

    @Override
    public Integer getTaxPeriodByDate(Date start) {
        StringBuilder sql = new StringBuilder("select id from tax_period where ");
        sql.append("tax_type = 'I' and ");
        sql.append("start_date = to_date('");
        sql.append(formatter.format(start)).append("', '").append("dd.mm.yyyy").append("')");
        try {
            return getJdbcTemplate().queryForInt(sql.toString());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода типа I для даты " + start);
        }
    }
}
