package com.aplana.sbrf.taxaccounting.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.WorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.Workflow;

//TODO: переработать, чтобы не было необходимости поднимать полный файл dao.xml, а то получается integration-тест вместо unit-теста 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class WorkflowDaoTest {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private WorkflowDao workflowDao;
	
	@Test
	public void testCorrectId() {
		Workflow w = workflowDao.getWorkflow(1);
		assert w != null;
		assert w.getMoves() != null;
	}
	
	@Test
	public void testIncorrectId() {
		try {
			workflowDao.getWorkflow(-5000);
			assert false;
		} catch (DaoException e) {
			logger.info("Exception caught, it's OK");
		}
	}	
	
}
