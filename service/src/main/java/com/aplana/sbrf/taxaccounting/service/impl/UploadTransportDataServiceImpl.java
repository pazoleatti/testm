package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional
public class UploadTransportDataServiceImpl implements UploadTransportDataService {

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private LoadRefBookDataService loadRefBookDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private FormTypeService formTypeService;

    // Сообщения при загрузке в каталоги
    final static String U1 = "В каталоге загрузки ранее загруженный файл «%s» был заменен!";
    final static String U2 = "Файл «%s» не загружен, т.к. имеет некорректный формат имени!";
    final static String U2_1 = " Код подразделения «%s» не существует в АС «Учет налогов»!";
    final static String U2_2 = " Код налоговой формы «%s» не существует в АС «Учет налогов»!";
    final static String U2_3 = " Код отчетного периода «%s» не существует в налоговом периоде %d года в АС «Учет налогов»!";
    final static String U3 = "Файл «%s» не загружен, т.к. текущий пользователь не имеет доступа к содержащейся в нем налоговой форме «%s» подразделения «%s»!";

    // Сообщения, которые не учтены в постановка
    final static String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    final static String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    final static String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    final static String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";

    protected static enum LogData {
        L32("Файл «%s» сохранен в каталоге загрузки «%s».", LogLevel.INFO, true),
        L33("Ошибка при сохранении файла «%s» в каталоге загрузки! %s.", LogLevel.ERROR, false),
        L34_1("Не указан путь к каталогу загрузки справочников Diasoft! Файл «%s» не сохранен.", LogLevel.ERROR, false),
        L34_2("Не указан каталог загрузки для ТБ «%s» в конфигурационных параметрах АС «Учет налогов».", LogLevel.ERROR, false),
        L35("Завершена процедура загрузки транспортных файлов в каталог загрузки. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, false);

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

    // Константы
    final static String ZIP_ENCODING = "cp866";

    @Override
    public List<String> uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger) {
        List<String> loadedFileNameList = new LinkedList<String>();
        ImportCounter importCounter = uploadFileWithoutLog(userInfo, fileName, inputStream, loadedFileNameList, logger);
        log(userInfo, LogData.L35, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return loadedFileNameList;
    }

    private ImportCounter uploadFileWithoutLog(TAUserInfo userInfo, String fileName, InputStream inputStream, List<String> loadedFileNameList, Logger logger) {
        // Проверка прав
        if (userInfo == null) {
            logger.error(USER_NOT_FOUND_ERROR);
            return new ImportCounter(0, 1);
        }

        if (!userInfo.getUser().hasRole(TARole.ROLE_OPER)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            logger.error(ACCESS_DENIED_ERROR);
            return new ImportCounter(0, 1);
        }

        if (fileName == null) {
            logger.error(NO_FILE_NAME_ERROR);
            return new ImportCounter(0, 1);
        }

        if (inputStream == null) {
            logger.error(EMPTY_INPUT_STREAM_ERROR);
            return new ImportCounter(0, 1);
        }

        // Счетчики
        int success = 0;
        int fail = 0;
        try {
            if (fileName.toLowerCase().endsWith(".zip")) {
                // Архив — извлекаем все содержимое
                ZipArchiveInputStream zais = new ZipArchiveInputStream(inputStream, ZIP_ENCODING);
                ArchiveEntry entry;
                try {
                    while ((entry = zais.getNextEntry()) != null) {
                        String path = checkFormDataAccess(userInfo, entry.getName(), logger);
                        if (path != null) {
                            try {
                                if (copyFileFromStream(userInfo, zais, path, entry.getName(), logger)) {
                                    loadedFileNameList.add(entry.getName());
                                    success++;
                                } else {
                                    fail++;
                                }
                            } catch (IOException e) {
                                // Ошибка копирования сущности из архива
                                log(userInfo, LogData.L33, logger, entry.getName(), e.getMessage());
                                fail++;
                            }
                        }
                    }
                } catch (IOException e) {
                    // Ошибка копирования из архива
                    log(userInfo, LogData.L33, logger, fileName, e.getMessage());
                    fail++;
                } finally {
                    IOUtils.closeQuietly(zais);
                }
            } else {
                // Не архив
                String path = checkFormDataAccess(userInfo, fileName, logger);
                if (path != null) {
                    try {
                        if (copyFileFromStream(userInfo, inputStream, path,
                                fileName, logger)) {
                            loadedFileNameList.add(fileName);
                            success++;
                        } else {
                            fail++;
                        }
                    } catch (IOException e) {
                        // Ошибка копирования файла
                        log(userInfo, LogData.L33, logger, fileName, e.getMessage());
                        fail++;
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return new ImportCounter(success, fail);
    }

    /**
     * Копирование файла из потока в каталог загрузки
     */
    private boolean copyFileFromStream(TAUserInfo userInfo, InputStream inputStream, String folderPath, String fileName, Logger logger)
            throws IOException {
        if (folderPath != null) {
            FileWrapper file = ResourceUtils.getSharedResource(folderPath + fileName, false);
            boolean exist = file.exists();
            OutputStream outputStream = file.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(outputStream);
            log(userInfo, LogData.L32, logger, fileName, folderPath);
            if (exist) {
                logger.info(U1, fileName);
            }
            return true;
        }
        return false;
    }

    /**
     * Проверка имени файла и проверка доступа к соответствующим НФ
     * http://conf.aplana.com/pages/viewpage.action?pageId=13111363
     * Возвращает путь к каталогу, если проверка прошла.
     */
    private String checkFormDataAccess(TAUserInfo userInfo, String fileName, Logger logger) {
        boolean isDiasoftRefBook = loadRefBookDataService.isDiasoftFile(fileName);
        boolean isFormData = TransportDataParam.isValidName(fileName);

        if (isDiasoftRefBook) {
            // Справочники не проверяем
            return getUploadPath(userInfo, fileName, ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, 0, LogData.L34_1, logger);
        }

        // Не справочники Diasoft и не ТФ НФ
        if (!isDiasoftRefBook && !isFormData) {
            logger.warn(U2, fileName);
            return null;
        }

        //// НФ

        // Параметры из имени файла
        TransportDataParam transportDataParam = TransportDataParam.valueOf(fileName);
        String formCode = transportDataParam.getFormCode();
        String reportPeriodCode = transportDataParam.getReportPeriodCode();
        Integer year = transportDataParam.getYear();
        String departmentCode = transportDataParam.getDepartmentCode();

        // Не задан код подразделения или код формы
        if (departmentCode == null || formCode == null || reportPeriodCode == null || year == null) {
            logger.warn(U2, fileName);
            return null;
        }

        // Указан несуществующий код налоговой формы
        FormType formType = formTypeService.getByCode(formCode);
        if (formType == null) {
            logger.warn(U2 + U2_2, fileName, formCode);
            return null;
        }

        // Указан несуществующий код подразделения
        Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode);
        if (formDepartment == null) {
            logger.warn(U2 + U2_1, fileName, transportDataParam.getDepartmentCode());
            return null;
        }

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(formType.getTaxType(), reportPeriodCode, year);
        if (reportPeriod == null) {
            logger.warn(U2 + U2_3, fileName, reportPeriodCode, year);
            return null;
        }

        // 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Integer> departmentList = departmentService.getTaxFormDepartments(userInfo.getUser(),
                Arrays.asList(TaxType.INCOME), null, null);

        if (!departmentList.contains(formDepartment.getId())) {
            logger.warn(U3, fileName, formType.getName(), formDepartment.getName());
            return null;
        }

        // Назначение подразделению типа и вида НФ
        if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY)) {
            logger.warn(U3, fileName, formType.getName(), formDepartment.getName());
            return null;
        }

        String retVal = getUploadPath(userInfo, fileName, ConfigurationParam.FORM_UPLOAD_DIRECTORY, formDepartment.getId(),
                LogData.L34_2, logger);

        if (retVal == null) {
            logger.warn(U3, fileName, formType.getName(), formDepartment.getName());
            return null;
        }

        return retVal;
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(TAUserInfo userInfo, String fileName, ConfigurationParam configurationParam,
                                 int departmentId, LogData logData, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            String param = "";
            if (logData == LogData.L34_1) {
                param = fileName;
            } else if (logData == LogData.L34_2) {
                param = departmentService.getDepartment(departmentId).getName();
            }

            log(userInfo, logData, logger, param);
            return null;
        }
        return uploadPathList.get(0);
    }

    /**
     * Логгирование в области уведомлений и ЖА при импорте из ТФ
     */
    protected final void log(TAUserInfo userInfo, LogData logData, Logger logger, Object... args) {
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
            Integer departmentId = null;
            if (userInfo != null) {
                departmentId = userInfo.getUser().getDepartmentId();
            }
            String prefix = "";
            if (userInfo.getUser().getId() == TAUser.SYSTEM_USER_ID) {
                prefix = "Событие инициировано Системой. ";
            }
            auditService.add(FormDataEvent.UPLOAD_TRANSPORT_FILE, userInfo, departmentId, null,
                    null, null, null, prefix + String.format(logData.getText(), args));
        }
    }
}
