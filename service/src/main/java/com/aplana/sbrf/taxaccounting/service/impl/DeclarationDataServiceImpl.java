package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    private DeclarationDataDao declarationDataDao;

    @Autowired
    private DeclarationDataAccessService declarationDataAccessService;

    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private PeriodService periodService;

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

    private static final String DD_NOT_IN_RANGE = "Найдена форма: %s %d %s, %s, состояние - %s";

    public static final String TAG_FILE = "Файл";
	public static final String TAG_DOCUMENT = "Документ";
	public static final String ATTR_FILE_ID = "ИдФайл";
	public static final String ATTR_DOC_DATE = "ДатаДок";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private static final String VALIDATION_ERR_MSG = "Обнаружены фатальные ошибки!";
    public static final String MSG_IS_EXIST_DECLARATION =
            "Существует экземпляр декларации \"%s\" в подразделении \"%s\" в периоде \"%s\" %d%s для макета";

    private static final Date MAX_DATE;
    private static final Calendar CALENDAR = Calendar.getInstance();
    static {
        CALENDAR.clear();
        CALENDAR.set(9999, Calendar.DECEMBER, 31);
        MAX_DATE = CALENDAR.getTime();
        CALENDAR.clear();
    }


    private class SAXHandler extends DefaultHandler {
        private List<String> values;
        private String tagName;
        private String attrName;

        private SAXHandler(){}

        public SAXHandler(String tagName, String attrName) {
            this.tagName = tagName;
            this.attrName = attrName;
        }


        public List<String> getValues() {
            return values;
        }

        @Override
        public void startDocument() throws SAXException {
            values = new ArrayList<String>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (tagName.equals(qName)) {
                values.add(attributes.getValue(attrName));
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
    public void calculate(Logger logger, long id, TAUserInfo userInfo, Date docDate) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.CALCULATE);
        DeclarationData declarationData = declarationDataDao.get(id);

        setDeclarationBlobs(logger, declarationData, docDate, userInfo);

        logBusinessService.add(null, id, userInfo, FormDataEvent.SAVE, null);
        auditService.add(FormDataEvent.CALCULATE , userInfo, declarationData.getDepartmentId(),
                declarationData.getReportPeriodId(),
                declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
				null, null, "Декларация обновлена", null, null);
    }

    @Override
    public void check(Logger logger, long id, TAUserInfo userInfo) {
        declarationDataScriptingService.executeScript(userInfo,
                declarationDataDao.get(id), FormDataEvent.CHECK, logger, null);
        validateDeclaration(userInfo, declarationDataDao.get(id), logger, true, FormDataEvent.CHECK);
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
    public DeclarationData get(long id, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        return declarationDataDao.get(id);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(long id, TAUserInfo userInfo) {
        LockData lockData = lock(id, userInfo);
        if (lockData == null) {
            try {
                declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.DELETE);
                DeclarationData declarationData = declarationDataDao.get(id);

                declarationDataDao.delete(id);

                    auditService.add(FormDataEvent.DELETE , userInfo, declarationData.getDepartmentId(),
                            declarationData.getReportPeriodId(),
                            declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                            null, null, "Декларация удалена", null, null);
            } finally {
                unlock(id, userInfo);
            }
        } else {
            throw new ServiceException(String.format(LockDataService.LOCK_DATA, taUserService.getUser(lockData.getUserId()).getName(), lockData.getUserId()));
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void setAccepted(Logger logger, long id, boolean accepted, TAUserInfo userInfo) {
        if (lock(id, userInfo) == null) {
            try {
                // TODO (sgoryachkin) Это 2 метода должо быть
                if (accepted) {
                    DeclarationData declarationData = declarationDataDao.get(id);

                    Map<String, Object> exchangeParams = new HashMap<String, Object>();
                    declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, logger, exchangeParams);

                    validateDeclaration(userInfo, declarationDataDao.get(id), logger, true, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);
                    declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);

                    declarationData.setAccepted(true);

                    String declarationTypeName = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName();
                    logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, null);
                    auditService.add(FormDataEvent.MOVE_CREATED_TO_ACCEPTED, userInfo, declarationData.getDepartmentId(),
                            declarationData.getReportPeriodId(), declarationTypeName, null, null, FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getTitle(), null, null);
                } else {
                    declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);

                    DeclarationData declarationData = declarationDataDao.get(id);
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

                }
                declarationDataDao.setAccepted(id, accepted);
            } finally {
                unlock(id, userInfo);
            }
        } else {
            throw new ServiceException("Декларация заблокирована и не может быть принята. Попробуйте выполнить операцию позже");
        }
    }

	@Override
	public String getXmlData(long declarationId, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, declarationId, FormDataEvent.GET_LEVEL1);
        String xmlUuid = reportService.getDec(userInfo, declarationId, ReportType.XML_DEC);
        if (xmlUuid == null) return null;
        return new String(getBytesFromInputstream(xmlUuid));
	}

    @Override
    public String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            String xmlUuid = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
            if (xmlUuid == null) return null;
            BlobData blobData = blobDataService.get(xmlUuid);
            SAXHandler handler = new SAXHandler(TAG_FILE, ATTR_FILE_ID);
            saxParser.parse(blobData.getInputStream(), handler);
            return handler.getValues().get(0);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

    @Override
    public Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            String xmlUuid = reportService.getDec(userInfo, declarationDataId, ReportType.XML_DEC);
            if (xmlUuid == null) return null;
            BlobData blobData = blobDataService.get(xmlUuid);
            SAXHandler handler = new SAXHandler(TAG_DOCUMENT, ATTR_DOC_DATE);
            saxParser.parse(blobData.getInputStream(), handler);
            return getFormattedDate(handler.getValues().get(0));
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

    @Override
    public byte[] getXlsxData(long id, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        try {
            DeclarationData declarationData = declarationDataDao.get(id);
            String uuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.JASPER_DEC);
            ObjectInputStream objectInputStream = new ObjectInputStream(blobDataService.get(uuid).getInputStream());
            JasperPrint jasperPrint = (JasperPrint)objectInputStream.readObject();
            return exportXLSX(jasperPrint);
        } catch (Exception e) {
            throw new ServiceException("Не удалось извлечь объект для печати.", e);
        }
    }

	@Override
	public byte[] getPdfData(long id, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        return getBytesFromInputstream(reportService.getDec(userInfo, id, ReportType.PDF_DEC));
	}

    // расчет декларации
    private void setDeclarationBlobs(Logger logger,
                                     DeclarationData declarationData, Date docDate, TAUserInfo userInfo) {

        Map<String, Object> exchangeParams = new HashMap<String, Object>();
        exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, docDate);
        StringWriter writer = new StringWriter();
        exchangeParams.put(DeclarationDataScriptParams.XML, writer);

        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.CALCULATE, logger, exchangeParams);

        String xml = XML_HEADER.concat(writer.toString());

        reportService.createDec(declarationData.getId(), blobDataService.create(new ByteArrayInputStream(xml.getBytes()), ""), ReportType.XML_DEC);

        validateDeclaration(userInfo, declarationData, logger, false, FormDataEvent.CALCULATE);
        // Заполнение отчета и экспорт в формате PDF
        JasperPrint jasperPrint = fillReport(xml,
                declarationTemplateService.getJasper(declarationData.getDeclarationTemplateId()));

        reportService.createDec(declarationData.getId(), blobDataService.create(new ByteArrayInputStream(exportPDF(jasperPrint)), ""), ReportType.PDF_DEC);
        try {
            reportService.createDec(declarationData.getId(), saveJPBlobData(jasperPrint), ReportType.JASPER_DEC);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    private void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                                     FormDataEvent operation) {
        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("ru", "RU"));
        String xmlUuid = reportService.getDec(userInfo, declarationData.getId(), ReportType.XML_DEC);
        if (xmlUuid == null) {
            TaxType taxType = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getTaxType();
            String declarationName = (taxType == TaxType.DEAL ? "уведомлении" : "декларации");
            String operationName = (operation == FormDataEvent.MOVE_CREATED_TO_ACCEPTED ? "Принять" : operation.getTitle());
            String msg = String.format("В %s отсутствуют данные (не был выполнен расчет). Операция \"%s\" не может быть выполнена", declarationName, operationName);
            throw new ServiceException(msg);
        }
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());

        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().isEmpty()) {
            try {
                validateXMLService.validate(declarationData, userInfo, logger, isErrorFatal);
            } catch (Exception e) {
                log.error(VALIDATION_ERR_MSG, e);
                logger.error(e);
                Locale.setDefault(oldLocale);
                throw new ServiceLoggerException(VALIDATION_ERR_MSG, logEntryService.save(logger.getEntries()));
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                Locale.setDefault(oldLocale);
                throw new ServiceLoggerException(VALIDATION_ERR_MSG, logEntryService.save(logger.getEntries()));
            }

            Locale.setDefault(oldLocale);
        }
    }

    private static JasperPrint fillReport(String xml, InputStream jasperTemplate) {
        try {
            InputSource inputSource = new InputSource(new StringReader(xml));
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
                ReportPeriod period = periodService.getReportPeriod(declarationData.getReportPeriodId());
                DepartmentReportPeriod drp = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId());

                logs.add(new LogEntry(LogLevel.ERROR, String.format(MSG_IS_EXIST_DECLARATION,
                        declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                        departmentService.getDepartment(departmentId).getName(),
                        period.getName(),
                        period.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                formatter.format(drp.getCorrectionDate())) : "")));
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
        return LockData.LockObjects.DECLARATION_DATA.name() + "_" + declarationDataId + "_" + reportType.getName();
    }

    @Override
    @Transactional
    public LockData lock(long declarationDataId, TAUserInfo userInfo) {
        LockData lockData = lockDataService.lock(generateAsyncTaskKey(declarationDataId, ReportType.XML_DEC), userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME);
        checkLock(lockData, userInfo.getUser());
        return lockData;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void unlock(final long declarationDataId, final TAUserInfo userInfo) {
        lockDataService.unlock(generateAsyncTaskKey(declarationDataId, ReportType.XML_DEC), userInfo.getUser().getId());
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
     * @param declarationDataId
     */
    @Override
    public void deleteReport(long declarationDataId, boolean isLock) {
        if (isLock) {
            ReportType[] reportTypes = {ReportType.XML_DEC, ReportType.EXCEL_DEC};
            for (ReportType reportType : reportTypes) {
                lockDataService.unlock(generateAsyncTaskKey(declarationDataId, reportType), 0, true);
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
                    dt.getName(), dd.isAccepted()?"принята":"не принята");
        }
    }
}
