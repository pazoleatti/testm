package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

@Local(AsyncTaskLocal.class)
@Remote(AsyncTaskRemote.class)
@Stateless
@Interceptors(AsyncTaskInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class XmlGeneratorAsyncTask extends AbstractAsyncTask {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private ReportService reportService;

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        System.out.println("XmlGeneratorAsyncTask.params: " + params.toString());
        Date docDate = (Date)params.get("docDate");
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));


        //DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        Logger logger = new Logger();

        declarationDataService.calculate(logger, declarationDataId, userInfo, docDate);/*
        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, docDate);
        StringWriter writer = new StringWriter();
        exchangeParams.put(DeclarationDataScriptParams.XML, writer);

        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);

        String xml = XML_HEADER.concat(writer.toString());
        declarationData.setXmlDataUuid(blobDataService.create(new ByteArrayInputStream(xml.getBytes()), ""));
        reportService.createDec(declarationDataId, blobDataService.create(new ByteArrayInputStream(xml.getBytes()), ""), ReportType.XML_DEC);

        declarationDataService.validateDeclaration(declarationData, logger, false, FormDataEvent.CALCULATE);
        // Заполнение отчета и экспорт в формате PDF
        JasperPrint jasperPrint = fillReport(xml,
                declarationTemplateService.getJasper(declarationData.getDeclarationTemplateId()));

        reportService.createDec(declarationDataId, blobDataService.create(new ByteArrayInputStream(exportPDF(jasperPrint)), ""), ReportType.PDF_DEC);
        try {
            reportService.createDec(declarationDataId, blobDataService.create(new ByteArrayInputStream(exportJPBlobData(jasperPrint)), ""), ReportType.JASPER_DEC);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }*/
    }

    private JasperPrint fillReport(String xml, InputStream jasperTemplate) {
        try {
            InputSource inputSource = new InputSource(new StringReader(xml));
            Document document = JRXmlUtils.parse(inputSource);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT,
                    document);

            return JasperFillManager.fillReport(jasperTemplate, params);
        } catch (Exception e) {
            throw new ServiceException("Невозможно заполнить отчет", e);
        }
    }

    private byte[] exportPDF(JasperPrint jasperPrint) {
        try {
            JRPdfExporter exporter = new JRPdfExporter();
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT,
                    jasperPrint);
            exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, data);
            exporter.getPropertiesUtil().setProperty(JRPdfExporterParameter.PROPERTY_SIZE_PAGE_TO_CONTENT, "true");

            exporter.exportReport();
            return data.toByteArray();
        } catch (Exception e) {
            throw new ServiceException("Невозможно экспортировать отчет в PDF",
                    e);
        }
    }

    private byte[] exportJPBlobData(JasperPrint jasperPrint) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(jasperPrint);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    protected String getAsyncTaskName() {
        return "Генерация xml-файла";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        //TODO
        return null;
    }
}
