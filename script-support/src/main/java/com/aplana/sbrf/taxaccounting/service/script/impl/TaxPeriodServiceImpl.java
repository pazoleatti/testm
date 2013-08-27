package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("taxPeriodService")
public class TaxPeriodServiceImpl implements TaxPeriodService {

	@Autowired
	TaxPeriodDao dao;
	
	@Override
	public TaxPeriod get(int taxPeriodId) {
		return dao.get(taxPeriodId);
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		return dao.listByTaxType(taxType);
	}

	@Override
	public List<TaxPeriod> listByTaxTypeAndDate(TaxType taxType, Date from, Date to) {
		return dao.listByTaxTypeAndDate(taxType, from, to);
	}

	@Override
	public int add(TaxPeriod taxPeriod) {
		return dao.add(taxPeriod);
	}


}
