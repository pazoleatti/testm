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
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional
public class TransportDataServiceImpl implements TransportDataService {

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

    // TODO Перепроверить ошибки и отсортировать в соответствии с http://conf.aplana.com/pages/viewpage.action?pageId=12324125
    final static String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    final static String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    final static String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    final static String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";
    final static String NO_CATALOG_UPLOAD_ERROR = "Не указан каталог загрузки в конфигурационных параметрах АС «Учет налогов»!";
    final static String NO_CATALOG_ERROR_ERROR = "Не указан каталог ошибок в конфигурационных параметрах АС «Учет налогов»!";
    final static String NO_CATALOG_ARCHIVE_ERROR = "Не указан каталог архива в конфигурационных параметрах АС «Учет налогов»!";
    final static String UPLOAD_FILE_SUCCESS = "Файл «%s» помещен в каталог загрузки «%s».";
    final static String UPLOAD_FILE_REPLACE = "В каталоге загрузки ранее загруженный файл «%s» был заменен!";
    final static String UPLOAD_ALL = "Загрузка транспортных файлов в каталог загрузки завершена.";
    final static String NO_IMPORT_FILES_ERROR = "В каталоге загрузки не найдены файлы!";
    final static String EXIST_FORM_DATA_ERROR = "Налоговая форма существует и имеет статус «Создана».";
    final static String IMPORT_FORM_DATA_REPORT = "Завершена процедура загрузки транспортных файлов, содержащих данные налоговых форм. Файлов загружено: %d. Файлов отклонено: %d.";
    final static String IMPORT_FORM_DATA_SCRIPT_ERROR = "При загрузке данных налоговой формы из транспортного файла произошли ошибки!";
    final static String MOVE_ARCHIVE_ERROR = "Ошибка при архивировании транспортного файла! Загрузка файла не выполнена.";
    final static String MOVE_ARCHIVE_SUCCESS = "Перенос «%s» в каталог архива успешно выполнен.";
    final static String MOVE_ERROR_ERROR = "Транспортный файл не записан в каталог ошибок! Загрузка файла не выполнена.";
    final static String MOVE_ERROR_SUCCESS = "Транспортный файл размещен в каталоге ошибок в составе архива «%s».";
    final static String WRONG_NAME = "Файл «%s» не загружен, т.к. имеет некорректный формат имени!";
    final static String WRONG_NAME_DEPARTMENT = "Код подразделения «%s» не существует в АС «Учет налогов»!";
    final static String WRONG_NAME_FORM_TYPE = "Код налоговой формы «%s» не существует в АС «Учет налогов»!";
    final static String ASSIGNATION_ERROR = "Файл «%s» не загружен, т.к. текущий пользователь не имеет доступа к содержащейся в нем налоговой форме «%s» подразделения «%s»!";
    final static String COPY_ERROR = "Файл «%s не загружен: %s!";
    final static String ZIP_ENCODING = "cp866";
    final static String LOG_FILE_NAME = "Ошибки.txt";

    @Override
    public boolean uploadFile(TAUserInfo userInfo, int departmentId, String fileName, InputStream inputStream, Logger logger) {
        // Проверка прав
        if (userInfo == null) {
            logger.error(USER_NOT_FOUND_ERROR);
            logger.error(UPLOAD_ALL);
            return false;
        }

        if (!userInfo.getUser().hasRole(TARole.ROLE_OPER)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            logger.error(ACCESS_DENIED_ERROR);
            logger.error(UPLOAD_ALL);
            return false;
        }

        if (fileName == null) {
            logger.error(NO_FILE_NAME_ERROR);
            logger.error(UPLOAD_ALL);
            return false;
        }

        if (inputStream == null) {
            logger.error(EMPTY_INPUT_STREAM_ERROR);
            logger.error(UPLOAD_ALL);
            return false;
        }

        // Список загруженных ТФ для ЖА
        List<String> fileNames = new LinkedList<String>();

        if (fileName.toLowerCase().endsWith(".zip")) {
            // Архив — извлекаем все содержимое
            ZipArchiveInputStream zais = new ZipArchiveInputStream(inputStream, ZIP_ENCODING);
            ArchiveEntry entry;
            try {
                while ((entry = zais.getNextEntry()) != null) {
                    ConfigurationParam configurationParam = checkFormDataAccess(entry.getName(), logger);
                    if (configurationParam != null) {
                        int departmentConfId = departmentId;
                        if (configurationParam.isCommon()) {
                            departmentConfId = 0;
                        }
                        try {
                            if (configurationParam != null
                                    && copyFileFromStream(zais, getUploadPath(configurationParam, departmentConfId, logger),
                                    entry.getName(), logger)) {
                                fileNames.add(entry.getName());
                            }
                        } catch (IOException e) {
                            logger.error(COPY_ERROR, e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(COPY_ERROR, e.getMessage());
            }
            IOUtils.closeQuietly(zais);
        } else {
            // Не архив
            ConfigurationParam configurationParam = checkFormDataAccess(fileName, logger);
            if (configurationParam != null) {
                int departmentConfId = departmentId;
                if (configurationParam.isCommon()) {
                    departmentConfId = 0;
                }
                try {
                    if (configurationParam != null
                            && copyFileFromStream(inputStream, getUploadPath(configurationParam, departmentConfId, logger),
                            fileName, logger)) {
                        fileNames.add(fileName);
                    }
                } catch (IOException e) {
                    logger.error(COPY_ERROR, e.getMessage());
                }
            }
        }

        IOUtils.closeQuietly(inputStream);

        if (fileNames.isEmpty()) {
            logger.error(UPLOAD_ALL);
            return false;
        }

        logger.info(UPLOAD_ALL);

        // ЖА
        String msg = StringUtils.collectionToDelimitedString(fileNames, "; ");
        auditService.add(FormDataEvent.UPLOAD_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(), null,
                null, null, null, msg);

        return true;
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(ConfigurationParam configurationParam, int departmentId, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        System.out.println("model = " + model + " configurationParam = " + configurationParam.name() + " departmentId = " + departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            logger.error(NO_CATALOG_UPLOAD_ERROR);
            return null;
        }
        return uploadPathList.get(0);
    }

    /**
     * Проверка имени файла и проверка доступа к соответствующим НФ
     * http://conf.aplana.com/pages/viewpage.action?pageId=13111363
     */
    private ConfigurationParam checkFormDataAccess(String fileName, Logger logger) {
        boolean isDiasoftRefBook = refBookExternalService.isDiasoftFile(fileName);
        boolean isFormData = TransportDataParam.isValidName(fileName);

        if (isDiasoftRefBook) {
            // Справочники не проверяем
            return ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY;
        }

        // Не справочники Diasoft и не ТФ НФ
        if (!isDiasoftRefBook && !isFormData) {
            logger.warn(WRONG_NAME, fileName);
            return null;
        }

        //// НФ

        // Параметры из имени файла
        TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
        String formCode = transportDataParam.getFormCode();
        String reportPeriodCode = transportDataParam.getReportPeriodCode();
        Integer year = transportDataParam.getYear();
        Integer departmentCode = transportDataParam.getDepartmentCode();

        // Не задан код подразделения или код формы
        if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
            logger.warn(WRONG_NAME, fileName);
            return null;
        }

        // Указан несуществующий код налоговой формы
        FormType formType = formTypeService.getByCode(formCode);
        if (formType == null) {
            logger.warn(WRONG_NAME + ' ' + WRONG_NAME_FORM_TYPE, fileName, transportDataParam.getDepartmentCode());
            return null;
        }

        // Указан несуществующий код подразделения
        Department formDepartment = departmentService.getDepartmentByCode(departmentCode);
        if (formDepartment == null) {
            logger.warn(WRONG_NAME + ' ' + WRONG_NAME_DEPARTMENT, fileName, transportDataParam.getDepartmentCode());
            return null;
        }

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
        if (reportPeriod == null) {
            logger.warn(WRONG_NAME + ' ' + WRONG_NAME_FORM_TYPE, fileName, transportDataParam.getDepartmentCode());
            return null;
        }

        // Назначение подразделению типа и вида НФ
        if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
            logger.warn(ASSIGNATION_ERROR, fileName, formType.getName(), formDepartment.getName());
            return null;
        }

        return ConfigurationParam.FORM_UPLOAD_DIRECTORY;
    }

    /**
     * Копирование файла из потока в каталог загрузки
     */
    private boolean copyFileFromStream(InputStream inputStream, String folderPath, String fileName, Logger logger)
            throws IOException {
        if (folderPath != null) {
            FileWrapper file = ResourceUtils.getSharedResource(folderPath + fileName, false);
            boolean exist = file.exists();
            OutputStream outputStream = file.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(outputStream);
            logger.info(UPLOAD_FILE_SUCCESS, fileName, folderPath);
            if (exist) {
                logger.info(UPLOAD_FILE_REPLACE, fileName);
            }
            return true;
        }
        return false;
    }

    private ImportCounter importNsiRefBook(TAUserInfo userInfo, Logger logger) {
        try {
            refBookExternalService.importRefBookNsi(userInfo, logger);
        } catch (ServiceException e) {
            logger.error(e.getMessage());
        }
        // TODO реализовать счетчики
        return new ImportCounter();
    }

    private ImportCounter importDiasoftRefBook(TAUserInfo userInfo, Logger logger) {
        try {
            refBookExternalService.importRefBookDiasoft(userInfo, logger);
            // TODO Пермещение
        } catch (ServiceException e) {
            logger.error(e.getMessage());
            // TODO Пермещение
        }
        // TODO реализовать счетчики
        return new ImportCounter();
    }

    // TODO Реализовать счетчики и их передачу
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ImportCounter importDataFromFolder(TAUserInfo userInfo,  ConfigurationParam param, Integer departmentId, Logger logger) {
        ImportCounter importCounter = new ImportCounter();
        if (param == null || departmentId == null && !param.isCommon()) {
            throw new ServiceException("Конфигурационный параметр каталога загрузки задан неправильно!");
        }

        Integer departmentIdLoad = param.isCommon() ? 0 : departmentId;

        String path = getUploadPath(param, departmentIdLoad, logger);
        if (path == null) {
            // Ошибка получения пути
            return importCounter;
        }

        if (param.isCommon()) {
            if (param == ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY) {
                importCounter.add(importDiasoftRefBook(userInfo, logger));
            } else if (param == ConfigurationParam.OKATO_UPLOAD_DIRECTORY
                    || param == ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY
                    || param == ConfigurationParam.REGION_UPLOAD_DIRECTORY) {
                importCounter.add(importNsiRefBook(userInfo, logger));
            }
        } else {
            // Загрузка НФ
            importCounter.add(importFormData(userInfo, path, logger));
        }
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
        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        while (!(workFilesList = getWorkFilesFromFolder(path, ignoreFileSet)).isEmpty()) {
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
                logger.warn(WRONG_NAME, fileName);
                skip++;
                continue;
            }

            // Указан несуществующий код подразделения
            Department formDepartment = departmentService.getDepartmentByCode(departmentCode);
            if (formDepartment == null) {
                logger.warn(WRONG_NAME + ' ' + WRONG_NAME_DEPARTMENT, fileName, transportDataParam.getDepartmentCode());
                skip++;
                continue;
            }

            // Указан несуществующий код налоговой формы
            FormType formType = formTypeService.getByCode(formCode);
            if (formType == null) {
                logger.warn(WRONG_NAME + ' ' + WRONG_NAME_FORM_TYPE, fileName, transportDataParam.getDepartmentCode());
                skip++;
                continue;
            }

            // Указан недопустимый код периода
            ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
            if (reportPeriod == null) {
                logger.warn(WRONG_NAME + ' ' + WRONG_NAME_FORM_TYPE, fileName, transportDataParam.getDepartmentCode());
                skip++;
                continue;
            }

            // Назначение подразделению типа и вида НФ
            if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
                logger.warn(ASSIGNATION_ERROR, fileName, formType.getName(), formDepartment.getName());
                skip++;
                continue;
            }
            FormDataKind formDataKind = FormDataKind.PRIMARY; // ТФ только для первичных НФ

            // TODO Проверка ЭЦП (15,16)

            // Поиск экземпляра НФ
            FormData formData;

            if (transportDataParam.getMonth() == null) {
                formData = formDataDao.find(formType.getId(), formDataKind, formDepartment.getId(), reportPeriod.getId());
            } else {
                formData = formDataDao.findMonth(formType.getId(), formDataKind, formDepartment.getId(), reportPeriod.getTaxPeriod().getId(),
                        transportDataParam.getMonth());
            }

            // Экземпляр уже есть и не в статусе «Создана»
            if (formData != null && formData.getState() != WorkflowState.CREATED) {
                Logger fileLogger = new Logger();
                fileLogger.error("Налоговая форма существует и находится в состоянии, отличном от «Создана»! Загрузка файла не выполнена.");
                logger.getEntries().addAll(fileLogger.getEntries());
                moveToErrorDirectory(userInfo, formDepartment.getId(), currentFile, fileLogger);
                skip++;
                continue;
            }

            if (formData != null) {
                logger.info("Налоговая форма существует и имеет статус «Создана».");
            }

            // Загрузка данных в НФ (скрипт)
            Logger fileLogger = new Logger();
            try {
                // Блокировка
                lockCoreService.lock(FormData.class, formData.getId(), userInfo);
                // Загрузка
                importFormData(userInfo, currentFile, formData, formType.getId(), formDepartment.getId(), reportPeriod.getId(), formDataKind,
                        transportDataParam, fileLogger);
                // Забираем вывод скрипта
                logger.getEntries().addAll(fileLogger.getEntries());
            } catch (Exception ex) {
                logger.error("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена. %s.", ex.getMessage());
                // Перемещение в каталог ошибок
                moveToErrorDirectory(userInfo, formDepartment.getId(), currentFile, fileLogger);
                fail++;
                continue;
            } finally {
                // Снимаем блокировку
                lockCoreService.unlock(FormData.class, formData.getId(), userInfo);
            }

            // Файл загружен
            success++;
        }

        return new ImportCounter(success, skip, fail);
    }

    /**
     * Загрузка ТФ конкретной НФ
     */
    private void importFormData(TAUserInfo userInfo, FileWrapper currentFile, FormData formData, int formTypeId, int departmentId,
                                int reportPeriodId, FormDataKind formDataKind, TransportDataParam transportDataParam,
                                Logger logger) {

        // Наличие фатальных ошибок в общем логе не должно откатывать изменения по импорту отдельной формы, если в ней фатальных ошибок нет
        Logger localLogger = new Logger();

        // Если формы нет, то создаем
        if (formData == null) {
            int formTeplateId = formTemplateService.getActiveFormTemplateId(formTypeId, reportPeriodId);
            ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
            long formDataId = formDataService.createFormData(logger, userInfo, formTeplateId, departmentId,
                    formDataKind, reportPeriod, transportDataParam.getMonth());
            formData = formDataDao.get(formDataId, false);
        }

        // Скрипт
        InputStream inputStream = currentFile.getInputStream();
        try {
            formDataService.importFormData(localLogger, userInfo, formData.getId(), inputStream, currentFile.getName(),
                    FormDataEvent.IMPORT_TRANSPORT_FILE);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        // Локальный лог → общий лог
        logger.getEntries().addAll(localLogger.getEntries());

        // Если при выполнении скрипта возникли фатальные ошибки, то
        if (localLogger.containsLevel(LogLevel.ERROR)) {
            // Исключение для отката транзакции сознания и заполнения НФ
            throw new ServiceException(IMPORT_FORM_DATA_SCRIPT_ERROR);
        }

        // Сохранение
        formDataService.saveFormData(logger, userInfo, formData);

        // Перенос в архив
        moveToArchiveDirectory(userInfo, departmentId, currentFile, logger);
    }

    /**
     * Перенос ТФ в каталог ошибок
     * @param userInfo
     * @param formDepartmentId
     * @param errorFileSrc Файл с ошибкой, который должен быть перенесен
     * @param logger
     */
    void moveToErrorDirectory(TAUserInfo userInfo, int formDepartmentId, FileWrapper errorFileSrc, Logger logger) {
        try {
            // Конфигурационные параметры
            ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
            List<String> errorPathList = null;
            if (model != null) {
                errorPathList = model.get(ConfigurationParam.FORM_ERROR_DIRECTORY, formDepartmentId);
            }

            // Проверка наличия каталога в параметрах
            if (errorPathList == null || errorPathList.isEmpty()) {
                logger.error(NO_CATALOG_ERROR_ERROR);
                return;
            }

            // Создание дерева каталогов
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            FileWrapper errorFolderDst = ResourceUtils.getSharedResource(errorPathList.get(0) + calendar.get(Calendar.YEAR)
                    + "/" + Months.fromId(calendar.get(Calendar.MONTH)).getName()
                    + "/" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "/", false);
            errorFolderDst.mkDirs();

            // Создание архива
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(yyyy.MM.dd HH.mm.ss)");

            String path = errorFolderDst.getPath() + "/" + errorFileSrc.getName() + simpleDateFormat.format(calendar.getTime()) + ".zip";
            FileWrapper errorFileDst = ResourceUtils.getSharedResource(path, false);
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(errorFileDst.getOutputStream());
            zaos.setEncoding(ZIP_ENCODING);
            zaos.putArchiveEntry(new ZipArchiveEntry(errorFileSrc.getName()));
            InputStream errorFileSrcInputStream = errorFileSrc.getInputStream();
            IOUtils.copy(errorFileSrcInputStream, zaos);
            zaos.closeArchiveEntry();
            IOUtils.closeQuietly(errorFileSrcInputStream);

            // Файл с логами
            zaos.putArchiveEntry(new ZipArchiveEntry(LOG_FILE_NAME));
            StringBuilder sb = new StringBuilder();
            for (LogEntry logEntry : logger.getEntries()) {
                sb.append(logEntry.getLevel().name() + "\t" + logEntry.getMessage() + "\r\n");
            }
            IOUtils.copy(new ByteArrayInputStream(sb.toString().getBytes()), zaos);
            zaos.closeArchiveEntry();
            IOUtils.closeQuietly(zaos);

            // Удаление
            errorFileSrc.delete();

            // ЖА
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, String.format(MOVE_ERROR_SUCCESS, errorFileDst.getName()));
        } catch (Exception e) {
            // ЖА
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, MOVE_ERROR_ERROR);
            logger.error(MOVE_ERROR_ERROR);
        }
    }

    /**
     * Перенос ТФ в каталог архива
     * @param userInfo
     * @param formDepartmentId
     * @param archiveFileSrc Файл с ошибкой, который должен быть перенесен
     * @param logger
     */
    void moveToArchiveDirectory(TAUserInfo userInfo, int formDepartmentId, FileWrapper archiveFileSrc, Logger logger) {
        try {
            // Конфигурационные параметры
            ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
            List<String> archivePathList = null;
            if (model != null) {
                archivePathList = model.get(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, formDepartmentId);
            }

            // Проверка наличия каталога в параметрах
            if (archivePathList == null || archivePathList.isEmpty()) {
                logger.error(NO_CATALOG_ARCHIVE_ERROR);
                return;
            }

            // Создание дерева каталогов
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            FileWrapper archiveFolderDst = ResourceUtils.getSharedResource(archivePathList.get(0) + calendar.get(Calendar.YEAR)
                    + "/" + Months.fromId(calendar.get(Calendar.MONTH)).getName()
                    + "/" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "/", false);
            archiveFolderDst.mkDirs();

            // Создание архива
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(yyyy.MM.dd HH.mm.ss)");

            String path = archiveFolderDst.getPath() + "/" + archiveFileSrc.getName() + simpleDateFormat.format(calendar.getTime()) + ".zip";
            FileWrapper errorFileDst = ResourceUtils.getSharedResource(path, false);
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(errorFileDst.getOutputStream());
            zaos.setEncoding(ZIP_ENCODING);
            zaos.putArchiveEntry(new ZipArchiveEntry(archiveFileSrc.getName()));
            InputStream errorFileSrcInputStream = archiveFileSrc.getInputStream();
            IOUtils.copy(errorFileSrcInputStream, zaos);
            zaos.closeArchiveEntry();
            IOUtils.closeQuietly(errorFileSrcInputStream);
            IOUtils.closeQuietly(zaos);

            // Удаление
            archiveFileSrc.delete();

            // ЖА
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, String.format(MOVE_ARCHIVE_SUCCESS, errorFileDst.getName()));
        } catch (Exception e) {
            // ЖА
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, MOVE_ARCHIVE_ERROR);
            logger.error(MOVE_ARCHIVE_ERROR);
        }
    }

    @Override
    public List<String> getWorkFilesFromFolder(String folderPath, Set<String> ignoreFileSet) {
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
