package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.Arrays.asList;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 12:37
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ConfigurationDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ConfigurationDaoTest {

	@Autowired
	private ConfigurationDao dao;

    @Test
	public void getAllTest() {
        ConfigurationParamModel model = dao.getAll();
        Assert.assertEquals(7, model.size());
        Assert.assertTrue(model.containsKey(ConfigurationParam.REF_BOOK_KEY_FILE));
        Assert.assertTrue(model.containsKey(ConfigurationParam.ERROR_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.FORM_DATA_KEY_FILE));
        Assert.assertTrue(model.containsKey(ConfigurationParam.ACCOUNT_PLAN_TRANSPORT_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.UPLOAD_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.OKATO_TRANSPORT_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.ARCHIVE_DIRECTORY));
        Assert.assertEquals("test1", model.get(ConfigurationParam.FORM_DATA_KEY_FILE, 1).get(0));
        Assert.assertEquals("test6", model.get(ConfigurationParam.UPLOAD_DIRECTORY, 1).get(0));
        Assert.assertEquals("test7", model.get(ConfigurationParam.UPLOAD_DIRECTORY, 2).get(0));
    }

    @Test
    public void save1Test() {
        ConfigurationParamModel model = dao.getAll();
        model.put(ConfigurationParam.REGION_TRANSPORT_DIRECTORY, 1, asList("testSaveRegion"));
        model.put(ConfigurationParam.UPLOAD_DIRECTORY, 1, null);
        dao.save(model);
        model = dao.getAll();
        Assert.assertTrue(model.containsKey(ConfigurationParam.UPLOAD_DIRECTORY));
        Assert.assertNull(model.get(ConfigurationParam.UPLOAD_DIRECTORY).get(1));
        Assert.assertTrue(model.containsKey(ConfigurationParam.REGION_TRANSPORT_DIRECTORY));
        Assert.assertEquals("testSaveRegion", model.get(ConfigurationParam.REGION_TRANSPORT_DIRECTORY, 1).get(0));
    }

    // Попытка сохраннить запись с ссылкой на несуществующий depatment_id
    @Test(expected = RuntimeException.class)
    public void save2Test() {
        ConfigurationParamModel model = dao.getAll();
        model.put(ConfigurationParam.REGION_TRANSPORT_DIRECTORY, -99, asList("testSaveRegion"));
        dao.save(model);
    }

    // Удаление
    public void save3Test() {
        ConfigurationParamModel model = dao.getAll();
        model.remove(ConfigurationParam.REF_BOOK_KEY_FILE);
        model.get(ConfigurationParam.UPLOAD_DIRECTORY).remove(1);
        dao.save(model);
        model = dao.getAll();
        Assert.assertNull(model.get(ConfigurationParam.REF_BOOK_KEY_FILE));
        Assert.assertNull(model.get(ConfigurationParam.UPLOAD_DIRECTORY).get(1));
    }
}
