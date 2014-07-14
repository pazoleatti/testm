package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodMappingDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodMappingTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReportPeriodMappingTest {

    @Autowired
    private ReportPeriodMappingDao mappingDao;

    @Autowired
    private ReportPeriodDao periodDao;

    @Test
    public void getByTaxPeriodAndDictTest() {
        int taxPeriodId = 21;
        int dictTaxPeriodId = 99; // TODO Левыкин: Тут какая-то ошибка в ДАО, имеется в виду не dictTaxPeriodId, а код периода «99»!

        Integer reportPeriodId = mappingDao.getByTaxPeriodAndDict(taxPeriodId, dictTaxPeriodId);

        ReportPeriod reportPeriod = periodDao.get(reportPeriodId);

        assertEquals(Integer.valueOf(3), reportPeriod.getId());
        assertEquals("VAT report period 1", reportPeriod.getName());
        assertEquals(taxPeriodId, reportPeriod.getTaxPeriod().getId().intValue());
        assertEquals(21, reportPeriod.getDictTaxPeriodId());
    }
}
