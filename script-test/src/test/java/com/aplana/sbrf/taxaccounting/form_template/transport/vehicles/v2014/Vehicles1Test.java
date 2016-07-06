package com.aplana.sbrf.taxaccounting.form_template.transport.vehicles.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог
 *
 * @author Levykin
 */
public class Vehicles1Test extends ScriptTestBase {
    private static final int TYPE_ID = 204;
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
        return getDefaultScriptTestMockHelper(Vehicles1Test.class);
    }

    @Before
    public void mock() {
        // Для работы логических проверок
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
                            result.add(map);
                        }
                        return result;
                    }
                });
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });

        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        // провайдер
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), anyLong(),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(testHelper.getRefBookDataProvider());

        // список id записей справочника 42
        when(testHelper.getRefBookDataProvider().getParentsHierarchy(anyLong())).thenReturn(Arrays.asList(3L));
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

    // Консолидация
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

        // Данные НФ-источника, формируются импортом
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(testHelper.getDataRowHelper().getAll());
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        int expected = 4; // 3 строки фиксированны (заголовки разделов) + 1 строка из тф
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        //checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {

        // графа 11
        Assert.assertEquals("12.01.2012", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(1).getCell("regDate").getDateValue())));
        Assert.assertEquals("12.01.2012", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(2).getCell("regDate").getDateValue())));
        Assert.assertEquals("12.02.2011", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(4).getCell("regDate").getDateValue())));
        Assert.assertEquals("20.01.2008", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(6).getCell("regDate").getDateValue())));

        // графа 12
        Assert.assertEquals("12.01.2014", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(1).getCell("regDateEnd").getDateValue())));

        // графа 13
        Assert.assertEquals(12.0, dataRows.get(1).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(23.0, dataRows.get(2).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(34.0, dataRows.get(4).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(56.0, dataRows.get(6).getCell("taxBase").getNumericValue().doubleValue(), 0.0);

        // графа 14
        Assert.assertEquals(1, dataRows.get(1).getCell("baseUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(2).getCell("baseUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(4).getCell("baseUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(6).getCell("baseUnit").getNumericValue().intValue());

        // графа 15
        Assert.assertEquals("2008", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(1).getCell("year").getDateValue())));
        Assert.assertEquals("2012", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(2).getCell("year").getDateValue())));
        Assert.assertEquals("2001", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(4).getCell("year").getDateValue())));
        Assert.assertEquals("2011", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(6).getCell("year").getDateValue())));

        // графа 20
        Assert.assertEquals(12.0, dataRows.get(1).getCell("costOnPeriodBegin").getNumericValue().doubleValue(), 0.0);
    }
}
