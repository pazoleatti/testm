package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataDaoTest.xml"})
@DirtiesContext
public class FormDataDaoTest {
	@Autowired
	FormTemplateDao formTemplateDao;
	
	@Autowired
	FormDataDao formDataDao;
	
	@Test
	public void testDao() {
		// TODO: разбить метод на несколько тестов
		FormTemplate form = formTemplateDao.get(1);
		
		FormData fd = new FormData(form);
		
		try {
			formDataDao.save(fd);
			Assert.fail("Exception expected");
		} catch (DaoException e) {
			// Падаем так как не задан ряд обязательных параметров
		}
		
		fd.setDepartmentId(Department.ROOT_BANK_ID);
		fd.setKind(FormDataKind.SUMMARY);
		fd.setState(WorkflowState.CREATED);
		fd.setReportPeriodId(Constants.REPORT_PERIOD_ID);
		long savedFormDataId = formDataDao.save(fd);
		
		fd = formDataDao.get(savedFormDataId);
		Assert.assertEquals("Изменился тип налоговой формы", FormDataKind.SUMMARY, fd.getKind());
		Assert.assertEquals("Изменилось подразделение", new Integer(Department.ROOT_BANK_ID), fd.getDepartmentId());
		Assert.assertEquals("Изменилась стадия ЖЦ", WorkflowState.CREATED, fd.getState());
		Assert.assertEquals("Изменился идентификатор отчётного периода", new Integer(Constants.REPORT_PERIOD_ID), fd.getReportPeriodId());

		int rowsCount = fd.getDataRows().size();
		fd.appendDataRow();
		
		// Проверяем сохранение существующей записи		
		long id = formDataDao.save(fd);
		Assert.assertEquals("Изменился id", savedFormDataId, id);
		
		fd = formDataDao.get(id);
		Assert.assertEquals("Не сохранилась строка", rowsCount + 1, fd.getDataRows().size());
		Assert.assertEquals("Изменился тип налоговой формы", FormDataKind.SUMMARY, fd.getKind());
		Assert.assertEquals("Изменилось подразделение", new Integer(Department.ROOT_BANK_ID), fd.getDepartmentId());
		Assert.assertEquals("Изменилась стадия ЖЦ", WorkflowState.CREATED, fd.getState());
		
		// Проверяем удаление записи
		formDataDao.delete(savedFormDataId);
		
		// Убеждаемся, что данные удалены
		try {
			fd = formDataDao.get(savedFormDataId);
			Assert.fail("Данные не удалены!");
		} catch (DaoException e) {
			// Это нормально - записи нет в БД
		}
	}
}
