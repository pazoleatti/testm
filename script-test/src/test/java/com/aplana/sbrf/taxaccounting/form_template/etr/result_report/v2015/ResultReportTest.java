package com.aplana.sbrf.taxaccounting.form_template.etr.result_report.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Величины налоговых платежей, вводимые вручную
 *
 * @author Stanislav Yasinskiy
 */
public class ResultReportTest extends ScriptTestBase {
    private static final int TYPE_ID = 730;
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
        formData.setPeriodOrder(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(ResultReportTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        // имя справочника
        RefBook rb = new RefBook();
        rb.setName("Проблемные зоны (test refBook)");
        when(testHelper.getRefBookFactory().get(anyLong())).thenReturn(rb);

        // провайдер
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), anyLong(),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(testHelper.getRefBookDataProvider());

        // записи 504
        Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(504L);
        // возвращаются все строки из справочника, делать отбор по фильтру не стал, т.к. лишние записи не вызывают ошибок, а только предупреждения
        final PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records.values());

        // список id записей справочника 42
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class),
                any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // фильтр - "REGION_ID = 1 and NAME like 'зона N'"
                String filter = (String) invocation.getArguments()[2];
                if (filter == null || filter.isEmpty()) {
                    return null;
                }
                // получить значение "зона N"
                int from = filter.indexOf("'") + 1;
                int to = filter.length() - 1;
                String name = filter.substring(from, to);

                for (Map<String, RefBookValue> item : result) {
                    if (item.get("NAME") != null && name.equals(item.get("NAME").getStringValue())) {
                        return new PagingResult<Map<String, RefBookValue>>(Arrays.asList(item));
                    }
                }
                return null;
            }
        });
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
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

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(0, testHelper.getLogger().getEntries().size());
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertFalse(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 4; // в файле 4 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("problemZone").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("problemZone").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(2).getCell("problemZone").getNumericValue().intValue());
        Assert.assertEquals(4, dataRows.get(3).getCell("problemZone").getNumericValue().intValue());

        Assert.assertEquals("предложение 1", dataRows.get(0).getCell("measures").getStringValue());
        Assert.assertEquals("предложение 2", dataRows.get(1).getCell("measures").getStringValue());
        Assert.assertEquals("предложение 3", dataRows.get(2).getCell("measures").getStringValue());
        Assert.assertEquals("предложение 4", dataRows.get(3).getCell("measures").getStringValue());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Assert.assertEquals("01.01.2014", sdf.format(dataRows.get(0).getCell("realizationDate").getDateValue()));
        Assert.assertEquals("02.01.2014", sdf.format(dataRows.get(1).getCell("realizationDate").getDateValue()));
        Assert.assertEquals("03.01.2014", sdf.format(dataRows.get(2).getCell("realizationDate").getDateValue()));
        Assert.assertEquals("04.01.2014", sdf.format(dataRows.get(3).getCell("realizationDate").getDateValue()));

        Assert.assertEquals(1, dataRows.get(0).getCell("performMark").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("performMark").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(2).getCell("performMark").getNumericValue().intValue());
        Assert.assertEquals(4, dataRows.get(3).getCell("performMark").getNumericValue().intValue());

        Assert.assertEquals("текст 1", dataRows.get(0).getCell("comments").getStringValue());
        Assert.assertEquals("текст 2", dataRows.get(1).getCell("comments").getStringValue());
        Assert.assertEquals("текст 3", dataRows.get(2).getCell("comments").getStringValue());
        Assert.assertEquals("текст 4", dataRows.get(3).getCell("comments").getStringValue());
    }
}
