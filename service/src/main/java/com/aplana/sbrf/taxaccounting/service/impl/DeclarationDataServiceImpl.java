package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
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

    protected static final Log log = LogFactory.getLog(DeclarationDataService.class);

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>";
    private static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat SDF_DD_MM_YYYY_HH_MM_SS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final String FILE_NAME_IN_TEMP_PATTERN = System.getProperty("java.io.tmpdir")+ File.separator +"%s.%s";

    private static final String CALCULATION_NOT_TOPICAL = "Декларация / Уведомление содержит неактуальные консолидированные данные  " +
            "(расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена " +
            "консолидация). Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\"";

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
    private IfrsDataService ifrsDataService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private PeriodService reportPeriodService;

    @Autowired
    private ValidateXMLService validateXMLService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

    private static final String DD_NOT_IN_RANGE = "Найдена форма: \"%s\", \"%d\", \"%s\", \"%s\", состояние - \"%s\"";

    public static final String TAG_FILE = "Файл";
	public static final String TAG_DOCUMENT = "Документ";
	public static final String ATTR_FILE_ID = "ИдФайл";
	public static final String ATTR_DOC_DATE = "ДатаДок";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private static final String VALIDATION_ERR_MSG = "Обнаружены фатальные ошибки!";
    public static final String MSG_IS_EXIST_DECLARATION =
            "Существует экземпляр \"%s\" в подразделении \"%s\" в периоде \"%s\"";
    private static final String NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\" в статусе \"%s\"";
    private static final String NOT_EXIST_SOURCE_DECLARATION_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\" - экземпляр формы не создан";
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


        public Map<String, String>  getValues() {
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

    @Override
    @Transactional(readOnly = false)
    public long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                       DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp) {
        declarationDataAccessService.checkEvents(userInfo, declarationTemplateId, departmentReportPeriod,
                FormDataEvent.CREATE, logger);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Декларация не создана",
                    logEntryService.save(logger.getEntries()));
        }

        DeclarationData newDeclaration = new DeclarationData();
        newDeclaration.setDepartmentReportPeriodId(departmentReportPeriod.getId());
        newDeclaration.setReportPeriodId(departmentReportPeriod.getReportPeriod().getId());
        newDeclaration.setDepartmentId(departmentReportPeriod.getDepartmentId());
        newDeclaration.setAccepted(false);
        newDeclaration.setDeclarationTemplateId(declarationTemplateId);
        newDeclaration.setTaxOrganCode(taxOrganCode);
        newDeclaration.setKpp(taxOrganKpp);

        // Вызываем событие скрипта CREATE
        declarationDataScriptingService.executeScript(userInfo, newDeclaration, FormDataEvent.CREATE, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте создания декларации",
                    logEntryService.save(logger.getEntries()));
        }

        // Вызываем событие скрипта AFTER_CREATE
        declarationDataScriptingService.executeScript(userInfo, newDeclaration, FormDataEvent.AFTER_CREATE, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте после создания декларации",
                    logEntryService.save(logger.getEntries()));
        }

        long id = declarationDataDao.saveNew(newDeclaration);

        logBusinessService.add(null, id, userInfo, FormDataEvent.CREATE, null);
        auditService.add(FormDataEvent.CREATE , userInfo, newDeclaration.getDepartmentId(),
                newDeclaration.getReportPeriodId(),
                declarationTemplateService.get(newDeclaration.getDeclarationTemplateId()).getType().getName(),
                null, null, "Декларация создана", null, null);
        return id;
    }

    @Override
    @Transactional(readOnly = false)
    public void calculate(Logger logger, long id, TAUserInfo userInfo, Date docDate, LockStateLogger stateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.CALCULATE);
        DeclarationData declarationData = declarationDataDao.get(id);

        //2. проверяет состояние XML отчета экземпляра декларации
        setDeclarationBlobs(logger, declarationData, docDate, userInfo, stateLogger);

        //3. обновляет записи о консолидации
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(declarationData.getReportPeriodId());
        DeclarationTemplate template = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        List<DepartmentFormType> dftSources = departmentFormTypeDao.getDeclarationSources(
                declarationData.getDepartmentId(),
                template.getType().getId(),
                reportPeriod.getCalendarStartDate(),
                reportPeriod.getEndDate());
        ArrayList<Long> formDataIds = new ArrayList<Long>();
        for (DepartmentFormType dftSource : dftSources){
            DepartmentReportPeriod sourceDepartmentReportPeriod = departmentReportPeriodService.getLast(dftSource.getDepartmentId(), declarationData.getReportPeriodId());
            FormData formData =
                    formDataService.findFormData(dftSource.getFormTypeId(), dftSource.getKind(), sourceDepartmentReportPeriod.getId(), null);
            if (formData != null && formData.getState() == WorkflowState.ACCEPTED) {
                formDataIds.add(formData.getId());
            }
        }
        //Обновление информации о консолидации.
        sourceService.deleteDeclarationConsolidateInfo(id);
        sourceService.addDeclarationConsolidationInfo(id, formDataIds);

        logBusinessService.add(null, id, userInfo, FormDataEvent.SAVE, null);
        auditService.add(FormDataEvent.CALCULATE , userInfo, declarationData.getDepartmentId(),
                declarationData.getReportPeriodId(),
                declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                null, null, "Декларация обновлена", null, null);
    }

    @Override
    public void check(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        log.info(String.format("Скриптовые проверки для декларации %s", id));
        lockStateLogger.updateState("Скриптовые проверки");
        DeclarationData dd = declarationDataDao.get(id);
        checkSources(dd, logger);
        declarationDataScriptingService.executeScript(userInfo, dd, FormDataEvent.CHECK, logger, null);
        validateDeclaration(userInfo, dd, logger, true, FormDataEvent.CHECK, lockStateLogger);
        // Проверяем ошибки при пересчете
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Найдены ошибки при выполнении проверки декларации",
                    logEntryService.save(logger.getEntries()));
        } else {
            logger.info("Проверка завершена, ошибок не обнаружено");
        }
    }

    @Override
    public void preCalculationCheck(Logger logger, long declarationDataId, TAUserInfo userInfo) {
        declarationDataScriptingService.executeScript(userInfo,
                declarationDataDao.get(declarationDataId), FormDataEvent.PRE_CALCULATION_CHECK, logger, null);
        // Проверяем ошибки
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Найдены ошибки при выполнении расчета декларации",
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public DeclarationData get(long id, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        return declarationDataDao.get(id);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(long id, TAUserInfo userInfo) {
        LockData lockData = lockDataService.getLock(generateAsyncTaskKey(id, ReportType.XML_DEC));
        LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(id, ReportType.ACCEPT_DEC));
        LockData lockDataCheck = lockDataService.getLock(generateAsyncTaskKey(id, ReportType.CHECK_DEC));
        if (lockData == null && lockDataAccept == null && lockDataCheck == null) {
            declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.DELETE);
            DeclarationData declarationData = declarationDataDao.get(id);

            deleteReport(id, userInfo.getUser().getId(), false);
            declarationDataDao.delete(id);

            auditService.add(FormDataEvent.DELETE , userInfo, declarationData.getDepartmentId(),
                    declarationData.getReportPeriodId(),
                    declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                    null, null, "Декларация удалена", null, null);
        } else {
            if (lockData == null) lockData = lockDataAccept;
            if (lockData == null) lockData = lockDataCheck;
            Logger logger = new Logger();
            TAUser blocker = taUserService.getUser(lockData.getUserId());
            logger.error("Текущая декларация не может быть удалена, т.к. пользователем \"%s\" в \"%s\" запущена операция \"%s\"", blocker.getName(), SDF_DD_MM_YYYY_HH_MM_SS.format(lockData.getDateLock()), lockData.getDescription());
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void accept(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        DeclarationData declarationData = declarationDataDao.get(id);
        checkSources(declarationData, logger);

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, logger, exchangeParams);

        validateDeclaration(userInfo, declarationDataDao.get(id), logger, true, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, lockStateLogger);
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);

        if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceLoggerException("Найдены ошибки при выполнении принятия декларации", logEntryService.save(logger.getEntries()));
        }
        declarationData.setAccepted(true);

        String declarationTypeName = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName();
        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, null);
        auditService.add(FormDataEvent.MOVE_CREATED_TO_ACCEPTED, userInfo, declarationData.getDepartmentId(),
                declarationData.getReportPeriodId(), declarationTypeName, null, null, FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getTitle(), null, null);

        declarationDataDao.setAccepted(id, true);
    }

    @Override
    @Transactional(readOnly = false)
    public void cancel(Logger logger, long id, TAUserInfo userInfo) {
        DeclarationData declarationData = declarationDataDao.get(id);
        /*checkSources(declarationData, logger);*/

        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);

        declarationData.setAccepted(false);

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger, exchangeParams);

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        if (declarationTemplate.getType().getIsIfrs() &&
                departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId()).getCorrectionDate() == null) {
            IfrsData ifrsData = ifrsDataService.get(declarationData.getReportPeriodId());
            if (ifrsData != null && ifrsData.getBlobDataId() != null) {
                ifrsDataService.deleteReport(declarationData, userInfo);
            } else if (lockDataService.getLock(ifrsDataService.generateTaskKey(declarationData.getReportPeriodId())) != null) {
                ifrsDataService.cancelTask(declarationData, userInfo);
            }
        }

        String declarationTypeName = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName();
        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, null);
        auditService.add(FormDataEvent.MOVE_ACCEPTED_TO_CREATED, userInfo, declarationData.getDepartmentId(),
                declarationData.getReportPeriodId(), declarationTypeName, null, null, FormDataEvent.MOVE_ACCEPTED_TO_CREATED.getTitle(), null, null);

        declarationDataDao.setAccepted(id, false);
    }


    @Override
    public InputStream getXmlDataAsStream(long declarationId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationId, FormDataEvent.GET_LEVEL1);
        String xmlUuid = reportService.getDec(userInfo, declarationId, ReportType.XML_DEC);
        if (xmlUuid == null) {
            return null;
        }
        return blobDataService.get(xmlUuid).getInputStream();
    }

    @Override
    public String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
        try {
            String xmlUuid = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
            if (xmlUuid == null) return null;
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getName();
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

    @Override
    public Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
        try {
            String xmlUuid = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
            if (xmlUuid == null) return null;
            BlobData blobData = blobDataService.get(xmlUuid);
            return blobData.getCreationDate();
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

    @Override
    public byte[] getXlsxData(long id, TAUserInfo userInfo, LockStateLogger stateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        DeclarationData declarationData = declarationDataDao.get(id);
        String uuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.JASPER_DEC);
        JasperPrint jasperPrint;
        if (uuid != null) {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(blobDataService.get(uuid).getInputStream());
                jasperPrint = (JasperPrint) objectInputStream.readObject();
            } catch (IOException e) {
                throw new ServiceException("Не удалось извлечь Jasper-отчет.", e);
            } catch (ClassNotFoundException e) {
                throw new ServiceException("Не удалось извлечь Jasper-отчет.", e);
            }
        } else {
            log.info(String.format("Заполнение Jasper-макета декларации %s", declarationData.getId()));
            stateLogger.updateState("Заполнение Jasper-макета");
            jasperPrint = createJasperReport(declarationData, userInfo);
            // для XLSX-отчета не сохраняем Jasper-отчет из-за возмжных проблем с паралельным формированием PDF-отчета
        }
        log.info(String.format("Заполнение XLSX-отчета декларации %s", declarationData.getId()));
        stateLogger.updateState("Заполнение XLSX-отчета");
        return exportXLSX(jasperPrint);
    }

	@Override
	public byte[] getPdfData(long id, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        return getBytesFromInputstream(reportService.getDec(userInfo, id, ReportType.PDF_DEC));
	}

    private JasperPrint createJasperReport(DeclarationData declarationData, TAUserInfo userInfo) {
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
        InputStream zipXml = blobDataService.get(xmlUuid).getInputStream();
        if (zipXml != null) {
            try {
                ZipInputStream zipXmlIn = new ZipInputStream(zipXml);
                zipXmlIn.getNextEntry();
                try {
                    return fillReport(zipXmlIn,
                            declarationTemplateService.getJasper(declarationData.getDeclarationTemplateId()));
                } finally {
                    IOUtils.closeQuietly(zipXml);
                    IOUtils.closeQuietly(zipXmlIn);
                }
            } catch (IOException e) {
                throw new ServiceException(e.getLocalizedMessage(), e);
            }
        } else {
            throw new ServiceException("Декларация не сформирована");
        }
    }
    
    @Override
    public void setPdfDataBlobs(Logger logger,
                                     DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        log.info(String.format("Получение данных декларации %s", declarationData.getId()));
        stateLogger.updateState("Получение данных декларации");
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
        if (xmlUuid != null) {            
            try {                
                log.info(String.format("Заполнение Jasper-макета декларации %s", declarationData.getId()));
                stateLogger.updateState("Заполнение Jasper-макета");
                JasperPrint jasperPrint = createJasperReport(declarationData, userInfo);
                
                log.info(String.format("Сохранение PDF в БД для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение PDF в БД");
                reportService.createDec(declarationData.getId(), blobDataService.create(new ByteArrayInputStream(exportPDF(jasperPrint)), ""), ReportType.PDF_DEC);
                log.info(String.format("Сохранение Jasper в БД для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение Jasper в БД");
                reportService.createDec(declarationData.getId(), saveJPBlobData(jasperPrint), ReportType.JASPER_DEC);
            } catch (IOException e) {
                throw new ServiceException(e.getLocalizedMessage(), e);
            }
        } else {
            throw new ServiceException("Декларация не сформирована");
        }
    }

    @Override
    public void setXlsxDataBlobs(Logger logger, DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        try {
            byte[] xlsxData = getXlsxData(declarationData.getId(), userInfo, stateLogger);
            log.info(String.format("Сохранение XLSX в БД для декларации %s", declarationData.getId()));
            stateLogger.updateState("Сохранение XLSX в БД");
            reportService.createDec(declarationData.getId(), blobDataService.create(new ByteArrayInputStream(xlsxData), ""), ReportType.EXCEL_DEC);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }

    // расчет декларации
    private void setDeclarationBlobs(Logger logger,
                                     DeclarationData declarationData, Date docDate, TAUserInfo userInfo, LockStateLogger stateLogger) {

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, docDate);
        StringWriter writer = new StringWriter();
        exchangeParams.put(DeclarationDataScriptParams.XML, writer);

        File xmlFile = null;
        Writer fileWriter = null;

        try {
            try {
                log.info(String.format("Создание временного файла для декларации %s", declarationData.getId()));
                stateLogger.updateState("Создание временного файла");
                try {
                    xmlFile = File.createTempFile("file_for_validate", ".xml");
                    fileWriter = new FileWriter(xmlFile);
                    fileWriter.write(XML_HEADER);
                } catch (IOException e) {
                    throw new ServiceException("Ошибка при формировании временного файла для XML", e);
                }
                exchangeParams.put(DeclarationDataScriptParams.XML, fileWriter);
                log.info(String.format("Выполнение скрипта расчета декларации %s", declarationData.getId()));
                stateLogger.updateState("Выполнение скрипта расчета");
                declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);
            } finally {
                try {
                    if (fileWriter != null) fileWriter.close();
                } catch (IOException e) {
                    log.warn("", e);
                }
            }

            //Получение имени файла записанного в xml
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            SAXHandler handler = new SAXHandler(new HashMap<String, String>(){{
                put(TAG_FILE, ATTR_FILE_ID);
                put(TAG_DOCUMENT, ATTR_DOC_DATE);
            }});
            saxParser.parse(xmlFile, handler);
            String decName = handler.getValues().get(TAG_FILE);
            Date decDate = getFormattedDate(handler.getValues().get(TAG_DOCUMENT));
            if (decDate == null)
                decDate = docDate;

            //Переименоввываем
            File renameToFile = new File(String.format(FILE_NAME_IN_TEMP_PATTERN, decName, "xml"));
            if (xmlFile.renameTo(renameToFile)){
                //validateDeclaration(userInfo, declarationData, logger, false, FormDataEvent.CALCULATE, renameToFile, stateLogger);

                //Архивирование перед сохраннеием в базу
                File zipOutFile = null;
                try {
                    zipOutFile = new File(String.format(FILE_NAME_IN_TEMP_PATTERN, decName, "zip"));
                    FileOutputStream fileOutputStream = new FileOutputStream(zipOutFile);
                    ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
                    ZipEntry zipEntry = new ZipEntry(decName+".xml");
                    zos.putNextEntry(zipEntry);
                    FileInputStream fi = new FileInputStream(renameToFile);

                    try {
                        IOUtils.copy(fi, zos);
                    } finally {
                        IOUtils.closeQuietly(fi);
                        IOUtils.closeQuietly(zos);
                        IOUtils.closeQuietly(fileOutputStream);
                    }

                    log.info(String.format("Сохранение в бд для декларации %s", declarationData.getId()));
                    stateLogger.updateState("Сохранение в БД");

                    reportService.createDec(declarationData.getId(), blobDataService.create(zipOutFile, zipOutFile.getName(), decDate), ReportType.XML_DEC);
                } finally {
                    if (zipOutFile != null && !zipOutFile.delete()) {
                        log.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                    }
                    if (!renameToFile.delete()) {
                        log.warn(String.format(FILE_NOT_DELETE, renameToFile.getAbsolutePath()));
                    }
                }
            } else {
                throw new IOException(String.format("Преименование из %s в %s не прошло.", xmlFile.getName(), renameToFile.getName()));
            }
        } catch (IOException e) {
            log.error("", e);
            throw new ServiceException("", e);
        } catch (ParserConfigurationException e) {
            log.error("Ошибка при парсинге xml", e);
            throw new ServiceException("", e);
        } catch (SAXException e) {
            log.error("", e);
            throw new ServiceException("", e);
        } finally {
            if (xmlFile != null && !xmlFile.delete())
                log.warn(String.format(FILE_NOT_DELETE, xmlFile.getName()));
        }
    }

    private void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation, LockStateLogger lockStateLogger) {
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
        if (xmlUuid == null) {
            TaxType taxType = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getTaxType();
            String declarationName = (taxType == TaxType.DEAL ? "уведомлении" : "декларации");
            String operationName = (operation == FormDataEvent.MOVE_CREATED_TO_ACCEPTED ? "Принять" : operation.getTitle());
            String msg = String.format("В %s отсутствуют данные (не был выполнен расчет). Операция \"%s\" не может быть выполнена", declarationName, operationName);
            throw new ServiceException(msg);
        }
        validateDeclaration(userInfo, declarationData, logger, isErrorFatal, operation, null, lockStateLogger);
    }

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     * @param isErrorFatal true-если ошибки при проверке фатальные
     * @param xmlFile файл декларации
     */
    private void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation, File xmlFile, LockStateLogger stateLogger) {
        Locale oldLocale = Locale.getDefault();
        log.info(String.format("Получение данных декларации %s", declarationData.getId()));
        Locale.setDefault(new Locale("ru", "RU"));
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().isEmpty()) {
            try {
                log.info(String.format("Валидация декларации %s", declarationData.getId()));
                stateLogger.updateState("Валидация");
                if (!validateXMLService.validate(declarationData, userInfo, logger, isErrorFatal, xmlFile) && logger.containsLevel(LogLevel.ERROR)){
                    throw new ServiceException();
                }
            } catch (Exception e) {
                log.info(String.format("Сохранение логов об ошибках валидации для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение логов об ошибках валидации");
                log.error(VALIDATION_ERR_MSG, e);
                if (!(e instanceof ServiceException))
                    logger.error(e);
                String uuid = logEntryService.save(logger.getEntries());
                throw new ServiceLoggerException(VALIDATION_ERR_MSG, uuid);
            } finally {
                Locale.setDefault(oldLocale);
            }
        }
    }

    private static JasperPrint fillReport(InputStream xml, InputStream jasperTemplate) {
        try {
            InputSource inputSource = new InputSource(xml);
            Document document = JRXmlUtils.parse(inputSource);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT,
                    document);

            return JasperFillManager.fillReport(jasperTemplate, params);

        } catch (Exception e) {
            throw new ServiceException("Невозможно заполнить отчет", e);
        }
    }

    private static byte[] exportXLSX(JasperPrint jasperPrint) {
        try {
            JRXlsxExporter exporter = new JRXlsxExporter();
            ByteArrayOutputStream data = new ByteArrayOutputStream();
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
            return data.toByteArray();
        } catch (Exception e) {
            throw new ServiceException(
                    "Невозможно экспортировать отчет в XLSX", e);
        }
    }

    private static byte[] exportPDF(JasperPrint jasperPrint) {
        try {
            JRPdfExporter exporter = new JRPdfExporter();
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT,
                    jasperPrint);
            exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, data);
            exporter.getPropertiesUtil().setProperty(JRPdfExporterParameter.PROPERTY_SIZE_PAGE_TO_CONTENT, "true");

            exporter.exportReport();
            return data.toByteArray();
        } catch (Exception e) {
            throw new ServiceException("Невозможно экспортировать отчет в PDF",
                    e);
        }
    }

    private static Date getFormattedDate(String stringToDate) {
        if (stringToDate == null)
            return null;
        // Преобразуем строку вида "dd.mm.yyyy" в Date
        try {
            return formatter.parse(stringToDate);
        } catch (ParseException e) {
            throw new ServiceException("Невозможно получить дату обновления декларации", e);
        }
    }

    private String saveJPBlobData(JasperPrint jasperPrint) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(jasperPrint);
        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        return blobDataService.create(inputStream, "");
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriod, String kpp, String taxOrganCode) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriod, kpp, taxOrganCode);
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

                logs.add(new LogEntry(LogLevel.ERROR, String.format(MSG_IS_EXIST_DECLARATION,
                        declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                        departmentService.getDepartment(departmentId).getName(),
                        period.getName() + " " + period.getTaxPeriod().getYear())));
            }
        }
        return !declarationIds.isEmpty();
    }

    private byte[] getBytesFromInputstream(String blobId){
        if (blobId == null) return null;
        BlobData blobData = blobDataService.get(blobId);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(blobData.getInputStream(), arrayOutputStream);
        } catch (IOException e) {
            throw new ServiceException("Не удалось извлечь отчет.", e);
        }
        return arrayOutputStream.toByteArray();
    }

    @Override
    public String generateAsyncTaskKey(long declarationDataId, ReportType reportType) {
        if (reportType == null) {
            return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId;
        }
        return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId + "_" + reportType.getName();
    }

    @Override
    @Transactional
    public LockData lock(long declarationDataId, TAUserInfo userInfo) {
        LockData lockData = lockDataService.lock(generateAsyncTaskKey(declarationDataId, null), userInfo.getUser().getId(),
                getDeclarationFullName(declarationDataId, null),
                lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA));
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
        checkLock(lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ReportType.XML_DEC)),
                userInfo.getUser());
    }

    private void checkLock(LockData lockData, TAUser user){
        if (lockData!= null && lockData.getUserId() != user.getId()) {
            TAUser lockUser = taUserService.getUser(lockData.getUserId());
            throw new ServiceException(String.format(LockDataService.LOCK_DATA, lockUser.getName(), lockUser.getId()));
        }
    }

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     */
    @Override
    public void deleteReport(long declarationDataId, int userId, boolean isCalc) {
        ReportType[] reportTypes = {ReportType.XML_DEC, ReportType.PDF_DEC, ReportType.EXCEL_DEC, ReportType.CHECK_DEC, ReportType.ACCEPT_DEC};
        for (ReportType reportType : reportTypes) {
            if (!isCalc || !ReportType.XML_DEC.equals(reportType)) {
                LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, reportType));
                if (lock != null)
                    lockDataService.interruptTask(lock, userId, true);
            }
        }
        reportService.deleteDec(declarationDataId);
    }

    @Override
    public void findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate, Logger logger) {
        List<Integer> ddIds = declarationDataDao.findDDIdsByRangeInReportPeriod(decTemplateId,
                startDate, endDate != null ? endDate : MAX_DATE);
        for (Integer id : ddIds){
            DeclarationData dd = declarationDataDao.get(id);
            ReportPeriod rp = reportPeriodService.getReportPeriod(dd.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.get(dd.getDepartmentReportPeriodId());
            DeclarationTemplate dt = declarationTemplateService.get(dd.getDeclarationTemplateId());
            logger.error(DD_NOT_IN_RANGE,
                    rp.getName(),
                    rp.getTaxPeriod().getYear(),
                    drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                            formatter.format(drp.getCorrectionDate())) : "",
                    dt.getName(),
                    dd.isAccepted()?"принята":"не принята");
        }
    }

    @Override
    public String getDeclarationFullName(long declarationId, ReportType reportType) {
        DeclarationData declaration = declarationDataDao.get(declarationId);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        if (reportType == null)
            return String.format(LockData.DescriptionTemplate.DECLARATION.getText(),
                    declarationTemplate.getType().getName(),
                    declaration.getKpp(),
                    declaration.getTaxOrganCode(),
                    department.getName(),
                    reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    reportPeriod.getCorrectionDate() != null
                            ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                            : "",
                    declaration.getTaxOrganCode() != null
                            ? ", налоговый орган \"" + declaration.getTaxOrganCode() + "\""
                            : "",
                    declaration.getKpp() != null
                            ? ", КПП \"" + declaration.getKpp() + "\""
                            : "");

        switch (reportType) {
            case EXCEL_DEC:
            case PDF_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_REPORT.getText(),
                        reportType.getName(),
                        declarationTemplate.getType().getTaxType().getDeclarationShortName(),
                        declarationTemplate.getType().getName(),
                        declaration.getKpp(),
                        declaration.getTaxOrganCode(),
                        department.getName(),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                : "",
                        declaration.getTaxOrganCode() != null
                                ? ", налоговый орган \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП \"" + declaration.getKpp() + "\""
                                : "");
            case XML_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_CALCULATE.getText(),
                        declarationTemplate.getType().getTaxType().getDeclarationShortName(),
                        declarationTemplate.getType().getName(),
                        declaration.getKpp(),
                        declaration.getTaxOrganCode(),
                        department.getName(),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                : "",
                        declaration.getTaxOrganCode() != null
                                ? ", налоговый орган \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП \"" + declaration.getKpp() + "\""
                                : "");
            case CHECK_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_CHECK.getText(),
                        declarationTemplate.getType().getTaxType().getDeclarationShortName(),
                        declarationTemplate.getType().getName(),
                        declaration.getKpp(),
                        declaration.getTaxOrganCode(),
                        department.getName(),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                : "",
                        declaration.getTaxOrganCode() != null
                                ? ", налоговый орган \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП \"" + declaration.getKpp() + "\""
                                : "");
            case ACCEPT_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_ACCEPT.getText(),
                        declarationTemplate.getType().getTaxType().getDeclarationShortName(),
                        declarationTemplate.getType().getName(),
                        declaration.getKpp(),
                        declaration.getTaxOrganCode(),
                        department.getName(),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                : "",
                        declaration.getTaxOrganCode() != null
                                ? ", налоговый орган \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП \"" + declaration.getKpp() + "\""
                                : "");
            default:
                return String.format(LockData.DescriptionTemplate.DECLARATION.getText(),
                        declarationTemplate.getType().getName(),
                        declaration.getKpp(),
                        declaration.getTaxOrganCode(),
                        department.getName(),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                : "",
                        declaration.getTaxOrganCode() != null
                                ? ", налоговый орган \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП \"" + declaration.getKpp() + "\""
                                : "");
        }
    }

    @Override
    public Pair<BalancingVariants, Long> checkTaskLimit(TAUserInfo userInfo, long declarationDataId, ReportType reportType) {
        switch (reportType) {
            case PDF_DEC:
            case EXCEL_DEC:
                String uuidXml = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
                if (uuidXml != null) {
                    Long size = blobDataService.getLength(uuidXml);
                    AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId(true));
                    long maxSize = taskTypeData.getTaskLimit() * 1024;
                    long shortSize = taskTypeData.getShortQueueLimit() * 1024;
                    if (size > maxSize) {
                        return new Pair<BalancingVariants, Long>(null, size);
                    } else if (size < shortSize) {
                        return new Pair<BalancingVariants, Long>(BalancingVariants.SHORT, size);
                    }
                    return new Pair<BalancingVariants, Long>(BalancingVariants.LONG, size);
                } else {
                    return null;
                }
            case XML_DEC:
                return new Pair<BalancingVariants, Long>(BalancingVariants.LONG, 0L);
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
        }
    }

    private void checkSources(DeclarationData dd, Logger logger){
        //Проверка на неактуальные консолидированные данные
        if (!sourceService.isDDConsolidationTopical(dd.getId())){
            logger.error(CALCULATION_NOT_TOPICAL);
            /*throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));*/
        }
        ReportPeriod rp = reportPeriodService.getReportPeriod(dd.getReportPeriodId());
        List<DepartmentFormType> sourceDDs = departmentFormTypeDao.getDeclarationSources(
                dd.getDepartmentId(),
                declarationTemplateService.get(dd.getDeclarationTemplateId()).getType().getId(),
                rp.getStartDate(),
                rp.getEndDate());
        for (DepartmentFormType sourceDFT : sourceDDs){
            FormData sourceFD =
                    formDataService.findFormData(sourceDFT.getFormTypeId(), sourceDFT.getKind(), dd.getDepartmentReportPeriodId(), null);
            if (sourceFD==null){
                DepartmentReportPeriod drp = departmentReportPeriodService.get(dd.getDepartmentReportPeriodId());
                logger.error(
                        NOT_EXIST_SOURCE_DECLARATION_WARNING,
                        departmentService.getDepartment(sourceDFT.getDepartmentId()).getName(),
                        formTypeService.get(sourceDFT.getFormTypeId()).getName(),
                        sourceDFT.getKind().getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                formatter.format(drp.getCorrectionDate())) : "");
            } else if (!sourceService.isDeclarationSourceConsolidated(dd.getId(), sourceFD.getId())){
                DepartmentReportPeriod sourceDRP = departmentReportPeriodService.get(sourceFD.getDepartmentReportPeriodId());
                logger.error(NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING,
                        departmentService.getDepartment(sourceFD.getDepartmentId()).getName(),
                        sourceFD.getFormType().getName(),
                        sourceFD.getKind().getName(),
                        rp.getName() + (sourceFD.getPeriodOrder() != null ? " " + Months.fromId(sourceFD.getPeriodOrder()).getTitle() : ""),
                        rp.getTaxPeriod().getYear(),
                        sourceDRP.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                formatter.format(sourceDRP.getCorrectionDate())) : "",
                        sourceFD.getState().getName());
            }
        }

        if (!logger.containsLevel(LogLevel.ERROR)){
            logger.info("Консолидация выполнена из всех форм-источников.");
        }
    }
}
