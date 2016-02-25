package com.aplana.sbrf.taxaccounting.form_template.income.rnu8.v2012;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * РНУ-8.
 */
public class Rnu8Test extends ScriptTestBase {
    private static final int TYPE_ID = 311;
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
        return getDefaultScriptTestMockHelper(Rnu8Test.class);
    }

    @Before
    public void mockFormDataService() {
        // настройка справочников
        final long refbookId = 27L;

        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refbookId);
        when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);

        // записи для справочника
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // ищет среди записей справочника запись соответствующую коду из фильтра
                // в фильтре: LOWER(CODE) = LOWER('codeA') and LOWER(NUMBER) = LOWER('numberA')
                // либо: LOWER(CODE) = LOWER('codeA')
                String filter = (String) invocation.getArguments()[2];
                String codeValue = filter.substring(filter.indexOf("('") + 2, filter.indexOf("')"));
                if (codeValue == null) {
                    return new PagingResult<Map<String, RefBookValue>>();
                }
                String numberValue = null;
                if (filter.lastIndexOf("('") != filter.indexOf("('")) {
                    numberValue = filter.substring(filter.lastIndexOf("('") + 2, filter.lastIndexOf("')"));
                }
                final Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(refbookId);
                for (Map<String, RefBookValue> row : records.values()) {
                    if (codeValue.equals(row.get("CODE").getStringValue())
                            && (numberValue == null || numberValue.equals(row.get("NUMBER").getStringValue()))) {
                        List<Map<String, RefBookValue>> tmpRecords = Arrays.asList(row);
                        return new PagingResult<Map<String, RefBookValue>>(tmpRecords);
                    }
                }
                return new PagingResult<Map<String, RefBookValue>>();
            }
        });

        // справочник
        RefBook refBook = new RefBook();
        refBook.setId(refbookId);
        refBook.setName("Классификатор расходов Сбербанка России для целей налогового учёта");
        // атрибут справочника
        RefBookAttribute attribute = new RefBookAttribute();
        attribute.setAlias("CODE");
        attribute.setName("Код налогового учёта ");
        refBook.setAttributes(Arrays.asList(attribute));
        when(testHelper.getRefBookFactory().get(refbookId)).thenReturn(refBook);

        // период ввода остатков
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
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

    // Проверка со входом во все ЛП
    //@Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//rnu7//v2012//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка на заполнение поля
        // 3. Проверка на нулевые значения
        // 5. Проверка даты совершения операции и границ отчётного периода
        DataRow<Cell> row1 = formData.createDataRow();
        row1.setIndex(1);
        row1.getCell("number").setValue(1, null);
        row1.getCell("helper").setValue("строка1000", null);
        // зависимая // row.getCell("kny").setValue(1, null); // Код налогового учёта
        row1.getCell("date").setValue(sdf.parse("01.01.2015"), null);
        row1.getCell("code").setValue(2, null); // Номер
        row1.getCell("docNumber").setValue("строка15", null);
        row1.getCell("docDate").setValue(new Date(), null);
        row1.getCell("currencyCode").setValue(3, null);  // Код валюты. Цифровой
        row1.getCell("rateOfTheBankOfRussia").setValue(null, null);
        dataRows.add(row1);

        // для попадания в ЛП:
        // 4. Проверка, что не  отображаются данные одновременно по бухгалтерскому и по налоговому учету
        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
        // 8. Арифметические проверки расчета неитоговых строк
        // 11. Проверка наличия суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        row2.getCell("number").setValue(1, null);
        row2.getCell("helper").setValue("строка1000", null);
        // зависимая // row.getCell("kny").setValue(1, null); // Код налогового учёта
        row2.getCell("date").setValue(sdf.parse("01.01.2014"), null);
        row2.getCell("code").setValue(2, null); // Номер
        row2.getCell("docNumber").setValue("строка15", null);
        row2.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row2.getCell("currencyCode").setValue(3, null);  // Код валюты. Цифровой
        row2.getCell("rateOfTheBankOfRussia").setValue(19.4, null);
        row2.getCell("taxAccountingCurrency").setValue(17.2, null);
        row2.getCell("taxAccountingRuble").setValue(17.2, null);
        row2.getCell("accountingCurrency").setValue(17.2, null);
        row2.getCell("ruble").setValue(17.3, null);
        dataRows.add(row2);

        // для попадания в ЛП:
        // 7. Проверка на уникальность записи по налоговому учету
        // 8. Арифметические проверки расчета неитоговых строк
        // 10. Арифметические проверки расчета строки общих итогов
        DataRow<Cell> row3 = formData.createDataRow();
        row3.setIndex(3);
        row3.getCell("number").setValue(1, null);
        row3.getCell("helper").setValue("строка1000", null);
        // зависимая // row.getCell("kny").setValue(1, null); // Код налогового учёта
        row3.getCell("date").setValue(sdf.parse("01.01.2014"), null);
        row3.getCell("code").setValue(2, null); // Номер
        row3.getCell("docNumber").setValue("строка15", null);
        row3.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row3.getCell("currencyCode").setValue(3, null);  // Код валюты. Цифровой
        row3.getCell("rateOfTheBankOfRussia").setValue(19.4, null);
        row3.getCell("taxAccountingCurrency").setValue(17.2, null);
        row3.getCell("taxAccountingRuble").setValue(17.2, null);
        row3.getCell("accountingCurrency").setValue(17.2, null);
        row3.getCell("ruble").setValue(17.3, null);
        dataRows.add(row3);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Курс Банка России"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Все суммы по операции нулевые!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Дата совершения операции вне границ отчётного периода!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Одновременно указаны данные по налоговому (графа 10) и бухгалтерскому (графа 12) учету.", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Сумма данных бухгалтерского учёта превышает сумму начисленных платежей для документа строка15 от 01.01.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Неверное значение граф: «Курс Банка России», «Сумма расхода, в налоговом учёте. Рубли», «Сумма расхода, в бухгалтерском учёте. Рубли»!", entries.get(i++).getMessage());
        Assert.assertEquals("Операция, указанная в строке 2, в налоговом учете за последние 3 года не проходила!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 3: Одновременно указаны данные по налоговому (графа 10) и бухгалтерскому (графа 12) учету.", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 3: Неверное значение граф: «Курс Банка России», «Сумма расхода, в налоговом учёте. Рубли», «Сумма расхода, в бухгалтерском учёте. Рубли»!", entries.get(i++).getMessage());
        Assert.assertEquals("Операция, указанная в строке 3, в налоговом учете за последние 3 года не проходила!", entries.get(i++).getMessage());
        Assert.assertEquals("Строки 2, 3 не уникальны в рамках текущей налоговой формы! По данным строкам значения следующих граф совпадают: «Балансовый счёт (номер)» (numberB), «Первичный документ. Номер» (строка15), «Первичный документ. Дата» (01.01.2014).", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Сумма расхода, в налоговом учёте. Рубли»!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Сумма расхода, в бухгалтерском учёте. Рубли»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

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

    //@Test
    public void importTransportFileTest() {
        int expected = 2 + 2 + 1; // в источнике 2 строки (без итогов и подитогов) + по 1 подитогу на строку + 1 итоговая строка
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 2 + 2 + 1; // в источнике 2 строки (без итогов и подитогов) + по 1 подитогу на строку + 1 итоговая строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        System.out.println(testHelper.getLogger().getEntries().toString());
        //testHelper.execute(FormDataEvent.CALCULATE);

        //Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        //checkLoadData(testHelper.getDataRowHelper().getAll());
        //checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Кроме простого выполнения события других проверок нет, т.к. для формы консолидация выполняется сервисом
        testHelper.execute(FormDataEvent.COMPOSE);
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        int precision = 4;

        // графа 5
        String[] strColumns = {"docNumber"};

        // графа 8..12
        String[] numColumns = {"rateOfTheBankOfRussia", "taxAccountingCurrency", "taxAccountingRuble", "accountingCurrency", "ruble"};

        // графа 2, 7
        String[] refbookColumns = {"code", "currencyCode"};

        // графа 3, 6
        String[] dateColumns = {"date", "docDate"};

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            String expectedString = "test" + index;
            for (String alias : strColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedString, row.getCell(alias).getStringValue());
            }

            BigDecimal expectedNum = roundValue(index, precision);
            for (String alias : numColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(precision, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
            }

            BigDecimal expectedRefbook = new BigDecimal(index);
            for (String alias : refbookColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedRefbook, row.getCell(alias).getNumericValue());
            }

            for (String alias : dateColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertNotNull(msg, row.getCell(alias).getDateValue());
            }

            index++;
        }
    }

    // Округляет число до требуемой точности
    BigDecimal roundValue(Long value, int precision) {
        if (value != null) {
            return (BigDecimal.valueOf(value)).setScale(precision, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }
}