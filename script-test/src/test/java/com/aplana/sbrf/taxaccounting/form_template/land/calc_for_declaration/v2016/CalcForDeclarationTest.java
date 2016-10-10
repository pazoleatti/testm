package com.aplana.sbrf.taxaccounting.form_template.land.calc_for_declaration.v2016;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Расчет земельного налога по земельным участкам, подлежащим включению в декларацию
 */
public class CalcForDeclarationTest extends ScriptTestBase {
    private static final int TYPE_ID = 918;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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
        return getDefaultScriptTestMockHelper(CalcForDeclarationTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//land//calc_for_declaration//v2016//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // для попадания в ЛП:
        // 1. Проверка заполнения граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Подразделение"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "КНО"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "КПП"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "КБК"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Код ОКТМО"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Кадастровый номер земельного участка"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Категория земель (код)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Кадастровая стоимость (доля стоимости) земельного участка"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата возникновения права собственности на земельный участок"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Период владения собственностью"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Ставка налога (%)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Коэффициент владения земельным участком (Кв)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Налог, исчисленный за период. 1 квартал"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Налог, исчисленный за период. 2 квартал"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Налог, исчисленный за период. 3 квартал"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Налог, исчисленный за период. Год"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("department").setValue(1L, null);
        row.getCell("kno").setValue("kno", null);
        row.getCell("kpp").setValue("kpp", null);
        row.getCell("kbk").setValue(1L, null);
        row.getCell("oktmo").setValue(1L, null);
        row.getCell("cadastralNumber").setValue("cadastralNumber", null);
        row.getCell("landCategory").setValue(1L, null);
        row.getCell("cadastralCost").setValue(0, null);
        row.getCell("ownershipDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("period").setValue(0, null);
        row.getCell("taxRate").setValue(0, null);
        row.getCell("kv").setValue(0, null);
        row.getCell("q1").setValue(0, null);
        row.getCell("q2").setValue(0, null);
        row.getCell("q3").setValue(0, null);
        row.getCell("year").setValue(0, null);

        i = 0;
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        int expected = 0;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(1, testHelper.getLogger().getEntries().size());
        Assert.assertEquals("Не удалось консолидировать данные в форму. В Системе отсутствует форма вида «Земельные участки, подлежащие включению в декларацию» " +
                "в состоянии «Принята» за период: «test period name 2014» для подразделения «test department name»", testHelper.getLogger().getEntries().get(0).getMessage());
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        testHelper.getLogger().clear();
    }

    // консолидация - все источники сразу
    @Test
    public void composeTest() {
        try {
            // вспомогательные данные источника
            // задать источники
            List<Relation> sourcesInfo = new ArrayList<Relation>();
            FormType formType = new FormType();
            formType.setId(916);
            Relation relarion = new Relation();
            relarion.setFormDataId(916L);
            relarion.setFormType(formType);
            sourcesInfo.add(relarion);
            FormType formType2 = new FormType();
            formType2.setId(917);
            Relation relarion2 = new Relation();
            relarion2.setFormDataId(917L);
            relarion2.setFormType(formType2);
            sourcesInfo.add(relarion2);

            when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                    any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

            Set<Integer> sourceTemplateIds = formTypeIdByTemplateIdMap.keySet();
            List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>(sourceTemplateIds.size());
            FormDataKind kind = FormDataKind.PRIMARY;
            for (int sourceTemplateId : sourceTemplateIds) {
                int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

                // источник
                DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
                departmentFormTypes.add(departmentFormType);

                // форма источника
                FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
                when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                        anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(sourceFormData);
                when(testHelper.getFormDataService().get(eq(sourceFormData.getId().longValue()), anyBoolean())).thenReturn(sourceFormData);

                // строки и хелпер источника
                List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
                DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
                sourceDataRowHelper.save(dataRows);
                sourceDataRowHelper.setAllCached(dataRows);
                when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
            }
            // источник
            when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                    any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);

            // провайдеры и получения записей для справочников
            mockProvider();

            testHelper.initRowData();

            // Консолидация
            testHelper.execute(FormDataEvent.COMPOSE);

            // TODO
            //int expected = 1;
            //Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
            //checkLogger();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();

    static {
        formTypeIdByTemplateIdMap.put(916, 916); // Расчет земельного налога за отчетные периоды
        //TODO formTypeIdByTemplateIdMap.put(917, 917); // Земельные участки, подлежащие включению в декларацию
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();

    static {
        templatesPathMap.put(916, "..//src/main//resources//form_template//land//calc_for_declaration//v2016//");
        //TODO templatesPathMap.put(917, "..//src/main//resources//form_template//land//...//v2016//");
    }

    private static Map<Long, RefBookDataProvider> providers = new HashMap<Long, RefBookDataProvider>();

    /**
     * Получить источник.
     *
     * @param id           идентификатор источника
     * @param sourceTypeId идентификатор типа формы источника
     * @param kind         вид формы источника
     */
    private DepartmentFormType getSource(int id, int sourceTypeId, FormDataKind kind) {
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setId(id);
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setKind(kind);
        departmentFormType.setTaxType(TaxType.LAND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        return departmentFormType;
    }

    /**
     * Получить форму источника.
     *
     * @param id               идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.LAND);
        formType.setName(sourceTemplate.getName());

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId((long) id);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        sourceFormData.setFormTemplateId(sourceTemplateId);

        return sourceFormData;
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData   форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "t" + sourceTemplateId;
        Date testDate = new Date();
        Long testLong = 1L;
        Long testRefbookId = 2L;
        DataRow<Cell> row = sourceFormData.createDataRow();
        row.getCell("department").setValue(testRefbookId, null);
        row.getCell("kno").setValue(testString, null);
        row.getCell("kpp").setValue(testString, null);
        row.getCell("kbk").setValue(testRefbookId, null);
        row.getCell("oktmo").setValue(testRefbookId, null);
        row.getCell("cadastralNumber").setValue(testString, null);
        row.getCell("landCategory").setValue(testRefbookId, null);
        row.getCell("cadastralCost").setValue(testLong, null);
        row.getCell("ownershipDate").setValue(testDate, null);
        row.getCell("period").setValue(testLong, null);
        row.getCell("taxRate").setValue(testLong, null);
        row.getCell("kv").setValue(testLong, null);
        row.getCell("q1").setValue(testLong, null);
        row.getCell("q2").setValue(testLong, null);
        row.getCell("q3").setValue(testLong, null);
        row.getCell("year").setValue(testLong, null);
        dataRows.add(row);
        DataRow<Cell> row2 = sourceFormData.createDataRow();
        row2.getCell("department").setValue(testRefbookId, null);
        row2.getCell("kno").setValue(testString, null);
        row2.getCell("kpp").setValue("skip", null);
        row2.getCell("kbk").setValue(testRefbookId, null);
        row2.getCell("oktmo").setValue(testRefbookId, null);
        row2.getCell("cadastralNumber").setValue(testString, null);
        row2.getCell("landCategory").setValue(testRefbookId, null);
        row2.getCell("cadastralCost").setValue(testLong, null);
        row2.getCell("ownershipDate").setValue(testDate, null);
        row2.getCell("period").setValue(testLong, null);
        row2.getCell("taxRate").setValue(testLong, null);
        row2.getCell("kv").setValue(testLong, null);
        row2.getCell("q1").setValue(testLong, null);
        row2.getCell("q2").setValue(testLong, null);
        row2.getCell("q3").setValue(testLong, null);
        row2.getCell("year").setValue(testLong, null);
        dataRows.add(row2);
        return dataRows;
    }

    private void mockProvider() {
        Long refBookId = 700L;
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refBookId);
        when(testHelper.getRefBookFactory().getDataProvider(refBookId)).thenReturn(provider);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);

        // вернуть все записи справочника
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(new Answer<PagingResult<Map<String, RefBookValue>>>() {
            @Override
            public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1));
                map.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "t916"));
                result.add(map);
                return result;
            }
        });
    }

}