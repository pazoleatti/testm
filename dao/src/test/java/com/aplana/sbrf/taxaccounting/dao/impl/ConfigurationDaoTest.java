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

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;


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
    public void fetchAllByEnums() {
        List<Configuration> configurations = dao.fetchAllByEnums(asList(ConfigurationParam.KEY_FILE, ConfigurationParam.SBERBANK_INN));
        assertNotNull(configurations);
        assertEquals(2, configurations.size());
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
    public void fetchAllByDepartment() {
        ConfigurationParamModel model = dao.fetchAllByDepartment(0);
        Assert.assertNotNull(model);
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
    public void fetchAllCommonParamTest() {
        PagingParams params = PagingParams.getInstance(1, 10);
        assertTrue(dao.fetchAllByGroupAndPaging(ConfigurationParamGroup.COMMON, params).size() > 0);
    }
}
