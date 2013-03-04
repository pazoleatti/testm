package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.log.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.*;
import com.aplana.sbrf.taxaccounting.service.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.*;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.io.*;
import java.util.*;

/**
 * Сервис для работы с декларациями
 * @author Eugene Stetsenko
 * @author dsultanbekov
 */
@Service
public class DeclarationDataServiceImpl implements DeclarationDataService {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private DeclarationDataDao declarationDataDao;

	@Autowired
	private DeclarationDataAccessService declarationDataAccessService ;
	
	@Autowired
	private DeclarationDataScriptingService declarationDataScriptingService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Override
	public long createDeclaration(Logger logger, int declarationTemplateId, int departmentId, int userId, int reportPeriodId) {
		if (declarationDataAccessService.canCreate(userId, declarationTemplateId, departmentId, reportPeriodId)) {
			DeclarationData newDeclaration = new DeclarationData();
			newDeclaration.setDepartmentId(departmentId);
			newDeclaration.setReportPeriodId(reportPeriodId);
			newDeclaration.setAccepted(false);
			newDeclaration.setDeclarationTemplateId(declarationTemplateId);
			
			long declarationId = declarationDataDao.saveNew(newDeclaration);
			
			this.logger.debug("New declaration saved, id = " + declarationId);
			String xml = declarationDataScriptingService.create(logger, departmentId, declarationTemplateId, reportPeriodId);
			declarationDataDao.setXmlData(declarationId, xml);
			return declarationId;
		} else {
			throw new AccessDeniedException("Недостаточно прав для создания декларации с указанными параметрами");
		}
	}

	@Override
	public DeclarationData get(long declarationId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationId)) {
			DeclarationData declaration = declarationDataDao.get(declarationId);
			return declaration;
		} else {
			throw new AccessDeniedException("Недостаточно прав на просмотр данных декларации");
		}
	}

	@Override
	@Transactional
	public void delete(long declarationId, int userId) {
		if (declarationDataAccessService.canDelete(userId, declarationId)) {
			declarationDataDao.delete(declarationId);
		} else {
			throw new AccessDeniedException("Недостаточно прав на удаление декларации");
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
				throw new AccessDeniedException("Невозможно отменить принятие декларации");
			}
		}
		declarationDataDao.setAccepted(declarationId, accepted);
	}

	@Override
	public String getXmlData(long declarationId, int userId) {
		if (declarationDataAccessService.canDownloadXml(userId, declarationId)) {
			String xmlData = declarationDataDao.getXmlData(declarationId);
			return xmlData;
		} else {
			throw new AccessDeniedException("Невозможно получить xml");
		}
	}

	@Override
	public byte[] getXlsxData(long declarationId, int userId) {
		if (declarationDataAccessService.canRead(userId, declarationId)) {
			DeclarationData declaration = declarationDataDao.get(declarationId);
			byte[] jasperTemplate = declarationTemplateService.getJasper(declaration.getDeclarationTemplateId());
			String xmlData = declarationDataDao.getXmlData(declarationId);
			JasperPrint print = null;
			try {
				JRXmlDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(xmlData.getBytes()));
				print = JasperFillManager.fillReport(new ByteArrayInputStream(jasperTemplate), new HashMap<String, Object>(), dataSource);
			} catch (JRException e) {
				throw new ServiceException("Невозможно заполнить отчет");
			}
			JRXlsxExporter exporter = new JRXlsxExporter();
			ByteArrayOutputStream xls = new ByteArrayOutputStream();
			exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, xls);
			exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
			exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
			exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
			exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
			try {
				exporter.exportReport();
			} catch (JRException e) {
				throw new ServiceException("Невозможно экспортировать отчет");
			}
			return xls.toByteArray();
		} else {
			throw new AccessDeniedException("Невозможно получить xlsx, так как у пользователя нет прав на просмотр декларации");
		}
	}

	@Override
	public byte[] getPdfData(long declarationId, int userId){
		if (declarationDataAccessService.canRead(userId, declarationId)) {
			DeclarationData declaration = declarationDataDao.get(declarationId);
			byte[] jasperTemplate = declarationTemplateService.getJasper(declaration.getDeclarationTemplateId());
			String xmlData = declarationDataDao.getXmlData(declarationId);
			JasperPrint print = null;
			try {
				JRXmlDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(xmlData.getBytes()));
				print = JasperFillManager.fillReport(new ByteArrayInputStream(jasperTemplate), new HashMap<String, Object>(), dataSource);
			} catch (JRException e) {
				throw new ServiceException("Невозможно заполнить отчет");
			}
			JRPdfExporter exporter = new JRPdfExporter();
			ByteArrayOutputStream pdf = new ByteArrayOutputStream();
			exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, pdf);
			try {
				exporter.exportReport();
			} catch (JRException e) {
				throw new ServiceException("Невозможно экспортировать отчет");
			}
			return pdf.toByteArray();
		} else {
			throw new AccessDeniedException("Невозможно получить pdf, так как у пользователя нет прав на просмотр декларации");
		}
	}

	@Override
	public void refreshDeclaration(Logger logger, long declarationDataId, int userId) {
		if (declarationDataAccessService.canRefresh(userId, declarationDataId)) {
			this.logger.debug("Refreshing declaration with id = " + declarationDataId);
			DeclarationData declarationData = declarationDataDao.get(declarationDataId);
			String xml = declarationDataScriptingService.create(
				logger,
				declarationData.getDepartmentId(),
				declarationData.getDeclarationTemplateId(),
				declarationData.getReportPeriodId()
			);
			declarationDataDao.setXmlData(declarationDataId, xml);
		} else {
			throw new AccessDeniedException("Недостаточно прав для обновления указанной декларации");
		}
	}
}
