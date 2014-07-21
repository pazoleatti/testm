package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, Logger logger) {
        log(userInfo, LogData.L1, logger);
        // Список ТБ
        List<Integer> departmenIdList = departmentService.getTBDepartmentIds(userInfo.getUser());
        ImportCounter importCounter = new ImportCounter();
        // По каталогам загрузки ТБ
        for (Integer departmentId : departmenIdList) {
            importCounter.add(importDataFromFolder(userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId, logger));
        }
        log(userInfo, LogData.L2, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    /**
     * Загрузка НФ из конкретного каталога
     */
    private ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam param, Integer departmentId, Logger logger) {
        String path = getUploadPath(userInfo, param, departmentId, logger);
        if (path == null) {
            // Ошибка получения пути
            return new ImportCounter();
        }
        return importFormData(userInfo, path, logger);
    }

    /**
     * Загрузка всех ТФ НФ из указанного каталога загрузки
     */
    private ImportCounter importFormData(TAUserInfo userInfo, String path, Logger logger) {
        int success = 0;
        int fail = 0;
        List<String> workFilesList;
        // Набор файлов, которые уже обработали
        Set<String> ignoreFileSet = new HashSet<String>();
        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        if (getWorkTransportFiles(path, ignoreFileSet).isEmpty()) {
            log(userInfo, LogData.L3, logger);
            return new ImportCounter();
        }

        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        while (!(workFilesList = getWorkTransportFiles(path, ignoreFileSet)).isEmpty()) {
            String fileName = workFilesList.get(0);
            ignoreFileSet.add(fileName);
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + fileName);

            // Обработка файла
            TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
            String formCode = transportDataParam.getFormCode();
            String reportPeriodCode = transportDataParam.getReportPeriodCode();
            Integer year = transportDataParam.getYear();
            Integer departmentCode = transportDataParam.getDepartmentCode();

            // Не задан код подразделения или код формы
            if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
                log(userInfo, LogData.L4, logger, fileName);
                fail++;
                continue;
            }

            // Указан несуществующий код подразделения
            Department formDepartment = departmentService.getDepartmentByCode(departmentCode);
            if (formDepartment == null) {
                log(userInfo, LogData.L5, logger, fileName);
                fail++;
                continue;
            }

            // Указан несуществующий код налоговой формы
            FormType formType = formTypeService.getByCode(formCode);
            if (formType == null) {
                log(userInfo, LogData.L6, logger, fileName);
                fail++;
                continue;
            }

            // Указан недопустимый код периода
            ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
            if (reportPeriod == null) {
                log(userInfo, LogData.L7, logger, fileName);
                fail++;
                continue;
            }

            // Назначение подразделению типа и вида НФ
            if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
                log(userInfo, LogData.L14, logger, formType.getName(), formDepartment.getName());
                fail++;
                continue;
            }

            // Открытость периода
            boolean active = periodService.isActivePeriod(reportPeriod.getId(), formDepartment.getId());
            if (!active) {
                String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                log(userInfo, LogData.L9, logger, reportPeriodName);
                fail++;
                continue;
            }

            // TODO Логика загрузки в коррерктирующий период реализуется в версии 0.4

            FormDataKind formDataKind = FormDataKind.PRIMARY; // ТФ только для первичных НФ

            // TODO Проверка ЭЦП (L15, L16, L25) // http://jira.aplana.com/browse/SBRFACCTAX-8059 0.3.9 Реализовать проверку ЭЦП ТФ
            log(userInfo, LogData.L15, logger, fileName);

            // Поиск экземпляра НФ
            FormData formData;

            // Признак ежемесячной формы по файлу
            boolean monthly = transportDataParam.getMonth() != null;

            Integer formTemplateId = formTemplateService.getActiveFormTemplateId(formType.getId(), reportPeriod.getId());

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
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentCode, logger), currentFile, localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            if (formData != null) {
                log(userInfo, LogData.L13, logger);
            }

            // Загрузка данных в НФ (скрипт)
            Logger localLogger = new Logger();
            try {
                // Загрузка
                importFormData(userInfo, currentFile, formData, formType, formTemplate, formDepartment, reportPeriod, formDataKind,
                        transportDataParam, localLogger);
                // Вывод скрипта в область уведомлений
                logger.getEntries().addAll(localLogger.getEntries());
            } catch (Exception ex) {
                // Вывод скрипта в область уведомлений
                logger.getEntries().addAll(localLogger.getEntries());
                // Вывод в область уведомленеий и ЖА
                log(userInfo, LogData.L21, logger, formType.getName(), formDepartment.getName());
                // Перемещение в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentCode, logger), currentFile, localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            // Файл загружен
            success++;
        }

        return new ImportCounter(success, fail);
    }

    /**
     * Загрузка ТФ конкретной НФ. Только этот метод в сервисе транзакционный.
     */
    @Transactional
    private void importFormData(TAUserInfo userInfo, FileWrapper currentFile, FormData formData, FormType formType,
                                FormTemplate formTemplate, Department formDepartment, ReportPeriod reportPeriod,
                                FormDataKind formDataKind, TransportDataParam transportDataParam,
                                Logger localLogger) {
        String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();

        boolean formCreated = false;
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
                throw new ServiceException();
            }

            // Перенос в архив
            moveToArchiveDirectory(userInfo, getFormDataArchivePath(userInfo, formDepartment.getId(), localLogger), currentFile, localLogger);

            if (formCreated) {
                log(userInfo, LogData.L18, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
            }

            // Сохранение
            formDataService.saveFormData(localLogger, userInfo, formData);

            log(userInfo, LogData.L19, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
        } finally {
            // Снимаем блокировку
            lockCoreService.unlock(FormData.class, formData.getId(), userInfo);
        }

        // Загрузка формы завершена
        log(userInfo, LogData.L20, localLogger, currentFile.getName());
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
            FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + candidateStr);
            // Файл, это файл, а не директория и соответствует формату имени ТФ
            if (candidateFile.isFile() && TransportDataParam.isValidName(candidateStr)) {
                retVal.add(candidateStr);
            }
        }
        return retVal;
    }
}
