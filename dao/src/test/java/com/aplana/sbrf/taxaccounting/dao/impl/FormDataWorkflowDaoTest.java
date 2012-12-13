package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataWorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataWorkflowDaoTest.xml"})
public class FormDataWorkflowDaoTest {
	@Autowired
	private FormTemplateDao formTemplateDao;
	
	@Autowired
	private FormDataDao formDataDao;
	
	@Autowired
	private FormDataWorkflowDao formDataWorkflowDao;
	
	private long formDataId;
	
	@Before
	public void setup() {
		FormTemplate ft = formTemplateDao.get(1);
		FormData formData = new FormData(ft);
		formData.setDepartmentId(Department.ROOT_BANK_ID);
		formData.setKind(FormDataKind.SUMMARY);
		formData.setState(WorkflowState.CREATED);
		formData.setReportPeriodId(Constants.REPORT_PERIOD_ID);
		formDataId = formDataDao.save(formData);
	}
	
	@Test
	public void testChangeState() {
		formDataWorkflowDao.changeFormDataState(formDataId, WorkflowState.APPROVED);
		
		FormData fd = formDataDao.get(formDataId);
		Assert.assertEquals("Состояние изменилось неверно!", WorkflowState.APPROVED, fd.getState());
	}
	
	@After
	public void tearDown() {
		formDataDao.delete(formDataId);
	}
}
