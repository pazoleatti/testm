package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFileComment;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataJournalItem;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateFile;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DescriptionTemplate;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder;
import com.aplana.sbrf.taxaccounting.model.ScriptTaskComplexityHolder;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaskInterruptCause;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationReportAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateReportAction;
import com.aplana.sbrf.taxaccounting.model.action.PrepareSubreportAction;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonOperation;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateDeclarationExcelTemplateResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateDeclarationReportResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateResult;
import com.aplana.sbrf.taxaccounting.model.result.DeclarationDataExistenceAndKindResult;
import com.aplana.sbrf.taxaccounting.model.result.DeclarationLockResult;
import com.aplana.sbrf.taxaccounting.model.result.DeclarationResult;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import com.aplana.sbrf.taxaccounting.model.result.PrepareSubreportResult;
import com.aplana.sbrf.taxaccounting.model.result.ReportAvailableReportDDResult;
import com.aplana.sbrf.taxaccounting.model.result.ReportAvailableResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.aplana.sbrf.taxaccounting.service.component.MoveToCreateFacade;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import com.aplana.sbrf.taxaccounting.utils.ZipUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import static java.util.Collections.singletonList;
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
    private static final String DECLARATION_DESCRIPTION = "№: %d, Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s";
    private static final String ACCESS_ERR_MSG_FMT = "Нет прав на доступ к налоговой форме. Проверьте назначение формы РНУ НДФЛ (первичная) для подразделения «%s» в «Назначении налоговых форм»%s.";

    private final static List<DeclarationDataReportType> reportTypes = Collections.unmodifiableList(Arrays.asList(DeclarationDataReportType.ACCEPT_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.XML_DEC, DeclarationDataReportType.IMPORT_TF_DEC, DeclarationDataReportType.DELETE_DEC));

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

    private static final Date MAX_DATE;
    private static final Calendar CALENDAR = Calendar.getInstance();

    private static final String STANDARD_DECLARATION_DESCRIPTION = "налоговой формы: Вид: \"%s\",  №: %d, Период: \"%s, %s%s\", Подразделение: \"%s\"";

    private static final String FAIL = "Не выполнена операция \"%s\" для %s.";

    static {
        CALENDAR.clear();
        CALENDAR.set(9999, Calendar.DECEMBER, 31);
        MAX_DATE = CALENDAR.getTime();
        CALENDAR.clear();
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
    private NdflPersonService ndflPersonService;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private TAUserService userService;
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

    private class SAXHandler extends DefaultHandler {
        private Map<String, String> values;
        private Map<String, String> tagAttrNames;

        SAXHandler(Map<String, String> tagAttrNames) {
            this.tagAttrNames = tagAttrNames;
        }


        public Map<String, String> getValues() {
            return values;
        }

        @Override
        public void startDocument() {
            values = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (tagAttrNames.containsKey(qName)) {
                values.put(qName, attributes.getValue(tagAttrNames.get(qName)));
            }
        }
    }

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
                Long declarationId = doCreate(newDeclarationData, departmentReportPeriod, logger, userInfo, true);
                result.setEntityId(declarationId);
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

    /**
     * Логика создания вынесена в отдельный метод, для решения проблем с транзакциями при вызове из других транзакционных методов
     */
    private Long doCreate(DeclarationData newDeclaration, DepartmentReportPeriod departmentReportPeriod, Logger logger, TAUserInfo userInfo, boolean writeAudit) {
        LOG.info(String.format("DeclarationDataServiceImpl.doCreate by %s. declarationTemplateId: %s; departmentReportPeriod: %s; taxOrganCode: %s; taxOrganKpp: %s; oktmo: %s; asunId: %s; fileName: %s; note: %s; writeAudit: %s; manuallyCreated: %s",
                userInfo, newDeclaration.getDeclarationTemplateId(), departmentReportPeriod, newDeclaration.getTaxOrganCode(), newDeclaration.getKpp(), newDeclaration.getOktmo(), newDeclaration.getAsnuId(), newDeclaration.getFileName(), newDeclaration.getNote(), writeAudit, newDeclaration.isManuallyCreated()));

        newDeclaration.setDepartmentReportPeriodId(departmentReportPeriod.getId());
        newDeclaration.setReportPeriodId(departmentReportPeriod.getReportPeriod().getId());
        newDeclaration.setDepartmentId(departmentReportPeriod.getDepartmentId());
        newDeclaration.setState(State.CREATED);

        String key = LockData.LockObjects.DECLARATION_CREATE.name() + "_" + newDeclaration.getDeclarationTemplateId() + "_" + departmentReportPeriod.getId() + "_" + newDeclaration.getKpp() + "_" + newDeclaration.getTaxOrganCode() + "_" + newDeclaration.getFileName();
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(newDeclaration.getDeclarationTemplateId());
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        if (lockDataService.lock(key, userInfo.getUser().getId(), makeDeclarationLockDescription(newDeclaration, declarationTemplate, departmentReportPeriod, department)) == null) {
            //Если блокировка успешно установлена
            try {
                canCreate(userInfo, newDeclaration.getDeclarationTemplateId(), departmentReportPeriod, newDeclaration.getAsnuId(), logger);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(("Налоговая форма не создана"), logEntryService.save(logger.getEntries()));
                }

                if (newDeclaration.isManuallyCreated() && declarationDataDao.existDeclarationData(newDeclaration)) {
                    String strCorrPeriod = "";
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
                    }
                    String message = "";
                    if (declarationTemplate.getDeclarationFormKind().getId() == DeclarationFormKind.PRIMARY.getId()) {
                        String asnu = refBookAsnuService.fetchByIds(Arrays.asList(newDeclaration.getAsnuId())).get(0).getName();
                        message = String.format("Налоговая форма с заданными параметрами: Период: \"%s\", Подразделение: \"%s\", " +
                                        " Вид налоговой формы: \"%s\", АСНУ: \"%s\" уже существует!",
                                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                                department.getName(), declarationTemplate.getDeclarationFormKind().getTitle(), asnu);
                    }
                    if (declarationTemplate.getDeclarationFormKind().getId() == DeclarationFormKind.CONSOLIDATED.getId()) {
                        message = String.format("Налоговая форма с заданными параметрами: Период: \"%s\", Подразделение: \"%s\", " +
                                        " Вид налоговой формы: \"%s\", Тип КНФ: \"%s\" уже существует!",
                                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                                department.getName(), declarationTemplate.getDeclarationFormKind().getName(), newDeclaration.getKnfType().getName());
                    }
                    logger.error(message);
                }

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
                long id = declarationDataDao.create(newDeclaration);

                logBusinessService.logFormEvent(id, FormDataEvent.CREATE, null, userInfo);
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

    private String makeDeclarationLockDescription(DeclarationData declarationData, DeclarationTemplate declarationTemplate, DepartmentReportPeriod departmentReportPeriod, Department department) {
        RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
        return String.format(DescriptionTemplate.DECLARATION.getText(),
                "Создание налоговой формы",
                departmentReportPeriod.getReportPeriod().getName() + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getCorrectionDate() != null
                        ? " с датой сдачи корректировки " + sdf.get().format(departmentReportPeriod.getCorrectionDate())
                        : "",
                department.getName(),
                declarationTemplate.getType().getName(),
                declarationData.getTaxOrganCode() != null
                        ? ", Налоговый орган: \"" + declarationData.getTaxOrganCode() + "\""
                        : "",
                declarationData.getKpp() != null
                        ? ", КПП: \"" + declarationData.getKpp() + "\""
                        : "",
                declarationData.getOktmo() != null
                        ? ", ОКТМО: \"" + declarationData.getOktmo() + "\""
                        : "",
                declarationData.getAsnuId() != null
                        ? ", Наименование АСНУ: \"" + asnuProvider.getRecordData(declarationData.getAsnuId()).get("NAME").getStringValue() + "\""
                        : "",
                declarationData.getFileName() != null
                        ? ", Имя файла: \"" + declarationData.getFileName() + "\""
                        : "");
    }

    /**
     * Проверка возможности создания пользователем формы из макета
     * TODO: вынести в пермишены
     */
    private void canCreate(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod, Long asnuId, Logger logger) {
        // Для начала проверяем, что в данном подразделении вообще можно
        // работать с декларациями данного вида
        if (!departmentReportPeriod.isActive()) {
            error("Выбранный период закрыт", logger);
        }
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
        int declarationTypeId = declarationTemplate.getType().getId();

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
    public Long create(DeclarationData newDeclaration, DepartmentReportPeriod departmentReportPeriod, Logger logger, TAUserInfo userInfo, boolean writeAudit) {
        return doCreate(newDeclaration, departmentReportPeriod, logger, userInfo, writeAudit);
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
            logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.SAVE, null, userInfo);
            auditService.add(FormDataEvent.CALCULATE, userInfo, declarationData, "Налоговая форма обновлена", null);
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

        logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.SAVE, null, userInfo);
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
            declarationDataScriptingService.executeScript(userInfo, dd, FormDataEvent.CHECK, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            if (departmentReportPeriodService.fetchOne(dd.getDepartmentReportPeriodId()).isActive()) {
                if (State.PREPARED.equals(dd.getState())) {
                    declarationDataDao.setStatus(id, State.CREATED);
                    logBusinessService.logFormEvent(id, FormDataEvent.MOVE_PREPARED_TO_CREATED, null, userInfo);
                }
            }
        } else {
            if (departmentReportPeriodService.fetchOne(dd.getDepartmentReportPeriodId()).isActive()) {
                if (State.CREATED.equals(dd.getState())) {
                    // Переводим в состояние подготовлено
                    declarationDataDao.setStatus(id, State.PREPARED);
                    logBusinessService.logFormEvent(id, FormDataEvent.MOVE_CREATED_TO_PREPARED, null, userInfo);
                }
            }
            logger.info("Проверка завершена, ошибок не обнаружено");
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
                    asyncManager.createTask(OperationType.IDENTIFY_PERSON, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("declarationDataId", declarationDataId);
                params.put("docDate", new Date());
                asyncManager.createTask(OperationType.CONSOLIDATE, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);
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
            result.setCreationUserName(logBusinessService.getFormCreationUserName(declaration.getId()));

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

            result.setCreationDate(logBusinessService.getFormCreationDate(declaration.getId()));
            result.setKpp(declaration.getKpp());
            result.setOktmo(declaration.getOktmo());
            result.setTaxOrganCode(declaration.getTaxOrganCode());
            result.setCorrectionNum(declaration.getCorrectionNum());
            if (declaration.getDocState() != null) {
                RefBookDataProvider stateEDProvider = refBookFactory.getDataProvider(RefBook.Id.DOC_STATE.getId());
                result.setDocState(stateEDProvider.getRecordData(declaration.getDocState()).get("NAME").getStringValue());
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
                    asyncManager.createTask(OperationType.CHECK_DEC, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);
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
    @Transactional
    @PreAuthorize("hasPermission(#dataFileComment.declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public DeclarationDataFileComment saveDeclarationFilesComment(TAUserInfo userInfo, DeclarationDataFileComment
            dataFileComment) {
        long declarationDataId = dataFileComment.getDeclarationDataId();

        DeclarationDataFileComment result = new DeclarationDataFileComment();
        if (!existDeclarationData(declarationDataId)) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationDataId), null);
        }

        Logger logger = new Logger();
        LockData lockData = lock(declarationDataId, userInfo);
        if (lockData != null && lockData.getUserId() == userInfo.getUser().getId()) {
            try {
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
            List<Relation> relationList = new ArrayList<Relation>();
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

    void setUpDeclarationFilter(DeclarationDataFilter filter, TAUserInfo userInfo) {
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

        asyncManager.createTask(OperationType.getOperationTypeBySubreport(alias), getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    @Override
    public CreateDeclarationReportResult createReportXlsx(final TAUserInfo userInfo, final long declarationDataId,
                                                          boolean force) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReportXlsx by %s. declarationDataId: %s; force: %s",
                userInfo, declarationDataId, force));
        final DeclarationDataReportType ddReportType = new DeclarationDataReportType(AsyncTaskType.EXCEL_DEC, null);
        CreateDeclarationReportResult result = new CreateDeclarationReportResult();
        if (!existDeclarationData(declarationDataId)) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(declarationDataId);
        } else {
            Logger logger = new Logger();
            final String keyTask = generateAsyncTaskKey(declarationDataId, ddReportType);
            Pair<Boolean, String> restartStatus = asyncManager.restartTask(keyTask, userInfo, force, logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setStatus(CreateAsyncTaskStatus.LOCKED);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setStatus(CreateAsyncTaskStatus.CREATE);
            } else {
                result.setStatus(CreateAsyncTaskStatus.CREATE);
                reportService.deleteByDeclarationAndType(declarationDataId, ddReportType);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("declarationDataId", declarationDataId);

                asyncManager.executeTask(keyTask, ddReportType.getReportType(), userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                    @Override
                    public LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(), getDeclarationFullName(declarationDataId, ddReportType));
                    }
                });
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public ReportAvailableResult checkAvailabilityReports(TAUserInfo userInfo, long declarationDataId) {
        ReportAvailableResult reportAvailableResult = new ReportAvailableResult();
        if (!existDeclarationData(declarationDataId)) {
            reportAvailableResult.setDeclarationDataExist(false);
        } else {
            reportAvailableResult.setReportAvailable(DeclarationDataReportType.EXCEL_DEC.getReportAlias(), reportService.getReportFileUuid(declarationDataId, DeclarationDataReportType.EXCEL_DEC) != null);
            reportAvailableResult.setReportAvailable(DeclarationDataReportType.XML_DEC.getReportAlias(), reportService.getReportFileUuid(declarationDataId, DeclarationDataReportType.XML_DEC) != null);
            reportAvailableResult.setReportAvailable(DeclarationDataReportType.EXCEL_TEMPLATE_DEC.getReportAlias(), reportService.getReportFileUuid(declarationDataId, DeclarationDataReportType.EXCEL_TEMPLATE_DEC) != null);

            DeclarationData declaration = get(declarationDataId, userInfo);
            List<DeclarationSubreport> subreports = declarationTemplateService.get(declaration.getDeclarationTemplateId()).getSubreports();
            for (DeclarationSubreport subreport : subreports) {
                reportAvailableResult.setReportAvailable(subreport.getAlias(), reportService.getReportFileUuid(declarationDataId, new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, subreport)) != null);
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
            result.setAvailablePdf(reportService.getReportFileUuid(declarationDataId, DeclarationDataReportType.PDF_DEC) != null);
            result.setDownloadXlsxAvailable(reportService.getReportFileUuid(declarationDataId, DeclarationDataReportType.EXCEL_DEC) != null);
            result.setDownloadXmlAvailable(reportService.getReportFileUuid(declarationDataId, DeclarationDataReportType.XML_DEC) != null);
        }
        return result;
    }

    @Override
    public void preCalculationCheck(Logger logger, long declarationDataId, TAUserInfo userInfo) {
        Logger localLogger = new Logger();
        declarationDataScriptingService.executeScript(userInfo,
                declarationDataDao.get(declarationDataId), FormDataEvent.PRE_CALCULATION_CHECK, localLogger, null);
        // Проверяем ошибки
        if (localLogger.containsLevel(LogLevel.ERROR)) {
            logger.getEntries().addAll(localLogger.getEntries());
            throw new ServiceLoggerException(
                    "Найдены ошибки при выполнении расчета налоговой формы",
                    logEntryService.save(logger.getEntries()));
        }
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
    @PreAuthorize("hasPermission(#id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).DELETE)")
    public void deleteSync(long id, TAUserInfo userInfo, boolean createLock) {
        LOG.info(String.format("DeclarationDataServiceImpl.deleteSync by %s. id: %s",
                userInfo, id));
        LockData lockData = lockDataService.findLock(generateAsyncTaskKey(id, DeclarationDataReportType.XML_DEC));
        LockData lockDataAccept = lockDataService.findLock(generateAsyncTaskKey(id, DeclarationDataReportType.ACCEPT_DEC));
        LockData lockDataCheck = lockDataService.findLock(generateAsyncTaskKey(id, DeclarationDataReportType.CHECK_DEC));
        LockData lockDataDelete = null;
        if (lockData == null && lockDataAccept == null && lockDataCheck == null && createLock) {
            lockDataDelete = lockDataService.lock(generateAsyncTaskKey(id, DeclarationDataReportType.DELETE_DEC), userInfo.getUser().getId(),
                    getDeclarationFullName(id, DeclarationDataReportType.DELETE_DEC));
        }
        if (lockData == null && lockDataAccept == null && lockDataCheck == null && lockDataDelete == null) {
            try {
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
                asyncManager.createTask(OperationType.DELETE_DEC, getStandardDeclarationDescription(declarationId), userInfo, params, logger);
            } else {
                makeNotificationForAccessDenied(logger);
            }
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
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CHECK, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            declarationData.setState(State.ACCEPTED);

            logBusinessService.logFormEvent(id, FormDataEvent.MOVE_PREPARED_TO_ACCEPTED, null, userInfo);
            auditService.add(FormDataEvent.MOVE_PREPARED_TO_ACCEPTED, userInfo, declarationData, FormDataEvent.MOVE_PREPARED_TO_ACCEPTED.getTitle(), null);

            lockStateLogger.updateState(AsyncTaskState.FORM_STATUS_CHANGE);

            declarationDataDao.setStatus(id, declarationData.getState());
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
                    asyncManager.createTask(OperationType.ACCEPT_DEC, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);
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
    @Transactional(noRollbackFor = {Throwable.class})
    public void cancelDeclarationList(List<Long> declarationDataIds, String note, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.cancelDeclarationList by %s. declarationDataIds: %s; note: %s",
                userInfo, declarationDataIds, note));

        List<Long> sortedDeclarationIds = sortDeclarationIdsByKnfThenPnf(declarationDataIds);

        for (Long declarationId : sortedDeclarationIds) {
            final Logger logger = new Logger();
            try {
                if (permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(),
                        new TargetIdAndLogger(declarationId, logger),
                        "com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger", DeclarationDataPermission.RETURN_TO_CREATED)) {
                    String declarationFullName = getDeclarationFullName(declarationId, DeclarationDataReportType.TO_CREATE_DEC);
                    LockData lockData = lockDataService.lock(generateAsyncTaskKey(declarationId, DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId(), declarationFullName);
                    if (lockData == null) {
                        LOG.info(String.format("DeclarationDataServiceImpl.cancel by %s. id: %s; note: %s",
                                userInfo, declarationId, note));
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

                        declarationData.setState(State.CREATED);
                        declarationDataDao.setStatus(declarationId, declarationData.getState());

                        logBusinessService.logFormEvent(declarationId, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, note, userInfo);
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
                        sendNotification(message, logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                        logger.clear();
                    } else {
                        DeclarationData declaration = get(declarationId, userInfo);
                        Department department = departmentService.getDepartment(declaration.getDepartmentId());
                        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
                        logger.error("Форма \"%s\" из \"%s\" заблокирована", declarationTemplate.getType().getName(), department.getName());
                    }
                } else {
                    makeNotificationForAccessDenied(logger);
                }
            } catch (Throwable e) {
                String errorMessage = String.format(FAIL, "Возврат в Создана", getStandardDeclarationDescription(declarationId).concat(". Причина: ").concat(e.toString()));
                logger.error(errorMessage);
                sendNotification(errorMessage, logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                LOG.error(e.getMessage(), e);
            } finally {
                lockDataService.unlock(generateAsyncTaskKey(declarationId, DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId());
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


    @Override
    @PreAuthorize("hasPermission(#declarationId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).VIEW)")
    public InputStream getXmlDataAsStream(long declarationId, TAUserInfo userInfo) {
        String xmlUuid = reportService.getReportFileUuidSafe(declarationId, DeclarationDataReportType.XML_DEC);
        if (xmlUuid == null) {
            return null;
        }
        return blobDataService.get(xmlUuid).getInputStream();
    }

    @Override
    public String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo) {
        String xmlUuid = reportService.getReportFileUuidSafe(declarationDataId, DeclarationDataReportType.XML_DEC);
        if (xmlUuid != null) {
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getName();
        }
        return null;
    }

    @Override
    public Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
        String xmlUuid = reportService.getReportFileUuidSafe(declarationDataId, DeclarationDataReportType.XML_DEC);
        if (xmlUuid != null) {
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getCreationDate();
        }
        return null;
    }

    private void getXlsxData(long id, File xlsxFile, TAUserInfo userInfo, LockStateLogger stateLogger) {
        DeclarationData declarationData = declarationDataDao.get(id);
        String uuid = reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationDataReportType.JASPER_DEC);
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
        String pdfUuid = reportService.getReportFileUuidSafe(declarationId, DeclarationDataReportType.PDF_DEC);
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
        String xmlUuid = reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationDataReportType.XML_DEC);
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
        String xmlUuid = reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationDataReportType.XML_DEC);
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
                String fileName = createPdfFileName(declarationData.getId(), userInfo);
                reportService.attachReportToDeclaration(declarationData.getId(), blobDataService.create(pdfFile.getPath(), fileName), DeclarationDataReportType.PDF_DEC);

                // не сохраняем jasper-отчет, если есть XLSX-отчет
                if (reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationDataReportType.EXCEL_DEC) == null) {
                    LOG.info(String.format("Сохранение Jasper-макета в базе данных для налоговой формы %s", declarationData.getId()));
                    stateLogger.updateState(AsyncTaskState.SAVING_JASPER);
                    reportService.attachReportToDeclaration(declarationData.getId(), saveJPBlobData(jasperPrint), DeclarationDataReportType.JASPER_DEC);
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
    public String createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, Map<String, String> viewParamValues, DataRow<Cell> selectedRecord, TAUserInfo userInfo, LockStateLogger stateLogger) {
        LOG.info(String.format("DeclarationDataServiceImpl.createSpecificReport by %s. declarationData: %s; ddReportType: %s; subreportParamValues: %s; viewParamValues: %s; selectedRecord: %s",
                userInfo, declarationData, ddReportType, subreportParamValues, viewParamValues, selectedRecord));
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

    private boolean createReportFormXml(Logger logger,
                                        DeclarationData declarationData, Date docDate, TAUserInfo userInfo, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        if (exchangeParams == null) {
            exchangeParams = new HashMap<>();
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
                    fileWriter = new OutputStreamWriter(new FileOutputStream(xmlFile), Charset.forName("windows-1251"));
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
                String fileName = handler.getValues().get(TAG_FILE);
                Date decDate = getFormattedDate(handler.getValues().get(TAG_DOCUMENT));
                if (decDate == null) {
                    decDate = docDate;
                }

                //Архивирование перед сохраннеием в базу
                File zipOutFile = null;
                try {
                    zipOutFile = ZipUtils.archive(xmlFile, fileName + ".xml");

                    LOG.info(String.format("Сохранение XML-файла в базе данных для налоговой формы %s", declarationData.getId()));
                    stateLogger.updateState(AsyncTaskState.SAVING_XML);

                    reportService.deleteByDeclarationAndType(declarationData.getId(), DeclarationDataReportType.XML_DEC);

                    String fileUuid = blobDataService.create(zipOutFile, fileName + ".zip", decDate);
                    reportService.attachReportToDeclaration(declarationData.getId(), fileUuid, DeclarationDataReportType.XML_DEC);
                    declarationDataDao.setFileName(declarationData.getId(), fileName);
                } finally {
                    deleteTempFile(zipOutFile);
                }
            }

            exchangeParams.put(DeclarationDataScriptParams.XML, null);
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.AFTER_CALCULATE, logger, exchangeParams);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceException();
            }
        } catch (IOException | SAXException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        } catch (ParserConfigurationException e) {
            LOG.error("Ошибка при парсинге xml", e);
            throw new ServiceException("", e);
        } finally {
            deleteTempFile(xmlFile);
        }
        if (params.get(DeclarationDataScriptParams.CREATE_FORM) != null) {
            return (Boolean) params.get(DeclarationDataScriptParams.CREATE_FORM);
        }
        return true;
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
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

            xsdBlobDataId = declarationTemplate.getXsdId();
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
    @Transactional
    public LockData lock(long declarationDataId, TAUserInfo userInfo) {
        LockData lockData = doLock(declarationDataId, userInfo);
        checkLock(lockData, userInfo.getUser());
        return lockData;
    }

    private LockData doLock(long declarationDataId, TAUserInfo userInfo) {
        return lockDataService.lock(generateAsyncTaskKey(declarationDataId, null), userInfo.getUser().getId(),
                getDeclarationFullName(declarationDataId, null));
    }

    private LockData getLock(long declarationDataId) {
        return lockDataService.findLock(generateAsyncTaskKey(declarationDataId, null));
    }

    @Override
    public DeclarationLockResult createLock(long declarationDataId, TAUserInfo userInfo) {
        DeclarationLockResult lockResult = new DeclarationLockResult();

        LockData lockData = lockDataService.lock(generateAsyncTaskKey(declarationDataId, null), userInfo.getUser().getId(), getDeclarationFullName(declarationDataId, null));
        if (lockData == null || lockData.getUserId() == userInfo.getUser().getId()) {
            lockResult.setDeclarationDataLocked(true);
        } else {
            TAUser lockOwner = taUserService.getUser(lockData.getUserId());
            Logger logger = new Logger();
            //TODO: (dloshkarev) тут не должно быть какой то бизнес-логики, метод должен только устанавливать блокировку (как и следует из названия)
            logger.info("Прикрепление файлов и редактирование комментариев недоступно, так как файлы и комментарии данного экземпляра налоговой формы " +
                    "в текущий момент редактируются пользователем \"%s\" (с %s)", lockOwner.getName(), SDF_DD_MM_YYYY_HH_MM_SS.get().format(lockData.getDateLock()));
            lockResult.setUuid(logEntryService.save(logger.getEntries()));
            lockResult.setDeclarationDataLocked(false);
        }

        return lockResult;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void unlock(final long declarationDataId, final TAUserInfo userInfo) {
        lockDataService.unlock(generateAsyncTaskKey(declarationDataId, null), userInfo.getUser().getId());
    }

    private void checkLock(LockData lockData, TAUser user) {
        if (lockData != null && lockData.getUserId() != user.getId()) {
            TAUser lockUser = taUserService.getUser(lockData.getUserId());
            throw new ServiceException(String.format(LockDataService.LOCK_DATA, lockUser.getName(), lockUser.getId()));
        }
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
        boolean exist = false;
        if (ddReportTypes != null) {
            DeclarationData declarationData = declarationDataDao.get(declarationDataId);
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
        }
        return exist;
    }

    private boolean checkExistTasks(long declarationDataId, DeclarationDataReportType ddReportType, Logger logger) {
        LockData lock = lockDataService.findLock(generateAsyncTaskKey(declarationDataId, ddReportType));
        if (lock != null) {
            AsyncTaskData taskData = asyncTaskDao.findByIdLight(lock.getTaskId());
            if (taskData != null) {
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
        }
        return false;
    }

    @Override
    public void interruptAsyncTask(long declarationDataId, TAUserInfo userInfo, AsyncTaskType reportType, TaskInterruptCause cause) {
        LOG.info(String.format("DeclarationDataServiceImpl.interruptAsyncTask by %s. declarationData: %s; reportType: %s; cause: %s",
                userInfo, declarationDataId, reportType, cause));
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
                LockData lock = lockDataService.findLock(key);
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
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
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
            case UPDATE_PERSONS_DATA:
                return String.format(ddReportType.getReportType().getDescription(),
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
    public Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType reportType) {
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        switch (reportType.getReportType()) {
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
                    Logger logger = new Logger();
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
            case DEPT_NOTICE_DEC:
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

    private void checkSources(DeclarationData dd, Logger logger) {
        boolean consolidationOk = true;
        //Проверка на неактуальные консолидированные данные  3А
        if (!sourceService.isDDConsolidationTopical(dd.getId())) {
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(dd.getDeclarationTemplateId());
            boolean isReports = DeclarationFormKind.REPORTS.equals(declarationTemplate.getDeclarationFormKind());
            logger.error(CALCULATION_NOT_TOPICAL + (isReports ? "" : CALCULATION_NOT_TOPICAL_SUFFIX));
            consolidationOk = false;
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
    public boolean isVisiblePDF(DeclarationData declarationData, TAUserInfo userInfo) {
        Map<String, Object> exchangeParams = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
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

    @Override
    public List<DeclarationDataFile> getFiles(long formDataId) {
        return declarationDataFileDao.fetchByDeclarationDataId(formDataId);
    }

    @Override
    public String getNote(long declarationDataId) {
        return declarationDataDao.getNote(declarationDataId);
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).PermissionEvaluatorVIEW)")
    public void saveFilesComments(long declarationDataId, String note, List<DeclarationDataFile> files) {
        declarationDataDao.updateNote(declarationDataId, note);
        declarationDataFileDao.createOrUpdateList(declarationDataId, files);
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
    @Transactional
    public void createReportForms(Long knfId, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, boolean adjustNegativeValues, LockStateLogger stateLogger, Logger logger, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReportForms by %s. departmentReportPeriod: %s; declarationTypeId: %s",
                userInfo, departmentReportPeriod, declarationTypeId));
        Map<String, Object> additionalParameters = new HashMap<>();
        Map<Long, Map<String, Object>> formMap = new HashMap<>();
        additionalParameters.put("formMap", formMap);
        Map<String, Object> scriptParams = new HashMap<>();
        additionalParameters.put("scriptParams", scriptParams);
        additionalParameters.put("knfId", knfId);
        additionalParameters.put("adjustNegativeValues", adjustNegativeValues);
        DeclarationData declarationDataTemp = new DeclarationData();
        declarationDataTemp.setDeclarationTemplateId(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));
        declarationDataTemp.setDepartmentReportPeriodId(departmentReportPeriod.getId());
        declarationDataScriptingService.executeScript(userInfo, declarationDataTemp, FormDataEvent.CREATE_FORMS, logger, additionalParameters);

        int success = 0;
        int pairKppOktomoTotal = (Integer) scriptParams.get("pairKppOktmoTotal");
        Long sourceFormId = (Long) scriptParams.get("sourceFormId");
        for (Map.Entry<Long, Map<String, Object>> entry : formMap.entrySet()) {
            Long reportFormCreatedId = entry.getKey();
            DeclarationData createdReportForm = declarationDataDao.get(reportFormCreatedId);
            Logger scriptLogger = new Logger();
            boolean createForm = true;
            try {
                Map<String, Object> exchangeParams = entry.getValue();
                exchangeParams.put("adjustNegativeValues", adjustNegativeValues);
                createForm = createReportFormXml(scriptLogger, createdReportForm, new Date(), userInfo, exchangeParams, stateLogger);
            } catch (Exception e) {
                createForm = false;
                if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                    scriptLogger.warn(e.getMessage());
                }
            } finally {
                if (!createForm) {
                    declarationDataDao.delete(reportFormCreatedId);
                } else {
                    success++;
                    DeclarationData declaration = declarationDataDao.get(reportFormCreatedId);
                    // Добавление информации о источнике созданной отчетной формы.
                    sourceService.deleteDeclarationConsolidateInfo(reportFormCreatedId);
                    sourceService.addDeclarationConsolidationInfo(reportFormCreatedId, singletonList(sourceFormId));

                    auditService.add(FormDataEvent.CREATE, userInfo, declaration, "Налоговая форма создана", null);
                    String message = getDeclarationFullName(reportFormCreatedId, null);
                    logger.info("Успешно выполнено создание " + message.replace("Налоговая форма", "налоговой формы"));
                }
                logger.getEntries().addAll(scriptLogger.getEntries());
            }
        }
        logger.info("Количество успешно созданных форм: %d. Не удалось создать форм: %d.", success, pairKppOktomoTotal - success);
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

    @Override
    public Map<DeclarationDataReportType, LockData> getLockTaskType(long declarationDataId) {
        Map<DeclarationDataReportType, LockData> result = new HashMap<DeclarationDataReportType, LockData>();
        for (DeclarationDataReportType reportType : reportTypes) {
            LockData lockData = lockDataService.findLock(generateAsyncTaskKey(declarationDataId, reportType));
            if (lockData != null) {
                result.put(reportType, lockData);
            }
        }
        return result;
    }

    @Override
    public ActionResult createReportsCreateTask(CreateDeclarationReportAction action, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReportsCreateTask by %s. action: %s", userInfo, action));
        Logger logger = new Logger();
        ActionResult taskResult = new ActionResult();

        DeclarationData declarationData = findConsolidatedDeclarationForReport(action, logger);
        if (!logger.containsLevel(LogLevel.ERROR)) {
            final Map<String, Object> params = generateTaskParams(action, declarationData);
            asyncManager.createTask(OperationType.getOperationByDeclarationTypeId(action.getDeclarationTypeId()),
                    getStandardDeclarationDescription(declarationData.getId()),
                    userInfo, params, logger);

        }
        taskResult.setUuid(logEntryService.save(logger.getEntries()));
        return taskResult;
    }

    private DeclarationData findConsolidatedDeclarationForReport(CreateDeclarationReportAction action, Logger logger) {
        DeclarationData consolidatedDeclaration;
        if (action.getKnfId() != null) {
            consolidatedDeclaration = declarationDataDao.get(action.getKnfId());
            if (consolidatedDeclaration.getState() != State.ACCEPTED) {
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(consolidatedDeclaration.getDepartmentReportPeriodId());
                DeclarationType declarationType = declarationTypeDao.get(action.getDeclarationTypeId());
                Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                logger.error("Отчетность %s для %s за период %s, %s%s" +
                                " не сформирована. Для указанного подразделения и периода форма РНУ НДФЛ (консолидированная) № %s должна быть в состоянии \"Принята\". Примите форму и повторите операцию",
                        declarationType.getName(),
                        department.getName(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        departmentReportPeriod.getReportPeriod().getName(),
                        formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                        consolidatedDeclaration.getId());
            }
        } else {
            RefBookKnfType knfType = action.getDeclarationTypeId() == DeclarationType.NDFL_2_2 ? RefBookKnfType.BY_NONHOLDING_TAX : RefBookKnfType.ALL;
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchLast(action.getDepartmentId(), action.getPeriodId());
            consolidatedDeclaration = declarationDataDao.findKnfByKnfTypeAndPeriodId(knfType, departmentReportPeriod.getId());
            if (consolidatedDeclaration == null || consolidatedDeclaration.getState() != State.ACCEPTED) {
                DeclarationType declarationType = declarationTypeDao.get(action.getDeclarationTypeId());
                Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                if (consolidatedDeclaration == null) {
                    logger.error("Отчетность %s для %s за период %s, %s%s" +
                                    " не сформирована. Для указанного подразделения и периода не найдена форма РНУ НДФЛ (консолидированная).",
                            declarationType.getName(),
                            department.getName(),
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            departmentReportPeriod.getReportPeriod().getName(),
                            formatCorrectionDate(departmentReportPeriod.getCorrectionDate()));
                } else if (consolidatedDeclaration.getState() != State.ACCEPTED) {
                    logger.error("Отчетность %s для %s за период %s, %s%s" +
                                    " не сформирована. Для указанного подразделения и периода форма РНУ НДФЛ (консолидированная) № %s должна быть в состоянии \"Принята\". Примите форму и повторите операцию",
                            declarationType.getName(),
                            department.getName(),
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            departmentReportPeriod.getReportPeriod().getName(),
                            formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                            consolidatedDeclaration.getId());
                }
                return null;
            }
        }
        return consolidatedDeclaration;
    }

    private Map<String, Object> generateTaskParams(CreateDeclarationReportAction action, DeclarationData declarationData) {
        Map<String, Object> params = new HashMap<>();

        params.put("declarationTypeId", action.getDeclarationTypeId());
        params.put("adjustNegativeValues", action.isAdjustNegativeValues());
        params.put("declarationDataId", declarationData.getId());
        params.put("departmentReportPeriodId", declarationData.getDepartmentReportPeriodId());

        return params;
    }

    private void sendNotification(String msg, String uuid, Integer userId, NotificationType notificationType, String reportId) {
        if (msg != null && !msg.isEmpty()) {
            List<Notification> notifications = new ArrayList<>();
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setCreateDate(new Date());
            notification.setText(msg);
            notification.setLogId(uuid);
            notification.setReportId(reportId);
            notification.setNotificationType(notificationType);
            notifications.add(notification);
            notificationService.create(notifications);
        }
    }

    @Override
    public ActionResult asyncExportReports(DeclarationDataFilter filter, TAUserInfo userInfo) {
        List<Long> declarationDataIdList = declarationDataDao.findAllIdsByFilter(filter);
        return asyncExportReports(declarationDataIdList, userInfo);
    }

    @Override
    public ActionResult asyncExportReports(List<Long> declarationDataIds, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (declarationDataIds.isEmpty()) {
            logger.error("По заданым параметрам не найдено ни одной формы");
        } else {
            String taskKey = AsyncTaskType.EXPORT_REPORTS.name() + System.currentTimeMillis();
            Map<String, Object> params = new HashMap<>();
            params.put("declarationDataIds", declarationDataIds);
            asyncManager.createTask(OperationType.EXPORT_REPORTS, "Выгрузка отчетности", userInfo, params, logger);
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
            String taskKey = AsyncTaskType.UPDATE_DOC_STATE.name() + System.currentTimeMillis();
            Map<String, Object> params = new HashMap<>();
            params.put("declarationDataIds", declarationDataIds);
            params.put("docStateId", docStateId);
            asyncManager.executeTask(taskKey, AsyncTaskType.UPDATE_DOC_STATE, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                protected LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user) {
                    return lockDataService.lockAsync(lockKey, user.getUser().getId());
                }
            });
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

                declarationDataDao.updateDocState(declaration.getId(), docStateId);

                logger.info("%s для отчетной  налоговой формы: № %s, Период: \"%s, %s%s\", Подразделение: \"%s\" завершено. " +
                                "Установлено \"Состояние ЭД\": %s.",
                        AsyncTaskType.UPDATE_DOC_STATE.getDescription(),
                        declaration.getId(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriodType.getName(),
                        formatCorrectionDate(departmentReportPeriod.getCorrectionDate()),
                        department.getName(),
                        docState.getName());
            } else {
                logger.getEntries().addAll(localLogger.getEntries());
            }
        }
    }

    @Override
    public ActionResult createReportForReportDD(TAUserInfo userInfo, final CreateReportAction action) {
        LOG.info(String.format("DeclarationDataServiceImpl.createReportForReportDD by %s. action: %s",
                userInfo, action));
        ActionResult result = new ActionResult();
        DeclarationData declaration = get(action.getDeclarationDataId(), userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String alias = "";

        if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_1) {
            alias = SubreportAliasConstants.REPORT_2NDFL1;
        } else if (declarationTemplate.getType().getId() == DeclarationType.NDFL_2_2) {
            if (action.getType().equalsIgnoreCase(SubreportAliasConstants.DEPT_NOTICE_DEC)) {
                alias = SubreportAliasConstants.DEPT_NOTICE_DEC;
            } else {
                alias = SubreportAliasConstants.REPORT_2NDFL2;
            }
        }

        Logger logger = new Logger();

        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", action.getDeclarationDataId());

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
        asyncManager.createTask(OperationType.getOperationTypeBySubreport(alias), getStandardDeclarationDescription(action.getDeclarationDataId()), userInfo, params, logger);
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
                if (action.getType().equalsIgnoreCase(SubreportAliasConstants.DEPT_NOTICE_DEC)) {
                    alias = action.getType();
                } else {
                    alias = SubreportAliasConstants.REPORT_2NDFL2;
                }
            }
            DeclarationDataReportType ddReportType = DeclarationDataReportType.SPECIFIC_REPORT_DEC;
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
    public CreateDeclarationExcelTemplateResult createTaskToCreateExcelTemplate(final long declarationDataId, TAUserInfo userInfo, boolean force) {
        LOG.info(String.format("DeclarationDataServiceImpl.createTaskToCreateExcelTemplate by %s. declarationDataId: %s; force: %s",
                userInfo, declarationDataId, force));
        final CreateDeclarationExcelTemplateResult result = new CreateDeclarationExcelTemplateResult();
        final TAUser user = userInfo.getUser();
        Logger logger = new Logger();

        String asyncLockKey = LockData.LockObjects.EXCEL_TEMPLATE_DECLARATION.name() + "_" + declarationDataId;
        Pair<Boolean, String> restartStatus = asyncManager.restartTask(asyncLockKey, userInfo, force, logger);
        if (restartStatus != null && restartStatus.getFirst()) {
            result.setStatus(CreateAsyncTaskStatus.LOCKED);
            result.setRestartMsg(restartStatus.getSecond());
        } else if (restartStatus != null && !restartStatus.getFirst()) {
            result.setStatus(CreateAsyncTaskStatus.CREATE);
            // в логгере будет что задача запущена и вы добавлены в список получателей оповещения
        } else {
            result.setStatus(CreateAsyncTaskStatus.CREATE);

            Map<String, Object> params = new HashMap<>();
            params.put("declarationDataId", declarationDataId);
            asyncManager.executeTask(asyncLockKey, AsyncTaskType.EXCEL_TEMPLATE_DEC, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, user.getId());
                }
            });
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
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
            if (reportFile != null)
                reportFile.delete();
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).IMPORT_EXCEL)")
    public ActionResult createTaskToImportExcel(final long declarationDataId, String fileName, InputStream inputStream, TAUserInfo userInfo) {
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
        asyncManager.createTask(OperationType.IMPORT_DECLARATION_EXCEL, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);
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
                String note = "Загрузка данных из файла \"" + blobData.getName() + "\" в налоговую форму";
                int ndflPersonCount = ndflPersonDao.getNdflPersonCount(declarationDataId);
                if (ndflPersonCount == 0) {
                    auditService.add(FormDataEvent.IMPORT, userInfo, declarationData, note, null);
                    logBusinessService.logFormEvent(declarationDataId, FormDataEvent.IMPORT, note, userInfo);
                } else {
                    auditService.add(FormDataEvent.DATA_MODIFYING, userInfo, declarationData, note, null);
                    logBusinessService.logFormEvent(declarationDataId, FormDataEvent.DATA_MODIFYING, note, userInfo);
                }
                declarationDataDao.updateLastDataModified(declarationDataId);
            }


        } finally {
            unlock(declarationDataId, userInfo);
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
    public void updateNdflIncomesAndTax(Long declarationDataId, TAUserInfo taUserInfo, NdflPersonIncomeDTO personIncome) {
        ndflPersonDao.updateOneNdflIncome(personIncome, taUserInfo);
        reportService.deleteDec(singletonList(declarationDataId),
                Arrays.asList(DeclarationDataReportType.SPECIFIC_REPORT_DEC, DeclarationDataReportType.EXCEL_DEC));
        NdflPerson ndflPerson = ndflPersonDao.fetchOne(personIncome.getNdflPersonId());
        Collections.sort(ndflPerson.getIncomes(), NdflPersonIncome.getComparator(ndflPerson));
        ndflPersonDao.updateIncomes(updateRowNum(ndflPerson.getIncomes()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public void updateNdflDeduction(Long declarationDataId, TAUserInfo taUserInfo, NdflPersonDeductionDTO personDeduction) {
        ndflPersonDao.updateOneNdflDeduction(personDeduction, taUserInfo);
        reportService.deleteDec(singletonList(declarationDataId),
                Arrays.asList(DeclarationDataReportType.SPECIFIC_REPORT_DEC, DeclarationDataReportType.EXCEL_DEC));
        NdflPerson ndflPerson = ndflPersonDao.fetchOne(personDeduction.getNdflPersonId());
        Collections.sort(ndflPerson.getDeductions(), NdflPersonDeduction.getComparator(ndflPerson));
        ndflPersonDao.updateDeductions(updateRowNum(ndflPerson.getDeductions()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).EDIT)")
    public void updateNdflPrepayment(Long declarationDataId, TAUserInfo taUserInfo, NdflPersonPrepaymentDTO personPrepayment) {
        ndflPersonDao.updateOneNdflPrepayment(personPrepayment, taUserInfo);
        reportService.deleteDec(singletonList(declarationDataId),
                Arrays.asList(DeclarationDataReportType.SPECIFIC_REPORT_DEC, DeclarationDataReportType.EXCEL_DEC));
        NdflPerson ndflPerson = ndflPersonDao.fetchOne(personPrepayment.getNdflPersonId());
        Collections.sort(ndflPerson.getPrepayments(), NdflPersonPrepayment.getComparator(ndflPerson));
        ndflPersonDao.updatePrepayments(updateRowNum(ndflPerson.getPrepayments()));
    }


    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ?
                String.format(" корр. %s", sdf.get().format(reportPeriod.getCorrectionDate())) :
                "";
    }

    private String getAdditionalString(DeclarationData declarationData) {
        if (declarationData.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            String asnuName = asnuProvider.getRecordData(declarationData.getAsnuId()).get("NAME").getStringValue();

            return String.format(", АСНУ: \"%s\"", asnuName);
        }
        return "";
    }

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     */
    private void deleteReport(long declarationDataId, TAUserInfo userInfo, boolean isCalc, TaskInterruptCause cause) {
        DeclarationDataReportType[] ddReportTypes = {DeclarationDataReportType.XML_DEC, DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC};
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                DeclarationData declarationData = declarationDataDao.get(declarationDataId);
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for (DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    LockData lock = lockDataService.findLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                    if (lock != null) {
                        asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                    }
                }
            } else if (!isCalc || !DeclarationDataReportType.XML_DEC.equals(ddReportType)) {
                LockData lock = lockDataService.findLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                if (lock != null) {
                    asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                }
            }
        }
        reportService.deleteAllByDeclarationId(declarationDataId);
    }

    public String createPdfFileName(Long declarationDataId, TAUserInfo userInfo) {
        DeclarationData declarationData = get(declarationDataId, userInfo);
        DeclarationTemplate dt = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        switch (dt.getType().getId()) {
            case DeclarationType.NDFL_2_1:
            case DeclarationType.NDFL_2_2:
                return new StringBuilder("Реестр_справок_").append(declarationData.getId()).append("_").append(SDF_DD_MM_YYYY_HH_MM_SS.get().format(new Date())).append(".pdf").toString();
            case DeclarationType.NDFL_6:
                return getXmlDataFileName(declarationData.getId(), userInfo).replace("zip", "pdf");
            default:
                return "";
        }
    }

    @Override
    @PreAuthorize("hasPermission(#declarationDataId, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).UPDATE_PERSONS_DATA)")
    public String createUpdatePersonsDataTask(Long declarationDataId, TAUserInfo userInfo) {
        Logger logger = new Logger();
        if (ndflPersonService.getNdflPersonCount(declarationDataId) > 0) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("declarationDataId", declarationDataId);
            asyncManager.createTask(OperationType.UPDATE_PERSONS_DATA, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);
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
    }

    public String createDocReportByPerson(DeclarationData declarationData, DataRow<Cell> selectedPerson, TAUserInfo userInfo, Logger logger) {
        try (InputStream inputStream = declarationTemplateDao.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.DEPT_NOTICE_DOC_TEMPLATE)) {
            Map<String, Object> params = new HashMap<>();
            params.put("selectedPerson", selectedPerson);
            params.put("templateInputStream", inputStream);
            BlobData blobDataOut = new BlobData();
            params.put("blobDataOut", blobDataOut);

            if (!declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.BUILD_DOC, logger, params)) {
                throw new ServiceException();
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceException();
            }
            return blobDataService.create(blobDataOut.getInputStream(), blobDataOut.getName());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
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
    public String createPdfTask(TAUserInfo userInfo, long declarationDataId) {
        final Logger logger = new Logger();

        Map<String, Object> params = new HashMap<>();
        params.put("declarationDataId", declarationDataId);
        asyncManager.createTask(OperationType.PDF_DEC, getStandardDeclarationDescription(declarationDataId), userInfo, params, logger);

        return logEntryService.save(logger.getEntries());
    }

    /**
     * Проверяет необходимо ли использовать xml формы для проведения операций с ней.
     *
     * @param userInfo          информация о пользователе
     * @param declarationDataId идентификатор налоговой формы
     * @return true если данные формы для операции берутся из xml
     */
    private boolean isXmlRequired(TAUserInfo userInfo, long declarationDataId) {
        DeclarationData declarationData = get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        return declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.REPORTS);
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

    private void makeNotificationForAccessDenied(Logger logger) {
        if (logger.getEntries().size() != 0) {
            Notification notification = new Notification();
            notification.setUserId(userService.getCurrentUser().getId());
            notification.setCreateDate(new Date());
            notification.setText(logger.getEntries().get(logger.getEntries().size() - 1).getMessage());
            notification.setLogId(logEntryService.save(Collections.singletonList(logger.getEntries().get(logger.getEntries().size() - 1))));
            notification.setReportId(null);
            notification.setNotificationType(NotificationType.DEFAULT);
            notificationService.create(Collections.singletonList(notification));
        }
    }

    private void makeNotificationForUnexpected(Throwable e, Logger logger, String operationName, Long declarationId) {
        Notification notification = new Notification();
        notification.setUserId(userService.getCurrentUser().getId());
        notification.setCreateDate(new Date());
        notification.setText(String.format(FAIL, operationName, getStandardDeclarationDescription(declarationId).concat(". Причина: ").concat(e.toString())));
        notification.setLogId(logEntryService.save(Collections.singletonList(logger.getEntries().get(logger.getEntries().size() - 1))));
        notification.setReportId(null);
        notification.setNotificationType(NotificationType.DEFAULT);
        notificationService.create(Collections.singletonList(notification));
    }
}
