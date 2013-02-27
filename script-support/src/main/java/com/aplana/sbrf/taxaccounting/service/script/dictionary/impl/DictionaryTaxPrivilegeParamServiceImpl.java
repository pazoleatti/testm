package com.aplana.sbrf.taxaccounting.service.script.dictionary.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryTaxBenefitParamDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxBenefitParam;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryTaxPrivilegeParamService;

public class DictionaryTaxPrivilegeParamServiceImpl implements DictionaryTaxPrivilegeParamService {
	
	@Autowired
	DictionaryTaxBenefitParamDao dictionaryTaxBenefitParamDao;
	
	@Override
	public DictionaryTaxBenefitParam get(Integer dictRegionId, String  taxBenefitId) {
			
		List<DictionaryTaxBenefitParam> privilegeParams = dictionaryTaxBenefitParamDao.getListParams();
		for (DictionaryTaxBenefitParam param: privilegeParams){
			if (param.getDictRegionId().equals(dictRegionId) && param.getTaxBenefitId().equals(taxBenefitId)){
				return param;
			}
		}
		return null;
	}

}
