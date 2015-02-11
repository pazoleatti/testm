package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
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
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

    private static final String LOCK_MSG = "Обработка данных транспортного файла не выполнена, " +
            "т.к. в данный момент выполняется изменение данных формы %s " +
            "для подразделения %s " +
            "в периоде %s, " +
            "инициированное пользователем %s " +
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

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, List<Integer> departmentIdList,
                                        List<String> loadedFileNameList, Logger logger) {
        log(userInfo, LogData.L1, logger);
        ImportCounter importCounter = new ImportCounter();
        if (departmentIdList != null) {
            // По каталогам загрузки ТБ
            List<Integer> tbList = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
            for (Object departmentIdObj : CollectionUtils.intersection(departmentIdList, tbList)) {
                int departmentId = (Integer) departmentIdObj;
                importCounter.add(importDataFromFolder(userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId,
                        loadedFileNameList, logger));
            }
        }
        log(userInfo, LogData.L2, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
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
    public ImportCounter importFormData(TAUserInfo userInfo, Logger logger) {
        return importFormData(userInfo, departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode()), null, logger);
    }

    /**
     * Загрузка всех ТФ НФ из указанного каталога загрузки
     */
    private ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam param, Integer departmentId,
                                               List<String> loadedFileNameList, Logger logger) {
        String path = getUploadPath(userInfo, param, departmentId, logger);
        if (path == null) {
            // Ошибка получения пути
            return new ImportCounter();
        }
        int success = 0;
        int fail = 0;
        // Набор файлов, которые уже обработали
        Set<String> ignoreFileSet = new HashSet<String>();
        ImportCounter wrongImportCounter = new ImportCounter();
        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        List<String> workFilesList = getWorkTransportFiles(userInfo, path, ignoreFileSet, loadedFileNameList, logger, wrongImportCounter);
        if (workFilesList.isEmpty()) {
            log(userInfo, LogData.L3, logger, departmentService.getDepartment(departmentId).getName());
            return wrongImportCounter;
        }

        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        for (String fileName : workFilesList) {
            ignoreFileSet.add(fileName);
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);

            // Обработка файла
            TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
            String formCode = transportDataParam.getFormCode();
            String reportPeriodCode = transportDataParam.getReportPeriodCode();
            Integer year = transportDataParam.getYear();
            String departmentCode = transportDataParam.getDepartmentCode();

            // Подразделение НФ
            Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode);
            formDepartmentId = formDepartment != null ? formDepartment.getId(): null;

            // Не задан код подразделения или код формы
            if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
                log(userInfo, LogData.L4, logger, fileName, path);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L4.getText(), fileName, path))), logger);
                fail++;
                continue;
            }

            // ЭЦП
            List<String> signList = configurationDao.getByDepartment(0).get(ConfigurationParam.SIGN_CHECK, 0);
            if (signList != null && !signList.isEmpty() && signList.get(0).equals("1")) {
                boolean check = false;
                try {
                    check = signService.checkSign(currentFile.getPath(), 0);
                } catch (Exception e) {
                    log(userInfo, LogData.L36, logger, e.getMessage());
                }
                if (!check) {
                    log(userInfo, LogData.L16, logger, fileName);
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                            Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L16.getText())), logger);
                    fail++;
                    continue;
                }
                log(userInfo, LogData.L15, logger, fileName);
            } else {
                log(userInfo, LogData.L15_1, logger, fileName);
            }

            log(userInfo, LogData.L15_FD, logger, fileName);
            if (transportDataParam.getMonth() == null) {
                log(userInfo, LogData.L15_RP, logger, getFileNamePart(formCode), getFileNamePart(departmentCode),
                        getFileNamePart(reportPeriodCode), getFileNamePart(year));
            } else {
                log(userInfo, LogData.L15_M, logger, getFileNamePart(formCode), getFileNamePart(departmentCode),
                        getFileNamePart(reportPeriodCode), getFileNamePart(year),
                        getFileNamePart(transportDataParam.getMonth()));
            }

            // Указан несуществующий код подразделения
            if (formDepartment == null) {
                log(userInfo, LogData.L5, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L5.getText(), fileName))), logger);
                fail++;
                continue;
            }

            // Указан несуществующий код налоговой формы
            FormType formType = formTypeService.getByCode(formCode);
            if (formType == null) {
                log(userInfo, LogData.L6, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L6.getText(), fileName))), logger);
                fail++;
                continue;
            }

            formTypeId = formType.getId();

            FormDataKind formDataKind = FormDataKind.PRIMARY;

            // Назначение подразделению типа и вида НФ
            boolean existedPrimaryAssigning = departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY);
            boolean existedAdditionalAssigning = departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.ADDITIONAL);
            // если нет назначения на первичную и выходную, то ошибка
            if (!existedPrimaryAssigning && !existedAdditionalAssigning) {
                log(userInfo, LogData.L14, logger, formType.getName(), formDepartment.getName());
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L14.getText(),
                                formType.getName(), formDepartment.getName()))), logger);
                fail++;
                continue;
            } else if (!existedPrimaryAssigning) {
                // иначе если нет назначения на первичную, то ищем выходную
                formDataKind = FormDataKind.ADDITIONAL;
            }

            // Указан недопустимый код периода
            ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
            if (reportPeriod == null) {
                log(userInfo, LogData.L7, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L7.getText(), fileName))), logger);
                fail++;
                continue;
            }

            // Последний отчетный период подразделения для указанного отчетного периода
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.getLast(formDepartment.getId(),
                    reportPeriod.getId());

            // Открытость периода
            if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
                String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                log(userInfo, LogData.L9, logger, formType.getName(), reportPeriodName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L9.getText(),
                                formType.getName(), reportPeriodName))), logger);
                fail++;
                continue;
            }

            if (departmentReportPeriod.getCorrectionDate() != null) {
                String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                log(userInfo, LogData.L8, logger, formType.getName(), reportPeriodName);
            }

            // Поиск экземпляра НФ
            FormData formData;

            // Признак ежемесячной формы по файлу
            boolean monthly = transportDataParam.getMonth() != null;

            // Актуальный шаблон НФ, введенный в действие
            Integer formTemplateId;
            try {
                formTemplateId = formTemplateService.getActiveFormTemplateId(formType.getId(), reportPeriod.getId());
            } catch (Exception e) {
                // Если шаблона нет, то не загружаем ТФ
                log(userInfo, LogData.L21, logger, e.getMessage());
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L21.getText(), e.getMessage()))), logger);
                fail++;
                continue;
            }

            FormTemplate formTemplate = formTemplateService.get(formTemplateId);
            if (monthly != formTemplate.isMonthly()) {
                log(userInfo, LogData.L4, logger, fileName, path);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L4.getText(), fileName, path))), logger);
                fail++;
                continue;
            }

            formData = formDataDao.find(formType.getId(), formDataKind, departmentReportPeriod.getId().intValue(),
                    monthly ? transportDataParam.getMonth() : null);

            // Экземпляр уже есть и не в статусе «Создана»
            if (formData != null && formData.getState() != WorkflowState.CREATED) {
                // Сообщение об ошибке в общий лог и в файл со списком ошибок
                Logger localLogger = new Logger();
                localLogger.error(LogData.L17.getText());
                log(userInfo, LogData.L17, logger, formType.getName(), formDepartment.getName());
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            // флаг того что форма уже создана
            boolean formWasCreated = false;
            if (formData != null) {
                // 13А.1 Существует и имеет статус "Создана"
                formWasCreated = true;
                log(userInfo, LogData.L13, logger);
            }

            // Загрузка данных в НФ (скрипт)
            Logger localLogger = new Logger();
            boolean result;
            try {
                // Загрузка
                result = importFormData(userInfo, departmentId, currentFile, formData, formType, formTemplate,
                        departmentReportPeriod, formDataKind, transportDataParam, formWasCreated, localLogger);

                // Вывод скрипта в область уведомлений
                logger.getEntries().addAll(localLogger.getEntries());
            } catch (Exception e) {
                // Вывод скрипта в область уведомлений
                logger.getEntries().addAll(localLogger.getEntries());
                // Вывод в область уведомленеий и ЖА
                log(userInfo, LogData.L21, logger, e.getMessage());
                // Перемещение в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            if (result) {
                success++;
            } else {
                fail++;
            }
        }

        return new ImportCounter(success, fail + wrongImportCounter.getFailCounter());
    }

    /**
     * Загрузка ТФ конкретной НФ. Только этот метод в сервисе транзакционный.
     */
    @Transactional
    private boolean importFormData(TAUserInfo userInfo, int departmentId, FileWrapper currentFile, FormData formData,
                                   FormType formType,
                                   FormTemplate formTemplate, DepartmentReportPeriod departmentReportPeriod,
                                   FormDataKind formDataKind, TransportDataParam transportDataParam,
                                   boolean formWasCreated, Logger localLogger) {
        String reportPeriodName = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + " - "
                + departmentReportPeriod.getReportPeriod().getName();

        boolean success = false;
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
                    departmentReportPeriod.getId(), formDataKind, month);
            formData = formDataDao.get(formDataId, false);
        } else {
            formData.initFormTemplateParams(formTemplate);
        }

        // Блокировка
        LockData lockData = lockDataService.lock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId(),
                userInfo.getUser().getId(),
                lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA));
        if (lockData!=null)
            throw new ServiceException(String.format(
                    LOCK_MSG,
                    formData.getKind().getName() + ": " + formData.getFormType().getName(),
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
                // Исключение для отката транзакции сознания и заполнения НФ
                throw new ServiceException("При выполнении загрузки произошли ошибки");
            }

            Department formDepartment = departmentDao.getDepartment(departmentReportPeriod.getDepartmentId());

            // 16 если форма не была создана
            if (!formWasCreated) {
                log(userInfo, LogData.L18, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
            } else {
                // 13А.2 НФ корректно заполнена значениями из ТФ.
                log(userInfo, LogData.L19, localLogger, formDataKind.getName(), formType.getName(), formDepartment.getName(), reportPeriodName);
            }
            // 17 Перенос в архив
            if (moveToArchiveDirectory(userInfo, getFormDataArchivePath(userInfo, departmentId, localLogger), currentFile, localLogger)) {
                // 18 Сохранение
                formDataService.saveFormData(localLogger, userInfo, formData);

                success = true;
            } else {
                // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, localLogger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), ""))), localLogger);
            }
        } finally {
            // Снимаем блокировку
            lockDataService.unlock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId(), userInfo.getUser().getId());
        }

        // 20 Загрузка формы завершена
        log(userInfo, LogData.L20, localLogger, currentFile.getName());
        return success;
    }

    /**
     * Путь к каталогу из ConfigurationParam
     */
    private String getPath(TAUserInfo userInfo, ConfigurationParam param, int formDepartmentId, LogData logData, Logger logger) {
        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
        List<String> pathList = null;
        if (model != null) {
            pathList = model.get(param, formDepartmentId);
        }
        // Проверка наличия каталога в параметрах
        if (pathList == null || pathList.isEmpty()) {
            log(userInfo, logData, logger);
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок НФ
     */
    private String getFormDataErrorPath(TAUserInfo userInfo, int formDepartmentId, Logger logger) {
        return getPath(userInfo, ConfigurationParam.FORM_ERROR_DIRECTORY, formDepartmentId,
                LogData.L_1, logger);
    }

    /**
     * Путь к каталогу архива НФ
     */
    private String getFormDataArchivePath(TAUserInfo userInfo, int formDepartmentId, Logger logger) {
        return getPath(userInfo, ConfigurationParam.FORM_ARCHIVE_DIRECTORY, formDepartmentId,
                LogData.L_2, logger);
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(TAUserInfo userInfo, ConfigurationParam configurationParam, int departmentId, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            log(userInfo, LogData.L30, logger, departmentService.getDepartment(departmentId).getName());
            return null;
        }
        return uploadPathList.get(0);
    }

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    private List<String> getWorkTransportFiles(TAUserInfo userInfo, String folderPath, Set<String> ignoreFileSet,
                                               List<String> loadedFileNameList, Logger logger, ImportCounter wrongImportCounter) {
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
                if (TransportDataParam.isValidName(candidateStr)) {
                    retVal.add(candidateStr);
                } else {
                    log(userInfo, LogData.L4, logger, candidateStr, folderPath);
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
