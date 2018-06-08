package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
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
import com.aplana.sbrf.taxaccounting.service.impl.TAAbstractScriptingServiceImpl;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_PARENT_ID_ALIAS;

@Service
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

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public RefBook get(Long refBookId) {
        return refBookDao.get(refBookId);
    }

    @Override
    public RefBook getByAttribute(Long attributeId) {
        return refBookDao.getByAttribute(attributeId);
    }

    @Override
    public List<RefBook> fetchAll(Boolean visible) {
        return refBookDao.fetchAll(visible);
    }

    @Override
    public List<RefBook> fetchAll() {
        return fetchAll(true);
    }

    @Override
    public String getSearchQueryStatement(String query, Long refBookId) {
        return getSearchQueryStatement(query, refBookId, false);
    }

    @Override
    public String getSearchQueryStatement(String query, Long refBookId, boolean exactSearch) {
        return getSearchQueryStatementWithAdditionalStringParameters(null, query, refBookId, exactSearch);
    }

    @Override
    public String getSearchQueryStatementWithAdditionalStringParameters(Map<String, String> parameters, String query, Long refBookId, boolean exactSearch) {
        if (query != null && !query.isEmpty()) {
            String q = com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString(query);
            q = q.toLowerCase().replaceAll("\'", "\\\\\'");
            StringBuilder resultSearch = new StringBuilder();
            RefBook refBook = get(refBookId);
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (attribute.getAlias().equals(RECORD_PARENT_ID_ALIAS) || attribute.getAlias().equals("IS_ACTIVE") || (!CollectionUtils.isEmpty(parameters) && parameters.containsKey(attribute.getAlias()))) {
                    continue;
                }

                if (resultSearch.length() > 0) {
                    resultSearch.append(" or ");
                }

                switch (attribute.getAttributeType()) {
                    case STRING:
                        resultSearch
                                .append("LOWER(")
                                .append(attribute.getAlias())
                                .append(")");
                        break;
                    case NUMBER:
                        resultSearch
                                .append("TO_CHAR(")
                                .append(attribute.getAlias())
                                .append(")");
                        break;
                    case DATE:
                        resultSearch
                                .append("TRUNC(")
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
    public RefBookAttribute getAttributeByAlias(long refBookId, String attributeAlias) {
        for (RefBookAttribute attribute : refBookDao.getAttributes(refBookId)) {
            if (attribute.getAlias().equals(attributeAlias)) {
                return attribute;
            }
        }
        return null;
    }

    @Override
    public boolean getEventScriptStatus(long refBookId, FormDataEvent event) {
        String script = refBookScriptingService.getScript(refBookId);
        if (script != null && !script.isEmpty()) {
            return TAAbstractScriptingServiceImpl.canExecuteScript(script, event);
        } else {
            return false;
        }
    }

    @Override
    public Map<FormDataEvent, Boolean> getEventScriptStatus(long refBookId) {
        List<FormDataEvent> formDataEventList = Arrays.asList(FormDataEvent.ADD_ROW, FormDataEvent.IMPORT);
        Map<FormDataEvent, Boolean> eventScriptStatus = new HashMap<FormDataEvent, Boolean>();
        String script = refBookScriptingService.getScript(refBookId);
        if (script != null && !script.isEmpty()) {
            for (FormDataEvent event : formDataEventList) {
                eventScriptStatus.put(event, TAAbstractScriptingServiceImpl.canExecuteScript(script, event));
            }
        } else {
            for (FormDataEvent event : formDataEventList) {
                eventScriptStatus.put(event, false);
            }
        }
        return eventScriptStatus;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public <T extends RefBookSimple> PagingResult<T> fetchAllRecords(long refBookId, List<String> columns, String filter, PagingParams pagingParams) {
        return refBookSimpleDao.getRecords(get(refBookId), pagingParams, columns, filter);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public <T extends RefBookSimple> T fetchRecord(Long refBookId, Long recordId) {
        return refBookSimpleDao.getRecord(get(refBookId), recordId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
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
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public ActionResult createRecord(TAUserInfo userInfo, Long refBookId, Map<String, RefBookValue> record) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        Date versionFrom = record.containsKey(RefBook.RECORD_VERSION_FROM_ALIAS) ? record.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue() : null;
        Date versionTo = record.containsKey(RefBook.RECORD_VERSION_TO_ALIAS) ? record.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue() : null;
        Long recordId = record.containsKey(RefBook.BUSINESS_ID_ALIAS) ? record.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue() : null;

        if (versionFrom == null) {
            throw new ServiceException("Дата начала актуальности записи не может быть пустой!");
        }
        if (versionTo != null && versionTo.before(versionFrom)) {
            throw new ServiceException("Дата начала актуальности записи не может быть больше даты окончания актуальности!");
        }

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
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public ActionResult deleteRecords(TAUserInfo userInfo, Long refBookId, List<Long> recordIds) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        refBookFactory.getDataProvider(refBookId).deleteAllRecords(logger, recordIds);
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public ActionResult deleteVersions(TAUserInfo userInfo, Long refBookId, List<Long> recordIds) {
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        refBookFactory.getDataProvider(refBookId).deleteRecordVersions(logger, recordIds);
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ActionResult createReport(TAUserInfo userInfo, long refBookId, Date version, PagingParams pagingParams, String searchPattern, boolean exactSearch, AsyncTaskType reportType) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        RefBook refBook = get(refBookId);

        LockData lockData = lockDataService.getLock(generateTaskKey(refBook.getId()));
        if (lockData == null) {
            String filter = "";
            if (StringUtils.isNotEmpty(searchPattern)) {
                // Волшебным образом получаем кусок sql-запроса, который подставляется в итоговый и применяется в качестве фильтра для отбора записей
                filter = getSearchQueryStatement(searchPattern, refBook.getId(), exactSearch);
            }
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
            params.put("exactSearch", exactSearch);
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
            logger.info(getRefBookLockDescription(lockData, refBook.getId()));
            throw new ServiceLoggerException("Для текущего справочника запущена операция, при которой формирование отчета невозможно", logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @SuppressWarnings("unchecked")
    public Collection<Map<String, RefBookValue>> fetchHierRecords(Long refBookId, String searchPattern, boolean exactSearch) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        String filter = null;
        if (StringUtils.isNotEmpty(searchPattern)) {
            // Волшебным образом получаем кусок sql-запроса, который подставляется в итоговый и применяется в качестве фильтра для отбора записей
            filter = getSearchQueryStatement(searchPattern, refBookId, exactSearch);
        }
        List<Map<String, RefBookValue>> records = provider.getRecords(null, null, filter, null, true);
        Map<Number, Map<String, RefBookValue>> recordsById = new HashMap<>();
        List<Map<String, RefBookValue>> result = new ArrayList<>();

        // Группируем по id
        for (Map<String, RefBookValue> record : records) {
            recordsById.put(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue(), record);
        }
        if (StringUtils.isNotEmpty(filter)) {
            Map<Number, Map<String, RefBookValue>> parents = new HashMap<>();
            // Если есть фильтр, то надо для найденных записей найти все родительские для корректного отображения в дереве
            for (Map<String, RefBookValue> record : recordsById.values()) {
                parents = fetchParentsRecursively(provider, record, parents);
            }
            recordsById.putAll(parents);
        }

        // Собираем дочерние подразделения внутри родительских
        for (Map<String, RefBookValue> record : recordsById.values()) {
            if (record.get(RefBook.RECORD_PARENT_ID_ALIAS) != null) {
                Number parentId = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
                if (parentId != null) {
                    Map<String, RefBookValue> parent = recordsById.get(parentId);
                    if (!parent.containsKey(RefBook.RECORD_CHILDREN_ALIAS)) {
                        parent.put(RefBook.RECORD_CHILDREN_ALIAS, new RefBookValue(RefBookAttributeType.COLLECTION, new ArrayList()));
                    }
                    parent.get(RefBook.RECORD_CHILDREN_ALIAS).getCollectionValue().add(record);
                    if (parentId.intValue() == Department.ROOT_DEPARTMENT_ID) {
                        // Отбираем только ТБ - все остальные подразделения будут уже как дочерние
                        result.add(record);
                    }
                }
            }
        }
        return result;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public PagingResult<Map<String, RefBookValue>> fetchAllRecords(Long refBookId, Long recordId, Date version, String searchPattern, boolean exactSearch, PagingParams pagingParams) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        RefBookAttribute sortAttribute = StringUtils.isNotEmpty(pagingParams.getProperty()) ?
                getAttributeByAlias(refBookId, pagingParams.getProperty()) : null;
        PagingResult<Map<String, RefBookValue>> records;
        if (recordId == null) {
            String filter = null;
            if (StringUtils.isNotEmpty(searchPattern)) {
                // Волшебным образом получаем кусок sql-запроса, который подставляется в итоговый и применяется в качестве фильтра для отбора записей
                filter = getSearchQueryStatement(searchPattern, refBookId, exactSearch);
            }
            // Отбираем все записи справочника
            records = provider.getRecordsWithVersionInfo(version,
                    pagingParams, filter, sortAttribute, pagingParams.getDirection().toLowerCase().equals("asc"));
        } else {
            // Отбираем все версии записи правочника
            records = provider.getRecordVersionsByRecordId(recordId, pagingParams, null, sortAttribute);
        }
        return records;
    }

    /**
     * Рекурсивно находит все родительские записи для указанной. В первую очередь запись ищется среди ранее найденных, т.к в этом случае искать дальше смысла нет - все родительские уже в этом списке
     *
     * @param provider провайдер для доступа к справочнику
     * @param record   текущая запись
     * @param parents  все ранее найденные родительские записи упорядоченные по ID
     * @return дополненный список родительских записей
     */
    private Map<Number, Map<String, RefBookValue>> fetchParentsRecursively(RefBookDataProvider provider, Map<String, RefBookValue> record, Map<Number, Map<String, RefBookValue>> parents) {
        if (record.containsKey(RefBook.RECORD_PARENT_ID_ALIAS)) {
            Long parentId = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
            if (parentId != null && !parents.containsKey(parentId)) {
                Map<String, RefBookValue> parent = provider.getRecordData(parentId);
                parents.put(parentId, parent);
                if (parentId == Department.ROOT_DEPARTMENT_ID) {
                    return parents;
                } else {
                    fetchParentsRecursively(provider, parent, parents);
                }
            }
        }
        return parents;
    }

    public PagingResult<Map<String, RefBookValue>> dereference(RefBook refBook, PagingResult<Map<String, RefBookValue>> records) {
        // Собираем атрибуты, которые ссылаются на другие справочники
        Map<String, RefBookDataProvider> referenceAttributes = new HashMap<>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                referenceAttributes.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
            }
        }

        if (!referenceAttributes.isEmpty()) {
            // Разыменовываем справочные атрибуты
            for (Map<String, RefBookValue> record : records) {
                for (Map.Entry<String, RefBookDataProvider> reference : referenceAttributes.entrySet()) {
                    String referenceAlias = reference.getKey();
                    if (record.get(referenceAlias).getReferenceValue() != null) {
                        Map<String, RefBookValue> referenceObject = reference.getValue().getRecordData(record.get(referenceAlias).getReferenceValue());
                        record.put(referenceAlias, new RefBookValue(RefBookAttributeType.REFERENCE, referenceObject));
                    }
                }
            }
        }
        return records;
    }
}
