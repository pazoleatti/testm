package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermissionSetter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Сервис для работы с декларациями
 *
 * @author Eugene Stetsenko
 * @author dsultanbekov
 */
@Service
@Transactional(readOnly = true)
public class DeclarationDataServiceImpl implements DeclarationDataService {

    private static final Log LOG = LogFactory.getLog(DeclarationDataService.class);
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>";
    private static final String ENCODING = "UTF-8";
    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY_HH_MM_SS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };
    private static final String FILE_NAME_IN_TEMP_PATTERN = System.getProperty("java.io.tmpdir") + File.separator + "%s.%s";
    private static final String CALCULATION_NOT_TOPICAL = "Налоговая форма содержит неактуальные консолидированные данные  " +
            "(расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена " +
            "консолидация).";
    private static final String CALCULATION_NOT_TOPICAL_SUFFIX = " Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\"";

    private final static List<DeclarationDataReportType> reportTypes = Collections.unmodifiableList(Arrays.asList(DeclarationDataReportType.ACCEPT_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.XML_DEC, DeclarationDataReportType.IMPORT_TF_DEC, DeclarationDataReportType.DELETE_DEC));

    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DeclarationDataAccessService declarationDataAccessService;
    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LogBusinessService logBusinessService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private PeriodService reportPeriodService;
    @Autowired
    private ValidateXMLService validateXMLService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private RefBookAsnuService refBookAsnuService;
    @Autowired
    private RefBookDepartmentDataService refBookDepartmentDataService;
    @Autowired
    private NdflPersonService ndflPersonService;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private TAUserService userService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private DBUtils bdUtils;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private NdflPersonDao ndflPersonDao;
    @Autowired
    private DeclarationDataPermissionSetter declarationDataPermissionSetter;
    @Autowired
    private NotificationService notificationService;

    private static final String DD_NOT_IN_RANGE = "Найдена форма: \"%s\", \"%d\", \"%s\", \"%s\", состояние - \"%s\"";

    public static final String TAG_FILE = "Файл";
    public static final String TAG_DOCUMENT = "Документ";
    public static final String ATTR_FILE_ID = "ИдФайл";
    public static final String ATTR_DOC_DATE = "ДатаДок";
    private static final String VALIDATION_ERR_MSG = "Обнаружены фатальные ошибки!";
    public static final String MSG_IS_EXIST_DECLARATION =
            "Существует экземпляр \"%s\" в подразделении \"%s\" в периоде \"%s\"%s%s для макета!";
    private static final String NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\" в статусе \"%s\"";
    private static final String NOT_EXIST_SOURCE_DECLARATION_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\" - экземпляр формы не создан";
    private static final String EXIST_DESTINATION_DECLARATION_ERROR =
            "Переход невозможен, т.к. уже подготовлена/принята вышестоящая налоговая форма.";
    private static final String FILE_NOT_DELETE = "Временный файл %s не удален";

    private static final Date MAX_DATE;
    private static final Calendar CALENDAR = Calendar.getInstance();

    static {
        CALENDAR.clear();
        CALENDAR.set(9999, Calendar.DECEMBER, 31);
        MAX_DATE = CALENDAR.getTime();
        CALENDAR.clear();
    }

    private class SAXHandler extends DefaultHandler {
        private Map<String, String> values;
        private Map<String, String> tagAttrNames;

        public SAXHandler(Map<String, String> tagAttrNames) {
            this.tagAttrNames = tagAttrNames;
        }


        public Map<String, String> getValues() {
            return values;
        }

        @Override
        public void startDocument() throws SAXException {
            values = new HashMap<String, String>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (tagAttrNames.containsKey(qName)) {
                values.put(qName, attributes.getValue(tagAttrNames.get(qName)));
            }
        }
    }

    /**
     * Создание декларации в заданном отчетном периоде подразделения
     *
     * @param userInfo          Информация о текущем пользователе
     * @param declarationTypeId ID вида налоговой формы
     * @param departmentId      ID подразделения
     * @param periodId          ID отчетного периода
     * @return Модель {@link CreateResult}, в которой содержится ID налоговой формы
     */
    @Override
    //TODO:https://jira.aplana.com/browse/SBRFNDFL-2071
    public CreateResult<Long> create(TAUserInfo userInfo, Long declarationTypeId, Integer departmentId, Integer periodId) {
        CreateResult<Long> result = new CreateResult<Long>();
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(departmentId, periodId);
        if (departmentReportPeriod != null) {
            int activeTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId.intValue(), periodId);
            try {
                Long declarationId = doCreate(logger, activeTemplateId, userInfo, departmentReportPeriod, null, null, null, null, null, null, true);
                result.setEntityId(declarationId);
            } catch (DaoException e) {
                DeclarationTemplate dt = declarationTemplateService.get(activeTemplateId);
                if (dt.getDeclarationFormKind().getId() == DeclarationFormKind.CONSOLIDATED.getId()) {
                    Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                    String strCorrPeriod = "";
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
                    }
                    logger.error("Консолидированная налоговая форма с заданными параметрами: Период: \"%s\", Подразделение: \"%s\" уже существует",
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                            department.getName());
                } else {
                    throw new ServiceException(e.getMessage());
                }
            }
            if (!logger.getEntries().isEmpty()) {
                result.setUuid(logEntryService.save(logger.getEntries()));
            }
            return result;
        } else {
            throw new ServiceException("Не удалось определить налоговый период.");
        }
    }

    /**
     * Логика создания вынесена в отдельный метод, для решения проблем с транзакциями при вызове из других транзакционных методов
     */
    private Long doCreate(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                          DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo, Long asunId, String fileName, String note, boolean writeAudit) {
        String key = LockData.LockObjects.DECLARATION_CREATE.name() + "_" + declarationTemplateId + "_" + departmentReportPeriod.getId() + "_" + taxOrganKpp + "_" + taxOrganCode + "_" + fileName;
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
        if (lockDataService.lock(key, userInfo.getUser().getId(),
                String.format(DescriptionTemplate.DECLARATION.getText(),
                        "Создание налоговой формы",
                        departmentReportPeriod.getReportPeriod().getName() + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        departmentReportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(departmentReportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        taxOrganCode != null
                                ? ", Налоговый орган: \"" + taxOrganCode + "\""
                                : "",
                        taxOrganKpp != null
                                ? ", КПП: \"" + taxOrganKpp + "\""
                                : "",
                        oktmo != null
                                ? ", ОКТМО: \"" + oktmo + "\""
                                : "",
                        asunId != null
                                ? ", Наименование АСНУ: \"" + asnuProvider.getRecordData(asunId).get("NAME").getStringValue() + "\""
                                : "",
                        fileName != null
                                ? ", Имя файла: \"" + fileName + "\""
                                : "")
        ) == null) {
            //Если блокировка успешно установлена
            try {
                /*
                DeclarationData declarationData = find(declarationTemplate.getType().getId(), departmentReportPeriod.getId(), taxOrganKpp, oktmo, taxOrganCode, asunId, fileName);
                if (declarationData != null) {
                    String msg = (declarationTemplate.getType().getTaxType().equals(TaxType.DEAL) ?
                            "Уведомление с заданными параметрами уже существует" :
                            "Налоговая форма с заданными параметрами уже существует");
                    throw new ServiceLoggerException(msg, null);
                }*/

                declarationDataAccessService.checkEvents(userInfo, declarationTemplateId, departmentReportPeriod, asunId,
                        FormDataEvent.CREATE, logger);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(("Налоговая форма не создана"), logEntryService.save(logger.getEntries()));
                }

                DeclarationData newDeclaration = new DeclarationData();
                newDeclaration.setDepartmentReportPeriodId(departmentReportPeriod.getId());
                newDeclaration.setReportPeriodId(departmentReportPeriod.getReportPeriod().getId());
                newDeclaration.setDepartmentId(departmentReportPeriod.getDepartmentId());
                newDeclaration.setState(State.CREATED);
                newDeclaration.setDeclarationTemplateId(declarationTemplateId);
                newDeclaration.setTaxOrganCode(taxOrganCode);
                newDeclaration.setKpp(taxOrganKpp);
                newDeclaration.setOktmo(oktmo);
                newDeclaration.setAsnuId(asunId);
                newDeclaration.setFileName(fileName);
                newDeclaration.setNote(note);

                // Вызываем событие скрипта CREATE

                declarationDataScriptingService.executeScript(userInfo, newDeclaration, FormDataEvent.CREATE, logger, null);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(
                            "Произошли ошибки в скрипте создания налоговой формы",
                            logEntryService.save(logger.getEntries()));
                }

                // Вызываем событие скрипта AFTER_CREATE
                declarationDataScriptingService.executeScript(userInfo, newDeclaration, FormDataEvent.AFTER_CREATE, logger, null);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(
                            "Произошли ошибки в скрипте после создания налоговой формы",
                            logEntryService.save(logger.getEntries()));
                }

                long id = declarationDataDao.saveNew(newDeclaration);
                declarationDataDao.updateNote(id, note);

                logBusinessService.add(null, id, userInfo, FormDataEvent.CREATE, null);
                if (writeAudit) {
                    auditService.add(FormDataEvent.CREATE, userInfo, newDeclaration, "Налоговая форма создана", null);
                }
                return id;
            } finally {
                lockDataService.unlock(key, userInfo.getUser().getId());
            }
        } else {
            throw new ServiceException("Создание налоговой формы с указанными параметрами уже выполняется!");
        }
    }

    @Override
    @Transactional
    public Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                       DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo, Long asunId, String fileName, String note, boolean writeAudit) {
        return doCreate(logger, declarationTemplateId, userInfo, departmentReportPeriod, taxOrganCode, taxOrganKpp, oktmo, asunId, fileName, note, writeAudit);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).CALCULATE)")
    public void calculate(Logger logger, long id, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        boolean createForm = calculateDeclaration(logger, id, userInfo, docDate, exchangeParams, stateLogger);
        if (createForm) {
            declarationDataDao.setStatus(id, State.CREATED);
        }
    }

    private boolean calculateDeclaration(Logger logger, long id, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.CALCULATE);
        DeclarationData declarationData = declarationDataDao.get(id);

        //2. проверяет состояние XML отчета экземпляра декларации
        boolean createForm = setDeclarationBlobs(logger, declarationData, docDate, userInfo, exchangeParams, stateLogger);

        if (!createForm) {
            return createForm;
        }

        //3. обновляет записи о консолидации
        ArrayList<Long> declarationDataIds = new ArrayList<Long>();
        for (Relation relation : sourceService.getDeclarationSourcesInfo(declarationData, true, true, State.ACCEPTED, userInfo, logger)) {
            declarationDataIds.add(relation.getDeclarationDataId());
        }

        //Обновление информации о консолидации.
        sourceService.deleteDeclarationConsolidateInfo(id);
        sourceService.addDeclarationConsolidationInfo(id, declarationDataIds);

        logBusinessService.add(null, id, userInfo, FormDataEvent.SAVE, null);
        auditService.add(FormDataEvent.CALCULATE, userInfo, declarationData, "Налоговая форма обновлена", null);
        return createForm;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).CHECK)")
    public void check(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        LOG.info(String.format("Проверка данных налоговой формы %s", id));
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.CHECK);
        DeclarationData dd = declarationDataDao.get(id);
        Logger scriptLogger = new Logger();
        try {
            if (lockStateLogger != null) {
                lockStateLogger.updateState(AsyncTaskState.SOURCE_FORM_CHECK);
            }
            checkSources(dd, logger, userInfo);
            if (lockStateLogger != null) {
                lockStateLogger.updateState(AsyncTaskState.FORM_CHECK);
            }
            declarationDataScriptingService.executeScript(userInfo, dd, FormDataEvent.CHECK, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            if (departmentReportPeriodService.get(dd.getDepartmentReportPeriodId()).isActive()) {
                if (State.PREPARED.equals(dd.getState())) {
                    declarationDataDao.setStatus(id, State.CREATED);
                    logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_PREPARED_TO_CREATED, null);
                }
            }
        } else {
            if (departmentReportPeriodService.get(dd.getDepartmentReportPeriodId()).isActive()) {
                if (State.CREATED.equals(dd.getState())) {
                    // Переводим в состояние подготовлено
                    declarationDataDao.setStatus(id, State.PREPARED);
                    logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_CREATED_TO_PREPARED, null);
                }
            }
            logger.info("Проверка завершена, ошибок не обнаружено");
        }
    }

    @Override
    public RecalculateDeclarationResult recalculateDeclaration(TAUserInfo userInfo, final long declarationDataId, final boolean force, final boolean cancelTask) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.XML_DEC;
        final RecalculateDeclarationResult result = new RecalculateDeclarationResult();
        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        final TaxType taxType = TaxType.NDFL;

        Logger logger = new Logger();
        try {
            preCalculationCheck(logger, declarationDataId, userInfo);
        } catch (Exception e) {
            String uuid;
            if (e instanceof ServiceLoggerException) {
                uuid = ((ServiceLoggerException) e).getUuid();
            } else {
                uuid = logEntryService.save(logger.getEntries());
            }
            throw new ServiceLoggerException("%s. Обнаружены фатальные ошибки", uuid, !TaxType.DEAL.equals(taxType) ? "Налоговая форма не может быть сформирована" : "Уведомление не может быть сформировано");
        }

        try {
            String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
            Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, force, logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setStatus(CreateAsyncTaskStatus.LOCKED);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setStatus(CreateAsyncTaskStatus.CREATE);
            } else {
                result.setStatus(CreateAsyncTaskStatus.CREATE);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("declarationDataId", declarationDataId);
                params.put("docDate", new Date());
                asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, cancelTask, new AbstractStartupAsyncTaskHandler() {
                    @Override
                    public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                getDeclarationFullName(declarationDataId, ddReportType));
                    }

                    @Override
                    public void postCheckProcessing() {
                        result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                    }

                    @Override
                    public boolean checkExistTasks(AsyncTaskType reportType, TAUserInfo userInfo, Logger logger) {
                        return checkExistAsyncTask(declarationDataId, reportType, logger);
                    }

                    @Override
                    public void interruptTasks(AsyncTaskType reportType, TAUserInfo userInfo) {
                        interruptAsyncTask(declarationDataId, userInfo, reportType, TaskInterruptCause.DECLARATION_RECALCULATION);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public ActionResult recalculateDeclarationList(final TAUserInfo userInfo, List<Long> declarationDataIds) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.XML_DEC;
        final ActionResult result = new ActionResult();
        final Logger logger = new Logger();
        final TaxType taxType = TaxType.NDFL;

        for (final Long declarationDataId : declarationDataIds) {
            if (existDeclarationData(declarationDataId)) {
                final String prefix = String.format("Постановка операции \"Расчет налоговой формы\" для формы № %d в очередь на исполнение: ", declarationDataId);
                try {
                    try {
                        preCalculationCheck(logger, declarationDataId, userInfo);
                    } catch (Exception e) {
                        logger.error(prefix + "Налоговая форма не может быть рассчитана");
                    }
                    final String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                    Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
                    if (restartStatus != null && restartStatus.getFirst()) {
                        logger.warn(prefix + "Данная операция уже запущена");
                    } else if (restartStatus != null && !restartStatus.getFirst()) {
                        // задача уже была создана, добавляем пользователя в получатели
                    } else {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("declarationDataId", declarationDataId);
                        params.put("docDate", new Date());
                        asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                            @Override
                            public LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                                return lockDataService.lock(keyTask, userInfo.getUser().getId(), getDeclarationFullName(declarationDataId, ddReportType));
                            }

                            @Override
                            public void postCheckProcessing() {
                                logger.error(prefix + "Найдены запущенные задачи, которые блокирует выполнение операции.");
                            }

                            @Override
                            public boolean checkExistTasks(AsyncTaskType taskType, TAUserInfo user, Logger logger) {
                                return checkExistAsyncTask(declarationDataId, taskType, logger);
                            }

                            @Override
                            public void interruptTasks(AsyncTaskType taskType, TAUserInfo user) {
                                interruptAsyncTask(declarationDataId, userInfo, taskType, TaskInterruptCause.DECLARATION_RECALCULATION);
                            }
                        });
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error(prefix + e.getMessage());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public DeclarationResult fetchDeclarationData(TAUserInfo userInfo, long declarationDataId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        if (!existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        DeclarationResult result = new DeclarationResult();

        DeclarationData declaration = get(declarationDataId, userInfo);
        result.setDepartment(departmentService.getParentsHierarchy(
                declaration.getDepartmentId()));

        result.setState(declaration.getState().getTitle());

        String userLogin = logBusinessService.getFormCreationUserName(declaration.getId());
        if (userLogin != null && !userLogin.isEmpty()) {
            result.setCreationUserName(taUserService.getUser(userLogin).getName());
        }

        declarationDataPermissionSetter.setPermissions(declaration, DeclarationDataPermission.VIEW,
                DeclarationDataPermission.DELETE, DeclarationDataPermission.RETURN_TO_CREATED,
                DeclarationDataPermission.ACCEPTED, DeclarationDataPermission.CHECK,
                DeclarationDataPermission.CALCULATE, DeclarationDataPermission.CREATE,
                DeclarationDataPermission.EDIT_ASSIGNMENT, DeclarationDataPermission.DOWNLOAD_REPORTS);

        result.setPermissions(declaration.getPermissions());

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        result.setDeclarationFormKind(declarationTemplate.getDeclarationFormKind().getTitle());

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                declaration.getDepartmentReportPeriodId());
        result.setReportPeriod(departmentReportPeriod.getReportPeriod().getName());
        result.setReportPeriodYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());

        if (declaration.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
            result.setAsnuName(asnuProvider.getRecordData(declaration.getAsnuId()).get("NAME").getStringValue());
        }

        result.setCreationDate(sdf.format(logBusinessService.getFormCreationDate(declaration.getId())));
        return result;
    }

    @Override
    public CheckDeclarationResult checkDeclaration(TAUserInfo userInfo, final long declarationDataId, final boolean force) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
        final CheckDeclarationResult result = new CheckDeclarationResult();

        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        final TaxType taxType = TaxType.NDFL;
        Logger logger = new Logger();
        LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.ACCEPT_DEC));
        if (lockDataAccept == null) {
            String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
            if (uuidXml != null) {
                String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, force, logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", declarationDataId);
                    asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                        @Override
                        public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                            return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                        }
                    });
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            try {
                asyncManager.addUserWaitingForTask(lockDataAccept.getTaskId(), userInfo.getUser().getId());
            } catch (Exception ignored) {
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            AsyncTaskData acceptTaskData = asyncTaskDao.getLightTaskData(lockDataAccept.getTaskId());
            logger.error(
                    String.format(
                            AsyncTask.LOCK_CURRENT,
                            sdf.format(lockDataAccept.getDateLock()),
                            taUserService.getUser(lockDataAccept.getUserId()).getName(),
                            acceptTaskData.getDescription())
            );
            throw new ServiceLoggerException("Для текущего экземпляра налоговой формы запущена операция, при которой ее проверка невозможна", logEntryService.save(logger.getEntries()));
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public ActionResult checkDeclarationList(final TAUserInfo userInfo, List<Long> declarationDataIds) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.CHECK_DEC;
        final ActionResult result = new ActionResult();

        final TaxType taxType = TaxType.NDFL;
        Logger logger = new Logger();

        for (final Long declarationDataId : declarationDataIds) {
            if (existDeclarationData(declarationDataId)) {
                final String prefix = String.format("Постановка операции \"Проверка налоговой формы\" для формы № %d в очередь на исполнение: ", declarationDataId);
                try {
                    LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.ACCEPT_DEC));
                    if (lockDataAccept == null) {
                        String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
                        if (uuidXml != null) {
                            final String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                            Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
                            if (restartStatus != null && restartStatus.getFirst()) {
                                logger.warn(prefix + "Данная операция уже запущена");
                            } else if (restartStatus != null && !restartStatus.getFirst()) {
                                // задача уже была создана, добавляем пользователя в получатели
                            } else {
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("declarationDataId", declarationDataId);
                                asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                                    @Override
                                    public LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                                        return lockDataService.lock(keyTask, userInfo.getUser().getId(), getDeclarationFullName(declarationDataId, ddReportType));
                                    }

                                    @Override
                                    public boolean checkExistTasks(AsyncTaskType taskType, TAUserInfo user, Logger logger) {
                                        return false;
                                    }

                                    @Override
                                    public void interruptTasks(AsyncTaskType taskType, TAUserInfo user) {
                                    }
                                });
                            }
                        } else {
                            logger.error(prefix + "Экземпляр налоговой формы не заполнен данными.");
                        }
                    } else {
                        try {
                            asyncManager.addUserWaitingForTask(lockDataAccept.getTaskId(), userInfo.getUser().getId());
                        } catch (Exception e) {
                        }
                        AsyncTaskData acceptTaskData = asyncTaskDao.getLightTaskData(lockDataAccept.getTaskId());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        logger.error(
                                String.format(
                                        AsyncTask.LOCK_CURRENT,
                                        sdf.format(lockDataAccept.getDateLock()),
                                        taUserService.getUser(lockDataAccept.getUserId()).getName(),
                                        acceptTaskData.getDescription())
                        );
                        logger.error(prefix + "Запущена операция, при которой выполнение данной операции невозможно");
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error(prefix + e.getMessage());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public DeclarationDataFileComment fetchFilesComments(long declarationDataId) {
        if (!existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        DeclarationDataFileComment result = new DeclarationDataFileComment();

        result.setDeclarationDataId(declarationDataId);
        result.setDeclarationDataFiles(getFiles(declarationDataId));
        result.setComment(getNote(declarationDataId));
        return result;
    }

    @Override
    @Transactional
    public DeclarationDataFileComment saveDeclarationFilesComment(TAUserInfo userInfo, DeclarationDataFileComment dataFileComment) {
        long declarationDataId = dataFileComment.getDeclarationDataId();

        DeclarationDataFileComment result = new DeclarationDataFileComment();
        if (!existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        Logger logger = new Logger();
        LockData lockData = lock(declarationDataId, userInfo);
        if (lockData != null && lockData.getUserId() == userInfo.getUser().getId()) {
            try {
                declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.CALCULATE);
            } catch (AccessDeniedException e) {
                //удаляем блокировку, если пользователю недоступно редактирование
                unlock(declarationDataId, userInfo);
                throw e;
            }
            saveFilesComments(declarationDataId, dataFileComment.getComment(), dataFileComment.getDeclarationDataFiles());
            logger.info("Данные успешно сохранены.");
        } else {
            logger.error("Сохранение не выполнено, так как файлы и комментарии данного экземпляра %s не заблокированы текущим пользователем.",
                    "налоговой формы");
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setDeclarationDataFiles(getFiles(declarationDataId));
        result.setComment(getNote(declarationDataId));
        result.setDeclarationDataId(declarationDataId);

        return result;
    }

    @Override
    public List<Relation> getDeclarationSourcesAndDestinations(TAUserInfo userInfo, long declarationDataId) {
        if (existDeclarationData(declarationDataId)) {
            Logger logger = new Logger();
            DeclarationData declaration = get(declarationDataId, userInfo);

            List<Relation> relationList = new ArrayList<Relation>();
            relationList.addAll(sourceService.getDeclarationSourcesInfo(declaration, true, false, null, userInfo, logger));
            relationList.addAll(sourceService.getDeclarationDestinationsInfo(declaration, true, false, null, userInfo, logger));
            return relationList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public PagingResult<DeclarationDataJournalItem> fetchDeclarations(TAUserInfo userInfo, DeclarationDataFilter filter, PagingParams pagingParams) {
        TAUser currentUser = userInfo.getUser();

        if (CollectionUtils.isEmpty(filter.getAsnuIds())) {
            //Контролерам доступны все АСНУ, поэтому фильтрации по АСНУ нет, поэтому список для них пустой
            //Операторам доступны только некоторые АСНУ. Если такие есть, добавить их в список. Если доступных АСНУ нет, то
            //список будет состоять из 1 элемента (-1), который не может быть id существующего АСНУ, чтобы не нашлась ни одна форма
            List<Long> asnuIds = new ArrayList<Long>();
            if (!currentUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRole(TARole.N_ROLE_OPER)) {
                List<RefBookAsnu> avaliableAsnuList = refBookAsnuService.fetchAvailableAsnu(userInfo);
                if (!avaliableAsnuList.isEmpty()) {
                    for (RefBookAsnu asnu : refBookAsnuService.fetchAvailableAsnu(userInfo)) {
                        asnuIds.add(asnu.getId());
                    }
                } else {
                    asnuIds.add(-1L);
                }
            }
            filter.setAsnuIds(asnuIds);
        }

        if (CollectionUtils.isEmpty(filter.getDepartmentIds())) {
            Set<Integer> receiverDepartmentIds = new HashSet<Integer>();
            for (RefBookDepartment department : refBookDepartmentDataService.fetchAllAvailableDepartments(currentUser)) {
                receiverDepartmentIds.add(department.getId());
            }
            filter.setDepartmentIds(new ArrayList<Integer>(receiverDepartmentIds));
        }

        if (CollectionUtils.isEmpty(filter.getFormKindIds())) {
            List<Long> availableDeclarationFormKindIds = new ArrayList<Long>();
            if (currentUser.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                availableDeclarationFormKindIds.addAll(Arrays.asList(DeclarationFormKind.PRIMARY.getId(), DeclarationFormKind.CONSOLIDATED.getId()));
            } else if (currentUser.hasRole(TaxType.NDFL, TARole.N_ROLE_OPER)) {
                availableDeclarationFormKindIds.add(DeclarationFormKind.PRIMARY.getId());
            }
            filter.setFormKindIds(availableDeclarationFormKindIds);
        }

        filter.setTaxType(TaxType.NDFL);

        if (!currentUser.hasRoles(TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRoles(TARole.N_ROLE_CONTROL_NS)) {
            filter.setUserDepartmentId(departmentService.getParentTB(currentUser.getDepartmentId()).getId());
            filter.setControlNs(true);
        } else if (!currentUser.hasRoles(TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRoles(TARole.N_ROLE_OPER)) {
            filter.setUserDepartmentId(currentUser.getDepartmentId());
            filter.setControlNs(false);
        }

        PagingResult<DeclarationDataJournalItem> page = declarationDataDao.findPage(filter, pagingParams);
        setPageItemsPermissions(page);

        return page;
    }

    /**
     * Установка прав доступа для всех налоговых форм страницы
     *
     * @param page Страница списка налоговых форм
     */
    private void setPageItemsPermissions(PagingResult<DeclarationDataJournalItem> page) {
        if (!page.isEmpty()) {
            //Получение id всех форм
            List<Long> declarationIds = new ArrayList<Long>();
            for (DeclarationDataJournalItem item : page) {
                declarationIds.add(item.getDeclarationDataId());
            }

            //Сохранение в мапе для получения формы по id
            Map<Long, DeclarationData> declarationDataMap = new HashMap<Long, DeclarationData>();
            for (DeclarationData declarationData : declarationDataDao.get(declarationIds)) {
                declarationDataMap.put(declarationData.getId(), declarationData);
            }

            //Для каждого элемента страницы взять форму, определить права доступа на нее и установить их элементу страницы
            for (DeclarationDataJournalItem item : page) {
                DeclarationData declaration = declarationDataMap.get(item.getDeclarationDataId());
                declarationDataPermissionSetter.setPermissions(declaration, DeclarationDataPermission.VIEW,
                        DeclarationDataPermission.DELETE, DeclarationDataPermission.RETURN_TO_CREATED,
                        DeclarationDataPermission.ACCEPTED, DeclarationDataPermission.CHECK,
                        DeclarationDataPermission.CALCULATE, DeclarationDataPermission.CREATE,
                        DeclarationDataPermission.EDIT_ASSIGNMENT, DeclarationDataPermission.DOWNLOAD_REPORTS);
                item.setPermissions(declaration.getPermissions());
            }
        }
    }


    //Формирование рну ндфл для физ лица
    @Override
    public CreateDeclarationReportResult createReportRnu(TAUserInfo userInfo, final long declarationDataId, long personId, final NdflPersonFilter ndflPersonFilter) {
        final DeclarationDataReportType ddReportType = new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, null);
        CreateDeclarationReportResult result = new CreateDeclarationReportResult();
        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        if (ddReportType.isSubreport()) {
            DeclarationData declaration = get(declarationDataId, userInfo);
            ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), SubreportAliasConstants.RNU_NDFL_PERSON_DB));
        } else if (ddReportType.equals(DeclarationDataReportType.PDF_DEC) && !isVisiblePDF(get(declarationDataId, userInfo), userInfo)) {
            throw new ServiceException("Данное действие недоступно");
        }

        Map<String, Object> filterParams = new HashMap<String, Object>();
        NdflPerson ndflPerson = null;

        //поиск лица для которого формируется рну
        for (NdflPerson itemNdflPerson : ndflPersonService.findPersonByFilter(declarationDataId, filterParams, new PagingParams())) {
            if (itemNdflPerson.getPersonId().equals(personId)) {
                ndflPerson = itemNdflPerson;
            }
        }

        filterParams.put("PERSON_ID", ndflPerson.getId());

        //Узнаем статус налогоплательщика
        if (refBookFactory.getDataProvider(RefBook.Id.TAXPAYER_STATUS.getId()).
                getRecords(null, null, "CODE = '" + ndflPerson.getStatus() + "'", null).get(0) != null) {
            ndflPerson.setStatus(refBookFactory.getDataProvider(RefBook.Id.TAXPAYER_STATUS.getId()).
                    getRecords(null, null, "CODE = '" + ndflPerson.getStatus() + "'", null).get(0).
                    get("NAME").getStringValue());
        } else {
            ndflPerson.setStatus("");
        }


        Logger logger = new Logger();
        String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
        if (uuidXml != null) {
            final String uuid = reportService.getDec(userInfo, declarationDataId, ddReportType);
            if (uuid != null) {
                result.setStatus(CreateAsyncTaskStatus.EXIST);
                return result;
            } else {
                String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>(10);
                    params.put("declarationDataId", declarationDataId);
                    if (ddReportType.isSubreport()) {
                        params.put("alias", ddReportType.getReportAlias());
                        params.put("viewParamValues", new LinkedHashMap<String, String>());
                        if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
                            params.put("subreportParamValues", filterParams);

                            if (ndflPerson != null) {
                                params.put("PERSON_ID", ndflPerson.getId());
                            }
                        }
                    }
                    asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {

                        @Override
                        public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                    getDeclarationFullName(declarationDataId, ddReportType));
                        }

                        @Override
                        public void postCheckProcessing() {
                        }

                        @Override
                        public boolean checkExistTasks(AsyncTaskType reportType, TAUserInfo userInfo, Logger logger) {
                            return false;
                        }

                        @Override
                        public void interruptTasks(AsyncTaskType reportType, TAUserInfo userInfo) {
                            if (uuid != null) {
                                reportService.deleteDec(uuid);
                            }
                        }
                    });
                }
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public CreateDeclarationReportResult createReportAllRnu(final TAUserInfo userInfo, final long declarationDataId, boolean force) {
        DeclarationData declaration = get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());

        CreateDeclarationReportResult result = new CreateDeclarationReportResult();

        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }

        Logger logger = new Logger();
        String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
        if (uuidXml != null) {
            for (DeclarationSubreport subreport : declarationTemplate.getSubreports()) {
                final DeclarationDataReportType ddReportType = new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, subreport);

                ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), "rnu_ndfl_person_all_db"));
                final String uuid = reportService.getDec(userInfo, declarationDataId, ddReportType);
                if (uuid != null) {
                    result.setStatus(CreateAsyncTaskStatus.EXIST);
                    return result;
                } else {
                    final String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                    Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, force, logger);
                    if (restartStatus != null && restartStatus.getFirst()) {
                        result.setStatus(CreateAsyncTaskStatus.LOCKED);
                        result.setRestartMsg(restartStatus.getSecond());
                    } else if (restartStatus != null && !restartStatus.getFirst()) {
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                    } else {
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("declarationDataId", declarationDataId);
                        params.put("alias", ddReportType.getReportAlias());
                        params.put("viewParamValues", new LinkedHashMap<String, String>());

                        asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                            @Override
                            public LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                                return lockDataService.lock(keyTask, userInfo.getUser().getId(), getDeclarationFullName(declarationDataId, ddReportType));
                            }

                            @Override
                            public boolean checkExistTasks(AsyncTaskType taskType, TAUserInfo user, Logger logger) {
                                return false;
                            }

                            @Override
                            public void interruptTasks(AsyncTaskType taskType, TAUserInfo user) {
                                if (uuid != null) {
                                    reportService.deleteDec(uuid);
                                }
                            }
                        });
                    }
                }
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public CreateDeclarationReportResult createReportXlsx(final TAUserInfo userInfo, final long declarationDataId, boolean force) {
        final DeclarationDataReportType ddReportType = new DeclarationDataReportType(AsyncTaskType.EXCEL_DEC, null);
        CreateDeclarationReportResult result = new CreateDeclarationReportResult();
        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }

        Logger logger = new Logger();
        String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
        if (uuidXml != null) {
            final String uuid = reportService.getDec(userInfo, declarationDataId, ddReportType);
            if (uuid != null) {
                result.setStatus(CreateAsyncTaskStatus.EXIST);
                return result;
            } else {
                final String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, force, logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", declarationDataId);

                    asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                        @Override
                        public LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                            return lockDataService.lock(keyTask, userInfo.getUser().getId(), getDeclarationFullName(declarationDataId, ddReportType));
                        }

                        @Override
                        public void interruptTasks(AsyncTaskType taskType, TAUserInfo user) {
                            if (uuid != null) {
                                reportService.deleteDec(uuid);
                            }
                        }
                    });
                }
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public ReportAvailableResult checkAvailabilityReports(TAUserInfo userInfo, long declarationDataId) {
        ReportAvailableResult reportAvailableResult = new ReportAvailableResult();
        reportAvailableResult.setDownloadXlsxAvailable(reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.EXCEL_DEC) != null);
        reportAvailableResult.setDownloadXmlAvailable(reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC) != null);

        DeclarationData declaration = get(declarationDataId, userInfo);
        List<DeclarationSubreport> subreports = declarationTemplateService.get(declaration.getDeclarationTemplateId()).getSubreports();
        for (DeclarationSubreport subreport : subreports) {
            if ("rnu_ndfl_person_all_db".equals(subreport.getAlias())) {
                reportAvailableResult.setDownloadSpecificAvailable((reportService.getDec(userInfo, declarationDataId, new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, subreport))) != null);
            }
        }

        return reportAvailableResult;
    }

    @Override
    public void preCalculationCheck(Logger logger, long declarationDataId, TAUserInfo userInfo) {
        declarationDataScriptingService.executeScript(userInfo,
                declarationDataDao.get(declarationDataId), FormDataEvent.PRE_CALCULATION_CHECK, logger, null);
        // Проверяем ошибки
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Найдены ошибки при выполнении расчета налоговой формы",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public DeclarationData get(long id, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        return declarationDataDao.get(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).DELETE)")
    public void delete(long id, TAUserInfo userInfo) {
        delete(id, userInfo, true);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).DELETE)")
    public void deleteIfExists(long id, TAUserInfo userInfo) {
        if (existDeclarationData(id)) {
            delete(id, userInfo);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).DELETE)")
    public void delete(long id, TAUserInfo userInfo, boolean createLock) {
        LockData lockData = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.XML_DEC));
        LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.ACCEPT_DEC));
        LockData lockDataCheck = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.CHECK_DEC));
        LockData lockDataDelete = null;
        if (lockData == null && lockDataAccept == null && lockDataCheck == null && createLock) {
            lockDataDelete = lockDataService.lock(generateAsyncTaskKey(id, DeclarationDataReportType.DELETE_DEC), userInfo.getUser().getId(),
                    getDeclarationFullName(id, DeclarationDataReportType.DELETE_DEC));
        }
        if (lockData == null && lockDataAccept == null && lockDataCheck == null && lockDataDelete == null) {
            try {
                declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.DELETE);
                DeclarationData declarationData = declarationDataDao.get(id);

                Logger logger = new Logger();
                declarationDataScriptingService.executeScript(userInfo,
                        declarationData, FormDataEvent.DELETE, new Logger(), null);

                // Проверяем ошибки
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(
                            "Найдены ошибки при выполнении удаления налоговой формы",
                            logEntryService.save(logger.getEntries()));
                }

                deleteReport(id, userInfo, false, TaskInterruptCause.DECLARATION_DELETE);
                declarationDataDao.delete(id);

                auditService.add(FormDataEvent.DELETE, userInfo, declarationData, "Налоговая форма удалена", null);
            } finally {
                if (createLock) {
                    lockDataService.unlock(generateAsyncTaskKey(id, DeclarationDataReportType.DELETE_DEC), userInfo.getUser().getId());
                }
            }
        } else {
            if (lockData == null) {
                lockData = lockDataAccept;
            }
            if (lockData == null) {
                lockData = lockDataCheck;
            }
            if (lockData == null) {
                lockData = lockDataDelete;
            }
            Logger logger = new Logger();
            TAUser blocker = taUserService.getUser(lockData.getUserId());
            String description = lockData.getDescription();
            if (lockData.getTaskId() != null) {
                AsyncTaskData taskData = asyncManager.getLightTaskData(lockData.getTaskId());
                if (taskData != null) {
                    description = taskData.getDescription();
                }
            }
            logger.error("Текущая налоговая форма не может быть удалена, т.к. пользователем \"%s\" в \"%s\" запущена операция \"%s\"", blocker.getName(), SDF_DD_MM_YYYY_HH_MM_SS.get().format(lockData.getDateLock()), description);
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    @Transactional
    public ActionResult deleteDeclarationList(TAUserInfo userInfo, List<Long> declarationDataIds) {
        ActionResult result = new ActionResult();
        Logger logger = new Logger();

        for (Long declarationId : declarationDataIds) {
            if (existDeclarationData(declarationId)) {
                String declarationFullName = getDeclarationFullName(declarationId, null);
                try {
                    delete(declarationId, userInfo);
                    logger.info("Успешно удалён объект: %s.", declarationFullName);
                    sendNotification("Успешно удалён объект: " + declarationFullName, logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                    logger.clear();
                } catch (ServiceLoggerException e) {
                    logger.getEntries().addAll(logEntryService.getAll(e.getUuid()));
                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationId);
            }
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.logTopMessage(LogLevel.ERROR, "При удалении возникли ошибки:");
            throw new ServiceLoggerException("При удалении возникли ошибки", logEntryService.save(logger.getEntries()));
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).ACCEPTED)")
    public void accept(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_PREPARED_TO_ACCEPTED);

        DeclarationData declarationData = declarationDataDao.get(id);

        Logger scriptLogger = new Logger();
        try {
            lockStateLogger.updateState(AsyncTaskState.SOURCE_FORM_CHECK);
            checkSources(declarationData, logger, userInfo);
            lockStateLogger.updateState(AsyncTaskState.FORM_CHECK);
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException();
        }

        declarationData.setState(State.ACCEPTED);

        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_PREPARED_TO_ACCEPTED, null);
        auditService.add(FormDataEvent.MOVE_PREPARED_TO_ACCEPTED, userInfo, declarationData, FormDataEvent.MOVE_PREPARED_TO_ACCEPTED.getTitle(), null);

        lockStateLogger.updateState(AsyncTaskState.FORM_STATUS_CHANGE);

        declarationDataDao.setStatus(id, declarationData.getState());
    }

    @Override
    @Transactional
    public ActionResult acceptDeclarationList(final TAUserInfo userInfo, List<Long> declarationDataIds) {
        final ActionResult result = new ActionResult();
        final Logger logger = new Logger();

        final TaxType taxType = TaxType.NDFL;
        final DeclarationDataReportType ddToAcceptedReportType = DeclarationDataReportType.ACCEPT_DEC;

        for (final Long declarationId : declarationDataIds) {
            if (existDeclarationData(declarationId)) {
                final String prefix = String.format("Постановка операции \"Принятие налоговой формы\" для формы № %d в очередь на исполнение: ", declarationId);
                try {
                    String uuidXml = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
                    if (uuidXml != null) {
                        DeclarationData declarationData = get(declarationId, userInfo);
                        if (!declarationData.getState().equals(State.ACCEPTED)) {
                            final String keyTask = generateAsyncTaskKey(declarationId, ddToAcceptedReportType);
                            Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
                            if (restartStatus != null && restartStatus.getFirst()) {
                                logger.warn(prefix + "Данная операция уже запущена");
                            } else if (restartStatus != null && !restartStatus.getFirst()) {
                                // задача уже была создана, добавляем пользователя в получатели
                            } else {
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("declarationDataId", declarationId);
                                asyncManager.executeTask(keyTask, ddToAcceptedReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                                    @Override
                                    public LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                                        return lockDataService.lock(keyTask, userInfo.getUser().getId(), getDeclarationFullName(declarationId, ddToAcceptedReportType));
                                    }

                                    @Override
                                    public void postCheckProcessing() {
                                        logger.error(prefix + "Найдена запущенная задача, которая блокирует выполнение операции.");
                                    }

                                    @Override
                                    public boolean checkExistTasks(AsyncTaskType taskType, TAUserInfo user, Logger logger) {
                                        return checkExistAsyncTask(declarationId, taskType, logger);
                                    }

                                    @Override
                                    public void interruptTasks(AsyncTaskType taskType, TAUserInfo user) {
                                        interruptAsyncTask(declarationId, userInfo, taskType, TaskInterruptCause.DECLARATION_ACCEPT);
                                    }
                                });
                            }
                        } else {
                            logger.error(prefix + "Налоговая форма уже находиться в статусе \"%s\".", State.ACCEPTED.getTitle());
                        }
                    } else {
                        logger.error(prefix + "Экземпляр налоговой формы не заполнен данными.");
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    logger.error(prefix + e.getMessage());
                }
            }
        }

        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).RETURN_TO_CREATED)")
    public void cancel(Logger logger, long id, String note, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);
        DeclarationData declarationData = declarationDataDao.get(id);

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger, exchangeParams);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
        }

        declarationData.setState(State.CREATED);
        sourceService.updateDDConsolidation(declarationData.getId());

        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, note);
        auditService.add(FormDataEvent.MOVE_ACCEPTED_TO_CREATED, userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED.getTitle(), null);

        declarationDataDao.setStatus(id, declarationData.getState());
    }

    @Override
    @Transactional
    public ActionResult cancelDeclarationList(List<Long> declarationDataIds, String note, TAUserInfo userInfo) {
        final ActionResult result = new ActionResult();
        final Logger logger = new Logger();

        for (Long declarationId : declarationDataIds) {
            if (existDeclarationData(declarationId)) {
                String declarationFullName = getDeclarationFullName(declarationId, DeclarationDataReportType.TO_CREATE_DEC);
                LockData lockData = lockDataService.lock(generateAsyncTaskKey(declarationId, DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId(), declarationFullName);
                if (lockData == null) {
                    try {
                        List<Long> receiversIdList = getReceiversAcceptedPrepared(declarationId, logger, userInfo);
                        if (receiversIdList.isEmpty()) {
                            cancel(logger, declarationId, note, userInfo);
                            String message = new Formatter().format("Налоговая форма № %d успешно переведена в статус \"%s\".", declarationId, State.CREATED.getTitle()).toString();
                            logger.info(message);
                            sendNotification("Выполнена операция \"Возврат в Создана\"", logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                        } else {
                            String message = getCheckReceiversErrorMessage(receiversIdList);
                            logger.error(message);
                            sendNotification(message, logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                        }
                        logger.clear();
                    } catch (Exception e) {
                        logger.error(e);
                    } finally {
                        lockDataService.unlock(generateAsyncTaskKey(declarationId, DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId());
                    }
                } else {
                    DeclarationData declaration = get(declarationId, userInfo);
                    Department department = departmentService.getDepartment(declaration.getDepartmentId());
                    DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
                    logger.error("Форма \"%s\" из \"%s\" заблокирована", declarationTemplate.getType().getName(), department.getName());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationId);
            }
        }

        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    private String getCheckReceiversErrorMessage(List<Long> receivers) {
        StringBuilder sb = new StringBuilder("Отмена принятия текущей формы невозможна. Формы-приёмники ");
        for (Long receiver : receivers) {
            sb.append(receiver).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" имеют состояние, отличное от \"Создана\". Выполните \"Возврат в Создана\" для перечисленных форм и повторите операцию.");
        return sb.toString();
    }

    @Override
    public List<Long> getReceiversAcceptedPrepared(long declarationDataId, Logger logger, TAUserInfo userInfo) {
        List<Long> toReturn = new LinkedList<Long>();
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

        if (declarationTemplate.getDeclarationFormKind().getId() == DeclarationFormKind.PRIMARY.getId()
                || declarationTemplate.getDeclarationFormKind().getId() == DeclarationFormKind.CONSOLIDATED.getId()) {
            List<Relation> relations = sourceService.getDeclarationDestinationsInfo(declarationData, true, false, null, userInfo, logger);
            for (Relation relation : relations) {
                if (relation.isCreated() && !State.CREATED.equals(relation.getDeclarationState())) {
                    toReturn.add(relation.getDeclarationDataId());
                }
            }
        }
        return toReturn;
    }

    @Override
    public InputStream getXmlDataAsStream(long declarationId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationId, FormDataEvent.GET_LEVEL1);
        String xmlUuid = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
        if (xmlUuid == null) {
            return null;
        }
        return blobDataService.get(xmlUuid).getInputStream();
    }

    @Override
    public String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
        try {
            String xmlUuid = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
            if (xmlUuid == null) return null;
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getName();
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            return null;
        }
    }

    @Override
    public LocalDateTime getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
        try {
            String xmlUuid = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
            if (xmlUuid == null) return null;
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getCreationDate();
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            return null;
        }
    }

    public void getXlsxData(long id, File xlsxFile, TAUserInfo userInfo, LockStateLogger stateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        DeclarationData declarationData = declarationDataDao.get(id);
        String uuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.JASPER_DEC);
        JasperPrint jasperPrint;
        JRSwapFile jrSwapFile = null;
        try {
            if (uuid != null) {
                ObjectInputStream objectInputStream = null;
                InputStream zipJasper = null;
                try {
                    zipJasper = blobDataService.get(uuid).getInputStream();
                    ZipInputStream zipJasperIn = new ZipInputStream(zipJasper);
                    try {
                        zipJasperIn.getNextEntry();
                        objectInputStream = new ObjectInputStream(zipJasperIn);
                        jasperPrint = (JasperPrint) objectInputStream.readObject();
                    } finally {
                        IOUtils.closeQuietly(zipJasperIn);
                    }
                } catch (IOException e) {
                    throw new ServiceException("Не удалось извлечь Jasper-отчет.", e);
                } catch (ClassNotFoundException e) {
                    throw new ServiceException("Не удалось извлечь Jasper-отчет.", e);
                } finally {
                    IOUtils.closeQuietly(zipJasper);
                    IOUtils.closeQuietly(objectInputStream);
                }
            } else {
                LOG.info(String.format("Заполнение Jasper-макета налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.FILLING_JASPER);
                jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 4096, 1000);
                jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);
                // для XLSX-отчета не сохраняем Jasper-отчет из-за возмжных проблем с паралельным формированием PDF-отчета
            }
            LOG.info(String.format("Заполнение XLSX-отчета налоговой формы %s", declarationData.getId()));
            stateLogger.updateState(AsyncTaskState.FILLING_XLSX_REPORT);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(xlsxFile);
                exportXLSX(jasperPrint, outputStream);
            } catch (FileNotFoundException e) {
                throw new ServiceException("Ошибка при работе с временным файлом для XLSX", e);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        } finally {
            if (jrSwapFile != null)
                jrSwapFile.dispose();
        }
    }

    @Override
    public InputStream getPdfDataAsStream(long declarationId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationId, FormDataEvent.GET_LEVEL0);
        String pdfUuid = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.PDF_DEC);
        if (pdfUuid == null) {
            return null;
        }
        return blobDataService.get(pdfUuid).getInputStream();
    }

    private InputStream getJasper(String jrxmlTemplate) {
        if (jrxmlTemplate == null) {
            throw new ServiceException("Шаблон отчета не найден");
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jrxmlTemplate.getBytes(ENCODING));
            return compileReport(inputStream);
        } catch (UnsupportedEncodingException e2) {
            LOG.error(e2.getMessage(), e2);
            throw new ServiceException("Шаблон отчета имеет неправильную кодировку!");
        }
    }

    private ByteArrayInputStream compileReport(InputStream jrxml) {
        ByteArrayOutputStream compiledReport = new ByteArrayOutputStream();
        try {
            JasperDesign jasperDesign = JRXmlLoader.load(jrxml);
            JasperCompileManager.compileReportToStream(jasperDesign, compiledReport);
        } catch (JRException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка компиляции jrxml-шаблона! " + jrxml);
        }
        return new ByteArrayInputStream(compiledReport.toByteArray());
    }


    @Override
    public JasperPrint createJasperReport(InputStream xmlIn, String jrxml, JRSwapFile jrSwapFile, Map<String, Object> params) {
        InputStream jasperTemplate = null;
        try {
            jasperTemplate = getJasper(jrxml);
            return fillReport(xmlIn, jasperTemplate, jrSwapFile, params);
        } finally {
            IOUtils.closeQuietly(xmlIn);
            IOUtils.closeQuietly(jasperTemplate);
        }
    }

    private JasperPrint createJasperReport(DeclarationData declarationData, JRSwapFile jrSwapFile, TAUserInfo userInfo) {
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC);
        InputStream zipXml = blobDataService.get(xmlUuid).getInputStream();
        try {
            if (zipXml != null) {
                ZipInputStream zipXmlIn = new ZipInputStream(zipXml);
                try {
                    zipXmlIn.getNextEntry();
                    Map<String, Object> params = new HashMap<String, Object>();

                    params.put("declarationId", declarationData.getId());
                    return createJasperReport(zipXmlIn, declarationTemplateService.getJrxml(declarationData.getDeclarationTemplateId()), jrSwapFile, params);
                } catch (IOException e) {
                    throw new ServiceException(e.getLocalizedMessage(), e);
                } finally {
                    IOUtils.closeQuietly(zipXmlIn);
                }
            } else {
                throw new ServiceException("Налоговая форма не сформирована");
            }
        } finally {
            IOUtils.closeQuietly(zipXml);
        }
    }

    @Override
    public void setPdfDataBlobs(Logger logger,
                                DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        LOG.info(String.format("Удаление старых отчетов налоговой формы %s", declarationData.getId()));
        reportService.deleteDec(Arrays.asList(declarationData.getId()), Arrays.asList(DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.JASPER_DEC));
        LOG.info(String.format("Получение данных налоговой формы %s", declarationData.getId()));
        stateLogger.updateState(AsyncTaskState.GET_FORM_DATA);
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC);
        if (xmlUuid != null) {
            File pdfFile = null;
            JRSwapFile jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 100);
            try {
                LOG.info(String.format("Заполнение Jasper-макета налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.FILLING_JASPER);
                JasperPrint jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);

                LOG.info(String.format("Заполнение PDF-файла налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.FILLING_PDF);
                pdfFile = File.createTempFile("report", ".pdf");

                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(pdfFile);
                    exportPDF(jasperPrint, outputStream);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }

                LOG.info(String.format("Сохранение PDF-файла в базе данных для налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.SAVING_PDF);
                reportService.createDec(declarationData.getId(), blobDataService.create(pdfFile.getPath(), ""), DeclarationDataReportType.PDF_DEC);

                // не сохраняем jasper-отчет, если есть XLSX-отчет
                if (reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.EXCEL_DEC) == null) {
                    LOG.info(String.format("Сохранение Jasper-макета в базе данных для налоговой формы %s", declarationData.getId()));
                    stateLogger.updateState(AsyncTaskState.SAVING_JASPER);
                    reportService.createDec(declarationData.getId(), saveJPBlobData(jasperPrint), DeclarationDataReportType.JASPER_DEC);
                }
            } catch (IOException e) {
                throw new ServiceException(e.getLocalizedMessage(), e);
            } finally {
                if (pdfFile != null)
                    pdfFile.delete();
                if (jrSwapFile != null)
                    jrSwapFile.dispose();
            }
        } else {
            throw new ServiceException("Налоговая форма не сформирована");
        }
    }

    @Override
    public String createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, Map<String, String> viewParamValues, DataRow<Cell> selectedRecord, TAUserInfo userInfo, LockStateLogger stateLogger) {
        Map<String, Object> params = new HashMap<String, Object>();
        ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = new ScriptSpecificDeclarationDataReportHolder();
        File reportFile = null;
        try {
            reportFile = File.createTempFile("specific_report", ".dat");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile));
            InputStream inputStream = null;
            if (ddReportType.getSubreport().getBlobDataId() != null) {
                inputStream = blobDataService.get(ddReportType.getSubreport().getBlobDataId()).getInputStream();
            }
            try {
                scriptSpecificReportHolder.setDeclarationSubreport(ddReportType.getSubreport());
                scriptSpecificReportHolder.setFileOutputStream(outputStream);
                scriptSpecificReportHolder.setFileInputStream(inputStream);
                scriptSpecificReportHolder.setFileName(ddReportType.getSubreport().getAlias());
                scriptSpecificReportHolder.setSubreportParamValues(subreportParamValues);
                scriptSpecificReportHolder.setSelectedRecord(selectedRecord);
                scriptSpecificReportHolder.setViewParamValues(viewParamValues);
                params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
                stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
                if (!declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CREATE_SPECIFIC_REPORT, logger, params)) {
                    throw new ServiceException("Не предусмотрена возможность формирования отчета \"%s\"", ddReportType.getSubreport().getName());
                }
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
                }
            } finally {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(inputStream);
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

    @Override
    public PrepareSpecificReportResult prepareSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, TAUserInfo userInfo) {
        Map<String, Object> params = new HashMap<String, Object>();
        ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = new ScriptSpecificDeclarationDataReportHolder();
        InputStream inputStream = null;
        if (ddReportType.getSubreport().getBlobDataId() != null) {
            inputStream = blobDataService.get(ddReportType.getSubreport().getBlobDataId()).getInputStream();
        }
        try {
            scriptSpecificReportHolder.setDeclarationSubreport(ddReportType.getSubreport());
            scriptSpecificReportHolder.setFileInputStream(inputStream);
            scriptSpecificReportHolder.setFileName(ddReportType.getSubreport().getAlias());
            scriptSpecificReportHolder.setSubreportParamValues(subreportParamValues);
            params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.PREPARE_SPECIFIC_REPORT, logger, params);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
            }

            return scriptSpecificReportHolder.getPrepareSpecificReportResult();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


    public String setXlsxDataBlobs(Logger logger, DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        String script = declarationTemplateService.getDeclarationTemplateScript(declarationData.getDeclarationTemplateId());
        if (DeclarationDataScriptingServiceImpl.canExecuteScript(script, FormDataEvent.CREATE_EXCEL_REPORT)) {
            Map<String, Object> params = new HashMap<String, Object>();
            ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = new ScriptSpecificDeclarationDataReportHolder();
            File reportFile = null;
            try {
                reportFile = File.createTempFile("specific_report", ".dat");
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile));
                InputStream inputStream = null;
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                if (declarationTemplate.getJrxmlBlobId() != null) {
                    inputStream = blobDataService.get(declarationTemplate.getJrxmlBlobId()).getInputStream();
                }
                try {
                    scriptSpecificReportHolder.setFileOutputStream(outputStream);
                    scriptSpecificReportHolder.setFileInputStream(inputStream);
                    scriptSpecificReportHolder.setFileName("report.xlsx");
                    params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
                    stateLogger.updateState(AsyncTaskState.FILLING_XLSX_REPORT);
                    declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CREATE_EXCEL_REPORT, logger, params);
                    if (logger.containsLevel(LogLevel.ERROR)) {
                        throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
                    }
                } finally {
                    IOUtils.closeQuietly(outputStream);
                    IOUtils.closeQuietly(inputStream);
                }
                stateLogger.updateState(AsyncTaskState.SAVING_XLSX);
                return blobDataService.create(reportFile.getPath(), scriptSpecificReportHolder.getFileName());
            } catch (IOException e) {
                throw new ServiceException(e.getLocalizedMessage(), e);
            } finally {
                if (reportFile != null)
                    reportFile.delete();
            }
        } else {
            File xlsxFile = null;
            try {
                xlsxFile = File.createTempFile("report", ".xlsx");
                getXlsxData(declarationData.getId(), xlsxFile, userInfo, stateLogger);

                LOG.info(String.format("Сохранение XLSX в базе данных для налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.SAVING_XLSX);

                reportService.deleteDec(Arrays.asList(declarationData.getId()), Arrays.asList(DeclarationDataReportType.JASPER_DEC));
                return blobDataService.create(xlsxFile.getPath(), getXmlDataFileName(declarationData.getId(), userInfo).replace("zip", "xlsx"));
            } catch (IOException e) {
                throw new ServiceException("Ошибка при формировании временного файла для XLSX", e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new ServiceException(e.getMessage());
            } finally {
                if (xlsxFile != null)
                    xlsxFile.delete();
            }
        }
    }

    private boolean setDeclarationBlobs(Logger logger,
                                        DeclarationData declarationData, Date docDate, TAUserInfo userInfo, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        if (exchangeParams == null) {
            exchangeParams = new HashMap<String, Object>();
        }
        exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, docDate);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DeclarationDataScriptParams.NOT_REPLACE_XML, false);
        exchangeParams.put("calculateParams", params);

        File xmlFile = null;
        Writer fileWriter = null;

        try {
            try {
                LOG.info(String.format("Создание временного файла для записи расчета для налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.PREPARE_TEMP_FILE);
                try {
                    xmlFile = File.createTempFile("file_for_validate", ".xml");
                    fileWriter = new FileWriter(xmlFile);
                    fileWriter.write(XML_HEADER);
                } catch (IOException e) {
                    throw new ServiceException("Ошибка при формировании временного файла для XML", e);
                }
                exchangeParams.put(DeclarationDataScriptParams.XML, fileWriter);
                exchangeParams.put(DeclarationDataScriptParams.XML_FILE, xmlFile);
                LOG.info(String.format("Формирование XML-файла налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.BUILDING_XML);
                declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceException();
                }
            } finally {
                IOUtils.closeQuietly(fileWriter);
            }

            boolean notReplaceXml = false;
            if (params.containsKey(DeclarationDataScriptParams.NOT_REPLACE_XML) && params.get(DeclarationDataScriptParams.NOT_REPLACE_XML) != null) {
                notReplaceXml = (Boolean) params.get(DeclarationDataScriptParams.NOT_REPLACE_XML);
            }
            if (!notReplaceXml) {
                //Получение имени файла записанного в xml
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                SAXHandler handler = new SAXHandler(new HashMap<String, String>() {{
                    put(TAG_FILE, ATTR_FILE_ID);
                    put(TAG_DOCUMENT, ATTR_DOC_DATE);
                }});
                saxParser.parse(xmlFile, handler);
                String decName = handler.getValues().get(TAG_FILE);
                Date decDate = getFormattedDate(handler.getValues().get(TAG_DOCUMENT));
                if (decDate == null) {
                    decDate = docDate;
                }

                //Архивирование перед сохраннеием в базу
                File zipOutFile = null;
                try {
                    zipOutFile = File.createTempFile("xml", ".zip");
                    FileOutputStream fileOutputStream = new FileOutputStream(zipOutFile);
                    ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
                    ZipEntry zipEntry = new ZipEntry(decName + ".xml");
                    zos.putNextEntry(zipEntry);
                    FileInputStream fi = new FileInputStream(xmlFile);

                    try {
                        IOUtils.copy(fi, zos);
                    } finally {
                        IOUtils.closeQuietly(fi);
                        IOUtils.closeQuietly(zos);
                        IOUtils.closeQuietly(fileOutputStream);
                    }

                    LOG.info(String.format("Сохранение XML-файла в базе данных для налоговой формы %s", declarationData.getId()));
                    stateLogger.updateState(AsyncTaskState.SAVING_XML);

                    reportService.deleteDec(Arrays.asList(declarationData.getId()), Arrays.asList(DeclarationDataReportType.XML_DEC));
                    reportService.createDec(declarationData.getId(),
                            blobDataService.create(zipOutFile, decName + ".zip", new LocalDateTime(decDate)),
                            DeclarationDataReportType.XML_DEC);
                    declarationDataDao.setFileName(declarationData.getId(), decName);
                } finally {
                    if (zipOutFile != null && !zipOutFile.delete()) {
                        LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                    }
                }
            }

            exchangeParams.put(DeclarationDataScriptParams.XML, null);
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.AFTER_CALCULATE, logger, exchangeParams);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceException();
            }
        } catch (IOException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка при парсинге xml", e);
            throw new ServiceException("", e);
        } catch (SAXException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        } finally {
            if (xmlFile != null && !xmlFile.delete())
                LOG.warn(String.format(FILE_NOT_DELETE, xmlFile.getName()));
        }
        if (params.get(DeclarationDataScriptParams.CREATE_FORM) != null) {
            return (Boolean) params.get(DeclarationDataScriptParams.CREATE_FORM);
        }
        return true;
    }

    private void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation, LockStateLogger lockStateLogger) {
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC);
        if (xmlUuid == null) {
            TaxType taxType = TaxType.NDFL;
            String declarationName = "налоговой формы";
            String operationName = operation == FormDataEvent.MOVE_CREATED_TO_ACCEPTED ? "Принять" : operation.getTitle();
            logger.error("В %s отсутствуют данные (не был выполнен расчет). Операция \"%s\" не может быть выполнена", declarationName, operationName);
        } else {
            validateDeclaration(userInfo, declarationData, logger, isErrorFatal, operation, null, null, null, lockStateLogger);
        }
    }

    @Override
    public void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                    FormDataEvent operation, File xmlFile, String fileName, String xsdBlobDataId, LockStateLogger stateLogger) {
        if (xsdBlobDataId == null && declarationData != null) {
            LOG.info(String.format("Получение данных налоговой формы %s", declarationData.getId()));
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

            xsdBlobDataId = declarationTemplate.getXsdId();
        }
        if (xsdBlobDataId != null && !xsdBlobDataId.isEmpty()) {
            try {
                if (declarationData != null) {
                    LOG.info(String.format("Выполнение проверок XSD-файла налоговой формы %s", declarationData.getId()));
                }
                stateLogger.updateState(AsyncTaskState.CHECK_XSD);
                boolean valid = validateXMLService.validate(declarationData, userInfo, logger, isErrorFatal, xmlFile, fileName, xsdBlobDataId);
                if (!logger.containsLevel(LogLevel.ERROR) && !valid) {
                    logger.error(VALIDATION_ERR_MSG);
                }
            } catch (Exception e) {
                LOG.error(VALIDATION_ERR_MSG, e);
                logger.error(e);
            }
        }
    }

    private JasperPrint fillReport(InputStream xml, InputStream jasperTemplate, JRSwapFile jrSwapFile, Map<String, Object> params) {
        try {
            if (params == null) {
                params = new HashMap<String, Object>();
            }
            params.put(JRXPathQueryExecuterFactory.XML_INPUT_STREAM, xml);
            final JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(100, jrSwapFile);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    virtualizer.cleanup();
                }
            });
            virtualizer.setReadOnly(false);
            params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
            Connection connection = getReportConnection();
            return JasperFillManager.fillReport(jasperTemplate, params, connection);
        } catch (Exception e) {
            throw new ServiceException("Невозможно заполнить отчет", e);
        }
    }

    @Override
    public void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
        try {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT,
                    jasperPrint);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, data);
            exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET,
                    Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
                    Boolean.TRUE);
            exporter.setParameter(
                    JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                    Boolean.FALSE);
            exporter.setParameter(
                    JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
                    Boolean.FALSE);
            exporter.setParameter(
                    JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
                    Boolean.FALSE);

            exporter.exportReport();
            exporter.reset();
        } catch (Exception e) {
            throw new ServiceException(
                    "Невозможно экспортировать отчет в XLSX", e);
        }
    }

    @Override
    public void exportPDF(JasperPrint jasperPrint, OutputStream data) {
        try {
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, data);
            exporter.setParameter(JRPdfExporterParameter.CHARACTER_ENCODING, "Ansi");
            exporter.getPropertiesUtil().setProperty(JRPdfExporterParameter.PROPERTY_SIZE_PAGE_TO_CONTENT, "true");

            exporter.exportReport();
        } catch (Exception e) {
            throw new ServiceException("Невозможно экспортировать отчет в PDF", e);
        }
    }

    private Date getFormattedDate(String stringToDate) {
        if (stringToDate == null)
            return null;
        // Преобразуем строку вида "dd.mm.yyyy" в Date
        try {
            return sdf.get().parse(stringToDate);
        } catch (ParseException e) {
            throw new ServiceException("Невозможно получить дату обновления налоговой формы", e);
        }
    }

    private String saveJPBlobData(JasperPrint jasperPrint) throws IOException {
        File jasperPrintFile = null;
        try {
            jasperPrintFile = File.createTempFile("report", ".jasper");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(jasperPrintFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(jasperPrint);
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }

            //Архивирование перед сохранением в базу
            File zipOutFile = null;
            try {
                zipOutFile = File.createTempFile("report", ".zip");
                fileOutputStream = new FileOutputStream(zipOutFile);
                ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
                ZipEntry zipEntry = new ZipEntry("report.jasper");
                zos.putNextEntry(zipEntry);
                FileInputStream fi = new FileInputStream(jasperPrintFile);

                try {
                    IOUtils.copy(fi, zos);
                } finally {
                    IOUtils.closeQuietly(fi);
                    IOUtils.closeQuietly(zos);
                    IOUtils.closeQuietly(fileOutputStream);
                }

                return blobDataService.create(zipOutFile.getPath(), "");
            } finally {
                if (zipOutFile != null && !zipOutFile.delete()) {
                    LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                }
            }
        } finally {
            if (jasperPrintFile != null)
                jasperPrintFile.delete();
        }
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriod, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriod, kpp, oktmo, taxOrganCode, asnuId, fileName);
    }

    @Override
    public List<Long> getFormDataListInActualPeriodByTemplate(int declarationTemplateId, Date startDate) {
        return declarationDataDao.findDeclarationDataByFormTemplate(declarationTemplateId, startDate);
    }

    @Override
    public boolean existDeclaration(int declarationTypeId, int departmentId, List<LogEntry> logs) {
        List<Long> declarationIds = declarationDataDao.getDeclarationIds(declarationTypeId, departmentId);
        if (logs != null) {
            for (long declarationId : declarationIds) {
                DeclarationData declarationData = declarationDataDao.get(declarationId);
                ReportPeriod period = reportPeriodService.getReportPeriod(declarationData.getReportPeriodId());
                DepartmentReportPeriod drp = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId());

                StringBuilder taKPPString = new StringBuilder("");
                if (declarationData.getTaxOrganCode() != null) {
                    taKPPString.append(", налоговый орган: \"").append(declarationData.getTaxOrganCode()).append("\"");
                }
                if (declarationData.getKpp() != null) {
                    taKPPString.append(", КПП: \"").append(declarationData.getKpp()).append("\"");
                }
                logs.add(new LogEntry(LogLevel.ERROR, String.format(MSG_IS_EXIST_DECLARATION,
                        declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                        departmentService.getDepartment(departmentId).getName(),
                        period.getName() + " " + period.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s", sdf.get().format(drp.getCorrectionDate())) : "",
                        taKPPString.toString())));
            }
        }
        return !declarationIds.isEmpty();
    }

    @Override
    public String generateAsyncTaskKey(long declarationDataId, DeclarationDataReportType type) {
        if (type == null) {
            return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId;
        } else if (!type.isSubreport() || type.getSubreport().getDeclarationSubreportParams().isEmpty()) {
            return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId + "_" + type.getReportAlias().toUpperCase();
        } else {
            return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId + "_" + type.getReportAlias().toUpperCase() + "_" + UUID.randomUUID();
        }
    }

    @Override
    public String generateAsyncTaskKey(int declarationTypeId, int reportPeriodId, int departmentId) {
        return LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTypeId + "_" + reportPeriodId + "_" + departmentId;
    }

    @Override
    @Transactional
    public LockData lock(long declarationDataId, TAUserInfo userInfo) {
        LockData lockData = lockDataService.lock(generateAsyncTaskKey(declarationDataId, null), userInfo.getUser().getId(),
                getDeclarationFullName(declarationDataId, null));
        checkLock(lockData, userInfo.getUser());
        return lockData;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void unlock(final long declarationDataId, final TAUserInfo userInfo) {
        lockDataService.unlock(generateAsyncTaskKey(declarationDataId, null), userInfo.getUser().getId());
    }

    @Override
    public void checkLockedMe(Long declarationDataId, TAUserInfo userInfo) {
        checkLock(lockDataService.getLock(generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.XML_DEC)),
                userInfo.getUser());
    }

    private void checkLock(LockData lockData, TAUser user) {
        if (lockData != null && lockData.getUserId() != user.getId()) {
            TAUser lockUser = taUserService.getUser(lockData.getUserId());
            throw new ServiceException(String.format(LockDataService.LOCK_DATA, lockUser.getName(), lockUser.getId()));
        }
    }

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     */
    @Override
    public void deleteReport(long declarationDataId, TAUserInfo userInfo, boolean isCalc, TaskInterruptCause cause) {
        DeclarationDataReportType[] ddReportTypes = {DeclarationDataReportType.XML_DEC, DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC};
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                DeclarationData declarationData = declarationDataDao.get(declarationDataId);
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for (DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                    if (lock != null) {
                        asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                    }
                }
            } else if (!isCalc || !DeclarationDataReportType.XML_DEC.equals(ddReportType)) {
                LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                if (lock != null) {
                    asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                }
            }
        }
        reportService.deleteDec(declarationDataId);
    }

    /**
     * Список операции, по которым требуется удалить блокировку
     *
     * @param reportType
     * @return
     */
    private DeclarationDataReportType[] getCheckTaskList(AsyncTaskType reportType) {
        switch (reportType) {
            case XML_DEC:
                return new DeclarationDataReportType[]{DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC, new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, null)};
            case ACCEPT_DEC:
                return new DeclarationDataReportType[]{DeclarationDataReportType.CHECK_DEC};
            case UPDATE_TEMPLATE_DEC:
                return new DeclarationDataReportType[]{new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, null)};
            default:
                return null;
        }
    }

    @Override
    public boolean checkExistAsyncTask(long declarationDataId, AsyncTaskType reportType, Logger logger) {
        DeclarationDataReportType[] ddReportTypes = getCheckTaskList(reportType);
        if (ddReportTypes == null) return false;
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        boolean exist = false;
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for (DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    exist |= checkExistTasks(declarationDataId, ddReportType, logger);
                }
            } else {
                exist |= checkExistTasks(declarationDataId, ddReportType, logger);
            }
        }
        return exist;
    }

    private boolean checkExistTasks(long declarationDataId, DeclarationDataReportType ddReportType, Logger logger) {
        LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
        if (lock != null) {
            AsyncTaskData taskData = asyncTaskDao.getLightTaskData(lock.getTaskId());
            if (AsyncTaskState.IN_QUEUE == taskData.getState()) {
                logger.info(AsyncTask.CANCEL_TASK_NOT_PROGRESS,
                        SDF_DD_MM_YYYY_HH_MM_SS.get().format(lock.getDateLock()),
                        taUserService.getUser(lock.getUserId()).getName(),
                        taskData.getDescription());
            } else {
                logger.info(AsyncTask.CANCEL_TASK_IN_PROGRESS,
                        SDF_DD_MM_YYYY_HH_MM_SS.get().format(lock.getDateLock()),
                        taUserService.getUser(lock.getUserId()).getName(),
                        taskData.getDescription());
            }
            return true;
        }
        return false;
    }

    @Override
    public void interruptAsyncTask(long declarationDataId, TAUserInfo userInfo, AsyncTaskType reportType, TaskInterruptCause cause) {
        DeclarationDataReportType[] ddReportTypes = getCheckTaskList(reportType);
        if (ddReportTypes == null) return;
        DeclarationData declarationData = get(declarationDataId, userInfo);
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            List<String> taskKeyList = new ArrayList<String>();
            if (ddReportType.isSubreport()) {
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for (DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    taskKeyList.add(generateAsyncTaskKey(declarationDataId, ddReportType));
                }
                reportService.deleteDec(Arrays.asList(declarationDataId), Arrays.asList(ddReportType));
            } else {
                taskKeyList.add(generateAsyncTaskKey(declarationDataId, ddReportType));
            }
            for (String key : taskKeyList) {
                LockData lock = lockDataService.getLock(key);
                if (lock != null) {
                    asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                }
            }
        }
    }

    @Override
    @Transactional
    public void cleanBlobs(Collection<Long> ids, List<DeclarationDataReportType> reportTypes) {
        if (ids.isEmpty()) {
            return;
        }
        reportService.deleteDec(ids, reportTypes);
    }

    @Override
    public void findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate, Logger logger) {
        List<Integer> ddIds = declarationDataDao.findDDIdsByRangeInReportPeriod(decTemplateId,
                startDate, endDate != null ? endDate : MAX_DATE);
        for (Integer id : ddIds) {
            DeclarationData dd = declarationDataDao.get(id);
            ReportPeriod rp = reportPeriodService.getReportPeriod(dd.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.get(dd.getDepartmentReportPeriodId());
            DeclarationTemplate dt = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logger.error(DD_NOT_IN_RANGE,
                    rp.getName(),
                    rp.getTaxPeriod().getYear(),
                    drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                            sdf.get().format(drp.getCorrectionDate())) : "",
                    dt.getName(),
                    dd.getState().getTitle());
        }
    }

    @Override
    public String getDeclarationFullName(int declarationTypeId, int departmentReportPeriodId, AsyncTaskType taskType) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));
        switch (taskType) {
            case CREATE_REPORTS_DEC:
            case CREATE_FORMS_DEC:
                return String.format(taskType.getDescription(),
                        declarationTemplate.getType().getName(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName(),
                        departmentReportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(departmentReportPeriod.getCorrectionDate())
                                : "",
                        departmentService.getDepartment(departmentReportPeriod.getDepartmentId()).getName()
                );
            default:
                throw new IllegalArgumentException("Unknow async type");
        }
    }

    @Override
    public String getDeclarationFullName(long declarationId, DeclarationDataReportType ddReportType, String... args) {
        DeclarationData declaration = declarationDataDao.get(declarationId);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        if (ddReportType == null)
            return String.format(DescriptionTemplate.DECLARATION.getText(),
                    "Налоговая форма",
                    reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    reportPeriod.getCorrectionDate() != null
                            ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                            : "",
                    department.getName(),
                    declarationTemplate.getType().getName(),
                    ", № " + declaration.getId(),
                    declaration.getTaxOrganCode() != null
                            ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                            : "",
                    declaration.getKpp() != null
                            ? ", КПП: \"" + declaration.getKpp() + "\""
                            : "",
                    declaration.getOktmo() != null
                            ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                            : "");

        switch (ddReportType.getReportType()) {
            case EXCEL_DEC:
            case PDF_DEC:
            case XML_DEC:
            case CHECK_DEC:
            case ACCEPT_DEC:
                return String.format(DescriptionTemplate.DECLARATION.getText(),
                        "Налоговая форма",
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        ", № " + declaration.getId(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                                : "");
            case SPECIFIC_REPORT_DEC:
                return String.format(DescriptionTemplate.DECLARATION.getText(),
                        "Налоговая форма",
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        ", № " + declaration.getId(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                                : "");
            case DELETE_DEC:
                return String.format(DescriptionTemplate.DECLARATION.getText(),
                        "Налоговая форма",
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        ", № " + declaration.getId(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                                : "");
            case CREATE_REPORTS_DEC:
            case CREATE_FORMS_DEC:
                return String.format(ddReportType.getReportType().getDescription(),
                        declarationTemplate.getType().getName(),
                        reportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + reportPeriod.getReportPeriod().getName(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        departmentService.getDepartment(reportPeriod.getDepartmentId()).getName()
                );
            default:
                return String.format(DescriptionTemplate.DECLARATION.getText(),
                        "Налоговая форма",
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        ", № " + declaration.getId(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                                : "");
        }
    }

    @Override
    public Long getTaskLimit(AsyncTaskType reportType) {
        return asyncTaskDao.getTaskTypeData(reportType.getAsyncTaskTypeId()).getTaskLimit();
    }

    @Override
    public Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType reportType) {
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        switch (reportType.getReportType()) {
            case PDF_DEC:
            case EXCEL_DEC:
            case ACCEPT_DEC:
            case CHECK_DEC:
                if (declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.REPORTS)) {
                    if (declarationTemplate.getType().getId() == DeclarationType.NDFL_6) {
                        // для 6НДФЛ
                        return (long) ndflPersonDao.get6NdflPersonCount(declarationDataId);
                    } else {
                        return (long) ndflPersonDao.getNdflPersonReferencesCount(declarationDataId);
                    }
                } else {
                    return (long) ndflPersonDao.getNdflPersonCount(declarationDataId);
                }
            case XML_DEC:
                if (declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.REPORTS)) {
                    return (long) ndflPersonDao.getNdflPersonReferencesCount(declarationDataId);
                } else if (declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED)) {
                    Logger logger = new Logger();
                    Long personCount = 0L;
                    try {
                        List<Relation> relationList = sourceService.getDeclarationSourcesInfo(declarationData, true, false, null, userInfo, logger);
                        for (Relation relation : relationList) {
                            if (relation.getDeclarationDataId() != null && State.ACCEPTED.equals(relation.getDeclarationState())) {
                                personCount += ndflPersonDao.getNdflPersonCount(relation.getDeclarationDataId());
                            }
                        }
                    } catch (ServiceException e) {
                        return 0L;
                    }
                    return personCount;
                } else {
                    return (long) ndflPersonDao.getNdflPersonCount(declarationDataId);
                }
            case SPECIFIC_REPORT_DEC:
                Map<String, Object> exchangeParams = new HashMap<String, Object>();
                ScriptTaskComplexityHolder taskComplexityHolder = new ScriptTaskComplexityHolder();
                taskComplexityHolder.setAlias(reportType.getReportAlias());
                taskComplexityHolder.setValue(0L);
                exchangeParams.put("taskComplexityHolder", taskComplexityHolder);
                declarationDataScriptingService.executeScript(userInfo, get(declarationDataId, userInfo), FormDataEvent.CALCULATE_TASK_COMPLEXITY, new Logger(), exchangeParams);
                return taskComplexityHolder.getValue();
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getReportAlias());
        }
    }

    private void checkSources(DeclarationData dd, Logger logger, TAUserInfo userInfo) {
        boolean consolidationOk = true;
        //Проверка на неактуальные консолидированные данные  3А
        if (!sourceService.isDDConsolidationTopical(dd.getId())) {
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(dd.getDeclarationTemplateId());
            boolean isReports = DeclarationFormKind.REPORTS.equals(declarationTemplate.getDeclarationFormKind());
            logger.error(CALCULATION_NOT_TOPICAL + (isReports ? "" : CALCULATION_NOT_TOPICAL_SUFFIX));
            consolidationOk = false;
        } else {
            //Проверка того, что консолидация вообще когда то выполнялась для всех источников
            List<Relation> relations = sourceService.getDeclarationSourcesInfo(dd, true, false, null, userInfo, logger);
            for (Relation relation : relations) {
                if (!relation.isCreated()) {
                    consolidationOk = false;
                    logger.warn(
                            NOT_EXIST_SOURCE_DECLARATION_WARNING,
                            relation.getFullDepartmentName(),
                            relation.getDeclarationTemplate().getName(),
                            relation.getDeclarationTemplate().getDeclarationFormKind().getTitle(),
                            relation.getPeriodName(),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    sdf.get().format(relation.getCorrectionDate())) : "");
                } else if (!sourceService.isDeclarationSourceConsolidated(dd.getId(), relation.getDeclarationDataId())) {
                    consolidationOk = false;
                    logger.warn(NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING,
                            relation.getFullDepartmentName(),
                            relation.getDeclarationTemplate().getName(),
                            relation.getDeclarationTemplate().getDeclarationFormKind().getTitle(),
                            relation.getPeriodName(),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    sdf.get().format(relation.getCorrectionDate())) : "",
                            relation.getDeclarationState().getTitle());
                }
            }

            if (!relations.isEmpty() && consolidationOk) {
                logger.info("Консолидация выполнена из всех форм-источников.");
            }
        }
    }

    @Override
    public boolean isVisiblePDF(DeclarationData declarationData, TAUserInfo userInfo) {
        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        Map<String, Object> params = new HashMap<String, Object>();
        exchangeParams.put("params", params);
        Logger logger = new Logger();
        if (declarationDataScriptingService.executeScript(userInfo,
                declarationData, FormDataEvent.CHECK_VISIBILITY_PDF, logger, exchangeParams)) {
            if (logger.containsLevel(LogLevel.ERROR)) {
                return false;
            }
            if (params.containsKey("isVisiblePDF") && params.get("isVisiblePDF") instanceof Boolean) {
                return (Boolean) (params.get("isVisiblePDF"));
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private String getFileName(String filename) {
        int dotPos = filename.lastIndexOf('.');
        if (dotPos < 0) {
            return filename;
        }
        return filename.substring(0, dotPos);
    }

    @Override
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, long declarationDataId, InputStream inputStream,
                                      String fileName, FormDataEvent formDataEvent, LockStateLogger stateLogger, File dataFile,
                                      AttachFileType fileType, LocalDateTime createDateFile) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.CALCULATE);
        try {
            DeclarationData declarationData = get(declarationDataId, userInfo);

            String fileUuid;
            if (AttachFileType.TYPE_1.equals(fileType)) {
                //Архивирование перед сохраннеием в базу
                File zipOutFile = null;
                try {
                    zipOutFile = File.createTempFile("xml", ".zip");
                    FileOutputStream fileOutputStream = new FileOutputStream(zipOutFile);
                    ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    FileInputStream fi = new FileInputStream(dataFile);

                    try {
                        IOUtils.copy(fi, zos);
                    } finally {
                        IOUtils.closeQuietly(fi);
                        IOUtils.closeQuietly(zos);
                        IOUtils.closeQuietly(fileOutputStream);
                    }

                    LOG.info(String.format("Сохранение XML-файла в базе данных для налоговой формы %s", declarationData.getId()));
                    if (stateLogger != null) {
                        stateLogger.updateState(AsyncTaskState.SAVING_XML);
                    }

                    createDateFile = createDateFile == null ? new LocalDateTime() : createDateFile;
                    fileUuid = blobDataService.create(zipOutFile, getFileName(fileName) + ".zip", createDateFile);

                    reportService.deleteDec(declarationData.getId());
                    reportService.createDec(declarationData.getId(), fileUuid, DeclarationDataReportType.XML_DEC);
                } finally {
                    if (zipOutFile != null && !zipOutFile.delete()) {
                        LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                    }
                }
            } else {
                fileUuid = blobDataService.create(dataFile, fileName, new LocalDateTime());
            }

            TAUser user = userService.getSystemUserInfo().getUser();
            RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId());
            Long fileTypeId = provider.getUniqueRecordIds(new Date(), "code = " + fileType.getId() + "").get(0);

            DeclarationDataFile declarationDataFile = new DeclarationDataFile();
            declarationDataFile.setDeclarationDataId(declarationData.getId());
            declarationDataFile.setUuid(fileUuid);
            declarationDataFile.setUserName(user.getName());
            declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(user.getDepartmentId()));
            declarationDataFile.setFileTypeId(fileTypeId);
            declarationDataFileDao.saveFile(declarationDataFile);

            InputStream dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
            try {
                Map<String, Object> additionalParameters = new HashMap<String, Object>();
                additionalParameters.put("ImportInputStream", dataFileInputStream);
                additionalParameters.put("UploadFileName", fileName);
                additionalParameters.put("dataFile", dataFile);
                if (!declarationDataScriptingService.executeScript(userInfo, declarationData, formDataEvent, logger, additionalParameters)) {
                    throw new ServiceException("Импорт данных не предусмотрен");
                }
                logBusinessService.add(null, declarationDataId, userInfo, formDataEvent, null);
                String note = "Загрузка данных из файла \"" + fileName + "\" в налоговую форму";
                auditService.add(formDataEvent, userInfo, declarationData, note, null);
            } finally {
                IOUtils.closeQuietly(dataFileInputStream);
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceException("При выполнении загрузки произошли ошибки");
            }
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<DeclarationDataFile> getFiles(long formDataId) {
        return declarationDataFileDao.getFiles(formDataId);
    }

    @Override
    public String getNote(long declarationDataId) {
        return declarationDataDao.getNote(declarationDataId);
    }

    @Override
    public void saveFilesComments(long declarationDataId, String note, List<DeclarationDataFile> files) {
        declarationDataDao.updateNote(declarationDataId, note);
        declarationDataFileDao.saveFiles(declarationDataId, files);
    }

    /**
     * Получить соединение для передачи в отчет, вынесено в отдельный метод для более удобного тестирования
     *
     * @return соединение, после завершения работы в вызывающем коде необходимо закрыть соединение
     */
    @Override
    public Connection getReportConnection() {
        try {
            return bdUtils.getConnection();
        } catch (CannotGetJdbcConnectionException e) {
            throw new ServiceException("Ошибка при попытке получить соединение с БД!", e);
        }
    }


    @Override
    public JasperPrint createJasperReport(InputStream xmlData, InputStream jrxmlTemplate, Map<String, Object> parameters) {
        return createJasperReport(xmlData, jrxmlTemplate, parameters, getReportConnection());
    }

    @Override
    public JasperPrint createJasperReport(InputStream xmlData, InputStream jrxmlTemplate, Map<String, Object> parameters, Connection connection) {
        if (xmlData != null) {
            parameters.put(JRXPathQueryExecuterFactory.XML_INPUT_STREAM, xmlData);
        }
        ByteArrayInputStream inputStream = compileReport(jrxmlTemplate);

        try {
            return JasperFillManager.fillReport(inputStream, parameters, connection);
        } catch (JRException e) {
            throw new ServiceException("Ошибка при вычислении отчета!", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createForms(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        Map<Long, Map<String, Object>> formMap = new HashMap<Long, Map<String, Object>>();
        additionalParameters.put("formMap", formMap);
        Map<String, Object> scriptParams = new HashMap<String, Object>();
        additionalParameters.put("scriptParams", scriptParams);
        DeclarationData declarationDataTemp = new DeclarationData();
        declarationDataTemp.setDeclarationTemplateId(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));
        declarationDataTemp.setDepartmentReportPeriodId(departmentReportPeriod.getId());
        declarationDataScriptingService.executeScript(userInfo, declarationDataTemp, FormDataEvent.CREATE_FORMS, logger, additionalParameters);

        int success = 0;
        int pairKppOktomoTotal = (Integer) scriptParams.get("pairKppOktmoTotal");
        List<String> errorMsgList = new ArrayList<String>();
        for (Map.Entry<Long, Map<String, Object>> entry : formMap.entrySet()) {
            Logger scriptLogger = new Logger();
            boolean createForm = true;
            try {
                createForm = calculateDeclaration(scriptLogger, entry.getKey(), userInfo, new Date(), entry.getValue(), stateLogger);
            } catch (Exception e) {
                createForm = false;
                if (e.getMessage() != null) {
                    scriptLogger.warn(e.getMessage());
                }
            } finally {
                if (!createForm) {
                    declarationDataDao.delete(entry.getKey());
                } else {
                    success++;
                    DeclarationData declaration = declarationDataDao.get(entry.getKey());
                    auditService.add(FormDataEvent.CREATE, userInfo, declaration, "Налоговая форма создана", null);
                    String message = getDeclarationFullName(entry.getKey(), null);
                    logger.info("Успешно выполнено создание " + message.replace("Налоговая форма", "налоговой формы"));
                }
                logger.getEntries().addAll(scriptLogger.getEntries());
            }
        }
        logger.info("Количество успешно созданных форм: %d. Не удалось создать форм: %d.", success, pairKppOktomoTotal - success);
        if (!errorMsgList.isEmpty()) {
            logger.warn("Не удалось создать формы со следующими параметрами:");
            for (String errorMsg : errorMsgList) {
                logger.warn(errorMsg);
            }
        }
    }

    @Override
    public String createReports(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger) {
        DeclarationData declarationDataTemp = new DeclarationData();
        declarationDataTemp.setDeclarationTemplateId(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));
        declarationDataTemp.setDepartmentReportPeriodId(departmentReportPeriod.getId());

        File reportFile = null;
        try {
            reportFile = File.createTempFile("reports", ".dat");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile));
            Map<String, Object> scriptParams = new HashMap<String, Object>();
            try {
                Map<String, Object> additionalParameters = new HashMap<String, Object>();
                additionalParameters.put("scriptParams", scriptParams);
                additionalParameters.put("outputStream", outputStream);
                declarationDataScriptingService.executeScript(userInfo, declarationDataTemp, FormDataEvent.CREATE_REPORTS, logger, additionalParameters);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceException("Обнаружены фатальные ошибки!");
            }
            String fileName = null;
            if (scriptParams.containsKey("fileName") && scriptParams.get("fileName") != null) {
                fileName = scriptParams.get("fileName").toString();
            }
            if (fileName == null || fileName.isEmpty()) {
                fileName = "reports";
            }
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
            return blobDataService.create(reportFile.getPath(), fileName);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (reportFile != null)
                reportFile.delete();
        }
    }

    @Override
    public void changeDocState(Logger logger, TAUserInfo userInfo, long declarationDataId, Long docStateId) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.CHANGE_STATUS_ED);
        DeclarationData declarationData = get(declarationDataId, userInfo);
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("docStateId", docStateId);
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CHANGE_STATUS_ED, logger, additionalParameters);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
        }
        declarationDataDao.setDocStateId(declarationDataId, docStateId);
        String note;
        if (docStateId != null) {
            RefBookDataProvider stateEDProvider = rbFactory.getDataProvider(RefBook.Id.DOC_STATE.getId());
            note = "Состояние ЭД установлено на \"" + stateEDProvider.getRecordData(docStateId).get("NAME").getStringValue() + "\"";
        } else {
            note = "Состояние ЭД удалено";
        }
        logBusinessService.add(null, declarationDataId, userInfo, FormDataEvent.CHANGE_STATUS_ED, note);
    }

    @Override
    public boolean existDeclarationData(long declarationDataId) {
        return declarationDataDao.existDeclarationData(declarationDataId);
    }

    private boolean preCreateReports(Logger logger, TAUserInfo userInfo, DeclarationData declarationData) {
        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        exchangeParams.put("paramMap", paramMap);

        declarationDataScriptingService.executeScript(userInfo,
                declarationData, FormDataEvent.PRE_CREATE_REPORTS, logger, exchangeParams);
        return (Boolean) paramMap.get("successfullPreCreate");
    }

    @Override
    public Map<DeclarationDataReportType, LockData> getLockTaskType(long declarationDataId) {
        Map<DeclarationDataReportType, LockData> result = new HashMap<DeclarationDataReportType, LockData>();
        for (DeclarationDataReportType reportType : reportTypes) {
            LockData lockData = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, reportType));
            if (lockData != null) {
                result.put(reportType, lockData);
            }
        }
        return result;
    }

    @Override
    public CreateDeclarationReportResult createReports(TAUserInfo userInfo, Integer declarationTypeId, Integer departmentId, Integer periodId) {
        // логика взята из CreateFormsDeclarationHandler
        Logger logger = new Logger();
        CreateDeclarationReportResult result = new CreateDeclarationReportResult();
        final AsyncTaskType reportType = AsyncTaskType.CREATE_FORMS_DEC;

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService
                .getLast(departmentId, periodId);
        if (departmentReportPeriod == null) {
            throw new ServiceException("Не удалось определить налоговый период.");
        }
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("declarationTypeId", declarationTypeId);
        params.put("departmentReportPeriodId", departmentReportPeriod.getId());

        String keyTask = generateAsyncTaskKey(declarationTypeId, periodId, departmentId);
        Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, false, logger);
        if (restartStatus != null && restartStatus.getFirst()) {
            // TODO: Реализовать логику случая, когда задача уже запущена.
            result.setStatus(CreateAsyncTaskStatus.EXIST);
            result.setRestartMsg(restartStatus.getSecond());
        } else if (restartStatus != null && !restartStatus.getFirst()) {
            result.setStatus(CreateAsyncTaskStatus.EXIST);
        } else {
            result.setStatus(CreateAsyncTaskStatus.CREATE);
            asyncManager.executeTask(keyTask, reportType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_NS', 'N_ROLE_CONTROL_UNP')")
    public AcceptDeclarationResult createAcceptDeclarationTask(TAUserInfo userInfo, final long declarationDataId, final boolean force, final boolean cancelTask) {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.ACCEPT_DEC;
        final AcceptDeclarationResult result = new AcceptDeclarationResult();
        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
            return result;
        }
        Logger logger = new Logger();
        final TaxType taxType = TaxType.NDFL;
        String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
        if (uuidXml != null) {
            DeclarationData declarationData = get(declarationDataId, userInfo);
            if (!declarationData.getState().equals(State.ACCEPTED)) {
                String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
                Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, force, logger);
                if (restartStatus != null && restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.LOCKED);
                    result.setRestartMsg(restartStatus.getSecond());
                } else if (restartStatus != null && !restartStatus.getFirst()) {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                } else {
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("declarationDataId", declarationDataId);
                    asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, cancelTask, new AbstractStartupAsyncTaskHandler() {
                        @Override
                        public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                            return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                        }

                        @Override
                        public void postCheckProcessing() {
                            result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                        }

                        @Override
                        public boolean checkExistTasks(AsyncTaskType reportType, TAUserInfo userInfo, Logger logger) {
                            return checkExistAsyncTask(declarationDataId, reportType, logger);
                        }

                        @Override
                        public void interruptTasks(AsyncTaskType reportType, TAUserInfo userInfo) {
                            interruptAsyncTask(declarationDataId, userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                        }
                    });
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.EXIST);
            }
        } else {
            result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    private void sendNotification(String msg, String uuid, Integer userId, NotificationType notificationType, String reportId) {
        if (msg != null && !msg.isEmpty()) {
            List<Notification> notifications = new ArrayList<Notification>();
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setCreateDate(new LocalDateTime());
            notification.setText(msg);
            notification.setLogId(uuid);
            notification.setReportId(reportId);
            notification.setNotificationType(notificationType);
            notifications.add(notification);
            notificationService.saveList(notifications);
        }
    }


    @Override
    public ActionResult downloadReports(TAUserInfo userInfo, List<Long> declarationDataIdList) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        List<DeclarationData> declarationDataList = declarationDataDao.get(declarationDataIdList);
        List<DeclarationData> succesfullPreCreateDeclarationDataList = new LinkedList<DeclarationData>();
        List<DeclarationData> unsuccesfullPreCreateDeclarationDataList = new LinkedList<DeclarationData>();
        for (DeclarationData declarationData : declarationDataList) {
            if (preCreateReports(logger, userInfo, declarationData)) {
                succesfullPreCreateDeclarationDataList.add(declarationData);
            } else {
                unsuccesfullPreCreateDeclarationDataList.add(declarationData);
            }
        }
        if (succesfullPreCreateDeclarationDataList.isEmpty()) {
            logger.error("Отчетность не выгружена. В выбранных отчетных формах некорректное количество файлов " +
                    "формата xml, категория которых равна \"Исходящий в ФНС\", должно быть файлов: один");
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        }
        for (DeclarationData declarationData : unsuccesfullPreCreateDeclarationDataList) {
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId());
            Department department = departmentService.getDepartment(departmentReportPeriod.getId());
            String strCorrPeriod = "";
            if (departmentReportPeriod.getCorrectionDate() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
            }
            String msg = String.format("Отчетность %s за период %s, подразделение: \"%s\" не выгружена. В налоговой" +
                            "форме № %d некорректное количество файлов формата xml, категория которых равна \"Исходящий в ФНС\"," +
                            "должно быть файлов: один",
                    declarationTemplate.getName(),
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                    department.getShortName(),
                    declarationData.getId());
            logger.warn(msg);
        }


        String reportId = createReports(succesfullPreCreateDeclarationDataList, userInfo);

        sendNotification("Подготовлена к выгрузке отчетность", logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.REF_BOOK_REPORT, reportId);

        return result;
    }

    private String createReports(List<DeclarationData> declarationDataList, TAUserInfo userInfo) {
        File reportFile = null;
        ZipArchiveOutputStream zos = null;
        try {
            reportFile = File.createTempFile("reports", ".dat");
            zos = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(reportFile)));
            Map<Integer, Department> departmentMap = new HashMap<Integer, Department>();
            Map<Integer, DepartmentReportPeriod> departmentReportPeriodMap = new HashMap<Integer, DepartmentReportPeriod>();
            for (DeclarationData declarationData : declarationDataList) {
                Department department = departmentMap.get(declarationData.getDepartmentId());
                DepartmentReportPeriod drp = departmentReportPeriodMap.get(declarationData.getDepartmentReportPeriodId());
                if (department == null) {
                    department = departmentService.getDepartment(declarationData.getDepartmentId());
                    departmentMap.put(department.getId(), department);
                }
                if (drp == null) {
                    drp = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId());
                    departmentReportPeriodMap.put(drp.getId(), drp);
                }
                String departmentName = department.getShortName();
                String strCorrPeriod = "";
                if (drp.getCorrectionDate() != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(drp.getCorrectionDate());
                }
                ZipArchiveEntry ze = new ZipArchiveEntry("Отчетность подразделения: " + departmentName + "/"
                        + "Период: " + drp.getReportPeriod().getTaxPeriod().getYear()
                        + ", " + drp.getReportPeriod().getName() + strCorrPeriod + "/" + declarationData.getTaxOrganCode()
                        + "/" + declarationData.getFileName() + ".xml");
                zos.putArchiveEntry(ze);
                ZipInputStream zipXml = null;
                try {
                    zipXml = new ZipInputStream(getXmlDataAsStream(declarationData.getId(), userInfo));
                    zipXml.getNextEntry();
                    IOUtils.copy(zipXml, zos);
                    zos.closeArchiveEntry();
                } catch (IOException e) {
                    throw new ServiceException(e.getLocalizedMessage(), e);
                } finally {
                    IOUtils.closeQuietly(zipXml);
                }
            }
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (zos != null) {
                IOUtils.closeQuietly(zos);
            }
        }
        Date creationDate = new Date();
        String fileName = "Выгрузка отчетности " + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(creationDate) + ".zip";
        return blobDataService.create(reportFile, fileName, new LocalDateTime(creationDate));
    }
}
