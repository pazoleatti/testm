package com.aplana.sbrf.taxaccounting.form_template.property.property_945_3.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Расчёт налога на имущество по средней/среднегодовой стоимости.
 *
 * @author Alexey Afanasyev
 */
public class Property945_3Test extends ScriptTestBase {
    private static final int TYPE_ID = 613;
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
        return getDefaultScriptTestMockHelper(Property945_3Test.class);
    }

    @Before
    public void mockRefBookDataProvider() {

        // Для работы проверок
        when(testHelper.getRefBookFactory().get(203L)).thenAnswer(
                new Answer<RefBook>() {

                    @Override
                    public RefBook answer(InvocationOnMock invocation) throws Throwable {
                        RefBook refBook = new RefBook();
                        refBook.setId(203L);
                        refBook.setName("Параметры налоговых льгот налога на имущество");
                        return refBook;
                    }
                }
        );
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

        when(testHelper.getRefBookFactory().getDataProvider(201L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
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
        when(testHelper.getRefBookFactory().getDataProvider(202L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();

                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("REGION_ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "2012400"));
                        result.add(map);

                        return result;
                    }
                });
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String) invocation.getArguments()[2];
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        if (filter.equals("DECLARATION_REGION_ID = 1 and OKTMO = 1")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("REGION_ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        } else if (filter.toLowerCase().contains("OKTMO_DEFINITION like ".toLowerCase()) || filter.toLowerCase().contains("CODE like ".toLowerCase())) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        } else if (filter.contains("(YEAR_FROM <")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("COEF", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        }
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
                        map.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
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
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются вручную
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        // формируем одну строку источника
            DataRow<Cell> row = sourceFormData.createDataRow();
            // графа 1
            row.getCell("rowNum").setValue(1L, null);
            // графа 2
            row.getCell("subject").setValue(11L, null);
            // графа 3
            row.getCell("taxAuthority").setValue("text", null);
            // графа 4
            row.getCell("kpp").setValue("testB", null);
            // графа 5
            row.getCell("oktmo").setValue(1L, null);
            // графа 6
            row.getCell("priceAverage").setValue(1L, null);
            // графа 7
            row.getCell("taxBenefitCode").setValue(1L, null);
            // графа 8
            row.getCell("benefitBasis").setValue("12345678", null);
            // графа 9
            row.getCell("priceAverageTaxFree").setValue(1L, null);
            // графа 10
            row.getCell("taxBase").setValue(1L, null);
            // графа 11
            row.getCell("taxBenefitCodeReduction").setValue(1L, null);
            // графа 12
            row.getCell("benefitReductionBasis").setValue("textB", null);
            // графа 13
            row.getCell("taxRate").setValue(1L, null);
            // графа 14
            row.getCell("taxSum").setValue(1L, null);
            // графа 15
            row.getCell("sumPayment").setValue(1L, null);
            // графа 16
            row.getCell("taxBenefitCodeDecrease").setValue(1L, null);
            // графа 17
            row.getCell("benefitDecreaseBasis").setValue("123456", null);
            // графа 18
            row.getCell("sumDecrease").setValue(1L, null);
            // графа 19
            row.getCell("residualValue").setValue(1L, null);

            dataRows.add(row);

            sourceDataRowHelper.save(dataRows);
            testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals("s1__i1__si1_", dataRows.get(0).getCell("benefitBasis").getStringValue());
        Assert.assertNull(dataRows.get(4).getCell("benefitBasis").getStringValue());
        testHelper.execute(FormDataEvent.CALCULATE);
        // Проверка расчетных данных
        Assert.assertEquals("s1__i1__si1_", dataRows.get(0).getCell("benefitBasis").getStringValue());
        Assert.assertEquals(null, dataRows.get(4).getCell("benefitBasis").getStringValue());
        checkLogger();
    }
}
