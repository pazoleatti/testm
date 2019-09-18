package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.*;
import com.aplana.sbrf.taxaccounting.model.dto.Declaration2NdflFLDTO;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.filter.Declaration2NdflFLFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageType;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.component.MoveToCreateFacade;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate.DateEditor;
import com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate.DateEditorFactory;
import com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate.EditableDateField;
import com.aplana.sbrf.taxaccounting.service.impl.transport.edo.SendToEdoResult;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.aplana.sbrf.taxaccounting.service.util.NdflRowEditChangelogBuilder;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import com.aplana.sbrf.taxaccounting.utils.ZipUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import static com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants.RNU_NDFL_PERSON_ALL_DB;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Сервис для работы с декларациями
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
    private static final String CALCULATION_NOT_TOPICAL = "Налоговая форма содержит неактуальные консолидированные данные  " +
            "(расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена " +
            "консолидация).";
    private static final String CALCULATION_NOT_TOPICAL_SUFFIX = " Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\"";
    private static final String ACCESS_ERR_MSG_FMT = "Нет прав на доступ к налоговой форме. Проверьте назначение формы РНУ НДФЛ (первичная) для подразделения «%s» в «Назначении налоговых форм»%s.";
    private static final String DD_NOT_IN_RANGE = "Найдена форма: \"%s\", \"%d\", \"%s\", \"%s\", состояние - \"%s\"";
    private static final String TAG_FILE = "Файл";
    private static final String TAG_DOCUMENT = "Документ";
    private static final String ATTR_FILE_ID = "ИдФайл";
    private static final String ATTR_DOC_DATE = "ДатаДок";
    private static final String VALIDATION_ERR_MSG = "Обнаружены фатальные ошибки!";
    private static final String MSG_IS_EXIST_DECLARATION =
            "Существует экземпляр \"%s\" в подразделении \"%s\" в периоде \"%s\"%s%s для макета!";
    private static final String NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\" в статусе \"%s\"";
    private static final String STANDARD_DECLARATION_DESCRIPTION = "налоговой формы: Вид: \"%s\",  №: %d, Период: \"%s, %s%s\", Подразделение: \"%s\"";
    private static final String FAIL = "Не выполнена операция \"%s\" для %s.";

    private static final Date MAX_DATE;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(9999, Calendar.DECEMBER, 31);
        MAX_DATE = calendar.getTime();
        calendar.clear();
    }

    @Autowired
    private DeclarationDataDao declarationDataDao;
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
    private NdflPersonService ndflPersonService;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private TAUserService userService;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private DBUtils bdUtils;
    @Autowired
    private NdflPersonDao ndflPersonDao;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MoveToCreateFacade moveToCreateFacade;
    @Autowired
    private BasePermissionEvaluator permissionEvaluator;
    @Autowired
    private DepartmentReportPeriodFormatter departmentReportPeriodFormatter;
    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private DeclarationLocker declarationLocker;
    @Autowired
    private EdoMessageService edoMessageService;
    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private TransportMessageDao transportMessageDao;

    @Override
    public CreateResult<Long> create(TAUserInfo userInfo, CreateDeclarationDataAction action) {
        CreateResult<Long> result = new CreateResult<>();
        Logger logger = new Logger();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchLast(action.getDepartmentId(), action.getPeriodId());
        if (departmentReportPeriod != null) {
            int activeTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(action.getDeclarationTypeId().intValue(), action.getPeriodId());
            try {
                DeclarationData newDeclarationData = new DeclarationData();
                newDeclarationData.setDeclarationTemplateId(activeTemplateId);
                newDeclarationData.setAsnuId(action.getAsnuId());
                newDeclarationData.setManuallyCreated(true);
                newDeclarationData.setKnfType(action.getKnfType());
                if (action.getKppList() != null) {
                    newDeclarationData.setIncludedKpps(new HashSet<>(action.getKppList()));
                }
                create(newDeclarationData, departmentReportPeriod, logger, userInfo);
                result.setEntityId(newDeclarationData.getId());
            } catch (DaoException e) {
                throw new ServiceException(e.getMessage());
            }
            if (!logger.getEntries().isEmpty()) {
                result.setUuid(logEntryService.save(logger.getEntries()));
            }
            return result;
        } else {
            throw new ServiceException("Не удалось определить налоговый период.");
        }
    }

    @Override
    public ActionResult asyncCreateReportForms(CreateReportFormsAction action, TAUserInfo userInfo) {
        LOG.info(String.format("asyncCreateReportForms by %s. action: %s", userInfo, action));
        Logger logger = new Logger();
        ActionResult taskResult = new ActionResult();

        DeclarationData knf = findKnfForReport(action, logger);
        if (!logger.containsLevel(LogLevel.ERROR)) {
            ReportFormsCreationParams params = new ReportFormsCreationParams(action);
            params.setSourceKnfId(knf.getId());
            final Map<String, Object> taskParams = new HashMap<>();
            taskParams.put("declarationDataId", knf.getId());
            taskParams.put("declarationTypeId", action.getDeclarationTypeId());
            taskParams.put("departmentReportPeriodId", knf.getDepartmentReportPeriodId());
            taskParams.put("params", params);
            asyncManager.createTask(OperationType.getOperationByDeclarationTypeId(action.getDeclarationTypeId()),
                    userInfo, taskParams, logger);

        }
        taskResult.setUuid(logEntryService.save(logger.getEntries()));
        return taskResult;
    }

    @Override
    @Transactional
    public void createReportForms(ReportFormsCreationParams params, LockStateLogger stateLogger, Logger logger, TAUserInfo userInfo) {
        LOG.info(String.format("createReportForms by %s. params: %s", userInfo, params));
        DeclarationData sourceKnf = get(params.getSourceKnfId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(sourceKnf.getDepartmentReportPeriodId());
        DeclarationData declarationDataTemp = new DeclarationData();
        declarationDataTemp.setDeclarationTemplateId(declarationTemplateService.getActiveDeclarationTemplateId(params.getDeclarationTypeId(), departmentReportPeriod.getReportPeriod().getId()));
        declarationDataTemp.setDepartmentReportPeriodId(departmentReportPeriod.getId());

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("reportFormsCreationParams", params);
        declarationDataScriptingService.executeScript(userInfo, declarationDataTemp, FormDataEvent.CREATE_FORMS, logger, additionalParameters);
    }

    @Override
    public ActionResult asyncCreate2NdflFL(Create2NdflFLParams params, TAUserInfo userInfo) {
        LOG.info(String.format("asyncCreate2NdflFL by %s. action: %s", userInfo, params));
        Logger logger = new Logger();
        ActionResult taskResult = new ActionResult();
        Map<String, Object> taskParams = new HashMap<>();

        List<Long> declarationDataIds = declarationDataDao.findAllIdsFor2NdflFL(params.getReportPeriodId(), params.getPersonId(), params.getKppOktmoPairs());
        if (isNotEmpty(declarationDataIds)) {
            params.setDeclaration2Ndfl1Ids(declarationDataIds);
            taskParams.put("declarationDataIds", declarationDataIds);
            taskParams.put("declarationTypeId", params.getDeclarationTypeId());
            taskParams.put("reportPeriodId", params.getReportPeriodId());
            taskParams.put("params", params);
            asyncManager.createTask(OperationType.getOperationByDeclarationTypeId(params.getDeclarationTypeId()),
                    userInfo, taskParams, logger);
            taskResult.setUuid(logEntryService.save(logger.getEntries()));
        } else {
            ReportPeriod reportPeriod = reportPeriodService.fetchReportPeriod(params.getReportPeriodId());
            RegistryPerson person = refBookPersonDao.fetchPersonVersion(params.getPersonId());
            String message = String.format("Не выполнена операция: \"Создание отчетной формы 2-НДФЛ(ФЛ)\" Сотрудник: " +
                            "%s, Отчетный период: %s %s. Причина: В системе нет данных для формирования 2-НДФЛ(ФЛ)",
                    person.getFullName(),
                    reportPeriod.getTaxPeriod().getYear(),
                    reportPeriod.getName());
            logger.error(message);
            Notification notification = createNotification(message, logger);
            taskResult.setUuid(notification.getLogId());
        }
        return taskResult;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void create2NdflFL(Create2NdflFLParams params, TAUserInfo userInfo, Logger logger) {
        LOG.info(String.format("create2NdflFL by %s. params: %s", userInfo, params));
        DeclarationData declarationDataTemp = new DeclarationData();
        declarationDataTemp.setDeclarationTemplateId(
                declarationTemplateService.getActiveDeclarationTemplateId(params.getDeclarationTypeId(), params.getReportPeriodId()));

        Map<String, Object> additionalParameters = new HashMap<>();

        try(InputStream arialStream = DeclarationDataServiceImpl.class.getResourceAsStream("/arial/arial.ttf");
        InputStream arialBoldStream = DeclarationDataServiceImpl.class.getResourceAsStream("/arial/arialbd.ttf")) {
            params.setArialFont(arialStream);
            params.setArialBoldFont(arialBoldStream);
            additionalParameters.put("createParams", params);

            declarationDataScriptingService.executeScript(userInfo, declarationDataTemp, FormDataEvent.CREATE_FORMS, logger, additionalParameters);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }

        for (Map.Entry<Long, File> result : params.getCreatedReports().entrySet()) {
            reportService.attachReportToDeclaration(result.getKey(), blobDataService.create(result.getValue().getPath(), params.getCreatedReportsFileName().get(result.getKey())), DeclarationReportType.PDF_DEC);
        }
    }

    /**
     * Логика создания вынесена в отдельный метод, для решения проблем с транзакциями при вызове из других транзакционных методов
     */
    private void create(DeclarationData newDeclaration, DepartmentReportPeriod departmentReportPeriod, Logger logger, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.create by %s. declarationTemplateId: %s; departmentReportPeriod: %s; taxOrganCode: %s; taxOrganKpp: %s; oktmo: %s; asunId: %s; fileName: %s; note: %s; manuallyCreated: %s",
                userInfo, newDeclaration.getDeclarationTemplateId(), departmentReportPeriod, newDeclaration.getTaxOrganCode(), newDeclaration.getKpp(), newDeclaration.getOktmo(), newDeclaration.getAsnuId(), newDeclaration.getFileName(), newDeclaration.getNote(), newDeclaration.isManuallyCreated()));

        newDeclaration.setDepartmentReportPeriodId(departmentReportPeriod.getId());
        newDeclaration.setReportPeriodId(departmentReportPeriod.getReportPeriod().getId());
        newDeclaration.setDepartmentId(departmentReportPeriod.getDepartmentId());
        newDeclaration.setState(State.CREATED);

        Object[] lockKeyParts = {LockData.LockObjects.DECLARATION_CREATE.name(), newDeclaration.getDeclarationTemplateId(), departmentReportPeriod.getId(), newDeclaration.getAsnuId()};
        String lockKey = StringUtils.joinNotEmpty(lockKeyParts, "_");

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(newDeclaration.getDeclarationTemplateId());
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        String lockDescription = makeCreateDeclarationLockDescription(newDeclaration, declarationTemplate, departmentReportPeriod, department);

        LockData lockData = lockDataService.lock(lockKey, userInfo.getUser().getId(), lockDescription);

        //Если блокировка успешно установлена
        if (lockData == null) {
            try {
                canCreate(userInfo, newDeclaration.getDeclarationTemplateId(), departmentReportPeriod, newDeclaration.getAsnuId(), logger);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(("Налоговая форма не создана"), logEntryService.save(logger.getEntries()));
                }

                if (declarationTemplate.getDeclarationFormKind().getId() == DeclarationFormKind.CONSOLIDATED.getId() &&
                        newDeclaration.isManuallyCreated() && declarationDataDao.existDeclarationData(newDeclaration)) {
                    String strCorrPeriod = "";
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
                    }
                    String message = String.format("Налоговая форма с заданными параметрами: Период: \"%s\", Подразделение: \"%s\", " +
                                    " Вид налоговой формы: \"%s\", Тип КНФ: \"%s\" уже существует!",
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                            department.getName(), declarationTemplate.getDeclarationFormKind().getName(), newDeclaration.getKnfType().getName());
                    logger.error(message);
                }
                doCreate(newDeclaration, declarationTemplate, logger, userInfo, true);
            } finally {
                lockDataService.unlock(lockKey);
            }
        } else { // Не удалось установить блокировку
            String errorMessage;
            int blockerUserId = lockData.getUserId();
            if (blockerUserId == userInfo.getUser().getId()) {
                errorMessage = String.format("Данная форма заблокирована. Вами уже запущена операция \"%s\"", lockDescription);
            } else {
                TAUser user = userService.getUser(blockerUserId);
                errorMessage = String.format("Данная форма заблокирована. Пользователем %s (%s) уже запущена операция \"%s\"", user.getName(), user.getLogin(), lockDescription);
            }
            logger.error(errorMessage);
        }
    }

    private void doCreate(DeclarationData newDeclaration, DeclarationTemplate declarationTemplate, Logger logger, TAUserInfo userInfo, boolean writeAudit) {
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

        if (declarationTemplate.getDeclarationFormKind() == DeclarationFormKind.REPORTS && newDeclaration.getCorrectionNum() == null) {
            newDeclaration.setCorrectionNum(0);
        }
        if (declarationTemplate.getType().getId() == DeclarationType.NDFL_6) {
            if (newDeclaration.getNegativeIncome() == null) {
                newDeclaration.setNegativeIncome(new BigDecimal(0));
            }
            if (newDeclaration.getNegativeTax() == null) {
                newDeclaration.setNegativeTax(new BigDecimal(0));
            }
            if (newDeclaration.getNegativeSumsSign() == null) {
                newDeclaration.setNegativeSumsSign(NegativeSumsSign.FROM_CURRENT_FORM);
            }
        }
        newDeclaration.setCreatedBy(userInfo.getUser());
        declarationDataDao.create(newDeclaration);

        if (writeAudit) {
            logBusinessService.create(new LogBusiness().declarationDataId(newDeclaration.getId()).event(FormDataEvent.CREATE)
                    .logId(logger.getLogId()).logDate(newDeclaration.getCreatedDate()).user(userInfo.getUser()));
            auditService.add(FormDataEvent.CREATE, userInfo, newDeclaration, "Налоговая форма создана", null);
        }
    }

    // Генерация текста описания задачи "Создание налоговой формы"
    private String makeCreateDeclarationLockDescription(DeclarationData declarationData,
                                                        DeclarationTemplate declarationTemplate,
                                                        DepartmentReportPeriod departmentReportPeriod,
                                                        Department department) {
        String periodName = departmentReportPeriod.getReportPeriod().getName();
        int periodYear = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
        String correctionDate = departmentReportPeriod.getCorrectionDate() != null
                ? " с датой сдачи корректировки " + sdf.get().format(departmentReportPeriod.getCorrectionDate())
                : "";
        String period = "Период: \"" + periodName + " " + periodYear + correctionDate + "\"";

        String departmentStr = "Подразделение: \"" + department.getName() + "\"";
        String declarationKind = "Вид: " + declarationTemplate.getDeclarationFormKind().getName();
        String declarationType = "Тип: " + declarationTemplate.getType().getName();

        String asnu;
        Long asnuId = declarationData.getAsnuId();
        if (asnuId != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            String asnuName = asnuProvider.getRecordData(asnuId).get("NAME").getStringValue();
            asnu = "Наименование АСНУ: " + asnuName;
        } else {
            asnu = "";
        }

        String[] parts = {period, departmentStr, declarationKind, declarationType, asnu};

        return "Создание налоговой формы: " + StringUtils.joinNotEmpty(parts, ", ");
    }

    /**
     * Проверка возможности создания пользователем формы из макета
     * TODO: вынести в пермишены
     */
    private void canCreate(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod, Long asnuId, Logger logger) {
        // Для начала проверяем, что в данном подразделении вообще можно
        // работать с декларациями данного вида
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
        int declarationTypeId = declarationTemplate.getType().getId();
        if (declarationTypeId != DeclarationType.NDFL_2_FL) {
            if (!departmentReportPeriod.isActive()) {
                error("Выбранный период закрыт", logger);
            }
            ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();
            List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(departmentReportPeriod.getDepartmentId(),
                    TaxType.NDFL, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
            boolean found = false;
            for (DepartmentDeclarationType ddt : ddts) {
                if (ddt.getDeclarationTypeId() == declarationTypeId) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                error("Выбранный вид налоговой формы не назначен подразделению", logger);
            }
            // Создавать декларацию могут только контролёры УНП и контролёры
            // текущего уровня обособленного подразделения

            //Подразделение формы
            Department declDepartment = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

            TaxType taxType = TaxType.NDFL;

            // Выборка для доступа к экземплярам деклараций
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
            // Не используется логика из com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission.VIEW, т.к тут нет экземпляра декларации

            // Контролёр УНП может просматривать все декларации
            if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP)) {
                return;
            }

            // Контролёр НС
            if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_NS)) {
                List<Integer> departments = departmentService.findAllAvailableIds(userInfo.getUser());
                if (departments.contains(declDepartment.getId())) {
                    return;
                }
            }

            // Оператор (НДФЛ или Сборы)
            if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_OPER)) {
                if (asnuId != null && !checkUserAsnu(userInfo, asnuId)) {
                    throw new AccessDeniedException("Нет прав на доступ к форме");
                }

                List<Integer> executors = departmentService.findAllAvailableIds(userInfo.getUser());
                if (executors.contains(declDepartment.getId())) {
                    if (!declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED)) {
                        return;
                    }
                }
            }

            // Прочие
            String asnuMsgPart = "";
            if (asnuId != null) {
                RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
                String asnuName = asnuProvider.getRecordData(asnuId).get("NAME").getStringValue();
                asnuMsgPart = String.format(" и наличие доступа к АСНУ «%s»", asnuName);
            }
            error(String.format(
                    ACCESS_ERR_MSG_FMT,
                    declDepartment.getName(),
                    asnuMsgPart
            ), logger);
        }
    }

    /**
     * Выбросить исключение или записать в лог.
     *
     * @param msg    текст ошибки
     * @param logger логгер
     */
    private void error(String msg, Logger logger) {
        if (logger == null) {
            throw new AccessDeniedException(msg);
        } else {
            logger.error(msg);
        }
    }

    /**
     * Проверяет есть у пользователя права на АСНУ декларации.
     *
     * @param userInfo пользователь
     * @param asnuId   АСНУ НФ, для ПНФ значение должно быть задано, для остальных форм null
     */
    private boolean checkUserAsnu(TAUserInfo userInfo, Long asnuId) {
        if (userInfo.getUser().hasRole(TARole.N_ROLE_OPER_ALL)) {
            return true;
        }

        return userInfo.getUser().getAsnuIds().contains(asnuId);
    }

    @Override
    @Transactional
    public void createWithotChecks(DeclarationData newDeclaration, Logger logger, TAUserInfo userInfo, boolean writeAudit) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(newDeclaration.getDeclarationTemplateId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(newDeclaration.getDepartmentReportPeriodId());
        canCreate(userInfo, declarationTemplate.getId(), departmentReportPeriod, newDeclaration.getAsnuId(), logger);
        if (logger.containsLevel(LogLevel.ERROR)) {
            return;
        }
        doCreate(newDeclaration, declarationTemplate, logger, userInfo, writeAudit);
    }

    @Override
    @PreAuthorize("hasPermission(#targetIdAndLogger, 'com.aplana.sbrf.taxaccounting.permissions.logging.LoggerIdTransfer', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).IDENTIFY)")
    public void identify(TargetIdAndLogger targetIdAndLogger, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        LOG.info(String.format("DeclarationDataServiceImpl.identify by %s. docDate: %s; docDate: %s; exchangeParams: %s",
                userInfo, docDate, docDate, exchangeParams));
        DeclarationData declarationData = declarationDataDao.get(targetIdAndLogger.getId());

        if (exchangeParams == null) {
            exchangeParams = new HashMap<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("declarationData", declarationData);
        exchangeParams.put("calculateParams", params);

        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, targetIdAndLogger.getLogger(), exchangeParams);

        if (!targetIdAndLogger.getLogger().containsLevel(LogLevel.ERROR)) {
            logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.IDENTIFY, targetIdAndLogger.getLogger().getLogId(), "Идентификация прошла успешно", userInfo);
            auditService.add(FormDataEvent.CALCULATE, userInfo, declarationData, "Налоговая форма обновлена", null);
        } else {
            logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.IDENTIFY, targetIdAndLogger.getLogger().getLogId(), "Идентификация не пройдена", userInfo);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#targetIdAndLogger, 'com.aplana.sbrf.taxaccounting.permissions.logging.LoggerIdTransfer', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).CONSOLIDATE)")
    @Transactional
    public void consolidate(TargetIdAndLogger targetIdAndLogger, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        LOG.info(String.format("DeclarationDataServiceImpl.consolidate by %s. docDate: %s; exchangeParams: %s",
                userInfo, docDate, exchangeParams));
        final DeclarationData declarationData = declarationDataDao.get(targetIdAndLogger.getId());

        if (exchangeParams == null) {
            exchangeParams = new HashMap<>();
        }
        final Set<Long> unacceptedSources = new HashSet<>();
        exchangeParams.put("unacceptedSources", unacceptedSources);

        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, targetIdAndLogger.getLogger(), exchangeParams);

        if (targetIdAndLogger.getLogger().containsLevel(LogLevel.ERROR)) {
            // Если из скрипта пришёл список источников, требуется их сохранить
            if (isNotEmpty(unacceptedSources)) {
                transactionHelper.executeInNewTransaction(new TransactionLogic() {
                    @Override
                    public Object execute() {
                        sourceService.addDeclarationConsolidationInfo(declarationData.getId(), unacceptedSources);
                        return null;
                    }
                });
            }
            throw new ServiceException();
        }

        logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.CONSOLIDATE, targetIdAndLogger.getLogger().getLogId(), null, userInfo);
        auditService.add(FormDataEvent.CALCULATE, userInfo, declarationData, "Налоговая форма обновлена", null);
        declarationDataDao.updateLastDataModified(declarationData.getId());
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).CHECK)")
    public void check(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        LOG.info(String.format("Проверка данных налоговой формы %s", id));
        DeclarationData dd = declarationDataDao.get(id);
        Logger scriptLogger = new Logger();
        try {
            if (lockStateLogger != null) {
                lockStateLogger.updateState(AsyncTaskState.SOURCE_FORM_CHECK);
            }
            checkSources(dd, logger);
            if (lockStateLogger != null) {
                lockStateLogger.updateState(AsyncTaskState.FORM_CHECK);
            }
            Map<String, Object> exchangeParams = new HashMap<>();
            exchangeParams.put("operationType", OperationType.CHECK_DEC);
            declarationDataScriptingService.executeScript(userInfo, dd, FormDataEvent.CHECK, scriptLogger, exchangeParams);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            if (State.CREATED.equals(dd.getState())) {
                declarationDataDao.setStatus(id, State.PREPARED);
                logBusinessService.logFormEvent(id, FormDataEvent.MOVE_CREATED_TO_PREPARED, logger.getLogId(), "Проверка пройдена, состояние изменено на \"Подготовлена\"", userInfo);
            } else {
                logBusinessService.logFormEvent(id, FormDataEvent.MOVE_CREATED_TO_PREPARED, logger.getLogId(), "Проверка пройдена", userInfo);
            }
            logger.info("Проверка завершена, ошибок не обнаружено");
        } else {
            if (State.PREPARED.equals(dd.getState())) {
                declarationDataDao.setStatus(id, State.CREATED);
            }
            logBusinessService.logFormEvent(id, FormDataEvent.MOVE_CREATED_TO_PREPARED, logger.getLogId(), "Фатальные ошибки", userInfo);
        }
    }

    @Override
    @Transactional
    public ActionResult createIdentifyDeclarationDataTask(TAUserInfo userInfo, List<Long> declarationDataIds) {
        LOG.info(String.format("DeclarationDataServiceImpl.createIdentifyDeclarationDataTask by %s. declarationDataIds: %s",
                userInfo, declarationDataIds));
        final ActionResult result = new ActionResult();
        final Logger logger = new Logger();

        for (final Long declarationDataId : declarationDataIds) {
            try {
                if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                        new TargetIdAndLogger(declarationDataId, logger),
                        "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.IDENTIFY)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("declarationDataId", declarationDataId);
                    params.put("docDate", new Date());
                    asyncManager.createTask(OperationType.IDENTIFY_PERSON, userInfo, params, logger);
                } else {
                    makeNotificationForAccessDenied(logger);
                }
            } catch (Exception e) {
                makeNotificationForUnexpected(e, logger, "Идентификация ФЛ", declarationDataId);
                LOG.error(e.getMessage(), e);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Transactional
    @Override
    public ActionResult consolidateDeclarationDataList(final TAUserInfo userInfo, List<Long> declarationDataIds) {
        LOG.info(String.format("DeclarationDataServiceImpl.consolidateDeclarationDataList by %s. ddReportType: %s; declarationDataIds: %s; permission: %s",
                userInfo, OperationType.CONSOLIDATE, declarationDataIds, DeclarationDataPermission.CONSOLIDATE));
        final ActionResult result = new ActionResult();
        final Logger logger = new Logger();

        for (final Long declarationDataId : declarationDataIds) {
            if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                    new TargetIdAndLogger(declarationDataId, logger),
                    "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.CONSOLIDATE)) {
                Map<String, Object> params = new HashMap<>();
                params.put("declarationDataId", declarationDataId);
                params.put("docDate", new Date());
                asyncManager.createTask(OperationType.CONSOLIDATE, userInfo, params, logger);
            } else {
                makeNotificationForAccessDenied(logger);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public DeclarationResult fetchDeclarationData(TAUserInfo userInfo, long declarationDataId) {
        DeclarationResult result = new DeclarationResult();
        if (existDeclarationData(declarationDataId)) {
            result.setDeclarationDataExists(true);
            DeclarationData declaration = get(declarationDataId, userInfo);
            result.setId(declarationDataId);
            result.setDepartmentId(declaration.getDepartmentId());
            result.setDepartment(departmentService.getParentsHierarchy(declaration.getDepartmentId()));

            result.setState(declaration.getState().getTitle());
            result.setManuallyCreated(declaration.isManuallyCreated());
            result.setLastDataModifiedDate(declaration.getLastDataModifiedDate());
            result.setActualDataDate(new Date());
            result.setAdjustNegativeValues(declaration.isAdjustNegativeValues());
            result.setHasNdflPersons(ndflPersonDao.ndflPersonExistsByDeclarationId(declarationDataId));
            result.setCreationUserName(declaration.getCreatedBy().getName());
            result.setPersonId(declaration.getPersonId());
            if (declaration.getPersonId() != null) {
                RegistryPerson person = refBookPersonDao.fetchPersonVersion(declaration.getPersonId());
                result.setPerson(Joiner.on(" ").skipNulls().join(asList(person.getLastName(), person.getFirstName(), person.getMiddleName())));
            }
            result.setSignatory(declaration.getSignatory());

            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
            result.setDeclarationFormKind(declarationTemplate.getDeclarationFormKind().getTitle());
            result.setDeclarationType(declarationTemplate.getType().getId());
            result.setDeclarationTypeName(declarationTemplate.getType().getName());

            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
            result.setReportPeriodId(departmentReportPeriod.getReportPeriod().getId());
            result.setReportPeriod(departmentReportPeriod.getReportPeriod().getName());
            result.setReportPeriodYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
            result.setCalendarStartDate(departmentReportPeriod.getReportPeriod().getCalendarStartDate());
            result.setEndDate(departmentReportPeriod.getReportPeriod().getEndDate());
            result.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

            if (declaration.getAsnuId() != null) {
                RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
                result.setAsnuName(asnuProvider.getRecordData(declaration.getAsnuId()).get("NAME").getStringValue());
            }
            result.setKnfType(declaration.getKnfType());
            if (RefBookKnfType.BY_KPP.equals(result.getKnfType())) {
                result.setKppList(declarationDataDao.getDeclarationDataKppList(declaration.getId()));
            }

            result.setCreationDate(declaration.getCreatedDate());
            result.setKpp(declaration.getKpp());
            result.setOktmo(declaration.getOktmo());
            result.setTaxOrganCode(declaration.getTaxOrganCode());
            result.setCorrectionNum(declaration.getCorrectionNum());
            result.setNegativeIncome(declaration.getNegativeIncome());
            result.setNegativeTax(declaration.getNegativeTax());
            result.setNegativeSumsSign(declaration.getNegativeSumsSign());
            result.setTaxRefundReflectionMode(declaration.getTaxRefundReflectionMode());
            if (declaration.getDocStateId() != null) {
                RefBookDataProvider stateEDProvider = refBookFactory.getDataProvider(RefBook.Id.DOC_STATE.getId());
                result.setDocState(stateEDProvider.getRecordData(declaration.getDocStateId()).get("NAME").getStringValue());
            }
        }

        return result;
    }

    @Override
    public List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId) {
        return declarationDataDao.findAllDeclarationData(declarationTypeId, departmentId, reportPeriodId);
    }

    @Override
    @Transactional
    public ActionResult createCheckDeclarationDataTask(final TAUserInfo userInfo, List<Long> declarationDataIds) {
        LOG.info(String.format("DeclarationDataServiceImpl.createCheckDeclarationDataTask by %s. operationType: %s; declarationDataIds: %s; permission: %s",
                userInfo, OperationType.CHECK_DEC, declarationDataIds, DeclarationDataPermission.CHECK));
        final ActionResult result = new ActionResult();
        Logger logger = new Logger();
        for (final Long declarationDataId : declarationDataIds) {
            try {
                if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                        new TargetIdAndLogger(declarationDataId, logger),
                        "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.CHECK)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("declarationDataId", declarationDataId);
                    asyncManager.createTask(OperationType.CHECK_DEC, userInfo, params, logger);
                } else {
                    makeNotificationForAccessDenied(logger);
                }
            } catch (Exception e) {
                makeNotificationForUnexpected(e, logger, "Проверка формы", declarationDataId);
                LOG.error(e.getMessage(), e);
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
    @PreAuthorize("hasPermission(#dataFileComment.declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public DeclarationDataFileComment updateDeclarationFilesComments(DeclarationDataFileComment dataFileComment, TAUserInfo userInfo) {
        long declarationDataId = dataFileComment.getDeclarationDataId();

        DeclarationDataFileComment result = new DeclarationDataFileComment();
        if (!existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        Logger logger = new Logger();
        if (declarationLocker.lockExists(declarationDataId, OperationType.EDIT_FILE, null, userInfo)) {
            declarationDataDao.updateNote(declarationDataId, dataFileComment.getComment());
            declarationDataFileDao.createOrUpdateList(declarationDataId, dataFileComment.getDeclarationDataFiles());
            logger.info("Данные успешно сохранены.");
        } else {
            logger.error("Сохранение не выполнено, так как файлы и комментарии данного экземпляра налоговой формы не заблокированы текущим пользователем.");
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
            List<Relation> relationList = new ArrayList<>();
            relationList.addAll(sourceService.getDeclarationSourcesInfo(declarationDataId));
            relationList.addAll(sourceService.getDeclarationDestinationsInfo(declarationDataId));
            return relationList;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public PagingResult<DeclarationDataJournalItem> fetchDeclarations(TAUserInfo userInfo, DeclarationDataFilter filter, PagingParams pagingParams) {
        PagingResult<DeclarationDataJournalItem> page = new PagingResult<>();

        if (filter != null) {
            setUpDeclarationFilter(filter, userInfo);
            page = declarationDataDao.findPage(filter, pagingParams);
        }

        return page;
    }

    private void setUpDeclarationFilter(DeclarationDataFilter filter, TAUserInfo userInfo) {
        TAUser currentUser = userInfo.getUser();
        if (CollectionUtils.isEmpty(filter.getAsnuIds())) {
            //Контролерам доступны все АСНУ, поэтому фильтрации по АСНУ нет, поэтому список для них пустой
            //Операторам доступны только некоторые АСНУ. Если такие есть, добавить их в список. Если доступных АСНУ нет, то
            //список будет состоять из 1 элемента (-1), который не может быть id существующего АСНУ, чтобы не нашлась ни одна форма
            List<Long> asnuIds = new ArrayList<>();
            if (!currentUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP) && currentUser.hasRole(TARole.N_ROLE_OPER)) {
                List<RefBookAsnu> availableAsnuList = refBookAsnuService.fetchAvailableAsnu(userInfo);
                if (!availableAsnuList.isEmpty()) {
                    for (RefBookAsnu asnu : refBookAsnuService.fetchAvailableAsnu(userInfo)) {
                        asnuIds.add(asnu.getId());
                    }
                } else {
                    asnuIds.add(-1L);
                }
            }
            filter.setAsnuIds(asnuIds);
        }

        // Отбираем подразделения (и соответственно их формы), доступные пользователю в соотстветствии с его ролями
        List<Integer> availableDepartments = departmentService.findAllAvailableIds(currentUser);
        List<Integer> departmentIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(filter.getDepartmentIds())) {
            departmentIds = availableDepartments;
        } else {
            for (Integer departmentId : filter.getDepartmentIds()) {
                if (availableDepartments.contains(departmentId)) {
                    departmentIds.add(departmentId);
                }
            }
        }
        // Если доступных подразделений нет, то список будет состоять из 1 элемента (-1),
        // который не может быть id существующего подразделения, чтобы не нашлась ни одна форма
        if (departmentIds.isEmpty()) {
            departmentIds.add(-1);
        }
        filter.setDepartmentIds(departmentIds);

        if (CollectionUtils.isEmpty(filter.getFormKindIds())) {
            List<Long> availableDeclarationFormKindIds = new ArrayList<>();
            if (currentUser.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                availableDeclarationFormKindIds.addAll(asList(DeclarationFormKind.PRIMARY.getId(), DeclarationFormKind.CONSOLIDATED.getId()));
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
    }

    @Override
    @PreAuthorize("hasPermission(#user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission)._2NDFL_FL)")
    public PagingResult<Declaration2NdflFLDTO> findAll2NdflFL(Declaration2NdflFLFilter filter, PagingParams pagingParams, TAUser user) {
        if (!user.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_VIP_READER)) {
            filter.setVip(false);
        }
        return declarationDataDao.findAll2NdflFL(filter, pagingParams);
    }

    @Override
    public String createTaskToCreateSpecificReport(final long declarationDataId, String alias, Map<String, Object> reportParams,
                                                   final TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createTaskToCreateSpecificReport by %s. declarationDataId: %s",
                userInfo, declarationDataId));
        Logger logger = new Logger();

        reportService.deleteSubreport(declarationDataId, alias);
        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);
        params.put("alias", alias);
        params.put("viewParamValues", new LinkedHashMap<String, String>());
        if (reportParams != null) {
            params.put("subreportParamValues", reportParams);
        }

        asyncManager.createTask(OperationType.getOperationTypeBySubreport(alias), userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public String createTaskToCreateRnuNdflByAllPersonsReport(long declarationDataId, TAUserInfo userInfo,
                                                              NdflFilter searchFilter,
                                                              RnuNdflAllPersonsReportSelectedRows selectedRows) {
        LOG.info(String.format("DeclarationDataServiceImpl.createTaskToCreateRnuNdflByAllPersonsReport by %s. declarationDataId: %s",
                userInfo, declarationDataId));
        Logger logger = new Logger();

        String reportAlias = RNU_NDFL_PERSON_ALL_DB;
        reportService.deleteSubreport(declarationDataId, reportAlias);
        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);
        params.put("alias", reportAlias);
        params.put("viewParamValues", new LinkedHashMap<String, String>());
        if (searchFilter != null) {
            params.put("searchFilter", searchFilter);
        }
        if (selectedRows != null) {
            params.put("selectedRows", selectedRows);
        }
        params.put("subreportParamValues", Collections.emptyMap());

        asyncManager.createTask(OperationType.getOperationTypeBySubreport(reportAlias), userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public String createTaskToCreateReportXlsx(final TAUserInfo userInfo, final long declarationDataId) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReportXlsx by %s. declarationDataId: %s",
                userInfo, declarationDataId));

        Logger logger = new Logger();

        reportService.deleteByDeclarationAndType(declarationDataId, DeclarationReportType.EXCEL_DEC);
        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);

        asyncManager.createTask(OperationType.EXCEL_DEC, userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public ReportAvailableResult checkAvailabilityReports(TAUserInfo userInfo, long declarationDataId) {
        ReportAvailableResult reportAvailableResult = new ReportAvailableResult();
        if (!existDeclarationData(declarationDataId)) {
            reportAvailableResult.setDeclarationDataExist(false);
        } else {
            reportAvailableResult.setReportAvailable(DeclarationReportType.EXCEL_DEC.getCode(), reportService.getReportFileUuid(declarationDataId, DeclarationReportType.EXCEL_DEC) != null);
            reportAvailableResult.setReportAvailable(DeclarationReportType.XML_DEC.getCode(), reportService.getReportFileUuid(declarationDataId, DeclarationReportType.XML_DEC) != null);
            reportAvailableResult.setReportAvailable(DeclarationReportType.EXCEL_TEMPLATE_DEC.getCode(), reportService.getReportFileUuid(declarationDataId, DeclarationReportType.EXCEL_TEMPLATE_DEC) != null);

            DeclarationData declaration = get(declarationDataId, userInfo);
            List<DeclarationSubreport> subreports = declarationTemplateService.get(declaration.getDeclarationTemplateId()).getSubreports();
            for (DeclarationSubreport subreport : subreports) {
                reportAvailableResult.setReportAvailable(subreport.getAlias(), reportService.getReportFileUuid(declarationDataId, DeclarationReportType.createSpecificReport(subreport)) != null);
            }
        }
        return reportAvailableResult;
    }

    @Override
    public ReportAvailableReportDDResult checkAvailabilityReportDD(TAUserInfo userInfo, long declarationDataId) {
        ReportAvailableReportDDResult result = new ReportAvailableReportDDResult();
        if (!existDeclarationData(declarationDataId)) {
            result.setDeclarationDataExist(false);
        } else {
            result.setAvailablePdf(reportService.getReportFileUuid(declarationDataId, DeclarationReportType.PDF_DEC) != null);
            result.setDownloadXlsxAvailable(reportService.getReportFileUuid(declarationDataId, DeclarationReportType.EXCEL_DEC) != null);
            result.setDownloadXmlAvailable(reportService.getReportFileUuid(declarationDataId, DeclarationReportType.XML_DEC) != null);
        }
        return result;
    }

    @Override
    public DeclarationData get(long id) {
        return declarationDataDao.get(id);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public DeclarationData get(long id, TAUserInfo userInfo) {
        return declarationDataDao.get(id);
    }

    @Override
    public List<DeclarationData> get(List<Long> ids) {
        return declarationDataDao.get(ids);
    }

    @Override
    @Transactional
    public void deleteSync(long id, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.deleteSync by %s. id: %s",
                userInfo, id));
        DeclarationData declarationData = get(id);
        deleteReport(id, userInfo, TaskInterruptCause.DECLARATION_DELETE);
        declarationDataDao.delete(id);
        auditService.add(FormDataEvent.DELETE, userInfo, declarationData, "Налоговая форма удалена", null);
    }

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов, связанных с декларациями
     */
    private void deleteReport(long declarationDataId, TAUserInfo userInfo, TaskInterruptCause cause) {
        // TODO не работает как надо из-за generateAsyncTaskKey
        AsyncTaskType[] asyncTaskTypes = {AsyncTaskType.XML_DEC, AsyncTaskType.PDF_DEC, AsyncTaskType.EXCEL_DEC, AsyncTaskType.CHECK_DEC, AsyncTaskType.ACCEPT_DEC};
        for (AsyncTaskType asyncTaskType : asyncTaskTypes) {
            LockData lock = lockDataService.findLock(generateAsyncTaskKey(declarationDataId, asyncTaskType));
            if (lock != null) {
                asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
            }
        }
        reportService.deleteAllByDeclarationId(declarationDataId);
    }


    @Override
    @Transactional
    public ActionResult createDeleteDeclarationDataTask(TAUserInfo userInfo, List<Long> declarationDataIds) {
        LOG.info(String.format("DeclarationDataServiceImpl.createDeleteDeclarationDataTask by %s. declarationDataIds: %s",
                userInfo, declarationDataIds));
        ActionResult result = new ActionResult();
        Logger logger = new Logger();

        for (Long declarationId : declarationDataIds) {

            if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                    new TargetIdAndLogger(declarationId, logger),
                    "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.DELETE)) {
                Map<String, Object> params = new HashMap<>();
                params.put("declarationDataId", declarationId);
                asyncManager.createTask(OperationType.DELETE_DEC, userInfo, params, logger);
            } else {
                makeNotificationForAccessDenied(logger);
            }
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            result.setSuccess(true);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).ACCEPTED)")
    public void accept(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        LOG.info(String.format("DeclarationDataServiceImpl.accept by %s. id: %s",
                userInfo, id));
        DeclarationData declarationData = declarationDataDao.get(id);

        Logger scriptLogger = new Logger();
        try {
            lockStateLogger.updateState(AsyncTaskState.FORM_CHECK);
            Map<String, Object> exchangeParams = new HashMap<>();
            exchangeParams.put("operationType", OperationType.ACCEPT_DEC);
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CHECK, scriptLogger, exchangeParams);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            lockStateLogger.updateState(AsyncTaskState.FORM_STATUS_CHANGE);
            declarationDataDao.setStatus(id, State.ACCEPTED);

            logBusinessService.logFormEvent(id, FormDataEvent.ACCEPT, logger.getLogId(), "Форма принята", userInfo);
            auditService.add(FormDataEvent.ACCEPT, userInfo, declarationData, "Форма принята", null);
        } else {
            logBusinessService.logFormEvent(id, FormDataEvent.ACCEPT, logger.getLogId(), "Фатальные ошибки", userInfo);
        }
    }

    @Override
    @Transactional
    public ActionResult createAcceptDeclarationDataTask(final TAUserInfo userInfo, List<Long> declarationDataIds) {
        LOG.info(String.format("DeclarationDataServiceImpl.createCheckDeclarationDataTask by %s. operationType: %s; declarationDataIds: %s; permission: %s",
                userInfo, OperationType.ACCEPT_DEC, declarationDataIds, DeclarationDataPermission.ACCEPTED));
        final ActionResult result = new ActionResult();
        Logger logger = new Logger();
        for (final Long declarationDataId : declarationDataIds) {
            try {
                if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                        new TargetIdAndLogger(declarationDataId, logger),
                        "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.ACCEPTED)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("declarationDataId", declarationDataId);
                    asyncManager.createTask(OperationType.ACCEPT_DEC, userInfo, params, logger);
                } else {
                    makeNotificationForAccessDenied(logger);
                }
            } catch (Exception e) {
                makeNotificationForUnexpected(e, logger, "Принятие формы", declarationDataId);
                LOG.error(e.getMessage(), e);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    private String formatCorrectionDate(Date correctionDate) {
        if (correctionDate == null) {
            return "";
        } else {
            return " корр. " + sdf.get().format(correctionDate);
        }
    }


    @Override
    @Transactional(noRollbackFor = {Exception.class})
    public void cancelDeclarationList(List<Long> declarationDataIds, String reason, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.cancelDeclarationList by %s. declarationDataIds: %s; note: %s",
                userInfo, declarationDataIds, reason));
        int userId = userInfo.getUser().getId();

        // Сортируем в порядке "КНФ, затем ПНФ", чтобы формы-приёмники не блокировали свои источники при массовом переводе в "Создана"
        List<Long> sortedDeclarationIds = sortDeclarationIdsByKnfThenPnf(declarationDataIds);

        for (Long declarationId : sortedDeclarationIds) {
            final Logger logger = logEntryService.createLogger();
            LockData lockData = null;
            try {
                // Проверяем права доступа
                if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                        new TargetIdAndLogger(declarationId, logger),
                        "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.RETURN_TO_CREATED)) {

                    // Блокируем текущую форму
                    lockData = declarationLocker.establishLock(declarationId, OperationType.RETURN_DECLARATION, userInfo, logger);
                    // Если блокировка успешная
                    if (lockData != null) {
                        LOG.info(String.format("DeclarationDataServiceImpl.cancel by %s. id: %s; reason: %s",
                                userInfo, declarationId, reason));
                        DeclarationData declarationData = declarationDataDao.get(declarationId);
                        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
                        ReportPeriodType reportPeriodType = reportPeriodService.getPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
                        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
                        Map<String, RefBookValue> asnu = provider.getRecordData(declarationData.getAsnuId());
                        String asnuClause = declarationTemplate.getType().getId() == DeclarationType.NDFL_PRIMARY ? String.format(", АСНУ: \"%s\"", asnu.get("NAME").getStringValue()) : "";
                        Map<String, Object> exchangeParams = new HashMap<>();
                        moveToCreateFacade.cancel(userInfo, declarationData, logger, exchangeParams);
                        if (logger.containsLevel(LogLevel.ERROR)) {
                            sendNotification(logger.getLastEntry().getMessage(), logEntryService.save(logger.getEntries()), userId);
                            continue;
                        }

                        declarationData.setState(State.CREATED);
                        declarationDataDao.setStatus(declarationId, declarationData.getState());

                        logBusinessService.logFormEvent(declarationId, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger.getLogId(), "Причина возврата: " + reason, userInfo);
                        auditService.add(FormDataEvent.MOVE_ACCEPTED_TO_CREATED, userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED.getTitle(), null);

                        String message = String.format("Выполнена операция \"Возврат в Создана\" для налоговой формы: № %d, Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s",
                                declarationId,
                                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                                reportPeriodType.getName(),
                                formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                                department.getName(),
                                declarationTemplate.getType().getName(),
                                asnuClause);

                        logger.info(message);
                        sendNotification(message, logEntryService.save(logger), userId);
                        logger.clear();
                    } else { // Не удалось установить блокировку
                        // Сообщение о причинах из алгоритма блокировки находится последним в logger
                        String errorMessage = logger.getLastEntry().getMessage();
                        String logsUuid = logEntryService.save(logger.getEntries());
                        sendNotification(errorMessage, logsUuid, userId);
                    }
                } else { // Нет прав доступа
                    makeNotificationForAccessDenied(logger);
                }
            } catch (Exception e) {
                String errorMessage = String.format(FAIL, "Возврат в Создана", getStandardDeclarationDescription(declarationId).concat(". Причина: ").concat(e.toString()));
                logger.error(errorMessage);
                sendNotification(errorMessage, logEntryService.save(logger.getEntries()), userId);
                LOG.error(e.getMessage(), e);
            } finally {
                if (lockData != null) {
                    lockDataService.unlock(lockData.getKey());
                }
            }
        }
    }

    // Сортировка форм: КНФ > ПНФ > другие
    private List<Long> sortDeclarationIdsByKnfThenPnf(List<Long> unsortedIds) {
        // ORDER BY KNF, THEN PNF
        List<DeclarationData> declarations = get(unsortedIds);

        List<Long> sortedIds = new ArrayList<>();
        List<Long> knfIds = new ArrayList<>();
        List<Long> pnfIds = new ArrayList<>();
        List<Long> otherIds = new ArrayList<>();

        for (DeclarationData declaration : declarations) {
            if (declaration.getDeclarationTemplateId() == DeclarationType.NDFL_CONSOLIDATE) {
                knfIds.add(declaration.getId());
            } else if (declaration.getDeclarationTemplateId() == DeclarationType.NDFL_PRIMARY) {
                pnfIds.add(declaration.getId());
            } else {
                otherIds.add(declaration.getId());
            }
        }

        sortedIds.addAll(knfIds);
        sortedIds.addAll(pnfIds);
        sortedIds.addAll(otherIds);

        return sortedIds;
    }

    private void sendNotification(String msg, String logUuid, Integer userId) {
        if (msg != null && !msg.isEmpty()) {
            List<Notification> notifications = new ArrayList<>();
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setCreateDate(new Date());
            notification.setText(msg);
            notification.setLogId(logUuid);
            notification.setNotificationType(NotificationType.DEFAULT);
            notifications.add(notification);
            notificationService.create(notifications);
        }
    }


    @Override
    @PreAuthorize("hasPermission(#declarationId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public InputStream getXmlDataAsStream(long declarationId, TAUserInfo userInfo) {
        String xmlUuid = reportService.getReportFileUuidSafe(declarationId, DeclarationReportType.XML_DEC);
        if (xmlUuid == null) {
            return null;
        }
        return blobDataService.get(xmlUuid).getInputStream();
    }

    @Override
    public String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo) {
        String xmlUuid = reportService.getReportFileUuidSafe(declarationDataId, DeclarationReportType.XML_DEC);
        if (xmlUuid != null) {
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getName();
        }
        return null;
    }

    @Override
    public Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
        String xmlUuid = reportService.getReportFileUuidSafe(declarationDataId, DeclarationReportType.XML_DEC);
        if (xmlUuid != null) {
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getCreationDate();
        }
        return null;
    }

    private void getXlsxData(long id, File xlsxFile, TAUserInfo userInfo, LockStateLogger stateLogger) {
        DeclarationData declarationData = declarationDataDao.get(id);
        String uuid = reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationReportType.JASPER_DEC);
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
                } catch (IOException | ClassNotFoundException e) {
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
            if (jrSwapFile != null) {
                jrSwapFile.dispose();
            }
        }
    }

    @Override
    @PreAuthorize("hasPermission(#declarationId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public InputStream getPdfDataAsStream(long declarationId, TAUserInfo userInfo) {
        String pdfUuid = reportService.getReportFileUuidSafe(declarationId, DeclarationReportType.PDF_DEC);
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
        LOG.info(String.format("DeclarationDataServiceImpl.createJasperReport by %s. declarationData: %s",
                userInfo, declarationData));
        String xmlUuid = reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationReportType.XML_DEC);
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
        reportService.deleteDec(asList(declarationData.getId()), asList(DeclarationReportType.PDF_DEC, DeclarationReportType.JASPER_DEC));
        LOG.info(String.format("Получение данных налоговой формы %s", declarationData.getId()));
        stateLogger.updateState(AsyncTaskState.GET_FORM_DATA);
        String xmlUuid = reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationReportType.XML_DEC);
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
                String fileName = createPdfFileName(declarationData.getId(), null, userInfo);
                reportService.attachReportToDeclaration(declarationData.getId(), blobDataService.create(pdfFile.getPath(), fileName), DeclarationReportType.PDF_DEC);

                // не сохраняем jasper-отчет, если есть XLSX-отчет
                if (reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationReportType.EXCEL_DEC) == null) {
                    LOG.info(String.format("Сохранение Jasper-макета в базе данных для налоговой формы %s", declarationData.getId()));
                    stateLogger.updateState(AsyncTaskState.SAVING_JASPER);
                    reportService.attachReportToDeclaration(declarationData.getId(), saveJPBlobData(jasperPrint), DeclarationReportType.JASPER_DEC);
                }
            } catch (IOException e) {
                throw new ServiceException(e.getLocalizedMessage(), e);
            } finally {
                if (pdfFile != null) {
                    pdfFile.delete();
                }
                jrSwapFile.dispose();
            }
        } else {
            throw new ServiceException("Налоговая форма не сформирована");
        }
    }

    @Override
    public String createSpecificReport(SpecificReportContext specificReportContext, LockStateLogger stateLogger) {
        Logger logger = specificReportContext.getLogger();
        LOG.info(String.format("DeclarationDataServiceImpl.createSpecificReport by %s. declarationData: %s; ddReportType: %s; subreportParamValues: %s; viewParamValues: %s; selectedRecord: %s",
                specificReportContext.getUserInfo(), specificReportContext.getDeclarationData(),
                specificReportContext.getDdReportType(), specificReportContext.getSubreportParamValues(),
                specificReportContext.getViewParamValues(), specificReportContext.getSelectedRecord()));
        Map<String, Object> subreportParamValues = specificReportContext.getSubreportParamValues();
        Map<String, Object> params = new HashMap<>();
        ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = new ScriptSpecificDeclarationDataReportHolder();
        File reportFile = null;
        try {
            reportFile = File.createTempFile("specific_report", ".dat");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile));
            InputStream inputStream = null;
            InputStream font = null;
            if (subreportParamValues.get("font") != null) {
                font = DeclarationDataServiceImpl.class.getResourceAsStream(subreportParamValues.get("font").toString());
                subreportParamValues.put("font", font);
            }

            DeclarationReportType ddReportType = specificReportContext.getDdReportType();
            if (ddReportType.getSubreport().getBlobDataId() != null) {
                inputStream = blobDataService.get(ddReportType.getSubreport().getBlobDataId()).getInputStream();
            }
            try {
                scriptSpecificReportHolder.setDeclarationSubreport(ddReportType.getSubreport());
                scriptSpecificReportHolder.setFileOutputStream(outputStream);
                scriptSpecificReportHolder.setFileInputStream(inputStream);
                scriptSpecificReportHolder.setFileName(ddReportType.getSubreport().getAlias());
                scriptSpecificReportHolder.setSubreportParamValues(subreportParamValues);
                scriptSpecificReportHolder.setSelectedRecord(specificReportContext.getSelectedRecord());
                scriptSpecificReportHolder.setViewParamValues(specificReportContext.getViewParamValues());
                params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
                params.put("searchFilter", specificReportContext.getSearchFilter());
                params.put("selectedRows", specificReportContext.getSelectedRows());
                stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
                if (!declarationDataScriptingService.executeScript(
                        specificReportContext.getUserInfo(), specificReportContext.getDeclarationData(),
                        FormDataEvent.CREATE_SPECIFIC_REPORT, logger, params)) {
                    throw new ServiceException("Не предусмотрена возможность формирования отчета \"%s\"", ddReportType.getSubreport().getName());
                }
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
                }
            } finally {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(font);
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
    public PrepareSpecificReportResult prepareSpecificReport(Logger logger, DeclarationData declarationData, DeclarationReportType ddReportType, Map<String, Object> subreportParamValues, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.prepareSpecificReport by %s. declarationData: %s; ddReportType: %s; subreportParamValues: %s",
                userInfo, declarationData, ddReportType, subreportParamValues));
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

    @Override
    public String setXlsxDataBlobs(Logger logger, DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        LOG.info(String.format("DeclarationDataServiceImpl.setXlsxDataBlobs by %s. declarationData: %s",
                userInfo, declarationData));
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
                if (reportFile != null) {
                    reportFile.delete();
                }
            }
        } else {
            File xlsxFile = null;
            try {
                xlsxFile = File.createTempFile("report", ".xlsx");
                getXlsxData(declarationData.getId(), xlsxFile, userInfo, stateLogger);

                LOG.info(String.format("Сохранение XLSX в базе данных для налоговой формы %s", declarationData.getId()));
                stateLogger.updateState(AsyncTaskState.SAVING_XLSX);

                reportService.deleteDec(asList(declarationData.getId()), asList(DeclarationReportType.JASPER_DEC));
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

    private void deleteTempFile(File tempFile) {
        if (tempFile != null && !tempFile.delete()) {
            LOG.warn(String.format("Временный файл %s не удален", tempFile.getAbsolutePath()));
        }
    }

    @Override
    public void validateDeclaration(DeclarationData declarationData,
                                    final Logger logger,
                                    File xmlFile,
                                    String fileName,
                                    String xsdBlobDataId) {
        if (xsdBlobDataId == null && declarationData != null) {
            LOG.info(String.format("Получение данных налоговой формы %s", declarationData.getId()));

            xsdBlobDataId = declarationTemplateDao.findXsdIdByTemplateId(declarationData.getDeclarationTemplateId());
        }
        if (xsdBlobDataId != null && !xsdBlobDataId.isEmpty()) {
            try {
                if (declarationData != null) {
                    LOG.info(String.format("Выполнение проверок XSD-файла налоговой формы %s", declarationData.getId()));
                }
                boolean valid = validateXMLService.validate(declarationData, logger, xmlFile, fileName, xsdBlobDataId);
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
                params = new HashMap<>();
            }
            params.put(JRXPathQueryExecuterFactory.XML_INPUT_STREAM, xml);
            final JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(100, jrSwapFile);
            Runtime.getRuntime().addShutdownHook(new Thread("JRSwapFileVirtualizerCleanup-" + virtualizer) {
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
                zipOutFile = ZipUtils.archive(jasperPrintFile, "report.jasper");
                return blobDataService.create(zipOutFile.getPath(), "");
            } finally {
                deleteTempFile(zipOutFile);
            }
        } finally {
            if (jasperPrintFile != null) {
                jasperPrintFile.delete();
            }
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
                ReportPeriod period = reportPeriodService.fetchReportPeriod(declarationData.getReportPeriodId());
                DepartmentReportPeriod drp = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());

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
    public String generateAsyncTaskKey(long declarationDataId, AsyncTaskType asyncTaskType) {
        if (asyncTaskType == null) {
            return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId;
        } else {
            return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId + "_" + asyncTaskType.getName().toUpperCase();
        }
    }

    @Override
    public ActionResult lock(long declarationDataId, OperationType operationType, TAUserInfo userInfo) {
        Logger logger = new Logger();
        LockData lockData = declarationLocker.establishLock(declarationDataId, operationType, userInfo, logger);

        ActionResult result = new ActionResult();
        result.setSuccess((lockData != null));
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public ActionResult unlock(long declarationDataId, OperationType operationType) {
        Logger logger = new Logger();
        declarationLocker.unlock(declarationDataId, operationType, null, logger);

        ActionResult result = new ActionResult();
        result.setSuccess(!logger.containsLevel(LogLevel.ERROR));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public void cleanBlobs(Collection<Long> ids, List<DeclarationReportType> reportTypes) {
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
            ReportPeriod rp = reportPeriodService.fetchReportPeriod(dd.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.fetchOne(dd.getDepartmentReportPeriodId());
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
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));
        switch (taskType) {
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
                throw new IllegalArgumentException("Unknown async type");
        }
    }

    @Override
    public String getDeclarationFullName(long declarationId, AsyncTaskType asyncTaskType, String... args) {
        DeclarationData declaration = declarationDataDao.get(declarationId);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        if (asyncTaskType == null)
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

        switch (asyncTaskType) {
            case CREATE_FORMS_DEC:
                return String.format(asyncTaskType.getDescription(),
                        declarationTemplate.getType().getName(),
                        reportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + reportPeriod.getReportPeriod().getName(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        departmentService.getDepartment(reportPeriod.getDepartmentId()).getName()
                );
            case UPDATE_PERSONS_DATA:
                return String.format(asyncTaskType.getDescription(),
                        declarationId,
                        departmentReportPeriodFormatter.formatPeriodName(reportPeriod, sdf.get().toPattern()),
                        departmentService.getDepartment(reportPeriod.getDepartmentId()).getName());
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
    public Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, AsyncTaskType asyncTaskType, Map<String, Object> params) {
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        switch (asyncTaskType) {
            case PDF_DEC:
            case EXCEL_DEC:
            case ACCEPT_DEC:
            case CHECK_DEC:
            case DELETE_DEC:
            case EXCEL_TEMPLATE_DEC:
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
            case IDENTIFY_PERSON:
            case CONSOLIDATE:
            case XML_DEC:
                if (declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.REPORTS)) {
                    return (long) ndflPersonDao.getNdflPersonReferencesCount(declarationDataId);
                } else if (declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED)) {
                    Long personCount = 0L;
                    try {
                        List<Relation> relationList = sourceService.getDeclarationSourcesInfo(declarationData.getId());
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
                String alias = (String) params.get("alias");
                if (RNU_NDFL_PERSON_ALL_DB.equals(alias) && isReportByAllData(params)) {
                    return ndflPersonDao.getNdflPersonAllSectionMaxCount(declarationDataId);
                } else {
                    return 0L;
                }
            default:
                throw new ServiceException("Неверный тип отчета(%s)", asyncTaskType);
        }
    }

    private boolean isReportByAllData(Map<String, Object> params) {
        Object searchFilter = params.get("searchFilter");
        Object selectedRows = params.get("selectedRows");
        return searchFilter == null && selectedRows == null;
    }

    private void checkSources(DeclarationData dd, Logger logger) {
        boolean consolidationOk = true;
        //Проверка на неактуальные консолидированные данные  3А
        if (!sourceService.isDDConsolidationTopical(dd.getId())) {
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(dd.getDeclarationTemplateId());
            boolean isReports = DeclarationFormKind.REPORTS.equals(declarationTemplate.getDeclarationFormKind());
            logger.error(CALCULATION_NOT_TOPICAL + (isReports ? "" : CALCULATION_NOT_TOPICAL_SUFFIX));
        } else {
            //Проверка того, что консолидация вообще когда то выполнялась для всех источников
            List<Relation> relations = sourceService.getDeclarationSourcesInfo(dd.getId());
            for (Relation relation : relations) {
                if (!sourceService.isDeclarationSourceConsolidated(dd.getId(), relation.getDeclarationDataId())) {
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
    public List<DeclarationDataFile> getFiles(long formDataId) {
        return declarationDataFileDao.fetchByDeclarationDataId(formDataId);
    }

    @Override
    public List<DeclarationDataFile> findAllFilesByDeclarationIdAndType(Long declarationDataId, AttachFileType fileType) {
        return declarationDataFileDao.findAllByDeclarationIdAndType(declarationDataId, fileType);
    }

    @Override
    public String getNote(long declarationDataId) {
        return declarationDataDao.getNote(declarationDataId);
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
    public String createReports(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReports by %s. departmentReportPeriod: %s; declarationTypeId: %s",
                userInfo, departmentReportPeriod, declarationTypeId));
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
    public boolean existDeclarationData(long declarationDataId) {
        return declarationDataDao.existDeclarationData(declarationDataId);
    }

    @Override
    public DeclarationDataExistenceAndKindResult fetchDeclarationDataExistenceAndKind(TAUserInfo userInfo, long declarationDataId) {
        if (!declarationDataDao.existDeclarationData(declarationDataId)) {
            return new DeclarationDataExistenceAndKindResult(false);
        } else {
            DeclarationData declarationData = get(declarationDataId, userInfo);
            long kind = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getDeclarationFormKind().getId();
            return new DeclarationDataExistenceAndKindResult(true, kind);
        }
    }

    private boolean preCreateReports(Logger logger, TAUserInfo userInfo, DeclarationData declarationData) {
        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        exchangeParams.put("paramMap", paramMap);

        declarationDataScriptingService.executeScript(userInfo,
                declarationData, FormDataEvent.PRE_CREATE_REPORTS, logger, exchangeParams);
        return (Boolean) paramMap.get("successfullPreCreate");
    }

    /**
     * Получение КНФ для формирования ОНФ.
     */
    private DeclarationData findKnfForReport(CreateReportFormsAction action, Logger logger) {

        // Определяем КНФ, Подразделение и Отчетный период.
        DeclarationData knf;
        DepartmentReportPeriod departmentReportPeriod;
        if (action.getKnfId() != null) {
            knf = declarationDataDao.get(action.getKnfId());
            departmentReportPeriod = departmentReportPeriodService.fetchOne(knf.getDepartmentReportPeriodId());
        } else {
            departmentReportPeriod = departmentReportPeriodService.fetchLast(action.getDepartmentId(), action.getPeriodId());
            RefBookKnfType knfType = action.getDeclarationTypeId() == DeclarationType.NDFL_2_2 ? RefBookKnfType.BY_NONHOLDING_TAX : RefBookKnfType.ALL;
            knf = declarationDataDao.findKnfByKnfTypeAndPeriodId(knfType, departmentReportPeriod.getId());
        }

        // Период должен быть открыт.
        if (!departmentReportPeriod.isActive()) {
            logger.error(generatePeriodIsClosedErrorText(action.getDeclarationTypeId(), departmentReportPeriod));
            return null;
        }
        // КНФ должна существовать :)
        if (knf == null) {
            logger.error(generateKnfIsNotFoundErrorText(action.getDeclarationTypeId(), departmentReportPeriod));
            return null;
        }
        // КНФ должна быть Принята
        if (knf.getState() != State.ACCEPTED) {
            logger.error(generateKnfIsNotAcceptedErrorText(action.getDeclarationTypeId(), knf, departmentReportPeriod));
            return null;
        }
        return knf;
    }

    /**
     * Генерация текста ошибки при формировании ОНФ по закрытому периоду.
     */
    private String generatePeriodIsClosedErrorText(Integer reportDeclarationTypeId, DepartmentReportPeriod departmentReportPeriod) {
        DeclarationType declarationType = declarationTypeDao.get(reportDeclarationTypeId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        return String.format("Не выполнена операция \"Создание отчетных форм: \"%s\", Период: \"%s, %s%s\", Подразделение: \"%s\". " +
                        "Причина: Выбранный период закрыт.",
                declarationType.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                department.getName()
        );
    }

    /**
     * Генерация текста ошибки при формировании ОНФ по отсутствующей КНФ.
     */
    private String generateKnfIsNotFoundErrorText(Integer reportDeclarationTypeId, DepartmentReportPeriod departmentReportPeriod) {
        DeclarationType declarationType = declarationTypeDao.get(reportDeclarationTypeId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        return String.format("Отчетность %s для %s за период %s, %s%s не сформирована. " +
                        "Для указанного подразделения и периода не найдена форма РНУ НДФЛ (консолидированная).",
                declarationType.getName(),
                department.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                formatCorrectionDate(departmentReportPeriod.getCorrectionDate())
        );
    }

    /**
     * Генерация текста ошибки при формировании ОНФ по непринятой КНФ.
     */
    private String generateKnfIsNotAcceptedErrorText(Integer reportDeclarationTypeId, DeclarationData knf, DepartmentReportPeriod departmentReportPeriod) {
        DeclarationType declarationType = declarationTypeDao.get(reportDeclarationTypeId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        return String.format("Отчетность %s для %s за период %s, %s%s не сформирована. " +
                        "Для указанного подразделения и периода форма РНУ НДФЛ (консолидированная) № %s должна быть в состоянии \"Принята\". Примите форму и повторите операцию",
                declarationType.getName(),
                department.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                knf.getId()
        );
    }

    @Override
    public ActionResult asyncExportReports(DeclarationDataFilter filter, TAUserInfo userInfo) {
        setUpDeclarationFilter(filter, userInfo);
        List<Long> declarationDataIdList = declarationDataDao.findAllIdsByFilter(filter);
        return asyncExportReports(declarationDataIdList, userInfo);
    }

    @Override
    public ActionResult asyncExportReports(List<Long> declarationDataIds, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (declarationDataIds.isEmpty()) {
            logger.error("По заданым параметрам не найдено ни одной формы");
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("declarationDataIds", declarationDataIds);
            asyncManager.createTask(OperationType.EXPORT_REPORTS, userInfo, params, logger);
        }
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    public String exportReports(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger) {
        List<DeclarationData> declarationDataList = declarationDataDao.get(declarationDataIds);
        List<DeclarationData> successfulPreCreateDeclarationDataList = new LinkedList<>();
        List<DeclarationData> unsuccessfulPreCreateDeclarationDataList = new LinkedList<>();
        for (DeclarationData declarationData : declarationDataList) {
            if (preCreateReports(logger, userInfo, declarationData)) {
                successfulPreCreateDeclarationDataList.add(declarationData);
            } else {
                unsuccessfulPreCreateDeclarationDataList.add(declarationData);
            }
        }
        String reportId = null;
        if (successfulPreCreateDeclarationDataList.isEmpty()) {
            logger.error("Отчетность не выгружена. В выбранных отчетных формах некорректное количество файлов " +
                    "формата xml, категория которых равна \"Исходящий в ФНС\", должно быть файлов: один");
        } else {
            for (DeclarationData declarationData : unsuccessfulPreCreateDeclarationDataList) {
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
                Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                String strCorrPeriod = "";
                if (departmentReportPeriod.getCorrectionDate() != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
                }
                String msg = String.format("Отчетность %s за период %s, подразделение: \"%s\" не выгружена. В налоговой " +
                                "форме № %d некорректное количество файлов формата xml, категория которых равна \"Исходящий в ФНС\", " +
                                "должно быть файлов: один",
                        declarationTemplate.getName(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                        department.getShortName(),
                        declarationData.getId());
                logger.warn(msg);
            }
            reportId = createReports(successfulPreCreateDeclarationDataList, userInfo);

            for (DeclarationData declarationData : successfulPreCreateDeclarationDataList) {
                RefBookDocState docState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), declarationData.getDocStateId());
                logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.EXPORT_REPORT_FROMS, logger.getLogId(),
                        "Выгружено для отправки в ФНС, Состояние ЭД =\"" + docState.getName() + "\"", userInfo);
            }
        }
        return reportId;
    }

    private String createReports(List<DeclarationData> declarationDataList, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReports by %s. declarationDataList: %s",
                userInfo, declarationDataList));
        File reportFile;
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
                    drp = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
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
                if (RefBookDocState.NOT_SENT.getId().equals(declarationData.getDocStateId())) {
                    declarationDataDao.updateDocState(declarationData.getId(), RefBookDocState.EXPORTED.getId());
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
        return blobDataService.create(reportFile, fileName, creationDate);
    }

    @Override
    public ActionResult createTaskToUpdateDocState(DeclarationDataFilter filter, long docStateId, TAUserInfo userInfo) {
        List<Long> declarationDataIdList = declarationDataDao.findAllIdsByFilter(filter);
        return createTaskToUpdateDocState(declarationDataIdList, docStateId, userInfo);
    }

    @Override
    public ActionResult createTaskToUpdateDocState(List<Long> declarationDataIds, long docStateId, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (declarationDataIds.isEmpty()) {
            logger.error("По заданым параметрам не найдено ни одной формы");
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("declarationDataIds", declarationDataIds);
            params.put("docStateId", docStateId);
            asyncManager.createTask(OperationType.UPDATE_DOC_STATE, userInfo, params, logger);
        }
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    public void updateDocState(List<Long> declarationDataIds, long docStateId, TAUserInfo userInfo, Logger logger) {
        RefBookDocState docState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), docStateId);
        List<DeclarationData> declarations = declarationDataDao.get(declarationDataIds);
        for (DeclarationData declaration : declarations) {
            Logger localLogger = new Logger();
            if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                    new TargetIdAndLogger(declaration.getId(), localLogger),
                    "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.UPDATE_DOC_STATE)) {
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
                ReportPeriodType reportPeriodType = reportPeriodService.getPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
                Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                RefBookDocState docStateBefore = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), declaration.getDocStateId());

                // Список состояний, при которых производится поиск транспортных сообщений
                List<Long> suitableStateList = Arrays.asList(RefBookDocState.NOT_SENT.getId(), RefBookDocState.EXPORTED.getId(), RefBookDocState.ERROR.getId());
                if (suitableStateList.contains(docStateId)) {
                    Integer transportMessageCount = transportMessageDao.countByDeclarationIdAndType(declaration.getId(), TransportMessageType.OUTGOING.getIntValue());
                    if (transportMessageCount > 0) {
                        logger.error("Не выполнена операция изменения состояния ЭД для налоговой формы: %s. " +
                                        "Причина: Для формы найдено Исходящее ТС в журнале \"Обмен с ФП АС Учет Налогов\". " +
                                        "Запрещено устанавливать Состояние ЭД= %s, для форм, которые уже отправлены в ЭДО.",
                                getFullDeclarationDescription(declaration.getId()),
                                docState.getName());
                        continue;
                    }
                }

                updateDocState(declaration.getId(), docStateId);

                logger.info("%s для отчетной  налоговой формы: № %s, Период: \"%s, %s%s\", Подразделение: \"%s\" завершено. " +
                                "Изменено \"Состояние ЭД\": \"%s\"->\"%s\".",
                        AsyncTaskType.UPDATE_DOC_STATE.getDescription(),
                        declaration.getId(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriodType.getName(),
                        formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                        department.getName(),
                        docStateBefore.getName(),
                        docState.getName());
                logBusinessService.logFormEvent(declaration.getId(), FormDataEvent.CHANGE_STATUS_ED, logger.getLogId(), "Состояние ЭД = " + docState.getName(), userInfo);
            } else {
                logger.getEntries().addAll(localLogger.getEntries());
            }
        }
    }

    @Override
    public void updateDocState(long declarationId, long docStateId) {
        declarationDataDao.updateDocState(declarationId, docStateId);
    }

    @Override
    public ActionResult createTaskToSendEdo(DeclarationDataFilter filter, TAUserInfo userInfo) {
        List<Long> declarationDataIdList = declarationDataDao.findAllIdsByFilter(filter);
        return createTaskToSendEdo(declarationDataIdList, userInfo);
    }

    @Override
    public ActionResult createTaskToSendEdo(List<Long> declarationDataIds, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (declarationDataIds.isEmpty()) {
            logger.error("По заданым параметрам не найдено ни одной формы");
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("noLockDeclarationDataIds", declarationDataIds);
            params.put("userIP", userInfo.getIp());
            asyncManager.createTask(OperationType.SEND_EDO, userInfo, params, logger);
        }
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    @Override
    public SendToEdoResult sendToEdo(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger) {
        return edoMessageService.sendToEdo(declarationDataIds, userInfo, logger);
    }

    @Override
    public ActionResult createReportNdflByPersonReport(long declarationDataId, final CreateReportAction action, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReportNdflByPersonReport by %s. action: %s",
                userInfo, action));
        ActionResult result = new ActionResult();
        DeclarationData declaration = get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String alias = "";

        if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_1) {
            alias = SubreportAliasConstants.REPORT_2NDFL1;
        } else if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_2) {
            alias = SubreportAliasConstants.REPORT_2NDFL2;
        }

        Logger logger = new Logger();

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> subreportParamValues = action.getSubreportParamValues();
        subreportParamValues.put("font", "/courier/cour.ttf");
        params.put("declarationDataId", declarationDataId);

        params.put("alias", alias);
        params.put("viewParamValues", new LinkedHashMap<String, String>());
        params.put("subreportParamValues", action.getSubreportParamValues());

        if (action.getSelectedRow() != null) {
            List<Cell> cellList = new ArrayList<>();
            for (Map.Entry<String, Object> cellData : action.getSelectedRow().entrySet()) {
                Column column = new StringColumn();
                column.setAlias(cellData.getKey());
                Cell cell = new Cell(column, new ArrayList<FormStyle>());
                cell.setStringValue(cellData.getValue().toString());
                cellList.add(cell);
            }
            DataRow<Cell> dataRow = new DataRow<>(cellList);
            params.put("selectedRecord", dataRow);
        }
        asyncManager.createTask(OperationType.getOperationTypeBySubreport(alias), userInfo, params, logger);
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @Transactional
    public PrepareSubreportResult prepareSubreport(TAUserInfo userInfo, PrepareSubreportAction action) {
        LOG.info(String.format("DeclarationDataServiceImpl.prepareSubreport by %s. action: %s",
                userInfo, action));
        PrepareSubreportResult result = new PrepareSubreportResult();
        if (!existDeclarationData(action.getDeclarationDataId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationDataId());
        } else {
            Logger logger = new Logger();
            DeclarationData declarationData = get(action.getDeclarationDataId(), userInfo);
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
            String alias = "";
            if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_1) {
                alias = SubreportAliasConstants.REPORT_2NDFL1;
            } else if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_2) {
                alias = SubreportAliasConstants.REPORT_2NDFL2;
            }
            DeclarationReportType ddReportType = DeclarationReportType.SPECIFIC_REPORT_DEC;
            ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias.toLowerCase()));

            Map<String, Object> subreportParamValues = null;
            if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
                for (Map.Entry<String, Object> entrie : action.getSubreportParamValues().entrySet()) {
                    Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}.*");
                    Matcher matcher = pattern.matcher(entrie.getValue() != null ? entrie.getValue().toString() : "");
                    if (matcher.find()) {
                        try {
                            entrie.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(entrie.getValue().toString().substring(0, 10)));
                        } catch (ParseException e) {
                            LOG.error(e);
                            throw new ServiceException(e.getMessage());
                        }
                    }
                }
                subreportParamValues = action.getSubreportParamValues();
            }

            result.setPrepareSpecificReportResult(prepareSpecificReport(logger, declarationData, ddReportType, subreportParamValues, userInfo));
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public String createTaskToCreateExcelTemplate(final long declarationDataId, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createTaskToCreateExcelTemplate by %s. declarationDataId: %s",
                userInfo, declarationDataId));
        Logger logger = new Logger();

        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);

        asyncManager.createTask(OperationType.EXCEL_TEMPLATE_DEC, userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public String createExcelTemplate(DeclarationData declaration, TAUserInfo userInfo, Logger logger, LockStateLogger stateLogger) throws IOException {
        LOG.info(String.format("DeclarationDataServiceImpl.createExcelTemplate by %s. declaration: %s",
                userInfo, declaration));
        Map<String, Object> params = new HashMap<>();
        ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = new ScriptSpecificDeclarationDataReportHolder();
        File reportFile = File.createTempFile("report", ".dat");
        try (InputStream inputStream = declarationTemplateDao.getTemplateFileContent(declaration.getDeclarationTemplateId(), DeclarationTemplateFile.TF_TEMPLATE);
             OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile))) {
            if (inputStream == null) {
                throw new ServiceException("Файл не найден");
            }
            scriptSpecificReportHolder.setFileOutputStream(outputStream);
            scriptSpecificReportHolder.setFileInputStream(inputStream);
            scriptSpecificReportHolder.setFileName("report.xlsx");
            params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
            stateLogger.updateState(AsyncTaskState.FILLING_XLSX_REPORT);
            declarationDataScriptingService.executeScript(userInfo, declaration, FormDataEvent.EXPORT_DECLARATION_DATA_TO_EXCEL, logger, params);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
            }
            stateLogger.updateState(AsyncTaskState.SAVING_XLSX);
            return blobDataService.create(reportFile.getPath(), scriptSpecificReportHolder.getFileName());
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            reportFile.delete();
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).IMPORT_EXCEL)")
    public ActionResult createTaskToImportExcel(final long declarationDataId, String fileName, InputStream inputStream, long fileSize, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createTaskToImportExcel by %s. declarationDataId: %s; fileName: %s",
                userInfo, declarationDataId, fileName));
        final ActionResult result = new ActionResult();
        Logger logger = new Logger();
        fileName = FilenameUtils.getName(fileName);
        String uuid = blobDataService.create(inputStream, fileName);
        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);
        params.put("blobDataId", uuid);
        params.put("fileName", fileName);
        params.put("fileSize", fileSize);
        asyncManager.createTask(OperationType.IMPORT_DECLARATION_EXCEL, userInfo, params, logger);
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).IMPORT_EXCEL)")
    public void importExcel(long declarationDataId, BlobData blobData, TAUserInfo userInfo, Logger logger) {
        LOG.info(String.format("DeclarationDataServiceImpl.importExcel by %s. declarationDataId: %s; blobData: %s",
                userInfo, declarationDataId, blobData.getUuid()));
        TAUser user = userInfo.getUser();
        File tempFile = null;
        try {

            LOG.info(String.format("Загрузка данных из Excel-файла в налоговую форму %s", declarationDataId));
            DeclarationData declarationData = declarationDataDao.get(declarationDataId);

            reportService.deleteNotXmlDec(declarationDataId);

            tempFile = createTempFile("tmp_dec_", ".xlsx", blobData.getInputStream());
            Map<String, Object> params = new HashMap<>();
            params.put("fileName", blobData.getName());
            params.put("file", tempFile);
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.IMPORT, logger, params);

            declarationDataFileDao.deleteTransportFileExcel(declarationDataId);

            DeclarationDataFile declarationDataFile = new DeclarationDataFile();
            declarationDataFile.setDeclarationDataId(declarationData.getId());
            declarationDataFile.setUuid(blobData.getUuid());
            declarationDataFile.setUserName(userInfo.getUser().getName());
            declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(user.getDepartmentId()));
            declarationDataFile.setFileTypeId(AttachFileType.TRANSPORT_FILE.getId());
            declarationDataFileDao.create(declarationDataFile);

            if (!logger.containsLevel(LogLevel.ERROR)) {
                String note = "Загружены данные из файла: \"" + blobData.getName() + "\"";
                auditService.add(FormDataEvent.IMPORT, userInfo, declarationData, note, null);
                logBusinessService.logFormEvent(declarationDataId, FormDataEvent.IMPORT_TRANSPORT_FILE, logger.getLogId(), note, userInfo);
                declarationDataDao.updateLastDataModified(declarationDataId);
            }

        } finally {
            // TODO: Добавить сюда снятие блокировки с формы в случае ошибки. Старое удалено, т.к. оно снимало блокировку старого формата.
            // Возможно, оно даже здесь и не нужно, вроде бы блокировка нормально удаляется, но лучше проверить на разных уровнях выбрасывания исключений.
            deleteTempFile(tempFile);
        }
    }

    private File createTempFile(String prefix, String suffix, InputStream inputStream) {
        File file;
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(
                file = File.createTempFile(prefix, suffix)
        ))) {
            IOUtils.copy(inputStream, out);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        return file;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public ActionResult updateNdflIncomesAndTax(Long declarationDataId, TAUserInfo userInfo, NdflPersonIncomeDTO incomeDTO) {
        try {
            Logger logger = logEntryService.createLogger();
            NdflPersonIncome income = incomeDTO.toIncome();

            NdflPersonIncome incomeBeforeUpdate = ndflPersonDao.fetchOneNdflPersonIncome(income.getId());
            NdflRowEditChangelogBuilder changelogBuilder = new NdflRowEditChangelogBuilder(incomeBeforeUpdate, income);
            if (changelogBuilder.hasChanges()) {
                ndflPersonDao.updateOneNdflIncome(income, userInfo);
                reportService.deleteDec(singletonList(declarationDataId), asList(DeclarationReportType.SPECIFIC_REPORT_DEC, DeclarationReportType.EXCEL_DEC));
                List<Long> changedPersonIds = updateAdditionalSortParams(incomeDTO.getNdflPersonId(), incomeDTO.getOperationId());
                sortPersonRows(changedPersonIds);

                NdflPersonIncome incomeAfterUpdate = ndflPersonDao.fetchOneNdflPersonIncome(income.getId());
                for (String message : changelogBuilder.build(declarationDataId, incomeBeforeUpdate.getRowNum(), incomeAfterUpdate.getRowNum())) {
                    logger.info(message);
                }
                logBusinessService.logFormEvent(declarationDataId, FormDataEvent.NDFL_EDIT, logger.getLogId(), null, userInfo);
                sendNotification(changelogBuilder.notificationMessage, logger.getLogId(), userInfo.getUser().getId());
            } else {
                String msg = "Не выполнена замена данных в строке № " + incomeBeforeUpdate.getRowNum() +
                        " Раздела 2 в форме № " + declarationDataId + ", так как в форме редактирования не было внесено изменений.";
                logger.error(msg);
                sendNotification(msg, logger.getLogId(), userInfo.getUser().getId());
            }
            return new ActionResult(logEntryService.save(logger));
        } catch (Exception e) {
            String errorMessage = generateDeclarationEditErrorMsg(declarationDataId, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public ActionResult updateNdflIncomeDates(Long declarationDataId, TAUserInfo userInfo, NdflPersonIncomeDatesDTO incomeDates) {
        LOG.info(String.format("Update NdflIncomes dates: %s", incomeDates));

        Logger logger = logEntryService.createLogger();
        Notification notification = new Notification();

        try {
            // Достаем из базы строки для редактирования
            List<Long> incomeIds = incomeDates.getIncomeIds();
            List<NdflPersonIncome> incomes = ndflPersonDao.findAllIncomesByIdIn(incomeIds);

            // Записываем ФЛ и операции, которые затронул алгоритм
            Set<Pair<Long, String>> personAndOperationAffected = new HashSet<>();
            // Записываем информацию о том, были ли вообще затронуты строки.
            boolean isAnyDateChanged = false;

            // Заменяем значения в строках
            for (NdflPersonIncome income : incomes) {
                // ФЛ, к которому относится строка дохода
                NdflPerson person = ndflPersonDao.findById(income.getNdflPersonId());

                // Редактируем все требуемые поля по очереди.
                for (EditableDateField dateField : EditableDateField.values()) {
                    try {
                        // Для каждого вида даты своя реализация DateEditor
                        DateEditor dateEditor = DateEditorFactory.getEditor(dateField);
                        boolean dateChanged = dateEditor.editIncomeDateField(income, incomeDates, person, logger);

                        isAnyDateChanged = isAnyDateChanged || dateChanged;

                        if (dateChanged) {
                            personAndOperationAffected.add(Pair.of(income.getNdflPersonId(), income.getOperationId()));
                            TAUser user = userInfo.getUser();
                            income.setModifiedDate(new Date());
                            income.setModifiedBy(user.getName() + "(" + user.getLogin() + ")");
                        }
                    } catch (Exception e) {
                        logger.errorExp(
                                "Раздел 2. Строка %s. %s не была заменена. %s",
                                "Ошибка замены",
                                String.format("%s, ИНП: %s, ID операции: %s", person.getFullName(), person.getInp(), income.getOperationId()),
                                income.getRowNum(), dateField.getTitle(), e.getMessage()
                        );
                    }
                }
            }

            // Сохраняем изменения
            ndflPersonDao.updateIncomes(incomes);

            // Складываем сюда id NdflPerson, которые нужно пересортировать
            Set<Long> personIdsToResort = new HashSet<>();

            // Обновляем "Дополнительные параметры сортировки" измененных записей
            for (Pair<Long, String> personAndOperation : personAndOperationAffected) {
                Long personId = personAndOperation.getLeft();
                String operationId = personAndOperation.getRight();
                List<Long> updatedPersonIds = updateAdditionalSortParams(personId, operationId);
                personIdsToResort.addAll(updatedPersonIds);
            }

            // Пересортируем затронутые записи
            if (isNotEmpty(personIdsToResort)) {
                sortPersonRows(Lists.newArrayList(personIdsToResort));
            }

            // Удаляем имеющиеся у формы спецотчеты, если хотя бы одна дата была изменена
            if (isAnyDateChanged) {
                reportService.deleteDec(
                        singletonList(declarationDataId),
                        asList(DeclarationReportType.SPECIFIC_REPORT_DEC, DeclarationReportType.EXCEL_DEC)
                );
            }

            String onEmpty = " __ ";
            if (isAnyDateChanged) {
                logger.info("Выполнено изменение дат, указанных пользователем. Дата начисления дохода = \"%s\", Дата выплаты дохода = \"%s\", Дата НДФЛ = \"%s\", Срок перечисления = \"%s\".",
                        DateUtils.commonDateFormat(incomeDates.getAccruedDate(), onEmpty),
                        DateUtils.commonDateFormat(incomeDates.getPayoutDate(), onEmpty),
                        DateUtils.commonDateFormat(incomeDates.getTaxDate(), onEmpty),
                        DateUtils.formatPossibleZeroDate(incomeDates.getTransferDate(), onEmpty)
                );
                notification.setText(String.format("Для формы №%s выполнено изменение дат раздела 2", declarationDataId));
            } else {
                logger.warn("Не выполнено изменение дат, указанных пользователем. Дата начисления дохода = \"%s\", Дата выплаты дохода = \"%s\", Дата НДФЛ = \"%s\", Срок перечисления = \"%s\".",
                        DateUtils.commonDateFormat(incomeDates.getAccruedDate(), onEmpty),
                        DateUtils.commonDateFormat(incomeDates.getPayoutDate(), onEmpty),
                        DateUtils.commonDateFormat(incomeDates.getTaxDate(), onEmpty),
                        DateUtils.formatPossibleZeroDate(incomeDates.getTransferDate(), onEmpty)
                );
                notification.setText(String.format("Для формы №%s не выполнено изменение дат раздела 2", declarationDataId));
            }
        } catch (Exception e) {
            logger.error("Ошибка редактирования формы. %s", e.getMessage());
            notification.setText(String.format("Для формы №%s не выполнено изменение дат раздела 2", declarationDataId));
        }

        logBusinessService.logFormEvent(declarationDataId, FormDataEvent.NDFL_DATES_EDIT, logger.getLogId(), null, userInfo);
        String logsUuid = logEntryService.save(logger);

        notification.setLogId(logsUuid);
        notification.setUserId(userInfo.getUser().getId());
        notification.setCreateDate(new Date());
        notificationService.create(Collections.singletonList(notification));

        ActionResult result = new ActionResult();
        result.setUuid(logsUuid);
        return result;
    }

    /**
     * Расчет дополнительных параметров сортировки строк.
     *
     * @param personId    идентификатор NdflPerson
     * @param operationId ID операции (Гр. 3)
     * @return идентификаторы NdflPerson, чьи записи были затронуты.
     */
    private List<Long> updateAdditionalSortParams(Long personId, String operationId) {
        LOG.info(String.format("Calculate additional sort params for personId = %s, operationId = %s", personId, operationId));
        NdflPerson ndflPerson = ndflPersonDao.findById(personId);
        List<NdflPerson> operationDatePersons = ndflPersonDao.findDeclarartionDataPersonWithSameOperationIdAndInp(ndflPerson.getDeclarationDataId(), ndflPerson.getInp(), operationId);
        List<NdflPersonIncome> operationDateIncomes = ndflPersonDao.findDeclarartionDataIncomesWithSameOperationIdAndInp(ndflPerson.getDeclarationDataId(), ndflPerson.getInp(), operationId);
        Date operationDate = ndflPersonDao.findOperationDate(ndflPerson.getDeclarationDataId(), ndflPerson.getInp(), operationId);
        for (NdflPersonIncome income : operationDateIncomes) {
            income.setOperationDate(operationDate);
            if (income.getTaxDate() != null) {
                income.setActionDate(income.getTaxDate());
            } else {
                income.setActionDate(income.getIncomePayoutDate());
            }
            if (income.getIncomeAccruedDate() != null) {
                income.setRowType(NdflPersonIncome.ACCRUED_ROW_TYPE);
            } else if (income.getIncomePayoutDate() != null) {
                income.setRowType(NdflPersonIncome.PAYOUT_ROW_TYPE);
            } else {
                income.setRowType(NdflPersonIncome.OTHER_ROW_TYPE);
            }
        }
        ndflPersonDao.updateIncomes(operationDateIncomes);

        List<Long> changedPersonIds = new ArrayList<>();
        for (NdflPerson operationPerson : operationDatePersons) {
            changedPersonIds.add(operationPerson.getId());
        }
        return changedPersonIds;
    }

    /**
     * Сортировка строк переданных NdflPerson
     *
     * @param personIds идентификаторы NdflPerson
     */
    private void sortPersonRows(List<Long> personIds) {
        if (isEmpty(personIds)) return;
        LOG.info(String.format("Resorting NdflPersons data: ids = %s", personIds));

        List<NdflPerson> persons = ndflPersonDao.findByIdIn(personIds);

        for (NdflPerson person : persons) {
            List<NdflPersonIncome> incomes = ndflPersonDao.fetchNdflPersonIncomeByNdflPerson(person.getId());
            List<NdflPersonDeduction> deductions = ndflPersonDao.fetchNdflPersonDeductionByNdflPerson(person.getId());
            List<NdflPersonPrepayment> prepayments = ndflPersonDao.fetchNdflPersonPrepaymentByNdflPerson(person.getId());
            Collections.sort(incomes, NdflPersonIncome.getComparator());
            Collections.sort(deductions, NdflPersonDeduction.getComparator(person));
            Collections.sort(prepayments, NdflPersonPrepayment.getComparator(person));
            ndflPersonDao.updateIncomes(updateRowNum(incomes));
            ndflPersonDao.updateDeductions(updateRowNum(deductions));
            ndflPersonDao.updatePrepayments(updateRowNum(prepayments));
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public ActionResult updateNdflIncomeDatesByFilter(Long declarationDataId, TAUserInfo userInfo, NdflPersonIncomeDatesDTO incomeDates, NdflFilter ndflFilter) {

        Integer maxCount = configurationService.getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT);
        int count = maxCount != null ? maxCount : 200;

        PagingParams pagingParams = PagingParams.getInstance(1, count);
        pagingParams.setProperty("id");
        PagingResult<NdflPersonIncomeDTO> incomeDTOs = ndflPersonService.findPersonIncomeByFilter(ndflFilter, pagingParams);

        List<Long> incomeIds = new ArrayList<>();
        for (NdflPersonIncomeDTO incomeDto : incomeDTOs) {
            incomeIds.add(incomeDto.getId());
        }
        incomeDates.setIncomeIds(incomeIds);

        return updateNdflIncomeDates(declarationDataId, userInfo, incomeDates);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public ActionResult updateNdflDeduction(Long declarationDataId, TAUserInfo userInfo, NdflPersonDeductionDTO personDeductionDTO) {
        try {
            Logger logger = logEntryService.createLogger();
            NdflPersonDeduction deduction = personDeductionDTO.toDeduction();

            NdflPersonDeduction deductionBeforeUpdate = ndflPersonDao.fetchOneNdflPersonDeduction(deduction.getId());
            NdflRowEditChangelogBuilder changelogBuilder = new NdflRowEditChangelogBuilder(deductionBeforeUpdate, deduction);
            if (changelogBuilder.hasChanges()) {
                ndflPersonDao.updateOneNdflDeduction(deduction, userInfo);
                reportService.deleteDec(singletonList(declarationDataId), asList(DeclarationReportType.SPECIFIC_REPORT_DEC, DeclarationReportType.EXCEL_DEC));
                NdflPerson ndflPerson = ndflPersonDao.findById(personDeductionDTO.getNdflPersonId());
                Collections.sort(ndflPerson.getDeductions(), NdflPersonDeduction.getComparator(ndflPerson));
                ndflPersonDao.updateDeductions(updateRowNum(ndflPerson.getDeductions()));

                NdflPersonDeduction deductionAfterUpdate = ndflPersonDao.fetchOneNdflPersonDeduction(deduction.getId());
                for (String message : changelogBuilder.build(declarationDataId, deductionBeforeUpdate.getRowNum(), deductionAfterUpdate.getRowNum())) {
                    logger.info(message);
                }
                logBusinessService.logFormEvent(declarationDataId, FormDataEvent.NDFL_EDIT, logger.getLogId(), null, userInfo);
            } else {
                String msg = "Не выполнена замена данных в строке № " + deductionBeforeUpdate.getRowNum() +
                        " Раздела 3 в форме № " + declarationDataId + ", так как в форме редактирования не было внесено изменений.";
                logger.error(msg);
                sendNotification(msg, logger.getLogId(), userInfo.getUser().getId());
            }
            return new ActionResult(logEntryService.save(logger));
        } catch (Exception e) {
            String errorMessage = generateDeclarationEditErrorMsg(declarationDataId, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public ActionResult updateNdflPrepayment(Long declarationDataId, TAUserInfo userInfo, NdflPersonPrepaymentDTO personPrepaymentDTO) {
        try {
            Logger logger = logEntryService.createLogger();
            NdflPersonPrepayment prepayment = personPrepaymentDTO.toPrepayment();

            NdflPersonPrepayment prepaymentBeforeUpdate = ndflPersonDao.fetchOneNdflPersonPrepayment(prepayment.getId());
            NdflRowEditChangelogBuilder changelogBuilder = new NdflRowEditChangelogBuilder(prepaymentBeforeUpdate, prepayment);
            if (changelogBuilder.hasChanges()) {
                ndflPersonDao.updateOneNdflPrepayment(prepayment, userInfo);
                reportService.deleteDec(singletonList(declarationDataId), asList(DeclarationReportType.SPECIFIC_REPORT_DEC, DeclarationReportType.EXCEL_DEC));
                NdflPerson ndflPerson = ndflPersonDao.findById(personPrepaymentDTO.getNdflPersonId());
                Collections.sort(ndflPerson.getPrepayments(), NdflPersonPrepayment.getComparator(ndflPerson));
                ndflPersonDao.updatePrepayments(updateRowNum(ndflPerson.getPrepayments()));

                NdflPersonPrepayment prepaymentAfterUpdate = ndflPersonDao.fetchOneNdflPersonPrepayment(prepayment.getId());
                for (String message : changelogBuilder.build(declarationDataId, prepaymentBeforeUpdate.getRowNum(), prepaymentAfterUpdate.getRowNum())) {
                    logger.info(message);
                }
                logBusinessService.logFormEvent(declarationDataId, FormDataEvent.NDFL_EDIT, logger.getLogId(), null, userInfo);
            } else {
                String msg = "Не выполнена замена данных в строке № " + prepaymentBeforeUpdate.getRowNum() +
                        " Раздела 4 в форме № " + declarationDataId + ", так как в форме редактирования не было внесено изменений.";
                logger.error(msg);
                sendNotification(msg, logger.getLogId(), userInfo.getUser().getId());
            }
            return new ActionResult(logEntryService.save(logger));
        } catch (Exception e) {
            String errorMessage = generateDeclarationEditErrorMsg(declarationDataId, e.getMessage());
            throw new ServiceException(errorMessage, e);
        }
    }

    @Override
    public String createPdfFileName(Long declarationDataId, String blobDataFileName, TAUserInfo userInfo) {
        DeclarationData declarationData = get(declarationDataId, userInfo);
        DeclarationTemplate dt = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        switch (dt.getType().getId()) {
            case DeclarationType.NDFL_2_1:
            case DeclarationType.NDFL_2_2:
                return "Реестр_справок_" + declarationData.getId() + "_" + SDF_DD_MM_YYYY_HH_MM_SS.get().format(new Date()) + ".pdf";
            case DeclarationType.NDFL_6:
                return getXmlDataFileName(declarationData.getId(), userInfo).replace("zip", "pdf");
            case DeclarationType.NDFL_2_FL:
                return blobDataFileName;
            default:
                return "";
        }
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).UPDATE_PERSONS_DATA)")
    public String createUpdatePersonsDataTask(Long declarationDataId, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (ndflPersonService.getNdflPersonCount(declarationDataId) > 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("declarationDataId", declarationDataId);
            asyncManager.createTask(OperationType.UPDATE_PERSONS_DATA, userInfo, params, logger);
        }
        return logEntryService.save(logger.getEntries());
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).UPDATE_PERSONS_DATA)")
    public void performUpdatePersonsData(Long declarationDataId, Logger logger, TAUserInfo userInfo) {
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        Logger scriptLogger = new Logger();
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.UPDATE_PERSONS_DATA, scriptLogger, null);
        logger.getEntries().addAll(scriptLogger.getEntries());
        logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.UPDATE_PERSONS_DATA, logger.getLogId(), null, userInfo);
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public String uploadFile(InputStream fileInputStream, String fileName, Long declarationDataId) {
        return blobDataService.create(fileInputStream, fileName);
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataFile, T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataFilePermission).DOWNLOAD)")
    public BlobData downloadFile(DeclarationDataFile declarationDataFile) {
        return blobDataService.get(declarationDataFile.getUuid());
    }

    @Override
    public String getStandardDeclarationDescription(Long declarationDataId) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();

        return String.format(STANDARD_DECLARATION_DESCRIPTION,
                declarationType.getName(),
                declaration.getId(),
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(reportPeriod),
                department.getName());
    }

    @Override
    public String getFullDeclarationDescription(Long declarationDataId) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());

        return String.format("№: %d, Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"",
                declaration.getId(),
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(reportPeriod),
                department.getName(),
                declarationTemplate.getType().getName());
    }

    @Override
    public String createPdfTask(TAUserInfo userInfo, long declarationDataId) {
        final Logger logger = new Logger();

        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);
        params.put("extendedDescription", createExtendDescription(declarationDataId));
        asyncManager.createTask(OperationType.PDF_DEC, userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public ActionResult checkRowsEditCountParam(int count) {
        ActionResult result = new ActionResult();
        Logger logger = new Logger();

        ConfigurationParam checkedParam = ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT;

        Integer maxCount = configurationService.getParamIntValue(checkedParam);
        if (maxCount == null) {
            result.setSuccess(false);
            logger.error("В системе не установлен конфигурационный параметр \"%s\". Обратитесь к администратору.", checkedParam.getCaption());
        } else if (count > maxCount) {
            result.setSuccess(false);
            logger.error("Сохранение невозможно. Найдено или выбрано для изменения строк больше, чем разрешенное значение %d.", maxCount);
        } else {
            result.setSuccess(true);
        }

        String logsUuid = logEntryService.save(logger.getEntries());
        result.setUuid(logsUuid);
        return result;
    }

    @Override
    public void changeStateToIssued(Long declarationDataId) {
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_FL) {
            declarationDataDao.setStatus(declarationDataId, State.ISSUED);
        }
    }

    /**
     * Обновление номеров строк операций. Начало нумерации
     * начинается с минимального номера строки из {@link NdflPerson#getIncomes()} или 1
     *
     * @param operations упорядоченный список операций{@link List<NdflPersonOperation>}
     * @return упорядоченный список {@link List<NdflPersonOperation>} с номерами строк, идущими по возрастанию,
     * начиная с минимального номера строки во входном списке или с 1, если номера не были проинициализированы
     */
    <T extends NdflPersonOperation> List<T> updateRowNum(List<T> operations) {
        BigDecimal rowNum = NdflPersonOperation.getMinRowNum(operations);
        for (T operation : operations) {
            operation.setRowNum(rowNum);
            rowNum = rowNum != null ? rowNum.add(new BigDecimal("1")) : null;
        }
        return operations;
    }

    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ?
                String.format(" корр. %s", sdf.get().format(reportPeriod.getCorrectionDate())) :
                "";
    }

    private void makeNotificationForAccessDenied(Logger logger) {
        if (logger.getEntries().size() != 0) {
            makeErrorNotification(logger.getEntries().get(logger.getEntries().size() - 1).getMessage(), logger);
        }
    }

    private void makeNotificationForUnexpected(Throwable e, Logger logger, String operationName, Long declarationId) {
        makeErrorNotification(String.format(FAIL, operationName, getStandardDeclarationDescription(declarationId).concat(". Причина: ").concat(e.toString())), logger);
    }

    private void makeErrorNotification(String errorMessage, Logger logger) {
        Notification notification = new Notification();
        notification.setUserId(userService.getCurrentUser().getId());
        notification.setCreateDate(new Date());
        notification.setText(errorMessage);
        notification.setLogId(logEntryService.save(Collections.singletonList(logger.getEntries().get(logger.getEntries().size() - 1))));
        notification.setReportId(null);
        notification.setNotificationType(NotificationType.DEFAULT);
        notificationService.create(Collections.singletonList(notification));
    }

    private Notification createNotification(String text, Logger logger) {
        Notification notification = new Notification();
        notification.setUserId(userService.getCurrentUser().getId());
        notification.setCreateDate(new Date());
        notification.setText(text);
        notification.setLogId(logEntryService.save(logger));
        notification.setReportId(null);
        notification.setNotificationType(NotificationType.DEFAULT);
        notificationService.create(Collections.singletonList(notification));
        return notification;
    }

    private String createExtendDescription(Long declarationDataId) {
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        return String.format("№ %s, Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\",  Налоговый орган: \"%s\", КПП: \"%s\", ОКТМО: \"%s\"",
                declarationDataId,
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(departmentReportPeriod),
                department.getName(),
                declarationType.getName(),
                declarationData.getTaxOrganCode(),
                declarationData.getKpp(),
                declarationData.getOktmo());
    }

    /**
     * Текст сообщения об ошибке редактирования формы.
     */
    private String generateDeclarationEditErrorMsg(Long declarationId, String cause) {

        DeclarationData declarationData = declarationDataDao.get(declarationId);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        ReportPeriodType reportPeriodType = reportPeriodService.getPeriodTypeById(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

        return String.format("Не выполнена операция \"Редактирование\" для налоговой формы: № %d, Период: \"%s, %s%s\", Подразделение: \"%s\". Причина: %s",
                declarationId,
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriodType.getName(),
                formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                department.getName(),
                cause
        );
    }
}
