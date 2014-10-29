package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.IfrsDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"IfrsDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IfrsDaoTest {


    @Autowired
    private IfrsDao dao;

    @Autowired
    private BlobDataDao blobDataDao;

    @Test
    public void updateTest() {
        BlobData blobData = new BlobData();
        blobData.setName("");
        blobData.setInputStream(new ByteArrayInputStream("test-data-string-2".getBytes()));
        blobData.setCreationDate(new Date());
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());

        Assert.assertEquals(dao.findByReportPeriod(null, null).size(), 0);

        dao.create(2);
        Assert.assertNull(dao.get(2).getBlobDataId());

        Assert.assertEquals(dao.findByReportPeriod(null, null).size(), 1);

        String uuid = blobDataDao.create(blobData);
        dao.update(2, uuid);
        Assert.assertEquals(dao.get(2).getBlobDataId(), uuid);
    }

}