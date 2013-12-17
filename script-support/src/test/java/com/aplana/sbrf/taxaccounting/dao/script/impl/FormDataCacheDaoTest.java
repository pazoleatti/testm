package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.BDUtils;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataCacheDaoTest.xml"})
public class FormDataCacheDaoTest {
    @Autowired
    FormDataCacheDao dao;

    @Autowired
    FormDataDao formDataDao;

    @Autowired
    DataRowDao dataRowDao;

    static Long cnt = 5L;

    @Before
    public void init(){
        /**
         * Т.к. hsqldb не поддерживает запрос который мы используем в дао
         * пришлось немного закостылять этот момент. Заапрувлено Ф.Маратом.
         */
        // мок утилитного сервиса
        BDUtils dbUtilsMock = mock(BDUtils.class);
        when(dbUtilsMock.getNextIds(anyLong())).thenAnswer(new org.mockito.stubbing.Answer<List<Long>>() {
            @Override
            public List<Long> answer(InvocationOnMock invocationOnMock) throws Throwable {
                List<Long> ids = new ArrayList<Long>();
                Object[] args = invocationOnMock.getArguments();
                int count = ((Long) args[0]).intValue();
                for (int i = 0; i < count; i++) {
                    ids.add(cnt++);
                }
                return ids;
            }
        });
        dataRowDao.setDbUtils(dbUtilsMock);
    }

    @Test
    public void getRefBookMapTest1() {
        Map<Long, Map<String, RefBookValue>> map = dao.getRefBookMap(1L);

        Map<String, RefBookValue> data = map.get(1L);
        Assert.assertEquals(data.get("author").getReferenceValue().longValue(), 5L);
        Assert.assertEquals(data.get("weight").getNumberValue().doubleValue(), 0.25d, 0);
        Assert.assertEquals(data.get("order").getNumberValue().doubleValue(), 1113d, 0);
        Assert.assertEquals(data.get("name").getStringValue(), "Алиса в стране чудес");

        data = map.get(7L);
        Assert.assertEquals(data.get("name").getStringValue(), "Петренко П.П.");
    }

    @Test
    public void getRefBookMapTest2() {
        FormData formData = formDataDao.get(1L);
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        DataRow<Cell> dataRow = formData.createDataRow();
        dataRow.put("referenceColumn1", 5L);
        dataRow.put("referenceColumn2", 4L);
        rows.add(dataRow);
        dataRowDao.saveRows(formData, rows);
        dataRowDao.commit(formData.getId());

        Map<Long, Map<String, RefBookValue>> map = dao.getRefBookMap(1L);

        Map<String, RefBookValue> data = map.get(4L);
        Assert.assertEquals(data.get("author").getReferenceValue().longValue(), 6L);
        Assert.assertEquals(data.get("weight").getNumberValue().doubleValue(), 2.399d, 0);
        Assert.assertEquals(data.get("order").getNumberValue().doubleValue(), 425d, 0);
        Assert.assertEquals(data.get("name").getStringValue(), "Вий");

        data = map.get(5L);
        Assert.assertEquals(data.get("name").getStringValue(), "Иванов И.И.");
    }

    @Test
    public void getRefBookMapTest3() {
        Assert.assertTrue(dao.getRefBookMap(-1L).size() == 0);
        Assert.assertTrue(dao.getRefBookMap(null).size() == 0);
    }
}
