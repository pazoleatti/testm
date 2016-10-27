package com.aplana.sbrf.taxaccounting.form_template.transport.summary.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.WRONG_NON_EMPTY;
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.getColumnName;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Расчет суммы налога по каждому транспортному средству
 *
 * @author Kinzyabulatov
 */
public class SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 203;
    private static final int SOURCE_TYPE_ID = 201;
    private static final long SOURCE_DATA_ID = 2L;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private static final FormDataKind SOURCE_KIND = FormDataKind.PRIMARY;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat yFormat = new SimpleDateFormat("yyyy");

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
        return getDefaultScriptTestMockHelper(SummaryTest.class);
    }

    @Before
    public void mockBefore() {
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

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

        // провайдер
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), anyLong(),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenAnswer(new Answer<RefBookDataProvider>() {
            @Override
            public RefBookDataProvider answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[1];
                if (refBookId == 209L || refBookId == 210L || refBookId == 41L || refBookId == 4L) {
                    RefBookUniversal provider = mock(RefBookUniversal.class);
                    // вернуть все записи справочника
                    Map<Long, Map<String, RefBookValue>> refBookAllRecords = testHelper.getRefBookAllRecords(refBookId);
                    ArrayList<Long> recordIds = new ArrayList<Long>(refBookAllRecords.keySet());
                    when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
                    when(provider.getRecordData(eq(recordIds))).thenReturn(refBookAllRecords);
                    return provider;
                }
                return testHelper.getRefBookDataProvider();
            }
        });
        // список id записей справочника 42
        when(testHelper.getRefBookDataProvider().getParentsHierarchy(anyLong())).thenAnswer(new Answer<List<Long>>() {
            @Override
            public List<Long> answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = 42L;
                Long recordId = (Long) invocation.getArguments()[0];
                Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(refBookId);
                List<Long> result = new ArrayList<Long>();
                fillLinks(recordId, records, result);
                return result;
            }
        });
    }

    private void fillLinks(Long recordId, Map<Long, Map<String, RefBookValue>> records, List<Long> result) {
        Map<String, RefBookValue> valueMap = records.get(recordId);
        if (valueMap == null || valueMap.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue() == 0L) {
            return;
        }
        result.add(0, valueMap.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
        fillLinks(valueMap.get("PARENT_ID").getReferenceValue(), records, result);
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
    }

    @Test
    public void afterCreateTest() {
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        checkLogger();
    }

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    // Консолидация падающая на всех предварительных проверках
    @Test
    public void composeFailTest() throws ParseException {
        // собирается из первички транспорта
        Relation relation = getSourceRelation();
        FormData formData = mockCompose(relation, false);

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        int i = 0;
        String msg;
        DataRow<Cell> row = formData.createDataRow();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        msg = String.format("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s». В справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, " +
                "актуальная на дату %s, в которой поле «Средняя стоимость» = «%s» и значение «%s» больше значения поля «Количество лет, прошедших с года выпуска ТС (от)» и меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)». " +
                "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                "2", getColumnName(row, "pastYear"), "20", getColumnName(row, "averageCost"), "A",
                "31.12.2014", "A", getColumnName(row, "pastYear"),
                relation.getFormDataKind().getTitle(), relation.getFormType().getName(), relation.getDepartment().getName(), "test period name", relation.getYear());
        Assert.assertEquals(LogLevel.WARNING, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        msg = String.format("Строки %s формы-источника. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                        "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» " +
                        "для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s». " +
                        "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                2, getColumnName(row, "codeOKATO"), "5200000", "31.12.2014", "02", "test department name", "03", "5200000",
                relation.getFormDataKind().getTitle(), relation.getFormType().getName(), relation.getDepartment().getName(), "test period name", relation.getYear());
        Assert.assertEquals(LogLevel.ERROR, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        msg = String.format("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                        "%s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                        "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s». " +
                        "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                "2", getColumnName(row, "codeOKATO"), "5200000", getColumnName(row, "tsTypeCode"), "50000", getColumnName(row, "baseUnit"), "A",
                "отсутствует запись, актуальная", "31.12.2014", "02", relation.getDepartment().getName(), "03", "50000", "A",
                relation.getFormDataKind().getTitle(), relation.getFormType().getName(), relation.getDepartment().getName(), "test period name", relation.getYear());
        Assert.assertEquals(LogLevel.ERROR, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        Assert.assertEquals(i, entries.size());
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());
    }

    // Консолидация
    @Test
    public void composeTest() throws ParseException {
        // собирается из первички транспорта
        mockCompose(getSourceRelation(), true);

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(0, testHelper.getLogger().getEntries().size());
        Assert.assertEquals(8, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
    }

    private Relation getSourceRelation(){
        return new Relation() {{
            setFormDataId(SOURCE_DATA_ID);
            setFormType(new FormType() {{
                setId(SOURCE_TYPE_ID);
                setName("Сведения о транспортных средствах, по которым уплачивается транспортный налог");
            }});
            setFormDataKind(FormDataKind.PRIMARY);
            setDepartment(new Department() {{
                setName("Department");
                setRegionId(1L);
            }});
            setPeriodName("первый квартал");
            setYear(2014);
            setFormTypeName("Сведения о транспортных средствах, по которым уплачивается транспортный налог");
        }};
    }

    private FormData mockCompose(Relation sourceRelation, boolean valid) throws ParseException {
        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");
        when(testHelper.getDepartmentService().get(DEPARTMENT_ID)).thenReturn(department);

        // назначаем приемник
        List<Relation> relations = new ArrayList<Relation>();
        relations.add(sourceRelation);
        when(testHelper.getFormDataService().getSourcesInfo(eq(testHelper.getFormData()), anyBoolean(), anyBoolean(), eq(WorkflowState.ACCEPTED), any(TAUserInfo.class), any(Logger.class))).thenReturn(relations);

        return fillSourceDataRows(valid);
    }

    private FormData getSourceFormData() {
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//transport//vehicles//v2015//");
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(sourceTemplate);

        FormData sourceFormData = new FormData();
        FormType formType = new FormType();
        formType.setId(SOURCE_TYPE_ID);
        sourceFormData.setId(2L);
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setKind(SOURCE_KIND);
        sourceFormData.setFormType(formType);
        sourceFormData.setDepartmentId(DEPARTMENT_ID);
        sourceFormData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        sourceFormData.setReportPeriodId(REPORT_PERIOD_ID);
        sourceFormData.setState(WorkflowState.ACCEPTED);

        when(testHelper.getFormDataService().get(eq(SOURCE_DATA_ID), isNull(Boolean.class))).thenReturn(sourceFormData);
        return sourceFormData;
    }

    /**
     * Заполнить строки источника.
     */
    private FormData fillSourceDataRows(boolean valid) throws ParseException {
        FormData sourceFormData = getSourceFormData();
        List<DataRow<Cell>> dataRows = sourceFormData.getFormTemplate().getRows();
        DataRow<Cell> row = sourceFormData.createDataRow();
        // графа 2  - codeOKATO         - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
        row.getCell("codeOKATO").setValue(valid ? 1L : 2L, null);
        // графа 3  - regionName        - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
        // графа 4  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
        row.getCell("tsTypeCode").setValue(valid ? 6L : 1L, null);
        // графа 5  - tsType            - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
        // графа 6  - model
        row.getCell("model").setValue("12", null);
        // графа 7  - ecoClass          - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
        row.getCell("ecoClass").setValue(1L, null);
        // графа 8  - identNumber
        row.getCell("identNumber").setValue("12", null);
        // графа 9  - regNumber
        row.getCell("regNumber").setValue("12", null);
        // графа 10 - regDate
        row.getCell("regDate").setValue(format.parse("12.01.2012"), null);
        // графа 11 - regDateEnd
        row.getCell("regDateEnd").setValue(format.parse("12.01.2014"), null);
        // графа 12 - taxBase
        row.getCell("taxBase").setValue(12, null);
        // графа 13 - baseUnit          - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
        row.getCell("baseUnit").setValue(1L, null);
        // графа 14 - year
        row.getCell("year").setValue(yFormat.parse("2008"), null);
        // графа 15 - pastYear
        row.getCell("pastYear").setValue(valid ? 2 : 20, null);
        // графа 16 - stealDateStart
        // графа 17 - stealDateEnd
        // графа 18 - share
        row.getCell("share").setValue("12/13", null);
        // графа 19 - costOnPeriodBegin
        row.getCell("costOnPeriodBegin").setValue(12, null);
        // графа 20 - costOnPeriodEnd
        row.getCell("costOnPeriodEnd").setValue(12, null);
        // графа 21 - benefitStartDate
        row.getCell("benefitStartDate").setValue(format.parse("12.01.2013"), null);
        // графа 22 - benefitEndDate
        row.getCell("benefitEndDate").setValue(format.parse("12.01.2014"), null);
        // графа 23 - taxBenefitCode    - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
        row.getCell("taxBenefitCode").setValue(1L, null);
        // графа 24 - base
        row.getCell("base").setValue("12/1", null);
        // графа 25 - version           - атрибут 2183 - MODEL - «Модель (версия)», справочник 218 «Средняя стоимость транспортных средств»
        row.getCell("version").setValue(1L, null);
        // графа 26 - averageCost       - атрибут 2111 - NAME - «Наименование», справочник 211 «Категории средней стоимости транспортных средств»

        dataRows.add(1, row);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.save(dataRows);
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
        return sourceFormData;
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        int expected = 12; // в файле 12 строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // TODO (Ramil Timerbaev) возможно надо будет добавить проверку загруженых значении
        checkLogger();
    }

    /**
     * Проверить загруженные данные, а также выполнение расчетов и логических проверок.
     */
    private void checkLoadData(List<DataRow<Cell>> dataRows) {
        //графа 2, 3
        Assert.assertEquals("A", dataRows.get(1).getCell("taxAuthority").getStringValue());
        Assert.assertEquals("A", dataRows.get(1).getCell("kpp").getStringValue());
        // графа 4
        Assert.assertEquals(1, dataRows.get(1).getCell("okato").getNumericValue().intValue());
        // графа 5 (6 пропускаем)
        Assert.assertEquals(6, dataRows.get(1).getCell("tsTypeCode").getNumericValue().intValue());
        // графа 7
        Assert.assertEquals("12", dataRows.get(1).getCell("model").getStringValue());
        // графа 8
        Assert.assertEquals(1, dataRows.get(1).getCell("ecoClass").getNumericValue().intValue());
        // графа 9
        Assert.assertEquals("12", dataRows.get(1).getCell("vi").getStringValue());
        // графа 10
        Assert.assertEquals("12", dataRows.get(1).getCell("regNumber").getStringValue());
        // графа 11
        Assert.assertEquals("12.01.2012", String.valueOf(format.format(dataRows.get(1).getCell("regDate").getDateValue())));
        // графа 12
        Assert.assertEquals("12.01.2014", String.valueOf(format.format(dataRows.get(1).getCell("regDateEnd").getDateValue())));
        // графа 13
        Assert.assertEquals(12.0, dataRows.get(1).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        // графа 14
        Assert.assertEquals(1, dataRows.get(1).getCell("taxBaseOkeiUnit").getNumericValue().intValue());
        // графа 15
        Assert.assertEquals("2008", String.valueOf(new SimpleDateFormat("yyyy").format(dataRows.get(1).getCell("createYear").getDateValue())));
        // графа 20
        Assert.assertEquals(12.0, dataRows.get(1).getCell("periodStartCost").getNumericValue().doubleValue(), 0.0);
        // Графа 23
        Assert.assertEquals(0.3333, dataRows.get(1).getCell("coef362").getNumericValue().doubleValue(), 0.0);
        // Графа 24
        Assert.assertEquals("12/13", dataRows.get(1).getCell("partRight").getStringValue());
        // Графа 25
        Assert.assertEquals(7.38, dataRows.get(1).getCell("calculatedTaxSum").getNumericValue().doubleValue(), 0.0);
        // Графа 26
        Assert.assertEquals(1, dataRows.get(1).getCell("benefitMonths").getNumericValue().intValue());
        // Графа 29
        Assert.assertEquals(0.3333, dataRows.get(1).getCell("coefKl").getNumericValue().doubleValue(), 0.0);
        // Графа 31
        Assert.assertEquals(7.38, dataRows.get(1).getCell("benefitSum").getNumericValue().doubleValue(), 0.0);
        // Графа 33
        Assert.assertNull(dataRows.get(1).getCell("benefitSumDecrease").getNumericValue());
        // Графа 35
        Assert.assertNull(dataRows.get(1).getCell("benefitSumReduction").getNumericValue());
        // Графа 37
        Assert.assertEquals(0.00, dataRows.get(1).getCell("taxSumToPay").getNumericValue().doubleValue(), 0.0);
    }

    // 1. Проверка на заполнение поля
    @Test
    public void check1Test() {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        String[] nonEmptyColumns = {"taxAuthority", "kpp", "okato", "tsTypeCode", "model", "vi", "regNumber", "regDate",
                "taxBase", "taxBaseOkeiUnit", "createYear", "years", "ownMonths", "partRight", "coef362",
                "taxRate", "calculatedTaxSum", "taxSumToPay"};
        testHelper.execute(FormDataEvent.CHECK);

        for (String alias : nonEmptyColumns) {
            msg = String.format(WRONG_NON_EMPTY, rowIndex, getColumnName(row, alias));
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    // 3. Проверка на корректность заполнения кода НО и КПП согласно справочнику параметров представления деклараций
    @Test
    public void check2Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("taxAuthority").setValue("8888", rowIndex);
        row.getCell("kpp").setValue("111111111", rowIndex);
        row.getCell("okato").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("vi").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("taxBaseOkeiUnit").setValue(1L, rowIndex);
        row.getCell("createYear").setValue(format.parse("01.01.2012"), rowIndex);
        row.getCell("years").setValue(2, rowIndex);
        row.getCell("ownMonths").setValue(2, rowIndex);
        row.getCell("partRight").setValue("18/23", rowIndex);
        row.getCell("coef362").setValue(0.2, rowIndex);
        row.getCell("taxRate").setValue(1L, rowIndex);
        row.getCell("calculatedTaxSum").setValue(0.2, rowIndex);
        row.getCell("taxSumToPay").setValue(0.2, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» " +
                        "отсутствует запись, актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                        "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s», поле «Код налогового органа (кон.)» = «%s» поле «КПП» = «%s»",
                rowIndex, getColumnName(row, "okato"), "7190000", getColumnName(row, "taxAuthority"), "8888", getColumnName(row, "kpp"), "111111111",
                "31.12.2014", "02", "test department name", "02", "7190000", "8888", "111111111");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 4. Проверка на наличие в форме строк с одинаковым значением граф 4, 5, 9, 13, 14
    @Test
    public void check3Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(2, row2);

        String msg;
        int i = 0;
        for (DataRow<Cell> dataRow : dataRows) {
            if(dataRow.getAlias() != null) {
                continue;
            }
            int rowIndex = dataRow.getIndex();
            dataRow.getCell("taxAuthority").setValue("A", rowIndex);
            dataRow.getCell("kpp").setValue("A", rowIndex);
            dataRow.getCell("okato").setValue(1L, rowIndex);
            dataRow.getCell("tsTypeCode").setValue(6L, rowIndex);
            dataRow.getCell("model").setValue("Модель", rowIndex);
            dataRow.getCell("vi").setValue("идентнамбер", rowIndex);
            dataRow.getCell("regNumber").setValue("регнамбер", rowIndex);
            dataRow.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
            dataRow.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
            dataRow.getCell("taxBaseOkeiUnit").setValue(1L, rowIndex);
            dataRow.getCell("createYear").setValue(format.parse("01.01.2012"), rowIndex);
            dataRow.getCell("years").setValue(2, rowIndex);
            dataRow.getCell("ownMonths").setValue(2, rowIndex);
            dataRow.getCell("partRight").setValue("18/23", rowIndex);
            dataRow.getCell("coef362").setValue(0.2, rowIndex);
            dataRow.getCell("taxRate").setValue(1L, rowIndex);
            dataRow.getCell("calculatedTaxSum").setValue(0.2, rowIndex);
            dataRow.getCell("taxSumToPay").setValue(0.2, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строки %s. Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                        "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, налоговой базой и единицей измерения налоговой базы по ОКЕИ",
                "1, 2", "7190000", "56100", "идентнамбер", "1.00", "A");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 5. Проверка кода вида ТС по разделу «Наземные транспортные средства»
    // 6. Проверка кода вида ТС по разделу «Водные транспортные средства»
    // 7. Проверка кода вида ТС по разделу «Воздушные транспортные средства»
    @Test
    public void check4Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("taxAuthority").setValue("A", rowIndex);
        row.getCell("kpp").setValue("A", rowIndex);
        row.getCell("okato").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(1L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("vi").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("taxBaseOkeiUnit").setValue(1L, rowIndex);
        row.getCell("createYear").setValue(format.parse("01.01.2012"), rowIndex);
        row.getCell("years").setValue(2, rowIndex);
        row.getCell("ownMonths").setValue(2, rowIndex);
        row.getCell("partRight").setValue("18/23", rowIndex);
        row.getCell("coef362").setValue(0.2, rowIndex);
        row.getCell("taxRate").setValue(1L, rowIndex);
        row.getCell("calculatedTaxSum").setValue(0.2, rowIndex);
        row.getCell("taxSumToPay").setValue(0.2, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Значение графы «Код вида ТС» должно относиться к виду ТС «%s»",
                rowIndex, "Наземные транспортные средства");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 8. Проверка корректности заполнения даты регистрации ТС
    // 9. Проверка корректности заполнения даты снятия с регистрации ТС
    // 10. Проверка года изготовления ТС
    // 11. Проверка на наличие даты начала розыска ТС при указании даты возврата ТС
    @Test
    public void check5Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("taxAuthority").setValue("A", rowIndex);
        row.getCell("kpp").setValue("A", rowIndex);
        row.getCell("okato").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("vi").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("taxBaseOkeiUnit").setValue(1L, rowIndex);
        row.getCell("createYear").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("years").setValue(2, rowIndex);
        row.getCell("stealDateEnd").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("ownMonths").setValue(2, rowIndex);
        row.getCell("partRight").setValue("18/23", rowIndex);
        row.getCell("coef362").setValue(0.2, rowIndex);
        row.getCell("taxRate").setValue(1L, rowIndex);
        row.getCell("calculatedTaxSum").setValue(0.2, rowIndex);
        row.getCell("taxSumToPay").setValue(0.2, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                rowIndex, getColumnName(row, "regDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                rowIndex, getColumnName(row, "regDateEnd"), "01.01.2014", getColumnName(row, "regDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»",
                rowIndex, getColumnName(row, "createYear"), 2014);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена, если заполнена графа «%s»",
                rowIndex, getColumnName(row, "stealDateStart"), getColumnName(row, "stealDateEnd"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 12. Проверка на соответствие дат сведений об угоне
    // 13. Проверка доли налогоплательщика в праве на ТС (графа 18) на корректность формата введенных данных
    // 14. Проверка значения знаменателя доли налогоплательщика в праве на ТС (графа 18)
    // 15. Проверка значения поля «Коэффициент, определяемый в соответствии с п.3 ст.362 НК РФ»
    // 17. Проверка корректности заполнения даты начала использования льготы
    // 18. Проверка корректности заполнения даты окончания использования льготы
    // 19. Проверка значения поля «Коэффициент, определяемый в соответствии с законами субъектов РФ»
    // 20. Проверка одновременного заполнения данных о налоговой льготе
    // 21. Проверка, что исчисленная сумма налога больше или равна сумме налоговой льготы
    @Test
    public void check6Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("taxAuthority").setValue("A", rowIndex);
        row.getCell("kpp").setValue("A", rowIndex);
        row.getCell("okato").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("vi").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("taxBaseOkeiUnit").setValue(1L, rowIndex);
        row.getCell("createYear").setValue(format.parse("01.01.2012"), rowIndex);
        row.getCell("years").setValue(2, rowIndex);
        row.getCell("stealDateStart").setValue(format.parse("01.02.2015"), rowIndex);
        row.getCell("stealDateEnd").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("ownMonths").setValue(2, rowIndex);
        row.getCell("partRight").setValue("180/00", rowIndex);
        row.getCell("coef362").setValue(1.2, rowIndex);
        row.getCell("taxRate").setValue(1L, rowIndex);
        row.getCell("calculatedTaxSum").setValue(0.2, rowIndex);
        row.getCell("benefitStartDate").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("benefitEndDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("coefKl").setValue(1.2, rowIndex);
        row.getCell("benefitSum").setValue(1.2, rowIndex);
        row.getCell("taxBenefitCode").setValue(1L, rowIndex);
        row.getCell("taxSumToPay").setValue(0.2, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        // 12
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»",
                rowIndex, getColumnName(row, "stealDateEnd"), getColumnName(row, "stealDateStart"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 13
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», числитель должен быть меньше либо равен знаменателю",
                rowIndex, getColumnName(row, "partRight"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 14
        msg = String.format("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю",
                rowIndex, getColumnName(row, "partRight"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 15
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно нуля и меньше либо равно единицы",
                rowIndex, getColumnName(row, "coef362"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 17
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                rowIndex, getColumnName(row, "benefitStartDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 18
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                rowIndex, getColumnName(row, "benefitEndDate"), "01.01.2014", getColumnName(row, "benefitStartDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 19
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно нуля и меньше либо равно единицы",
                rowIndex, getColumnName(row, "coefKl"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 20
        msg = String.format("Строка %s: Данные о налоговой льготе указаны не полностью.", rowIndex);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        // 21
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы с суммой налоговой льготы",
                rowIndex, getColumnName(row, "calculatedTaxSum"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 2. Проверка наличия параметров представления декларации для кода ОКТМО, заданного по графе 4 «Код ОКТМО»
    // 16. Проверка наличия ставки ТС в справочнике
    @Test
    public void calc1Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(5, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("taxAuthority").setValue("A", rowIndex);
        row.getCell("kpp").setValue("A", rowIndex);
        row.getCell("okato").setValue(4L, rowIndex);
        row.getCell("tsTypeCode").setValue(4L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("vi").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.11.2013"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("taxBaseOkeiUnit").setValue(1L, rowIndex);
        row.getCell("createYear").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("years").setValue(2, rowIndex);
        row.getCell("ownMonths").setValue(1, rowIndex);
        row.getCell("partRight").setValue("18/23", rowIndex);
        row.getCell("coef362").setValue(0.3333, rowIndex);
        row.getCell("taxRate").setValue(1L, rowIndex);
        row.getCell("calculatedTaxSum").setValue(0.2, rowIndex);
        row.getCell("taxSumToPay").setValue(0.2, rowIndex);
        testHelper.execute(FormDataEvent.CALCULATE);

        msg = String.format("Строка %s. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» %s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s»",
                rowIndex, getColumnName(row, "okato"), "6500000", "отсутствует запись, актуальная", "31.12.2014", "02", "test department name", "65", "6500000");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                        "%s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                        "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s». ",
                rowIndex, getColumnName(row, "okato"), "6500000", getColumnName(row, "tsTypeCode"), "41100", getColumnName(row, "taxBaseOkeiUnit"), "A",
                "отсутствует запись, актуальная", "31.12.2014", "02", "test department name", "65", "41100", "A");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }
}
