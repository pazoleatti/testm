package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CommonRefBookServiceImpl implements CommonRefBookService {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookSimpleDao refBookDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncManager asyncManager;

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

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

    @Override
    public ActionResult createReport(TAUserInfo userInfo, long refBookId, Date version, PagingParams pagingParams, AsyncTaskType reportType) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        RefBook refBook = refBookFactory.get(refBookId);

        LockData lockData = lockDataService.getLock(refBookFactory.generateTaskKey(refBook.getId()));
        if (lockData == null) {
            String filter = "";
            String searchPattern = "";
            // TODO: возможно это пригодится после реализации поиска
            /*String filter = null;
            String lastNamePattern = action.getLastNamePattern();
            String firstNamePattern = action.getFirstNamePattern();
            String searchPattern = action.getSearchPattern();
            boolean isPerson = action.getRefBookId() == RefBook.Id.PERSON.getId();
            if (searchPattern != null && !searchPattern.isEmpty() && (!isPerson || (firstNamePattern == null && lastNamePattern == null))) {
                filter = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId(), action.isExactSearch());
                searchPattern = "Фильтр: \"" + searchPattern + "\"";
            } else if (isPerson && (firstNamePattern != null && !firstNamePattern.isEmpty() || lastNamePattern != null && !lastNamePattern.isEmpty())) {
                Map<String, String> params = new HashMap<String, String>();
                StringBuilder sb = new StringBuilder();
                if (lastNamePattern != null && !lastNamePattern.isEmpty()) {
                    sb.append("Фильтр по фамилии: \"").append(lastNamePattern).append("\"");
                    params.put("LAST_NAME", lastNamePattern);
                }
                if (firstNamePattern != null && !firstNamePattern.isEmpty()) {
                    sb.append(sb.length() != 0 ? ", " : "");
                    sb.append("Фильтр по имени: \"").append(firstNamePattern).append("\"");
                    params.put("FIRST_NAME", firstNamePattern);
                }
                if (searchPattern != null && !searchPattern.isEmpty()) {
                    sb.append(sb.length() != 0 ? ", " : "");
                    sb.append("Фильтр по всем полям: \"").append(searchPattern).append("\"");
                }
                filter = refBookFactory.getSearchQueryStatementWithAdditionalStringParameters(params, searchPattern, refBook.getId(), action.isExactSearch());
                searchPattern = sb.toString();
            }*/
            RefBookAttribute sortAttribute;
            boolean isAscSorting;
            if (refBook.isHierarchic()) {
                sortAttribute = refBook.getAttribute("NAME");
                isAscSorting = true;
            } else {
                sortAttribute = refBook.getAttribute(pagingParams.getProperty());
                isAscSorting = pagingParams.getDirection().toLowerCase().equals("asc");
            }

            Map<String, Object> params = new HashMap<>();
            params.put("refBookId", refBookId);
            if (version != null) {
                params.put("version", version);
            }
            params.put("searchPattern", searchPattern);
            params.put("filter", filter);
            params.put("sortAttribute", sortAttribute.getId());
            params.put("isSortAscending", isAscSorting);

            String keyTask = String.format("%s_%s_refBookId_%d_version_%s_filter_%s_%s_%s_%s",
                    LockData.LockObjects.REF_BOOK.name(), reportType.getName(), refBookId, version != null ? SDF_DD_MM_YYYY.get().format(version) : null, searchPattern,
                    sortAttribute.getAlias(), isAscSorting, UUID.randomUUID());
            asyncManager.executeTask(keyTask, reportType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        } else {
            logger.info(refBookFactory.getRefBookLockDescription(lockData, refBook.getId()));
            throw new ServiceLoggerException("Для текущего справочника запущена операция, при которой формирование отчета невозможно", logEntryService.save(logger.getEntries()));
        }
    }
}
