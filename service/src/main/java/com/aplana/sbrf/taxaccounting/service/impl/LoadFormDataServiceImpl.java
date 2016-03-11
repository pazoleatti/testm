package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class LoadFormDataServiceImpl extends AbstractLoadTransportDataService implements LoadFormDataService {

	private static final Log LOG = LogFactory.getLog(LoadFormDataServiceImpl.class);
    private static final String LOCK_MSG = "Обработка данных транспортного файла не выполнена, " +
            "т.к. в данный момент выполняется изменение данных формы \"%s\" " +
            "для подразделения \"%s\" " +
            "в периоде \"%s\", " +
            "инициированное пользователем \"%s\" " +
            "в %s";
    private static final SimpleDateFormat SDF_HH_MM_DD_MM_YYYY = new SimpleDateFormat("HH:mm dd.MM.yyyy");

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private FormDataDao formDataDao;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private SignService signService;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private TAUserService userService;
    @Autowired
    private LogBusinessService logBusinessService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, List<Integer> departmentIdList,
                                        List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync) {
        log(userInfo, LogData.L1, logger, lockId);
        ImportCounter importCounter = new ImportCounter();
        if (departmentIdList != null) {
            // По каталогам загрузки ТБ
            List<Integer> tbList = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
            for (Object departmentIdObj : CollectionUtils.intersection(departmentIdList, tbList)) {
                int departmentId = (Integer) departmentIdObj;
                try {
                    importCounter.add(importDataFromFolder(userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId,
                            loadedFileNameList, logger, lockId, isAsync));
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error("Ошибка при загрузке транспортных файлов подразделения «%s». %s", departmentService.getDepartment(departmentId).getName(), e.getMessage());
                }
            }
        }
        log(userInfo, LogData.L2, logger, lockId, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    @Override
    public List<TransportFileInfo> getFormDataFiles(TAUserInfo userInfo, List<Integer> departmentIdList,
                                        List<String> loadedFileNameList, Logger logger) {
        List<TransportFileInfo> fileList = new ArrayList<TransportFileInfo>();
        if (departmentIdList != null) {
            // По каталогам загрузки ТБ
            List<Integer> tbList = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
            for (Object departmentIdObj : CollectionUtils.intersection(departmentIdList, tbList)) {
                int departmentId = (Integer) departmentIdObj;
                try {
                    getFormDataTBFiles(fileList, userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId,
                            loadedFileNameList, logger, "");
                } catch (Exception e) {
                }
            }
        }
        return fileList;
    }

    @Override
    public List<Integer> getTB(TAUserInfo userInfo, Logger logger) {
        // Выборка подразделений http://conf.aplana.com/pages/viewpage.action?pageId=13111363
        List<Integer> departmentList = departmentService.getTaxFormDepartments(userInfo.getUser(),
                Arrays.asList(TaxType.INCOME), null, null);

        List<Department> departmentTBList = new LinkedList<Department>();
        if (departmentList != null) {
            Collection<Department> departments = departmentService.getRequiredForTreeDepartments(
                    new HashSet(departmentList)).values();
            for (Department department : departments) {
                if (department.getType() == DepartmentType.TERR_BANK) {
                    departmentTBList.add(department);
                }
            }
        }

        List<Integer> departmentTBIdList = new ArrayList<Integer>(departmentTBList.size());

        if (departmentTBList.isEmpty()) {
            logger.info("Не определено доступных для пользователя ТБ.");
        }

        if (departmentTBList.size() == 1) {
            departmentTBIdList.add( departmentTBList.get(0).getId());
            logger.info("Определен доступный для пользователя ТБ: «%s».", departmentTBList.get(0).getName());
        }

        if (departmentTBList.size() > 1) {
            List<String> names = new ArrayList<String>(departmentTBList.size());
            for (Department department : departmentTBList) {
                departmentTBIdList.add(department.getId());
                names.add("«" + department.getName() + "»");
            }
            logger.info("Определены доступные для пользователя ТБ: %s.", StringUtils.join(names, ", "));
        }
        return departmentTBIdList;
    }

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync) {
        return importFormData(userInfo, departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode()), null, logger, lock, isAsync);
    }

    /**
     * Загрузка всех ТФ НФ из указанного каталога загрузки
     */
    private ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam param, Integer departmentId,
                                               List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync) {
        String path = getUploadPath(userInfo, param, departmentId, logger, lockId);
        if (path == null) {
            // Ошибка получения пути
            return new ImportCounter();
        }
        int success = 0;
        int fail = 0;
        // Набор файлов, которые уже обработали
        Set<String> ignoreFileSet = new HashSet<String>();
        ImportCounter wrongImportCounter = new ImportCounter();
        String departmentName = departmentService.getDepartment(departmentId).getName();

        // Проверка каталогов, указанных в параметрах "Путь к каталогу загрузки", "Путь к каталогу архива" и "Путь к каталогу ошибок" для ТБ, на наличие доступа
        String archivePath = getFormDataArchivePath(userInfo, departmentId, logger, lockId);
        String errorPath = getFormDataErrorPath(userInfo, departmentId, logger, lockId);
        List<String> pathList = new ArrayList<String>();
        if (!checkPath(path)) {
            pathList.add("к каталогу загрузки «" + path + "»");
        }
        if (!checkPath(archivePath)) {
            pathList.add("к каталогу архива «" + archivePath + "»");
        }
        if (!checkPath(errorPath)) {
            pathList.add("к каталогу ошибок «" + errorPath + "»");
        }
        if (!pathList.isEmpty()) {
            log(userInfo, LogData.L42, logger, lockId, StringUtils.join(pathList, ", "), departmentName);
            return wrongImportCounter;
        }

        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        List<String> workFilesList = getWorkTransportFiles(userInfo, path, ignoreFileSet, loadedFileNameList, logger, wrongImportCounter, lockId, true);
        if (workFilesList.isEmpty()) {
            if (wrongImportCounter.getFailCounter() == 0) log(userInfo, LogData.L3, logger, lockId, departmentName);
            return wrongImportCounter;
        }

        long maxFileSize = 0;
        if (isAsync) {
            maxFileSize = asyncTaskTypeDao.get(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId()).getTaskLimit();
        }
        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        for (String fileName : workFilesList) {
            ignoreFileSet.add(fileName);
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);

            if (maxFileSize != 0 && currentFile.length() / 1024 > maxFileSize) {
                log(userInfo, LogData.L47, logger, lockId, fileName, currentFile.length() / 1024, path, maxFileSize);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L4.getText(), lockId, fileName, path))), logger, lockId);
                fail++;
                continue;
            }
            // Блокировка файла
            LockData fileLock = lockDataService.lock(LockData.LockObjects.FILE.name() + "_" + fileName,
                    userInfo.getUser().getId(),
                    String.format(LockData.DescriptionTemplate.FILE.getText(), fileName));
            if (fileLock != null) {
                log(userInfo, LogData.L41, logger, lockId, fileName);
                fail++;
                continue;
            }

            try {
                // Обработка файла
                TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
                String formCode = transportDataParam.getFormCode();
                String reportPeriodCode = transportDataParam.getReportPeriodCode();
                Integer year = transportDataParam.getYear();
                String departmentCode = transportDataParam.getDepartmentCode();

                // Указан несуществующий код налоговой формы
                FormType formType = formTypeService.getByCode(formCode);
                if (formType == null) {
                    log(userInfo, LogData.L6, logger, lockId, fileName);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L6.getText(), lockId, fileName))), logger, lockId);
                    fail++;
                    continue;
                }

                formTypeId = formType.getId();

                // Указан недопустимый код периода
                ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
                if (reportPeriod == null) {
                    log(userInfo, LogData.L7, logger, lockId, fileName);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L7.getText(), lockId, fileName))), logger, lockId);
                    fail++;
                    continue;
                }

                // Актуальный шаблон НФ, введенный в действие
                Integer formTemplateId;
                try {
                    formTemplateId = formTemplateService.getActiveFormTemplateId(formType.getId(), reportPeriod.getId());
                } catch (Exception e) {
                    // Если шаблона нет, то не загружаем ТФ
                    log(userInfo, LogData.L21, logger, lockId, e.getMessage());
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L21.getText(), lockId, e.getMessage()))), logger, lockId);
                    fail++;
                    continue;
                }

                FormTemplate formTemplate = formTemplateService.get(formTemplateId);

                if (!TAAbstractScriptingServiceImpl.canExecuteScript(formTemplate.getScript(), FormDataEvent.IMPORT_TRANSPORT_FILE)) {
                    log(userInfo, LogData.L48, logger, lockId, fileName);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L48.getText(), logger, lockId, fileName))), logger, lockId);
                    fail++;
                    continue;
                }

                // Подразделение НФ
                Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode);
                formDepartmentId = formDepartment != null ? formDepartment.getId(): null;

                // Не задан код подразделения или код формы
                if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
                    log(userInfo, LogData.L4, logger, lockId, fileName, path);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L4.getText(), lockId, fileName, path))), logger, lockId);
                    fail++;
                    continue;
                }

                // ЭЦП
                List<String> signList = configurationDao.getByDepartment(0).get(ConfigurationParam.SIGN_CHECK, 0);
                if (signList != null && !signList.isEmpty() && SignService.SIGN_CHECK.equals(signList.get(0))) {
                    Pair<Boolean, Set<String>> check = new Pair<Boolean, Set<String>>(false, new HashSet<String>());
                    try {
                        check = signService.checkSign(fileName, currentFile.getPath(), 0, logger);
                    } catch (Exception e) {
                        log(userInfo, LogData.L36, logger, lockId, e.getMessage());
                    }
                    if (!check.getFirst()) {
                        for(String msg: check.getSecond())
                            log(userInfo, LogData.L0_ERROR, logger, lockId, msg);
                        log(userInfo, LogData.L16, logger, lockId, fileName);
                        moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                                Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L16.getText(), lockId, fileName))), logger, lockId);
                        fail++;
                        continue;
                    }
                    for(String msg: check.getSecond())
                        log(userInfo, LogData.L0_INFO, logger, lockId, msg);
                    log(userInfo, LogData.L15, logger, lockId, fileName);
                } else {
                    log(userInfo, LogData.L15_1, logger, lockId, fileName);
                }

                log(userInfo, LogData.L15_FD, logger, lockId, fileName);
                if (transportDataParam.getMonth() == null) {
                    log(userInfo, LogData.L15_RP, logger, lockId, getFileNamePart(formCode), getFileNamePart(departmentCode),
                            getFileNamePart(reportPeriodCode), getFileNamePart(year));
                } else {
                    log(userInfo, LogData.L15_M, logger, lockId, getFileNamePart(formCode), getFileNamePart(departmentCode),
                            getFileNamePart(reportPeriodCode), getFileNamePart(year),
                            getFileNamePart(transportDataParam.getMonth()));
                }

                // Указан несуществующий код подразделения
                if (formDepartment == null) {
                    log(userInfo, LogData.L5, logger, lockId, fileName);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L5.getText(), lockId, fileName))), logger, lockId);
                    fail++;
                    continue;
                }

                FormDataKind formDataKind = FormDataKind.PRIMARY;

                // Назначение подразделению типа и вида НФ
                boolean existedPrimaryAssigning = departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY);
                boolean existedAdditionalAssigning = departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.ADDITIONAL);
                // если нет назначения на первичную и выходную, то ошибка
                if (!existedPrimaryAssigning && !existedAdditionalAssigning) {
                    log(userInfo, LogData.L14, logger, lockId, formType.getName(), formDepartment.getName());
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L14.getText(),
                                    lockId, formType.getName(), formDepartment.getName()))), logger, lockId);
                    fail++;
                    continue;
                } else if (!existedPrimaryAssigning) {
                    // иначе если нет назначения на первичную, то ищем выходную
                    formDataKind = FormDataKind.ADDITIONAL;
                }

                // Последний отчетный период подразделения для указанного отчетного периода
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.getLast(formDepartment.getId(),
                        reportPeriod.getId());

                // Открытость периода
                if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
                    String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                    log(userInfo, LogData.L9, logger, lockId, formType.getName(), reportPeriodName);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L9.getText(),
                                    lockId, formType.getName(), reportPeriodName))), logger, lockId);
                    fail++;
                    continue;
                }

                if (departmentReportPeriod.getCorrectionDate() != null) {
                    String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                    log(userInfo, LogData.L8, logger, lockId, formType.getName(), reportPeriodName);
                }

                // Поиск экземпляра НФ
                FormData formData;

                // Признак ежемесячной формы по файлу
                boolean monthly = transportDataParam.getMonth() != null;

                if (monthly != formTemplate.isMonthly()) {
                    log(userInfo, LogData.L4, logger, lockId, fileName, path);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L4.getText(), lockId, fileName, path))), logger, lockId);
                    fail++;
                    continue;
                }

                formData = formDataDao.find(formType.getId(), formDataKind, departmentReportPeriod.getId().intValue(),
                        monthly ? transportDataParam.getMonth() : null,
                        null, false);

                // Экземпляр уже есть и не в статусе «Создана»
                if (formData != null && formData.getState() != WorkflowState.CREATED) {
                    // Сообщение об ошибке в общий лог и в файл со списком ошибок
                    Logger localLogger = new Logger();
                    localLogger.error(String.format(LogData.L17.getText(), lockId, formType.getName(), formDepartment.getName()));
                    log(userInfo, LogData.L17, logger, lockId, formType.getName(), formDepartment.getName());
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            localLogger.getEntries(), logger, lockId);
                    fail++;
                    continue;
                }

                // флаг того что форма уже создана
                if (formData != null) {
                    // 13А.1 Существует и имеет статус "Создана"
                    log(userInfo, LogData.L13, logger, lockId);
                }

                // Загрузка данных в НФ (скрипт)
                Logger localLogger = new Logger();
                boolean result;
                try {
                    // Загрузка
                    result = importFormData(userInfo, departmentId, currentFile, formData, formType, formTemplate,
                            departmentReportPeriod, formDataKind, transportDataParam, localLogger, lockId);

                    // Вывод скрипта в область уведомлений
                    logger.getEntries().addAll(localLogger.getEntries());
                } catch (Exception e) {
                    // Вывод скрипта в область уведомлений
                    logger.getEntries().addAll(localLogger.getEntries());
                    // Вывод в область уведомленеий и ЖА
                    log(userInfo, LogData.L21, logger, lockId, e.getMessage());
                    // Перемещение в каталог ошибок
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            localLogger.getEntries(), logger, lockId);
                    fail++;
                    continue;
                }

                if (result) {
                    success++;
                } else {
                    fail++;
                }
            } finally {
                lockDataService.unlock(LockData.LockObjects.FILE.name() + "_" + fileName, userInfo.getUser().getId(), true);
            }
        }

        return new ImportCounter(success, fail + wrongImportCounter.getFailCounter());
    }

    private void getFormDataTBFiles(List<TransportFileInfo> fileList, TAUserInfo userInfo, ConfigurationParam param, Integer departmentId,
                                               List<String> loadedFileNameList, Logger logger, String lockId) {
        String path = getUploadPath(userInfo, param, departmentId, logger, lockId);
        if (path == null) {
            // Ошибка получения пути
            return;
        }

        // Проверка каталогов, указанных в параметрах "Путь к каталогу загрузки", "Путь к каталогу архива" и "Путь к каталогу ошибок" для ТБ, на наличие доступа
        String archivePath = getFormDataArchivePath(userInfo, departmentId, logger, lockId);
        String errorPath = getFormDataErrorPath(userInfo, departmentId, logger, lockId);
        if (!checkPath(path) || !checkPath(archivePath) || !checkPath(errorPath)) {
            return;
        }


        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        List<String> workFilesList = getWorkTransportFiles(userInfo, path, new HashSet<String>(), loadedFileNameList, logger, new ImportCounter(), lockId, false);
        if (workFilesList.isEmpty()) {
            return;
        }

        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        for (String fileName : workFilesList) {
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);
            fileList.add(new TransportFileInfo(currentFile.getName(), path, currentFile.length() / 1024));
        }
    }

    private boolean checkPath(String path) {
        if (path == null || !FileWrapper.canReadFolder(path + "/") || !FileWrapper.canWriteFolder(path + "/"))
            return false;
        try {
            ResourceUtils.getSharedResource(path + "/");
        } catch (Exception e) {
			LOG.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Загрузка ТФ конкретной НФ. Только этот метод в сервисе транзакционный.
     */
    private boolean importFormData(TAUserInfo userInfo, int departmentId, FileWrapper currentFile, FormData formData,
                                   FormType formType,
                                   FormTemplate formTemplate, DepartmentReportPeriod departmentReportPeriod,
                                   FormDataKind formDataKind, TransportDataParam transportDataParam,
                                   Logger localLogger, String lock) {
        String reportPeriodName = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + " - "
                + departmentReportPeriod.getReportPeriod().getName();

        boolean success = false;
        boolean formWasCreated = formData != null;
        // Если формы нет, то создаем
        if (formData == null) {
            // Если форма не ежемесячная, то месяц при созданнии не указывается
            Integer month = transportDataParam.getMonth();
            if (formTemplate != null && !formTemplate.isMonthly()) {
                month = null;
            }
            int formTemplateId = formTemplateService.getActiveFormTemplateId(formType.getId(),
                    departmentReportPeriod.getReportPeriod().getId());
            long formDataId = formDataService.createFormData(localLogger, userInfo, formTemplateId,
                    departmentReportPeriod.getId(), null, false, formDataKind, month, true);
            formData = formDataDao.get(formDataId, false);
        } else {
            // 17А.2Б
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(formData.getId());
            if (lockType != null) {
                ReportType reportType = lockType.getFirst();
                List<ReportType> blockReportTypes = Arrays.asList(ReportType.EDIT_FD, ReportType.REFRESH_FD, ReportType.CALCULATE_FD, ReportType.IMPORT_FD, ReportType.MOVE_FD);
                if (blockReportTypes.contains(reportType)) {
                    log(userInfo, LogData.L40, localLogger,
                            lock, formDataService.getTaskName(reportType, formData.getId(), userInfo), userService.getUser(lockType.getSecond().getUserId()).getName(), SDF_HH_MM_DD_MM_YYYY.format(lockType.getSecond().getDateLock()));
                    // Переносим файл в каталог ошибок
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, localLogger, lock), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), lock, ""))), localLogger, lock);
                    return false;
                }
            }
            // 17А.4.А.1 Система снимает обнаруженные блокировки с задачи на формирование XLSM/CSV отчета и/или проверки НФ
            // 17А.5 Система инициирует выполнение сценария Удаление отчета НФ для экземпляра НФ, данные которой были перезаполнены.
            formDataService.interruptTask(formData.getId(), null, userInfo.getUser().getId(), ReportType.IMPORT_TF_FD, "Изменены данные налоговой формы путем загрузки транспортного файла");

            formData.initFormTemplateParams(formTemplate);
        }

        // Блокировка
        LockData lockData = lockDataService.lock(formDataService.generateTaskKey(formData.getId(), ReportType.IMPORT_TF_FD),
                userInfo.getUser().getId(),
                formDataService.getFormDataFullName(formData.getId(), false, currentFile.getName(), ReportType.IMPORT_TF_FD));
        if (lockData != null)
            throw new ServiceException(String.format(
                    LOCK_MSG,
                    formData.getKind().getTitle() + ": " + formData.getFormType().getName(),
                    departmentService.getDepartment(formData.getDepartmentId()).getName(),
                    reportPeriodName,
                    userService.getUser(lockData.getUserId()).getName(),
                    SDF_HH_MM_DD_MM_YYYY.format(lockData.getDateLock())
            ));

        try {
            // 15 Скрипт
            InputStream inputStream = currentFile.getInputStream();
            try {
                formDataService.importFormData(localLogger, userInfo, formData.getId(), formData.isManual(), inputStream,
                        currentFile.getName(), FormDataEvent.IMPORT_TRANSPORT_FILE);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            // Если при выполнении скрипта возникли фатальные ошибки, то
            if (localLogger.containsLevel(LogLevel.ERROR)) {
                if (!formWasCreated) {
                    formDataDao.delete(formData.getId());
                }
                // Исключение для отката транзакции сознания и заполнения НФ
                throw new ServiceException("При выполнении загрузки произошли ошибки");
            } else {
                if (!formWasCreated) {
                    logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.CREATE, null);
                    auditService.add(FormDataEvent.CREATE, userInfo, null, formData, "Форма создана", null);
                }
            }

            Department formDepartment = departmentDao.getDepartment(departmentReportPeriod.getDepartmentId());

            // 16 если форма не была создана
            if (!formWasCreated) {
                log(userInfo, LogData.L18, localLogger, lock, formDataKind.getTitle(), formType.getName(), formDepartment.getName(), reportPeriodName);
            } else {
                // 13А.2 НФ корректно заполнена значениями из ТФ.
                log(userInfo, LogData.L19, localLogger, lock, formDataKind.getTitle(), formType.getName(), formDepartment.getName(), reportPeriodName);
            }
            // 17 Перенос в архив
            if (moveToArchiveDirectory(userInfo, getFormDataArchivePath(userInfo, departmentId, localLogger, lock), currentFile, localLogger, lock)) {
                // 18 Сохранение
                formDataService.saveFormData(localLogger, userInfo, formData, false);

                success = true;
            } else {
                // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, localLogger, lock), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), lock, ""))), localLogger, lock);
            }
        } finally {
            // Снимаем блокировку
            lockDataService.unlock(formDataService.generateTaskKey(formData.getId(), ReportType.IMPORT_TF_FD), userInfo.getUser().getId());
            if (localLogger.containsLevel(LogLevel.ERROR) && !formWasCreated) {
                formDataDao.delete(formData.getId());
            }
        }

        // 20 Загрузка формы завершена
        log(userInfo, LogData.L20, localLogger, lock, currentFile.getName());
        return success;
    }

    /**
     * Путь к каталогу из ConfigurationParam
     */
    private String getPath(TAUserInfo userInfo, ConfigurationParam param, int formDepartmentId, LogData logData, Logger logger, String lock) {
        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
        List<String> pathList = null;
        if (model != null) {
            pathList = model.get(param, formDepartmentId);
        }
        // Проверка наличия каталога в параметрах
        if (pathList == null || pathList.isEmpty()) {
            if (lock != null && !lock.isEmpty()) log(userInfo, logData, logger, lock);
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок НФ
     */
    private String getFormDataErrorPath(TAUserInfo userInfo, int formDepartmentId, Logger logger, String lock) {
        return getPath(userInfo, ConfigurationParam.FORM_ERROR_DIRECTORY, formDepartmentId,
                LogData.L_1, logger, lock);
    }

    /**
     * Путь к каталогу архива НФ
     */
    private String getFormDataArchivePath(TAUserInfo userInfo, int formDepartmentId, Logger logger, String lock) {
        return getPath(userInfo, ConfigurationParam.FORM_ARCHIVE_DIRECTORY, formDepartmentId,
                LogData.L_2, logger, lock);
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(TAUserInfo userInfo, ConfigurationParam configurationParam, int departmentId, Logger logger, String lock) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            if (lock != null && !lock.isEmpty()) log(userInfo, LogData.L30, logger, lock, departmentService.getDepartment(departmentId).getName());
            return null;
        }
        return uploadPathList.get(0);
    }

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    private List<String> getWorkTransportFiles(TAUserInfo userInfo, String folderPath, Set<String> ignoreFileSet,
                                               List<String> loadedFileNameList, Logger logger, ImportCounter wrongImportCounter, String lock, boolean validateFileName) {
        List<String> retVal = new LinkedList<String>();
        FileWrapper catalogFile = ResourceUtils.getSharedResource(folderPath + "/");
        for (String candidateStr : catalogFile.list()) {
            if (ignoreFileSet != null && ignoreFileSet.contains(candidateStr)) {
                continue;
            }

            if (loadedFileNameList != null && !loadedFileNameList.contains(candidateStr)) {
                // Если задан список определенных имен, а имя не из списка, то не загружаем такой файл
                continue;
            }

            // Это файл, а не директория и соответствует формату имени ТФ
            FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + "/" + candidateStr);
            if (candidateFile.isFile()) {
                if (!validateFileName || TransportDataParam.isValidName(candidateStr)) {
                    retVal.add(candidateStr);
                } else {
                    log(userInfo, LogData.L4, logger, lock, candidateStr, folderPath);
                    wrongImportCounter.add(new ImportCounter(0, 1));
                }
            }
        }
        return retVal;
    }

    /**
     * Вывод частей имени файла с учетом возможного null-значения
     */
    private String getFileNamePart(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }
}
