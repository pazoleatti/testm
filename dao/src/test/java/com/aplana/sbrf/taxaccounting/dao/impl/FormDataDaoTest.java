package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// TODO: (sgoryachkin) Пришлось вычистить тесты, т.к. они тестировали в основном только работу со строками, что теперь не актуально
// Нужно написать нормальные тесты на получение сохранение FormData и проверить поля

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "FormDataDaoTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FormDataDaoTest {
	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	FormDataDao formDataDao;
    @Autowired
    FormPerformerDao formPerformerDao;

	@Test
	public void testGet() {
		@SuppressWarnings("unused")
		FormData formData = formDataDao.get(1, false);

	}

	@Test
	public void testGetWithoutRows(){
		FormData formData = formDataDao.get(1, false);
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


	@Test
	public void testSave() {
		FormTemplate formTemplate = formTemplateDao.get(1);

		FormData formData = new FormData(formTemplate);

		formData.setState(WorkflowState.CREATED);
		formData.setKind(FormDataKind.SUMMARY);
		formData.setDepartmentId(1);
		formData.setReportPeriodId(1);

		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId, false);

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

		fd = formDataDao.get(savedFormDataId, false);
		Assert.assertEquals("Изменился тип налоговой формы",
				FormDataKind.SUMMARY, fd.getKind());
		Assert.assertEquals("Изменилось подразделение", new Integer(
				Department.ROOT_BANK_ID), fd.getDepartmentId());
		Assert.assertEquals("Изменилась стадия ЖЦ", WorkflowState.CREATED,
				fd.getState());
		Assert.assertEquals("Изменился идентификатор отчётного периода",
				new Integer(Constants.REPORT_PERIOD_ID), fd.getReportPeriodId());



		// Проверяем сохранение существующей записи
		long id = formDataDao.save(fd);
		Assert.assertEquals("Изменился id", savedFormDataId, id);

		fd = formDataDao.get(id, false);

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
			fd = formDataDao.get(savedFormDataId, false);
			Assert.fail("Данные не удалены!");
		} catch (DaoException e) {
			// Это нормально - записи нет в БД
		}
	}

	private FormData fillFormData() {
		FormTemplate formTemplate = formTemplateDao.get(1);
		FormData formData = new FormData(formTemplate);


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

	
		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId, false);

	}
	
	@Test
	@Transactional
	public void editsSaveGetSuccess() {
		FormData formData = fillFormData();
		long formDataId = formDataDao.save(formData);
		formData = formDataDao.get(formDataId, false);

	}
	
	@Test
	public void testFind() {
		FormData fd = formDataDao.find(1, FormDataKind.SUMMARY, 1, 11);
		Assert.assertEquals(11l, fd.getId().longValue());
	}

    @Test
    public void getFormDataIdsTest(){
        List<Long> list = new ArrayList<Long>() {{ add(1l); add(11l); add(12l); add(13l); }};
        List<Long> list1 = new ArrayList<Long>() {{ add(14l); add(15l); add(16l); add(17l); add(18l); add(19l); add(20l); }};
        Assert.assertEquals(list, formDataDao.getFormDataIds(1, FormDataKind.SUMMARY, 1));
        Assert.assertEquals(list1, formDataDao.getFormDataIds(2, FormDataKind.PRIMARY, 1));
    }

    @Test
    public void testGetFormDataIds(){
        List<Integer> list = new ArrayList<Integer>() {{ add(1); add(11); add(12); add(13); }};
        Assert.assertEquals(11, formDataDao.getFormDataIds(Arrays.asList(TaxType.values()), list).size());
    }

    @Test
    public void findMonth1Test() {
        FormData fdJanuary = formDataDao.findMonth(2, FormDataKind.PRIMARY, 1, 12, 1);
        Assert.assertEquals(fdJanuary.getId().longValue(), 14L);

        FormData fdMarth = formDataDao.findMonth(2, FormDataKind.PRIMARY, 1, 12, 3);
        Assert.assertEquals(fdMarth.getId().longValue(), 16L);

        FormData fdJune = formDataDao.findMonth(2, FormDataKind.PRIMARY, 1, 12, 6);
        Assert.assertEquals(fdJune.getId().longValue(), 20L);

        Assert.assertNull(formDataDao.findMonth(2, FormDataKind.PRIMARY, 1, 12, 60));
    }

    @Test(expected = DaoException.class)
    public void findMonth2Test() {
        formDataDao.findMonth(2, FormDataKind.PRIMARY, 1, 12, 5);
    }

    @Test(expected = DaoException.class)
    public void testFindTooManyResult() {
        formDataDao.find(1, FormDataKind.SUMMARY, 1, 12);
    }
	
	@Test
	public void testFindEmptyResult() {
		FormData fd = formDataDao.find(1, FormDataKind.SUMMARY, 1, 13);
		Assert.assertNull(fd);
	}
	
	
	@Test
	public void updateStateSuccess() {
		formDataDao.updateState(1, WorkflowState.APPROVED);
		Assert.assertEquals(WorkflowState.APPROVED, formDataDao.get(1, false).getState());
	}
	
	@Test(expected=DaoException.class)
	public void updateStateError() {
		formDataDao.updateState(1000, WorkflowState.APPROVED);
	}
	
	@Test
	public void updateReturnSignSuccess() {
		formDataDao.updateReturnSign(1, true);
		Assert.assertTrue(formDataDao.get(1, false).isReturnSign());
		formDataDao.updateReturnSign(1, false);
		Assert.assertFalse(formDataDao.get(1, false).isReturnSign());
	}
	
	@Test(expected=DaoException.class)
	public void updateReturnSignError() {
		formDataDao.updateReturnSign(1000, true);
	}

    @Test
    public void testFindFormDataByFormTemplate(){
        Assert.assertEquals(9, formDataDao.findFormDataByFormTemplate(1).size());
        Assert.assertTrue(formDataDao.findFormDataByFormTemplate(10000).isEmpty());
    }

    @Test
    public void testGetFormDataListInActualPeriodByTemplate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        Assert.assertEquals(9, formDataDao.getFormDataListInActualPeriodByTemplate(1, format.parse("2013.01.01")).size());
    }

    @Test
    public void testFindByDepAndReportPeriod() throws ParseException {
        Assert.assertEquals(1, formDataDao.find(Arrays.asList(1), 1).size());
    }

    @Test
    public void testUpdateFDPerformerTBDepartmentNames() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date startDate = calendar.getTime();
        calendar.set(2013, Calendar.AUGUST, 1);
        Date endDate = calendar.getTime();
        formDataDao.updateFDPerformerTBDepartmentNames("Bank1", "Ban", startDate, endDate);
        Assert.assertEquals("Bank1/Uralsib", formPerformerDao.get(11).getReportDepartmentName());
    }

    @Test
    public void testUpdateFDPerformerDepartmentNames() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date startDate = calendar.getTime();
        calendar.set(2013, Calendar.AUGUST, 1);
        Date endDate = calendar.getTime();
        formDataDao.updateFDPerformerDepartmentNames(1, "Bank1", startDate, endDate);
        Assert.assertEquals("Ban/Bank1", formPerformerDao.get(11).getReportDepartmentName());
    }

    @Test
    public void testGetFormDataListForCrossNumeration() {
        List<FormData> formDataList = formDataDao.getFormDataListForCrossNumeration(2014, 3, "I", FormDataKind.PRIMARY.getId());
        Assert.assertEquals(3, formDataList.size());
        Assert.assertEquals(1, formDataList.get(0).getPeriodOrder().intValue());
        Assert.assertEquals(2, formDataList.get(1).getPeriodOrder().intValue());
        Assert.assertEquals(3, formDataList.get(2).getPeriodOrder().intValue());
    }

}
