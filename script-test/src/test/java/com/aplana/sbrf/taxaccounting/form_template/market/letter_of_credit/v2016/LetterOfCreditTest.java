package com.aplana.sbrf.taxaccounting.form_template.market.letter_of_credit.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Данные по непокрытым аккредитивам
 */
public class LetterOfCreditTest extends ScriptTestBase {
    private static final int TYPE_ID = 913;
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
        return getDefaultScriptTestMockHelper(LetterOfCreditTest.class);
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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка заполнения граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Вид Продукта (гарантия / непокрытый аккредитив)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Наименование контрагента и ОПФ"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "ИНН / КИО клиента"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата подписания Договора / ГенСоглашения"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номер обязательства (референс)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата выдачи обязательства"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата окончания действия обязательства"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма обязательства (в ед. валюты)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Валюта выдачи"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Срок в днях"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Кредитный рейтинг контрагента / класс кредитоспособности"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номинальная ставка (в % или в абсолютном выражении) платы за выданный аккредитив / гарантию"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номинальная ставка (в % годовых или в абсолютном выражении) плата за подтверждение платежа по аккредитиву"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "График платежей по уплате комиссий"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Наличие обеспечения / поручительства (Да / Нет)"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 2. Положительность графы
        // 3. Неотрицательность графы
        // 4. Проверка даты выдачи обязательства
        // 5. Дата выдачи обязательства должна попадать в отчетный период
        // 6. Проверка валюты
        row.getCell("productType").setValue("string0", null);
        row.getCell("name").setValue("string1", null);
        row.getCell("innKio").setValue("string2", null);
        row.getCell("docDate").setValue(sdf.parse("04.01.2990"), null);
        row.getCell("docNumber").setValue("string3", null);
        row.getCell("creditDate").setValue(sdf.parse("03.01.2990"), null);
        row.getCell("creditEndDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("sum").setValue(0L, null);
        row.getCell("currency").setValue(1L, null);
        row.getCell("period").setValue(-1L, null);
        row.getCell("creditRating").setValue(1L, null);
        row.getCell("faceValueStr").setValue("string4", null);
        row.getCell("faceValueNum").setValue(-1L, null);
        row.getCell("paymentSchedule").setValue("string5", null);
        row.getCell("sign").setValue(1L, null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Сумма обязательства (в ед. валюты)» должно быть больше 0!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Срок в днях» должно быть больше либо равно 0!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Номинальная ставка (в % годовых или в абсолютном выражении) плата за подтверждение платежа по аккредитиву» должно быть больше либо равно 0!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата окончания действия обязательства» должно быть больше либо равно значению графы «Дата выдачи обязательства»!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата выдачи обязательства", "01.01.2014", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для российского рубля должно быть проставлено буквенное значение RUR!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 7. Проверка на отсутствие нескольких записей по одном и тому же непокрытому аккредитиву
        row.getCell("sum").setValue(1L, null);
        row.getCell("period").setValue(1L, null);
        row.getCell("faceValueNum").setValue(0L, null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2014"), null);
        row.getCell("creditDate").setValue(sdf.parse("03.01.2014"), null);
        row.getCell("creditEndDate").setValue(sdf.parse("04.01.2014"), null);
        row.getCell("currency").setValue(2L, null);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CALCULATE);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: На форме уже существует строка со значениями граф «ИНН / КИО клиента» = «string2», " +
                "«Номер обязательства (референс)» = «string3», «Дата выдачи обязательства» = «03.01.2014»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        dataRows.remove(1);
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().getEntries().isEmpty());
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 1;

        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        // ошибок быть не должно
        checkLogger();
        // Количество строк должно увеличиться на 1
        Assert.assertEquals("Add new row", expected, testHelper.getDataRowHelper().getAll().size());

        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        // Количество строк должно уменьшиться на 1
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        expected--;
        // Количество строк должно уменьшиться на 1
        Assert.assertEquals("Delete row", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        FormTemplate formTemplate = testHelper.getFormTemplate();
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(formTemplate);

        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        List<String> aliases = Arrays.asList("productType", "name", "innKio", "docDate", "docNumber", "creditDate",
                "creditEndDate", "sum", "period", "faceValueStr", "faceValueNum", "paymentSchedule");
        defaultCheckLoadData(aliases, expected);
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("country").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("country").getNumericValue().longValue());

        Assert.assertEquals(3L, dataRows.get(0).getCell("currency").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("currency").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("creditRating").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("creditRating").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("sign").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("sign").getNumericValue().longValue());
    }
}