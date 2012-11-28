package com.aplana.sbrf.taxaccounting.dao.dictionary.impl;

import java.math.BigDecimal;

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
}
