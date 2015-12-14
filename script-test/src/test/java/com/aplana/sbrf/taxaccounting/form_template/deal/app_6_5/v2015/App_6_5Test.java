package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_5.v2015;

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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 6.5. Приобретение услуг по техническому обслуживанию нежилых помещений
 */
public class App_6_5Test extends ScriptTestBase {
    private static final int TYPE_ID = 814;
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
        return getDefaultScriptTestMockHelper(App_6_5Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_6_5//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка заполнения граф
        // 3. Проверка заполнения населенного пункта
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        printLog();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Полное наименование юридического лица с указанием ОПФ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма расходов Банка, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Страна (код)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Стоимость» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Населенный пункт» не заполнена! Выполнение расчета невозможно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 2. Проверка суммы расходов
        // 4. Проверка цены с учетом количества
        // 6. Проверка стоимости
        // 7. Корректность даты совершения сделки относительно даты договора
        // 8. Проверка года совершения сделки
        // 9. Проверка диапазона дат
        row.getCell("name").setValue(1L, null);
        row.getCell("country").setValue(1L, null);
        row.getCell("settlement").setValue("string", null);
        row.getCell("sum").setValue(-5, null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("count").setValue(2, null);
        row.getCell("price").setValue(55, null);
        row.getCell("cost").setValue(555, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2989"), null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Сумма расходов Банка, руб.» должно быть больше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Цена» должно быть равно отношению графы «Сумма расходов Банка, руб.» к графе «Количество»! Выполнение расчета невозможно!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма расходов Банка, руб.» должно быть равно значению графы «Стоимость»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата договора»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Год, указанный по графе «Дата совершения сделки» (2989), должен относиться к календарному году текущей формы (2014)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение даты атрибута «Дата договора» должно принимать значение из следующего диапазона: 01.01.1991 - 31.12.2099", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 5. Проверка цены без учета количества
        row.getCell("count").setNumericValue(null);
        row.getCell("sum").setValue(1, null);
        row.getCell("name").setValue(1L, null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("cost").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Цена» должно быть равно значению графы «Сумма расходов Банка, руб.»! Выполнение расчета невозможно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("price").setValue(1, null);

        // проверка суммы расходов:
        row.getCell("sum").setValue(1, null);
        row.getCell("price").setValue(7.77, null);
        row.getCell("cost").setValue(0.86, null);
        row.getCell("count").setValue(9, null);
        i = 0;
        testHelper.execute(FormDataEvent.CALCULATE);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // Расчет пустой (в импорте - растчет заполненной)
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        mockBeforeImport();
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<String> aliases = Arrays.asList("sum", "docNumber", "docDate", "city", "settlement", "count", "price", "cost", "dealDoneDate");
        // ожидается 5 строк: 4 из файла + 1 итоговая строка
        int expected = 4 + 1;
        try {
            defaultCheckLoadData(aliases, expected);
        } catch (Exception e) {
            printLog();
            e.printStackTrace();
        }

        checkLogger();
        // "count", "name"
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
    }

    //@Test
    public void importTransportFileTest() {
        mockBeforeImport();
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        List<String> aliases = Arrays.asList("sum", "docNumber", "docDate", "city", "settlement", "count", "price", "cost", "dealDoneDate");
        // ожидается 5 строк
        int expected = 5;
        defaultCheckLoadData(aliases, expected);

        checkLoadData(testHelper.getDataRowHelper().getAll());
    }

    void mockBeforeImport() {
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

                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "A"));
                        map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "A"));
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "B"));
                        map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "B"));
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "C"));
                        map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "C"));
                        result.add(map);

                        return result;
                    }
                });
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("name").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2, dataRows.get(0).getCell("count").getNumericValue().intValue());
        Assert.assertEquals(6, dataRows.get(1).getCell("count").getNumericValue().intValue());
        Assert.assertEquals(10, dataRows.get(2).getCell("count").getNumericValue().intValue());
        Assert.assertEquals(14, dataRows.get(3).getCell("count").getNumericValue().intValue());

        double price0 = 2 / 4;
        double price1 = 6 / 7;
        double price2 = 10 / 11;
        double price3 = 14 / 15;
        Assert.assertEquals(price0, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 2);
        Assert.assertEquals(price1, dataRows.get(1).getCell("price").getNumericValue().doubleValue(), 2);
        Assert.assertEquals(price2, dataRows.get(2).getCell("price").getNumericValue().doubleValue(), 2);
        Assert.assertEquals(price3, dataRows.get(3).getCell("price").getNumericValue().doubleValue(), 2);

        Assert.assertEquals(1, dataRows.get(0).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(5, dataRows.get(1).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(9, dataRows.get(2).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(13, dataRows.get(3).getCell("cost").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(28, dataRows.get(4).getCell("sum").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(32, dataRows.get(4).getCell("count").getNumericValue().intValue());
        Assert.assertEquals(28, dataRows.get(4).getCell("cost").getNumericValue().doubleValue(), 0);
    }
}

