package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.action.ImportDepartmentConfigsAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.ImportDepartmentConfigsResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.*;
import static com.google.common.base.Objects.firstNonNull;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service("departmentConfigService")
public class DepartmentConfigServiceImpl implements DepartmentConfigService {
    protected static final Log LOG = LogFactory.getLog(DepartmentConfigServiceImpl.class);

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private DepartmentConfigDao departmentConfigDao;

    @Override
    public PagingResult<DepartmentConfig> fetchAllByFilter(DepartmentConfigsFilter filter, PagingParams pagingParams) {
        Assert.notNull(filter.getDepartmentId());

        StringBuilder departmentConfigFilter = new StringBuilder().append(DepartmentConfigDetailAliases.DEPARTMENT_ID).append(" = ").append(filter.getDepartmentId());
        if (!isEmpty(filter.getKpp())) {
            departmentConfigFilter.append(" AND LOWER(KPP) like '%").append(filter.getKpp().toLowerCase()).append("%'");
        }
        if (!isEmpty(filter.getOktmo())) {
            departmentConfigFilter.append(" AND LOWER(OKTMO.CODE) like '%").append(filter.getOktmo().toLowerCase()).append("%'");
        }
        if (!isEmpty(filter.getTaxOrganCode())) {
            departmentConfigFilter.append(" AND LOWER(TAX_ORGAN_CODE) like '%").append(filter.getTaxOrganCode().toLowerCase()).append("%'");
        }
        RefBook refBook = commonRefBookService.get(NDFL_DETAIL.getId());
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        RefBookAttribute sortAttribute = null;
        // См. код с составлением запросов, там сортировка доделана если такого атрибута нет
        if (pagingParams != null && pagingParams.getProperty() != null && refBook.hasAttribute(pagingParams.getProperty())) {
            sortAttribute = refBook.getAttribute(pagingParams.getProperty());
        }
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(
                filter.getRelevanceDate(), pagingParams, departmentConfigFilter.toString(), sortAttribute);
        return new PagingResult<>(convertToDepartmentConfigs(records), records.getTotalCount());
    }

    @Override
    public List<DepartmentConfig> fetchAllByDepartmentId(int departmentId) {
        DepartmentConfigsFilter filter = new DepartmentConfigsFilter();
        filter.setDepartmentId(departmentId);
        return fetchAllByFilter(filter, null);
    }

    @Override
    public List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration) {
        return departmentConfigDao.findAllByDeclaration(declaration);
    }

    @Override
    public int fetchCount(DepartmentConfigsFilter filter) {
        StringBuilder departmentConfigFilter = new StringBuilder().append(DepartmentConfigDetailAliases.DEPARTMENT_ID).append(" = ").append(filter.getDepartmentId());
        if (!isEmpty(filter.getKpp())) {
            departmentConfigFilter.append(" AND LOWER(KPP) like '%").append(filter.getKpp().toLowerCase()).append("%'");
        }
        if (!isEmpty(filter.getOktmo())) {
            departmentConfigFilter.append(" AND LOWER(OKTMO.CODE) like '%").append(filter.getOktmo().toLowerCase()).append("%'");
        }
        if (!isEmpty(filter.getTaxOrganCode())) {
            departmentConfigFilter.append(" AND LOWER(TAX_ORGAN_CODE) like '%").append(filter.getTaxOrganCode().toLowerCase()).append("%'");
        }
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        return provider.getRecordsCount(filter.getRelevanceDate(), departmentConfigFilter.toString());
    }

    public DepartmentConfig fetch(long id) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        Map<String, RefBookValue> record = provider.getRecordData(id);
        return convertToDepartmentConfig(record, null);
    }

    @Override
    public DepartmentConfig findByKppAndOktmoAndDate(String kpp, String oktmoCode, Date date) {
        return departmentConfigDao.findByKppAndOktmoAndDate(kpp, oktmoCode, date);
    }

    @Override
    public List<DepartmentConfig> fetchAllByKppAndOktmo(String kpp, String oktmoCode) {
        String filter = "LOWER(KPP) like '%" + kpp.toLowerCase() + "%'" +
                " AND LOWER(OKTMO.CODE) like '%" + oktmoCode.toLowerCase() + "%'";
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(
                null, null, filter, null);
        return convertToDepartmentConfigs(records);
    }

    @Override
    public PagingResult<KppSelect> findAllKppByDepartmentIdAndKpp(int departmentId, String kpp, PagingParams pagingParams) {
        return departmentConfigDao.findAllKppByDepartmentIdAndKpp(departmentId, kpp, pagingParams);
    }

    @Override
    public PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter filter, PagingParams pagingParams) {
        return departmentConfigDao.findAllKppOktmoPairsByFilter(filter, pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#departmentConfig, T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).CREATE)")
    public ActionResult create(DepartmentConfig departmentConfig, TAUserInfo userInfo) {
        LOG.info("create: kpp=" + departmentConfig.getKpp() + ", oktmo=" + departmentConfig.getOktmo().getCode());
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        try {
            departmentConfig.setStartDate(SimpleDateUtils.toStartOfDay(departmentConfig.getStartDate()));
            departmentConfig.setEndDate(SimpleDateUtils.toStartOfDay(departmentConfig.getEndDate()));
            create(departmentConfig, logger);
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getCreateFailedMessage(departmentConfig));
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            LOG.error("failed");
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getCreateFailedMessage(departmentConfig));
        }
        logger.info("Для подразделения \"%s\" создана настройка КПП: \"%s\", ОКТМО: \"%s\", настройка действительна с \"%s\" по \"%s\"",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    public void create(DepartmentConfig departmentConfig, Logger logger) {
        List<DepartmentConfig> relatedDepartmentConfigs = fetchAllByKppAndOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
        if (!relatedDepartmentConfigs.isEmpty()) {
            departmentConfig.setRecordId(relatedDepartmentConfigs.get(0).getRecordId());
        }
        checkDepartmentConfig(departmentConfig, relatedDepartmentConfigs);
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        provider.createRecordVersion(logger, departmentConfig.getStartDate(), departmentConfig.getEndDate(), new ArrayList<>(singletonList(convertToRefBookRecord(departmentConfig))));
    }

    private String getCreateFailedMessage(DepartmentConfig departmentConfig) {
        return String.format("Ошибка при создании настройки подразделения для \"%s\" с параметрами: КПП: \"%s\", ОКТМО: \"%s\", дата начала действия настройки: " +
                        "\"%s\". Обратитесь к Администратору Системы или повторите операцию позднее.",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()));
    }

    @Override
    @PreAuthorize("hasPermission(#departmentConfig, T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).UPDATE)")
    public ActionResult update(DepartmentConfig departmentConfig, TAUserInfo userInfo) {
        LOG.info("update: id=" + departmentConfig.getId() + ", kpp=" + departmentConfig.getKpp() + ", oktmo=" + departmentConfig.getOktmo().getCode());
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        try {
            departmentConfig.setStartDate(SimpleDateUtils.toStartOfDay(departmentConfig.getStartDate()));
            departmentConfig.setEndDate(SimpleDateUtils.toStartOfDay(departmentConfig.getEndDate()));
            update(departmentConfig, logger);
        } catch (ServiceException e) {
            LOG.error(e.getMessage());
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getUpdateFailedMessage(departmentConfig));
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            LOG.error("failed");
            return new ActionResult(logEntryService.save(logger.getEntries()))
                    .error(getUpdateFailedMessage(departmentConfig));
        }
        logger.info("В подразделении \"%s\" изменена настройка КПП: \"%s\", ОКТМО: \"%s\", настройка действительна с \"%s\" по \"%s\"",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    public void update(DepartmentConfig departmentConfig, Logger logger) {
        checkDepartmentConfig(departmentConfig, fetchAllByKppAndOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode()));
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        provider.updateRecordVersions(logger, departmentConfig.getStartDate(), departmentConfig.getEndDate(), new HashSet<>(singletonList(convertToMap(departmentConfig))));
    }

    private String getUpdateFailedMessage(DepartmentConfig departmentConfig) {
        return String.format("Ошибка при сохранении настройки подразделения для \"%s\" с параметрами: КПП: \"%s\", ОКТМО: \"%s\", дата начала действия настройки: " +
                        "\"%s\". Обратитесь к Администратору Системы или повторите операцию позднее.",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()));
    }

    @Override
    @PreAuthorize("hasPermission(#ids, 'com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).DELETE)")
    public ActionResult delete(List<Long> ids, TAUserInfo userInfo) {
        LOG.info("delete: ids=" + ids);
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        for (long id : ids) {
            LOG.info("delete: id=" + id);
            Logger localLogger = new Logger();
            localLogger.setTaUserInfo(userInfo);
            DepartmentConfig departmentConfig = fetch(id);
            try {
                delete(departmentConfig, localLogger);
            } catch (ServiceException e) {
                LOG.error(e.getMessage());
                localLogger.error(e.getMessage());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                localLogger.error(getDeleteFailedMessage(departmentConfig, e.getMessage()));
            }
            if (localLogger.containsLevel(LogLevel.ERROR)) {
                LOG.error("failed");
                localLogger.error(getDeleteFailedMessage(departmentConfig, ""));
            } else {
                DepartmentConfig prev = departmentConfigDao.findPrev(departmentConfig);
                if (prev != null) {
                    logger.info("При удалении настройки подразделения \"" + departmentConfig.getDepartment().getName() + "\", " +
                            "КПП: " + departmentConfig.getKpp() + ", ОКТМО: " + departmentConfig.getOktmo().getCode() + ", период актуальности " +
                            "с \"" + FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()) +
                            "\" по \"" + (departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate())) +
                            "\" для другой настройки с той же парой КПП/ОКТМО и датой начала действия \"" + FastDateFormat.getInstance("dd.MM.yyyy").format(prev.getStartDate()) +
                            "\", дата окончания действия установлена в значение \"" +
                            (prev.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(prev.getEndDate())) + "\".");
                } else {
                    localLogger.info("В подразделении \"%s\" удалена настройка КПП: \"%s\", ОКТМО: \"%s\", период актуальности с \"%s\" по \"%s\"",
                            departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                            FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                            departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
                }
            }
            logger.getEntries().addAll(localLogger.getEntries());
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    public void delete(DepartmentConfig departmentConfig, Logger logger) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        provider.deleteRecordVersions(logger, new ArrayList<>(singletonList(departmentConfig.getId())));
    }

    private String getDeleteFailedMessage(DepartmentConfig departmentConfig, String error) {
        return String.format("Возникла ошибка при удалении настройки подразделения \"%s\", КПП: \"%s\", ОКТМО: \"%s\", период актуальности с " +
                        "\"%s\" по \"%s\". %s",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                error);
    }

    @Override
    public void delete(List<DepartmentConfig> departmentConfigs, Logger logger) {
        if (!departmentConfigs.isEmpty()) {
            List<Long> ids = new ArrayList<>(departmentConfigs.size());
            for (DepartmentConfig departmentConfig : departmentConfigs) {
                ids.add(departmentConfig.getId());
            }
            LOG.info("delete: ids=" + ids);
            RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
            provider.deleteRecordVersions(logger, ids);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EXPORT_DEPARTMENT_CONFIG)")
    public ActionResult createTaskToCreateExcel(DepartmentConfigsFilter filter, PagingParams pagingParams, TAUserInfo userInfo) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        AsyncTaskType taskType = AsyncTaskType.EXCEL_DEPARTMENT_CONFIGS;

        Map<String, Object> params = new HashMap<>();
        params.put("departmentId", filter.getDepartmentId());

        String keyTask = "EXCEL_DEPARTMENT_CONFIGS_" + System.currentTimeMillis();
        asyncManager.executeTask(keyTask, taskType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
            @Override
            public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
            }
        });
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).IMPORT_DEPARTMENT_CONFIG)")
    public ImportDepartmentConfigsResult createTaskToImportExcel(ImportDepartmentConfigsAction action, TAUserInfo userInfo) {
        Logger logger = new Logger();
        ImportDepartmentConfigsResult result = new ImportDepartmentConfigsResult();
        AsyncTaskType taskType = AsyncTaskType.IMPORT_DEPARTMENT_CONFIGS;
        TAUser user = userInfo.getUser();

        // Извлечение id подразделения из названия файла и валидация названия файла
        int fileNameDepartmentId = parseDepartmentIdFromFileName(action.getFileName());
        // Валидация id подразделения
        Department fileNameDepartment;
        if (!departmentService.existDepartment(fileNameDepartmentId) ||
                (fileNameDepartment = departmentService.getDepartment(fileNameDepartmentId)).getType() != DepartmentType.TERR_BANK) {
            throw new ServiceException("Не найден территориальный банк с идентификатором (" + fileNameDepartmentId + "), указанным в имени файла \"" + action.getFileName() + "\"");
        }
        int userTbId = departmentService.getParentTBId(user.getDepartmentId());
        if (!(user.hasRole(TARole.N_ROLE_CONTROL_UNP) ||
                user.hasRole(TARole.N_ROLE_CONTROL_NS) && userTbId == fileNameDepartmentId)) {
            throw new ServiceException("У вас отсутствуют права загрузки настроек подразделения " + departmentService.getDepartment(fileNameDepartmentId).getShortName() + ".");
        }
        if (!action.isSkipDepartmentCheck() && fileNameDepartmentId != action.getDepartmentId()) {
            Department selectedDepartment = departmentService.getDepartment(action.getDepartmentId());
            result.setConfirmDepartmentCheck("Загружаемый файл содержит настройки подразделения \"" + fileNameDepartment.getShortName() + "\", " +
                    "хотя отображаются данные подразделения \"" + selectedDepartment.getShortName() + "\". " +
                    "Вы действительно хотите загрузить файл?");
        } else {
            String uuid = blobDataService.create(action.getInputStream(), action.getFileName());
            Map<String, Object> params = new HashMap<>();
            params.put("departmentId", fileNameDepartmentId);
            params.put("blobDataId", uuid);
            params.put("fileName", action.getFileName());
            params.put("fileSize", action.getFileSize());

            String keyTask = "IMPORT_DEPARTMENT_CONFIGS_" + fileNameDepartmentId;
            asyncManager.executeTask(keyTask, taskType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void importExcel(int departmentId, BlobData blobData, TAUserInfo userInfo, Logger logger) {
        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("departmentId", departmentId);
        scriptParams.put("inputStream", blobData.getInputStream());
        scriptParams.put("fileName", blobData.getName());
        if (!refBookScriptingService.executeScript(userInfo, RefBook.Id.NDFL_DETAIL.getId(), FormDataEvent.IMPORT, logger, scriptParams)) {
            throw new ServiceException("Не удалось выполнить скрипт");
        }
    }

    private int parseDepartmentIdFromFileName(String fileName) {
        int departmentId;
        Matcher matcher = Pattern.compile("(\\d*)_.*_\\d{12}\\.xlsx").matcher(fileName);
        if (matcher.matches()) {
            departmentId = Integer.valueOf(matcher.group(1));
        } else {
            throw new ServiceException("Неверное имя файла \"" + fileName + "\".");
        }
        return departmentId;
    }

    @Override
    public void checkDepartmentConfig(DepartmentConfig departmentConfig, List<DepartmentConfig> relatedDepartmentConfigs) {
        if (departmentConfig.getEndDate() != null && departmentConfig.getStartDate().after(departmentConfig.getEndDate())) {
            throw new ServiceException("Дата начала актуальности записи не может быть больше даты окончания актуальности");
        }
        Date minDate = null, maxDate = new Date(0);
        List<DepartmentConfig> overlappingDepartmentConfigs = new ArrayList<>();
        for (DepartmentConfig relatedDepartmentConfig : relatedDepartmentConfigs) {
            if (departmentConfig.getId() == null || !departmentConfig.getId().equals(relatedDepartmentConfig.getId())) {
                // Проверка пересечения существующей с исходной
                if (!(relatedDepartmentConfig.getEndDate() != null && departmentConfig.getStartDate().after(relatedDepartmentConfig.getEndDate())
                        || departmentConfig.getEndDate() != null && departmentConfig.getEndDate().before(relatedDepartmentConfig.getStartDate()))) {
                    overlappingDepartmentConfigs.add(relatedDepartmentConfig);
                }
                if (minDate == null || relatedDepartmentConfig.getStartDate().before(minDate)) {
                    minDate = relatedDepartmentConfig.getStartDate();
                }
                if (maxDate != null && (relatedDepartmentConfig.getEndDate() == null || relatedDepartmentConfig.getEndDate().after(maxDate))) {
                    maxDate = relatedDepartmentConfig.getEndDate();
                }
            }
        }
        if (!overlappingDepartmentConfigs.isEmpty()) {
            throw new ServiceException("Для настройки подразделения \"%s\" с КПП: \"%s\", ОКТМО: \"%s\" период действия с \"%s\" по \"%s\" " +
                    "пересекается с периодом действия уже существующих настроек с теми же КПП и ОКТМО: %s",
                    firstNonNull(departmentConfig.getDepartment().getShortName(), departmentConfig.getDepartment().getName()),
                    departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                    departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                    makeOverlappingDepartmentConfigsString(overlappingDepartmentConfigs));
        }
        if (!(minDate == null || DateUtils.addDays(minDate, -1).equals(departmentConfig.getEndDate())
                || maxDate != null && DateUtils.addDays(maxDate, 1).equals(departmentConfig.getStartDate()))) {
            throw new ServiceException("Между периодом актуальности настройки подразделения КПП: \"%s\", ОКТМО: \"%s\" период актуальности с \"%s\" по \"%s\" " +
                    "в подразделении: \"%s\" и другими настройками с такими же КПП и ОКТМО имеется временной разрыв более 1 календарного дня.",
                    departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                    departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                    departmentConfig.getDepartment().getName());
        }
    }

    private String makeOverlappingDepartmentConfigsString(List<DepartmentConfig> overlappingDepartmentConfigs) {
        List<String> overlappingDepartmentConfigsStrings = new ArrayList<>(overlappingDepartmentConfigs.size());
        for (DepartmentConfig overlappingDepartmentConfig : overlappingDepartmentConfigs) {
            overlappingDepartmentConfigsStrings.add(String.format(
                    "[\"%s\", период действия с %s по %s]",
                    firstNonNull(overlappingDepartmentConfig.getDepartment().getShortName(), overlappingDepartmentConfig.getDepartment().getName()),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingDepartmentConfig.getStartDate()),
                    overlappingDepartmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingDepartmentConfig.getEndDate())));
        }
        return Joiner.on(", ").join(overlappingDepartmentConfigsStrings);
    }

    private List<DepartmentConfig> convertToDepartmentConfigs(List<Map<String, RefBookValue>> records) {
        List<DepartmentConfig> result = new ArrayList<>();
        // Создаем кэши для исключения избыточных запросов к БД
        Map<Long, Map<Long, Object>> cache = new HashMap<>();
        for (Map<String, RefBookValue> record : records) {
            result.add(convertToDepartmentConfig(record, cache));
        }
        return result;
    }

    private DepartmentConfig convertToDepartmentConfig(Map<String, RefBookValue> record, Map<Long, Map<Long, Object>> cache) {
        DepartmentConfig result = new DepartmentConfig();
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId());
        Long departmentId = record.get(DepartmentConfigDetailAliases.DEPARTMENT_ID.name()).getReferenceValue();
        Long presentPlaceId = record.get(DepartmentConfigDetailAliases.PRESENT_PLACE.name()).getReferenceValue();
        Long oktmoId = record.get(DepartmentConfigDetailAliases.OKTMO.name()).getReferenceValue();
        Long reorganizationId = record.get(DepartmentConfigDetailAliases.REORG_FORM_CODE.name()).getReferenceValue();
        Long signatoryMarkId = record.get(DepartmentConfigDetailAliases.SIGNATORY_ID.name()).getReferenceValue();

        result.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
        result.setRecordId(record.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue());
        RefBookRecordVersion version = provider.getRecordVersionInfo(result.getId());
        if (record.get(DepartmentConfigDetailAliases.ROW_ORD.name()) != null) {
            result.setRowOrd(record.get(DepartmentConfigDetailAliases.ROW_ORD.name()).getNumberValue().intValue());
        }
        result.setStartDate(version.getVersionStart());
        result.setEndDate(version.getVersionEnd());
        result.setDepartment((RefBookDepartment) getRecordWithCache(DEPARTMENT.getId(), departmentId, cache));
        result.setKpp(record.get(DepartmentConfigDetailAliases.KPP.name()).getStringValue());
        result.setOktmo((RefBookOktmo) getRecordWithCache(OKTMO.getId(), oktmoId, cache));
        result.setTaxOrganCode(record.get(DepartmentConfigDetailAliases.TAX_ORGAN_CODE.name()).getStringValue());
        result.setPresentPlace((RefBookPresentPlace) getRecordWithCache(PRESENT_PLACE.getId(), presentPlaceId, cache));
        result.setName(record.get(DepartmentConfigDetailAliases.NAME.name()).getStringValue());
        result.setPhone(record.get(DepartmentConfigDetailAliases.PHONE.name()).getStringValue());
        result.setSignatoryMark((RefBookSignatoryMark) getRecordWithCache(MARK_SIGNATORY_CODE.getId(), signatoryMarkId, cache));
        result.setSignatorySurName(record.get(DepartmentConfigDetailAliases.SIGNATORY_SURNAME.name()).getStringValue());
        result.setSignatoryFirstName(record.get(DepartmentConfigDetailAliases.SIGNATORY_FIRSTNAME.name()).getStringValue());
        result.setSignatoryLastName(record.get(DepartmentConfigDetailAliases.SIGNATORY_LASTNAME.name()).getStringValue());
        result.setApproveDocName(record.get(DepartmentConfigDetailAliases.APPROVE_DOC_NAME.name()).getStringValue());
        result.setApproveOrgName(record.get(DepartmentConfigDetailAliases.APPROVE_ORG_NAME.name()).getStringValue());
        result.setReorganization((RefBookReorganization) getRecordWithCache(REORGANIZATION.getId(), reorganizationId, cache));
        result.setReorgInn(record.get(DepartmentConfigDetailAliases.REORG_INN.name()).getStringValue());
        result.setReorgKpp(record.get(DepartmentConfigDetailAliases.REORG_KPP.name()).getStringValue());
        return result;
    }

    @Override
    public RefBookRecord convertToRefBookRecord(DepartmentConfig departmentConfig) {
        RefBookRecord refBookRecord = new RefBookRecord();
        refBookRecord.setUniqueRecordId(departmentConfig.getId());
        refBookRecord.setRecordId(departmentConfig.getRecordId());
        refBookRecord.setValues(convertToMap(departmentConfig));
        return refBookRecord;
    }

    private Map<String, RefBookValue> convertToMap(DepartmentConfig departmentConfig) {
        Map<String, RefBookValue> values = new HashMap<>();
        values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, departmentConfig.getId()));
        values.put(RefBook.BUSINESS_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, departmentConfig.getRecordId()));
        values.put(DepartmentConfigDetailAliases.DEPARTMENT_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getDepartment().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.KPP.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getKpp()));
        values.put(DepartmentConfigDetailAliases.OKTMO.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getOktmo().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.TAX_ORGAN_CODE.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getTaxOrganCode()));
        values.put(DepartmentConfigDetailAliases.PRESENT_PLACE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getPresentPlace().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.NAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getName()));
        values.put(DepartmentConfigDetailAliases.PHONE.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getPhone()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getSignatoryMark().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_SURNAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getSignatorySurName()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_FIRSTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getSignatoryFirstName()));
        values.put(DepartmentConfigDetailAliases.SIGNATORY_LASTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getSignatoryLastName()));
        values.put(DepartmentConfigDetailAliases.APPROVE_DOC_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getApproveDocName()));
        values.put(DepartmentConfigDetailAliases.APPROVE_ORG_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getApproveOrgName()));
        values.put(DepartmentConfigDetailAliases.REORG_FORM_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, departmentConfig.getReorganization() == null ? null : departmentConfig.getReorganization().getId().longValue()));
        values.put(DepartmentConfigDetailAliases.REORG_INN.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getReorgInn()));
        values.put(DepartmentConfigDetailAliases.REORG_KPP.name(), new RefBookValue(RefBookAttributeType.STRING, departmentConfig.getReorgKpp()));
        return values;
    }

    private <T extends RefBookSimple> T getRecordWithCache(Long refBookId, Long id, Map<Long, Map<Long, Object>> cache) {
        T value = null;
        if (id != null) {
            Map<Long, Object> recordCache = null;
            if (cache != null) {
                recordCache = cache.get(refBookId);
                if (recordCache == null) {
                    recordCache = new HashMap<>();
                    cache.put(refBookId, recordCache);
                }
                value = (T) recordCache.get(id);
            }
            if (value == null) {
                value = commonRefBookService.fetchRecord(refBookId, id);
                if (recordCache != null) {
                    recordCache.put(id, value);
                }
            }
        }
        return value;
    }

    /**
     * Константы соответствующие названиям аттрибутов в справочнике настроек подразделений
     */
    public enum DepartmentConfigDetailAliases {
        ROW_ORD,
        DEPARTMENT_ID,
        TAX_ORGAN_CODE,
        KPP,
        PRESENT_PLACE,
        NAME,
        OKTMO,
        PHONE,
        REORG_FORM_CODE,
        REORG_INN,
        REORG_KPP,
        SIGNATORY_ID,
        SIGNATORY_SURNAME,
        SIGNATORY_FIRSTNAME,
        SIGNATORY_LASTNAME,
        APPROVE_DOC_NAME,
        APPROVE_ORG_NAME
    }
}
