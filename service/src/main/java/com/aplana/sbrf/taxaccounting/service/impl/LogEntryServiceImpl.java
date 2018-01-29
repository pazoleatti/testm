package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogDao;
import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public PagingResult<LogEntry> fetch(String uuid, PagingParams pagingParams) {
        return logEntryDao.fetch(uuid, pagingParams);
    }

    @Override
    public List<LogEntry> getAll(String uuid) {
        return logEntryDao.fetch(uuid);
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
        return logEntryDao.countLogLevel(uuid);
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
