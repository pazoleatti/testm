package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import org.springframework.cache.annotation.Cacheable;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QTaxPeriod.taxPeriod;

/**
 * Реализация DAO для работы с {@link com.aplana.sbrf.taxaccounting.model.TaxPeriod налоговыми периодами}
 */
@Repository
@Transactional(readOnly = true)
public class TaxPeriodDaoImpl extends AbstractDao implements TaxPeriodDao {

	private SQLQueryFactory sqlQueryFactory;

	@Autowired
	public TaxPeriodDaoImpl(SQLQueryFactory sqlQueryFactory) {
		this.sqlQueryFactory = sqlQueryFactory;
	}

	private final class TaxPeriodRowMapper implements RowMapper<TaxPeriod> {
		@Override
		public TaxPeriod mapRow(ResultSet rs, int index) throws SQLException {
			TaxPeriod t = new TaxPeriod();
			t.setId(SqlUtils.getInteger(rs, "id"));
			t.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
			t.setYear(SqlUtils.getInteger(rs,"year"));
			return t;
		}
	}

	@Override
	@Cacheable(CacheConstants.TAX_PERIOD)
	public TaxPeriod get(int taxPeriodId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select id, tax_type, year from tax_period where id = ?",
					new Object[] { taxPeriodId },
					new int[] { Types.NUMERIC },
					new TaxPeriodRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти налоговый период с id = " + taxPeriodId);
		}
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		try {
			return getJdbcTemplate().query(
					"select id, tax_type, year from tax_period where tax_type = ? order by year",
					new Object[]{taxType.getCode()},
					new int[] { Types.VARCHAR },
					new TaxPeriodRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти налоговые периоды с типом = " + taxType.getCode());
		}
	}

	@Override
	public TaxPeriod getByTaxTypeAndYear(TaxType taxType, int year) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, tax_type, year from tax_period where tax_type = ? and year = ?",
                    new Object[]{taxType.getCode(), year},
                    new int[]{Types.VARCHAR, Types.NUMERIC},
                    new TaxPeriodRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
	}

	@Override
	public int add(TaxPeriod newTaxPeriod) {
		Integer id = newTaxPeriod.getId();
		if (id == null) {
			id = generateId("seq_tax_period", Integer.class);
		}
		sqlQueryFactory.insert(taxPeriod)
				.columns(
						taxPeriod.id,
						taxPeriod.year,
						taxPeriod.taxType)
				.values(
						id,
						newTaxPeriod.getYear(),
						TaxType.NDFL.getCode()
				)
				.execute();
		return id;
	}

	@Override
	public TaxPeriod getLast(TaxType taxType) {
		try {
			return getJdbcTemplate().queryForObject( //TODO Вероятно, это можно оптимизировать
					"select id, tax_type, year from tax_period where tax_type = ? and " +
							"year = (select max(year) from tax_period where tax_type = ?)",
					new Object[]{taxType.getCode(), taxType.getCode()},
					new int[] { Types.VARCHAR, Types.VARCHAR},
					new TaxPeriodRowMapper()
			);

		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

    @Override
    public void delete(int taxPeriodId) {
        getJdbcTemplate().update(
          "delete from tax_period where id = ?",
          new Object[]{taxPeriodId},
          new int[]{Types.NUMERIC}
        );
    }
}