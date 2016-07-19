package com.aplana.sbrf.taxaccounting.form_template.deal.app_9.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

/**
 * Приложение 9. Отчет в отношении доходов и расходов ПАО "Сбербанк России"
 */
public class App_9Test extends ScriptTestBase {
    private static final int TYPE_ID = 854;
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
        formData.setPeriodOrder(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(App_9Test.class);
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

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // Проверка заполнения граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;

        Assert.assertEquals("Строка 1: Графа «Полное наименование и ОПФ юридического лица» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки с ценными бумагами» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки купли-продажи иностранной валюты» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки купли-продажи драгоценных металлов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки, отраженные в Журнале взаиморасчетов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки с лицами, информация о которых не отражена в Журнале взаиморасчетов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма дополнительно начисленных налогооблагаемых доходов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки с ценными бумагами» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки купли-продажи иностранной валюты» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки купли-продажи драгоценных металлов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки, отраженные в Журнале взаиморасчетов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сделки с лицами, информация о которых не отражена в Журнале взаиморасчетов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма дополнительно начисленных налогооблагаемых расходов» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Итого, руб.» не заполнена!", entries.get(i++).getMessage());

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }
}
