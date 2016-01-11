package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_13.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
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
 * 6.13. Приобретение услуг по организации и проведению торгов по реализации имущества.
 */
public class App_6_13Test extends ScriptTestBase {
    private static final int TYPE_ID = 826;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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
        return getDefaultScriptTestMockHelper(App_6_13Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_6_13//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        dataRows.clear();

        // 1. Проверка заполнения обязательных полей
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Полное наименование юридического лица с указанием ОПФ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма расходов Банка, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Количество сделок» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Стоимость» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 2. Проверка суммы расходов
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(-1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("count").setValue(1, null);
        row.getCell("price").setValue(-1, null);
        row.getCell("cost").setValue(-1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Сумма расходов Банка, руб.» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 3. Проверка корректности даты договора
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.1990"), null);
        row.getCell("count").setValue(1, null);
        row.getCell("price").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Графа «Дата договора» должна принимать значение из следующего диапазона: 01.01.1991 - 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 4. Проверка цены
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("count").setValue(1, null);
        row.getCell("price").setValue(2, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Цена» должно быть равно значению графы «Сумма расходов Банка, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 5. Проверка стоимости
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("count").setValue(1, null);
        row.getCell("price").setValue(1, null);
        row.getCell("cost").setValue(2, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Стоимость» должно быть равно значению графы «Сумма расходов Банка, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 6. Проверка количества
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("count").setValue(0, null);
        row.getCell("price").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Количество сделок» должно быть больше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 7. Проверка корректности даты совершения сделки
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("count").setValue(1, null);
        row.getCell("price").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата договора» и не больше 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 8. Проверка итоговых значений по фиксированной строке «Итого»
        row.getCell("name").setValue(1L, null);
        row.getCell("outcomeSum").setValue(1, null);
        row.getCell("docNumber").setValue("string1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("count").setValue(1, null);
        row.getCell("price").setValue(1, null);
        row.getCell("cost").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setIndex(2);
        totalRow.setAlias("total");
        dataRows.add(totalRow);
        totalRow.getCell("outcomeSum").setValue(2, null);
        totalRow.getCell("count").setValue(2, null);
        totalRow.getCell("cost").setValue(2, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Сумма расходов Банка, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Количество сделок»!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Стоимость»!", entries.get(i++).getMessage());
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
    public void importExcelTest() throws ParseException {
        mockBeforeImport();
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<String> aliases = Arrays.asList("incomeSum", "outcomeSum", "docNumber", "docDate", "count", "price", "cost", "dealDoneDate");
        int expected = 4; // ожидается 4 строк: 3 из файла + 1 итоговая строка
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());
    }

    @Test
    public void importTransportFileTest() throws ParseException {
        mockBeforeImport();
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        int expected = 4; // ожидается 4 строк: 3 из файла + 1 итоговая строка
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());
    }

    void mockBeforeImport(){
        Long refbookId = 520L;

        when(testHelper.getRefBookFactory().get(refbookId)).thenAnswer(
                new Answer<RefBook>() {

                    @Override
                    public RefBook answer(InvocationOnMock invocation) throws Throwable {
                        RefBook refBook = new RefBook();
                        ArrayList<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
                        RefBookAttribute e = new RefBookAttribute();
                        e.setAlias("INN");
                        e.setName("ИНН/ КИО");
                        attributes.add(e);
                        e = new RefBookAttribute();
                        e.setAlias("NAME");
                        e.setName("Наименование");
                        attributes.add(e);
                        refBook.setAttributes(attributes);
                        return refBook;
                    }
                }
        );

        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refbookId);
        when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        String str = ((String) invocation.getArguments()[2]).split("\'")[1];
                        char iksr = str.charAt(0);
                        long id = 0;
                        switch (iksr) {
                            case 'A':  id = 1L;
                                break;
                            case 'B':  id = 2L;
                                break;
                            case 'C':  id = 3L;
                                break;
                            default: str = null;
                        }
                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, id));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, str));
                        map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, str));
                        result.add(map);
                        return result;
                    }
                });
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) throws ParseException {
        // графа 2
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(2).getCell("name").getNumericValue().longValue());
        // графа 5
        Assert.assertEquals(1L, dataRows.get(0).getCell("outcomeSum").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("outcomeSum").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(2).getCell("outcomeSum").getNumericValue().longValue());
        // графа 6
        Assert.assertEquals("string1", dataRows.get(0).getCell("docNumber").getStringValue());
        Assert.assertEquals("string2", dataRows.get(1).getCell("docNumber").getStringValue());
        Assert.assertEquals("string3", dataRows.get(2).getCell("docNumber").getStringValue());
        // графа 7
        Assert.assertEquals(sdf.parse("01.01.2016"), dataRows.get(0).getCell("docDate").getDateValue());
        Assert.assertEquals(sdf.parse("02.01.2016"), dataRows.get(1).getCell("docDate").getDateValue());
        Assert.assertEquals(sdf.parse("03.01.2016"), dataRows.get(2).getCell("docDate").getDateValue());
        // графа 8
        Assert.assertEquals(11L, dataRows.get(0).getCell("count").getNumericValue().longValue());
        Assert.assertEquals(22L, dataRows.get(1).getCell("count").getNumericValue().longValue());
        Assert.assertEquals(33L, dataRows.get(2).getCell("count").getNumericValue().longValue());
        // графа 9
        Assert.assertEquals(111L, dataRows.get(0).getCell("price").getNumericValue().longValue());
        Assert.assertEquals(222L, dataRows.get(1).getCell("price").getNumericValue().longValue());
        Assert.assertEquals(333L, dataRows.get(2).getCell("price").getNumericValue().longValue());
        // графа 10
        Assert.assertEquals(1111L, dataRows.get(0).getCell("cost").getNumericValue().longValue());
        Assert.assertEquals(2222L, dataRows.get(1).getCell("cost").getNumericValue().longValue());
        Assert.assertEquals(3333L, dataRows.get(2).getCell("cost").getNumericValue().longValue());
        // графа 11
        Assert.assertEquals(sdf.parse("04.01.2016"), dataRows.get(0).getCell("dealDoneDate").getDateValue());
        Assert.assertEquals(sdf.parse("05.01.2016"), dataRows.get(1).getCell("dealDoneDate").getDateValue());
        Assert.assertEquals(sdf.parse("06.01.2016"), dataRows.get(2).getCell("dealDoneDate").getDateValue());
    }
}

