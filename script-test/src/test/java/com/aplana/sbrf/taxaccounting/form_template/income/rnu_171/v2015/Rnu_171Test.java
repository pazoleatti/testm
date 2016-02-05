package com.aplana.sbrf.taxaccounting.form_template.income.rnu_171.v2015;

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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 843 - РНУ-171. Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока
 * погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
 */
public class Rnu_171Test extends ScriptTestBase {
    private static final int TYPE_ID = 843;
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
        return getDefaultScriptTestMockHelper(Rnu_171Test.class);
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
        // 1. Проверка на заполнение необходимых граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Наименование контрагента» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер договора цессии» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора цессии» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Стоимость права требования (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Стоимость права требования, списанного за счёт резервов (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата погашения основного долга» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата уступки права требования» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Доход (выручка) от уступки права требования (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Финансовый результат уступки права требования (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код налогового учета» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Рыночная цена прав требования для целей налогообложения (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Финансовый результат, рассчитанный исходя из рыночной цены для целей налогообложения (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Корректировка финансового результата (руб. коп.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «графа 12 не задана» не имеет строки подитога!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка даты договора цессии
        // Проверка даты погашения и уступки
        // Проверка положительности суммы дохода
        // Проверка финансовых результатов
        // Проверка кода налогового учета
        // Проверка корректности финансового результата из рыночной цены
        row.getCell("name").setValue(1L, null);
        row.getCell("dealNum").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("03.03.2015"), null);
        row.getCell("repaymentDate").setValue(sdf.parse("01.03.2015"), null);
        row.getCell("concessionsDate").setValue(sdf.parse("02.03.2015"), null);
        row.getCell("income").setValue(-1L, null);
        row.getCell("cost").setValue(-1L, null);
        row.getCell("costReserve").setValue(-1L, null);
        row.getCell("marketPrice").setValue(-1L, null);
        row.getCell("finResult").setValue(2L, null);
        row.getCell("finResultTax").setValue(1L, null);
        row.getCell("code").setValue("10345", null);
        row.getCell("incomeCorrection").setValue(0L, null);
        testHelper.execute(FormDataEvent.CHECK);

        i = 0;
        Assert.assertEquals("Строка 1: Дата по графе «Дата договора цессии» должна принимать значение из диапазона: 01.01.1991 - 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Дата по графе «Дата погашения основного долга» должна принимать значение из диапазона: 01.01.2014 - 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата погашения основного долга» должно быть больше или равно значения графы «Дата договора цессии»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Дата по графе «Дата уступки права требования» должна принимать значение из диапазона: 01.01.2014 - 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата уступки права требования» должно быть больше или равно значения графы «Дата договора цессии»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Доход (выручка) от уступки права требования (руб. коп.)» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Корректировка финансового результата (руб. коп.)» заполнена значением «0», т.к. не выполнен порядок заполнения графы!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Финансовый результат уступки права требования (руб. коп.)» должно равняться выражению: «Доход (выручка) от уступки права требования (руб. коп.)» - («Стоимость права требования (руб. коп.)» - «Стоимость права требования, списанного за счёт резервов (руб. коп.)»)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код налогового учета» должна принимать значение из следующего списка: «10360» или «10361»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Финансовый результат, рассчитанный исходя из рыночной цены для целей налогообложения (руб. коп.)» должно равняться выражению: «Рыночная цена прав требования для целей налогообложения (руб. коп.)» - («Стоимость права требования (руб. коп.)» - «Стоимость права требования, списанного за счёт резервов (руб. коп.)»)!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «10345» не имеет строки подитога!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка финансового результата
        row.getCell("dealDate").setValue(sdf.parse("01.03.2014"), null);
        row.getCell("cost").setValue(1L, null);
        row.getCell("costReserve").setValue(1L, null);
        row.getCell("repaymentDate").setValue(sdf.parse("01.03.2014"), null);
        row.getCell("concessionsDate").setValue(sdf.parse("02.03.2014"), null);
        row.getCell("income").setValue(1L, null);
        row.getCell("code").setValue("10360", null);
        row.getCell("marketPrice").setValue(2L, null);
        row.getCell("finResult").setValue(1L, null);
        row.getCell("finResultTax").setValue(2L, null);
        DataRow<Cell> subTotal = formData.createDataRow();
        dataRows.add(subTotal);
        subTotal.setAlias("itg#1");
        subTotal.getCell("incomeCorrection").setValue(0L, null);
        subTotal.setIndex(2);
        testHelper.execute(FormDataEvent.CHECK);

        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Корректировка финансового результата (руб. коп.)» должно равняться разнице между графой «Финансовый результат, рассчитанный исходя из рыночной цены для целей налогообложения (руб. коп.)» и «Финансовый результат уступки права требования (руб. коп.)»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        row.getCell("cost").setValue(3L, null);
        row.getCell("costReserve").setValue(1L, null);
        row.getCell("finResult").setValue(-1L, null);
        row.getCell("finResultTax").setValue(0L, null);
        testHelper.execute(FormDataEvent.CHECK);

        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Корректировка финансового результата (руб. коп.)» должно равняться разнице между графой «Рыночная цена прав требования для целей налогообложения (руб. коп.)» по модулю и «Финансовый результат уступки права требования (руб. коп.)» по модулю!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2. Для прохождения всех ЛП
        row.getCell("cost").setValue(1L, null);
        row.getCell("costReserve").setValue(1L, null);
        row.getCell("finResult").setValue(1L, null);
        row.getCell("finResultTax").setValue(2L, null);
        row.getCell("incomeCorrection").setValue(1L, null);
        subTotal.getCell("incomeCorrection").setValue(1L, null);
        row.getCell("code").setValue("10361", null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals(i, entries.size());
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
        List<String> aliases = Arrays.asList("dealNum", "dealDate", "cost", "costReserve", "repaymentDate", "concessionsDate", "income",
                "finResult", "marketPrice", "finResultTax", "incomeCorrection");
        defaultCheckLoadData(aliases, 3);
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        Assert.assertEquals(3, testHelper.getDataRowHelper().getCount());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals("10345", dataRows.get(0).getCell("code").getStringValue());
        Assert.assertEquals("10345", dataRows.get(1).getCell("code").getStringValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2.00, dataRows.get(0).getCell("incomeCorrection").getNumericValue().doubleValue(), 0.001);
        Assert.assertEquals(2.00, dataRows.get(1).getCell("incomeCorrection").getNumericValue().doubleValue(), 0.001);
        Assert.assertEquals(2.00, dataRows.get(2).getCell("incomeCorrection").getNumericValue().doubleValue(), 0.001);
    }
}

