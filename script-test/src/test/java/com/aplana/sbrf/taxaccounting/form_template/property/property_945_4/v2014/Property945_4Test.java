package com.aplana.sbrf.taxaccounting.form_template.property.property_945_4.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Расчёт налога на имущество по средней/среднегодовой стоимости.
 *
 * @author Alexey Afanasyev
 */
public class Property945_4Test extends ScriptTestBase {
    private static final int TYPE_ID = 612;
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
        return getDefaultScriptTestMockHelper(Property945_4Test.class);
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
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        // идентификатор шаблона источника
        int sourceTypeId = 611;
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//property//property_945_2//v2014//");

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются вручную
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        // формируем три строки источника
        for (int i = 1; i <= 3; i++) {

            DataRow<Cell> row = sourceFormData.createDataRow();
            // графа 1
            row.getCell("rowNum").setValue(i, null);
            // графа 2
            row.getCell("fix").setValue("test", null);
            // графа 3
            row.getCell("subject").setValue(1L, null);
            // графа 4
            row.getCell("taxAuthority").setValue("test", null);
            // графа 5
            row.getCell("kpp").setValue("testB", null);
            // графа 6
            row.getCell("oktmo").setValue(1L, null);
            // графа 7
            row.getCell("address").setValue("testB", null);
            // графа 8
            row.getCell("sign").setValue(1L, null);
            // графа 9
            row.getCell("cadastreNumBuilding").setValue("testB", null);
            // графа 10
            row.getCell("cadastreNumRoom").setValue("testB", null);
            // графа 11
            row.getCell("cadastrePriceJanuary").setValue(1L, null);
            // графа 12
            row.getCell("cadastrePriceTaxFree").setValue(1L, null);
            // графа 13
            row.getCell("propertyRightBeginDate").setValue(null, null);
            // графа 14
            row.getCell("propertyRightEndDate").setValue(null, null);
            // графа 15
            row.getCell("taxBenefitCode").setValue(1L, null);
            // графа 16
            row.getCell("benefitBasis").setValue("testA", null);

            dataRows.add(row);
        }

        sourceDataRowHelper.save(dataRows);
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(3, testHelper.getDataRowHelper().getAll().size());
    }
}
