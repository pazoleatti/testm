package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogDao;
import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class LogEntryServiceImpl implements LogEntryService {

    @Autowired
    private LogEntryDao logEntryDao;

    @Autowired
    private LogDao logDao;
    
    @Autowired
    private TransactionHelper tx;

    @Override
    public PagingResult<LogEntry> get(String uuid, int offset, int length) {
        return logEntryDao.get(uuid, offset, length);
    }

    @Override
    public List<LogEntry> getAll(String uuid) {
        return logEntryDao.get(uuid);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String save(final List<LogEntry> logEntries) {

        return tx.executeInNewTransaction(new TransactionLogic<String>() {
            @Override
            public String execute() {
                if (logEntries == null || logEntries.isEmpty()) {
                    return null;
                }

                String uuid = UUID.randomUUID().toString().toLowerCase();

                logDao.save(uuid);
                logEntryDao.save(logEntries, uuid);

                return uuid;
            }
        });
    }

    @Override
    public Map<LogLevel, Integer> getLogCount(String uuid) {
        Map<LogLevel, Integer> retMap = new HashMap<LogLevel, Integer>();
        int error = 0;
        int warn = 0;
        int info = 0;

        List<LogEntry> logEntries = getAll(uuid);
        if (logEntries != null) {
            for (LogEntry logEntry : logEntries) {
                switch (logEntry.getLevel()) {
                    case ERROR:
                        error++;
                        break;
                    case WARNING:
                        warn++;
                        break;
                    case INFO:
                        info++;
                }
            }
        }

        retMap.put(LogLevel.ERROR, error);
        retMap.put(LogLevel.WARNING, warn);
        retMap.put(LogLevel.INFO, info);

        return retMap;
    }

    @Override
    public String update(List<LogEntry> logEntries, String uuid) {
        return add(logEntries, uuid, false);
    }

    @Override
    public String addFirst(List<LogEntry> logEntries, String uuid) {
        return add(logEntries, uuid, true);
    }

    private String add(List<LogEntry> logEntries, String uuid, boolean first) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }

        logEntryDao.update(logEntries, uuid, first);

        return uuid;
    }
}
