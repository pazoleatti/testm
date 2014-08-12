package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.Charset;
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

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>";

    public static final String MSG_IS_EXIST_DECLARATION = "Существует экземпляр %s в подразделении %s периоде %s";

	@Autowired
	private DeclarationDataDao declarationDataDao;

	@Autowired
	private DeclarationDataAccessService declarationDataAccessService;

	@Autowired
	private DeclarationDataScriptingService declarationDataScriptingService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private BlobDataService blobDataService;

	@Autowired
	private LogBusinessService logBusinessService;

	@Autowired
	private AuditService auditService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockCoreService lockCoreService;

    @Autowired
    private TransactionHelper tx;

    @Autowired
    private TAUserDao userDao;

	public static final String TAG_FILE = "Файл";
	public static final String TAG_DOCUMENT = "Документ";
	public static final String ATTR_FILE_ID = "ИдФайл";
	public static final String ATTR_DOC_DATE = "ДатаДок";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private static final String VALIDATION_ERR_MSG = "Декларация / Уведомление не может быть создана, т.к. шаблон некорректен. Обратитесь к настройщику шаблонов";

	@Override
	@Transactional(readOnly = false)
	public long create(Logger logger, int declarationTemplateId, int departmentId, TAUserInfo userInfo, int reportPeriodId) {
		declarationDataAccessService.checkEvents(userInfo, declarationTemplateId, departmentId, reportPeriodId, FormDataEvent.CREATE);

        DeclarationData newDeclaration = new DeclarationData();
        newDeclaration.setDepartmentId(departmentId);
        newDeclaration.setReportPeriodId(reportPeriodId);
        newDeclaration.setAccepted(false);
        newDeclaration.setDeclarationTemplateId(declarationTemplateId);

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
				declarationTemplateDao.get(newDeclaration.getDeclarationTemplateId()).getType().getName(),
				null, null, null, null);
		return id;
	}

	@Override
	@Transactional(readOnly = false)
	public void calculate(Logger logger, long id, TAUserInfo userInfo, Date docDate) {
		declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.CALCULATE);
        DeclarationData declarationData = declarationDataDao.get(id);
        List<String> strings = new ArrayList<String>();

        //Обнуляем данные в таблице блобов
        if (declarationData.getJasperPrintUuid() != null){
            strings.add(declarationData.getJasperPrintUuid());
            declarationData.setJasperPrintUuid(null);
        }
        if (declarationData.getXlsxDataUuid() != null){
            strings.add(declarationData.getXlsxDataUuid());
            declarationData.setXlsxDataUuid(null);
        }
        if (declarationData.getPdfDataUuid() != null){
            strings.add(declarationData.getPdfDataUuid());
            declarationData.setPdfDataUuid(null);
        }
        if (declarationData.getXmlDataUuid() != null){
            strings.add(declarationData.getXmlDataUuid());
            declarationData.setXmlDataUuid(null);
        }
        declarationDataDao.update(declarationData);
        if (!strings.isEmpty()) blobDataService.delete(strings);

		setDeclarationBlobs(logger, declarationData, docDate, userInfo);
		logBusinessService.add(null, id, userInfo, FormDataEvent.SAVE, null);
		auditService.add(FormDataEvent.SAVE , userInfo, declarationData.getDepartmentId(),
				declarationData.getReportPeriodId(),
				declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getType().getName(),
				null, null, null, null);
	}

	@Override
	public void check(Logger logger, long id, TAUserInfo userInfo) {
        declarationDataScriptingService.executeScript(userInfo,
                declarationDataDao.get(id), FormDataEvent.CHECK, logger, null);
        validateDeclaration(declarationDataDao.get(id), logger, true);
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
        checkLockedMe(id, userInfo);
		declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.DELETE);
			DeclarationData declarationData = declarationDataDao.get(id);

			declarationDataDao.delete(id);

			auditService.add(FormDataEvent.DELETE , userInfo, declarationData.getDepartmentId(),
					declarationData.getReportPeriodId(),
					declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getType().getName(),
					null, null, null, null);

	}

	@Override
	@Transactional(readOnly = false)
	public void setAccepted(Logger logger, long id, boolean accepted, TAUserInfo userInfo) {
        checkLockedMe(id, userInfo);
		// TODO (sgoryachkin) Это 2 метода должо быть
		if (accepted) {
            DeclarationData declarationData  = declarationDataDao.get(id);

            Map<String, Object> exchangeParams = new HashMap<String, Object>();
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, logger, exchangeParams);

            validateDeclaration(declarationDataDao.get(id), logger, true);
            declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);

            declarationData.setAccepted(true);

            String declarationTypeName = declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getType().getName();
            logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, null);
            auditService.add(FormDataEvent.MOVE_CREATED_TO_ACCEPTED , userInfo, declarationData.getDepartmentId(),
                    declarationData.getReportPeriodId(), declarationTypeName, null, null, null, null);
		} else {
			declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);

			DeclarationData declarationData  = declarationDataDao.get(id);
			declarationData.setAccepted(false);

			Map<String, Object> exchangeParams = new HashMap<String, Object>();
			declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger, exchangeParams);

            String declarationTypeName = declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getType().getName();
			logBusinessService.add(null, id, userInfo, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, null);
			auditService.add(FormDataEvent.MOVE_ACCEPTED_TO_CREATED , userInfo, declarationData.getDepartmentId(),
					declarationData.getReportPeriodId(), declarationTypeName, null, null, null, null);

		}
		declarationDataDao.setAccepted(id, accepted);
	}

	@Override
	public String getXmlData(long declarationId, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, declarationId, FormDataEvent.GET_LEVEL1);
        String xmlUuid = declarationDataDao.get(declarationId).getXmlDataUuid();
		return new String(getBytesFromInputstream(xmlUuid));
	}

	@Override
	public String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
			Document document = getDocument(declarationDataId);
			Node fileNode = document.getElementsByTagName(TAG_FILE).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_FILE_ID);
			return fileNameNode.getTextContent();
	}

	@Override
	public Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, declarationDataId, FormDataEvent.GET_LEVEL0);
			Document document = getDocument(declarationDataId);
            if (document == null) return null;
			Node fileNode = document.getElementsByTagName(TAG_DOCUMENT).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_DOC_DATE);
			return getFormattedDate(fileNameNode.getTextContent());
	}

	@Override
	public byte[] getXlsxData(long id, TAUserInfo userInfo) {
        declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        try {
            DeclarationData declarationData = declarationDataDao.get(id);
            if (declarationData.getXlsxDataUuid() != null && !declarationData.getXlsxDataUuid().isEmpty()){
                return getBytesFromInputstream(declarationData.getXlsxDataUuid());
            }else {
                ObjectInputStream objectInputStream = new ObjectInputStream(blobDataService.get(declarationData.getJasperPrintUuid()).getInputStream());
                JasperPrint jasperPrint = (JasperPrint)objectInputStream.readObject();
                byte[] xlsxBytes = exportXLSX(jasperPrint);
                declarationData.setXlsxDataUuid(blobDataService.create(new ByteArrayInputStream(xlsxBytes), ""));
                declarationDataDao.update(declarationData);
                return xlsxBytes;
            }
        } catch (Exception e) {
            throw new ServiceException("Не удалось извлечь объект для печати.", e);
        }
	}

	@Override
	public byte[] getPdfData(long id, TAUserInfo userInfo) {
		declarationDataAccessService.checkEvents(userInfo, id, FormDataEvent.GET_LEVEL0);
        DeclarationData declarationData = declarationDataDao.get(id);
        return getBytesFromInputstream(declarationData.getPdfDataUuid());
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
        declarationData.setXmlDataUuid(blobDataService.create(new ByteArrayInputStream(xml.getBytes()), ""));

        validateDeclaration(declarationData, logger, false);
        // Заполнение отчета и экспорт в формате PDF и XLSX
        JasperPrint jasperPrint = fillReport(xml,
                declarationTemplateService.getJasper(declarationData.getDeclarationTemplateId()));
        declarationData.setPdfDataUuid(blobDataService.create(new ByteArrayInputStream(exportPDF(jasperPrint)), ""));
        try {
            declarationData.setJasperPrintUuid(saveJPBlobData(jasperPrint));
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
        declarationDataDao.update(declarationData);
        /*declarationDataDao.setXlsxDataUuid(declarationData.getId(), blobDataService.create(new ByteArrayInputStream(exportXLSX(jasperPrint)), ""));*/
	}

    /**
     * Проверка валидности xml декларации
     * @param declarationData идентификатор данных декларации
     * @param logger логгер лог панели
     * @param isErrorFatal признак того, что операция не может быть продолжена с невалидным xml
     */
    private void validateDeclaration(DeclarationData declarationData, final Logger logger, final boolean isErrorFatal) {
        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("ru", "RU"));
        String xmlUuid = declarationData.getXmlDataUuid();
        String xml = new String(getBytesFromInputstream(xmlUuid));
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());

        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().isEmpty()) {
            InputStreamReader xsdStream = new InputStreamReader(
                    blobDataService.get(declarationTemplate.getXsdId()).getInputStream(), Charset.forName("windows-1251"));
            InputStreamReader xmlStream = new InputStreamReader(
                    new ByteArrayInputStream(xml.getBytes()), Charset.forName("windows-1251"));

            try {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new StreamSource(xsdStream));
                Validator validator = schema.newValidator();
                validator.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException e) throws SAXException {
                        logger.info(getMessage(e));
                    }

                    @Override
                    public void error(SAXParseException e) throws SAXException {
                        if (isErrorFatal){
                            logger.error(getMessage(e));
                        } else {
                            logger.warn(getMessage(e));
                        }
                    }

                    @Override
                    public void fatalError(SAXParseException e) throws SAXException {
                        if (isErrorFatal){
                            logger.error(getMessage(e));
                        } else {
                            logger.warn(getMessage(e));
                        }
                    }

                    private String getMessage(SAXParseException e) throws SAXException {
                        return String.format(e.getLocalizedMessage() + " Строка: %s; Столбец: %s",
                                e.getLineNumber(), e.getColumnNumber());
                    }
                });
                validator.validate(new StreamSource(xmlStream));
            } catch (Exception e) {
                logger.error(e);
                Locale.setDefault(oldLocale);
                throw new ServiceException(VALIDATION_ERR_MSG, logger.getEntries());
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

	private Document getDocument(long declarationDataId) {
		try {
			String xmlUuid = declarationDataDao.get(declarationDataId).getXmlDataUuid();
            if (xmlUuid == null) return null;
            String xml = new String(getBytesFromInputstream(xmlUuid));
			InputSource inputSource = new InputSource(new StringReader(xml));

			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(inputSource);
		} catch (Exception e) {
			throw new ServiceException(
					"Не удалось получить структуру документа", e);
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
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId) {
		return declarationDataDao.find(declarationTypeId, departmentId, reportPeriodId);
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
                ReportPeriod period = reportPeriodDao.get(declarationData.getReportPeriodId());

                logs.add(new LogEntry(LogLevel.ERROR, String.format(MSG_IS_EXIST_DECLARATION,
                        declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                        departmentDao.getDepartment(departmentId).getName(),
                        period.getName() + " " + period.getTaxPeriod().getYear())));
            }
        }
        return !declarationIds.isEmpty();
    }

    private byte[] getBytesFromInputstream(String blobId){
        if (blobId == null) return null;
        BlobData blobPdfData = blobDataService.get(blobId);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(blobPdfData.getInputStream(), arrayOutputStream);
        } catch (IOException e) {
            throw new ServiceException("Не удалось извлечь pdf.", e);
        }
        return arrayOutputStream.toByteArray();
    }

    @Override
    @Transactional
    public void lock(long declarationDataId, TAUserInfo userInfo) {
        lockCoreService.lock(DeclarationData.class, declarationDataId, userInfo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void unlock(final long declarationDataId, final TAUserInfo userInfo) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                lockCoreService.unlock(DeclarationData.class, declarationDataId, userInfo);
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
    }

    @Override
    public void checkLockedMe(Long declarationDataId, TAUserInfo userInfo) {
        lockCoreService.checkNoLockedAnother(DeclarationData.class, declarationDataId, userInfo);
    }
}
