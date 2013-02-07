package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.TaxPeriodMapper;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация DAO для работы с {@link com.aplana.sbrf.taxaccounting.model.TaxPeriod налоговыми периодами}
 */
@Repository
@Transactional(readOnly = true)
public class TaxPeriodDaoImpl implements TaxPeriodDao {

	@Autowired
	TaxPeriodMapper taxPeriodMapper;

	@Override
	public TaxPeriod get(int taxPeriodId) {
		TaxPeriod result = taxPeriodMapper.get(taxPeriodId);
		if (result == null) {
			throw new DaoException("Не удалось найти налоговый период с id = " + taxPeriodId);
		}
		return result;
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		return taxPeriodMapper.listByTaxType(taxType.getCode());
	}
}