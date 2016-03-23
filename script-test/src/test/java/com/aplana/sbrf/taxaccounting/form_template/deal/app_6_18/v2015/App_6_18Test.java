package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_18.v2015;

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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 6.18
 */
public class App_6_18Test extends ScriptTestBase {
    private static final int TYPE_ID = 838;
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
        return getDefaultScriptTestMockHelper(App_6_18Test.class);
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
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // Проверка на заполнение граф
        // Заполнение граф сумма дохода, расхода - оба не заполнены
        // Проверка подитога
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Полное наименование с указанием ОПФ"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Признак взаимозависимости"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номер договора"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата договора"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номер сделки"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата заключения сделки"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Направленность сделки"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Признак физической поставки драгоценного металла"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Наименование драгоценного металла"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Внешнеторговая сделка"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Количество"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма доходов Банка по данным бухгалтерского учета, руб."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма расходов Банка по данным бухгалтерского учета, руб."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Итого стоимость без учета НДС, акцизов и пошлины, руб."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата совершения сделки"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 3. Проверка корректности даты договора
        // 4. Проверка корректности даты заключения сделки
        // 5. Проверка признака физической поставки
        // 8. Проверка количества
        // Проверка заполнения сумм доходов и расходов
        row.getCell("name").setValue(1L, null);
        row.getCell("dependence").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("dealFocus").setValue(1L, null);
        row.getCell("signPhis").setValue(2L, null);
        row.getCell("metalName").setValue(1L, null);
        row.getCell("foreignDeal").setValue(1L, null);
        row.getCell("count").setValue(0, null);
        row.getCell("incomeSum").setValue(0, null);
        row.getCell("outcomeSum").setValue(0, null);
        row.getCell("price").setValue(1, null);
        row.getCell("total").setValue(1, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2989"), null);

        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата договора", "01.01.1991", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата заключения сделки» должно быть не меньше значения графы «Дата договора» и не больше 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Признак физической поставки драгоценного металла» может содержать только одно из значений: ОМС, Поставочная сделка!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Количество» должна быть заполнена значением «1»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значения граф «Сумма доходов Банка по данным бухгалтерского учета, руб.», «Сумма расходов Банка по данным бухгалтерского учета, руб.» не должны одновременно быть равны «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD_EXT, 1, "Дата совершения сделки", "01.01.2014", "31.12.2014", "Дата заключения сделки"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("signPhis").setValue(3L, null);
        row.getCell("countryCodeNumeric").setValue(1L, null);
        row.getCell("regionCode").setValue(1L, null);
        row.getCell("count").setValue(1, null);
        row.getCell("incomeSum").setValue(-1, null);
        row.getCell("outcomeSum").setValue(null, null);
        row.getCell("price").setValue(2, null);
        row.getCell("total").setValue(-1, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма расходов Банка по данным бухгалтерского учета, руб."), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графы «Код страны по классификатору ОКСМ (цифровой)» (13.1, 14.1) должны быть заполнены, т.к. в графе «Признак физической поставки драгоценного металла» указано значение «Поставочная сделка»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. \"Регион (код)\"» (13.2) не должна быть заполнена, т.к указанная страна отправки не Россия!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Должна быть заполнена одна из граф «Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. Город» (13.3) или «Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. Населенный пункт (село, поселок и т.д.)» (13.4)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Должна быть заполнена одна из граф «Место совершения сделки (адрес места доставки (разгрузки драгоценного металла). Город» (14.3) или «Место совершения сделки (адрес места доставки (разгрузки драгоценного металла). Населенный пункт (село, поселок и т.д.)» (14.4)!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        //  для прохождения ЛП:
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealFocus").setValue(1L, null);
        row.getCell("signPhis").setValue(1L, null);
        row.getCell("countryCodeNumeric").setValue(null, null);
        row.getCell("regionCode").setValue(null, null);
        row.getCell("metalName").setValue(1L, null);
        row.getCell("foreignDeal").setValue(1L, null);
        row.getCell("incomeSum").setValue(1, null);
        row.getCell("outcomeSum").setValue(3, null);
        row.getCell("price").setValue(1, null);
        row.getCell("total").setValue(1, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(i, entries.size());
        Assert.assertEquals(1L, row.getCell("dependence").getValue());
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

        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        testHelper.getLogger().clear();
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("dependence").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(0).getCell("count").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(0).getCell("total").getNumericValue().doubleValue(), 0);
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(4, dataRows.get(0).getCell("total").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(1, dataRows.size());
    }
}

