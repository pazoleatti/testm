package com.aplana.sbrf.taxaccounting.form_template.land.include_in_declaration.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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

    // TODO (Ramil Timerbaev) пока нет возможности передать дополнительные параметры (https://jira.aplana.com/browse/SBRFACCTAX-17230)
    // @Test
    public void getSourcesTest() {
        testHelper.execute(FormDataEvent.GET_SOURCES);
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        mockProvider(96L);

        // текущий период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setOrder(4);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(reportPeriod);

        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // простая строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        setDefaultValues(row);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setIndex(2);
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);
        total2Row.getCell("q1").setValue(1, null);
        total2Row.getCell("q2").setValue(1, null);
        total2Row.getCell("q3").setValue(1, null);
        total2Row.getCell("year").setValue(1, null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setIndex(3);
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total1Row.getCell("q1").setValue(1, null);
        total1Row.getCell("q2").setValue(1, null);
        total1Row.getCell("q3").setValue(1, null);
        total1Row.getCell("year").setValue(1, null);
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setIndex(4);
        totalRow.setAlias("total");
        totalRow.getCell("q1").setValue(1, null);
        totalRow.getCell("q2").setValue(1, null);
        totalRow.getCell("q3").setValue(1, null);
        totalRow.getCell("year").setValue(1, null);
        dataRows.add(totalRow);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i;
        String msg;

        // успешное выполнение всех логических проверок
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
        testHelper.getLogger().clear();

        // 1. Проверка обязательности заполнения граф
        for (Column column : formData.getFormColumns()) {
            row.getCell(column.getAlias()).setValue(null, row.getIndex());
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // графа 1..8, 10, 12, 14, 21, 22, 25..28
        String [] nonEmptyColumns = { "rowNumber", "department", "kno", "kpp", "kbk", "oktmo", "cadastralNumber",
                "landCategory", "cadastralCost", "ownershipDate", "period", "taxRate", "kv", "q1", "q2", "q3", "year" };
        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);

        // 2. Проверка одновременного заполнения данных о налоговой льготе
        setDefaultValues(row);
        row.getCell("benefitCode").setValue(null, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Данные о налоговой льготе указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка корректности заполнения даты возникновения права собственности
        // 7. Проверка корректности заполнения даты начала действия льготы
        setDefaultValues(row);
        row.getCell("ownershipDate").setValue(sdf.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                row.getIndex(), row.getCell("ownershipDate").getColumn().getName(), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                row.getIndex(), row.getCell("startDate").getColumn().getName(), row.getCell("ownershipDate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка корректности заполнения даты прекращения права собственности
        setDefaultValues(row);
        row.getCell("terminationDate").setValue(sdf.parse("01.01.2013"), null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                row.getIndex(), row.getCell("terminationDate").getColumn().getName(), "01.01.2014", row.getCell("ownershipDate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5. Проверка доли налогоплательщика в праве на земельный участок
        // 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
        setDefaultValues(row);
        row.getCell("taxPart").setValue("1a/0", null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                    "«(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                    "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю",
                row.getIndex(), row.getCell("taxPart").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю",
                row.getIndex(), row.getCell("taxPart").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 8. Проверка корректности заполнения даты окончания действия льготы
        setDefaultValues(row);
        row.getCell("endDate").setValue(sdf.parse("01.01.2013"), null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s» и быть меньше либо равно значению графы «%s»",
                row.getIndex(), row.getCell("endDate").getColumn().getName(),
                row.getCell("startDate").getColumn().getName(), row.getCell("terminationDate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 9. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
        setDefaultValues(row);
        // дополнительная строка
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(1);
        setDefaultValues(row2);
        dataRows.add(row2);
        setDefaultValues(row2);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строки %s. Кадастровый номер земельного участка «%s», Код ОКТМО «%s»: на форме не должно быть строк с одинаковым кадастровым номером, кодом ОКТМО и пересекающимися периодами владения правом собственности",
                row.getIndex(), row.getCell("cadastralNumber").getValue(), "codeA96");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);

        // 10. Проверка корректности заполнения кода налоговой льготы (графа 15)
        setDefaultValues(row);
        row.getCell("oktmo").setValue(4L, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Код ОКТМО, в котором действует выбранная в графе «%s» льгота, должен быть равен значению графы «%s»",
                row.getIndex(), row.getCell("benefitCode").getColumn().getName(), row.getCell("oktmo").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11. Проверка корректности заполнения граф 14, 20, 22-28
        setDefaultValues(row);
        String [] calcColumns = { "period", "benefitPeriod", "kv", "kl", "sum", "q1", "q2", "q3", "year" };
        for (String alias : calcColumns) {
            row.getCell(alias).setValue(9L, null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        List<String> columnNames = new ArrayList<String>(calcColumns.length);
        for (String alias : calcColumns) {
            columnNames.add(row.getCell(alias).getColumn().getName());
        }
        String subMsg = StringUtils.join(columnNames.toArray(), "», «", null);
        msg = String.format("Строка %s: Графы «%s» заполнены неверно", row.getIndex(), subMsg);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 12. Проверка правильности заполнения КПП
        setDefaultValues(row);
        row.getCell("kpp").setValue("testKpp", null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Не найдено ни одного подразделения, для которого на форме настроек подразделений существует запись с КПП равным «%s»",
                row.getIndex(), row.getCell("kpp").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    private void setDefaultValues(DataRow<Cell> row) throws ParseException {
        Date date = sdf.parse("01.01.2014");
        // графа 1
        row.getCell("rowNumber").setValue(row.getIndex(), null);
        // графа 2
        row.getCell("department").setValue(1L, null);
        // графа 3
        row.getCell("kno").setValue("kno", null);
        // графа 4
        row.getCell("kpp").setValue("kppA710", null);
        // графа 5
        row.getCell("kbk").setValue(1L, null);
        // графа 6
        row.getCell("oktmo").setValue(1L, null);
        // графа 7
        row.getCell("cadastralNumber").setValue("cadastralNumber", null);
        // графа 8
        row.getCell("landCategory").setValue(1L, null);
        // графа 9
        row.getCell("constructionPhase").setValue(1L, null);
        // графа 10
        row.getCell("cadastralCost").setValue(100L, null);
        // графа 11
        row.getCell("taxPart").setValue(null, null);
        // графа 12
        row.getCell("ownershipDate").setValue(date, null);
        // графа 13
        row.getCell("terminationDate").setValue(null, null);
        // графа 14
        row.getCell("period").setValue(12L, null);
        // графа 15
        row.getCell("benefitCode").setValue(3L, null);
        // графа 16
        row.getCell("benefitBase").setValue(null, null);
        // графа 17
        row.getCell("benefitParam").setValue(null, null);
        // графа 18
        row.getCell("startDate").setValue(date, null);
        // графа 19
        row.getCell("endDate").setValue(null, null);
        // графа 20
        row.getCell("benefitPeriod").setValue(12L, null);
        // графа 21
        row.getCell("taxRate").setValue(9L, null);
        // графа 22
        row.getCell("kv").setValue(1L, null);
        // графа 23
        row.getCell("kl").setValue(0L, null);
        // графа 24
        row.getCell("sum").setValue(2L, null);
        // графа 25
        row.getCell("q1").setValue(4L, null);
        // графа 26
        row.getCell("q2").setValue(4L, null);
        // графа 27
        row.getCell("q3").setValue(4L, null);
        // графа 28
        row.getCell("year").setValue(4L, null);
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