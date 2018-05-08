package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommonRefBookServiceImpl implements CommonRefBookService {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookSimpleDao refBookDao;
    @Autowired
    private LogEntryService logEntryService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public PagingResult<RefBookListResult> fetchAllRefBooks() {
        List<RefBook> refBookList = refBookFactory.getAll(true);
        PagingResult<RefBookListResult> toRet = new PagingResult<>();
        for (RefBook refBook : refBookList) {
            RefBookListResult res = new RefBookListResult();
            res.setRefBookName(refBook.getName());
            res.setRefBookType(refBook.getType());
            res.setRefBookId(refBook.getId());
            res.setReadOnly(refBook.isReadOnly());
            toRet.add(res);
        }
        return toRet;
    }

    @Override
    public <T extends RefBookSimple> PagingResult<T> fetchAllRecords(long refBookId, List<String> columns, String filter, PagingParams pagingParams) {
        return refBookDao.getRecords(refBookFactory.get(refBookId), pagingParams, columns, filter);
    }

    @Override
    public <T extends RefBookSimple> T fetchRecord(Long refBookId, Long recordId) {
        return refBookDao.getRecord(refBookFactory.get(refBookId), recordId);
    }

    @Override
    public ActionResult editRecord(TAUserInfo userInfo, long refBookId, long recordId, Map<String, RefBookValue> record) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        Date versionFrom = record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS) ? record.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue() : null;
        Date versionTo = record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS) ? record.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue() : null;
        record.remove(RefBook.RECORD_VERSION_FROM_ALIAS);
        record.remove(RefBook.RECORD_VERSION_TO_ALIAS);

        refBookFactory.getDataProvider(refBookId).updateRecordVersion(logger, recordId, versionFrom, versionTo, record);
        String uuid = logEntryService.save(logger.getEntries());
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Не удалось сохранить запись справочника!", uuid);
        } else {
            return new ActionResult(uuid);
        }
    }

    @Override
    public ActionResult createRecord(TAUserInfo userInfo, Long refBookId, Map<String, RefBookValue> record) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        Date versionFrom = record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS) ? record.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue() : null;
        Date versionTo = record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS) ? record.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue() : null;
        Long recordId = record.containsKey(RefBook.BUSINESS_ID_ALIAS) ? record.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue() : null;
        RefBookRecord refBookRecord = new RefBookRecord();
        refBookRecord.setValues(record);
        refBookRecord.setRecordId(recordId);
        refBookRecord.setVersionTo(versionTo);

        refBookFactory.getDataProvider(refBookId).createRecordVersion(logger, versionFrom, versionTo, Collections.singletonList(refBookRecord));
        String uuid = logEntryService.save(logger.getEntries());
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Не удалось создать запись справочника!", uuid);
        } else {
            return new ActionResult(uuid);
        }
    }

    @Override
    public ActionResult deleteRecords(TAUserInfo userInfo, Long refBookId, List<Long> recordIds) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        refBookFactory.getDataProvider(refBookId).deleteRecordVersions(logger, recordIds);
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    public int getRecordVersionCount(Long refBookId, Long recordId) {
        return refBookFactory.getDataProvider(refBookId).getRecordVersionsCount(recordId);
    }
}
