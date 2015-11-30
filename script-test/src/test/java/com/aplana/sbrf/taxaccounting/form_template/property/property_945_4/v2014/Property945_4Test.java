package com.aplana.sbrf.taxaccounting.form_template.property.property_945_4.v2014;

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
public class Property945_4Test extends ScriptTestBase {
    private static final int TYPE_ID = 612;
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
        return getDefaultScriptTestMockHelper(Property945_4Test.class);
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
                        map.put("RATE", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
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
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals("s1__i1__si1_", dataRows.get(0).getCell("benefitBasis").getStringValue());
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        // идентификатор шаблона источника
        int sourceTypeId = 612;
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//property//property_945_4//v2014//");

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются вручную
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        // формируем три строки источника
        for (int i = 1; i <= 3; i++) {

            DataRow<Cell> row = sourceFormData.createDataRow();
            // графа 1
            row.getCell("rowNum").setValue(i, null);
            // графа 2
            row.getCell("fix").setValue("test", null);
            // графа 3
            row.getCell("subject").setValue(1L, null);
            // графа 4
            row.getCell("taxAuthority").setValue("test", null);
            // графа 5
            row.getCell("kpp").setValue("testB", null);
            // графа 6
            row.getCell("oktmo").setValue(1L, null);
            // графа 7
            row.getCell("address").setValue("testB", null);
            // графа 8
            row.getCell("sign").setValue(1L, null);
            // графа 9
            row.getCell("cadastreNumBuilding").setValue("testB", null);
            // графа 10
            row.getCell("cadastreNumRoom").setValue("testB", null);
            // графа 11
            row.getCell("cadastrePriceJanuary").setValue(1L, null);
            // графа 12
            row.getCell("cadastrePriceTaxFree").setValue(1L, null);
            // графа 13
            row.getCell("tenure").setValue(1L, null);
            // графа 14
            row.getCell("taxBenefitCode").setValue(1L, null);
            // графа 15
            row.getCell("benefitBasis").setValue("testA", null);
            // графа 16
            row.getCell("taxBase").setValue(1L, null);
            // графа 17
            row.getCell("taxRate").setValue(1L, null);
            // графа 18
            row.getCell("sum").setValue(1L, null);
            // графа 19
            row.getCell("periodSum").setValue(1L, null);
            // графа 20
            row.getCell("reductionPaymentSum").setValue(1L, null);
            dataRows.add(row);
        }

        sourceDataRowHelper.save(dataRows);
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        checkLogger();
    }
}
