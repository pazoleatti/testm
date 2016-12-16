package com.aplana.sbrf.taxaccounting.form_template.transport.summary.v2016;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Расчет суммы налога по каждому транспортному средству.
 *
 * TODO:
 *      - доделать тесты для ЛП в расчетах
 *      - добавить тесты для консолидации и предконослидационных проверок
 *      - добавить тесты для сравнения и копирования
 */
public class SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 200;
    private static final int SOURCE_TYPE_ID_V = 201;
    private static final int SOURCE_TYPE_ID_B = 202;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

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
    public void mockBefore() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
    }

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        // должна быть ошибка что нет итогов
        int i = 0;
        String msg = "Итоговые значения рассчитаны неверно!";
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        mockProviders(4L, 6L, 7L, 8L, 31L, 41L, 210L, 310L);

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

        // простая строка 1
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        setDefaultValues(row);
        dataRows.add(row);

        // простая строка 2 (для некоторых проверок)
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setIndex(2);
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("okato").setValue(row.getCell("okato").getValue(), null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setIndex(3);
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setIndex(4);
        totalRow.setAlias("total");
        dataRows.add(totalRow);

        String [] totalColumns = { "taxSumToPay", "q1", "q2", "q3", "q4" };
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        total2Row.getCell("kno").setRefBookDereference(row.getCell("kno").getRefBookDereference());
        total2Row.getCell("kpp").setRefBookDereference(row.getCell("kpp").getRefBookDereference());
        total2Row.getCell("okato").setRefBookDereference(row.getCell("okato").getRefBookDereference());
        total1Row.getCell("kno").setRefBookDereference(row.getCell("kno").getRefBookDereference());
        total1Row.getCell("kpp").setRefBookDereference(row.getCell("kpp").getRefBookDereference());

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i;
        String msg;
        String subMsg;

        // успешное выполнение всех логических проверок
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
        testHelper.getLogger().clear();

        // 2. Проверка обязательности заполнения граф
        for (Column column : formData.getFormColumns()) {
            row.getCell(column.getAlias()).setValue(null, row.getIndex());
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // графа 1..7, 9..11, 13..20, 22, 30, 31 (графа 32..34 обязательны для некоторых периодов)
        String [] nonEmptyColumns = { "kno", "okato", "tsTypeCode", "model", "vi", "regNumber", "regDate",
                "taxBase", "taxBaseOkeiUnit", "createYear", "years", "ownMonths", "partRight", "coefKv",
                "taxRate", "calculatedTaxSum", "taxSumToPay", "q1", "q2", "q3", "q4" };
        for (String alias : nonEmptyColumns) {
            String columnName = ScriptUtils.getColumnName(row, alias);
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        testHelper.getLogger().clear();

        // 3. Проверка наличия параметров представления декларации для кода ОКТМО
        // Выполняется при расчете, в методе calc2()

        // 4. Проверка на наличие в форме строк с одинаковым значением граф 9, 10 и пересекающимися периодами владения
        // 5. Проверка на наличие в форме строк с одинаковым значением граф 4, 5, 9, 10, 13, 14 и пересекающимися периодами владения
        setDefaultValues(row);
        setDefaultValues(row2);
        dataRows.add(1, row2);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getNumericValue().longValue() + row2.getCell(alias).getNumericValue().longValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getNumericValue().longValue() + row2.getCell(alias).getNumericValue().longValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getNumericValue().longValue() + row2.getCell(alias).getNumericValue().longValue(), null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // 4.
        String [] columns = { "vi", "regNumber" };
        for (String alias : columns) {
            msg = String.format("Строки %s: На форме не должно быть строк с одинаковым значением графы «%s» («%s») и пересекающимися периодами владения ТС",
                    "1, 2", ScriptUtils.getColumnName(row, alias), row2.getCell(alias).getValue());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        // 5.a
        msg = String.format("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                "Регистрационный знак «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, " +
                "регистрационным знаком ТС, налоговой базой, единицей измерения налоговой базы по ОКЕИ и пересекающимися периодами владения ТС",
                "1, 2", "codeA96", "codeA42", row.getCell("vi").getValue(), row.getCell("regNumber").getValue(),
                row.getCell("taxBase").getValue(), "codeA12");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 6. Проверка корректности заполнения даты регистрации ТС
        setDefaultValues(row);
        row.getCell("regDate").setValue(format.parse("01.01.2015"), null);
        row.getCell("q1").setValue(0L, null);
        row.getCell("q2").setValue(0L, null);
        row.getCell("q3").setValue(0L, null);
        row.getCell("q4").setValue(83330L, null);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                row.getIndex(), ScriptUtils.getColumnName(row, "regDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 7. Проверка корректности заполнения даты снятия с регистрации ТС
        setDefaultValues(row);
        row.getCell("regDateEnd").setValue(format.parse("01.01.2013"), null);
        row.getCell("q1").setValue(0L, null);
        row.getCell("q2").setValue(0L, null);
        row.getCell("q3").setValue(0L, null);
        row.getCell("q4").setValue(83330L, null);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "regDateEnd"), "01.01.2014", ScriptUtils.getColumnName(row, "regDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 8. Проверка корректности заполнения года выпуска ТС
        setDefaultValues(row);
        row.getCell("createYear").setValue(format.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "createYear"), "2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 9. Проверка корректности заполнения расчетных граф 2, 19, 20, 22, 24, 27, 30-34
        setDefaultValues(row);
        String [] calcColumns = { /* "kno", */ "coefKv", "taxRate", "calculatedTaxSum", "coefKl", "taxBenefitSum", "taxSumToPay", "q1", "q2", "q3", "q4" };
        for (String alias : calcColumns) {
            row.getCell(alias).setValue(-1L, null);
        }
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        List<String> columnNames = new ArrayList<String>(calcColumns.length);
        for (String alias : calcColumns) {
            columnNames.add(ScriptUtils.getColumnName(row, alias));
        }
        subMsg = StringUtils.join(columnNames.toArray(), "», «", null);
        msg = String.format("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", row.getIndex(), subMsg);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 10. Проверка корректности заполнения доли налогоплательщика в праве на ТС
        setDefaultValues(row);
        row.getCell("partRight").setValue("fake", null);
        row.getCell("calculatedTaxSum").setValue(null, null);
        row.getCell("taxBenefitSum").setValue(null, null);
        row.getCell("taxSumToPay").setValue(null, null);
        row.getCell("q1").setValue(0L, null);
        row.getCell("q2").setValue(0L, null);
        row.getCell("q3").setValue(0L, null);
        row.getCell("q4").setValue(0L, null);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        total2Row.getCell("taxSumToPay").setValue(0L, null);
        total1Row.getCell("taxSumToPay").setValue(0L, null);
        totalRow.getCell("taxSumToPay").setValue(0L, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // побочные проверки
        // графа 22, 30
        String [] emptyColumns = { "calculatedTaxSum", "taxSumToPay" };
        for (String alias : emptyColumns) {
            String columnName = ScriptUtils.getColumnName(row, alias);
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        // основная проверка
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                "«(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», " +
                "числитель должен быть меньше либо равен знаменателю, " +
                "числитель и знаменатель не должны быть равны нулю",
                row.getIndex(), ScriptUtils.getColumnName(row, "partRight"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        msg = String.format("Строка %s: Данные о налоговой льготе указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 11. Проверка наличия ставки ТС в справочнике
        // Выполняется при расчете, в методе calc20()

        // 12. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется при расчете

        // 13. Проверка одновременного заполнения данных о налоговой льготе
        setDefaultValues(row);
        row.getCell("taxBenefitCode").setValue(null, null);
        row.getCell("taxBenefitSum").setValue(null, null);
        row.getCell("taxSumToPay").setValue(100000, null);
        row.getCell("q1").setValue(25000L, null);
        row.getCell("q2").setValue(25000L, null);
        row.getCell("q3").setValue(25000L, null);
        row.getCell("q4").setValue(25000L, null);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // основная проверка
        msg = String.format("Строка %s: Данные о налоговой льготе указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 14. Проверка одновременного заполнения данных о налоговом вычете
        setDefaultValues(row);
        row.getCell("deductionCode").setValue(1L, null);
        row.getCell("q1").setValue(0L, null);
        row.getCell("q2").setValue(0L, null);
        row.getCell("q3").setValue(0L, null);
        row.getCell("q4").setValue(83330L, null);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // основная проверка
        msg = String.format("Строка %s: Данные о налоговом вычете указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 15. Проверка заполнения формы настроек подразделений
        mockProvider(310L, false);
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // основная проверка
        msg = String.format("Строки %s: На форме настроек подразделений для подразделения «%s» " +
                "отсутствует запись с «Код налогового органа (кон.) = %s» и «КПП = %s»",
                row.getIndex(), "test department name", "taxOrganCodeA210", "kppA210");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        mockProvider(310L, true);

        // 16.1 Проверка корректности значений итоговых строк (строка "итого")
        setDefaultValues(row);
        dataRows.remove(total2Row);
        dataRows.remove(total1Row);
        dataRows.remove(totalRow);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        subMsg = ScriptUtils.getColumnName(row, "kno") + "=" + row.getCell("kno").getRefBookDereference() + ", " +
                ScriptUtils.getColumnName(row, "kpp") + "=" + row.getCell("kpp").getRefBookDereference() + ", " +
                ScriptUtils.getColumnName(row, "okato") + "=" + row.getCell("okato").getRefBookDereference();
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG, subMsg);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        subMsg = ScriptUtils.getColumnName(row, "kno") + "=" + row.getCell("kno").getRefBookDereference() + ", " +
                ScriptUtils.getColumnName(row, "kpp") + "=" + row.getCell("kpp").getRefBookDereference();
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG, subMsg);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        dataRows.add(total2Row);
        dataRows.add(total1Row);
        dataRows.add(totalRow);

        // 16.2 Проверка корректности значений итоговых строк (строка "итого")
        setDefaultValues(row);
        columnNames.clear();
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(-100, null);
            total1Row.getCell(alias).setValue(-100, null);
            totalRow.getCell(alias).setValue(-100, null);
            columnNames.add(ScriptUtils.getColumnName(row, alias));
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        subMsg = StringUtils.join(columnNames.toArray(), "», «", null);
        int [] rowIndexes = { total2Row.getIndex(), total1Row.getIndex(), totalRow.getIndex() };
        for (int index : rowIndexes) {
            msg = String.format("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", index, subMsg);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

        // 16.3 Проверка корректности значений итоговых строк (строка "итого")
        DataRow<Cell> tmpTotal2Row = formData.createDataRow();
        tmpTotal2Row.setIndex(6);
        tmpTotal2Row.setAlias("total2#tmp");
        dataRows.add(tmpTotal2Row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG_ROW, tmpTotal2Row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        dataRows.remove(tmpTotal2Row);

        // 17. Проверка разрядности значений граф 30-34, рассчитываемых в итоговых строках
        // Выполняется при расчете, в методе checkOverflowLocal()

        // 18. Проверка корректности заполнения кода налогового органа
        setDefaultValues(row);
        row.getCell("okato").setValue(3L, null);
        total2Row.getCell("okato").setValue(row.getCell("okato").getValue(), null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // основная проверка
        msg = String.format("Строка %s: значение графы «%s» (%s) должно быть равно значению поля «Код ОКТМО» и " +
                        "соответствовать значению поля «Код региона РФ» записи справочника, выбранной в графе 2",
                row.getIndex(), ScriptUtils.getColumnName(row, "okato"), "codeC96");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        total2Row.getCell("okato").setValue(row.getCell("okato").getValue(), null);

        // 19. Проверка наличия информации в форме-источнике вида «Сведения о ТС»
        // Выполняется при расчете, в методе getEqualsRowFromVehicles()

        // 20. Проверка наличия информации в форме-источнике вида «Сведения о льготируемых ТС»
        // Выполняется при расчете, в методе getEqualsRowsFromBenefit()

        // 21. Проверка наличия формы-источника вида «Сведения о ТС» в состоянии «Принята»
        // 22. Проверка наличия формы-источника вида «Сведения о льготируемых ТС» в состоянии «Принята»
        // Выполняются при расчете, в методе getSourceRowsMap()
    }

    @Test
    public void calcTest() throws ParseException {
        mockPrevData();

        testHelper.execute(FormDataEvent.CALCULATE);
        int expected = 1; // одна итоговая строка
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(i, entries.size());
    }

    // Проверка с данными
    @Test
    public void calcAndCheck1Test() throws ParseException {
        DataRowHelper dataRowHelper = mockPrevData();
        mockProviders(4L, 6L, 7L, 8L, 31L, 41L, 210L, 310L);
        Map<Integer, DataRowHelper> sourceHelperMap = mockSources(true);
        DataRow<Cell> rowV = sourceHelperMap.get(SOURCE_TYPE_ID_V).getAllSaved().get(0);
        DataRow<Cell> rowB = sourceHelperMap.get(SOURCE_TYPE_ID_B).getAllSaved().get(0);

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

        // простая строка 1
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        setDefaultValues(row);
        dataRows.add(row);

        rowV.getCell("identNumber").setValue(row.getCell("vi").getValue(), null);
        rowV.getCell("regNumber").setValue(row.getCell("regNumber").getValue(), null);
        rowV.getCell("regDate").setValue(row.getCell("regDate").getValue(), null);

        rowB.getCell("codeOKATO").setValue(row.getCell("okato").getValue(), null);
        rowB.getCell("tsTypeCode").setValue(row.getCell("tsTypeCode").getValue(), null);
        rowB.getCell("identNumber").setValue(row.getCell("vi").getValue(), null);
        rowB.getCell("regNumber").setValue(row.getCell("regNumber").getValue(), null);
        rowB.getCell("benefitStartDate").setValue(row.getCell("regDate").getValue(), null);
        rowB.getCell("benefitEndDate").setValue(row.getCell("regDateEnd").getValue(), null);

        // простая строка 2 (для некоторых проверок)
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setIndex(2);
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("okato").setValue(row.getCell("okato").getValue(), null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setIndex(3);
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setIndex(4);
        totalRow.setAlias("total");
        dataRows.add(totalRow);

        String [] totalColumns = { "taxSumToPay", "q1", "q2", "q3", "q4" };
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        total2Row.getCell("kno").setRefBookDereference(row.getCell("kno").getRefBookDereference());
        total2Row.getCell("kpp").setRefBookDereference(row.getCell("kpp").getRefBookDereference());
        total2Row.getCell("okato").setRefBookDereference(row.getCell("okato").getRefBookDereference());
        total1Row.getCell("kno").setRefBookDereference(row.getCell("kno").getRefBookDereference());
        total1Row.getCell("kpp").setRefBookDereference(row.getCell("kpp").getRefBookDereference());

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i;
        String msg;

        // успешное выполнение всех логических проверок
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CALCULATE);

        checkLogger();
        // графа 2
        Assert.assertEquals(1L, row.getCell("kno").getValue());
        // графа 19
        Assert.assertEquals(1.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertEquals(4L, row.getCell("taxRate").getValue());
        // графа 22
        Assert.assertEquals(100000L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(0.1667, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 27
        Assert.assertEquals(16670L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(83330L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(0L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(0L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(0L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(83330, row.getCell("q4").getNumericValue().longValue());
        testHelper.getLogger().clear();

        // Логическая проверка 3. Проверка наличия параметров представления декларации для кода ОКТМО
        setDefaultValues(row);
        row.getCell("kno").setValue(null, null);
        row.getCell("okato").setValue(2L, null);
        rowB.getCell("codeOKATO").setValue(row.getCell("okato").getValue(), null);
        testHelper.execute(FormDataEvent.CALCULATE);
        i = 0;
        msg = String.format("Строка %s: Графа «%s» = «%s»: В справочнике " +
                "«Параметры представления деклараций по транспортному налогу» %s на дату %s, в которой " +
                "поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», поле «Код ОКТМО» = «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "okato"), "codeB96",
                "отсутствует запись, актуальная", "31.12.2014",
                "01", "test department name", "01", "codeB96");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), ScriptUtils.getColumnName(row, "kno"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        rowB.getCell("codeOKATO").setValue(row.getCell("okato").getValue(), null);

        // Логическая проверка 11. Проверка наличия ставки ТС в справочнике
        setDefaultValues(row);
        row.getCell("taxBase").setValue(1000000, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        i = 0;
        msg = String.format("Строка %s: Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: " +
                "В справочнике «Ставки транспортного налога» отсутствует запись, актуальная, на дату %s, " +
                "в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», " +
                "поле «Код вида ТС» = «%s», поле «Ед. измерения мощности» = «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "okato"), "codeA96",
                ScriptUtils.getColumnName(row, "tsTypeCode"), "codeA42",
                ScriptUtils.getColumnName(row, "taxBaseOkeiUnit"), "codeA12",
                "31.12.2014", "01", "test department name", "01", "codeA42", "codeA12");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочные проверки (графа 20, 22, 27)
        String [] nonEmptyColumns = { "taxRate", "calculatedTaxSum", "taxSumToPay" };
        for (String alias : nonEmptyColumns) {
            String columnName = ScriptUtils.getColumnName(row, alias);
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // Логическая проверка 12. Проверка наличия формы предыдущего периода в состоянии «Принята»
        setDefaultValues(row);
        List<DataRow<Cell>> tmp = dataRowHelper.getAllSaved();
        dataRowHelper.setAllCached(null);
        testHelper.execute(FormDataEvent.CALCULATE);
        i = 0;
        // побочная проверка
        // Проверка при сравнении 1. Проверка наличия формы за предыдущий период в состоянии «Принята»
        msg = String.format("Не удалось сравнить данные формы с данными формы за предыдущий период. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: %s %s для подразделения «%s»",
                "test period", "2014", "test department name");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // основная проверка
        msg = String.format("Данные граф «%s», «%s» не были скопированы из формы предыдущего периода. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                ScriptUtils.getColumnName(row, "kno"), ScriptUtils.getColumnName(row, "kpp"),
                "test period", "2014", "test department name");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRowHelper.setAllCached(tmp);

        // Логическая проверка 17. Проверка разрядности значений граф 30-34, рассчитываемых в итоговых строках
        setDefaultValues(row);
        setDefaultValues(row2);
        row.getCell("taxBase").setValue(999999999999L, null);
        row2.getCell("taxBase").setValue(999999999999L, null);
        total2Row.setIndex(3);
        total1Row.setIndex(4);
        totalRow.setIndex(5);
        dataRows.add(1, row2);
        testHelper.execute(FormDataEvent.CALCULATE);
        i = 0;
        // основная проверка
        String [] columns = { "taxSumToPay", "q4" };
        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        rows.add(total2Row);
        rows.add(total1Row);
        rows.add(totalRow);
        for (DataRow<Cell> tmpRow : rows) {
            for (String alias : columns) {
                msg = String.format("Строка %s: Значение графы «%s» превышает допустимую разрядность (%s знаков)",
                        tmpRow.getIndex(), ScriptUtils.getColumnName(tmpRow, alias), 15);
                Assert.assertEquals(msg, entries.get(i++).getMessage());
            }
        }
        // побочные проверки
        String [] columns2 = { "vi", "regNumber" };
        for (String alias : columns2) {
            msg = String.format("Строки %s: На форме не должно быть строк с одинаковым значением графы «%s» («%s») и пересекающимися периодами владения ТС",
                    "1, 2", ScriptUtils.getColumnName(row, alias), row2.getCell(alias).getValue());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        msg = String.format("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                "Регистрационный знак «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, " +
                "регистрационным знаком ТС, налоговой базой, единицей измерения налоговой базы по ОКЕИ и пересекающимися периодами владения ТС",
                "1, 2", "codeA96", "codeA42", row.getCell("vi").getValue(), row.getCell("regNumber").getValue(),
                row.getCell("taxBase").getValue(), "codeA12");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
        total2Row.setIndex(2);
        total1Row.setIndex(3);
        totalRow.setIndex(4);

        // TODO (Ramil Timerbaev) доделать
        // Логическая проверка 19. Проверка наличия информации в форме-источнике вида «Сведения о ТС»
        // Логическая проверка 20. Проверка наличия информации в форме-источнике вида «Сведения о льготируемых ТС»
        // Логическая проверка 21. Проверка наличия формы-источника вида «Сведения о ТС» в состоянии «Принята»
        // Логическая проверка 22. Проверка наличия формы-источника вида «Сведения о льготируемых ТС» в состоянии «Принята»
    }

    @Test
    public void composeTest() throws ParseException {
        mockPrevData();

        testHelper.execute(FormDataEvent.COMPOSE);
        int expected = 1; // одна итоговая строка
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void sortRows1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // простая строка 1
        DataRow<Cell> row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue(2, null);
        row.getCell("kno").setRefBookDereference("kno2");
        row.getCell("model").setValue("model2", null);
        dataRows.add(row);

        // простая строка 2
        row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue(2, null);
        row.getCell("kno").setRefBookDereference("kno2");
        row.getCell("model").setValue("model1", null);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО для строки 1 и 2
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kno").setRefBookDereference("kno2");
        total2Row.getCell("okato").setValue(row.getCell("okato").getValue(), null);
        total2Row.getCell("okato").setRefBookDereference("okato2");
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО для строки 1 и 2
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kno").setRefBookDereference("kno2");
        dataRows.add(total1Row);

        // простая строка 3
        row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue(1, null);
        row.getCell("kno").setRefBookDereference("kno1");
        row.getCell("model").setValue("model3", null);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО для строки 3
        total2Row = formData.createDataRow();
        total2Row.setAlias("total2#2");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kno").setRefBookDereference("kno1");
        total2Row.getCell("okato").setValue(row.getCell("okato").getValue(), null);
        total2Row.getCell("okato").setRefBookDereference("okato1");
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО для строки 3
        total1Row = formData.createDataRow();
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kno").setRefBookDereference("kno1");
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setAlias("total");
        dataRows.add(totalRow);

        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();

        int expected = 8;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        row = testHelper.getDataRowHelper().getAll().get(0);
        Assert.assertEquals("model3", row.getCell("model").getStringValue());
        row = testHelper.getDataRowHelper().getAll().get(3);
        Assert.assertEquals("model1", row.getCell("model").getStringValue());
        row = testHelper.getDataRowHelper().getAll().get(4);
        Assert.assertEquals("model2", row.getCell("model").getStringValue());
    }

    @Test
    public void importExcelTest() {
        mockProviders(4L, 6L, 7L, 41L, 210L);
        int expected = 2; // в файле 1 строка + 1 итоговая
        String fileName = "importFile.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("kno").getValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertEquals(refbookRecordId, row.getCell("okato").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("tsTypeCode").getValue());
        // графа 6 - зависимая графа
        // графа 7
        Assert.assertEquals("test7", row.getCell("model").getValue());
        // графа 8
        Assert.assertEquals(3L, row.getCell("ecoClass").getValue());
        // графа 9
        Assert.assertEquals("test9", row.getCell("vi").getValue());
        // графа 10
        Assert.assertEquals("test10", row.getCell("regNumber").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 13
        Assert.assertEquals(5.00, row.getCell("taxBase").getNumericValue().doubleValue(), 2);
        // графа 14
        Assert.assertEquals(refbookRecordId, row.getCell("taxBaseOkeiUnit").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("createYear").getValue());
        // графа 16
        Assert.assertEquals(2L, row.getCell("years").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("ownMonths").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals("1/2", row.getCell("partRight").getValue());
        // графа 19
        Assert.assertEquals(2.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertEquals(refbookRecordId, row.getCell("taxRate").getValue());
        // графа 21
        Assert.assertEquals(refbookRecordId, row.getCell("coefKp").getValue());
        // графа 22
        Assert.assertEquals(22L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 23
        Assert.assertEquals(3L, row.getCell("benefitMonths").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(1.0000, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 25
        Assert.assertEquals(refbookRecordId, row.getCell("taxBenefitCode").getValue());
        // графа 26 - зависимая графа
        // графа 27
        Assert.assertEquals(27L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(refbookRecordId, row.getCell("deductionCode").getValue());
        // графа 29
        Assert.assertEquals(29L, row.getCell("deductionSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(30L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(31L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(32L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(33L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(34L, row.getCell("q4").getNumericValue().longValue());
    }

    @Test
    public void importMsg2_4aTest() {
        mockProviders(4L, 6L, 7L, 41L, 210L);
        int expected = 2; // в файле 1 строка + 1 итоговая
        String fileName = "importFile2.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 2
        Assert.assertNull(row.getCell("kno").getValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertNull(row.getCell("okato").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("tsTypeCode").getValue());
        // графа 6 - зависимая графа
        // графа 7
        Assert.assertEquals("test7", row.getCell("model").getValue());
        // графа 8
        Assert.assertEquals(3L, row.getCell("ecoClass").getValue());
        // графа 9
        Assert.assertEquals("test9", row.getCell("vi").getValue());
        // графа 10
        Assert.assertEquals("test10", row.getCell("regNumber").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 13
        Assert.assertEquals(5.00, row.getCell("taxBase").getNumericValue().doubleValue(), 2);
        // графа 14
        Assert.assertEquals(refbookRecordId, row.getCell("taxBaseOkeiUnit").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("createYear").getValue());
        // графа 16
        Assert.assertEquals(2L, row.getCell("years").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("ownMonths").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals("1/2", row.getCell("partRight").getValue());
        // графа 19
        Assert.assertEquals(2.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertNull(row.getCell("taxRate").getValue());
        // графа 21
        Assert.assertEquals(refbookRecordId, row.getCell("coefKp").getValue());
        // графа 22
        Assert.assertEquals(22L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 23
        Assert.assertEquals(3L, row.getCell("benefitMonths").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(1.0000, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 25
        Assert.assertNull(row.getCell("taxBenefitCode").getValue());
        // графа 26 - зависимая графа
        // графа 27
        Assert.assertEquals(27L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(refbookRecordId, row.getCell("deductionCode").getValue());
        // графа 29
        Assert.assertEquals(29L, row.getCell("deductionSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(30L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(31L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(32L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(33L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(34L, row.getCell("q4").getNumericValue().longValue());

        // проверка сообщении
        // 2. Проверка заполнения кода ОКТМО - перенес сюда что б не нарушалась последовательность вывода сообщений
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графы «%s», «%s», «%s», «%s», «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(5),
                ScriptUtils.getColumnName(row, "kno"), ScriptUtils.getColumnName(row, "kpp"),
                ScriptUtils.getColumnName(row, "taxRate"), ScriptUtils.getColumnName(row, "taxBenefitCode"),
                ScriptUtils.getColumnName(row, "taxBenefitBase"), ScriptUtils.getColumnName(row, "okato"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        // 4.a Проверка заполнения обязательных граф для выбора налоговой ставки
        msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнены графы «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(21),
                ScriptUtils.getColumnName(row, "taxRate"), ScriptUtils.getColumnName(row, "okato"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void importMsg3BTest() {
        mockProviders(4L, 6L, 7L, 41L, 210L);
        int expected = 2; // в файле 1 строка + 1 итоговая
        String fileName = "importFile3b.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 2
        Assert.assertNull(row.getCell("kno").getValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertEquals(refbookRecordId, row.getCell("okato").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("tsTypeCode").getValue());
        // графа 6 - зависимая графа
        // графа 7
        Assert.assertEquals("test7", row.getCell("model").getValue());
        // графа 8
        Assert.assertEquals(3L, row.getCell("ecoClass").getValue());
        // графа 9
        Assert.assertEquals("test9", row.getCell("vi").getValue());
        // графа 10
        Assert.assertEquals("test10", row.getCell("regNumber").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 13
        Assert.assertEquals(5.00, row.getCell("taxBase").getNumericValue().doubleValue(), 2);
        // графа 14
        Assert.assertEquals(refbookRecordId, row.getCell("taxBaseOkeiUnit").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("createYear").getValue());
        // графа 16
        Assert.assertEquals(2L, row.getCell("years").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("ownMonths").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals("1/2", row.getCell("partRight").getValue());
        // графа 19
        Assert.assertEquals(2.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertEquals(refbookRecordId, row.getCell("taxRate").getValue());
        // графа 21
        Assert.assertEquals(refbookRecordId, row.getCell("coefKp").getValue());
        // графа 22
        Assert.assertEquals(22L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 23
        Assert.assertEquals(3L, row.getCell("benefitMonths").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(1.0000, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 25
        Assert.assertEquals(refbookRecordId, row.getCell("taxBenefitCode").getValue());
        // графа 26 - зависимая графа
        // графа 27
        Assert.assertEquals(27L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(refbookRecordId, row.getCell("deductionCode").getValue());
        // графа 29
        Assert.assertEquals(29L, row.getCell("deductionSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(30L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(31L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(32L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(33L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(34L, row.getCell("q4").getNumericValue().longValue());

        // проверка сообщении
        // 3.b В справочнике «Параметры представления декларации по транспортному налогу» не заполнены графы 2, 3 в файле
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графы «%s», «%s», т.к. не заполнены графы «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(3),
                ScriptUtils.getColumnName(row, "kno"), ScriptUtils.getColumnName(row, "kpp"),
                ScriptUtils.getColumnName(row, "kno"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void importMsg3ATest() {
        mockProviders(4L, 6L, 7L, 41L, 210L);
        int expected = 2; // в файле 1 строка + 1 итоговая
        String fileName = "importFile3a.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 2
        Assert.assertNull(row.getCell("kno").getValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertEquals(refbookRecordId, row.getCell("okato").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("tsTypeCode").getValue());
        // графа 6 - зависимая графа
        // графа 7
        Assert.assertEquals("test7", row.getCell("model").getValue());
        // графа 8
        Assert.assertEquals(3L, row.getCell("ecoClass").getValue());
        // графа 9
        Assert.assertEquals("test9", row.getCell("vi").getValue());
        // графа 10
        Assert.assertEquals("test10", row.getCell("regNumber").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 13
        Assert.assertEquals(5.00, row.getCell("taxBase").getNumericValue().doubleValue(), 2);
        // графа 14
        Assert.assertEquals(refbookRecordId, row.getCell("taxBaseOkeiUnit").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("createYear").getValue());
        // графа 16
        Assert.assertEquals(2L, row.getCell("years").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("ownMonths").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals("1/2", row.getCell("partRight").getValue());
        // графа 19
        Assert.assertEquals(2.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertEquals(refbookRecordId, row.getCell("taxRate").getValue());
        // графа 21
        Assert.assertEquals(refbookRecordId, row.getCell("coefKp").getValue());
        // графа 22
        Assert.assertEquals(22L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 23
        Assert.assertEquals(3L, row.getCell("benefitMonths").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(1.0000, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 25
        Assert.assertEquals(refbookRecordId, row.getCell("taxBenefitCode").getValue());
        // графа 26 - зависимая графа
        // графа 27
        Assert.assertEquals(27L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(refbookRecordId, row.getCell("deductionCode").getValue());
        // графа 29
        Assert.assertEquals(29L, row.getCell("deductionSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(30L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(31L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(32L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(33L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(34L, row.getCell("q4").getNumericValue().longValue());

        // проверка сообщении
        // 3.a В справочнике «Параметры представления декларации по транспортному налогу» не найдена запись
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графы: «%s», «%s», т.к. в справочнике " +
                "«Параметры представления деклараций по транспортному налогу» отсутствует запись, актуальная на дату %s, в которой " +
                "поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», " +
                "поле «Код ОКТМО» = «%s», поле «Код налогового органа (кон.)» = «%s», поле «КПП» = «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(3),
                ScriptUtils.getColumnName(row, "kno"), ScriptUtils.getColumnName(row, "kpp"),
                "31.12.2014", "01", "test department name", "01", "codeA96", "taxOrganCodeA210", "fakeKpp");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void importMsg4BTest() {
        mockProviders(4L, 6L, 7L, 41L, 210L);
        int expected = 2; // в файле 1 строка + 1 итоговая
        String fileName = "importFile4b.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("kno").getValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertEquals(refbookRecordId, row.getCell("okato").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("tsTypeCode").getValue());
        // графа 6 - зависимая графа
        // графа 7
        Assert.assertEquals("test7", row.getCell("model").getValue());
        // графа 8
        Assert.assertEquals(3L, row.getCell("ecoClass").getValue());
        // графа 9
        Assert.assertEquals("test9", row.getCell("vi").getValue());
        // графа 10
        Assert.assertEquals("test10", row.getCell("regNumber").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 13
        Assert.assertEquals(5.00, row.getCell("taxBase").getNumericValue().doubleValue(), 2);
        // графа 14
        Assert.assertEquals(4L, row.getCell("taxBaseOkeiUnit").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("createYear").getValue());
        // графа 16
        Assert.assertEquals(2L, row.getCell("years").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("ownMonths").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals("1/2", row.getCell("partRight").getValue());
        // графа 19
        Assert.assertEquals(2.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertNull(row.getCell("taxRate").getValue());
        // графа 21
        Assert.assertEquals(refbookRecordId, row.getCell("coefKp").getValue());
        // графа 22
        Assert.assertEquals(22L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 23
        Assert.assertEquals(3L, row.getCell("benefitMonths").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(1.0000, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 25
        Assert.assertEquals(refbookRecordId, row.getCell("taxBenefitCode").getValue());
        // графа 26 - зависимая графа
        // графа 27
        Assert.assertEquals(27L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(refbookRecordId, row.getCell("deductionCode").getValue());
        // графа 29
        Assert.assertEquals(29L, row.getCell("deductionSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(30L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(31L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(32L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(33L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(34L, row.getCell("q4").getNumericValue().longValue());

        // проверка сообщении
        // 4.b Нет записи в справочнике
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                "«Ставки транспортного налога» отсутствует запись, актуальная, на дату %s, в которой " +
                "поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», " +
                "поле «Код региона РФ» = «%s», поле «Код вида ТС» = «%s», " +
                "поле «Ед. измерения мощности» = «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(21), ScriptUtils.getColumnName(row, "taxRate"),
                "31.12.2014", "01", "test department name", "01", "codeA42", "codeD12");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void importExcelMsg5Test() {
        mockProviders(4L, 6L, 7L, 41L, 210L);
        int expected = 2; // в файле 1 строка + 1 итоговая
        String fileName = "importFile5.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);

        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("kno").getValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertEquals(refbookRecordId, row.getCell("okato").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("tsTypeCode").getValue());
        // графа 6 - зависимая графа
        // графа 7
        Assert.assertEquals("test7", row.getCell("model").getValue());
        // графа 8
        Assert.assertEquals(3L, row.getCell("ecoClass").getValue());
        // графа 9
        Assert.assertEquals("test9", row.getCell("vi").getValue());
        // графа 10
        Assert.assertEquals("test10", row.getCell("regNumber").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 13
        Assert.assertEquals(5.00, row.getCell("taxBase").getNumericValue().doubleValue(), 2);
        // графа 14
        Assert.assertEquals(refbookRecordId, row.getCell("taxBaseOkeiUnit").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("createYear").getValue());
        // графа 16
        Assert.assertEquals(2L, row.getCell("years").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("ownMonths").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals("1/2", row.getCell("partRight").getValue());
        // графа 19
        Assert.assertEquals(2.0000, row.getCell("coefKv").getNumericValue().doubleValue(), 4);
        // графа 20
        Assert.assertEquals(refbookRecordId, row.getCell("taxRate").getValue());
        // графа 21
        Assert.assertEquals(refbookRecordId, row.getCell("coefKp").getValue());
        // графа 22
        Assert.assertEquals(22L, row.getCell("calculatedTaxSum").getNumericValue().longValue());
        // графа 23
        Assert.assertEquals(3L, row.getCell("benefitMonths").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(1.0000, row.getCell("coefKl").getNumericValue().doubleValue(), 4);
        // графа 25
        Assert.assertNull(row.getCell("taxBenefitCode").getValue());
        // графа 26 - зависимая графа
        // графа 27
        Assert.assertEquals(27L, row.getCell("taxBenefitSum").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(refbookRecordId, row.getCell("deductionCode").getValue());
        // графа 29
        Assert.assertEquals(29L, row.getCell("deductionSum").getNumericValue().longValue());
        // графа 30
        Assert.assertEquals(30L, row.getCell("taxSumToPay").getNumericValue().longValue());
        // графа 31
        Assert.assertEquals(31L, row.getCell("q1").getNumericValue().longValue());
        // графа 32
        Assert.assertEquals(32L, row.getCell("q2").getNumericValue().longValue());
        // графа 33
        Assert.assertEquals(33L, row.getCell("q3").getNumericValue().longValue());
        // графа 34
        Assert.assertEquals(34L, row.getCell("q4").getNumericValue().longValue());

        // проверка сообщении
        // 5. Проверка наличия информации о налоговой льготе в справочнике «Параметры налоговых льгот транспортного налога»
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графы «%s», «%s», т.к. в справочнике " +
                "«Параметры налоговых льгот транспортного налога» отсутствует запись, актуальная на дату %s, " +
                "в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», " +
                "поле «Код налоговой льготы» = «%s», поле «Основание» = «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(26),
                ScriptUtils.getColumnName(row, "taxBenefitCode"), ScriptUtils.getColumnName(row, "taxBenefitBase"),
                "31.12.2014", "01", "test department name", "01", "codeA6", "fakeValue");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    private void setDefaultValues(DataRow<Cell> row) throws ParseException {
        long refbookRecordId = 1L;
        long number = 1L;
        Date date = format.parse("01.01.2014");
        String str = "test";

        // графа 1
        // row.getCell("rowNumber").setValue(row.getIndex(), null);
        // графа 2
        row.getCell("kno").setValue(refbookRecordId, null);
        row.getCell("kno").setRefBookDereference("taxOrganCodeA210");
        // графа 3 - зависимая
        row.getCell("kpp").setRefBookDereference("kppA210");
        // графа 4
        row.getCell("okato").setValue(refbookRecordId, null);
        row.getCell("okato").setRefBookDereference("codeA96");
        // графа 5
        row.getCell("tsTypeCode").setValue(refbookRecordId, null);
        // графа 6 - зависимая
        // графа 7
        row.getCell("model").setValue(str, null);
        // графа 8
        row.getCell("ecoClass").setValue(refbookRecordId, null);
        // графа 9
        row.getCell("vi").setValue(str, null);
        // графа 10
        row.getCell("regNumber").setValue(str, null);
        // графа 11
        row.getCell("regDate").setValue(date, null);
        // графа 12
        row.getCell("regDateEnd").setValue(null, null);
        // графа 13
        row.getCell("taxBase").setValue(1000, null);
        // графа 14
        row.getCell("taxBaseOkeiUnit").setValue(refbookRecordId, null);
        // графа 15
        row.getCell("createYear").setValue(date, null);
        // графа 16
        row.getCell("years").setValue(number, null);
        // графа 17
        row.getCell("ownMonths").setValue(12, null);
        // графа 18
        row.getCell("partRight").setValue("1/1", null);
        // графа 19
        row.getCell("coefKv").setValue(1, null);
        // графа 20
        row.getCell("taxRate").setValue(4L, null);
        // графа 21
        row.getCell("coefKp").setValue(3, null);
        // графа 22
        row.getCell("calculatedTaxSum").setValue(100000, null);
        // графа 23
        row.getCell("benefitMonths").setValue(2, null);
        // графа 24
        row.getCell("coefKl").setValue(0.1667, null);
        // графа 25
        row.getCell("taxBenefitCode").setValue(4L, null);
        // графа 26 - зависимая
        // графа 27
        row.getCell("taxBenefitSum").setValue(16670, null);
        // графа 28
        row.getCell("deductionCode").setValue(null, null);
        // графа 29
        row.getCell("deductionSum").setValue(null, null);
        // графа 30
        row.getCell("taxSumToPay").setValue(83330, null);
        // графа 31
        row.getCell("q1").setValue(25000, null);
        // графа 32
        row.getCell("q2").setValue(25000, null);
        // графа 33
        row.getCell("q3").setValue(25000, null);
        // графа 34
        row.getCell("q4").setValue(8330, null);
    }

    private DataRowHelper mockPrevData() throws ParseException {
        // провайдер для периодов
        mockProvider(8L, true);

        // текущий период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setOrder(4);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(reportPeriod);

        // предыдущий период
        TaxPeriod taxPeriodPrev = new TaxPeriod();
        taxPeriodPrev.setYear(2014);
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setOrder(3);
        prevReportPeriod.setName("test period");
        prevReportPeriod.setTaxPeriod(taxPeriodPrev);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        FormData prevFormData = new FormData();
        prevFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(prevFormData);

        // строки и хелпер источника
        prevFormData.initFormTemplateParams(testHelper.getFormTemplate());
        DataRow<Cell> row = prevFormData.createDataRow();
        setDefaultValues(row);
        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        rows.add(row);
        DataRowHelper dataRowHelper = new DataRowHelperStub();
        dataRowHelper.save(rows);
        dataRowHelper.setAllCached(rows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(prevFormData))).thenReturn(dataRowHelper);
        return dataRowHelper;
    }

    private void mockProviders(Long ... refBookIds) {
        for (Long refBookId : refBookIds) {
            mockProvider(refBookId, true);
        }
    }

    private void mockProvider(final Long refBookId, final boolean enable) {
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
                if (enable) {
                    result.addAll(testHelper.getRefBookAllRecords(refBookId).values());
                }
                return result;
            }
        });

        if (enable) {
            // вернуть все записи справочника
            Map<Long, Map<String, RefBookValue>> refBookAllRecords = testHelper.getRefBookAllRecords(refBookId);
            ArrayList<Long> recordIds = new ArrayList<Long>(refBookAllRecords.keySet());
            when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
            when(provider.getRecordData(eq(recordIds))).thenReturn(refBookAllRecords);
        } else {
            ArrayList<Long> recordIds = new ArrayList<Long>();
            when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
            when(provider.getRecordData(eq(recordIds))).thenReturn(null);
        }

    }

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();

    static {
        formTypeIdByTemplateIdMap.put(SOURCE_TYPE_ID_V, SOURCE_TYPE_ID_V); // Сведения о транспортных средствах, по которым уплачивается транспортный налог
        formTypeIdByTemplateIdMap.put(SOURCE_TYPE_ID_B, SOURCE_TYPE_ID_B); // Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();

    static {
        templatesPathMap.put(SOURCE_TYPE_ID_V, "..//src/main//resources//form_template//transport//vehicles//v2016//");
        templatesPathMap.put(SOURCE_TYPE_ID_B, "..//src/main//resources//form_template//transport//benefit_vehicles//v2016//");
    }

    private Map<Integer, DataRowHelper> mockSources(boolean enable) {
        Map<Integer, DataRowHelper> result = new HashMap<Integer, DataRowHelper>();
        // вспомогательные данные источника
        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        FormType formType = new FormType();
        formType.setId(SOURCE_TYPE_ID_V);
        Relation relarion = new Relation();
        relarion.setFormDataId((long) SOURCE_TYPE_ID_V);
        relarion.setFormType(formType);
        sourcesInfo.add(relarion);
        FormType formType2 = new FormType();
        formType2.setId(SOURCE_TYPE_ID_B);
        Relation relarion2 = new Relation();
        relarion2.setFormDataId((long) SOURCE_TYPE_ID_B);
        relarion2.setFormType(formType2);
        sourcesInfo.add(relarion2);

        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(enable ? sourcesInfo : null);

        Set<Integer> sourceTemplateIds = formTypeIdByTemplateIdMap.keySet();
        FormDataKind kind = FormDataKind.PRIMARY;
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(enable ? sourceFormData : null);
            when(testHelper.getFormDataService().get(eq(sourceFormData.getId().longValue()), anyBoolean())).thenReturn(enable ? sourceFormData : null);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            sourceDataRowHelper.setAllCached(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(enable ? sourceDataRowHelper : null);

            result.put(sourceTemplateId, sourceDataRowHelper);
        }
        return result;
    }

    /**
     * Получить форму источника.
     *
     * @param id               идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.TRANSPORT);
        formType.setName(sourceTemplate.getName());

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId((long) id);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        sourceFormData.setFormTemplateId(sourceTemplateId);

        return sourceFormData;
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData   форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "t" + sourceTemplateId;
        Date testDate = new Date();
        Long testLong = 1L;
        Long testRefbookId = 2L;
        DataRow<Cell> row = sourceFormData.createDataRow();
        dataRows.add(row);
        DataRow<Cell> row2 = sourceFormData.createDataRow();
        dataRows.add(row2);
        return dataRows;
    }
}