package com.aplana.sbrf.taxaccounting.form_template.income.rnu_110.v2015;

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
 * РНУ 110
 */
public class Rnu_110Test extends ScriptTestBase {
    private static final int TYPE_ID = 822;
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
        return getDefaultScriptTestMockHelper(Rnu_110Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//rnu_110//v2015//"));
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
        Assert.assertEquals("Строка 1: Графа «Наименование Взаимозависимого лица (резидента оффшорной зоны)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата совершения операции» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код налогового учёта» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Основание для совершения операции. Номер» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Основание для совершения операции. Дата» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Арендная ставка» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Арендная ставка, признаваемая рыночной для целей налогообложения» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма фактически начисленной арендной платы» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма арендной платы, соответствующая рыночному уровню» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма доначисления арендной платы до рыночного уровня арендной ставки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // Для попадания в ЛП
        i = 0;
        row.getCell("name").setValue(1L, null);
        row.getCell("code").setValue("1", null);
        row.getCell("transDoneDate").setValue(sdf.parse("10.11.2015"), null);
        row.getCell("reasonNumber").setValue("string", null);
        row.getCell("reasonDate").setValue(sdf.parse("11.11.2015"), null);
        row.getCell("rent").setValue(-1, null);
        row.getCell("taxRent").setValue(-1, null);
        row.getCell("sum1").setValue(-1, null);
        row.getCell("sum2").setValue(-1, null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Строка 1: Графа «Сумма доначисления арендной платы до рыночного уровня арендной ставки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Дата по графе «Дата совершения операции» должна принимать значение из диапазона 01.01.2014 - 31.12.2014 и быть больше либо равна дате по графе «Основание для совершения операции. Дата»!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Основание для совершения операции. Дата","01.01.1991", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Арендная ставка» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Арендная ставка, признаваемая рыночной для целей налогообложения» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма фактически начисленной арендной платы» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма арендной платы, соответствующая рыночному уровню» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // Для прохождения всех ЛП
        i = 0;
        row.getCell("name").setValue(1L, null);
        row.getCell("code").setValue("1", null);
        row.getCell("transDoneDate").setValue(sdf.parse("11.11.2014"), null);
        row.getCell("reasonNumber").setValue("string", null);
        row.getCell("reasonDate").setValue(sdf.parse("11.11.2014"), null);
        row.getCell("rent").setValue(1L, null);
        row.getCell("taxRent").setValue(1L, null);
        row.getCell("sum1").setValue(1L, null);
        row.getCell("sum2").setValue(1L, null);
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
        Assert.assertEquals(1L, dataRows.get(0).getCell("sum1").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(0).getCell("sum2").getNumericValue().longValue());

    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(0, dataRows.get(0).getCell("sum3").getNumericValue().longValue());
    }
}

