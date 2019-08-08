package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_PARENT_ID_ALIAS;

@Service("commonRefBookService")
public class CommonRefBookServiceImpl implements CommonRefBookService {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleDao refBookSimpleDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private TAUserService userService;
    @Autowired
    DBUtils dbUtils;

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    @Transactional(readOnly = true)
    public RefBook get(Long refBookId) {
        return refBookDao.get(refBookId);
    }

    @Override
    @Transactional(readOnly = true)
    public RefBook getByAttribute(Long attributeId) {
        return refBookDao.getByAttribute(attributeId);
    }

    @Override
    public List<RefBook> findAllVisible() {
        return refBookDao.findAllVisible();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefBookShortInfo> findAllShortInfo(String name, PagingParams pagingParams) {
        return refBookDao.findAllVisibleShortInfo(name, pagingParams);
    }

    @Override
    public String getSearchQueryStatement(String tablePrefix, String query, Long refBookId, boolean exactSearch) {
        return getSearchQueryStatementWithAdditionalStringParameters(tablePrefix, null, query, refBookId, exactSearch);
    }

    @Override
    public String getSearchQueryStatement(String query, Long refBookId, boolean exactSearch) {
        return getSearchQueryStatement("frb", query, refBookId, exactSearch);
    }

    @Override
    public String getSearchQueryStatementWithAdditionalStringParameters(Map<String, String> parameters, String query, Long refBookId, boolean exactSearch) {
        return getSearchQueryStatementWithAdditionalStringParameters("frb", parameters, query, refBookId, exactSearch);
    }

    @Override
    public String getSearchQueryStatementWithAdditionalStringParameters(String tablePrefix, Map<String, String> parameters, String query, Long refBookId, boolean exactSearch) {
        if (query != null && !query.isEmpty()) {
            String q = com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString(query);
            q = q.toLowerCase().replaceAll("\'", "\\\\\'");
            StringBuilder resultSearch = new StringBuilder();
            RefBook refBook = get(refBookId);
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (attribute.getAlias().equals(RECORD_PARENT_ID_ALIAS) || attribute.getAlias().equals("IS_ACTIVE") || (!CollectionUtils.isEmpty(parameters) && parameters.containsKey(attribute.getAlias()))) {
                    continue;
                }

                if (attribute.getAttributeType() == RefBookAttributeType.DATE) {
                    try {
                        SDF_DD_MM_YYYY.get().parse(q);
                    } catch (ParseException e) {
                        // Если дата пришла в нестандартном формате - не поиск по дате
                        continue;
                    }
                }

                if (resultSearch.length() > 0) {
                    resultSearch.append(" or ");
                }

                switch (attribute.getAttributeType()) {
                    case STRING:
                        resultSearch
                                .append("LOWER(")
                                .append(StringUtils.isNotEmpty(tablePrefix) ? tablePrefix + "." : "")
                                .append(attribute.getAlias())
                                .append(")");
                        break;
                    case NUMBER:
                        resultSearch
                                .append("TO_CHAR(")
                                .append(StringUtils.isNotEmpty(tablePrefix) ? tablePrefix + "." : "")
                                .append(attribute.getAlias())
                                .append(")");
                        break;
                    case DATE:
                        resultSearch
                                .append("TRUNC(")
                                .append(StringUtils.isNotEmpty(tablePrefix) ? tablePrefix + "." : "")
                                .append(attribute.getAlias())
                                .append(") = TO_DATE('")
                                .append(q)
                                .append("')");
                        break;
                    case REFERENCE:
                        String fullAlias = getStackAlias(attribute);
                        switch (getLastAttribute(attribute).getAttributeType()) {
                            case STRING:
                                resultSearch
                                        .append("LOWER(")
                                        .append(fullAlias)
                                        .append(")");
                                break;
                            case NUMBER:
                                resultSearch
                                        .append("TO_CHAR(")
                                        .append(fullAlias)
                                        .append(")");
                                break;
                            case DATE:
                                resultSearch.append(fullAlias);
                                break;
                            default:
                                throw new RuntimeException("Unknown RefBookAttributeType");
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown RefBookAttributeType");

                }

                if (attribute.getAttributeType() != RefBookAttributeType.DATE) {
                    if (exactSearch) {
                        resultSearch
                                .append(" like ")
                                .append("'")
                                .append(q)
                                .append("'");
                    } else {
                        resultSearch
                                .append(" like ")
                                .append("'%")
                                .append(q)
                                .append("%'");
                    }
                }
                if (!CollectionUtils.isEmpty(parameters)) {
                    resultSearch.append(" and ")
                            .append(buildQueryFromParams(parameters, exactSearch));
                }
            }
            return resultSearch.toString();
        } else {
            if (CollectionUtils.isEmpty(parameters)) {
                return null;
            } else {
                return buildQueryFromParams(parameters, exactSearch).toString();
            }
        }
    }

    private StringBuilder buildQueryFromParams(Map<String, String> parameters, boolean exactSearch) {
        StringBuilder queryBuilder = new StringBuilder();

        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }
            String q = com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString(param.getValue());
            q = q.toLowerCase().replaceAll("\'", "\\\\\'");
            queryBuilder.append("LOWER(")
                    .append(param.getKey())
                    .append(")");
            if (exactSearch) {
                queryBuilder
                        .append(" like ")
                        .append("'")
                        .append(q)
                        .append("'");
            } else {
                queryBuilder
                        .append(" like ")
                        .append("'%")
                        .append(q)
                        .append("%'");
            }
        }
        return queryBuilder;
    }

    /**
     * Метод возврадет полный алиас для ссылочного атрибута вида
     * user.city.name
     *
     * @param attribute
     * @return
     */
    private String getStackAlias(RefBookAttribute attribute) {
        switch (attribute.getAttributeType()) {
            case STRING:
            case DATE:
            case NUMBER:
                return attribute.getAlias();
            case REFERENCE:
                RefBook rb = get(attribute.getRefBookId());
                RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());
                return attribute.getAlias() + "." + getStackAlias(nextAttribute);
            default:
                throw new RuntimeException("Unknown RefBookAttributeType");
        }
    }

    /**
     * Метод возвращает последний не ссылочный атрибут по цепочке
     * ссылок
     *
     * @param attribute ссылочный атрибут для которого нужно получить последний не ссылочный атрибут
     * @return
     */
    private RefBookAttribute getLastAttribute(RefBookAttribute attribute) {
        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
            RefBook rb = getByAttribute(attribute.getRefBookAttributeId());
            RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());

            return getLastAttribute(nextAttribute);
        } else {
            return attribute;
        }
    }

    @Override
    public String getRefBookActionDescription(DescriptionTemplate descriptionTemplate, Long refBookId) {
        RefBook refBook = get(refBookId);
        switch (descriptionTemplate) {
            case REF_BOOK_EDIT:
                return String.format(descriptionTemplate.getText(), refBook.getName());
            default:
                throw new ServiceException("Неверный тип шаблона(%s)", descriptionTemplate);
        }
    }

    @Override
    public String generateTaskKey(long refBookId) {
        return LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
    }

    @Override
    @Transactional(readOnly = true)
    public String getRefBookLockDescription(LockData lockData, long refBookId) {
        if (lockData.getTaskId() != null) {
            //Заблокировано асинхроннной задачей
            AsyncTaskData lockTaskData = asyncManager.getLightTaskData(lockData.getTaskId());
            return String.format(AsyncTask.LOCK_CURRENT,
                    SDF_DD_MM_YYYY.get().format(lockData.getDateLock()),
                    userService.getUser(lockData.getUserId()).getName(),
                    lockTaskData.getDescription());
        } else {
            //Заблокировано редактированием
            return String.format(AsyncTask.LOCK_CURRENT,
                    SDF_DD_MM_YYYY.get().format(lockData.getDateLock()),
                    userService.getUser(lockData.getUserId()).getName(),
                    getRefBookActionDescription(DescriptionTemplate.REF_BOOK_EDIT, refBookId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RefBookAttribute getAttributeByAlias(long refBookId, String attributeAlias) {
        for (RefBookAttribute attribute : refBookDao.getAttributes(refBookId)) {
            if (attribute.getAlias().equals(attributeAlias)) {
                return attribute;
            }
        }
        return null;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public <T extends RefBookSimple> PagingResult<T> fetchAllRecords(long refBookId, List<String> columns, String searchPattern, String filter, PagingParams pagingParams) {
        RefBookAttribute sortAttribute = pagingParams != null && StringUtils.isNotEmpty(pagingParams.getProperty()) ?
                getAttributeByAlias(refBookId, pagingParams.getProperty()) : null;
        String direction = pagingParams != null ? pagingParams.getDirection() : "asc";
        return refBookSimpleDao.getRecords(get(refBookId), sortAttribute, direction, pagingParams, columns, searchPattern, filter);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public <T extends RefBookSimple> T fetchRecord(Long refBookId, Long recordId) {
        return refBookSimpleDao.getRecord(get(refBookId), recordId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    @Transactional
    public ActionResult editRecord(TAUserInfo userInfo, long refBookId, long recordId, Map<String, RefBookValue> record) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("record", record);
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.SAVE, logger, scriptParams);
        Date versionFrom = record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS) ? record.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue() : null;
        Date versionTo = record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS) ? record.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue() : null;
        Map<String, RefBookValue> recordToSave = new HashMap<>(record); // Создаем копию записи без дат актуальности для корректного формирования записи в Журнал Аудита
        recordToSave.remove(RefBook.RECORD_VERSION_FROM_ALIAS);
        recordToSave.remove(RefBook.RECORD_VERSION_TO_ALIAS);

        refBookFactory.getDataProvider(refBookId).updateRecordVersion(logger, recordId, versionFrom, versionTo, recordToSave);
        String uuid = logEntryService.save(logger.getEntries());
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Не удалось сохранить запись справочника!", uuid);
        } else {
            return new ActionResult(uuid);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    @Transactional
    public ActionResult createRecord(TAUserInfo userInfo, Long refBookId, Map<String, RefBookValue> record) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        RefBook refBook = get(refBookId);

        RefBookRecord refBookRecord = new RefBookRecord();
        refBookRecord.setValues(record);
        Date versionFrom = null;
        Date versionTo = null;

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("record", record);
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.SAVE, logger, scriptParams);
        if (refBook.isVersioned()) {
            versionFrom = record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS) ? record.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue() : null;
            versionTo = record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS) ? record.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue() : null;
            Long recordId = record.containsKey(RefBook.BUSINESS_ID_ALIAS) ? record.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue() : null;

            if (versionFrom == null) {
                throw new ServiceException("Дата начала актуальности записи не может быть пустой!");
            }
            if (versionTo != null && versionTo.before(versionFrom)) {
                throw new ServiceException("Дата начала актуальности записи не может быть больше даты окончания актуальности!");
            }

            refBookRecord.setRecordId(recordId);
            refBookRecord.setVersionTo(versionTo);
        }

        refBookFactory.getDataProvider(refBookId).createRecordVersion(logger, versionFrom, versionTo, Collections.singletonList(refBookRecord));
        String uuid = logEntryService.save(logger.getEntries());
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Не удалось создать запись справочника!", uuid);
        } else {
            return new ActionResult(uuid);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    @Transactional
    public ActionResult deleteRecords(TAUserInfo userInfo, Long refBookId, List<Long> recordIds) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        refBookFactory.getDataProvider(refBookId).deleteAllRecords(logger, recordIds);
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    @Transactional
    public ActionResult deleteVersions(TAUserInfo userInfo, Long refBookId, List<Long> recordIds) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        refBookFactory.getDataProvider(refBookId).deleteRecordVersions(logger, recordIds);
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ActionResult createReport(TAUserInfo userInfo, long refBookId, Date version, PagingParams pagingParams,
                                     String searchPattern, boolean exactSearch, Map<String, String> extraParams, AsyncTaskType reportType) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        RefBook refBook = get(refBookId);

        LockData lockData = lockDataService.findLock(generateTaskKey(refBook.getId()));
        if (lockData == null) {
            RefBookAttribute sortAttribute;
            String direction = "asc";
            if (refBook.isHierarchic()) {
                sortAttribute = refBook.getAttribute("NAME");
            } else {
                if (StringUtils.isNotEmpty(pagingParams.getProperty())) {
                    sortAttribute = refBook.getAttribute(pagingParams.getProperty());
                    direction = pagingParams.getDirection();
                } else {
                    sortAttribute = new RefBookAttribute(RefBook.RECORD_ID_ALIAS, RefBookAttributeType.NUMBER);
                }
            }

            Map<String, Object> params = new HashMap<>();
            params.put("refBookId", refBookId);
            if (version != null) {
                params.put("version", version);
            }
            params.put("searchPattern", searchPattern);
            params.put("exactSearch", exactSearch);
            params.put("sortAttribute", sortAttribute);
            params.put("direction", direction);
            if (extraParams == null) {
                extraParams = new HashMap<>();
            }
            params.put("extraParams", extraParams);


            String keyTask = String.format("%s_%s_refBookId_%d_version_%s_filter_%s_%s_%s_%s",
                    LockData.LockObjects.REF_BOOK.name(), reportType.getName(), refBookId, version != null ? SDF_DD_MM_YYYY.get().format(version) : null, searchPattern,
                    sortAttribute.getAlias(), direction, UUID.randomUUID());
            asyncManager.executeTask(keyTask, reportType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        } else {
            logger.info(getRefBookLockDescription(lockData, refBook.getId()));
            throw new ServiceLoggerException("Для текущего справочника запущена операция, при которой формирование отчета невозможно", logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PagingResult<Map<String, RefBookValue>> fetchAllRecords(Long refBookId, Long recordId, Date version,
                                                                   String searchPattern, boolean exactSearch, Map<String, String> extraParams,
                                                                   PagingParams pagingParams, RefBookAttribute sortAttribute, String direction) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        PagingResult<Map<String, RefBookValue>> records;
        if (recordId == null) {
            String filter = createSearchFilter(refBookId, extraParams, searchPattern, exactSearch);

            // Отбираем все записи справочника
            records = provider.getRecordsWithVersionInfo(version, pagingParams, filter, sortAttribute, direction);
        } else {
            // Отбираем все версии записи правочника
            records = provider.getRecordVersionsByRecordId(recordId, pagingParams, null, sortAttribute);
        }
        return records;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public int getRecordsCount(Long refBookId, Date version, String searchPattern, boolean exactSearch, Map<String, String> extraParams) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        String filter = null;
        if (StringUtils.isNotEmpty(searchPattern)) {
            // Волшебным образом получаем кусок sql-запроса, который подставляется в итоговый и применяется в качестве фильтра для отбора записей
            if (CollectionUtils.isEmpty(extraParams)) {
                filter = getSearchQueryStatement(searchPattern, refBookId, exactSearch);
            } else {
                filter = getSearchQueryStatementWithAdditionalStringParameters(extraParams, searchPattern, refBookId, exactSearch);
            }
        }
        return provider.getRecordsCount(version, filter);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> dereference(RefBook refBook, PagingResult<Map<String, RefBookValue>> records) {
        // Алиас атрибута - id справочника
        Map<String, RefBookDataProvider> attributeProviders = new HashMap<>();
        // Собираем атрибуты, которые ссылаются на другие справочники
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                attributeProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
            }
        }

        if (!attributeProviders.isEmpty()) {
            // алиас атрибута - список ссылок на справочник, соответствующий этому атрибуту
            Map<String, List<Long>> referencesByAttribute = new HashMap<>();
            // id ссылки - список записей с этой ссылкой
            Map<Long, List<Map<String, RefBookValue>>> recordsByReference = new HashMap<>();

            // Группируем ссылки на другие записи по справочнику, чтобы потом получить их одним запросом
            for (Map<String, RefBookValue> record : records) {
                for (Map.Entry<String, RefBookDataProvider> reference : attributeProviders.entrySet()) {
                    String referenceAlias = reference.getKey();
                    if (record.get(referenceAlias).getReferenceValue() != null) {
                        Long uniqueRecordId = record.get(referenceAlias).getReferenceValue();

                        // Сохраняем привязку алиаса и идентификатора ссылки
                        if (!referencesByAttribute.containsKey(referenceAlias)) {
                            referencesByAttribute.put(referenceAlias, new ArrayList<Long>());
                        }
                        referencesByAttribute.get(referenceAlias).add(uniqueRecordId);

                        // Сохраняем идентификатора ссылки и записи в которой эта ссылка находится
                        if (!recordsByReference.containsKey(uniqueRecordId)) {
                            recordsByReference.put(uniqueRecordId, new ArrayList<Map<String, RefBookValue>>());
                        }
                        recordsByReference.get(uniqueRecordId).add(record);
                    }
                }
            }

            // Получаем разыменованные значения записей справочников и подкладываем внутрь записей
            // TODO: тут не очень оптимально сделано - сгруппировано фактически по атрибутам а не по справочникам,
            // TODO: так если в справочнике (например АСНУ) есть 2 атрибута, ссылающихся на 1 справочник, то запроса будет 2, а не 1
            // TODO: это сделано, потому что так удобнее потом подставлять по алиасам атрибутов разыменованные значения, а таких больших справочников нет
            for (Map.Entry<String, List<Long>> reference : referencesByAttribute.entrySet()) {
                String attributeAlias = reference.getKey();
                Map<Long, Map<String, RefBookValue>> values = attributeProviders.get(attributeAlias).getRecordData(reference.getValue());
                for (Map.Entry<Long, Map<String, RefBookValue>> value : values.entrySet()) {
                    Long uniqueRecordId = value.getKey();
                    Map<String, RefBookValue> referenceObject = value.getValue();
                    if (recordsByReference.containsKey(uniqueRecordId)) {
                        for (Map<String, RefBookValue> record : recordsByReference.get(uniqueRecordId)) {
                            record.put(attributeAlias, new RefBookValue(RefBookAttributeType.REFERENCE, referenceObject));
                        }
                    }
                }
            }
        }
        return records;
    }

    @Override
    public Long createNextRefBookRecordId() {
        return dbUtils.getNextRefBookRecordIds(1).get(0);
    }

    private String createSearchFilter(Long refBookId, Map<String, String> extraParams, String searchPattern, Boolean exactSearch) {
        String filter = "";

        if (StringUtils.isNotEmpty(searchPattern) || !CollectionUtils.isEmpty(extraParams)) {
            // Волшебным образом получаем кусок sql-запроса, который подставляется в итоговый и применяется в качестве фильтра для отбора записей
            if (CollectionUtils.isEmpty(extraParams)) {
                filter = getSearchQueryStatement(searchPattern, refBookId, exactSearch);
            } else {
                filter = getSearchQueryStatementWithAdditionalStringParameters(extraParams, searchPattern, refBookId, exactSearch);
            }
        }

        return filter;
    }
}