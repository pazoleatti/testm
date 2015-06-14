package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"BlobDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogEntryDaoTest {
    private static final String FILE_NAME = "c:\\test\\test.txt";

    @Autowired
    LogEntryDao logEntryDao;

    @Test
    public void testSave() {
        Logger logger = new Logger();
        logger.error("E1");
        logger.error("E2");
        logger.warn("W1");
        logger.warn("W2");
        String uuid = UUID.randomUUID().toString().toLowerCase();

        logEntryDao.save(logger.getEntries(), uuid);
    }

    @Test
    public void testGet() {
        Logger logger = new Logger();
        logger.error("E1");
        logger.error("E2");
        logger.warn("W1");
        logger.warn("W2");
        String uuid = UUID.randomUUID().toString().toLowerCase();
        logEntryDao.save(logger.getEntries(), uuid);
        List<LogEntry> lel = logEntryDao.get(uuid);

        Assert.assertEquals(lel.size(), 4);
        Assert.assertEquals(lel.get(0).getMessage(), "E1");
        Assert.assertEquals(lel.get(3).getLevel(), LogLevel.WARNING);
    }

    @Test(expected = RuntimeException.class)
    public void testGetNull(){
        logEntryDao.get(null);
    }

    @Test
    public void testGetEmpty1() {
        Assert.assertNull(logEntryDao.get(""));
    }

    @Test(expected = DaoException.class)
    public void testGetEmpty2() {
        logEntryDao.get(UUID.randomUUID().toString().toLowerCase());
    }

    @Test
    public void testUpdate() {
        Logger logger = new Logger();
        logger.error("E1");
        logger.error("E2");
        logger.warn("W1");
        logger.warn("W2");
        String uuid = UUID.randomUUID().toString().toLowerCase();
        logEntryDao.save(logger.getEntries(), uuid);

        List<LogEntry> logEntries = logEntryDao.get(uuid);

        Logger newLogger = new Logger();
        newLogger.error("E3");
        newLogger.warn("W3");
        logEntries.addAll(newLogger.getEntries());

        logEntryDao.update(logEntries, uuid);
        List<LogEntry> list = logEntryDao.get(uuid);

        Assert.assertEquals(list.size(), 6);
        Assert.assertEquals(list.get(4).getMessage(), "E3");
        Assert.assertEquals(list.get(5).getLevel(), LogLevel.WARNING);
    }

    //@Test
    public void hugeLogsTest() {
        int size = 1000000;
        long start = System.nanoTime();
        generateLogs(size);
        System.out.println("Generating file..."+(double) (System.nanoTime() - start) / 1000000000.0);
        start = System.nanoTime();
        List<LogEntry> entries = loadLogs();
        System.out.println("Reading file..."+(double) (System.nanoTime() - start) / 1000000000.0);
        start = System.nanoTime();
        String uuid = UUID.randomUUID().toString().toLowerCase();
        logEntryDao.save(entries, uuid);
        System.out.println("Saving logs..."+(double) (System.nanoTime() - start) / 1000000000.0);
        start = System.nanoTime();
        List<LogEntry> logs = logEntryDao.get(uuid);
        System.out.println("Getting logs..."+(double) (System.nanoTime() - start) / 1000000000.0);
        Assert.assertEquals(logs.size(), size + 1);
        LogEntry testEntry = logs.get(0);
        Assert.assertEquals(testEntry.getMessage(), "test");
        Assert.assertEquals(testEntry.getLevel(), LogLevel.WARNING);
    }

    private static List<LogEntry> loadLogs() {
        BufferedReader br = null;
        try {
            List<LogEntry> entries = new ArrayList<LogEntry>();
            entries.add(new LogEntry(LogLevel.WARNING, "test"));
            br = new BufferedReader(new FileReader(FILE_NAME));
            String text;
            while ((text = br.readLine()) != null) {
                entries.add(new LogEntry(LogLevel.INFO, text));
            }
            return entries;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {}
        }
        return null;
    }

    private static void generateLogs(int count) {
        try {
            FileWriter writer = new FileWriter(new File(FILE_NAME));
            Random random = new Random();
            for (int i=0; i<count; i++) {
                writer.write(random.nextInt(10) + String.valueOf(new Date().getTime()) + "\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
