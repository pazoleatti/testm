package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 12:37
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ConfigurationDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class ConfigurationDaoTest {

    @Autowired
    private ConfigurationDao dao;

    @Test
    public void fetchByEnum() {
        Configuration configuration = dao.fetchByEnum(ConfigurationParam.KEY_FILE);
        assertNotNull(configuration);
        assertEquals("test1", configuration.getValue());
    }

    @Test
    public void fetchAll() {
        List<Configuration> configurations = dao.fetchAll();
        assertTrue(configurations.size() > 0);
    }

    @Test
    public void fetchAllAsModel() {
        ConfigurationParamModel model = dao.fetchAllAsModel();
        assertEquals(9, model.size());
        assertTrue(model.containsKey(ConfigurationParam.KEY_FILE));
        assertTrue(model.containsKey(ConfigurationParam.FORM_ERROR_DIRECTORY));
        assertTrue(model.containsKey(ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY));
        assertTrue(model.containsKey(ConfigurationParam.OKATO_UPLOAD_DIRECTORY));
        assertTrue(model.containsKey(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY));
        assertTrue(model.containsKey(ConfigurationParam.FORM_UPLOAD_DIRECTORY));
        assertTrue(model.containsKey(ConfigurationParam.FORM_ARCHIVE_DIRECTORY));
        assertTrue(model.containsKey(ConfigurationParam.NO_CODE));
        assertTrue(model.containsKey(ConfigurationParam.SBERBANK_INN));
        assertEquals("test6", model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY, 1).get(0));
    }

    @Test
    public void fetchAllByGroup() {
        List<Configuration> configurations = dao.fetchAllByGroup(ConfigurationParamGroup.COMMON);
        assertTrue(configurations.size() > 0);
    }

    @Test
    public void save1Test() {
        ConfigurationParamModel model = dao.fetchAllAsModel();
        model.put(ConfigurationParam.REGION_UPLOAD_DIRECTORY, 1, asList("testSaveRegion"));
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, 1, asList("testSaveRegion2"));
        dao.save(model);
        model = dao.fetchAllAsModel();
        assertTrue(model.containsKey(ConfigurationParam.FORM_UPLOAD_DIRECTORY));
    }

    // Попытка сохраннить запись с ссылкой на несуществующий depatment_id
    @Test(expected = RuntimeException.class)
    public void save2Test() {
        ConfigurationParamModel model = dao.fetchAllAsModel();
        model.put(ConfigurationParam.REGION_UPLOAD_DIRECTORY, -99, asList("testSaveRegion"));
        dao.save(model);
    }

    @Test
    public void fetchAllByDepartment() {
        ConfigurationParamModel model = dao.fetchAllByDepartment(0);
        Assert.assertNotNull(model);
    }

    // Удаление
    @Test
    public void save3Test() {
        ConfigurationParamModel model = dao.fetchAllAsModel();
        model.remove(ConfigurationParam.KEY_FILE);
        model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY).remove(1);
        dao.save(model);
        model = dao.fetchAllAsModel();
        Assert.assertNull(model.get(ConfigurationParam.KEY_FILE));
        Assert.assertNull(model.get(ConfigurationParam.FORM_UPLOAD_DIRECTORY).get(1));
    }

    @Test
    public void updateTest() {
        dao.update(new Configuration(ConfigurationParam.KEY_FILE.name(), null, "keyValueUpdated"));
        assertEquals("keyValueUpdated", dao.fetchByEnum(ConfigurationParam.KEY_FILE).getValue());
    }

    @Test
    public void updateTest2() {
        dao.update(new Configuration(ConfigurationParam.KEY_FILE.name(), 1, "keyValueUpdated"));
        assertEquals("keyValueUpdated", dao.fetchByEnum(ConfigurationParam.KEY_FILE).getValue());
    }

    @Test
    public void updateTest3() {
        Map<ConfigurationParam, String> updateData = new HashMap<ConfigurationParam, String>();
        updateData.put(ConfigurationParam.NO_CODE, "33333");
        updateData.put(ConfigurationParam.SBERBANK_INN, "44444");
        dao.update(updateData, 1);

        ConfigurationParamModel updateModel = dao.fetchAllAsModel();
        assertEquals("33333", updateModel.getFullStringValue(ConfigurationParam.NO_CODE, 1));
        assertEquals("44444", updateModel.getFullStringValue(ConfigurationParam.SBERBANK_INN, 1));
    }

    @Test
    public void fetchAllCommonParamTest() {
        PagingParams params = PagingParams.getInstance(1, 10);
        assertTrue(dao.fetchAllByGroupAndPaging(ConfigurationParamGroup.COMMON, params).size() > 0);
    }
}
