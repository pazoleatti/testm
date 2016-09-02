package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookAsyncConfigDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookAsyncConfigTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookAsyncConfigDaoTest {
    @Autowired
    RefBookAsyncConfigDao refBookAsyncConfigDao;

    @Test
    public void getRecords() {
        PagingResult<Map<String, RefBookValue>> result = refBookAsyncConfigDao.getRecords();
        assertEquals(3, result.size());
        assertEquals("task1", result.get(0).get(ConfigurationParamModel.ASYNC_TYPE).getStringValue());
        assertEquals(200, result.get(1).get(ConfigurationParamModel.ASYNC_SHORT_LIMIT).getNumberValue());
        assertEquals("kind3", result.get(2).get(ConfigurationParamModel.ASYNC_LIMIT_KIND).getStringValue());
    }

    @Test
    public void updateRecords() {
        List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();
        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
        map.put(ConfigurationParamModel.ASYNC_TYPE_ID, new RefBookValue(RefBookAttributeType.STRING, "1"));
        map.put(ConfigurationParamModel.ASYNC_SHORT_LIMIT, new RefBookValue(RefBookAttributeType.STRING, "111"));
        map.put(ConfigurationParamModel.ASYNC_LIMIT, new RefBookValue(RefBookAttributeType.STRING, "1111"));
        records.add(map);
        map = new HashMap<String, RefBookValue>();
        map.put(ConfigurationParamModel.ASYNC_TYPE_ID, new RefBookValue(RefBookAttributeType.STRING, "2"));
        map.put(ConfigurationParamModel.ASYNC_SHORT_LIMIT, new RefBookValue(RefBookAttributeType.STRING, "222"));
        map.put(ConfigurationParamModel.ASYNC_LIMIT, new RefBookValue(RefBookAttributeType.STRING, "2222"));
        records.add(map);

        refBookAsyncConfigDao.updateRecords(records);

        PagingResult<Map<String, RefBookValue>> result = refBookAsyncConfigDao.getRecords();
        assertEquals(111, result.get(0).get(ConfigurationParamModel.ASYNC_SHORT_LIMIT).getNumberValue());
        assertEquals(1111, result.get(0).get(ConfigurationParamModel.ASYNC_LIMIT).getNumberValue());
        assertEquals(222, result.get(1).get(ConfigurationParamModel.ASYNC_SHORT_LIMIT).getNumberValue());
        assertEquals(2222, result.get(1).get(ConfigurationParamModel.ASYNC_LIMIT).getNumberValue());
    }
}
