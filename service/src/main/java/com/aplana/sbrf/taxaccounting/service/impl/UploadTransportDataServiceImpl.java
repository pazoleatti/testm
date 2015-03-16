package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitriy Levykin
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)//Поменял scope бина, из-за переменных formTypeId, formTypeName
public class UploadTransportDataServiceImpl implements UploadTransportDataService {

	private static final Log LOG = LogFactory.getLog(UploadTransportDataServiceImpl.class);

    //Добавил исключительно для записи в лог
    private Integer formTypeId = null;
    private String formTypeName = null;

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
    final static String U3 = "Файл «%s» не загружен, т.к. пользователь не имеет доступа к содержащейся в нем налоговой форме «%s» подразделения «%s»!";
    final static String U4 = "Загружаемая налоговая форма «%s» подразделения «%s» не относится ни к одному ТБ, " +
            "в связи с чем для нее не существует каталог загрузки в конфигурационных параметрах АС «Учет налогов»!";

    final static String U5 = "Начата загрузка транспортного файла «%s» в каталог загрузки.";
    final static String U5_1 = "Из наименования транспортного файла получены следующие данные:";
    final static String U6_1 = "Код вида НФ: %s, код подразделения: %s, код периода: %s, год: %s.";
    final static String U6_2 = "Код вида НФ: %s, код подразделения: %s, код периода: %s, год: %s, месяц: %s";

    private static final String DIASOFT_NAME = "справочников Diasoft";
    private static final String AVG_COST_NAME = "справочника «Средняя стоимость транспортных средств»";

    // Сообщения, которые не учтены в постановка
    final static String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    final static String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    final static String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    final static String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";

    protected static enum LogData {
        L32("Файл «%s» сохранен в каталоге загрузки «%s».", LogLevel.INFO, true),
        L33("Ошибка при сохранении файла «%s» в каталоге загрузки! %s.", LogLevel.ERROR, true),
        L34_1("Не указан путь к каталогу загрузки %s! Файл «%s» не сохранен.", LogLevel.ERROR, true),
        L34_2("Не указан путь к каталогу загрузки для ТБ «%s» в конфигурационных параметрах АС «Учет налогов». Файл «%s» не сохранен.", LogLevel.ERROR, true),
        L35("Завершена процедура загрузки транспортных файлов в каталог загрузки. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true),
        L37("При загрузке файла «%s» произошла непредвиденная ошибка: %s.", LogLevel.ERROR, true);

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
    public UploadResult uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger) {
        UploadResult uploadResult = new UploadResult();
        ImportCounter importCounter = uploadFileWithoutLog(userInfo, fileName, inputStream,
                uploadResult.getDiasoftFileNameList(), uploadResult.getAvgCostFileNameList(),
                uploadResult.getFormDataFileNameList(), uploadResult.getFormDataDepartmentList(), logger);
        log(userInfo, LogData.L35, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        uploadResult.setSuccessCounter(importCounter.getSuccessCounter());
        uploadResult.setFailCounter(importCounter.getFailCounter());
        return uploadResult;
    }

    private ImportCounter uploadFileWithoutLog(TAUserInfo userInfo, String fileName, InputStream inputStream,
                                               List<String> diasoftFileNameList, List<String> avgCostFileNameList,
                                               List<String> formDataFileNameList, List<Integer> formDataDepartmentList,
                                               Logger logger) {
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
                        CheckResult checkResult = checkFileNameAccess(userInfo, entry.getName(), logger);
                        if (checkResult != null) {
                            try {
                                if (copyFileFromStream(userInfo, zais, checkResult.getPath(), entry.getName(), logger)) {
                                    if (checkResult.isRefBook()) {
                                        if (loadRefBookDataService.isDiasoftFile(entry.getName())) {
                                            diasoftFileNameList.add(entry.getName());
                                        } else {
                                            avgCostFileNameList.add(entry.getName());
                                        }
                                    } else {
                                        formDataFileNameList.add(entry.getName());
                                        formDataDepartmentList.add(checkResult.getDepartmentTbId());
                                    }
                                    success++;
                                } else {
                                    fail++;
                                }
                            } catch (IOException e) {
                                // Ошибка копирования сущности из архива
                                log(userInfo, LogData.L33, logger, entry.getName(), e.getMessage());
                                fail++;
								LOG.error(e.getMessage(), e);
                            }  catch (ServiceException se) {
                                log(userInfo, LogData.L33, logger, entry.getName(), se.getMessage());
                                fail++;
								LOG.error(se.getMessage(), se);
                            }
                        } else {
                            fail++;
                        }
                    }
                } catch (IOException e) {
                    // Ошибка копирования из архива
                    log(userInfo, LogData.L33, logger, fileName, e.getMessage());
                    fail++;
					LOG.error(e.getMessage(), e);
                } catch (ServiceException se) {
                    log(userInfo, LogData.L33, logger, fileName, se.getMessage());
                    fail++;
					LOG.error(se.getMessage(), se);
                } finally {
                    IOUtils.closeQuietly(zais);
                }
            } else {
                // Не архив
                CheckResult checkResult = checkFileNameAccess(userInfo, fileName, logger);
                if (checkResult != null) {
                    try {
                        if (copyFileFromStream(userInfo, inputStream, checkResult.getPath(),
                                fileName, logger)) {
                            if (checkResult.isRefBook()) {
                                if (loadRefBookDataService.isDiasoftFile(fileName)) {
                                    diasoftFileNameList.add(fileName);
                                } else {
                                    avgCostFileNameList.add(fileName);
                                }
                            } else {
                                formDataFileNameList.add(fileName);
                                formDataDepartmentList.add(checkResult.getDepartmentTbId());
                            }
                            success++;
                        } else {
                            fail++;
                        }
                    } catch (IOException e) {
                        // Ошибка копирования файла
                        log(userInfo, LogData.L33, logger, fileName, e.getMessage());
                        fail++;
						LOG.error(e.getMessage(), e);
                    } catch (ServiceException se) {
                        log(userInfo, LogData.L33, logger, fileName, se.getMessage());
                        fail++;
						LOG.error(se.getMessage(), se);
                    }
                } else {
                    fail++;
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
            FileWrapper file = ResourceUtils.getSharedResource(folderPath + "/" + fileName, false);
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
     * Вывод частей имени файла с учетом возможного null-значения
     */
    private String getFileNamePart(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    /**
     * Проверка имени файла и проверка доступа к соответствующим НФ
     * http://conf.aplana.com/pages/viewpage.action?pageId=13111363
     * Возвращает путь к каталогу, если проверка прошла.
     */
    private CheckResult checkFileNameAccess(TAUserInfo userInfo, String fileName, Logger logger) {
        try {
            boolean isDiasoftRefBook = loadRefBookDataService.isDiasoftFile(fileName);
            boolean isAvgCostRefBook = loadRefBookDataService.isAvgCostFile(fileName);
            boolean isFormData = TransportDataParam.isValidName(fileName);

            CheckResult checkResult = new CheckResult();

            if (isDiasoftRefBook) {
                // Справочники не проверяем
                checkResult.setPath(getUploadPath(userInfo, fileName, ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, 0,
                        DIASOFT_NAME, LogData.L34_1, logger));
                checkResult.setRefBook(true);
                return checkResult;
            }

            if (isAvgCostRefBook) {
                checkResult.setPath(getUploadPath(userInfo, fileName, ConfigurationParam.AVG_COST_UPLOAD_DIRECTORY, 0,
                        AVG_COST_NAME, LogData.L34_1, logger));
                checkResult.setRefBook(true);
                return checkResult;
            }

            // Не справочники Diasoft и не ТФ НФ
            if (!isFormData) {
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

            // Вывод результата разбора имени файла
            logger.info(U5, fileName);
            logger.info(U5_1, fileName);
            if (transportDataParam.getMonth() == null) {
                logger.info(U6_1, getFileNamePart(formCode), getFileNamePart(departmentCode),
                        getFileNamePart(reportPeriodCode), getFileNamePart(year));
            } else {
                logger.info(U6_2, getFileNamePart(formCode), getFileNamePart(departmentCode),
                        getFileNamePart(reportPeriodCode), getFileNamePart(year),
                        getFileNamePart(transportDataParam.getMonth()));
            }

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
                    Arrays.asList(formType.getTaxType()), null, null);

            if (!departmentList.contains(formDepartment.getId())) {
                logger.warn(U3, fileName, formType.getName(), formDepartment.getName());
                return null;
            }

            // Назначение подразделению типа и вида НФ
            if (!departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.PRIMARY) &&
                    !departmentFormTypeDao.existAssignedForm(formDepartment.getId(), formType.getId(), FormDataKind.ADDITIONAL)) {
                logger.warn(U3, fileName, formType.getName(), formDepartment.getName());
                return null;
            }

            checkResult.setRefBook(false);

            // ТБ, к которому относится подразделение, код которого содержится в имени ТФ
            Department parentTB = departmentService.getParentTB(formDepartment.getId());
            if (parentTB == null) {
                logger.warn(U4, formType.getName(), formDepartment.getName());
                return null;
            }
            Integer departmentTbId = parentTB.getId();

            checkResult.setDepartmentTbId(departmentTbId);
            checkResult.setPath(getUploadPath(userInfo, fileName, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentTbId,
                    DIASOFT_NAME, LogData.L34_2, logger));
            formTypeId = formType.getId();
            formTypeName = formType.getName();

            return checkResult;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            log(userInfo, LogData.L37, logger, fileName, e.getMessage());
            return null;
        }
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(TAUserInfo userInfo, String fileName, ConfigurationParam configurationParam,
                                 int departmentId, String refBookName, LogData logData, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            if (logData == LogData.L34_1) {
                log(userInfo, logData, logger, refBookName, fileName);
            } else if (logData == LogData.L34_2) {
                log(userInfo, logData, logger, departmentService.getDepartment(departmentId).getName(), fileName);
            }
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
            String prefix = "";
            if (userInfo != null) {
                departmentId = userInfo.getUser().getDepartmentId();
                if (userInfo.getUser().getId() == TAUser.SYSTEM_USER_ID) {
                    prefix = "Событие инициировано Системой. ";
                }
            }
            auditService.add(FormDataEvent.UPLOAD_TRANSPORT_FILE, userInfo, departmentId, null,
                    null, formTypeName, null, prefix + String.format(logData.getText(), args), null, formTypeId);
        }
    }

    private class CheckResult {
        private String path;
        private boolean refBook;
        Integer departmentTbId;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isRefBook() {
            return refBook;
        }

        public void setRefBook(boolean refBook) {
            this.refBook = refBook;
        }

        public Integer getDepartmentTbId() {
            return departmentTbId;
        }

        public void setDepartmentTbId(Integer departmentTbId) {
            this.departmentTbId = departmentTbId;
        }
    }
}
