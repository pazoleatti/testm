package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogDao;
import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.apache.commons.lang3.StringUtils;
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
@ContextConfiguration({"LogEntryDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogEntryDaoTest {
    private static final int MAX_MESSAGE_SIZE = 2000;

    private static final String FILE_NAME = "c:\\test\\test.txt";
    private static final String UID_WITHOUT_LOG_ENTRY_1 = "1-1-1";
    private static final String UID_WITHOUT_LOG_ENTRY_2 = "2-2-2";
    private static final String UID_WITHOUT_LOG_ENTRY_3 = "3-3-3";
    private static final String UID_WITH_LOG_ENTRY_4 = "4-4-4";
    private static final String UID_WITH_LOG_ENTRY_5 = "5-5-5";
    private static final String UID_WITH_LOG_ENTRY_6 = "6-6-6";

    @Autowired
    LogEntryDao logEntryDao;

    @Autowired
    LogDao logDao;

    @Test
    public void testLogSave() {
        logDao.save(UUID.randomUUID().toString().toLowerCase());
    }

    @Test
    public void testSave() {
        Logger logger = new Logger();
        logger.error("E1");
        logger.error("E2");
        logger.warn("W1");
        logger.warn("W2");

        logEntryDao.save(logger.getEntries(), UID_WITHOUT_LOG_ENTRY_1);
    }

    @Test
    public void testGet() {
        Logger logger = new Logger();
        logger.error("E1");
        logger.error("E2");
        logger.warn("W1");
        logger.warn("W2");
        logEntryDao.save(logger.getEntries(), UID_WITHOUT_LOG_ENTRY_2);
        List<LogEntry> lel = logEntryDao.get(UID_WITHOUT_LOG_ENTRY_2);

        Assert.assertEquals(lel.size(), 4);
        Assert.assertEquals(lel.get(0).getMessage(), "E1");
        Assert.assertEquals(lel.get(3).getLevel(), LogLevel.WARNING);
    }

    @Test
    public void testGetEmptyPage() {
        List<LogEntry> emptyPage = logEntryDao.get(UID_WITHOUT_LOG_ENTRY_3, 1, 10);
        Assert.assertTrue(emptyPage.isEmpty());
    }

    @Test
    public void testGetPage() {
        List<LogEntry> page = logEntryDao.get(UID_WITH_LOG_ENTRY_4, 1, 2);

        Assert.assertEquals(page.size(), 2);
        Assert.assertEquals(page.get(0).getOrd(), 2);
        Assert.assertEquals(page.get(1).getOrd(), 3);
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
    public void testMinOrder() {
        Assert.assertEquals(1, logEntryDao.minOrder(UID_WITH_LOG_ENTRY_4).intValue());
        Assert.assertNull(logEntryDao.minOrder(UID_WITHOUT_LOG_ENTRY_3));
    }

    @Test
    public void testMaxOrder() {
        Assert.assertEquals(3, logEntryDao.maxOrder(UID_WITH_LOG_ENTRY_4).intValue());
        Assert.assertNull(logEntryDao.maxOrder(UID_WITHOUT_LOG_ENTRY_3));
    }

    @Test
    public void testUpdate() {
        List<LogEntry> before = Arrays.asList(new LogEntry(LogLevel.INFO, "-3"), new LogEntry(LogLevel.INFO, "-2"), new LogEntry(LogLevel.INFO, "-1"));
        List<LogEntry> after = Arrays.asList(new LogEntry(LogLevel.INFO, "1"), new LogEntry(LogLevel.INFO, "2"), new LogEntry(LogLevel.INFO, "3"));

        logEntryDao.update(before, UID_WITH_LOG_ENTRY_5, true);
        logEntryDao.update(after, UID_WITH_LOG_ENTRY_5, false);

        List<LogEntry> logEntries = logEntryDao.get(UID_WITH_LOG_ENTRY_5);

        Assert.assertEquals(before.get(0).getMessage(), logEntries.get(0).getMessage());
        Assert.assertEquals(before.get(1).getMessage(), logEntries.get(1).getMessage());
        Assert.assertEquals(before.get(2).getMessage(), logEntries.get(2).getMessage());

        Assert.assertEquals(after.get(0).getMessage(), logEntries.get(7).getMessage());
        Assert.assertEquals(after.get(1).getMessage(), logEntries.get(8).getMessage());
        Assert.assertEquals(after.get(2).getMessage(), logEntries.get(9).getMessage());
    }

    @Test
    public void testBigMessage() {
        String largeMessage = "";

        largeMessage += StringUtils.repeat('\n', MAX_MESSAGE_SIZE);
        largeMessage += StringUtils.repeat('б', MAX_MESSAGE_SIZE);
        largeMessage += StringUtils.repeat('z', MAX_MESSAGE_SIZE);
        largeMessage += StringUtils.repeat('e', 10);

        Logger logger = new Logger();
        logger.error(largeMessage);

        String uuid = UUID.randomUUID().toString().toLowerCase();
        logDao.save(uuid);
        logEntryDao.save(logger.getEntries(), uuid);

        List<LogEntry> logEntries = logEntryDao.get(uuid);
        Assert.assertEquals(4, logEntries.size());
        Assert.assertEquals(StringUtils.repeat('\n', MAX_MESSAGE_SIZE), logEntries.get(0).getMessage());
        Assert.assertEquals(StringUtils.repeat('б', MAX_MESSAGE_SIZE), logEntries.get(1).getMessage());
        Assert.assertEquals(StringUtils.repeat('z', MAX_MESSAGE_SIZE), logEntries.get(2).getMessage());
        Assert.assertEquals(StringUtils.repeat('e', 10), logEntries.get(3).getMessage());
    }

    @Test
    public void testCountLogLevel() {
        Map<LogLevel, Integer> countLogLevelMap = logEntryDao.countLogLevel(UID_WITH_LOG_ENTRY_6);
        Assert.assertEquals(1, countLogLevelMap.get(LogLevel.INFO).intValue());
        Assert.assertEquals(2, countLogLevelMap.get(LogLevel.WARNING).intValue());
        Assert.assertEquals(3, countLogLevelMap.get(LogLevel.ERROR).intValue());
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
