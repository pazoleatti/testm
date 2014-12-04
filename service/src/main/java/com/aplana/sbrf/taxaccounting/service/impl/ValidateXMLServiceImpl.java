package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;

@Service
public class ValidateXMLServiceImpl implements ValidateXMLService {

    static final Log log = LogFactory.getLog(ValidateXMLService.class);

    private static final String TEMPLATE = ClassUtils
            .classPackageAsResourcePath(ValidateXMLServiceImpl.class) + "/VSAX3.exe";

    private static SAXParserFactory factory = SAXParserFactory.newInstance();

    private static final String SUCCESS_FLAG = "SUCCESS";
    public static final String TAG_FILE = "Файл";
    public static final String ATTR_FILE_ID = "ИдФайл";

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ReportService reportService;

    private URL url;

    private class SAXHandler extends DefaultHandler{
        String fileName = "";

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(TAG_FILE)){
                fileName = attributes.getValue(ATTR_FILE_ID);
                return;
            }
        }
    }

    public ValidateXMLServiceImpl() {
        this.url = Thread.currentThread().getContextClassLoader().getResource(TEMPLATE);
    }

    @Override
    public boolean validate(DeclarationData data,  TAUserInfo userInfo, Logger logger, boolean isErrorFatal) {
        String[] params = new String[4];
        assert url != null;
        DeclarationTemplate template = declarationTemplateService.get(data.getDeclarationTemplateId());

        FileOutputStream outputStream;
        InputStream inputStream;
        BufferedReader reader;
        File xsdFile = null, xmlFile = null, vsax3File = null;
        try {
            vsax3File = File.createTempFile("VSAX3",".exe");
            outputStream = new FileOutputStream(vsax3File);
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
            log.info("VSAX3.exe copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
            inputStream.close();
            outputStream.close();
            params[0] = vsax3File.getAbsolutePath();

            //Получаем xml
            xmlFile = File.createTempFile("file_for_validate",".xml");
            outputStream = new FileOutputStream(xmlFile);
            inputStream = blobDataService.get(reportService.getDec(userInfo, data.getId(), ReportType.XML_DEC)).getInputStream();
            log.info("Xml copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
            inputStream.close();
            outputStream.close();
            params[1] = xmlFile.getAbsolutePath();

            //Получаем xsd файл
            xsdFile = File.createTempFile("validation_file",".xsd");
            outputStream = new FileOutputStream(xsdFile);
            inputStream = blobDataService.get(template.getXsdId()).getInputStream();
            log.info("Xsd copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
            inputStream.close();
            outputStream.close();
            params[2] = xsdFile.getAbsolutePath();

            //Имя файла
            InputSource inputSource = new InputSource(new FileReader(xmlFile));
            SAXHandler handler = new SAXHandler();
            factory.newSAXParser().parse(inputSource, handler);
            params[3] = handler.fileName;
            log.info("File name: " + handler.fileName);

            Process process = (new ProcessBuilder(params)).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            try {
                String s = reader.readLine();
                if (s != null && s.startsWith("Result: " + SUCCESS_FLAG)) {
                    process.waitFor();
                    return true;
                } else {
                    while ((s = reader.readLine()) != null) {
                        if (!s.startsWith("Execution time:")) {
                            if (isErrorFatal) {
                                logger.error(s);
                            } else {
                                logger.warn(s);
                            }
                        }
                    }
                    process.waitFor();
                    return false;
                }
            } catch (InterruptedException e) {
                log.error("", e);
                return false;
            } finally {
                process.destroy();
                reader.close();
            }
        } catch (IOException e) {
            log.error("", e);
            throw new ServiceException("", e);
        } catch (SAXException e) {
            log.error("", e);
            throw new ServiceException("Ошибка при разборе xml.", e);
        } catch (ParserConfigurationException e) {
            log.error("", e);
            throw new ServiceException("Ошибка при разборе xml.", e);
        } finally {
            //assert xsdFile!= null;
            if (xsdFile != null && !vsax3File.delete()){
                log.warn(String.format("Файл %s не был удален", vsax3File.getName()));
            }
            if (xsdFile != null && !xsdFile.delete()){
                log.warn(String.format("Файл %s не был удален", xsdFile.getName()));
            }
            if (xmlFile != null && !xmlFile.delete()){
                log.warn(String.format("Файл %s не был удален", xmlFile.getName()));
            }
        }
    }
}
