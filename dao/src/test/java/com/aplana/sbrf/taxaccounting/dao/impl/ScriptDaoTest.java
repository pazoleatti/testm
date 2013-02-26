package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ScriptDaoTest.xml"})
@Transactional
public class ScriptDaoTest {

	@Autowired
	ScriptDao scriptDao;

	@Autowired
	FormTemplateDao formTemplateDao;

	@Test
	public void testSaveNewFormScripts() {
		FormTemplate formTemplate = formTemplateDao.get(1);
		assertEquals(1, formTemplate.getScripts().size());

		Script script = new Script();
		script.setBody("testBody");
		script.setCondition("testCondition");
		script.setName("testName");
		script.setRowScript(true);

		formTemplate.addScript(script);
		scriptDao.saveFormScripts(formTemplate);

		formTemplate = formTemplateDao.get(1);
		assertEquals(2, formTemplate.getScripts().size());
		Script lastScript = formTemplate.getScripts().get(formTemplate.getScripts().size() - 1);
		assertEquals("testBody", lastScript.getBody());
		assertEquals("testCondition", lastScript.getCondition());
		assertEquals("testName", lastScript.getName());
		assertTrue(lastScript.isRowScript());
	}

	@Test
	public void testSaveOldFormScripts() {
		FormTemplate formTemplate = formTemplateDao.get(1);

		Script script = new Script();
		script.setId(1);
		script.setBody("testBody");
		script.setCondition("testCondition");
		script.setName("testName");
		script.setRowScript(true);

		formTemplate.addScript(script);
		scriptDao.saveFormScripts(formTemplate);

		formTemplate = formTemplateDao.get(1);
		assertEquals(1, formTemplate.getScripts().size());
		Script firsScript = formTemplate.getScripts().get(0);
		assertEquals("testBody", firsScript.getBody());
		assertEquals("testCondition", firsScript.getCondition());
		assertEquals("testName", firsScript.getName());
		assertTrue(firsScript.isRowScript());
	}

	@Test
	public void testFillFormScripts() {
		FormTemplate formTemplate = formTemplateDao.get(1);
		formTemplate.clearScripts();
		assertEquals(0, formTemplate.getEventScripts().size());
		scriptDao.fillFormScripts(formTemplate);
		assertEquals(1, formTemplate.getEventScripts().size());
	}

}
