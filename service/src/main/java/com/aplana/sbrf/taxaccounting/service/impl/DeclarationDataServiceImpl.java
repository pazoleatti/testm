package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
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
import org.apache.commons.io.IOUtils;
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
    private FormDataDao formDataDao;
    @Autowired
    private DataRowDao dataRowDao;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

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
        auditService.add(FormDataEvent.CREATE , userInfo, newDeclaration, null, "Декларация создана", null);
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
        ArrayList<Long> formDataIds = new ArrayList<Long>();
        for (Relation relation : sourceService.getDeclarationSourcesInfo(declarationData, true, true, WorkflowState.ACCEPTED, userInfo, logger)){
            formDataIds.add(relation.getFormDataId());
        }
        //Обновление информации о консолидации.
        sourceService.deleteDeclarationConsolidateInfo(id);
        sourceService.addDeclarationConsolidationInfo(id, formDataIds);

        logBusinessService.add(null, id, userInfo, FormDataEvent.SAVE, null);
        auditService.add(FormDataEvent.CALCULATE , userInfo, declarationData, null, "Декларация обновлена", null);
    }

    @Override
    public void check(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        LOG.info(String.format("Проверка данных декларации/уведомления %s", id));
        DeclarationData dd = declarationDataDao.get(id);
        Logger scriptLogger = new Logger();
        Logger validateLogger = new Logger();
        try {
            lockStateLogger.updateState("Проверка форм-источников");
            checkSources(dd, logger, userInfo);
            lockStateLogger.updateState("Проверка данных декларации/уведомления");
            declarationDataScriptingService.executeScript(userInfo, dd, FormDataEvent.CHECK, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException();
        }
        try {
            validateDeclaration(userInfo, dd, validateLogger, true, FormDataEvent.CHECK, lockStateLogger);
        } finally {
            logger.getEntries().addAll(validateLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException();
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
        LockData lockData = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.XML_DEC));
        LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.ACCEPT_DEC));
        LockData lockDataCheck = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.CHECK_DEC));
        if (lockData == null && lockDataAccept == null && lockDataCheck == null) {
            declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.DELETE);
            DeclarationData declarationData = declarationDataDao.get(id);

            deleteReport(id, userInfo.getUser().getId(), false, "Удалена декларация");
            declarationDataDao.delete(id);

            auditService.add(FormDataEvent.DELETE , userInfo, declarationData, null, "Декларация удалена", null);
        } else {
            if (lockData == null) lockData = lockDataAccept;
            if (lockData == null) lockData = lockDataCheck;
            Logger logger = new Logger();
            TAUser blocker = taUserService.getUser(lockData.getUserId());
            logger.error("Текущая декларация не может быть удалена, т.к. пользователем \"%s\" в \"%s\" запущена операция \"%s\"", blocker.getName(), SDF_DD_MM_YYYY_HH_MM_SS.get().format(lockData.getDateLock()), lockData.getDescription());
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void accept(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);

        DeclarationData declarationData = declarationDataDao.get(id);

        Logger scriptLogger = new Logger();
        Logger validateLogger = new Logger();
        try {
            lockStateLogger.updateState("Проверка форм-источников");
            checkSources(declarationData, logger, userInfo);
            lockStateLogger.updateState("Проверка данных декларации/уведомления");
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CHECK, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException();
        }
        try {
            validateDeclaration(userInfo, declarationData, validateLogger, true, FormDataEvent.CHECK, lockStateLogger);
        } finally {
            logger.getEntries().addAll(validateLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceException();
        }

        declarationData.setAccepted(true);

        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, null);
        auditService.add(FormDataEvent.MOVE_CREATED_TO_ACCEPTED, userInfo, declarationData, null, FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getTitle(), null);

        lockStateLogger.updateState("Изменение состояния декларации");
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
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
        }
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

        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, null);
        auditService.add(FormDataEvent.MOVE_ACCEPTED_TO_CREATED, userInfo, declarationData, null, FormDataEvent.MOVE_ACCEPTED_TO_CREATED.getTitle(), null);

        declarationDataDao.setAccepted(id, false);
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
    public Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
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
                LOG.info(String.format("Заполнение Jasper-макета декларации %s", declarationData.getId()));
                stateLogger.updateState("Заполнение Jasper-макета");
                jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 4096, 1000);
                jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);
                // для XLSX-отчета не сохраняем Jasper-отчет из-за возмжных проблем с паралельным формированием PDF-отчета
            }
            LOG.info(String.format("Заполнение XLSX-отчета декларации %s", declarationData.getId()));
            stateLogger.updateState("Заполнение XLSX-отчета");
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

    private InputStream getJasper(String jrxml) {
        ByteArrayOutputStream compiledReport = new ByteArrayOutputStream();
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            try {
                byteArrayInputStream = new ByteArrayInputStream(jrxml.getBytes(ENCODING));
                JasperDesign jasperDesign = JRXmlLoader.load(byteArrayInputStream);
                JasperCompileManager.compileReportToStream(jasperDesign, compiledReport);
            } catch (JRException e) {
                LOG.error(e.getMessage(), e);
                throw new ServiceException("Произошли ошибки во время формирования отчета!");
            } catch (UnsupportedEncodingException e2) {
                LOG.error(e2.getMessage(), e2);
                throw new ServiceException("Шаблон отчета имеет неправильную кодировку!");
            }
            return new ByteArrayInputStream(compiledReport.toByteArray());
        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
            IOUtils.closeQuietly(compiledReport);
        }
    }

    @Override
    public JasperPrint createJasperReport(InputStream xmlIn, String jrxml, JRSwapFile jrSwapFile) {
        InputStream jasperTemplate = null;
        try {
            jasperTemplate = getJasper(jrxml);
            return fillReport(xmlIn, jasperTemplate, jrSwapFile);
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
                    return createJasperReport(zipXmlIn, declarationTemplateService.getJrxml(declarationData.getDeclarationTemplateId()), jrSwapFile);
                } catch (IOException e) {
                    throw new ServiceException(e.getLocalizedMessage(), e);
                } finally {
                    IOUtils.closeQuietly(zipXmlIn);
                }
            } else {
                throw new ServiceException("Декларация не сформирована");
            }
        } finally {
            IOUtils.closeQuietly(zipXml);
        }
    }
    
    @Override
    public void setPdfDataBlobs(Logger logger,
                                     DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        LOG.info(String.format("Получение данных декларации %s", declarationData.getId()));
        stateLogger.updateState("Получение данных декларации");
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC);
        if (xmlUuid != null) {
            File pdfFile = null;
            JRSwapFile jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 100);
            try {                
                LOG.info(String.format("Заполнение Jasper-макета декларации %s", declarationData.getId()));
                stateLogger.updateState("Заполнение Jasper-макета");
                JasperPrint jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);

                LOG.info(String.format("Заполнение PDF-файла декларации %s", declarationData.getId()));
                stateLogger.updateState("Заполнение PDF-файла");
                pdfFile = File.createTempFile("report", ".pdf");

                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(pdfFile);
                    exportPDF(jasperPrint, outputStream);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }

                LOG.info(String.format("Сохранение PDF-файла в базе данных для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение PDF-файла в базе данных");
                reportService.createDec(declarationData.getId(), blobDataService.create(pdfFile.getPath(), ""), DeclarationDataReportType.PDF_DEC);

                // не сохраняем jasper-отчет, если есть XLSX-отчет
                if (reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.EXCEL_DEC) == null) {
                    LOG.info(String.format("Сохранение Jasper-макета в базе данных для декларации %s", declarationData.getId()));
                    stateLogger.updateState("Сохранение Jasper-макета в базе данных");
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
            throw new ServiceException("Декларация не сформирована");
        }
    }

    @Override
    public void createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, TAUserInfo userInfo, LockStateLogger stateLogger) {
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
                params.put(DeclarationDataScriptParams.DOC_DATE, new Date());
                params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
                stateLogger.updateState("Формирование отчета");
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
            stateLogger.updateState("Сохранение отчета в базе данных");
            reportService.createDec(declarationData.getId(), blobDataService.create(reportFile.getPath(), scriptSpecificReportHolder.getFileName()), ddReportType);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (reportFile != null)
                reportFile.delete();
        }
    }

    public void setXlsxDataBlobs(Logger logger, DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        File xlsxFile = null;
        try {
            xlsxFile = File.createTempFile("report", ".xlsx");
            getXlsxData(declarationData.getId(), xlsxFile, userInfo, stateLogger);

            LOG.info(String.format("Сохранение XLSX в базе данных для декларации %s", declarationData.getId()));
            stateLogger.updateState("Сохранение XLSX в базе данных");

            reportService.createDec(declarationData.getId(), blobDataService.create(xlsxFile.getPath(), ""), DeclarationDataReportType.EXCEL_DEC);
            reportService.deleteDec(Arrays.asList(declarationData.getId()), Arrays.asList(DeclarationDataReportType.JASPER_DEC));
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

    // расчет декларации
    private void setDeclarationBlobs(Logger logger,
                                     DeclarationData declarationData, Date docDate, TAUserInfo userInfo, LockStateLogger stateLogger) {

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, docDate);

        File xmlFile = null;
        Writer fileWriter = null;

        try {
            try {
                LOG.info(String.format("Создание временного файла для записи расчета для декларации %s", declarationData.getId()));
                stateLogger.updateState("Создание временного файла для записи расчета");
                try {
                    xmlFile = File.createTempFile("file_for_validate", ".xml");
                    fileWriter = new FileWriter(xmlFile);
                    fileWriter.write(XML_HEADER);
                } catch (IOException e) {
                    throw new ServiceException("Ошибка при формировании временного файла для XML", e);
                }
                exchangeParams.put(DeclarationDataScriptParams.XML, fileWriter);
                LOG.info(String.format("Формирование XML-файла декларации %s", declarationData.getId()));
                stateLogger.updateState("Формирование XML-файла");
                declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceException();
                }
            } finally {
                IOUtils.closeQuietly(fileWriter);
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

            //Архивирование перед сохраннеием в базу
            File zipOutFile = null;
            try {
                zipOutFile = File.createTempFile("xml", ".zip");
                FileOutputStream fileOutputStream = new FileOutputStream(zipOutFile);
                ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
                ZipEntry zipEntry = new ZipEntry(decName+".xml");
                zos.putNextEntry(zipEntry);
                FileInputStream fi = new FileInputStream(xmlFile);

                try {
                    IOUtils.copy(fi, zos);
                } finally {
                    IOUtils.closeQuietly(fi);
                    IOUtils.closeQuietly(zos);
                    IOUtils.closeQuietly(fileOutputStream);
                }

                LOG.info(String.format("Сохранение XML-файла в базе данных для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение XML-файла в базе данных");

                reportService.createDec(declarationData.getId(),
                        blobDataService.create(zipOutFile, decName + ".zip", decDate),
                        DeclarationDataReportType.XML_DEC);
            } finally {
                if (zipOutFile != null && !zipOutFile.delete()) {
                    LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                }
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
    }

    private void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation, LockStateLogger lockStateLogger) {
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC);
        if (xmlUuid == null) {
            TaxType taxType = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getTaxType();
            String declarationName = taxType == TaxType.DEAL ? "уведомлении" : "декларации";
            String operationName = operation == FormDataEvent.MOVE_CREATED_TO_ACCEPTED ? "Принять" : operation.getTitle();
            logger.error("В %s отсутствуют данные (не был выполнен расчет). Операция \"%s\" не может быть выполнена", declarationName, operationName);
        } else {
            validateDeclaration(userInfo, declarationData, logger, isErrorFatal, operation, null, lockStateLogger);
        }
    }

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     * @param isErrorFatal true-если ошибки при проверке фатальные
     * @param xmlFile файл декларации
     */
    private void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation, File xmlFile, LockStateLogger stateLogger) {
        Locale oldLocale = Locale.getDefault();
        LOG.info(String.format("Получение данных декларации %s", declarationData.getId()));
        Locale.setDefault(new Locale("ru", "RU"));
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().isEmpty()) {
            try {
                LOG.info(String.format("Выполнение проверок XSD-файла декларации %s", declarationData.getId()));
                stateLogger.updateState("Выполнение проверок XSD-файла");
                boolean valid = validateXMLService.validate(declarationData, userInfo, logger, isErrorFatal, xmlFile);
                if (!logger.containsLevel(LogLevel.ERROR) && !valid) {
                    logger.error(VALIDATION_ERR_MSG);
                }
            } catch (Exception e) {
                LOG.error(VALIDATION_ERR_MSG, e);
                logger.error(e);
            } finally {
                Locale.setDefault(oldLocale);
            }
        }
    }

    private static JasperPrint fillReport(InputStream xml, InputStream jasperTemplate, JRSwapFile jrSwapFile) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
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

            return JasperFillManager.fillReport(jasperTemplate, params);
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
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    private Date getFormattedDate(String stringToDate) {
        if (stringToDate == null)
            return null;
        // Преобразуем строку вида "dd.mm.yyyy" в Date
        try {
            return sdf.get().parse(stringToDate);
        } catch (ParseException e) {
            throw new ServiceException("Невозможно получить дату обновления декларации", e);
        }
    }

    private String saveJPBlobData(JasperPrint jasperPrint) throws IOException {
        File jasperPrintFile = null;
        try {
            jasperPrintFile = File.createTempFile("report",".jasper");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(jasperPrintFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(jasperPrint);
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }

            //Архивирование перед сохраннеием в базу
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
        }
        return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId + "_" + type.getReportType();
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
    public void deleteReport(long declarationDataId, int userId, boolean isCalc, String cause) {
        DeclarationDataReportType[] ddReportTypes = {DeclarationDataReportType.XML_DEC, DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC};
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                DeclarationData declarationData = declarationDataDao.get(declarationDataId);
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for(DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                    if (lock != null)
                        lockDataService.interruptTask(lock, userId, true, cause);
                }
            } else if (!isCalc || !DeclarationDataReportType.XML_DEC.equals(ddReportType)) {
                LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                if (lock != null)
                    lockDataService.interruptTask(lock, userId, true, cause);
            }
        }
        reportService.deleteDec(declarationDataId);
    }

    /**
     * Список операции, по которым требуется удалить блокировку
     * @param reportType
     * @return
     */
    private DeclarationDataReportType[] getCheckTaskList(ReportType reportType) {
        switch (reportType) {
            case XML_DEC:
                return new DeclarationDataReportType[]{DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC, new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, null)};
            case ACCEPT_DEC:
                return new DeclarationDataReportType[]{DeclarationDataReportType.CHECK_DEC};
            case UPDATE_TEMPLATE_DEC:
                return new DeclarationDataReportType[]{new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, null)};
            default:
                return null;
        }
    }

    @Override
    public boolean checkExistTask(long declarationDataId, ReportType reportType, Logger logger) {
        DeclarationDataReportType[] ddReportTypes = getCheckTaskList(reportType);
        if (ddReportTypes == null) return false;
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        boolean exist = false;
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for(DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    exist |= checkExistTask(declarationDataId, ddReportType, declarationTemplate.getType().getTaxType(), logger);
                }
            } else {
                exist |= checkExistTask(declarationDataId, ddReportType, declarationTemplate.getType().getTaxType(), logger);
            }
        }
        return exist;
    }

    private boolean checkExistTask(long declarationDataId, DeclarationDataReportType ddReportType, TaxType taxType, Logger logger) {
        LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
        if (lock != null) {
            if (LockData.State.IN_QUEUE.getText().equals(lock.getState())) {
                logger.info(LockData.CANCEL_TASK_NOT_PROGRESS,
                        SDF_DD_MM_YYYY_HH_MM_SS.get().format(lock.getDateLock()),
                        taUserService.getUser(lock.getUserId()).getName(),
                        getTaskName(ddReportType, taxType));
            } else {
                logger.info(LockData.CANCEL_TASK_IN_PROGRESS,
                        SDF_DD_MM_YYYY_HH_MM_SS.get().format(lock.getDateLock()),
                        taUserService.getUser(lock.getUserId()).getName(),
                        getTaskName(ddReportType, taxType));
            }
            return true;
        }
        return false;
    }

    @Override
    public void interruptTask(long declarationDataId, int userId, ReportType reportType, String cause) {
        DeclarationDataReportType[] ddReportTypes = getCheckTaskList(reportType);
        if (ddReportTypes == null) return;
        TAUserInfo taUserInfo = new TAUserInfo();
        taUserInfo.setUser(taUserService.getUser(userId));
        DeclarationData declarationData = get(declarationDataId, taUserInfo);
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            List<String> taskKeyList = new ArrayList<String>();
            if (ddReportType.isSubreport()) {
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for(DeclarationSubreport subreport: subreports) {
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
                    lockDataService.interruptTask(lock, userId, true, cause);
                }
            }
        }
        if (DeclarationDataReportType.XML_DEC.getReportType().equals(reportType)) {
            reportService.deleteDec(declarationDataId);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void cleanBlobs(Collection<Long> ids, List<DeclarationDataReportType> reportTypes) {
        if (ids.isEmpty()){
            return;
        }
        reportService.deleteDec(ids, reportTypes);
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
                            sdf.get().format(drp.getCorrectionDate())) : "",
                    dt.getName(),
                    dd.isAccepted()?"принята":"не принята");
        }
    }

    @Override
    public String getDeclarationFullName(long declarationId, DeclarationDataReportType ddReportType, String... args) {
        DeclarationData declaration = declarationDataDao.get(declarationId);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        if (ddReportType == null)
            return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                    declarationTemplate.getType().getTaxType() == TaxType.DEAL ? "Уведомление" : "Декларация",
                    reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    reportPeriod.getCorrectionDate() != null
                            ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                            : "",
                    department.getName(),
                    declarationTemplate.getType().getName(),
                    declaration.getTaxOrganCode() != null
                            ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                            : "",
                    declaration.getKpp() != null
                            ? ", КПП: \"" + declaration.getKpp() + "\""
                            : "");

        switch (ddReportType.getReportType()) {
            case EXCEL_DEC:
            case PDF_DEC:
            case XML_DEC:
            case CHECK_DEC:
            case ACCEPT_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        getTaskName(ddReportType, declarationTemplate.getType().getTaxType()),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "");
            case SPECIFIC_REPORT_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        getTaskName(ddReportType, declarationTemplate.getType().getTaxType()),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "");
            default:
                return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        declarationTemplate.getType().getTaxType() == TaxType.DEAL ? "Уведомление" : "Декларация",
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + sdf.get().format(reportPeriod.getCorrectionDate())
                                : "",
                        department.getName(),
                        declarationTemplate.getType().getName(),
                        declaration.getTaxOrganCode() != null
                                ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                                : "",
                        declaration.getKpp() != null
                                ? ", КПП: \"" + declaration.getKpp() + "\""
                                : "");
        }
    }

    @Override
    public Long getTaskLimit(ReportType reportType) {
        return asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId()).getTaskLimit();
    }

    @Override
    public Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType reportType) {
        switch (reportType.getReportType()) {
            case PDF_DEC:
            case EXCEL_DEC:
            case ACCEPT_DEC:
            case CHECK_DEC:
                String uuidXml = reportService.getDec(userInfo, declarationDataId, DeclarationDataReportType.XML_DEC);
                if (uuidXml != null) {
                    return (long)Math.ceil(blobDataService.getLength(uuidXml) / 1024.);
                } else {
                    return null;
                }
            case XML_DEC:
                long cellCountSource = 0;
                DeclarationData declarationData = get(declarationDataId, userInfo);
                for (Relation relation : sourceService.getDeclarationSourcesInfo(declarationData, true, true, null, userInfo, new Logger())){
                    System.out.println("getFormDataId: "+relation.getFormDataId());
                    if (relation.isCreated() && relation.getState() == WorkflowState.ACCEPTED) {
                        FormData formData = formDataDao.getWithoutRows(relation.getFormDataId());
                        int rowCountSource = dataRowDao.getRowCount(formData);
                        int columnCountSource = formTemplateService.get(formData.getFormTemplateId()).getColumns().size();
                        cellCountSource += rowCountSource * columnCountSource;
                    }
                }
                return cellCountSource;
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

    private void checkSources(DeclarationData dd, Logger logger, TAUserInfo userInfo){
        boolean consolidationOk = true;
        //Проверка на неактуальные консолидированные данные  3А
        if (!sourceService.isDDConsolidationTopical(dd.getId())){
            logger.error(CALCULATION_NOT_TOPICAL);
            consolidationOk = false;
        } else {
            //Проверка того, что консолидация вообще когда то выполнялась для всех источников
            List<Relation> relations = sourceService.getDeclarationSourcesInfo(dd, true, false, null, userInfo, logger);
            for (Relation relation : relations){
                if (!relation.isCreated()){
                    consolidationOk = false;
                    logger.warn(
                            NOT_EXIST_SOURCE_DECLARATION_WARNING,
                            relation.getFullDepartmentName(),
                            relation.getFormTypeName(),
                            relation.getFormDataKind().getTitle(),
                            relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    sdf.get().format(relation.getCorrectionDate())) : "");
                } else if (!sourceService.isDeclarationSourceConsolidated(dd.getId(), relation.getFormDataId())){
                    consolidationOk = false;
                    logger.warn(NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING,
                            relation.getFullDepartmentName(),
                            relation.getFormTypeName(),
                            relation.getFormDataKind().getTitle(),
                            relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    sdf.get().format(relation.getCorrectionDate())) : "",
                            relation.getState().getTitle());
                }
            }

            if (!relations.isEmpty() && consolidationOk){
                logger.info("Консолидация выполнена из всех форм-источников.");
            }
        }
    }

    @Override
    public String getTaskName(DeclarationDataReportType ddReportType, TaxType taxType) {
        switch (ddReportType.getReportType()) {
            case CHECK_DEC:
            case ACCEPT_DEC:
            case EXCEL_DEC:
            case XML_DEC:
            case PDF_DEC:
                return String.format(ddReportType.getReportType().getDescription(), taxType.getDeclarationShortName());
            case SPECIFIC_REPORT_DEC:
                return String.format(ddReportType.getReportType().getDescription(), ddReportType.getSubreport().getName(), taxType.getDeclarationShortName());
            default:
                throw new ServiceException("Неверный тип отчета(%s)", ddReportType.getReportType().getName());
        }
    }
}
