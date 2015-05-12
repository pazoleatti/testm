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

import java.io.*;
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

    File file;

    @Before
    public void init() throws FileNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(ClassUtils.classPackageAsResourcePath(BlobDataDaoTest.class));
        assert url != null;
        file = new File(url.getPath() + "/BlobDataDaoTest.xml");
    }

    @Test
    public void createGetTest() throws IOException {
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new FileInputStream(file));
        Assert.assertEquals(file.length(), blobData.getInputStream().available());
        blobDataDao.create(blobData);
        Assert.assertEquals(blobData.getName(), blobDataDao.get(blobData.getUuid()).getName());
    }

    @Test
    public void saveTest() throws FileNotFoundException {
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new FileInputStream(file));
        blobDataDao.create(blobData);
        blobDataDao.save(blobData.getUuid(), new FileInputStream(file));
    }

    @Test
    public void deleteTest() throws FileNotFoundException {
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new FileInputStream(file));
        blobDataDao.create(blobData);
        blobDataDao.delete(blobData.getUuid());
        Assert.assertNull(blobDataDao.get(blobData.getUuid()));
    }

    @Test
    public void deleteListTest() throws FileNotFoundException {
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new FileInputStream(file));
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(blobDataDao.create(blobData));
        blobDataDao.delete(strings);
        Assert.assertNull(blobDataDao.get(strings.get(0)));
    }

    @Test
    public void clean() {
        String uuid1 = "uuid_1";
        String uuid2 = "uuid_2";
        String uuid3 = "uuid_3";
        String uuid4 = "uuid_4";
        String uuid5 = "uuid_5";
        String uuid6 = "uuid_6";

        BlobData bd1 = blobDataDao.get(uuid1);
        BlobData bd2 = blobDataDao.get(uuid2);
        BlobData bd3 = blobDataDao.get(uuid3);
        BlobData bd4 = blobDataDao.get(uuid4);
        BlobData bd5 = blobDataDao.get(uuid5);
        BlobData bd6 = blobDataDao.get(uuid6);

        Assert.assertNotNull(bd1);
        Assert.assertNotNull(bd2);
        Assert.assertNotNull(bd3);
        Assert.assertNotNull(bd4);
        Assert.assertNotNull(bd5);
        Assert.assertNotNull(bd6);

        int deleted = blobDataDao.clean();

        Assert.assertEquals(2, deleted);

        bd1 = blobDataDao.get(uuid1);
        bd2 = blobDataDao.get(uuid2);
        bd3 = blobDataDao.get(uuid3);
        bd4 = blobDataDao.get(uuid4);
        bd5 = blobDataDao.get(uuid5);
        bd6 = blobDataDao.get(uuid6);

        Assert.assertNotNull(bd1);
        Assert.assertNotNull(bd2);
        Assert.assertNull(bd3);
        Assert.assertNull(bd4);
        Assert.assertNotNull(bd5);
        Assert.assertNotNull(bd6);
    }

    @Test
    public void createWithCustomDateTest() throws IOException {
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new FileInputStream(file));
        blobData.setCreationDate(new Date());

        blobDataDao.createWithDate(blobData);
        File file1 = new File(System.getProperty("java.io.tmpdir")+ File.separator +"1.xml");
        FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write('d');
        fileWriter.close();
    }
}
