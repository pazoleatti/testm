package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookIncome101DaoImplTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookIncome101DaoTest {

    @Autowired
    RefBookIncome101Dao dao;

    @Test
    public void getRecordsTest() {
        // Без фильтра
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 4);
        Assert.assertEquals(records.getTotalCount(), 4);

        // ACCOUNT_PERIOD_ID = 1
        records = dao.getRecords(null, "ACCOUNT_PERIOD_ID = 1", null);
        Assert.assertEquals(records.size(), 3);
        Assert.assertEquals(records.getTotalCount(), 3);
    }

    @Test
    public void getRecordDataTest() {
            Map<String, RefBookValue> record = dao.getRecordData(2L);
            Assert.assertEquals(record.get("ACCOUNT_PERIOD_ID").getReferenceValue().longValue(), 1L);
    }

    @Test
    public void updateRecordsTest1() {
        // Обновление всех имеющихся записей без изменений
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(null, null, null);
        List<Map<String, RefBookValue>> list1 = records;
        Assert.assertEquals(list1.size(), 4);
        dao.updateRecords(records);
        records = dao.getRecords(null, null, null);
        List<Map<String, RefBookValue>> list2 = records;
        Assert.assertEquals(list2.size(), 4);

        for (int i = 0; i < list1.size(); i++) {
            Map<String, RefBookValue> map1 = list1.get(i);
            Map<String, RefBookValue> map2 = list2.get(i);
            Assert.assertEquals(map1.get("ACCOUNT"), map2.get("ACCOUNT"));
            Assert.assertEquals(map1.get("INCOME_DEBET_REMAINS"), map2.get("INCOME_DEBET_REMAINS"));
            Assert.assertEquals(map1.get("INCOME_CREDIT_REMAINS"), map2.get("INCOME_CREDIT_REMAINS"));
            Assert.assertEquals(map1.get("DEBET_RATE"), map2.get("DEBET_RATE"));
            Assert.assertEquals(map1.get("CREDIT_RATE"), map2.get("CREDIT_RATE"));
            Assert.assertEquals(map1.get("OUTCOME_DEBET_REMAINS"), map2.get("OUTCOME_DEBET_REMAINS"));
            Assert.assertEquals(map1.get("OUTCOME_CREDIT_REMAINS"), map2.get("OUTCOME_CREDIT_REMAINS"));
            Assert.assertEquals(map1.get("ACCOUNT_NAME"), map2.get("ACCOUNT_NAME"));
            Assert.assertEquals(map1.get("ACCOUNT_PERIOD_ID"), map2.get("ACCOUNT_PERIOD_ID"));
            Assert.assertNotEquals(map1.get(RefBook.RECORD_ID_ALIAS), map2.get(RefBook.RECORD_ID_ALIAS));
        }
    }

    @Test
    public void updateRecordsTest2() {
        // Обновление одной записи с изменениями
        String testVal1 = "test1";
        Double testVal2 = 100.56755d;
        Double testVal3 = 100.5676d;
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 4);
        Map<String, RefBookValue> record = records.get(0);
        long accountPeriodId = record.get("ACCOUNT_PERIOD_ID").getReferenceValue();

        record.put("ACCOUNT_NAME", new RefBookValue(RefBookAttributeType.STRING, testVal1));
        record.put("INCOME_DEBET_REMAINS", new RefBookValue(RefBookAttributeType.NUMBER, testVal2));
        record.put("OUTCOME_DEBET_REMAINS", new RefBookValue(RefBookAttributeType.NUMBER, null));
        dao.updateRecords(Arrays.asList(record));
        records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 2);

        boolean catcha = false;
        for (Map<String, RefBookValue> map : records) {
            long accountPeriodId2 = map.get("ACCOUNT_PERIOD_ID").getReferenceValue();
            if (accountPeriodId == accountPeriodId2) {
                Assert.assertEquals(map.get("ACCOUNT_NAME").getStringValue(), testVal1);
                Assert.assertEquals(map.get("INCOME_DEBET_REMAINS").getNumberValue().doubleValue(), testVal3.doubleValue(), 0d);
                catcha = true;
                break;
            }
        }
        Assert.assertTrue(catcha);
    }

    @Test
    public void updateRecordsTest3() {
        // Добавление одной записи
        Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
        record.put("ACCOUNT_PERIOD_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
        record.put("ACCOUNT", new RefBookValue(RefBookAttributeType.STRING, "a1"));
        dao.updateRecords(Arrays.asList(record));
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 4);
    }

    @Test
    public void testUniqueRecordIds(){
        Assert.assertEquals(0, dao.getUniqueRecordIds("department_id = 0").size());
    }
    
    @Test
    public void testGetSeparatedValue(){
        Assert.assertEquals("2015.0000000000000000000 Период1", dao.getPeriodNameFromRefBook(1l));
    }
}
