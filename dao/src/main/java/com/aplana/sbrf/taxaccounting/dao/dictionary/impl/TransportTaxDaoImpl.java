package com.aplana.sbrf.taxaccounting.dao.dictionary.impl;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryManager;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.dao.dictionary.mapper.TransportTaxMapper;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;

@Repository("transportTaxDao")
@Transactional(readOnly = true)
public class TransportTaxDaoImpl implements TransportTaxDao {
	@Autowired
	private TransportTaxMapper transportTaxMapper;

	@Autowired
	private DictionaryManager<String> stringDictionaryManager;

	@Autowired
	private DictionaryManager<BigDecimal> numericDictionaryManager;

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
		return stringDictionaryManager.getDataProvider("transportOkato").getItem(okato) != null;
	}

	/**
	 * Проверяет существование кода транспортного средства
	 *
	 * @param code код типа транспортного средства
	 * @return true - если код существует, false - если не существует
	 */
	@Override
	public boolean validateTransportTypeCode(String code) {
		return stringDictionaryManager.getDataProvider("transportTypeCode").getItem(code) != null;
	}

	/**
	 * Возвращает название типа транспортного средства по коду
	 *
	 * @param code код типа транспортного средства
	 * @return название типа транспортного средства
	 */
	@Override
	public String getTransportTypeName(String code) {
		DictionaryItem<String> item = stringDictionaryManager.getDataProvider("transportTypeCode").getItem(code);
		if (item != null) {
			return item.getName();
		} else {
			return null;
		}
	}

	/**
	 * Проверяет существование кода единицы измерения
	 *
	 * @param code код
	 * @return true - если код существует, false - если не существует
	 */
	@Override
	public boolean validateTaxBaseUnit(BigDecimal code) {
		return numericDictionaryManager.getDataProvider("transportTaxBaseUnitCode").getItem(code) != null;
	}
}
