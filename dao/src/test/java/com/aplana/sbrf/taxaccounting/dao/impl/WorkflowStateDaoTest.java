package com.aplana.sbrf.taxaccounting.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.WorkflowStateDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

//TODO: переработать, чтобы не было необходимости поднимать полный файл dao.xml, а то получается integration-тест вместо unit-теста 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class WorkflowStateDaoTest {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private WorkflowStateDao workflowStateDao;
	
	@Test
	public void testCorrectId() {
		WorkflowState state = workflowStateDao.getState(1001);
		assert state.getId() == 1001;
		assert state.getName() != null;
	}
	
	@Test
	public void testIncorrectId() {
		try {
			workflowStateDao.getState(-5000);
			assert false;
		} catch (DaoException e) {
			logger.info("Exception caught, it's OK");
		}
	}
}
