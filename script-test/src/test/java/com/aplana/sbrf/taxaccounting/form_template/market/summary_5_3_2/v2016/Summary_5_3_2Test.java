package com.aplana.sbrf.taxaccounting.form_template.market.summary_5_3_2.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 5.3.2 Внутренние интервалы процентных ставок по Кредитным продуктам и Субординированным кредитам.
 *
 * TODO: недоделана консолидация, т.к. источника еще нет в git
 */
public class Summary_5_3_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 908;
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
        return getDefaultScriptTestMockHelper(Summary_5_3_2Test.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
        testHelper.reset();
    }

    @Test
    public void afterCreate() {
        int expected = 0;
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
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

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        int expected = 0;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // TODO (Ramil Timerbaev) недоотлажен
    // консолидация с 1 источником
    // @Test
    public void compose1SourceTest() {
        // задать источники
        List<Relation> sourcesInfo = new ArrayList<Relation>();
        Relation relarion = new Relation();
        relarion.setFormDataId(1L); // TODO (Ramil Timerbaev)
        sourcesInfo.add(relarion);
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        // получение одного источника
        FormData sourceformData = new FormData();
        sourceformData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//market//summary_5_2//v2016//"));
        when(testHelper.getFormDataService().get(anyLong(), isNull(Boolean.class))).thenReturn(sourceformData);

        // заполнение данных источника
        List<DataRow<Cell>> sourceDataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> sourceDataRow = sourceformData.createDataRow();
        sourceDataRow.setIndex(1);
        long testRecordkId = 1L;
        // графа 2
        sourceDataRow.getCell("dealNum").setValue("test", sourceDataRow.getIndex());
        // графа 3
        sourceDataRow.getCell("debtorName").setValue("test", sourceDataRow.getIndex());
        // графа 8
        sourceDataRow.getCell("creditRating").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 15
        sourceDataRow.getCell("avgPeriod").setValue(4L, sourceDataRow.getIndex());
        // графа 16
        sourceDataRow.getCell("currencyCode").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 17
        sourceDataRow.getCell("creditSum").setValue(182L, sourceDataRow.getIndex());
        // графа 18
        sourceDataRow.getCell("rateType").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 23
        sourceDataRow.getCell("provideCategory").setValue(testRecordkId, sourceDataRow.getIndex());
        // графа 28
        sourceDataRow.getCell("economyRate").setValue(1000L, sourceDataRow.getIndex());
        // графа 29
        sourceDataRow.getCell("groupExclude").setValue(testRecordkId, sourceDataRow.getIndex());
        sourceDataRows.add(sourceDataRow);

        // получение строк источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(sourceDataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(sourceformData))).thenReturn(sourceDataRowHelper);

        testHelper.execute(FormDataEvent.COMPOSE);
        int expected = 0;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }
}
