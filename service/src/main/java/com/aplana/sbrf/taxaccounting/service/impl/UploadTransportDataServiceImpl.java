package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.service.impl.LoadDeclarationDataServiceImpl.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)//Поменял scope бина, из-за переменных formTypeId, formTypeName
public class UploadTransportDataServiceImpl implements UploadTransportDataService {

	private static final Log LOG = LogFactory.getLog(UploadTransportDataServiceImpl.class);

    //Добавил исключительно для записи в лог
    private String declarationTypeName = null;
    public static final String TAG_DOCUMENT = "Документ";
    public static final String ATTR_PERIOD = "Период";
    public static final String ATTR_YEAR = "ОтчетГод";

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private LoadRefBookDataService loadRefBookDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private SourceService sourceService;

    private static final long REF_BOOK_DEPARTMENT = 30L; // Подразделения
    private static final long REF_BOOK_PERIOD_DICT = 8L; // Коды отчетных периодов
    private static final String SBRF_CODE_ATTR_NAME = "SBRF_CODE";

    // Сообщения при загрузке в каталоги
    static final String U1 = "В каталоге загрузки ранее загруженный файл «%s» был заменен!";
    static final String U2 = "Файл «%s» не загружен, так как:";
    static final String U2_0 = "Файл «%s» не загружен, т.к. имеет некорректный формат имени!";
    static final String U2_1 = "В справочнике «%s» отсутствует подразделение, поле «%s» которого равно «%s»!";
    static final String U2_1_ASNU = "В справочнике «%s» отсутствует код АСНУ, поле «%s» которого равно «%s»!";
    static final String U2_2 = "В Системе отсутствует налоговая форма с кодом «%s»!";
    static final String U2_3 = "Для вида налога «%s» в Системе не создан период с кодом «%s»%s, календарный год «%s»!";
    static final String U2_5 = "В наименовании файла должен быть указан код месяца, т.к. налоговая форма с кодом «%s» ежемесячная!";
    static final String U2_6 = "Код месяца «%s» не соответствует коду периода «%s»%s!";
    static final String U2_7 = "В наименовании файла не должно быть кода месяца, т.к. налоговая форма с кодом «%s» неежемесячная!";
    static final String U3 = "Файл «%s» не загружен, так как ";
    static final String U3_1 = "подразделение «%s» недоступно текущему пользователю!";
    static final String U3_3 = "подразделение «%s» является недействующим (справочник «%s»)!";
    static final String U3_6 = "для подразделения «%s» не назначено декларации «%s»!";
    static final String U3_4 = "для вида налога «%s» закрыт (либо еще не открыт) период с кодом «%s»%s, календарный год «%s»!";
    static final String U3_5 = "налоговая форма уже существует";
    static final String U4 = "Загружаемая налоговая форма «%s» подразделения «%s» не относится ни к одному ТБ, " +
            "в связи с чем для нее не существует каталог загрузки в конфигурационных параметрах АС «Учет налогов»!";

    static final String U5 = "Начата загрузка транспортного файла «%s» в каталог загрузки.";
    static final String U5_1 = "Из наименования транспортного файла получены следующие данные:";
    static final String U6_1 = "Код вида НФ: %s, код подразделения: %s, код периода: %s, год: %s.";
    static final String U6_2 = "Код вида НФ: %s, код подразделения: %s, код периода: %s, год: %s, месяц: %s";
    static final String U6_3 = "Код подразделения: %s, код периода: %s, год: %s, КПП: %s, код АСНУ: %s, GUID: %s";

    private static final String DIASOFT_NAME = "справочников Diasoft";
    private static final String AVG_COST_NAME = "справочника «Средняя стоимость транспортных средств»";

    // Сообщения, которые не учтены в постановка
    static final String USER_NOT_FOUND_ERROR = "Не определен пользователь!";
    static final String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    static final String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    static final String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";

    protected enum LogData {
        L32("Файл «%s» сохранен в каталоге загрузки «%s».", LogLevel.INFO, true),
        L33("Ошибка при сохранении файла «%s» в каталоге загрузки! %s.", LogLevel.ERROR, true),
        L34_1_1("Не указан путь к каталогу загрузки %s! Файл «%s» не сохранен.", LogLevel.ERROR, true),
        L34_1_2("Не указан путь к каталогу загрузки для ТБ «%s» в конфигурационных параметрах АС «Учет налогов». Файл «%s» не сохранен.", LogLevel.ERROR, true),
        L34_2_1("Нет доступа к каталогу загрузки %s! Файл «%s» не сохранен.", LogLevel.ERROR, true),
        L34_2_2("Нет доступа к каталогу загрузки для ТБ «%s» в конфигурационных параметрах АС «Учет налогов». Файл «%s» не сохранен.", LogLevel.ERROR, true),
        L35("Завершена процедура загрузки транспортных файлов в каталог загрузки. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true),
        L37("При загрузке файла «%s» произошла непредвиденная ошибка: %s.", LogLevel.ERROR, true),
        L48("Для деклараций загружаемого файла «%s» не предусмотрена обработка транспортного файла! Загрузка не выполнена.", LogLevel.ERROR, true);

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
    static final String ZIP_ENCODING = "cp866";

    @Override
    public UploadResult uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger) {
        UploadResult uploadResult = new UploadResult();
        ImportCounter importCounter = uploadFileWithoutLog(userInfo, fileName, inputStream,
                uploadResult.getFormDataFileNameList(), uploadResult.getFormDataDepartmentList(), logger);
        log(userInfo, LogData.L35, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        uploadResult.setSuccessCounter(importCounter.getSuccessCounter());
        uploadResult.setFailCounter(importCounter.getFailCounter());
        return uploadResult;
    }

    private ImportCounter uploadFileWithoutLog(TAUserInfo userInfo, String fileName, InputStream inputStream,
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
                        if (copyFile(zais, entry.getName(), formDataFileNameList, formDataDepartmentList, userInfo, logger)) {
                            success++;
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
                try {
                    if (copyFile(inputStream, fileName, formDataFileNameList, formDataDepartmentList, userInfo, logger)) {
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
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return new ImportCounter(success, fail);
    }

    private boolean copyFile(InputStream inputStream, String fileName, List<String> formDataFileNameList, List<Integer> formDataDepartmentList, TAUserInfo userInfo, Logger logger) throws IOException {
        File dataFile = null;
        try {
            dataFile = File.createTempFile("dataFile", ".original");
            OutputStream dataFileOutputStream = new FileOutputStream(dataFile);
            try {
                IOUtils.copy(inputStream, dataFileOutputStream);
            } finally {
                IOUtils.closeQuietly(dataFileOutputStream);
            }

            CheckResult checkResult;
            InputStream checkFileIS = null;
            try {
                checkFileIS = new FileInputStream(dataFile);
                checkResult = checkFileNameAccess(userInfo, checkFileIS, fileName, logger);
            } finally {
                IOUtils.closeQuietly(checkFileIS);
            }
            if (checkResult != null) {
                InputStream copyFileIS = null;
                try {
                    copyFileIS = new FileInputStream(dataFile);
                    if (copyFileFromStream(userInfo, copyFileIS, checkResult, fileName, logger)) {
                        if (!checkResult.isRefBook()) {
                            formDataFileNameList.add(fileName);
                            formDataDepartmentList.add(checkResult.getDepartmentTbId());
                        }
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    // Ошибка копирования сущности из архива
                    log(userInfo, LogData.L33, logger, fileName, e.getMessage());
                    LOG.error(e.getMessage(), e);
                    return false;
                } catch (ServiceException se) {
                    log(userInfo, LogData.L33, logger, fileName, se.getMessage());
                    LOG.error(se.getMessage(), se);
                    return false;
                } finally {
                    IOUtils.closeQuietly(copyFileIS);
                }
            } else {
                return false;
            }
        } finally {
            if (dataFile != null) {
                dataFile.delete();
            }
        }
        return true;
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
     * Копирование файла из потока в каталог загрузки
     */
    private boolean copyFileFromStream(TAUserInfo userInfo, InputStream inputStream, CheckResult checkResult, String fileName, Logger logger)
            throws IOException {
        if (checkResult.getPath() != null) {
            if (!checkPath(checkResult.getPath())) {
                if (checkResult.isRefBook()) {
                    log(userInfo, LogData.L34_2_1, logger, checkResult.getRefBookName(), fileName);
                } else {
                    log(userInfo, LogData.L34_2_2, logger, departmentService.getDepartment(checkResult.getDepartmentTbId()).getName(), fileName);
                }
                return false;
            }
            FileWrapper file = ResourceUtils.getSharedResource(checkResult.getPath() + "/" + fileName, false);
            boolean exist = file.exists();
            OutputStream outputStream = file.getOutputStream();
			try {
            	IOUtils.copy(inputStream, outputStream);
			} finally {
            	IOUtils.closeQuietly(outputStream);
			}
            log(userInfo, LogData.L32, logger, fileName, checkResult.getPath());
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
    private CheckResult checkFileNameAccess(TAUserInfo userInfo, InputStream inputStream, String fileName, Logger logger) {
        try {
            // Не ТФ декларации
            if (!TransportDataParam.isValidDecName(fileName, inputStream)) {
                logger.warn(U2_0, fileName);
                return null;
            }

            CheckResult checkResult = new CheckResult();

            // Параметры из имени файла
            TransportDataParam transportDataParam = TransportDataParam.valueOfDec(fileName, inputStream);
            String reportPeriodCode = transportDataParam.getReportPeriodCode();
            Integer year = transportDataParam.getYear();
            String departmentCode = transportDataParam.getDepartmentCode();
            String asnuCode = transportDataParam.getAsnuCode();
            String guid = transportDataParam.getGuid();
            String kpp = transportDataParam.getKpp();
            Integer declarationTypeId = transportDataParam.getDeclarationTypeId();

            if (declarationTypeId == 200) {
                departmentCode = "18_0000_00"; // ToDo нужно определять по КПП
            }

            // Вывод результата разбора имени файла
            logger.info(U5, fileName);
            logger.info(U5_1, fileName);
            logger.info(U6_3, getFileNamePart(departmentCode),
                    getFileNamePart(reportPeriodCode), getFileNamePart(year),
                    getFileNamePart(kpp), getFileNamePart(asnuCode), getFileNamePart(guid));

            // Не задан код подразделения/период/год
            if (departmentCode == null || reportPeriodCode == null || year == null) {
                logger.warn(U2_0, fileName);
                return null;
            }

            // Указан несуществующий код налоговой формы
            DeclarationType declarationType = declarationTypeService.get(declarationTypeId);
            declarationTypeName = declarationType.getName();

            // Указан несуществующий код подразделения
            Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode, false);
            if (formDepartment == null) {
                logger.warn(U2, fileName);
                RefBook refBook = refBookDao.get(REF_BOOK_DEPARTMENT);
                logger.warn(U2_1, refBook.getName(), refBook.getAttribute(SBRF_CODE_ATTR_NAME).getName(), transportDataParam.getDepartmentCode());
                return null;
            }

            // АСНУ
            Long asnuId = null;
            if (asnuCode != null) {
                RefBookDataProvider asnuProvider = rbFactory.getDataProvider(900L);
                List<Long> asnuIds = asnuProvider.getUniqueRecordIds(null, "CODE = '" + asnuCode + "'");
                if (asnuIds.size() != 1) {
                    RefBook refBook = refBookDao.get(900L);
                    logger.warn(U2, fileName);
                    logger.warn(U2_1_ASNU, refBook.getName(), refBook.getAttribute("CODE").getName(), transportDataParam.getAsnuCode());
                    return null;
                } else {
                    asnuId = asnuIds.get(0);
                }
            }

            String reportPeriodName = "";
            String filter = "CODE='" + reportPeriodCode + "' AND " + declarationType.getTaxType().getCode() + "=1";
            PagingResult<Map<String, RefBookValue>> reportPeriodDicts = refBookDao.getRecords(REF_BOOK_PERIOD_DICT, null, null, filter, null);
            if (reportPeriodDicts.size() == 1) {
                reportPeriodName = reportPeriodDicts.get(0).get("NAME").getStringValue();
                if (reportPeriodName != null && !reportPeriodName.isEmpty()) {
                    reportPeriodName = " (" + reportPeriodName + ")";
                }
            }
            // Указан недопустимый код периода
            ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(declarationType.getTaxType(), reportPeriodCode, year);
            if (reportPeriod == null) {
                logger.warn(U2, fileName);
                logger.warn(U2_3, declarationType.getTaxType().getName(), reportPeriodCode, reportPeriodName, year);
                return null;
            }

            //
            Integer declarationTemplateId;
            try {
                declarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationType.getId(), reportPeriod.getId());
            } catch (Exception e) {
                logger.warn(U3 + e.getMessage(), fileName);
                return null;
            }
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);

            if (!TAAbstractScriptingServiceImpl.canExecuteScript(declarationTemplateService.getDeclarationTemplateScript(declarationTemplateId), FormDataEvent.IMPORT_TRANSPORT_FILE)) {
                log(userInfo, LogData.L48, logger, fileName);
                return null;
            }

            // 40 - Выборка для доступа к экземплярам НФ/деклараций
            List<Integer> departmentList = departmentService.getTaxFormDepartments(userInfo.getUser(),
                    Arrays.asList(declarationType.getTaxType()), null, null);
            if (!departmentList.contains(formDepartment.getId())) {
                logger.warn(U3 + U3_1, fileName, formDepartment.getName());
                return null;
            }

            // Неактивное подразделение
            if (!formDepartment.isActive()) {
                RefBook refBook = refBookDao.get(REF_BOOK_DEPARTMENT);
                logger.warn(U3 + U3_3, fileName, formDepartment.getName(), refBook.getName());
                return null;
            }

            // Назначение подразделению типа и вида НФ
            List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(formDepartment.getId(),
                    declarationTemplate.getType().getTaxType(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
            boolean found = false;
            for (DepartmentDeclarationType ddt : ddts) {
                if (ddt.getDeclarationTypeId() == declarationType.getId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                logger.warn(U3 + U3_6, fileName, formDepartment.getName(), declarationType.getName());
            }

            // Период отсутствует либо закрыт
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(formDepartment.getId(), reportPeriod.getId());
            if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
                logger.warn(U3 + U3_4, fileName, declarationType.getTaxType().getName(), reportPeriodCode, reportPeriodName, year);
                return null;
            }

            DeclarationData declarationData = declarationDataService.find(declarationTemplateId, departmentReportPeriod.getId(), null, kpp, null, asnuId, guid);

            // Экземпляр уже есть и не в статусе «Создана»
            if (declarationData != null) {
                logger.warn(U3 + U3_5, fileName);
                return null;
            }

            checkResult.setRefBook(false);

            // ТБ, к которому относится подразделение, код которого содержится в имени ТФ
            Department parentTB = departmentService.getParentTB(formDepartment.getId());
            if (parentTB == null) {
                logger.warn(U4, declarationType.getName(), formDepartment.getName());
                return null;
            }
            Integer departmentTbId = parentTB.getId();

            checkResult.setDepartmentTbId(departmentTbId);
            checkResult.setPath(getUploadPath(userInfo, fileName, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentTbId,
                    null, LogData.L34_1_2, logger));

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
            if (logData == LogData.L34_1_1) {
                log(userInfo, logData, logger, refBookName, fileName);
            } else if (logData == LogData.L34_1_2) {
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
            String prefix = "";
            if (userInfo != null) {
                if (userInfo.getUser().getId() == TAUser.SYSTEM_USER_ID) {
                    prefix = "Событие инициировано Системой. ";
                }
            }
            auditService.add(FormDataEvent.UPLOAD_TRANSPORT_FILE, userInfo, null, null,
                    declarationTypeName, null, null, prefix + String.format(logData.getText(), args), null);
        }
    }

    private class CheckResult {
        private String path;
        private boolean refBook;
        private String refBookName;
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

        public String getRefBookName() {
            return refBookName;
        }

        public void setRefBookName(String refBookName) {
            this.refBookName = refBookName;
        }

        public Integer getDepartmentTbId() {
            return departmentTbId;
        }

        public void setDepartmentTbId(Integer departmentTbId) {
            this.departmentTbId = departmentTbId;
        }
    }
}
