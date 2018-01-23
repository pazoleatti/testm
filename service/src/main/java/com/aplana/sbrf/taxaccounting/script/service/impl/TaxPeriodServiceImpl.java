package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.script.service.TaxPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("taxPeriodService")
public class TaxPeriodServiceImpl implements TaxPeriodService {

	@Autowired
	TaxPeriodDao taxPeriodDao;
	
	@Override
	public TaxPeriod get(int taxPeriodId) {
		return taxPeriodDao.fetchOne(taxPeriodId);
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		return taxPeriodDao.fetchAllByTaxType(taxType);
	}
}
