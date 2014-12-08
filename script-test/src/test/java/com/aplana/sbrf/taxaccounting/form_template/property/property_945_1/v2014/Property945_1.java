package com.aplana.sbrf.taxaccounting.form_template.property.property_945_1.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Данные бухгалтерского учета для расчета налога на имущество.
 *
 * TODO:
 *      - загрузка *.rnu
 */
public class Property945_1 extends ScriptTestBase {
    private static final int TYPE_ID =610;
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
        return getDefaultScriptTestMockHelper(Property945_1.class);
    }

    @Before
    public void mockFormDataService() {
        // для поиска подразделений и настроек подразделении
//        when(testHelper.getFormDataService().getRefBookRecord(anyLong(), anyMap(), anyMap(), anyMap(), anyString(),
//                anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
//                new Answer<Map<String, RefBookValue>>() {
//                    @Override
//                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
//                        Long refBookId = (Long) invocation.getArguments()[0];
//                        if (refBookId == null) {
//                            return null;
//                        }
//                        String value5 = (String) invocation.getArguments()[5];
//                        if (value5 == null || "".equals(value5.trim())) {
//                            return null;
//                        }
//                        Long departmentId = Long.valueOf(value5);
//                        return testHelper.getFormDataService().getRefBookValue(refBookId, departmentId, new HashMap<String, Map<String, RefBookValue>>());
//                    }
//                });
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

    // TODO (Ramil Timerbaev)
    // @Test
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
        Assert.assertEquals(58, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Double [] values = new Double [] {
                32661.00, 32661.00, 0.00, 0.00, 32661.00, -1.00, -2.00,
                -1.00, -233.00, -1.00, -1.00, -2.00, -1.00, -1.00, -2.00,
                -1.00, -233.00, 0.00, 0.00, -1.00, -1.00, -1.00, 0.00, -233.00,
                -233.00, -433.00, -433.00, -433.00, -233.00, -1.00, -1.00, 0.00,
                0.00, -1.00, -1.00, -2.00, -1.00, -233.00, -1.00, -1.00, -2.00,
                -1.00, -1.00, -2.00, -1.00, -233.00, 0.00, 0.00, -1.00, -1.00,
                -1.00, 0.00, -233.00, -233.00, -433.00, -433.00, -433.00, -2331.00
        };

        // графа 7
        for (int i = 0; i < dataRows.size() - 1; i++) {
            Assert.assertEquals(values[i], dataRows.get(i).getCell("taxBaseSum").getNumericValue().doubleValue(), 0.00001);
        }
    }
}
