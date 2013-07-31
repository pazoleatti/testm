package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;


public interface DictionaryTaxPeriodDao {
	List<DictionaryTaxPeriod> getByTaxType(TaxType taxType);
	DictionaryTaxPeriod get(int code);
}
