package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Модульные тесты для реализации {@link BlobDataDao}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("BlobDataDaoTest.xml")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BlobDataDaoTest {

    @Autowired
    private BlobDataDao blobDataDao;

    @Test
    public void createWithSysdateFetchTest() throws IOException {
        byte[] bytes = {0, 1, 2};
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new ByteArrayInputStream(bytes));
        blobDataDao.createWithSysdate(blobData);

        BlobData fetchedBlobData = blobDataDao.fetch(blobData.getUuid());
        assertEquals(blobData.getUuid(), fetchedBlobData.getUuid());
        assertEquals(blobData.getName(), fetchedBlobData.getName());
        byte[] fetchedBytes = ByteStreams.toByteArray(fetchedBlobData.getInputStream());
        assertEquals(bytes.length, fetchedBytes.length);
    }

    @Test
    public void createFetchTest() throws IOException {
        byte[] bytes = {0, 1, 2};
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new ByteArrayInputStream(bytes));
        blobData.setCreationDate(new Date());

        blobDataDao.create(blobData);

        BlobData fetchedBlobData = blobDataDao.fetch(blobData.getUuid());
        assertEquals(blobData.getUuid(), fetchedBlobData.getUuid());
        assertEquals(blobData.getName(), fetchedBlobData.getName());
        assertArrayEquals(bytes, ByteStreams.toByteArray(fetchedBlobData.getInputStream()));

        Calendar creationCalendar = Calendar.getInstance();
        creationCalendar.setTime(blobData.getCreationDate());
        creationCalendar.clear(Calendar.MILLISECOND);
        creationCalendar.clear(Calendar.SECOND);
        creationCalendar.clear(Calendar.MINUTE);
        creationCalendar.clear(Calendar.HOUR);
        Calendar fetchedCalendar = Calendar.getInstance();
        fetchedCalendar.setTime(fetchedBlobData.getCreationDate());
        fetchedCalendar.clear(Calendar.MILLISECOND);
        fetchedCalendar.clear(Calendar.SECOND);
        fetchedCalendar.clear(Calendar.MINUTE);
        fetchedCalendar.clear(Calendar.HOUR);

        assertTrue(creationCalendar.equals(fetchedCalendar));
    }

    @Test
    public void updateDataByUUIDTest() throws IOException {
        byte[] bytes3 = {0, 1, 2};
        byte[] bytes5 = {0, 1, 2, 3, 4};
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new ByteArrayInputStream(bytes3));
        blobDataDao.createWithSysdate(blobData);
        blobDataDao.updateDataByUUID(blobData.getUuid(), new ByteArrayInputStream(bytes5));
        BlobData fetchedBlobData = blobDataDao.fetch(blobData.getUuid());
        byte[] fetchedBytes = ByteStreams.toByteArray(fetchedBlobData.getInputStream());
        assertArrayEquals(bytes5, fetchedBytes);
    }

    @Test
    public void deleteTest() throws FileNotFoundException {
        byte[] bytes = {0, 1, 2};
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new ByteArrayInputStream(bytes));
        blobDataDao.createWithSysdate(blobData);
        assertNotNull(blobDataDao.fetch(blobData.getUuid()));
        blobDataDao.delete(blobData.getUuid());
        assertNull(blobDataDao.fetch(blobData.getUuid()));
    }

    @Test
    public void deleteListTest() throws FileNotFoundException {
        byte[] bytes = {0, 1, 2};
        BlobData blobData = new BlobData();
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setName("hello.xls");
        blobData.setInputStream(new ByteArrayInputStream(bytes));
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(blobDataDao.createWithSysdate(blobData));
        blobDataDao.delete(strings);
        assertNull(blobDataDao.fetch(strings.get(0)));
    }

    @Test
    public void cleanTest() {
        String uuid1 = "uuid_1";
        String uuid2 = "uuid_2";
        String uuid3 = "uuid_3";
        String uuid4 = "uuid_4";
        String uuid5 = "uuid_5";
        String uuid6 = "uuid_6";

        BlobData bd1 = blobDataDao.fetch(uuid1);
        BlobData bd2 = blobDataDao.fetch(uuid2);
        BlobData bd3 = blobDataDao.fetch(uuid3);
        BlobData bd4 = blobDataDao.fetch(uuid4);
        BlobData bd5 = blobDataDao.fetch(uuid5);
        BlobData bd6 = blobDataDao.fetch(uuid6);

        assertNotNull(bd1);
        assertNotNull(bd2);
        assertNotNull(bd3);
        assertNotNull(bd4);
        assertNotNull(bd5);
        assertNotNull(bd6);

        long deleted = blobDataDao.clean();

        assertEquals(3, deleted);

        bd1 = blobDataDao.fetch(uuid1);
        bd2 = blobDataDao.fetch(uuid2);
        bd3 = blobDataDao.fetch(uuid3);
        bd4 = blobDataDao.fetch(uuid4);
        bd5 = blobDataDao.fetch(uuid5);
        bd6 = blobDataDao.fetch(uuid6);

        assertNotNull(bd1);
        assertNotNull(bd2);
        assertNull(bd3);
        assertNull(bd4);
        assertNull(bd5);
        assertNotNull(bd6);
    }
}