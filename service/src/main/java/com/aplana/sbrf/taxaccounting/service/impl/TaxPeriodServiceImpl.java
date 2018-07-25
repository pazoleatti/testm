package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.TaxPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaxPeriodServiceImpl implements TaxPeriodService {

    @Autowired
    TaxPeriodDao taxPeriodDao;

    @Override
    public TaxPeriod fetchOrCreate(int year) {
        TaxPeriod taxPeriod = taxPeriodDao.fetchOneByYear(year);
        if (taxPeriod == null) {
            taxPeriod = new TaxPeriod();
            taxPeriod.setYear(year);
            taxPeriod.setId(taxPeriodDao.create(taxPeriod));
        }
        return taxPeriod;
    }
}
