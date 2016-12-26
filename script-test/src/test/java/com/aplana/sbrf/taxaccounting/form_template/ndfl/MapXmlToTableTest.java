package com.aplana.sbrf.taxaccounting.form_template.ndfl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class MapXmlToTableTest extends ScriptTestBase {

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


  /*  public void f(){

        File xmlFile = null;
        Writer fileWriter = null;


            try {
                LOG.info(String.format("Создание временного файла для записи расчета для декларации %s", declarationData.getId()));
                stateLogger.updateState("Создание временного файла для записи расчета");
                try {
                    xmlFile = File.createTempFile("file_for_validate", ".xml");
                    fileWriter = new FileWriter(xmlFile);
                    fileWriter.write(XML_HEADER);
                } catch (IOException e) {
                    throw new ServiceException("Ошибка при формировании временного файла для XML", e);
                }
                exchangeParams.put(DeclarationDataScriptParams.XML, fileWriter);
                LOG.info(String.format("Формирование XML-файла декларации %s", declarationData.getId()));
                stateLogger.updateState("Формирование XML-файла");
                declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceException();
                }
            } finally {
                IOUtils.closeQuietly(fileWriter);
            }


    }*/


    @Test
    public void create() throws IOException {

        System.out.println("run test");

        InputStream xmlInputStream = MapXmlToTableTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/ndfl/rnu_ndfl.xml");
        //String xml = IOUtils.toString(xmlInputStream, "windows-1251");

        Map<String, Object> param = new HashedMap<String, Object>();
        param.put("xmlInputStream", xmlInputStream);
        testHelper.execute(FormDataEvent.CALCULATE, param);



       /// while (xmlStreamReader.hasNext()) {
       //     xmlStreamReader.next();
       // }



        //Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        //checkLogger();
    }


    //public getXml




}
