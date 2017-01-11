package com.aplana.sbrf.taxaccounting.form_template.fond.t.v2016;

import com.aplana.sbrf.taxaccounting.form_template.ndfl.MapXmlToTableTest;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Тестирование разбора xml-файла Расчета по страховым взносам
 */
public class RaschsvParseTest extends ScriptTestBase {

    /**
     * Тип формы НДФЛ
     */
    private static final int TYPE_ID = 814;
    /**
     * Подразделение
     */
    private static final int DEPARTMENT_ID = 1;

    /**
     * Идентификатор отчетного периода
     */
    private static final int REPORT_PERIOD_ID = 1;

    private static final int DEPARTMENT_PERIOD_ID = 1;

    /**
     * Тип формы первичная
     */
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
        return getDefaultScriptTestMockHelper(MapXmlToTableTest.class);
    }


    /**
     * Инициализация перед каждым отдельным тестом
     */
    @Before
    public void init() {
        // Хэлпер хранится статично для оптимизации, чтобы он был один для всех тестов отдельного скрипта
        if (testHelper == null) {
            String path = getFolderPath();
            if (path == null) {
                throw new ServiceException("Test folder path is null!");
            }
            testHelper = new TestScriptHelper(path, getFormData(), getMockHelper()) {
                @Override
                protected void initFormTemplate() {
                    //do nothing...
                }

                @Override
                public void initRowData() {
                    //do nothing...
                }
            };
        }
        testHelper.reset();
    }

    /**
     * Тестирование числа узлов, которые были перебраны
     * @throws IOException
     */
    @Test
    public void create() throws IOException {

        InputStream inputStream = MapXmlToTableTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/t/v2016/raschsv.xml");

        Map<String, Object> param = new HashedMap<String, Object>();

        // Проверка соответствия числа узлов по их имени
        Map<String, Integer> countNodes = new HashedMap<String, Integer>();
        countNodes.put("ПерсСвСтрахЛиц", 10);
        countNodes.put("УплПерОПС", 1);
        countNodes.put("УплПерОМС", 1);
        countNodes.put("УплПерОПСДоп", 10);
        countNodes.put("УплПерДСО", 10);
        countNodes.put("УплПерОСС", 1);
        countNodes.put("ПревРасхОСС", 1);
        countNodes.put("РасчСВ_ОПС_ОМС", 10);
        countNodes.put("РасчСВ_ОСС.ВНМ", 1);
        countNodes.put("РасхОССЗак", 1);
        countNodes.put("ВыплФинФБ", 1);

        countNodes.put("ПравТариф3.1.427", 1);
        countNodes.put("ПравТариф5.1.427", 1);
        countNodes.put("ПравТариф7.1.427", 1);
        countNodes.put("СвПримТариф9.1.427", 1);
        countNodes.put("СвПримТариф2.2.425", 1);
        countNodes.put("СвПримТариф1.3.422", 1);

        countNodes.put("СведПатент", 10);
        countNodes.put("СвИноГражд", 10);
        countNodes.put("СведОбуч", 10);

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        param.put("declarationData", declarationData);
        param.put("countNodes", countNodes);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, param);

        checkLogger();
    }
}
