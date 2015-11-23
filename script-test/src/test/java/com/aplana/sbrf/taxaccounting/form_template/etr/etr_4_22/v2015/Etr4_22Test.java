package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_22.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Профессиональное суждение (Приложение 4-22)
 */
public class Etr4_22Test extends ScriptTestBase {
    private static final int TYPE_ID = 722;
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
        return getDefaultScriptTestMockHelper(Etr4_22Test.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());
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

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 21; // в файле 21 строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {

        for (int i = 0; i < 21; i++) {
            Assert.assertEquals("+", dataRows.get(i).getCell("dynamics").getStringValue());
            Assert.assertEquals(String.valueOf(i + 1), dataRows.get(i).getCell("factors").getStringValue());
            // 1, 2, 3, 4 и опять 1, 2, 3...
            Assert.assertEquals((i % 4) + 1, dataRows.get(i).getCell("areas").getNumericValue().intValue());
            Assert.assertEquals(String.valueOf(i + 1), dataRows.get(i).getCell("offers").getStringValue());
            Assert.assertEquals(String.valueOf(i + 1), dataRows.get(i).getCell("offersCA").getStringValue());
            Assert.assertEquals(String.valueOf(i + 1), dataRows.get(i).getCell("other").getStringValue());

        }
    }
}
