package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_16.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 6.16. Поставочные срочные сделки, базисным активом которых является иностранная валюта
 */
public class App_6_16Test extends ScriptTestBase {
    private static final int TYPE_ID = 839;
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
        return getDefaultScriptTestMockHelper(App_6_16Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_6_16//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // Проверка заполнения обязательных полей
        // Проверка доходов и расходов (a)
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
        Assert.assertEquals("Строка 1: Графа «Дата заключения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Вид срочной сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код валюты по сделке» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код страны происхождения предмета сделки по классификатору ОКСМ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена (тариф) за единицу измерения, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Итого стоимость, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Должна быть заполнена хотя бы одна из граф «Сумма доходов Банка по данным бухгалтерского учета, руб.», «Сумма расходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «графа 2 не задана, графа 5 не задана, графа 6 не задана, графа 9 не задана» не имеет строки подитога!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // Проверка корректности даты договора
        // Проверка корректности даты заключения сделки
        // Проверка доходов и расходов (b)
        // Проверка цены и стоимости (a)
        // Проверка корректности даты совершения сделки
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("docNumber", null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("dealNumber").setValue("dealNumber", null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("dealType").setValue(1L, null);
        row.getCell("currencyCode").setValue(1L, null);
        row.getCell("countryDealCode").setValue(1L, null);
        row.getCell("incomeSum").setValue(-1, null);
        row.getCell("price").setValue(555, null);
        row.getCell("cost").setValue(555, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2989"), null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата договора","01.01.1991", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата заключения сделки» должно быть не меньше значения графы «Дата договора» и не больше 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма доходов Банка по данным бухгалтерского учета, руб.» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Цена (тариф) за единицу измерения, руб.» должно быть равно значению графы «Сумма доходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Итого стоимость, руб.» должно быть равно значению графы «Сумма доходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата договора» и не больше 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «A, docNumber, 02.01.2990, 1» не имеет строки подитога!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("name").setValue(1L, null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("incomeSum").setValue(555, null);
        row.getCell("price").setValue(555, null);
        row.getCell("cost").setValue(555, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);

        i = 0;
        testHelper.execute(FormDataEvent.CALCULATE);
        testHelper.execute(FormDataEvent.CHECK);
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
    public void importExcelTest() {
        mockBeforeImport();
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<String> aliases = Arrays.asList("docNumber", "docDate","dealNumber", "dealDate", "incomeSum","outcomeSum",
                "price", "cost", "dealDoneDate");
        // ожидается 5 строк: 4 из файла + 1 итоговая строка
        int expected = 4 + 1;
        defaultCheckLoadData(aliases, expected);

        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("name").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("dealType").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(1).getCell("dealType").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("dealType").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("dealType").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(1).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("currencyCode").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("countryDealCode").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(1).getCell("countryDealCode").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("countryDealCode").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(3).getCell("countryDealCode").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(2).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(4).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(6).getCell("price").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(1, dataRows.get(0).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(2).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(4).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(6).getCell("cost").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(9, dataRows.size());
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
                            case 'A':
                                id = 1L;
                                break;
                            case 'B':
                                id = 2L;
                                break;
                            case 'C':
                                id = 3L;
                                break;
                            default:
                                str = null;
                        }
                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, id));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, str));
                        map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, str));
                        result.add(map);
                        return result;
                    }
                });
        when(provider.getRecordData(anyLong())).thenAnswer(new Answer<Map<String, RefBookValue>>() {
            @Override
            public Map<String, RefBookValue> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Long id = (Long) invocationOnMock.getArguments()[0];
                Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                String str;
                switch (id.intValue()) {
                    case 1 : str = "A"; break;
                    case 2 : str = "B"; break;
                    case 3 : str = "C"; break;
                    default : str = "";
                }
                map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, id));
                map.put("INN", new RefBookValue(RefBookAttributeType.STRING, str));
                map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, str));
                return map;
            }
        });
    }
}

