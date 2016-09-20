package com.aplana.sbrf.taxaccounting.form_template.market.summary_2_6.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 2.6 (Сводный) Отчет о состоянии кредитного портфеля
 */
public class Summary_2_6Test extends ScriptTestBase {
    private static final int TYPE_ID = 904;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

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
        return getDefaultScriptTestMockHelper(Summary_2_6Test.class);
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

    @Test
    public void check1Test() {
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        String msg;
        int i;
        int rowIndex = row.getIndex();

        // Дополнительная проверка - ошибок быть не должно
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        checkLogger();
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 1. Проверка заполнения обязательных полей
        // очистить значения
        for (Column column : formData.getFormColumns()) {
            row.getCell(column.getAlias()).setValue(null, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);
        // должно быть много сообщении об незаполненности обязательных полей
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
        String [] nonEmptyColumns = { "rowNum", "codeBank", "nameBank", "depNumber", "okved", "opf", "debtorName",
                "inn", "sign", "direction", "law", "creditType", "docNum", "docDate", "closeDate", "extendNum",
                "creditMode", "currencySum", "sumDoc", "sumGiven", "rate", "payFrequency", "currencyCredit",
                "debtSum", "inTimeDebtSum", "overdueDebtSum", "percentDebtSum", "provision", "loanSign",
                "loanQuality", "finPosition", "debtService", "creditRisk", "reservePercent", "reserveSum" };
        i = 0;
        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        // получение макета текущей форму
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        when(testHelper.getFormDataService().getFormTemplate(eq(getFormData().getFormTemplateId()))).thenReturn(formData.getFormTemplate());

        int expected = testHelper.getDataRowHelper().getAll().size();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // консолидация с 1 источником
    @Test
    public void compose1SourceTest() {
        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relarion = new Relation();
        relarion.setFormDataId(1L);
        sourcesInfo.add(relarion);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        when(testHelper.getFormDataService().get(anyLong(), isNull(Boolean.class))).thenReturn(sourceFormData);

        // получение строк источника
        List<DataRow<Cell>> sourceDataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> sourceDataRow = sourceFormData.createDataRow();
        sourceDataRow.setIndex(1);
        setDefaultValues(sourceDataRow);
        sourceDataRows.add(sourceDataRow);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(sourceDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceFormData))).thenReturn(sourceDataRowHelper);
        int expected;

        // консолидация должна пройти нормально
        expected = sourceDataRows.size();
        testHelper.execute(FormDataEvent.COMPOSE);
        // проверка количества строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // проверка значении
        String [] allColumns = { "rowNum", "codeBank", "nameBank", "depNumber", "okved", "opf", "debtorName",
                "inn", "sign", "direction", "law", "creditType", "docNum", "docDate", "creditDate", "closeDate",
                "extendNum", "creditMode", "currencySum", "sumDoc", "sumGiven", "rate", "payFrequency",
                "currencyCredit", "debtSum", "inTimeDebtSum", "overdueDebtSum", "percentDebtSum", "deptDate",
                "percentDate", "percentPeriod", "provision", "provisionComment", "loanSign", "loanQuality",
                "finPosition", "debtService", "creditRisk", "portfolio", "reservePercent", "reserveSum" };
        for (int i = 0; i < expected; i++ ) {
            DataRow<Cell> sRow = sourceDataRows.get(i);
            DataRow<Cell> row = sourceDataRows.get(i);
            for (String column : allColumns) {
                Assert.assertEquals(sRow.getCell(column).getValue(), row.getCell(column).getValue());
            }
        }

        checkLogger();
    }

    private void setDefaultValues(DataRow<Cell> row) {
        int index = row.getIndex();
        long refbookRecordId = 1L;
        long number = 1L;
        Date date = new Date();
        String str = "test";

        // графа 1
        row.getCell("rowNum").setValue(index, index);
        // графа 2
        row.getCell("codeBank").setValue(str, index);
        // графа 3
        row.getCell("nameBank").setValue(str, index);
        // графа 4
        row.getCell("depNumber").setValue(str, index);
        // графа 5
        row.getCell("okved").setValue(str, index);
        // графа 6
        row.getCell("opf").setValue(refbookRecordId, index);
        // графа 7
        row.getCell("debtorName").setValue(str, index);
        // графа 8
        row.getCell("inn").setValue(str, index);
        // графа 9
        row.getCell("sign").setValue(number, index);
        // графа 10
        row.getCell("direction").setValue("tst", index);
        // графа 11
        row.getCell("law").setValue(str, index);
        // графа 12
        row.getCell("creditType").setValue(str, index);
        // графа 13
        row.getCell("docNum").setValue(str, index);
        // графа 14
        row.getCell("docDate").setValue(date, index);
        // графа 15
        row.getCell("creditDate").setValue(date, index);
        // графа 16
        row.getCell("closeDate").setValue(date, index);
        // графа 17
        row.getCell("extendNum").setValue(number, index);
        // графа 18
        row.getCell("creditMode").setValue(str, index);
        // графа 19
        row.getCell("currencySum").setValue(refbookRecordId, index);
        // графа 20
        row.getCell("sumDoc").setValue(number, index);
        // графа 21
        row.getCell("sumGiven").setValue(number, index);
        // графа 22
        row.getCell("rate").setValue(number, index);
        // графа 23
        row.getCell("payFrequency").setValue(number, index);
        // графа 24
        row.getCell("currencyCredit").setValue(refbookRecordId, index);
        // графа 25
        row.getCell("debtSum").setValue(number, index);
        // графа 26
        row.getCell("inTimeDebtSum").setValue(number, index);
        // графа 27
        row.getCell("overdueDebtSum").setValue(number, index);
        // графа 28
        row.getCell("percentDebtSum").setValue(number, index);
        // графа 29
        row.getCell("deptDate").setValue(date, index);
        // графа 30
        row.getCell("percentDate").setValue(date, index);
        // графа 31
        row.getCell("percentPeriod").setValue(number, index);
        // графа 32
        row.getCell("provision").setValue(str, index);
        // графа 33
        row.getCell("provisionComment").setValue(str, index);
        // графа 34
        row.getCell("loanSign").setValue(number, index);
        // графа 35
        row.getCell("loanQuality").setValue(number, index);
        // графа 36
        row.getCell("finPosition").setValue(number, index);
        // графа 37
        row.getCell("debtService").setValue(number, index);
        // графа 38
        row.getCell("creditRisk").setValue(refbookRecordId, index);
        // графа 39
        row.getCell("portfolio").setValue(str, index);
        // графа 40
        row.getCell("reservePercent").setValue(number, index);
        // графа 41
        row.getCell("reserveSum").setValue(number, index);
    }
}
