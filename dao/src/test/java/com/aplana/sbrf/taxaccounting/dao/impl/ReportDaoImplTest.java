package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.ReportType;
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
    public void get() {
        String id = reportDao.get(1, "Excel", false, false, false);
        assertEquals("uuid_5", id);
    }

    @Test
    public void getDec() {
        DeclarationDataReportType type = new DeclarationDataReportType(ReportType.PDF_DEC, null);
        String id = reportDao.getDec(3, type);
        assertEquals("uuid_6", id);
    }

    @Test
    public void getAudit() {
        String id = reportDao.getAudit(1, ReportType.EXCEL);
        assertEquals("uuid_6", id);
    }

    @Test
    @Transactional(readOnly = false)
    public void delete(){
        String id = reportDao.get(1, "Excel", false, false, false);
        assertEquals("uuid_5", id);
        reportDao.delete(1, false);
        id = reportDao.get(1, "Excel", false, false, false);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest1(){
        DeclarationDataReportType type = new DeclarationDataReportType(ReportType.PDF_DEC, null);

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
        DeclarationDataReportType type = new DeclarationDataReportType(ReportType.PDF_DEC, null);

        String id = reportDao.getDec(3, type);
        assertEquals("uuid_6", id);

        reportDao.deleteDec(3L);

        id = reportDao.getDec(3, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest3(){
        DeclarationDataReportType type = new DeclarationDataReportType(ReportType.EXCEL_DEC, null);

        reportDao.deleteDec(Arrays.asList(1l, 2l), Arrays.asList(DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.XML_DEC));

        String id = reportDao.getDec(1, type);
        assertEquals(null, id);
        id = reportDao.getDec(2, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest4(){
        DeclarationDataReportType type = new DeclarationDataReportType(ReportType.EXCEL_DEC, null);
        String id = reportDao.getDec(1, type);
        assertEquals("uuid_1", id);

        reportDao.deleteDec("uuid_1");

        id = reportDao.getDec(1, type);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteAuditTest1(){
        String id = reportDao.getAudit(1, ReportType.EXCEL);
        assertEquals("uuid_6", id);

        reportDao.deleteAudit(1, ReportType.EXCEL);

        id = reportDao.getAudit(1, ReportType.EXCEL);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteAuditTest2(){
        String id = reportDao.getAudit(1, ReportType.EXCEL);
        assertEquals("uuid_6", id);

        reportDao.deleteAudit("uuid_6");

        id = reportDao.getAudit(1, ReportType.EXCEL);
        assertEquals(null, id);
    }

    @Test
    @Transactional(readOnly = false)
    public void create() {
        String id = reportDao.get(11, "Excel", true, true, true);
        assertEquals(null, id);

        reportDao.create(11, "uuid_2", "Excel", true, true, true);
        id = reportDao.get(11, "Excel", true, true, true);
        assertEquals("uuid_2", id);
    }

    @Test
    @Transactional(readOnly = false)
    public void createDecTest(){
        BlobData blobData = new BlobData();
        String uuid = UUID.randomUUID().toString();
        blobData.setUuid(uuid);
        blobData.setName("hello");
        blobData.setInputStream(new ByteArrayInputStream(new byte[]{'a'}));
        blobDataDao.create(blobData);
        reportDao.createDec(3, uuid, DeclarationDataReportType.EXCEL_DEC);
    }

    @Test
    @Transactional(readOnly = false)
    public void createAuditTest(){
        BlobData blobData = new BlobData();
        String uuid = UUID.randomUUID().toString();
        blobData.setUuid(uuid);
        blobData.setName("hello");
        blobData.setInputStream(new ByteArrayInputStream(new byte[]{'a'}));
        blobDataDao.create(blobData);
        reportDao.createAudit(2, uuid, ReportType.CSV);
    }

    @Test
    @Transactional(readOnly = false)
    public void clean(){
        assertEquals(reportDao.clean(), 1);
    }

}
