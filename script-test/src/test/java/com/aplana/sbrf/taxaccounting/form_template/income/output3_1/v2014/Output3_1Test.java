package com.aplana.sbrf.taxaccounting.form_template.income.output3_1.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика (new).
 */
public class Output3_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 412;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        // «Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)»
        formTypeIdByTemplateIdMap.put(10080, 10070);
        // «Сведения о уплаченных суммах налога по операциям с ГЦБ»
        formTypeIdByTemplateIdMap.put(420, 420);
        // «Сведения о суммах налога на прибыль, уплаченного Банком за рубежом»
        formTypeIdByTemplateIdMap.put(421, 421);
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();
    static {
        templatesPathMap.put(10080, "..//src/main//resources//form_template//income//incomeWithHoldingAgent//v2014//");
        templatesPathMap.put(420,   "..//src/main//resources//form_template//income//output5//v2014//");
        templatesPathMap.put(421,   "..//src/main//resources//form_template//income//output4_1//v2014//");
    }

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
        return getDefaultScriptTestMockHelper(Output3_1Test.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
        testHelper.reset();
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

    @Test
    public void addDelRowTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 1;

        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        // ошибок быть не должно
        checkLogger();
        // Количество строк должно увеличиться на 1
        Assert.assertEquals("Add new row", expected, testHelper.getDataRowHelper().getAll().size());

        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        // Количество строк должно уменьшиться на 1
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        expected--;
        // Количество строк должно уменьшиться на 1
        Assert.assertEquals("Delete row", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Консолидация
    // TODO (Ramil Timerbaev)
    // @Test
    public void composeTest() {
        // настройка справочника
        Long refbookId = 4L;
        // провайдер для каждого справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refbookId);
        when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);

        // записи для каждого справочника
        Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(refbookId);

        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records.values());
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenReturn(result);

        // источники
        int[] sourceTemplateIds = new int [] { 10080, 420, 421 };
        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>(sourceTemplateIds.length);
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);
            FormDataKind kind = FormDataKind.CONSOLIDATED;

            // источник
            DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
            departmentFormTypes.add(departmentFormType);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
        }
        // источник
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);

        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        int expected = 3; // по 1 стоке из 3 источников
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        int precision = 0;

        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }
            BigDecimal expectedRefbook = roundValue(index, precision);
            BigDecimal expectedNum = roundValue(index, precision);
            String expectedString = "test" + index;

            // графа 1
            Assert.assertEquals("row.paymentType[" + row.getIndex() + "]", expectedRefbook, row.getCell("paymentType").getNumericValue());
            // графа 2
            Assert.assertEquals("row.okatoCode[" + row.getIndex() + "]", expectedString, row.getCell("okatoCode").getStringValue());
            // графа 3
            Assert.assertEquals("row.budgetClassificationCode[" + row.getIndex() + "]", expectedString, row.getCell("budgetClassificationCode").getStringValue());
            // графа 4
            Assert.assertNotNull("row.dateOfPayment[" + row.getIndex() + "]", row.getCell("dateOfPayment").getDateValue());
            // графа 5
            Assert.assertEquals("row.sumTax[" + row.getIndex() + "]", expectedNum, row.getCell("sumTax").getNumericValue());

            index++;
        }
    }

    // Округляет число до требуемой точности
    BigDecimal roundValue(Long value, int precision) {
        if (value != null) {
            return (BigDecimal.valueOf(value)).setScale(precision, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }

    /**
     * Получить источник.
     *
     * @param id идентификатор источника
     * @param sourceTypeId идентификатор типа формы источника
     * @param kind вид формы источника
     */
    private DepartmentFormType getSource(int id, int sourceTypeId, FormDataKind kind) {
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setId(id);
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setKind(kind);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        return departmentFormType;
    }

    /**
     * Получить форму источника.
     *
     * @param id идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.DEAL);
        formType.setName(sourceTemplate.getName());

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId((long) id);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        sourceFormData.setFormTemplateId(sourceTemplateId);

        return sourceFormData;
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "test_" + 1;
        Date testDate = new Date();
        Long testNum = 100L;
        Long testRefbookId = 1L;
        DataRow<Cell> row;
        switch (sourceTemplateId) {
            // 10080, 420, 421
            case 10080 :
                row = sourceFormData.createDataRow();
                row.setIndex(1);

                // графа 2, 3, 7, 13, 14, 15, 21, 26, 29, 30, 32..42
                String [] strColumns = { "emitentName", "emitentInn", "decisionNumber", "addresseeName", "inn", "kpp",
                        "series", "number", "withheldNumber", "postcode", "district", "city", "locality", "street",
                        "house", "housing", "apartment", "surname", "name", "patronymic", "phone" };
                for (String alias : strColumns) {
                    row.getCell(alias).setValue(testString, null);
                }

                // графа 4, 5, 6, 10, 11, 12, 16, 17, 22, 23, 24, 27
                String [] numColumns = { "all", "rateZero", "distributionSum", "firstMonth", "lastMonth", "allSum", "type",
                        "status", "rate", "dividends", "sum", "withheldSum" };
                List<String> shortNumColumns = Arrays.asList("firstMonth", "lastMonth", "type", "status");
                for (String alias : numColumns) {
                    row.getCell(alias).setValue(shortNumColumns.contains(alias) ? 1L : testNum, null);
                }

                // графа 19, 20, 31
                String [] refbookColumns = { "citizenship", "kind", "region" };
                for (String alias : refbookColumns) {
                    row.getCell(alias).setValue(testRefbookId, null);
                }

                // графа 8, 9, 18, 25, 28
                String [] dateColumns = { "decisionDate", "year", "birthday", "date", "withheldDate" };
                for (String alias : dateColumns) {
                    row.getCell(alias).setValue(testDate, null);
                }
                dataRows.add(row);
                break;
            case 420 :
                row = sourceFormData.createDataRow();
                // TODO (Ramil Timerbaev)
                // графа 1
                // row.getCell("").setValue(1L, null);
                dataRows.add(row);
                break;
            case 421 :
                row = sourceFormData.createDataRow();
                // TODO (Ramil Timerbaev)
                // графа 1
                // row.getCell("").setValue(1L, null);
                dataRows.add(row);
                break;
        }
        return dataRows;
    }
}