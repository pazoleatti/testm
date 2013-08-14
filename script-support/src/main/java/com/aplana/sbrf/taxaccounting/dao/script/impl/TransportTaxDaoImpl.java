package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.script.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.dao.script.mapper.TransportTaxMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

@Repository("transportTaxDao")
@Transactional(readOnly = true)
public class TransportTaxDaoImpl extends AbstractDao implements TransportTaxDao {

	/**
	 * Проверяет существование кода транспортного средства
	 *
	 * @param code код типа транспортного средства
	 * @return true - если код существует, false - если не существует
	 */
	@Override
	public boolean validateTransportTypeCode(String code) {
		return getJdbcTemplate().queryForInt(
				"select count(*) from transport_type_code where code=?",
				new Object[]{code}, new int[]{Types.VARCHAR}
		) > 0;
	}

	/**
	 * Проверяет существование кода единицы измерения
	 *
	 * @param code код
	 * @return true - если код существует, false - если не существует
	 */
	@Override
	public boolean validateTaxBaseUnit(BigDecimal code) {
		return getJdbcTemplate().queryForInt(
				"select count(*) from transport_unit_code where code=?",
				new Object[]{code}, new int[]{Types.NUMERIC}
		) > 0;
	}
}
