package com.aplana.sbrf.taxaccounting.dao.dictionary.impl;

import java.math.BigDecimal;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.dao.dictionary.mapper.TransportTaxMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;

@Repository
@Transactional(readOnly = true)
public class TransportTaxDaoImpl extends AbstractDao implements TransportTaxDao {
	@Autowired
	private TransportTaxMapper transportTaxMapper;
	
	@Override
	public String getRegionName(String okato) {
		return transportTaxMapper.getRegionName(okato);
	}

	@Override
	public int getTaxRate(String code, BigDecimal age, BigDecimal power) {
		String sql = "select value from transport_tax_rate " +
				"where code = ? " +
				"and (min_age is null or min_age <= ?) and (max_age is null or max_age >= ?)" +
				"and (min_power is null or min_power <= ?) and (max_power is null or max_power >= ?)";
		Object[] params = new Object[] {code, age, age, power, power};
		int[] types = new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC };
		try {
			try {
				return getJdbcTemplate().queryForInt(sql, params, types);
			} catch (EmptyResultDataAccessException e) {
				return getJdbcTemplate().queryForInt(sql, params, types);
			}
		} catch (DataAccessException e) {
			throw new RuntimeException("Не удалось определить значение ставки налога для транспортного средства"); 
		}
	}
}
