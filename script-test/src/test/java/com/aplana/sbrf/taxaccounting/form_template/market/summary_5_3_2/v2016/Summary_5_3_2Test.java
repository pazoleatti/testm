package com.aplana.sbrf.taxaccounting.form_template.market.summary_5_3_2.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 5.3.2 Внутренние интервалы процентных ставок по Кредитным продуктам и Субординированным кредитам.
 */
public class Summary_5_3_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 908;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formData.setId(TestScriptHelper.CURRENT_FORM_DATA_ID);
        formData.setFormType(formType);
        formData.setFormTemplateId(TYPE_ID);
        formData.setKind(KIND);
        formData.setState(WorkflowState.CREATED);
        formData.setDepartmentId(DEPARTMENT_ID);
        formData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Summary_5_3_2Test.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
        testHelper.reset();
    }

    @Test
    public void afterCreate() {
        int expected = 0;
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        // ошибок быть не должно
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        int expected = 0;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // консолидация с 1 источником
    @Test
    public void compose1SourceTest() {
        // получение макета текущей форму
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//summary_5_3_2//v2016//"));
        when(testHelper.getFormDataService().getFormTemplate(eq(getFormData().getFormTemplateId()))).thenReturn(formData.getFormTemplate());

        // для поиска кредитного рейтинга
        when(testHelper.getFormDataService().getRefBookRecord(eq(603L), anyMap(), anyMap(), anyMap(), anyString(),
                anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Long refBookId = (Long) invocation.getArguments()[0];
                        String value = (String) invocation.getArguments()[5];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
                        for (Long id : records.keySet()) {
                            Map<String, RefBookValue> record = records.get(id);
                            if (record.get("CREDIT_RATING").getStringValue().equals(value)) {
                                return record;
                            }
                        }
                        return null;
                    }
                });

        // для поиска курса валют
        when(testHelper.getFormDataService().getRefBookRecord(eq(22L), anyMap(), anyMap(), anyMap(), anyString(),
                anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Long refBookId = (Long) invocation.getArguments()[0];
                        String value = (String) invocation.getArguments()[5];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        Long tmp = Long.valueOf(value);
                        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
                        for (Long id : records.keySet()) {
                            Map<String, RefBookValue> record = records.get(id);
                            if (record.get("CODE_LETTER").getReferenceValue().equals(tmp)) {
                                return record;
                            }
                        }
                        return null;
                    }
                });

        // вспомогательные данные источника
        FormType formType = new FormType();
        formType.setName("testName");
        Department department = new Department();
        department.setName("testDepartmentName");
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2016);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("testReportPeriodName");
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setReportPeriod(reportPeriod);

        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relarion = new Relation();
        relarion.setFormDataId(1L);
        relarion.setFormType(formType);
        relarion.setDepartment(department);
        relarion.setDepartmentReportPeriod(departmentReportPeriod);
        sourcesInfo.add(relarion);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//summary_5_2//v2016//"));
        when(testHelper.getFormDataService().get(anyLong(), isNull(Boolean.class))).thenReturn(sourceFormData);

        // получение строк источника
        List<DataRow<Cell>> sourceDataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(1);
        sourceDataRows.add(sourceDataRow);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(sourceDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);
        int expected;

        // 1. консолидация должна пройти без проблем, 1 подходящая строка источника, в приемнике должен получится 1 блок из 20 строк
        long testMinMax = 1000L;
        setDefaultValues(sourceDataRow);
        // графа 28
        sourceDataRow.getCell("economyRate").setValue(testMinMax, sourceDataRow.getIndex());
        testHelper.execute(FormDataEvent.COMPOSE);
        expected = 20;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // количество
        DataRow<Cell> tmpDataRow = testHelper.getDataRowHelper().getAll().get(2);
        long expectedCount = 1L;
        Assert.assertEquals(expectedCount, tmpDataRow.getCell("count1_5year100").getNumericValue().longValue());
        // min, max
        tmpDataRow = testHelper.getDataRowHelper().getAll().get(3);
        Assert.assertEquals(testMinMax, tmpDataRow.getCell("min1_5year100").getNumericValue().longValue());
        Assert.assertEquals(testMinMax, tmpDataRow.getCell("max1_5year100").getNumericValue().longValue());
        checkLogger();

        // 2. консолидация должна пройти без проблем, 1 подходящая строка источника, в приемнике должен получится 1 блок из 20 строк
        setDefaultValues(sourceDataRow);
        // графа 8
        sourceDataRow.getCell("creditRating").setValue(3L, sourceDataRow.getIndex());
        // графа 28
        sourceDataRow.getCell("economyRate").setValue(testMinMax, sourceDataRow.getIndex());
        testHelper.execute(FormDataEvent.COMPOSE);
        expected = 20;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // количество
        tmpDataRow = testHelper.getDataRowHelper().getAll().get(2);
        expectedCount = 1L;
        Assert.assertEquals(expectedCount, tmpDataRow.getCell("count1_5year100").getNumericValue().longValue());
        // min, max
        tmpDataRow = testHelper.getDataRowHelper().getAll().get(3);
        Assert.assertEquals(testMinMax, tmpDataRow.getCell("min1_5year100").getNumericValue().longValue());
        Assert.assertEquals(testMinMax, tmpDataRow.getCell("max1_5year100").getNumericValue().longValue());
        checkLogger();

        // 3. консолидация не должна пройти, должно быть 1 фатальное сообщение
        setDefaultValues(sourceDataRow);
        // графа 16
        sourceDataRow.getCell("currencyCode").setValue(4L, sourceDataRow.getIndex());
        testHelper.getDataRowHelper().getAll().clear();
        testHelper.execute(FormDataEvent.COMPOSE);
        expected = 0;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        String msg = entries.get(i++).getMessage();
        boolean isContainErrorMsg = (msg.contains("Не найден курс валюты для "));
        Assert.assertEquals("Must have fatal error", true, isContainErrorMsg);
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. консолидация должна пройти без проблем, нет подходящих строк источника, в приемнике не должно быть строк
        setDefaultValues(sourceDataRow);
        // графа 29
        sourceDataRow.getCell("groupExclude").setValue(0L, sourceDataRow.getIndex());
        testHelper.getDataRowHelper().getAll().clear();
        testHelper.execute(FormDataEvent.COMPOSE);
        expected = 0;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    private void setDefaultValues(DataRow<Cell> sourceDataRow) {
        Long testRecordkId = 1L;
        // графа 2
        sourceDataRow.getCell("dealNum").setValue("test", sourceDataRow.getIndex());
        // графа 3
        sourceDataRow.getCell("debtorName").setValue("test", sourceDataRow.getIndex());
        // графа 8
        sourceDataRow.getCell("creditRating").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 15
        sourceDataRow.getCell("avgPeriod").setValue(4L, sourceDataRow.getIndex());
        // графа 16
        sourceDataRow.getCell("currencyCode").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 17
        sourceDataRow.getCell("creditSum").setValue(182L, sourceDataRow.getIndex());
        // графа 18
        sourceDataRow.getCell("rateType").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 23
        sourceDataRow.getCell("provideCategory").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 28
        sourceDataRow.getCell("economyRate").setValue(1000L, sourceDataRow.getIndex());
        // графа 29
        sourceDataRow.getCell("groupExclude").setValue(testRecordkId, sourceDataRow.getIndex());
    }
}
