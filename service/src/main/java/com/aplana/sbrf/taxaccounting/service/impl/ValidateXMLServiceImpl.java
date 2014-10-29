package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
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
                for (int i=0;i<attributes.getLength();i++){
                    if (attributes.getLocalName(i).equals(ATTR_FILE_ID)){
                        fileName = attributes.getValue(i);
                        return;
                    }
                }
            }
        }
    }

    public ValidateXMLServiceImpl() {
        this.url = Thread.currentThread().getContextClassLoader().getResource(TEMPLATE);
    }

    @Override
    public boolean validate(DeclarationData data,  TAUserInfo userInfo) {
        String[] params = new String[4];
        assert url != null;
        params[0] = ResourceUtils.getSharedResource(url.getPath()).getPath();
        DeclarationTemplate template = declarationTemplateService.get(data.getDeclarationTemplateId());

        FileOutputStream outputStream;
        InputStream inputStream;
        BufferedReader reader;
        File xsdFile = null, xmlFile = null;
        try {
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

            Process process = new ProcessBuilder(params).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            try {
                process.waitFor();
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s =reader.readLine()) != null){
                    sb.append(s);
                }
                if (!sb.toString().contains(SUCCESS_FLAG)){
                    log.error(sb.toString());
                }
                return sb.toString().contains(SUCCESS_FLAG);
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
            assert xsdFile!= null;
            if (!xsdFile.delete()){
                log.warn(String.format("Файл %s не был удален", xsdFile.getName()));
            }
            if (!xmlFile.delete()){
                log.warn(String.format("Файл %s не был удален", xmlFile.getName()));
            }
        }
    }
}
