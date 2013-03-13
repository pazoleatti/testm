package com.aplana.sbrf.taxaccounting.service.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.DictionaryTaxBenefitParam;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface DictionaryTaxBenefitParamService {
	/*
	 * Функция для получения в справочнике «Параметры налоговых льгот» записи, соответствующей значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
	 */
	public  DictionaryTaxBenefitParam get(String dictRegionId, String  taxBenefitId);
}
