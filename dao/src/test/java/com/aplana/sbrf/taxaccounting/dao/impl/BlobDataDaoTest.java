package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("BlobDataDaoTest.xml")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BlobDataDaoTest {

    @Autowired
    BlobDataDao blobDataDao;

    BlobData blobData;

    File file;

    @Before
    public void init() throws FileNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(ClassUtils.classPackageAsResourcePath(BlobDataDaoTest.class));
        assert url != null;
        file = new File(url.getPath() + "/BlobDataDaoTest.xml");

        blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new FileInputStream(file));
        blobData.setCreationDate(new Date());
    }

    @Test
    public void createGetTest() throws IOException {
        Assert.assertEquals(file.length(), blobData.getInputStream().available());
        blobDataDao.create(blobData);
        Assert.assertEquals(blobData.getName(),blobDataDao.get(blobData.getUuid()).getName());
    }

    @Test
    public void saveTest(){
        blobDataDao.create(blobData);
        blobDataDao.save(blobData);
    }

    @Test
    public void deleteTest(){
        blobDataDao.create(blobData);
        blobDataDao.delete(blobData.getUuid());
        Assert.assertNull(blobDataDao.get(blobData.getUuid()));
    }

    @Test
    public void deleteListTest(){
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(blobDataDao.create(blobData));
        blobDataDao.delete(strings);
        Assert.assertNull(blobDataDao.get(strings.get(0)));
    }
}
