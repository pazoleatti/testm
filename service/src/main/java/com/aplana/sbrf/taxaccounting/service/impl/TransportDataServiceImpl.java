package com.aplana.sbrf.taxaccounting.service.impl;

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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
    private FormDataScriptingService formDataScriptingService;

    @Autowired
    private RefBookExternalService refBookExternalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;

    @Autowired
    private FormTypeService formTypeService;

    final static String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    final static String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    final static String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    final static String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";
    final static String NO_CATALOG_UPLOAD_ERROR = "Не указан каталог загрузки в конфигурационных параметрах АС «Учет налогов»!";
    final static String NO_CATALOG_ERROR_ERROR = "Не указан каталог ошибок в конфигурационных параметрах АС «Учет налогов»!";
    final static String UPLOAD_FILE_SUCCESS = "Файл «%s» помещен в каталог загрузки «%s».";
    final static String UPLOAD_FILE_REPLACE = "В каталоге загрузки ранее загруженный файл «%s» был заменен!";
    final static String UPLOAD_ALL = "Загрузка транспортных файлов в каталог загрузки завершена.";
    final static String NO_IMPORT_FILES_ERROR = "В каталоге загрузки не найдены файлы!";
    final static String EXIST_FORM_DATA_ERROR = "Налоговая форма существует и имеет статус «Создана».";
    final static String IMPORT_FORM_DATA_REPORT = "Завершена процедура загрузки транспортных файлов, содержащих данные налоговых форм. Файлов загружено: %d. Файлов отклонено: %d.";
    final static String IMPORT_FORM_DATA_SCRIPT_ERROR = "При загрузке данных налоговой формы из транспортного файла произошли ошибки!";
    final static String MOVE_ARCHIVE_ERROR = "Ошибка при архивировании транспортного файла!";
    final static String MOVE_ARCHIVE_SUCCESS = "Перенос «%s» в каталог архива успешно выполнен!";
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
                            departmentConfId = DepartmentType.ROOT_BANK.getCode();
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
                    departmentConfId = DepartmentType.ROOT_BANK.getCode();
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
        ConfigurationParamModel model = configurationDao.getAll();
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

        // Указан несуществующий код подразделения
        Department formDepartment = departmentService.getDepartmentByCode(departmentCode);
        if (formDepartment == null) {
            logger.warn(WRONG_NAME + ' ' + WRONG_NAME_DEPARTMENT, fileName, transportDataParam.getDepartmentCode());
            return null;
        }

        // Указан несуществующий код налоговой формы
        FormType formType = formTypeService.getByCode(formCode);
        if (formType == null) {
            logger.warn(WRONG_NAME + ' ' + WRONG_NAME_FORM_TYPE, fileName, transportDataParam.getDepartmentCode());
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

    @Override
    public void importDataFromFolder(TAUserInfo userInfo, ConfigurationParam folderParam, Logger logger) {
        importDataFromFolder(userInfo, null, folderParam, logger);
    }

    @Override
    public void importDataFromFolder(TAUserInfo userInfo, List<Department> departmentList,
                                     ConfigurationParam folderParam, Logger logger) {
        if (departmentList == null) {
            // Ручная загрузка
            // TODO Выборка
        }

        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.getAll();
        List<String> uploadPathList = model.get(folderParam, DepartmentType.ROOT_BANK.getCode()); // TODO По подразделениям выборки

        // Проверка наличия каталога в параметрах
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            logger.error(NO_CATALOG_UPLOAD_ERROR);
            return;
        }

        List<String> workFilesList;

        boolean existFiles = false; // Признак наличия хотябы одного ТФ в каталоге
        int fileTotalCnt = 0; // Счетчик обработанных файлов
        int fileAcceptedCnt = 0; // Счетчик загруженных файлов

        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        while (!(workFilesList = getWorkFilesFromFolder(uploadPathList.get(0))).isEmpty()) {
            existFiles = true;
            fileTotalCnt++;
            FileWrapper currentFile = ResourceUtils.getSharedResource(uploadPathList.get(0) + workFilesList.get(0));
            // Обработка файла
            TransportDataParam transportDataParam = TransportDataParam.valueOf(workFilesList.get(0));
            // TODO Маппинг, выполнение скрипта, перемещение в каталог архива или в каталог ошибок (5-7)
            int formTypeId = 0; // TODO Брать из маппинга
            int departmentId = 0; // TODO Брать из маппинга
            int reportPeriodId = 0; // TODO Брать из маппинга
            FormDataKind formDataKind = FormDataKind.PRIMARY; // ТФ только для первичных НФ

            // TODO Проверка ЭЦП (15,16)

            FormData formData; // Экземпляр НФ
            if (transportDataParam.getMonth() == null) {
                formData = formDataDao.find(formTypeId, formDataKind, departmentId, reportPeriodId);
            } else {
                formData = formDataDao.findMonth(formTypeId, formDataKind, departmentId, reportPeriodId,
                        transportDataParam.getMonth());
            }

            // Экземпляр уже есть и не в статусе «Создана»
            if (formData != null && formData.getState() != WorkflowState.CREATED) {
                Logger fileLogger = new Logger();
                fileLogger.error(EXIST_FORM_DATA_ERROR);
                logger.getEntries().addAll(fileLogger.getEntries());
                moveToErrorDirectory(currentFile, userInfo, fileLogger);
                continue;
            }

            // Загрузка данных в НФ отдельной транзакцией
            Logger fileLogger = new Logger();
            try {
                importFormData(userInfo, formData, formTypeId, departmentId, reportPeriodId, formDataKind,
                        transportDataParam, fileLogger);
                logger.getEntries().addAll(fileLogger.getEntries());
            } catch (Exception ex) {
                moveToErrorDirectory(currentFile, userInfo, fileLogger);
                continue;
            }

            // Файл загружен
            fileAcceptedCnt++;
        }

        if (!existFiles) {
            logger.error(NO_IMPORT_FILES_ERROR);
            return;
        }

        // Загрузка завершена TODO Справочники или НФ
        logger.info(IMPORT_FORM_DATA_REPORT, fileAcceptedCnt, fileTotalCnt - fileAcceptedCnt);
    }

    /**
     * Загрузка данных в НФ отдельной транзакцией
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void importFormData(TAUserInfo userInfo, FormData formData, int formTypeId, int departmentId,
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

        // Выполнение события FormDataEvent.IMPORT_TRANSPORT_FILE скриптом НФ
        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.IMPORT_TRANSPORT_FILE, localLogger, null);

        // Локальный лог → общий лог
        logger.getEntries().addAll(localLogger.getEntries());

        // Если при выполнении скрипта возникли фатальные ошибки, то
        if (localLogger.containsLevel(LogLevel.ERROR)) {
            // Исключение для отката транзакции сознания и заполнения НФ
            throw new ServiceException(IMPORT_FORM_DATA_SCRIPT_ERROR);
        }
    }

    /**
     * Перенос ТФ в каталог ошибок
     * @return true/false - успешно/нет
     */
    void moveToErrorDirectory(FileWrapper errorFileSrc, TAUserInfo userInfo, Logger logger) {
        try {
            // Конфигурационные параметры
            ConfigurationParamModel model = configurationDao.getAll();
            List<String> errorPathList = null;
            if (model != null) {
                errorPathList = model.get(ConfigurationParam.FORM_ERROR_DIRECTORY, DepartmentType.ROOT_BANK.getCode());
            }

            // Проверка наличия каталога в параметрах
            if (errorPathList == null || errorPathList.isEmpty()) {
                throw new ServiceException(NO_CATALOG_ERROR_ERROR);
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
            IOUtils.closeQuietly(errorFileSrcInputStream);
            zaos.closeArchiveEntry();

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
                    null, null, null, null, MOVE_ARCHIVE_SUCCESS);
        } catch (IOException e) {
            // ЖА
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, MOVE_ARCHIVE_ERROR);
            // Ошибка перемещения прерывает загрузку всех файлов
            throw new ServiceException(MOVE_ARCHIVE_ERROR, e);
        }
    }

    @Override
    public List<String> getWorkFilesFromFolder(String folderPath) {
        List<String> retVal = new LinkedList<String>();
        FileWrapper catalogFile = ResourceUtils.getSharedResource(folderPath);
        for (String candidateStr : catalogFile.list()) {
            FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + candidateStr);
            // Файл, это файл, а не директория и соответствует формату имени ТФ
            if (candidateFile.isFile() && TransportDataParam.isValidName(candidateStr)) {
                retVal.add(candidateStr);
            }
        }
        return retVal;
    }
}
