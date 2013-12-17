package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LogEntryServiceImpl implements LogEntryService {

    @Autowired
    private LogEntryDao logEntryDao;

    @Override
    public PagingResult<LogEntry> get(String uuid, int offset, int length) {
        PagingResult<LogEntry> logEntryPagingResult = new PagingResult<LogEntry>();
        List<LogEntry> logEntries = logEntryDao.get(uuid);

        if (logEntries == null || offset < 0 || length < 0) {
            logEntryPagingResult.setTotalCount(0);
            return logEntryPagingResult;
        }

        int size = logEntries.size();
        if (size > offset + length) {
            logEntryPagingResult.addAll(logEntries.subList(offset, offset + length));
        } else if (size > offset) {
            logEntryPagingResult.addAll(logEntries.subList(offset, size));
        }
        logEntryPagingResult.setTotalCount(size);
        return logEntryPagingResult;
    }

    @Override
    public List<LogEntry> getAll(String uuid) {
        return logEntryDao.get(uuid);
    }

    @Override
    public String save(List<LogEntry> logEntry) {
        String uuid = UUID.randomUUID().toString().toLowerCase();
        logEntryDao.save(logEntry, uuid);
        return uuid;
    }
}
