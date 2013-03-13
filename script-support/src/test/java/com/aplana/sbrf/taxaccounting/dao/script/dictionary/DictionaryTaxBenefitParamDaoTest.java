package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxBenefitParam;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DictionaryTaxBenefitParamDaoTest.xml"})
public class DictionaryTaxBenefitParamDaoTest {

	@Autowired
    private DictionaryTaxBenefitParamDao dao;
	
	private static DictionaryTaxBenefitParam benefitParam1 = new DictionaryTaxBenefitParam("02", "20200", null, null, null, 0d, 0d);
    private static DictionaryTaxBenefitParam benefitParam2 = new DictionaryTaxBenefitParam("02","20210", null, null, null, 0d, 0d);

	@Test
    public void testSimple(){
        assertNotNull(dao);
    }
    @Test
    public void testListParams() {
        List<DictionaryTaxBenefitParam> values = new ArrayList<DictionaryTaxBenefitParam>();
        values.add(benefitParam1);
        values.add(benefitParam2);
        assertArrayEquals( values.toArray(), dao.getListParams().toArray());   
    }
}

