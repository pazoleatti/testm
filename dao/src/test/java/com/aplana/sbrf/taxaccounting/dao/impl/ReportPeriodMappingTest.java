package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodMappingDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodMappingTest.xml"})
@Transactional
public class ReportPeriodMappingTest {

    @Autowired
    private ReportPeriodMappingDao mappingDao;

    @Autowired
    private ReportPeriodDao periodDao;

    @Test
    public void getByTaxPeriodAndDictTest() {

        int taxPeriodId = 31;
        int dictTaxPeriodId = 21;

        Integer reportPeriodId = mappingDao.getByTaxPeriodAndDict(taxPeriodId, dictTaxPeriodId);

        ReportPeriod reportPeriod = periodDao.get(reportPeriodId);

        assertEquals(Integer.valueOf(4), reportPeriod.getId());
        assertEquals("Income report period 1", reportPeriod.getName());
        assertEquals(3, reportPeriod.getMonths());
        assertEquals(Integer.valueOf(31), reportPeriod.getTaxPeriod().getId());
        assertEquals(1, reportPeriod.getOrder());
        assertEquals(21, reportPeriod.getDictTaxPeriodId());

    }

}
