package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class LoadDeclarationDataServiceImpl extends AbstractLoadTransportDataService implements LoadDeclarationDataService {

	private static final Log LOG = LogFactory.getLog(LoadDeclarationDataServiceImpl.class);
    private static final String LOCK_MSG = "Обработка данных транспортного файла не выполнена, " +
            "т.к. в данный момент выполняется изменение данных декларации \"%s\" " +
            "для подразделения \"%s\" " +
            "в периоде \"%s\", " +
            "инициированное пользователем \"%s\" " +
            "в %s";
    private static final ThreadLocal<SimpleDateFormat> SDF_HH_MM_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm dd.MM.yyyy");
        }
    };
    private static final long REF_BOOK_DEPARTMENT = 30L; // Подразделения
    private static final long REF_BOOK_PERIOD_DICT = 8L; // Коды отчетных периодов
    private static final String SBRF_CODE_ATTR_NAME = "SBRF_CODE";

    public static final String TAG_DOCUMENT = "Документ";
    public static final String ATTR_PERIOD = "Период";
    public static final String ATTR_YEAR = "ОтчетГод";

    public static class SAXHandler extends DefaultHandler {
        private Map<String, Map<String, String>> values;
        private Map<String, List<String>> tagAttrNames;

        public SAXHandler(Map<String, List<String>> tagAttrNames) {
            this.tagAttrNames = tagAttrNames;
        }

        public Map<String, Map<String, String>> getValues() {
            return values;
        }

        @Override
        public void startDocument() throws SAXException {
            values = new HashMap<String, Map<String, String>>();
            for (Map.Entry<String, List<String>> entry : tagAttrNames.entrySet()) {
                values.put(entry.getKey(), new HashMap<String, String>());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            for (Map.Entry<String, List<String>> entry : tagAttrNames.entrySet()) {
                if (entry.getKey().equals(qName)) {
                    for (String attrName: entry.getValue()) {
                        values.get(qName).put(attrName, attributes.getValue(attrName));
                    }
                }
            }
        }
    }

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private TAUserService userService;
    @Autowired
    private LogBusinessService logBusinessService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Override
    public ImportCounter importDeclaration(TAUserInfo userInfo, Map<Integer, List<TaxType>> departmentTaxMap, Logger logger, String lockId, boolean isAsync) {
        log(userInfo, LogData.L1, logger, lockId);
        ImportCounter importCounter = new ImportCounter();
        List<Integer> departmentIdList = new ArrayList<Integer>();
        departmentIdList.addAll(departmentTaxMap.keySet());
        if (!departmentIdList.isEmpty()) {
            // По каталогам загрузки ТБ
            List<String> notDefinedDepartments = new ArrayList<String>();
            List<Integer> tbList = departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
            for (Object departmentIdObj : CollectionUtils.intersection(departmentIdList, tbList)) {
                int departmentId = (Integer) departmentIdObj;
                List<TaxType> taxTypeList = departmentTaxMap.get(departmentId);
                try {
                    importCounter.add(importDataFromFolder(userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, notDefinedDepartments, departmentId, taxTypeList, logger, lockId, isAsync));
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error("Ошибка при загрузке транспортных файлов подразделения «%s». %s", departmentService.getDepartment(departmentId).getName(), e.getMessage());
                }
            }
            if (!notDefinedDepartments.isEmpty()) {
                StringBuilder depNames = new StringBuilder();
                for(String depName: notDefinedDepartments) {
                    depNames.append("«" + depName + "», ");
                }
                log(userInfo, LogData.L30, logger, lockId, depNames.delete(depNames.length() - 2, depNames.length()).toString());
            }
        }
        log(userInfo, LogData.L2, logger, lockId, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    /*@Override
    public List<TransportFileInfo> getFormDataFiles(TAUserInfo userInfo, Logger logger) {
        List<TransportFileInfo> fileList = new ArrayList<TransportFileInfo>();
        Map<Integer, List<TaxType>> departmentTaxMap = getTB(userInfo, logger);
        List<Integer> departmentIdList = new ArrayList<Integer>();
        departmentIdList.addAll(departmentTaxMap.keySet());
        // По каталогам загрузки ТБ
        for (Integer departmentId : departmentIdList) {
            try {
                getFormDataTBFiles(fileList, userInfo, ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId, departmentTaxMap.get(departmentId), logger, "");
            } catch (Exception e) {
            }
        }
        return fileList;
    }*/

    /*
    @Override
    public Map<Integer, List<TaxType>> getTB(TAUserInfo userInfo, Logger logger) {
        Map<Integer, List<TaxType>> departmentTaxMap = new HashMap<Integer, List<TaxType>>();
        List<Department> departmentTBList = new LinkedList<Department>();
        for (TaxType taxType : TaxType.values()) {
            // Выборка подразделений http://conf.aplana.com/pages/viewpage.action?pageId=13111363
            List<Integer> departmentList = departmentService.getTaxFormDepartments(userInfo.getUser(), Collections.singletonList(taxType), null, null);

            List<Integer> departmentTBIdList = new LinkedList<Integer>();
            if (departmentList != null) {
                Collection<Department> departments = departmentService.getRequiredForTreeDepartments(new HashSet(departmentList)).values();
                for (Department department : departments) {
                    if (department.getType() == DepartmentType.TERR_BANK) {
                        departmentTBIdList.add(department.getId());
                        if (departmentTaxMap.get(department.getId()) == null) {
                            departmentTBList.add(department);
                        }
                    }
                }
            }

            for (Integer departmentId : departmentTBIdList) {
                if (departmentTaxMap.get(departmentId) == null) {
                    departmentTaxMap.put(departmentId, new ArrayList<TaxType>());
                }
                departmentTaxMap.get(departmentId).add(taxType);
            }

        }

        if (departmentTBList.isEmpty()) {
            logger.info("Не определено доступных для пользователя ТБ.");
        }

        if (departmentTBList.size() == 1) {
            logger.info("Определен доступный для пользователя ТБ: «%s».", departmentTBList.get(0).getName());
        }

        if (departmentTBList.size() > 1) {
            List<String> names = new ArrayList<String>(departmentTBList.size());
            for (Department department : departmentTBList) {
                names.add("«" + department.getName() + "»");
            }
            logger.info("Определены доступные для пользователя ТБ: %s.", StringUtils.join(names, ", "));
        }

        return departmentTaxMap;
    }*/

    @Override
    public ImportCounter importDeclaration(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync) {
        Map<Integer, List<TaxType>> departmentTaxMap = new HashMap<Integer, List<TaxType>>();
        for (Integer depId : departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode())) {
            departmentTaxMap.put(depId, Arrays.asList(TaxType.NDFL));
        }
        return importDeclaration(userInfo, departmentTaxMap, logger, lock, isAsync);
    }

    /**
     * Загрузка всех ТФ НФ из указанного каталога загрузки
     */
    private ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam param, List<String> notDefinedDepartments, Integer departmentId,
                                               List<TaxType> taxTypeList, Logger logger, String lockId, boolean isAsync) {
        String path = getUploadPath(userInfo, param, departmentId, logger, lockId);
        if (path == null) {
            notDefinedDepartments.add(departmentService.getDepartment(departmentId).getName());
            // Ошибка получения пути
            return new ImportCounter();
        }
        int success = 0;
        int fail = 0;
        // Набор файлов, которые уже обработали
        Set<String> ignoreFileSet = new HashSet<String>();
        ImportCounter wrongImportCounter = new ImportCounter();
        String departmentName = departmentService.getDepartment(departmentId).getName();

        // Проверка каталогов, указанных в параметрах "Путь к каталогу загрузки", "Путь к каталогу архива" и "Путь к каталогу ошибок" для ТБ, на наличие доступа
        String archivePath = getFormDataArchivePath(userInfo, departmentId, logger, lockId);
        String errorPath = getFormDataErrorPath(userInfo, departmentId, logger, lockId);
        List<String> pathList = new ArrayList<String>();
        if (!checkPath(path)) {
            pathList.add("к каталогу загрузки «" + path + "»");
        }
        if (!checkPath(archivePath)) {
            pathList.add("к каталогу архива «" + archivePath + "»");
        }
        if (!checkPath(errorPath)) {
            pathList.add("к каталогу ошибок «" + errorPath + "»");
        }
        if (!pathList.isEmpty()) {
            log(userInfo, LogData.L42, logger, lockId, StringUtils.join(pathList, ", "), departmentName);
            return wrongImportCounter;
        }

        // 5. Система проверяет каталог загрузки ТБ на наличие в нем ТФ, к которым пользователь-инициатор имеет доступ
        List<String> workFilesList = getWorkTransportFiles(userInfo, path, ignoreFileSet, taxTypeList, logger, wrongImportCounter, lockId);
        if (workFilesList.isEmpty()) {
            //if (wrongImportCounter.getFailCounter() == 0) log(userInfo, LogData.L3, logger, lockId, departmentName); до 1.0
            return wrongImportCounter;
        }
        // 6. Система создает запись о начале загрузки из каталога ТБ (вариант текста 52) в: Журнале аудита и "логгере".
        log(userInfo, LogData.L52, logger, lockId, departmentName);

        long maxFileSize = 0;
        if (isAsync) {
            maxFileSize = asyncTaskTypeDao.get(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId()).getTaskLimit();
        }
        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        for (String fileName : workFilesList) {
            if (ignoreFileSet.contains(fileName)) {
                continue;
            }
            ignoreFileSet.add(fileName);
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);

            if (maxFileSize != 0 && currentFile.length() / 1024 > maxFileSize) {
                log(userInfo, LogData.L47, logger, lockId, fileName, currentFile.length() / 1024, path, maxFileSize);
                moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                        Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L4.getText(), lockId, fileName, path))), logger, lockId);
                fail++;
                continue;
            }
            // Блокировка файла
            LockData fileLock = lockDataService.lock(LockData.LockObjects.FILE.name() + "_" + fileName,
                    userInfo.getUser().getId(),
                    String.format(LockData.DescriptionTemplate.FILE.getText(), fileName));
            if (fileLock != null) {
                log(userInfo, LogData.L41, logger, lockId, fileName);
                fail++;
                continue;
            }

            InputStream inputStream = currentFile.getInputStream();
            try {
                List<LogEntry> logs;
                try {
                    logs = loadDeclarationDataFile(userInfo, departmentId, fileName, currentFile, logger, lockId);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
                if (logs == null) {
                    // 20 Загрузка формы завершена
                    log(userInfo, LogData.L20, logger, lockId, currentFile.getName());
                    success++;
                    if (moveToArchiveDirectory(userInfo, getFormDataArchivePath(userInfo, departmentId, logger, lockId), currentFile, logger, lockId)) {
                    } else {
                        // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                        moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                                Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), lockId, ""))), logger, lockId);
                    }
                } else {
                    moveToErrorDirectory(userInfo, getFormDataErrorPath(userInfo, departmentId, logger, lockId), currentFile,
                            logs, logger, lockId);
                    fail++;
                    continue;
                }
            } finally {
                lockDataService.unlock(LockData.LockObjects.FILE.name() + "_" + fileName, userInfo.getUser().getId(), true);
            }
        }

        log(userInfo, LogData.L53, logger, lockId, departmentName);
        return new ImportCounter(success, fail + wrongImportCounter.getFailCounter());
    }

    private List<LogEntry> loadDeclarationDataFile(TAUserInfo userInfo, Integer departmentId, String fileName, FileWrapper currentFile, Logger logger, String lockId) {
        TransportDataParam transportDataParam;
        try {
            transportDataParam = TransportDataParam.valueOfDec(fileName);
        } catch (IllegalArgumentException e) {
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.Ln_2, logger, lockId, fileName)));
        }
        String reportPeriodCode = transportDataParam.getReportPeriodCode();
        Integer year = transportDataParam.getYear();
        String departmentCode = transportDataParam.getDepartmentCode();
        String asnuCode = transportDataParam.getAsnuCode();
        String guid = transportDataParam.getGuid();
        String kpp = transportDataParam.getKpp();
        Integer declarationTypeId = transportDataParam.getDeclarationTypeId();

        // для 1151111 парсим файл для определения периода
        if (declarationTypeId == 200) {
            departmentCode = "18_0000_00"; // ToDo нужно определять по КПП
            kpp = null;
            InputStream inputStream = currentFile.getInputStream();
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                SAXHandler handler = new SAXHandler(new HashMap<String, List<String>>(){{
                    put(TAG_DOCUMENT, Arrays.asList(ATTR_PERIOD, ATTR_YEAR));
                }});
                saxParser.parse(inputStream, handler);
                reportPeriodCode = handler.getValues().get(TAG_DOCUMENT).get(ATTR_PERIOD);
                try {
                    year = Integer.parseInt(handler.getValues().get(TAG_DOCUMENT).get(ATTR_YEAR));
                } catch (NumberFormatException nfe) {
                    return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.Ln_4, logger, lockId, fileName)));
                }
            } catch (IOException e) {
                LOG.error("", e);
                return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.Ln_1, logger, lockId, fileName)));
            } catch (ParserConfigurationException e) {
                LOG.error("Ошибка при парсинге xml", e);
                return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.Ln_2, logger, lockId, fileName)));
            } catch (SAXException e) {
                LOG.error("", e);
                return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.Ln_2, logger, lockId, fileName)));
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        // Не задан код подразделения/период/год
        if (departmentCode == null || reportPeriodCode == null || year == null) {
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.L4, logger, lockId, fileName, "path")));
        }

        // Подразделение НФ
        Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode, false);
        formDepartmentId = formDepartment != null ? formDepartment.getId(): null;

        // Указан несуществующий код налоговой формы
        DeclarationType declarationType = declarationTypeService.get(declarationTypeId);

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = periodService.getByTaxTypedCodeYear(declarationType.getTaxType(), reportPeriodCode, year);
        if (reportPeriod == null) {
            String reportPeriodName = "";
            String filter = "CODE='" + reportPeriodCode + "' AND " + declarationType.getTaxType().getCode() + "=1";
            PagingResult<Map<String, RefBookValue>> reportPeriodDicts = refBookDao.getRecords(REF_BOOK_PERIOD_DICT, null, null, filter, null);
            if (reportPeriodDicts.size() == 1) {
                reportPeriodName = reportPeriodDicts.get(0).get("NAME").getStringValue();
                if (reportPeriodName != null && !reportPeriodName.isEmpty()) {
                    reportPeriodName = " (" + reportPeriodName + ")";
                }
            }
            log(userInfo, LogData.L7, logger, lockId, declarationType.getTaxType().getName(), reportPeriodCode, reportPeriodName, year, fileName);
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L7.getText(), lockId, declarationType.getTaxType().getName(), reportPeriodCode, reportPeriodName, year, fileName)));
        }

        // Актуальный шаблон НФ, введенный в действие
        Integer declarationTemplateId;
        try {
            declarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationType.getId(), reportPeriod.getId());
        } catch (Exception e) {
            // Если шаблона нет, то не загружаем ТФ
            log(userInfo, LogData.L21, logger, lockId, e.getMessage());
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L21.getText(), lockId, e.getMessage())));
        }

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);

        if (!TAAbstractScriptingServiceImpl.canExecuteScript(declarationTemplateService.getDeclarationTemplateScript(declarationTemplateId), FormDataEvent.IMPORT_TRANSPORT_FILE)) {
            log(userInfo, LogData.L48, logger, lockId, fileName);
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L48.getText(), logger, lockId, fileName)));
        }


        // Указан несуществующий код подразделения
        if (formDepartment == null) {
            RefBook refBook = refBookDao.get(REF_BOOK_DEPARTMENT);
            log(userInfo, LogData.L5, logger, lockId, refBook.getName(), refBook.getAttribute(SBRF_CODE_ATTR_NAME).getName(), departmentCode, fileName);
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L5.getText(), lockId, refBook.getName(), refBook.getAttribute(SBRF_CODE_ATTR_NAME).getName(), departmentCode, fileName)));
        } else {
            if (!departmentId.equals(departmentService.getParentTB(formDepartment.getId()).getId())) {
                return null;
            }
        }

        // АСНУ
        Long asnuId = null;
        if (asnuCode != null) {
            RefBookDataProvider asnuProvider = rbFactory.getDataProvider(900L);
            List<Long> asnuIds = asnuProvider.getUniqueRecordIds(null, "CODE = '" + asnuCode + "'");
            if (asnuIds.size() != 1) {
                RefBook refBook = refBookDao.get(900L);
                log(userInfo, LogData.L5_ASNU, logger, lockId, refBook.getName(), refBook.getAttribute("CODE").getName(), asnuCode, fileName);
                return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L5.getText(), lockId, refBook.getName(), refBook.getAttribute(SBRF_CODE_ATTR_NAME).getName(), asnuCode, fileName)));
            } else {
                asnuId = asnuIds.get(0);
            }
        }

        // Назначение подразделению Декларации
        List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(departmentId,
                declarationTemplate.getType().getTaxType(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        boolean found = false;
        for (DepartmentDeclarationType ddt : ddts) {
            if (ddt.getDeclarationTypeId() == declarationType.getId()) {
                found = true;
                break;
            }
        }
        if (!found) {
            log(userInfo, LogData.L14, logger, lockId, formDepartment.getName(), declarationType.getName(), fileName);
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.L14.getText(),
                    lockId, formDepartment.getName(), declarationType.getName(), fileName)));
        }

        // Последний отчетный период подразделения для указанного отчетного периода
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.getLast(formDepartment.getId(),
                reportPeriod.getId());

        // Открытость периода
        if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
            String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.Ln_5, logger, lockId, formDepartment.getCode(), reportPeriodName)));
        }

        // Проверка GUID
        if (guid != null && !guid.isEmpty()) {
            DeclarationDataFilter declarationFilter = new DeclarationDataFilter();
            declarationFilter.setFileName(guid);
            declarationFilter.setTaxType(declarationType.getTaxType());
            declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID);
            List<Long> declarationDataSearchResultItems = declarationDataSearchService.getDeclarationIds(declarationFilter, declarationFilter.getSearchOrdering(), false);
            if (!declarationDataSearchResultItems.isEmpty()) {
                log(userInfo, LogData.Ln_3, logger, lockId, guid);
                return Collections.singletonList(new LogEntry(LogLevel.ERROR, String.format(LogData.Ln_3.getText(), lockId, guid)));
            }
        }


        // Поиск экземпляра декларации
        DeclarationData declarationData = declarationDataService.find(declarationTemplateId, departmentReportPeriod.getId(), null, kpp, null, asnuId, guid);

        // Экземпляр уже есть и не в статусе «Создана»
        if (declarationData != null) {
            // Сообщение об ошибке в общий лог и в файл со списком ошибок
            return Collections.singletonList(new LogEntry(LogLevel.ERROR, log(userInfo, LogData.L17, logger, lockId, declarationType.getName(), formDepartment.getName(), fileName)));
        }

        log(userInfo, LogData.L15_FD, logger, lockId, fileName);
        log(userInfo, LogData.L15_DEC, logger, lockId, getFileNamePart(departmentCode),
                getFileNamePart(reportPeriodCode), getFileNamePart(year),
                getFileNamePart(kpp), getFileNamePart(asnuCode), getFileNamePart(guid));

        // Загрузка данных в НФ (скрипт)
        Logger localLogger = new Logger();
        boolean result;
        InputStream inputStream = currentFile.getInputStream();
        try {
            // Загрузка
            result = importDeclarationData(userInfo, inputStream, fileName, declarationData, declarationType,
                    departmentReportPeriod, asnuId, transportDataParam, localLogger, lockId);

            // Вывод скрипта в область уведомлений
            logger.getEntries().addAll(localLogger.getEntries());
        } catch (Exception e) {
            // Вывод скрипта в область уведомлений
            logger.getEntries().addAll(localLogger.getEntries());
            // Вывод в область уведомленеий и ЖА
            log(userInfo, LogData.L21, logger, lockId, e.getMessage());
            return localLogger.getEntries();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        if (result) {
            return null;
        } else {
            return null;
        }
    }

    /*
    private void getFormDataTBFiles(List<TransportFileInfo> fileList, TAUserInfo userInfo, ConfigurationParam param, Integer departmentId,
                                    List<TaxType> taxTypes, Logger logger, String lockId) {
        String path = getUploadPath(userInfo, param, departmentId, logger, lockId);
        if (path == null) {
            // Ошибка получения пути
            return;
        }

        // Проверка каталогов, указанных в параметрах "Путь к каталогу загрузки", "Путь к каталогу архива" и "Путь к каталогу ошибок" для ТБ, на наличие доступа
        String archivePath = getFormDataArchivePath(userInfo, departmentId, logger, lockId);
        String errorPath = getFormDataErrorPath(userInfo, departmentId, logger, lockId);
        if (!checkPath(path) || !checkPath(archivePath) || !checkPath(errorPath)) {
            return;
        }


        // Если изначально нет подходящих файлов то выдаем отдельную ошибку
        List<String> workFilesList = getWorkTransportFiles(userInfo, path, new HashSet<String>(), taxTypes, logger, new ImportCounter(), lockId);
        if (workFilesList.isEmpty()) {
            return;
        }

        // Обработка всех подходящих файлов, с получением списка на каждой итерации
        for (String fileName : workFilesList) {
            FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);
            fileList.add(new TransportFileInfo(currentFile.getName(), path, currentFile.length() / 1024));
        }
    }*/

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
     * Загрузка ТФ конкретной декларации. Только этот метод в сервисе транзакционный.
     */
    private boolean importDeclarationData(TAUserInfo userInfo, InputStream inputStream, String fileName, DeclarationData declarationData,
                                   DeclarationType declarationType,
                                   DepartmentReportPeriod departmentReportPeriod,
                                   Long asnuId, TransportDataParam transportDataParam,
                                   Logger localLogger, String lock) {
        String reportPeriodName = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + " - "
                + departmentReportPeriod.getReportPeriod().getName();

        boolean success = false;
        boolean formWasCreated = declarationData != null;
        // Если формы нет, то создаем
        if (declarationData == null) {
            int declarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationType.getId(),
                    departmentReportPeriod.getReportPeriod().getId());
            long declarationDataId = declarationDataService.create(localLogger, declarationTemplateId, userInfo, departmentReportPeriod, null, transportDataParam.getKpp(), null, asnuId, fileName, null);
            declarationData = declarationDataService.get(declarationDataId, userInfo);
        }

        // Блокировка
        LockData lockData = lockDataService.lock(declarationDataService.generateAsyncTaskKey(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC),
                userInfo.getUser().getId(),
                declarationDataService.getDeclarationFullName(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC));
        if (lockData != null)
            throw new ServiceException(String.format(
                    LOCK_MSG,
                    declarationType.getName(),
                    departmentService.getDepartment(declarationData.getDepartmentId()).getName(),
                    reportPeriodName,
                    userService.getUser(lockData.getUserId()).getName(),
                    SDF_HH_MM_DD_MM_YYYY.get().format(lockData.getDateLock())
            ));

        try {
            // 15 Проверка по XSD + Скрипт загрузки ТФ
            try {
                declarationDataService.importDeclarationData(localLogger, userInfo, declarationData.getId(), inputStream,
                        fileName, FormDataEvent.IMPORT_TRANSPORT_FILE, null, lock);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            // Если при выполнении скрипта возникли фатальные ошибки, то
            if (localLogger.containsLevel(LogLevel.ERROR)) {
                // Исключение для отката транзакции сознания и заполнения НФ
                throw new ServiceException("При выполнении загрузки произошли ошибки");
            }
        } finally {
            // Снимаем блокировку
            lockDataService.unlock(declarationDataService.generateAsyncTaskKey(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC), userInfo.getUser().getId());
            if (localLogger.containsLevel(LogLevel.ERROR) && !formWasCreated) {
                declarationDataService.delete(declarationData.getId(), userInfo);
            }
        }

        return success;
    }

    /**
     * Путь к каталогу из ConfigurationParam
     */
    private String getPath(TAUserInfo userInfo, ConfigurationParam param, int formDepartmentId, LogData logData, Logger logger, String lock) {
        // Конфигурационные параметры
        ConfigurationParamModel model = configurationDao.getByDepartment(formDepartmentId);
        List<String> pathList = null;
        if (model != null) {
            pathList = model.get(param, formDepartmentId);
        }
        // Проверка наличия каталога в параметрах
        if (pathList == null || pathList.isEmpty()) {
            if (lock != null && !lock.isEmpty()) log(userInfo, logData, logger, lock);
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок НФ
     */
    private String getFormDataErrorPath(TAUserInfo userInfo, int formDepartmentId, Logger logger, String lock) {
        return getPath(userInfo, ConfigurationParam.FORM_ERROR_DIRECTORY, formDepartmentId,
                LogData.L_1, logger, lock);
    }

    /**
     * Путь к каталогу архива НФ
     */
    private String getFormDataArchivePath(TAUserInfo userInfo, int formDepartmentId, Logger logger, String lock) {
        return getPath(userInfo, ConfigurationParam.FORM_ARCHIVE_DIRECTORY, formDepartmentId,
                LogData.L_2, logger, lock);
    }

    /**
     * Получение пути из конф. параметров
     */
    private String getUploadPath(TAUserInfo userInfo, ConfigurationParam configurationParam, int departmentId, Logger logger, String lock) {
        ConfigurationParamModel model = configurationDao.getByDepartment(departmentId);
        List<String> uploadPathList = model.get(configurationParam, departmentId);
        if (uploadPathList == null || uploadPathList.isEmpty()) {
            //if (lock != null && !lock.isEmpty()) log(userInfo, LogData.L30, logger, lock, departmentService.getDepartment(departmentId).getName());
            return null;
        }
        return uploadPathList.get(0);
    }

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    private List<String> getWorkTransportFiles(TAUserInfo userInfo, String folderPath, Set<String> ignoreFileSet,
                                               List<TaxType> taxTypeList, Logger logger, ImportCounter wrongImportCounter, String lock) {
        List<String> retVal = new LinkedList<String>();
        FileWrapper catalogFile = ResourceUtils.getSharedResource(folderPath + "/");
        for (String candidateStr : catalogFile.list()) {
            if (ignoreFileSet != null && ignoreFileSet.contains(candidateStr)) {
                continue;
            }

            // Это файл, а не директория и соответствует формату имени ТФ
            FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + "/" + candidateStr);
            if (candidateFile.isFile()) {
                if (TransportDataParam.isValidDecName(candidateStr)) {
                    DeclarationType declarationType = declarationTypeService.get(100);
                    if (declarationType == null || taxTypeList.contains(declarationType.getTaxType())) {
                            retVal.add(candidateStr);
                    }
                } else {
                    log(userInfo, LogData.L4, logger, lock, candidateStr, folderPath);
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
