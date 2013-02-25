package com.aplana.sbrf.taxaccounting.service.script.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;

@Repository("taxPeriodService")
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
	

}
