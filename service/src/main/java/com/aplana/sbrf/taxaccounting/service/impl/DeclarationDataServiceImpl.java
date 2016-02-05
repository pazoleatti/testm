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
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRSwapFile;
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

	private static final Log LOG = LogFactory.getLog(DeclarationDataService.class);
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
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
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
        LockData lockData = lockDataService.getLock(generateAsyncTaskKey(id, ReportType.XML_DEC));
        LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(id, ReportType.ACCEPT_DEC));
        LockData lockDataCheck = lockDataService.getLock(generateAsyncTaskKey(id, ReportType.CHECK_DEC));
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
            logger.error("Текущая декларация не может быть удалена, т.к. пользователем \"%s\" в \"%s\" запущена операция \"%s\"", blocker.getName(), SDF_DD_MM_YYYY_HH_MM_SS.format(lockData.getDateLock()), lockData.getDescription());
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
            LOG.error(e.toString(), e);
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
            LOG.error(e.toString(), e);
            return null;
        }
    }

    public void getXlsxData(long id, File xlsxFile, TAUserInfo userInfo, LockStateLogger stateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        DeclarationData declarationData = declarationDataDao.get(id);
        String uuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.JASPER_DEC);
        JasperPrint jasperPrint;
        JRSwapFile jrSwapFile = null;
        try {
            if (uuid != null) {
                ObjectInputStream objectInputStream = null;
                try {
                    objectInputStream = new ObjectInputStream(blobDataService.get(uuid).getInputStream());
                    jasperPrint = (JasperPrint) objectInputStream.readObject();
                } catch (IOException e) {
                    throw new ServiceException("Не удалось извлечь Jasper-отчет.", e);
                } catch (ClassNotFoundException e) {
                    throw new ServiceException("Не удалось извлечь Jasper-отчет.", e);
                } finally {
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
	public byte[] getPdfData(long id, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        return getBytesFromInputstream(reportService.getDec(userInfo, id, ReportType.PDF_DEC));
	}

    private JasperPrint createJasperReport(DeclarationData declarationData, JRSwapFile jrSwapFile, TAUserInfo userInfo) {
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
        InputStream zipXml = blobDataService.get(xmlUuid).getInputStream();
        try {
            if (zipXml != null) {
                InputStream jasperTemplate = null;
                ZipInputStream zipXmlIn = new ZipInputStream(zipXml);
                try {
                    zipXmlIn.getNextEntry();
                    jasperTemplate = declarationTemplateService.getJasper(declarationData.getDeclarationTemplateId());
                    return fillReport(zipXmlIn, jasperTemplate, jrSwapFile);
                } catch (IOException e) {
                    throw new ServiceException(e.getLocalizedMessage(), e);
                } finally {
                    IOUtils.closeQuietly(zipXmlIn);
                    IOUtils.closeQuietly(jasperTemplate);
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
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
        if (xmlUuid != null) {
            File pdfFile = null;
            JRSwapFile jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 100);
            try {                
                LOG.info(String.format("Заполнение Jasper-макета декларации %s", declarationData.getId()));
                stateLogger.updateState("Заполнение Jasper-макета");
                JasperPrint jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);
                
                LOG.info(String.format("Сохранение PDF-файла в базе данных для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение PDF-файла в базе данных");

                pdfFile = File.createTempFile("report", ".pdf");
                exportPDF(jasperPrint, pdfFile);

                reportService.createDec(declarationData.getId(), blobDataService.create(pdfFile.getPath(), ""), ReportType.PDF_DEC);
                LOG.info(String.format("Сохранение Jasper-макета в базе данных для декларации %s", declarationData.getId()));
                stateLogger.updateState("Сохранение Jasper-макета в базе данных");
                reportService.createDec(declarationData.getId(), saveJPBlobData(jasperPrint), ReportType.JASPER_DEC);
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
    public void setXlsxDataBlobs(Logger logger, DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger) {
        File xlsxFile = null;
        try {
            xlsxFile = File.createTempFile("report", ".xlsx");
            getXlsxData(declarationData.getId(), xlsxFile, userInfo, stateLogger);

            LOG.info(String.format("Сохранение XLSX в базе данных для декларации %s", declarationData.getId()));
            stateLogger.updateState("Сохранение XLSX в базе данных");

            reportService.createDec(declarationData.getId(), blobDataService.create(xlsxFile.getPath(), ""), ReportType.EXCEL_DEC);
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
        StringWriter writer = new StringWriter();
        exchangeParams.put(DeclarationDataScriptParams.XML, writer);

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
                try {
                    if (fileWriter != null) fileWriter.close();
                } catch (IOException e) {
                    LOG.warn("", e);
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

                    LOG.info(String.format("Сохранение XML-файла в базе данных для декларации %s", declarationData.getId()));
                    stateLogger.updateState("Сохранение XML-файла в базе данных");

                    reportService.createDec(declarationData.getId(), blobDataService.create(zipOutFile, zipOutFile.getName(), decDate), ReportType.XML_DEC);
                } finally {
                    if (zipOutFile != null && !zipOutFile.delete()) {
                        LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                    }
                    if (!renameToFile.delete()) {
                        LOG.warn(String.format(FILE_NOT_DELETE, renameToFile.getAbsolutePath()));
                    }
                }
            } else {
                throw new IOException(String.format("Преименование из %s в %s не прошло.", xmlFile.getName(), renameToFile.getName()));
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
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
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

    private static void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
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

    private static void exportPDF(JasperPrint jasperPrint, File pdfFile) {
        OutputStream data = null;
        try {
            data = new FileOutputStream(pdfFile);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, data);
            exporter.getPropertiesUtil().setProperty(JRPdfExporterParameter.PROPERTY_SIZE_PAGE_TO_CONTENT, "true");

            exporter.exportReport();
        } catch (Exception e) {
            throw new ServiceException("Невозможно экспортировать отчет в PDF", e);
        } finally {
            IOUtils.closeQuietly(data);
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
        File jasperPrintFile = null;
        try {
            jasperPrintFile = File.createTempFile("jasperPrint",".dat");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(jasperPrintFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(jasperPrint);
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }
            return blobDataService.create(jasperPrintFile.getPath(), "");
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
                        drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s", formatter.format(drp.getCorrectionDate())) : "",
                        taKPPString.toString())));
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
    public void deleteReport(long declarationDataId, int userId, boolean isCalc, String cause) {
        ReportType[] reportTypes = {ReportType.XML_DEC, ReportType.PDF_DEC, ReportType.EXCEL_DEC, ReportType.CHECK_DEC, ReportType.ACCEPT_DEC};
        for (ReportType reportType : reportTypes) {
            if (!isCalc || !ReportType.XML_DEC.equals(reportType)) {
                LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, reportType));
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
    private ReportType[] getCheckTaskList(ReportType reportType) {
        switch (reportType) {
            case XML_DEC:
                return new ReportType[]{ReportType.PDF_DEC, ReportType.EXCEL_DEC, ReportType.CHECK_DEC, ReportType.ACCEPT_DEC};
            case ACCEPT_DEC:
                return new ReportType[]{ReportType.CHECK_DEC};
            default:
                return null;
        }
    }

    @Override
    public boolean checkExistTask(long declarationDataId, ReportType reportType, Logger logger) {
        ReportType[] reportTypes = getCheckTaskList(reportType);
        if (reportTypes == null) return false;
        DeclarationData declarationData = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        boolean exist = false;
        for (ReportType type : reportTypes) {
            LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, type));
            if (lock != null) {
                exist = true;
                if (LockData.State.IN_QUEUE.getText().equals(lock.getState())) {
                    logger.info(LockData.CANCEL_TASK_NOT_PROGRESS,
                            SDF_DD_MM_YYYY_HH_MM_SS.format(lock.getDateLock()),
                            taUserService.getUser(lock.getUserId()).getName(),
                            getTaskName(type, declarationTemplate.getType().getTaxType()));
                } else {
                    logger.info(LockData.CANCEL_TASK_IN_PROGRESS,
                            SDF_DD_MM_YYYY_HH_MM_SS.format(lock.getDateLock()),
                            taUserService.getUser(lock.getUserId()).getName(),
                            getTaskName(type, declarationTemplate.getType().getTaxType()));
                }
            }
        }
        return exist;
    }

    @Override
    public void interruptTask(long declarationDataId, int userId, ReportType reportType, String cause) {
        ReportType[] reportTypes = getCheckTaskList(reportType);
        if (reportTypes == null) return;
        for (ReportType type : reportTypes) {
            LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, type));
            if (lock != null) {
                lockDataService.interruptTask(lock, userId, true, cause);
            }
        }
        if (ReportType.XML_DEC.equals(reportType)) {
            reportService.deleteDec(declarationDataId);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void cleanBlobs(Collection<Long> ids, List<ReportType> reportTypes) {
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
            return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                    declarationTemplate.getType().getTaxType() == TaxType.DEAL ? "Уведомление" : "Декларация",
                    reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    reportPeriod.getCorrectionDate() != null
                            ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                            : "",
                    department.getName(),
                    declarationTemplate.getType().getName(),
                    declaration.getTaxOrganCode() != null
                            ? ", Налоговый орган: \"" + declaration.getTaxOrganCode() + "\""
                            : "",
                    declaration.getKpp() != null
                            ? ", КПП: \"" + declaration.getKpp() + "\""
                            : "");

        switch (reportType) {
            case EXCEL_DEC:
            case PDF_DEC:
            case XML_DEC:
            case CHECK_DEC:
            case ACCEPT_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        getTaskName(reportType, declarationTemplate.getType().getTaxType()),
                        reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                        reportPeriod.getCorrectionDate() != null
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
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
                                ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
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
    public Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, ReportType reportType) {
        switch (reportType) {
            case PDF_DEC:
            case EXCEL_DEC:
                String uuidXmlReport = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
                if (uuidXmlReport != null) {
                    return blobDataService.getLength(uuidXmlReport)/1024;
                } else {
                    return null;
                }
            case ACCEPT_DEC:
            case CHECK_DEC:
                String uuidXml = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
                if (uuidXml != null) {
                    return blobDataService.getLength(uuidXml)/1024;
                } else {
                    return null;
                }
            case XML_DEC:
                long cellCountSource = 0;
                DeclarationData declarationData = get(declarationDataId, userInfo);
                for (Relation relation : sourceService.getDeclarationSourcesInfo(declarationData, true, true, null, userInfo, new Logger())){
                    if (relation.isCreated() && relation.getState() == WorkflowState.ACCEPTED) {
                        FormData formData = formDataDao.getWithoutRows(relation.getFormDataId());
                        int rowCountSource = dataRowDao.getRowCount(formData);
                        int columnCountSource = formTemplateService.get(formData.getFormTemplateId()).getColumns().size();
                        cellCountSource += rowCountSource * columnCountSource;
                    }
                }
                return cellCountSource;
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
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
                                    formatter.format(relation.getCorrectionDate())) : "");
                } else if (!sourceService.isDeclarationSourceConsolidated(dd.getId(), relation.getFormDataId())){
                    consolidationOk = false;
                    logger.warn(NOT_CONSOLIDATE_SOURCE_DECLARATION_WARNING,
                            relation.getFullDepartmentName(),
                            relation.getFormTypeName(),
                            relation.getFormDataKind().getTitle(),
                            relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    formatter.format(relation.getCorrectionDate())) : "",
                            relation.getState().getTitle());
                }
            }

            if (!relations.isEmpty() && consolidationOk){
                logger.info("Консолидация выполнена из всех форм-источников.");
            }
        }
    }

    @Override
    public String getTaskName(ReportType reportType, TaxType taxType) {
        switch (reportType) {
            case CHECK_DEC:
            case ACCEPT_DEC:
            case EXCEL_DEC:
            case XML_DEC:
            case PDF_DEC:
                return String.format(reportType.getDescription(), taxType.getDeclarationShortName());
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
        }
    }
}
