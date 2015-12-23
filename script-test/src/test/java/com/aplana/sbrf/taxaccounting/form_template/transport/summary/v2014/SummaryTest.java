package com.aplana.sbrf.taxaccounting.form_template.transport.summary.v2014;

import com.aplana.sbrf.taxaccounting.form_template.transport.vehicles.v2014.Vehicles1Test;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Расчет суммы налога по каждому транспортному средству
 *
 * @author Kinzyabulatov
 */
public class SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 203;
    private static final int SOURCE_TYPE_ID = 201;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private static final FormDataKind SOURCE_KIND = FormDataKind.PRIMARY;

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
        return getDefaultScriptTestMockHelper(SummaryTest.class);
    }

    @Before
    public void mockRefBookDataProvider() {
        // Для работы проверок
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String)invocation.getArguments()[2];
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        if (filter.equals("DECLARATION_REGION_ID = 1 and OKTMO = 1")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("REGION_ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "taxA"));
                            map.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "kpp"));

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

        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
    }

    @Test
    public void afterCreateTest() {
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        checkLogger();
    }

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    // Консолидация
    // TODO @Test
    public void composeTest() {
        // собирается из первички транспорта
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(SOURCE_KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(SOURCE_TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Один экземпляр-источник(первичная)
        FormData sourceFormData = new FormData();
        FormType formType = new FormType();
        formType.setId(SOURCE_TYPE_ID);
        sourceFormData.setId(2L);
        sourceFormData.setKind(SOURCE_KIND);
        sourceFormData.setFormType(formType);
        sourceFormData.setDepartmentId(DEPARTMENT_ID);
        sourceFormData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        sourceFormData.setReportPeriodId(REPORT_PERIOD_ID);
        sourceFormData.setState(WorkflowState.ACCEPTED);

        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");

        // DataRowHelper источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        TestScriptHelper sourceTestHelper = new TestScriptHelper("/form_template/transport/vehicles/v2014/", sourceFormData, getDefaultScriptTestMockHelper(Vehicles1Test.class));
        sourceTestHelper.setImportFileInputStream(getCustomInputStream("sourceImportFile.xlsm"));
        sourceTestHelper.initRowData();

        when(sourceTestHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });

        when(sourceTestHelper.getRefBookFactory().get(anyLong())).then(
                new Answer<RefBook>() {
                    @Override
                    public RefBook answer(InvocationOnMock invocation) throws Throwable {
                        RefBook result = new RefBook();
                        result.setName("ref_book_" + invocation.getArguments()[0]);
                        return result;
                    }
                });

        when(sourceTestHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String)invocation.getArguments()[2];
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        if (filter.startsWith("TAX_BENEFIT_ID = ") && filter.endsWith(" and DECLARATION_REGION_ID = 1")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            if (filter.contains("TAX_BENEFIT_ID = 2")) {
                                map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                            }
                            if (filter.contains("TAX_BENEFIT_ID = 3")) {
                                map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                            }
                            if (filter.contains("TAX_BENEFIT_ID = 1"))  {
                                map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            }
                            result.add(map);
                        }
                        return result;
                    }
                });

        sourceTestHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(sourceTestHelper.getDataRowHelper().getAll());

        when(testHelper.getFormDataService().getLast(eq(SOURCE_TYPE_ID), eq(SOURCE_KIND), eq(DEPARTMENT_ID), anyInt(),
                any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        when(testHelper.getDepartmentService().get(DEPARTMENT_ID)).thenReturn(department);

        // DataRowHelper НФ-источника
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Консолидация
        testHelper.initRowData();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(11, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());

        Assert.assertTrue("Logger must contains error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
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
        int expected = 12; // в файле 12 строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // TODO (Ramil Timerbaev) возможно надо будет добавить проверку загруженых значении
        checkLogger();
    }

    /**
     * Проверить загруженные данные, а также выполнение расчетов и логических проверок.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {

        // графа 11
        Assert.assertEquals("12.01.2012", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(1).getCell("regDate").getDateValue())));
        Assert.assertEquals("12.01.2012", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(2).getCell("regDate").getDateValue())));
        Assert.assertEquals("12.02.2011", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(5).getCell("regDate").getDateValue())));
        Assert.assertEquals("20.01.2008", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(8).getCell("regDate").getDateValue())));

        // графа 12
        Assert.assertEquals("12.01.2014", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(1).getCell("regDateEnd").getDateValue())));

        // графа 13
        Assert.assertEquals(12.0, dataRows.get(1).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertNull(dataRows.get(2).getCell("taxBase").getNumericValue());
        Assert.assertEquals(34.0, dataRows.get(5).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(56.0, dataRows.get(8).getCell("taxBase").getNumericValue().doubleValue(), 0.0);

        // графа 14
        Assert.assertEquals(1, dataRows.get(1).getCell("taxBaseOkeiUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(2).getCell("taxBaseOkeiUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(5).getCell("taxBaseOkeiUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(8).getCell("taxBaseOkeiUnit").getNumericValue().intValue());

        // графа 15
        Assert.assertEquals("2008", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(1).getCell("createYear").getDateValue())));
        Assert.assertEquals("2012", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(2).getCell("createYear").getDateValue())));
        Assert.assertEquals("2001", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(5).getCell("createYear").getDateValue())));
        Assert.assertEquals("2011", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(8).getCell("createYear").getDateValue())));

        // графа 20
        Assert.assertEquals(12.0, dataRows.get(1).getCell("periodStartCost").getNumericValue().doubleValue(), 0.0);

        //графа 2, 3
        Assert.assertEquals("taxA", dataRows.get(1).getCell("taxAuthority").getStringValue());
        Assert.assertEquals("kpp", dataRows.get(2).getCell("kpp").getStringValue());

        // графа 21
        Assert.assertEquals(12, dataRows.get(2).getCell("ownMonths").getNumericValue().intValue());

        // Графа 23
        Assert.assertEquals(0.3333, dataRows.get(1).getCell("coef362").getNumericValue().doubleValue(), 0.0);

        // Графа 24
        Assert.assertEquals("12/1", dataRows.get(1).getCell("partRight").getStringValue());
        Assert.assertEquals("23/1", dataRows.get(2).getCell("partRight").getStringValue());

        // Графа 25
        Assert.assertEquals(48, dataRows.get(1).getCell("calculatedTaxSum").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(2).getCell("calculatedTaxSum").getNumericValue());

        // Графа 26
        Assert.assertEquals(1, dataRows.get(1).getCell("benefitMonths").getNumericValue().intValue());

        // Графа 29
        Assert.assertEquals(0.3333, dataRows.get(1).getCell("coefKl").getNumericValue().doubleValue(), 0.0);
        Assert.assertNull(dataRows.get(2).getCell("coefKl").getNumericValue());

        // Графа 31
        Assert.assertNull(dataRows.get(1).getCell("benefitSum").getNumericValue());
        Assert.assertNull(dataRows.get(5).getCell("benefitSum").getNumericValue());
        Assert.assertEquals(1045.23, dataRows.get(8).getCell("benefitSum").getNumericValue().doubleValue(), 0.0);

        // Графа 33
        Assert.assertNull(dataRows.get(1).getCell("benefitSumDecrease").getNumericValue());
        Assert.assertEquals(7.71, dataRows.get(5).getCell("benefitSumDecrease").getNumericValue().doubleValue(), 0.0);
        Assert.assertNull(dataRows.get(8).getCell("benefitSumDecrease").getNumericValue());

        // Графа 35
        Assert.assertEquals(-0.96, dataRows.get(1).getCell("benefitSumReduction").getNumericValue().doubleValue(), 0.0);
        Assert.assertNull(dataRows.get(5).getCell("benefitSumReduction").getNumericValue());
        Assert.assertNull(dataRows.get(8).getCell("benefitSumReduction").getNumericValue());

        // Графа 37
        Assert.assertEquals(49.00, dataRows.get(1).getCell("taxSumToPay").getNumericValue().doubleValue(), 0.0);
    }
}
