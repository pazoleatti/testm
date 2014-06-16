package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
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

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 12:37
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ConfigurationDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ConfigurationDaoTest {

	@Autowired
	private ConfigurationDao dao;

	@Test
	public void loadParamsTest() {
        ConfigurationParamModel model = dao.loadParams();
        List<String> result1 = model.get(ConfigurationParam.FORM_DATA_KEY_FILE);
        List<String> result2 = model.get(ConfigurationParam.REF_BOOK_KEY_FILE);
        List<String> result3 = model.get(ConfigurationParam.FORM_DATA_DIRECTORY);
        List<String> result4 = model.get(ConfigurationParam.REF_BOOK_DIRECTORY);

        Assert.assertEquals(1, result1.size());
        Assert.assertEquals(1, result2.size());
        Assert.assertEquals(2, result3.size());
        Assert.assertNull(result4);

        Assert.assertEquals("test1", result1.get(0));
        Assert.assertEquals("test2", result2.get(0));
        Assert.assertEquals("test3", result3.get(0));
        Assert.assertEquals("test5", result3.get(1));
    }

	@Test
	public void saveParamsTest() {
        ConfigurationParamModel model1 = new ConfigurationParamModel();
        model1.put(ConfigurationParam.FORM_DATA_KEY_FILE, asList("test11"));
        model1.put(ConfigurationParam.REF_BOOK_KEY_FILE, asList("test22"));
        model1.put(ConfigurationParam.FORM_DATA_DIRECTORY, asList("test33"));
        model1.put(ConfigurationParam.REF_BOOK_DIRECTORY, asList("test44","test55"));

		dao.saveParams(model1);

        ConfigurationParamModel model2 = dao.loadParams();
		assertEquals("test11", model2.get(ConfigurationParam.FORM_DATA_KEY_FILE).get(0));
		assertEquals("test22", model2.get(ConfigurationParam.REF_BOOK_KEY_FILE).get(0));
		assertEquals("test33", model2.get(ConfigurationParam.FORM_DATA_DIRECTORY).get(0));
		assertEquals("test44", model2.get(ConfigurationParam.REF_BOOK_DIRECTORY).get(0));
        assertEquals("test55", model2.get(ConfigurationParam.REF_BOOK_DIRECTORY).get(1));
	}
}
