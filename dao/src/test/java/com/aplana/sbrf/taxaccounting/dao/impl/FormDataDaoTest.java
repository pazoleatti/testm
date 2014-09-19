package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FormDataDaoTest {
    @Autowired
    FormTemplateDao formTemplateDao;

    @Autowired
    FormDataDao formDataDao;

    @Autowired
    FormPerformerDao formPerformerDao;

    @Autowired
    TaxPeriodDao taxPeriodDao;

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    ReportPeriodDao reportPeriodDao;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    public void getTest() {
        FormData formData = formDataDao.get(1, null);
        Assert.assertNotNull(formData);
        Assert.assertEquals(1, formData.getId().intValue());
        Assert.assertEquals(WorkflowState.CREATED, formData.getState());
        Assert.assertEquals(1, formData.getDepartmentId().intValue());
        Assert.assertEquals(1, formData.getReportPeriodId().intValue());
        Assert.assertEquals(101, formData.getDepartmentReportPeriodId().intValue());
    }

    @Test
    public void testGetWithoutRows() {
        FormData formData = formDataDao.get(1, false);
        FormData formDataWithoutRows = formDataDao.getWithoutRows(1);
        Assert.assertEquals(1, formDataWithoutRows.getId().intValue());
        Assert.assertEquals(1, formDataWithoutRows.getReportPeriodId().intValue());
        Assert.assertEquals(1, formDataWithoutRows.getDepartmentId().intValue());
        Assert.assertEquals(101, formData.getDepartmentReportPeriodId().intValue());
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
        formData.setDepartmentReportPeriodId(101);
        long formDataId = formDataDao.save(formData);
        formData = formDataDao.get(formDataId, false);
        Assert.assertNotNull(formData);
        Assert.assertEquals(WorkflowState.CREATED, formData.getState());
        Assert.assertEquals(FormDataKind.SUMMARY, formData.getKind());
        Assert.assertEquals(101, formData.getDepartmentReportPeriodId().intValue());
    }

    @Test(expected = DaoException.class)
    public void failSaveTest() {
        FormTemplate formTemplate = formTemplateDao.get(1);
        FormData fd = new FormData(formTemplate);
        formDataDao.save(fd);
    }

    @Test
    public void testDao() {
        FormTemplate form = formTemplateDao.get(1);
        FormData formData = new FormData(form);
        formData.setKind(FormDataKind.SUMMARY);
        formData.setState(WorkflowState.CREATED);
        formData.setDepartmentReportPeriodId(102);
        long savedFormDataId = formDataDao.save(formData);

        formData = formDataDao.get(savedFormDataId, false);
        Assert.assertEquals(FormDataKind.SUMMARY, formData.getKind());
        Assert.assertEquals(WorkflowState.CREATED, formData.getState());
        Assert.assertEquals(102, formData.getDepartmentReportPeriodId().intValue());

        // Проверяем сохранение существующей записи
        long id = formDataDao.save(formData);
        Assert.assertEquals(savedFormDataId, id);

        formData = formDataDao.get(id, false);
        Assert.assertEquals(FormDataKind.SUMMARY, formData.getKind());
        Assert.assertEquals(1, formData.getDepartmentId().intValue());
        Assert.assertEquals(WorkflowState.CREATED, formData.getState());
    }

    @Test
    public void deleteTest() {
        formDataDao.delete(1);
    }

    @Test(expected = DaoException.class)
    public void deleteFailTest() {
        formDataDao.delete(1);
        formDataDao.get(1, false);
    }

    @Test
    public void find1Test() {
        FormData formData = formDataDao.find(1, FormDataKind.SUMMARY, 1, 11);
        Assert.assertNotNull(formData);
        Assert.assertEquals(11l, formData.getId().longValue());
    }

    @Test
    public void find2Test() {
        FormData formData = formDataDao.find(1, FormDataKind.PRIMARY, 301, Integer.valueOf(1));
        Assert.assertNotNull(formData);
        Assert.assertEquals(301, formData.getId().intValue());
    }

    @Test
    public void find3Test() {
        FormData formData = formDataDao.find(1, FormDataKind.SUMMARY, 111, null);
        Assert.assertNotNull(formData);
        Assert.assertNotNull(formData.getFormType());
        Assert.assertEquals(11, formData.getId().intValue());
    }

    @Test
    public void find4Test() {
        List<FormData> formDataList = formDataDao.find(Arrays.asList(1, 2), 20);
        Assert.assertEquals(5, formDataList.size());
        Assert.assertEquals(400, formDataList.get(0).getId().intValue());
        Assert.assertEquals(301, formDataList.get(1).getId().intValue());
        Assert.assertEquals(302, formDataList.get(2).getId().intValue());
        Assert.assertEquals(303, formDataList.get(3).getId().intValue());
        Assert.assertEquals(402, formDataList.get(4).getId().intValue());
    }

    @Test
    public void getFormDataIdsTest() {
        List<Long> list1 = Arrays.asList(1L, 11L, 12L, 13L, 1000L);
        List<Long> list2 = Arrays.asList(14L, 15L, 16L, 17L, 18L, 19L, 20L, 402L);
        Assert.assertEquals(list1, formDataDao.getFormDataIds(1, FormDataKind.SUMMARY, 1));
        Assert.assertEquals(list2, formDataDao.getFormDataIds(2, FormDataKind.PRIMARY, 1));
    }

    @Test
    public void testGetFormDataIds() {
        List<Integer> list = Arrays.asList(1, 11, 12, 13, 1000);
        List<Long> formDataIdList = formDataDao.getFormDataIds(Arrays.asList(TaxType.values()), list);
        Assert.assertArrayEquals(new Long[]{1L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 301L, 302L, 303L,
                304L, 305L, 306L, 307L, 308L, 402L, 1000L}, formDataIdList.toArray());
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

    @Test
    public void findMonth3Test() throws ParseException {
        FormData formData = formDataDao.find(2, FormDataKind.PRIMARY, 115, Integer.valueOf(4));
        Assert.assertNotNull(formData);
        Assert.assertEquals(17, formData.getId().intValue());
    }

    @Test
    public void findMonth4Test() throws ParseException {
        // Проверка с наличием корректирующего периода
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setId(1000);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setReportPeriod(reportPeriodDao.get(15));
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        departmentReportPeriod.setCorrectionDate(format.parse("10.05.2013"));
        departmentReportPeriodDao.save(departmentReportPeriod);

        FormData formData = new FormData(formTemplateDao.get(2));
        formData.setDepartmentReportPeriodId(1000);
        formData.setPeriodOrder(4);
        formData.setState(WorkflowState.CREATED);
        formData.setKind(FormDataKind.PRIMARY);
        long id = formDataDao.save(formData);

        formData = formDataDao.find(2, FormDataKind.PRIMARY, 1000, Integer.valueOf(4));
        Assert.assertNotNull(formData);
        Assert.assertEquals(id, formData.getId().intValue());
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

    @Test(expected = DaoException.class)
    public void updateStateError() {
        formDataDao.updateState(10000, WorkflowState.APPROVED);
    }

    @Test
    public void updateReturnSignSuccess() {
        formDataDao.updateReturnSign(1, true);
        assertTrue(formDataDao.get(1, false).isReturnSign());
        formDataDao.updateReturnSign(1, false);
        Assert.assertFalse(formDataDao.get(1, false).isReturnSign());
    }

    @Test(expected = DaoException.class)
    public void updateReturnSignError() {
        formDataDao.updateReturnSign(10000, true);
    }

    @Test
    public void testFindFormDataByFormTemplate() {
        List<Long> formIdList = formDataDao.findFormDataByFormTemplate(1);
        Assert.assertArrayEquals(new Long[]{1L, 11L, 12L, 13L, 301L, 302L, 303L, 304L, 305L, 306L, 307L, 308L, 1000L}, formIdList.toArray());
        assertTrue(formDataDao.findFormDataByFormTemplate(10000).isEmpty());
    }

    @Test
    public void testGetFormDataListInActualPeriodByTemplate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        List<Long> formIdList = formDataDao.getFormDataListInActualPeriodByTemplate(1, format.parse("2013.01.01"));
        Assert.assertArrayEquals(new Long[]{1L, 11L, 12L, 13L, 301L, 302L, 303L, 304L, 305L, 306L, 307L, 308L, 1000L}, formIdList.toArray());
    }

    @Test
    public void findByDepAndReportPeriodTest() {
        List<FormData> formDataList = formDataDao.find(Arrays.asList(1), 20);
        Assert.assertEquals(4, formDataList.size());
        Assert.assertEquals(301, formDataList.get(0).getId().intValue());
        Assert.assertEquals(302, formDataList.get(1).getId().intValue());
        Assert.assertEquals(303, formDataList.get(2).getId().intValue());
        Assert.assertEquals(402, formDataList.get(3).getId().intValue());
    }

    @Test
    public void testUpdateFDPerformerDepartmentNames() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        Date startDate = calendar.getTime();
        calendar.set(2013, Calendar.AUGUST, 1);
        Date endDate = calendar.getTime();
        formDataDao.updateFDPerformerDepartmentNames(1, "Bank1", startDate, endDate);
        Assert.assertEquals("Ban/Bank1", formPerformerDao.get(11).getReportDepartmentName());
    }

    @Test
    public void testGetFormDataListByTemplateId() {
        List<FormData> formDataList = formDataDao.getFormDataListByTemplateId(4);
        Assert.assertEquals(4, formDataList.size());
        Assert.assertEquals(400, formDataList.get(0).getId().intValue());
        Assert.assertEquals(401, formDataList.get(1).getId().intValue());
        Assert.assertEquals(402, formDataList.get(2).getId().intValue());
        Assert.assertEquals(403, formDataList.get(3).getId().intValue());
    }

    @Test
    public void testGetPrevFormDataList() {
        TaxPeriod taxPeriod = taxPeriodDao.get(100);
        FormData formData = formDataDao.get(304, false);
        Assert.assertNotNull(formData);
        List<FormData> formDataList = formDataDao.getPrevFormDataList(formData, taxPeriod);
        Assert.assertEquals(3, formDataList.size());
        Assert.assertEquals(301, formDataList.get(0).getId().intValue());
        Assert.assertEquals(302, formDataList.get(1).getId().intValue());
        Assert.assertEquals(303, formDataList.get(2).getId().intValue());
    }

    @Test
    public void testGetNextFormDataList() {
        TaxPeriod taxPeriod = taxPeriodDao.get(100);
        FormData formData = formDataDao.get(304, false);
        List<FormData> formDataList = formDataDao.getNextFormDataList(formData, taxPeriod);
        Assert.assertEquals(3, formDataList.size());
        Assert.assertEquals(305, formDataList.get(0).getId().intValue());
        Assert.assertEquals(306, formDataList.get(1).getId().intValue());
        Assert.assertEquals(307, formDataList.get(2).getId().intValue());
    }

    @Test
    public void getLast1Test() {
        // Ежемесячная НФ
        FormData formData = formDataDao.getLast(1, FormDataKind.PRIMARY, 1, 20, 3);
        Assert.assertNotNull(formData);
        Assert.assertEquals(303, formData.getId().intValue());
        // Не ежемесячная форма
        formData = formDataDao.getLast(1, FormDataKind.SUMMARY, 1, 12, null);
        Assert.assertNotNull(formData);
        Assert.assertEquals(12, formData.getId().intValue());
    }

    @Test
    public void getLast2Test() throws ParseException {
        // Более сложный случай — периодов несколько
        final int typeId = 1;
        final int departmentId = 1;
        Date data1 = SIMPLE_DATE_FORMAT.parse("01.01.2015");
        Date data2 = SIMPLE_DATE_FORMAT.parse("31.12.2015");

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2015);
        taxPeriod.setTaxType(TaxType.INCOME);
        final int taxPeriodId = taxPeriodDao.add(taxPeriod);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCalendarStartDate(data1);
        reportPeriod.setStartDate(data1);
        reportPeriod.setEndDate(data2);
        reportPeriod.setName("Name");
        reportPeriod.setTaxPeriod(taxPeriodDao.get(taxPeriodId));
        reportPeriod.setDictTaxPeriodId(21);

        final int reportPeriodId = reportPeriodDao.save(reportPeriod);

        // Закрытый обычный период
        DepartmentReportPeriod departmentReportPeriod1 = new DepartmentReportPeriod();
        departmentReportPeriod1.setBalance(false);
        departmentReportPeriod1.setActive(false);
        departmentReportPeriod1.setReportPeriod(reportPeriodDao.get(reportPeriodId));
        departmentReportPeriod1.setDepartmentId(departmentId);
        final int departmentReportPeriodId1 = departmentReportPeriodDao.save(departmentReportPeriod1);

        // Закрытый корректирующий период
        DepartmentReportPeriod departmentReportPeriod2 = new DepartmentReportPeriod();
        departmentReportPeriod2.setBalance(false);
        departmentReportPeriod2.setActive(false);
        departmentReportPeriod2.setReportPeriod(reportPeriodDao.get(reportPeriodId));
        departmentReportPeriod2.setDepartmentId(departmentId);
        departmentReportPeriod2.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("01.01.2013"));
        final int departmentReportPeriodId2 = departmentReportPeriodDao.save(departmentReportPeriod2);

        // Открытый корректирующий период
        DepartmentReportPeriod departmentReportPeriod3 = new DepartmentReportPeriod();
        departmentReportPeriod3.setBalance(false);
        departmentReportPeriod3.setActive(true);
        departmentReportPeriod3.setReportPeriod(reportPeriodDao.get(reportPeriodId));
        departmentReportPeriod3.setDepartmentId(departmentId);
        departmentReportPeriod3.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("03.01.2013"));
        final int departmentReportPeriodId3 = departmentReportPeriodDao.save(departmentReportPeriod3);

        FormDataKind primary = FormDataKind.PRIMARY;
        WorkflowState state = WorkflowState.CREATED;
        // НФ нет
        FormData formData = formDataDao.getLast(typeId, primary, departmentId, reportPeriodId, null);
        Assert.assertNull(formData);

        FormTemplate formTemplate = formTemplateDao.get(typeId);

        // НФ в первом периоде
        FormData formData1 = new FormData(formTemplate);
        formData1.setDepartmentReportPeriodId(departmentReportPeriodId1);
        formData1.setKind(primary);

        formData1.setState(state);
        final long fd1 = formDataDao.save(formData1);
        formData = formDataDao.getLast(typeId, primary, departmentId, reportPeriodId, null);
        Assert.assertNotNull(formData);
        Assert.assertEquals(fd1, formData.getId().longValue());

        // НФ во втором периоде
        FormData formData2 = new FormData(formTemplate);
        formData2.setDepartmentReportPeriodId(departmentReportPeriodId2);
        formData2.setKind(primary);
        formData2.setState(state);
        final long fd2 = formDataDao.save(formData2);
        formData = formDataDao.getLast(typeId, primary, departmentId, reportPeriodId, null);
        Assert.assertNotNull(formData);
        Assert.assertEquals(fd2, formData.getId().longValue());

        // НФ в третьем периоде
        FormData formData3 = new FormData(formTemplate);
        formData3.setDepartmentReportPeriodId(departmentReportPeriodId3);
        formData3.setKind(primary);
        formData3.setState(state);
        final long fd3 = formDataDao.save(formData3);
        formData = formDataDao.getLast(typeId, primary, departmentId, reportPeriodId, null);
        Assert.assertNotNull(formData);
        Assert.assertEquals(fd3, formData.getId().longValue());
    }
}
