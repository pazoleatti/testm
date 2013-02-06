package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * Сервис для работы с декларациями
 * @author Eugene Stetsenko
 */
@Service
public class DeclarationServiceImpl implements DeclarationService {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	DeclarationDao declarationDao;

	@Autowired
	DeclarationAccessService declarationAccessService ;

	@Autowired
	DeclarationTemplateService declarationTemplateService;


	// TODO Добавить declarationTemplateId
	@Override
	public long createDeclaration(Logger logger, TaxType taxType, int departmentId, int userId, int reportPeriodId) {
		if (declarationAccessService.canCreate(taxType, departmentId, reportPeriodId)) {
			Declaration newDeclaration = new Declaration();
			newDeclaration.setDepartmentId(departmentId);
			newDeclaration.setDeclarationTemplateId(1);
			newDeclaration.setReportPeriodId(reportPeriodId);
			newDeclaration.setAccepted(false);
			return declarationDao.saveNew(newDeclaration);
		} else {
			throw new AccessDeniedException("Недостаточно прав для создания декларации с указанными параметрами");
		}
	}

	@Override
	public Declaration get(long declarationId, int userId) {
		if (declarationAccessService.canRead(userId, declarationId)) {
			Declaration declaration = declarationDao.get(declarationId);
			return declaration;
		} else {
			throw new AccessDeniedException("Недостаточно прав на просмотр данных декларации");
		}
	}

	@Override
	public void setAccepted(long declarationId, boolean accepted, int userId) {
		if (accepted) {
			if (!declarationAccessService.canAccept(userId, declarationId)) {
				throw new AccessDeniedException("Невозможно принять декларацию");
			}
		} else {
			if (!declarationAccessService.canReject(userId, declarationId)) {
				throw new AccessDeniedException("Невозможно отменить принятие декларации");
			}
		}
		declarationDao.setAccepted(declarationId, accepted);
	}

	@Override
	public String getXmlData(long declarationId, int userId) {
		if (declarationAccessService.canRead(userId, declarationId)) {
			String xmlData = declarationDao.getXmlData(declarationId);
			return xmlData;
		} else {
			throw new AccessDeniedException("Невозможно получить xml");
		}
	}

	@Override
	public byte[] getXlsxData(long declarationId, int userId) {
		if (declarationAccessService.canRead(userId, declarationId)) {
			Declaration declaration = declarationDao.get(declarationId);
			byte[] jasperTemplate = declarationTemplateService.getJasper(declaration.getDeclarationTemplateId());
			String xmlData = declarationDao.getXmlData(declarationId);
			JasperPrint print = null;
			try {
				JRXmlDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(xmlData.getBytes()));
				print = JasperFillManager.fillReport(new ByteArrayInputStream(jasperTemplate), new HashMap(), dataSource);
			} catch (JRException e) {
				throw new ServiceException("Невозможно заполнить отчет");
			}
			JRXlsExporter exporter = new JRXlsExporter();
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
			throw new AccessDeniedException("Невозможно получить xlsx");
		}
	}
}
