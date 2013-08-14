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
	@Autowired
	private TransportTaxMapper transportTaxMapper;

	@Override
	public int getTaxRate(String code, BigDecimal age, BigDecimal power, String regionId) {
		List<Integer> list = transportTaxMapper.getTransportTaxRate(code, age.intValue(), power.intValue(), regionId);

		if (list.size() <= 0 && code.length() == 5) {
			// Если не найдём совпадения по точному коду, то ищем совпадение по коду с плейсхолдерами
			code = code.substring(0, 3) + "??";
			list = transportTaxMapper.getTransportTaxRate(code, age.intValue(), power.intValue(), regionId);
		}

		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() <= 0) {
			throw new DaoException("Не удалось определить значение ставки налога для транспортного средства.");
		} else {
			throw new DaoException(
					"Не удалось определить значение ставки налога для транспортного средства, " +
							"т.к. в справчонике существует более 1й записи, " +
							"удовлетворяющей заданным параметрам транспортного средства."
			);
		}
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
