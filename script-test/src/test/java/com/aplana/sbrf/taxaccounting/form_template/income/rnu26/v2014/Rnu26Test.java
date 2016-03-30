package com.aplana.sbrf.taxaccounting.form_template.income.rnu26.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * (РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения.
 */
public class Rnu26Test extends ScriptTestBase {
    private static final int TYPE_ID = 325;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.CONSOLIDATED;

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
        return getDefaultScriptTestMockHelper(Rnu26Test.class);
    }

    @Before
    public void mockFormDataService() {
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(false);
                        return result;
                    }
                });
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
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    // TODO в логе должна быть ошибка о неверной итоговой строке
    @Test
    public void importTransportFileTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Кроме простого выполнения события других проверок нет, т.к. для формы консолидация выполняется сервисом
        testHelper.execute(FormDataEvent.COMPOSE);
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        DataRow<Cell> tmpRow = dataRows.get(0);
        Assert.assertEquals(null, tmpRow.getCell("rowNumber").getNumericValue());
        Assert.assertEquals(null, tmpRow.getCell("fix").getStringValue());
        Assert.assertEquals("Министерство финансов Российской Федерации", tmpRow.getCell("issuer").getStringValue());
        Assert.assertEquals(null, tmpRow.getCell("shareType").getNumericValue());
        Assert.assertEquals("12", tmpRow.getCell("tradeNumber").getStringValue());
        Assert.assertEquals(null, tmpRow.getCell("currency").getNumericValue());
        Assert.assertEquals(0, tmpRow.getCell("lotSizePrev").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, tmpRow.getCell("lotSizeCurrent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveCalcValuePrev").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, tmpRow.getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, tmpRow.getCell("signSecurity").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, tmpRow.getCell("marketQuotation").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(32.9390, tmpRow.getCell("rubCourse").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(395.268000, tmpRow.getCell("marketQuotationInRub").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4743.22, tmpRow.getCell("costOnMarketQuotation").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveCalcValue").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveCreation").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveRecovery").getNumericValue().doubleValue(), 0);

        tmpRow = dataRows.get(1);
        Assert.assertEquals(null, tmpRow.getCell("rowNumber").getNumericValue());
        Assert.assertEquals("Общий итог", tmpRow.getCell("fix").getStringValue());
        Assert.assertEquals(null, tmpRow.getCell("issuer").getStringValue());
        Assert.assertEquals(null, tmpRow.getCell("shareType").getNumericValue());
        Assert.assertEquals(null, tmpRow.getCell("tradeNumber").getStringValue());
        Assert.assertEquals(null, tmpRow.getCell("currency").getNumericValue());
        Assert.assertEquals(0, tmpRow.getCell("lotSizePrev").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, tmpRow.getCell("lotSizeCurrent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveCalcValuePrev").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, tmpRow.getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(null, tmpRow.getCell("signSecurity").getNumericValue());
        Assert.assertEquals(null, tmpRow.getCell("marketQuotation").getNumericValue());
        Assert.assertEquals(null, tmpRow.getCell("rubCourse").getNumericValue());
        Assert.assertEquals(null, tmpRow.getCell("marketQuotationInRub").getNumericValue());
        Assert.assertEquals(1.22, tmpRow.getCell("costOnMarketQuotation").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveCalcValue").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveCreation").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, tmpRow.getCell("reserveRecovery").getNumericValue().doubleValue(), 0);
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//rnu26//v2014//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;
        int i;

        // суммы (графа 6..9, 14..17)
        String [] totalColumns = { "lotSizePrev", "lotSizeCurrent", "reserveCalcValuePrev", "cost",
                "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        String [] allColumns = { "rowNumber", "fix", "issuer", "shareType", "tradeNumber", "currency", "lotSizePrev",
                "lotSizeCurrent", "reserveCalcValuePrev", "cost", "signSecurity", "marketQuotation", "rubCourse",
                "marketQuotationInRub", "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "reserveRecovery" };

        final FormData prevFormData = new FormData();
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(prevFormData);

        List<DataRow<Cell>> prevDataRows = new ArrayList<DataRow<Cell>>();
        // предыдущая строка
        DataRow<Cell> prevRow = formData.createDataRow();
        prevRow.setIndex(1);
        prevDataRows.add(prevRow);
        // предыдущий итог
        DataRow<Cell> prevTotalRow = formData.createDataRow();
        prevTotalRow.setIndex(2);
        prevTotalRow.setAlias("total");
        prevDataRows.add(prevTotalRow);

        DataRowHelper prevDataRowHelper = new DataRowHelperStub();
        prevDataRowHelper.save(prevDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(prevFormData)).thenReturn(prevDataRowHelper);

        prevRow.getCell("rowNumber").setValue(1, null);
        prevRow.getCell("fix").setValue(null, null);
        prevRow.getCell("issuer").setValue("1", null);
        prevRow.getCell("shareType").setValue(1, null);
        prevRow.getCell("tradeNumber").setValue("1", null);
        prevRow.getCell("currency").setValue(1, null);
        prevRow.getCell("lotSizePrev").setValue(1, null);
        prevRow.getCell("lotSizeCurrent").setValue(1, null);
        prevRow.getCell("reserveCalcValuePrev").setValue(1, null);
        prevRow.getCell("cost").setValue(1, null);
        prevRow.getCell("signSecurity").setValue(1, null);
        prevRow.getCell("marketQuotation").setValue(1, null);
        prevRow.getCell("rubCourse").setValue(1, null);
        prevRow.getCell("marketQuotationInRub").setValue(1, null);
        prevRow.getCell("costOnMarketQuotation").setValue(1, null);
        prevRow.getCell("reserveCalcValue").setValue(1, null);
        prevRow.getCell("reserveCreation").setValue(1, null);
        prevRow.getCell("reserveRecovery").setValue(1, null);

        // итог
        DataRow<Cell> totalRow = dataRows.get(0);
        dataRows.clear();

        // строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // подитог
        DataRow<Cell> subTotalRow = formData.createDataRow();
        subTotalRow.setIndex(2);
        subTotalRow.setAlias("total1#1");
        dataRows.add(subTotalRow);

        totalRow.setIndex(3);
        totalRow.setAlias("total");
        dataRows.add(totalRow);

        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(0, null);
            totalRow.getCell(alias).setValue(0, null);
        }

        // 1. Проверка на заполнение поля
        for (String alias : allColumns) {
            row.getCell(alias).setValue(null, null);
        }
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(0, null);
            totalRow.getCell(alias).setValue(0, null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        // обязательные (графа 1..3, 5..9, 13, 14)
        String [] nonEmptyColumns = { "issuer", "currency", "lotSizePrev", "lotSizeCurrent", "reserveCalcValuePrev",
                "cost", "signSecurity", "marketQuotationInRub", "costOnMarketQuotation" };
        for (String alias : nonEmptyColumns) {
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), row.getCell(alias).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        String [] arithmeticCheckAlias_1 = { "costOnMarketQuotation" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_1);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3.1 Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        prevRow.getCell("tradeNumber").setValue("2", null);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(0, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(0, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(0, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Отсутствуют строки с номерами сделок: 2!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        prevRow.getCell("tradeNumber").setValue("1", null);
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 3.2 Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(1, row2);
        subTotalRow.setIndex(3);
        subTotalRow.setAlias("total1#3");
        totalRow.setIndex(4);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(0, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(0, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : allColumns) {
            row2.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        for (String alias : totalColumns) {
            int tmp = row.getCell(alias).getNumericValue().intValue() + row2.getCell(alias).getNumericValue().intValue();
            subTotalRow.getCell(alias).setValue(tmp, null);
            totalRow.getCell(alias).setValue(tmp, null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevRow.getCell("lotSizeCurrent").setValue(row.getCell("lotSizePrev").getValue(), null);
        prevRow.getCell("reserveCalcValue").setValue(row.getCell("reserveCalcValuePrev").getValue(), null);
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserveCalcValuePrev").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Существует несколько строк с номерами сделок: 1!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
        subTotalRow.setAlias("total1#1");
        subTotalRow.setIndex(2);
        totalRow.setIndex(3);
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 4. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(1, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(1, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Графы 8 и 17 неравны!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_4 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_4);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(1, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(1, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Графы 9, 14 и 15 ненулевые!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(1, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(1, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Графы 9, 14 и 15 ненулевые!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 7. Проверка необращающихся акций (графа 10, 15, 16)
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(4, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Акции необращающиеся, графы 15 и 16 ненулевые!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_7 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_7);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(5, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(2, null);
        row.getCell("reserveCreation").setValue(2, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Акции обращающиеся – резерв сформирован (восстановлен) некорректно! Графа 17 ненулевая";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_8 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_8);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 9. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(5, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Акции обращающиеся – резерв сформирован (восстановлен) некорректно! Графа 16 ненулевая";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_9 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_9);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 10. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(5, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Акции обращающиеся – резерв сформирован (восстановлен) некорректно! Графы 16 и 17 ненулевые";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_10 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_10);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Резерв сформирован неверно! Сумма граф 8 и 16 не равна сумме граф 15 и 17";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_11 = { "reserveCreation" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_11);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 12. Проверка на положительные значения при наличии созданного резерва
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(-1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_12 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_12);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 13. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevRow.getCell("lotSizeCurrent").setValue(11, null);
        prevRow.getCell("reserveCalcValue").setValue(row.getCell("reserveCalcValuePrev").getValue(), null);
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserveCalcValuePrev").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: РНУ сформирован некорректно! Не выполняется условие: " +
                "«Графа 6» (%s) текущей строки РНУ-26 за текущий период = «Графе 7» (%s) строки РНУ-26 за предыдущий период, " +
                "значение «Графы 4» которой соответствует значению «Графы 4» РНУ-26 за текущий период.";
        msg = String.format(msg, row.getCell("lotSizePrev").getValue(), prevRow.getCell("lotSizeCurrent").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_13 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_13);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 14. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevRow.getCell("reserveCalcValue").setValue(11, null);
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserveCalcValuePrev").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: РНУ сформирован некорректно! Не выполняется условие: " +
                "«Графа 8» (%s) текущей строки РНУ-26 за текущий период = «Графе 15» (%s) строки РНУ-26 за предыдущий период, " +
                "значение «Графы 4» которой соответствует значению «Графы 4» РНУ-26 за текущий период.";
        msg = String.format(msg, row.getCell("reserveCalcValuePrev").getValue(), prevRow.getCell("reserveCalcValue").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias_14 = { "reserveCalcValuePrev", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_14);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 15. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(2, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevRow.getCell("reserveCalcValue").setValue(row.getCell("reserveCalcValuePrev").getValue(), null);
        prevRow.getCell("lotSizeCurrent").setValue(row.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserveCalcValuePrev").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias_15 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_15);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = "РНУ сформирован некорректно! Не выполняется условие: " +
                "«Итого» по «Графе 6» (%s) = «Общий итог» по графе 7 (%s) формы РНУ-26 за предыдущий отчётный период";
        msg = String.format(msg, totalRow.getCell("lotSizePrev").getValue(), prevTotalRow.getCell("lotSizeCurrent").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 16. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(2, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevRow.getCell("reserveCalcValue").setValue(row.getCell("reserveCalcValuePrev").getValue(), null);
        prevRow.getCell("lotSizeCurrent").setValue(row.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias_16 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias_16);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = "РНУ сформирован некорректно! Не выполняется условие: " +
                "«Итого» по «Графе 8» (%s) = «Общий итог» по графе 15 (%s) формы РНУ-26 за предыдущий отчётный период";
        msg = String.format(msg, totalRow.getCell("reserveCalcValuePrev").getValue(), prevTotalRow.getCell("reserveCalcValue").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 17. Арифметическая проверка графы 8, 13..17
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(1, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(2, null);
        row.getCell("lotSizePrev").setValue(3, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(4, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        String [] arithmeticCheckAlias = { "lotSizePrev", "reserveCalcValuePrev", "marketQuotationInRub",
                "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        i = 1;
        for (String alias : arithmeticCheckAlias) {
            i++;
            row.getCell(alias).setValue(i * 2, null);
        }
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        row.getCell("signSecurity").setValue(4, null);
        prevRow.getCell("signSecurity").setValue(5, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = getWrongCalcMsg(row, arithmeticCheckAlias);
        boolean ok = false;
        for (LogEntry log : entries) {
            if (log.getMessage().equals(msg)) {
                ok = true;
                break;
            }
        }
        Assert.assertTrue(ok);
        testHelper.getLogger().clear();
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 18.1 Проверка подитоговых значений
        // Проверка наличия всех фиксированных строк
        dataRows.remove(subTotalRow);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(0, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(0, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(0, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG, 1);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.add(1, subTotalRow);

        // 18.2 Проверка подитоговых значений
        // Проверка отсутствия лишних фиксированных строк
        DataRow<Cell> subTotalRow2 = formData.createDataRow();
        subTotalRow2.setIndex(4);
        subTotalRow2.setAlias("total1#2");
        dataRows.add(2, subTotalRow2);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(0, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(0, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(0, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG_ROW, subTotalRow2.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(subTotalRow2);

        // 18.3 Проверка подитоговых значений
        // Проверка итоговых значений по фиксированным строкам
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(0, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(0, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(0, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(1, null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG_SUM, subTotalRow.getIndex(), "1", subTotalRow.getCell(totalColumns[0]).getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 19.1 Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        testHelper.getFormData().setKind(FormDataKind.PRIMARY);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserveCalcValuePrev").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(1, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(100, null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevRow.getCell("lotSizeCurrent").setValue(row.getCell("lotSizePrev").getValue(), null);
        prevRow.getCell("reserveCalcValue").setValue(row.getCell("reserveCalcValuePrev").getValue(), null);
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserveCalcValuePrev").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias19_1 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias19_1);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        for (String alias : totalColumns) {
            msg = String.format(ScriptUtils.WRONG_TOTAL, row.getCell(alias).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        testHelper.getFormData().setKind(FormDataKind.CONSOLIDATED);

        // 19.2 Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        dataRows.remove(totalRow);
        row.getCell("rowNumber").setValue(0, null);
        row.getCell("fix").setValue(null, null);
        row.getCell("issuer").setValue("1", null);
        row.getCell("shareType").setValue(0, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("currency").setValue(0, null);
        row.getCell("lotSizePrev").setValue(0, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserveCalcValuePrev").setValue(0, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(0, null);
        row.getCell("marketQuotation").setValue(0, null);
        row.getCell("rubCourse").setValue(0, null);
        row.getCell("marketQuotationInRub").setValue(0, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(0, null);
        row.getCell("reserveRecovery").setValue(0, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Итоговые значения рассчитаны неверно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.add(totalRow);
    }

    /**
     * Сообщение арифметической проверки.
     *
     * @param row строка
     * @param columns список неправильных алиасов
     */
    private String getWrongCalcMsg(DataRow<Cell> row, String [] columns) {
        List<String> tmpColumnNames = new ArrayList<String>();
        for (String alias : columns) {
            tmpColumnNames.add(row.getCell(alias).getColumn().getName());
        }
        String subStr = "«" + org.springframework.util.StringUtils.collectionToDelimitedString(tmpColumnNames, "», «") + "»";
        return String.format(ScriptUtils.WRONG_CALC, row.getIndex(), subStr);
    }
}