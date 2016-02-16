package com.aplana.sbrf.taxaccounting.form_template.income.rnu_115.v2015;

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
 * РНУ 115
 */
public class Rnu_115Test extends ScriptTestBase {
    private static final int TYPE_ID = 842;
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
        return getDefaultScriptTestMockHelper(Rnu_115Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//rnu_115//v2015//"));
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

        Assert.assertEquals("Строка 1: Графа «Номер сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Вид сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата заключения сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Дата окончания сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Контрагент» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Тип сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код валюты (драгоценных металлов) по сделке приобретения (требования)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Объем покупаемой валюты / драгоценных металлов (в граммах)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Код валюты (драгоценных металлов) по сделке продажи (обязательства)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Объем продаваемой валюты / драгоценных металлов (в граммах)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Цена сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении требования» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении обязательства» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Требования (обязательства) по сделке, руб. Требования» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Требования (обязательства) по сделке, руб. Обязательства» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Доходы (расходы) учитываемые в целях налога на прибыль по сделке, руб. Доходы» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Доходы (расходы) учитываемые в целях налога на прибыль по сделке, руб. Расходы» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Рыночная цена сделки» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Отклонения по доходам, в руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Отклонения по расходам, в руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // Проверка суммы обязательств
        // Проверка расчётных граф
        i = 0;
        row.getCell("name").setValue(1L, null);
        row.getCell("dealNum").setValue("1", null);
        row.getCell("dealType").setValue(1, null);
        row.getCell("dealDate").setValue(sdf.parse("11.11.2014"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("11.11.2014"), null);
        row.getCell("dealFocus").setValue(1, null);
        row.getCell("reqCurCode").setValue(1L, null);
        row.getCell("reqVolume").setValue(1L, null);
        row.getCell("guarCurCode").setValue(1, null);
        row.getCell("guarVolume").setValue(1L, null);
        row.getCell("price").setValue(1L, null);
        row.getCell("reqCourse").setValue(1, null);
        row.getCell("guarCourse").setValue(1, null);
        row.getCell("reqSum").setValue(1L, null);
        row.getCell("guarSum").setValue(1L, null);
        row.getCell("incomeSum").setValue(11L, null);
        row.getCell("outcomeSum").setValue(12L, null);
        row.getCell("marketPrice").setValue(1L, null);
        row.getCell("incomeDelta").setValue(13L, null);
        row.getCell("outcomeDelta").setValue(14L, null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals("Строка 1: Значение графы «Требования (обязательства) по сделке, руб. Обязательства» должно быть меньше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Требования (обязательства) по сделке, руб. Обязательства» должно равняться модулю произведения граф «Объем продаваемой валюты / драгоценных металлов (в граммах)» и «Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении обязательства» со знаком «-»!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format("Строка %d: Неверное значение граф: %s!",1, "«Доходы (расходы) учитываемые в целях налога на прибыль по сделке, руб. Доходы», «Доходы (расходы) учитываемые в целях налога на прибыль по сделке, руб. Расходы», «Отклонения по доходам, в руб.», «Отклонения по расходам, в руб.»"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

        // Для прохождения всех ЛП после расчета
        row.getCell("guarSum").setValue(-1L, null);
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
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2, dataRows.get(0).getCell("incomeSum").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, dataRows.get(0).getCell("outcomeSum").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, dataRows.get(0).getCell("incomeDelta").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0, dataRows.get(0).getCell("outcomeDelta").getNumericValue().doubleValue(), 0);
    }
}

