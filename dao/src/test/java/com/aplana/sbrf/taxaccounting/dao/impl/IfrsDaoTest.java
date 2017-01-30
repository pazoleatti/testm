package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.IfrsDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.IfrsDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Ignore("Налоговые формы не используются!")
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
    public void createTest() {
        List<Integer> reportPeriodIds = new ArrayList<Integer>();
        reportPeriodIds.add(1);

        Assert.assertEquals(dao.findByReportPeriod(reportPeriodIds, null).size(), 0);

        dao.create(1);

        PagingResult<IfrsDataSearchResultItem> result = dao.findByReportPeriod(reportPeriodIds, null);
        Assert.assertEquals(result.size(), 1);
        Assert.assertNull(result.get(0).getBlobDataId());
    }

    @Test
    public void updateTest() {
        BlobData blobData = new BlobData();
        blobData.setName("");
        blobData.setInputStream(new ByteArrayInputStream("test-data-string-2".getBytes()));
        blobData.setCreationDate(new Date());
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());

        Assert.assertEquals(2, dao.findByReportPeriod(null, null).size());

        dao.create(2);
        Assert.assertNull(dao.get(2).getBlobDataId());

        Assert.assertEquals(3, dao.findByReportPeriod(null, null).size());

        String uuid = blobDataDao.create(blobData);
        dao.update(2, uuid);
        Assert.assertEquals(dao.get(2).getBlobDataId(), uuid);
    }

    @Test
    public void testDelete() {
        List<Integer> reportPeriodIds = new ArrayList<Integer>();
        reportPeriodIds.add(20);
        reportPeriodIds.add(21);
        dao.delete(reportPeriodIds);
        Assert.assertEquals(0, dao.findByReportPeriod(null, null).size());
        Assert.assertNull(blobDataDao.get("uuid_1"));
    }
}