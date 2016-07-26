package com.aplana.sbrf.taxaccounting.form_template.deal.journal_settlements_tb.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Журнал взаиморасчетов по ТБ.
 */
public class JournalSettlementsTbTest extends ScriptTestBase {
    private static final int TYPE_ID = 853;
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
        return getDefaultScriptTestMockHelper(JournalSettlementsTbTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        int size = testHelper.getDataRowHelper().getAll().size();
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        Assert.assertEquals(size + 1, testHelper.getDataRowHelper().getAll().size());
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
        Assert.assertEquals(size, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        // 1. Проверка заполнения обязательных полей
        testHelper.execute(FormDataEvent.CHECK);
        // должно быть много сообщении об незаполненности обязательных полей и неправильных итогов
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
        // ожижается 35 сообщении незаполненности и 36 сообщени неправильных итогов
        int expected = 35 + 36;
        Assert.assertEquals(expected, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка итоговых значений по разделам
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//journal_settlements_tb//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        for (DataRow<Cell> row : dataRows) {
            if (row.get("rowNum") != null && !"".equals(row.get("rowNum"))) {
                row.getCell("sum").setValue(0L, row.getIndex());
            }
        }

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(4);
        // графа 3
        row.getCell("statReportId").setValue(1L, null);
        // графа 5
        row.getCell("currency").setValue(1L, null);
        // графа 6
        row.getCell("currencySum").setValue(1L, null);
        // графа 7
        row.getCell("sum").setValue(1L, null);
        // графа 8
        row.getCell("otherSum").setValue(1L, null);

        dataRows.add(3, row);
        dataRows.get(2).getCell("sum").setValue(2L, null);
        dataRows.get(17).getCell("sum").setValue(2L, null);

        testHelper.execute(FormDataEvent.CHECK);

        int i = 0;
        int [] indexes = { 2, 17 };
        for (int index : indexes) {
            int rowIndex = dataRows.get(index).getIndex();
            String columnName = dataRows.get(i).getCell("sum").getColumn().getName();
            String msg = String.format("Строка %d: Итоговые значения рассчитаны неверно в графе «%s»!", rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // Расчет пустой (в импорте - расчет заполненной)
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        // провайдер для справочника 15
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), anyLong(),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(testHelper.getRefBookDataProvider());

        // вернуть  все записи справочника 15
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                result.addAll(testHelper.getRefBookAllRecords(15L).values());
                return result;
            }
        });

        // информация о справочнике 15
        RefBookAttribute refBookAttribute = new RefBookAttribute();
        refBookAttribute.setAlias("NAME");
        refBookAttribute.setName("testName");

        List<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
        attributes.add(refBookAttribute);

        RefBook refBook = new RefBook();
        refBook.setName("testName");
        refBook.setAttributes(attributes);
        when(testHelper.getRefBookFactory().get(anyLong())).thenReturn(refBook);

        int size = testHelper.getDataRowHelper().getAll().size();

        String name = "importFile.xls";
        testHelper.setImportFileInputStream(getCustomInputStream(name));
        testHelper.setImportFileName(name);
        testHelper.execute(FormDataEvent.IMPORT);

        // ожидается +2 строки из файла
        int expected = size + 2;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;

        // должен сожержать предупреждение о том что не найдена валюта в справочнике
        boolean hasWarn = entries.get(i++).getMessage().contains("не найдено значение «D»");
        Assert.assertTrue("must contain a warning about the currency", hasWarn);
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        // проверка расчетов
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        // проверка что есть сообщение о незаполненности графы с валютой
        String columnName = testHelper.getDataRowHelper().getAll().get(0).getCell("currency").getColumn().getName();
        String msg = String.format(ScriptUtils.WRONG_NON_EMPTY, 5, columnName);
        i = 0;
        boolean hasError = entries.get(i++).getMessage().contains(msg);
        Assert.assertTrue("must contain an error about empty currency", hasError);
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 7
        Assert.assertEquals(2L, dataRows.get(2).getCell("sum").getNumericValue().longValue());

        DataRow<Cell> row = dataRows.get(3);
        // графа 3
        Assert.assertEquals(1L, row.getCell("statReportId").getNumericValue().longValue());
        // графа 5
        Assert.assertEquals(1L, row.getCell("currency").getNumericValue().longValue());
        // графа 6
        Assert.assertEquals(182L, row.getCell("currencySum").getNumericValue().longValue());
        // графа 7
        Assert.assertEquals(1L, row.getCell("sum").getNumericValue().longValue());
        // графа 8
        Assert.assertEquals(183L, row.getCell("otherSum").getNumericValue().longValue());
        // графа 9
        Assert.assertEquals("test185", row.getCell("description").getStringValue());

        row = dataRows.get(4);
        // графа 3
        Assert.assertEquals(1L, row.getCell("statReportId").getNumericValue().longValue());
        // графа 5
        Assert.assertEquals(null, row.getCell("currency").getNumericValue());
        // графа 6
        Assert.assertEquals(182L, row.getCell("currencySum").getNumericValue().longValue());
        // графа 7
        Assert.assertEquals(1L, row.getCell("sum").getNumericValue().longValue());
        // графа 8
        Assert.assertEquals(183L, row.getCell("otherSum").getNumericValue().longValue());
        // графа 9
        Assert.assertEquals("test185", row.getCell("description").getStringValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2L, dataRows.get(2).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(4).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(18).getCell("sum").getNumericValue().longValue());

        for (DataRow<Cell> row : dataRows) {
            if (row.get("rowNum") != null && !"".equals(row.get("rowNum"))) {
                String msg = "row[" + row.getIndex() + "].sum";
                Assert.assertNotNull(msg, row.getCell("sum").getNumericValue());
            }
        }
    }
}