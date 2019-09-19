package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.action.ImportDepartmentConfigsAction;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.ImportDepartmentConfigsResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Objects.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Service("departmentConfigService")
public class DepartmentConfigServiceImpl implements DepartmentConfigService {
    protected static final Log LOG = LogFactory.getLog(DepartmentConfigServiceImpl.class);

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
    public DepartmentConfig findById(long id) {
        return departmentConfigDao.findById(id);
    }

    @Override
    public List<DepartmentConfig> findAllByDepartmentId(int departmentId) {
        return departmentConfigDao.findAllByDepartmentId(departmentId);
    }

    @Override
    public List<KppOktmoPair> findAllKppOktmoPairs(long declarationId, Integer departmentId, int reportPeriodId) {
        return departmentConfigDao.findAllKppOKtmoPairsByDeclaration(declarationId, departmentId, reportPeriodId, new Date());
    }

    @Override
    public List<KppOktmoPair> findAllKppOktmoPairs(Integer departmentId, int reportPeriodId) {
        return departmentConfigDao.findAllKppOKtmoPairs(departmentId, reportPeriodId, new Date());
    }

    @Override
    public PagingResult<DepartmentConfig> findPageByFilter(DepartmentConfigsFilter filter, PagingParams pagingParams) {
        return departmentConfigDao.findPageByFilter(filter, pagingParams);
    }

    @Override
    public List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration) {
        return departmentConfigDao.findAllByDeclaration(declaration, new Date());
    }

    @Override
    public int countByFilter(DepartmentConfigsFilter filter) {
        return departmentConfigDao.countByFilter(filter);
    }

    @Override
    public List<DepartmentConfig> findAllByKppAndOktmo(String kpp, String oktmo) {
        return departmentConfigDao.findAllByKppAndOktmo(kpp, oktmo);
    }

    @Override
    public List<DepartmentConfig> findAllByKppAndOktmoAndFilter(String kpp, String oktmo, ConsolidationSourceDataSearchFilter filter) {
        return departmentConfigDao.findAllByKppAndOktmoAndFilter(kpp, oktmo, filter);
    }

    @Override
    public PagingResult<KppSelect> findAllKppByDepartmentIdAndKppContaining(int departmentId, String kpp, PagingParams pagingParams) {
        return departmentConfigDao.findAllKppByDepartmentIdAndKppContaining(departmentId, kpp, pagingParams);
    }

    @Override
    public PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(KppOktmoPairFilter filter, PagingParams pagingParams) {
        return departmentConfigDao.findAllKppOktmoPairsByFilter(filter, pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#departmentConfig, T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).CREATE)")
    public ActionResult createForGui(DepartmentConfig departmentConfig) {
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        try {
            create(departmentConfig, logger);
        } catch (ServiceException e) {
            throw new ServiceException("Невозможно создать настройку подразделения. " + e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при создании настройки подразделения для \"%s\" с параметрами: КПП: \"%s\", ОКТМО: \"%s\", дата начала действия настройки: " +
                            "\"%s\". Обратитесь к Администратору Системы или повторите операцию позднее.",
                    departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate())), e);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public void create(DepartmentConfig departmentConfig, Logger logger) {
        LOG.info("create: " + departmentConfig);
        List<DepartmentConfig> relatedDepartmentConfigs = findAllByKppAndOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
        checkDepartmentConfig(departmentConfig, relatedDepartmentConfigs);

        departmentConfigDao.create(departmentConfig);
        DepartmentConfig prev = departmentConfigDao.findPrevById(departmentConfig.getId());
        if (prev != null) {
            departmentConfigDao.updateEndDate(prev.getId(), addDays(departmentConfig.getStartDate(), -1));
        }
        logger.info("Для подразделения \"%s\" создана настройка КПП: \"%s\", ОКТМО: \"%s\", настройка действительна с \"%s\" по \"%s\"",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
    }

    @Override
    @PreAuthorize("hasPermission(#departmentConfig, T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).UPDATE)")
    public ActionResult updateForGui(DepartmentConfig departmentConfig) {
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        try {
            update(departmentConfig, logger);
        } catch (ServiceException e) {
            throw new ServiceException("Невозможно сохранить настройку подразделения. " + e.getMessage());
        } catch (Exception e) {
            throw new ServiceException(String.format("Ошибка при сохранении настройки подразделения для \"%s\" с параметрами: КПП: \"%s\", ОКТМО: \"%s\", дата начала действия настройки: " +
                            "\"%s\". Обратитесь к Администратору Системы или повторите операцию позднее.",
                    departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate())), e);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public void update(DepartmentConfig departmentConfig, Logger logger) {
        LOG.info("updateForGui: id=" + departmentConfig.getId() + ", kpp=" + departmentConfig.getKpp() + ", oktmo=" + departmentConfig.getOktmo().getCode());
        DepartmentConfig departmentConfigBeforeUpdate = findById(departmentConfig.getId());
        List<DepartmentConfig> relatedDepartmentConfigs = findAllByKppAndOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
        checkDepartmentConfig(departmentConfig, relatedDepartmentConfigs);

        DepartmentConfig prevOld = departmentConfigDao.findPrevById(departmentConfig.getId());
        departmentConfigDao.update(departmentConfig);
        if (prevOld != null) {
            DepartmentConfig newNextOfPrevOld = departmentConfigDao.findNextById(prevOld.getId());
            if (newNextOfPrevOld != null) {
                departmentConfigDao.updateEndDate(prevOld.getId(), addDays(newNextOfPrevOld.getStartDate(), -1));
            }
        }
        DepartmentConfig prevNew = departmentConfigDao.findPrevById(departmentConfig.getId());
        if (prevNew != null) {
            departmentConfigDao.updateEndDate(prevNew.getId(), addDays(departmentConfig.getStartDate(), -1));
        }
        if (departmentConfig.getEndDate() != null && !departmentConfig.getEndDate().equals(departmentConfigBeforeUpdate.getEndDate())) {
            DepartmentConfig next = departmentConfigDao.findNextById(departmentConfig.getId());
            if (next != null) {
                departmentConfigDao.updateStartDate(next.getId(), addDays(departmentConfig.getEndDate(), 1));
            }
        }
        logger.info("В подразделении \"%s\" изменена настройка КПП: \"%s\", ОКТМО: \"%s\", настройка действительна с \"%s\" по \"%s\"",
                departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
    }

    @Override
    @PreAuthorize("hasPermission(#ids, 'com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig', T(com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission).DELETE)")
    public ActionResult deleteForGui(List<Long> ids) {
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        for (long id : ids) {
            DepartmentConfig departmentConfig = findById(id);
            try {
                delete(departmentConfig, logger);
            } catch (ServiceException e) {
                LOG.error(e.getMessage());
                logger.error(e.getMessage());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                logger.error(String.format("Возникла ошибка при удалении настройки подразделения \"%s\", КПП: \"%s\", ОКТМО: \"%s\", период актуальности с " +
                                "\"%s\" по \"%s\". %s",
                        departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                        FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                        departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                        e.getMessage()));
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public void delete(DepartmentConfig departmentConfig, Logger logger) {
        LOG.info("delete: id=" + departmentConfig.getId());
        DepartmentConfig prev = departmentConfigDao.findPrevById(departmentConfig.getId());
        DepartmentConfig next = prev != null ? departmentConfigDao.findNextById(departmentConfig.getId()) : null;
        departmentConfigDao.deleteById(departmentConfig.getId());

        if (prev != null) {
            prev.setEndDate(next != null ? addDays(next.getStartDate(), -1) : null);
            departmentConfigDao.updateEndDate(prev.getId(), prev.getEndDate());
            logger.info("При удалении настройки подразделения \"" + departmentConfig.getDepartment().getName() + "\", " +
                    "КПП: " + departmentConfig.getKpp() + ", ОКТМО: " + departmentConfig.getOktmo().getCode() + ", период актуальности " +
                    "с \"" + FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()) +
                    "\" по \"" + (departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate())) +
                    "\" для другой настройки с той же парой КПП/ОКТМО и датой начала действия \"" + FastDateFormat.getInstance("dd.MM.yyyy").format(prev.getStartDate()) +
                    "\", дата окончания действия установлена в значение \"" +
                    (prev.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(prev.getEndDate())) + "\".");
        } else {
            logger.info("В подразделении \"%s\" удалена настройка КПП: \"%s\", ОКТМО: \"%s\", период актуальности с \"%s\" по \"%s\"",
                    departmentConfig.getDepartment().getName(), departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                    departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()));
        }
    }

    @Override
    @Transactional
    public void deleteByDepartmentId(int departmentId) {
        departmentConfigDao.deleteByDepartmentId(departmentId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EXPORT_DEPARTMENT_CONFIG)")
    public ActionResult createTaskToCreateExcel(DepartmentConfigsFilter filter, PagingParams pagingParams, TAUserInfo userInfo) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        AsyncTaskType taskType = AsyncTaskType.EXCEL_DEPARTMENT_CONFIGS;

        Department department = departmentService.getDepartment(filter.getDepartmentId());
        if (isEmpty(department.getShortName())) {
            logger.error("При формировании файла выгрузки настроек подразделения произошла ошибка. " +
                    "Обратитесь к Администратору Системы или повторите операцию позднее. Описание ошибки: Сокращенное наименование не может быть пустым");
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("departmentId", filter.getDepartmentId());

            String keyTask = "EXCEL_DEPARTMENT_CONFIGS_" + System.currentTimeMillis();
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
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).IMPORT_DEPARTMENT_CONFIG)")
    public ImportDepartmentConfigsResult createTaskToImportExcel(ImportDepartmentConfigsAction action, TAUserInfo userInfo) {
        Logger logger = new Logger();
        ImportDepartmentConfigsResult result = new ImportDepartmentConfigsResult();
        AsyncTaskType taskType = AsyncTaskType.IMPORT_DEPARTMENT_CONFIGS;
        TAUser user = userInfo.getUser();

        if (action.getFileSize() == 0) {
            logger.error("Загружаемый файл пуст. Загружать в Систему пустые файлы настроек подразделений запрещено.");
        } else {
            // Извлечение id подразделения из названия файла и валидация названия файла
            Integer fileNameDepartmentId = parseDepartmentIdFromFileName(action.getFileName(), logger);
            if (fileNameDepartmentId != null) {
                // Валидация id подразделения
                Department fileNameDepartment;
                if (!departmentService.existDepartment(fileNameDepartmentId) ||
                        (fileNameDepartment = departmentService.getDepartment(fileNameDepartmentId)).getType() != DepartmentType.TERR_BANK) {
                    logger.error("Не найден территориальный банк с идентификатором (" + fileNameDepartmentId + "), указанным в имени файла \"" + action.getFileName() + "\"");
                } else {
                    int userTbId = departmentService.getParentTBId(user.getDepartmentId());
                    if (!(user.hasRole(TARole.N_ROLE_CONTROL_UNP) ||
                            user.hasRole(TARole.N_ROLE_CONTROL_NS) && userTbId == fileNameDepartmentId)) {
                        logger.error("У вас отсутствуют права загрузки настроек подразделения " + departmentService.getDepartment(fileNameDepartmentId).getShortName() + ".");
                    } else {
                        if (action.getDepartmentId() != null && !action.isSkipDepartmentCheck() && fileNameDepartmentId != action.getDepartmentId()) {
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
                    }
                }
            }
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

    private Integer parseDepartmentIdFromFileName(String fileName, Logger logger) {
        Integer departmentId = null;
        Matcher matcher = Pattern.compile("(\\d*)_.*_\\d{12}\\.xlsx").matcher(fileName);
        if (matcher.matches()) {
            departmentId = Integer.valueOf(matcher.group(1));
        } else {
            logger.error("Неверное имя файла \"" + fileName + "\".");
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
                if (overlaps(relatedDepartmentConfig.getStartDate(), relatedDepartmentConfig.getEndDate(),
                        departmentConfig.getStartDate(), departmentConfig.getEndDate())) {
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
        // Проверяемая настройка не должна пересекаться с существующими
        if (!overlappingDepartmentConfigs.isEmpty()) {
            throw new ServiceException("Для настройки подразделения \"%s\" с КПП: \"%s\", ОКТМО: \"%s\" период действия с \"%s\" по \"%s\" " +
                    "пересекается с периодом действия уже существующих настроек с теми же КПП и ОКТМО: %s",
                    firstNonNull(departmentConfig.getDepartment().getShortName(), departmentConfig.getDepartment().getName()),
                    departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                    departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                    makeOverlappingDepartmentConfigsString(overlappingDepartmentConfigs));
        }
        // Проверяемая настройка не должна создавать разрывы между версиями
        if (!(minDate == null ||
                overlaps(minDate, maxDate,
                        departmentConfig.getStartDate(), departmentConfig.getEndDate()) ||
                addDays(minDate, -1).equals(departmentConfig.getEndDate()) ||
                addDays(departmentConfig.getStartDate(), -1).equals(maxDate))) {
            throw new ServiceException("Между периодом актуальности настройки подразделения КПП: \"%s\", ОКТМО: \"%s\" период актуальности с \"%s\" по \"%s\" " +
                    "в подразделении: \"%s\" и другими настройками с такими же КПП и ОКТМО имеется временной разрыв более 1 календарного дня.",
                    departmentConfig.getKpp(), departmentConfig.getOktmo().getCode(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getStartDate()),
                    departmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(departmentConfig.getEndDate()),
                    departmentConfig.getDepartment().getName());
        }
    }

    private boolean overlaps(Date from1, Date to1, Date from2, Date to2) {
        return !(to1 != null && to1.before(from2) || to2 != null && to2.before(from1));
    }

    private String makeOverlappingDepartmentConfigsString(List<DepartmentConfig> overlappingDepartmentConfigs) {
        List<String> overlappingDepartmentConfigsStrings = new ArrayList<>(overlappingDepartmentConfigs.size());
        for (DepartmentConfig overlappingDepartmentConfig : overlappingDepartmentConfigs) {
            overlappingDepartmentConfigsStrings.add(String.format(
                    "[\"%s\", период действия с \"%s\" по \"%s\"]",
                    firstNonNull(overlappingDepartmentConfig.getDepartment().getShortName(), overlappingDepartmentConfig.getDepartment().getName()),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingDepartmentConfig.getStartDate()),
                    overlappingDepartmentConfig.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingDepartmentConfig.getEndDate())));
        }
        return Joiner.on(", ").join(overlappingDepartmentConfigsStrings);
    }
}
