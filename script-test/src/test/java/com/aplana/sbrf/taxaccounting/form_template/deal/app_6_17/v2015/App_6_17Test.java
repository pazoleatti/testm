package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_17.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
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

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.checkAndReadFile;
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.checkHeaderSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 6.17 Купля-продажа иностранной валюты
 */
public class App_6_17Test extends ScriptTestBase {
    private static final int TYPE_ID = 811;
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
        return getDefaultScriptTestMockHelper(App_6_17Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_6_17//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка на заполнение граф
        // 2. Заполнение граф 13 и 14 (сумма дохода, расхода) - оба не заполнены
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Полное наименование юридического лица с указанием ОПФ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата заключения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код валюты по сделке» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код страны происхождения предмета сделки по классификатору ОКСМ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма расходов Банка по данным бухгалтерского учета, руб.» должна быть заполнена, если не заполнена графа «Сумма доходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 5. Проверка даты совершения сделки
        // 6. Заполнение граф (сумма дохода, расхода) - оба заполнены
        // 8. Корректность даты (заключения) сделки
        // 11. Корректность даты совершения сделки
        // 12. Проверка диапазона дат
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2989"), null);
        row.getCell("currencyCode").setValue(1L, null);
        row.getCell("income").setValue(1, null);
        row.getCell("outcome").setValue(1, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Графа «Код страны происхождения предмета сделки по классификатору ОКСМ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма расходов Банка по данным бухгалтерского учета, руб.» не может быть заполнена одновременно с графой «Сумма доходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата заключения сделки» должно быть не меньше значения графы «Дата договора»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата заключения сделки»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Дата, указанная в графе «Дата совершения сделки» (01.01.2989), должна относиться к отчетному периоду текущей формы  (01.01.2014 - 31.12.2014)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение даты атрибута «Дата договора» должно принимать значение из следующего диапазона: 01.01.1900 - 31.12.2099", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 6. Заполнение граф 13 и 14 (сумма дохода, расхода) - валидная
        // 8. Корректность даты (заключения) сделки - валидная
        // 11. Корректность даты совершения сделки - валидная
        // 12. Проверка диапазона дат - валидная
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("02.01.2014"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("04.01.2014"), null);
        row.getCell("income").setValue(null, null);
        row.getCell("outcome").setValue(0, null);
        row.getCell("currencyCode").setValue(1L, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Графа «Код страны происхождения предмета сделки по классификатору ОКСМ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();


        // для попадания в ЛП:
        // 7. Проверка положительной суммы дохода +
        // Проверка на несоответствие строки подитога
        row.getCell("income").setValue(-1, null);
        row.getCell("outcome").setValue(null, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Графа «Код страны происхождения предмета сделки по классификатору ОКСМ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма доходов Банка по данным бухгалтерского учета, руб.» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Сумма доходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 7. Проверка положительной суммы расхода
        row.getCell("income").setValue(null, null);
        row.getCell("outcome").setValue(-1, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Графа «Код страны происхождения предмета сделки по классификатору ОКСМ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма расходов Банка по данным бухгалтерского учета, руб.» должно быть больше или равно «0»!", entries.get(i++).getMessage());
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
        // TODO тесты для логики поиска по iksr
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

        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
        checkAfterCalc(testHelper.getDataRowHelper().getAll());

    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("name").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(2).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("currencyCode").getNumericValue().longValue());
    }
    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(7, dataRows.get(1).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, dataRows.get(2).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(17, dataRows.get(3).getCell("price").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(2, dataRows.get(0).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(7, dataRows.get(1).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12, dataRows.get(2).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(17, dataRows.get(3).getCell("cost").getNumericValue().doubleValue(), 0);
    }
}

