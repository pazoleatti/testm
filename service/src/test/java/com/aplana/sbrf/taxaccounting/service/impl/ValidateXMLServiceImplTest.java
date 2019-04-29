package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationReportType;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("ValidateServiceImplTest.xml")
@Ignore
public class ValidateXMLServiceImplTest implements Runnable {

    private static final String XSD_1 = "NO_NDS.12_1_003_07_05_04_01.xsd";
    private static final String XSD_2 = "1020.xsd";
    private static final String XSD_3 = "NO_NDS.8_1_003_01_05_04_01.xsd";
    private static final String ZIP_XML_1 = "NO_NDS.12_1_1_0212345678020012345_20140331_1.zip";
    private static final String ZIP_XML_2 = "NO_NDS.8_1_1_0212345678020012345_20140331_1.zip";

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ValidateXMLService validateService;

    private String uuidXsd1;

    @Before
    public void init() {
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
        when(reportService.getReportFileUuid(eq(3L), eq(DeclarationReportType.XML_DEC))).thenReturn(uuidXml);

        when(blobDataService.get(uuidXsd1)).thenReturn(blobDataXsd);
        when(blobDataService.get(uuidXml)).thenReturn(blobDataXml);
    }

    @Test
    public void validateTestSampleSuccess() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows"))
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
        when(reportService.getReportFileUuid(eq(3L), eq(DeclarationReportType.XML_DEC))).thenReturn(uuidXml);

        when(blobDataService.get(uuidXsd1)).thenReturn(blobDataXsd);
        when(blobDataService.get(uuidXml)).thenReturn(blobDataXml);

        Logger logger = new Logger();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(5);
        data.setId(3L);
        Assert.assertTrue(validateService.validate(data, logger, null, null, null));
    }

    @Test
    public void validateTestFail() {
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
        when(reportService.getReportFileUuid(eq(5L), eq(DeclarationReportType.XML_DEC))).thenReturn(uuidXml2);

        when(blobDataService.get(uuidXsd2)).thenReturn(blobDataXsd2);
        when(blobDataService.get(uuidXml2)).thenReturn(blobDataXml2);

        Logger logger = new Logger();
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(3);
        data.setId(5L);
        Assert.assertFalse(validateService.validate(data, logger, null, null, null));
    }

    @Test
    public void validateDiffThreads() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }

        for (int i = 1; i <= 3; i++) {
            new Thread(this).run();
        }
    }

    @Test
    public void validateLargeXml() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows"))
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
        when(reportService.getReportFileUuid(eq(5L), eq(DeclarationReportType.XML_DEC))).thenReturn(uuidXml2);

        when(blobDataService.get(uuidXsd2)).thenReturn(blobDataXsd2);
        when(blobDataService.get(uuidXml2)).thenReturn(blobDataXml2);

        Logger logger = new Logger();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(3);
        data.setId(5L);
        // Приводим интерфейс к текущей реализации
        // при маленьком таймауте проверка не должна пройти
        Assert.assertFalse(((ValidateXMLServiceImpl) validateService).validate(data, logger, null, null, null, 1000L));
        Assert.assertEquals(4, logger.getEntries().size());
    }

    @Override
    public void run() {
        Logger logger = new Logger();
        init();

        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(5);
        data.setId(3L);
        Assert.assertTrue(validateService.validate(data, logger, null, null, uuidXsd1));
        Iterables.find(logger.getEntries(), new Predicate<LogEntry>() {
            @Override
            public boolean apply(@Nullable LogEntry input) {
                return input.getMessage().equals("Проверка выполнена по библиотеке ФНС версии 3.2.0.2");
            }
        });
    }

    @Test
    public void fileInfoTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (!System.getProperty("os.name").toLowerCase().contains("windows"))
            return;

        File fileVSAX = File.createTempFile("VSAX3", ".dll");
        try {
            FileOutputStream outputStream = new FileOutputStream(fileVSAX);
            InputStream inputStream = this.getClass().getResourceAsStream("/vsax3/VSAX3.dll");
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
            Assert.assertEquals("Проверка выполнена по библиотеке ФНС версии 3.2.0.2", logger.getEntries().get(0).getMessage());
        } finally {
            fileVSAX.delete();
        }
    }
}
