package com.aplana.sbrf.taxaccounting.form_template.income.rnu_114.v2015;

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
 * РНУ 114.
 */
public class Rnu_114Test extends ScriptTestBase {
    private static final int TYPE_ID = 829;
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
        return getDefaultScriptTestMockHelper(Rnu_114Test.class);
    }

    @Before
    public void mockServices() {
        final Long refbookId = 27L;
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refbookId);
        when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // вынимаем значение кода из фильтра LOWER(CODE) = LOWER('$code')
                String filter = (String) invocation.getArguments()[2];
                String codeValue = filter.substring(filter.indexOf("('") + 2, filter.indexOf("')"));
                if (codeValue == null) {
                    return new PagingResult<Map<String, RefBookValue>>();
                }
                final Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(refbookId);
                for (Map<String, RefBookValue> row : records.values()) {
                    if (codeValue.equals(row.get("CODE").getStringValue())) {
                        List<Map<String, RefBookValue>> tmpRecords = Arrays.asList(row);
                        return new PagingResult<Map<String, RefBookValue>>(tmpRecords);
                    }
                }
                return new PagingResult<Map<String, RefBookValue>>();
            }});
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
        // Проверка на заполнение необходимых граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Наименование Взаимозависимого лица/резидента оффшорной зоны"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Код классификации дохода"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номер кредитного договора"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата кредитного договора"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Остаток задолженности"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Валюта"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Курс валюты Банка России"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Количество календарных дней в периоде"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "База года (360/365/366)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Совокупная процентная ставка"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Рыночная ставка"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма фактического процентного дохода"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();


        // Проверка даты кредитного договора
        // Проверка количества календарных дней
        // Проверка базы года
        // Проверка соотношения процентных ставок
        row.getCell("name").setValue(1L, null);
        row.getCell("code").setValue("1", null);
        row.getCell("docNumber").setValue("1", null);
        row.getCell("docDate").setValue(sdf.parse("11.11.1990"), null);
        row.getCell("residual").setValue(1, null);
        row.getCell("currencyCode").setValue(1, null);
        row.getCell("courseCB").setValue(1, null);
        row.getCell("period").setValue(0, null);
        row.getCell("base").setValue(333, null);
        row.getCell("rate1").setValue(2, null);
        row.getCell("rate2").setValue(1, null);
        row.getCell("sum2").setValue(1, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата кредитного договора","01.01.1991", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Количество календарных дней в периоде» должно быть больше нуля!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «База года (360/365/366)» должно быть равно «360» или «365» или «366»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Совокупная процентная ставка» должно быть меньше или равно значению графы «Рыночная ставка»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // Для прохождения всех ЛП
        i = 0;
        row.getCell("name").setValue(1L, null);
        row.getCell("code").setValue("1", null);
        row.getCell("docNumber").setValue("1", null);
        row.getCell("docDate").setValue(sdf.parse("11.11.2014"), null);
        row.getCell("residual").setValue(1, null);
        row.getCell("currencyCode").setValue(1, null);
        row.getCell("courseCB").setValue(1, null);
        row.getCell("period").setValue(10, null);
        row.getCell("base").setValue(365, null);
        row.getCell("rate1").setValue(1, null);
        row.getCell("rate2").setValue(1, null);
        row.getCell("sum2").setValue(1, null);
        testHelper.execute(FormDataEvent.CALCULATE);
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

        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        checkLogger();

    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("name").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(0.14, dataRows.get(2).getCell("sum1").getNumericValue().doubleValue(), 0);
    }
}

