package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.service.*;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;

/**
 * Сервис для работы с декларациями
 * 
 * @author Eugene Stetsenko
 * @author dsultanbekov
 */
@Service
@Transactional(readOnly = true)
public class DeclarationDataServiceImpl implements DeclarationDataService {

	private static final Log log = LogFactory
			.getLog(DeclarationDataServiceImpl.class);

	@Autowired
	private TAUserDao userDao;

	@Autowired
	private DeclarationDataDao declarationDataDao;

	@Autowired
	private DeclarationDataAccessService declarationDataAccessService;

	@Autowired
	private DeclarationDataScriptingService declarationDataScriptingService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private LogBusinessService logBusinessService;

	public static final String TAG_FILE = "Файл";
	public static final String TAG_DOCUMENT = "Документ";
	public static final String ATTR_FILE_ID = "ИдФайл";
	public static final String ATTR_DOC_DATE = "ДатаДок";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

	@Override
	@Transactional(readOnly = false)
	public long create(Logger logger, int declarationTemplateId,
			int departmentId, int userId, int reportPeriodId) {
		if (declarationDataAccessService.canCreate(userId,
				declarationTemplateId, departmentId, reportPeriodId)) {
			DeclarationData newDeclaration = new DeclarationData();
			newDeclaration.setDepartmentId(departmentId);
			newDeclaration.setReportPeriodId(reportPeriodId);
			newDeclaration.setAccepted(false);
			newDeclaration.setDeclarationTemplateId(declarationTemplateId);

			long id = declarationDataDao.saveNew(newDeclaration);
			setDeclarationBlobs(logger, newDeclaration, new Date());
			logBusinessService.addLogBusiness(null, id, userDao.getUser(userId), FormDataEvent.CREATE, null);
			return id;
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для создания декларации с указанными параметрами");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void reCreate(Logger logger, long id, int userId,
			Date docDate) {
		if (declarationDataAccessService.canRefresh(userId, id)) {
			DeclarationData declarationData = declarationDataDao.get(id);
			setDeclarationBlobs(logger, declarationData, docDate);
			logBusinessService.addLogBusiness(null, id, userDao.getUser(userId), FormDataEvent.SAVE, null);
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
	public void delete(long id, int userId) {
		if (declarationDataAccessService.canDelete(userId, id)) {
			declarationDataDao.delete(id);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на удаление декларации");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void setAccepted(Logger logger, long id, boolean accepted, int userId) {
		if (accepted) {
			if (!declarationDataAccessService.canAccept(userId, id)) {
				throw new AccessDeniedException(
						"Недостаточно прав для принятия декларации");
			}
			log.debug("Accept declaration, id = " + id);
			DeclarationData declarationData = declarationDataDao.get(id);
			declarationData.setAccepted(true);
			declarationDataScriptingService.accept(logger, declarationData);
			if (logger.containsLevel(LogLevel.ERROR)) {
				throw new ServiceLoggerException(
						"Есть ошибки в скрипте принятия декларации",
						logger.getEntries());
			}
		} else {
			if (!declarationDataAccessService.canReject(userId, id)) {
				throw new AccessDeniedException(
						"Недостаточно прав для отмены принятия декларации");
			}
		}
		declarationDataDao.setAccepted(id, accepted);
		if (accepted) {
			logBusinessService.addLogBusiness(null, id, userDao.getUser(userId), FormDataEvent.MOVE_CREATED_TO_APPROVED, null);
		} else {

		}

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
			DeclarationData declarationData, Date docDate) {

		// Генерация и сохранение XML
		String xml = declarationDataScriptingService.create(logger,
				declarationData, docDate);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Есть ошибки в скрипте создания декларации",
					logger.getEntries());
		}
		declarationDataDao.setXmlData(declarationData.getId(), xml);

		// Заполнение отчета и экспорт в формате PDF и XLSX
		JasperPrint jasperPrint = fillReport(xml,
				declarationTemplateService.getJasper(declarationData
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
