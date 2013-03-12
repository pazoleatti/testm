package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

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
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;

/**
 * Сервис для работы с декларациями
 * 
 * @author Eugene Stetsenko
 * @author dsultanbekov
 */
@Service
public class DeclarationDataServiceImpl implements DeclarationDataService {

	private static final Log log = LogFactory
			.getLog(DeclarationDataServiceImpl.class);

	@Autowired
	private DeclarationDataDao declarationDataDao;

	@Autowired
	private DeclarationDataAccessService declarationDataAccessService;

	@Autowired
	private DeclarationDataScriptingService declarationDataScriptingService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Override
	public long createDeclaration(Logger logger, int declarationTemplateId,
			int departmentId, int userId, int reportPeriodId) {
		if (declarationDataAccessService.canCreate(userId,
				declarationTemplateId, departmentId, reportPeriodId)) {
			DeclarationData newDeclaration = new DeclarationData();
			newDeclaration.setDepartmentId(departmentId);
			newDeclaration.setReportPeriodId(reportPeriodId);
			newDeclaration.setAccepted(false);
			newDeclaration.setDeclarationTemplateId(declarationTemplateId);

			long declarationId = declarationDataDao.saveNew(newDeclaration);

			log.debug("New declaration saved, id = " + declarationId);
			String xml = declarationDataScriptingService.create(logger,
					departmentId, declarationTemplateId, reportPeriodId);
			declarationDataDao.setXmlData(declarationId, xml);
			return declarationId;
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для создания декларации с указанными параметрами");
		}
	}

	@Override
	public DeclarationData get(long declarationId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationId)) {
			return declarationDataDao.get(declarationId);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на просмотр данных декларации");
		}
	}

	@Override
	@Transactional
	public void delete(long declarationId, int userId) {
		if (declarationDataAccessService.canDelete(userId, declarationId)) {
			declarationDataDao.delete(declarationId);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на удаление декларации");
		}
	}

	@Override
	public void setAccepted(long declarationId, boolean accepted, int userId) {
		if (accepted) {
			if (!declarationDataAccessService.canAccept(userId, declarationId)) {
				throw new AccessDeniedException("Невозможно принять декларацию");
			}
		} else {
			if (!declarationDataAccessService.canReject(userId, declarationId)) {
				throw new AccessDeniedException(
						"Невозможно отменить принятие декларации");
			}
		}
		declarationDataDao.setAccepted(declarationId, accepted);
	}

	@Override
	public String getXmlData(long declarationId, int userId) {
		if (declarationDataAccessService.canDownloadXml(userId, declarationId)) {
			return declarationDataDao.getXmlData(declarationId);
		} else {
			throw new AccessDeniedException("Невозможно получить xml");
		}
	}

	@Override
	public String getXmlDataFileName(int declarationDataId, int userId) {

		final String ATTR_FILE_ID = "ИдФайл";
		final String TAG_FILE = "Файл";

		if (declarationDataAccessService.canRead(userId, declarationDataId)) {
			String xml = declarationDataDao.getXmlData(declarationDataId);
			InputSource inputSource = new InputSource(new StringReader(xml));
			Document document;
			try {
				document = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(inputSource);
			} catch (Exception e) {
				throw new ServiceException(
						"Неудалось распарсить документ в формате законодателя");
			}
			Node fileNode = document.getElementsByTagName(TAG_FILE).item(0);
			NamedNodeMap attributes = fileNode.getAttributes();
			Node fileNameNode = attributes.getNamedItem(ATTR_FILE_ID);
			return fileNameNode.getTextContent();
		} else {
			throw new AccessDeniedException("Невозможно получить xml");
		}
	}

	@Override
	public byte[] getXlsxData(long declarationId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationId)) {
			DeclarationData declaration = declarationDataDao.get(declarationId);
			byte[] jasperTemplate = declarationTemplateService
					.getJasper(declaration.getDeclarationTemplateId());
			String xmlData = declarationDataDao.getXmlData(declarationId);
			JasperPrint print = getJasperPrint(xmlData, jasperTemplate);
			JRXlsxExporter exporter = new JRXlsxExporter();
			ByteArrayOutputStream xls = new ByteArrayOutputStream();
			exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, xls);
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
			try {
				exporter.exportReport();
			} catch (JRException e) {
				log.error(e.getMessage(), e);
				throw new ServiceException("Невозможно экспортировать отчет");
			}
			return xls.toByteArray();
		} else {
			throw new AccessDeniedException(
					"Невозможно получить xlsx, так как у пользователя нет прав на просмотр декларации");
		}
	}

	@Override
	public byte[] getPdfData(long declarationId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationId)) {
			DeclarationData declaration = declarationDataDao.get(declarationId);
			byte[] jasperTemplate = declarationTemplateService
					.getJasper(declaration.getDeclarationTemplateId());
			String xmlData = declarationDataDao.getXmlData(declarationId);
			JasperPrint print = getJasperPrint(xmlData, jasperTemplate);
			JRPdfExporter exporter = new JRPdfExporter();
			ByteArrayOutputStream pdf = new ByteArrayOutputStream();
			exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, pdf);
			try {
				exporter.exportReport();
			} catch (JRException e) {
				log.error(e.getMessage(), e);
				throw new ServiceException("Невозможно экспортировать отчет");
			}
			return pdf.toByteArray();
		} else {
			throw new AccessDeniedException(
					"Невозможно получить pdf, так как у пользователя нет прав на просмотр декларации");
		}
	}

	@Override
	public void refreshDeclaration(Logger logger, long declarationDataId,
			int userId) {
		if (declarationDataAccessService.canRefresh(userId, declarationDataId)) {
			log.debug("Refreshing declaration with id = "
					+ declarationDataId);
			DeclarationData declarationData = declarationDataDao
					.get(declarationDataId);
			String xml = declarationDataScriptingService.create(logger,
					declarationData.getDepartmentId(),
					declarationData.getDeclarationTemplateId(),
					declarationData.getReportPeriodId());
			declarationDataDao.setXmlData(declarationDataId, xml);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для обновления указанной декларации");
		}
	}

	private JasperPrint getJasperPrint(String xml, byte[] jasperTemplate) {
		try {
			InputSource inputSource = new InputSource(new StringReader(xml));
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(inputSource);
			JRXmlDataSource dataSource = new JRXmlDataSource(document);
			return JasperFillManager.fillReport(new ByteArrayInputStream(
					jasperTemplate), new HashMap<String, Object>(), dataSource);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("Невозможно заполнить отчет");
		}
	}

}
