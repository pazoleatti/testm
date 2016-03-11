package com.aplana.sbrf.taxaccounting.form_template.deal.app_6_3.v2015;

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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 6.3 Предоставление нежилых помещений в аренду
 *  @author Emamedova
 */
public class App_6_3Test extends ScriptTestBase {
    private static final int TYPE_ID = 812;
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
        return getDefaultScriptTestMockHelper(App_6_3Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_6_3//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // Проверка на заполнение граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Полное наименование юридического лица с указанием ОПФ"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма доходов Банка, руб."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата договора"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Страна (код)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Количество"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Цена"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Стоимость"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата совершения сделки"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Должна быть заполнена графа «Город» или «Населенный пункт»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // Проверка заполнения кода региона
        // Корректность даты (заключения) сделки
        // Корректность даты совершения сделки
        row.getCell("name").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("docNumber").setValue("1", null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("country").setValue(4L, null);
        row.getCell("settlement").setValue("settlement", null);
        row.getCell("price").setValue(1L, null);
        row.getCell("count").setValue(1L, null);
        row.getCell("cost").setValue(1L, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("01.01.2990"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата договора","01.01.1991", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Регион (код)» должна быть заполнена, т.к. указанная страна местонахождения объекта недвижимости Россия!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD_EXT, 1, "Дата совершения сделки", "01.01.2014", "31.12.2014", "Дата договора"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // Проверка заполнения кода региона
        // Проверка суммы доходов
        // Проверка заполнения населенного пункта
        // Проверка количества
        // Проверка стоимости
        row.getCell("name").setValue(1L, null);
        row.getCell("sum").setValue(-1L, null);
        row.getCell("docNumber").setValue("1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("country").setValue(1L, null);
        row.getCell("region").setValue(1L, null);
        row.getCell("city").setValue("city", null);
        row.getCell("settlement").setValue(null, null);
        row.getCell("price").setValue(1L, null);
        row.getCell("count").setValue(0L, null);
        row.getCell("cost").setValue(1L, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("02.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Сумма доходов Банка, руб.» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Регион (код)» не должна быть заполнена, т.к. указанная страна местонахождения объекта недвижимости не Россия!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Количество» должно быть больше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Стоимость» должно быть равно значению графы «Сумма доходов Банка, руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // Проверка цены
        row.getCell("name").setValue(1L, null);
        row.getCell("sum").setValue(222L, null);
        row.getCell("docNumber").setValue("1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("country").setValue(4L, null);
        row.getCell("city").setValue(null, null);
        row.getCell("settlement").setValue("settlement", null);
        row.getCell("price").setValue(1L, null);
        row.getCell("count").setValue(2L, null);
        row.getCell("cost").setValue(222L, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("02.01.2014"), null);
        testHelper.execute(FormDataEvent.CHECK);
        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы  «Цена», должно быть равно отношению графы «Сумма доходов Банка, руб.» к графе «Количество»! Выполнение расчета невозможно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для прохождения всех ЛП:
        row.getCell("name").setValue(1L, null);
        row.getCell("sum").setValue(7.77, null);
        row.getCell("docNumber").setValue("1", null);
        row.getCell("docDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("country").setValue(4L, null);
        row.getCell("city").setValue(null, null);
        row.getCell("settlement").setValue("settlement", null);
        row.getCell("price").setValue(3.88, null);
        row.getCell("count").setValue(2, null);
        row.getCell("cost").setValue(7.77, null);
        row.getCell("dealDoneDate").setValue(sdf.parse("02.01.2014"), null);
        testHelper.execute(FormDataEvent.CALCULATE);
        i = 0;
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
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());
    }
    @Test
    public void importTransportFileTest() {
        mockBeforeImport();
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        checkLoadData(testHelper.getDataRowHelper().getAll());
        Assert.assertEquals(2, testHelper.getDataRowHelper().getAll().size());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(222, dataRows.get(0).getCell("sum").getNumericValue().longValue());
        Assert.assertEquals(222, dataRows.get(0).getCell("cost").getNumericValue().longValue());
        Assert.assertEquals(2, dataRows.get(0).getCell("count").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(111, dataRows.get(0).getCell("price").getNumericValue().doubleValue(), 0);
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

