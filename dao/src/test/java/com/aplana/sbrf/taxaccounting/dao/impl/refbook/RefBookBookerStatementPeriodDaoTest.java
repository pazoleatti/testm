package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBookerStatementPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookBookerStatementPeriodDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookBookerStatementPeriodDaoTest {

    @Autowired
    RefBookBookerStatementPeriodDao dao;

    @Test
    public void getRecordsTest() {
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords();
        assertEquals(4, records.size());
        assertEquals(4, records.getTotalCount());
        assertEquals(2016, records.get(0).get("YEAR").getNumberValue().intValue());
    }

    @Test
    public void testGetRecordData(){
        Map<String, RefBookValue> record = dao.getRecordData(11l);
        assertEquals(4, record.size());
        assertEquals(2014, record.get("YEAR").getNumberValue().intValue());
    }
}
