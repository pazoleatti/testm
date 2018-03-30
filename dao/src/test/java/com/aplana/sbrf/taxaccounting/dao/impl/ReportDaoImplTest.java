package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.UUID;

import static com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportDaoTest.xml"})
@Transactional(readOnly = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReportDaoImplTest {

    @Autowired
    ReportDao reportDao;
    @Autowired
    BlobDataDao blobDataDao;

    @Test
    public void getDec() {
        DeclarationDataReportType type = new DeclarationDataReportType(AsyncTaskType.PDF_DEC, null);
        String id = reportDao.getDec(3, type);
        assertEquals("uuid_6", id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest1(){
        DeclarationDataReportType type = new DeclarationDataReportType(AsyncTaskType.PDF_DEC, null);

        String id = reportDao.getDec(1, type);
        assertEquals("uuid_3", id);
        id = reportDao.getDec(2, type);
        assertEquals("uuid_6", id);

        reportDao.deleteDec(Arrays.asList(1l, 2l));

        id = reportDao.getDec(1, type);
        assertEquals(null, id);
        id = reportDao.getDec(2, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest2(){
        DeclarationDataReportType type = new DeclarationDataReportType(AsyncTaskType.PDF_DEC, null);

        String id = reportDao.getDec(3, type);
        assertEquals("uuid_6", id);

        reportDao.deleteDec(3L);

        id = reportDao.getDec(3, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecExceptTest(){
        assertEquals("uuid_1", reportDao.getDec(1, EXCEL_DEC));
        assertEquals("uuid_2", reportDao.getDec(1, XML_DEC));
        assertEquals("uuid_3", reportDao.getDec(1, PDF_DEC));
        assertEquals("uuid_4", reportDao.getDec(1, JASPER_DEC));

        reportDao.deleteNotXmlDec(1);

        assertEquals(null, reportDao.getDec(1, EXCEL_DEC));
        assertEquals("uuid_2", reportDao.getDec(1, XML_DEC));
        assertEquals(null, reportDao.getDec(1, PDF_DEC));
        assertEquals(null, reportDao.getDec(1, JASPER_DEC));
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest3(){
        DeclarationDataReportType type = new DeclarationDataReportType(AsyncTaskType.EXCEL_DEC, null);

        reportDao.deleteDec(Arrays.asList(1l, 2l), Arrays.asList(EXCEL_DEC, XML_DEC));

        String id = reportDao.getDec(1, type);
        assertEquals(null, id);
        id = reportDao.getDec(2, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest4(){
        DeclarationDataReportType type = new DeclarationDataReportType(AsyncTaskType.EXCEL_DEC, null);

        reportDao.deleteDec(1l, EXCEL_DEC);

        String id = reportDao.getDec(1, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest5(){
        DeclarationDataReportType type = new DeclarationDataReportType(AsyncTaskType.EXCEL_DEC, null);
        String id = reportDao.getDec(1, type);
        assertEquals("uuid_1", id);

        reportDao.deleteDec("uuid_1");

        id = reportDao.getDec(1, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void createDecTest(){
        BlobData blobData = new BlobData();
        String uuid = UUID.randomUUID().toString();
        blobData.setUuid(uuid);
        blobData.setName("hello");
        blobData.setInputStream(new ByteArrayInputStream(new byte[]{'a'}));
        blobDataDao.createWithSysdate(blobData);
        reportDao.createDec(3, uuid, EXCEL_DEC);
    }

    @Test
    @Transactional(readOnly = false)
    public void clean(){
        assertEquals(reportDao.clean(), 1);
    }

}
