package com.aplana.sbrf.taxaccounting.form_template.deal.app_4_2.v2015;

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
 * Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода
 */
public class App_4_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 803;
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
        return getDefaultScriptTestMockHelper(App_4_2Test.class);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//app_4_2//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // Проверка заполнения граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;

        Assert.assertEquals("Строка 1: Графа «Полное наименование и ОПФ юридического лица» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Группа юридического лица» не заполнена!", entries.get(i++).getMessage());
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
        Assert.assertEquals("Строка 1: Графа «Итого объем доходов и расходов, руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Применимое Пороговое значение, руб» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Признак наличия Контролируемых сделок (Да / Нет)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Пересмотренная Категория юридического лица по состоянию на 1 апреля» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для типа участника ТЦО «null» не задано пороговое значение в данном Налоговом периоде!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном Налоговом периоде!", entries.get(i++).getMessage());

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        row.getCell("name").setValue(1L, null);
        row.getCell("group").setValue(1L, null);
        row.getCell("sum51").setValue(1L, null);
        row.getCell("sum52").setValue(1L, null);
        row.getCell("sum53").setValue(1L, null);
        row.getCell("sum54").setValue(1L, null);
        row.getCell("sum55").setValue(1L, null);
        row.getCell("sum56").setValue(1L, null);
        row.getCell("sum61").setValue(1L, null);
        row.getCell("sum62").setValue(1L, null);
        row.getCell("sum63").setValue(1L, null);
        row.getCell("sum64").setValue(1L, null);
        row.getCell("sum65").setValue(1L, null);
        row.getCell("sum66").setValue(1L, null);
        row.getCell("sum7").setValue(1L, null);
        row.getCell("thresholdValue").setValue(1L, null);
        row.getCell("sign").setValue(1L, null);
        row.getCell("categoryRevised").setValue(1L, null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Для типа участника ТЦО «null» не задано пороговое значение в данном Налоговом периоде!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном Налоговом периоде!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов указана неверная категория!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }
}
