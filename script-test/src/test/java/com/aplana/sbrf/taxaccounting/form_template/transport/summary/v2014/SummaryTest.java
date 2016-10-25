package com.aplana.sbrf.taxaccounting.form_template.transport.summary.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
                relation.getFormDataKind().getTitle(), relation.getFormTypeName(), relation.getDepartment().getName(), relation.getPeriodName(), relation.getYear());
        Assert.assertEquals(LogLevel.WARNING, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        msg = String.format("Строки %s формы-источника. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                        "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» " +
                        "для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s». " +
                        "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                2, getColumnName(row, "codeOKATO"), "5200000", "31.12.2014", "02", "test department name", "03", "5200000",
                relation.getFormDataKind().getTitle(), relation.getFormTypeName(), relation.getDepartment().getName(), relation.getPeriodName(), relation.getYear());
        Assert.assertEquals(LogLevel.ERROR, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        msg = String.format("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                        "%s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                        "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s». " +
                        "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                "2", getColumnName(row, "codeOKATO"), "5200000", getColumnName(row, "tsTypeCode"), "50000", getColumnName(row, "baseUnit"), "A",
                "отсутствует запись, актуальная", "31.12.2014", "02", relation.getDepartment().getName(), "03", "50000", "A",
                relation.getFormDataKind().getTitle(), relation.getFormTypeName(), relation.getDepartment().getName(), relation.getPeriodName(), relation.getYear());
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
}
