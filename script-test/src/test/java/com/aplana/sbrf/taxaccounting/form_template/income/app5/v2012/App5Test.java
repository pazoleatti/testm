package com.aplana.sbrf.taxaccounting.form_template.income.app5.v2012;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * (Приложение 5) Сведения для расчета налога на прибыль.
 *
 * TODO:
 *      - убрал тест консолидации, потому что в скрипте используется FormDataService.consolidationSimple.
 *          Нет смысла его мокать и производить расчеты-проверки. Их проверить можно при загрузке
 */
public class App5Test extends ScriptTestBase {
    private static final int TYPE_ID = 372;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

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
        return getDefaultScriptTestMockHelper(App5Test.class);
    }

    @Before
    public void mockFormDataService() {
        // для поиска подразделений и настроек подразделении
        when(testHelper.getFormDataService().getRefBookRecord(anyLong(), anyMap(), anyMap(), anyMap(), anyString(),
                anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Long refBookId = (Long) invocation.getArguments()[0];
                        if (refBookId == null) {
                            return null;
                        }
                        String value5 = (String) invocation.getArguments()[5];
                        if (value5 == null || "".equals(value5.trim())) {
                            return null;
                        }
                        Long departmentId = Long.valueOf(value5);
                        return testHelper.getFormDataService().getRefBookValue(refBookId, departmentId, new HashMap<String, Map<String, RefBookValue>>());
                    }
                });
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size() + 1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 2
        for (int i = 0; i < dataRows.size() - 1; i++) {
            Assert.assertEquals(1, dataRows.get(i).getCell("regionBank").getNumericValue().intValue());
        }

        // графа 3
        Assert.assertEquals(2, dataRows.get(0).getCell("regionBankDivision").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("regionBankDivision").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("regionBankDivision").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(3).getCell("regionBankDivision").getNumericValue().intValue());

        // графа 4
        Assert.assertEquals("addNameB", dataRows.get(0).getCell("divisionName").getStringValue());
        Assert.assertEquals("addNameB", dataRows.get(1).getCell("divisionName").getStringValue());
        Assert.assertEquals("addNameB", dataRows.get(2).getCell("divisionName").getStringValue());
        Assert.assertEquals("addNameC", dataRows.get(3).getCell("divisionName").getStringValue());

        // графа 5
        Assert.assertEquals("kppB", dataRows.get(0).getCell("kpp").getStringValue());
        Assert.assertEquals("kppB", dataRows.get(1).getCell("kpp").getStringValue());
        Assert.assertEquals("kppB", dataRows.get(2).getCell("kpp").getStringValue());
        Assert.assertEquals("kppC", dataRows.get(3).getCell("kpp").getStringValue());

        // графа 6..10
        String [] aliases = new String [] {"avepropertyPricerageCost", "workersCount", "subjectTaxCredit", "decreaseTaxSum", "taxRate"};
        for (String alias : aliases) {
            Assert.assertEquals(23, dataRows.get(0).getCell(alias).getNumericValue().intValue());
            Assert.assertEquals(45, dataRows.get(1).getCell(alias).getNumericValue().intValue());
            Assert.assertEquals(12, dataRows.get(2).getCell(alias).getNumericValue().intValue());
            Assert.assertEquals(34, dataRows.get(3).getCell(alias).getNumericValue().intValue());
        }

        // итогова строка
        Assert.assertEquals(114, dataRows.get(4).getCell("avepropertyPricerageCost").getNumericValue().intValue());
        Assert.assertEquals(114, dataRows.get(4).getCell("workersCount").getNumericValue().intValue());
        Assert.assertEquals(114, dataRows.get(4).getCell("subjectTaxCredit").getNumericValue().intValue());
        Assert.assertEquals(114, dataRows.get(4).getCell("decreaseTaxSum").getNumericValue().intValue());
    }
}
