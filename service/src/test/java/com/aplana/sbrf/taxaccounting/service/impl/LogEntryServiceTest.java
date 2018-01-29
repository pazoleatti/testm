package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogEntryServiceTest {

    private static final String UUID = "uuid";
    private static final int SIZE = 100;
    private static LogEntryService logEntryService = new LogEntryServiceImpl();


    @BeforeClass
    public static void init() {
        LogEntryDao logEntryDao = mock(LogEntryDao.class);
        List<LogEntry> logEntries = new ArrayList<LogEntry>();
        for (int i = 0; i < SIZE; i++) {
            logEntries.add(new LogEntry(i % 3 == 0 ? LogLevel.INFO : (i % 2 == 0 ? LogLevel.WARNING : LogLevel.ERROR), "item " + i));
        }
        when(logEntryDao.fetch(UUID)).thenReturn(logEntries);
        ReflectionTestUtils.setField(logEntryService, "logEntryDao", logEntryDao);
    }

    @Test
    public void getAllTest() {
        Assert.assertEquals(logEntryService.getAll(UUID).size(), SIZE);
    }
}
