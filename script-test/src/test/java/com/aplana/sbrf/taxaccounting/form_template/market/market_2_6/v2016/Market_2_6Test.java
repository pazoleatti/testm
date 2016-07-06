package com.aplana.sbrf.taxaccounting.form_template.market.market_2_6.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 2.6 (Ежемесячный) Отчет о состоянии кредитного портфеля
 */
public class Market_2_6Test extends ScriptTestBase {
    private static final int TYPE_ID = 900;
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
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Market_2_6Test.class);
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

    @Test
    public void addDelRowTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 1;

        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        // ошибок быть не должно
        checkLogger();
        // Количество строк должно увеличиться на 1
        Assert.assertEquals("Add new row", expected, testHelper.getDataRowHelper().getAll().size());

        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        // Количество строк должно уменьшиться на 1
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        expected--;
        // Количество строк должно уменьшиться на 1
        Assert.assertEquals("Delete row", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        FormTemplate formTemplate = testHelper.getFormTemplate();
        ((RefBookColumn) formTemplate.getColumn("opf")).setRefBookAttribute(new RefBookAttribute() {{
            setId(6051L);
            setAttributeType(RefBookAttributeType.NUMBER);
            setAlias("CODE");
            setPrecision(0);
            setName("Код ОПФ");
        }});
        ((RefBookColumn) formTemplate.getColumn("currencySum")).setRefBookAttribute(new RefBookAttribute() {{
            setId(65L);
            setAttributeType(RefBookAttributeType.STRING);
            setAlias("CODE_2");
            setName("Код валюты. Буквенный");
        }});
        ((RefBookColumn) formTemplate.getColumn("currencyCredit")).setRefBookAttribute(new RefBookAttribute() {{
            setId(65L);
            setAttributeType(RefBookAttributeType.STRING);
            setAlias("CODE_2");
            setName("Код валюты. Буквенный");
        }});
        ((RefBookColumn) formTemplate.getColumn("creditRisk")).setRefBookAttribute(new RefBookAttribute() {{
            setId(6041L);
            setAttributeType(RefBookAttributeType.STRING);
            setAlias("NAME");
            setName("Наименование");
        }});
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(formTemplate);
        when(testHelper.getRefBookFactory().getByAttribute(eq(6051L))).thenReturn(new RefBook() {{
            setId(605L);
        }});
        when(testHelper.getRefBookFactory().getByAttribute(eq(6041L))).thenReturn(new RefBook() {{
            setId(604L);
        }});
        when(testHelper.getRefBookFactory().getByAttribute(eq(65L))).thenReturn(new RefBook() {{
            setId(15L);
        }});
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
        int expected = 1; // в файле 0 строк
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }
    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // пропускаю первый столбец, по 10 на строку
        compareRow(dataRows.get(0), "111", "111", "111", "111", 2L, "111", "111", 11.00, "111", "111",
                "111", "111", "01.01.2016", "02.01.2016", "03.01.2016", 0.00, "111", 1L, 111.00, 111.00,
                111.00, 111.00, 1L, 111.00, 111.00, 111.00, 111.00, "04.01.2016", "05.01.2016", 111.00,
                "111", "111", 11.00, 11.00, 11.00, 11.00, 2L, "111", 11.00, 111.00);
    }

    void compareRow(DataRow<Cell> row, Object... args) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        int skipColumnCount = 1;
        List<Column> columns = testHelper.getFormTemplate().getColumns();
        for (int i = 0; i < (columns.size() - skipColumnCount); i++) {
            int columnCount = i + skipColumnCount;
            Column column = columns.get(columnCount);
            Object expected = null;
            String alias = column.getAlias();
            if (i < args.length) {
                expected = args[i];
            }
            if (expected != null) {
                if (column.getColumnType() == ColumnType.NUMBER) {
                    expected = BigDecimal.valueOf((Double) expected).setScale(((NumericColumn)column).getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
                if (column.getColumnType() == ColumnType.DATE) {
                    try {
                        expected = format.parse((String)expected);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, row.getCell(alias).getValue());
            } else {
                Assert.assertNull("row." + alias + "[" + row.getIndex() + "]", row.getCell(alias).getValue());
            }
        }
    }
}
