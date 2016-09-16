package com.aplana.sbrf.taxaccounting.form_template.market.summary_5_5.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 5.5 Внутренние интервалы плат по гарантиям и непокрытым аккредитивам, ИТФ.
 */
public class Summary_5_5Test extends ScriptTestBase {
    private static final int TYPE_ID = 914;
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
        return getDefaultScriptTestMockHelper(Summary_5_5Test.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
        testHelper.reset();
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
        // получение макета текущей форму
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        when(testHelper.getFormDataService().getFormTemplate(eq(getFormData().getFormTemplateId()))).thenReturn(formData.getFormTemplate());

        int expected = testHelper.getDataRowHelper().getAll().size();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // консолидация с 1 источником
    @Test
    public void compose1SourceTest() {
        // получение макета текущей форму
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        when(testHelper.getFormDataService().getFormTemplate(eq(getFormData().getFormTemplateId()))).thenReturn(formData.getFormTemplate());

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
        sourceFormData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//summary_5_2b//v2016//"));
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

        // 1. нет подходящих строк источника, в приемнике строки должны быть пустые
        setDefaultValues(sourceDataRow);
        // графа 24
        sourceDataRow.getCell("groupExclude").setValue(0L, sourceDataRow.getIndex());
        expected = testHelper.getDataRowHelper().getAll().size();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // графа 5..28
        String [] consolidatedColumns = { "count05year100", "min05year100", "max05year100", "count05_1year100", "min05_1year100", "max05_1year100", "count1_3year100", "min1_3year100", "max1_3year100", "count3year100", "min3year100", "max3year100", "count05year101", "min05year101", "max05year101", "count05_1year101", "min05_1year101", "max05_1year101", "count1_3year101", "min1_3year101", "max1_3year101", "count3year101", "min3year101", "max3year101" };
        for (DataRow<Cell> row : testHelper.getDataRowHelper().getAll()) {
            if (row.getAlias() != null && row.getAlias().contains("header")) {
                continue;
            }
            for (String column : consolidatedColumns) {
                String msg = String.format("Строка %s (алиас %s): графа %s не пустая:", row.getIndex(), row.getAlias(), column);
                Assert.assertNull(msg, row.getCell(column).getValue());
            }
        }
        checkLogger();
        testHelper.getLogger().clear();

        // 2. одна подходящая строка источника, в приемнике должны заполнится количество, min и max в одной группе
        setDefaultValues(sourceDataRow);
        expected = testHelper.getDataRowHelper().getAll().size();
        testHelper.execute(FormDataEvent.COMPOSE);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        for (DataRow<Cell> row : testHelper.getDataRowHelper().getAll()) {
            if (row.getAlias() != null && row.getAlias().contains("header")) {
                continue;
            }
            for (String column : consolidatedColumns) {
                if (("1_AAA_yesNo_count".equals(row.getAlias()) && "count05_1year100".equals(column)) ||
                        ("1_AAA_yesNo_minmax".equals(row.getAlias()) && ("min05_1year100".equals(column) || "max05_1year100".equals(column)))) {
                    String msg = String.format("Строка %s (алиас %s): графа %s пустая:", row.getIndex(), row.getAlias(), column);
                    Assert.assertNotNull(msg, row.getCell(column).getValue());
                    continue;
                }
                String msg = String.format("Строка %s (алиас %s): графа %s не пустая:", row.getIndex(), row.getAlias(), column);
                Assert.assertNull(msg, row.getCell(column).getValue());
            }
        }
        checkLogger();
        testHelper.getLogger().clear();
    }

    private void setDefaultValues(DataRow<Cell> sourceDataRow) {
        Long testRecordkId = 1L;
        // графа 9
        sourceDataRow.getCell("internationalRating").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 16
        sourceDataRow.getCell("period").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 17
        sourceDataRow.getCell("obligationType").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 20
        sourceDataRow.getCell("rate").setValue(1000L, sourceDataRow.getIndex());
        // графа 21
        sourceDataRow.getCell("provisionPresence").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 23
        sourceDataRow.getCell("endSum").setValue(100000L, sourceDataRow.getIndex());
        // графа 24
        sourceDataRow.getCell("groupExclude").setValue(testRecordkId, sourceDataRow.getIndex());
    }
}
