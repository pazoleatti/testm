package com.aplana.sbrf.taxaccounting.service.impl;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.dao.refbook.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.AppFileUtils;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.departmentConfigs.DepartmentConfigsReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.persons.PersonsReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookCSVReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookExcelReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.sourcesAndDestinations.SourcesAndDestinationsReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.transportMessages.ExcelTransportMessagesReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.BatchIterator;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.aplana.sbrf.taxaccounting.model.util.StringUtils.containsAll;
import static org.apache.commons.lang3.StringUtils.*;

@Service
public class PrintingServiceImpl implements PrintingService {

    private static final Log LOG = LogFactory.getLog(PrintingServiceImpl.class);

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentConfigService departmentConfigService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DeclarationDataService declarationService;
    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;
    @Autowired
    private RefBookDocTypeDao refBookDocTypeDao;
    @Autowired
    private RefBookCountryDao refBookCountryDao;
    @Autowired
    private RefBookTaxpayerStateDao refBookTaxpayerStateDao;
    @Autowired
    private RefBookAsnuDao refBookAsnuDao;
    @Autowired
    private SourceService sourceService;

    @Override
    public String generateCsvLogEntries(List<LogEntry> logEntries) {
        String reportPath = null;
        try {
            LogEntryReportBuilder builder = new LogEntryReportBuilder(logEntries);
            reportPath = builder.createReport();
            return blobDataService.create(reportPath, "errors.csv");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + LogEntryReportBuilder.class);
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public String generateCsvNotificationsLogsArchive(List<Notification> notifications) {
        File zipFile = null;
        List<String> tmpFiles = new ArrayList<>();
        Multiset<String> fileNames = HashMultiset.create();
        try {
            zipFile = File.createTempFile("archive", ".zip");
            FileOutputStream fileOut = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fileOut);

            for (Notification notification : notifications) {
                String logUuid = notification.getLogId();
                if (isNotEmpty(logUuid)) {
                    List<LogEntry> logs = logEntryService.getAll(logUuid);

                    LogEntryReportBuilder builder = new LogEntryReportBuilder(logs);
                    String reportFilePath = builder.createReport();
                    tmpFiles.add(reportFilePath);

                    byte[] reportFileBytes = Files.readAllBytes(Paths.get(reportFilePath));

                    String reportName = createReportNameForNotification(notification);
                    // Исключаем дублирующиеся имена файлов.
                    fileNames.add(reportName);
                    int nameOccurrences = fileNames.count(reportName);
                    // Если уже такой был, file.csv -> file (2).csv
                    if (nameOccurrences > 1) {
                        reportName = substringBeforeLast(reportName, ".csv") + " (" + nameOccurrences + ").csv";
                    }

                    ZipEntry zipEntry = new ZipEntry(reportName);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(reportFileBytes);
                }
            }
            zipOut.close();
            fileOut.close();

            String fileName = "Протоколы оповещений_" + currentMoscowDateTime() + ".zip";
            return blobDataService.create(zipFile, fileName, new Date());

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("");
        } finally {
            AppFileUtils.deleteTmp(zipFile);
            for (String tmpFile : tmpFiles) {
                AppFileUtils.deleteTmp(tmpFile);
            }
        }
    }


    private class NotificationType {
        final String operation;
        final String result;

        NotificationType(String operation, String result) {
            this.operation = operation;
            this.result = result;
        }
    }

    private static final String OPERATION_IMPORT = "Zagr";
    private static final String OPERATION_CHECK = "Prov";
    private static final String OPERATION_IDENTIFICATION = "Iden";
    private static final String OPERATION_ACCEPTING = "Prin";
    private static final String OPERATION_REPORT = "Otch";

    // Символы - латинские.
    private static final String RESULT_EXECUTED = "B";
    private static final String RESULT_NOT_EXECUTED = "H";
    private static final String RESULT_EXECUTED_WITH_ERRORS = "BF";

    private NotificationType getNotificationType(Notification notification) {
        String text = notification.getText();

        if (containsAll(text, "Загрузка файла", "Выполнено создание налоговой формы", ".xml")) {
            return new NotificationType(OPERATION_IMPORT, RESULT_EXECUTED);
        }
        if (containsAll(text, "Ошибка загрузки файла", ".xml")) {
            return new NotificationType(OPERATION_IMPORT, RESULT_NOT_EXECUTED);
        }
        if (text.contains("Выполнена проверка налоговой формы") && !containsAny(text, "фаталь", "отмен")) {
            return new NotificationType(OPERATION_CHECK, RESULT_EXECUTED);
        }
        if (containsAll(text, "Выполнена проверка налоговой формы", "фаталь") && !text.contains("отмен")) {
            return new NotificationType(OPERATION_CHECK, RESULT_EXECUTED_WITH_ERRORS);
        }
        if (text.contains("Не выполнена операция \"Проверка\" для налоговой формы") && !text.contains("отмен")) {
            return new NotificationType(OPERATION_CHECK, RESULT_NOT_EXECUTED);
        }
        if (text.contains("Операция \"Идентификация ФЛ\" выполнена для налоговой формы") && !text.contains("отмен")) {
            return new NotificationType(OPERATION_IDENTIFICATION, RESULT_EXECUTED);
        }
        if (text.contains("Не выполнена операция \"Идентификация ФЛ\" для налоговой формы") && !text.contains("отмен")) {
            return new NotificationType(OPERATION_IDENTIFICATION, RESULT_NOT_EXECUTED);
        }
        if (text.contains("Успешно выполнено принятие налоговой формы")) {
            return new NotificationType(OPERATION_ACCEPTING, RESULT_EXECUTED);
        }
        if (text.contains("Не выполнена операция \"Принятие")) {
            return new NotificationType(OPERATION_ACCEPTING, RESULT_NOT_EXECUTED);
        }
        if (text.contains("Сформирован отчет")) {
            return new NotificationType(OPERATION_REPORT, RESULT_EXECUTED);
        }
        return null;
    }

    private static Map<String, String> TB_CODES = new HashMap<>();

    static {
        TB_CODES.put("40", "SRB");
        TB_CODES.put("44", "SIB");
        TB_CODES.put("54", "PB");
        TB_CODES.put("18", "BB");
        TB_CODES.put("42", "VVB");
        TB_CODES.put("31", "VSB");
        TB_CODES.put("70", "DB");
        TB_CODES.put("67", "ZSB");
        TB_CODES.put("49", "ZUB");
        TB_CODES.put("38", "MB");
        TB_CODES.put("77", "SB");
        TB_CODES.put("36", "SVB");
        TB_CODES.put("55", "SZB");
        TB_CODES.put("60", "SKB");
        TB_CODES.put("16", "UB");
        TB_CODES.put("13", "CCB");
        TB_CODES.put("52", "YZB");
        TB_CODES.put("99", "CA");
    }

    private String createReportNameForNotification(Notification notification) {

        String text = notification.getText();

        Date notificationDate = notification.getCreateDate();
        LocalDateTime jodaNotificationDate = LocalDateTime.fromDateFields(notificationDate);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        String notificationDateString = formatter.print(jodaNotificationDate);

        NotificationType notificationType = getNotificationType(notification);

        if (notificationType != null && notificationType.operation.equals(OPERATION_IMPORT)) {
            String[] notificationParts = text.split("\"");
            if (notificationParts[0].equals("Загрузка файла ") || notificationParts[0].equals("Ошибка загрузки файла ")) {
                String fileName = notificationParts[1].replace(".xml", "");
                String tbCode = fileName.replace("_", "").substring(0, 2);
                String tbIndex = TB_CODES.get(tbCode);
                String reportName = Joiner.on("_").skipNulls().join(
                        Arrays.asList(tbIndex, fileName, notificationDateString, notificationType.operation, notificationType.result)
                );
                return reportName + ".csv";
            }
        }
        if (notificationType != null && (notificationType.operation.equals(OPERATION_ACCEPTING) ||
                notificationType.operation.equals(OPERATION_CHECK) ||
                notificationType.operation.equals(OPERATION_IDENTIFICATION) ||
                notificationType.operation.equals(OPERATION_REPORT))) {

            String[] notificationParts = text.replace(":", "").split("№ ");

            if (notificationParts.length > 1) {
                Matcher matcher = Pattern.compile("\\d+").matcher(notificationParts[1]);
                matcher.find();
                long declarationId = Long.parseLong(matcher.group());

                if (declarationService.existDeclarationData(declarationId)) {
                    DeclarationData declaration = declarationService.get(declarationId);
                    int departmentId = declaration.getDepartmentId();
                    Department department = departmentService.getDepartment(departmentId);

                    String tbIndex = department.getTbIndex();
                    if (tbIndex == null && isNotEmpty(department.getSbrfCode())) {
                        tbIndex = department.getSbrfCode().substring(0, 2);
                    }
                    String tbCode = TB_CODES.get(tbIndex);
                    String fileName = declaration.getFileName() != null ? declaration.getFileName().replace(".xml", "") : null;

                    String reportName = Joiner.on("_").skipNulls().join(
                            Arrays.asList(tbCode, fileName, declarationId, notificationDateString, notificationType.operation, notificationType.result)
                    );
                    return reportName + ".csv";
                } else {
                    String reportName = Joiner.on("_").skipNulls().join(
                            Arrays.asList("--", "удалена", declarationId, notificationDateString, notificationType.operation, notificationType.result)
                    );
                    return reportName + ".csv";
                }
            }
        }
        return notificationDateString + ".csv";
    }


    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
    public String generateExcelUsers(List<TAUserView> taUserViewList) {
        String reportPath = null;
        try {
            TAUsersReportBuilder taBuilder = new TAUsersReportBuilder(taUserViewList);
            reportPath = taBuilder.createReport();
            return blobDataService.create(reportPath, "Список_пользователей.xlsx");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + TAUsersReportBuilder.class);
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public String generateRefBookSpecificReport(long refBookId, String specificReportType, Date version, String filter, String searchPattern, RefBookAttribute sortAttribute, boolean isSortAscending, TAUserInfo userInfo, LockStateLogger stateLogger) {
        Map<String, Object> params = new HashMap<>();
        ScriptSpecificRefBookReportHolder scriptSpecificReportHolder = new ScriptSpecificRefBookReportHolder();
        File reportFile = null;
        try {
            reportFile = File.createTempFile("specific_report", ".dat");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile));
            try {
                Logger logger = new Logger();
                scriptSpecificReportHolder.setSpecificReportType(specificReportType);
                scriptSpecificReportHolder.setFileOutputStream(outputStream);
                scriptSpecificReportHolder.setFileName(specificReportType);
                scriptSpecificReportHolder.setVersion(version);
                scriptSpecificReportHolder.setFilter(filter);
                scriptSpecificReportHolder.setSearchPattern(searchPattern);
                scriptSpecificReportHolder.setSortAttribute(sortAttribute);
                scriptSpecificReportHolder.setSortAscending(isSortAscending);
                params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
                stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
                if (!refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.CREATE_SPECIFIC_REPORT, logger, params)) {
                    throw new ServiceException("Не предусмотрена возможность формирования отчета \"%s\"", specificReportType);
                }
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
                }
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
            return blobDataService.create(reportFile.getPath(), scriptSpecificReportHolder.getFileName());
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (reportFile != null)
                reportFile.delete();
        }
    }

    /**
     * Возвращает список атрибутов, которые будут отображаться в отчете по справочнику
     *
     * @param refBook справочник
     * @return список атрибутов
     */
    private List<RefBookAttribute> getAttributes(RefBook refBook) {
        List<RefBookAttribute> attributes = new ArrayList<>();
        if (refBook.isVersioned()) {
            // Для версионируемых справочников дополняем список атрибутов, т.к они отсутствуют в БД
            RefBookAttribute versionFromAttribute = new RefBookAttribute(RefBook.RECORD_VERSION_FROM_ALIAS, RefBookAttributeType.DATE);
            versionFromAttribute.setName(RefBook.REF_BOOK_VERSION_FROM_TITLE);
            versionFromAttribute.setWidth(10);
            versionFromAttribute.setVisible(true);
            attributes.add(versionFromAttribute);

            RefBookAttribute versionToAttribute = new RefBookAttribute(RefBook.RECORD_VERSION_TO_ALIAS, RefBookAttributeType.DATE);
            versionToAttribute.setName(RefBook.REF_BOOK_VERSION_TO_TITLE);
            versionToAttribute.setWidth(10);
            versionToAttribute.setVisible(true);
            attributes.add(versionToAttribute);
        }

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.isVisible()) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    @Override
    public String generateRefBookCSV(long refBookId, Date version, String searchPattern, boolean exactSearch, Map<String, String> extraParams,
                                     RefBookAttribute sortAttribute, String direction, LockStateLogger stateLogger) {

        String reportPath = null;
        try {
            RefBook refBook = commonRefBookService.get(refBookId);
            List<Map<String, RefBookValue>> records;

            if (refBook.isHierarchic()) {
                List<RefBookDepartment> departmentsTree = refBookDepartmentDao.findAllByNameAsTree(searchPattern, exactSearch);
                records = toMap(departmentsTree, null);
            } else {
                records = commonRefBookService.fetchAllRecords(refBookId, null, version, searchPattern, exactSearch, extraParams, null, sortAttribute, direction);
            }

            RefBookCSVReportBuilder refBookCSVReportBuilder = new RefBookCSVReportBuilder(refBook, getAttributes(refBook), records, version, searchPattern, exactSearch, sortAttribute);
            stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
            reportPath = refBookCSVReportBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при формировании отчета по справочнику.");
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public String generateRefBookExcel(final long refBookId, final Date version, final String searchPattern, final boolean exactSearch, final Map<String, String> extraParams,
                                       final RefBookAttribute sortAttribute, final String direction, LockStateLogger stateLogger) {

        String reportPath = null;
        try {
            final RefBook refBook = commonRefBookService.get(refBookId);

            RefBookExcelReportBuilder refBookExcelReportBuilder;
            if (refBook.isHierarchic()) {
                List<RefBookDepartment> departmentsTree = refBookDepartmentDao.findAllByNameAsTree(searchPattern, exactSearch);
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, getAttributes(refBook),
                        version, searchPattern, exactSearch, sortAttribute, toMap(departmentsTree, null));
            } else {
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, getAttributes(refBook),
                        version, searchPattern, exactSearch, sortAttribute, new BatchIterator() {
                    private PagingResult<Map<String, RefBookValue>> currentBatch = null;
                    private Iterator<Map<String, RefBookValue>> iterator = null;
                    private int page = 1;

                    @Override
                    public boolean hasNext() {
                        if (iterator == null || !iterator.hasNext()) {
                            PagingParams pagingParams = new PagingParams();
                            pagingParams.setPage(page);
                            pagingParams.setCount(BATCH_SIZE);
                            page++;
                            currentBatch = commonRefBookService.fetchAllRecords(refBookId, null, version, searchPattern, exactSearch, extraParams, pagingParams, sortAttribute, direction);
                            iterator = currentBatch.iterator();
                            return iterator.hasNext();
                        }
                        return true;
                    }

                    @Override
                    public Map<String, RefBookValue> getNextRecord() {
                        if (iterator.hasNext()) {
                            return iterator.next();
                        }
                        return null;
                    }
                });
            }
            stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
            reportPath = refBookExcelReportBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при формировании отчета по справочнику.");
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public String generateExcelPersons(RefBookPersonFilter filter, PagingParams pagingParams, TAUser user) {
        pagingParams.setNoPaging();
        List<RegistryPersonDTO> persons = personService.getPersonsData(pagingParams, filter);
        filter.setTerBanks(departmentService.findAllByIdIn(filter.getTerBankIds()));
        filter.setDocTypes(refBookDocTypeDao.findAllByIdIn(filter.getDocTypeIds()));
        filter.setCitizenshipCountries(refBookCountryDao.findAllByIdIn(filter.getCitizenshipCountryIds()));
        filter.setTaxpayerStates(refBookTaxpayerStateDao.findAllByIdIn(filter.getTaxpayerStateIds()));
        filter.setSourceSystems(refBookAsnuDao.findAllByIdIn(filter.getSourceSystemIds()));
        filter.setCountries(refBookCountryDao.findAllByIdIn(filter.getCountryIds()));

        PersonsReportBuilder reportBuilder = new PersonsReportBuilder(persons, filter);
        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();
            return blobDataService.create(reportPath, "Физические_лица_" + FastDateFormat.getInstance("dd.MM.yyyy").format(new Date()) + ".xlsx");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public String generateExcelDepartmentConfigs(int departmentId) {
        DepartmentConfigsFilter filter = new DepartmentConfigsFilter();
        filter.setDepartmentId(departmentId);
        List<DepartmentConfig> departmentConfigs = departmentConfigService.findPageByFilter(filter, null);
        Department department = departmentService.getDepartment(filter.getDepartmentId());
        DepartmentConfigsReportBuilder reportBuilder = new DepartmentConfigsReportBuilder(departmentConfigs, department);
        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();
            String fileName = makeDepartmentConfigsExcelFileName(departmentId);
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public String generateExcelUnloadList(long declarationDataId, boolean sources, boolean destinations, TAUser user) {
        List<Relation> relationList = new ArrayList<>();
        if (sources) relationList.addAll(sourceService.getDeclarationSourcesInfo(declarationDataId));
        if (destinations) relationList.addAll(sourceService.getDeclarationDestinationsInfo(declarationDataId));

        SourcesAndDestinationsReportBuilder reportBuilder = new SourcesAndDestinationsReportBuilder(relationList, declarationDataId);
        String reportPath = null;
        try {
            reportPath = reportBuilder.createReport();
            return blobDataService.create(reportPath, declarationDataId + "_Источники приемники_" + FastDateFormat.getInstance("dd.MM.yyyy").format(new Date()) + ".xlsx");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        } finally {
            AppFileUtils.deleteTmp(reportPath);
        }
    }

    @Override
    public InputStream generateExcelTransportMessages(List<TransportMessage> transportMessages, String headerDescription) throws IOException {
        ExcelTransportMessagesReportBuilder reportBuilder = new ExcelTransportMessagesReportBuilder(transportMessages,headerDescription);
        return reportBuilder.createReportAsStream();
    }

    private String makeDepartmentConfigsExcelFileName(int departmentId) {
        Department department = departmentService.getDepartment(departmentId);
        return department.getId() + "_" + department.getShortName() + "_" + FastDateFormat.getInstance("yyyyMMddHHmm").format(new Date()) + ".xlsx";
    }

    private List<Map<String, RefBookValue>> toMap(List<RefBookDepartment> departmentsTree, Map<String, RefBookValue> parent) {
        List<Map<String, RefBookValue>> result = new ArrayList<>();
        if (departmentsTree != null) {
            for (RefBookDepartment department : departmentsTree) {
                Map<String, RefBookValue> mapDepartment = toMap(department, parent);
                result.add(mapDepartment);
                result.addAll(toMap(department.getChildren(), mapDepartment));
            }
        }
        return result;
    }

    private Map<String, RefBookValue> toMap(RefBookDepartment department, Map<String, RefBookValue> parent) {
        Map<String, RefBookValue> values = null;
        if (department != null) {
            values = new HashMap<>();
            values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, department.getId()));
            values.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, department.getCode()));
            values.put("NAME", new RefBookValue(RefBookAttributeType.STRING, department.getName()));
            values.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, department.getShortName()));
            values.put("PARENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, parent));
            values.put("TYPE", new RefBookValue(RefBookAttributeType.STRING, department.getType() != null ? department.getType().getLabel() : null));
            values.put("IS_ACTIVE", new RefBookValue(RefBookAttributeType.STRING, department.isActive() ? "Да" : "Нет"));
            values.put("TB_INDEX", new RefBookValue(RefBookAttributeType.STRING, department.getTbIndex()));
            values.put("SBRF_CODE", new RefBookValue(RefBookAttributeType.STRING, department.getSbrfCode()));
        }
        return values;
    }

    @Override
    public String generateCsvNotifications(List<Notification> notifications) {
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            file = File.createTempFile("notifications", ".csv");
            fileOutputStream = new FileOutputStream(file);
            CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new OutputStreamWriter(fileOutputStream, "windows-1251")), ';');

            String[] header = {"№ п/п", "Дата оповещения", "Содержание"};
            csvWriter.writeNext(header);
            for (int i = 0; i < notifications.size(); i++) {
                Notification notification = notifications.get(i);
                String notificationDate = DateUtils.commonDateTimeFormat(notification.getCreateDate());
                String[] row = {String.valueOf(i + 1), notificationDate, notification.getText()};
                csvWriter.writeNext(row);
            }
            csvWriter.close();

            String fileName = "Список оповещений_" + currentMoscowDateTime() + ".csv";
            return blobDataService.create(file.getAbsolutePath(), fileName);

        } catch (Exception e) {
            throw new ServiceException("Не выполнена операция \"Выгрузка списка оповещений\". Причина: " + e.getMessage(), e);
        } finally {
            AppFileUtils.deleteTmp(file);

            if (fileOutputStream != null) {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }
    }

    /**
     * Текущая московская дата со временем в формате "2000-01-01_23:59:59". Используется в названиях файлов.
     */
    private static String currentMoscowDateTime() {
        DateTime now = DateTime.now().withZone(DateTimeZone.forID("Europe/Moscow"));
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
        return formatter.print(now);
    }
}
