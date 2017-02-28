package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
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
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
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
            "консолидация). Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\"";
    private static final int DEFAULT_TF_FILE_TYPE_CODE = 1;

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
    private FormDataDao formDataDao;
    @Autowired
    private DataRowDao dataRowDao;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TAUserService userService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private BDUtils bdUtils;

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
    public Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                       DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo, Long asunId, String fileName, String note) {
        String key = LockData.LockObjects.DECLARATION_CREATE.name() + "_" + declarationTemplateId + "_" + departmentReportPeriod.getId() + "_" + taxOrganKpp + "_" + taxOrganCode;
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
        if (lockDataService.lock(key, userInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        String.format("Создание %s", declarationTemplate.getType().getTaxType().getDeclarationShortName()),
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

                declarationDataAccessService.checkEvents(userInfo, declarationTemplateId, departmentReportPeriod,
                        FormDataEvent.CREATE, logger);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException(
                            (declarationTemplate.getType().getTaxType().equals(TaxType.DEAL) ? "Уведомление не создано" : "Налоговая форма не создана"),
                            logEntryService.save(logger.getEntries()));
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
                auditService.add(FormDataEvent.CREATE, userInfo, newDeclaration, null, "Налоговая форма создана", null);
                return id;
            } finally {
                lockDataService.unlock(key, userInfo.getUser().getId());
            }
        } else {
            throw new ServiceException("Создание налоговой формы с указанными параметрами уже выполняется!");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void calculate(Logger logger, long id, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        calculateDeclaration(logger, id, userInfo, docDate, exchangeParams, stateLogger);
        declarationDataDao.setStatus(id, State.CREATED);
    }

    private void calculateDeclaration(Logger logger, long id, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.CALCULATE);
        DeclarationData declarationData = declarationDataDao.get(id);

        //2. проверяет состояние XML отчета экземпляра декларации
        setDeclarationBlobs(logger, declarationData, docDate, userInfo, exchangeParams, stateLogger);

        //3. обновляет записи о консолидации
        ArrayList<Long> declarationDataIds = new ArrayList<Long>();
        for (Relation relation : sourceService.getDeclarationSourcesInfo(declarationData, true, true, State.ACCEPTED, userInfo, logger)){
            declarationDataIds.add(relation.getDeclarationDataId());
        }

        //Обновление информации о консолидации.
        sourceService.deleteDeclarationConsolidateInfo(id);
        sourceService.addDeclarationConsolidationInfo(id, declarationDataIds);

        logBusinessService.add(null, id, userInfo, FormDataEvent.SAVE, null);
        auditService.add(FormDataEvent.CALCULATE , userInfo, declarationData, null, "Налоговая форма обновлена", null);

    }

    @Override
    public void check(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        LOG.info(String.format("Проверка данных налоговой формы %s", id));
        DeclarationData dd = declarationDataDao.get(id);
        Logger scriptLogger = new Logger();
        try {
            lockStateLogger.updateState("Проверка форм-источников");
            checkSources(dd, logger, userInfo);
            lockStateLogger.updateState("Проверка данных налоговой формы");
            declarationDataScriptingService.executeScript(userInfo, dd, FormDataEvent.CHECK, scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            if (departmentReportPeriodService.get(dd.getDepartmentReportPeriodId()).isActive()) {
                declarationDataDao.setStatus(id, State.CREATED);
            }
        } else {
            if (departmentReportPeriodService.get(dd.getDepartmentReportPeriodId()).isActive()) {
                if (State.CREATED.equals(dd.getState())) {
                    // Переводим в состояние подготовлено
                    declarationDataDao.setStatus(id, State.PREPARED);
                }
            }
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
    @Transactional(readOnly = false)
    public void delete(long id, TAUserInfo userInfo) {
        LockData lockData = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.XML_DEC));
        LockData lockDataAccept = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.ACCEPT_DEC));
        LockData lockDataCheck = lockDataService.getLock(generateAsyncTaskKey(id, DeclarationDataReportType.CHECK_DEC));
        if (lockData == null && lockDataAccept == null && lockDataCheck == null) {
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

            auditService.add(FormDataEvent.DELETE , userInfo, declarationData, null, "Налоговая форма удалена", null);
        } else {
            if (lockData == null) lockData = lockDataAccept;
            if (lockData == null) lockData = lockDataCheck;
            Logger logger = new Logger();
            TAUser blocker = taUserService.getUser(lockData.getUserId());
            logger.error("Текущая налоговая форма не может быть удалена, т.к. пользователем \"%s\" в \"%s\" запущена операция \"%s\"", blocker.getName(), SDF_DD_MM_YYYY_HH_MM_SS.get().format(lockData.getDateLock()), lockData.getDescription());
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void accept(Logger logger, long id, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);

        DeclarationData declarationData = declarationDataDao.get(id);

        Logger scriptLogger = new Logger();
        try {
            lockStateLogger.updateState("Проверка форм-источников");
            checkSources(declarationData, logger, userInfo);
            lockStateLogger.updateState("Проверка данных налоговой формы");
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_CREATED_TO_ACCEPTED , scriptLogger, null);
        } finally {
            logger.getEntries().addAll(scriptLogger.getEntries());
        }
        if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceException();
        }

        declarationData.setState(State.ACCEPTED);

        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, null);
        auditService.add(FormDataEvent.MOVE_CREATED_TO_ACCEPTED, userInfo, declarationData, null, FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getTitle(), null);

        lockStateLogger.updateState("Изменение состояния налоговой формы");

        declarationDataDao.setStatus(id, declarationData.getState());
    }

    @Override
    @Transactional(readOnly = false)
    public void cancel(Logger logger, long id, TAUserInfo userInfo) {
        DeclarationData declarationData = declarationDataDao.get(id);
        /*checkSources(declarationData, logger);*/

        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger, exchangeParams);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
        }

        declarationData.setState(State.CREATED);
        sourceService.updateDDConsolidation(declarationData.getId());

        logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, null);
        auditService.add(FormDataEvent.MOVE_ACCEPTED_TO_CREATED, userInfo, declarationData, null, FormDataEvent.MOVE_ACCEPTED_TO_CREATED.getTitle(), null);

        declarationDataDao.setStatus(id, declarationData.getState());
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
                LOG.info(String.format("Заполнение Jasper-макета налоговой формы %s", declarationData.getId()));
                stateLogger.updateState("Заполнение Jasper-макета");
                jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 4096, 1000);
                jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);
                // для XLSX-отчета не сохраняем Jasper-отчет из-за возмжных проблем с паралельным формированием PDF-отчета
            }
            LOG.info(String.format("Заполнение XLSX-отчета налоговой формы %s", declarationData.getId()));
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
            throw new ServiceException("Ошибка компиляции jrxml-шаблона! "+jrxml);
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
        LOG.info(String.format("Получение данных налоговой формы %s", declarationData.getId()));
        stateLogger.updateState("Получение данных налоговой формы");
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC);
        if (xmlUuid != null) {
            File pdfFile = null;
            JRSwapFile jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 100);
            try {                
                LOG.info(String.format("Заполнение Jasper-макета налоговой формы %s", declarationData.getId()));
                stateLogger.updateState("Заполнение Jasper-макета");
                JasperPrint jasperPrint = createJasperReport(declarationData, jrSwapFile, userInfo);

                LOG.info(String.format("Заполнение PDF-файла налоговой формы %s", declarationData.getId()));
                stateLogger.updateState("Заполнение PDF-файла");
                pdfFile = File.createTempFile("report", ".pdf");

                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(pdfFile);
                    exportPDF(jasperPrint, outputStream);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }

                LOG.info(String.format("Сохранение PDF-файла в базе данных для налоговой формы %s", declarationData.getId()));
                stateLogger.updateState("Сохранение PDF-файла в базе данных");
                reportService.createDec(declarationData.getId(), blobDataService.create(pdfFile.getPath(), ""), DeclarationDataReportType.PDF_DEC);

                // не сохраняем jasper-отчет, если есть XLSX-отчет
                if (reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.EXCEL_DEC) == null) {
                    LOG.info(String.format("Сохранение Jasper-макета в базе данных для налоговой формы %s", declarationData.getId()));
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
            throw new ServiceException("Налоговая форма не сформирована");
        }
    }

    @Override
    public String createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, DataRow<Cell> selectedRecord, TAUserInfo userInfo, LockStateLogger stateLogger) {
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
        File xlsxFile = null;
        try {
            xlsxFile = File.createTempFile("report", ".xlsx");
            getXlsxData(declarationData.getId(), xlsxFile, userInfo, stateLogger);

            LOG.info(String.format("Сохранение XLSX в базе данных для налоговой формы %s", declarationData.getId()));
            stateLogger.updateState("Сохранение XLSX в базе данных");

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

    // расчет декларации
    private void setDeclarationBlobs(Logger logger,
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
                stateLogger.updateState("Создание временного файла для записи расчета");
                try {
                    xmlFile = File.createTempFile("file_for_validate", ".xml");
                    fileWriter = new FileWriter(xmlFile);
                    fileWriter.write(XML_HEADER);
                } catch (IOException e) {
                    throw new ServiceException("Ошибка при формировании временного файла для XML", e);
                }
                exchangeParams.put(DeclarationDataScriptParams.XML, fileWriter);
                LOG.info(String.format("Формирование XML-файла налоговой формы %s", declarationData.getId()));
                stateLogger.updateState("Формирование XML-файла");
                declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceException();
                }
            } finally {
                IOUtils.closeQuietly(fileWriter);
            }

            boolean notReplaceXml = false;
            if (params.containsKey(DeclarationDataScriptParams.NOT_REPLACE_XML) && params.get(DeclarationDataScriptParams.NOT_REPLACE_XML) != null) {
                notReplaceXml = (Boolean)params.get(DeclarationDataScriptParams.NOT_REPLACE_XML);
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
                if (decDate == null)
                    decDate = docDate;

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
                    stateLogger.updateState("Сохранение XML-файла в базе данных");

                    reportService.deleteDec(Arrays.asList(declarationData.getId()), Arrays.asList(DeclarationDataReportType.XML_DEC));
                    reportService.createDec(declarationData.getId(),
                            blobDataService.create(zipOutFile, decName + ".zip", decDate),
                            DeclarationDataReportType.XML_DEC);
                    declarationDataDao.setFileName(declarationData.getId(), decName);
                } finally {
                    if (zipOutFile != null && !zipOutFile.delete()) {
                        LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                    }
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
            String declarationName = "налоговой формы";
            String operationName = operation == FormDataEvent.MOVE_CREATED_TO_ACCEPTED ? "Принять" : operation.getTitle();
            logger.error("В %s отсутствуют данные (не был выполнен расчет). Операция \"%s\" не может быть выполнена", declarationName, operationName);
        } else {
            validateDeclaration(userInfo, declarationData, logger, isErrorFatal, operation, null, null, lockStateLogger);
        }
    }

    @Override
    public void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation, File xmlFile, String xsdBlobDataId, LockStateLogger stateLogger) {
        Locale oldLocale = Locale.getDefault();
        LOG.info(String.format("Получение данных налоговой формы %s", declarationData.getId()));
        Locale.setDefault(new Locale("ru", "RU"));
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

        if (xsdBlobDataId == null) {
            xsdBlobDataId = declarationTemplate.getXsdId();
        }
        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().isEmpty()) {
            try {
                LOG.info(String.format("Выполнение проверок XSD-файла налоговой формы %s", declarationData.getId()));
                stateLogger.updateState("Выполнение проверок XSD-файла");
                boolean valid = validateXMLService.validate(declarationData, userInfo, logger, isErrorFatal, xmlFile, xsdBlobDataId);
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
            jasperPrintFile = File.createTempFile("report",".jasper");
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
    public void deleteReport(long declarationDataId, TAUserInfo userInfo, boolean isCalc, TaskInterruptCause cause) {
        DeclarationDataReportType[] ddReportTypes = {DeclarationDataReportType.XML_DEC, DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC};
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                DeclarationData declarationData = declarationDataDao.get(declarationDataId);
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for(DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                    if (lock != null)
                        lockDataService.interruptTask(lock, userInfo, true, cause);
                }
            } else if (!isCalc || !DeclarationDataReportType.XML_DEC.equals(ddReportType)) {
                LockData lock = lockDataService.getLock(generateAsyncTaskKey(declarationDataId, ddReportType));
                if (lock != null)
                    lockDataService.interruptTask(lock, userInfo, true, cause);
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
    public void interruptTask(long declarationDataId, TAUserInfo userInfo, ReportType reportType, TaskInterruptCause cause) {
        DeclarationDataReportType[] ddReportTypes = getCheckTaskList(reportType);
        if (ddReportTypes == null) return;
        DeclarationData declarationData = get(declarationDataId, userInfo);
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
                    lockDataService.interruptTask(lock, userInfo, true, cause);
                }
            }
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
                    dd.getState().getTitle());
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
                    "налоговая форма",
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
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
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
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                                : "");
            case DELETE_DEC:
                return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        "налоговой формы",
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
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
                                : "");
            default:
                return String.format(LockData.DescriptionTemplate.DECLARATION_TASK.getText(),
                        "Налоговая форма",
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
                                : "",
                        declaration.getOktmo() != null
                                ? ", ОКТМО: \"" + declaration.getOktmo() + "\""
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
                            relation.getDeclarationTemplate().getName(),
                            relation.getDeclarationTemplate().getDeclarationFormKind().getTitle(),
                            relation.getPeriodName(),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    sdf.get().format(relation.getCorrectionDate())) : "");
                } else if (!sourceService.isDeclarationSourceConsolidated(dd.getId(), relation.getDeclarationDataId())){
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

    @Override
    public String getTaskName(ReportType reportType, TaxType taxType, Map<String, Object> params) {
        switch (reportType) {
            case CREATE_FORMS_DEC:
            case CREATE_REPORTS_DEC:
                return reportType.getDescription();
        }
        throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
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
                return (Boolean)(params.get("isVisiblePDF"));
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private static String getFileName(String filename){
        int dotPos = filename.lastIndexOf('.');
        if (dotPos < 0) {
            return filename;
        }
        return filename.substring(0, dotPos);
    }

    @Override
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, long declarationDataId, InputStream inputStream,
                                      String fileName, FormDataEvent formDataEvent, LockStateLogger stateLogger, File dataFile,
                                      AttachFileType fileType, Date createDateFile) {
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
                        stateLogger.updateState("Сохранение XML-файла в базе данных");
                    }

                    createDateFile = createDateFile == null ? new Date() : createDateFile;
                    fileUuid = blobDataService.create(zipOutFile, getFileName(fileName) + ".zip", createDateFile);

                    reportService.deleteDec(declarationData.getId());
                    reportService.createDec(declarationData.getId(), fileUuid, DeclarationDataReportType.XML_DEC);
                } finally {
                    if (zipOutFile != null && !zipOutFile.delete()) {
                        LOG.warn(String.format(FILE_NOT_DELETE, zipOutFile.getAbsolutePath()));
                    }
                }
            } else {
                fileUuid = blobDataService.create(dataFile, fileName, new Date());
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
                auditService.add(formDataEvent, userInfo, declarationData, null, note, null);
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

        parameters.put(JRXPathQueryExecuterFactory.XML_INPUT_STREAM, xmlData);

        ByteArrayInputStream inputStream = compileReport(jrxmlTemplate);

        try {
            return JasperFillManager.fillReport(inputStream, parameters, connection);
        } catch (JRException e) {
            throw new ServiceException("Ошибка при вычислении отчета!", e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void createForms(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        Map<Long, Map<String, Object>> formMap = new HashMap<Long, Map<String, Object>>();
        additionalParameters.put("formMap", formMap);
        DeclarationData declarationDataTemp = new DeclarationData();
        declarationDataTemp.setDeclarationTemplateId(declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId()));
        declarationDataTemp.setDepartmentReportPeriodId(departmentReportPeriod.getId());
        declarationDataScriptingService.executeScript(userInfo, declarationDataTemp, FormDataEvent.CREATE_FORMS, logger, additionalParameters);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("Обнаружены фатальные ошибки!");
        }

        int success = 0;
        int fail = 0;
        List<String> oktmoKppList = new ArrayList<String>();
        for (Map.Entry<Long, Map<String, Object>> entry: formMap.entrySet()) {
            Logger scriptLogger = new Logger();
            try {
                calculateDeclaration(scriptLogger, entry.getKey(), userInfo, new Date(), entry.getValue(), stateLogger);
            } catch (Exception e) {
                scriptLogger.error(e);
            } finally {
                if (scriptLogger.containsLevel(LogLevel.ERROR)) {
                    fail++;
                    DeclarationData declarationData = get(entry.getKey(), userInfo);
                    oktmoKppList.add(String.format("ОКТМО: %s, КПП: %s.", declarationData.getOktmo(), declarationData.getKpp()));
                    logger.error("Произошла непредвиденная ошибка при расчете для " + getDeclarationFullName(entry.getKey(), null));
                    logger.getEntries().addAll(scriptLogger.getEntries());
                    declarationDataDao.delete(entry.getKey());
                } else {
                    success++;
                    logger.info("Успешно выполнена расчет для " + getDeclarationFullName(entry.getKey(), null));
                    logger.getEntries().addAll(scriptLogger.getEntries());
                }
            }
        }
        logger.info("Успешно созданных форм: %d. Не удалось создать форм: %d.", success, fail);
        if (!oktmoKppList.isEmpty()) {
            logger.info("Не удалось создать формы со следующими парметрами:");
            for(String oktmoKpp: oktmoKppList) {
                logger.warn(oktmoKpp);
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
            stateLogger.updateState("Сохранение отчета в базе данных");
            return blobDataService.create(reportFile.getPath(), fileName);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (reportFile != null)
                reportFile.delete();
        }

    }
}
