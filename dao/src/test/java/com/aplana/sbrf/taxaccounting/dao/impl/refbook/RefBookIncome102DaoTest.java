package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
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
@ContextConfiguration({"RefBookIncome102DaoImplTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookIncome102DaoTest {

    @Autowired
    RefBookIncome102Dao dao;

    @Test
    public void getRecordsTest() {
        // Без фильтра
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 5);
        Assert.assertEquals(records.getTotalCount(), 5);

        // ACCOUNT_PERIOD_ID = 1
        records = dao.getRecords(null, "ACCOUNT_PERIOD_ID = 2", null);
        Assert.assertEquals(records.size(), 1);
        Assert.assertEquals(records.getTotalCount(), 1);
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
        Assert.assertEquals(list1.size(), 5);
        dao.updateRecords(records);
        records = dao.getRecords(null, null, null);
        List<Map<String, RefBookValue>> list2 = records;
        Assert.assertEquals(list2.size(), 5);

        for (int i = 0; i < list1.size(); i++) {
            Map<String, RefBookValue> map1 = list1.get(i);
            Map<String, RefBookValue> map2 = list2.get(i);
            Assert.assertEquals(map1.get("ACCOUNT_PERIOD_ID"), map2.get("ACCOUNT_PERIOD_ID"));
            Assert.assertEquals(map1.get("OPU_CODE"), map2.get("OPU_CODE"));
            Assert.assertEquals(map1.get("TOTAL_SUM"), map2.get("TOTAL_SUM"));
            Assert.assertEquals(map1.get("ITEM_NAME"), map2.get("ITEM_NAME"));
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
        Assert.assertEquals(records.size(), 5);
        Map<String, RefBookValue> record = records.get(0);
        long accountPeriodId = record.get("ACCOUNT_PERIOD_ID").getReferenceValue();

        record.put("OPU_CODE", new RefBookValue(RefBookAttributeType.STRING, testVal1));
        record.put("TOTAL_SUM", new RefBookValue(RefBookAttributeType.NUMBER, testVal2));

        dao.updateRecords(Arrays.asList(record));
        records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 2);

        boolean catcha = false;
        for (Map<String, RefBookValue> map : records) {
            long accountPeriodId2 = map.get("ACCOUNT_PERIOD_ID").getReferenceValue();

            if (accountPeriodId2 == accountPeriodId) {
                Assert.assertEquals(map.get("OPU_CODE").getStringValue(), testVal1);
                Assert.assertEquals(map.get("TOTAL_SUM").getNumberValue().doubleValue(), testVal3, 0d);
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
        record.put("OPU_CODE", new RefBookValue(RefBookAttributeType.STRING, "a1"));
        dao.updateRecords(Arrays.asList(record));
        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(null, null, null);
        Assert.assertEquals(records.size(), 5);
    }

    @Test
    public void getPeriodNameFromRefBookTest() {
        Assert.assertEquals("", dao.getPeriodNameFromRefBook(1));
    }
}
