package com.aplana.sbrf.taxaccounting.dao.dictionary.impl;

import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.dao.dictionary.mapper.TransportTaxMapper;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Types;

@Repository("transportTaxDao")
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
		Integer result = transportTaxMapper.getTransportTaxRate(code, age.intValue(), power.intValue());
		if (result == null && code.length() == 5) {
			// Если не найдём совпадения по точному коду, то ищем совпадение по коду с плейсхолдерами
			code = code.substring(0, 3) + "xx";
			result = transportTaxMapper.getTransportTaxRate(code, age.intValue(), power.intValue());
		}
		if (result == null) {
			throw new DaoException("Не удалось определить значение ставки налога для транспортного средства");
		}
		return result.intValue();
	}

	/**
	 * Возвращает название типа транспортного средства по коду
	 *
	 * @param tsTypeCode код типа транспортного средства
	 * @return название типа транспортного средства
	 */
	@Override
	public String getTsTypeName(String tsTypeCode) {
		return transportTaxMapper.getTsTypeName(tsTypeCode);
	}

	/**
	 * Проверяет существование кода ОКАТО
	 *
	 * @param okato код ОКАТО
	 * @return true - если код ОКАТО существует, false - если не существует
	 */
	@Override
	public boolean validateOkato(String okato) {
		return getJdbcTemplate().queryForInt(
				"select count(*) from transport_okato where okato=?",
				new Object[]{okato}, new int[]{Types.VARCHAR}
		) > 0;
	}

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

	/**
	 * Проверяет существование кода экологического класса
	 *
	 * @param code код экологического класса
	 * @return true - если код существует, false - если нет
	 */
	@Override
	public boolean validateEcoClass(BigDecimal code) {
		return getJdbcTemplate().queryForInt(
				"select count(*) from transport_eco_class where code = ?",
				new Object[]{code}, new int[]{Types.NUMERIC}
		) > 0;
	}
}
