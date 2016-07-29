package com.aplana.sbrf.taxaccounting.form_template.deal.summary2.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Сводный отчет для уведомления по КС.
 */
public class Summary2Test extends ScriptTestBase {
    private static final int TYPE_ID = 849;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        formTypeIdByTemplateIdMap.put(803, 803); // 4.2
        formTypeIdByTemplateIdMap.put(816, 816); // 6.1
        formTypeIdByTemplateIdMap.put(804, 804); // 6.2
        formTypeIdByTemplateIdMap.put(812, 812); // 6.3
        formTypeIdByTemplateIdMap.put(813, 813); // 6.4
        formTypeIdByTemplateIdMap.put(814, 814); // 6.5
        formTypeIdByTemplateIdMap.put(806, 806); // 6.6
        formTypeIdByTemplateIdMap.put(805, 805); // 6.7
        formTypeIdByTemplateIdMap.put(815, 815); // 6.8
        formTypeIdByTemplateIdMap.put(817, 817); // 6.9
        formTypeIdByTemplateIdMap.put(823, 823); // 6.10.1
        formTypeIdByTemplateIdMap.put(825, 825); // 6.10.2
        formTypeIdByTemplateIdMap.put(827, 827); // 6.11
        formTypeIdByTemplateIdMap.put(819, 819); // 6.12
        formTypeIdByTemplateIdMap.put(826, 826); // 6.13
        formTypeIdByTemplateIdMap.put(835, 835); // 6.14
        formTypeIdByTemplateIdMap.put(837, 837); // 6.15
        formTypeIdByTemplateIdMap.put(839, 839); // 6.16
        formTypeIdByTemplateIdMap.put(811, 811); // 6.17
        formTypeIdByTemplateIdMap.put(838, 838); // 6.18
        formTypeIdByTemplateIdMap.put(828, 828); // 6.19
        formTypeIdByTemplateIdMap.put(831, 831); // 6.20
        formTypeIdByTemplateIdMap.put(830, 830); // 6.21
        formTypeIdByTemplateIdMap.put(834, 834); // 6.22
        formTypeIdByTemplateIdMap.put(832, 832); // 6.23
        formTypeIdByTemplateIdMap.put(833, 833); // 6.24
        formTypeIdByTemplateIdMap.put(836, 836); // 6.25
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();
    static {
        templatesPathMap.put(803, "..//src/main//resources//form_template//deal//app_4_2//v2015//");
        templatesPathMap.put(816, "..//src/main//resources//form_template//deal//app_6_1//v2015//");
        templatesPathMap.put(804, "..//src/main//resources//form_template//deal//app_6_2//v2015//");
        templatesPathMap.put(812, "..//src/main//resources//form_template//deal//app_6_3//v2015//");
        templatesPathMap.put(813, "..//src/main//resources//form_template//deal//app_6_4//v2015//");
        templatesPathMap.put(814, "..//src/main//resources//form_template//deal//app_6_5//v2015//");
        templatesPathMap.put(806, "..//src/main//resources//form_template//deal//app_6_6//v2015//");
        templatesPathMap.put(805, "..//src/main//resources//form_template//deal//app_6_7//v2015//");
        templatesPathMap.put(815, "..//src/main//resources//form_template//deal//app_6_8//v2015//");
        templatesPathMap.put(817, "..//src/main//resources//form_template//deal//app_6_9//v2015//");
        templatesPathMap.put(823, "..//src/main//resources//form_template//deal//app_6_10_1//v2015//");
        templatesPathMap.put(825, "..//src/main//resources//form_template//deal//app_6_10_2//v2015//");
        templatesPathMap.put(827, "..//src/main//resources//form_template//deal//app_6_11//v2015//");
        templatesPathMap.put(819, "..//src/main//resources//form_template//deal//app_6_12//v2015//");
        templatesPathMap.put(826, "..//src/main//resources//form_template//deal//app_6_13//v2015//");
        templatesPathMap.put(835, "..//src/main//resources//form_template//deal//app_6_14//v2015//");
        templatesPathMap.put(837, "..//src/main//resources//form_template//deal//app_6_15//v2015//");
        templatesPathMap.put(839, "..//src/main//resources//form_template//deal//app_6_16//v2015//");
        templatesPathMap.put(811, "..//src/main//resources//form_template//deal//app_6_17//v2015//");
        templatesPathMap.put(838, "..//src/main//resources//form_template//deal//app_6_18//v2015//");
        templatesPathMap.put(828, "..//src/main//resources//form_template//deal//app_6_19//v2015//");
        templatesPathMap.put(831, "..//src/main//resources//form_template//deal//app_6_20//v2015//");
        templatesPathMap.put(830, "..//src/main//resources//form_template//deal//app_6_21//v2015//");
        templatesPathMap.put(834, "..//src/main//resources//form_template//deal//app_6_22//v2015//");
        templatesPathMap.put(832, "..//src/main//resources//form_template//deal//app_6_23//v2015//");
        templatesPathMap.put(833, "..//src/main//resources//form_template//deal//app_6_24//v2015//");
        templatesPathMap.put(836, "..//src/main//resources//form_template//deal//app_6_25//v2015//");
    }

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
        return getDefaultScriptTestMockHelper(Summary2Test.class);
    }

    @Test
    public void create() {
        when(testHelper.getFormDataService().find(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), any(Integer.class), any(Boolean.class))).thenReturn(null);

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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//summary2//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;
        int i;

        // для попадания в ЛП:
        // Проверка пустой строки
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // 2. Проверка заполнения обязательных полей
        // обязательные графы (4..19, 21, 24..25, 29..31, 42..46, 48..51)
        String [] nonEmptyColumns = { "f121", "f122", "f123", "f124", "f131", "f132", "f133", "f134", "f135",
                "similarDealGroup", "dealNameCode", "taxpayerSideCode", "dealPriceSign", "dealPriceCode", "dealMemberCount",
                "income", "outcome", "dealType", "dealSubjectName", "otherNum", "contractNum", "contractDate",
                "okeiCode", "count", "price", "total", "dealDoneDate", "dealMemberNum", "countryCode3", "organName" };
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        for (String column : nonEmptyColumns) {
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, 1, row.getCell(column).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка кода основания отнесения сделки к контролируемой
        fillRow(row);
        String [] useColumn3 = { "f121", "f122", "f123", "f124", "f131", "f132", "f133", "f134", "f135" };
        for (String column : useColumn3) {
            row.getCell(column).setValue(1L, row.getIndex());
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        String [] tmpColumn = { "f131", "f132", "f133", "f134", "f135" };
        List<String> tmpColumnNames = new ArrayList<String>();
        for (String alias : tmpColumn) {
            tmpColumnNames.add(row.getCell(alias).getColumn().getName());
        }
        String subStr = org.springframework.util.StringUtils.collectionToDelimitedString(tmpColumnNames, "», «");
        msg = String.format("Строка %d: Не допускается одновременное заполнение значением «1» любой из граф «%s», «%s» с любой из граф «%s»!",
                row.getIndex(), row.getCell("f122").getColumn().getName(), row.getCell("f123").getColumn().getName(), subStr);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка одновременного заполнения доходов и расходов
        fillRow(row);
        row.getCell("income").setValue(100L, row.getIndex());
        row.getCell("outcome").setValue(100L, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %d: Значение граф «%s» и «%s» не должны быть одновременно больше «0»!",
                row.getIndex(), row.getCell("income").getColumn().getName(), row.getCell("outcome").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5. Проверка неотрицательности доходов
        fillRow(row);
        row.getCell("income").setValue(-100L, row.getIndex());
        row.getCell("outcome").setValue(0L, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %d: Значение графы «%s» должно быть больше или равно «0»!",
                row.getIndex(), row.getCell("income").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка неотрицательности расходов
        fillRow(row);
        row.getCell("income").setValue(0L, row.getIndex());
        row.getCell("outcome").setValue(-100L, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %d: Значение графы «%s» должно быть больше или равно «0»!",
                row.getIndex(), row.getCell("outcome").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 7. Проверка одновременного заполнения полей «Код предмета сделки»
        fillRow(row);
        row.getCell("dealSubjectCode1").setValue(1L, row.getIndex());
        row.getCell("dealSubjectCode2").setValue(1L, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %d: Значение граф «%s» и «%s» не должны быть одновременно заполнены!",
                row.getIndex(), row.getCell("dealSubjectCode1").getColumn().getName(), row.getCell("dealSubjectCode2").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size() + 1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Консолидация - нет источников
    @Test
    public void composeNotSourcesTest() {
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        // нет источника 4.2
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(0, testHelper.getDataRowHelper().getAll().size());
        int i = 0;
        ReportPeriod reportPeriod = testHelper.getReportPeriodService().get(1);
        String subStr = String.format("%s %s", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
        String msg = String.format("Не существует формы-источника «Приложение 4.2» в статусе «Принята» за период «%s»!", subStr);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // есть источник 4.2, нет других источников
        mockCheckApp4_2();
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(null);
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(0, testHelper.getDataRowHelper().getAll().size());
        Assert.assertEquals(0, entries.size());
        testHelper.getLogger().clear();

        checkLogger();
    }

    // Консолидация - все источники по отдельности
    @Test
    public void composeAllSourceTest() {
        mockCheckApp4_2();

        HashMap<Integer, Integer> sourceExpectedMap = new LinkedHashMap<Integer, Integer>();
        sourceExpectedMap.put(816, 2);
        sourceExpectedMap.put(804, 2);
        sourceExpectedMap.put(812, 2);
        sourceExpectedMap.put(813, 2);
        sourceExpectedMap.put(814, 2);
        sourceExpectedMap.put(806, 2);
        sourceExpectedMap.put(805, 2);
        sourceExpectedMap.put(815, 2);
        sourceExpectedMap.put(817, 2);
        sourceExpectedMap.put(823, 2);
        sourceExpectedMap.put(825, 2);
        sourceExpectedMap.put(827, 2);
        sourceExpectedMap.put(819, 2);
        sourceExpectedMap.put(826, 2);
        sourceExpectedMap.put(835, 2);
        sourceExpectedMap.put(837, 2);
        sourceExpectedMap.put(839, 2);
        sourceExpectedMap.put(811, 2);
        sourceExpectedMap.put(838, 2);
        sourceExpectedMap.put(828, 2);
        sourceExpectedMap.put(831, 2);
        sourceExpectedMap.put(830, 2);
        sourceExpectedMap.put(834, 2);
        sourceExpectedMap.put(832, 2);
        sourceExpectedMap.put(833, 2);
        sourceExpectedMap.put(836, 2);
        FormDataKind kind = FormDataKind.CONSOLIDATED;

        for (Integer key : sourceExpectedMap.keySet()) {
            // идентификатор шаблона источников
            int sourceTemplateId = key;
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

            // источник
            DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
            when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                    any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData, 1L);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
            testHelper.initRowData();

            // Консолидация
            testHelper.execute(FormDataEvent.COMPOSE);

            int expected = sourceExpectedMap.get(key);
            Assert.assertEquals(getMsg(sourceFormData), expected, testHelper.getDataRowHelper().getAll().size());
            testHelper.getLogger().containsLevel(LogLevel.ERROR);
        }
    }

    // Консолидация - все источники сразу
    @Test
    public void composeTest() {
        int[] sourceTemplateIds = new int [] { 816, 804, 812, 813, 814, 806, 805, 815, 817, 823, 825, 827, 819, 826, 835, 837, 839, 811, 838, 828, 831, 830, 834, 832, 833, 836};
        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>(sourceTemplateIds.length);
        FormDataKind kind = FormDataKind.CONSOLIDATED;
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

            // источник
            DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
            departmentFormTypes.add(departmentFormType);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData, 1L);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
        }
        // источник
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);

        testHelper.initRowData();
        mockCheckApp4_2();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        int expected = sourceTemplateIds.length + 1; // 26 сток из 26 источников + 1 строка для граппировки по организации
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        testHelper.getLogger().containsLevel(LogLevel.ERROR);
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 6;
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
    }

    /**
     * Получить источник.
     *
     * @param id идентификатор источника
     * @param sourceTypeId идентификатор типа формы источника
     * @param kind вид формы источника
     */
    private DepartmentFormType getSource(int id, int sourceTypeId, FormDataKind kind) {
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setId(id);
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setKind(kind);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        return departmentFormType;
    }

    /**
     * Получить форму источника.
     *
     * @param id идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.DEAL);
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
     * Получить сообещния для проверки количества строк после консолидации.
     *
     * @param sourceFormData форма источника
     */
    private String getMsg(FormData sourceFormData) {
        return String.format("Неверное количество строк при консолидации в сводный отчет из иcточника %s - \"%s\" (type_id = %d, template_id = %d)",
                templatesPathMap.get(sourceFormData.getFormTemplateId()),
                sourceFormData.getFormType().getName(),
                sourceFormData.getFormType().getId(),
                sourceFormData.getFormTemplateId());
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData форма источника
     * @param refbook9Id идентификатор для графы организации (юр лица), влияющая на группировку
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData, long refbook9Id) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "test_" + sourceTemplateId;
        Date testDate = new Date();
        Long testLong = 100L;
        Long testRefbookId = 1L;
        DataRow<Cell> row;
        switch (sourceTemplateId) {
            case 816:
            case 804:
            case 812:
            case 813:
            case 814:
            case 806:
            case 805:
            case 815:
            case 817:
            case 823:
            case 825:
            case 827:
            case 819:
            case 826:
            case 835:
            case 837:
            case 839:
            case 811:
            case 838:
            case 828:
            case 831:
            case 830:
            case 834:
            case 832:
            case 833:
            case 836:
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(1L, null);
                dataRows.add(row);
                break;
            case 803:
                Map<Long, Map<String, RefBookValue>> records = getMockHelper().getRefBookAllRecords(520L);
                Set<Long> ids = records.keySet();
                for (Long id : ids) {
                    DataRow<Cell> dataRow = sourceFormData.createDataRow();
                    dataRow.getCell("name").setValue(id, null);
                    dataRow.getCell("sign").setValue(1, null);
                    dataRows.add(dataRow);
                }
                break;
        }
        return dataRows;
    }

    private void mockCheckApp4_2() {
        int app4_2FormTypeId = 803;
        // форма источника
        FormData sourceFormData = getSourceFormData(app4_2FormTypeId, app4_2FormTypeId);
        when(testHelper.getFormDataService().getLast(eq(app4_2FormTypeId), eq(FormDataKind.SUMMARY), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // строки и хелпер источника
        List<DataRow<Cell>> dataRows = getFillDataRows(app4_2FormTypeId, sourceFormData, 1L);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.save(dataRows);
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
    }

    private void fillRow(DataRow<Cell> row) {
        Date date = new Date();
        // графа 3
        row.getCell("interdependenceSing").setValue(2L, row.getIndex());
        // графа 4
        row.getCell("f121").setValue(2L, row.getIndex());
        // графа 5
        row.getCell("f122").setValue(1L, row.getIndex());
        // графа 6
        row.getCell("f123").setValue(1L, row.getIndex());
        // графа 7
        row.getCell("f124").setValue(2L, row.getIndex());
        // графа 8
        row.getCell("f131").setValue(2L, row.getIndex());
        // графа 9
        row.getCell("f132").setValue(2L, row.getIndex());
        // графа 10
        row.getCell("f133").setValue(2L, row.getIndex());
        // графа 11
        row.getCell("f134").setValue(2L, row.getIndex());
        // графа 12
        row.getCell("f135").setValue(2L, row.getIndex());
        // графа 13
        row.getCell("similarDealGroup").setValue(2L, row.getIndex());
        // графа 14
        row.getCell("dealNameCode").setValue(2L, row.getIndex());
        // графа 15
        row.getCell("taxpayerSideCode").setValue(1L, row.getIndex());
        // графа 16
        row.getCell("dealPriceSign").setValue(2L, row.getIndex());
        // графа 17
        row.getCell("dealPriceCode").setValue(1L, row.getIndex());
        // графа 18
        row.getCell("dealMemberCount").setValue(2, row.getIndex());
        // графа 19
        row.getCell("income").setValue(100, row.getIndex());
        // графа 20
        row.getCell("incomeIncludingRegulation").setValue(100, row.getIndex());
        // графа 21
        row.getCell("outcome").setValue(0, row.getIndex());
        // графа 22
        row.getCell("outcomeIncludingRegulation").setValue(100, row.getIndex());
        // графа 24
        row.getCell("dealType").setValue(1L, row.getIndex());
        // графа 25
        row.getCell("dealSubjectName").setValue("test", row.getIndex());
        // графа 26
        row.getCell("dealSubjectCode1").setValue(1L, row.getIndex());
        // графа 27
        row.getCell("dealSubjectCode2").setValue(null, row.getIndex());
        // графа 28
        row.getCell("dealSubjectCode3").setValue(1L, row.getIndex());
        // графа 29
        row.getCell("otherNum").setValue(100, row.getIndex());
        // графа 30
        row.getCell("contractNum").setValue("test", row.getIndex());
        // графа 31
        row.getCell("contractDate").setValue(date, row.getIndex());
        // графа 32
        row.getCell("countryCode").setValue(1L, row.getIndex());
        // графа 33
        row.getCell("countryCode1").setValue(1L, row.getIndex());
        // графа 34
        row.getCell("region1").setValue(1L, row.getIndex());
        // графа 35
        row.getCell("city1").setValue("test", row.getIndex());
        // графа 36
        row.getCell("locality1").setValue("test", row.getIndex());
        // графа 37
        row.getCell("countryCode2").setValue(1L, row.getIndex());
        // графа 38
        row.getCell("region2").setValue(1L, row.getIndex());
        // графа 39
        row.getCell("city2").setValue("test", row.getIndex());
        // графа 40
        row.getCell("locality2").setValue("test", row.getIndex());
        // графа 41
        row.getCell("deliveryCode").setValue(1L, row.getIndex());
        // графа 42
        row.getCell("okeiCode").setValue(1L, row.getIndex());
        // графа 43
        row.getCell("count").setValue(100, row.getIndex());
        // графа 44
        row.getCell("price").setValue(100, row.getIndex());
        // графа 45
        row.getCell("total").setValue(100, row.getIndex());
        // графа 46
        row.getCell("dealDoneDate").setValue(date, row.getIndex());
        // графа 48
        row.getCell("dealMemberNum").setValue(100L, row.getIndex());
        // графа 50
        row.getCell("countryCode3").setValue(1L, row.getIndex());
        // графа 51
        row.getCell("organName").setValue(1L, row.getIndex());
    }
}