package com.aplana.sbrf.taxaccounting.dao.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "FormDataDaoTest.xml" })
@Transactional
public class FormDataDaoTest {
	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	FormDataDao formDataDao;

	@Test
	public void testGet() {
		FormData formData = formDataDao.get(1);

		Assert.assertEquals(2, formData.getDataRows().size());

		DataRow dr = formData.getDataRows().get(0);
		Assert.assertEquals("testAlias", dr.getAlias());
		Assert.assertEquals("Строка 1", (String) dr.get("stringColumn"));

		BigDecimal numericValue1 = getNumericValue(1.01, 2);
		Assert.assertEquals(numericValue1, (BigDecimal) dr.get("numericColumn"));
		Assert.assertEquals(getDate(2012, 11, 31), (Date) dr.get("dateColumn"));

		dr = formData.getDataRows().get(1);
		Assert.assertNull(dr.getAlias());
		Assert.assertEquals("Строка 2", (String) dr.get("stringColumn"));
		BigDecimal numericValue2 = getNumericValue(2.02, 2);
		Assert.assertEquals(numericValue2, (BigDecimal) dr.get("numericColumn"));
		Assert.assertEquals(getDate(2013, 0, 1), (Date) dr.get("dateColumn"));
	}

	@Test
	public void testGetWithoutRows(){
		FormData formData = formDataDao.get(1);
		FormData formDataWithoutRows = formDataDao.getWithoutRows(1);

		Assert.assertEquals(Long.valueOf(1), formDataWithoutRows.getId());
		Assert.assertEquals(Integer.valueOf(1), formDataWithoutRows.getReportPeriodId());
		Assert.assertEquals(Integer.valueOf(1), formDataWithoutRows.getDepartmentId());
		Assert.assertEquals(FormDataKind.SUMMARY, formDataWithoutRows.getKind());
		Assert.assertEquals(WorkflowState.CREATED, formDataWithoutRows.getState());

		Assert.assertEquals(formData.getId(), formDataWithoutRows.getId());
		Assert.assertEquals(formData.getReportPeriodId(), formDataWithoutRows.getReportPeriodId());
		Assert.assertEquals(formData.getDepartmentId(), formDataWithoutRows.getDepartmentId());
		Assert.assertEquals(formData.getKind(), formDataWithoutRows.getKind());
		Assert.assertEquals(formData.getState(), formDataWithoutRows.getState());
	}

	private BigDecimal getNumericValue(double value, int scale) {
		BigDecimal val = new BigDecimal(value);
		val = val.setScale(scale, BigDecimal.ROUND_HALF_UP);
		return val;

	}

	private Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Test
	public void testSave() {
		FormTemplate formTemplate = formTemplateDao.get(1);

		FormData formData = new FormData(formTemplate);

		DataRow dr = formData.appendDataRow();
		dr.put("stringColumn", "Строка 1");
		dr.put("numericColumn", 1.01);
		Date date1 = getDate(2012, 11, 31);
		dr.put("dateColumn", date1);

		dr = formData.appendDataRow("newAlias");
		dr.put("stringColumn", "Строка 2");
		dr.put("numericColumn", 2.02);
		Date date2 = getDate(2013, 0, 1);
		dr.put("dateColumn", date2);

		formData.setState(WorkflowState.CREATED);
		formData.setKind(FormDataKind.SUMMARY);
		formData.setDepartmentId(1);
		formData.setReportPeriodId(1);

		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId);

		Assert.assertEquals(2, formData.getDataRows().size());

		dr = formData.getDataRows().get(0);
		Assert.assertNull(dr.getAlias());
		Assert.assertEquals("Строка 1", (String) dr.get("stringColumn"));
		Assert.assertEquals(getNumericValue(1.01, 2),
				(BigDecimal) dr.get("numericColumn"));
		Assert.assertEquals(date1, (Date) dr.get("dateColumn"));

		dr = formData.getDataRows().get(1);
		Assert.assertEquals("newAlias", dr.getAlias());
		Assert.assertEquals("Строка 2", (String) dr.get("stringColumn"));
		Assert.assertEquals(getNumericValue(2.02, 2),
				(BigDecimal) dr.get("numericColumn"));
		Assert.assertEquals(date2, (Date) dr.get("dateColumn"));
	}

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
		Assert.assertEquals("Изменился тип налоговой формы",
				FormDataKind.SUMMARY, fd.getKind());
		Assert.assertEquals("Изменилось подразделение", new Integer(
				Department.ROOT_BANK_ID), fd.getDepartmentId());
		Assert.assertEquals("Изменилась стадия ЖЦ", WorkflowState.CREATED,
				fd.getState());
		Assert.assertEquals("Изменился идентификатор отчётного периода",
				new Integer(Constants.REPORT_PERIOD_ID), fd.getReportPeriodId());

		int rowsCount = fd.getDataRows().size();
		fd.appendDataRow();

		// Проверяем сохранение существующей записи
		long id = formDataDao.save(fd);
		Assert.assertEquals("Изменился id", savedFormDataId, id);

		fd = formDataDao.get(id);
		Assert.assertEquals("Не сохранилась строка", rowsCount + 1, fd
				.getDataRows().size());
		Assert.assertEquals("Изменился тип налоговой формы",
				FormDataKind.SUMMARY, fd.getKind());
		Assert.assertEquals("Изменилось подразделение", new Integer(
				Department.ROOT_BANK_ID), fd.getDepartmentId());
		Assert.assertEquals("Изменилась стадия ЖЦ", WorkflowState.CREATED,
				fd.getState());

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

	private FormData fillFormData() {
		FormTemplate formTemplate = formTemplateDao.get(1);
		FormData formData = new FormData(formTemplate);

		DataRow<Cell> dr = formData.appendDataRow();
		dr.put("stringColumn", "Строка 1");
		dr.getCell("stringColumn").setStyleAlias("alias1");
		dr.getCell("stringColumn").setEditable(true);
		dr.put("numericColumn", 1.01);
		dr.getCell("numericColumn").setStyleAlias("alias1");
		dr.getCell("numericColumn").setEditable(false);
		Date date1 = getDate(2012, 11, 31);
		dr.put("dateColumn", date1);

		dr = formData.appendDataRow("newAlias");
		dr.put("stringColumn", "Строка 2");
		dr.getCell("stringColumn").setStyleAlias("alias1");
		dr.getCell("stringColumn").setEditable(false);
		dr.put("numericColumn", 2.02);
		dr.getCell("numericColumn").setStyleAlias("alias1");
		dr.getCell("numericColumn").setEditable(true);
		Date date2 = getDate(2013, 0, 1);
		dr.put("dateColumn", date2);

		formData.setState(WorkflowState.CREATED);
		formData.setKind(FormDataKind.SUMMARY);
		formData.setDepartmentId(1);
		formData.setReportPeriodId(1);

		return formData;
	}

	@Test
	@Transactional
	public void spansSaveGetSuccess() {

		FormData formData = fillFormData();

		DataRow dr = formData.getDataRow("newAlias");
		dr.getCell("numericColumn").setColSpan(2);
		dr.getCell("numericColumn").setRowSpan(2);

		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId);

		dr = formData.getDataRows().get(0);
		Assert.assertEquals(1, dr.getCell("numericColumn").getColSpan());
		Assert.assertEquals(1, dr.getCell("numericColumn").getRowSpan());

		dr = formData.getDataRows().get(1);
		Assert.assertEquals(2, dr.getCell("numericColumn").getColSpan());
		Assert.assertEquals(2, dr.getCell("numericColumn").getRowSpan());
		Assert.assertEquals(1, dr.getCell("stringColumn").getColSpan());
		Assert.assertEquals(1, dr.getCell("stringColumn").getRowSpan());

	}
	
	@Test
	@Transactional
	public void stylesRemoveStyleSuccess() {
		FormData formData = fillFormData();
		DataRow<Cell> dr = formData.getDataRows().get(0);
		dr.getCell("stringColumn").setStyleAlias("alias2");
		
		Assert.assertEquals("alias2", dr.getCell("stringColumn").getStyle().getAlias());
		Assert.assertEquals("alias2", dr.getCell("stringColumn").getStyleAlias());
		
		formData.getFormStyles().clear();
		
		Assert.assertNull(dr.getCell("stringColumn").getStyle());
		Assert.assertNull(dr.getCell("stringColumn").getStyleAlias());
	}

	@Test
	public void stylesSaveGetSuccess() {

		FormData formData = fillFormData();

		DataRow<Cell> dr = formData.getDataRows().get(0);
		dr.getCell("stringColumn").setStyleAlias(null);

		dr = formData.getDataRow("newAlias");
		dr.getCell("numericColumn").setStyleAlias("alias2");

		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId);

		dr = formData.getDataRows().get(0);
		Assert.assertEquals("alias1", dr.getCell("numericColumn").getStyle()
				.getAlias());
		Assert.assertEquals(null, dr.getCell("dateColumn").getStyle());

		dr = formData.getDataRows().get(1);
		Assert.assertEquals("alias2", dr.getCell("numericColumn").getStyle()
				.getAlias());
		Assert.assertEquals("alias1", dr.getCell("stringColumn").getStyle()
				.getAlias());

	}

	@Test
	@Transactional
	public void editsSaveGetSuccess() {
		FormData formData = fillFormData();
		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId);

		DataRow<Cell> dr = formData.getDataRows().get(0);
		Assert.assertEquals(true, dr.getCell("stringColumn").isEditable());
		Assert.assertEquals(false, dr.getCell("numericColumn").isEditable());

		dr = formData.getDataRows().get(1);
		Assert.assertEquals(false, dr.getCell("stringColumn").isEditable());
		Assert.assertEquals(true, dr.getCell("numericColumn").isEditable());
		Assert.assertEquals(false, dr.getCell("dateColumn").isEditable());
	}

	@Test(expected = IllegalArgumentException.class)
	public void stylesSaveErrorStyleNotExist() {
		FormData formData = fillFormData();
		DataRow<Cell> dr = formData.getDataRow("newAlias");
		dr.getCell("numericColumn").setStyleAlias("not existing style");
		formDataDao.save(formData);
	}

	@Test(expected = IllegalArgumentException.class)
	public void spansColError() {
		FormData formData = fillFormData();
		DataRow dr = formData.getDataRow("newAlias");
		dr.getCell("numericColumn").setColSpan(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void spansRowError() {
		FormData formData = fillFormData();
		DataRow dr = formData.getDataRow("newAlias");
		dr.getCell("numericColumn").setRowSpan(-1);
	}
	
	@Test
	public void testFind() {
		FormData fd = formDataDao.find(1, FormDataKind.SUMMARY, 1, 11);
		Assert.assertEquals(11l, fd.getId().longValue());
	}

	@Test(expected=DaoException.class)
	public void testFindTooManyResult() {
		formDataDao.find(1, FormDataKind.SUMMARY, 1, 12);
	}
	
	@Test
	public void testFindEmptyResult() {
		FormData fd = formDataDao.find(1, FormDataKind.SUMMARY, 1, 13);
		Assert.assertNull(fd);
	}
}
