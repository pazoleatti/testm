package com.aplana.sbrf.taxaccounting.form_template.market.summary_5_2b.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 5.2(б) Отчет о выданных Банком гарантиях, аккредитивах и ИТФ
 */
public class Summary_5_2bTest extends ScriptTestBase {
    private static final int TYPE_ID = 915;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private static final int FORM_TYPE_2_1 = 910;
    private static final int FORM_TYPE_5_2A = 911;
    private static final int FORM_TYPE_LETTER = 913;

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
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Summary_5_2bTest.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
        testHelper.reset();
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
        // ошибок быть не должно
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
    }

    private void mockGetRefBookRecord() {
        // для поиска видов обязательств и кредитного рейтинга и курса валют
        when(testHelper.getFormDataService().getRefBookRecord(anyLong(), anyMap(), anyMap(), anyMap(), anyString(),
                anyString(), anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Long refBookId = (Long) invocation.getArguments()[0];
                        String alias = (String) invocation.getArguments()[4];
                        String value = (String) invocation.getArguments()[5];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
                        for (Long id : records.keySet()) {
                            Map<String, RefBookValue> record = records.get(id);
                            RefBookAttributeType type = record.get(alias).getAttributeType();
                            String recordValue = null;
                            if (type == RefBookAttributeType.STRING){
                                recordValue = record.get(alias).getStringValue();
                            } else if (type == RefBookAttributeType.NUMBER) {
                                recordValue = record.get(alias).getNumberValue().toString();
                            } else if (type == RefBookAttributeType.REFERENCE) {
                                recordValue = record.get(alias).getReferenceValue().toString();
                            }
                            if (value.equals(recordValue)) {
                                return record;
                            }
                        }
                        return null;
                    }
                });
    }

    private void mockRefBook520() {
        // 520-й провайдер
        final Long refBookId = 520L;
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refBookId);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);
        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
        List<Long> recordIds = new ArrayList<Long>();
        for (Long recordId : records.keySet()) {
            recordIds.add(recordId);
        }
        when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
        when(provider.getRecordData(eq(recordIds))).thenReturn(records);
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        String value = ((String) invocation.getArguments()[2]).split("\'")[1];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
                        for (Long id : records.keySet()) {
                            Map<String, RefBookValue> record = records.get(id);
                            if (record.get("INN").getStringValue().equals(value)) {
                                result.add(record);
                            }
                        }
                        return result;
                    }
                });
        // для текстов сообщений
        when(testHelper.getRefBookFactory().get(refBookId)).thenAnswer(
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
        );    }

    private void mockRefBook602() {
        // 602-й провайдер
        final Long refBookId = 602L;
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refBookId);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);
        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
        List<Long> recordIds = new ArrayList<Long>();
        for (Long recordId : records.keySet()) {
            recordIds.add(recordId);
        }
        when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
        when(provider.getRecordData(eq(recordIds))).thenReturn(records);
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        String value = ((String) invocation.getArguments()[2]).split("\'")[1];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
                        for (Long id : records.keySet()) {
                            Map<String, RefBookValue> record = records.get(id);
                            if (record.get("INTERNATIONAL_CREDIT_RATING").getStringValue().equals(value)) {
                                result.add(record);
                            }
                        }
                        return result;
                    }
                });
    }

    @Test
    public void calc1Test() throws ParseException {
        mockGetRefBookRecord();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> dataRow = testHelper.getFormData().createDataRow();
        dataRow.setIndex(1);
        dataRow.getCell("name").setStringValue("name");
        dataRow.getCell("relatedPerson").setValue(1L, 1);
        dataRow.getCell("offshore").setValue(1L, 1);
        dataRow.getCell("innKio").setStringValue("11111111111");
        dataRow.getCell("creditRating").setValue(1L, 1);
        dataRow.getCell("internationalRating").setValue(1L, 1);
        // важно
        dataRow.getCell("issuanceDate").setDateValue(format.parse("01.01.2014"));
        // важно
        dataRow.getCell("endDate").setDateValue(format.parse("01.01.2015"));
        //dataRow.getCell("period").setNumericValue(new BigDecimal("1.00"));
        dataRow.getCell("obligationType").setValue(1L, 1);
        // важно
        dataRow.getCell("currencyCode").setValue(2L, 1);
        // важно
        dataRow.getCell("sum").setNumericValue(new BigDecimal("2.65"));
        dataRow.getCell("rate").setNumericValue(new BigDecimal("1.00"));
        //dataRow.getCell("currencyRate").setNumericValue(new BigDecimal("1.00"));
        //dataRow.getCell("endSum").setNumericValue(new BigDecimal("1.00"));
        dataRow.getCell("groupExclude").setValue(1L, 1);

        dataRows.add(dataRow);
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
        Assert.assertEquals(new BigDecimal("1.00"), dataRow.getCell("period").getNumericValue());
        Assert.assertEquals(new BigDecimal("1.0010"), dataRow.getCell("currencyRate").getNumericValue());
        Assert.assertEquals(new BigDecimal("2.65"), dataRow.getCell("endSum").getNumericValue());
    }

    @Test
    public void calc22ErrorTest () throws ParseException {
        mockGetRefBookRecord();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> dataRow = testHelper.getFormData().createDataRow();
        dataRow.setIndex(1);
        dataRow.getCell("name").setStringValue("name");
        dataRow.getCell("relatedPerson").setValue(1L, 1);
        dataRow.getCell("offshore").setValue(1L, 1);
        dataRow.getCell("innKio").setStringValue("11111111111");
        dataRow.getCell("creditRating").setValue(1L, 1);
        dataRow.getCell("internationalRating").setValue(1L, 1);
        // важно
        dataRow.getCell("issuanceDate").setDateValue(format.parse("01.01.2014"));
        dataRow.getCell("endDate").setDateValue(format.parse("01.01.2015"));
        dataRow.getCell("period").setNumericValue(new BigDecimal("1.00"));
        dataRow.getCell("obligationType").setValue(1L, 1);
        // важно
        dataRow.getCell("currencyCode").setValue(4L, 1);
        dataRow.getCell("sum").setNumericValue(new BigDecimal("2.65"));
        dataRow.getCell("rate").setNumericValue(new BigDecimal("1.00"));
        dataRow.getCell("currencyRate").setNumericValue(new BigDecimal("1.00"));
        dataRow.getCell("endSum").setNumericValue(new BigDecimal("1.00"));
        dataRow.getCell("groupExclude").setValue(1L, 1);

        dataRows.add(dataRow);
        testHelper.execute(FormDataEvent.CALCULATE);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        Assert.assertEquals(3, entries.size());
        int i = 0;
        Assert.assertEquals("Строка 1: В справочнике «Курсы валют» не найден курс валюты для «D» на дату 01.01.2014!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("currencyRate").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("endSum").getColumn().getName()), entries.get(i++).getMessage());
        entries.clear();
    }

    @Test
    public void check1Test() {
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> dataRow = testHelper.getFormData().createDataRow();
        dataRow.setIndex(1);
        dataRows.add(dataRow);
        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        Assert.assertEquals(16, entries.size());
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("name").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("relatedPerson").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("offshore").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("innKio").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("creditRating").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("internationalRating").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("issuanceDate").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("endDate").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("period").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("obligationType").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("currencyCode").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("sum").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("rate").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("currencyRate").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("endSum").getColumn().getName()), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, dataRow.getIndex(), dataRow.getCell("groupExclude").getColumn().getName()), entries.get(i++).getMessage());
        entries.clear();
    }

    @Test
    public void composeNotSourcesTest() {
        testHelper.execute(FormDataEvent.COMPOSE);
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals(0, dataRows.size());
        checkLogger();
    }

    /**
     * Строки из каждого источника собираются по-отдельности так что можно тестировать тоже раздельно
     * Консолидация из 2.1
     * @throws ParseException
     */
    @Test
    public void compose1Test() throws ParseException {
        mockGetRefBookRecord();
        mockRefBook520();
        // для текста сообщения и источников
        FormType formType2_1 = new FormType() {{
            setId(FORM_TYPE_2_1);
            setName("2.1 (Сводный) Реестр выданных Банком гарантий (контргарантий, поручительств)");
        }};
        when(testHelper.getFormTypeService().get(FORM_TYPE_2_1)).thenReturn(formType2_1);

        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relation = new Relation();
        relation.setFormDataId(1L);
        relation.setFormType(formType2_1);
        sourcesInfo.add(relation);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//summary_2_1//v2016//"));
        when(testHelper.getFormDataService().get(eq(1L), isNull(Boolean.class))).thenReturn(sourceFormData);

        // получение строк источника
        List<DataRow<Cell>> sourceDataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(1);
        setDefaultValuesSource2_1(sourceDataRow);
        sourceDataRows.add(sourceDataRow);

        //// добавляем строки которые не пройдут условия выборки
        sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(2);
        setDefaultValuesSource2_1(sourceDataRow);
        // -	значение графы 20 «Дата выдачи обязательства» больше либо равно даты начала отчетного периода текущей формы (01.01)
        // -	значение графы 20 «Дата выдачи обязательства» меньше либо равно даты окончания отчетного периода текущей формы (31.12)
        sourceDataRow.getCell("issuanceDate").setValue(format.parse("01.01.2013"), sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(3);
        setDefaultValuesSource2_1(sourceDataRow);
        // -	длина строки, указанной в графе 11 «ИНН Принципала / Налогоплательщика», равна 10, или значение в графе 11 «ИНН Принципала / Налогоплательщика» начинается на «99999»
        sourceDataRow.getCell("taxpayerInn").setValue("11111111111", sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(4);
        setDefaultValuesSource2_1(sourceDataRow);
        // -	значение в графе 5 «ВНД, в рамках которого выдано ГО» не равно «Альбом 2008»
        sourceDataRow.getCell("vnd").setValue("Альбом 2008", sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(5);
        setDefaultValuesSource2_1(sourceDataRow);
        // -	задано значение в графе 24 «Дата окончания действия обязательства»
        sourceDataRow.getCell("endDate").setValue(null, sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(6);
        setDefaultValuesSource2_1(sourceDataRow);
        // -	задано значение в графе 13 «Рейтинг»
        sourceDataRow.getCell("creditRating").setValue(null, sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(sourceDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);

        // консолидация должна пройти нормально
        // из 6 строк источника только первая должна пройти выборку
        int expected = 1;
        testHelper.execute(FormDataEvent.COMPOSE);
        // проверка количества строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // проверка значении
        DataRow<Cell> expectedRow = testHelper.getFormData().createDataRow();
        setComposeRow1(expectedRow);
        DataRow<Cell> resultRow = testHelper.getDataRowHelper().getAllCached().get(0);
        printLog();
        for (String column : resultRow.keySet()) {
            Assert.assertEquals(column, expectedRow.getCell(column).getValue(), resultRow.getCell(column).getValue());
        }

        checkLogger();

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        Assert.assertEquals(1, entries.size());
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Информация о Клиенте. Наименование Клиента и ОПФ» заполнена данными записи из справочника «Участники ТЦО», в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «A», атрибут «ИНН/ КИО» = «1111111111». В форме-источнике «2.1 (Сводный) Реестр выданных Банком гарантий (контргарантий, поручительств)» указано другое наименование клиента - «test»!", entries.get(i++).getMessage());
        entries.clear();
    }

    private void setDefaultValuesSource2_1(DataRow<Cell> sourceDataRow) throws ParseException {
        Long testRecordId = 1L;
        // -	значение графы 20 «Дата выдачи обязательства» больше либо равно даты начала отчетного периода текущей формы (01.01)
        // -	значение графы 20 «Дата выдачи обязательства» меньше либо равно даты окончания отчетного периода текущей формы (31.12)
        sourceDataRow.getCell("issuanceDate").setValue(format.parse("01.01.2014"), sourceDataRow.getIndex());
        // -	длина строки, указанной в графе 11 «ИНН Принципала / Налогоплательщика», равна 10, или значение в графе 11 «ИНН Принципала / Налогоплательщика» начинается на «99999»
        sourceDataRow.getCell("taxpayerInn").setValue("1111111111", sourceDataRow.getIndex());
        // -	значение в графе 5 «ВНД, в рамках которого выдано ГО» не равно «Альбом 2008»
        sourceDataRow.getCell("vnd").setValue("test", sourceDataRow.getIndex());
        // -	задано значение в графе 24 «Дата окончания действия обязательства»
        sourceDataRow.getCell("endDate").setValue(format.parse("01.01.2015"), sourceDataRow.getIndex());
        // -	задано значение в графе 13 «Рейтинг»
        sourceDataRow.getCell("creditRating").setValue(testRecordId, sourceDataRow.getIndex());

        sourceDataRow.getCell("code").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("name").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("guarantor").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("procuct1").setValue("product1", sourceDataRow.getIndex());
        sourceDataRow.getCell("taxpayerName").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("beneficiaryName").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("beneficiaryInn").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("number").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("sumInCurrency").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("sumInRub").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("currency").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("debtBalance").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("isNonRecurring").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("isCharged").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("tariff").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("remuneration").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("remunerationStartYear").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("remunerationIssuance").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("provide").setValue("Без обеспечения", sourceDataRow.getIndex());
        sourceDataRow.getCell("numberGuarantee").setValue("test", sourceDataRow.getIndex());
    }

    private void setComposeRow1(DataRow<Cell> dataRow) throws ParseException {
        dataRow.getCell("code").setValue("test", dataRow.getIndex());
        dataRow.getCell("name").setValue("A", dataRow.getIndex());
        dataRow.getCell("country").setValue(1L, dataRow.getIndex());
        dataRow.getCell("relatedPerson").setValue(2L, dataRow.getIndex()); // да
        dataRow.getCell("offshore").setValue(1L, dataRow.getIndex()); // нет
        dataRow.getCell("innKio").setValue("1111111111", dataRow.getIndex());
        dataRow.getCell("creditRating").setValue(1L, dataRow.getIndex());
        dataRow.getCell("internationalRating").setValue(1L, dataRow.getIndex());
        dataRow.getCell("number").setValue("test", dataRow.getIndex());
        dataRow.getCell("issuanceDate").setValue(format.parse("01.01.2014"), dataRow.getIndex());
        dataRow.getCell("docDate").setValue(null, dataRow.getIndex());
        dataRow.getCell("endDate").setValue(format.parse("01.01.2015"), dataRow.getIndex());
        dataRow.getCell("beneficiaryName").setValue("test", dataRow.getIndex());
        dataRow.getCell("beneficiaryInn").setValue("test", dataRow.getIndex());
        dataRow.getCell("period").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("obligationType").setValue(1L, dataRow.getIndex());
        dataRow.getCell("currencyCode").setValue(1L, dataRow.getIndex());
        dataRow.getCell("sum").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("rate").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("provisionPresence").setValue(1L, dataRow.getIndex()); // нет
        dataRow.getCell("currencyRate").setValue(new BigDecimal("30.0000"), dataRow.getIndex());
        dataRow.getCell("endSum").setValue(new BigDecimal("30.00"), dataRow.getIndex());
        dataRow.getCell("groupExclude").setValue(2L, dataRow.getIndex()); // да
    }

    /**
     * Строки из каждого источника собираются по-отдельности так что можно тестировать тоже раздельно
     * Консолидация из 5.2а
     * @throws ParseException
     */
    @Test
    public void compose2Test() throws ParseException {
        mockGetRefBookRecord();
        mockRefBook520();
        mockRefBook602();
        // для текста сообщения и источников
        FormType formType5_2a = new FormType() {{
            setId(FORM_TYPE_5_2A);
            setName("5.2(а) Отчет о выданных Банком инструментах торгового финансирования");
        }};
        when(testHelper.getFormTypeService().get(FORM_TYPE_5_2A)).thenReturn(formType5_2a);

        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relation = new Relation();
        relation.setFormDataId(1L);
        relation.setFormType(formType5_2a);
        sourcesInfo.add(relation);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//market_5_2a//v2016//"));
        when(testHelper.getFormDataService().get(eq(1L), isNull(Boolean.class))).thenReturn(sourceFormData);

        // получение строк источника
        List<DataRow<Cell>> sourceDataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(1);
        setDefaultValuesSource5_2a(sourceDataRow);
        sourceDataRows.add(sourceDataRow);

        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(sourceDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);

        // консолидация должна пройти нормально
        int expected = 1;
        testHelper.execute(FormDataEvent.COMPOSE);
        // проверка количества строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // проверка значении
        DataRow<Cell> expectedRow = testHelper.getFormData().createDataRow();
        setComposeRow2(expectedRow);
        DataRow<Cell> resultRow = testHelper.getDataRowHelper().getAllCached().get(0);
        printLog();
        for (String column : resultRow.keySet()) {
            Assert.assertEquals(column, expectedRow.getCell(column).getValue(), resultRow.getCell(column).getValue());
        }

        checkLogger();

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        Assert.assertEquals(2, entries.size());
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Информация о Клиенте. Наименование Клиента и ОПФ» заполнена данными записи из справочника «Участники ТЦО», в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «A», атрибут «ИНН/ КИО» = «1111111111». В форме-источнике «5.2(а) Отчет о выданных Банком инструментах торгового финансирования» указано другое наименование клиента - «test»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Информация о Клиенте. Страна регистрации (местоположения Клиента)» заполнена данными записи из справочника «Участники ТЦО», в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «A», атрибут «ИНН/ КИО» = «1111111111». В форме-источнике «5.2(а) Отчет о выданных Банком инструментах торгового финансирования» указано другое наименование страны - «B»!", entries.get(i++).getMessage());
        entries.clear();
    }

    private void setDefaultValuesSource5_2a(DataRow<Cell> sourceDataRow) throws ParseException {
        Long testRecordId = 1L;
        sourceDataRow.getCell("nameBank").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("country").setValue(2L, sourceDataRow.getIndex()); // некорректная страна
        sourceDataRow.getCell("swift").setValue("1111111111", sourceDataRow.getIndex());
        sourceDataRow.getCell("creditRating").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("tool").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("issueDate").setValue(format.parse("01.01.2014"), sourceDataRow.getIndex());
        sourceDataRow.getCell("expireDate").setValue(format.parse("01.01.2015"), sourceDataRow.getIndex());
        sourceDataRow.getCell("period").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("currency").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("sum").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("payRate").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
    }

    private void setComposeRow2(DataRow<Cell> dataRow) throws ParseException {
        dataRow.getCell("code").setValue(null, dataRow.getIndex());
        dataRow.getCell("name").setValue("A", dataRow.getIndex());
        dataRow.getCell("country").setValue(1L, dataRow.getIndex());
        dataRow.getCell("relatedPerson").setValue(2L, dataRow.getIndex()); // да
        dataRow.getCell("offshore").setValue(1L, dataRow.getIndex()); // нет
        dataRow.getCell("innKio").setValue("1111111111", dataRow.getIndex());
        dataRow.getCell("creditRating").setValue(1L, dataRow.getIndex());
        dataRow.getCell("internationalRating").setValue(1L, dataRow.getIndex());
        dataRow.getCell("number").setValue("test", dataRow.getIndex());
        dataRow.getCell("issuanceDate").setValue(format.parse("01.01.2014"), dataRow.getIndex());
        dataRow.getCell("docDate").setValue(null, dataRow.getIndex());
        dataRow.getCell("endDate").setValue(format.parse("01.01.2015"), dataRow.getIndex());
        dataRow.getCell("beneficiaryName").setValue(null, dataRow.getIndex());
        dataRow.getCell("beneficiaryInn").setValue(null, dataRow.getIndex());
        dataRow.getCell("period").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("obligationType").setValue(2L, dataRow.getIndex()); // Иные гарантии и аккредитивы
        dataRow.getCell("currencyCode").setValue(1L, dataRow.getIndex());
        dataRow.getCell("sum").setValue(new BigDecimal("1000.00"), dataRow.getIndex());
        dataRow.getCell("rate").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("provisionPresence").setValue(2L, dataRow.getIndex()); // да
        dataRow.getCell("currencyRate").setValue(new BigDecimal("30.0000"), dataRow.getIndex());
        dataRow.getCell("endSum").setValue(new BigDecimal("30000.00"), dataRow.getIndex());
        dataRow.getCell("groupExclude").setValue(2L, dataRow.getIndex()); // да
    }

    /**
     * Строки из каждого источника собираются по-отдельности так что можно тестировать тоже раздельно
     * Консолидация из "Данные по непокрытым аккредитивам"
     * @throws ParseException
     */
    @Test
    public void compose3Test() throws ParseException {
        mockGetRefBookRecord();
        mockRefBook520();
        // для текста сообщения и источников
        FormType formTypeLetter = new FormType() {{
            setId(FORM_TYPE_LETTER);
            setName("Данные по непокрытым аккредитивам");
        }};
        when(testHelper.getFormTypeService().get(FORM_TYPE_LETTER)).thenReturn(formTypeLetter);

        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relation = new Relation();
        relation.setFormDataId(1L);
        relation.setFormType(formTypeLetter);
        sourcesInfo.add(relation);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//letter_of_credit//v2016//"));
        when(testHelper.getFormDataService().get(eq(1L), isNull(Boolean.class))).thenReturn(sourceFormData);

        // получение строк источника
        List<DataRow<Cell>> sourceDataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(1);
        setDefaultValuesSourceLetter(sourceDataRow);
        sourceDataRows.add(sourceDataRow);

        //// добавляем строки которые не пройдут условия выборки
        sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(2);
        setDefaultValuesSourceLetter(sourceDataRow);
        // -	длина строки, указанной в графе 5 равна 10
        sourceDataRow.getCell("innKio").setValue("11111111111", sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(sourceDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);

        // консолидация должна пройти нормально
        // из 2 строк источника только первая должна пройти выборку
        int expected = 1;
        testHelper.execute(FormDataEvent.COMPOSE);
        // проверка количества строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // проверка значении
        DataRow<Cell> expectedRow = testHelper.getFormData().createDataRow();
        setComposeRow3(expectedRow);
        DataRow<Cell> resultRow = testHelper.getDataRowHelper().getAllCached().get(0);
        printLog();
        for (String column : resultRow.keySet()) {
            Assert.assertEquals(column, expectedRow.getCell(column).getValue(), resultRow.getCell(column).getValue());
        }

        checkLogger();

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        Assert.assertEquals(2, entries.size());
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Информация о Клиенте. Наименование Клиента и ОПФ» заполнена данными записи из справочника «Участники ТЦО», в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «A», атрибут «ИНН/ КИО» = «1111111111». В форме-источнике «Данные по непокрытым аккредитивам» указано другое наименование клиента - «test»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Информация о Клиенте. Страна регистрации (местоположения Клиента)» заполнена данными записи из справочника «Участники ТЦО», в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «A», атрибут «ИНН/ КИО» = «1111111111». В форме-источнике «Данные по непокрытым аккредитивам» указано другое наименование страны - «B»!", entries.get(i++).getMessage());
        entries.clear();
    }

    private void setDefaultValuesSourceLetter(DataRow<Cell> sourceDataRow) throws ParseException {
        Long testRecordId = 1L;
        // -	длина строки, указанной в графе 5 равна 10
        sourceDataRow.getCell("innKio").setValue("1111111111", sourceDataRow.getIndex());

        sourceDataRow.getCell("productType").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("name").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("country").setValue(2L, sourceDataRow.getIndex()); // другая страна
        sourceDataRow.getCell("docDate").setValue(format.parse("01.01.2014"), sourceDataRow.getIndex());
        sourceDataRow.getCell("docNumber").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("creditDate").setValue(format.parse("01.01.2014"), sourceDataRow.getIndex());
        sourceDataRow.getCell("creditEndDate").setValue(format.parse("01.01.2015"), sourceDataRow.getIndex());
        sourceDataRow.getCell("sum").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("currency").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("period").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("creditRating").setValue(testRecordId, sourceDataRow.getIndex());
        sourceDataRow.getCell("faceValueStr").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("faceValueNum").setValue(new BigDecimal("1.00"), sourceDataRow.getIndex());
        sourceDataRow.getCell("paymentSchedule").setValue("test", sourceDataRow.getIndex());
        sourceDataRow.getCell("sign").setValue(testRecordId, sourceDataRow.getIndex());
    }

    private void setComposeRow3(DataRow<Cell> dataRow) throws ParseException {
        dataRow.getCell("code").setValue(null, dataRow.getIndex());
        dataRow.getCell("name").setValue("A", dataRow.getIndex());
        dataRow.getCell("country").setValue(1L, dataRow.getIndex());
        dataRow.getCell("relatedPerson").setValue(2L, dataRow.getIndex()); // да
        dataRow.getCell("offshore").setValue(1L, dataRow.getIndex()); // нет
        dataRow.getCell("innKio").setValue("1111111111", dataRow.getIndex());
        dataRow.getCell("creditRating").setValue(1L, dataRow.getIndex());
        dataRow.getCell("internationalRating").setValue(1L, dataRow.getIndex());
        dataRow.getCell("number").setValue("test", dataRow.getIndex());
        dataRow.getCell("issuanceDate").setValue(format.parse("01.01.2014"), dataRow.getIndex());
        dataRow.getCell("docDate").setValue(format.parse("01.01.2014"), dataRow.getIndex());
        dataRow.getCell("endDate").setValue(format.parse("01.01.2015"), dataRow.getIndex());
        dataRow.getCell("beneficiaryName").setValue(null, dataRow.getIndex());
        dataRow.getCell("beneficiaryInn").setValue(null, dataRow.getIndex());
        dataRow.getCell("period").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("obligationType").setValue(2L, dataRow.getIndex()); // Иные гарантии и аккредитивы
        dataRow.getCell("currencyCode").setValue(1L, dataRow.getIndex());
        dataRow.getCell("sum").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("rate").setValue(new BigDecimal("1.00"), dataRow.getIndex());
        dataRow.getCell("provisionPresence").setValue(1L, dataRow.getIndex()); // нет
        dataRow.getCell("currencyRate").setValue(new BigDecimal("30.0000"), dataRow.getIndex());
        dataRow.getCell("endSum").setValue(new BigDecimal("30.00"), dataRow.getIndex());
        dataRow.getCell("groupExclude").setValue(2L, dataRow.getIndex()); // да
    }
}