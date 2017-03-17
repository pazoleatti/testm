package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("ValidateServiceImplTest.xml")
public class ValidateXMLServiceImplTest implements Runnable {

    private static final String XSD_1 = "NO_NDS.12_1_003_07_05_04_01.xsd";
    private static final String XSD_2 = "1020.xsd";
    private static final String XSD_3 = "NO_NDS.8_1_003_01_05_04_01.xsd";
    private static final String ZIP_XML_1 = "NO_NDS.12_1_1_0212345678020012345_20140331_1.zip";
    private static final String ZIP_XML_2 = "NO_NDS.8_1_1_0212345678020012345_20140331_1.zip";

    private static final String TEMPLATE = ClassUtils
            .classPackageAsResourcePath(ValidateXMLServiceImpl.class) + "/VSAX3.exe";

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ValidateXMLServiceImpl validateService;
    @Autowired
    private LockDataService lockDataService;

    private String uuidXsd1;
    @Before
    public void init() throws IOException {
        uuidXsd1 = UUID.randomUUID().toString();
        DeclarationTemplate declarationTemplate1 = new DeclarationTemplate();
        declarationTemplate1.setXsdId(uuidXsd1);
        when(declarationTemplateService.get(5)).thenReturn(declarationTemplate1);
        BlobData blobDataXsd = new BlobData();
        InputStream inputStreamXsd = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + XSD_1);
        blobDataXsd.setInputStream(inputStreamXsd);

        BlobData blobDataXml = new BlobData();
        InputStream inputStreamXml = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + ZIP_XML_1);
        blobDataXml.setInputStream(inputStreamXml);
        blobDataXml.setName(ZIP_XML_1);
        String uuidXml = UUID.randomUUID().toString();
        when(reportService.getDec(any(TAUserInfo.class), eq(3l), eq(DeclarationDataReportType.XML_DEC))).thenReturn(uuidXml);

        when(blobDataService.get(uuidXsd1)).thenReturn(blobDataXsd);
        when(blobDataService.get(uuidXml)).thenReturn(blobDataXml);
    }

    @Test
    public void validateTestSampleSuccess(){
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;

        String uuidXsd1 = UUID.randomUUID().toString();
        DeclarationTemplate declarationTemplate1 = new DeclarationTemplate();
        declarationTemplate1.setXsdId(uuidXsd1);
        when(declarationTemplateService.get(5)).thenReturn(declarationTemplate1);
        BlobData blobDataXsd = new BlobData();
        InputStream inputStreamXsd = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + XSD_1);
        blobDataXsd.setInputStream(inputStreamXsd);

        BlobData blobDataXml = new BlobData();
        InputStream inputStreamXml = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + ZIP_XML_1);
        blobDataXml.setInputStream(inputStreamXml);
        blobDataXml.setName(ZIP_XML_1);
        String uuidXml = UUID.randomUUID().toString();
        when(reportService.getDec(any(TAUserInfo.class), eq(3l), eq(DeclarationDataReportType.XML_DEC))).thenReturn(uuidXml);

        when(blobDataService.get(uuidXsd1)).thenReturn(blobDataXsd);
        when(blobDataService.get(uuidXml)).thenReturn(blobDataXml);

        Logger logger = new Logger();
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(5);
        data.setId(3l);
        Assert.assertTrue(validateService.validate(data, userInfo, logger, true, null, null));
    }

    @Test
    public void validateTestFail(){
        String uuidXsd2 = UUID.randomUUID().toString();
        DeclarationTemplate declarationTemplate2 = new DeclarationTemplate();
        declarationTemplate2.setXsdId(uuidXsd2);
        when(declarationTemplateService.get(3)).thenReturn(declarationTemplate2);
        BlobData blobDataXsd2 = new BlobData();
        InputStream inputStreamXsd2 = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + XSD_2);
        blobDataXsd2.setInputStream(inputStreamXsd2);

        BlobData blobDataXml2 = new BlobData();
        String uuidXml2 = UUID.randomUUID().toString();
        InputStream inputStreamXml2 = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + ZIP_XML_1);
        blobDataXml2.setInputStream(inputStreamXml2);
        blobDataXml2.setName(ZIP_XML_1);
        when(reportService.getDec(any(TAUserInfo.class), eq(5l), eq(DeclarationDataReportType.XML_DEC))).thenReturn(uuidXml2);

        when(blobDataService.get(uuidXsd2)).thenReturn(blobDataXsd2);
        when(blobDataService.get(uuidXml2)).thenReturn(blobDataXml2);

        Logger logger = new Logger();
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(3);
        data.setId(5l);
        Assert.assertFalse(validateService.validate(data, userInfo, logger, true, null, null));
    }

    @Test
    public void validateDiffThreads(){
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;

        for (int i=1; i<=3; i++){
            new Thread(this).run();
        }
    }

    @Test
    public void validateLargeXml() throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;
        String uuidXsd2 = UUID.randomUUID().toString();
        DeclarationTemplate declarationTemplate2 = new DeclarationTemplate();
        declarationTemplate2.setXsdId(uuidXsd2);
        when(declarationTemplateService.get(3)).thenReturn(declarationTemplate2);
        BlobData blobDataXsd2 = new BlobData();
        InputStream inputStreamXsd2 = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + XSD_3);
        blobDataXsd2.setInputStream(inputStreamXsd2);
        blobDataXsd2.setName(XSD_3);

        BlobData blobDataXml2 = new BlobData();
        String uuidXml2 = UUID.randomUUID().toString();
        InputStream inputStreamXml2 = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) +
                        File.separator + "validate" + File.separator + ZIP_XML_2);
        blobDataXml2.setInputStream(inputStreamXml2);
        blobDataXml2.setName(ZIP_XML_2);
        when(reportService.getDec(any(TAUserInfo.class), eq(5l), eq(DeclarationDataReportType.XML_DEC))).thenReturn(uuidXml2);

        when(blobDataService.get(uuidXsd2)).thenReturn(blobDataXsd2);
        when(blobDataService.get(uuidXml2)).thenReturn(blobDataXml2);

        Logger logger = new Logger();
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(3);
        data.setId(5l);
		// при маленьком таймауте проверка не должна пройти
		Assert.assertFalse(validateService.validate(data, userInfo, logger, true, null, null, 1000L));
        Assert.assertEquals(2, logger.getEntries().size());
    }

    @Override
    public void run() {
        Logger logger = new Logger();
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(5);
        data.setId(3l);
        Assert.assertTrue(validateService.validate(data, userInfo, logger, true, null, uuidXsd1));
    }

    @Test
    public void fileInfoTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;

        File fileVSAX = File.createTempFile("VSAX3",".exe");
        try {
            FileOutputStream outputStream = new FileOutputStream(fileVSAX);
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
			try {
            	IOUtils.copy(inputStream, outputStream);
			} finally {
				IOUtils.closeQuietly(inputStream);
				IOUtils.closeQuietly(outputStream);
			}

            Method method = validateService.getClass().getDeclaredMethod("fileInfo", Logger.class, File.class);
            method.setAccessible(true);
            Logger logger = new Logger();
            method.invoke(validateService, logger, fileVSAX);

            Assert.assertTrue(!logger.containsLevel(LogLevel.ERROR));
            Assert.assertTrue(logger.containsLevel(LogLevel.INFO));
            System.out.println(logger.getEntries().get(0).getMessage());
        }finally {
            fileVSAX.delete();
        }

    }
}
