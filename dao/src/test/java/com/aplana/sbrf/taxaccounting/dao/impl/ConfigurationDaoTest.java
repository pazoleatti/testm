package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

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
        Assert.assertEquals(9, model.size());
        Assert.assertTrue(model.containsKey(ConfigurationParam.KEY_FILE));
        Assert.assertTrue(model.containsKey(ConfigurationParam.FORM_ERROR_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.OKATO_UPLOAD_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.FORM_UPLOAD_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.FORM_ARCHIVE_DIRECTORY));
        Assert.assertTrue(model.containsKey(ConfigurationParam.NO_CODE));
        Assert.assertTrue(model.containsKey(ConfigurationParam.SBERBANK_INN));
        Assert.assertEquals("test6", model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY, 1).get(0));
        Assert.assertEquals("test7", model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY, 2).get(0));
    }

    @Test
    public void save1Test() {
        ConfigurationParamModel model = dao.getAll();
        model.put(ConfigurationParam.REGION_UPLOAD_DIRECTORY, 1, asList("testSaveRegion"));
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, 1, null);
        dao.save(model);
        model = dao.getAll();
        Assert.assertTrue(model.containsKey(ConfigurationParam.FORM_UPLOAD_DIRECTORY));
        Assert.assertNull(model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY).get(1));
        Assert.assertTrue(model.containsKey(ConfigurationParam.REGION_UPLOAD_DIRECTORY));
        Assert.assertEquals("testSaveRegion", model.get(ConfigurationParam.REGION_UPLOAD_DIRECTORY, 1).get(0));
    }

    // Попытка сохраннить запись с ссылкой на несуществующий depatment_id
    @Test(expected = RuntimeException.class)
    public void save2Test() {
        ConfigurationParamModel model = dao.getAll();
        model.put(ConfigurationParam.REGION_UPLOAD_DIRECTORY, -99, asList("testSaveRegion"));
        dao.save(model);
    }

    @Test
    public void getByDepartmentTest() {
        ConfigurationParamModel model = dao.getByDepartment(1);
        Assert.assertNotNull(model);
    }

    // Удаление
    public void save3Test() {
        ConfigurationParamModel model = dao.getAll();
        model.remove(ConfigurationParam.KEY_FILE);
        model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY).remove(1);
        dao.save(model);
        model = dao.getAll();
        Assert.assertNull(model.get(ConfigurationParam.KEY_FILE));
        Assert.assertNull(model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY).get(1));
    }

    @Test
    public void updateTest() {
        Map<ConfigurationParam, String> updateData = new HashMap<ConfigurationParam, String>();
        updateData.put(ConfigurationParam.NO_CODE, "33333");
        updateData.put(ConfigurationParam.SBERBANK_INN, "44444");
        dao.update(updateData, 1);

        ConfigurationParamModel updateModel = dao.getAll();
        Assert.assertEquals("33333", updateModel.getFullStringValue(ConfigurationParam.NO_CODE, 1));
        Assert.assertEquals("44444", updateModel.getFullStringValue(ConfigurationParam.SBERBANK_INN, 1));
    }
}
