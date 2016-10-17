package com.aplana.sbrf.taxaccounting.form_template.land.include_in_declaration.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Расчет земельного налога за отчетные периоды.
 */
public class IncludeInDeclarationTest extends ScriptTestBase {
    private static final int TYPE_ID = 917;
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
        return getDefaultScriptTestMockHelper(IncludeInDeclarationTest.class);
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
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        // ошибок быть не должно
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        mockProvider(705L);
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        int expected = 1; // в файле 1 строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // проверка значении
        checkValues(testHelper.getDataRowHelper().getAll());
    }

    // консолидация без кпп
    @Test
    public void composeNotKppTest() {
        int i = 0;
        int expected = 0;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        String msg = "Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью";
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // консолидация без кпп
    @Test
    public void composeTest() {
        mockProvider(710L);

        // вспомогательные данные источника
        FormType formType = new FormType();
        formType.setId(916);

        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relarion = new Relation();
        relarion.setFormDataId(1L);
        relarion.setFormType(formType);
        sourcesInfo.add(relarion);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceFormData = new FormData();
        when(testHelper.getFormDataService().get(anyLong(), isNull(Boolean.class))).thenReturn(sourceFormData);

        // данные НФ-источника, формируются импортом
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(testHelper.getDataRowHelper().getAll());
        testHelper.initRowData();
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);
        DataRow<Cell> sourceDataRow = sourceDataRowHelper.getAll().get(0);

        // консолидация должна пройти без проблем, 1 подходящая строка источника
        sourceDataRow.getCell("kpp").setValue("kppA710", sourceDataRow.getIndex());
        testHelper.execute(FormDataEvent.COMPOSE);
        int expected = 1;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // консолидация должна пройти без проблем, 1 подходящая строка источника
        sourceDataRow.getCell("kpp").setValue("fake_kpp", sourceDataRow.getIndex());
        testHelper.execute(FormDataEvent.COMPOSE);
        expected = 0;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // TODO (Ramil Timerbaev) добавить тест, что б выполнилась каждая логическая проверка
    // @Test
    public void check1Test() {
        testHelper.execute(FormDataEvent.CHECK);
    }

    void checkValues(List<DataRow<Cell>> dataRows) {
        long refbookRecordId = 1L;
        DataRow<Cell> row = dataRows.get(0);

        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("department").getValue());
        // графа 3
        Assert.assertEquals("8888", row.getCell("kno").getValue());
        // графа 4
        Assert.assertEquals("111111111", row.getCell("kpp").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("kbk").getValue());
        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("oktmo").getValue());
        // графа 7
        Assert.assertEquals("test7", row.getCell("cadastralNumber").getValue());
        // графа 8
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 9
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 10
        Assert.assertEquals(10, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 11
        Assert.assertEquals("1/1", row.getCell("taxPart").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("ownershipDate").getValue());
        // графа 13
        Assert.assertNotNull(row.getCell("terminationDate").getValue());
        // графа 14
        Assert.assertEquals(4L, row.getCell("period").getNumericValue().longValue());
        // графа 15
        Assert.assertEquals(refbookRecordId, row.getCell("benefitCode").getValue());
        // графа 16 - зависимая графа
        // графа 17 - зависимая графа
        // графа 18
        Assert.assertNotNull(row.getCell("startDate").getValue());
        // графа 19
        Assert.assertNotNull(row.getCell("endDate").getValue());
        // графа 20
        Assert.assertEquals(4L, row.getCell("benefitPeriod").getNumericValue().longValue());
        // графа 21
        Assert.assertEquals(2.5, row.getCell("taxRate").getNumericValue().doubleValue(), 4);
        // графа 22
        Assert.assertEquals(0.3333, row.getCell("kv").getNumericValue().doubleValue(), 4);
        // графа 23
        Assert.assertEquals(2.0, row.getCell("kl").getNumericValue().doubleValue(), 4);
        // графа 24
        Assert.assertEquals(-400L, row.getCell("sum").getNumericValue().longValue());
        // графа 25
        Assert.assertEquals(2833L, row.getCell("q1").getNumericValue().longValue());
        // графа 26
        Assert.assertEquals(2833L, row.getCell("q2").getNumericValue().longValue());
        // графа 27
        Assert.assertEquals(2833L, row.getCell("q3").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(-4766L, row.getCell("year").getNumericValue().longValue());
    }

    private void mockProvider(final Long refBookId) {
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);

        // вернуть все записи справочника
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(new Answer<PagingResult<Map<String, RefBookValue>>>() {
            @Override
            public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                result.addAll(testHelper.getRefBookAllRecords(refBookId).values());
                return result;
            }
        });
    }
}