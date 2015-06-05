package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataCacheDaoTest.xml"})
@DirtiesContext
public class FormDataCacheDaoTest {
    @Autowired
    FormDataCacheDao dao;

    @Autowired
    FormDataDao formDataDao;

    @Autowired
    DataRowDao dataRowDao;

    @Test
    public void getRefBookMapTest1() {
		FormData formData = formDataDao.get(1L, false);
        Map<String, Map<String, RefBookValue>> map = dao.getRefBookMap(formData);
        Map<String, RefBookValue> data = map.get("1_1");
        Assert.assertEquals(data.get("author").getReferenceValue().longValue(), 5L);
        Assert.assertEquals(data.get("weight").getNumberValue().doubleValue(), 0.25d, 0);
        Assert.assertEquals(data.get("order").getNumberValue().doubleValue(), 1113d, 0);
        Assert.assertEquals(data.get("name").getStringValue(), "Алиса в стране чудес");

        data = map.get("2_7");
        Assert.assertEquals(data.get("name").getStringValue(), "Петренко П.П.");
    }

    @Test
    public void getRefBookMapTest2() {
        FormData formData = formDataDao.get(1L, false);
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        DataRow<Cell> dataRow = formData.createDataRow();
        dataRow.put("referenceColumn1", 5L);
        dataRow.put("referenceColumn2", 4L);
        rows.add(dataRow);
        dataRowDao.saveRows(formData, rows);
        dataRowDao.commit(formData);

        Map<String, Map<String, RefBookValue>> map = dao.getRefBookMap(formData);

        Map<String, RefBookValue> data = map.get("1_4");
        Assert.assertEquals(data.get("author").getReferenceValue().longValue(), 6L);
        Assert.assertEquals(data.get("weight").getNumberValue().doubleValue(), 2.399d, 0);
        Assert.assertEquals(data.get("order").getNumberValue().doubleValue(), 425d, 0);
        Assert.assertEquals(data.get("name").getStringValue(), "Вий");

        data = map.get("2_5");
        Assert.assertEquals(data.get("name").getStringValue(), "Иванов И.И.");
    }

}
