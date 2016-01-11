package com.aplana.sbrf.taxaccounting.form_template.deal.journal_settlements.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Журнал взаиморасчетов.
 */
public class JournalSettlementsTest extends ScriptTestBase {
    private static final int TYPE_ID = 807;
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
        return getDefaultScriptTestMockHelper(JournalSettlementsTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
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
        // 1. Проверка заполнения обязательных полей
        testHelper.execute(FormDataEvent.CHECK);
        // должно быть много сообщении об незаполненности обязательных полей и неправильных итогов
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
        // ожижается 35 сообщении незаполненности и 36 сообщени неправильных итогов
        int expected = 35 + 36;
        Assert.assertEquals(expected, testHelper.getLogger().getEntries().size());
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
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//journal_settlements//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        for (DataRow<Cell> row : dataRows) {
            if (row.get("fix1") != null && !"".equals(row.get("fix1"))) {
                row.getCell("sum").setValue(0L, row.getIndex());
            }
        }

        // для попадания в ЛП:
        // 2. Обязательное заполнение идентификационного кода первой стороны сделки
        // 4. Обязательное заполнение идентификационного кода второй стороны сделки
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(4);
        row.getCell("sum").setValue(1L, null);
        dataRows.add(3, row);
        dataRows.get(2).getCell("sum").setValue(1L, null);
        dataRows.get(17).getCell("sum").setValue(1L, null);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 4: Необходимо заполнить графу «№. Код подразделения» или графу «№. Код организации-участника»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 4: Необходимо заполнить графу «Код организации - участника группы (подразделения ПАО \"Сбербанк России\"), с которыми осуществляются взаиморасчеты. Код подразделения» или графу «Код организации - участника группы (подразделения ПАО \"Сбербанк России\"), с которыми осуществляются взаиморасчеты. Код организации-участника»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 3. Корректное указание данных первой стороны сделки
        // 5. Корректное указание данных второй стороны сделки
        row.getCell("sbrfCode1").setValue(1L, null);
        row.getCell("sbrfCode2").setValue(1L, null);
        row.getCell("statReportId1").setValue(1L, null);
        row.getCell("statReportId2").setValue(1L, null);

        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 4: Графа «№. Код подразделения» и графа «№. Код организации-участника» не могут быть одновременно заполнены!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 4: Графа «Код организации - участника группы (подразделения ПАО \"Сбербанк России\"), с которыми осуществляются взаиморасчеты. Код подразделения» и графа «Код организации - участника группы (подразделения ПАО \"Сбербанк России\"), с которыми осуществляются взаиморасчеты. Код организации-участника» не могут быть одновременно заполнены!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 6. Обязательность заполнения гр.3
        // 8. Обязательность заполнения гр.7
        row.getCell("sbrfCode1").setValue(4L, null);
        row.getCell("sbrfCode2").setValue(4L, null);
        row.getCell("statReportId1").setValue(null, null);
        row.getCell("statReportId2").setValue(null, null);

        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 4: Графа «Наименование статей. Наименование подразделения» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 4: Графа «Наименование организации – участника группы (подразделения ПАО \"Сбербанк России\"), с которым осуществляются взаиморасчеты. Наименование подразделения» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 7. Обязательность заполнения гр.4
        // 9. Обязательность заполнения гр.8
        row.getCell("sbrfCode1").setValue(null, null);
        row.getCell("sbrfCode2").setValue(null, null);
        row.getCell("statReportId1").setValue(4L, null);
        row.getCell("statReportId2").setValue(4L, null);

        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 4: Графа «Наименование статей. Наименование организации-участника» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 4: Графа «Наименование организации – участника группы (подразделения ПАО \"Сбербанк России\"), с которым осуществляются взаиморасчеты. Наименование организации-участника» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 10. Проверка итоговых значений по разделам
        row.getCell("sbrfCode1").setValue(1L, null);
        row.getCell("sbrfCode2").setValue(1L, null);
        row.getCell("statReportId1").setValue(null, null);
        row.getCell("statReportId2").setValue(null, null);
        dataRows.get(2).getCell("sum").setValue(2L, null);
        dataRows.get(17).getCell("sum").setValue(2L, null);

        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 3: Итоговые значения рассчитаны неверно в графе «Сумма доходов / расходов с начала года (тыс. руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 17: Итоговые значения рассчитаны неверно в графе «Сумма доходов / расходов с начала года (тыс. руб.)»!", entries.get(i++).getMessage());
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

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(3).getCell("sbrfCode1").getNumericValue().longValue());
        Assert.assertEquals(null, dataRows.get(4).getCell("sbrfCode1").getNumericValue());

        Assert.assertEquals(null, dataRows.get(3).getCell("statReportId1").getNumericValue());
        Assert.assertEquals(1L, dataRows.get(4).getCell("statReportId1").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(3).getCell("sbrfCode2").getNumericValue().longValue());
        Assert.assertEquals(null, dataRows.get(4).getCell("sbrfCode2").getNumericValue());

        Assert.assertEquals(1L, dataRows.get(4).getCell("statReportId2").getNumericValue().longValue());
        Assert.assertEquals(null, dataRows.get(3).getCell("statReportId2").getNumericValue());

        Assert.assertEquals(2L, dataRows.get(2).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(4).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(18).getCell("sum").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2L, dataRows.get(2).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(4).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(18).getCell("sum").getNumericValue().longValue());

        for (DataRow<Cell> row : dataRows) {
            if (row.get("fix1") != null && !"".equals(row.get("fix1"))) {
                String msg = "row.sum[" + row.getIndex() + "]";
                Assert.assertNotNull(msg, row.getCell("sum").getNumericValue());
            }
        }
    }
}