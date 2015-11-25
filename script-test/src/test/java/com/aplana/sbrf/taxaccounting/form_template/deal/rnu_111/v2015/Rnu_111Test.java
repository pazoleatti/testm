package com.aplana.sbrf.taxaccounting.form_template.deal.rnu_111.v2015;

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

import java.awt.datatransfer.DataFlavor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * РНУ 111. Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению
 * межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон процентных ставок, не соответствующих рыночному уровню
 */
public class Rnu_111Test extends ScriptTestBase {
    private static final int TYPE_ID = 808;
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
        return getDefaultScriptTestMockHelper(Rnu_111Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//rnu_111//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка на заполнение необходимых граф
        // + проверка на невозможность автозаполнения граф 8, 16,17
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Наименование Взаимозависимого лица (резидента оффшорной зоны)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код налогового учёта» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Основание для совершения операции. номер» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Основание для совершения операции. дата» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма кредита (ед. валюты)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Валюта» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Срок» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Процентная ставка, (% годовых)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма фактически начисленного дохода (руб.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Процентная ставка, признаваемая рыночной для целей налогообложения (% годовых)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма дохода, соответствующая рыночному уровню (руб.)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Отклонение процентной ставки от рыночного уровня, (% годовых)»: выполнение расчета невозможно, " +
                "так как не заполнена используемая в расчете графа «Процентная ставка, (% годовых)», «Процентная ставка, признаваемая рыночной для целей налогообложения (% годовых)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма доначисления дохода до рыночного уровня процентной ставки (руб.)»: выполнение расчета невозможно, " +
                "так как не заполнена используемая в расчете графа «Сумма фактически начисленного дохода (руб.)», «Сумма дохода, соответствующая рыночному уровню (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «База для расчёта процентного дохода (дней в году)»: выполнение расчета невозможно, " +
                "так как не заполнена используемая в расчете графа «Основание для совершения операции. дата»!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «ВЗЛ/РОЗ не задано» не имеет строки подитога!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        //2. Для прохождения всех ЛП
        i = 0;
        row.getCell("name").setValue(1L, null);
        row.getCell("code").setValue(1L, null);
        row.getCell("reasonNumber").setValue("string", null);
        row.getCell("reasonDate").setValue(sdf.parse("11.11.2016"), null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("currency").setValue(1L, null);
        row.getCell("time").setValue(1L, null);
        row.getCell("rate").setValue(1L, null);
        row.getCell("sum1").setValue(1L, null);
        row.getCell("rate1").setValue(1L, null);
        row.getCell("sum2").setValue(1L, null);
        testHelper.execute(FormDataEvent.CALCULATE);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 3. Проверка -  графа 13, 15 больше или равно 0
        row.getCell("sum1").setValue(-1, null);
        row.getCell("sum2").setValue(-1, null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Сумма фактически начисленного дохода (руб.)» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма дохода, соответствующая рыночному уровню (руб.)» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Неверное итоговое значение по группе «A» в графе «Сумма фактически начисленного дохода (руб.)»", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Сумма фактически начисленного дохода (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно в графе «Сумма дохода, соответствующая рыночному уровню (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 4. Проверка -  графа 13, 15 больше или равно 0
        row.getCell("sum1").setValue(0, null);
        row.getCell("sum2").setValue(0, null);
        testHelper.execute(FormDataEvent.CALCULATE);//перерасчет sum3
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 5. Проверка -  графа 13 должна быть >= графе 15
        row.getCell("sum1").setValue(1, null);
        row.getCell("sum2").setValue(2, null);
        testHelper.execute(FormDataEvent.CALCULATE);//перерасчет sum3
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Сумма фактически начисленного дохода (руб.)» должно быть не меньше значения графы «Сумма дохода, соответствующая рыночному уровню (руб.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма фактически начисленного дохода (руб.)» должно быть не меньше значения графы «Сумма дохода, соответствующая рыночному уровню (руб.)»!", entries.get(i++).getMessage());
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
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 2L));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "B"));
                        result.add(map);

                        map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 3L));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "C"));
                        result.add(map);

                        return result;
                    }
                });

        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<String> aliases = Arrays.asList("reasonNumber", "reasonDate", "base", "sum", "time", "rate", "sum1",
                "rate1", "sum2", "rate2", "sum3");
        defaultCheckLoadData(aliases, 3);
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        Assert.assertEquals(5, testHelper.getDataRowHelper().getCount());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(0).getCell("code").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("code").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(0).getCell("currency").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("currency").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(365, dataRows.get(0).getCell("base").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(1).getCell("base").getNumericValue());
        Assert.assertEquals(365, dataRows.get(2).getCell("base").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(3).getCell("base").getNumericValue());
        Assert.assertNull(dataRows.get(4).getCell("base").getNumericValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("rate2").getNumericValue().doubleValue(), 0);
        Assert.assertNull(dataRows.get(1).getCell("rate2").getNumericValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("rate2").getNumericValue().doubleValue(), 0);
        Assert.assertNull(dataRows.get(3).getCell("rate2").getNumericValue());
        Assert.assertNull(dataRows.get(4).getCell("rate2").getNumericValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(2, dataRows.get(1).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(2, dataRows.get(2).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(2, dataRows.get(3).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4, dataRows.get(4).getCell("sum3").getNumericValue().doubleValue(), 0);
    }

}

