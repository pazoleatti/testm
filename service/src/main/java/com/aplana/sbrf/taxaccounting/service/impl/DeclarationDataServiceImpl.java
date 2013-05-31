package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;

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

	@Autowired
	private TAUserDao userDao;

	@Autowired
	private DeclarationDataDao declarationDataDao;

	@Autowired
	private DeclarationDataAccessService declarationDataAccessService;

	@Autowired
	private DeclarationDataScriptingService declarationDataScriptingService;
	
	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;
	
	@Autowired
	private DepartmentFormTypeDao departmentFormTypeDao;
	
	@Autowired
	private FormDataDao formDataDao;
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private FormTypeDao formTypeDao;

	@Autowired
	private LogBusinessService logBusinessService;

	@Autowired
	private AuditService auditService;

	public static final String TAG_FILE = "Файл";
	public static final String TAG_DOCUMENT = "Документ";
	public static final String ATTR_FILE_ID = "ИдФайл";
	public static final String ATTR_DOC_DATE = "ДатаДок";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

	@Override
	@Transactional(readOnly = false)
	public long create(Logger logger, int declarationTemplateId,
			int departmentId, String ip, int userId, int reportPeriodId) {
		if (declarationDataAccessService.canCreate(userId,
				declarationTemplateId, departmentId, reportPeriodId)) {
			
			DeclarationData newDeclaration = new DeclarationData();
			newDeclaration.setDepartmentId(departmentId);
			newDeclaration.setReportPeriodId(reportPeriodId);
			newDeclaration.setAccepted(false);
			newDeclaration.setDeclarationTemplateId(declarationTemplateId);
			long id = declarationDataDao.saveNew(newDeclaration);
			TAUser user = userDao.getUser(userId);
			
			setDeclarationBlobs(logger, newDeclaration, new Date(), userId);
			logBusinessService.add(null, id, user, FormDataEvent.CREATE, null);
			auditService.add(ip, FormDataEvent.CREATE , user, newDeclaration.getDepartmentId(),
					newDeclaration.getReportPeriodId(),
					declarationTemplateDao.get(newDeclaration.getDeclarationTemplateId()).getDeclarationType().getId(),
					null, null, null);
			return id;
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для создания декларации с указанными параметрами");
		}
	}
	
	

	@Override
	@Transactional(readOnly = false)
	public void reCreate(Logger logger, long id, String ip, int userId,
			Date docDate) {
		if (declarationDataAccessService.canRefresh(userId, id)) {
			DeclarationData declarationData = declarationDataDao.get(id);
			setDeclarationBlobs(logger, declarationData, docDate, userId);
			TAUser user = userDao.getUser(userId);
			logBusinessService.add(null, id, user, FormDataEvent.SAVE, null);
			auditService.add(ip, FormDataEvent.SAVE , user, declarationData.getDepartmentId(),
					declarationData.getReportPeriodId(),
					declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getDeclarationType().getId(),
					null, null, null);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для обновления указанной декларации");
		}
	}

	@Override
	public DeclarationData get(long id, int userId) {
		if (declarationDataAccessService.canRead(userId, id)) {
			return declarationDataDao.get(id);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на просмотр данных декларации");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void delete(long id, String ip, int userId) {
		if (declarationDataAccessService.canDelete(userId, id)) {
			DeclarationData declarationData = declarationDataDao.get(id);
			TAUser user = userDao.getUser(userId);

			declarationDataDao.delete(id);

			auditService.add(ip, FormDataEvent.DELETE , user, declarationData.getDepartmentId(),
					declarationData.getReportPeriodId(),
					declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getDeclarationType().getId(),
					null, null, null);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на удаление декларации");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void setAccepted(Logger logger, long id, boolean accepted, String ip, int userId) {
		
		TAUser user = userDao.getUser(userId);
		// TODO (sgoryachkin) Это 2 метода должо быть
		if (accepted) {
			declarationDataAccessService.checkEvents(userId, id, FormDataEvent.MOVE_CREATED_TO_ACCEPTED);
			
			DeclarationData declarationData  = declarationDataDao.get(id);
			declarationData.setAccepted(true);
			
			Map<String, Object> exchangeParams = new HashMap<String, Object>();
			declarationDataScriptingService.executeScript(user, declarationData, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, logger, exchangeParams);
			
			Integer declarationTypeId = declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getDeclarationType().getId();
			logBusinessService.add(null, id, user, FormDataEvent.MOVE_CREATED_TO_ACCEPTED, null);
			auditService.add(ip, FormDataEvent.MOVE_CREATED_TO_ACCEPTED , user, declarationData.getDepartmentId(),
					declarationData.getReportPeriodId(), declarationTypeId, null, null, null);
		} else {
			declarationDataAccessService.checkEvents(userId, id, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);
			
			DeclarationData declarationData  = declarationDataDao.get(id);
			declarationData.setAccepted(false);
			
			Map<String, Object> exchangeParams = new HashMap<String, Object>();		
			declarationDataScriptingService.executeScript(null, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger, exchangeParams);
			
			Integer declarationTypeId = declarationTemplateDao.get(declarationData.getDeclarationTemplateId()).getDeclarationType().getId();
			logBusinessService.add(null, id, user, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, null);
			auditService.add(ip, FormDataEvent.MOVE_ACCEPTED_TO_CREATED , user, declarationData.getDepartmentId(),
					declarationData.getReportPeriodId(), declarationTypeId, null, null, null);
		
		}
		declarationDataDao.setAccepted(id, accepted);

	}

	@Override
	public String getXmlData(long declarationId, int userId) {
		if (declarationDataAccessService.canDownloadXml(userId, declarationId)) {
			return declarationDataDao.getXmlData(declarationId);
		} else {
			throw new AccessDeniedException(
					"Нет прав на получение декларации в формате законодателя (xml)");
		}
	}

	@Override
	public String getXmlDataFileName(long declarationDataId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationDataId)) {
			Document document = getDocument(declarationDataId);
			Node fileNode = document.getElementsByTagName(TAG_FILE).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_FILE_ID);
			return fileNameNode.getTextContent();
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на просмотр данных декларации");
		}
	}

	@Override
	public Date getXmlDataDocDate(long declarationDataId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationDataId)) {
			Document document = getDocument(declarationDataId);
			Node fileNode = document.getElementsByTagName(TAG_DOCUMENT).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_DOC_DATE);
			return getFormattedDate(fileNameNode.getTextContent());
		} else {
			throw new AccessDeniedException("Невозможно получить xml");
		}
	}

	@Override
	public byte[] getXlsxData(long id, int userId) {
		if (declarationDataAccessService.canRead(userId, id)) {
			return declarationDataDao.getXlsxData(id);
		} else {
			throw new AccessDeniedException("Нет прав на просмотр декларации");
		}
	}

	@Override
	public byte[] getPdfData(long id, int userId) {
		if (declarationDataAccessService.canRead(userId, id)) {
			return declarationDataDao.getPdfData(id);
		} else {
			throw new AccessDeniedException("Нет прав на просмотр декларации");
		}
	}
	
	private void setDeclarationBlobs(Logger logger,
			DeclarationData declarationData, Date docDate, int userId) {
		
		Map<String, Object> exchangeParams = new HashMap<String, Object>();
		exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, docDate);
		StringWriter writer = new StringWriter();
		exchangeParams.put(DeclarationDataScriptParams.XML, writer);
			
		declarationDataScriptingService.executeScript(userDao.getUser(userId), declarationData, FormDataEvent.CREATE, logger, exchangeParams);
	
		String xml = XML_HEADER.concat(writer.toString());
		
		
		declarationDataDao.setXmlData(declarationData.getId(), xml);

		// Заполнение отчета и экспорт в формате PDF и XLSX
		JasperPrint jasperPrint = fillReport(xml,
				declarationTemplateDao.getJasper(declarationData
						.getDeclarationTemplateId()));
		declarationDataDao.setPdfData(declarationData.getId(),
				exportPDF(jasperPrint));
		declarationDataDao.setXlsxData(declarationData.getId(),
				exportXLSX(jasperPrint));

	}

	private static JasperPrint fillReport(String xml, byte[] jasperTemplate) {
		try {
			InputSource inputSource = new InputSource(new StringReader(xml));
			Document document = JRXmlUtils.parse(inputSource);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT,
					document);

			return JasperFillManager.fillReport(new ByteArrayInputStream(
					jasperTemplate), params);

		} catch (Exception e) {
			throw new ServiceException("Невозможно заполнить отчет", e);
		}
	}

	private Document getDocument(long declarationDataId) {
		try {
			String xml = declarationDataDao.getXmlData(declarationDataId);
			InputSource inputSource = new InputSource(new StringReader(xml));

			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(inputSource);
		} catch (Exception e) {
			throw new ServiceException(
					"Неудалось получить структуру документа", e);
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
					Boolean.TRUE);

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
	

	
	
}
