package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class LoadFormDataServiceImpl extends AbstractLoadTransportDataService implements LoadFormDataService {

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
    private LockCoreService lockCoreService;
    @Autowired
    private SignService signService;

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, List<Integer> departmentIdList, Logger logger) {
        log(userInfo, LogData.L1, logger);
        ImportCounter importCounter = new ImportCounter();
        // По каталогам загрузки ТБ
        List<Integer> tbList = departmentService.getTBDepartmentIds(userInfo.getUser());
        for (Object departmentIdObj : CollectionUtils.intersection(departmentIdList, tbList)) {
            int departmentId = (Integer) departmentIdObj;
            importCounter.add(importDataFromFolder(userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId, logger));
        }
        log(userInfo, LogData.L2, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, Logger logger) {
        return importFormData(userInfo, departmentService.getTBDepartmentIds(userInfo.getUser()), logger);
    }

    /**
     * Загрузка всех ТФ НФ из указанного каталога загрузки
     */
    private ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam param, Integer departmentId, Logger logger) {
        String path = getUploadPath(userInfo, param, departmentId, logger);
        if (path == null) {
            // Ошибка получения пути
            return new ImportCounter();
        }
        int success = 0;
        int fail = 0;
        // Набор файлов, которые уже обработали
        Set<String> ignoreFileSet = new HashSet<String>();
        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        List<String> workFilesList = getWorkTransportFiles(path, ignoreFileSet);
        if (workFilesList.isEmpty()) {
            log(userInfo, LogData.L3, logger, departmentService.getDepartment(departmentId).getName());
            return new ImportCounter();
        }

        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        for (String fileName : workFilesList) {
            ignoreFileSet.add(fileName);
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + fileName);

            // Обработка файла
            TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
            String formCode = transportDataParam.getFormCode();
            String reportPeriodCode = transportDataParam.getReportPeriodCode();
            Integer year = transportDataParam.getYear();
            String departmentCode = transportDataParam.getDepartmentCode();

            // Не задан код подразделения или код формы
            if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
                log(userInfo, LogData.L4, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L4.getText())), logger);
                fail++;
                continue;
            }

            // Указан несуществующий код подразделения
            Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode);
            if (formDepartment == null) {
                log(userInfo, LogData.L5, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L4.getText())), logger);
                fail++;
                continue;
            }

            // Указан несуществующий код налоговой формы
            FormType formType = formTypeService.getByCode(formCode);
            if (formType == null) {
                log(userInfo, LogData.L6, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L4.getText())), logger);
                fail++;
                continue;
            }

            // Указан недопустимый код периода
            ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
            if (reportPeriod == null) {
                log(userInfo, LogData.L7, logger, fileName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L4.getText())), logger);
                fail++;
                continue;
            }

            // Назначение подразделению типа и вида НФ
            if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
                log(userInfo, LogData.L14, logger, formType.getName(), formDepartment.getName());
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L4.getText())), logger);
                fail++;
                continue;
            }

            // Открытость периода
            boolean active = periodService.isActivePeriod(reportPeriod.getId(), formDepartment.getId());
            if (!active) {
                String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                log(userInfo, LogData.L9, logger, formType.getName(), reportPeriodName);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, LogData.L4.getText())), logger);
                fail++;
                continue;
            }

            // TODO Логика загрузки в коррерктирующий период реализуется в версии 0.4

            FormDataKind formDataKind = FormDataKind.PRIMARY; // ТФ только для первичных НФ

            if (!signService.checkSign(currentFile.getPath(), 0)) {
                log(userInfo, LogData.L16, logger, fileName);
                fail++;
                continue;
            }

            log(userInfo, LogData.L15, logger, fileName);

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

            FormTemplate formTemplate = null;
            if (formTemplateId != null) {
                formTemplate = formTemplateService.get(formTemplateId);
                // Уточнение из шаблона
                monthly = formTemplate.isMonthly();
            }

            if (!monthly) {
                formData = formDataDao.find(formType.getId(), formDataKind, formDepartment.getId(), reportPeriod.getId());
            } else {
                formData = formDataDao.findMonth(formType.getId(), formDataKind, formDepartment.getId(),
                        reportPeriod.getTaxPeriod().getId(), transportDataParam.getMonth());
            }

            // Экземпляр уже есть и не в статусе «Создана»
            if (formData != null && formData.getState() != WorkflowState.CREATED) {
                // Сообщение об ошибке в общий лог и в файл со списком ошибок
                Logger localLogger = new Logger();
                localLogger.error(LogData.L17.getText());
                log(userInfo, LogData.L17, logger, formType.getName(), formDepartment.getName());
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile, localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            if (formData != null) {
                log(userInfo, LogData.L13, logger);
            }

            // Загрузка данных в НФ (скрипт)
            Logger localLogger = new Logger();
            boolean result;
            try {
                // Загрузка
                result = importFormData(userInfo, departmentId, currentFile, formData, formType, formTemplate,
                        formDepartment, reportPeriod, formDataKind, transportDataParam, localLogger);

                // Вывод скрипта в область уведомлений
                logger.getEntries().addAll(localLogger.getEntries());
            } catch (Exception e) {
                // Вывод скрипта в область уведомлений
                logger.getEntries().addAll(localLogger.getEntries());
                // Вывод в область уведомленеий и ЖА
                log(userInfo, LogData.L21, logger, e.getMessage());
                // Перемещение в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger), currentFile, localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            if (result) {
                success++;
            } else {
                fail++;
            }
        }

        return new ImportCounter(success, fail);
    }

    /**
     * Загрузка ТФ конкретной НФ. Только этот метод в сервисе транзакционный.
     */
    @Transactional
    private boolean importFormData(TAUserInfo userInfo, int departmentId, FileWrapper currentFile, FormData formData, FormType formType,
                                FormTemplate formTemplate, Department formDepartment, ReportPeriod reportPeriod,
                                FormDataKind formDataKind, TransportDataParam transportDataParam,
                                Logger localLogger) {
        String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();

        boolean formCreated = false;
        boolean success = false;
        // Если формы нет, то создаем
        if (formData == null) {
            // Если форма не ежемесячная, то месяц при созданнии не указывается
            Integer month = transportDataParam.getMonth();
            if (formTemplate != null && !formTemplate.isMonthly()) {
                month = null;
            }
            int formTeplateId = formTemplateService.getActiveFormTemplateId(formType.getId(), reportPeriod.getId());
            long formDataId = formDataService.createFormData(localLogger, userInfo, formTeplateId, formDepartment.getId(),
                    formDataKind, reportPeriod, month);
            formData = formDataDao.get(formDataId, false);
            formCreated = true;
        }

        // Блокировка
        lockCoreService.lock(FormData.class, formData.getId(), userInfo);

        try {
            // Скрипт
            InputStream inputStream = currentFile.getInputStream();
            try {
                formDataService.importFormData(localLogger, userInfo, formData.getId(), inputStream, currentFile.getName(),
                        FormDataEvent.IMPORT_TRANSPORT_FILE);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            // Если при выполнении скрипта возникли фатальные ошибки, то
            if (localLogger.containsLevel(LogLevel.ERROR)) {
                // Исключение для отката транзакции сознания и заполнения НФ
                throw new ServiceException("При выполнении загрузки произошли ошибки");
            }

            // Перенос в архив
            if (moveToArchiveDirectory(userInfo, getFormDataArchivePath(userInfo, departmentId, localLogger), currentFile, localLogger)) {
                if (formCreated) {
                    log(userInfo, LogData.L18, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
                }

                // Сохранение
                formDataService.saveFormData(localLogger, userInfo, formData);

                log(userInfo, LogData.L19, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
                success = true;
            } else {
                // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, localLogger), currentFile,
                        Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), ""))), localLogger);
            }
        } finally {
            // Снимаем блокировку
            lockCoreService.unlock(FormData.class, formData.getId(), userInfo);
        }

        // Загрузка формы завершена
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
    private List<String> getWorkTransportFiles(String folderPath, Set<String> ignoreFileSet) {
        List<String> retVal = new LinkedList<String>();
        FileWrapper catalogFile = ResourceUtils.getSharedResource(folderPath);
        for (String candidateStr : catalogFile.list()) {
            if (ignoreFileSet != null && ignoreFileSet.contains(candidateStr)) {
                continue;
            }

            // Это файл, а не директория и соответствует формату имени ТФ
            if (TransportDataParam.isValidName(candidateStr)) {
                FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + candidateStr);
                if (candidateFile.isFile()) {
                    retVal.add(candidateStr);
                }
            }
        }
        return retVal;
    }
}
