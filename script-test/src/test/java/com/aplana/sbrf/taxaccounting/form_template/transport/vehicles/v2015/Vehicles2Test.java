package com.aplana.sbrf.taxaccounting.form_template.transport.vehicles.v2015;

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
import java.util.*;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.WRONG_NON_EMPTY;
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.getColumnName;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог
 * @author Yasinskii, Kinzyabulatov
 */
public class Vehicles2Test extends ScriptTestBase {
    private static final int TYPE_ID = 2201;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formType.setName("Сведения о транспортных средствах, по которым уплачивается транспортный налог");
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
        return getDefaultScriptTestMockHelper(Vehicles2Test.class);
    }

    @Before
    public void mockBefore() {
        // Для работы логических проверок
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String)invocation.getArguments()[2];
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        if (filter.equals("DECLARATION_REGION_ID = 1 and OKTMO = 1")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("REGION_ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        }
                        return result;
                    }
                });
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });

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
        checkLogger();
    }

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
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

    @Test
    public void afterCreateTest() {
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются импортом
        testHelper.setImportFileInputStream(getCustomInputStream("importFileCompose.xlsm"));
        testHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(testHelper.getDataRowHelper().getAll());
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        int expected = 4; // 3 строки фиксированны (заголовки разделов) + 1 строка из тф
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        //checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    private void checkLoadData(List<DataRow<Cell>> dataRows) {

        // графа 11
        Assert.assertEquals("12.01.2012", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(1).getCell("regDate").getDateValue())));
        Assert.assertEquals("12.01.2012", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(2).getCell("regDate").getDateValue())));
        Assert.assertEquals("12.02.2011", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(4).getCell("regDate").getDateValue())));
        Assert.assertEquals("20.01.2008", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(6).getCell("regDate").getDateValue())));

        // графа 12
        Assert.assertEquals("12.01.2014", String.valueOf(new SimpleDateFormat("dd.MM.yyyy").format(dataRows.get(1).getCell("regDateEnd").getDateValue())));

        // графа 13
        Assert.assertEquals(12.0, dataRows.get(1).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(23.0, dataRows.get(2).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(34.0, dataRows.get(4).getCell("taxBase").getNumericValue().doubleValue(), 0.0);
        Assert.assertEquals(56.0, dataRows.get(6).getCell("taxBase").getNumericValue().doubleValue(), 0.0);

        // графа 14
        Assert.assertEquals(1, dataRows.get(1).getCell("baseUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(2).getCell("baseUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(4).getCell("baseUnit").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(6).getCell("baseUnit").getNumericValue().intValue());

        // графа 15
        SimpleDateFormat yFormat = new SimpleDateFormat("yyyy");
        Assert.assertEquals("2008", String.valueOf(yFormat.format(dataRows.get(1).getCell("year").getDateValue())));
        Assert.assertEquals("2012", String.valueOf(yFormat.format(dataRows.get(2).getCell("year").getDateValue())));
        Assert.assertEquals("2001", String.valueOf(yFormat.format(dataRows.get(4).getCell("year").getDateValue())));
        Assert.assertEquals("2011", String.valueOf(yFormat.format(dataRows.get(6).getCell("year").getDateValue())));

        // графа 20
        Assert.assertEquals(12.0, dataRows.get(1).getCell("costOnPeriodBegin").getNumericValue().doubleValue(), 0.0);
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
        String[] nonEmptyColumns = {"codeOKATO", "tsTypeCode", "model", "identNumber", "regNumber", "regDate", "taxBase", "baseUnit", "year", "pastYear", "share"};
        testHelper.execute(FormDataEvent.CHECK);

        for (String alias : nonEmptyColumns) {
            String columnName = getColumnName(row, alias);
            msg = String.format(WRONG_NON_EMPTY, rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 2. Проверка на наличие в форме строк с одинаковым значением граф 2, 4, 8, 12, 13
    @Test
    public void check2Test() throws ParseException {
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
            dataRow.getCell("codeOKATO").setValue(1L, rowIndex);
            dataRow.getCell("tsTypeCode").setValue(6L, rowIndex);
            dataRow.getCell("model").setValue("Модель", rowIndex);
            dataRow.getCell("identNumber").setValue("идентнамбер", rowIndex);
            dataRow.getCell("regNumber").setValue("регнамбер", rowIndex);
            dataRow.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
            dataRow.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
            dataRow.getCell("baseUnit").setValue(1L, rowIndex);
            dataRow.getCell("year").setValue(format.parse("01.01.2012"), rowIndex);
            dataRow.getCell("pastYear").setValue(2, rowIndex);
            dataRow.getCell("share").setValue("18/23", rowIndex);
            dataRow.getCell("benefitStartDate").setValue(format.parse("01.01.2014"), rowIndex);
            dataRow.getCell("taxBenefitCode").setValue(1L, rowIndex);
            dataRow.getCell("base").setValue("00A100B100C1", rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Cтроки %s. Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                        "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, налоговой базой и единицей измерения налоговой базы по ОКЕИ",
                "1, 2", "7190000", "56100", "идентнамбер", "1.00", "A");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 3. Проверка кода вида ТС (графа 4) по разделу «Наземные транспортные средства»
    // 4, 5 аналогичны
    @Test
    public void check3Test() throws ParseException {
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("codeOKATO").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(1L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("identNumber").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("baseUnit").setValue(1L, rowIndex);
        row.getCell("year").setValue(format.parse("01.01.2012"), rowIndex);
        row.getCell("pastYear").setValue(2, rowIndex);
        row.getCell("share").setValue("18/23", rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Значение графы «Код вида ТС» должно относиться к виду ТС «%s»",
                rowIndex, "Наземные транспортные средства");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 6. Проверка корректности заполнения даты регистрации ТС
    // 7. Проверка корректности заполнения даты снятия с регистрации ТС
    // 8. Проверка года изготовления ТС
    // 9. Проверка количества лет, прошедших с года выпуска ТС
    // 10. Проверка на наличие даты начала розыска ТС при указании даты возврата ТС
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
        row.getCell("codeOKATO").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("identNumber").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.06.2013"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("baseUnit").setValue(1L, rowIndex);
        row.getCell("year").setValue(format.parse("01.01.2015"), rowIndex);
        row.getCell("pastYear").setValue(1, rowIndex);
        row.getCell("stealDateEnd").setValue(format.parse("01.06.2013"), rowIndex);
        row.getCell("share").setValue("18/23", rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                rowIndex, getColumnName(row, "regDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                rowIndex, getColumnName(row, "regDateEnd"), "01.01.2014", getColumnName(row, "regDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»",
                rowIndex, getColumnName(row, "year"), "2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Графа «%s» заполнена неверно. Выполните расчет формы",
                rowIndex, getColumnName(row, "pastYear"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена, если заполнена графа «%s»",
                rowIndex, getColumnName(row, "stealDateStart"), getColumnName(row, "stealDateEnd"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 11. Проверка на соответствие дат сведений об угоне
    // 12. Проверка доли налогоплательщика в праве на ТС (графа 18) на корректность формата введенных данных
    // 13. Проверка значения знаменателя доли налогоплательщика в праве на ТС (графа 18)
    // 14. Проверка корректности заполнения даты начала использования льготы
    // 15. Проверка корректности заполнения даты окончания использования льготы
    // 16. Проверка на наличие даты начала использования льготы и кода налоговой льготы
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
        row.getCell("codeOKATO").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("identNumber").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("baseUnit").setValue(1L, rowIndex);
        row.getCell("year").setValue(format.parse("01.01.2013"), rowIndex);
        row.getCell("pastYear").setValue(1, rowIndex);
        row.getCell("stealDateStart").setValue(format.parse("01.07.2013"), rowIndex);
        row.getCell("stealDateEnd").setValue(format.parse("01.06.2013"), rowIndex);
        row.getCell("share").setValue("18/0", rowIndex);
        row.getCell("benefitStartDate").setValue(format.parse("01.06.2015"), rowIndex);
        row.getCell("benefitEndDate").setValue(format.parse("01.01.2015"), rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»",
                rowIndex, getColumnName(row, "stealDateEnd"), getColumnName(row, "stealDateStart"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», числитель должен быть меньше либо равен знаменателю",
                rowIndex, getColumnName(row, "share"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", rowIndex, getColumnName(row, "share"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", rowIndex, getColumnName(row, "benefitStartDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                rowIndex, getColumnName(row, "benefitEndDate"), "01.01.2014", getColumnName(row, "benefitStartDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Графы «%s», «%s» должны быть одновременно заполнены либо не заполнены",
                rowIndex, getColumnName(row, "benefitStartDate"), getColumnName(row, "taxBenefitCode"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 17. Проверка корректности заполнения «Графы 24»
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
        row.getCell("codeOKATO").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("identNumber").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("baseUnit").setValue(1L, rowIndex);
        row.getCell("year").setValue(format.parse("01.01.2013"), rowIndex);
        row.getCell("pastYear").setValue(1, rowIndex);
        row.getCell("share").setValue("18/23", rowIndex);
        row.getCell("benefitStartDate").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBenefitCode").setValue(1L, rowIndex);
        row.getCell("base").setValue("", rowIndex); // правильно 00A100B100C1
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Графа «%s» заполнена неверно! Выполните расчет формы", rowIndex, getColumnName(row, "base"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 18. Проверка наличия повышающего коэффициента для ТС с заполненной графой 25
    // 19 и 20 должны пройти
    @Test
    public void check7Test() throws ParseException {
        final String departmentName = "Подразделение";
        final String summaryTypeName = "Расчет суммы налога по каждому транспортному средству";
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        mockDestination(departmentName, summaryTypeName, true);

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("codeOKATO").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("identNumber").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("baseUnit").setValue(1L, rowIndex);
        row.getCell("year").setValue(format.parse("01.01.2013"), rowIndex);
        row.getCell("pastYear").setValue(2, rowIndex);
        row.getCell("share").setValue("18/23", rowIndex);
        row.getCell("benefitStartDate").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBenefitCode").setValue(1L, rowIndex);
        row.getCell("base").setValue("00A100B100C1", rowIndex);
        row.getCell("version").setValue(1L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: В справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, актуальная на дату %s, " +
                        "в которой поле «Средняя стоимость» равно значению графы «%s» (%s) и значение графы «%s» (%s) больше значения поля " +
                        "«Количество лет, прошедших с года выпуска ТС (от)» и меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)»",
                rowIndex, "31.12.2014", getColumnName(row, "averageCost"), "От 3 до 5 млн. руб.", getColumnName(row, "pastYear"), row.getCell("pastYear").getNumericValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 19. Проверка наличия параметров представления декларации для кода ОКТМО
    // 20. Проверка наличия ставки для ТС
    @Test
    public void check8Test() throws ParseException {
        final String departmentName = "Подразделение";
        final String summaryTypeName = "Расчет суммы налога по каждому транспортному средству";
        FormData formData = testHelper.getFormData();
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        mockDestination(departmentName, summaryTypeName, false);

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(1, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("codeOKATO").setValue(1L, rowIndex);
        row.getCell("tsTypeCode").setValue(6L, rowIndex);
        row.getCell("model").setValue("Модель", rowIndex);
        row.getCell("identNumber").setValue("идентнамбер", rowIndex);
        row.getCell("regNumber").setValue("регнамбер", rowIndex);
        row.getCell("regDate").setValue(format.parse("01.01.2014"), rowIndex);
        row.getCell("regDateEnd").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBase").setValue(new BigDecimal("1.00"), rowIndex);
        row.getCell("baseUnit").setValue(1L, rowIndex);
        row.getCell("year").setValue(format.parse("01.01.2013"), rowIndex);
        row.getCell("pastYear").setValue(2, rowIndex);
        row.getCell("share").setValue("18/23", rowIndex);
        row.getCell("benefitStartDate").setValue(format.parse("01.06.2014"), rowIndex);
        row.getCell("taxBenefitCode").setValue(1L, rowIndex);
        row.getCell("base").setValue("00A100B100C1", rowIndex);
        row.getCell("version").setValue(2L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                        "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s» формы-приемника вида «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s»",
                rowIndex, "31.12.2014", "00", departmentName, summaryTypeName, "02", "7190000");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: В справочнике «Ставки транспортного налога» %s на дату %s, в которой поле «Код субъекта РФ представителя декларации» " +
                        "равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s» формы-приемника вида «%s», поле «Код субъекта РФ» равно " +
                        "значению «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s»",
                rowIndex, "отсутствует запись, актуальная", "31.12.2014", "00", departmentName, summaryTypeName, "02", "56100", "A");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    private void mockDestination(final String departmentName, final String summaryTypeName, boolean exist) {
        final int summaryTypeId = 203;
        final Long declarationRegionId = exist ? 1L : 2L;
        // назначаем приемник
        List<Relation> relations = new ArrayList<Relation>();
        relations.add(new Relation() {{
            setFormType(new FormType() {{
                setId(summaryTypeId);
            }});
            setDepartment( new Department() {{
                setName(departmentName);
                setRegionId(declarationRegionId);
            }});
            setFormTypeName(summaryTypeName);
        }});
        when(testHelper.getFormDataService().getDestinationsInfo(eq(testHelper.getFormData()), anyBoolean(), anyBoolean(), isNull(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(relations);
    }
}
