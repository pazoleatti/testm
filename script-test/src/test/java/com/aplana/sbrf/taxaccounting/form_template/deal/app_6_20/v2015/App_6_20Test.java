package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_20.v2015;

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
 * 6.20. Привлечение средств на межбанковском рынке
 */
public class App_6_20Test extends ScriptTestBase {
    private static final int TYPE_ID = 831;
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
        return getDefaultScriptTestMockHelper(App_6_20Test.class);
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
        // 1. Проверка на заполнение граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Полное наименование юридического лица с указанием ОПФ» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Номер договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата заключения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Количество» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма расходов Банка по данным бухгалтерского учета, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена(тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Итого стоимость без учета НДС, акцизов и пошлины, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 3. Проверка корректности даты заключения сделки
        // 8. Корректность даты (заключения) сделки
        // 11. Корректность даты совершения сделки
        // 12. Проверка диапазона дат
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("01.01.2990"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2989"), null);
        row.getCell("count").setValue(1L, null);
        row.getCell("outcome").setValue(1, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Графа «Цена(тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Итого стоимость без учета НДС, акцизов и пошлины, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата договора» должна принимать значение из следующего диапазона: 01.01.1991 - 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата договора» и не больше 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Цена(тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.» должно быть равно значению графы «Сумма расходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Итого стоимость без учета НДС, акцизов и пошлины, руб.» должно быть равно значению графы «Сумма расходов Банка по данным бухгалтерского учета, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата совершения сделки» должно быть не меньше значения графы «Дата договора» и не больше 31.12.2014!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 6. Заполнение графы 11 (сумма расхода) - заполнена
        // 8. Корректность даты (заключения) сделки - валидная
        // 11. Корректность даты совершения сделки - валидная
        // 12. Проверка диапазона дат - валидная
        row.getCell("name").setValue(1L, null);
        row.getCell("docNumber").setValue("string", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealNumber").setValue("string", null);
        row.getCell("dealDate").setValue(sdf.parse("02.01.2014"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("04.01.2014"), null);
        row.getCell("outcome").setValue(0, null);
        row.getCell("count").setValue(1L, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        checkLogger();

        // для попадания в ЛП:
        // 4. Проверка количества
        // 5. Проверка суммы расходов
        row.getCell("count").setValue(-1, null);
        row.getCell("outcome").setValue(-1, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        entries = testHelper.getLogger().getEntries();
        i = 0;
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
        checkLogger();
        checkAfterCalc(testHelper.getDataRowHelper().getAll());

    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(2).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("name").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("count").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(1).getCell("count").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(2).getCell("count").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(3).getCell("count").getNumericValue().longValue());
    }
    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("count").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(1).getCell("count").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(2).getCell("count").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(3).getCell("count").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4, dataRows.get(4).getCell("count").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(1, dataRows.get(0).getCell("outcome").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(1).getCell("outcome").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(2).getCell("outcome").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(3).getCell("outcome").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4, dataRows.get(4).getCell("outcome").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(1, dataRows.get(0).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(1).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(2).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, dataRows.get(3).getCell("cost").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4, dataRows.get(4).getCell("cost").getNumericValue().doubleValue(), 0);
    }
}

