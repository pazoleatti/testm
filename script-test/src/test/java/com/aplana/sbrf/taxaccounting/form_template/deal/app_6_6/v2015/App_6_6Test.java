package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_6.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 6.2. Размещение средств на межбанковском рынке
 */
public class App_6_6Test extends ScriptTestBase {
    private static final int TYPE_ID = 806;
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
        return getDefaultScriptTestMockHelper(App_6_6Test.class);
    }

    @Before
    public void mockServices() {
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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_6_6//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка на заполнение графы
        // 2. Проверка возможности заполнения режима переговорных сделок
        // 4. Проверка возможности заполнения даты совершения сделки
        // 6. Заполнение граф 13 и 14 (сумма дохода, расхода)
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Полное наименование с указанием ОПФ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата (заключения) сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата исполнения 1-ой части сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата исполнения 2-ой части сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена 1-ой части сделки, ед. валюты» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код валюты расчетов по сделке» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Курс ЦБ РФ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена 1-ой части сделки, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Выполнение расчета графы «Режим переговорных сделок» невозможно, так как не заполнена используемая в расчете графа «Код страны регистрации по классификатору ОКСМ»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Выполнение расчета графы «Дата совершения сделки» невозможно, так как не заполнена используемая в расчете графа «Дата исполнения 2-ой части сделки»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма процентного расхода (руб.)» должна быть заполнена, если не заполнена графа «Сумма процентного дохода (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 2. Проверка возможности заполнения режима переговорных сделок
        // 5. Проверка даты совершения сделки
        // 6. Заполнение граф 13 и 14 (сумма дохода, расхода)
        // 8. Корректность даты (заключения) сделки
        // 9. Корректность даты исполнения 1–ой части сделки (проверка даты окончания периода)
        // 11. Корректность даты совершения сделки
        // 12. Проверка диапазона дат
        row.getCell("name").setValue(123L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2989"), null);
        row.getCell("date1").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("date2").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("priceFirstCurrency").setValue(2, null);
        row.getCell("currencyCode").setValue(1L, null);
        row.getCell("courseCB").setValue(4, null);
        row.getCell("priceFirstRub").setValue(5, null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Выполнение расчета графы «Режим переговорных сделок» невозможно, так как не заполнена используемая в расчете графа «Код страны регистрации по классификатору ОКСМ»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения сделки» заполнена неверно!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма процентного расхода (руб.)» должна быть заполнена, если не заполнена графа «Сумма процентного дохода (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата (заключения) сделки» должно быть не меньше значения графы «Дата договора»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата исполнения 1-ой части сделки» не может быть больше даты окончания отчётного периода!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата (заключения) сделки»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение даты атрибута «Дата договора» должно принимать значение из следующего диапазона: 01.01.1900 - 31.12.2099", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 6. Заполнение граф 13 и 14 (сумма дохода, расхода)
        // 10. Корректность даты исполнения 1–ой части сделки (проверка даты начала периода)
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("02.01.2014"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("04.01.2014"), null);
        row.getCell("date1").setValue(sdf.parse("03.01.2004"), null);
        row.getCell("date2").setValue(sdf.parse("04.01.2014"), null);
        row.getCell("priceFirstCurrency").setValue(2, null);
        row.getCell("incomeSum").setValue(2, null);
        row.getCell("outcomeSum").setValue(2, null);
        row.getCell("currencyCode").setValue(1L, null);
        row.getCell("courseCB").setValue(4, null);
        row.getCell("priceFirstRub").setValue(5, null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Выполнение расчета графы «Режим переговорных сделок» невозможно, так как не заполнена используемая в расчете графа «Код страны регистрации по классификатору ОКСМ»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма процентного расхода (руб.)» не может быть заполнена одновременно с графой «Сумма процентного дохода (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата исполнения 1-ой части сделки» не может быть меньше даты начала отчётного периода!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();


        // для попадания в ЛП:
        // 7. Проверка положительной суммы дохода/расхода
        row.getCell("incomeSum").setValue(0, null);
        row.getCell("outcomeSum").setValue(null, null);
        row.getCell("date1").setValue(sdf.parse("03.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Выполнение расчета графы «Режим переговорных сделок» невозможно, так как не заполнена используемая в расчете графа «Код страны регистрации по классификатору ОКСМ»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма процентного дохода (руб.)» должно быть больше значения «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 7. Проверка положительной суммы дохода/расхода
        row.getCell("incomeSum").setValue(null, null);
        row.getCell("outcomeSum").setValue(0, null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Выполнение расчета графы «Режим переговорных сделок» невозможно, так как не заполнена используемая в расчете графа «Код страны регистрации по классификатору ОКСМ»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма процентного расхода (руб.)» должно быть больше значения «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("outcomeSum").setValue(1, null);
        testHelper.execute(FormDataEvent.CHECK);

        Assert.assertEquals(1, testHelper.getLogger().getEntries().size());
    }

    // Расчет пустой (в импорте - растчет заполненной)
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void importExcelTest() throws ParseException {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<String> aliases = Arrays.asList("docNumber", "docDate", "dealNumber", "dealDate", "date1", "date2",
                "incomeSum", "outcomeSum", "priceFirstCurrency", "courseCB", "priceFirstRub", "dealDoneDate");
        defaultCheckLoadData(aliases, 4);
        checkLogger();
        // "name" , "dealsMode", "currencyCode"
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("name").getNumericValue().longValue());

        Assert.assertEquals("", dataRows.get(0).getCell("dealsMode").getStringValue());
        Assert.assertEquals("Да", dataRows.get(1).getCell("dealsMode").getStringValue());
        Assert.assertEquals("", dataRows.get(2).getCell("dealsMode").getStringValue());
        Assert.assertEquals("Да", dataRows.get(3).getCell("dealsMode").getStringValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(2).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("currencyCode").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) throws ParseException {
        Assert.assertEquals(null, dataRows.get(0).getCell("dealsMode").getStringValue());
        Assert.assertEquals(null, dataRows.get(1).getCell("dealsMode").getStringValue());
        Assert.assertEquals(null, dataRows.get(2).getCell("dealsMode").getStringValue());
        Assert.assertEquals(null, dataRows.get(3).getCell("dealsMode").getStringValue());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Assert.assertEquals(sdf.parse("31.12.2014"), dataRows.get(0).getCell("dealDoneDate").getDateValue());
        Assert.assertEquals(sdf.parse("31.12.2014"), dataRows.get(1).getCell("dealDoneDate").getDateValue());
        Assert.assertEquals(sdf.parse("31.12.2014"), dataRows.get(2).getCell("dealDoneDate").getDateValue());
        Assert.assertEquals(sdf.parse("31.12.2014"), dataRows.get(3).getCell("dealDoneDate").getDateValue());
    }
}

