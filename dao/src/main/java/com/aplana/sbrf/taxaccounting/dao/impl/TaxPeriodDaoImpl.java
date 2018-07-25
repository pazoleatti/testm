package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;


/**
 * Реализация DAO для работы с {@link TaxPeriod}
 */
@Repository
@Transactional(readOnly = true)
public class TaxPeriodDaoImpl extends AbstractDao implements TaxPeriodDao {

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link TaxPeriod}
     */
    private final class TaxPeriodRowMapper implements RowMapper<TaxPeriod> {
        @Override
        public TaxPeriod mapRow(ResultSet rs, int index) throws SQLException {
            TaxPeriod t = new TaxPeriod();
            t.setId(SqlUtils.getInteger(rs, "id"));
            t.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            t.setYear(SqlUtils.getInteger(rs, "year"));
            return t;
        }
    }

    @Override
    @Cacheable(CacheConstants.TAX_PERIOD)
    public TaxPeriod fetchOne(int taxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, tax_type, year FROM tax_period WHERE id = ?",
                    new Object[]{taxPeriodId},
                    new int[]{Types.NUMERIC},
                    new TaxPeriodRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти налоговый период с id = " + taxPeriodId);
        }
    }

    @Override
    public List<TaxPeriod> fetchAllByTaxType(TaxType taxType) {
        try {
            return getJdbcTemplate().query(
                    "SELECT id, tax_type, year FROM tax_period WHERE tax_type = ? ORDER BY year",
                    new Object[]{taxType.getCode()},
                    new int[]{Types.VARCHAR},
                    new TaxPeriodRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти налоговые периоды с типом = " + taxType.getCode());
        }
    }

    @Override
    public TaxPeriod fetchOneByYear(int year) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, tax_type, year FROM tax_period WHERE year = ?",
                    new Object[]{year},
                    new int[]{Types.NUMERIC},
                    new TaxPeriodRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int create(TaxPeriod taxPeriod) {
        JdbcTemplate jt = getJdbcTemplate();

        Integer id = taxPeriod.getId();
        if (id == null) {
            id = generateId("seq_tax_period", Integer.class);
        }
        jt.update(
                "INSERT INTO tax_period (id, tax_type, year)" +
                        " VALUES (?, ?, ?)",
                new Object[]{
                        id,
                        taxPeriod.getTaxType().getCode(),
                        taxPeriod.getYear()
                },
                new int[]{Types.NUMERIC, Types.VARCHAR, Types.NUMERIC}

        );
        taxPeriod.setId(id);
        return id;
    }

    @Override
    @Deprecated
    public TaxPeriod getLast(TaxType taxType) {
        try {
            return getJdbcTemplate().queryForObject( //TODO Вероятно, это можно оптимизировать
                    "SELECT id, tax_type, year FROM tax_period WHERE tax_type = ? AND " +
                            "year = (SELECT max(year) FROM tax_period WHERE tax_type = ?)",
                    new Object[]{taxType.getCode(), taxType.getCode()},
                    new int[]{Types.VARCHAR, Types.VARCHAR},
                    new TaxPeriodRowMapper()
            );

        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void delete(int taxPeriodId) {
        getJdbcTemplate().update(
                "DELETE FROM tax_period WHERE id = ?",
                new Object[]{taxPeriodId},
                new int[]{Types.NUMERIC}
        );
    }
}