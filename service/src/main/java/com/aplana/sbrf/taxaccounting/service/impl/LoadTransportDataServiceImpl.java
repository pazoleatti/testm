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
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional
public class LoadTransportDataServiceImpl implements LoadTransportDataService {

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private FormDataDao formDataDao;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private RefBookExternalService refBookExternalService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private LockCoreService lockCoreService;

    // Сообщения при загрузке из каталогов
    private static enum LogData {
        L1("Запущена процедура загрузки транспортных файлов, содержащих данные налоговых форм.", LogLevel.INFO, true),
        L2("Завершена процедура загрузки транспортных файлов, содержащих данные налоговых форм. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true),
        L3("В каталоге загрузки не найдены файлы! Загрузка не выполнена.", LogLevel.ERROR, true),
        L4("Имя или формат файла «%s» не соответствует требованиям к транспортному файлу! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L5("Указанный в имени файла «%s» код подразделения не существует в Системе! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L6("Указанный в имени файла «%s» код налоговой формы не существует в Системе! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L7("Указанный в имени файла «%s» код отчетного периода не существует в Системе! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        // L8("Для налоговой формы «%s» открыт корректирующий период «%s»", LogLevel.INFO, false),
        L9("Для налоговой формы «%s» закрыт (либо еще не открыт) отчетный период «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        // L10("ТФ с таким именем уже есть в архиве, текущий ТФ переименован в «%s».", LogLevel.INFO, false),
        L11("Перенос «%s» в каталог архива успешно выполнен.", LogLevel.INFO, false),
        L12("Ошибка при архивировании транспортного файла! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L13("Налоговая форма существует и имеет статус «" + WorkflowState.CREATED.getName() + "».", LogLevel.INFO, false),
        L14("Назначение налоговой формы «%s» подразделению «%s» не выполнено! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        // L15("ЭЦП файла «%s» принята, начата загрузка данных файла.", LogLevel.INFO, true),
        // L16("ЭЦП файла «%s» не принята или отсутствует! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L17("Налоговая форма существует и находится в состоянии, отличном от «" + WorkflowState.CREATED.getName() + "»! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L18("Создана новая налоговая форма «%s» для подразделения «%s» в периоде «%s».", LogLevel.INFO, true),
        L19("Первичная налоговая форма «%s» для подразделения «%s» в периоде «%s» сохранена.", LogLevel.INFO, true),
        L20("Закончена загрузка данных файла «%s».", LogLevel.INFO, true),
        L21("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена.", LogLevel.ERROR, true),
        // L22("Итоговая сумма в графе «%s» строки %d в транспортном файле некорректна. Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L23("Запущена процедура загрузки транспортных файлов, содержащих данные справочников.", LogLevel.INFO, true),
        L24("Завершена процедура загрузки транспортных файлов, содержащих данные справочников. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true),
        // L25("Не указан путь к корректному файлу ключей ЭЦП! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L26("Транспортный файл размещен в каталоге ошибок в составе архива «%s».", LogLevel.INFO, true),
        L27("Транспортный файл не записан в каталог ошибок! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L28("Ошибка при удалении файла «%s» из каталога «%s» при перемещении в каталог ошибок!", LogLevel.INFO, true),
        L29("Ошибка при удалении файла «%s» из каталога «%s» при перемещении в каталог архива!", LogLevel.ERROR, true),
        L30("К каталогу загрузки для подразделения «%s» не указан корректный путь!", LogLevel.ERROR, true),
        // Сообщения которых нет в постановке
        L_1("Не указан каталог ошибок в конфигурационных параметрах АС «Учет налогов»!", LogLevel.ERROR, true),
        L_2("Не указан каталог архива в конфигурационных параметрах АС «Учет налогов»!", LogLevel.ERROR, true);

        private LogLevel level;
        private String text;
        private boolean logSystem;

        private LogData(String text, LogLevel level, boolean logSystem) {
            this.text = text;
            this.level = level;
            this.logSystem = logSystem;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getText() {
            return text;
        }

        public boolean isLogSystem() {
            return logSystem;
        }
    }

    // Сообщения, которые не учтены в постановка
    final static String IMPORT_REF_BOOK_ERROR = "Ошибка при загрузке транспортных файлов справочников %s.";

    // Константы
    final static String LOG_FILE_NAME = "Ошибки.txt";
    final static String ZIP_ENCODING = "cp866";

    /**
     * Путь к каталогу архива справочников
     */
    private String getRefBookArchivePath(TAUserInfo userInfo, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            logImport(userInfo, LogData.L_2, logger);
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок справочников
     */
    private String getRefBookErrorPath(TAUserInfo userInfo, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            logImport(userInfo, LogData.L_1, logger);
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Загрузка всех справочников НСИ
     */
    @Override
    public ImportCounter importRefBookNsi(TAUserInfo userInfo, Logger logger) {
        logImport(userInfo, LogData.L23, logger);
        ImportResult<FileWrapper> importResult;
        try {
            importResult = refBookExternalService.importRefBookNsi(userInfo, logger);
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, e.getMessage());
            return new ImportCounter();
        }
        ImportCounter importCounter = new ImportCounter(importResult.getSuccessFileList().size(),
                importResult.getSkipFileList().size(), importResult.getFailFileList().size());
        logImport(userInfo, LogData.L24, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    /**
     * Загрузка всех справочников Diasoft
     */
    @Override
    public ImportCounter importRefBookDiasoft(TAUserInfo userInfo, Logger logger) {
        logImport(userInfo, LogData.L23, logger);
        ImportResult<FileWrapper> importResult;
        try {
            importResult = refBookExternalService.importRefBookDiasoft(userInfo, logger);
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, e.getMessage());
            return new ImportCounter();
        }

        // Пермещение в каталог архива
        String archivePath = null;
        for (FileWrapper file : importResult.getSuccessFileList()) {
            if (archivePath == null) {
                archivePath = getRefBookArchivePath(userInfo, logger);
                if (archivePath == null) {
                    break;
                }
            }
            moveToArchiveDirectory(userInfo, archivePath, file, logger);
        }

        // Пермещение в каталог ошибок
        String errorPath = null;
        for (FileWrapper file : importResult.getFailFileList()) {
            if (errorPath == null) {
                errorPath = getRefBookErrorPath(userInfo, logger);
                if (errorPath == null) {
                    break;
                }
            }
            List<LogEntry> errorList = importResult.getFailLogMap().get(file);
            moveToErrorDirectory(userInfo, errorPath, file, errorList, logger);
        }
        ImportCounter importCounter = new ImportCounter(importResult.getSuccessFileList().size(),
                importResult.getSkipFileList().size(), importResult.getFailFileList().size());
        logImport(userInfo, LogData.L24, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    /**
     * Загрузка из указанного каталога ТФНФ
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    private ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam param, Integer departmentId, Logger logger) {
        String path = getUploadPath(userInfo, param, departmentId, logger);
        if (path == null) {
            // Ошибка получения пути
            return new ImportCounter();
        }
        return importFormData(userInfo, path, logger);
    }

    @Override
    public ImportCounter importFormData(TAUserInfo userInfo, Logger logger) {
        logImport(userInfo, LogData.L1, logger);
        List<Integer> departmenIdList = departmentService.getTBDepartmentIds(userInfo.getUser());
        ImportCounter importCounter = new ImportCounter();
        for (Integer departmentId : departmenIdList) {
            importCounter.add(importDataFromFolder(userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId, logger));
        }
        logImport(userInfo, LogData.L2, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    /**
     * Загрузка всех ТФ НФ из указанного каталога загрузки
     */
    private ImportCounter importFormData(TAUserInfo userInfo, String path, Logger logger) {
        List<String> workFilesList;
        int success = 0;
        int skip = 0;
        int fail = 0;
        // Набор файлов, которые не удалось переместить с удалением. Их нужно пропускать.
        Set<String> ignoreFileSet = new HashSet<String>();
        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        if (getWorkTransportFiles(path, ignoreFileSet).isEmpty()) {
            logImport(userInfo, LogData.L3, logger);
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
                logImport(userInfo, LogData.L4, logger, fileName);
                skip++;
                continue;
            }

            // Указан несуществующий код подразделения
            Department formDepartment = departmentService.getDepartmentByCode(departmentCode);
            if (formDepartment == null) {
                logImport(userInfo, LogData.L5, logger, fileName);
                skip++;
                continue;
            }

            // Указан несуществующий код налоговой формы
            FormType formType = formTypeService.getByCode(formCode);
            if (formType == null) {
                logImport(userInfo, LogData.L6, logger, fileName);
                skip++;
                continue;
            }

            // Указан недопустимый код периода
            ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
            if (reportPeriod == null) {
                logImport(userInfo, LogData.L7, logger, fileName);
                skip++;
                continue;
            }

            // Назначение подразделению типа и вида НФ
            if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
                logImport(userInfo, LogData.L14, logger, formType.getName(), formDepartment.getName());
                skip++;
                continue;
            }

            // Открытость периода
            boolean active = periodService.isActivePeriod(reportPeriod.getId(), formDepartment.getId());
            if (!active) {
                String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
                logImport(userInfo, LogData.L9, logger, reportPeriodName);
                skip++;
                continue;
            }

            // TODO Логика загрузки в корерктирующий период реализуется в версии 0.4

            FormDataKind formDataKind = FormDataKind.PRIMARY; // ТФ только для первичных НФ

            // TODO Проверка ЭЦП (L15, L16) // http://jira.aplana.com/browse/SBRFACCTAX-8059 0.3.9 Реализовать проверку ЭЦП ТФ

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
                logImport(userInfo, LogData.L17, logger, formType.getName(), formDepartment.getName());
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentCode, logger), currentFile, localLogger.getEntries(), logger);
                skip++;
                continue;
            }

            if (formData != null) {
                logImport(userInfo, LogData.L13, logger);
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
                logImport(userInfo, LogData.L21, logger, formType.getName(), formDepartment.getName());
                // Перемещение в каталог ошибок
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentCode, logger), currentFile, localLogger.getEntries(), logger);
                fail++;
                continue;
            }

            // Файл загружен
            success++;
        }

        return new ImportCounter(success, skip, fail);
    }

    /**
     * Загрузка ТФ конкретной НФ
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
                logImport(userInfo, LogData.L18, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
            }

            // Сохранение
            formDataService.saveFormData(localLogger, userInfo, formData);

            logImport(userInfo, LogData.L19, localLogger, formType.getName(), formDepartment.getName(), reportPeriodName);
        } finally {
            // Снимаем блокировку
            lockCoreService.unlock(FormData.class, formData.getId(), userInfo);
        }

        // Загрузка формы завершена
        logImport(userInfo, LogData.L20, localLogger, currentFile.getName());
    }

    /**
     * Путь к каталогу ошибок НФ
     */
    private String getFormDataErrorPath(TAUserInfo userInfo, int formDepartmentId, Logger logger) {
        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
        List<String> archivePathList = null;
        if (model != null) {
            archivePathList = model.get(ConfigurationParam.FORM_ERROR_DIRECTORY, formDepartmentId);
        }
        // Проверка наличия каталога в параметрах
        if (archivePathList == null || archivePathList.isEmpty()) {
            logImport(userInfo, LogData.L_1, logger);
            return null;
        }
        return archivePathList.get(0);
    }

    /**
     * Перенос ТФ в каталог ошибок
     *
     * @param errorPath Путь к каталогу ошибок
     * @param errorFileSrc Файл с ошибкой, который должен быть перенесен
     */
    void moveToErrorDirectory(TAUserInfo userInfo, String errorPath, FileWrapper errorFileSrc, List<LogEntry> errorList, Logger logger) {
        try {
            // Создание дерева каталогов
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            // Каталог_ошибок/Текущий_год/Текущий_месяц/Текущий_день_месяца/
            FileWrapper errorFolderDst = ResourceUtils.getSharedResource(errorPath + calendar.get(Calendar.YEAR)
                    + "/" + Months.fromId(calendar.get(Calendar.MONTH)).getName()
                    + "/" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "/", false);
            errorFolderDst.mkDirs();

            // Создание архива
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(yyyy.MM.dd HH.mm.ss)");

            String path = errorFolderDst.getPath() + "/" + errorFileSrc.getName()
                    + simpleDateFormat.format(calendar.getTime()) + ".zip";
            FileWrapper errorFileDst = ResourceUtils.getSharedResource(path, false);
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(errorFileDst.getOutputStream());
            zaos.setEncoding(ZIP_ENCODING);
            zaos.putArchiveEntry(new ZipArchiveEntry(errorFileSrc.getName()));
            InputStream errorFileSrcInputStream = errorFileSrc.getInputStream();
            IOUtils.copy(errorFileSrcInputStream, zaos);
            zaos.closeArchiveEntry();
            IOUtils.closeQuietly(errorFileSrcInputStream);

            // Файл с логами, если логи есть
            if (errorList != null && !errorList.isEmpty()) {
                zaos.putArchiveEntry(new ZipArchiveEntry(LOG_FILE_NAME));
                StringBuilder sb = new StringBuilder();
                for (LogEntry logEntry : errorList) {
                    sb.append(logEntry.getLevel().name() + "\t" + logEntry.getMessage() + "\r\n");
                }
                IOUtils.copy(new ByteArrayInputStream(sb.toString().getBytes()), zaos);
                zaos.closeArchiveEntry();
            }

            IOUtils.closeQuietly(zaos);

            try {
                // Удаление
                errorFileSrc.delete();
            } catch (Exception e) {
                logImport(userInfo, LogData.L28, logger);
            }

            logImport(userInfo, LogData.L26, logger, errorFileDst.getName());
        } catch (Exception e) {
            logImport(userInfo, LogData.L27, logger);
        }
    }

    /**
     * Путь к каталогу архива НФ
     */
    private String getFormDataArchivePath(TAUserInfo userInfo, int formDepartmentId, Logger logger) {
        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
        List<String> archivePathList = null;
        if (model != null) {
            archivePathList = model.get(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, formDepartmentId);
        }
        // Проверка наличия каталога в параметрах
        if (archivePathList == null || archivePathList.isEmpty()) {
            logImport(userInfo, LogData.L_2, logger);
            return null;
        }
        return archivePathList.get(0);
    }

    /**
     * Перенос ТФ в каталог архива
     *
     * @param archivePath Путь к каталогу архива
     * @param archiveFileSrc Файл, который должен быть перенесен
     */
    void moveToArchiveDirectory(TAUserInfo userInfo, String archivePath, FileWrapper archiveFileSrc, Logger logger) {
        try {
            // Создание дерева каталогов
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            // Каталог_архива/Текущий_год/Текущий_месяц/Текущий_день_месяца/
            FileWrapper archiveFolderDst = ResourceUtils.getSharedResource(archivePath + calendar.get(Calendar.YEAR)
                    + "/" + Months.fromId(calendar.get(Calendar.MONTH)).getName()
                    + "/" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "/", false);
            archiveFolderDst.mkDirs();

            // Создание архива
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(yyyy.MM.dd HH.mm.ss)");

            String path = archiveFolderDst.getPath() + "/" + archiveFileSrc.getName()
                    + simpleDateFormat.format(calendar.getTime()) + ".zip";
            FileWrapper archiveFileDst = ResourceUtils.getSharedResource(path, false);
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(archiveFileDst.getOutputStream());
            zaos.setEncoding(ZIP_ENCODING);
            zaos.putArchiveEntry(new ZipArchiveEntry(archiveFileSrc.getName()));
            InputStream errorFileSrcInputStream = archiveFileSrc.getInputStream();
            IOUtils.copy(errorFileSrcInputStream, zaos);
            zaos.closeArchiveEntry();
            IOUtils.closeQuietly(errorFileSrcInputStream);
            IOUtils.closeQuietly(zaos);

            // Удаление
            try {
                archiveFileSrc.delete();
            } catch (Exception e) {
                logImport(userInfo, LogData.L29, logger);
            }

            logImport(userInfo, LogData.L11, logger, archiveFileDst.getName());
        } catch (Exception e) {
            logImport(userInfo, LogData.L12, logger);
        }
    }

    @Override
    public List<String> getWorkTransportFiles(String folderPath, Set<String> ignoreFileSet) {
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

    /**
     * Логирование в области уведомлений и ЖА при импорте из ТФ
     */
    private void logImport(TAUserInfo userInfo, LogData logData, Logger logger, Object... args) {
        // Область уведомлений
        switch (logData.getLevel()) {
            case INFO:
                logger.info(logData.getText(), args);
                break;
            case ERROR:
                logger.error(logData.getText(), args);
                break;
        }
        // ЖА
        // TODO Указать признак ошибки в ЖА. См. logData.getLevel()
        if (logData.isLogSystem()) {
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(), null,
                    null, null, null, String.format(logData.getText(), args));
        }
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(TAUserInfo userInfo, ConfigurationParam configurationParam, int departmentId, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            logImport(userInfo, LogData.L30, logger, departmentService.getDepartment(departmentId).getName());
            return null;
        }
        return uploadPathList.get(0);
    }
}
