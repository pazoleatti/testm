package com.aplana.sbrf.taxaccounting.service.script;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryTaxBenefitParamDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxBenefitParam;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryTaxPrivilegeParamService;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.impl.DictionaryTaxPrivilegeParamServiceImpl;

public class DictionaryTaxPrivilegeParamServiceTest {
	
	private static DictionaryTaxPrivilegeParamService service = new DictionaryTaxPrivilegeParamServiceImpl();
	private static DictionaryTaxBenefitParam benefitParam1 = new DictionaryTaxBenefitParam(2, "20200", null, null, null, 0d, 0d);
    private static DictionaryTaxBenefitParam benefitParam2 = new DictionaryTaxBenefitParam(2, "20210", null, null, null, 0d, 0d);
    
    @BeforeClass
    public static void tearUp() {
    	DictionaryTaxBenefitParamDao dictionaryTaxBenefitParamDao = mock(DictionaryTaxBenefitParamDao.class);
        List<DictionaryTaxBenefitParam> values = new ArrayList<DictionaryTaxBenefitParam>();
        values.add(benefitParam1);
        values.add(benefitParam2);
        when(dictionaryTaxBenefitParamDao.getListParams()).thenReturn(values);

        ReflectionTestUtils.setField(service, "dictionaryTaxBenefitParamDao", dictionaryTaxBenefitParamDao);
    }

	/*
	 * Тестирование сервиса получения данных по 
	 * «Код субъекта» и «Код налоговой льготы»
	 * используемого для декларации по транспорту
	 */
	@Test
	public void TestDeclarationData(){
		assertTrue(service.get(2, "20210").equals(benefitParam2));
	}

}
