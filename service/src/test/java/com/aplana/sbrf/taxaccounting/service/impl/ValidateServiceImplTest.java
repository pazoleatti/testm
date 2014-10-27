package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.ValidateService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("ValidateServiceImplTest.xml")
public class ValidateServiceImplTest implements Runnable {

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ValidateService validateService;

    @Before
    public void init() throws IOException {
        //Success
        String uuidXsd1 = UUID.randomUUID().toString();
        DeclarationTemplate declarationTemplate1 = new DeclarationTemplate();
        declarationTemplate1.setXsdId(uuidXsd1);
        when(declarationTemplateService.get(5)).thenReturn(declarationTemplate1);
        BlobData blobDataXsd = new BlobData();
        InputStream inputStreamXsd = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateServiceImpl.class) +
                        File.separator + "validate" + File.separator + "NO_NDS.12_1_003_07_05_04_01.xsd");
        blobDataXsd.setInputStream(inputStreamXsd);

        BlobData blobDataXml = new BlobData();
        String uuidXml = UUID.randomUUID().toString();
        InputStream inputStreamXml = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateServiceImpl.class) +
                        File.separator + "validate" + File.separator + "NO_NDS.12_1_1_0212345678020012345_20140331_1.xml");
        blobDataXml.setInputStream(inputStreamXml);
        when(reportService.getDec(any(TAUserInfo.class), eq(3l), eq(ReportType.XML_DEC))).thenReturn(uuidXml);

        when(blobDataService.get(uuidXsd1)).thenReturn(blobDataXsd);
        when(blobDataService.get(uuidXml)).thenReturn(blobDataXml);

        //Fail
        String uuidXsd2 = UUID.randomUUID().toString();
        DeclarationTemplate declarationTemplate2 = new DeclarationTemplate();
        declarationTemplate2.setXsdId(uuidXsd2);
        when(declarationTemplateService.get(3)).thenReturn(declarationTemplate2);
        BlobData blobDataXsd2 = new BlobData();
        InputStream inputStreamXsd2 = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateServiceImpl.class) +
                        File.separator + "validate" + File.separator + "1020.xsd");
        blobDataXsd2.setInputStream(inputStreamXsd2);

        BlobData blobDataXml2 = new BlobData();
        String uuidXml2 = UUID.randomUUID().toString();
        InputStream inputStreamXml2 = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(ClassUtils.classPackageAsResourcePath(ValidateServiceImpl.class) +
                        File.separator + "validate" + File.separator + "report.jrxml");
        blobDataXml2.setInputStream(inputStreamXml2);
        when(reportService.getDec(any(TAUserInfo.class), eq(5l), eq(ReportType.XML_DEC))).thenReturn(uuidXml2);

        when(blobDataService.get(uuidXsd2)).thenReturn(blobDataXsd2);
        when(blobDataService.get(uuidXml2)).thenReturn(blobDataXml2);
    }

    //@Test
    public void validateTestSample(){
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(5);
        data.setId(3l);
        Assert.assertTrue(validateService.validate(data, userInfo));
    }

    @Test
    public void validateTest(){
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(3);
        data.setId(5l);
        Assert.assertTrue(validateService.validate(data, userInfo));
    }

    @Test
    public void validateDiffThreads(){
        for (int i=1; i<=3; i++){
            new Thread(this).run();
        }
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TAUserInfo userInfo = new TAUserInfo();
        DeclarationData data = new DeclarationData();
        data.setDeclarationTemplateId(3);
        data.setId(5l);
        Assert.assertTrue(validateService.validate(data, userInfo));
    }
}
