package com.aplana.sbrf.taxaccounting.form_template.land.calc_tax_period.v2016;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Расчет земельного налога за отчетные периоды.
 * TODO:
 *      - расчет пустой
 *      - расчет непустой
 */
public class CalcTaxPeriodTest extends ScriptTestBase {
    private static final int TYPE_ID = 916;
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
        return getDefaultScriptTestMockHelper(CalcTaxPeriodTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
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
        // должна быть ошибка что нет итогов
        int i = 0;
        String msg = "Итоговые значения рассчитаны неверно!";
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        mockProvider(96L);
        mockProvider(705L);
        mockProvider(710L);

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
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setIndex(3);
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setIndex(4);
        totalRow.setAlias("total");
        dataRows.add(totalRow);

        String [] totalColumns = { "q1", "q2", "q3", "year" };
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            total1Row.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }

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
        // графа 1..8, 10, 12, 14, 21, 22, 25..28
        String [] nonEmptyColumns = { "rowNumber", "department", "kno", "kpp", "kbk", "oktmo", "cadastralNumber",
                "landCategory", "cadastralCost", "ownershipDate", "period", "taxRate", "kv", "q1" /*, "q2", "q3", "year"*/};
        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        testHelper.getLogger().clear();
        setDefaultValues(row);

        // 3. Проверка одновременного заполнения данных о налоговой льготе
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

        // 4. Проверка корректности заполнения даты возникновения права собственности
        // 8. Проверка корректности заполнения даты начала действия льготы
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

        // 5. Проверка корректности заполнения даты прекращения права собственности
        setDefaultValues(row);
        Date date = sdf.parse("01.01.2013");
        row.getCell("terminationDate").setValue(date, null);
        row.getCell("ownershipDate").setValue(date, null);
        row.getCell("startDate").setValue(date, null);
        row.getCell("endDate").setValue(date, null);
        row.getCell("benefitPeriod").setValue(0, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                row.getIndex(), row.getCell("terminationDate").getColumn().getName(), "01.01.2014", row.getCell("ownershipDate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка доли налогоплательщика в праве на земельный участок
        // 7. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
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

        // 9. Проверка корректности заполнения даты окончания действия льготы
        setDefaultValues(row);
        row.getCell("terminationDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("endDate").setValue(sdf.parse("01.01.2013"), null);
        row.getCell("benefitPeriod").setValue(0, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена. Значение графы должно быть больше либо равно значению графы «%s» и быть меньше либо равно значению графы «%s»",
                row.getIndex(), row.getCell("endDate").getColumn().getName(),
                row.getCell("startDate").getColumn().getName(), row.getCell("terminationDate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        setDefaultValues(row);
        row.getCell("endDate").setValue(sdf.parse("01.01.2013"), null);
        row.getCell("benefitPeriod").setValue(0, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                row.getIndex(), row.getCell("endDate").getColumn().getName(), row.getCell("startDate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // побочная проверка
        Assert.assertTrue(entries.get(i++).getMessage().contains("заполнены неверно"));
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 9. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется после консолидации, перед копированием данных, в методе copyFromPrevForm()

        // 10. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется после консолидации, перед копированием данных, в методе copyFromPrevForm()

        // 11. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется после расчетов, перед сравнением данных, в методе comparePrevRows()

        // 12. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
        setDefaultValues(row);
        // дополнительная строка
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(1);
        setDefaultValues(row2);
        dataRows.add(row2);
        setDefaultValues(row2);
        totalRow.getCell("q1").setValue(row.getCell("q1").getNumericValue().longValue() + row2.getCell("q1").getNumericValue().longValue(), null);
        totalRow.getCell("q2").setValue(row.getCell("q2").getNumericValue().longValue() + row2.getCell("q2").getNumericValue().longValue(), null);
        totalRow.getCell("q3").setValue(row.getCell("q3").getNumericValue().longValue() + row2.getCell("q3").getNumericValue().longValue(), null);
        totalRow.getCell("year").setValue(row.getCell("year").getNumericValue().longValue() + row2.getCell("year").getNumericValue().longValue(), null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строки %s. Кадастровый номер земельного участка «%s», Код ОКТМО «%s»: на форме не должно быть строк с одинаковым кадастровым номером, кодом ОКТМО и пересекающимися периодами владения правом собственности",
                row.getIndex(), row.getCell("cadastralNumber").getValue(), "codeA96");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
        totalRow.getCell("q1").setValue(row.getCell("q1").getValue(), null);
        totalRow.getCell("q2").setValue(row.getCell("q2").getValue(), null);
        totalRow.getCell("q3").setValue(row.getCell("q3").getValue(), null);
        totalRow.getCell("year").setValue(row.getCell("year").getValue(), null);

        // 13. Проверка корректности заполнения кода налоговой льготы (графа 15)
        setDefaultValues(row);
        row.getCell("oktmo").setValue(4L, null);
        total2Row.getCell("oktmo").setValue(4L, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Код ОКТМО, в котором действует выбранная в графе «%s» льгота, должен быть равен значению графы «%s»",
                row.getIndex(), row.getCell("benefitCode").getColumn().getName(), row.getCell("oktmo").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        setDefaultValues(row);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);

        // 14. Проверка корректности заполнения граф 14, 20, 22-28
        setDefaultValues(row);
        String [] calcColumns = { "period", /* "benefitPeriod", */ "kv", "kl", "sum", "q1", "q2", "q3", "year" };
        for (String alias : calcColumns) {
            row.getCell(alias).setValue(9L, null);
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
            columnNames.add(row.getCell(alias).getColumn().getName());
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

        // TODO (Ramil Timerbaev)
        // 15. Проверка заполнения формы настроек подразделений
//        setDefaultValues(row);
//        row.getCell("kpp").setValue("testKpp", null);
//        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
//        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
//        testHelper.execute(FormDataEvent.CHECK);
//        i = 0;
//        msg = String.format("Строки %s: На форме настроек подразделений отсутствует запись с «Код налогового органа (кон.) = %s» и «КПП = %s»",
//                "1, 2", row.getCell("kno").getValue(), row.getCell("kpp").getValue());
//        Assert.assertEquals(msg, entries.get(i++).getMessage());
//        Assert.assertEquals(i, entries.size());
//        testHelper.getLogger().clear();
//        setDefaultValues(row);
//        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
//        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);

        // 16. Проверка корректности значения пониженной ставки
        // Выполняется в методе calc24() при расчете

        // 17. Проверка корректности значения налоговой базы
        // Выполняется в методе getB() при расчете

        // 18. Проверка корректности суммы исчисленного налога и суммы налоговой льготы
        // Выполняется в методе getH() при расчете

        // 19.1 Проверка корректности значений итоговых строк (нет строки ВСЕГО и подитговых строк)
        setDefaultValues(row);
        dataRows.remove(total2Row);
        dataRows.remove(total1Row);
        dataRows.remove(totalRow);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Группа «КНО=kno, КПП=kppA710, Код ОКТМО=codeA96» не имеет строки итога!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «КНО=kno, КПП=kppA710» не имеет строки итога!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        dataRows.add(total2Row);
        dataRows.add(total1Row);
        dataRows.add(totalRow);

        // 19.2 Проверка корректности значений итоговых строк (ошибка в суммах ВСЕГО и в подитогах)
        setDefaultValues(row);
        for (String alias : totalColumns) {
            total2Row.getCell(alias).setValue(0, null);
            total1Row.getCell(alias).setValue(0, null);
            totalRow.getCell(alias).setValue(0, null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        subMsg = String.format("%s», «%s», «%s», «%s", row.getCell("q1").getColumn().getName(),
                row.getCell("q2").getColumn().getName(), row.getCell("q3").getColumn().getName(), row.getCell("year").getColumn().getName());
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

        // 19.3 Проверка корректности значений итоговых строк (лишний подитог)
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
    }

    @Test
    public void sortRowsTest() throws ParseException {
        mockProvider(96L);

        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // простая строка 1
        DataRow<Cell> row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue("kno2", null);
        row.getCell("cadastralNumber").setValue("cadastralNumber2", null);
        dataRows.add(row);

        // простая строка 2
        row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue("kno2", null);
        row.getCell("cadastralNumber").setValue("cadastralNumber1", null);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО для строки 1 и 2
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);
        total2Row.getCell("q1").setValue(1, null);
        total2Row.getCell("q2").setValue(1, null);
        total2Row.getCell("q3").setValue(1, null);
        total2Row.getCell("year").setValue(1, null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО для строки 1 и 2
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total1Row.getCell("q1").setValue(1, null);
        total1Row.getCell("q2").setValue(1, null);
        total1Row.getCell("q3").setValue(1, null);
        total1Row.getCell("year").setValue(1, null);
        dataRows.add(total1Row);

        // простая строка 3
        row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue("kno1", null);
        row.getCell("cadastralNumber").setValue("cadastralNumber3", null);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО для строки 3
        total2Row = formData.createDataRow();
        total2Row.setAlias("total2#2");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);
        total2Row.getCell("q1").setValue(1, null);
        total2Row.getCell("q2").setValue(1, null);
        total2Row.getCell("q3").setValue(1, null);
        total2Row.getCell("year").setValue(1, null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО для строки 3
        total1Row = formData.createDataRow();
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
        totalRow.setAlias("total");
        totalRow.getCell("q1").setValue(1, null);
        totalRow.getCell("q2").setValue(1, null);
        totalRow.getCell("q3").setValue(1, null);
        totalRow.getCell("year").setValue(1, null);
        dataRows.add(totalRow);

        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();

        int expected = 8;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        row = testHelper.getDataRowHelper().getAll().get(0);
        Assert.assertEquals("cadastralNumber3", row.getCell("cadastralNumber").getStringValue());
        row = testHelper.getDataRowHelper().getAll().get(3);
        Assert.assertEquals("cadastralNumber1", row.getCell("cadastralNumber").getStringValue());
        row = testHelper.getDataRowHelper().getAll().get(4);
        Assert.assertEquals("cadastralNumber2", row.getCell("cadastralNumber").getStringValue());
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

    @Test
    public void importTest() {
        mockProvider(705L);
        int expected = 1 + 3; // в файле 1 простая строка и 3 итоговых
        String fileName = "importFile.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        // TODO (Ramil Timerbaev)
        int count = 0;
        for (LogEntry log : testHelper.getLogger().getEntries()) {
            count++;
            System.out.println(count + " - " + log.getLevel().name() + " >>>>>> " + log.getMessage());
        }
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("department").getValue());
        // графа 3
        Assert.assertEquals("9999", row.getCell("kno").getValue());
        // графа 4
        Assert.assertEquals("111111111", row.getCell("kpp").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("kbk").getValue());
        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("oktmo").getValue());
        // графа 7
        Assert.assertEquals("тест7", row.getCell("cadastralNumber").getValue());
        // графа 8
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 9
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 10
        Assert.assertEquals(10L, row.getCell("cadastralCost").getNumericValue().longValue());
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
        Assert.assertEquals(2.5, row.getCell("taxRate").getNumericValue().doubleValue(), 0);
        // графа 22
        Assert.assertEquals(0.3333, row.getCell("kv").getNumericValue().doubleValue(), 0);
        // графа 23
        Assert.assertEquals(2L, row.getCell("kl").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(-400L, row.getCell("sum").getNumericValue().longValue());
        // графа 25
        Assert.assertEquals(2833, row.getCell("q1").getNumericValue().longValue());
        // графа 26
        Assert.assertEquals(2833, row.getCell("q2").getNumericValue().longValue());
        // графа 27
        Assert.assertEquals(2833, row.getCell("q3").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(-4766, row.getCell("year").getNumericValue().longValue());
        // графа 29
        Assert.assertEquals("тест29", row.getCell("name").getValue());
    }

    @Test
    public void importOktmoEmptyTest() {
        mockProvider(705L);
        int expected = 1 + 1; // в файле 1 простая строка и 1 итоговая
        String fileName = "importFile2.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("department").getValue());
        // графа 3
        Assert.assertEquals("9999", row.getCell("kno").getValue());
        // графа 4
        Assert.assertEquals("111111111", row.getCell("kpp").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("kbk").getValue());
        // графа 6
        Assert.assertNull(row.getCell("oktmo").getValue());
        // графа 7
        Assert.assertEquals("тест7", row.getCell("cadastralNumber").getValue());
        // графа 8
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 9
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 10
        Assert.assertEquals(10L, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 11
        Assert.assertEquals("1/1", row.getCell("taxPart").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("ownershipDate").getValue());
        // графа 13
        Assert.assertNotNull(row.getCell("terminationDate").getValue());
        // графа 14
        Assert.assertEquals(4L, row.getCell("period").getNumericValue().longValue());
        // графа 15
        Assert.assertNull(row.getCell("benefitCode").getValue());
        // графа 16 - зависимая графа
        // графа 17 - зависимая графа
        // графа 18
        Assert.assertNotNull(row.getCell("startDate").getValue());
        // графа 19
        Assert.assertNotNull(row.getCell("endDate").getValue());
        // графа 20
        Assert.assertEquals(4L, row.getCell("benefitPeriod").getNumericValue().longValue());
        // графа 21
        Assert.assertEquals(2.5, row.getCell("taxRate").getNumericValue().doubleValue(), 0);
        // графа 22
        Assert.assertEquals(0.3333, row.getCell("kv").getNumericValue().doubleValue(), 0);
        // графа 23
        Assert.assertEquals(2L, row.getCell("kl").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(-400L, row.getCell("sum").getNumericValue().longValue());
        // графа 25
        Assert.assertEquals(2833, row.getCell("q1").getNumericValue().longValue());
        // графа 26
        Assert.assertEquals(2833, row.getCell("q2").getNumericValue().longValue());
        // графа 27
        Assert.assertEquals(2833, row.getCell("q3").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(-4766, row.getCell("year").getNumericValue().longValue());
        // графа 29
        Assert.assertEquals("тест29", row.getCell("name").getValue());

        // проверка сообщении
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(7), ScriptUtils.getColumnName(row, "benefitCode"), ScriptUtils.getColumnName(row, "oktmo"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void imporBadCodeTest() {
        mockProvider(705L);
        int expected = 1 + 1; // в файле 1 простая строка и 1 итоговая
        String fileName = "importFile3.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals(refbookRecordId, row.getCell("department").getValue());
        // графа 3
        Assert.assertEquals("9999", row.getCell("kno").getValue());
        // графа 4
        Assert.assertEquals("111111111", row.getCell("kpp").getValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("kbk").getValue());
        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("oktmo").getValue());
        // графа 7
        Assert.assertEquals("тест7", row.getCell("cadastralNumber").getValue());
        // графа 8
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 9
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 10
        Assert.assertEquals(10L, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 11
        Assert.assertEquals("1/1", row.getCell("taxPart").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("ownershipDate").getValue());
        // графа 13
        Assert.assertNotNull(row.getCell("terminationDate").getValue());
        // графа 14
        Assert.assertEquals(4L, row.getCell("period").getNumericValue().longValue());
        // графа 15
        Assert.assertNull(row.getCell("benefitCode").getValue());
        // графа 16 - зависимая графа
        // графа 17 - зависимая графа
        // графа 18
        Assert.assertNotNull(row.getCell("startDate").getValue());
        // графа 19
        Assert.assertNotNull(row.getCell("endDate").getValue());
        // графа 20
        Assert.assertEquals(4L, row.getCell("benefitPeriod").getNumericValue().longValue());
        // графа 21
        Assert.assertEquals(2.5, row.getCell("taxRate").getNumericValue().doubleValue(), 0);
        // графа 22
        Assert.assertEquals(0.3333, row.getCell("kv").getNumericValue().doubleValue(), 0);
        // графа 23
        Assert.assertEquals(2L, row.getCell("kl").getNumericValue().longValue());
        // графа 24
        Assert.assertEquals(-400L, row.getCell("sum").getNumericValue().longValue());
        // графа 25
        Assert.assertEquals(2833, row.getCell("q1").getNumericValue().longValue());
        // графа 26
        Assert.assertEquals(2833, row.getCell("q2").getNumericValue().longValue());
        // графа 27
        Assert.assertEquals(2833, row.getCell("q3").getNumericValue().longValue());
        // графа 28
        Assert.assertEquals(-4766, row.getCell("year").getNumericValue().longValue());
        // графа 29
        Assert.assertEquals("тест29", row.getCell("name").getValue());

        // проверка сообщении
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графы: «%s», «%s», «%s», " +
                "т.к. в справочнике «Параметры налоговых льгот земельного налога» не найдена соответствующая запись",
                fileRowIndex, ScriptUtils.getXLSColumnName(16), ScriptUtils.getColumnName(row, "benefitCode"),
                ScriptUtils.getColumnName(row, "benefitBase"), ScriptUtils.getColumnName(row, "benefitParam"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        mockProvider(8L);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;

        int expected = 1; // 1 итоговая строка
        testHelper.execute(FormDataEvent.COMPOSE);
        int i = 0;
        msg = String.format("Данные по земельным участкам из предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма за период: %s %s для подразделения «%s»",
                "4 квартал", "2013", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Не удалось сравнить данные формы с данными формы за предыдущий период. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: %s %s для подразделения «%s»",
                "4 квартал", "2013", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        testHelper.getLogger().clear();
    }

    // консолидация - все источники сразу
    @Test
    public void composeTest() throws ParseException {
        // провайдеры и получения записей для справочников
        mockProvider(8L);
        mockProvider(710L);

        int sourceFormTypeId = 912;
        int sourceTemplateId = 912;
        FormDataKind kind = FormDataKind.SUMMARY;

        // вспомогательные данные источника
        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        FormType formType = new FormType();
        formType.setId(sourceFormTypeId);
        Relation relarion = new Relation();
        relarion.setFormDataId(1L);
        relarion.setFormType(formType);
        sourcesInfo.add(relarion);
        Relation relarion2 = new Relation();
        relarion2.setFormDataId(2L);
        relarion2.setFormType(formType);
        sourcesInfo.add(relarion2);

        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//land//land_registry//v2016//");

        // форма источника
        for (Relation relation : sourcesInfo) {
            // экземпляр-источник
            FormData sourceFormData = new FormData();
            sourceFormData.initFormTemplateParams(sourceTemplate);
            sourceFormData.setId(relation.getFormDataId());
            sourceFormData.setState(WorkflowState.ACCEPTED);
            sourceFormData.setFormType(relation.getFormType());
            sourceFormData.setFormTemplateId(sourceTemplateId);

            when(testHelper.getFormDataService().getLast(eq(sourceFormTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(sourceFormData);
            when(testHelper.getFormDataService().get(eq(sourceFormData.getId().longValue()), anyBoolean())).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceFormData);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.setAllCached(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);
        }

        // предыдущий период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2013);
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setOrder(4);
        prevReportPeriod.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // данные за предыдущий период
        FormData prevFormData = new FormData();
        prevFormData.initFormTemplateParams(testHelper.getFormTemplate());
        prevFormData.setId(0L);
        prevFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(prevFormData);
        DataRow<Cell> row = prevFormData.createDataRow();
        // графа 2
        row.getCell("department").setValue(5L, null);
        // графа 3
        row.getCell("kno").setValue("5", null);
        // графа 4
        row.getCell("kpp").setValue("5", null);
        // графа 6
        row.getCell("oktmo").setValue(1L, null);
        // графа 7
        row.getCell("cadastralNumber").setValue("test", null);
        // графа 21
        row.getCell("taxRate").setValue(5, null);
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        dataRows.add(row);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(dataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(prevFormData))).thenReturn(sourceDataRowHelper);

        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        // ожидается:
        // 2 простые строки из источников
        // 2 строк подитогов
        // 1 строка всего
        int expected = 2 + 2 + 1;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        checkConsolidationData(testHelper.getDataRowHelper().getAll());
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceFormData форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(FormData sourceFormData) throws ParseException {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> row = sourceFormData.createDataRow();
        row.setIndex(1);

        int index = row.getIndex();
        long refbookRecordId = 1L;
        long number = 1L;
        Date date = sdf.parse("01.01.2014");
        String str = "test";

        // графа 1
        row.getCell("rowNumber").setValue(number, index);
        // графа 2
        row.getCell("name").setValue(str, index);
        // графа 3
        row.getCell("oktmo").setValue(refbookRecordId, index);
        // графа 4
        row.getCell("cadastralNumber").setValue(str, index);
        // графа 5
        row.getCell("landCategory").setValue(refbookRecordId, index);
        // графа 6
        row.getCell("constructionPhase").setValue(refbookRecordId, index);
        // графа 7
        row.getCell("cadastralCost").setValue(number, index);
        // графа 8
        row.getCell("taxPart").setValue("1/2", index);
        // графа 9
        row.getCell("ownershipDate").setValue(date, index);
        // графа 10
        row.getCell("terminationDate").setValue(date, index);
        // графа 11
        row.getCell("benefitCode").setValue(refbookRecordId, index);
        // графа 12
        row.getCell("benefitBase").setValue(null, index);
        // графа 13
        row.getCell("benefitParam").setValue(null, index);
        // графа 14
        row.getCell("startDate").setValue(date, index);
        // графа 15
        row.getCell("endDate").setValue(date, index);
        // графа 16
        row.getCell("benefitPeriod").setValue(number, index);

        dataRows.add(row);
        return dataRows;
    }

    private void checkConsolidationData(List<DataRow<Cell>> dateRows) throws ParseException {
        // проверяется только первая простая строка
        DataRow<Cell> row = dateRows.get(0);

        // графы 2, 3, 4, 21 - скопированные с предыдущего периода
        // графы 6..13, 15..20 - сконсолидированные

        // графа 2
        Assert.assertEquals(5L, row.getCell("department").getValue());
        // графа 3
        Assert.assertEquals("5", row.getCell("kno").getValue());
        // графа 4
        Assert.assertEquals("5", row.getCell("kpp").getValue());
        // графа 21
        Assert.assertEquals(5, row.getCell("taxRate").getNumericValue().longValue());

        long refbookRecordId = 1L;
        long number = 1L;
        Date date = sdf.parse("01.01.2014");
        String str = "test";

        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("oktmo").getValue());
        // графа 7
        Assert.assertEquals(str, row.getCell("cadastralNumber").getValue());
        // графа 8
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 9
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 10
        Assert.assertEquals(number, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 11
        Assert.assertEquals("1/2", row.getCell("taxPart").getValue());
        // графа 12
        Assert.assertEquals(date, row.getCell("ownershipDate").getValue());
        // графа 13
        Assert.assertEquals(date, row.getCell("terminationDate").getValue());
        // графа 15
        Assert.assertEquals(refbookRecordId, row.getCell("benefitCode").getValue());
        // графа 16 - зависимая графа
        // графа 17 - зависимая графа
        // графа 18
        Assert.assertEquals(date, row.getCell("startDate").getValue());
        // графа 19
        Assert.assertEquals(date, row.getCell("endDate").getValue());
        // графа 20
        Assert.assertEquals(number, row.getCell("benefitPeriod").getNumericValue().longValue());
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