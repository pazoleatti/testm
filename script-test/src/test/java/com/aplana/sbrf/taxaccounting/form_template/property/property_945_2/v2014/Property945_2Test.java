package com.aplana.sbrf.taxaccounting.form_template.property.property_945_2.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
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

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Данные о кадастровой стоимости объектов недвижимости для расчета налога на имущество.
 *
 * @author Alexey Afanasyev
 */
public class Property945_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 611;
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
        return getDefaultScriptTestMockHelper(Property945_2Test.class);
    }

    @Before
    public void mock() {
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });
        when(testHelper.getRefBookFactory().getDataProvider(any(Long.class))).thenAnswer(
                new Answer<RefBookDataProvider>() {
                    @Override
                    public RefBookDataProvider answer(InvocationOnMock invocation) throws Throwable {
                        return testHelper.getRefBookDataProvider();
                    }
                });

        when(testHelper.getRefBookFactory().getDataProvider(202L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();

                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("REGION_ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        result.add(map);

                        return result;
                    }
                });
        when(testHelper.getRefBookFactory().getDataProvider(203L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();

                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                        map.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "i1__"));
                        map.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "si1_"));
                        map.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "s1__"));
                        map.put("DECLARATION_REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                        map.put("REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                        map.put("PARAM_DESTINATION", new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        map.put("TAX_BENEFIT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                        map.put("ITEM", new RefBookValue(RefBookAttributeType.STRING, "i2__"));
                        map.put("SUBITEM", new RefBookValue(RefBookAttributeType.STRING, "si2_"));
                        map.put("SECTION", new RefBookValue(RefBookAttributeType.STRING, "s2__"));
                        map.put("DECLARATION_REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                        map.put("REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
                        map.put("PARAM_DESTINATION", new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        result.add(map);

                        return result;
                    }
                });

        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());
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
    public void afterCreateTest() {
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        // Проверка расчетных данных
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals("s1__i1__si1_", dataRows.get(0).getCell("benefitBasis").getStringValue());
        Assert.assertNull(dataRows.get(1).getCell("benefitBasis").getStringValue());
        Assert.assertEquals("s2__i2__si2_", dataRows.get(2).getCell("benefitBasis").getStringValue());
        Assert.assertNull(dataRows.get(3).getCell("benefitBasis").getStringValue());
        Assert.assertEquals("", dataRows.get(4).getCell("benefitBasis").getStringValue());
        checkLogger();
    }
    @Test
    public void importTransportFileTest() {
        int expected = 1 + 1 + 1; // в источнике 1 строка (без итогов и подитогов) + по 1 подитогу на строку + 1 итоговая строка
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }
    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 12, 13
        String[] dateColumns = {"propertyRightBeginDate", "propertyRightEndDate"};

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }
            for (String alias : dateColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertNotNull(msg, row.getCell(alias).getDateValue());
            }
        }
    }

}
