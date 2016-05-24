package com.aplana.sbrf.taxaccounting.form_template.income.rnu25.v2014;

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
 * (РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения.
 */
public class Rnu25Test extends ScriptTestBase {
    private static final int TYPE_ID = 324;
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
        return getDefaultScriptTestMockHelper(Rnu25Test.class);
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
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
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

    @Test
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        checkLoadData(testHelper.getDataRowHelper().getAll(), false);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 4; // в файле 4 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll(), true);
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
    void checkLoadData(List<DataRow<Cell>> dataRows, boolean isXlsm) {
        DataRow<Cell> tmpRow;

        int i = 0;
        Assert.assertEquals("142173370BB10OFZ0000", dataRows.get(i++).getCell("tradeNumber").getStringValue());
        Assert.assertEquals("142173370BB10OFZ0001", dataRows.get(i++).getCell("tradeNumber").getStringValue());
        if (isXlsm) {
            Assert.assertEquals("SU25065RMFS2 Итог", dataRows.get(i++).getCell("fix").getStringValue());
        }
        Assert.assertEquals("Общий итог", dataRows.get(i++).getCell("fix").getStringValue());

        int [] rowIndexes = { 0, 1 };
        for (int index : rowIndexes) {
            tmpRow = dataRows.get(index);
            Assert.assertEquals(null, tmpRow.getCell("rowNumber").getNumericValue());
            Assert.assertEquals(null, tmpRow.getCell("fix").getStringValue());
            Assert.assertEquals("SU25065RMFS2", tmpRow.getCell("regNumber").getStringValue());
            Assert.assertEquals(150000, tmpRow.getCell("lotSizePrev").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(150000, tmpRow.getCell("lotSizeCurrent").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(null, tmpRow.getCell("reserve").getNumericValue());
            Assert.assertEquals(170717070, tmpRow.getCell("cost").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(1, tmpRow.getCell("signSecurity").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(1041.07, tmpRow.getCell("marketQuotation").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(null, tmpRow.getCell("costOnMarketQuotation").getNumericValue());
            Assert.assertEquals(null, tmpRow.getCell("reserveCalcValue").getNumericValue());
            Assert.assertEquals(null, tmpRow.getCell("reserveCreation").getNumericValue());
            Assert.assertEquals(null, tmpRow.getCell("reserveRecovery").getNumericValue());
        }

        int [] totalIndexes = ( isXlsm ? new int[] {2, 3} : new int [] { 2 } );
        for (int index : totalIndexes) {
            tmpRow = dataRows.get(index);
            Assert.assertEquals(null, tmpRow.getCell("rowNumber").getNumericValue());
            Assert.assertEquals(null, tmpRow.getCell("regNumber").getStringValue());
            Assert.assertEquals(null, tmpRow.getCell("tradeNumber").getStringValue());
            Assert.assertEquals(300000, tmpRow.getCell("lotSizePrev").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(300000, tmpRow.getCell("lotSizeCurrent").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(0, tmpRow.getCell("reserve").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(341434140, tmpRow.getCell("cost").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(null, tmpRow.getCell("signSecurity").getNumericValue());
            Assert.assertEquals(null, tmpRow.getCell("marketQuotation").getNumericValue());
            Assert.assertEquals(0, tmpRow.getCell("costOnMarketQuotation").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(0, tmpRow.getCell("reserveCalcValue").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(0, tmpRow.getCell("reserveCreation").getNumericValue().doubleValue(), 0.0);
            Assert.assertEquals(0, tmpRow.getCell("reserveRecovery").getNumericValue().doubleValue(), 0.0);
        }
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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//rnu25//v2014//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;

        // суммы (графа 4..7, 10..13)
        String [] totalColumns = { "lotSizePrev", "lotSizeCurrent", "reserve", "cost", "costOnMarketQuotation",
                "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        String [] allColumns = { "rowNumber", "fix", "regNumber", "tradeNumber", "lotSizePrev", "lotSizeCurrent",
                "reserve", "cost", "signSecurity", "marketQuotation", "costOnMarketQuotation", "reserveCalcValue",
                "reserveCreation", "reserveRecovery" };

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
        prevRow.getCell("regNumber").setValue("1", null);
        prevRow.getCell("tradeNumber").setValue("1", null);
        prevRow.getCell("lotSizePrev").setValue(1, null);
        prevRow.getCell("lotSizeCurrent").setValue(1, null);
        prevRow.getCell("reserve").setValue(1, null);
        prevRow.getCell("cost").setValue(1, null);
        prevRow.getCell("signSecurity").setValue(1, null);
        prevRow.getCell("marketQuotation").setValue(1, null);
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

        // 1.1 Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        prevRow.getCell("tradeNumber").setValue("2", null);
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell("fix").setValue("1 Итог", null);
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(1, null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        int i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Отсутствуют строки с номерами сделок: 2!", entries.get(i++).getMessage());
        String [] arithmeticCheckAlias1 = { "reserve", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias1);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        prevRow.getCell("tradeNumber").setValue("1", null);

        // 1.2 Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(1, row2);
        subTotalRow.setIndex(3);
        subTotalRow.setAlias("total1#3");
        totalRow.setIndex(4);
        prevRow.getCell("tradeNumber").setValue("1", null);
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : allColumns) {
            row2.getCell(alias).setValue(row.getCell(alias).getValue(), null);
        }
        for (String alias : totalColumns) {
            int tmp = row.getCell(alias).getNumericValue().intValue() + row2.getCell(alias).getNumericValue().intValue();
            subTotalRow.getCell(alias).setValue(tmp, null);
            totalRow.getCell(alias).setValue(2, null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Существует несколько строк с номерами сделок: 1!", entries.get(i++).getMessage());
        String [] arithmeticCheckAlias1_2 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias1_2);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = getWrongCalcMsg(row2, arithmeticCheckAlias1_2);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
        subTotalRow.setAlias("total1#1");
        subTotalRow.setIndex(2);
        totalRow.setIndex(3);

        // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(0, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(0, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Строка 1: графы 6 и 13 неравны!", entries.get(i++).getMessage());
        String [] arithmeticCheckAlias2 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias2);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Строка 1: графы 7, 10 и 11 ненулевые!", entries.get(i++).getMessage());
        String [] arithmeticCheckAlias3 = { "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias3);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
        prevRow.getCell("lotSizeCurrent").setValue(0, null);
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(0, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Строка 1: графы 6 и 13 ненулевые!", entries.get(i++).getMessage());
        String [] arithmeticCheckAlias4 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias4);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        prevRow.getCell("lotSizeCurrent").setValue(1, null);

        // 5. Проверка необращающихся акций (графа 8, 11, 12)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(4, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Строка 1: облигации необращающиеся, графы 11 и 12 ненулевые!", entries.get(i++).getMessage());
        String [] arithmeticCheckAlias5 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias5);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(5, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(2, null);
        row.getCell("reserveCreation").setValue(2, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «Графа 11» – «Графа 6» > 0, то «Графа 13» = 0";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias6 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias6);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(5, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(0, null);
        row.getCell("reserveCreation").setValue(2, null);
        row.getCell("reserveRecovery").setValue(3, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «Графа 11» – «Графа 6» < 0, то «Графа 12» = 0";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias7 = { "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias7);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(5, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(2, null);
        row.getCell("reserveRecovery").setValue(2, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «Графа 11» – «Графа 6» = 0, то «Графа 12» и «Графа 13» = 0";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias8 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias8);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 9. Проверка на положительные значения при наличии созданного резерва
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(-1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(-1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(-1, null);
        row.getCell("reserveCalcValue").setValue(-1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(3, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: резерв сформирован. Графы 5, 7, 10 и 11 неположительные!";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias9 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias9);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 10. Проверка корректности создания резерва (графа 6, 11, 12, 13)
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(3, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = "Строка 1: резерв сформирован некорректно! Не выполняется условие: «Графа 6» + «Графа 12» - «Графа 11» - «Графа 13» = 0";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias10 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias10);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(2, null); ///
        row.getCell("lotSizeCurrent").setValue(1, null); ///
        row.getCell("reserve").setValue(1, null);  ////
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null); ///
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %d: РНУ сформирован некорректно! Не выполняется условие: " +
                "«Графа 4» (%s) текущей строки РНУ-25 за текущий период = «Графе 5» (%s) строки РНУ-25 за предыдущий период, " +
                "значение «Графы 3» которой соответствует значению «Графы 3» РНУ-25 за текущий период.",
                row.getIndex(), row.getCell("lotSizePrev").getValue(), prevRow.getCell("lotSizeCurrent").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias11 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias11);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
        prevRow.getCell("reserveCalcValue").setValue(2, null);
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %d: РНУ сформирован некорректно! Не выполняется условие: " +
                "«Графа 6» (%s) текущей строки РНУ-25 за текущий период = «Графе 11» (%s) строки РНУ-25 за предыдущий период, " +
                "значение «Графы 3» которой соответствует значению «Графы 3» РНУ-25 за текущий период.",
                row.getIndex(), row.getCell("reserve").getValue(), prevRow.getCell("reserveCalcValue").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        String [] arithmeticCheckAlias12 = { "reserve", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias12);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        prevRow.getCell("reserveCalcValue").setValue(1, null);

        // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
        // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null); ///
        row.getCell("lotSizeCurrent").setValue(1, null); ///
        row.getCell("reserve").setValue(1, null);  ////
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null); ///
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(null, null);
        prevTotalRow.getCell("reserveCalcValue").setValue(null, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias13 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias13);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("РНУ сформирован некорректно! Не выполняется условие: " +
                "«Общий итог» по графе 4 (%s) = «Общий итог» по графе 5 (%s) Формы РНУ-25 за предыдущий отчетный период.",
                totalRow.getCell("lotSizePrev").getValue(), prevTotalRow.getCell("lotSizeCurrent").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("РНУ сформирован некорректно! Не выполняется условие: " +
                "«Общий итог» по графе 6 (%s)= «Общий итог» по графе 11 (%s) формы РНУ-25 за предыдущий отчётный период",
                totalRow.getCell("reserve").getValue(), prevTotalRow.getCell("reserveCalcValue").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        prevRow.getCell("reserveCalcValue").setValue(1, null);

        // 15. Проверка обязательных полей
        // 16. Арифметические проверки
        dataRows.remove(subTotalRow);
        row.getCell("rowNumber").setValue(null, null);
        row.getCell("regNumber").setValue(null, null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(null, null);
        row.getCell("lotSizeCurrent").setValue(null, null);
        row.getCell("reserve").setValue(null, null);
        row.getCell("cost").setValue(null, null);
        row.getCell("signSecurity").setValue(null, null);
        row.getCell("marketQuotation").setValue(null, null);
        row.getCell("costOnMarketQuotation").setValue(null, null);
        row.getCell("reserveCalcValue").setValue(null, null);
        row.getCell("reserveCreation").setValue(null, null);
        row.getCell("reserveRecovery").setValue(null, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(0, null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        // обязательные (графа 1..3, 5..13)
        String [] nonEmptyColumns = { "regNumber", /*"tradeNumber",*/ "lotSizeCurrent", "reserve", "cost", "signSecurity",
                "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        for (String column : nonEmptyColumns) {
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, 1, row.getCell(column).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        String [] arithmeticCheckAlias = { "reserve", "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 17.1 Проверка итоговых значений по ГРН
        // Проверка наличия всех фиксированных строк
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias17_1 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias17_1);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG, 1);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.add(1, subTotalRow);

        // 17.2 Проверка итоговых значений по ГРН
        // Проверка отсутствия лишних фиксированных строк
        DataRow<Cell> subTotalRow2 = formData.createDataRow();
        subTotalRow2.setIndex(4);
        subTotalRow2.setAlias("total1#2");
        dataRows.add(2, subTotalRow2);
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias17_2 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias17_2);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG_ROW, subTotalRow2.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(subTotalRow2);

        // 17.3 Проверка итоговых значений по ГРН
        // Проверка итоговых значений по фиксированным строкам
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(0, null);
            totalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias17_3 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias17_3);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG_SUM, subTotalRow.getIndex(), "1", subTotalRow.getCell(totalColumns[0]).getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 18.1 Проверка итогового значений по всей форме
        row.getCell("rowNumber").setValue(1, null);
        row.getCell("regNumber").setValue("1", null);
        row.getCell("tradeNumber").setValue("1", null);
        row.getCell("lotSizePrev").setValue(1, null);
        row.getCell("lotSizeCurrent").setValue(1, null);
        row.getCell("reserve").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("signSecurity").setValue(1, null);
        row.getCell("marketQuotation").setValue(1, null);
        row.getCell("costOnMarketQuotation").setValue(1, null);
        row.getCell("reserveCalcValue").setValue(1, null);
        row.getCell("reserveCreation").setValue(1, null);
        row.getCell("reserveRecovery").setValue(1, null);
        for (String alias : totalColumns) {
            subTotalRow.getCell(alias).setValue(row.getCell(alias).getValue(), null);
            totalRow.getCell(alias).setValue(100, null);
            prevTotalRow.getCell(alias).setValue(totalRow.getCell(alias).getValue(), null);
        }
        prevTotalRow.getCell("lotSizeCurrent").setValue(totalRow.getCell("lotSizePrev").getValue(), null);
        prevTotalRow.getCell("reserveCalcValue").setValue(totalRow.getCell("reserve").getValue(), null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        String [] arithmeticCheckAlias18 = { "reserveCalcValue", "reserveCreation", "reserveRecovery" };
        msg = getWrongCalcMsg(row, arithmeticCheckAlias18);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        for (String alias : totalColumns) {
            msg = String.format(ScriptUtils.WRONG_TOTAL, row.getCell(alias).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 18.2 Проверка итогового значений по всей форме
        dataRows.clear();
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Итоговые значения рассчитаны неверно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }
}