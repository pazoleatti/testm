package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DictionaryTaxBenefitParam;

public interface DictionaryTaxBenefitParamDao {
	/**
     * получает список параметров
     * @return
     */
	public List<DictionaryTaxBenefitParam> getListParams();
}
