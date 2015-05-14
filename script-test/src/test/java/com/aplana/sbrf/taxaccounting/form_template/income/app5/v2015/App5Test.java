package com.aplana.sbrf.taxaccounting.form_template.income.app5.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
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
    private static final int DEPARTMENT_ID = 2;
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

        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");
        department.setType(DepartmentType.TERR_BANK);

        when(testHelper.getDepartmentService().get(DEPARTMENT_ID)).thenReturn(department);

        when(testHelper.getRefBookFactory().getDataProvider(any(Long.class))).thenAnswer(
                new Answer<RefBookDataProvider>() {
                    @Override
                    public RefBookDataProvider answer(InvocationOnMock invocation) throws Throwable {
                        return testHelper.getRefBookDataProvider();
                    }
                });

        when(testHelper.getRefBookFactory().getDataProvider(33L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();

                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("TAX_RATE", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        map.put("TAX_RATE", new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                        map.put("TAX_RATE", new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                        result.add(map);

                        return result;
                    }
                });

        when(testHelper.getRefBookFactory().getDataProvider(30L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String)invocation.getArguments()[2];
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();

                        if (filter.contains("DEPARTMENT_ID = 2")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "A"));
                            map.put("PARENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                            map.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                            result.add(map);

                            map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                            map.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                            map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "B"));
                            map.put("PARENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
                            map.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
                            result.add(map);

                            map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                            map.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                            map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "C"));
                            map.put("PARENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 3L));
                            map.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, 3L));
                            result.add(map);

                        }  else if (filter.equals("LINK = 1")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("SUM_TAX", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("ADDITIONAL_NAME", new RefBookValue(RefBookAttributeType.STRING, "addNameA"));
                            map.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "111111111"));
                            map.put("LINK", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                            map.put("OBLIGATION", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                            result.add(map);

                            map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                            map.put("SUM_TAX", new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                            map.put("ADDITIONAL_NAME", new RefBookValue(RefBookAttributeType.STRING, "addNameA"));
                            map.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "111111111"));
                            map.put("LINK", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
                            map.put("OBLIGATION", new RefBookValue(RefBookAttributeType.REFERENCE, 2L));
                            result.add(map);

                            map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                            map.put("SUM_TAX", new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                            map.put("ADDITIONAL_NAME", new RefBookValue(RefBookAttributeType.STRING, "addNameA"));
                            map.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "111111111"));
                            map.put("LINK", new RefBookValue(RefBookAttributeType.REFERENCE, 3L));
                            map.put("OBLIGATION", new RefBookValue(RefBookAttributeType.REFERENCE, 3L));
                            result.add(map);

                        }
                        return result;
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
            Assert.assertNull(dataRows.get(i).getCell("regionBank").getNumericValue());
        }

        // графа 3
        Assert.assertEquals(2, dataRows.get(0).getCell("regionBankDivision").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("regionBankDivision").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("regionBankDivision").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("regionBankDivision").getNumericValue().intValue());

        // графа 4
        Assert.assertNull(dataRows.get(0).getCell("divisionName").getStringValue());
        Assert.assertNull(dataRows.get(1).getCell("divisionName").getStringValue());
        Assert.assertNull(dataRows.get(2).getCell("divisionName").getStringValue());
        Assert.assertNull(dataRows.get(3).getCell("divisionName").getStringValue());

        // графа 5
        Assert.assertEquals("111111111", dataRows.get(0).getCell("kpp").getStringValue());
        Assert.assertEquals("111111111", dataRows.get(1).getCell("kpp").getStringValue());
        Assert.assertEquals("111111111", dataRows.get(2).getCell("kpp").getStringValue());
        Assert.assertEquals("111111111", dataRows.get(3).getCell("kpp").getStringValue());

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
