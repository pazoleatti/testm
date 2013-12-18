package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"BlobDataDaoTest.xml"})
@Transactional
public class LogEntryDaoTest {

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

    @Test
    public void testGetEmpty1() {
        Assert.assertNull(logEntryDao.get(""));
        Assert.assertNull(logEntryDao.get(null));
    }

    @Test(expected = DaoException.class)
    public void testGetEmpty2() {
        logEntryDao.get(UUID.randomUUID().toString().toLowerCase());
    }
}
