package com.aplana.sbrf.taxaccounting.script.dao.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:script/refbook/RefBookOkatoDaoTest.xml"})
@DirtiesContext
public class RefBookOkatoDaoTest {

    @Autowired
    private RefBookOkatoDao refBookOkatoDao;

    @Autowired
    private RefBookDao rbDao;

    private Date getDate(int day, int month, int year) {
        return new GregorianCalendar(year, month - 1, day, 15, 46, 57).getTime();
    }

    @Test
    public void updateValueNamesTest() {
        String okato1 = "57401365000", okato2 = "57401000000", okato3 = "123000000000",
                name1 = "Дзержинский_test", name2 = "Пермь_test", name3 = "test_test";
        Date version = getDate(1, 1, 2013);
        List<Map<String, RefBookValue>> recList = new LinkedList<Map<String, RefBookValue>>();
        HashMap<String, RefBookValue> map1 = new HashMap<String, RefBookValue>();
        map1.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name1));
        map1.put("OKATO", new RefBookValue(RefBookAttributeType.STRING, okato1));
        recList.add(map1);
        HashMap<String, RefBookValue> map2 = new HashMap<String, RefBookValue>();
        map2.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name2));
        map2.put("OKATO", new RefBookValue(RefBookAttributeType.STRING, okato2));
        recList.add(map2);
        HashMap<String, RefBookValue> map3 = new HashMap<String, RefBookValue>();
        map3.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name3));
        map3.put("OKATO", new RefBookValue(RefBookAttributeType.STRING, okato3));
        recList.add(map3);

        List<Map<String, RefBookValue>> updResult = refBookOkatoDao.updateValueNames(version, recList);
        // Один код не должен быть найден
        Assert.assertEquals(updResult.size(), 1);
        Assert.assertEquals(updResult.get(0), map3);

        PagingResult<Map<String, RefBookValue>> result = rbDao.getRecords(3L, version,
                new PagingParams(), null, null);

        Assert.assertEquals(result.size(), 3);

        for (Map<String, RefBookValue> rec : result) {
            if (rec.get("OKATO").getStringValue().equals(okato1)) {
                Assert.assertEquals(rec.get("NAME").getStringValue(), name1);
            } else if (rec.get("OKATO").getStringValue().equals(okato2)) {
                Assert.assertEquals(rec.get("NAME").getStringValue(), name2);
            } else {
                Assert.assertEquals(rec.get("OKATO").getStringValue(), "57000000000");
                Assert.assertEquals(rec.get("NAME").getStringValue(), "Пермский край");
            }
        }
    }
}
