package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AuditDaoTest.xml"})
@Transactional(readOnly = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReportDaoImplTest {

    @Autowired
    ReportDao reportDao;
    @Autowired
    BlobDataDao blobDataDao;

    @Test
    @Transactional(readOnly = false)
    public void createAuditTest(){
        BlobData blobData = new BlobData();
        String uuid = UUID.randomUUID().toString();
        blobData.setUuid(uuid);
        blobData.setName("hello");
        blobData.setInputStream(new ByteArrayInputStream(new byte[]{'a'}));
        blobDataDao.create(blobData);
        reportDao.createAudit(1, uuid, ReportType.CSV);
    }

    @Test
    @Transactional(readOnly = false)
    public void deleteDecTest(){
        reportDao.deleteDec(Arrays.asList(1l, 2l));
    }

    @Test
    @Transactional(readOnly = false)
    public void clean(){
        reportDao.clean();
    }

}
