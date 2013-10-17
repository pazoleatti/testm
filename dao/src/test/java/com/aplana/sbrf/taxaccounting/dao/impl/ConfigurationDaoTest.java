package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 12:37
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ConfigurationDaoTest.xml"})
@Transactional
public class ConfigurationDaoTest {

	@Autowired
	private ConfigurationDao dao;

	@Test
	public void loadParamsTest() {
		Map<ConfigurationParam, String> params = dao.loadParams();
		assertEquals("test1", params.get(ConfigurationParam.FORM_DATA_KEY_FILE));
		assertEquals("test2", params.get(ConfigurationParam.REF_BOOK_KEY_FILE));
		assertEquals("test3", params.get(ConfigurationParam.FORM_DATA_DIRECTORY));
		//assertEquals("test4", params.get(ConfigurationParam.REF_BOOK_DIRECTORY));
	}

	@Test
	public void saveParamsTest() {
		Map<ConfigurationParam, String> params = new HashMap<ConfigurationParam, String>();
		params.put(ConfigurationParam.FORM_DATA_KEY_FILE, "test11");
		params.put(ConfigurationParam.REF_BOOK_KEY_FILE, "test22");
		params.put(ConfigurationParam.FORM_DATA_DIRECTORY, "test33");
		params.put(ConfigurationParam.REF_BOOK_DIRECTORY, "test44");

		dao.saveParams(params);

		Map<ConfigurationParam, String> params2 = dao.loadParams();
		assertEquals("test11", params2.get(ConfigurationParam.FORM_DATA_KEY_FILE));
		assertEquals("test22", params2.get(ConfigurationParam.REF_BOOK_KEY_FILE));
		assertEquals("test33", params2.get(ConfigurationParam.FORM_DATA_DIRECTORY));
		assertEquals("test44", params2.get(ConfigurationParam.REF_BOOK_DIRECTORY));
	}
}
